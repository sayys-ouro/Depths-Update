package sayys.depthsupdate.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import sayys.depthsupdate.registry.RegistryHandler;

public class BlockBigDripleaf extends Block implements IGrowable {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyEnum<sayys.depthsupdate.block.BlockBigDripleaf.EnumTilt> TILT = PropertyEnum.create("tilt", sayys.depthsupdate.block.BlockBigDripleaf.EnumTilt.class);

    protected static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D);
    protected static final AxisAlignedBB LEAF_COLLISION_NONE = new AxisAlignedBB(0.0D, 0.6875D, 0.0D, 1.0D, 0.9375D, 1.0D);
    protected static final AxisAlignedBB LEAF_COLLISION_PARTIAL = new AxisAlignedBB(0.0D, 0.6875D, 0.0D, 1.0D, 0.8125D, 1.0D);

    public BlockBigDripleaf() {
        super(Material.PLANTS, MapColor.FOLIAGE);

        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TILT, EnumTilt.NONE));
        this.setHardness(0.1F);
        this.setHarvestLevel("axe", 0);
        this.setSoundType(SoundType.PLANT);
        this.setRegistryName("depthsupdate", "big_dripleaf");
        this.setTranslationKey("big_dripleaf");
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        EnumTilt tilt = blockState.getValue(TILT);

        if (tilt == EnumTilt.FULL) {
            return NULL_AABB;
        } else if (tilt == EnumTilt.PARTIAL) {
            return LEAF_COLLISION_PARTIAL;
        } else {
            return LEAF_COLLISION_NONE;
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public net.minecraft.util.BlockRenderLayer getRenderLayer() {
        return net.minecraft.util.BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        IBlockState downState = worldIn.getBlockState(pos.down());
        Block downBlock = downState.getBlock();

        return downBlock == this || (RegistryHandler.big_dripleaf_stem != null && downBlock == RegistryHandler.big_dripleaf_stem) || downState.isSideSolid(worldIn, pos.down(), EnumFacing.UP) || downBlock == Blocks.DIRT || downBlock == Blocks.GRASS || downBlock == Blocks.CLAY || downBlock == Blocks.FARMLAND;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canPlaceBlockAt(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        } else if (worldIn.isBlockPowered(pos)) {
            resetTilt(state, worldIn, pos);
        }
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (!worldIn.isRemote) {
            if (entityIn instanceof IProjectile) {
                this.setTiltAndScheduleTick(state, worldIn, pos, EnumTilt.FULL, SoundEvents.BLOCK_GRASS_BREAK);
            } else if (state.getValue(TILT) == EnumTilt.NONE && canEntityTilt(pos, entityIn) && !worldIn.isBlockPowered(pos)) {
                this.setTiltAndScheduleTick(state, worldIn, pos, EnumTilt.UNSTABLE, null);
            }
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isBlockPowered(pos)) {
            resetTilt(state, worldIn, pos);

            return;
        }

        EnumTilt tilt = state.getValue(TILT);

        if (tilt == EnumTilt.UNSTABLE) {
            this.setTiltAndScheduleTick(state, worldIn, pos, EnumTilt.PARTIAL, SoundEvents.BLOCK_GRASS_BREAK);
        } else if (tilt == EnumTilt.PARTIAL) {
            this.setTiltAndScheduleTick(state, worldIn, pos, EnumTilt.FULL, SoundEvents.BLOCK_GRASS_BREAK);
        } else if (tilt == EnumTilt.FULL) {
            resetTilt(state, worldIn, pos);
        }
    }

    private void setTiltAndScheduleTick(IBlockState state, World world, BlockPos pos, EnumTilt tilt, @Nullable net.minecraft.util.SoundEvent sound) {
        setTilt(state, world, pos, tilt);

        if (sound != null) {
            playTiltSound(world, pos, sound);
        }

        int tickDelay = -1;

        if (tilt == EnumTilt.UNSTABLE) tickDelay = 10;
        else if (tilt == EnumTilt.PARTIAL) tickDelay = 10;
        else if (tilt == EnumTilt.FULL) tickDelay = 100;

        if (tickDelay != -1) {
            world.scheduleUpdate(pos, this, tickDelay);
        }
    }

    private void resetTilt(IBlockState state, World level, BlockPos pos) {
        setTilt(state, level, pos, EnumTilt.NONE);

        if (state.getValue(TILT) != EnumTilt.NONE) {
            playTiltSound(level, pos, SoundEvents.BLOCK_GRASS_PLACE);
        }
    }

    private void setTilt(IBlockState state, World world, BlockPos pos, EnumTilt tilt) {
        world.setBlockState(pos, state.withProperty(TILT, tilt), 2);
    }

    private static boolean canEntityTilt(BlockPos pos, Entity entity) {
        return entity.onGround && entity.posY > (double)((float)pos.getY() + 0.6875F);
    }

    private static void playTiltSound(World level, BlockPos pos, net.minecraft.util.SoundEvent tiltSound) {
        float pitch = 0.8F + level.rand.nextFloat() * 0.4F;
        level.playSound(null, pos, tiltSound, SoundCategory.BLOCKS, 1.0F, pitch);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState belowState = world.getBlockState(pos.down());
        boolean belowIsDripleafPart = belowState.getBlock() == this || (RegistryHandler.big_dripleaf_stem != null && belowState.getBlock() == RegistryHandler.big_dripleaf_stem);

        return this.getDefaultState().withProperty(FACING, belowIsDripleafPart ? belowState.getValue(FACING) : placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(TILT, EnumTilt.values()[(meta >> 2) & 3]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = state.getValue(FACING).getHorizontalIndex();
        i |= (state.getValue(TILT).ordinal() << 2);

        return i;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING, TILT});
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return worldIn.isAirBlock(pos.up());
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        BlockPos upPos = pos.up();

        if (worldIn.isAirBlock(upPos) && RegistryHandler.big_dripleaf_stem != null) {
            worldIn.setBlockState(pos, RegistryHandler.big_dripleaf_stem.getDefaultState().withProperty(FACING, state.getValue(FACING)), 3);
            worldIn.setBlockState(upPos, state, 3);
        }
    }

    public enum EnumTilt implements IStringSerializable {
        NONE("none"),
        UNSTABLE("unstable"),
        PARTIAL("partial"),
        FULL("full");

        private final String name;

        EnumTilt(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
