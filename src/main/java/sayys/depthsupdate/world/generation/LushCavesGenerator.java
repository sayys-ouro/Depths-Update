package sayys.depthsupdate.world.generation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.block.BlockCaveVines;
import sayys.depthsupdate.compat.FluidloggedCompat;
import sayys.depthsupdate.registry.PlantRegistry;
import sayys.depthsupdate.util.BlockUtils;

public class LushCavesGenerator implements IWorldGenerator {
    private static final int VERTICAL_RANGE = 5;
    private static final int PATCH_RADIUS_MIN = 4;
    private static final int PATCH_RADIUS_RANGE = 4;

    private static final float MOSS_VEGETATION_CHANCE = 0.8F;
    private static final float MOSS_EDGE_CHANCE = 0.3F;
    private static final float CEILING_VEGETATION_CHANCE = 0.08F;
    private static final int CEILING_DEPTH_RANGE = 2;

    private static final int CLAY_DEPTH = 3;
    private static final float CLAY_EXTRA_BOTTOM_CHANCE = 0.8F;
    private static final float CLAY_EDGE_CHANCE = 0.7F;
    private static final float CLAY_POOL_VEGETATION_CHANCE = 0.1F;
    private static final float CLAY_DRY_VEGETATION_CHANCE = 0.05F;

    private static final float BERRY_CHANCE = 0.2F;
    private static final int ROOT_HANGING_ATTEMPTS = 6;

    public static void register() {
        GameRegistry.registerWorldGenerator(new LushCavesGenerator(), 100);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!DepthsUpdateConfig.lushCaves.enableLushCaves) {
            return;
        }

        if (world.provider.getDimension() != 0) {
            return;
        }

        if (chunkGenerator instanceof ChunkGeneratorFlat || chunkGenerator instanceof ChunkGeneratorDebug) {
            return;
        }

