package committee.nova.mek_ex.common.network;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record ElectricSkateboardInputPayload(int entityId, boolean forward, boolean back, boolean left, boolean right, boolean jump)
      implements CustomPacketPayload {
    public static final Type<ElectricSkateboardInputPayload> TYPE = new Type<>(MekEXMod.rl("electric_skateboard_input"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ElectricSkateboardInputPayload> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, ElectricSkateboardInputPayload::entityId,
          ByteBufCodecs.BOOL, ElectricSkateboardInputPayload::forward,
          ByteBufCodecs.BOOL, ElectricSkateboardInputPayload::back,
          ByteBufCodecs.BOOL, ElectricSkateboardInputPayload::left,
          ByteBufCodecs.BOOL, ElectricSkateboardInputPayload::right,
          ByteBufCodecs.BOOL, ElectricSkateboardInputPayload::jump,
          ElectricSkateboardInputPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                throw new IllegalStateException("Electric skateboard input did not originate from a server player");
            }
            if (!(player.getVehicle() instanceof EntityElectricSkateboard bike)) {
                return;
            }
            if (bike.getId() != payload.entityId()
                  || bike.getControllingPassenger() != player) {
                MekEXMod.LOGGER.warn("Rejected electric skateboard input from {} for entity {}", player.getGameProfile().getName(), payload.entityId());
                return;
            }
            bike.setDriverInput(player, payload.forward(), payload.back(), payload.left(), payload.right(), payload.jump());
        }));
    }
}
