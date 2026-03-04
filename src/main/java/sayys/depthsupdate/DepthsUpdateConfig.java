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
}
