package committee.nova.mek_ex.common.chunk;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.multiblock.MekanismHeartMultiblockData;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.world.chunk.LoadingValidationCallback;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import org.jetbrains.annotations.Nullable;

public final class MekanismHeartChunkManager implements LoadingValidationCallback {

    public static final MekanismHeartChunkManager INSTANCE = new MekanismHeartChunkManager();
    public static final TicketController CONTROLLER = new TicketController(MekEXMod.rl("mekanism_heart"), INSTANCE);

    private static final Map<UUID, ForcedHeart> ACTIVE = new ConcurrentHashMap<>();

    private MekanismHeartChunkManager() {
    }

    public static void refresh(ServerLevel level, MekanismHeartMultiblockData data) {
        if (!data.isFormed()) {
            release(level, data.inventoryID);
            return;
        }
        UUID id = data.inventoryID;
        if (id == null) {
            return;
        }
        BlockPos owner = data.getBounds() != null ? data.getBounds().getMinPos() : data.getCenter();
        List<ChunkPos> desired = data.getForcedChunks();
        ForcedHeart existing = ACTIVE.get(id);
        LongSet desiredSet = new LongOpenHashSet();
        for (ChunkPos chunk : desired) {
            desiredSet.add(chunk.toLong());
        }
        if (existing != null && existing.chunks.equals(desiredSet) && existing.owner.equals(owner)) {
            return;
        }
        if (existing != null) {
            releaseTickets(level, existing);
        }
        LongSet registered = new LongOpenHashSet();
        for (ChunkPos chunk : desired) {
            if (CONTROLLER.forceChunk(level, owner, chunk.x, chunk.z, true, true)) {
                registered.add(chunk.toLong());
            }
        }
        ACTIVE.put(id, new ForcedHeart(owner.immutable(), registered));
    }

    public static void release(@Nullable ServerLevel level, @Nullable UUID id) {
        if (id == null) {
            return;
        }
        ForcedHeart existing = ACTIVE.remove(id);
        if (existing != null && level != null) {
            releaseTickets(level, existing);
        }
    }

    private static void releaseTickets(ServerLevel level, ForcedHeart heart) {
        for (long packed : heart.chunks) {
            CONTROLLER.forceChunk(level, heart.owner, ChunkPos.getX(packed), ChunkPos.getZ(packed), false, true);
        }
        heart.chunks.clear();
    }

    @Override
    public void validateTickets(ServerLevel level, TicketHelper ticketHelper) {
        ticketHelper.getBlockTickets().keySet().forEach(ticketHelper::removeAllTickets);
        ACTIVE.entrySet().removeIf(entry -> {
            releaseTickets(level, entry.getValue());
            return true;
        });
    }

    private record ForcedHeart(BlockPos owner, LongSet chunks) {
    }
}
