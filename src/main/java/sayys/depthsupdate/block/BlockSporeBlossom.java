package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sayys.depthsupdate.client.particle.ParticleSporeBlossomAir;
import sayys.depthsupdate.client.particle.ParticleSporeBlossomFall;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockSporeBlossom extends BlockBush implements IShearable {
    protected static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.125D, 0.1875D, 0.125D, 0.875D, 1.0D, 0.875D);

    public BlockSporeBlossom() {
        super(Material.PLANTS, MapColor.FOLIAGE);

        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.setRegistryName("depthsupdate", "spore_blossom");
        this.setTranslationKey("spore_blossom");
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        IBlockState upState = worldIn.getBlockState(pos.up());
        Block upBlock = upState.getBlock();
        return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos) && upState.isSideSolid(worldIn, pos.up(), EnumFacing.DOWN) && upBlock != Blocks.LEAVES && upBlock != Blocks.LEAVES2;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState();
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        IBlockState upState = worldIn.getBlockState(pos.up());
        Block upBlock = upState.getBlock();
        return upState.isSideSolid(worldIn, pos.up(), EnumFacing.DOWN) && upBlock != Blocks.LEAVES && upBlock != Blocks.LEAVES2;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        double xFalling = (double)x + rand.nextDouble();
        double yFalling = (double)y + 0.7D;
        double zFalling = (double)z + rand.nextDouble();

        ParticleSporeBlossomFall fallParticle = new ParticleSporeBlossomFall(worldIn, xFalling, yFalling, zFalling);
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(fallParticle);

        BlockPos.MutableBlockPos ambientPos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 14; ++i) {
            int cx = x + MathHelper.getInt(rand, -10, 10);
            int cy = y - rand.nextInt(10);
            int cz = z + MathHelper.getInt(rand, -10, 10);

            ambientPos.setPos(cx, cy, cz);
            if (worldIn.isAirBlock(ambientPos)) {
                double pa_x = cx + rand.nextDouble();
                double pa_y = cy + rand.nextDouble();
                double pa_z = cz + rand.nextDouble();
                ParticleSporeBlossomAir airParticle = new ParticleSporeBlossomAir(worldIn, pa_x, pa_y, pa_z, 0.0D, -0.800000011920929D, 0.0D);
                FMLClientHandler.instance().getClient().effectRenderer.addEffect(airParticle);
            }
        }
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return Collections.singletonList(new ItemStack(this, 1));
    }
}
