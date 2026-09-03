package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    private void standOnElectricSkateboard(LivingEntity entity, float limbSwing, float limbSwingAmount,
          float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player && player.getVehicle() instanceof EntityElectricSkateboard) {
            ((HumanoidModel<?>)(Object) this).riding = false;
        }
    }
}
