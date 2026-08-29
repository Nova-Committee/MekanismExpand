package committee.nova.mek_ex.client.jei;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.block.entity.TileEntityPotionNebulizer;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.FakeRVRecipeType;
import mekanism.common.registries.MekanismChemicals;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

@JeiPlugin
public class MEXJEI implements IModPlugin {
    public static final FakeRVRecipeType<PotionNebulizingRecipe> POTION_NEBULIZING_VIEWER = new FakeRVRecipeType<>(
          MEXBlocks.potion_nebulizer, PotionNebulizingRecipe.class, 0, 0, 176, 90);
    public static final RecipeType<PotionNebulizingRecipe> POTION_NEBULIZING = MekanismJEI.recipeType(POTION_NEBULIZING_VIEWER);

    @Override
    public net.minecraft.resources.ResourceLocation getPluginUid() {
        return MekEXMod.rl("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PotionNebulizingRecipeCategory(registration.getJeiHelpers().getGuiHelper(), POTION_NEBULIZING));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<PotionNebulizingRecipe> recipes = new ArrayList<>();
        for (FluidStack stack : registration.getIngredientManager().getAllIngredients(NeoForgeTypes.FLUID_STACK)) {
            if (stack.isEmpty() || stack.get(DataComponents.POTION_CONTENTS) == null || TileEntityPotionNebulizer.isNebulized(stack)) continue;
            FluidStack input = stack.copyWithAmount(FluidType.BUCKET_VOLUME);
            FluidStack output = TileEntityPotionNebulizer.createOutput(input).copyWithAmount(FluidType.BUCKET_VOLUME);
            if (recipes.stream().noneMatch(recipe -> FluidStack.isSameFluidSameComponents(recipe.input(), input))) {
                ChemicalStack steam = MekanismChemicals.WATER_VAPOR.asStack(30L * FluidType.BUCKET_VOLUME);
                recipes.add(new PotionNebulizingRecipe(input, steam, output));
            }
        }
        registration.addRecipes(POTION_NEBULIZING, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(MEXBlocks.potion_nebulizer, POTION_NEBULIZING);
    }
}
