package committee.nova.mek_ex.common.block.entity;

import committee.nova.mek_ex.init.enums.MEXWindTier;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityEliteWindGenerator extends AbstractWindGenerator {

    public TileEntityEliteWindGenerator(BlockPos pos, BlockState state) {
        super(MEXBlocks.elite_wind_generator, pos, state, MEXWindTier.ELITE::getGeneratorConstructorOutput);
    }
}
