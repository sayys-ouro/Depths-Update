package sayys.depthsupdate;

import com.cleanroommc.configanytime.ConfigAnytime;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;

@Config(modid = Reference.MOD_ID)
public class DepthsUpdateConfig {
    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Reference.MOD_ID)) {
                ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
                BlockUtils.clearCaches();
                HeightManager.initialize();
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

    @Config.Name("Height Extension")
    @Config.Comment("Settings for extended world height.")
    public static final HeightExtension heightExtension = new HeightExtension();

    public static class HeightExtension {
        @Config.Name("Global Minimum Y")
        @Config.Comment("The minimum Y coordinate for extended dimensions. Must be a multiple of 16.")
        @Config.RangeInt(min = -2048, max = 0)
        @Config.RequiresMcRestart
        public int globalMinY = -64;

        @Config.Name("Global Maximum Y")
        @Config.Comment("The maximum Y coordinate for extended dimensions. Must be a multiple of 16.")
        @Config.RangeInt(min = 256, max = 2048)
        @Config.RequiresMcRestart
        public int globalMaxY = 320;

        @Config.Name("Extended Dimensions")
        @Config.Comment("Dimension IDs to apply height extension to. Default: [0] (Overworld only).")
        @Config.RequiresMcRestart
        public int[] extendedDimensions = {0};

        @Config.Name("Dimension Overrides")
        @Config.Comment({
                "Per-dimension height overrides.",
                "Format: \"dimId:minY:maxY\" or \"dimId:minY:maxY:lavaLevel:voidDamageLevel\"",
                "Example: \"-1:-64:256\" extends the Nether to -64..256.",
                "Example: \"0:-64:320:-54:-128\" sets custom lava/void levels for the Overworld.",
                "Overrides globalMinY/globalMaxY (and optionally lava/void levels) for the specified dimension."
        })
        @Config.RequiresMcRestart
        public String[] dimensionOverrides = {};

        @Config.Name("Sea Level")
        @Config.Comment("The sea level Y coordinate. Used by terrain generation and API queries.")
        public int seaLevel = 63;

        @Config.Name("Lava Level")
        @Config.Comment("The Y level at which underground air is replaced with lava.")
        public int lavaLevel = -54;

        @Config.Name("Void Damage Level")
        @Config.Comment("The Y level at which players start taking void damage.")
        public int voidDamageLevel = -128;

        @Config.Name("Convert Old Worlds")
        @Config.Comment("When loading chunks from a non-extended world, fill below Y=0 with stone.")
        public boolean convertOldWorlds = true;
    }

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

        @Config.Name("Enable Spyglass")
        public boolean enableSpyglass = true;
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

    @Config.Name("Amethyst Geodes")
    public static final AmethystGeodes amethystGeodes = new AmethystGeodes();

    public static class AmethystGeodes {
        @Config.Name("Enable Amethyst Geodes")
        @Config.RequiresMcRestart
        public boolean enableAmethystGeodes = true;

        @Config.Name("Geode Rarity")
        @Config.Comment("The rarity of Amethyst Geodes. Higher numbers mean rarer geodes. (1 in X chance per chunk)")
        @Config.RangeInt(min = 1, max = 1000)
        public int geodeRarity = 24;

        @Config.Name("Minimum Height")
        public int geodeMinY = -58;

        @Config.Name("Maximum Height")
        public int geodeMaxY = 30;
    }

    @Config.Name("Aquifers")
    public static final Aquifers aquifers = new Aquifers();

    public static class Aquifers {
        @Config.Name("Enable Aquifers")
        @Config.RequiresMcRestart
        public boolean enableAquifers = false;
    }

    @Config.Name("Modern World Generation")
    @Config.Comment("Caves & Cliffs style world generation using density functions.")
    public static final ModernWorldGen modernWorldGen = new ModernWorldGen();

    public static class ModernWorldGen {
        @Config.Name("Enable Modern World Gen")
        @Config.Comment("Replaces vanilla terrain generation with C&C density function pipeline. EXPERIMENTAL.")
        @Config.RequiresMcRestart
        public boolean enableModernWorldGen = false;

        @Config.Name("Large Biomes")
        @Config.RequiresMcRestart
        public boolean largeBiomes = false;

        @Config.Name("Amplified")
        @Config.RequiresMcRestart
        public boolean amplified = false;

        @Config.Name("Enable Cheese Caves")
        @Config.Comment("Generate large open caverns (cheese caves) in the modern pipeline.")
        @Config.RequiresMcRestart
        public boolean enableCheeseCaves = true;

        @Config.Name("Enable Spaghetti Caves")
        @Config.Comment("Generate spaghetti-shaped tunnels in the modern pipeline.")
        @Config.RequiresMcRestart
        public boolean enableSpaghettiCaves = true;

        @Config.Name("Enable Noodle Caves")
        @Config.Comment("Generate narrow noodle-shaped tunnels in the modern pipeline.")
        @Config.RequiresMcRestart
        public boolean enableNoodleCaves = true;

        @Config.Name("Enable Aquifers")
        @Config.Comment("Generate noise-based underground water/lava aquifers in the modern pipeline.")
        @Config.RequiresMcRestart
        public boolean enableAquifers = true;
    }

    static {
        ConfigAnytime.register(DepthsUpdateConfig.class);
    }
}
