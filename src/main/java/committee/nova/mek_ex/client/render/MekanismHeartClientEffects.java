package committee.nova.mek_ex.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import committee.nova.mek_ex.common.multiblock.MekanismHeartTemplate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class MekanismHeartClientEffects {

    private static final int DISC_SEGMENTS = 12;
    private static final int AXIS_SLICES = 20;
    private static final int CORE_LAYERS = 5;
    private static final int RING_COUNT = 5;
    private static final int ORBIT_NODES = 12;
    private static final int HEX_SEGMENTS = 6;
    private static final int SPOKE_COUNT = 8;
    private static final int RING_SEGMENTS = 24;
    private static final int TUBE_SEGMENTS = 4;
    private static final int FIELD_LAYERS = 6;

    private static final float[] DISC_COS = cosTable(DISC_SEGMENTS);
    private static final float[] DISC_SIN = sinTable(DISC_SEGMENTS);
    private static final float[] RING_COS = cosTable(RING_SEGMENTS);
    private static final float[] RING_SIN = sinTable(RING_SEGMENTS);
    private static final float[] HEX_COS = cosTableOffset(HEX_SEGMENTS, (float) (-Math.PI / 6));
    private static final float[] HEX_SIN = sinTableOffset(HEX_SEGMENTS, (float) (-Math.PI / 6));
    private static final float[] TUBE_COS = cosTable(TUBE_SEGMENTS);
    private static final float[] TUBE_SIN = sinTable(TUBE_SEGMENTS);

    private static final Vector3f SCRATCH = new Vector3f();

    private MekanismHeartClientEffects() {
    }

    public static void renderHeart(PoseStack matrix, MultiBufferSource renderer, Quaternionf camRot, float time) {
        VertexConsumer glow = renderer.getBuffer(RenderType.lightning());
        renderHeartGeometry(matrix, glow, camRot, time);
    }

    private static void renderHeartGeometry(PoseStack matrix, VertexConsumer glow, Quaternionf camRot, float time) {
        float pulse = 0.92F + 0.08F * Mth.sin(time * 2.4F);
        float heartbeat = 0.5F + 0.5F * Mth.sin(time * 3.15F);
        float scan = 0.5F + 0.5F * Mth.sin(time * 1.35F);
        float halfHeight = MekanismHeartTemplate.SIZE_Y * 0.36F;
        float radius = 1.55F * pulse;

        matrix.pushPose();

        renderFieldShell(matrix, glow, radius * 1.85F, halfHeight * 1.08F, time, 90, 40, 255, 18);
        renderFieldShell(matrix, glow, radius * 1.55F, halfHeight * 0.96F, time * 1.15F, 40, 210, 255, 14);

        renderBillboardCapsule(matrix, glow, camRot, radius * 1.05F, halfHeight, time, 120, 45, 255, 28);
        renderBillboardCapsule(matrix, glow, camRot, radius * 0.72F, halfHeight * 0.86F, time * 1.08F, 180, 90, 255, 46);
        renderBillboardCapsule(matrix, glow, camRot, radius * 0.42F, halfHeight * 0.62F, time * 1.18F, 210, 160, 255, 70);
        renderBillboardCapsule(matrix, glow, camRot, radius * 0.18F, halfHeight * 0.38F, time * 1.32F, 255, 245, 255, 110);

        PoseStack.Pose corePose = matrix.last();
        Matrix4f coreMat = corePose.pose();
        for (int i = 0; i < CORE_LAYERS; i++) {
            float t = i / (float) (CORE_LAYERS - 1);
            float coreR = (0.78F - t * 0.58F) * pulse;
            int alpha = 48 + (int) (120 * (1.0F - t));
            int r = 255;
            int g = (int) (230 - t * 90);
            int b = 255;
            if (t < 0.35F) {
                r = 180 + (int) (75 * (1.0F - t / 0.35F));
                g = 240;
                b = 255;
            }
            renderSoftDiscBillboard(coreMat, corePose, glow, camRot, 0F, coreR, r, g, b, alpha);
        }

        for (int i = 0; i < RING_COUNT; i++) {
            float ringY = (i - (RING_COUNT - 1) * 0.5F) * (halfHeight * 0.22F);
            float ringR = radius * (0.70F + i * 0.10F + 0.04F * Mth.sin(time * 2.0F + i));
            float spin = time * (0.42F + i * 0.18F) * ((i & 1) == 0 ? 1F : -1F) + i * 0.7F;
            float tilt = 10F + i * 7F + 4F * Mth.sin(time + i);
            int alpha = 90 + (int) (55 * heartbeat) - i * 8;
            matrix.pushPose();
            matrix.translate(0.0D, ringY, 0.0D);
            matrix.mulPose(Axis.YP.rotation(spin));
            matrix.mulPose(Axis.XP.rotationDegrees(tilt));
            renderTubeRing(matrix, glow, ringR, 0.065F + i * 0.010F, RING_COS, RING_SIN, true, 255, 130 + i * 18, 255, alpha);
            if ((i & 1) == 0) {
                matrix.mulPose(Axis.ZP.rotationDegrees(90F));
                renderTubeRing(matrix, glow, ringR * 0.92F, 0.055F, HEX_COS, HEX_SIN, false, 80, 230, 255, 70 + (int) (30 * scan));
            }
            matrix.popPose();
        }

        matrix.pushPose();
        matrix.mulPose(Axis.YP.rotation(time * 0.55F));
        matrix.mulPose(Axis.XP.rotationDegrees(18F + 8F * Mth.sin(time * 0.7F)));
        renderTubeRing(matrix, glow, radius * 1.28F, 0.060F, RING_COS, RING_SIN, true, 60, 220, 255, 85);
        matrix.mulPose(Axis.ZP.rotationDegrees(70F));
        renderTubeRing(matrix, glow, radius * 1.12F, 0.052F, RING_COS, RING_SIN, true, 220, 120, 255, 70);
        matrix.popPose();

        renderOrbitNodes(matrix, glow, camRot, radius * 1.35F, halfHeight * 0.55F, time, heartbeat);
        renderEnergySpokes(matrix, glow, radius * 1.15F, halfHeight * 0.75F, time, scan);
        renderScanBeams(matrix, glow, radius * 0.95F, halfHeight, time);

        matrix.popPose();
    }

    private static void renderFieldShell(PoseStack matrix, VertexConsumer buffer, float radius, float halfHeight,
          float time, int r, int g, int b, int baseAlpha) {
        for (int i = 0; i < FIELD_LAYERS; i++) {
            float t = i / (float) (FIELD_LAYERS - 1);
            float y = -halfHeight + 2F * halfHeight * t;
            float profile = (float) Math.sin(Math.PI * t);
            float localR = radius * (0.55F + 0.45F * profile);
            float wobble = 1.0F + 0.025F * Mth.sin(time * 1.8F + t * 9.0F);
            int alpha = Math.max(4, (int) (baseAlpha * (0.35F + 0.65F * profile)));
            matrix.pushPose();
            matrix.translate(0.0D, y, 0.0D);
            matrix.mulPose(Axis.YP.rotation(time * 0.25F + t * 1.2F));
            renderThickRing(matrix, buffer, localR * wobble, 0.045F, HEX_COS, HEX_SIN, r, g, b, alpha);
            matrix.popPose();
        }
    }

    private static void renderOrbitNodes(PoseStack matrix, VertexConsumer buffer, Quaternionf camRot,
          float radius, float amp, float time, float heartbeat) {
        PoseStack.Pose pose = matrix.last();
        Matrix4f mat = pose.pose();
        for (int i = 0; i < ORBIT_NODES; i++) {
            float a = time * 1.1F + (float) (Math.PI * 2 * i / ORBIT_NODES);
            float y = Mth.sin(time * 1.7F + i * 0.9F) * amp * 0.55F;
            float x = Mth.cos(a) * radius;
            float z = Mth.sin(a) * radius;
            float size = (0.10F + 0.05F * heartbeat) * (0.75F + 0.25F * Mth.sin(time * 3.0F + i));
            boolean cyan = (i & 1) == 0;
            if (cyan) {
                renderSoftDiscBillboard(mat, pose, buffer, camRot, x, y, z, size, 90, 240, 255, 160);
                renderSoftDiscBillboard(mat, pose, buffer, camRot, x, y, z, size * 0.45F, 230, 255, 255, 210);
            } else {
                renderSoftDiscBillboard(mat, pose, buffer, camRot, x, y, z, size, 240, 120, 255, 150);
                renderSoftDiscBillboard(mat, pose, buffer, camRot, x, y, z, size * 0.45F, 255, 230, 255, 200);
            }
        }
    }

    private static void renderEnergySpokes(PoseStack matrix, VertexConsumer buffer, float radius, float halfHeight,
          float time, float scan) {
        for (int i = 0; i < SPOKE_COUNT; i++) {
            float a = time * 0.65F + (float) (Math.PI * 2 * i / SPOKE_COUNT);
            float flicker = 0.55F + 0.45F * Mth.sin(time * 4.2F + i * 1.3F);
            int alpha = (int) ((40 + 50 * scan) * flicker);
            if (alpha < 12) {
                continue;
            }
            float x = Mth.cos(a) * radius;
            float z = Mth.sin(a) * radius;
            boolean cyan = (i % 3) != 0;
            int r = cyan ? 70 : 230;
            int g = cyan ? 220 : 110;
            int b = 255;
            renderBeam(matrix, buffer, 0F, 0F, 0F, x, halfHeight * 0.15F * Mth.sin(time + i), z, 0.035F, r, g, b, alpha);
            renderBeam(matrix, buffer, 0F, 0F, 0F, x * 0.72F, -halfHeight * 0.25F, z * 0.72F, 0.025F, r, g, b, alpha / 2);
        }
    }

    private static void renderScanBeams(PoseStack matrix, VertexConsumer buffer, float radius, float halfHeight, float time) {
        float y = Mth.sin(time * 1.25F) * halfHeight * 0.72F;
        matrix.pushPose();
        matrix.translate(0.0D, y, 0.0D);
        matrix.mulPose(Axis.YP.rotation(time * 1.6F));
        renderTubeRing(matrix, buffer, radius * 0.85F, 0.055F, HEX_COS, HEX_SIN, false, 120, 240, 255, 95);
        renderTubeRing(matrix, buffer, radius * 1.05F, 0.045F, RING_COS, RING_SIN, true, 255, 160, 255, 70);
        matrix.popPose();

        float y2 = Mth.sin(time * 1.25F + Mth.PI) * halfHeight * 0.72F;
        matrix.pushPose();
        matrix.translate(0.0D, y2, 0.0D);
        matrix.mulPose(Axis.YP.rotation(-time * 1.35F));
        renderTubeRing(matrix, buffer, radius * 0.70F, 0.048F, HEX_COS, HEX_SIN, false, 210, 120, 255, 80);
        matrix.popPose();
    }

    private static void renderBillboardCapsule(PoseStack matrix, VertexConsumer buffer, Quaternionf camRot,
          float radius, float halfHeight, float time, int r, int g, int b, int baseAlpha) {
        PoseStack.Pose pose = matrix.last();
        Matrix4f mat = pose.pose();
        for (int i = 0; i < AXIS_SLICES; i++) {
            float t = i / (float) (AXIS_SLICES - 1);
            float y = -halfHeight + 2F * halfHeight * t;
            float localR = capsuleRadius(radius, halfHeight, y);
            if (localR <= 0.02F) {
                continue;
            }
            float breathe = 1.0F + 0.035F * Mth.sin(time * 2.1F + t * 8.0F);
            float profile = (float) Math.sin(Math.PI * t);
            int alpha = Math.max(6, (int) (baseAlpha * (0.35F + 0.65F * profile)));
            renderSoftDiscBillboard(mat, pose, buffer, camRot, 0F, y, 0F, localR * breathe, r, g, b, alpha);
        }
    }

    private static void renderSoftDiscBillboard(Matrix4f mat, PoseStack.Pose pose, VertexConsumer buffer,
          Quaternionf camRot, float y, float radius, int r, int g, int b, int alpha) {
        renderSoftDiscBillboard(mat, pose, buffer, camRot, 0F, y, 0F, radius, r, g, b, alpha);
    }

    private static void renderSoftDiscBillboard(Matrix4f mat, PoseStack.Pose pose, VertexConsumer buffer,
          Quaternionf camRot, float ox, float oy, float oz, float radius, int r, int g, int b, int alpha) {
        for (int ring = 0; ring < 2; ring++) {
            float inner = ring == 0 ? 0F : radius * 0.45F;
            float outer = ring == 0 ? radius * 0.45F : radius;
            int aInner = ring == 0 ? alpha : (int) (alpha * 0.45F);
            int aOuter = ring == 0 ? (int) (alpha * 0.45F) : 0;
            for (int seg = 0; seg < DISC_SEGMENTS; seg++) {
                float c0 = DISC_COS[seg], s0 = DISC_SIN[seg];
                float c1 = DISC_COS[seg + 1], s1 = DISC_SIN[seg + 1];
                putBillboard(buffer, mat, pose, camRot, ox, oy, oz, c0 * inner, s0 * inner, aInner, r, g, b);
                putBillboard(buffer, mat, pose, camRot, ox, oy, oz, c0 * outer, s0 * outer, aOuter, r, g, b);
                putBillboard(buffer, mat, pose, camRot, ox, oy, oz, c1 * outer, s1 * outer, aOuter, r, g, b);
                putBillboard(buffer, mat, pose, camRot, ox, oy, oz, c0 * inner, s0 * inner, aInner, r, g, b);
                putBillboard(buffer, mat, pose, camRot, ox, oy, oz, c1 * outer, s1 * outer, aOuter, r, g, b);
                putBillboard(buffer, mat, pose, camRot, ox, oy, oz, c1 * inner, s1 * inner, aInner, r, g, b);
            }
        }
    }

    private static void renderThickRing(PoseStack matrix, VertexConsumer buffer, float radius, float halfThick,
          float[] cos, float[] sin, int r, int g, int b, int alpha) {
        PoseStack.Pose pose = matrix.last();
        Matrix4f mat = pose.pose();
        float tube = Math.max(0.02F, halfThick);
        float inner = Math.max(0.04F, radius - tube);
        float outer = radius + tube;
        int rim = Math.max(6, alpha / 3);
        int segs = cos.length - 1;
        for (int seg = 0; seg < segs; seg++) {
            float c0 = cos[seg], s0 = sin[seg];
            float c1 = cos[seg + 1], s1 = sin[seg + 1];
            float ix0 = inner * c0, iz0 = inner * s0;
            float ix1 = inner * c1, iz1 = inner * s1;
            float ox0 = outer * c0, oz0 = outer * s0;
            float ox1 = outer * c1, oz1 = outer * s1;

            put3(buffer, mat, pose, ox0, tube, oz0, alpha, r, g, b);
            put3(buffer, mat, pose, ox1, tube, oz1, alpha, r, g, b);
            put3(buffer, mat, pose, ix1, tube, iz1, rim, r, g, b);
            put3(buffer, mat, pose, ox0, tube, oz0, alpha, r, g, b);
            put3(buffer, mat, pose, ix1, tube, iz1, rim, r, g, b);
            put3(buffer, mat, pose, ix0, tube, iz0, rim, r, g, b);

            put3(buffer, mat, pose, ox0, -tube, oz0, alpha, r, g, b);
            put3(buffer, mat, pose, ix0, -tube, iz0, rim, r, g, b);
            put3(buffer, mat, pose, ix1, -tube, iz1, rim, r, g, b);
            put3(buffer, mat, pose, ox0, -tube, oz0, alpha, r, g, b);
            put3(buffer, mat, pose, ix1, -tube, iz1, rim, r, g, b);
            put3(buffer, mat, pose, ox1, -tube, oz1, alpha, r, g, b);

            put3(buffer, mat, pose, ox0, -tube, oz0, rim, r, g, b);
            put3(buffer, mat, pose, ox1, -tube, oz1, rim, r, g, b);
            put3(buffer, mat, pose, ox1, tube, oz1, alpha, r, g, b);
            put3(buffer, mat, pose, ox0, -tube, oz0, rim, r, g, b);
            put3(buffer, mat, pose, ox1, tube, oz1, alpha, r, g, b);
            put3(buffer, mat, pose, ox0, tube, oz0, alpha, r, g, b);

            put3(buffer, mat, pose, ix0, -tube, iz0, rim, r, g, b);
            put3(buffer, mat, pose, ix0, tube, iz0, alpha, r, g, b);
            put3(buffer, mat, pose, ix1, tube, iz1, alpha, r, g, b);
            put3(buffer, mat, pose, ix0, -tube, iz0, rim, r, g, b);
            put3(buffer, mat, pose, ix1, tube, iz1, alpha, r, g, b);
            put3(buffer, mat, pose, ix1, -tube, iz1, rim, r, g, b);
        }
    }

    private static void renderTubeRing(PoseStack matrix, VertexConsumer buffer, float radius, float tubeRadius,
          float[] cos, float[] sin, boolean dashed, int r, int g, int b, int alpha) {
        PoseStack.Pose pose = matrix.last();
        Matrix4f mat = pose.pose();
        float tube = Math.max(0.02F, tubeRadius);
        int coreAlpha = Math.min(255, (int) (alpha * 1.15F));
        int rimAlpha = Math.max(8, alpha / 3);
        int segs = cos.length - 1;
        for (int seg = 0; seg < segs; seg++) {
            if (dashed && (seg % 6) == 5) {
                continue;
            }
            float cos0 = cos[seg], sin0 = sin[seg];
            float cos1 = cos[seg + 1], sin1 = sin[seg + 1];
            for (int tubeSeg = 0; tubeSeg < TUBE_SEGMENTS; tubeSeg++) {
                float ct0 = TUBE_COS[tubeSeg], st0 = TUBE_SIN[tubeSeg];
                float ct1 = TUBE_COS[tubeSeg + 1], st1 = TUBE_SIN[tubeSeg + 1];
                float rr0 = radius + tube * ct0;
                float rr1 = radius + tube * ct1;
                float x00 = rr0 * cos0, y00 = tube * st0, z00 = rr0 * sin0;
                float x01 = rr1 * cos0, y01 = tube * st1, z01 = rr1 * sin0;
                float x10 = rr0 * cos1, y10 = tube * st0, z10 = rr0 * sin1;
                float x11 = rr1 * cos1, y11 = tube * st1, z11 = rr1 * sin1;
                boolean edge = Math.abs(st0) > 0.7F || Math.abs(st1) > 0.7F;
                int aA = edge ? rimAlpha : coreAlpha;
                int aB = edge ? rimAlpha : coreAlpha;
                put3(buffer, mat, pose, x00, y00, z00, aA, r, g, b);
                put3(buffer, mat, pose, x01, y01, z01, aB, r, g, b);
                put3(buffer, mat, pose, x11, y11, z11, aB, r, g, b);
                put3(buffer, mat, pose, x00, y00, z00, aA, r, g, b);
                put3(buffer, mat, pose, x11, y11, z11, aB, r, g, b);
                put3(buffer, mat, pose, x10, y10, z10, aA, r, g, b);
            }
            put3(buffer, mat, pose, radius * cos0, tube * 0.15F, radius * sin0, coreAlpha, r, g, b);
            put3(buffer, mat, pose, radius * cos0, -tube * 0.15F, radius * sin0, coreAlpha, r, g, b);
            put3(buffer, mat, pose, radius * cos1, -tube * 0.15F, radius * sin1, coreAlpha, r, g, b);
            put3(buffer, mat, pose, radius * cos0, tube * 0.15F, radius * sin0, coreAlpha, r, g, b);
            put3(buffer, mat, pose, radius * cos1, -tube * 0.15F, radius * sin1, coreAlpha, r, g, b);
            put3(buffer, mat, pose, radius * cos1, tube * 0.15F, radius * sin1, coreAlpha, r, g, b);
        }
    }

    private static void renderBeam(PoseStack matrix, VertexConsumer buffer,
          float x0, float y0, float z0, float x1, float y1, float z1,
          float thickness, int r, int g, int b, int alpha) {
        PoseStack.Pose pose = matrix.last();
        Matrix4f mat = pose.pose();
        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-4F) {
            return;
        }
        float inv = 1.0F / len;
        dx *= inv;
        dy *= inv;
        dz *= inv;
        float px = -dz;
        float pz = dx;
        float pLen = Mth.sqrt(px * px + pz * pz);
        if (pLen < 1.0E-4F) {
            px = 1F;
            pz = 0F;
        } else {
            px /= pLen;
            pz /= pLen;
        }
        float hx = px * thickness;
        float hz = pz * thickness;
        put3(buffer, mat, pose, x0 - hx, y0, z0 - hz, 0, r, g, b);
        put3(buffer, mat, pose, x0 + hx, y0, z0 + hz, alpha, r, g, b);
        put3(buffer, mat, pose, x1 + hx, y1, z1 + hz, alpha / 3, r, g, b);
        put3(buffer, mat, pose, x0 - hx, y0, z0 - hz, 0, r, g, b);
        put3(buffer, mat, pose, x1 + hx, y1, z1 + hz, alpha / 3, r, g, b);
        put3(buffer, mat, pose, x1 - hx, y1, z1 - hz, 0, r, g, b);
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

    private static void putBillboard(VertexConsumer buffer, Matrix4f mat, PoseStack.Pose pose, Quaternionf camRot,
          float ox, float oy, float oz, float lx, float ly, int a, int r, int g, int b) {
        SCRATCH.set(lx, ly, 0F);
        camRot.transform(SCRATCH);
        put3(buffer, mat, pose, ox + SCRATCH.x, oy + SCRATCH.y, oz + SCRATCH.z, a, r, g, b);
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

    private static float[] cosTable(int segments) {
        float[] values = new float[segments + 1];
        for (int i = 0; i <= segments; i++) {
            values[i] = Mth.cos((float) (Math.PI * 2 * i / segments));
        }
        return values;
    }

    private static float[] sinTable(int segments) {
        float[] values = new float[segments + 1];
        for (int i = 0; i <= segments; i++) {
            values[i] = Mth.sin((float) (Math.PI * 2 * i / segments));
        }
        return values;
    }

    private static float[] cosTableOffset(int segments, float offset) {
        float[] values = new float[segments + 1];
        for (int i = 0; i <= segments; i++) {
            values[i] = Mth.cos((float) (Math.PI * 2 * i / segments) + offset);
        }
        return values;
    }

    private static float[] sinTableOffset(int segments, float offset) {
        float[] values = new float[segments + 1];
        for (int i = 0; i <= segments; i++) {
            values[i] = Mth.sin((float) (Math.PI * 2 * i / segments) + offset);
        }
        return values;
    }

    public static Vec3 structureCenter(committee.nova.mek_ex.common.multiblock.MekanismHeartMultiblockData data) {
        var bounds = data.getBounds();
        if (bounds == null) {
            return Vec3.ZERO;
        }
        return Vec3.atLowerCornerOf(bounds.getMinPos())
              .add(Vec3.atLowerCornerOf(bounds.getMaxPos()))
              .add(1.0D, 1.0D, 1.0D)
              .scale(0.5D);
    }
}
