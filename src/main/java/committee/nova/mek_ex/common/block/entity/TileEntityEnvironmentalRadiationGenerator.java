package committee.nova.mek_ex.common.block.entity;

import committee.nova.mek_ex.common.attachment.EnvironmentalRadiationData;
import committee.nova.mek_ex.common.energy.MEXCapacityEnergyContainer;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import committee.nova.mek_ex.init.registry.MEXDataComponents;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyConversionHelper;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.IRadiationSource;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityEnvironmentalRadiationGenerator extends TileEntityConfigurableMachine {

    public static final double BASE_PRODUCTION_PER_TICK = 256D / 20D;

    public static final double MAX_RADIATION_PRODUCTION_PER_TICK = 4_000D;

    public static final long ENERGY_CAPACITY_FE = 8_000_000L;

    public static final double RADIATION_CAPACITY = 100_000D;

    public static final double ABSORPTION_FRACTION = 0.05D;

    public static final double MAX_ABSORPTION_PER_TICK = 50D;

    public static final double SATURATED_ABSORPTION_MULTIPLIER = 0.01D;

    public static final double RADIATION_VALUE_FE = 80_000D;

    public static final double MAX_CYCLE_CONVERSION_FE = 800_000D;

    public static final int STABLE_TICKS_REQUIRED = 20;

    private static final double NEUTRALIZATION_FRACTION = 0.01D;
    private static final double RADIATION_TAIL_THRESHOLD = 1.0E-6D;

    public static long getEnergyUsage() {
        return 0L;
    }

    public static long getEnergyStorage() {
        return IEnergyConversionHelper.INSTANCE.feConversion().convertFrom(ENERGY_CAPACITY_FE);
    }

    private static final String STORED_RADIATION_TAG = "EnvironmentalRadiationStored";
    private static final String ENVIRONMENTAL_RADIATION_TAG = "EnvironmentalRadiationLevel";
    private static final String CURRENT_GENERATION_TAG = "EnvironmentalRadiationGeneration";
    private static final String GENERATION_FRACTION_TAG = "EnvironmentalRadiationGenerationFraction";
    private static final String CONVERSION_GENERATION_FRACTION_TAG = "EnvironmentalRadiationConversionGenerationFraction";
    private static final String CONVERSION_PHASE_TAG = "EnvironmentalRadiationConversionPhase";
    private static final String STABLE_TICKS_TAG = "EnvironmentalRadiationStableTicks";
    private static final String CYCLE_CONVERSION_QUOTA_TAG = "EnvironmentalRadiationCycleConversionQuota";
    private static final String CYCLE_CONVERTED_TAG = "EnvironmentalRadiationCycleConverted";
    private static final String RADIATION_PROCESSING_TAG = "EnvironmentalRadiationProcessing";
    private static final double EPSILON = 1.0E-9D;
    private static final double FE_TO_JOULES = IEnergyConversionHelper.INSTANCE.feConversion().getConversion();

    private MEXCapacityEnergyContainer energyContainer;
    private double storedRadiation;
    private double environmentalRadiation;

    private double generationFraction;

    private double conversionGenerationFraction;

    private ConversionPhase conversionPhase = ConversionPhase.ABSORBING;

    private int stableTicks;

    private double cycleConversionQuota;

    private double cycleConverted;

    private long currentGeneration;

    private boolean radiationProcessing;

    public TileEntityEnvironmentalRadiationGenerator(BlockPos pos, BlockState state) {
        super(MEXBlocks.environmental_radiation_generator, pos, state);

        ConfigInfo energyConfig = configComponent.setupOutputConfig(TransmissionType.ENERGY, energyContainer);
        if (energyConfig == null) {
            throw new IllegalStateException("Environmental radiation generator requires energy side configuration");
        }

        energyConfig.addDisabledSides(RelativeSide.TOP);
        for (RelativeSide side : RelativeSide.values()) {
            if (side == RelativeSide.TOP) {
                continue;
            }
            if (!energyConfig.setDataType(DataType.OUTPUT, side)) {
                throw new IllegalStateException("Environmental radiation generator requires output support on " + side);
            }
        }
        energyConfig.setEjecting(true);

        ejectorComponent = new TileComponentEjector(this, this::getMaxOutputJoules, true)
              .setOutputData(configComponent, TransmissionType.ENERGY)
              .setCanEject(type -> canFunction());
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = new MEXCapacityEnergyContainer(this, getEnergyStorage(), listener));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        double previousEnvironmentalRadiation = environmentalRadiation;
        double previousStoredRadiation = storedRadiation;
        long previousGeneration = currentGeneration;
        boolean wasRadiationProcessing = radiationProcessing;
        radiationProcessing = false;
        updateEnvironmentalRadiation();
        updateRadiationCycle();
        generateEnergy();
        boolean sendUpdatePacket = super.onUpdateServer();
        return sendUpdatePacket
              || Double.compare(previousEnvironmentalRadiation, environmentalRadiation) != 0
              || Double.compare(previousStoredRadiation, storedRadiation) != 0
              || previousGeneration != currentGeneration
              || wasRadiationProcessing != radiationProcessing;
    }

    private void updateRadiationCycle() {
        double baseline = IRadiationManager.INSTANCE.baselineRadiation();
        if (environmentalRadiation > baseline) {
            boolean phaseChanged = stableTicks != 0 || conversionPhase != ConversionPhase.ABSORBING;
            stableTicks = 0;
            conversionPhase = ConversionPhase.ABSORBING;
            absorbRadiation();
            if (phaseChanged) {
                markForSave();
            }
            return;
        }
        if (storedRadiation <= RADIATION_TAIL_THRESHOLD) {
            completeConversionCycle();
            return;
        }
        if (conversionPhase != ConversionPhase.CONVERTING) {
            conversionPhase = ConversionPhase.STABILIZING;
            stableTicks++;
            if (stableTicks >= STABLE_TICKS_REQUIRED) {
                stableTicks = STABLE_TICKS_REQUIRED;
                conversionPhase = ConversionPhase.CONVERTING;
            }
            markForSave();
        }
    }

    private void updateEnvironmentalRadiation() {
        Level world = getLevel();
        if (world == null || world.isClientSide()) {
            return;
        }
        double baseline = IRadiationManager.INSTANCE.baselineRadiation();
        environmentalRadiation = IRadiationManager.INSTANCE.isRadiationEnabled()
              ? Math.max(baseline, IRadiationManager.INSTANCE.getRadiationLevel(world, getBlockPos()))
              : baseline;
    }

    private void absorbRadiation() {
        if (level == null || level.isClientSide() || !IRadiationManager.INSTANCE.isRadiationEnabled()) {
            return;
        }
        double remainingCapacity = Math.max(0D, RADIATION_CAPACITY - storedRadiation);

        double excess = environmentalRadiation - IRadiationManager.INSTANCE.baselineRadiation();
        if (!(excess > 0D) || !Double.isFinite(excess)) {
            return;
        }
        double headroomRatio = remainingCapacity / RADIATION_CAPACITY;
        double absorptionMultiplier = Math.max(SATURATED_ABSORPTION_MULTIPLIER, headroomRatio);
        double requested = Math.min(MAX_ABSORPTION_PER_TICK, excess * ABSORPTION_FRACTION) * absorptionMultiplier;
        if (!(requested > 0D)) {
            return;
        }
        AbsorptionResult result = reduceNearbyRadiation(requested, Double.MAX_VALUE);
        radiationProcessing = result.removedMagnitude() > 0D;
        double storedAmount = Math.min(remainingCapacity, result.removedMagnitude());
        if (storedAmount > 0D) {
            storedRadiation = Math.min(RADIATION_CAPACITY, storedRadiation + storedAmount);
            double remainingCycleCapacity = MAX_CYCLE_CONVERSION_FE - cycleConversionQuota;
            if (remainingCycleCapacity > 0D) {
                cycleConversionQuota += Math.min(remainingCycleCapacity, storedAmount * RADIATION_VALUE_FE);
            }
            markForSave();
        }
    }


    private AbsorptionResult reduceNearbyRadiation(double requestedExposure, double maximumMagnitude) {
        if (level == null) {
            return AbsorptionResult.NONE;
        }
        BlockPos origin = getBlockPos();
        List<IRadiationSource> sources = new ArrayList<>();
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        for (int x = chunkX - 1; x <= chunkX + 1; x++) {
            for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                sources.addAll(IRadiationManager.INSTANCE.getRadiationSources(level, x, z));
            }
        }
        sources.sort(Comparator.comparingDouble(source -> source.getPosition().distSqr(origin)));

        double remainingExposure = requestedExposure;
        double remainingMagnitude = maximumMagnitude;
        double removedMagnitude = 0D;
        double removedExposure = 0D;
        double minimum = IRadiationManager.INSTANCE.minRadiationMagnitude();
        for (IRadiationSource source : sources) {
            if (remainingExposure <= EPSILON || remainingMagnitude <= EPSILON) {
                break;
            }
            double magnitude = source.getMagnitude();
            double reducible = magnitude - minimum;
            if (!(reducible > 0D) || !Double.isFinite(reducible)) {
                continue;
            }
            double distanceSquared = Math.max(1D, source.getPosition().distSqr(origin));
            double delta = Math.min(Math.min(remainingExposure * distanceSquared, reducible), remainingMagnitude);
            IRadiationManager.INSTANCE.radiate(level, source.getPosition(), -delta);
            double exposureDelta = delta / distanceSquared;
            removedMagnitude += delta;
            removedExposure += exposureDelta;
            remainingExposure -= exposureDelta;
            remainingMagnitude -= delta;
            if (magnitude - delta <= minimum + EPSILON) {
                IRadiationManager.INSTANCE.removeRadiationSource(level, source.getPosition());
            }
        }
        return removedMagnitude > 0D ? new AbsorptionResult(removedMagnitude, removedExposure) : AbsorptionResult.NONE;
    }

    private void generateEnergy() {
        currentGeneration = 0L;
        if (!canFunction()) {
            setActive(false);
            return;
        }
        if (energyContainer.getNeeded() > 0L) {
            generationFraction += BASE_PRODUCTION_PER_TICK * FE_TO_JOULES;
            long requested = (long) Math.floor(generationFraction);
            generationFraction -= requested;
            currentGeneration += insertEnergy(requested);
        }
        if (conversionPhase == ConversionPhase.CONVERTING) {
            convertStoredRadiation();
        }
        setActive(currentGeneration > 0L);
    }

    private long insertEnergy(long requested) {
        long before = energyContainer.getEnergy();
        energyContainer.insert(requested, Action.EXECUTE, AutomationType.INTERNAL);
        return energyContainer.getEnergy() - before;
    }

    private void convertStoredRadiation() {
        if (storedRadiation <= RADIATION_TAIL_THRESHOLD) {
            completeConversionCycle();
            return;
        }
        double neutralized = storedRadiation * NEUTRALIZATION_FRACTION;
        if (storedRadiation - neutralized <= RADIATION_TAIL_THRESHOLD) {
            neutralized = storedRadiation;
        }
        double remainingQuota = cycleConversionQuota - cycleConverted;
        if (remainingQuota <= EPSILON) {
            cycleConverted = cycleConversionQuota;
            neutralizeRadiation(neutralized);
            return;
        }
        if (energyContainer.getNeeded() <= 0L) {
            return;
        }

        double requestedFE = Math.min(Math.min(MAX_RADIATION_PRODUCTION_PER_TICK, remainingQuota),
              neutralized * RADIATION_VALUE_FE);
        long maximumQuotaJoules = (long) Math.floor(remainingQuota * FE_TO_JOULES);
        if (maximumQuotaJoules <= 0L) {
            cycleConverted = cycleConversionQuota;
            conversionGenerationFraction = 0D;
            neutralizeRadiation(neutralized);
            return;
        }
        double requestedWithFraction = conversionGenerationFraction + requestedFE * FE_TO_JOULES;
        long wholeRequestedJoules = (long) Math.floor(requestedWithFraction);
        long requestedJoules = Math.min(maximumQuotaJoules, wholeRequestedJoules);
        conversionGenerationFraction = wholeRequestedJoules > maximumQuotaJoules
              ? 0D
              : requestedWithFraction - requestedJoules;
        if (requestedJoules <= 0L) {
            // The sub-Joule value remains in conversionGenerationFraction, so the
            // radiation can continue decaying without losing its eventual output.
            neutralizeRadiation(neutralized);
            return;
        }

        long insertedJoules = insertEnergy(requestedJoules);
        currentGeneration += insertedJoules;
        if (insertedJoules <= 0L) {
            return;
        }
        double insertedFE = insertedJoules / FE_TO_JOULES;
        cycleConverted = Math.min(cycleConversionQuota, cycleConverted + insertedFE);
        double insertionRatio = Math.min(1D, (double) insertedJoules / requestedJoules);
        neutralizeRadiation(neutralized * insertionRatio);
    }

    private void neutralizeRadiation(double amount) {
        radiationProcessing = amount > 0D;
        storedRadiation = Math.max(0D, storedRadiation - amount);
        if (storedRadiation <= RADIATION_TAIL_THRESHOLD) {
            completeConversionCycle();
        } else {
            markForSave();
        }
    }

    private void completeConversionCycle() {
        boolean changed = storedRadiation != 0D || stableTicks != 0 || cycleConversionQuota != 0D
              || cycleConverted != 0D || conversionGenerationFraction != 0D
              || conversionPhase != ConversionPhase.ABSORBING;
        storedRadiation = 0D;
        stableTicks = 0;
        cycleConversionQuota = 0D;
        cycleConverted = 0D;
        conversionGenerationFraction = 0D;
        conversionPhase = ConversionPhase.ABSORBING;
        if (changed) {
            markForSave();
        }
    }

    private record AbsorptionResult(double removedMagnitude, double removedExposure) {

        private static final AbsorptionResult NONE = new AbsorptionResult(0D, 0D);
    }

    private enum ConversionPhase {
        ABSORBING,
        STABILIZING,
        CONVERTING;

        private static ConversionPhase byId(int id) {
            ConversionPhase[] phases = values();
            if (id < 0 || id >= phases.length) {
                throw new IllegalStateException("Invalid environmental radiation generator conversion phase: " + id);
            }
            return phases[id];
        }
    }

    @ComputerMethod
    public double getEnvironmentalRadiation() {
        return environmentalRadiation;
    }

    @ComputerMethod
    public double getStoredRadiation() {
        return storedRadiation;
    }

    public double getRadiationStored() {
        return getStoredRadiation();
    }

    @ComputerMethod
    public double getRadiationCapacity() {
        return RADIATION_CAPACITY;
    }

    @ComputerMethod
    public double getRemainingRadiationConversionQuota() {
        return Math.max(0D, cycleConversionQuota - cycleConverted);
    }

    @ComputerMethod
    public double getConvertedRadiationEnergyThisCycle() {
        return cycleConverted;
    }

    @ComputerMethod
    public int getRadiationStableTicks() {
        return stableTicks;
    }

    @ComputerMethod
    public String getRadiationConversionPhase() {
        return conversionPhase.name();
    }

    @ComputerMethod

    public long getProductionRate() {
        return currentGeneration;
    }

    @ComputerMethod(nameOverride = "getGenerationRate")

    public long getGenerationRate() {
        return getProductionRate();
    }


    @ComputerMethod
    public double getGenerationRateFE() {
        return currentGeneration / FE_TO_JOULES;
    }

    @ComputerMethod
    public long getMaximumProductionRate() {
        return getMaxOutput();
    }

    @ComputerMethod
    public long getMaxOutput() {
        return getMaxOutputJoules();
    }

    public long getGenerationRateJoules() {
        return currentGeneration;
    }

    public long getMaxOutputJoules() {
        return IEnergyConversionHelper.INSTANCE.feConversion().convertFrom(getMaxOutputFE());
    }

    @ComputerMethod
    public long getMaxOutputFE() {
        return (long) Math.ceil(BASE_PRODUCTION_PER_TICK + MAX_RADIATION_PRODUCTION_PER_TICK);
    }

    public MEXCapacityEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @Override
    protected boolean canPlaySound() {
        return radiationProcessing;
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MEXDataComponents.ENVIRONMENTAL_RADIATION_DATA, new EnvironmentalRadiationData(
              storedRadiation,
              generationFraction,
              conversionGenerationFraction,
              conversionPhase.ordinal(),
              stableTicks,
              cycleConversionQuota,
              cycleConverted
        ));
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        EnvironmentalRadiationData data = input.get(MEXDataComponents.ENVIRONMENTAL_RADIATION_DATA);
        if (data == null) {
            return;
        }
        storedRadiation = data.storedRadiation();
        generationFraction = data.generationFraction();
        conversionGenerationFraction = data.conversionGenerationFraction();
        conversionPhase = ConversionPhase.byId(data.conversionPhase());
        stableTicks = data.stableTicks();
        cycleConversionQuota = data.cycleConversionQuota();
        cycleConverted = data.cycleConverted();
        validateConversionState();
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getEnvironmentalRadiation, value -> environmentalRadiation = value));
        container.track(SyncableDouble.create(this::getStoredRadiation, value -> storedRadiation = value));
        container.track(SyncableLong.create(this::getProductionRate, value -> currentGeneration = value));
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        storedRadiation = readFiniteDouble(nbt, STORED_RADIATION_TAG, 0D, RADIATION_CAPACITY);
        generationFraction = readFiniteDouble(nbt, GENERATION_FRACTION_TAG, 0D, 1D);
        if (generationFraction >= 1D) {
            throw new IllegalStateException("Invalid environmental radiation generator generation fraction: " + generationFraction);
        }
        conversionGenerationFraction = readFiniteDouble(nbt, CONVERSION_GENERATION_FRACTION_TAG, 0D, 1D);
        if (conversionGenerationFraction >= 1D) {
            throw new IllegalStateException("Invalid environmental radiation generator conversion generation fraction: " + conversionGenerationFraction);
        }
        stableTicks = readInt(nbt, STABLE_TICKS_TAG, 0, STABLE_TICKS_REQUIRED);
        cycleConversionQuota = readFiniteDouble(nbt, CYCLE_CONVERSION_QUOTA_TAG, 0D, MAX_CYCLE_CONVERSION_FE);
        cycleConverted = readFiniteDouble(nbt, CYCLE_CONVERTED_TAG, 0D, MAX_CYCLE_CONVERSION_FE);
        conversionPhase = ConversionPhase.byId(readInt(nbt, CONVERSION_PHASE_TAG, 0, ConversionPhase.values().length - 1));
        validateConversionState();
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbt, provider);
        nbt.putDouble(STORED_RADIATION_TAG, storedRadiation);
        nbt.putDouble(GENERATION_FRACTION_TAG, generationFraction);
        nbt.putDouble(CONVERSION_GENERATION_FRACTION_TAG, conversionGenerationFraction);
        nbt.putInt(CONVERSION_PHASE_TAG, conversionPhase.ordinal());
        nbt.putInt(STABLE_TICKS_TAG, stableTicks);
        nbt.putDouble(CYCLE_CONVERSION_QUOTA_TAG, cycleConversionQuota);
        nbt.putDouble(CYCLE_CONVERTED_TAG, cycleConverted);
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.putDouble(ENVIRONMENTAL_RADIATION_TAG, environmentalRadiation);
        updateTag.putDouble(STORED_RADIATION_TAG, storedRadiation);
        updateTag.putLong(CURRENT_GENERATION_TAG, currentGeneration);
        updateTag.putInt(CONVERSION_PHASE_TAG, conversionPhase.ordinal());
        updateTag.putBoolean(RADIATION_PROCESSING_TAG, radiationProcessing);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        double baseline = IRadiationManager.INSTANCE.baselineRadiation();

        double syncedRadiation = readFiniteDouble(tag, ENVIRONMENTAL_RADIATION_TAG, 0D, Double.MAX_VALUE);
        environmentalRadiation = Math.max(baseline, syncedRadiation);
        storedRadiation = readFiniteDouble(tag, STORED_RADIATION_TAG, 0D, RADIATION_CAPACITY);
        conversionPhase = ConversionPhase.byId(readInt(tag, CONVERSION_PHASE_TAG, 0, ConversionPhase.values().length - 1));
        radiationProcessing = tag.getBoolean(RADIATION_PROCESSING_TAG);
        long generation = tag.getLong(CURRENT_GENERATION_TAG);
        long maxOutputJoules = getMaxOutputJoules();
        if (generation < 0L || generation > maxOutputJoules) {
            throw new IllegalStateException("Invalid environmental radiation generator generation: " + generation);
        }
        currentGeneration = generation;
    }

    private static double readFiniteDouble(CompoundTag nbt, String key, double min, double max) {
        if (!nbt.contains(key)) {
            return min;
        }
        double value = nbt.getDouble(key);
        if (!Double.isFinite(value) || value < min - EPSILON || value > max + EPSILON) {
            throw new IllegalStateException("Invalid environmental radiation generator value for " + key + ": " + value);
        }
        if (Math.abs(value - min) <= EPSILON) {
            return min;
        }
        if (Math.abs(value - max) <= EPSILON) {
            return max;
        }
        return value;
    }

    private static int readInt(CompoundTag nbt, String key, int min, int max) {
        if (!nbt.contains(key)) {
            return min;
        }
        if (!nbt.contains(key, Tag.TAG_INT)) {
            throw new IllegalStateException("Invalid environmental radiation generator integer tag for " + key);
        }
        int value = nbt.getInt(key);
        if (value < min || value > max) {
            throw new IllegalStateException("Invalid environmental radiation generator value for " + key + ": " + value);
        }
        return value;
    }

    private void validateConversionState() {
        if (cycleConverted > cycleConversionQuota + EPSILON) {
            throw new IllegalStateException("Environmental radiation generator converted energy exceeds its cycle quota");
        }
        if (conversionPhase == ConversionPhase.ABSORBING && stableTicks != 0) {
            throw new IllegalStateException("Absorbing environmental radiation generator cannot have stable ticks");
        }
        if (conversionPhase == ConversionPhase.STABILIZING && stableTicks >= STABLE_TICKS_REQUIRED) {
            throw new IllegalStateException("Stabilizing environmental radiation generator has completed its stable period");
        }
        if (conversionPhase == ConversionPhase.CONVERTING && stableTicks != STABLE_TICKS_REQUIRED) {
            throw new IllegalStateException("Converting environmental radiation generator requires a completed stable period");
        }
    }

}
