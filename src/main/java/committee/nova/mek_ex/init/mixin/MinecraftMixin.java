package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.client.SonarDetectionClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void mek_ex$sonarEntityOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (SonarDetectionClient.isSonarOutlined(entity)) {
            cir.setReturnValue(true);
        }
    }
}
