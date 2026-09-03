package committee.nova.mek_ex.common.inventory.container;

import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import committee.nova.mek_ex.init.registry.MEXContainerTypes;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public final class ElectricSkateboardContainer extends MekanismEntityContainer<EntityElectricSkateboard> {
    public ElectricSkateboardContainer(int id, Inventory inventory, EntityElectricSkateboard entity) {
        super(MEXContainerTypes.ELECTRIC_SKATEBOARD, id, inventory, entity);
        entity.addContainerTrackers(this);
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        for (IInventorySlot slot : entity.getInventorySlots(null)) {
            Slot containerSlot = slot.createContainerSlot();
            if (containerSlot != null) addSlot(containerSlot);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return entity.isAlive() && entity.distanceToSqr(player) <= 64 && entity.canAccess(player);
    }
}
