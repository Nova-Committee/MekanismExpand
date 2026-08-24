package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.init.enums.MEXLang;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public final class MEXCreativeTabs {

    public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(MekEXMod.MOD_ID, MEXCreativeTabs::addToExistingTabs);
    public static final MekanismDeferredHolder<CreativeModeTab, CreativeModeTab> MEKANISM_EXPAND = CREATIVE_TABS.registerMain(
          MEXLang.MEKANISM_EXPAND, MEXBlocks.basic_wind_generator.getItemHolder(), builder -> builder
                .withTabsBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .displayItems((parameters, output) -> {
                    output.accept(MEXItems.capacity_upgrade);
                    CreativeTabDeferredRegister.addToDisplay(MEXBlocks.BLOCKS, output);
                }));

    private MEXCreativeTabs() {
    }

    private static void addToExistingTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tabKey = event.getTabKey();
        if (tabKey == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            CreativeTabDeferredRegister.addToDisplay(event, MEXBlocks.basic_wind_generator, MEXBlocks.advanced_wind_generator,
                  MEXBlocks.elite_wind_generator, MEXBlocks.ultimate_wind_generator,
                  MEXBlocks.nuclear_control_tank, MEXBlocks.nuclear_control_valve);
        } else if (tabKey == CreativeModeTabs.INGREDIENTS) {
            CreativeTabDeferredRegister.addToDisplay(event, MEXItems.capacity_upgrade);
        }
    }
}
