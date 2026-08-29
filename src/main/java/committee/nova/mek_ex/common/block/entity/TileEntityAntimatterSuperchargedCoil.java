package committee.nova.mek_ex.common.block.entity;

import committee.nova.mek_ex.init.registry.MEXBlocks;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;


public class TileEntityAntimatterSuperchargedCoil extends TileEntityInternalMultiblock {

    public TileEntityAntimatterSuperchargedCoil(BlockPos pos, BlockState state) {
        super(MEXBlocks.antimatter_supercharged_coil, pos, state);
    }
}
