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
        assets.put(
            "assets/minecraft/textures/block/azalea_side.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/azalea_side.png"
        );
        assets.put(
            "assets/minecraft/textures/block/azalea_top.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/azalea_top.png"
        );
        assets.put(
            "assets/minecraft/textures/block/azalea_plant.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/azalea_plant.png"
        );
        assets.put(
            "assets/minecraft/textures/block/flowering_azalea_side.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/flowering_azalea_side.png"
        );
        assets.put(
            "assets/minecraft/textures/block/flowering_azalea_top.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/flowering_azalea_top.png"
        );
        assets.put(
            "assets/minecraft/textures/block/hanging_roots.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/hanging_roots.png"
        );
        assets.put(
            "assets/minecraft/textures/block/big_dripleaf_side.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/big_dripleaf_side.png"
        );
        assets.put(
            "assets/minecraft/textures/block/big_dripleaf_stem.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/big_dripleaf_stem.png"
        );
        assets.put(
            "assets/minecraft/textures/block/big_dripleaf_tip.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/big_dripleaf_tip.png"
        );
        assets.put(
            "assets/minecraft/textures/block/big_dripleaf_top.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/big_dripleaf_top.png"
        );
        assets.put(
            "assets/minecraft/textures/block/small_dripleaf_side.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/small_dripleaf_side.png"
        );
        assets.put(
            "assets/minecraft/textures/block/small_dripleaf_stem_bottom.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/small_dripleaf_stem_bottom.png"
        );
        assets.put(
            "assets/minecraft/textures/block/small_dripleaf_stem_top.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/small_dripleaf_stem_top.png"
        );
        assets.put(
            "assets/minecraft/textures/block/small_dripleaf_top.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/small_dripleaf_top.png"
        );
        assets.put(
            "assets/minecraft/textures/block/spore_blossom.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/spore_blossom.png"
        );
        assets.put(
            "assets/minecraft/textures/block/spore_blossom_base.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/spore_blossom_base.png"
        );
        assets.put(
            "assets/minecraft/textures/block/cave_vines.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/cave_vines.png"
        );
        assets.put(
            "assets/minecraft/textures/block/cave_vines_lit.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/cave_vines_lit.png"
        );
        assets.put(
            "assets/minecraft/textures/block/cave_vines_plant.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/cave_vines_plant.png"
        );
        assets.put(
            "assets/minecraft/textures/block/cave_vines_plant_lit.png",
            "assets/" + Reference.MOD_ID + "/textures/blocks/cave_vines_plant_lit.png"
        );
        assets.put(
            "assets/minecraft/textures/item/glow_berries.png",
            "assets/" + Reference.MOD_ID + "/textures/items/glow_berries.png"
        );

        AssetMoverAPI.fromMinecraft("1.21.11", assets);
    }
}
