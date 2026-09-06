package committee.nova.mek_ex.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.base.ProfilerConstants;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderMekanismHeart extends MekanismTileEntityRenderer<TileEntityMekanismHeart> {

    public RenderMekanismHeart(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityMekanismHeart tile, float partialTick, PoseStack matrix,
          MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityMekanismHeart tile) {
        return true;
    }

    @Override
    public boolean shouldRender(TileEntityMekanismHeart tile, Vec3 cameraPos) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 512;
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityMekanismHeart tile) {
        return new AABB(
              Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
              Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
        );
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SPS_CORE;
    }
}
