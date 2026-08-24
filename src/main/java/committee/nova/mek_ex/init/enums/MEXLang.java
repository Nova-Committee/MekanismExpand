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
