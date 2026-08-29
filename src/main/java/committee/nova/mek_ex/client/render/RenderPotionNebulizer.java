package committee.nova.mek_ex.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mek_ex.common.block.entity.TileEntityPotionNebulizer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;

@NothingNullByDefault
public class RenderPotionNebulizer extends MekanismTileEntityRenderer<TileEntityPotionNebulizer> {

    private static final float BOTTLE_MIN_XZ = 5.49F / 16F;
    private static final float BOTTLE_MAX_XZ = 10.49F / 16F;
    private static final float BOTTLE_MIN_Y = 9.99F / 16F;
    private static final float BOTTLE_MAX_Y = 14.99F / 16F;

    public RenderPotionNebulizer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityPotionNebulizer tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        if (!tile.outputFluidTank.isEmpty() || !tile.inputFluidTank.isEmpty()) {
            var tank = tile.outputFluidTank.isEmpty() ? tile.inputFluidTank : tile.outputFluidTank;
            var fluid = tank.getFluid();
            float scale = fluid.getAmount() / (float) tank.getCapacity();
            Model3D model = new Model3D()
                  .setTexture(MekanismRenderer.getFluidTexture(fluid, FluidTextureType.STILL))
                  .setSideRender(Direction.DOWN, false)
                  .xBounds(BOTTLE_MIN_XZ, BOTTLE_MAX_XZ)
                  .zBounds(BOTTLE_MIN_XZ, BOTTLE_MAX_XZ)
                  .yBounds(BOTTLE_MIN_Y, BOTTLE_MIN_Y + (BOTTLE_MAX_Y - BOTTLE_MIN_Y) * scale);
            MekanismRenderer.renderObject(model, matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()), MekanismRenderer.getColorARGB(fluid, scale),
                  light, overlayLight, FaceDisplay.FRONT, getCamera(), tile.getBlockPos());
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.CONFIGURABLE_MACHINE;
    }
}
