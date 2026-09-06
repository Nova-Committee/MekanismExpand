package committee.nova.mek_ex.common.multiblock;

import mekanism.common.lib.multiblock.MultiblockManager;

public final class MekanismHeartManager {

    public static final MultiblockManager<MekanismHeartMultiblockData> MANAGER = new MultiblockManager<>(
          "mekanismHeart", MekanismHeartCache::new, MekanismHeartValidator::new
    );

    private MekanismHeartManager() {
    }
}
