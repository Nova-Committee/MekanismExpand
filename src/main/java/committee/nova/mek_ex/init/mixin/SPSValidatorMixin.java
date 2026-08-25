package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.init.registry.MEXBlocks;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import mekanism.common.content.sps.SPSValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SPSValidator.class, remap = false)
public abstract class SPSValidatorMixin {

    @Inject(method = "validateInner", at = @At("HEAD"), cancellable = true)
    private void allowAntimatterCoil(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos,
          CallbackInfoReturnable<Boolean> cir) {
        if (state.is(MEXBlocks.antimatter_supercharged_coil)) {
            cir.setReturnValue(true);
        }
    }
}
