package sayys.depthsupdate.world.generation;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.block.BlockPointedDripstone;
import sayys.depthsupdate.registry.RegistryHandler;

public class DripstoneCavesGenerator implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!DepthsUpdateConfig.dripstoneCaves.enableDripstoneCaves) {
            return;
        }

        if (world.provider.getDimension() != 0) {
            return;
        }

        if (random.nextInt(DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRarity) == 0) {
            int x = chunkX * 16 + random.nextInt(16);
            int minY = DepthsUpdateConfig.dripstoneCaves.dripstoneCavesMinY;
            int maxY = DepthsUpdateConfig.dripstoneCaves.dripstoneCavesMaxY;
            int yRange = Math.max(1, maxY - minY + 1);

            int y = minY + random.nextInt(yRange);
            int z = chunkZ * 16 + random.nextInt(16);

            BlockPos centerPos = new BlockPos(x, y, z);
            generateDripstoneCave(world, random, centerPos);
        }
    }

    private void generateDripstoneCave(World world, @NonNull Random random, BlockPos center) {
        int radiusX = DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusBase + random.nextInt(Math.max(1, DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusVariation));
        int radiusY = DepthsUpdateConfig.dripstoneCaves.dripstoneCavesHeightBase + random.nextInt(Math.max(1, DepthsUpdateConfig.dripstoneCaves.dripstoneCavesHeightVariation));
        int radiusZ = DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusBase + random.nextInt(Math.max(1, DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusVariation));

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

        if (!state.getBlock().isAir(state, world, pos)) {
            return;
        }

        IBlockState stateDown = world.getBlockState(pos.down());
        IBlockState stateUp = world.getBlockState(pos.up());
        Block blockDown = stateDown.getBlock();
        Block blockUp = stateUp.getBlock();

        boolean isFloorGround = (blockDown == Blocks.STONE || blockDown == Blocks.DIRT || blockDown == RegistryHandler.deepslate || blockDown == RegistryHandler.dripstone_block);
        boolean isCeilingGround = (blockUp == Blocks.STONE || blockUp == Blocks.DIRT || blockUp == RegistryHandler.deepslate || blockUp == RegistryHandler.dripstone_block);

        if (isFloorGround) {
            decorateFloor(world, random, pos);
        } else if (isCeilingGround) {
            decorateCeiling(world, random, pos);
        }
    }

    private void decorateFloor(World world, @NonNull Random random, @NonNull BlockPos pos) {
        BlockPos floorPos = pos.down();

        if (random.nextInt(3) == 0) {
            world.setBlockState(floorPos, RegistryHandler.dripstone_block.getDefaultState(), 3);
        }

        int decType = random.nextInt(100);

        if (decType < 20) {
            int length = 1 + random.nextInt(4);
            generateDripstonePillar(world, random, pos, EnumFacing.UP, length);
        } else if (decType < 25) {
            world.setBlockState(floorPos, Blocks.WATER.getDefaultState(), 3);
        }
    }

    private void decorateCeiling(World world, @NonNull Random random, @NonNull BlockPos pos) {
        BlockPos ceilingPos = pos.up();

        if (random.nextInt(3) == 0) {
            world.setBlockState(ceilingPos, RegistryHandler.dripstone_block.getDefaultState(), 3);
        }


        if (random.nextInt(100) < 25) {
            int length = 1 + random.nextInt(4);
            generateDripstonePillar(world, random, pos, EnumFacing.DOWN, length);
        }
    }

    private void generateDripstonePillar(World world, Random random, BlockPos pos, EnumFacing direction, int length) {
        BlockPos currentPos = pos;
        for (int i = 0; i < length; i++) {
            if (world.isAirBlock(currentPos)) {
                IBlockState state = RegistryHandler.pointed_dripstone.getDefaultState().withProperty(BlockPointedDripstone.TIP_DIRECTION, direction);
                world.setBlockState(currentPos, state, 3);

                currentPos = (direction == EnumFacing.UP) ? currentPos.up() : currentPos.down();
            } else {
                break;
            }
        }
    }
}
