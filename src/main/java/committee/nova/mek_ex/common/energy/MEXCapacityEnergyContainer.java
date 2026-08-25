package committee.nova.mek_ex.common.energy;

import mekanism.api.IContentsListener;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import committee.nova.mek_ex.common.upgrade.MEXUpgrades;
import mekanism.api.functions.ConstantPredicates;

public final class MEXCapacityEnergyContainer extends BasicEnergyContainer {

    private final TileEntityMekanism tile;
    private final long baseCapacity;

    public MEXCapacityEnergyContainer(TileEntityMekanism tile, long baseCapacity, IContentsListener listener) {
        super(baseCapacity, ConstantPredicates.alwaysTrue(), internalOnly, listener);
        this.tile = tile;
        this.baseCapacity = baseCapacity;
    }

    @Override
    public long getMaxEnergy() {
        int upgrades = tile.getComponent() == null ? 0 : tile.getComponent().getUpgrades(MEXUpgrades.capacity());
        return multiplyCapacity(baseCapacity, upgrades);
    }

    private static long multiplyCapacity(long baseCapacity, int upgrades) {
        long capacity = baseCapacity;
        for (int i = 0; i < upgrades; i++) {
            capacity = Math.multiplyExact(capacity, 3L) / 2L;
        }
        return capacity;
    }

}
