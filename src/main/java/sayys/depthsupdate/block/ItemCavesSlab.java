package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSlab;

public class ItemCavesSlab extends ItemSlab {
    public ItemCavesSlab(Block block, BlockCavesSlab.Half singleSlab, BlockCavesSlab.Double doubleSlab) {
        super(block, singleSlab, doubleSlab);
    }
}
