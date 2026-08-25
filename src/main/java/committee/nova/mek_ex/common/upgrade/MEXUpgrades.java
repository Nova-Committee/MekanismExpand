package committee.nova.mek_ex.common.upgrade;

import java.util.Arrays;
import mekanism.api.Upgrade;


public final class MEXUpgrades {

    private MEXUpgrades() {
    }

    public static Upgrade capacity() {
        return Arrays.stream(Upgrade.values())
              .filter(upgrade -> "capacity".equals(upgrade.getSerializedName()))
              .findFirst()
              .orElseThrow(() -> new IllegalStateException("Capacity upgrade was not initialized"));
    }
}
