package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.block.entity.TileEntityAdvancedWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityEliteWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityBasicWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityUltimateWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityNuclearControlTank;
import committee.nova.mek_ex.common.block.entity.TileEntityNuclearControlValve;
import committee.nova.mek_ex.common.block.entity.TileEntityNeutronActivator;
import committee.nova.mek_ex.common.block.entity.TileEntityAntimatterSuperchargedCoil;
import committee.nova.mek_ex.common.block.entity.TileEntityEnvironmentalRadiationGenerator;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;

public final class MEXGenTileEntityTypes {

    private MEXGenTileEntityTypes() {
    }

    public static final TileEntityTypeDeferredRegister TILE_ENTITY_TYPES = new TileEntityTypeDeferredRegister(MekEXMod.MOD_ID);

    public static final TileEntityTypeRegistryObject<TileEntityBasicWindGenerator> BASIC_WIND_GENERATOR = TILE_ENTITY_TYPES.mekBuilder(MEXBlocks.basic_wind_generator, TileEntityBasicWindGenerator::new)
            .clientTicker(TileEntityMekanism::tickClient)
            .serverTicker(TileEntityMekanism::tickServer)
            .withSimple(Capabilities.CONFIG_CARD)
            .build();

    public static final TileEntityTypeRegistryObject<TileEntityAdvancedWindGenerator> ADVANCED_WIND_GENERATOR = TILE_ENTITY_TYPES.mekBuilder(MEXBlocks.advanced_wind_generator, TileEntityAdvancedWindGenerator::new)
            .clientTicker(TileEntityMekanism::tickClient)
            .serverTicker(TileEntityMekanism::tickServer)
            .withSimple(Capabilities.CONFIG_CARD)
            .build();

    public static final TileEntityTypeRegistryObject<TileEntityEliteWindGenerator> ELITE_WIND_GENERATOR = TILE_ENTITY_TYPES.mekBuilder(MEXBlocks.elite_wind_generator, TileEntityEliteWindGenerator::new)
            .clientTicker(TileEntityMekanism::tickClient)
            .serverTicker(TileEntityMekanism::tickServer)
            .withSimple(Capabilities.CONFIG_CARD)
            .build();

    public static final TileEntityTypeRegistryObject<TileEntityUltimateWindGenerator> ULTIMATE_WIND_GENERATOR = TILE_ENTITY_TYPES.mekBuilder(MEXBlocks.ultimate_wind_generator, TileEntityUltimateWindGenerator::new)
            .clientTicker(TileEntityMekanism::tickClient)
            .serverTicker(TileEntityMekanism::tickServer)
            .withSimple(Capabilities.CONFIG_CARD)
            .build();

    public static final TileEntityTypeRegistryObject<TileEntityNuclearControlTank> NUCLEAR_CONTROL_TANK = TILE_ENTITY_TYPES.mekBuilder(MEXBlocks.nuclear_control_tank, TileEntityNuclearControlTank::new)
            .clientTicker(TileEntityMekanism::tickClient).serverTicker(TileEntityMekanism::tickServer)
            .withSimple(Capabilities.CONFIGURABLE).without(Capabilities.ITEM.block()).build();

    public static final TileEntityTypeRegistryObject<TileEntityNuclearControlValve> NUCLEAR_CONTROL_VALVE = TILE_ENTITY_TYPES.mekBuilder(MEXBlocks.nuclear_control_valve, TileEntityNuclearControlValve::new)
            .clientTicker(TileEntityMekanism::tickClient).serverTicker(TileEntityMekanism::tickServer)
            .withSimple(Capabilities.CONFIGURABLE).build();

    public static final TileEntityTypeRegistryObject<TileEntityNeutronActivator> NEUTRON_ACTIVATOR = TILE_ENTITY_TYPES.mekBuilder(MEXBlocks.neutron_activator, TileEntityNeutronActivator::new)
            .clientTicker(TileEntityMekanism::tickClient)
            .serverTicker(TileEntityMekanism::tickServer)
            .withSimple(Capabilities.CONFIG_CARD)
            .build();

    public static final TileEntityTypeRegistryObject<TileEntityEnvironmentalRadiationGenerator> ENVIRONMENTAL_RADIATION_GENERATOR = TILE_ENTITY_TYPES.mekBuilder(MEXBlocks.environmental_radiation_generator, TileEntityEnvironmentalRadiationGenerator::new)
            .clientTicker(TileEntityMekanism::tickClient)
            .serverTicker(TileEntityMekanism::tickServer)
            .withSimple(Capabilities.CONFIG_CARD)
            .build();

    public static final TileEntityTypeRegistryObject<TileEntityAntimatterSuperchargedCoil> ANTIMATTER_SUPERCHARGED_COIL = TILE_ENTITY_TYPES.mekBuilder(MEXBlocks.antimatter_supercharged_coil, TileEntityAntimatterSuperchargedCoil::new)
            .serverTicker(TileEntityMekanism::tickServer)
            .build();
}
