package committee.nova.mek_ex.client.jei;

import mekanism.api.chemical.ChemicalStack;
import net.neoforged.neoforge.fluids.FluidStack;

public record PotionNebulizingRecipe(FluidStack input, ChemicalStack steam, FluidStack output) {
    public PotionNebulizingRecipe {
        input = input.copy();
        steam = steam.copy();
        output = output.copy();
    }
}
