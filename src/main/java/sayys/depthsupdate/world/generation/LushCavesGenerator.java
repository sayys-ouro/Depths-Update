package sayys.depthsupdate.world.generation;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.block.BlockCaveVines;
import sayys.depthsupdate.registry.RegistryHandler;

public class LushCavesGenerator implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!DepthsUpdateConfig.lushCaves.enableLushCaves) {
            return;
        }

        if (world.provider.getDimension() != 0) {
            return;
        }

        if (random.nextInt(DepthsUpdateConfig.lushCaves.lushCavesRarity) == 0) {
            int x = chunkX * 16 + random.nextInt(16);
            int minY = DepthsUpdateConfig.lushCaves.lushCavesMinY;
            int maxY = DepthsUpdateConfig.lushCaves.lushCavesMaxY;
            int yRange = maxY - minY + 1;

            if (yRange <= 0) {
                yRange = 1;
            };

            int y = minY + random.nextInt(yRange);
            int z = chunkZ * 16 + random.nextInt(16);

            BlockPos centerPos = new BlockPos(x, y, z);
            generateLushCave(world, random, centerPos);
        }
    }

    private void generateLushCave(World world, @NonNull Random random, BlockPos center) {
        int radiusX = DepthsUpdateConfig.lushCaves.lushCavesRadiusBase + random.nextInt(Math.max(1, DepthsUpdateConfig.lushCaves.lushCavesRadiusVariation));
        int radiusY = DepthsUpdateConfig.lushCaves.lushCavesHeightBase + random.nextInt(Math.max(1, DepthsUpdateConfig.lushCaves.lushCavesHeightVariation));
        int radiusZ = DepthsUpdateConfig.lushCaves.lushCavesRadiusBase + random.nextInt(Math.max(1, DepthsUpdateConfig.lushCaves.lushCavesRadiusVariation));

        double radiusX2 = radiusX * radiusX;
        double radiusY2 = radiusY * radiusY;
        double radiusZ2 = radiusZ * radiusZ;

        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    double distX = x * x;
                    double distY = y * y;
                    double distZ = z * z;

                    if (distX / radiusX2 + distY / radiusY2 + distZ / radiusZ2 <= 1) {
                        BlockPos pos = center.add(x, y, z);
                        decorateCaveBlock(world, random, pos);
                    }
                }
            }
        }
    }

    private void decorateCaveBlock(@NonNull World world, Random random, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);

        if (!state.getBlock().isAir(state, world, pos) && state.getMaterial() != net.minecraft.block.material.Material.WATER) {
            return;
        }

        IBlockState stateDown = world.getBlockState(pos.down());
        IBlockState stateUp = world.getBlockState(pos.up());
        Block blockDown = stateDown.getBlock();
        Block blockUp = stateUp.getBlock();

        boolean isFloorGround = (blockDown == Blocks.STONE || blockDown == Blocks.DIRT || blockDown == RegistryHandler.deepslate);
        boolean isCeilingGround = (blockUp == Blocks.STONE || blockUp == Blocks.DIRT || blockUp == RegistryHandler.deepslate);

        if (isFloorGround) {
            decorateFloor(world, random, pos);
        } else if (isCeilingGround) {
            decorateCeiling(world, random, pos);
        } else {
            decorateWall(world, random, pos);
        }
    }

    private void decorateFloor(World world, @NonNull Random random, @NonNull BlockPos pos) {
        BlockPos floorPos = pos.down();
        int floorType = random.nextInt(100);

        if (floorType < 70) {
            world.setBlockState(floorPos, RegistryHandler.moss_block.getDefaultState(), 2);

            if (world.isAirBlock(pos)) {
                int decType = random.nextInt(100);

                if (decType < 20) {
                    world.setBlockState(pos, RegistryHandler.moss_carpet.getDefaultState(), 2);
                } else if (decType < 25) {
                    world.setBlockState(pos, RegistryHandler.azalea.getDefaultState(), 2);
                } else if (decType < 27) {
                    world.setBlockState(pos, RegistryHandler.flowering_azalea.getDefaultState(), 2);
                } else if (decType < 65) {
                    world.setBlockState(pos, Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS), 2);
                }
            }
        } else if (floorType < 80) {
            world.setBlockState(floorPos, Blocks.CLAY.getDefaultState(), 2);

            if (world.isAirBlock(pos) || world.getBlockState(pos).getMaterial() == Material.WATER) {
                if (random.nextBoolean()) {
                    world.setBlockState(pos, RegistryHandler.small_dripleaf.getDefaultState(), 2);
                } else if (world.isAirBlock(pos.up())) {
                    world.setBlockState(pos, RegistryHandler.big_dripleaf_stem.getDefaultState(), 2);
                    world.setBlockState(pos.up(), RegistryHandler.big_dripleaf.getDefaultState(), 2);
                }
            }
        }
    }

    private void decorateCeiling(World world, @NonNull Random random, @NonNull BlockPos pos) {
        BlockPos ceilingPos = pos.up();

        if (random.nextBoolean()) {
            world.setBlockState(ceilingPos, RegistryHandler.moss_block.getDefaultState(), 2);

            if (world.isAirBlock(pos)) {
                int decType = random.nextInt(100);

                if (decType < 5) {
                    world.setBlockState(pos, RegistryHandler.spore_blossom.getDefaultState(), 2);
                } else if (decType < 20) {
                    int vineLength = 2 + random.nextInt(6);
                    BlockPos currentPos = pos;

                    for (int i = 0; i < vineLength; i++) {
                        if (world.isAirBlock(currentPos)) {
                            boolean hasBerries = random.nextFloat() < 0.11F;
                            world.setBlockState(currentPos, RegistryHandler.cave_vines_plant.getDefaultState().withProperty(BlockCaveVines.BERRIES, hasBerries), 2);
                            currentPos = currentPos.down();
                        } else {
                            break;
                        }
                    }

                    if (world.isAirBlock(currentPos.up())) {
                        boolean hasBerries = random.nextFloat() < 0.11F;
                        world.setBlockState(currentPos.up(), RegistryHandler.cave_vines.getDefaultState().withProperty(BlockCaveVines.BERRIES, hasBerries), 2);
                    }
                }
            }
        }
    }

    private void decorateWall(@NonNull World world, Random random, BlockPos pos) {
        if (!world.isAirBlock(pos)) return;

        boolean placedVine = false;

        for (net.minecraft.util.EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos offset = pos.offset(facing);
            IBlockState hitState = world.getBlockState(offset);
            Block blockHit = hitState.getBlock();

            if (hitState.isOpaqueCube()) {
                if (blockHit == Blocks.STONE || blockHit == RegistryHandler.deepslate) {
                    if (random.nextInt(10) == 0) {
                        world.setBlockState(offset, RegistryHandler.moss_block.getDefaultState(), 2);
                    }
                }
                if (!placedVine && random.nextInt(5) == 0) {
                    try {
                        net.minecraft.block.properties.PropertyBool prop = BlockVine.NORTH;
                        if (facing == EnumFacing.SOUTH) prop = BlockVine.SOUTH;
                        else if (facing == EnumFacing.EAST) prop = BlockVine.EAST;
                        else if (facing == EnumFacing.WEST) prop = BlockVine.WEST;

                        world.setBlockState(pos, Blocks.VINE.getDefaultState().withProperty(prop, true).withProperty(BlockVine.UP, false), 2);
                        placedVine = true;
                    } catch (Exception e) {}
                }
            }
        }
    }
}
