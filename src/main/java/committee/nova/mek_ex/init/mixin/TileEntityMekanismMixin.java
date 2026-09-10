package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.common.upgrade.MEXUpgrades;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.generators.common.tile.TileEntityGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityMekanism.class, remap = false)
public class TileEntityMekanismMixin {

    @Inject(method = "getSupportedUpgrade", at = @At("RETURN"), cancellable = true)
    private void mek_ex$addCustomUpgrades(CallbackInfoReturnable<Set<Upgrade>> cir) {
        Object self = this;
        Set<Upgrade> current = cir.getReturnValue();
        if (current == null) {
            return;
        }

        boolean addCapacity = self instanceof TileEntityGenerator && !current.contains(MEXUpgrades.capacity());
        boolean addVoid = self instanceof TileEntityDigitalMiner && !current.contains(MEXUpgrades.voidUpgrade());
        if (!addCapacity && !addVoid) {
            return;
        }

        Set<Upgrade> upgrades = EnumSet.noneOf(Upgrade.class);
        upgrades.addAll(current);
        if (addCapacity) {
            upgrades.add(MEXUpgrades.capacity());
        }
        if (addVoid) {
            upgrades.add(MEXUpgrades.voidUpgrade());
        }
        cir.setReturnValue(Collections.unmodifiableSet(upgrades));
    }
}
