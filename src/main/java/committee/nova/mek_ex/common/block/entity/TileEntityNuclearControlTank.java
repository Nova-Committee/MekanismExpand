package committee.nova.mek_ex.common.block.entity;

import committee.nova.mek_ex.common.multiblock.NuclearControlTankManager;
import committee.nova.mek_ex.common.multiblock.NuclearControlTankMultiblockData;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.FluidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Nuclear control tank casing tile. It reuses Mekanism's merged fluid/chemical
 * tank data so every registered fluid and chemical, including radioactive waste,
 * plutonium and polonium, can be stored by the formed structure.
 */
public class TileEntityNuclearControlTank extends TileEntityMultiblock<NuclearControlTankMultiblockData> implements IFluidContainerManager {

    public TileEntityNuclearControlTank(BlockPos pos, BlockState state) {
        super(MEXBlocks.nuclear_control_tank, pos, state);
    }

    protected TileEntityNuclearControlTank(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public MultiblockManager<NuclearControlTankMultiblockData> getManager() {
        return NuclearControlTankManager.MANAGER;
    }

    @Override
    @NotNull
    public NuclearControlTankMultiblockData createMultiblock() {
        return new NuclearControlTankMultiblockData(this);
    }

    /**
     * Handles chemical containers before delegating to Mekanism's fluid
     * interaction, since the upstream dynamic tank only handles fluids here.
     */
    @Override
    public ItemInteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack) {
        if (!player.isShiftKeyDown()) {
            NuclearControlTankMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                if (handleChemicalInteraction(player, hand, stack, multiblock.getChemicalTank())) {
                    player.getInventory().setChanged();
                    return ItemInteractionResult.SUCCESS;
                }
                if (FluidUtils.handleTankInteraction(player, hand, stack, multiblock.getFluidTank())) {
                    player.getInventory().setChanged();
                    return ItemInteractionResult.SUCCESS;
                }
                InteractionResult result = openGui(player);
                return switch (result) {
                    case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
                    case CONSUME -> ItemInteractionResult.CONSUME;
                    case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
                    case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    case FAIL -> ItemInteractionResult.FAIL;
                };
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public IFluidContainerManager.ContainerEditMode getContainerEditMode() {
        return getMultiblock().editMode;
    }

    @Override
    public void nextMode() {
        NuclearControlTankMultiblockData multiblock = getMultiblock();
        multiblock.setContainerEditMode(multiblock.editMode.getNext());
    }

    @Override
    public void previousMode() {
        NuclearControlTankMultiblockData multiblock = getMultiblock();
        multiblock.setContainerEditMode(multiblock.editMode.getPrevious());
    }

    private boolean handleChemicalInteraction(Player player, InteractionHand hand, ItemStack stack, IChemicalTank chemicalTank) {
        if (stack.isEmpty()) {
            return false;
        }
        // Mekanism chemical item capabilities commonly require an unstacked item.
        // Work on one copy, then put the updated container back into the player's inventory.
        ItemStack itemCopy = stack.copyWithCount(1);
        IChemicalHandler itemHandler = Capabilities.CHEMICAL.getCapability(itemCopy);
        if (itemHandler == null) {
            return false;
        }
        if (chemicalTank.isEmpty()) {
            for (int tank = 0; tank < itemHandler.getChemicalTanks(); tank++) {
                ChemicalStack contained = itemHandler.getChemicalInTank(tank);
                if (contained.isEmpty()) {
                    continue;
                }
                ChemicalStack simulatedRemainder = chemicalTank.insert(contained, Action.SIMULATE, AutomationType.MANUAL);
                long amount = contained.getAmount() - simulatedRemainder.getAmount();
                if (amount <= 0) {
                    continue;
                }
                Action action = player.isCreative() ? Action.SIMULATE : Action.EXECUTE;
                ChemicalStack extracted = itemHandler.extractChemical(tank, amount, action);
                if (!extracted.isEmpty()) {
                    chemicalTank.insert(extracted, Action.EXECUTE, AutomationType.MANUAL);
                    finishChemicalInteraction(player, hand, stack, itemCopy);
                    return true;
                }
            }
            return false;
        }

        ChemicalStack stored = chemicalTank.getStack();
        ChemicalStack simulatedRemainder = itemHandler.insertChemical(stored, Action.SIMULATE);
        long amount = stored.getAmount() - simulatedRemainder.getAmount();
        if (amount <= 0) {
            return false;
        }
        Action action = player.isCreative() ? Action.SIMULATE : Action.EXECUTE;
        ChemicalStack remainder = itemHandler.insertChemical(stored.copyWithAmount(amount), action);
        long inserted = amount - remainder.getAmount();
        if (inserted <= 0) {
            return false;
        }
        chemicalTank.extract(inserted, Action.EXECUTE, AutomationType.MANUAL);
        finishChemicalInteraction(player, hand, stack, itemCopy);
        return true;
    }

    private void finishChemicalInteraction(Player player, InteractionHand hand, ItemStack original, ItemStack updated) {
        if (player.isCreative()) {
            return;
        }
        if (original.getCount() == 1) {
            player.setItemInHand(hand, updated);
            return;
        }
        original.shrink(1);
        if (!player.getInventory().add(updated)) {
            player.drop(updated, false, true);
        }
    }
}
