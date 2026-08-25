package committee.nova.mek_ex.common.multiblock;

import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import committee.nova.mek_ex.init.registry.MEXBlockTypes;


public final class NuclearControlTankValidator extends CuboidStructureValidator<NuclearControlTankMultiblockData> {

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, MEXBlockTypes.NUCLEAR_CONTROL_TANK)) {
            return CasingType.FRAME;
        }
        if (BlockType.is(block, MEXBlockTypes.NUCLEAR_CONTROL_VALVE)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }
}
