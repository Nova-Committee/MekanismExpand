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
    CAPACITY_UPGRADE_EFFECT("gui", "upgrades.capacity_effect");

    private final String key;

    MEXLang(String type, String path) {
        key = Util.makeDescriptionId(type, MekEXMod.rl(path));
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
