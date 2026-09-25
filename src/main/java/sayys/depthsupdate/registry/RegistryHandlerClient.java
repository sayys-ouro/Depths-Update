package sayys.depthsupdate.registry;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import sayys.depthsupdate.Reference;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Side.CLIENT)
public class RegistryHandlerClient {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        RegistryHandler.registerModelsCommon(event);
    }
}
