package committee.nova.mek_ex.common.network;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record ElectricSkateboardGearPayload(int entityId, int gear) implements CustomPacketPayload {
    public static final Type<ElectricSkateboardGearPayload> TYPE = new Type<>(MekEXMod.rl("electric_skateboard_gear"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ElectricSkateboardGearPayload> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, ElectricSkateboardGearPayload::entityId,
          ByteBufCodecs.VAR_INT, ElectricSkateboardGearPayload::gear,
          ElectricSkateboardGearPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                throw new IllegalStateException("Electric skateboard gear change did not originate from a server player");
            }
            Entity entity = player.level().getEntity(payload.entityId());
            if (!(entity instanceof EntityElectricSkateboard bike) || !bike.canAccess(player)) {
                MekEXMod.LOGGER.warn("Rejected electric skateboard gear change from {}", player.getGameProfile().getName());
                return;
            }
            bike.setGear(Mth.clamp(payload.gear(), EntityElectricSkateboard.MIN_GEAR, EntityElectricSkateboard.MAX_GEAR));
        }));
    }
}
