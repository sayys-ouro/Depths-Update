package sayys.depthsupdate.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import sayys.depthsupdate.Reference;
import sayys.depthsupdate.mixin.IMixinBlock;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class RegistryHandler {
    private static final List<RegistrationFeature> FEATURES = new ArrayList<>();

    private static void applyNeighborBrightness(IForgeRegistry<Block> registry) {
        for (Block block : registry) {
            ResourceLocation name = block.getRegistryName();

            if (name == null || !Reference.MOD_ID.equals(name.getNamespace())) {
                continue;
            }

            IMixinBlock access = (IMixinBlock) block;

            boolean useNeighborBrightness = block instanceof BlockStairs
                    || block instanceof BlockSlab
                    || access.depthsupdate$isTranslucent()
                    || access.depthsupdate$getLightOpacity() == 0;

            access.depthsupdate$setUseNeighborBrightness(useNeighborBrightness);
        }
    }

    private static void applyCreativeTab(Iterable<? extends net.minecraftforge.registries.IForgeRegistryEntry<?>> entries) {
        CreativeTabs tab = ModCreativeTab.get();

        if (tab == null) {
            return;
        }

        for (net.minecraftforge.registries.IForgeRegistryEntry<?> entry : entries) {
            ResourceLocation name = entry.getRegistryName();

            if (name == null || !Reference.MOD_ID.equals(name.getNamespace())) {
                continue;
            }

            if (entry instanceof Block block) {
                if (block.getCreativeTab() != null) {
                    block.setCreativeTab(tab);
                }
            } else if (entry instanceof ItemBlock) {
                continue;
            } else if (entry instanceof Item item && item.getCreativeTab() != null) {
                item.setCreativeTab(tab);
            }
        }
    }

    static {
        FEATURES.add(DeepslateRegistry.DEEPSLATE_FAMILY);
        FEATURES.add(DeepslateRegistry.DRIPSTONE_FEATURE);
        FEATURES.add(DeepslateRegistry.RAW_ORE_BLOCK_FEATURE);
        FEATURES.add(DeepslateRegistry.CALCITE_FEATURE);
        FEATURES.add(DeepslateRegistry.TUFF_FEATURE);
        FEATURES.add(DeepslateRegistry.SMOOTH_BASALT_FEATURE);
        FEATURES.add(AmethystRegistry.AMETHYST_FEATURE);
        FEATURES.add(PlantRegistry.MOSS_FEATURE);
        FEATURES.add(PlantRegistry.ROOTED_DIRT_FEATURE);
        FEATURES.add(PlantRegistry.AZALEA_FEATURE);
        FEATURES.add(PlantRegistry.SPORE_BLOSSOM_FEATURE);
        FEATURES.add(PlantRegistry.DRIPLEAF_FEATURE);
        FEATURES.add(PlantRegistry.VINE_FEATURE);
        FEATURES.add(StandaloneRegistry.SPYGLASS_FEATURE);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ModCreativeTab.init();

        FEATURES.forEach(f -> f.registerBlocks(event));
        applyNeighborBrightness(event.getRegistry());
        applyCreativeTab(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        FEATURES.forEach(f -> f.registerItems(event));
        applyCreativeTab(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        FEATURES.forEach(f -> f.registerSounds(event));
    }

    @SideOnly(Side.CLIENT)
    public static void registerModelsCommon(ModelRegistryEvent event) {
        FEATURES.forEach(f -> f.registerModels(event));
    }

    public static void init() {
        FEATURES.forEach(RegistrationFeature::init);
    }
}
