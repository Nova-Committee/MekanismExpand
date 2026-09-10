package committee.nova.mek_ex.common.structure;

import committee.nova.mek_ex.init.registry.MEXRecipeTypes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class MultiblockRecipeIndex {

    private MultiblockRecipeIndex() {
    }

    public static List<RecipeHolder<MultiblockBuildRecipe>> list(@Nullable Level level) {
        if (level == null) {
            return List.of();
        }
        List<RecipeHolder<MultiblockBuildRecipe>> recipes = new ArrayList<>(
              level.getRecipeManager().getAllRecipesFor(MEXRecipeTypes.MULTIBLOCK_BUILD.get())
        );
        recipes.sort(Comparator.comparing(holder -> holder.id().toString()));
        return recipes;
    }

    @Nullable
    public static RecipeHolder<MultiblockBuildRecipe> byIndex(@Nullable Level level, int index) {
        List<RecipeHolder<MultiblockBuildRecipe>> recipes = list(level);
        if (recipes.isEmpty() || index < 0 || index >= recipes.size()) {
            return null;
        }
        return recipes.get(index);
    }

    public static int clampIndex(@Nullable Level level, int index) {
        List<RecipeHolder<MultiblockBuildRecipe>> recipes = list(level);
        if (recipes.isEmpty()) {
            return 0;
        }
        return Math.floorMod(index, recipes.size());
    }

    public static int size(@Nullable Level level) {
        return list(level).size();
    }
}
