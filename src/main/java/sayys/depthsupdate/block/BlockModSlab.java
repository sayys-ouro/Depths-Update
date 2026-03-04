package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

public abstract class BlockModSlab extends BlockSlab {
    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

    public BlockModSlab(String name, Material material) {
        super(material);

        this.setRegistryName("depthsupdate", name);
        this.setTranslationKey(name);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        IBlockState iblockstate = this.blockState.getBaseState();

        if (!this.isDouble()) {
            iblockstate = iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
        }

        this.setDefaultState(iblockstate.withProperty(VARIANT, Variant.COBBLED));
        this.useNeighborBrightness = true;
    }

    @Override
    public String getTranslationKey(int meta) {
        return super.getTranslationKey() + "." + Variant.byMetadata(meta & 7).getName();
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return Variant.byMetadata(stack.getMetadata() & 7);
    }

    public static enum Variant implements IStringSerializable {
        COBBLED(0, "cobbled"),
        POLISHED(1, "polished"),
        BRICKS(2, "bricks"),
        TILES(3, "tiles");

        private final int meta;
        private final String name;

        private Variant(int meta, String name) {
            this.meta = meta;
            this.name = name;
        }

        public int getMetadata() {
            return this.meta;
        }

        @Override
        public String getName() {
            return this.name;
        }

        public static Variant byMetadata(int meta) {
            if (meta < 0 || meta >= values().length)
                meta = 0;
            return values()[meta];
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, Variant.byMetadata(meta & 7));

        if (!this.isDouble()) {
            iblockstate = iblockstate.withProperty(HALF,
                    (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        }

        return iblockstate;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = state.getValue(VARIANT).getMetadata();

        if (!this.isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
            i |= 8;
        }

        return i;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, net.minecraft.util.NonNullList<ItemStack> items) {
        for (Variant variant : Variant.values()) {
            items.add(new ItemStack(this, 1, variant.getMetadata()));
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return this.isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
    }

    public static class Half extends BlockModSlab {
        private final Block doubleSlab;

        public Half(String name, Material material, Block doubleSlab) {
            super(name, material);
            this.doubleSlab = doubleSlab;
        }

        @Override
        public boolean isDouble() {
            return false;
        }

        public Block getDoubleSlab() {
            return doubleSlab;
        }
    }

    public static class Double extends BlockModSlab {
        public Double(String name, Material material) {
            super(name, material);
        }

        @Override
        public boolean isDouble() {
            return true;
        }
    }
}
