package committee.nova.mek_ex.common.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record StructureBlockEntry(int x, int y, int z, BlockState state, int auxCount, Item auxItem) {

    public StructureBlockEntry(int x, int y, int z, BlockState state) {
        this(x, y, z, state, 0, Items.AIR);
    }

    public static final Codec<StructureBlockEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.INT.fieldOf("x").forGetter(StructureBlockEntry::x),
          Codec.INT.fieldOf("y").forGetter(StructureBlockEntry::y),
          Codec.INT.fieldOf("z").forGetter(StructureBlockEntry::z),
          BlockState.CODEC.fieldOf("state").forGetter(StructureBlockEntry::state),
          Codec.INT.optionalFieldOf("aux_count", 0).forGetter(StructureBlockEntry::auxCount),
          BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("aux_item", Items.AIR).forGetter(StructureBlockEntry::auxItem)
    ).apply(instance, StructureBlockEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StructureBlockEntry> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, StructureBlockEntry::x,
          ByteBufCodecs.VAR_INT, StructureBlockEntry::y,
          ByteBufCodecs.VAR_INT, StructureBlockEntry::z,
          ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), StructureBlockEntry::state,
          ByteBufCodecs.VAR_INT, StructureBlockEntry::auxCount,
          ByteBufCodecs.idMapper(BuiltInRegistries.ITEM), StructureBlockEntry::auxItem,
          StructureBlockEntry::new
    );

    public BlockPos relativePos() {
        return new BlockPos(x, y, z);
    }
}
