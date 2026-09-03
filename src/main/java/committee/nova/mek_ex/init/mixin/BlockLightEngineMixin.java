package committee.nova.mek_ex.init.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import committee.nova.mek_ex.client.util.ESBLightUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineMixin {

    @WrapOperation(
          method = "getEmission",
          at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"
          )
    )
    private int includeElectricSkateboardLight(BlockState state, BlockGetter level, BlockPos pos, Operation<Integer> original) {
        return Math.max(original.call(state, level, pos), ESBLightUtil.getLightEmission(level, pos));
    }
}
