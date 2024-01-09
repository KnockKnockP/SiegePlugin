package knockknockp.siegeplugin.Siege;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public final class BlockModification {
    public BlockState originalBlockState;

    public BlockModification(BlockState originalBlockState) {
        this.originalBlockState = originalBlockState;
    }

    public void revert() {
        Block block = originalBlockState.getBlock();
        block.setType(originalBlockState.getType());
        block.setBlockData(originalBlockState.getBlockData());
    }
}