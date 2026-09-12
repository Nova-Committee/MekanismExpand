package committee.nova.mek_ex.init.mixin;

import mekanism.api.Upgrade;
import mekanism.common.util.EnumUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EnumUtils.class, remap = false)
public class EnumUtilsMixin {

    @Shadow
    @Final
    @Mutable
    public static Upgrade[] UPGRADES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void clinitInject(CallbackInfo ci) {

        Upgrade.values();
        UPGRADES = Upgrade.values();
    }
}