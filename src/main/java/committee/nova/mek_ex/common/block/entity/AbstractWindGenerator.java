package committee.nova.mek_ex.common.block.entity;

import mekanism.api.*;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.TileEntityGenerator;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongSupplier;
import java.util.Objects;
import committee.nova.mek_ex.common.upgrade.WindGeneratorUpgradeData;
import committee.nova.mek_ex.init.enums.MEXWindTier;

public abstract class AbstractWindGenerator extends TileEntityGenerator implements IBoundingBlock {

    private static final float SPEED = 32F;
    private static final RelativeSide[] ENERGY_SIDES = {RelativeSide.FRONT, RelativeSide.BOTTOM};

    private float angle;
    private double currentMultiplier = 0;
    private boolean isBlacklistDimension;
    @WrappingComputerMethod(wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy item slot")
    EnergyInventorySlot energySlot;

    public AbstractWindGenerator(Holder<Block> blockProvider,BlockPos pos, BlockState state,@NotNull LongSupplier maxOutput) {
        super(blockProvider, pos, state,maxOutput);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), listener, 143, 35));
        return builder.build();
    }

    @Override
    protected RelativeSide[] getEnergySides() {
        return ENERGY_SIDES;
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.drainContainer();

        if (isBlacklistDimension) {
            return sendUpdatePacket;
        }
        if (ticker % SharedConstants.TICKS_PER_SECOND == 0) {

            currentMultiplier = getMultiplier();
            setActive(canFunction() && currentMultiplier != 0L);
        }
        if (currentMultiplier != 0L && canFunction() && getEnergyContainer().getNeeded() > 0L) {
            getEnergyContainer().insert(getCurrentGeneration(), Action.EXECUTE, AutomationType.INTERNAL);
        }
        return sendUpdatePacket;
    }

    public long getCurrentGeneration() {
        return Math.round(getWindTier().getGenerationRate() * currentMultiplier);
    }

    protected MEXWindTier getWindTier() {
        BaseTier baseTier = Objects.requireNonNull(Attribute.getBaseTier(getBlockHolder()), "Wind generator is missing a tier attribute");
        for (MEXWindTier tier : MEXWindTier.values()) {
            if (tier.getBaseTier() == baseTier) {
                return tier;
            }
        }
        throw new IllegalStateException("Unsupported wind generator tier: " + baseTier);
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (getActive()) {
            angle = (angle + getHeightSpeedRatio()) % 360;
        }
    }

    public float getHeightSpeedRatio() {
        int height = getBlockPos().getY() + 4;
        if (level == null) {

            return SPEED * height / 384F;
        }

        int minBuildHeight = level.getMinBuildHeight();
        height -= minBuildHeight;
        return SPEED * height / (level.getMaxBuildHeight() - minBuildHeight);
    }

    private double getMultiplier() {
        if (level != null) {
            BlockPos top = getBlockPos().above(4);

            if (level.getFluidState(top).isEmpty() && level.canSeeSky(top)) {
                int minBuildHeight = level.getMinBuildHeight();

                int maxLevelHeight = Math.min(level.getMaxBuildHeight(), minBuildHeight + level.dimensionType().logicalHeight()) - 1;
                int minY = Math.max(MekanismGeneratorsConfig.generators.windGenerationMinY.get(), minBuildHeight);
                int maxY = Math.min(MekanismGeneratorsConfig.generators.windGenerationMaxY.get(), maxLevelHeight);
                int clampedY = Math.min(maxY, Math.max(minY, top.getY()));
                long minG = MekanismGeneratorsConfig.generators.windGenerationMin.get();
                long maxG = MekanismGeneratorsConfig.generators.windGenerationMax.get();
                double slope = ((double) (maxG - minG)) / (maxY - minY);
                double toGen = minG + (slope * (clampedY - minY));
                return (toGen / minG);
            }
        }
        return 0L;
    }

    @Override
    public void setLevel(@NotNull Level world) {
        super.setLevel(world);

        isBlacklistDimension = world.dimensionTypeRegistration().is(MekanismAPITags.DimensionTypes.NO_WIND);
        if (isBlacklistDimension) {
            setActive(false);
        }
    }

    public double getCurrentMultiplier() {
        return currentMultiplier;
    }

    public float getAngle() {
        return angle;
    }

    @ComputerMethod(nameOverride = "isBlacklistedDimension")
    public boolean isBlacklistDimension() {
        return isBlacklistDimension;
    }

    @Override
    public SoundSource getSoundCategory() {
        return SoundSource.WEATHER;
    }

    @Override
    public BlockPos getSoundPos() {
        return super.getSoundPos().above(4);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getCurrentMultiplier, value -> currentMultiplier = value));
        container.track(SyncableBoolean.create(this::isBlacklistDimension, value -> isBlacklistDimension = value));
    }

    @Override
    public void parseUpgradeData(net.minecraft.core.HolderLookup.Provider provider, @NotNull IUpgradeData upgradeData) {
        if (upgradeData instanceof WindGeneratorUpgradeData data) {
            redstone = data.redstone;
            setControlType(data.controlType);
            getEnergyContainer().setEnergy(data.energyContainer.getEnergy());
            energySlot.deserializeNBT(provider, data.energySlot.serializeNBT(provider));
            for (mekanism.common.tile.component.ITileComponent component : getComponents()) {
                component.read(data.components, provider);
            }
        } else {
            super.parseUpgradeData(provider, upgradeData);
        }
    }

    @NotNull
    @Override
    public WindGeneratorUpgradeData getUpgradeData(net.minecraft.core.HolderLookup.Provider provider) {
        return new WindGeneratorUpgradeData(provider, redstone, getControlType(), getEnergyContainer(), energySlot, getComponents());
    }

    @Override
    protected long getProductionRate() {
        return getActive() ? getCurrentGeneration() : 0L;
    }
}
