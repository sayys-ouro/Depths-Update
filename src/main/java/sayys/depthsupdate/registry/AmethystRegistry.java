package sayys.depthsupdate.registry;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.Reference;
import sayys.depthsupdate.block.BlockAmethystCluster;
import sayys.depthsupdate.block.BlockBuddingAmethyst;
import sayys.depthsupdate.block.BlockDeepslateVariant;

public class AmethystRegistry {
    public static final Block amethyst_block = new BlockDeepslateVariant("amethyst_block", 1.5F, 1.5F, SoundType.GLASS);
    public static final Block budding_amethyst = new BlockBuddingAmethyst();
    public static final Block small_amethyst_bud = new BlockAmethystCluster("small_amethyst_bud", 3.0F, 8.0F, 1);
    public static final Block medium_amethyst_bud = new BlockAmethystCluster("medium_amethyst_bud", 4.0F, 10.0F, 2);
    public static final Block large_amethyst_bud = new BlockAmethystCluster("large_amethyst_bud", 5.0F, 10.0F, 4);
    public static final Block amethyst_cluster = new BlockAmethystCluster("amethyst_cluster", 7.0F, 10.0F, 5);

    public static final Item amethyst_shard = new Item()
        .setRegistryName(Reference.MOD_ID, "amethyst_shard")
        .setTranslationKey("amethyst_shard")
        .setCreativeTab(CreativeTabs.MATERIALS);

    public static final RegistrationFeature AMETHYST_FEATURE = new RegistrationFeature(
        () -> DepthsUpdateConfig.REGISTRY.enableAmethystFamily
    ).add(
        amethyst_block,
        budding_amethyst,
        small_amethyst_bud,
        medium_amethyst_bud,
        large_amethyst_bud,
        amethyst_cluster,
        amethyst_shard
    );

}
