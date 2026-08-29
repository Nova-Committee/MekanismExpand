package committee.nova.mek_ex.client.jei;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.block.entity.TileEntityPotionNebulizer;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public final class PotionNebulizingRecipeCategory implements IRecipeCategory<PotionNebulizingRecipe> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 90;
    private final RecipeType<PotionNebulizingRecipe> recipeType;
    private final IDrawable background;
    private final IDrawable icon;

    public PotionNebulizingRecipeCategory(IGuiHelper helper, RecipeType<PotionNebulizingRecipe> recipeType) {
        this.recipeType = recipeType;
        this.background = helper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = helper.createDrawableItemStack(MEXBlocks.potion_nebulizer.asItem().getDefaultInstance());
    }

    @Override
    public RecipeType<PotionNebulizingRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.mek_ex.potion_nebulizer");
    }

    @SuppressWarnings("removal")
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PotionNebulizingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 16)
              .setStandardSlotBackground()
              .addIngredient(NeoForgeTypes.FLUID_STACK, recipe.input());
        builder.addSlot(RecipeIngredientRole.INPUT, 45, 16)
              .setStandardSlotBackground()
              .addIngredient(MekanismJEI.TYPE_CHEMICAL, recipe.steam());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 16)
              .setOutputSlotBackground()
              .addIngredient(NeoForgeTypes.FLUID_STACK, recipe.output());
        builder.addSlot(RecipeIngredientRole.INPUT, 45, 58)
              .setStandardSlotBackground()
              .addItemStack(Items.GLASS_BOTTLE.getDefaultInstance());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 58)
              .setOutputSlotBackground()
              .addItemStack(TileEntityPotionNebulizer.createFilledPotion(recipe.output()).copyWithCount(recipe.output().getAmount() / (FluidType.BUCKET_VOLUME)));
    }
}
