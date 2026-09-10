package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.structure.MultiblockBuildRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MEXRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
          DeferredRegister.create(Registries.RECIPE_SERIALIZER, MekEXMod.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, MultiblockBuildRecipe.Serializer> MULTIBLOCK_BUILD =
          RECIPE_SERIALIZERS.register("multiblock_build", MultiblockBuildRecipe.Serializer::new);

    private MEXRecipeSerializers() {
    }

    public static void register(IEventBus bus) {
        RECIPE_SERIALIZERS.register(bus);
    }
}
