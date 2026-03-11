package sayys.depthsupdate.registry;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import sayys.depthsupdate.Reference;
import sayys.depthsupdate.block.BlockDeepslate;
import sayys.depthsupdate.block.BlockModSlab;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class RegistryHandler {
    public static final Block deepslate = new BlockDeepslate();
    public static final Block cobbled_deepslate = new sayys.depthsupdate.block.BlockCobbledDeepslate();
    public static final Block infested_deepslate = new sayys.depthsupdate.block.BlockInfestedDeepslate();
    public static final Block polished_deepslate = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "polished_deepslate", 3.5F, 6.0F, SoundType.STONE);
    public static final Block deepslate_bricks = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "deepslate_bricks",
        3.5F, 6.0F, SoundType.STONE);
    public static final Block deepslate_tiles = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "deepslate_tiles",
        3.5F, 6.0F, SoundType.STONE);
    public static final Block chiseled_deepslate = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "chiseled_deepslate", 3.5F, 6.0F, SoundType.STONE);
    public static final Block cracked_deepslate_bricks = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "cracked_deepslate_bricks", 3.5F, 6.0F, SoundType.STONE);
    public static final Block cracked_deepslate_tiles = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "cracked_deepslate_tiles", 3.5F, 6.0F, SoundType.STONE);
    public static final Block cobbled_deepslate_stairs = new sayys.depthsupdate.block.BlockModStairs(
        "cobbled_deepslate_stairs", cobbled_deepslate.getDefaultState());
    public static final Block polished_deepslate_stairs = new sayys.depthsupdate.block.BlockModStairs(
        "polished_deepslate_stairs", polished_deepslate.getDefaultState());
    public static final Block deepslate_brick_stairs = new sayys.depthsupdate.block.BlockModStairs(
        "deepslate_brick_stairs", deepslate_bricks.getDefaultState());
    public static final Block deepslate_tile_stairs = new sayys.depthsupdate.block.BlockModStairs(
        "deepslate_tile_stairs", deepslate_tiles.getDefaultState());
    public static final Block cobbled_deepslate_wall = new sayys.depthsupdate.block.BlockModWall(
        "cobbled_deepslate_wall", cobbled_deepslate);
    public static final Block polished_deepslate_wall = new sayys.depthsupdate.block.BlockModWall(
        "polished_deepslate_wall", polished_deepslate);
    public static final Block deepslate_brick_wall = new sayys.depthsupdate.block.BlockModWall(
        "deepslate_brick_wall",
        deepslate_bricks);
    public static final Block deepslate_tile_wall = new sayys.depthsupdate.block.BlockModWall(
        "deepslate_tile_wall",
        deepslate_tiles);
    public static final Block calcite = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "calcite", 0.75F, 0.75F, SoundType.STONE);
    public static final Block dripstone_block = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "dripstone_block", 1.5F, 1.0F, SoundType.STONE);
    public static final Block pointed_dripstone = new sayys.depthsupdate.block.BlockPointedDripstone();
    public static final Block moss_block = new sayys.depthsupdate.block.BlockSimple(
        "moss_block", Material.GRASS, 0.1F, 0.1F, SoundType.PLANT);
    public static final Block rooted_dirt = new sayys.depthsupdate.block.BlockSimple(
        "rooted_dirt", Material.GROUND, 0.5F, 0.5F, SoundType.GROUND);
    public static final Block tuff = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "tuff", 1.5F, 6.0F, SoundType.STONE);
    public static final Block amethyst_block = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "amethyst_block", 1.5F, 1.5F, SoundType.GLASS);
    public static final Block budding_amethyst = new sayys.depthsupdate.block.BlockBuddingAmethyst();
    public static final Block small_amethyst_bud = new sayys.depthsupdate.block.BlockAmethystCluster("small_amethyst_bud", 3.0F, 8.0F, 1);
    public static final Block medium_amethyst_bud = new sayys.depthsupdate.block.BlockAmethystCluster("medium_amethyst_bud", 4.0F, 10.0F, 2);
    public static final Block large_amethyst_bud = new sayys.depthsupdate.block.BlockAmethystCluster("large_amethyst_bud", 5.0F, 10.0F, 4);
    public static final Block amethyst_cluster = new sayys.depthsupdate.block.BlockAmethystCluster("amethyst_cluster", 7.0F, 10.0F, 5);

    public static final Block moss_carpet = new sayys.depthsupdate.block.BlockMossCarpet();
    public static final Block azalea_leaves = new sayys.depthsupdate.block.BlockModLeaves(
        "azalea_leaves");
    public static final Block flowering_azalea_leaves = new sayys.depthsupdate.block.BlockModLeaves(
        "flowering_azalea_leaves");
    public static final Block azalea = new sayys.depthsupdate.block.BlockAzalea("azalea");
    public static final Block flowering_azalea = new sayys.depthsupdate.block.BlockAzalea("flowering_azalea");
    public static final Block hanging_roots = new sayys.depthsupdate.block.BlockHangingRoots();
    public static final Block small_dripleaf = new sayys.depthsupdate.block.BlockSmallDripleaf();
    public static final Block big_dripleaf = new sayys.depthsupdate.block.BlockBigDripleaf();
    public static final Block big_dripleaf_stem = new sayys.depthsupdate.block.BlockBigDripleafStem();
    public static final Block smooth_basalt = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "smooth_basalt", 1.25F, 4.2F, SoundType.STONE);
    public static final Block raw_iron_block = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "raw_iron_block", 5.0F, 6.0F, SoundType.STONE);
    public static final Block raw_gold_block = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "raw_gold_block", 5.0F, 6.0F, SoundType.STONE);
    public static final Block raw_copper_block = new sayys.depthsupdate.block.BlockDeepslateVariant(
        "raw_copper_block", 5.0F, 6.0F, SoundType.STONE);
    public static final Block spore_blossom = new sayys.depthsupdate.block.BlockSporeBlossom();

    public static final Block cave_vines = new sayys.depthsupdate.block.BlockCaveVines();
    public static final Block cave_vines_plant = new sayys.depthsupdate.block.BlockCaveVinesPlant();
    public static final Item glow_berries = new sayys.depthsupdate.item.ItemGlowBerries();
    public static final Item amethyst_shard = new Item().setRegistryName(Reference.MOD_ID, "amethyst_shard").setTranslationKey("amethyst_shard").setCreativeTab(CreativeTabs.MATERIALS);

    public static final BlockModSlab.Double deepslate_slab_double = new BlockModSlab.Double(
        "deepslate_slab_double", Material.ROCK);
    public static final BlockModSlab.Half deepslate_slab_half = new BlockModSlab.Half(
        "deepslate_slab_half",
        Material.ROCK, deepslate_slab_double);

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableDeepslateFamily) {
            event.getRegistry().registerAll(
                deepslate, cobbled_deepslate, infested_deepslate, polished_deepslate,
                deepslate_bricks, deepslate_tiles, chiseled_deepslate, cracked_deepslate_bricks,
                cracked_deepslate_tiles, cobbled_deepslate_stairs, polished_deepslate_stairs, deepslate_brick_stairs,
                deepslate_tile_stairs, cobbled_deepslate_wall, polished_deepslate_wall, deepslate_brick_wall,
                deepslate_tile_wall, deepslate_slab_half, deepslate_slab_double
            );
        }
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableCalcite) event.getRegistry().register(calcite);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableTuff) event.getRegistry().register(tuff);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableSmoothBasalt) event.getRegistry().register(smooth_basalt);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableAmethystFamily) event.getRegistry().registerAll(
            amethyst_block, budding_amethyst, small_amethyst_bud, medium_amethyst_bud, large_amethyst_bud, amethyst_cluster);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableMossFamily) event.getRegistry().registerAll(moss_block, moss_carpet);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableAzaleaFamily) event.getRegistry().registerAll(azalea_leaves, flowering_azalea_leaves, azalea, flowering_azalea, hanging_roots);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableSporeBlossom) event.getRegistry().register(spore_blossom);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableDripstoneBlock) event.getRegistry().registerAll(dripstone_block, pointed_dripstone);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableDripleafFamily) event.getRegistry().registerAll(small_dripleaf, big_dripleaf, big_dripleaf_stem);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableCaveVinesAndBerries) event.getRegistry().registerAll(cave_vines, cave_vines_plant);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableRootedDirt) event.getRegistry().register(rooted_dirt);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableRawOreBlocks) event.getRegistry().registerAll(raw_iron_block, raw_gold_block, raw_copper_block);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableDeepslateFamily) {
            registerItemBlock(event, deepslate);
            registerItemBlock(event, cobbled_deepslate);
            registerItemBlock(event, infested_deepslate);
            registerItemBlock(event, polished_deepslate);
            registerItemBlock(event, deepslate_bricks);
            registerItemBlock(event, deepslate_tiles);
            registerItemBlock(event, chiseled_deepslate);
            registerItemBlock(event, cracked_deepslate_bricks);
            registerItemBlock(event, cracked_deepslate_tiles);
            registerItemBlock(event, cobbled_deepslate_stairs);
            registerItemBlock(event, polished_deepslate_stairs);
            registerItemBlock(event, deepslate_brick_stairs);
            registerItemBlock(event, deepslate_tile_stairs);
            registerItemBlock(event, cobbled_deepslate_wall);
            registerItemBlock(event, polished_deepslate_wall);
            registerItemBlock(event, deepslate_brick_wall);
            registerItemBlock(event, deepslate_tile_wall);
            event.getRegistry().register(new sayys.depthsupdate.block.ItemModSlab(deepslate_slab_half,
                deepslate_slab_half, deepslate_slab_double)
                .setRegistryName(deepslate_slab_half.getRegistryName()));
        }

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableCalcite) registerItemBlock(event, calcite);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableTuff) registerItemBlock(event, tuff);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableSmoothBasalt) registerItemBlock(event, smooth_basalt);

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableAmethystFamily) {
            registerItemBlock(event, amethyst_block);
            registerItemBlock(event, budding_amethyst);
            registerItemBlock(event, small_amethyst_bud);
            registerItemBlock(event, medium_amethyst_bud);
            registerItemBlock(event, large_amethyst_bud);
            registerItemBlock(event, amethyst_cluster);
            event.getRegistry().register(amethyst_shard);
        }
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableMossFamily) {
            registerItemBlock(event, moss_block);
            registerItemBlock(event, moss_carpet);
        }
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableAzaleaFamily) {
            registerItemBlock(event, azalea_leaves);
            registerItemBlock(event, flowering_azalea_leaves);
            registerItemBlock(event, azalea);
            registerItemBlock(event, flowering_azalea);
            registerItemBlock(event, hanging_roots);
        }

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableSporeBlossom) registerItemBlock(event, spore_blossom);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableDripstoneBlock) {
            registerItemBlock(event, dripstone_block);
            registerItemBlock(event, pointed_dripstone);
        }

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableDripleafFamily) {
            registerItemBlock(event, small_dripleaf);
            registerItemBlock(event, big_dripleaf);
        }
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableCaveVinesAndBerries) {
            event.getRegistry().register(glow_berries);
        }
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableRootedDirt) registerItemBlock(event, rooted_dirt);

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableRawOreBlocks) {
            registerItemBlock(event, raw_iron_block);
            registerItemBlock(event, raw_gold_block);
            registerItemBlock(event, raw_copper_block);
        }
    }

    private static void registerItemBlock(RegistryEvent.Register<Item> event, Block block) {
        event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableDeepslateFamily) {
            registerModel(deepslate);
            registerModel(cobbled_deepslate);
            registerModel(infested_deepslate);
            registerModel(polished_deepslate);
            registerModel(deepslate_bricks);
            registerModel(deepslate_tiles);
            registerModel(chiseled_deepslate);
            registerModel(cracked_deepslate_bricks);
            registerModel(cracked_deepslate_tiles);
            registerModel(cobbled_deepslate_stairs);
            registerModel(polished_deepslate_stairs);
            registerModel(deepslate_brick_stairs);
            registerModel(deepslate_tile_stairs);
            registerModel(cobbled_deepslate_wall, "variant=cobblestone");
            registerModel(polished_deepslate_wall, "variant=cobblestone");
            registerModel(deepslate_brick_wall, "variant=cobblestone");
            registerModel(deepslate_tile_wall, "variant=cobblestone");
            for (sayys.depthsupdate.block.BlockModSlab.Variant variant : sayys.depthsupdate.block.BlockModSlab.Variant.values()) {
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(deepslate_slab_half),
                    variant.getMetadata(),
                    new ModelResourceLocation(Reference.MOD_ID + ":deepslate_slab_" + variant.getName(), "inventory"));
            }
        }

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableCalcite) registerModel(calcite);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableTuff) registerModel(tuff);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableSmoothBasalt) registerModel(smooth_basalt);

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableAmethystFamily) {
            registerModel(amethyst_block);
            registerModel(budding_amethyst);
            registerModel(small_amethyst_bud);
            registerModel(medium_amethyst_bud);
            registerModel(large_amethyst_bud);
            registerModel(amethyst_cluster);

            ModelLoader.setCustomModelResourceLocation(amethyst_shard, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(amethyst_shard.getRegistryName(), "inventory"));
        }

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableMossFamily) {
            registerModel(moss_block);
            registerModel(moss_carpet);
        }

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableAzaleaFamily) {
            registerModel(azalea_leaves);
            registerModel(flowering_azalea_leaves);
            registerModel(azalea);
            registerModel(flowering_azalea);
            registerModel(hanging_roots);
        }

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableSporeBlossom) registerModel(spore_blossom);
        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableDripstoneBlock) {
            registerModel(dripstone_block);
            ModelLoader.setCustomStateMapper(pointed_dripstone, (new net.minecraft.client.renderer.block.statemap.StateMap.Builder()).ignore(sayys.depthsupdate.block.BlockPointedDripstone.WATERLOGGED).build());
            registerModel(pointed_dripstone);
        }

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableDripleafFamily) {
            registerModel(small_dripleaf);
            registerModel(big_dripleaf);
        }

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableCaveVinesAndBerries) {
            ModelLoader.setCustomModelResourceLocation(glow_berries, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(glow_berries.getRegistryName(), "inventory"));
        }

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableRootedDirt) registerModel(rooted_dirt);

        if (sayys.depthsupdate.DepthsUpdateConfig.REGISTRY.enableRawOreBlocks) {
            registerModel(raw_iron_block);
            registerModel(raw_gold_block);
            registerModel(raw_copper_block);
        }
    }

    public static void init() {
        OreDictionary.registerOre("cobblestone", cobbled_deepslate);

        GameRegistry.addSmelting(cobbled_deepslate, new ItemStack(deepslate), 0.1f);
        GameRegistry.addSmelting(deepslate_bricks, new ItemStack(cracked_deepslate_bricks), 0.1f);
        GameRegistry.addSmelting(deepslate_tiles, new ItemStack(cracked_deepslate_tiles), 0.1f);
    }

    private static void registerModel(Block block) {
        registerModel(block, "inventory");
    }

    private static void registerModel(Block block, String variant) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
            new ModelResourceLocation(block.getRegistryName(), variant));
    }
}
