package committee.nova.mek_ex.common.multiblock;

import mekanism.common.lib.multiblock.MultiblockManager;


public final class NuclearControlTankManager {

    public static final MultiblockManager<NuclearControlTankMultiblockData> MANAGER = new MultiblockManager<>(
          "nuclearControlTank", NuclearControlTankCache::new, NuclearControlTankValidator::new
    );

    private NuclearControlTankManager() {
    }
}
