package sayys.depthsupdate.world.generation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.block.BlockPointedDripstone;
import sayys.depthsupdate.block.BlockPointedDripstone.DripstoneThickness;
import sayys.depthsupdate.compat.FluidloggedCompat;
import sayys.depthsupdate.registry.DeepslateRegistry;
import sayys.depthsupdate.util.BlockUtils;

public class DripstoneCavesGenerator implements IWorldGenerator {
    private static final int NONE = Integer.MIN_VALUE;

    private static final int COLUMN_SCAN_RANGE = 12;
    private static final int LARGE_SCAN_RANGE = 30;

    private static final int CLUSTER_LAYER_MIN = 2;
    private static final int CLUSTER_LAYER_RANGE = 3;
    private static final int CLUSTER_HEIGHT_DEVIATION = 3;
    private static final int CLUSTER_MAX_HEIGHT_DIFF = 1;
    private static final int CLUSTER_CENTER_BIAS_RANGE = 8;
    private static final int CLUSTER_EDGE_FADE_RANGE = 3;
    private static final float CLUSTER_EDGE_CHANCE = 0.1F;

    private static final float LARGE_RADIUS_TO_HEIGHT_RATIO = 0.33F;
    private static final int LARGE_MIN_RADIUS = 3;
    private static final int LARGE_MAX_RADIUS = 16;
    private static final float LARGE_WIND_MAX_SPEED = 0.3F;
    private static final int LARGE_MIN_RADIUS_FOR_WIND = 4;
    private static final float LARGE_MIN_BLUNTNESS_FOR_WIND = 0.6F;

    private static final int MIN_OPEN_HEIGHT = 3;

    private static final float POINTED_TALLER_CHANCE = 0.35F;
    private static final float POINTED_SPREAD_CHANCE = 0.7F;
    private static final float POINTED_SPREAD2_CHANCE = 0.5F;
    private static final float POINTED_SPREAD3_CHANCE = 0.5F;

    public static void register() {
        GameRegistry.registerWorldGenerator(new DripstoneCavesGenerator(), 110);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!DepthsUpdateConfig.dripstoneCaves.enableDripstoneCaves) {
            return;
        }

        if (world.provider.getDimension() != 0) {
            return;
        }

        if (chunkGenerator instanceof ChunkGeneratorFlat || chunkGenerator instanceof ChunkGeneratorDebug) {
            return;
        }

