package sayys.depthsupdate.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.registry.DeepslateRegistry;

public class BlockUtils {
    private static IBlockState cachedDeepslateBlockState;
    private static IBlockState cachedCheeseDebugBlockState;
    private static IBlockState cachedSpaghettiDebugBlockState;
    private static IBlockState cachedRiverDebugBlockState;

    private BlockUtils() {}

    private static final Map<Block, Block> DEEPSLATE_ORE_MAP = new HashMap<>();

    public static void initializeOreMap() {
        DEEPSLATE_ORE_MAP.clear();
        DEEPSLATE_ORE_MAP.put(Blocks.COAL_ORE, DeepslateRegistry.deepslate_coal_ore);
        DEEPSLATE_ORE_MAP.put(Blocks.IRON_ORE, DeepslateRegistry.deepslate_iron_ore);
        DEEPSLATE_ORE_MAP.put(Blocks.GOLD_ORE, DeepslateRegistry.deepslate_gold_ore);
        DEEPSLATE_ORE_MAP.put(Blocks.REDSTONE_ORE, DeepslateRegistry.deepslate_redstone_ore);
        DEEPSLATE_ORE_MAP.put(Blocks.LIT_REDSTONE_ORE, DeepslateRegistry.deepslate_redstone_ore);
        DEEPSLATE_ORE_MAP.put(Blocks.LAPIS_ORE, DeepslateRegistry.deepslate_lapis_ore);
        DEEPSLATE_ORE_MAP.put(Blocks.DIAMOND_ORE, DeepslateRegistry.deepslate_diamond_ore);
        DEEPSLATE_ORE_MAP.put(Blocks.EMERALD_ORE, DeepslateRegistry.deepslate_emerald_ore);
        DEEPSLATE_ORE_MAP.put(DeepslateRegistry.copper_ore, DeepslateRegistry.deepslate_copper_ore);
    }

    private static final Map<Block, Boolean> DEEPSLATE_LOOKUP_CACHE = new ConcurrentHashMap<>();

    public static void clearCaches() {
        cachedDeepslateBlockState = null;
        cachedCheeseDebugBlockState = null;
        cachedSpaghettiDebugBlockState = null;
        cachedRiverDebugBlockState = null;
        DEEPSLATE_LOOKUP_CACHE.clear();
        deepslateOreID = -1;
    }

    public static boolean isRegistered(Block block) {
        return block != null
                && block.getRegistryName() != null
                && Block.REGISTRY.containsKey(block.getRegistryName());
    }

    public static IBlockState getDeepslateBlockState() {
        if (cachedDeepslateBlockState != null) return cachedDeepslateBlockState;

        String blockName = DepthsUpdateConfig.deepslateBlock;
        Block block = Block.getBlockFromName(blockName);

        if (block == null || block == Blocks.AIR) {
            if (DepthsUpdateConfig.REGISTRY.enableDeepslateFamily) {
                cachedDeepslateBlockState = DeepslateRegistry.deepslate.getDefaultState();
            } else {
                cachedDeepslateBlockState = Blocks.STONE.getDefaultState();
            }
        } else {
            cachedDeepslateBlockState = block.getDefaultState();
        }

        return cachedDeepslateBlockState;
    }

    public static IBlockState getDebugBlockState(String blockName, Block fallback, IBlockState currentCache) {
        if (currentCache != null) return currentCache;

        Block block = Block.getBlockFromName(blockName);

        return (block == null || block == Blocks.AIR) ? fallback.getDefaultState() : block.getDefaultState();
    }

    public static IBlockState getCheeseDebugBlockState() {
        cachedCheeseDebugBlockState = getDebugBlockState(DepthsUpdateConfig.DEBUG.cheeseDebugBlock, Blocks.SPONGE, cachedCheeseDebugBlockState);

        return cachedCheeseDebugBlockState;
    }

    public static IBlockState getSpaghettiDebugBlockState() {
        cachedSpaghettiDebugBlockState = getDebugBlockState(DepthsUpdateConfig.DEBUG.spaghettiDebugBlock, Blocks.GLASS, cachedSpaghettiDebugBlockState);

        return cachedSpaghettiDebugBlockState;
    }

    public static IBlockState getRiverDebugBlockState() {
        cachedRiverDebugBlockState = getDebugBlockState(DepthsUpdateConfig.DEBUG.riverDebugBlock, Blocks.GLOWSTONE, cachedRiverDebugBlockState);

        return cachedRiverDebugBlockState;
    }

    public static IBlockState getDeepslateVariant(IBlockState oreState) {
        Block ore = oreState.getBlock();
        Block deepVariant = DEEPSLATE_ORE_MAP.get(ore);

        return deepVariant != null ? deepVariant.getDefaultState() : oreState;
    }

    private static int deepslateOreID = -1;

    /** Equivalent of Vanilla's base_stone_overworld tag, which worldgen features replace into. */
    public static boolean isBaseStone(IBlockState state) {
        if (state == null) return false;

        Block block = state.getBlock();

        return block == Blocks.STONE || block == DeepslateRegistry.tuff || isDeepslate(state);
    }

    public static boolean isDeepslate(IBlockState state) {
        if (state == null) return false;

        Block block = state.getBlock();

        Boolean cached = DEEPSLATE_LOOKUP_CACHE.get(block);

        if (cached != null) {
            return cached;
        }

        boolean result = computeIsDeepslate(block);
        DEEPSLATE_LOOKUP_CACHE.put(block, result);

        return result;
    }

    private static boolean computeIsDeepslate(Block block) {
        if (block == DeepslateRegistry.deepslate) return true;

        if (block.getRegistryName() != null && block.getRegistryName().toString().equals(DepthsUpdateConfig.deepslateBlock)) return true;

        if (deepslateOreID == -1) {
            deepslateOreID = OreDictionary.getOreID("stoneDeepslate");
        }

        if (deepslateOreID != -1) {
            Item item = Item.getItemFromBlock(block);

            if (item != Items.AIR) {
                int[] ids = OreDictionary.getOreIDs(new ItemStack(item));

                for (int id : ids) {
                    if (id == deepslateOreID) return true;
                }
            }
        }

        return false;
    }
}
