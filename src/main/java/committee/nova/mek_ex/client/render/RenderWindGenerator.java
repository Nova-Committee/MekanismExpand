package committee.nova.mek_ex.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import committee.nova.mek_ex.common.block.entity.AbstractWindGenerator;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.IWireFrameRenderer;
import mekanism.client.render.tileentity.ModelTileEntityRenderer;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.GeneratorsProfilerConstants;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

@NothingNullByDefault
public class RenderWindGenerator<TILE extends AbstractWindGenerator> extends ModelTileEntityRenderer<TILE, ModelWindGenerator> implements IWireFrameRenderer {

    private final ResourceLocation texture;

    public RenderWindGenerator(BlockEntityRendererProvider.Context context, ResourceLocation texture) {
        super(context, ModelWindGenerator::new);
        this.texture = texture;
    }

    @Override
    protected void render(TILE tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        float angle = setupRenderer(tile, partialTick, matrix);
        MultiBufferSource texturedRenderer = ignored -> renderer.getBuffer(RenderType.entitySolid(texture));
        model.render(matrix, texturedRenderer, angle, light, overlayLight, false);
        matrix.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.WIND_GENERATOR;
    }

    @Override
    public boolean shouldRenderOffScreen(TILE tile) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(TILE tile) {
        BlockPos pos = tile.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-2, 0, -2), pos.offset(2, 6, 2));
    }

    @Override
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer) {
        if (tile instanceof AbstractWindGenerator windGenerator) {
            float angle = setupRenderer(windGenerator, partialTick, matrix);
            model.renderWireFrame(matrix, buffer, angle);
            matrix.popPose();
        }
    }

    private float setupRenderer(AbstractWindGenerator tile, float partialTick, PoseStack matrix) {
        matrix.pushPose();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.mulPose(Axis.ZP.rotationDegrees(180));
        float angle = tile.getAngle();
        if (tile.getActive() && partialTick > 0) {
            angle = (angle + tile.getHeightSpeedRatio() * partialTick) % 360;
        }
        return angle;
    }
}
