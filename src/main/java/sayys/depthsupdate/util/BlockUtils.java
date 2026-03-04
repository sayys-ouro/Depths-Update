package sayys.depthsupdate.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import sayys.depthsupdate.DepthsUpdateConfig;

public class BlockUtils {
    private BlockUtils() {}

    public static IBlockState getDeepslateBlockState() {
        String blockName = DepthsUpdateConfig.deepslateBlock;
        Block block = Block.getBlockFromName(blockName);

        if (block == null || block == Blocks.AIR) {
            return sayys.depthsupdate.DepthsUpdateMod.RegistrationHandler.deepslate.getDefaultState();
        }

        return block.getDefaultState();
    }

    public static IBlockState getCheeseDebugBlockState() {
        String blockName = DepthsUpdateConfig.DEBUG.cheeseDebugBlock;
        Block block = Block.getBlockFromName(blockName);

        return (block == null || block == Blocks.AIR) ? Blocks.SPONGE.getDefaultState() : block.getDefaultState();
    }

    public static IBlockState getSpaghettiDebugBlockState() {
        String blockName = DepthsUpdateConfig.DEBUG.spaghettiDebugBlock;
        Block block = Block.getBlockFromName(blockName);

        return (block == null || block == Blocks.AIR) ? Blocks.GLASS.getDefaultState() : block.getDefaultState();
    }

    public static IBlockState getRiverDebugBlockState() {
        String blockName = DepthsUpdateConfig.DEBUG.riverDebugBlock;
        Block block = Block.getBlockFromName(blockName);

        return (block == null || block == Blocks.AIR) ? Blocks.GLOWSTONE.getDefaultState() : block.getDefaultState();
    }
}
