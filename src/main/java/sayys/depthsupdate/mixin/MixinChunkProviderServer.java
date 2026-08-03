package sayys.depthsupdate.mixin;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.BedrockFilter;
import sayys.depthsupdate.core.DeepFill;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.ChunkPrimerAdapter;
import sayys.depthsupdate.world.generation.noise.CaveNoiseGenerator;
import sayys.depthsupdate.world.generation.river.UndergroundRiverGenerator;

/**
 * Global hook for extending custom world types
 * that use their own IChunkGenerator instead of ChunkGeneratorOverworld.
 */
@Mixin(ChunkProviderServer.class)
public class MixinChunkProviderServer {
    @Shadow
    @Final
    private IChunkGenerator chunkGenerator;

    @Shadow
    @Final
    private WorldServer world;

    @Unique
    private Random depthsupdate$fillRandom;

    @Unique
    private UndergroundRiverGenerator depthsupdate$riverGenerator;

    @Unique
    private CaveNoiseGenerator depthsupdate$noiseCaveGenerator;

    @Redirect(
        method = "provideChunk(II)Lnet/minecraft/world/chunk/Chunk;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/gen/IChunkGenerator;generateChunk(II)Lnet/minecraft/world/chunk/Chunk;"
        )
    )
    private Chunk depthsupdate$onGenerateChunk(IChunkGenerator generator, int x, int z) {
        boolean vanillaOverworld = generator.getClass() == ChunkGeneratorOverworld.class;
        boolean flatOrDebug = generator instanceof ChunkGeneratorFlat || generator instanceof ChunkGeneratorDebug;
        boolean deepWorld = !flatOrDebug
                && HeightManager.isExtended(this.world)
                && HeightManager.get(this.world).minY() < 0;
        boolean fillCustom = deepWorld && !vanillaOverworld
                && DepthsUpdateConfig.heightExtension.extendCustomWorldTypes;

        boolean filterBedrock = deepWorld && (vanillaOverworld || fillCustom);

        Chunk chunk;

        if (filterBedrock) {
            BedrockFilter.begin();
        }

        try {
            chunk = generator.generateChunk(x, z);
        } finally {
            if (filterBedrock) {
                BedrockFilter.end();
            }
        }

        if (!fillCustom || chunk == null) {
            return chunk;
        }

        HeightContext ctx = HeightManager.get(this.world);
        int minY = ctx.minY();

        if (this.depthsupdate$fillRandom == null) {
            this.depthsupdate$fillRandom = new Random();
        }

        this.depthsupdate$fillRandom.setSeed((long) x * 341873128712L + (long) z * 132897987541L);

        IBlockState stone = Blocks.STONE.getDefaultState();
        IBlockState deepslate = BlockUtils.getDeepslateBlockState();
        IBlockState bedrock = Blocks.BEDROCK.getDefaultState();

        int fillMaxY = Math.max(4, DepthsUpdateConfig.deepslateMaxY);

        ExtendedBlockStorage[] storageArrays = chunk.getBlockStorageArray();
        boolean hasSkyLight = this.world.provider.hasSkyLight();

        for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
                for (int by = minY; by <= fillMaxY; by++) {
                    // Backstop for generators that write bedrock without going
                    // through ChunkPrimer. Runs before the carve so caves cut
                    // through the converted stone.
                    if (by >= 0 && by <= 4) {
                        int storageIdx = ctx.toStorageIndex(by);

                        if (storageIdx >= 0 && storageIdx < storageArrays.length) {
                            ExtendedBlockStorage section = storageArrays[storageIdx];

                            if (section != Chunk.NULL_BLOCK_STORAGE
                                    && section.get(bx, by & 15, bz).getBlock() == Blocks.BEDROCK) {
                                section.set(bx, by & 15, bz, stone);
                            }
                        }
                    }

                    IBlockState state = DeepFill.bandAt(by, minY, this.depthsupdate$fillRandom, bedrock, deepslate, stone);

                    if (state == null) {
                        continue;
                    }

                    int storageIdx = ctx.toStorageIndex(by);

                    if (storageIdx < 0 || storageIdx >= storageArrays.length) {
                        continue;
                    }

                    ExtendedBlockStorage section = storageArrays[storageIdx];

                    if (section == Chunk.NULL_BLOCK_STORAGE) {
                        section = new ExtendedBlockStorage(by >> 4 << 4, hasSkyLight);
                        storageArrays[storageIdx] = section;
                    }

                    // Above zero this chunk holds finished terrain, caves
                    // included; the transition may only recolor stone there.
                    if (by >= 0 && section.get(bx, by & 15, bz).getBlock() != Blocks.STONE) {
                        continue;
                    }

                    section.set(bx, by & 15, bz, state);
                }
            }
        }

        ChunkPrimerAdapter adapter = new ChunkPrimerAdapter(chunk, ctx);

        if (DepthsUpdateConfig.generateUndergroundRivers) {
            if (this.depthsupdate$riverGenerator == null) {
                this.depthsupdate$riverGenerator = new UndergroundRiverGenerator(this.world);
            }

            this.depthsupdate$riverGenerator.generate(x, z, adapter);
        }

        if (this.depthsupdate$noiseCaveGenerator == null) {
            this.depthsupdate$noiseCaveGenerator = new CaveNoiseGenerator(this.world);
        }

        // Chunk biome bytes use index z << 4 | x, which is the same flat
        // layout the carve expects for column (x, z).
        byte[] biomeIds = chunk.getBiomeArray();
        Biome[] biomes = new Biome[biomeIds.length];

        for (int i = 0; i < biomeIds.length; i++) {
            biomes[i] = Biome.getBiome(biomeIds[i] & 255, Biomes.PLAINS);
        }

        this.depthsupdate$noiseCaveGenerator.generate(x, z, adapter, biomes);

        chunk.generateSkylightMap();

        return chunk;
    }
}
