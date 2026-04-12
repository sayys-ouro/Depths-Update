package sayys.depthsupdate.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.AquiferGenerator;
import sayys.depthsupdate.world.generation.NoiseBasedChunkGenerator;
import sayys.depthsupdate.world.generation.river.UndergroundRiverGenerator;

@Mixin(ChunkGeneratorOverworld.class)
public abstract class MixinChunkGeneratorOverworld {
    @Shadow
    private World world;

    @Unique
    private UndergroundRiverGenerator depthsupdate$riverGenerator;

    @Unique
    private sayys.depthsupdate.world.generation.noise.CaveNoiseGenerator depthsupdate$noiseCaveGenerator;

    @Unique
    private AquiferGenerator depthsupdate$aquiferGenerator;

    @Unique
    private NoiseBasedChunkGenerator depthsupdate$modernGenerator;

    /** Stores climate-mapped biomes from generateTerrain for applying to the Chunk later. */
    @Unique
    private Biome[] depthsupdate$lastClimateBiomes;

    @Inject(method = "setBlocksInChunk", at = @At("RETURN"))
    private void depthsupdate$fillDeepUnderground(int x, int z, ChunkPrimer primer, CallbackInfo ci) {
        // If modern world gen is enabled, use the new density function pipeline
        if (DepthsUpdateConfig.modernWorldGen.enableModernWorldGen) {
            if (this.depthsupdate$modernGenerator == null) {
                this.depthsupdate$modernGenerator = new NoiseBasedChunkGenerator(this.world);
            }

            // Get biomes for this chunk — generateTerrain will overwrite with climate biomes
            Biome[] biomes = this.world.getBiomeProvider().getBiomes(null, x * 16, z * 16, 16, 16);
            this.depthsupdate$modernGenerator.generateTerrain(x, z, primer, biomes);
            this.depthsupdate$lastClimateBiomes = biomes;
            return; // Skip legacy generation
        }

        // Legacy generation path
        HeightContext ctx = HeightManager.get(this.world);
        int minY = ctx.minY();
        IBlockState stone = Blocks.STONE.getDefaultState();
        IBlockState deepslate = BlockUtils.getDeepslateBlockState();

        int maxY = DepthsUpdateConfig.deepslateMaxY;
        int transitionRange = DepthsUpdateConfig.deepslateTransitionRange;
        int fullDeepslateY = maxY - transitionRange;

        for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
                for (int by = minY; by <= Math.max(0, maxY); by++) {
                    if (by <= minY + this.world.rand.nextInt(5)) {
                        primer.setBlockState(bx, by, bz, Blocks.BEDROCK.getDefaultState());
                    } else if (by <= fullDeepslateY) {
                        primer.setBlockState(bx, by, bz, deepslate);
                    } else if (by < maxY) {
                        double chance = (double) (maxY - by) / (double) transitionRange;

                        if (this.world.rand.nextDouble() < chance) {
                            primer.setBlockState(bx, by, bz, deepslate);
                        } else if (by < 0) {
                            primer.setBlockState(bx, by, bz, stone);
                        }
                    } else if (by < 0) {
                        primer.setBlockState(bx, by, bz, stone);
                    }
                }
            }
        }

        if (DepthsUpdateConfig.generateUndergroundRivers) {
            if (this.depthsupdate$riverGenerator == null) {
                this.depthsupdate$riverGenerator = new UndergroundRiverGenerator(this.world);
            }

            this.depthsupdate$riverGenerator.generate(x, z, primer);
        }
        if (this.depthsupdate$noiseCaveGenerator == null) {
            this.depthsupdate$noiseCaveGenerator = new sayys.depthsupdate.world.generation.noise.CaveNoiseGenerator(this.world);
        }

        this.depthsupdate$noiseCaveGenerator.generate(x, z, primer);

        if (DepthsUpdateConfig.aquifers.enableAquifers) {
            if (this.depthsupdate$aquiferGenerator == null) {
                this.depthsupdate$aquiferGenerator = new AquiferGenerator(this.world);
            }

            this.depthsupdate$aquiferGenerator.generate(x, z, primer);
        }
    }

    /**
     * After the Chunk is fully constructed, overwrite its biome array with our
     * climate-mapped biomes so F3 display and mob spawning use correct biomes.
     */
    @Inject(method = "generateChunk", at = @At("RETURN"))
    private void depthsupdate$fixChunkBiomes(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        if (DepthsUpdateConfig.modernWorldGen.enableModernWorldGen
                && this.depthsupdate$lastClimateBiomes != null) {
            Chunk chunk = cir.getReturnValue();
            byte[] biomeArray = chunk.getBiomeArray();
            for (int i = 0; i < 256 && i < this.depthsupdate$lastClimateBiomes.length; i++) {
                Biome biome = this.depthsupdate$lastClimateBiomes[i];
                if (biome != null) {
                    biomeArray[i] = (byte) (Biome.getIdForBiome(biome) & 0xFF);
                }
            }
            this.depthsupdate$lastClimateBiomes = null;
        }
    }
}
