package committee.nova.mek_ex.init.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidStack.class, remap = false)
public class FluidStackMixin {

    @Inject(method = "getHoverName", at = @At("HEAD"), cancellable = true)
    private void useItemName(CallbackInfoReturnable<Component> cir) {
        Component itemName = ((FluidStack) (Object) this).get(DataComponents.ITEM_NAME);
        if (itemName != null) {
            cir.setReturnValue(itemName);
        }
    }
}
