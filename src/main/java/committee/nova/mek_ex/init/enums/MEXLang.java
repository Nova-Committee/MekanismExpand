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
    DESCRIPTION_MEKANISM_HEART("description", "mekanism_heart"),
    MEKANISM_HEART_INVALID_BLOCK("multiblock", "mekanism_heart.invalid_block"),
    CAPACITY_UPGRADE("upgrade", "capacity"),
    CAPACITY_UPGRADE_DESCRIPTION("upgrade", "capacity.desc"),
    CAPACITY_UPGRADE_EFFECT("gui", "upgrades.capacity_effect"),
    VOID_UPGRADE("upgrade", "void"),
    VOID_UPGRADE_DESCRIPTION("upgrade", "void.desc"),
    VOID_UPGRADE_EFFECT("gui", "upgrades.void_effect"),
    MODULE_SONAR_DETECTION("module", "sonar_detection_unit"),
    SONAR_FILTERS("gui", "sonar_filters"),
    SONAR_CONFIGURE_FILTERS("gui", "sonar_configure_filters"),
    SONAR_ADD_ITEM("gui", "sonar_add_item"),
    SONAR_ADD_TAG("gui", "sonar_add_tag"),
    SONAR_ADD_MODID("gui", "sonar_add_modid"),
    SONAR_ADD_ENTITY_ID("gui", "sonar_add_entity_id"),
    SONAR_ADD_ENTITY_TAG("gui", "sonar_add_entity_tag"),
    SONAR_TOGGLE("gui", "sonar_toggle"),
    SONAR_REMOVE("gui", "sonar_remove"),
    SONAR_DONE("gui", "sonar_done"),
    SKATEBOARD_GEAR("gui", "skateboard_gear"),
    SKATEBOARD_MAX_SPEED("gui", "skateboard_max_speed"),
    SKATEBOARD_DRIVE_COST("gui", "skateboard_drive_cost"),
    SKATEBOARD_GEAR_UP("gui", "skateboard_gear_up"),
    SKATEBOARD_GEAR_DOWN("gui", "skateboard_gear_down"),

    DESCRIPTION_MULTIBLOCKS_BUILDER("description", "multiblocks_builder"),
    STRUCTURE_BUILDER("gui", "multiblocks_builder"),
    STRUCTURE_BUILDER_TOGGLE("gui", "multiblocks_builder.toggle"),
    STRUCTURE_BUILDER_RESET("gui", "multiblocks_builder.reset"),
    STRUCTURE_BUILDER_ROTATE("gui", "multiblocks_builder.rotate"),
    STRUCTURE_BUILDER_PROGRESS("gui", "multiblocks_builder.progress"),
    STRUCTURE_BUILDER_SIZE("gui", "multiblocks_builder.size"),
    STRUCTURE_BUILDER_ROTATION("gui", "multiblocks_builder.rotation"),
    STRUCTURE_BUILDER_COILS("gui", "multiblocks_builder.coils"),
    STRUCTURE_BUILDER_ROTORS("gui", "multiblocks_builder.rotors"),
    STRUCTURE_BUILDER_HEATERS("gui", "multiblocks_builder.heaters"),
    STRUCTURE_BUILDER_INTERIOR("gui", "multiblocks_builder.interior"),
    STRUCTURE_BUILDER_OPTIONAL("gui", "multiblocks_builder.optional"),
    STRUCTURE_BUILDER_PREV_RECIPE("gui", "multiblocks_builder.prev_recipe"),
    STRUCTURE_BUILDER_NEXT_RECIPE("gui", "multiblocks_builder.next_recipe"),
    STRUCTURE_BUILDER_RECIPE_INDEX("gui", "multiblocks_builder.recipe_index"),
    STRUCTURE_BUILDER_NO_RECIPE("gui", "multiblocks_builder.no_recipe"),
    STRUCTURE_BUILDER_MATERIALS("gui", "multiblocks_builder.materials"),
    STRUCTURE_BUILDER_MATERIAL_SLOTS("gui", "multiblocks_builder.material_slots"),
    STRUCTURE_BUILDER_MATERIALS_MORE("gui", "multiblocks_builder.materials_more"),
    STRUCTURE_BUILDER_MATERIAL_COUNT("gui", "multiblocks_builder.material_count"),
    STRUCTURE_BUILDER_STATUS_IDLE("gui", "multiblocks_builder.status.idle"),
    STRUCTURE_BUILDER_STATUS_BUILDING("gui", "multiblocks_builder.status.building"),
    STRUCTURE_BUILDER_STATUS_MISSING("gui", "multiblocks_builder.status.missing"),
    STRUCTURE_BUILDER_STATUS_BLOCKED("gui", "multiblocks_builder.status.blocked"),
    STRUCTURE_BUILDER_STATUS_DONE("gui", "multiblocks_builder.status.done"),
    STRUCTURE_BUILDER_STATUS_NO_RECIPE("gui", "multiblocks_builder.status.no_recipe"),
    STRUCTURE_BUILDER_STATUS_NO_ENERGY("gui", "multiblocks_builder.status.no_energy");

    private final String key;

    MEXLang(String type, String path) {
        key = Util.makeDescriptionId(type, MekEXMod.rl(path));
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
