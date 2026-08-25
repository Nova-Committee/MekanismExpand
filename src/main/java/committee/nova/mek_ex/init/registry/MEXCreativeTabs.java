package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.init.enums.MEXLang;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MEXCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MekEXMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_TABS.register("expand_group", () -> CreativeModeTab.builder()
            .title(Component.translatable(MEXLang.MEKANISM_EXPAND.getTranslationKey()))
            .icon(MEXItems.capacity_upgrade.get()::getDefaultInstance)
            .displayItems((parameters, output) -> {
                output.accept(MEXItems.capacity_upgrade);
                output.accept(MEXBlocks.basic_wind_generator);
                output.accept(MEXBlocks.advanced_wind_generator);
                output.accept(MEXBlocks.elite_wind_generator);
                output.accept(MEXBlocks.ultimate_wind_generator);
                output.accept(MEXBlocks.nuclear_control_tank);
                output.accept(MEXBlocks.nuclear_control_valve);
                output.accept(MEXBlocks.neutron_activator);
                output.accept(MEXBlocks.antimatter_supercharged_coil);
            })
        .build());

}
