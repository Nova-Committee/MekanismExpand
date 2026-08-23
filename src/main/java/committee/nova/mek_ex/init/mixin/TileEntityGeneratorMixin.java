package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.common.energy.MEXCapacityEnergyContainer;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.generators.common.tile.TileEntityGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityGenerator.class, remap = false)
public class TileEntityGeneratorMixin {

    @Unique
    private static final ThreadLocal<TileEntityGenerator> CURRENT_GENERATOR = new ThreadLocal<>();

    @Inject(method = "getInitialEnergyContainers", at = @At("HEAD"))
    private void beginEnergyContainerCreation(IContentsListener listener, CallbackInfoReturnable<IEnergyContainerHolder> cir) {
        CURRENT_GENERATOR.set((TileEntityGenerator) (Object) this);
    }

    @Inject(method = "getInitialEnergyContainers", at = @At("RETURN"))
    private void finishEnergyContainerCreation(IContentsListener listener, CallbackInfoReturnable<IEnergyContainerHolder> cir) {
        CURRENT_GENERATOR.remove();
    }

    @Redirect(method = "getInitialEnergyContainers", at = @At(value = "INVOKE", target = "Lmekanism/common/capabilities/energy/BasicEnergyContainer;output(JLmekanism/api/IContentsListener;)Lmekanism/common/capabilities/energy/BasicEnergyContainer;"))
    private static BasicEnergyContainer createCapacityContainer(long maxEnergy, IContentsListener listener) {
        TileEntityGenerator generator = CURRENT_GENERATOR.get();
        if (generator == null) {
            throw new IllegalStateException("Generator energy container was created outside its initialization scope");
        }
        return new MEXCapacityEnergyContainer(generator, maxEnergy, listener);
    }
}
