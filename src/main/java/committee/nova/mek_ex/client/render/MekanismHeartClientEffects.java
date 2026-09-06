package committee.nova.mek_ex.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart;
import committee.nova.mek_ex.common.multiblock.MekanismHeartMultiblockData;
import committee.nova.mek_ex.common.multiblock.MekanismHeartTemplate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class MekanismHeartClientEffects {

    private static final Map<BlockPos, HeartFx> ACTIVE = new ConcurrentHashMap<>();
    private static final int SOFT_SEGMENTS = 28;
    private static final int AXIS_SLICES = 40;
    private static final int CORE_LAYERS = 6;
    private static final int RING_COUNT = 3;

    private MekanismHeartClientEffects() {
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        ACTIVE.clear();
        BlockPos playerPos = minecraft.player.blockPosition();
        int chunkRadius = 8;
        int minCx = (playerPos.getX() >> 4) - chunkRadius;
        int maxCx = (playerPos.getX() >> 4) + chunkRadius;
        int minCz = (playerPos.getZ() >> 4) - chunkRadius;
        int maxCz = (playerPos.getZ() >> 4) + chunkRadius;
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                if (!minecraft.level.hasChunk(cx, cz)) {
                    continue;
                }
                var chunk = minecraft.level.getChunk(cx, cz);
                for (var entry : chunk.getBlockEntities().entrySet()) {
                    if (!(entry.getValue() instanceof TileEntityMekanismHeart heart)) {
                        continue;
                    }
                    MekanismHeartMultiblockData data = heart.getMultiblock();
                    if (!heart.isMaster() || !data.isFormed() || data.getBounds() == null) {
                        continue;
                    }
                    VoxelCuboid bounds = data.getBounds();
                    Vec3 center = Vec3.atLowerCornerOf(bounds.getMinPos())
                          .add(Vec3.atLowerCornerOf(bounds.getMaxPos()))
                          .add(1.0D, 1.0D, 1.0D)
                          .scale(0.5D);
                    ACTIVE.put(heart.getBlockPos().immutable(), new HeartFx(center, bounds.getMinPos().immutable(), bounds.getMaxPos().immutable()));
                }
            }
        }
        if (ACTIVE.isEmpty()) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer glow = buffers.getBuffer(RenderType.lightning());
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        Quaternionf camRot = new Quaternionf(camera.rotation());
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float time = (minecraft.level.getGameTime() + partialTick) * 0.045F;

        pose.pushPose();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);

        for (HeartFx fx : ACTIVE.values()) {
            renderHeart(pose, glow, camRot, fx.center(), time);
        }

        pose.popPose();
        buffers.endBatch(RenderType.lightning());
    }

    private static void renderHeart(PoseStack matrix, VertexConsumer glow, Quaternionf camRot, Vec3 center, float time) {
        float pulse = 0.94F + 0.06F * Mth.sin(time * 2.2F);
        float heartbeat = 0.5F + 0.5F * Mth.sin(time * 3.0F);
        float halfHeight = MekanismHeartTemplate.SIZE_Y * 0.34F;
        float radius = 1.45F * pulse;

        matrix.pushPose();
        matrix.translate(center.x, center.y, center.z);

        renderBillboardCapsule(matrix, glow, camRot, radius, halfHeight, time, 170, 70, 255, 40);
        renderBillboardCapsule(matrix, glow, camRot, radius * 0.62F, halfHeight * 0.78F, time * 1.1F, 230, 140, 255, 60);
        renderBillboardCapsule(matrix, glow, camRot, radius * 0.28F, halfHeight * 0.45F, time * 1.25F, 255, 230, 255, 95);

        for (int i = 0; i < CORE_LAYERS; i++) {
            float t = i / (float) (CORE_LAYERS - 1);
            float coreR = (0.70F - t * 0.48F) * pulse;
            int alpha = 55 + (int) (100 * (1.0F - t));
            matrix.pushPose();
            matrix.mulPose(camRot);
            renderSoftDisc(matrix, glow, coreR, 255, 210 - i * 18, 255, alpha);
            matrix.popPose();
        }

        for (int i = 0; i < RING_COUNT; i++) {
            float ringY = (i - 1) * (halfHeight * 0.30F);
            float ringR = radius * (0.78F + i * 0.08F);
            float spin = time * (0.55F + i * 0.25F) + i;
            matrix.pushPose();
            matrix.translate(0.0D, ringY, 0.0D);
            matrix.mulPose(Axis.YP.rotation(spin));
            matrix.mulPose(Axis.XP.rotationDegrees(12F + i * 8F));
            renderSoftRing(matrix, glow, ringR, 0.10F, 255, 150 + i * 25, 255, 110 + (int) (40 * heartbeat));
            matrix.popPose();
        }

        matrix.popPose();
    }

    private static void renderBillboardCapsule(PoseStack matrix, VertexConsumer buffer, Quaternionf camRot,
          float radius, float halfHeight, float time, int r, int g, int b, int baseAlpha) {
        for (int i = 0; i < AXIS_SLICES; i++) {
            float t = i / (float) (AXIS_SLICES - 1);
            float y = -halfHeight + 2F * halfHeight * t;
            float localR = capsuleRadius(radius, halfHeight, y);
            if (localR <= 0.02F) {
                continue;
            }
            float breathe = 1.0F + 0.03F * Mth.sin(time * 2.0F + t * 7.0F);
            float profile = (float) Math.sin(Math.PI * t);
            int alpha = Math.max(8, (int) (baseAlpha * (0.40F + 0.60F * profile)));
            matrix.pushPose();
            matrix.translate(0.0D, y, 0.0D);
            matrix.mulPose(camRot);
            renderSoftDisc(matrix, buffer, localR * breathe, r, g, b, alpha);
            matrix.popPose();
        }
    }

    private static void renderSoftDisc(PoseStack matrix, VertexConsumer buffer, float radius,
          int r, int g, int b, int alpha) {
        PoseStack.Pose pose = matrix.last();
        Matrix4f mat = pose.pose();
        for (int ring = 0; ring < 2; ring++) {
            float inner = ring == 0 ? 0F : radius * 0.42F;
            float outer = ring == 0 ? radius * 0.42F : radius;
            int aInner = ring == 0 ? alpha : (int) (alpha * 0.50F);
            int aOuter = ring == 0 ? (int) (alpha * 0.50F) : 0;
            for (int seg = 0; seg < SOFT_SEGMENTS; seg++) {
                float a0 = (float) (Math.PI * 2 * seg / SOFT_SEGMENTS);
                float a1 = (float) (Math.PI * 2 * (seg + 1) / SOFT_SEGMENTS);
                float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
                float c1 = Mth.cos(a1), s1 = Mth.sin(a1);
                put(buffer, mat, pose, c0 * inner, s0 * inner, aInner, r, g, b);
                put(buffer, mat, pose, c0 * outer, s0 * outer, aOuter, r, g, b);
                put(buffer, mat, pose, c1 * outer, s1 * outer, aOuter, r, g, b);
                put(buffer, mat, pose, c0 * inner, s0 * inner, aInner, r, g, b);
                put(buffer, mat, pose, c1 * outer, s1 * outer, aOuter, r, g, b);
                put(buffer, mat, pose, c1 * inner, s1 * inner, aInner, r, g, b);
            }
        }
    }

    private static void renderSoftRing(PoseStack matrix, VertexConsumer buffer, float radius, float thickness,
          int r, int g, int b, int alpha) {
        PoseStack.Pose pose = matrix.last();
        Matrix4f mat = pose.pose();
        float inner = Math.max(0.05F, radius - thickness);
        float mid = radius;
        float outer = radius + thickness;
        for (int seg = 0; seg < SOFT_SEGMENTS; seg++) {
            float a0 = (float) (Math.PI * 2 * seg / SOFT_SEGMENTS);
            float a1 = (float) (Math.PI * 2 * (seg + 1) / SOFT_SEGMENTS);
            float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1), s1 = Mth.sin(a1);
            put3(buffer, mat, pose, c0 * inner, 0F, s0 * inner, 0, r, g, b);
            put3(buffer, mat, pose, c0 * mid, 0F, s0 * mid, alpha, r, g, b);
            put3(buffer, mat, pose, c1 * mid, 0F, s1 * mid, alpha, r, g, b);
            put3(buffer, mat, pose, c0 * inner, 0F, s0 * inner, 0, r, g, b);
            put3(buffer, mat, pose, c1 * mid, 0F, s1 * mid, alpha, r, g, b);
            put3(buffer, mat, pose, c1 * inner, 0F, s1 * inner, 0, r, g, b);
            put3(buffer, mat, pose, c0 * mid, 0F, s0 * mid, alpha, r, g, b);
            put3(buffer, mat, pose, c0 * outer, 0F, s0 * outer, 0, r, g, b);
            put3(buffer, mat, pose, c1 * outer, 0F, s1 * outer, 0, r, g, b);
            put3(buffer, mat, pose, c0 * mid, 0F, s0 * mid, alpha, r, g, b);
            put3(buffer, mat, pose, c1 * outer, 0F, s1 * outer, 0, r, g, b);
            put3(buffer, mat, pose, c1 * mid, 0F, s1 * mid, alpha, r, g, b);
        }
    }

    private static float capsuleRadius(float radius, float halfHeight, float y) {
        float edge = Math.max(0.05F, halfHeight - radius);
        if (y > edge) {
            float dy = y - edge;
            return (float) Math.sqrt(Math.max(0F, radius * radius - dy * dy));
        }
        if (y < -edge) {
            float dy = -edge - y;
            return (float) Math.sqrt(Math.max(0F, radius * radius - dy * dy));
        }
        return radius;
    }

    private static void put(VertexConsumer buffer, Matrix4f mat, PoseStack.Pose pose,
          float x, float y, int a, int r, int g, int b) {
        buffer.addVertex(mat, x, y, 0F)
              .setColor(r, g, b, a)
              .setUv(0F, 0F)
              .setOverlay(0)
              .setLight(0xF000F0)
              .setNormal(pose, 0F, 0F, 1F);
    }

    private static void put3(VertexConsumer buffer, Matrix4f mat, PoseStack.Pose pose,
          float x, float y, float z, int a, int r, int g, int b) {
        buffer.addVertex(mat, x, y, z)
              .setColor(r, g, b, a)
              .setUv(0F, 0F)
              .setOverlay(0)
              .setLight(0xF000F0)
              .setNormal(pose, 0F, 1F, 0F);
    }

    private record HeartFx(Vec3 center, BlockPos min, BlockPos max) {
    }
}
