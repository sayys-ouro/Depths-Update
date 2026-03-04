package sayys.depthsupdate;

import com.cleanroommc.assetmover.AssetMoverAPI;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import sayys.depthsupdate.block.BlockDeepslate;
import sayys.depthsupdate.block.BlockModSlab;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Reference.VERSION
)
public class DepthsUpdateMod {
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     * Take a look at how many FMLStateEvents you can listen to via
     * the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void construct(@NonNull FMLConstructionEvent event) {
        Map<String, String> assets = new HashMap<>();

        assets.put(
            "assets/minecraft/textures/block/deepslate.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/deepslate.png"
        );
        assets.put(
            "assets/minecraft/textures/block/deepslate_top.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/deepslate_top.png"
        );
        assets.put(
            "assets/minecraft/textures/block/cobbled_deepslate.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/cobbled_deepslate.png"
        );
        assets.put(
            "assets/minecraft/textures/block/polished_deepslate.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/polished_deepslate.png"
        );
        assets.put(
            "assets/minecraft/textures/block/deepslate_bricks.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/deepslate_bricks.png"
        );
        assets.put(
            "assets/minecraft/textures/block/deepslate_tiles.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/deepslate_tiles.png"
        );
        assets.put(
            "assets/minecraft/textures/block/chiseled_deepslate.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/chiseled_deepslate.png"
        );
        assets.put(
            "assets/minecraft/textures/block/cracked_deepslate_bricks.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/cracked_deepslate_bricks.png"
        );
        assets.put(
            "assets/minecraft/textures/block/cracked_deepslate_tiles.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/cracked_deepslate_tiles.png"
        );
        assets.put(
            "assets/minecraft/textures/block/calcite.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/calcite.png"
        );
        assets.put(
            "assets/minecraft/textures/block/dripstone_block.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/dripstone_block.png"
        );
        assets.put(
            "assets/minecraft/textures/block/moss_block.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/moss_block.png"
        );
        assets.put(
            "assets/minecraft/textures/block/rooted_dirt.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/rooted_dirt.png"
        );
        assets.put(
            "assets/minecraft/textures/block/tuff.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/tuff.png"
        );
        assets.put(
            "assets/minecraft/textures/block/amethyst_block.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/amethyst_block.png"
        );
        assets.put(
            "assets/minecraft/textures/block/budding_amethyst.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/budding_amethyst.png"
        );
        assets.put(
            "assets/minecraft/textures/block/smooth_basalt.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/smooth_basalt.png"
        );
        assets.put(
            "assets/minecraft/textures/block/raw_iron_block.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/raw_iron_block.png"
        );
        assets.put(
            "assets/minecraft/textures/block/raw_gold_block.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/raw_gold_block.png"
        );
        assets.put(
            "assets/minecraft/textures/block/raw_copper_block.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/raw_copper_block.png"
        );
        assets.put(
            "assets/minecraft/textures/block/azalea_leaves.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/azalea_leaves.png"
        );
        assets.put(
            "assets/minecraft/textures/block/flowering_azalea_leaves.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/flowering_azalea_leaves.png"
        );

        AssetMoverAPI.fromMinecraft("1.21.11", assets);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class RegistrationHandler {
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
        public static final Block moss_block = new sayys.depthsupdate.block.BlockSimple(
            "moss_block", Material.GRASS, 0.1F, 0.1F, SoundType.PLANT);
        public static final Block rooted_dirt = new sayys.depthsupdate.block.BlockSimple(
            "rooted_dirt", Material.GROUND, 0.5F, 0.5F, SoundType.GROUND);
        public static final Block tuff = new sayys.depthsupdate.block.BlockDeepslateVariant(
            "tuff", 1.5F, 6.0F, SoundType.STONE);
        public static final Block amethyst_block = new sayys.depthsupdate.block.BlockDeepslateVariant(
            "amethyst_block", 1.5F, 1.5F, SoundType.GLASS);
        public static final Block budding_amethyst = new sayys.depthsupdate.block.BlockDeepslateVariant(
            "budding_amethyst", 1.5F, 1.5F, SoundType.GLASS);
        public static final Block moss_carpet = new sayys.depthsupdate.block.BlockMossCarpet();
        public static final Block azalea_leaves = new sayys.depthsupdate.block.BlockModLeaves(
            "azalea_leaves");
        public static final Block flowering_azalea_leaves = new sayys.depthsupdate.block.BlockModLeaves(
            "flowering_azalea_leaves");
        public static final Block smooth_basalt = new sayys.depthsupdate.block.BlockDeepslateVariant(
            "smooth_basalt", 1.25F, 4.2F, SoundType.STONE);
        public static final Block raw_iron_block = new sayys.depthsupdate.block.BlockDeepslateVariant(
            "raw_iron_block", 5.0F, 6.0F, SoundType.STONE);
        public static final Block raw_gold_block = new sayys.depthsupdate.block.BlockDeepslateVariant(
            "raw_gold_block", 5.0F, 6.0F, SoundType.STONE);
        public static final Block raw_copper_block = new sayys.depthsupdate.block.BlockDeepslateVariant(
            "raw_copper_block", 5.0F, 6.0F, SoundType.STONE);
        public static final BlockModSlab.Double deepslate_slab_double = new BlockModSlab.Double(
            "deepslate_slab_double", Material.ROCK);
        public static final BlockModSlab.Half deepslate_slab_half = new BlockModSlab.Half(
            "deepslate_slab_half",
            Material.ROCK, deepslate_slab_double);

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(
                deepslate, cobbled_deepslate, infested_deepslate,
                polished_deepslate, deepslate_bricks, deepslate_tiles, chiseled_deepslate,
                cracked_deepslate_bricks,
                cracked_deepslate_tiles,
                cobbled_deepslate_stairs, polished_deepslate_stairs, deepslate_brick_stairs,
                deepslate_tile_stairs,
                cobbled_deepslate_wall, polished_deepslate_wall, deepslate_brick_wall,
                deepslate_tile_wall,
                calcite, dripstone_block, moss_block, rooted_dirt, tuff,
                amethyst_block, budding_amethyst,
                moss_carpet, azalea_leaves, flowering_azalea_leaves,
                smooth_basalt, raw_iron_block, raw_gold_block, raw_copper_block,
                deepslate_slab_half, deepslate_slab_double);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
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
            registerItemBlock(event, calcite);
            registerItemBlock(event, dripstone_block);
            registerItemBlock(event, moss_block);
            registerItemBlock(event, rooted_dirt);
            registerItemBlock(event, tuff);
            registerItemBlock(event, amethyst_block);
            registerItemBlock(event, budding_amethyst);
            registerItemBlock(event, moss_carpet);
            registerItemBlock(event, azalea_leaves);
            registerItemBlock(event, flowering_azalea_leaves);
            registerItemBlock(event, smooth_basalt);
            registerItemBlock(event, raw_iron_block);
            registerItemBlock(event, raw_gold_block);
            registerItemBlock(event, raw_copper_block);

            event.getRegistry().register(new sayys.depthsupdate.block.ItemModSlab(deepslate_slab_half,
                deepslate_slab_half, deepslate_slab_double)
                .setRegistryName(deepslate_slab_half.getRegistryName()));
        }

        private static void registerItemBlock(RegistryEvent.Register<Item> event, Block block) {
            event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }

        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
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
            registerModel(calcite);
            registerModel(dripstone_block);
            registerModel(moss_block);
            registerModel(rooted_dirt);
            registerModel(tuff);
            registerModel(amethyst_block);
            registerModel(budding_amethyst);
            registerModel(moss_carpet);
            registerModel(azalea_leaves);
            registerModel(flowering_azalea_leaves);
            registerModel(smooth_basalt);
            registerModel(raw_iron_block);
            registerModel(raw_gold_block);
            registerModel(raw_copper_block);

            for (sayys.depthsupdate.block.BlockModSlab.Variant variant : sayys.depthsupdate.block.BlockModSlab.Variant
                .values()) {
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(deepslate_slab_half),
                    variant.getMetadata(),
                    new ModelResourceLocation(
                        Reference.MOD_ID + ":deepslate_slab_"
                            + variant.getName(),
                        "inventory"));
            }
        }

        private static void registerModel(Block block) {
            registerModel(block, "inventory");
        }

        private static void registerModel(Block block, String variant) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
                new ModelResourceLocation(block.getRegistryName(), variant));
        }
    }
}
