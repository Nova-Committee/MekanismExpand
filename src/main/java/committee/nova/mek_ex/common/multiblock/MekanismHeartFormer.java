package committee.nova.mek_ex.common.multiblock;

import committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart;
import committee.nova.mek_ex.common.multiblock.MekanismHeartTemplate.HeartPart;
import committee.nova.mek_ex.init.registry.MEXBlockTypes;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class MekanismHeartFormer {

    private MekanismHeartFormer() {
    }

    public static void tick(TileEntityMekanismHeart tile, MekanismHeartMultiblockData multiblock) {
        Level level = tile.getLevel();
        if (!(level instanceof ServerLevel) || level.isClientSide()) {
            return;
        }
        BlockPos pos = tile.getBlockPos();
        BlockPos min = MekanismHeartTemplate.minCornerFromController(pos);
        boolean complete = matchesTemplate(level, min);

        if (multiblock.isFormed()) {
            if (tile.isMaster() && !complete) {
                tile.getStructure().invalidate(level);
            }
            return;
        }

        if (!complete) {
            return;
        }
        if (!isAntimatter(level.getBlockState(pos))) {
            return;
        }
        if (!pos.equals(min.offset(MekanismHeartTemplate.CONTROLLER_OFFSET))) {
            return;
        }
        form(tile, min);
    }

    public static boolean matchesTemplate(Level level, BlockPos min) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = 0; y < MekanismHeartTemplate.SIZE_Y; y++) {
            for (int z = 0; z < MekanismHeartTemplate.SIZE_Z; z++) {
                for (int x = 0; x < MekanismHeartTemplate.SIZE_X; x++) {
                    HeartPart expected = MekanismHeartTemplate.getPart(x, y, z);
                    cursor.setWithOffset(min, x, y, z);
                    if (!matches(expected, level.getBlockState(cursor))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void form(TileEntityMekanismHeart tile, BlockPos min) {
        MultiblockManager<MekanismHeartMultiblockData> manager = MekanismHeartManager.MANAGER;
        Structure structure = tile.resetStructure(manager);

        MekanismHeartMultiblockData data = tile.createMultiblock();
        VoxelCuboid cuboid = new VoxelCuboid(
              min,
              min.offset(MekanismHeartTemplate.SIZE_X - 1, MekanismHeartTemplate.SIZE_Y - 1, MekanismHeartTemplate.SIZE_Z - 1)
        );
        if (!data.setShape(cuboid)) {
            return;
        }

        data.locations = new ObjectOpenHashSet<>();
        data.locations.add(tile.getBlockPos().immutable());
        data.internalLocations = new ObjectOpenHashSet<>();
        data.valves = new ObjectOpenHashSet<>();

        int cells = 0;
        int providers = 0;
        for (int y = 0; y < MekanismHeartTemplate.SIZE_Y; y++) {
            for (int z = 0; z < MekanismHeartTemplate.SIZE_Z; z++) {
                for (int x = 0; x < MekanismHeartTemplate.SIZE_X; x++) {
                    HeartPart part = MekanismHeartTemplate.getPart(x, y, z);
                    if (part == HeartPart.CELL) {
                        cells++;
                    } else if (part == HeartPart.PROVIDER) {
                        providers++;
                    }
                }
            }
        }
        data.setHeartStats(MekanismHeartTemplate.structureCenter(min), cells, providers);

        Level level = tile.getLevel();
        MultiblockCache<MekanismHeartMultiblockData> cache = manager.createCache();
        cache.apply(level.registryAccess(), data);
        data.inventoryID = manager.getUniqueInventoryID();
        data.setFormedForce(true);
        data.onCreated(level);
        cache.sync(data);
        manager.trackCache(data.inventoryID, cache);

        tile.setMultiblockData(manager, data);
        structure.setMultiblockData(data);
    }

    private static boolean matches(HeartPart expected, BlockState state) {
        return switch (expected) {
            case AIR -> state.isAir();
            case CONTROLLER, CASING -> isAntimatter(state);
            case GLASS -> isStructuralGlass(state);
            case CELL -> isUltimateCell(state);
            case PROVIDER -> isUltimateProvider(state);
        };
    }

    private static boolean isAntimatter(BlockState state) {
        return BlockType.is(state.getBlock(), MEXBlockTypes.BLOCK_ANTIMATTER)
              || state.is(MEXBlocks.block_antimatter.get());
    }

    private static boolean isStructuralGlass(BlockState state) {
        Block block = state.getBlock();
        return BlockType.is(block, MekanismBlockTypes.STRUCTURAL_GLASS)
              || state.is(MekanismBlocks.STRUCTURAL_GLASS.get());
    }

    private static boolean isUltimateCell(BlockState state) {
        Block block = state.getBlock();
        return BlockType.is(block, MekanismBlockTypes.ULTIMATE_INDUCTION_CELL)
              || state.is(MekanismBlocks.ULTIMATE_INDUCTION_CELL.get());
    }

    private static boolean isUltimateProvider(BlockState state) {
        Block block = state.getBlock();
        return BlockType.is(block, MekanismBlockTypes.ULTIMATE_INDUCTION_PROVIDER)
              || state.is(MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER.get());
    }
}
