package sayys.depthsupdate.mixin;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
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
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.AquiferGenerator;
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

    @Unique
    private AquiferGenerator depthsupdate$aquiferGenerator;

    @Redirect(
        method = "provideChunk(II)Lnet/minecraft/world/chunk/Chunk;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/gen/IChunkGenerator;generateChunk(II)Lnet/minecraft/world/chunk/Chunk;"
        )
    )
    private Chunk depthsupdate$onGenerateChunk(IChunkGenerator generator, int x, int z) {
        Chunk chunk = generator.generateChunk(x, z);

        if (generator.getClass() == ChunkGeneratorOverworld.class) {
            return chunk;
        }

        if (generator instanceof ChunkGeneratorFlat || generator instanceof ChunkGeneratorDebug) {
            return chunk;
        }

        if (!DepthsUpdateConfig.heightExtension.extendCustomWorldTypes) {
            return chunk;
        }

        if (!HeightManager.isExtended(this.world)) {
            return chunk;
        }

        if (chunk == null) {
            return chunk;
        }

        HeightContext ctx = HeightManager.get(this.world);
        int minY = ctx.minY();

        if (minY >= 0) {
            return chunk;
        }

        if (this.depthsupdate$fillRandom == null) {
            this.depthsupdate$fillRandom = new Random();
        }

        this.depthsupdate$fillRandom.setSeed((long) x * 341873128712L + (long) z * 132897987541L);

        IBlockState stone = Blocks.STONE.getDefaultState();
        IBlockState deepslate = BlockUtils.getDeepslateBlockState();
        IBlockState bedrock = Blocks.BEDROCK.getDefaultState();

        int deepslateMaxY = DepthsUpdateConfig.deepslateMaxY;
        int transitionRange = DepthsUpdateConfig.deepslateTransitionRange;
        int fullDeepslateY = deepslateMaxY - transitionRange;
        int fillMaxY = Math.max(4, deepslateMaxY);

        ExtendedBlockStorage[] storageArrays = chunk.getBlockStorageArray();
        boolean hasSkyLight = this.world.provider.hasSkyLight();

        for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
                for (int by = minY; by <= fillMaxY; by++) {
                    IBlockState state;

                    if (by <= minY + this.depthsupdate$fillRandom.nextInt(5)) {
                        state = bedrock;
                    } else if (by <= fullDeepslateY) {
                        state = deepslate;
                    } else if (by < deepslateMaxY) {
                        double chance = (double) (deepslateMaxY - by) / (double) transitionRange;
                        if (this.depthsupdate$fillRandom.nextDouble() < chance) {
                            state = deepslate;
                        } else if (by < 0) {
                            state = stone;
                        } else {
                            continue;
                        }
                    } else if (by < 0) {
                        state = stone;
                    } else {
                        // check for vanilla bedrock replacement
                        if (by <= 4) {
                            int storageIdx = ctx.toStorageIndex(by);

                            if (storageIdx >= 0 && storageIdx < storageArrays.length) {
                                ExtendedBlockStorage section = storageArrays[storageIdx];
                                if (section != Chunk.NULL_BLOCK_STORAGE
                                        && section.get(bx, by & 15, bz).getBlock() == Blocks.BEDROCK) {
                                    section.set(bx, by & 15, bz, stone);
                                }
                            }
                        }

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

        this.depthsupdate$noiseCaveGenerator.generate(x, z, adapter);

        if (DepthsUpdateConfig.aquifers.enableAquifers) {
            if (this.depthsupdate$aquiferGenerator == null) {
                this.depthsupdate$aquiferGenerator = new AquiferGenerator(this.world);
            }

            this.depthsupdate$aquiferGenerator.generate(x, z, adapter);
        }

        chunk.generateSkylightMap();

        return chunk;
    }
}
