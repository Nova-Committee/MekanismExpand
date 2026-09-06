package committee.nova.mek_ex.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart;
import committee.nova.mek_ex.common.multiblock.MekanismHeartMultiblockData;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

@NothingNullByDefault
public class RenderMekanismHeart extends MekanismTileEntityRenderer<TileEntityMekanismHeart> {

    private static final double EFFECT_PADDING = 4.0D;
    private final Quaternionf camRot = new Quaternionf();

    public RenderMekanismHeart(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityMekanismHeart tile, float partialTick, PoseStack matrix,
          MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        if (!tile.isMaster() || tile.getLevel() == null) {
            return;
        }
        MekanismHeartMultiblockData data = tile.getMultiblock();
        if (!data.isFormed() || data.getBounds() == null) {
            return;
        }

        Vec3 center = MekanismHeartClientEffects.structureCenter(data);
        BlockPos pos = tile.getBlockPos();
        camRot.set(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        float time = (tile.getLevel().getGameTime() + partialTick) * 0.045F;

        matrix.pushPose();
        matrix.translate(center.x - pos.getX(), center.y - pos.getY(), center.z - pos.getZ());
        MekanismHeartClientEffects.renderHeart(matrix, renderer, camRot, time);
        matrix.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityMekanismHeart tile) {
        return tile.isMaster() && tile.getMultiblock().isFormed();
    }

    @Override
    public boolean shouldRender(TileEntityMekanismHeart tile, Vec3 cameraPos) {
        if (!tile.isMaster()) {
            return false;
        }
        MekanismHeartMultiblockData data = tile.getMultiblock();
        if (!data.isFormed() || data.getBounds() == null) {
            return false;
        }
        Vec3 center = MekanismHeartClientEffects.structureCenter(data);
        double maxDist = getViewDistance();
        return center.distanceToSqr(cameraPos) < maxDist * maxDist;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityMekanismHeart tile) {
        MekanismHeartMultiblockData data = tile.getMultiblock();
        VoxelCuboid bounds = data.getBounds();
        if (!tile.isMaster() || !data.isFormed() || bounds == null) {
            return new AABB(tile.getBlockPos());
        }
        BlockPos min = bounds.getMinPos();
        BlockPos max = bounds.getMaxPos();
        return new AABB(
              min.getX() - EFFECT_PADDING,
              min.getY() - EFFECT_PADDING,
              min.getZ() - EFFECT_PADDING,
              max.getX() + 1.0D + EFFECT_PADDING,
              max.getY() + 1.0D + EFFECT_PADDING,
              max.getZ() + 1.0D + EFFECT_PADDING
        );
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SPS_CORE;
    }
}
