package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.block.entity.TileEntityAdvancedWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityEliteWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityBasicWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityUltimateWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityNuclearControlTank;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;

public class MEXContainerTypes {

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MekEXMod.MOD_ID);

    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityBasicWindGenerator>> BASIC_WIND_GENERATOR = CONTAINER_TYPES.custom(MEXBlocks.basic_wind_generator, TileEntityBasicWindGenerator.class).armorSideBar(-20, 11, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityAdvancedWindGenerator>> ADVANCED_WIND_GENERATOR = CONTAINER_TYPES.custom(MEXBlocks.advanced_wind_generator, TileEntityAdvancedWindGenerator.class).armorSideBar(-20, 11, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityEliteWindGenerator>> ELITE_WIND_GENERATOR = CONTAINER_TYPES.custom(MEXBlocks.elite_wind_generator, TileEntityEliteWindGenerator.class).armorSideBar(-20, 11, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityUltimateWindGenerator>> ULTIMATE_WIND_GENERATOR = CONTAINER_TYPES.custom(MEXBlocks.ultimate_wind_generator, TileEntityUltimateWindGenerator.class).armorSideBar(-20, 11, 0).build();
    public static final ContainerTypeRegistryObject<MekanismTileContainer<TileEntityNuclearControlTank>> NUCLEAR_CONTROL_TANK = CONTAINER_TYPES.custom(MEXBlocks.nuclear_control_tank, TileEntityNuclearControlTank.class).armorSideBar().build();
}
