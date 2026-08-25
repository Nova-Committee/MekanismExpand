package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.init.registry.MEXBlocks;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSMultiblockData.CoilData;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SPSMultiblockData.class, remap = false)
public abstract class SPSMultiblockDataMixin {

    @Unique
    private static final long MEX_ANTIMATTER_COIL_SPEED_MULTIPLIER = 5L;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lmekanism/common/config/value/CachedLongValue;get()J"))
    private long adjustEnergyPerInput(CachedLongValue config) {
        long baseEnergy = config.get();
        SPSMultiblockData data = (SPSMultiblockData) (Object) this;
        if (!mex$hasAntimatterCoil(data)) {
            return baseEnergy;
        }
        return Math.max(1L, baseEnergy / MEX_ANTIMATTER_COIL_SPEED_MULTIPLIER);
    }

    @Unique
    private static boolean mex$hasAntimatterCoil(SPSMultiblockData data) {
        if (data.getLevel() == null) {
            return false;
        }
        for (CoilData coil : data.coilData.coilMap.values()) {
            BlockState state = data.getLevel().getBlockState(coil.coilPos.relative(coil.side));
            if (state.is(MEXBlocks.antimatter_supercharged_coil)) {
                return true;
            }
        }
        return false;
    }
}
