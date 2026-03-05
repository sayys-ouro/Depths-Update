package sayys.depthsupdate.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import sayys.depthsupdate.DepthsUpdateMod;

public class ItemGlowBerries extends ItemFood {
    public ItemGlowBerries() {
        super(2, 0.4F, false);

        this.setRegistryName("depthsupdate", "glow_berries");
        this.setTranslationKey("glow_berries");
        this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.FOOD);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (facing != EnumFacing.DOWN) {
            return EnumActionResult.PASS;
        }

        BlockPos placePos = pos.down();
        if (itemstack.isEmpty() || !player.canPlayerEdit(placePos, facing, itemstack)) {
            return EnumActionResult.FAIL;
        }

        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        IBlockState placeState = DepthsUpdateMod.RegistrationHandler.cave_vines.getDefaultState();

        if (worldIn.isAirBlock(placePos) && DepthsUpdateMod.RegistrationHandler.cave_vines.canPlaceBlockAt(worldIn, placePos)) {
            worldIn.setBlockState(placePos, placeState, 11);
            worldIn.playSound(player, placePos, DepthsUpdateMod.RegistrationHandler.cave_vines.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, (DepthsUpdateMod.RegistrationHandler.cave_vines.getSoundType().getVolume() + 1.0F) / 2.0F, DepthsUpdateMod.RegistrationHandler.cave_vines.getSoundType().getPitch() * 0.8F);
            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }
}
