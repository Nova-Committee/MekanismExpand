package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.common.upgrade.MEXUpgrades;
import mekanism.api.Upgrade;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.generators.common.tile.TileEntityGenerator;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityMekanism.class, remap = false)
public class TileEntityMekanismMixin {

    @Inject(method = "getSupportedUpgrade", at = @At("RETURN"), cancellable = true)
    private void addCapacityUpgrade(CallbackInfoReturnable<Set<Upgrade>> cir) {
        if ((Object) this instanceof TileEntityGenerator && !cir.getReturnValue().contains(MEXUpgrades.capacity())) {
            Set<Upgrade> upgrades = EnumSet.noneOf(Upgrade.class);
            upgrades.addAll(cir.getReturnValue());
            upgrades.add(MEXUpgrades.capacity());
            cir.setReturnValue(Collections.unmodifiableSet(upgrades));
        }
    }
}
