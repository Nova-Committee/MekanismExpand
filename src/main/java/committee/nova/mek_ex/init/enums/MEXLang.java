package committee.nova.mek_ex.init.enums;

import committee.nova.mek_ex.MekEXMod;
import mekanism.api.text.ILangEntry;
import net.minecraft.Util;

public enum MEXLang implements ILangEntry {
    MEKANISM_EXPAND("itemGroup", "mekanism_expand"),
    DESCRIPTION_BASIC_WIND_GENERATOR("description", "basic_wind_generator"),
    DESCRIPTION_ADVANCED_WIND_GENERATOR("description", "advanced_wind_generator"),
    DESCRIPTION_ELITE_WIND_GENERATOR("description", "elite_wind_generator"),
    DESCRIPTION_ULTIMATE_WIND_GENERATOR("description", "ultimate_wind_generator"),
    NUCLEAR_CONTROL_TANK("gui", "nuclear_control_tank"),
    DESCRIPTION_NUCLEAR_CONTROL_TANK("description", "nuclear_control_tank"),
    DESCRIPTION_NUCLEAR_CONTROL_VALVE("description", "nuclear_control_valve"),
    DESCRIPTION_NEUTRON_ACTIVATOR("description", "neutron_activator"),
    NEUTRON_ACTIVATOR("gui", "neutron_activator"),
    DESCRIPTION_ENVIRONMENTAL_RADIATION_GENERATOR("description", "environmental_radiation_generator"),
    ENVIRONMENTAL_RADIATION_GENERATOR("gui", "environmental_radiation_generator"),
    DESCRIPTION_POTION_NEBULIZER("description", "potion_nebulizer"),
    POTION_NEBULIZER("gui", "potion_nebulizer"),
    RADIATION_STORAGE("gui", "radiation_storage"),
    ENVIRONMENTAL_RADIATION("gui", "environmental_radiation"),
    GENERATION_RATE("gui", "generation_rate"),
    MAX_OUTPUT("gui", "max_output"),
    DESCRIPTION_ANTIMATTER_SUPERCHARGED_COIL("description", "antimatter_supercharged_coil"),
    CAPACITY_UPGRADE("upgrade", "capacity"),
    CAPACITY_UPGRADE_DESCRIPTION("upgrade", "capacity.desc"),
    CAPACITY_UPGRADE_EFFECT("gui", "upgrades.capacity_effect"),
    MODULE_SONAR_DETECTION("module", "sonar_detection_unit"),
    SONAR_FILTERS("gui", "sonar_filters"),
    SONAR_CONFIGURE_FILTERS("gui", "sonar_configure_filters"),
    SONAR_ADD_ITEM("gui", "sonar_add_item"),
    SONAR_ADD_TAG("gui", "sonar_add_tag"),
    SONAR_ADD_MODID("gui", "sonar_add_modid"),
    SONAR_TOGGLE("gui", "sonar_toggle"),
    SONAR_REMOVE("gui", "sonar_remove"),
    SONAR_DONE("gui", "sonar_done"),
    SKATEBOARD_GEAR("gui", "skateboard_gear"),
    SKATEBOARD_MAX_SPEED("gui", "skateboard_max_speed"),
    SKATEBOARD_DRIVE_COST("gui", "skateboard_drive_cost"),
    SKATEBOARD_GEAR_UP("gui", "skateboard_gear_up"),
    SKATEBOARD_GEAR_DOWN("gui", "skateboard_gear_down");

    private final String key;

    MEXLang(String type, String path) {
        key = Util.makeDescriptionId(type, MekEXMod.rl(path));
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
