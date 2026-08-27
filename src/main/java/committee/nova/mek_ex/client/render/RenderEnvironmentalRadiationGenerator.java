package committee.nova.mek_ex.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import committee.nova.mek_ex.common.block.entity.TileEntityEnvironmentalRadiationGenerator;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.lib.radiation.RadiationScale;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;

@NothingNullByDefault
public class RenderEnvironmentalRadiationGenerator extends MekanismTileEntityRenderer<TileEntityEnvironmentalRadiationGenerator> {

    private static final int RING_SEGMENTS = 8;
    private static final int RING_COUNT = 3;
    private static final float CORE_RADIUS = 0.22F;
    private static final float CORE_CENTER_Y = 0.57F;
    private static final float ROTATION_SPEED = 0.65F;
    private static final int CORE_ALPHA = 235;
    private static final float[][] CORE_VERTICES = createVertices();
    private static final int[][] CORE_TRIANGLES = createTriangles();

    public RenderEnvironmentalRadiationGenerator(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityEnvironmentalRadiationGenerator tile, float partialTick, PoseStack matrix,
          MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        if (tile.getLevel() == null) {
            return;
        }
        double radiation = tile.getEnvironmentalRadiation();
        float severity = getSeverity(radiation);
        int red = (int) (40 + 215 * severity);
        int green = (int) (220 - 175 * severity);
        int blue = (int) (70 - 50 * severity);

        long gameTime = tile.getLevel().getGameTime();
        float rotation = (gameTime + partialTick) * ROTATION_SPEED % 360F;
        VertexConsumer buffer = renderer.getBuffer(RenderType.dragonRays());
        matrix.pushPose();
        matrix.translate(0.5D, CORE_CENTER_Y, 0.5D);
        matrix.mulPose(Axis.YP.rotationDegrees(rotation));
        matrix.mulPose(Axis.XP.rotationDegrees(rotation * 0.37F));
        renderCore(matrix.last(), buffer, red, green, blue);
        matrix.popPose();
    }

    private static float getSeverity(double radiation) {
        if (!Double.isFinite(radiation) || radiation <= 0D) {
            return 0F;
        }
        return (float) Math.min(1D, Math.max(0D, RadiationScale.getScaledDoseSeverity(radiation)));
    }

    private static void renderCore(PoseStack.Pose pose, VertexConsumer buffer, int red, int green, int blue) {
        for (int[] triangle : CORE_TRIANGLES) {
            addVertex(pose, buffer, CORE_VERTICES[triangle[0]], red, green, blue);
            addVertex(pose, buffer, CORE_VERTICES[triangle[1]], red, green, blue);
            addVertex(pose, buffer, CORE_VERTICES[triangle[2]], red, green, blue);
        }
    }

    private static float[][] createVertices() {
        float[][] vertices = new float[2 + RING_COUNT * RING_SEGMENTS][3];
        vertices[0] = new float[]{0F, CORE_RADIUS, 0F};
        for (int ring = 0; ring < RING_COUNT; ring++) {
            double theta = Math.PI * (ring + 1) / (RING_COUNT + 1);
            float y = (float) Math.cos(theta) * CORE_RADIUS;
            float ringRadius = (float) Math.sin(theta) * CORE_RADIUS;
            for (int segment = 0; segment < RING_SEGMENTS; segment++) {
                double phi = 2D * Math.PI * segment / RING_SEGMENTS;
                int index = 1 + ring * RING_SEGMENTS + segment;
                vertices[index] = new float[]{
                      (float) Math.cos(phi) * ringRadius,
                      y,
                      (float) Math.sin(phi) * ringRadius
                };
            }
        }
        vertices[vertices.length - 1] = new float[]{0F, -CORE_RADIUS, 0F};
        return vertices;
    }

    private static int[][] createTriangles() {
        int triangleCount = RING_SEGMENTS * 2 + (RING_COUNT - 1) * RING_SEGMENTS * 2;
        int[][] triangles = new int[triangleCount][3];
        int triangle = 0;
        for (int segment = 0; segment < RING_SEGMENTS; segment++) {
            int next = (segment + 1) % RING_SEGMENTS;
            triangles[triangle++] = new int[]{0, ringVertex(0, next), ringVertex(0, segment)};
            triangles[triangle++] = new int[]{ringVertex(RING_COUNT - 1, segment), ringVertex(RING_COUNT - 1, next), CORE_VERTICES.length - 1};
        }

        for (int ring = 0; ring < RING_COUNT - 1; ring++) {
            for (int segment = 0; segment < RING_SEGMENTS; segment++) {
                int next = (segment + 1) % RING_SEGMENTS;
                int upper = ringVertex(ring, segment);
                int upperNext = ringVertex(ring, next);
                int lower = ringVertex(ring + 1, segment);
                int lowerNext = ringVertex(ring + 1, next);
                triangles[triangle++] = new int[]{upper, lowerNext, lower};
                triangles[triangle++] = new int[]{upper, upperNext, lowerNext};
            }
        }
        return triangles;
    }

    private static int ringVertex(int ring, int segment) {
        return 1 + ring * RING_SEGMENTS + segment;
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer buffer, float[] vertex, int red, int green, int blue) {
        buffer.addVertex(pose, vertex[0], vertex[1], vertex[2]).setColor(red, green, blue, CORE_ALPHA);
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.CONFIGURABLE_MACHINE;
    }
}
