package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.init.registry.MEXBlocks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.tileentity.RenderSPS;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSMultiblockData.CoilData;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderSPS.class, remap = false)
public abstract class RenderSPSMixin {

    private static final Color ANTIMATTER_BOLT_COLOR = Color.rgbad(0.78F, 0.12F, 1F, 0.9F);

    @Inject(method = "getBoltFromData", at = @At("HEAD"), cancellable = true)
    private static void renderAntimatterCoilBolt(CoilData data, BlockPos pos, Vec3 center, CallbackInfoReturnable<BoltEffect> cir) {
        if (Minecraft.getInstance().level == null || !Minecraft.getInstance().level.getBlockState(data.coilPos.relative(data.side)).is(MEXBlocks.antimatter_supercharged_coil)) {
            return;
        }
        Vec3 start = data.coilPos.relative(data.side).getCenter();
        start = start.add(Vec3.atLowerCornerOf(data.side.getNormal()).scale(0.5));
        int count = 1 + (data.prevLevel - 1) / 2;
        float size = 0.01F * data.prevLevel;
        BoltEffect bolt = new BoltEffect(BoltRenderInfo.electricity().color(ANTIMATTER_BOLT_COLOR),
              start.subtract(pos.getX(), pos.getY(), pos.getZ()), center, 15)
              .count(count).size(size).lifespan(8).spawn(BoltEffect.SpawnFunction.delay(4));
        cir.setReturnValue(bolt);
    }

    @WrapOperation(method = "render", at = @At(value = "NEW", target = "Lmekanism/common/lib/effect/BoltEffect;"))
    private static BoltEffect renderAntimatterInternalBolt(BoltRenderInfo renderInfo, Vec3 start, Vec3 end, int segments,
          Operation<BoltEffect> original, TileEntitySPSCasing tile, SPSMultiblockData multiblock, float partialTick,
          PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        if (mex$hasAntimatterCoil(multiblock)) {
            renderInfo = BoltRenderInfo.electricity().color(ANTIMATTER_BOLT_COLOR);
        }
        return original.call(renderInfo, start, end, segments);
    }

    private static boolean mex$hasAntimatterCoil(SPSMultiblockData multiblock) {
        if (multiblock.getLevel() == null) {
            return false;
        }
        for (CoilData coil : multiblock.coilData.coilMap.values()) {
            BlockState state = multiblock.getLevel().getBlockState(coil.coilPos.relative(coil.side));
            if (state.is(MEXBlocks.antimatter_supercharged_coil)) {
                return true;
            }
        }
        return false;
    }
}
