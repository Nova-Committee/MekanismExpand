package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.item.CapacityUpgradeItem;
import committee.nova.mek_ex.common.item.ElectricSkateboardItem;
import mekanism.api.gear.IModuleHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MEXItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MekEXMod.MOD_ID);

    public static final DeferredItem<CapacityUpgradeItem> capacity_upgrade = ITEMS.register("upgrade_capacity",()-> new CapacityUpgradeItem(new Item.Properties()));
    public static final DeferredItem<ElectricSkateboardItem> electric_skateboard = ITEMS.register("electric_skateboard", () -> new ElectricSkateboardItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MODULE_SONAR_DETECTION = ITEMS.register("module_sonar_detection_unit",
          () -> IModuleHelper.INSTANCE.createModuleItem(() -> MEXModules.SONAR_DETECTION_UNIT, new Item.Properties().rarity(Rarity.RARE)));

    public static void register(IEventBus bus){
        ITEMS.register(bus);
    }
}
