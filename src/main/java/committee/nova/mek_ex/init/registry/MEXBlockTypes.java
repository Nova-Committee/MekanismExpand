package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.common.block.entity.TileEntityAdvancedWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityEliteWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityBasicWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityUltimateWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityNuclearControlTank;
import committee.nova.mek_ex.common.block.entity.TileEntityNuclearControlValve;
import committee.nova.mek_ex.common.block.entity.TileEntityNeutronActivator;
import committee.nova.mek_ex.common.block.entity.TileEntityAntimatterSuperchargedCoil;
import committee.nova.mek_ex.common.block.entity.TileEntityEnvironmentalRadiationGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityPotionNebulizer;
import committee.nova.mek_ex.common.upgrade.MEXUpgrades;
import committee.nova.mek_ex.init.enums.MEXLang;
import committee.nova.mek_ex.init.enums.MEXWindTier;
import mekanism.api.Upgrade;
import mekanism.api.energy.IEnergyConversionHelper;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.AttributeCustomSelectionBox;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeStateFacing.FacePlacementType;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.BlockTypeTile.BlockTileBuilder;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.content.blocktype.BlockShapes;
import mekanism.generators.common.content.blocktype.Generator;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MEXBlockTypes {

    private static final AttributeUpgradeSupport GENERATOR_UPGRADES = AttributeUpgradeSupport.create(Upgrade.MUFFLING, MEXUpgrades.capacity());
    private static final AttributeHasBounding.HandleBoundingBlock WIND_BOUNDING = new AttributeHasBounding.HandleBoundingBlock() {
        @Override
        public <DATA> boolean handle(Level level, BlockPos pos, BlockState state, DATA data,
              AttributeHasBounding.TriBooleanFunction<Level, BlockPos, DATA> consumer) {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (int i = 0; i < 4; i++) {
                mutable.setWithOffset(pos, 0, i + 1, 0);
                if (!consumer.accept(level, mutable, data)) {
                    return false;
                }
            }
            return true;
        }
    };
    private static final VoxelShape[] NEUTRON_ACTIVATOR_SHAPE = {
          Block.box(0, 0, 0, 16, 16, 16),
          Block.box(0, 0, 0, 16, 16, 16),
          Block.box(0, 0, 0, 16, 16, 16),
          Block.box(0, 0, 0, 16, 16, 16)
    };
    private static final VoxelShape[] POTION_NEBULIZER_SHAPE = new VoxelShape[4];

    static {
        VoxelShape potionNebulizerNorth = VoxelShapeUtils.combine(
              Block.box(1, 0, 1, 15, 2, 15),
              Block.box(7, 2, 12, 9, 10, 14),
              Block.box(12, 2, 7, 14, 10, 9),
              Block.box(2, 2, 7, 4, 10, 9),
              Block.box(7, 2, 2, 9, 10, 4),
              Block.box(6.5, 2, 6.5, 9.5, 8, 9.5),
              Block.box(7.5, 8, 7, 9, 9, 8.75),
              Block.box(6.75, 8, 7.5, 7.5, 8.75, 8.25),
              Block.box(4, 9, 4, 12, 9.25, 12),
              Block.box(6.5, 9.25, 6.5, 9.5, 12.25, 9.5),
              Block.box(7.5, 12.25, 7.5, 8.5, 12.75, 8.5),
              Block.box(7, 12.75, 7, 9, 13.75, 9),
              Block.box(4, 2, 4, 5, 9, 5),
              Block.box(4, 2, 11, 5, 9, 12),
              Block.box(11, 2, 11, 12, 9, 12),
              Block.box(11, 2, 4, 12, 9, 5),
              Block.box(1, 2, 4, 2, 10, 12),
              Block.box(6, 4, 1, 10, 8, 1),
              Block.box(4, 2, 14, 12, 10, 15),
              Block.box(14, 2, 4, 15, 10, 12)
        );
        VoxelShapeUtils.setShape(potionNebulizerNorth, POTION_NEBULIZER_SHAPE);
    }
    private static final VoxelShape ENVIRONMENTAL_RADIATION_GENERATOR_FRAME = Shapes.or(
          Block.box(0, 0, 0, 16, 5, 16),
          Block.box(4, 5, 0, 12, 13, 3),
          Block.box(4, 5, 13, 12, 13, 16),
          Block.box(0, 5, 4, 3, 13, 12),
          Block.box(13, 5, 4, 16, 13, 12),
          Block.box(3, 12, 3, 13, 14, 13),
          Block.box(4, 14, 4, 12, 16, 12)
    ).optimize();
    private static final VoxelShape[] ENVIRONMENTAL_RADIATION_GENERATOR_SHAPE = {
          ENVIRONMENTAL_RADIATION_GENERATOR_FRAME,
          ENVIRONMENTAL_RADIATION_GENERATOR_FRAME,
          ENVIRONMENTAL_RADIATION_GENERATOR_FRAME,
          ENVIRONMENTAL_RADIATION_GENERATOR_FRAME
    };

    private MEXBlockTypes() {
    }

    public static final Generator<TileEntityBasicWindGenerator> BASIC_WIND_GENERATOR = Generator.GeneratorBuilder
          .createGenerator(() -> MEXGenTileEntityTypes.BASIC_WIND_GENERATOR, GeneratorsLang.DESCRIPTION_WIND_GENERATOR)
          .withGui(() -> MEXContainerTypes.BASIC_WIND_GENERATOR)
          .withEnergyConfig(MEXWindTier.BASIC::getEnergyStorage)
          .withCustomShape(BlockShapes.WIND_GENERATOR)
          .with(AttributeCustomSelectionBox.JAVA, GENERATOR_UPGRADES, new mekanism.common.block.attribute.AttributeTier<>(MEXWindTier.BASIC))
          .with(new AttributeUpgradeable(() -> MEXBlocks.advanced_wind_generator))
          .withSound(mekanism.generators.common.registries.GeneratorsSounds.WIND_GENERATOR)
          .withBounding(WIND_BOUNDING)
          .withComputerSupport("basicWindGenerator")
          .build();

    public static final Generator<TileEntityAdvancedWindGenerator> ADVANCED_WIND_GENERATOR = Generator.GeneratorBuilder
          .createGenerator(() -> MEXGenTileEntityTypes.ADVANCED_WIND_GENERATOR,GeneratorsLang.DESCRIPTION_WIND_GENERATOR)
          .withGui(() -> MEXContainerTypes.ADVANCED_WIND_GENERATOR)
          .withEnergyConfig(MEXWindTier.ADVANCED::getEnergyStorage)
          .withCustomShape(BlockShapes.WIND_GENERATOR)
          .with(AttributeCustomSelectionBox.JAVA, GENERATOR_UPGRADES, new mekanism.common.block.attribute.AttributeTier<>(MEXWindTier.ADVANCED))
          .with(new AttributeUpgradeable(() -> MEXBlocks.elite_wind_generator))
          .withSound(mekanism.generators.common.registries.GeneratorsSounds.WIND_GENERATOR)
          .withBounding(WIND_BOUNDING)
          .withComputerSupport("advancedWindGenerator")
          .build();

    public static final Generator<TileEntityEliteWindGenerator> ELITE_WIND_GENERATOR = Generator.GeneratorBuilder
          .createGenerator(() -> MEXGenTileEntityTypes.ELITE_WIND_GENERATOR,GeneratorsLang.DESCRIPTION_WIND_GENERATOR)
          .withGui(() -> MEXContainerTypes.ELITE_WIND_GENERATOR)
          .withEnergyConfig(MEXWindTier.ELITE::getEnergyStorage)
          .withCustomShape(BlockShapes.WIND_GENERATOR)
          .with(AttributeCustomSelectionBox.JAVA, GENERATOR_UPGRADES, new mekanism.common.block.attribute.AttributeTier<>(MEXWindTier.ELITE))
          .with(new AttributeUpgradeable(() -> MEXBlocks.ultimate_wind_generator))
          .withSound(mekanism.generators.common.registries.GeneratorsSounds.WIND_GENERATOR)
          .withBounding(WIND_BOUNDING)
          .withComputerSupport("eliteWindGenerator")
          .build();

    public static final Generator<TileEntityUltimateWindGenerator> ULTIMATE_WIND_GENERATOR = Generator.GeneratorBuilder
          .createGenerator(() -> MEXGenTileEntityTypes.ULTIMATE_WIND_GENERATOR,GeneratorsLang.DESCRIPTION_WIND_GENERATOR)
          .withGui(() -> MEXContainerTypes.ULTIMATE_WIND_GENERATOR)
          .withEnergyConfig(MEXWindTier.ULTIMATE::getEnergyStorage)
          .withCustomShape(BlockShapes.WIND_GENERATOR)
          .with(AttributeCustomSelectionBox.JAVA, GENERATOR_UPGRADES, new mekanism.common.block.attribute.AttributeTier<>(MEXWindTier.ULTIMATE))
          .withSound(mekanism.generators.common.registries.GeneratorsSounds.WIND_GENERATOR)
          .withBounding(WIND_BOUNDING)
          .withComputerSupport("ultimateWindGenerator")
          .build();

    public static final BlockTypeTile<TileEntityNuclearControlTank> NUCLEAR_CONTROL_TANK = BlockTileBuilder
          .createBlock(() -> MEXGenTileEntityTypes.NUCLEAR_CONTROL_TANK, MEXLang.DESCRIPTION_NUCLEAR_CONTROL_TANK)
          .withGui(() -> MEXContainerTypes.NUCLEAR_CONTROL_TANK, MEXLang.NUCLEAR_CONTROL_TANK)
          .with(Attributes.INVENTORY)
          .externalMultiblock()
          .build();

    public static final BlockTypeTile<TileEntityNuclearControlValve> NUCLEAR_CONTROL_VALVE = BlockTileBuilder
          .createBlock(() -> MEXGenTileEntityTypes.NUCLEAR_CONTROL_VALVE, MEXLang.DESCRIPTION_NUCLEAR_CONTROL_VALVE)
          .withGui(() -> MEXContainerTypes.NUCLEAR_CONTROL_TANK, MEXLang.NUCLEAR_CONTROL_TANK)
          .with(Attributes.INVENTORY, Attributes.COMPARATOR)
          .externalMultiblock()
          .withComputerSupport("nuclearControlValve")
          .build();

    public static final Machine<TileEntityNeutronActivator> NEUTRON_ACTIVATOR = Machine.MachineBuilder
          .createMachine(() -> MEXGenTileEntityTypes.NEUTRON_ACTIVATOR, MEXLang.DESCRIPTION_NEUTRON_ACTIVATOR)
          .withGui(() -> MEXContainerTypes.NEUTRON_ACTIVATOR)
          .withEnergyConfig(() -> 1L, () -> IEnergyConversionHelper.INSTANCE.feConversion().convertFrom(28_000L))
          .without(AttributeParticleFX.class, AttributeUpgradeSupport.class)
          .withCustomShape(NEUTRON_ACTIVATOR_SHAPE)
          .with(AttributeCustomSelectionBox.JSON)
          .withSideConfig(TransmissionType.ITEM, TransmissionType.CHEMICAL, TransmissionType.ENERGY)
          .withComputerSupport("neutronActivator")
          .replace(Attributes.ACTIVE)
          .build();


    public static final Machine<TileEntityEnvironmentalRadiationGenerator> ENVIRONMENTAL_RADIATION_GENERATOR = Machine.MachineBuilder
          .createMachine(() -> MEXGenTileEntityTypes.ENVIRONMENTAL_RADIATION_GENERATOR, MEXLang.DESCRIPTION_ENVIRONMENTAL_RADIATION_GENERATOR)
          .withGui(() -> MEXContainerTypes.ENVIRONMENTAL_RADIATION_GENERATOR)
          .withEnergyConfig(TileEntityEnvironmentalRadiationGenerator::getEnergyUsage, TileEntityEnvironmentalRadiationGenerator::getEnergyStorage)
          .replace(GENERATOR_UPGRADES)
          .withCustomShape(ENVIRONMENTAL_RADIATION_GENERATOR_SHAPE)
          .with(AttributeCustomSelectionBox.JSON)
          .withSideConfig(TransmissionType.ENERGY)
          .withSound(MEXSounds.ENVIRONMENTAL_RADIATION_GENERATOR)
          .withComputerSupport("environmentalRadiationGenerator")
          .build();

    public static final Machine<TileEntityPotionNebulizer> POTION_NEBULIZER = Machine.MachineBuilder
          .createMachine(() -> MEXGenTileEntityTypes.POTION_NEBULIZER, MEXLang.DESCRIPTION_POTION_NEBULIZER)
          .withGui(() -> MEXContainerTypes.POTION_NEBULIZER)
          .withEnergyConfig(() -> 2_000L, () -> 100_000L)
          .without(AttributeParticleFX.class)
          .with(AttributeUpgradeSupport.create(Upgrade.ENERGY, Upgrade.CHEMICAL))
          .withCustomShape(POTION_NEBULIZER_SHAPE)
          .with(AttributeCustomSelectionBox.JSON)
          .withSideConfig(TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.CHEMICAL, TransmissionType.ENERGY)
          .withComputerSupport("potionNebulizer")
          .replace(Attributes.ACTIVE)
          .build();

    public static final BlockTypeTile<TileEntityAntimatterSuperchargedCoil> ANTIMATTER_SUPERCHARGED_COIL = BlockTileBuilder
          .createBlock(() -> MEXGenTileEntityTypes.ANTIMATTER_SUPERCHARGED_COIL, MEXLang.DESCRIPTION_ANTIMATTER_SUPERCHARGED_COIL)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE))
          .withCustomShape(mekanism.common.content.blocktype.BlockShapes.SUPERCHARGED_COIL)
          .internalMultiblock()
          .build();
}
