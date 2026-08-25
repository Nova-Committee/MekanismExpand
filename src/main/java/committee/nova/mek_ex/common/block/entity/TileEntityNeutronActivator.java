package committee.nova.mek_ex.common.block.entity;

import java.util.List;
import committee.nova.mek_ex.MekEXMod;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.energy.IEnergyConversionHelper;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.ChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleChemical;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityNeutronActivator extends TileEntityRecipeMachine<ChemicalToChemicalRecipe>
      implements ChemicalRecipeLookupHandler<ChemicalToChemicalRecipe> {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    public static final long MAX_CHEMICAL = 10L * FluidType.BUCKET_VOLUME;
    private static final double ENERGY_PER_MILLIBUCKET_JOULES = 2.8D * IEnergyConversionHelper.INSTANCE.feConversion().getConversion();
    private static final double ENERGY_EPSILON = 1.0E-9D;
    private static final String ENERGY_FRACTION_TAG = "NeutronActivatorEnergyFraction";
    private static final double MAX_ENERGY_VALUE = Long.MAX_VALUE;
    private double energyFraction;

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"}, docPlaceholder = "input tank")
    public IChemicalTank inputTank;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"}, docPlaceholder = "output tank")
    public IChemicalTank outputTank;

    @SyntheticComputerMethod(getter = "getProductionRate")
    private float productionRate = 1;

    private final IOutputHandler<@NotNull ChemicalStack> outputHandler;
    private final IInputHandler<@NotNull ChemicalStack> inputHandler;
    private MachineEnergyContainer<TileEntityNeutronActivator> energyContainer;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input slot")
    ChemicalInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    ChemicalInventorySlot outputSlot;

    public TileEntityNeutronActivator(BlockPos pos, BlockState state) {
        super(committee.nova.mek_ex.init.registry.MEXBlocks.neutron_activator, pos, state, TRACKED_ERROR_TYPES);
        configComponent.setupIOConfig(TransmissionType.ITEM, inputSlot, outputSlot, RelativeSide.FRONT);
        configComponent.setupIOConfig(TransmissionType.CHEMICAL, inputTank, outputTank, RelativeSide.FRONT, false, true);
        ConfigInfo energyConfig = configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);
        for (RelativeSide side : RelativeSide.values()) {
            if (side != RelativeSide.TOP) {
                energyConfig.addDisabledSides(side);
            }
        }
        energyConfig.setDataType(DataType.INPUT, RelativeSide.TOP);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.CHEMICAL)
              .setCanTankEject(tank -> tank != inputTank);
        inputHandler = InputHelper.getInputHandler(inputTank, RecipeError.NOT_ENOUGH_INPUT);
        outputHandler = OutputHelper.getOutputHandler(outputTank, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        double persistedFraction = nbt.getDouble(ENERGY_FRACTION_TAG);
        if (!Double.isFinite(persistedFraction) || persistedFraction < -ENERGY_EPSILON || persistedFraction >= 1 + ENERGY_EPSILON) {
            throw new IllegalStateException("Invalid neutron activator energy fraction: " + persistedFraction);
        }
        energyFraction = normalizeEnergyFraction(persistedFraction);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        nbt.putDouble(ENERGY_FRACTION_TAG, energyFraction);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener,
          IContentsListener recipeCacheUnpauseListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, recipeCacheUnpauseListener));
        return builder.build();
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener, IContentsListener recipeCacheListener,
          IContentsListener recipeCacheUnpauseListener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        builder.addTank(inputTank = BasicChemicalTank.createModern(MAX_CHEMICAL,
              ChemicalTankHelper.radioactiveInputTankPredicate(() -> outputTank), ConstantPredicates.alwaysTrueBi(),
              this::isInputChemicalAllowed, ChemicalAttributeValidator.ALWAYS_ALLOW, recipeCacheListener));
        builder.addTank(outputTank = BasicChemicalTank.output(MAX_CHEMICAL, recipeCacheUnpauseListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener,
          IContentsListener recipeCacheUnpauseListener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        builder.addSlot(inputSlot = ChemicalInventorySlot.fill(inputTank, listener, 45, 56));
        builder.addSlot(outputSlot = ChemicalInventorySlot.drain(outputTank, listener, 115, 56));
        inputSlot.setSlotType(ContainerSlotType.INPUT);
        inputSlot.setSlotOverlay(SlotOverlay.MINUS);
        outputSlot.setSlotType(ContainerSlotType.OUTPUT);
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        inputSlot.fillTank();
        outputSlot.drainTank();
        recipeCacheLookupMonitor.updateAndProcess();
        return sendUpdatePacket;
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleChemicalRecipeInput, ChemicalToChemicalRecipe, SingleChemical<ChemicalToChemicalRecipe>> getRecipeType() {
        return MekanismRecipeType.ACTIVATING;
    }

    @Override
    public IRecipeViewerRecipeType<ChemicalToChemicalRecipe> recipeViewerType() {
        return RecipeViewerRecipeType.ACTIVATING;
    }

    @Nullable
    @Override
    public ChemicalToChemicalRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    @NotNull
    @Override
    public CachedRecipe<ChemicalToChemicalRecipe> createNewCachedRecipe(@NotNull ChemicalToChemicalRecipe recipe, int cacheIndex) {
        long outputAmount = recipe.getOutputDefinition().stream().mapToLong(ChemicalStack::getAmount).max().orElse(0L);
        if (!isUsableOutputAmount(outputAmount)) {
            MekEXMod.LOGGER.error("Ignoring neutron activator recipe with invalid output amount {}: {}", outputAmount, recipe);
            return OneInputCachedRecipe.chemicalToChemical(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
                  .setCanHolderFunction(() -> false);
        }
        return OneInputCachedRecipe.chemicalToChemical(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
              .setErrorsChanged(this::onErrorsChanged)
              .setCanHolderFunction(this::canFunction)
              .setActive(this::setActive)
              .setEnergyRequirements(() -> getEnergyPerOperation(outputAmount), energyContainer)
              .setOnFinish(() -> {
                  consumeEnergyFraction(outputAmount);
                  markForSave();
              })
              .setRequiredTicks(() -> 1)
              .setBaselineMaxOperations(() -> 1);
    }

    private long getEnergyPerOperation(long outputAmount) {
        double next = energyFraction + outputAmount * ENERGY_PER_MILLIBUCKET_JOULES;
        if (!Double.isFinite(next) || next < 0 || next > MAX_ENERGY_VALUE + ENERGY_EPSILON) {
            throw new IllegalStateException("Neutron activator recipe energy cost is out of range: " + next);
        }
        long currentWhole = wholeEnergy(energyFraction);
        long nextWhole = wholeEnergy(next);
        return Math.max(0L, nextWhole - currentWhole);
    }

    private void consumeEnergyFraction(long outputAmount) {
        double next = energyFraction + outputAmount * ENERGY_PER_MILLIBUCKET_JOULES;
        if (!Double.isFinite(next) || next < 0) {
            throw new IllegalStateException("Invalid neutron activator energy accumulation: " + next);
        }
        energyFraction = normalizeEnergyFraction(next);
    }

    private boolean isInputChemicalAllowed(ChemicalStack stack) {
        return !stack.isEmpty() && (stack.isRadioactive() || getRecipeType().getInputCache().findTypeBasedRecipe(getLevel(), stack) != null);
    }

    private boolean isUsableOutputAmount(long outputAmount) {
        return outputAmount > 0 && outputAmount <= (MAX_ENERGY_VALUE - 1D) / ENERGY_PER_MILLIBUCKET_JOULES;
    }

    private static long wholeEnergy(double value) {
        double adjusted = value + ENERGY_EPSILON;
        return adjusted >= MAX_ENERGY_VALUE ? Long.MAX_VALUE : (long) Math.floor(adjusted);
    }

    private static double normalizeEnergyFraction(double value) {
        double fraction = value - Math.floor(value);
        if (fraction < ENERGY_EPSILON || fraction > 1 - ENERGY_EPSILON) {
            return 0;
        }
        return fraction;
    }

    public MachineEnergyContainer<TileEntityNeutronActivator> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getStored(), inputTank.getCapacity());
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.CHEMICAL;
    }
}
