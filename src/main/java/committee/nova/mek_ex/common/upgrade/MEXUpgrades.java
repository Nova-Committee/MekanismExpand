package committee.nova.mek_ex.common.upgrade;

import java.util.Arrays;
import mekanism.api.Upgrade;

public final class MEXUpgrades {

    private MEXUpgrades() {
    }

    public static Upgrade capacity() {
        return byName("capacity");
    }

    public static Upgrade voidUpgrade() {
        return byName("void");
    }

    private static Upgrade byName(String name) {
        return Arrays.stream(Upgrade.values())
              .filter(upgrade -> name.equals(upgrade.getSerializedName()))
              .findFirst()
              .orElseThrow(() -> new IllegalStateException(name + " upgrade was not initialized"));
    }
}