        if (random.nextInt(DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRarity) == 0) {
            int y = CaveRegion.randomYInWindow(random, HeightManager.get(world),
                    DepthsUpdateConfig.dripstoneCaves.dripstoneCavesMinY,
                    DepthsUpdateConfig.dripstoneCaves.dripstoneCavesMaxY);

            if (y == CaveRegion.NO_Y) {
                return;
            }

            int x = chunkX * 16 + 8 + random.nextInt(16);
            int z = chunkZ * 16 + 8 + random.nextInt(16);

            BlockPos center = new BlockPos(x, y, z);
            GenerationLog.featurePlaced("dripstone cave", center);
            generateDripstoneCave(world, random, center);
        }
    }

    private void generateDripstoneCave(World world, @NonNull Random random, BlockPos center) {
        int radiusX = DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusBase + random.nextInt(Math.max(1, DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusVariation));
        int radiusY = DepthsUpdateConfig.dripstoneCaves.dripstoneCavesHeightBase + random.nextInt(Math.max(1, DepthsUpdateConfig.dripstoneCaves.dripstoneCavesHeightVariation));
        int radiusZ = DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusBase + random.nextInt(Math.max(1, DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusVariation));

        double volume = CaveRegion.volume(radiusX, radiusY, radiusZ);
        int clusters = Math.max(6, (int) (volume / 700.0));
        int spires = Math.max(3, (int) (volume / 2600.0));
        int pointedPatches = Math.max(4, (int) (volume / 2600.0));

        List<BlockPos> placedSpikes = new ArrayList<>();

        for (int i = 0; i < clusters; i++) {
            placeCluster(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ), placedSpikes);
        }

        for (int i = 0; i < spires; i++) {
            placeLargeDripstone(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
        }

        for (int i = 0; i < pointedPatches; i++) {
            BlockPos patchCenter = CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ);
            int attempts = 1 + random.nextInt(5);

            for (int j = 0; j < attempts; j++) {
                BlockPos pos = patchCenter.add(
                        (int) clampedNormal(random, 0.0F, 3.0F, -10.0F, 10.0F),
                        (int) clampedNormal(random, 0.0F, 0.6F, -2.0F, 2.0F),
                        (int) clampedNormal(random, 0.0F, 3.0F, -10.0F, 10.0F));
                placePointedSpike(world, random, pos, placedSpikes);
            }
        }

        relinkSpikes(world, placedSpikes);
    }

    private void relinkSpikes(World world, List<BlockPos> placed) {
        Set<BlockPos> targets = new HashSet<>(placed);

        for (BlockPos pos : placed) {
            addIfSpike(world, targets, pos.up());
            addIfSpike(world, targets, pos.down());
        }

        List<BlockPos> ordered = new ArrayList<>(targets);
        ordered.sort(Comparator.comparingInt(BlockPos::getY));

        for (BlockPos pos : ordered) {
            if (hasTipDirection(world, pos, EnumFacing.DOWN)) {
                BlockPointedDripstone.refreshThickness(world, pos);
            }
        }

        for (int i = ordered.size() - 1; i >= 0; i--) {
            BlockPos pos = ordered.get(i);

            if (hasTipDirection(world, pos, EnumFacing.UP)) {
                BlockPointedDripstone.refreshThickness(world, pos);
            }
        }
    }

    private static void addIfSpike(World world, Set<BlockPos> targets, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == DeepslateRegistry.pointed_dripstone) {
            targets.add(pos);
        }
    }

    private static boolean hasTipDirection(World world, BlockPos pos, EnumFacing tipDirection) {
        return BlockPointedDripstone.isPointedDripstoneWithDirection(world.getBlockState(pos), tipDirection);
    }


    private void placeCluster(World world, Random random, BlockPos origin, List<BlockPos> placed) {
        if (!isEmptyOrWater(world.getBlockState(origin))) {
            return;
        }

        int clusterHeight = 3 + random.nextInt(4);
        float wetness = clampedNormal(random, 0.1F, 0.3F, 0.1F, 0.9F);
        float density = 0.3F + random.nextFloat() * 0.4F;
        int radiusX = 2 + random.nextInt(7);
        int radiusZ = 2 + random.nextInt(7);

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                double chance = spikeChance(radiusX, radiusZ, dx, dz);
                placeClusterColumn(world, random, origin.add(dx, 0, dz), dx, dz, wetness, chance, clusterHeight, density, placed);
            }
        }
    }

    private void placeClusterColumn(World world, Random random, BlockPos pos, int dx, int dz,
            float wetness, double chance, int clusterHeight, float density, List<BlockPos> placed) {
        int ceilingY = scanForSolid(world, pos, EnumFacing.UP, COLUMN_SCAN_RANGE, false);
        int floorY = scanForSolid(world, pos, EnumFacing.DOWN, COLUMN_SCAN_RANGE, false);

        if (ceilingY == NONE && floorY == NONE) {
            return;
        }

        if (ceilingY != NONE && floorY != NONE && ceilingY - floorY - 1 < MIN_OPEN_HEIGHT) {
            return;
        }

        if (floorY != NONE && random.nextFloat() < wetness) {
            BlockPos floorPos = new BlockPos(pos.getX(), floorY, pos.getZ());

            if (canPlacePool(world, floorPos)) {
                world.setBlockState(floorPos, Blocks.WATER.getDefaultState(), 2);
                floorY--;
            }
        }

        int stalactiteHeight = 0;
        if (ceilingY != NONE && random.nextDouble() < chance && !isLava(world, pos.getX(), ceilingY, pos.getZ())) {
            int layerThickness = CLUSTER_LAYER_MIN + random.nextInt(CLUSTER_LAYER_RANGE);
            replaceWithDripstone(world, new BlockPos(pos.getX(), ceilingY, pos.getZ()), layerThickness, EnumFacing.UP);

            int maxHeight = (floorY != NONE) ? Math.min(clusterHeight, ceilingY - floorY) : clusterHeight;
            stalactiteHeight = clusterSpikeHeight(random, dx, dz, density, maxHeight);
        }

        int stalagmiteHeight = 0;
        if (floorY != NONE && random.nextDouble() < chance && !isLava(world, pos.getX(), floorY, pos.getZ())) {
            int layerThickness = CLUSTER_LAYER_MIN + random.nextInt(CLUSTER_LAYER_RANGE);
            replaceWithDripstone(world, new BlockPos(pos.getX(), floorY, pos.getZ()), layerThickness, EnumFacing.DOWN);

            if (ceilingY != NONE) {
                stalagmiteHeight = Math.max(0, stalactiteHeight + random.nextInt(2 * CLUSTER_MAX_HEIGHT_DIFF + 1) - CLUSTER_MAX_HEIGHT_DIFF);
            } else {
                stalagmiteHeight = clusterSpikeHeight(random, dx, dz, density, clusterHeight);
            }
        }

        if (ceilingY != NONE && floorY != NONE && ceilingY - stalactiteHeight <= floorY + stalagmiteHeight) {
            int lowestStalactiteBottom = Math.max(ceilingY - stalactiteHeight, floorY + 1);
            int highestStalagmiteTop = Math.min(floorY + stalagmiteHeight, ceilingY - 1);
            int bound = Math.max(1, highestStalagmiteTop + 2 - lowestStalactiteBottom);

            int stalactiteBottom = lowestStalactiteBottom + random.nextInt(bound);
            stalactiteHeight = ceilingY - stalactiteBottom;
            stalagmiteHeight = stalactiteBottom - 1 - floorY;
        }

        int gap = (ceilingY != NONE && floorY != NONE) ? ceilingY - floorY - 1 : -1;
        boolean mergedTip = random.nextBoolean() && stalactiteHeight > 0 && stalagmiteHeight > 0
                && gap > 0 && stalactiteHeight + stalagmiteHeight == gap;

        if (ceilingY != NONE) {
            growSpeleothem(world, new BlockPos(pos.getX(), ceilingY - 1, pos.getZ()), EnumFacing.DOWN, stalactiteHeight, mergedTip, placed);
        }

        if (floorY != NONE) {
            growSpeleothem(world, new BlockPos(pos.getX(), floorY + 1, pos.getZ()), EnumFacing.UP, stalagmiteHeight, mergedTip, placed);
        }
    }

    private int clusterSpikeHeight(Random random, int dx, int dz, float density, int maxHeight) {
        if (random.nextFloat() > density) {
            return 0;
        }

        int distanceFromCenter = Math.abs(dx) + Math.abs(dz);
        float mean = clampedMap(distanceFromCenter, 0.0F, CLUSTER_CENTER_BIAS_RANGE, maxHeight / 2.0F, 0.0F);

        return (int) clampedNormal(random, mean, CLUSTER_HEIGHT_DEVIATION, 0.0F, maxHeight);
    }

    private static double spikeChance(int radiusX, int radiusZ, int dx, int dz) {
        int distanceFromEdge = Math.min(radiusX - Math.abs(dx), radiusZ - Math.abs(dz));

        return clampedMap(distanceFromEdge, 0.0F, CLUSTER_EDGE_FADE_RANGE, CLUSTER_EDGE_CHANCE, 1.0F);
    }

    private boolean canPlacePool(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (state.getMaterial() == Material.WATER || block == DeepslateRegistry.dripstone_block || block == DeepslateRegistry.pointed_dripstone) {
            return false;
        }

        if (world.getBlockState(pos.up()).getMaterial() == Material.WATER) {
            return false;
        }

        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            if (!isStoneOrWater(world, pos.offset(facing))) {
                return false;
            }
        }

        return isStoneOrWater(world, pos.down());
    }

    private boolean isStoneOrWater(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);

        return isReplaceableStone(state) || state.getMaterial() == Material.WATER;
    }

    private void placeLargeDripstone(World world, Random random, BlockPos origin) {
        if (!isEmptyOrWater(world.getBlockState(origin))) {
            return;
        }

        int ceilingY = scanForSolid(world, origin, EnumFacing.UP, LARGE_SCAN_RANGE, true);
        int floorY = scanForSolid(world, origin, EnumFacing.DOWN, LARGE_SCAN_RANGE, true);

        if (ceilingY == NONE || floorY == NONE) {
            return;
        }

        int caveHeight = ceilingY - floorY - 1;
        if (caveHeight < 4) {
            return;
        }

        int maxRadius = MathHelper.clamp((int) (caveHeight * LARGE_RADIUS_TO_HEIGHT_RATIO), LARGE_MIN_RADIUS, LARGE_MAX_RADIUS);
        int radius = LARGE_MIN_RADIUS + random.nextInt(maxRadius - LARGE_MIN_RADIUS + 1);

        Spire stalactite = new Spire(new BlockPos(origin.getX(), ceilingY - 1, origin.getZ()), false, radius,
                uniform(random, 0.3F, 0.9F), uniform(random, 0.4F, 2.0F));
        Spire stalagmite = new Spire(new BlockPos(origin.getX(), floorY + 1, origin.getZ()), true, radius,
                uniform(random, 0.4F, 1.0F), uniform(random, 0.4F, 2.0F));

        Wind wind = (stalactite.isSuitableForWind() && stalagmite.isSuitableForWind())
                ? new Wind(origin.getY(), random, LARGE_WIND_MAX_SPEED, LARGE_MAX_RADIUS - radius)
                : Wind.NONE;

        if (stalactite.embedRoot(world, wind)) {
            stalactite.place(world, random, wind);
        }

        if (stalagmite.embedRoot(world, wind)) {
            stalagmite.place(world, random, wind);
        }
    }

    private void placePointedSpike(World world, Random random, BlockPos origin, List<BlockPos> placed) {
        if (!isEmptyOrWater(world.getBlockState(origin))) {
            return;
        }

        int ceilingY = scanForSolid(world, origin, EnumFacing.UP, COLUMN_SCAN_RANGE, false);
        int floorY = scanForSolid(world, origin, EnumFacing.DOWN, COLUMN_SCAN_RANGE, false);

        if (ceilingY == NONE && floorY == NONE) {
            return;
        }

        if (ceilingY != NONE && floorY != NONE && ceilingY - floorY - 1 < MIN_OPEN_HEIGHT) {
            return;
        }

        boolean fromCeiling = (floorY == NONE) || (ceilingY != NONE && random.nextBoolean());
        EnumFacing tipDirection = fromCeiling ? EnumFacing.DOWN : EnumFacing.UP;
        BlockPos pos = new BlockPos(origin.getX(), fromCeiling ? ceilingY - 1 : floorY + 1, origin.getZ());

        if (!isEmptyOrWater(world.getBlockState(pos)) || !hasOpenSide(world, pos)) {
            return;
        }

        if (!isBase(world.getBlockState(pos.offset(tipDirection.getOpposite())))) {
            return;
        }

        placeBasePatch(world, random, pos.offset(tipDirection.getOpposite()));

        int height = (random.nextFloat() < POINTED_TALLER_CHANCE && isEmptyOrWater(world.getBlockState(pos.offset(tipDirection)))) ? 2 : 1;
        growSpeleothem(world, pos, tipDirection, height, false, placed);
    }

    private static boolean hasOpenSide(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            IBlockState state = world.getBlockState(pos.offset(facing));

            if (isEmptyOrWater(state) || state.getBlock() == DeepslateRegistry.pointed_dripstone) {
                return true;
            }
        }

        return false;
    }

    private void placeBasePatch(World world, Random random, BlockPos pos) {
        placeDripstoneBlockIfPossible(world, pos);

        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            if (random.nextFloat() > POINTED_SPREAD_CHANCE) {
                continue;
            }

            BlockPos first = pos.offset(facing);
            placeDripstoneBlockIfPossible(world, first);

            if (random.nextFloat() > POINTED_SPREAD2_CHANCE) {
                continue;
            }

            BlockPos second = first.offset(EnumFacing.random(random));
            placeDripstoneBlockIfPossible(world, second);

            if (random.nextFloat() > POINTED_SPREAD3_CHANCE) {
                continue;
            }

            placeDripstoneBlockIfPossible(world, second.offset(EnumFacing.random(random)));
        }
    }

    private void growSpeleothem(World world, BlockPos start, EnumFacing tipDirection, int totalLength, boolean mergedTip, List<BlockPos> placed) {
        if (totalLength <= 0) {
            return;
        }

        if (!isBase(world.getBlockState(start.offset(tipDirection.getOpposite())))) {
            return;
        }

        BlockPos pos = start;

        if (totalLength >= 3) {
            pos = placePointed(world, pos, tipDirection, DripstoneThickness.BASE, placed);

            for (int i = 0; i < totalLength - 3; i++) {
                pos = placePointed(world, pos, tipDirection, DripstoneThickness.MIDDLE, placed);
            }
        }

        if (totalLength >= 2) {
            pos = placePointed(world, pos, tipDirection, DripstoneThickness.FRUSTUM, placed);
        }

        placePointed(world, pos, tipDirection, mergedTip ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP, placed);
    }

    private BlockPos placePointed(World world, BlockPos pos, EnumFacing tipDirection, DripstoneThickness thickness, List<BlockPos> placed) {
        boolean inWater = world.getBlockState(pos).getMaterial() == Material.WATER;
        IBlockState state = DeepslateRegistry.pointed_dripstone.getDefaultState()
                .withProperty(BlockPointedDripstone.TIP_DIRECTION, tipDirection)
                .withProperty(BlockPointedDripstone.THICKNESS, thickness);
        world.setBlockState(pos, state, 2);
        placed.add(pos);

        if (inWater) {
            FluidloggedCompat.logWater(world, pos, state);
        }

        return pos.offset(tipDirection);
    }

    private void replaceWithDripstone(World world, BlockPos start, int maxCount, EnumFacing direction) {
        BlockPos pos = start;

        for (int i = 0; i < maxCount; i++) {
            if (!placeDripstoneBlockIfPossible(world, pos)) {
                return;
            }

            pos = pos.offset(direction);
        }
    }

    private boolean placeDripstoneBlockIfPossible(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);

        if (isReplaceableStone(state)) {
            world.setBlockState(pos, DeepslateRegistry.dripstone_block.getDefaultState(), 2);
            return true;
        }

        return false;
    }

    private int scanForSolid(World world, BlockPos origin, EnumFacing direction, int range, boolean requireBaseOrLava) {
        BlockPos pos = origin;

        for (int i = 0; i < range; i++) {
            pos = pos.offset(direction);
            IBlockState state = world.getBlockState(pos);

            if (isEmptyOrWater(state)) {
                continue;
            }

            if (requireBaseOrLava && !isBaseOrLava(state)) {
                return NONE;
            }

            return pos.getY();
        }

        return NONE;
    }

    private static boolean isEmptyOrWater(IBlockState state) {
        return state.getMaterial() == Material.AIR || state.getMaterial() == Material.WATER;
    }

    private static boolean isEmptyOrWaterOrLava(IBlockState state) {
        return state.getMaterial() == Material.AIR || state.getMaterial() == Material.WATER || state.getMaterial() == Material.LAVA;
    }

    /** Vanilla's dripstone_replaceable_blocks tag is exactly base_stone_overworld. */
    private static boolean isReplaceableStone(IBlockState state) {
        return BlockUtils.isBaseStone(state);
    }

    private static boolean isBase(IBlockState state) {
        return state.getBlock() == DeepslateRegistry.dripstone_block || isReplaceableStone(state);
    }

    private static boolean isBaseOrLava(IBlockState state) {
        return isBase(state) || state.getMaterial() == Material.LAVA;
    }

    private static boolean isLava(World world, int x, int y, int z) {
        return world.getBlockState(new BlockPos(x, y, z)).getMaterial() == Material.LAVA;
    }

    private static float uniform(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private static float clampedNormal(Random random, float mean, float deviation, float min, float max) {
        return MathHelper.clamp(mean + deviation * (float) random.nextGaussian(), min, max);
    }

    private static float clampedMap(float value, float fromMin, float fromMax, float toMin, float toMax) {
        float t = MathHelper.clamp((value - fromMin) / (fromMax - fromMin), 0.0F, 1.0F);

        return toMin + t * (toMax - toMin);
    }

    /** Vanilla's speleothem height curve. Bluntness flattens the tip, scale stretches it. */
    private static double spireHeight(double distance, double radius, double scale, double bluntness) {
        if (distance < bluntness) {
            distance = bluntness;
        }

        double r = distance / radius * 0.384;
        double height = scale * (0.75 * Math.pow(r, 4.0 / 3.0) - Math.pow(r, 2.0 / 3.0) - Math.log(r) / 3.0);

        return Math.max(height, 0.0) / 0.384 * radius;
    }

    private static boolean isCircleEmbedded(World world, BlockPos center, int radius) {
        if (isEmptyOrWaterOrLava(world.getBlockState(center))) {
            return false;
        }

        float angleStep = 6.0F / radius;

        for (float angle = 0.0F; angle < (float) (Math.PI * 2.0); angle += angleStep) {
            int dx = (int) (MathHelper.cos(angle) * radius);
            int dz = (int) (MathHelper.sin(angle) * radius);

            if (isEmptyOrWaterOrLava(world.getBlockState(center.add(dx, 0, dz)))) {
                return false;
            }
        }

        return true;
    }

    private static final class Spire {
        private BlockPos root;
        private final boolean pointingUp;
        private int radius;
        private final double bluntness;
        private final double scale;

        private Spire(BlockPos root, boolean pointingUp, int radius, double bluntness, double scale) {
            this.root = root;
            this.pointingUp = pointingUp;
            this.radius = radius;
            this.bluntness = bluntness;
            this.scale = scale;
        }

        private int height() {
            return heightAt(0.0F);
        }

        private int heightAt(float distance) {
            return (int) spireHeight(distance, radius, scale, bluntness);
        }

        private boolean isSuitableForWind() {
            return radius >= LARGE_MIN_RADIUS_FOR_WIND && bluntness >= LARGE_MIN_BLUNTNESS_FOR_WIND;
        }

        private boolean embedRoot(World world, Wind wind) {
            while (radius > 1) {
                BlockPos pos = root;
                int tries = Math.min(10, height());

                for (int i = 0; i < tries; i++) {
                    if (world.getBlockState(pos).getMaterial() == Material.LAVA) {
                        return false;
                    }

                    if (isCircleEmbedded(world, wind.offset(pos), radius)) {
                        root = pos;
                        return true;
                    }

                    pos = pos.offset(pointingUp ? EnumFacing.DOWN : EnumFacing.UP);
                }

                radius /= 2;
            }

            return false;
        }

        private void place(World world, Random random, Wind wind) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    float distance = MathHelper.sqrt(dx * dx + dz * dz);

                    if (distance > radius) {
                        continue;
                    }

                    int height = heightAt(distance);
                    if (height <= 0) {
                        continue;
                    }

                    if (random.nextFloat() < 0.2F) {
                        height = (int) (height * (0.8F + random.nextFloat() * 0.2F));
                    }

                    BlockPos pos = root.add(dx, 0, dz);
                    boolean brokeSurface = false;
                    int maxY = pointingUp ? world.getHeight(pos.getX(), pos.getZ()) : Integer.MAX_VALUE;

                    for (int i = 0; i < height && pos.getY() < maxY; i++) {
                        BlockPos target = wind.offset(pos);
                        IBlockState state = world.getBlockState(target);

                        if (isEmptyOrWaterOrLava(state)) {
                            brokeSurface = true;
                            world.setBlockState(target, DeepslateRegistry.dripstone_block.getDefaultState(), 2);
                        } else if (brokeSurface && isReplaceableStone(state)) {
                            break;
                        }

                        pos = pos.offset(pointingUp ? EnumFacing.UP : EnumFacing.DOWN);
                    }
                }
            }
        }
    }

    private static final class Wind {
        private static final Wind NONE = new Wind();

        private final int originY;
        private final double speedX;
        private final double speedZ;
        private final int maxOffset;
        private final boolean active;

        private Wind(int originY, Random random, float maxSpeed, int maxOffset) {
            float speed = random.nextFloat() * maxSpeed;
            float direction = random.nextFloat() * (float) Math.PI;

            this.originY = originY;
            this.speedX = MathHelper.cos(direction) * speed;
            this.speedZ = MathHelper.sin(direction) * speed;
            this.maxOffset = maxOffset;
            this.active = true;
        }

        private Wind() {
            this.originY = 0;
            this.speedX = 0.0;
            this.speedZ = 0.0;
            this.maxOffset = 0;
            this.active = false;
        }

        private BlockPos offset(BlockPos pos) {
            if (!active) {
                return pos;
            }

            int dy = originY - pos.getY();
            int dx = MathHelper.clamp(MathHelper.floor(speedX * dy), -maxOffset, maxOffset);
            int dz = MathHelper.clamp(MathHelper.floor(speedZ * dy), -maxOffset, maxOffset);

            return (dx == 0 && dz == 0) ? pos : pos.add(dx, 0, dz);
        }
    }
}
