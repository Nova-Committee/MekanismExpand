package committee.nova.mek_ex.common.block.entity;

import committee.nova.mek_ex.init.registry.MEXBlocks;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Valve tile for the nuclear control tank. Valves expose both sides of the
 * formed merged tank and retain Mekanism's transfer/comparator behavior.
 */
public class TileEntityNuclearControlValve extends TileEntityNuclearControlTank {

    public TileEntityNuclearControlValve(BlockPos pos, BlockState state) {
        super(MEXBlocks.nuclear_control_valve, pos, state);
    }

    @Override
    @NotNull
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return side -> getMultiblock().getFluidTanks(side);
    }

    @Override
    @NotNull
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        return side -> getMultiblock().getChemicalTanks(side);
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        if (type == ContainerType.FLUID || type == ContainerType.CHEMICAL) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    @NotNull
    public FluidStack insertFluid(int tank, @NotNull FluidStack stack, Direction side, @NotNull Action action) {
        return handleValveTransfer(stack, action, super.insertFluid(tank, stack, side, action));
    }

    @Override
    @NotNull
    public FluidStack insertFluid(@NotNull FluidStack stack, Direction side, @NotNull Action action) {
        return handleValveTransfer(stack, action, super.insertFluid(stack, side, action));
    }

    @Override
    @NotNull
    public ChemicalStack insertChemical(int tank, @NotNull ChemicalStack stack, Direction side, @NotNull Action action) {
        ChemicalStack remainder = super.insertChemical(tank, stack, side, action);
        if (action.execute() && remainder.getAmount() < stack.getAmount()) {
            getMultiblock().triggerValveTransfer(this);
        }
        return remainder;
    }

    @Override
    @NotNull
    public ChemicalStack insertChemical(@NotNull ChemicalStack stack, Direction side, @NotNull Action action) {
        ChemicalStack remainder = super.insertChemical(stack, side, action);
        if (action.execute() && remainder.getAmount() < stack.getAmount()) {
            getMultiblock().triggerValveTransfer(this);
        }
        return remainder;
    }

    private FluidStack handleValveTransfer(@NotNull FluidStack stack, @NotNull Action action, @NotNull FluidStack remainder) {
        if (action.execute() && remainder.getAmount() < stack.getAmount()) {
            getMultiblock().triggerValveTransfer(this);
        }
        return remainder;
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }
}
