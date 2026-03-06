package sayys.depthsupdate.util;

import sayys.depthsupdate.registry.RegistryHandler;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import sayys.depthsupdate.DepthsUpdateConfig;

public class BlockUtils {
    private static IBlockState cachedDeepslateBlockState;
    private static IBlockState cachedCheeseDebugBlockState;
    private static IBlockState cachedSpaghettiDebugBlockState;
    private static IBlockState cachedRiverDebugBlockState;

    private BlockUtils() {}

    public static void clearCaches() {
        cachedDeepslateBlockState = null;
        cachedCheeseDebugBlockState = null;
        cachedSpaghettiDebugBlockState = null;
        cachedRiverDebugBlockState = null;
    }

    public static IBlockState getDeepslateBlockState() {
        if (cachedDeepslateBlockState != null) return cachedDeepslateBlockState;

        String blockName = DepthsUpdateConfig.deepslateBlock;
        Block block = Block.getBlockFromName(blockName);

        if (block == null || block == Blocks.AIR) {
            cachedDeepslateBlockState = RegistryHandler.deepslate.getDefaultState();
        } else {
            cachedDeepslateBlockState = block.getDefaultState();
        }
        return cachedDeepslateBlockState;
    }

    public static IBlockState getCheeseDebugBlockState() {
        if (cachedCheeseDebugBlockState != null) return cachedCheeseDebugBlockState;

        String blockName = DepthsUpdateConfig.DEBUG.cheeseDebugBlock;
        Block block = Block.getBlockFromName(blockName);

        cachedCheeseDebugBlockState = (block == null || block == Blocks.AIR) ? Blocks.SPONGE.getDefaultState() : block.getDefaultState();
        return cachedCheeseDebugBlockState;
    }

    public static IBlockState getSpaghettiDebugBlockState() {
        if (cachedSpaghettiDebugBlockState != null) return cachedSpaghettiDebugBlockState;

        String blockName = DepthsUpdateConfig.DEBUG.spaghettiDebugBlock;
        Block block = Block.getBlockFromName(blockName);

        cachedSpaghettiDebugBlockState = (block == null || block == Blocks.AIR) ? Blocks.GLASS.getDefaultState() : block.getDefaultState();
        return cachedSpaghettiDebugBlockState;
    }

    public static IBlockState getRiverDebugBlockState() {
        if (cachedRiverDebugBlockState != null) return cachedRiverDebugBlockState;

        String blockName = DepthsUpdateConfig.DEBUG.riverDebugBlock;
        Block block = Block.getBlockFromName(blockName);

        cachedRiverDebugBlockState = (block == null || block == Blocks.AIR) ? Blocks.GLOWSTONE.getDefaultState() : block.getDefaultState();
        return cachedRiverDebugBlockState;
    }
}
