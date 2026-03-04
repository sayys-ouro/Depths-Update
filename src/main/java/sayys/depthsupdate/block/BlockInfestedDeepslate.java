package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sayys.depthsupdate.DepthsUpdateMod;

public class BlockInfestedDeepslate extends BlockRotatedPillar {
    public BlockInfestedDeepslate() {
        super(Material.CLAY);

        this.setRegistryName("depthsupdate", "infested_deepslate");
        this.setTranslationKey("infested_deepslate");
        this.setHardness(0.75F);
        this.setResistance(0.75F);
        this.setSoundType(SoundType.STONE);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops")) {
            EntitySilverfish entitysilverfish = new EntitySilverfish(worldIn);
            entitysilverfish.setLocationAndAngles((double) pos.getX() + 0.5D, (double) pos.getY(),
                    (double) pos.getZ() + 0.5D, 0.0F, 0.0F);
            worldIn.spawnEntity(entitysilverfish);
            entitysilverfish.spawnExplosionParticle();
        }
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(DepthsUpdateMod.RegistrationHandler.deepslate);
    }
}
