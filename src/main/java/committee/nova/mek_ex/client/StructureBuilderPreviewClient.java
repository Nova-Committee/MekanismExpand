package committee.nova.mek_ex.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mek_ex.common.block.entity.TileEntityStructureBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class StructureBuilderPreviewClient {

    private static final float COLOR_R = 0.25F;
    private static final float COLOR_G = 0.85F;
    private static final float COLOR_B = 1.0F;
    private static final float COLOR_A = 0.85F;
    private static final double MAX_DISTANCE_SQ = 48.0 * 48.0;
    private static final int CHUNK_RADIUS = 3;

    private StructureBuilderPreviewClient() {
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lines());

        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);

        BlockPos playerPos = player.blockPosition();
        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        boolean drawn = false;

        for (int cx = centerChunkX - CHUNK_RADIUS; cx <= centerChunkX + CHUNK_RADIUS; cx++) {
            for (int cz = centerChunkZ - CHUNK_RADIUS; cz <= centerChunkZ + CHUNK_RADIUS; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(cx, cz);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof TileEntityStructureBuilder builder)) {
                        continue;
                    }
                    if (player.distanceToSqr(Vec3.atCenterOf(builder.getBlockPos())) > MAX_DISTANCE_SQ) {
                        continue;
                    }
                    AABB box = builder.getPreviewBounds();
                    if (box == null) {
                        continue;
                    }
                    LevelRenderer.renderLineBox(pose, consumer, box, COLOR_R, COLOR_G, COLOR_B, COLOR_A);
                    drawn = true;
                }
            }
        }

        pose.popPose();
        if (drawn) {
            buffers.endBatch(RenderType.lines());
        }
    }
}
