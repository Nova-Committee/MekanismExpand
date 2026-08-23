package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.block.entity.TileEntityAdvancedWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityEliteWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityBasicWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityUltimateWindGenerator;
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
}
