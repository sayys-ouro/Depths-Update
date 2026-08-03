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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.registry.DeepslateRegistry;

public class BlockUtils {
    private static final Logger LOGGER = LogManager.getLogger("DepthsUpdate/BlockUtils");

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

    public static IBlockState parseBlockState(String spec) {
        BlockSpec parsed = BlockSpec.parse(spec);
        Block block = Block.getBlockFromName(parsed.name());

        if (block == null || block == Blocks.AIR) {
            return null;
        }

        if (!parsed.hasMeta()) {
            return block.getDefaultState();
        }

        IBlockState state;

        try {
            state = block.getStateFromMeta(parsed.meta());
        } catch (RuntimeException unsupported) {
            LOGGER.warn("Block {} rejected metadata {}, using its default state", parsed.name(), parsed.meta());

            return block.getDefaultState();
        }

        if (block.getMetaFromState(state) != parsed.meta()) {
            LOGGER.warn("Block {} has no metadata {}, using its default state", parsed.name(), parsed.meta());

            return block.getDefaultState();
        }

        return state;
    }

    public static IBlockState getDeepslateBlockState() {
        if (cachedDeepslateBlockState != null) return cachedDeepslateBlockState;

        IBlockState configured = parseBlockState(DepthsUpdateConfig.deepslateBlock);

        if (configured == null) {
            cachedDeepslateBlockState = DepthsUpdateConfig.REGISTRY.enableDeepslateFamily
                    ? DeepslateRegistry.deepslate.getDefaultState()
                    : Blocks.STONE.getDefaultState();
        } else {
            cachedDeepslateBlockState = configured;
        }

        return cachedDeepslateBlockState;
    }

    public static IBlockState getDebugBlockState(String blockName, Block fallback, IBlockState currentCache) {
        if (currentCache != null) return currentCache;

        IBlockState state = parseBlockState(blockName);

        return state != null ? state : fallback.getDefaultState();
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
        IBlockState configured = getDeepslateBlockState();
        Block configuredBlock = configured.getBlock();

        if (block == configuredBlock) {
            return configured == configuredBlock.getDefaultState() || state == configured;
        }

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
