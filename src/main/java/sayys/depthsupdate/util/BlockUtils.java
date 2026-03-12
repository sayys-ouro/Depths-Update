package sayys.depthsupdate.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.registry.RegistryHandler;

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

    public static IBlockState getDeepslateVariant(IBlockState oreState) {
        Block ore = oreState.getBlock();

        if (ore == Blocks.COAL_ORE) return RegistryHandler.deepslate_coal_ore.getDefaultState();
        if (ore == Blocks.IRON_ORE) return RegistryHandler.deepslate_iron_ore.getDefaultState();
        if (ore == Blocks.GOLD_ORE) return RegistryHandler.deepslate_gold_ore.getDefaultState();
        if (ore == Blocks.REDSTONE_ORE || ore == Blocks.LIT_REDSTONE_ORE) return RegistryHandler.deepslate_redstone_ore.getDefaultState();
        if (ore == Blocks.LAPIS_ORE) return RegistryHandler.deepslate_lapis_ore.getDefaultState();
        if (ore == Blocks.DIAMOND_ORE) return RegistryHandler.deepslate_diamond_ore.getDefaultState();
        if (ore == Blocks.EMERALD_ORE) return RegistryHandler.deepslate_emerald_ore.getDefaultState();
        if (ore == RegistryHandler.copper_ore) return RegistryHandler.deepslate_copper_ore.getDefaultState();

        return oreState;
    }

    public static boolean isDeepslate(IBlockState state) {
        if (state == null) return false;
        Block block = state.getBlock();

        return block == RegistryHandler.deepslate || (block.getRegistryName() != null && block.getRegistryName().toString().equals(DepthsUpdateConfig.deepslateBlock));
    }
}
