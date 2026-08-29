package committee.nova.mek_ex.common.block.entity;

import committee.nova.mek_ex.init.registry.MEXBlocks;
import committee.nova.mek_ex.init.registry.MEXDataComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismChemicals;

public class TileEntityPotionNebulizer extends TileEntityConfigurableMachine {
    public static final int MAX_FLUID = 10 * FluidType.BUCKET_VOLUME;
    public static final long MAX_CHEMICAL = 10L * FluidType.BUCKET_VOLUME;
    private static final int ENERGY_PER_OPERATION = 2_000;
    private static final int DURATION_MULTIPLIER = 10;
    private static final int BASE_CHEMICAL_USAGE = 30;
    private static final int POTION_BOTTLE_AMOUNT = FluidType.BUCKET_VOLUME / 4;
    public BasicFluidTank inputFluidTank;
    public BasicFluidTank outputFluidTank;
    public IChemicalTank steamTank;
    private MachineEnergyContainer<TileEntityPotionNebulizer> energyContainer;
    private long chemicalUsage = BASE_CHEMICAL_USAGE;
    public InputInventorySlot containerFillSlot;
    public OutputInventorySlot containerOutputSlot;

    public TileEntityPotionNebulizer(BlockPos pos, BlockState state) {
        super(MEXBlocks.potion_nebulizer, pos, state);
        ConfigInfo item = configComponent.setupIOConfig(TransmissionType.ITEM, containerFillSlot, containerOutputSlot, RelativeSide.RIGHT, false, true);
        ConfigInfo fluid = configComponent.setupIOConfig(TransmissionType.FLUID, inputFluidTank, outputFluidTank, RelativeSide.RIGHT, false, true);
        ConfigInfo chemical = configComponent.setupInputConfig(TransmissionType.CHEMICAL, steamTank);
        ConfigInfo energy = configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        for (RelativeSide side : new RelativeSide[]{RelativeSide.BACK, RelativeSide.LEFT, RelativeSide.RIGHT}) {
            item.setDataType(DataType.INPUT_OUTPUT, side);
            fluid.setDataType(DataType.INPUT_OUTPUT, side);
            chemical.setDataType(DataType.INPUT, side);
        }
        energy.setDataType(DataType.INPUT, RelativeSide.FRONT);
        for (RelativeSide side : RelativeSide.values()) {
            if (side == RelativeSide.TOP || side == RelativeSide.BOTTOM || side == RelativeSide.FRONT) {
                if (fluid != null) fluid.addDisabledSides(side);
                if (chemical != null) chemical.addDisabledSides(side);
                if (item != null) item.addDisabledSides(side);
            }
            if (energy != null && side != RelativeSide.FRONT) {
                energy.addDisabledSides(side);
            }
        }
        ejectorComponent = new TileComponentEjector(this).setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.FLUID);
    }

    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this);
        builder.addTank(inputFluidTank = BasicFluidTank.input(MAX_FLUID, stack -> stack.get(DataComponents.POTION_CONTENTS) != null, listener));
        builder.addTank(outputFluidTank = BasicFluidTank.output(MAX_FLUID, listener));
        return builder.build();
    }

    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        builder.addTank(steamTank = BasicChemicalTank.inputModern(MAX_CHEMICAL, stack -> stack.getChemical() == MekanismChemicals.WATER_VAPOR.value(), listener));
        return builder.build();
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        builder.addSlot(containerFillSlot = InputInventorySlot.at(this::isFillableContainer, listener, 145, 30));
        builder.addSlot(containerOutputSlot = OutputInventorySlot.at(listener, 145, 56));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean changed = super.onUpdateServer();
        fillContainer();
        long energyPerTick = energyContainer.getEnergyPerTick();
        if (inputFluidTank.isEmpty() || steamTank.getStored() < chemicalUsage || energyContainer.getEnergy() < energyPerTick || outputFluidTank.getNeeded() < 1) {
            setActive(false);
            return changed;
        }
        if (steamTank.getStored() < chemicalUsage) {
            setActive(false);
            return changed;
        }
        FluidStack input = inputFluidTank.getFluid();
        FluidStack out = createOutput(input);
        if (!outputFluidTank.isEmpty() && !FluidStack.isSameFluidSameComponents(outputFluidTank.getFluid(), out)) {
            setActive(false);
            return changed;
        }
        energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
        inputFluidTank.extract(1, Action.EXECUTE, AutomationType.INTERNAL);
        steamTank.extract(chemicalUsage, Action.EXECUTE, AutomationType.INTERNAL);
        outputFluidTank.insert(out, Action.EXECUTE, AutomationType.INTERNAL);
        setActive(true);
        return true;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.CHEMICAL) {
            chemicalUsage = Math.max(1, MekanismUtils.getBaseUsage(this, BASE_CHEMICAL_USAGE));
        }
    }

    @Override
    public CompoundTag getReducedUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getReducedUpdateTag(provider);
        tag.put("InputFluid", inputFluidTank.serializeNBT(provider));
        tag.put("OutputFluid", outputFluidTank.serializeNBT(provider));
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("InputFluid")) inputFluidTank.deserializeNBT(provider, tag.getCompound("InputFluid"));
        if (tag.contains("OutputFluid")) outputFluidTank.deserializeNBT(provider, tag.getCompound("OutputFluid"));
    }

    private boolean isFillableContainer(ItemStack stack) {
        if (stack.is(Items.GLASS_BOTTLE)) return true;
        IFluidHandlerItem handler = FluidInventorySlot.tryGetFluidHandlerUnstacked(stack);
        return handler != null && (outputFluidTank.isEmpty() || handler.fill(outputFluidTank.getFluid().copy(), FluidAction.SIMULATE) > 0);
    }

    private void fillContainer() {
        if (containerFillSlot.isEmpty() || outputFluidTank.isEmpty()) return;
        ItemStack input = containerFillSlot.getStack();
        if (input.is(Items.GLASS_BOTTLE)) {
            if (outputFluidTank.getFluidAmount() < POTION_BOTTLE_AMOUNT) return;
            ItemStack filled = createFilledPotion(outputFluidTank.getFluid());
            if (canMoveFilledContainer(filled) && moveFilledContainer(filled)) outputFluidTank.extract(POTION_BOTTLE_AMOUNT, Action.EXECUTE, AutomationType.INTERNAL);
            return;
        }
        ItemStack copy = input.copyWithCount(1);
        IFluidHandlerItem handler = Capabilities.FLUID.getCapability(copy);
        if (handler == null) return;
        int accepted = handler.fill(outputFluidTank.getFluid().copy(), FluidAction.SIMULATE);
        if (accepted <= 0) return;
        ItemStack simulatedContainer = handler.getContainer();
        if (!canMoveFilledContainer(simulatedContainer)) return;
        accepted = handler.fill(outputFluidTank.getFluid().copy(), FluidAction.EXECUTE);
        if (accepted > 0 && moveFilledContainer(handler.getContainer())) {
            outputFluidTank.extract(accepted, Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    private boolean canMoveFilledContainer(ItemStack filled) {
        return containerOutputSlot.isEmpty() || (ItemStack.isSameItemSameComponents(containerOutputSlot.getStack(), filled)
              && containerOutputSlot.getCount() < containerOutputSlot.getLimit(filled));
    }

    private boolean moveFilledContainer(ItemStack filled) {
        if (containerOutputSlot.isEmpty()) {
            containerOutputSlot.setStack(filled);
        } else if (ItemStack.isSameItemSameComponents(containerOutputSlot.getStack(), filled) && containerOutputSlot.getCount() < containerOutputSlot.getLimit(filled)) {
            containerOutputSlot.growStack(1, Action.EXECUTE);
        } else {
            return false;
        }
        containerFillSlot.shrinkStack(1, Action.EXECUTE);
        return true;
    }


    public static FluidStack createOutput(FluidStack input) {
        PotionContents contents = input.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            throw new IllegalStateException("Potion fluid missing POTION_CONTENTS component: " + input);
        }
        FluidStack output = input.copyWithAmount(1);
        output.set(DataComponents.POTION_CONTENTS, scale(contents));
        output.set(DataComponents.ITEM_NAME, Component.translatable("gui.mek_ex.nebulized_potion", getPotionName(input, contents)));
        output.set(MEXDataComponents.NEBULIZED_POTION, true);
        return output;
    }

    public static ItemStack createFilledPotion(FluidStack fluid) {
        ItemStack filled = new ItemStack(Items.POTION);
        PotionContents contents = fluid.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            throw new IllegalStateException("Potion fluid missing POTION_CONTENTS component: " + fluid);
        }
        filled.set(DataComponents.POTION_CONTENTS, contents);
        Component itemName = fluid.get(DataComponents.ITEM_NAME);
        if (itemName != null) filled.set(DataComponents.ITEM_NAME, itemName);
        return filled;
    }

    public static boolean isNebulized(FluidStack fluid) {
        return Boolean.TRUE.equals(fluid.get(MEXDataComponents.NEBULIZED_POTION));
    }

    private static PotionContents scale(PotionContents source) {
        List<MobEffectInstance> effects = new ArrayList<>();
        source.forEachEffect(effect -> {
            int duration = effect.mapDuration(value -> value > Integer.MAX_VALUE / DURATION_MULTIPLIER ? Integer.MAX_VALUE : value * DURATION_MULTIPLIER);
            MobEffectInstance scaled = new MobEffectInstance(effect.getEffect(), duration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(),
                  effect.showIcon());
            scaled.getCures().clear();
            scaled.getCures().addAll(effect.getCures());
            effects.add(scaled);
        });
        return new PotionContents(Optional.empty(), Optional.of(source.getColor()), effects);
    }

    private static Component getPotionName(FluidStack input, PotionContents contents) {
        Component name = input.get(DataComponents.CUSTOM_NAME);
        if (name != null) return name;
        name = input.get(DataComponents.ITEM_NAME);
        if (name != null) return name;
        return Component.translatable(Potion.getName(contents.potion(), Items.POTION.getDescriptionId() + ".effect."));
    }

    public MachineEnergyContainer<TileEntityPotionNebulizer> getEnergyContainer() { return energyContainer; }
}
