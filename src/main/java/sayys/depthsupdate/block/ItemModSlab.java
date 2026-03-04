package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSlab;

public class ItemModSlab extends ItemSlab {
    public ItemModSlab(Block block, BlockModSlab.Half singleSlab, BlockModSlab.Double doubleSlab) {
        super(block, singleSlab, doubleSlab);
    }
}
