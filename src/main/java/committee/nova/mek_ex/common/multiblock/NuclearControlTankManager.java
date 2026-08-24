package committee.nova.mek_ex.common.multiblock;

import mekanism.common.lib.multiblock.MultiblockManager;

/**
 * Multiblock manager for nuclear control tanks.
 *
 * <p>The data model is the nuclear-control merged fluid/chemical tank, while this
 * manager keeps nuclear control tanks from joining ordinary dynamic tanks.</p>
 */
public final class NuclearControlTankManager {

    public static final MultiblockManager<NuclearControlTankMultiblockData> MANAGER = new MultiblockManager<>(
          "nuclearControlTank", NuclearControlTankCache::new, NuclearControlTankValidator::new
    );

    private NuclearControlTankManager() {
    }
}
