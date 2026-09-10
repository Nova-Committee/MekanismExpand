package committee.nova.mek_ex.common.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public record StructurePlan(int sizeX, int sizeY, int sizeZ, List<StructureBlockEntry> blocks) {

    public static final int MAX_SIZE = 32;
    public static final int MAX_BLOCKS = 8192;

    public static final Codec<StructurePlan> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.intRange(1, MAX_SIZE).fieldOf("size_x").forGetter(StructurePlan::sizeX),
          Codec.intRange(1, MAX_SIZE).fieldOf("size_y").forGetter(StructurePlan::sizeY),
          Codec.intRange(1, MAX_SIZE).fieldOf("size_z").forGetter(StructurePlan::sizeZ),
          StructureBlockEntry.CODEC.listOf().fieldOf("blocks").forGetter(StructurePlan::blocks)
    ).apply(instance, StructurePlan::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StructurePlan> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, StructurePlan::sizeX,
          ByteBufCodecs.VAR_INT, StructurePlan::sizeY,
          ByteBufCodecs.VAR_INT, StructurePlan::sizeZ,
          StructureBlockEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), StructurePlan::blocks,
          StructurePlan::new
    );

    public StructurePlan {
        blocks = List.copyOf(blocks);
        if (sizeX < 1 || sizeY < 1 || sizeZ < 1 || sizeX > MAX_SIZE || sizeY > MAX_SIZE || sizeZ > MAX_SIZE) {
            throw new IllegalArgumentException("Invalid structure plan size: " + sizeX + "x" + sizeY + "x" + sizeZ);
        }
        if (blocks.size() > MAX_BLOCKS) {
            throw new IllegalArgumentException("Structure plan exceeds max blocks: " + blocks.size());
        }
    }

    public static StructurePlan hollowCuboid(int sizeX, int sizeY, int sizeZ, BlockState frame, BlockState wall) {
        return StructureAssemblers.hollowCuboid(sizeX, sizeY, sizeZ, frame, wall, wall, List.of());
    }

    public static StructurePlan hollowCuboid(
          int sizeX, int sizeY, int sizeZ,
          BlockState frame, BlockState wall,
          List<MultiblockBuildRecipe.PortSpec> ports
    ) {
        return StructureAssemblers.hollowCuboid(sizeX, sizeY, sizeZ, frame, wall, wall, ports);
    }

    public int blockCount() {
        return blocks.size();
    }

    public Object2IntMap<Item> materialCounts() {
        return StructureAssemblers.materialCounts(this);
    }

    public StructurePlan rotateY(int turns) {
        int t = Math.floorMod(turns, 4);
        if (t == 0) {
            return this;
        }
        List<StructureBlockEntry> rotated = new ArrayList<>(blocks.size());
        int newSizeX = sizeX;
        int newSizeZ = sizeZ;
        for (int i = 0; i < t; i++) {
            int tmp = newSizeX;
            newSizeX = newSizeZ;
            newSizeZ = tmp;
        }
        for (StructureBlockEntry entry : blocks) {
            int rx = entry.x();
            int rz = entry.z();
            int curSX = sizeX;
            int curSZ = sizeZ;
            for (int i = 0; i < t; i++) {
                int nx = curSZ - 1 - rz;
                int nz = rx;
                rx = nx;
                rz = nz;
                int tmp = curSX;
                curSX = curSZ;
                curSZ = tmp;
            }
            rotated.add(new StructureBlockEntry(rx, entry.y(), rz, rotateStateY(entry.state(), t), entry.auxCount(), entry.auxItem()));
        }
        return new StructurePlan(newSizeX, sizeY, newSizeZ, rotated);
    }

    private static BlockState rotateStateY(BlockState state, int turns) {
        int t = Math.floorMod(turns, 4);
        if (t == 0) {
            return state;
        }
        BlockState result = state;
        for (int i = 0; i < t; i++) {
            result = rotateStateYOnce(result);
        }
        return result;
    }

    private static BlockState rotateStateYOnce(BlockState state) {
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, dir.getClockWise());
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            if (dir.getAxis() != Direction.Axis.Y) {
                return state.setValue(BlockStateProperties.FACING, dir.getClockWise());
            }
        }
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
            if (axis == Direction.Axis.X) {
                return state.setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
            }
            if (axis == Direction.Axis.Z) {
                return state.setValue(BlockStateProperties.AXIS, Direction.Axis.X);
            }
        }
        return state;
    }
}
