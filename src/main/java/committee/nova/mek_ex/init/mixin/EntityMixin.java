package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.client.SonarDetectionClient;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void mek_ex$sonarOutlineColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        if (SonarDetectionClient.isSonarOutlined(self)) {
            cir.setReturnValue(SonarDetectionClient.OUTLINE_COLOR_RGB);
        }
    }
}
