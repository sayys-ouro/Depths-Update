package sayys.depthsupdate.registry;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.Reference;

public final class ModCreativeTab {
    private static final String[] ICON_CANDIDATES = {
        "glow_berries", "amethyst_shard", "deepslate", "moss_block", "calcite"
    };

    private static CreativeTabs instance;

    private ModCreativeTab() {}

    public static void init() {
        if (instance == null && DepthsUpdateConfig.REGISTRY.useCustomCreativeTab) {
            instance = new CreativeTabs(Reference.MOD_ID) {
                @Override
                @SideOnly(Side.CLIENT)
                public ItemStack createIcon() {
                    return ModCreativeTab.icon();
                }
            };
        }
    }

    public static CreativeTabs get() {
        return instance;
    }

    @SideOnly(Side.CLIENT)
    private static ItemStack icon() {
        for (String candidate : ICON_CANDIDATES) {
            ResourceLocation name = new ResourceLocation(Reference.MOD_ID, candidate);

            if (Item.REGISTRY.containsKey(name)) {
                return new ItemStack(Item.REGISTRY.getObject(name));
            }
        }

        return new ItemStack(Blocks.STONE);
    }
}