        if (random.nextInt(DepthsUpdateConfig.lushCaves.lushCavesRarity) == 0) {
            int x = chunkX * 16 + 8 + random.nextInt(16);
            int minY = DepthsUpdateConfig.lushCaves.lushCavesMinY;
            int maxY = DepthsUpdateConfig.lushCaves.lushCavesMaxY;
            int yRange = Math.max(1, maxY - minY + 1);

            int y = minY + random.nextInt(yRange);
            int z = chunkZ * 16 + 8 + random.nextInt(16);

            generateLushCave(world, random, new BlockPos(x, y, z));
        }
    }

    private void generateLushCave(World world, @NonNull Random random, BlockPos center) {
        int radiusX = DepthsUpdateConfig.lushCaves.lushCavesRadiusBase + random.nextInt(Math.max(1, DepthsUpdateConfig.lushCaves.lushCavesRadiusVariation));
        int radiusY = DepthsUpdateConfig.lushCaves.lushCavesHeightBase + random.nextInt(Math.max(1, DepthsUpdateConfig.lushCaves.lushCavesHeightVariation));
        int radiusZ = DepthsUpdateConfig.lushCaves.lushCavesRadiusBase + random.nextInt(Math.max(1, DepthsUpdateConfig.lushCaves.lushCavesRadiusVariation));

        double volume = CaveRegion.volume(radiusX, radiusY, radiusZ);
        int mossPatches = Math.max(6, (int) (volume / 400.0));
        int ceilingPatches = Math.max(5, (int) (volume / 450.0));
        int clayPatches = Math.max(2, (int) (volume / 1400.0));
        int vineColumns = Math.max(8, (int) (volume / 220.0));
        int sporeBlossoms = Math.max(3, (int) (volume / 900.0));
        int rootPatches = Math.max(2, (int) (volume / 1500.0));
        int vinePatches = Math.max(3, (int) (volume / 800.0));

        for (int i = 0; i < mossPatches; i++) {
            placeMossPatch(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
        }

        for (int i = 0; i < ceilingPatches; i++) {
            placeCeilingMossPatch(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
        }

        for (int i = 0; i < clayPatches; i++) {
            placeClayPatch(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ), random.nextBoolean());
        }

        for (int i = 0; i < vineColumns; i++) {
            placeCaveVineColumn(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
        }

        for (int i = 0; i < sporeBlossoms; i++) {
            placeSporeBlossom(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
        }

        for (int i = 0; i < rootPatches; i++) {
            placeRootedDirt(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
        }

        for (int i = 0; i < vinePatches; i++) {
            placeWallVine(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
        }
    }


    private void placeMossPatch(World world, Random random, BlockPos origin) {
        Set<BlockPos> ground = placeGroundPatch(world, random, origin, EnumFacing.DOWN,
                PlantRegistry.moss_block.getDefaultState(), 1, 0.0F, MOSS_EDGE_CHANCE);

        for (BlockPos pos : ground) {
            if (random.nextFloat() < MOSS_VEGETATION_CHANCE) {
                placeMossVegetation(world, random, pos.up());
            }
        }
    }

    private void placeCeilingMossPatch(World world, Random random, BlockPos origin) {
        int depth = 1 + random.nextInt(CEILING_DEPTH_RANGE);
        Set<BlockPos> ground = placeGroundPatch(world, random, origin, EnumFacing.UP,
                PlantRegistry.moss_block.getDefaultState(), depth, 0.0F, MOSS_EDGE_CHANCE);

        for (BlockPos pos : ground) {
            if (random.nextFloat() < CEILING_VEGETATION_CHANCE) {
                growCaveVines(world, random, pos.down());
            }
        }
    }

    private void placeClayPatch(World world, Random random, BlockPos origin, boolean pool) {
        Set<BlockPos> ground = placeGroundPatch(world, random, origin, EnumFacing.DOWN,
                Blocks.CLAY.getDefaultState(), CLAY_DEPTH, CLAY_EXTRA_BOTTOM_CHANCE, CLAY_EDGE_CHANCE);

        if (ground.isEmpty()) {
            return;
        }

        Set<BlockPos> surface = ground;

        if (pool) {
            surface = new HashSet<>();

            for (BlockPos pos : ground) {
                if (!isRimBlock(world, pos)) {
                    surface.add(pos);
                }
            }

            for (BlockPos pos : surface) {
                world.setBlockState(pos, Blocks.WATER.getDefaultState(), 2);
            }
        }

        float chance = pool ? CLAY_POOL_VEGETATION_CHANCE : CLAY_DRY_VEGETATION_CHANCE;

        for (BlockPos pos : surface) {
            if (random.nextFloat() < chance) {
                placeDripleaf(world, random, pool ? pos : pos.up(), pool);
            }
        }
    }

    private Set<BlockPos> placeGroundPatch(World world, Random random, BlockPos origin, EnumFacing surface,
            IBlockState ground, int depth, float extraBottomChance, float edgeChance) {
        if (!world.isAirBlock(origin)) {
            return Collections.emptySet();
        }

        int xRadius = PATCH_RADIUS_MIN + random.nextInt(PATCH_RADIUS_RANGE) + 1;
        int zRadius = PATCH_RADIUS_MIN + random.nextInt(PATCH_RADIUS_RANGE) + 1;

        EnumFacing outwards = surface.getOpposite();
        Set<BlockPos> placed = new HashSet<>();

        for (int dx = -xRadius; dx <= xRadius; dx++) {
            boolean xEdge = dx == -xRadius || dx == xRadius;

            for (int dz = -zRadius; dz <= zRadius; dz++) {
                boolean zEdge = dz == -zRadius || dz == zRadius;

                if (xEdge && zEdge) {
                    continue;
                }

                if ((xEdge || zEdge) && random.nextFloat() > edgeChance) {
                    continue;
                }

                BlockPos pos = origin.add(dx, 0, dz);

                for (int i = 0; i < VERTICAL_RANGE && world.isAirBlock(pos); i++) {
                    pos = pos.offset(surface);
                }

                for (int i = 0; i < VERTICAL_RANGE && !world.isAirBlock(pos); i++) {
                    pos = pos.offset(outwards);
                }

                BlockPos groundPos = pos.offset(surface);

                if (!world.isAirBlock(pos) || !world.getBlockState(groundPos).isSideSolid(world, groundPos, outwards)) {
                    continue;
                }

                int columnDepth = depth + (random.nextFloat() < extraBottomChance ? 1 : 0);

                if (placeGroundColumn(world, groundPos, surface, ground, columnDepth)) {
                    placed.add(groundPos);
                }
            }
        }

        return placed;
    }

    private boolean placeGroundColumn(World world, BlockPos start, EnumFacing surface, IBlockState ground, int depth) {
        BlockPos pos = start;

        for (int i = 0; i < depth; i++) {
            IBlockState state = world.getBlockState(pos);

            if (state.getBlock() == ground.getBlock()) {
                pos = pos.offset(surface);
                continue;
            }

            if (!isReplaceable(state)) {
                return i != 0;
            }

            world.setBlockState(pos, ground, 2);
            pos = pos.offset(surface);
        }

        return true;
    }

    private static boolean isRimBlock(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos side = pos.offset(facing);

            if (!world.getBlockState(side).isSideSolid(world, side, facing.getOpposite())) {
                return true;
            }
        }

        BlockPos below = pos.down();

        return !world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP);
    }

    private void placeMossVegetation(World world, Random random, BlockPos pos) {
        if (!world.isAirBlock(pos)) {
            return;
        }

        int roll = random.nextInt(96);

        if (roll < 50) {
            world.setBlockState(pos, Blocks.TALLGRASS.getDefaultState()
                    .withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS), 2);
        } else if (roll < 75) {
            world.setBlockState(pos, PlantRegistry.moss_carpet.getDefaultState(), 2);
        } else if (roll < 85) {
            placeTallGrass(world, pos);
        } else if (roll < 92) {
            world.setBlockState(pos, PlantRegistry.azalea.getDefaultState(), 2);
        } else {
            world.setBlockState(pos, PlantRegistry.flowering_azalea.getDefaultState(), 2);
        }
    }

    private static void placeTallGrass(World world, BlockPos pos) {
        if (!world.isAirBlock(pos.up())) {
            world.setBlockState(pos, Blocks.TALLGRASS.getDefaultState()
                    .withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS), 2);
            return;
        }

        world.setBlockState(pos, Blocks.DOUBLE_PLANT.getDefaultState()
                .withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS), 2);
        world.setBlockState(pos.up(), Blocks.DOUBLE_PLANT.getDefaultState()
                .withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 2);
    }

    private void placeDripleaf(World world, Random random, BlockPos pos, boolean waterlogged) {
        if (!world.isAirBlock(pos) && world.getBlockState(pos).getMaterial() != Material.WATER) {
            return;
        }

        if (random.nextBoolean()) {
            world.setBlockState(pos, PlantRegistry.small_dripleaf.getDefaultState(), 2);
        } else if (world.isAirBlock(pos.up()) || world.getBlockState(pos.up()).getMaterial() == Material.WATER) {
            world.setBlockState(pos, PlantRegistry.big_dripleaf_stem.getDefaultState(), 2);
            world.setBlockState(pos.up(), PlantRegistry.big_dripleaf.getDefaultState(), 2);
        } else {
            return;
        }

        if (waterlogged) {
            FluidloggedCompat.logWater(world, pos, world.getBlockState(pos));
        }
    }

    private void placeCaveVineColumn(World world, Random random, BlockPos origin) {
        if (!world.isAirBlock(origin)) {
            return;
        }

        BlockPos pos = origin;

        for (int i = 0; i < VERTICAL_RANGE && world.isAirBlock(pos.up()); i++) {
            pos = pos.up();
        }

        if (!isReplaceable(world.getBlockState(pos.up()))) {
            return;
        }

        growCaveVines(world, random, pos);
    }

    private void growCaveVines(World world, Random random, BlockPos start) {
        int roll = random.nextInt(15);
        int length = roll < 10 ? random.nextInt(7) : (roll < 13 ? random.nextInt(3) : random.nextInt(20));

        if (length <= 0) {
            return;
        }

        BlockPos pos = start;
        int placed = 0;

        while (placed < length && world.isAirBlock(pos)) {
            boolean berries = random.nextFloat() < BERRY_CHANCE;
            world.setBlockState(pos, PlantRegistry.cave_vines_plant.getDefaultState()
                    .withProperty(BlockCaveVines.BERRIES, berries), 2);
            pos = pos.down();
            placed++;
        }

        if (placed > 0 && world.isAirBlock(pos)) {
            boolean berries = random.nextFloat() < BERRY_CHANCE;
            world.setBlockState(pos, PlantRegistry.cave_vines.getDefaultState()
                    .withProperty(BlockCaveVines.BERRIES, berries), 2);
        }
    }

    private void placeSporeBlossom(World world, Random random, BlockPos origin) {
        if (!world.isAirBlock(origin)) {
            return;
        }

        BlockPos pos = origin;

        for (int i = 0; i < VERTICAL_RANGE && world.isAirBlock(pos.up()); i++) {
            pos = pos.up();
        }

        IBlockState above = world.getBlockState(pos.up());

        if (above.getBlock() == PlantRegistry.moss_block || isReplaceable(above)) {
            world.setBlockState(pos, PlantRegistry.spore_blossom.getDefaultState(), 2);
        }
    }

    private void placeRootedDirt(World world, Random random, BlockPos origin) {
        if (!world.isAirBlock(origin)) {
            return;
        }

        BlockPos pos = origin;

        for (int i = 0; i < VERTICAL_RANGE && world.isAirBlock(pos.up()); i++) {
            pos = pos.up();
        }

        BlockPos ceiling = pos.up();

        if (!isReplaceable(world.getBlockState(ceiling))) {
            return;
        }

        world.setBlockState(ceiling, PlantRegistry.rooted_dirt.getDefaultState(), 2);

        for (int i = 0; i < ROOT_HANGING_ATTEMPTS; i++) {
            BlockPos rootPos = ceiling.add(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);

            if (isReplaceable(world.getBlockState(rootPos)) && world.isAirBlock(rootPos.down())) {
                world.setBlockState(rootPos, PlantRegistry.rooted_dirt.getDefaultState(), 2);
                world.setBlockState(rootPos.down(), PlantRegistry.hanging_roots.getDefaultState(), 2);
            }
        }
    }

    private void placeWallVine(World world, Random random, BlockPos origin) {
        if (!world.isAirBlock(origin)) {
            return;
        }

        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos wall = origin.offset(facing);

            if (!world.getBlockState(wall).isSideSolid(world, wall, facing.getOpposite())) {
                continue;
            }

            PropertyBool side = vineSide(facing);

            int length = 1 + random.nextInt(6);
            BlockPos pos = origin;

            for (int i = 0; i < length && world.isAirBlock(pos); i++) {
                BlockPos support = pos.offset(facing);

                if (!world.getBlockState(support).isSideSolid(world, support, facing.getOpposite())) {
                    break;
                }

                world.setBlockState(pos, Blocks.VINE.getDefaultState().withProperty(side, true), 2);
                pos = pos.down();
            }

            return;
        }
    }

    private static PropertyBool vineSide(EnumFacing facing) {
        return switch (facing) {
            case NORTH -> BlockVine.NORTH;
            case SOUTH -> BlockVine.SOUTH;
            case EAST -> BlockVine.EAST;
            case WEST -> BlockVine.WEST;
            default -> null;
        };
    }

    /** Vanilla's moss_replaceable tag: base stone plus dirt, grass and moss. */
    private static boolean isReplaceable(IBlockState state) {
        Block block = state.getBlock();

        return BlockUtils.isBaseStone(state)
                || block == Blocks.DIRT
                || block == Blocks.GRASS
                || block == Blocks.GRAVEL
                || block == Blocks.CLAY
                || block == PlantRegistry.moss_block;
    }
}
