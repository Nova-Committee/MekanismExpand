package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.common.block.entity.TileEntityAdvancedWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityEliteWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityBasicWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityUltimateWindGenerator;
import committee.nova.mek_ex.common.block.entity.TileEntityNuclearControlTank;
import committee.nova.mek_ex.common.block.entity.TileEntityNuclearControlValve;
import committee.nova.mek_ex.common.block.entity.TileEntityNeutronActivator;
import committee.nova.mek_ex.common.block.entity.TileEntityAntimatterSuperchargedCoil;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MEXBlockTypes {

    private static final AttributeUpgradeSupport WIND_UPGRADES = AttributeUpgradeSupport.create(Upgrade.MUFFLING, MEXUpgrades.capacity());
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

    private MEXBlockTypes() {
    }

    public static final Generator<TileEntityBasicWindGenerator> BASIC_WIND_GENERATOR = Generator.GeneratorBuilder
          .createGenerator(() -> MEXGenTileEntityTypes.BASIC_WIND_GENERATOR, GeneratorsLang.DESCRIPTION_WIND_GENERATOR)
          .withGui(() -> MEXContainerTypes.BASIC_WIND_GENERATOR)
          .withEnergyConfig(MEXWindTier.BASIC::getEnergyStorage)
          .withCustomShape(BlockShapes.WIND_GENERATOR)
          .with(AttributeCustomSelectionBox.JAVA, WIND_UPGRADES, new mekanism.common.block.attribute.AttributeTier<>(MEXWindTier.BASIC))
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
          .with(AttributeCustomSelectionBox.JAVA, WIND_UPGRADES, new mekanism.common.block.attribute.AttributeTier<>(MEXWindTier.ADVANCED))
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
          .with(AttributeCustomSelectionBox.JAVA, WIND_UPGRADES, new mekanism.common.block.attribute.AttributeTier<>(MEXWindTier.ELITE))
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
          .with(AttributeCustomSelectionBox.JAVA, WIND_UPGRADES, new mekanism.common.block.attribute.AttributeTier<>(MEXWindTier.ULTIMATE))
          .withSound(mekanism.generators.common.registries.GeneratorsSounds.WIND_GENERATOR)
          .withBounding(WIND_BOUNDING)
          .withComputerSupport("ultimateWindGenerator")
          .build();

    /** Framework casing for the nuclear control tank multiblock. */
    public static final BlockTypeTile<TileEntityNuclearControlTank> NUCLEAR_CONTROL_TANK = BlockTileBuilder
          .createBlock(() -> MEXGenTileEntityTypes.NUCLEAR_CONTROL_TANK, MEXLang.DESCRIPTION_NUCLEAR_CONTROL_TANK)
          .withGui(() -> MEXContainerTypes.NUCLEAR_CONTROL_TANK, MEXLang.NUCLEAR_CONTROL_TANK)
          .with(Attributes.INVENTORY)
          .externalMultiblock()
          .build();

    /** Valve casing that exposes fluid and chemical capabilities for the tank. */
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

    public static final BlockTypeTile<TileEntityAntimatterSuperchargedCoil> ANTIMATTER_SUPERCHARGED_COIL = BlockTileBuilder
          .createBlock(() -> MEXGenTileEntityTypes.ANTIMATTER_SUPERCHARGED_COIL, MEXLang.DESCRIPTION_ANTIMATTER_SUPERCHARGED_COIL)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE))
          .withCustomShape(mekanism.common.content.blocktype.BlockShapes.SUPERCHARGED_COIL)
          .internalMultiblock()
          .build();
}
