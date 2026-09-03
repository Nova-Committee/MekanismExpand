package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.content.gear.mekasuit.ModuleSonarDetectionUnit;
import committee.nova.mek_ex.common.gear.config.ModuleSonarFiltersConfig;
import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import net.neoforged.bus.api.IEventBus;

public final class MEXModules {
    private MEXModules() {
    }

    public static final ModuleDeferredRegister MODULES = new ModuleDeferredRegister(MekEXMod.MOD_ID);

    public static final ModuleRegistryObject<ModuleSonarDetectionUnit> SONAR_DETECTION_UNIT = MODULES.register(
          "sonar_detection_unit",
          ModuleSonarDetectionUnit::new,
          () -> MEXItems.MODULE_SONAR_DETECTION,
          builder -> builder.maxStackSize(4)
                .rendersHUD()
                .addConfig(ModuleSonarFiltersConfig.DEFAULT, ModuleSonarFiltersConfig.CODEC, ModuleSonarFiltersConfig.STREAM_CODEC)
    );

    public static void register(IEventBus bus) {
        MODULES.register(bus);
    }
}
