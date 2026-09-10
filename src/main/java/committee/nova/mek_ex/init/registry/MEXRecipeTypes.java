package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.structure.MultiblockBuildRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MEXRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
          DeferredRegister.create(Registries.RECIPE_TYPE, MekEXMod.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MultiblockBuildRecipe>> MULTIBLOCK_BUILD =
          RECIPE_TYPES.register("multiblock_build", () -> RecipeType.simple(MekEXMod.rl("multiblock_build")));

    private MEXRecipeTypes() {
    }

    public static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
    }
}
