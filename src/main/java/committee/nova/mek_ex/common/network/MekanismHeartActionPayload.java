package committee.nova.mek_ex.common.network;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record MekanismHeartActionPayload(BlockPos pos, int action, int value, String machineId) implements CustomPacketPayload {
    public static final int SET_RANGE = 0;
    public static final int TOGGLE_MACHINE = 1;
    public static final Type<MekanismHeartActionPayload> TYPE = new Type<>(MekEXMod.rl("mekanism_heart_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MekanismHeartActionPayload> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, MekanismHeartActionPayload::pos,
          ByteBufCodecs.VAR_INT, MekanismHeartActionPayload::action,
          ByteBufCodecs.VAR_INT, MekanismHeartActionPayload::value,
          ByteBufCodecs.STRING_UTF8, MekanismHeartActionPayload::machineId,
          MekanismHeartActionPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.level().isLoaded(payload.pos())) return;
            BlockEntity be = player.level().getBlockEntity(payload.pos());
            if (!(be instanceof TileEntityMekanismHeart tile)
                  || player.distanceToSqr(payload.pos().getX() + .5, payload.pos().getY() + .5, payload.pos().getZ() + .5) > 4096) return;
            var data = tile.getMultiblock();
            if (payload.action() == SET_RANGE) data.setTransferRange(payload.value());
            else if (payload.action() == TOGGLE_MACHINE) {
                var id = net.minecraft.resources.ResourceLocation.tryParse(payload.machineId());
                if (id != null) data.toggleExcludedMachine(id);
            }
        }));
    }
}
