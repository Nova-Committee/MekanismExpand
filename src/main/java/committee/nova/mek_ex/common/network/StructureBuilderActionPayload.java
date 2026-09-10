package committee.nova.mek_ex.common.network;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.block.entity.TileEntityStructureBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record StructureBuilderActionPayload(BlockPos pos, int action, int value) implements CustomPacketPayload {

    public static final int ACTION_TOGGLE_RUNNING = 0;
    public static final int ACTION_SET_SIZE_X = 1;
    public static final int ACTION_SET_SIZE_Y = 2;
    public static final int ACTION_SET_SIZE_Z = 3;
    public static final int ACTION_ROTATE = 4;
    public static final int ACTION_RESET_PROGRESS = 5;
    public static final int ACTION_CYCLE_RECIPE = 6;
    public static final int ACTION_SET_OPTIONAL_COUNT = 7;

    public static final Type<StructureBuilderActionPayload> TYPE = new Type<>(MekEXMod.rl("structure_builder_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StructureBuilderActionPayload> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, StructureBuilderActionPayload::pos,
          ByteBufCodecs.VAR_INT, StructureBuilderActionPayload::action,
          ByteBufCodecs.VAR_INT, StructureBuilderActionPayload::value,
          StructureBuilderActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!player.level().isLoaded(payload.pos())) {
                return;
            }
            BlockEntity be = player.level().getBlockEntity(payload.pos());
            if (!(be instanceof TileEntityStructureBuilder builder)) {
                return;
            }
            if (!builder.isOwnerOrTrusted(player)) {
                return;
            }
            if (player.distanceToSqr(payload.pos().getX() + 0.5, payload.pos().getY() + 0.5, payload.pos().getZ() + 0.5) > 64 * 64) {
                return;
            }
            builder.handleGuiAction(payload.action(), payload.value());
        }));
    }
}
