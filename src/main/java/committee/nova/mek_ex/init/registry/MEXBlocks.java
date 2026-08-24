package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.block.entity.TileEntityAdvancedWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityEliteWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityBasicWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityUltimateWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityNuclearControlTank;
import committee.nova.mek_ex.common.block.entity.TileEntityNuclearControlValve;
import committee.nova.mek_ex.common.item.WindGeneratorItem;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.item.ItemSlotsBuilder;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.generators.common.content.blocktype.Generator;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;

public final class MEXBlocks {

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(MekEXMod.MOD_ID);


    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityBasicWindGenerator, Generator<TileEntityBasicWindGenerator>>, WindGeneratorItem> basic_wind_generator = BLOCKS.register("basic_wind_generator", () -> new BlockTile.BlockTileModel<>(MEXBlockTypes.BASIC_WIND_GENERATOR, properties -> properties.mapColor(MapColor.METAL)), WindGeneratorItem::new)
            .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));

    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityAdvancedWindGenerator, Generator<TileEntityAdvancedWindGenerator>>, WindGeneratorItem> advanced_wind_generator = BLOCKS.register("advanced_wind_generator", () -> new BlockTile.BlockTileModel<>(MEXBlockTypes.ADVANCED_WIND_GENERATOR, properties -> properties.mapColor(MapColor.METAL)), WindGeneratorItem::new)
            .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));

    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityEliteWindGenerator, Generator<TileEntityEliteWindGenerator>>, WindGeneratorItem> elite_wind_generator = BLOCKS.register("elite_wind_generator", () -> new BlockTile.BlockTileModel<>(MEXBlockTypes.ELITE_WIND_GENERATOR, properties -> properties.mapColor(MapColor.METAL)), WindGeneratorItem::new)
            .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));

    public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityUltimateWindGenerator, Generator<TileEntityUltimateWindGenerator>>, WindGeneratorItem> ultimate_wind_generator = BLOCKS.register("ultimate_wind_generator", () -> new BlockTile.BlockTileModel<>(MEXBlockTypes.ULTIMATE_WIND_GENERATOR, properties -> properties.mapColor(MapColor.METAL)), WindGeneratorItem::new)
            .forItemHolder(holder -> holder.addAttachmentOnlyContainers(ContainerType.ITEM, () -> ItemSlotsBuilder.builder().addEnergy().build()));

    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityNuclearControlTank>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityNuclearControlTank>>> nuclear_control_tank = BLOCKS.registerDetails("nuclear_control_tank", () -> new BlockBasicMultiblock<>(MEXBlockTypes.NUCLEAR_CONTROL_TANK, properties -> properties.mapColor(MapColor.COLOR_GRAY)));

    public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityNuclearControlValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityNuclearControlValve>>> nuclear_control_valve = BLOCKS.registerDetails("nuclear_control_valve", () -> new BlockBasicMultiblock<>(MEXBlockTypes.NUCLEAR_CONTROL_VALVE, properties -> properties.mapColor(MapColor.COLOR_GRAY)));



    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

}
