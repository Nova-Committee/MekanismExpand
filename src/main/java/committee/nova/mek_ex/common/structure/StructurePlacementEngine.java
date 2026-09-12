package committee.nova.mek_ex.common.structure;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class StructurePlacementEngine {

    public enum PlaceResult {
        PLACED,
        SKIPPED,
        MISSING_MATERIAL,
        BLOCKED,
        DONE
    }

    private StructurePlacementEngine() {
    }





    public static BlockPos localToWorld(BlockPos machinePos, Direction facing, int localX, int localY, int localZ) {
        Direction forward = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        Direction right = forward.getClockWise();
        return machinePos
              .relative(forward, localZ + 1)
              .relative(right, localX)
              .relative(Direction.UP, localY);
    }

    public static AABB previewBounds(BlockPos machinePos, Direction facing, StructurePlan oriented) {
        int sx = oriented.sizeX();
        int sy = oriented.sizeY();
        int sz = oriented.sizeZ();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (int x : new int[]{0, Math.max(0, sx - 1)}) {
            for (int y : new int[]{0, Math.max(0, sy - 1)}) {
                for (int z : new int[]{0, Math.max(0, sz - 1)}) {
                    BlockPos p = localToWorld(machinePos, facing, x, y, z);
                    minX = Math.min(minX, p.getX());
                    minY = Math.min(minY, p.getY());
                    minZ = Math.min(minZ, p.getZ());
                    maxX = Math.max(maxX, p.getX());
                    maxY = Math.max(maxY, p.getY());
                    maxZ = Math.max(maxZ, p.getZ());
                }
            }
        }
        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    public static PlaceResult placeNext(
          ServerLevel level,
          StructurePlan plan,
          BlockPos machinePos,
          Direction facing,
          int rotationTurns,
          int index,
          List<IInventorySlot> materialSlots,
          boolean replaceReplaceable
    ) {
        int turns = Math.floorMod(rotationTurns + facingToTurns(facing), 4);
        StructurePlan oriented = plan.rotateY(turns);
        if (index < 0 || index >= oriented.blockCount()) {
            return PlaceResult.DONE;
        }
        StructureBlockEntry entry = oriented.blocks().get(index);
        BlockPos target = localToWorld(machinePos, facing, entry.x(), entry.y(), entry.z());
        if (target.equals(machinePos)) {
            return PlaceResult.SKIPPED;
        }
        BlockState desired = entry.state();
        BlockState existing = level.getBlockState(target);

        if (!existing.isAir() && !existing.equals(desired)) {
            if (!(replaceReplaceable && existing.canBeReplaced())) {
                return PlaceResult.BLOCKED;
            }
        }

        Item required = desired.getBlock().asItem();
        if (required == null || required == Items.AIR) {
            return PlaceResult.SKIPPED;
        }

        boolean needPlace = !existing.equals(desired);
        int auxNeeded = auxStillNeeded(level, target, entry, existing.equals(desired));
        if (!needPlace && auxNeeded <= 0) {
            return PlaceResult.SKIPPED;
        }

        if (needPlace && !hasMaterial(materialSlots, required)) {
            return PlaceResult.MISSING_MATERIAL;
        }
        if (auxNeeded > 0 && entry.auxItem() != null && entry.auxItem() != Items.AIR
              && countMaterial(materialSlots, entry.auxItem()) < auxNeeded) {
            return PlaceResult.MISSING_MATERIAL;
        }

        if (needPlace) {
            if (!consumeMaterial(materialSlots, required)) {
                return PlaceResult.MISSING_MATERIAL;
            }
            level.setBlock(target, desired, Block.UPDATE_ALL);
        }

        if (auxNeeded > 0 && entry.auxItem() != null && entry.auxItem() != Items.AIR) {
            for (int i = 0; i < auxNeeded; i++) {
                if (!consumeMaterial(materialSlots, entry.auxItem())) {
                    return PlaceResult.MISSING_MATERIAL;
                }
            }
            applyAux(level, target, entry);
        }
        return PlaceResult.PLACED;
    }

    private static int auxStillNeeded(ServerLevel level, BlockPos target, StructureBlockEntry entry, boolean blockMatches) {
        if (entry.auxCount() <= 0 || entry.auxItem() == null || entry.auxItem() == Items.AIR) {
            return 0;
        }
        if (blockMatches) {
            BlockEntity be = level.getBlockEntity(target);
            if (be instanceof TileEntityTurbineRotor rotor) {
                return Math.max(0, Math.min(2, entry.auxCount()) - rotor.getHousedBlades());
            }
        }
        return entry.auxCount();
    }

    private static void applyAux(ServerLevel level, BlockPos target, StructureBlockEntry entry) {
        BlockEntity be = level.getBlockEntity(target);
        if (be instanceof TileEntityTurbineRotor rotor) {
            int want = Math.min(2, entry.auxCount());
            while (rotor.getHousedBlades() < want) {
                if (!rotor.addBlade(false)) {
                    break;
                }
            }
        }
    }

    public static boolean consumeMaterial(List<IInventorySlot> slots, Item required) {
        for (IInventorySlot slot : slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) {
                continue;
            }
            if (!matchesRequired(stack, required)) {
                continue;
            }
            ItemStack simulated = slot.extractItem(1, Action.SIMULATE, AutomationType.INTERNAL);
            if (simulated.getCount() < 1) {
                continue;
            }
            slot.extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);
            return true;
        }
        return false;
    }

    public static int countMaterial(List<IInventorySlot> slots, Item required) {
        int total = 0;
        for (IInventorySlot slot : slots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && matchesRequired(stack, required)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static boolean matchesRequired(ItemStack stack, Item required) {
        if (stack.is(required)) {
            return true;
        }
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock().asItem() == required;
    }

    public static boolean hasMaterial(List<IInventorySlot> slots, Item required) {
        return countMaterial(slots, required) > 0;
    }




    public static int facingToTurns(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0;
            case WEST -> 1;
            case NORTH -> 2;
            case EAST -> 3;
            default -> 0;
        };
    }
}
