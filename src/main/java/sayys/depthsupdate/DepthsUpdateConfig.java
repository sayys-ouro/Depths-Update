package sayys.depthsupdate;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MOD_ID)
public class DepthsUpdateConfig {
    @Config.Name("General")
    public static final General GENERAL = new General();

    public static class General {
        @Config.Name("Generate Underground Rivers")
        // @Config.RequiresWorldRestart
        public boolean generateUndergroundRivers = false;
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Reference.MOD_ID)) {
                ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
