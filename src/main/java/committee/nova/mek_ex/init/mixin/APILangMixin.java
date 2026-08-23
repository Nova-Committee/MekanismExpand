package committee.nova.mek_ex.init.mixin;

import java.util.Arrays;

import committee.nova.mek_ex.init.enums.MEXAPILang;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mekanism.api.text.APILang;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

@Mixin(value = APILang.class, remap = false)
public class APILangMixin {

    @Shadow
    @Final
    @Mutable
    @SuppressWarnings("target")
    static APILang[] $VALUES;

    @Invoker("<init>")
    private static APILang invokeInit(String name, int i, String arg) {
        return null;
    }

    @Unique
    private static APILang createNew(String type, String path) {
        int index = $VALUES.length;
        String name = Util.makeDescriptionId(type, ResourceLocation.fromNamespaceAndPath("mek_ex", path));

        // Build internal name: e.g. UPGRADE_RADIOACTIVE, UPGRADE_RADIOACTIVE_DESC
        String internal = type.toUpperCase();
        String[] paths = path.split("\\.");
        for (String s : paths) {
            internal += "_";
            internal += s.toUpperCase();
        }

        APILang result = invokeInit(internal, index, name);
        APILang[] newVALUES = Arrays.copyOf($VALUES, index + 1);
        newVALUES[index] = result;
        $VALUES = newVALUES;
        return result;
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void clinitInject(CallbackInfo ci) {
        MEXAPILang.UPGRADE_CAPACITY = createNew("upgrade", "capacity");

    }
}
