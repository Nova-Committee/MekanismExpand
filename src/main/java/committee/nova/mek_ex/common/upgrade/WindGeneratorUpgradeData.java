package committee.nova.mek_ex.common.upgrade;

import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.upgrade.IUpgradeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;


public final class WindGeneratorUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final RedstoneControl controlType;
    public final IEnergyContainer energyContainer;
    public final EnergyInventorySlot energySlot;
    public final CompoundTag components;

    public WindGeneratorUpgradeData(HolderLookup.Provider provider, boolean redstone, RedstoneControl controlType,
          IEnergyContainer energyContainer, EnergyInventorySlot energySlot, List<ITileComponent> tileComponents) {
        this.redstone = redstone;
        this.controlType = controlType;
        this.energyContainer = energyContainer;
        this.energySlot = energySlot;
        this.components = new CompoundTag();
        for (ITileComponent component : tileComponents) {
            component.write(this.components, provider);
        }
    }
}
