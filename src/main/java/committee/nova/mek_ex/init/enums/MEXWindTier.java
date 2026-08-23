package committee.nova.mek_ex.init.enums;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.api.energy.IEnergyConversionHelper;

/**
 * Defines the generation and storage balance for each wind generator tier.
 */
public enum MEXWindTier implements ITier {
    // Public balance values are authored in FE; Mekanism APIs receive Joules below.
    BASIC(BaseTier.BASIC, 120_000L, 34L),
    ADVANCED(BaseTier.ADVANCED, 260_000L, 68L),
    ELITE(BaseTier.ELITE, 520_000L, 88L),
    ULTIMATE(BaseTier.ULTIMATE, 720_000L, 188L);

    private final BaseTier baseTier;
    private final long energyStorageFe;
    private final long generationRateFe;

    MEXWindTier(BaseTier baseTier, long energyStorage, long generationRate) {
        this.baseTier = baseTier;
        this.energyStorageFe = energyStorage;
        this.generationRateFe = generationRate;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public long getEnergyStorage() {
        return IEnergyConversionHelper.INSTANCE.feConversion().convertFrom(energyStorageFe);
    }

    public long getGenerationRate() {
        return IEnergyConversionHelper.INSTANCE.feConversion().convertFrom(generationRateFe);
    }

    /**
     * The original wind generator outputs sixteen times its minimum-height
     * generation rate. TileEntityGenerator doubles the constructor value, so
     * eight times the tier rate is passed to the parent.
     */
    public long getGeneratorConstructorOutput() {
        return Math.multiplyExact(getGenerationRate(), 8L);
    }

    public long getOutputRate() {
        return Math.multiplyExact(getGenerationRate(), 16L);
    }
}
