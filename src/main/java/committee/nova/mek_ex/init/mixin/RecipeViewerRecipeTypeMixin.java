package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.init.registry.MEXBlocks;
import java.util.stream.Stream;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.client.recipe_viewer.recipe.SPSRecipeViewerRecipe;
import mekanism.client.recipe_viewer.type.FakeRVRecipeType;
import mekanism.client.recipe_viewer.type.RVRecipeTypeWrapper;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.recipe.MekanismRecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeViewerRecipeType.class, remap = false)
public abstract class RecipeViewerRecipeTypeMixin {

    @Shadow
    @Final
    @Mutable
    public static FakeRVRecipeType<SPSRecipeViewerRecipe> SPS;

    @Shadow
    @Final
    @Mutable
    public static RVRecipeTypeWrapper<?, ChemicalToChemicalRecipe, ?> ACTIVATING;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addMekExWorkstations(CallbackInfo ci) {
        SPS = new FakeRVRecipeType<>(SPS.id(), SPS.icon(), SPS.item(), SPS.name(), SPS.recipeClass(), SPS.xOffset(), SPS.yOffset(),
              SPS.width(), SPS.height(), Stream.concat(SPS.workstations().stream(), Stream.of(MEXBlocks.antimatter_supercharged_coil)).toList());
        ACTIVATING = new RVRecipeTypeWrapper<>(MekanismRecipeType.ACTIVATING, ChemicalToChemicalRecipe.class, ACTIVATING.xOffset(), ACTIVATING.yOffset(),
              ACTIVATING.width(), ACTIVATING.height(), ACTIVATING.item(), MEXBlocks.neutron_activator);
    }
}
