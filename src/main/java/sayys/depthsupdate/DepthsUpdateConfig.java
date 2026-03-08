package sayys.depthsupdate;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MOD_ID)
public class DepthsUpdateConfig {
    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Reference.MOD_ID)) {
                ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
                sayys.depthsupdate.util.BlockUtils.clearCaches();
            }
        }
    }

    public static class Debug {
        @Config.Name("Enable Debug Visualizer")
        @Config.Comment("Outlines cave and river borders with distinctive blocks.")
        public boolean enableDebugVisualizers = false;

        @Config.Name("Cheese Cave Debug Block")
        public String cheeseDebugBlock = "minecraft:sponge";

        @Config.Name("Spaghetti Cave Debug Block")
        public String spaghettiDebugBlock = "minecraft:glass";

        @Config.Name("River Debug Block")
        public String riverDebugBlock = "minecraft:glowstone";
    }

    @Config.Name("Debug")
    @Config.Comment("Debug Settings")
    public static final Debug DEBUG = new Debug();

    @Config.Name("Generate Underground Rivers")
    public static boolean generateUndergroundRivers = false;

    @Config.Name("Generate Cheese Caves")
    public static boolean generateCheeseCaves = false;

    @Config.Name("Generate Spaghetti Caves")
    public static boolean generateSpaghettiCaves = true;

    @Config.Name("Deepslate Max Y")
    public static int deepslateMaxY = 0;

    @Config.Name("Deepslate Transition Range")
    @Config.Comment("The number of blocks over which stone transitions into Deepslate.")
    public static int deepslateTransitionRange = 8;

    @Config.Name("Deepslate Block")
    @Config.Comment("The registry name of the block to use as 'Deepslate'.")
    public static String deepslateBlock = "depthsupdate:deepslate";

    public static class Registry {
        @Config.Name("Enable Deepslate")
        @Config.Comment("Enables Deepslate, Cobbled Deepslate, Bricks, Tiles, Stairs, Walls, Slabs, and Infested Deepslate.")
        public boolean enableDeepslateFamily = true;

        @Config.Name("Enable Calcite")
        public boolean enableCalcite = true;

        @Config.Name("Enable Tuff")
        public boolean enableTuff = true;

        @Config.Name("Enable Smooth Basalt")
        public boolean enableSmoothBasalt = true;

        @Config.Name("Enable Amethyst")
        @Config.Comment("Enables Amethyst Blocks and Budding Amethyst.")
        public boolean enableAmethystFamily = true;

        @Config.Name("Enable Moss")
        @Config.Comment("Enables Moss Blocks and Moss Carpets.")
        public boolean enableMossFamily = true;

        @Config.Name("Enable Azalea")
        @Config.Comment("Enables Azalea, Flowering Azalea, Leaves, and Hanging Roots.")
        public boolean enableAzaleaFamily = true;

        @Config.Name("Enable Spore Blossom")
        public boolean enableSporeBlossom = true;

        @Config.Name("Enable Dripstone Block")
        public boolean enableDripstoneBlock = true;

        @Config.Name("Enable Dripleaf")
        @Config.Comment("Enables Small Dripleaf, Big Dripleaf, and Stems.")
        public boolean enableDripleafFamily = true;

        @Config.Name("Enable Cave Vines and Glow Berries")
        public boolean enableCaveVinesAndBerries = true;

        @Config.Name("Enable Rooted Dirt")
        public boolean enableRootedDirt = true;

        @Config.Name("Enable Raw Ore Blocks")
        @Config.Comment("Enables Raw Iron, Raw Gold, and Raw Copper blocks.")
        public boolean enableRawOreBlocks = true;
    }

    @Config.Name("Registry")
    @Config.Comment("Registry Toggles for blocks and items.")
    public static final Registry REGISTRY = new Registry();

    @Config.Name("Lush Caves")
    @Config.Comment("Settings related to the Lush Caves biome.")
    public static final LushCaves lushCaves = new LushCaves();

    public static class LushCaves {
        @Config.Name("Enable Lush Caves")
        @Config.Comment("Allow Lush Caves to generate underground.")
        @Config.RequiresMcRestart
        public boolean enableLushCaves = true;

        @Config.Name("Lush Caves Rarity")
        @Config.Comment("The rarity of Lush Caves. Higher numbers mean rarer caves. (1 in X chance per chunk)")
        @Config.RangeInt(min = 1, max = 1000)
        public int lushCavesRarity = 12;

        @Config.Name("Minimum Height")
        public int lushCavesMinY = -64;

        @Config.Name("Maximum Height")
        public int lushCavesMaxY = 63;

        @Config.Name("Radius Base Size")
        @Config.RangeInt(min = 1, max = 100)
        public int lushCavesRadiusBase = 16;

        @Config.Name("Radius Variation")
        @Config.RangeInt(min = 0, max = 100)
        public int lushCavesRadiusVariation = 8;

        @Config.Name("Height Base Size")
        @Config.RangeInt(min = 1, max = 100)
        public int lushCavesHeightBase = 8;

        @Config.Name("Height Variation")
        @Config.RangeInt(min = 0, max = 100)
        public int lushCavesHeightVariation = 6;
    }

    @Config.Name("Dripstone Caves")
    @Config.Comment("Settings related to the Dripstone Caves biome.")
    public static final DripstoneCaves dripstoneCaves = new DripstoneCaves();

    public static class DripstoneCaves {
        @Config.Name("Enable Dripstone Caves")
        @Config.Comment("Allow Dripstone Caves to generate underground.")
        @Config.RequiresMcRestart
        public boolean enableDripstoneCaves = true;

        @Config.Name("Dripstone Caves Rarity")
        @Config.Comment("The rarity of Dripstone Caves. Higher numbers mean rarer caves. (1 in X chance per chunk)")
        @Config.RangeInt(min = 1, max = 1000)
        public int dripstoneCavesRarity = 15;

        @Config.Name("Minimum Height")
        public int dripstoneCavesMinY = -64;

        @Config.Name("Maximum Height")
        public int dripstoneCavesMaxY = 63;

        @Config.Name("Radius Base Size")
        @Config.RangeInt(min = 1, max = 100)
        public int dripstoneCavesRadiusBase = 20;

        @Config.Name("Radius Variation")
        @Config.RangeInt(min = 0, max = 100)
        public int dripstoneCavesRadiusVariation = 10;

        @Config.Name("Height Base Size")
        @Config.RangeInt(min = 1, max = 100)
        public int dripstoneCavesHeightBase = 12;

        @Config.Name("Height Variation")
        @Config.RangeInt(min = 0, max = 100)
        public int dripstoneCavesHeightVariation = 8;
    }
}
