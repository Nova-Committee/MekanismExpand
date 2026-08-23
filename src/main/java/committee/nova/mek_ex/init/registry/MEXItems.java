package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.item.CapacityUpgradeItem;
import mekanism.api.Upgrade;
import mekanism.common.item.ItemUpgrade;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MEXItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MekEXMod.MOD_ID);

    public static final DeferredItem<CapacityUpgradeItem> capacity_upgrade = ITEMS.register("upgrade_capacity",()-> new CapacityUpgradeItem(new Item.Properties()));


    public static void register(IEventBus bus){
        ITEMS.register(bus);
    }
}
