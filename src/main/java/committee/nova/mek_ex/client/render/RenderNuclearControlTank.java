package committee.nova.mek_ex.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mek_ex.common.block.entity.TileEntityNuclearControlTank;
import committee.nova.mek_ex.common.multiblock.NuclearControlTankMultiblockData;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderNuclearControlTank extends MultiblockTileEntityRenderer<NuclearControlTankMultiblockData, TileEntityNuclearControlTank> {

    public RenderNuclearControlTank(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityNuclearControlTank tile, NuclearControlTankMultiblockData multiblock, float partialTick,
          PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        RenderData data = getRenderData(multiblock);
        if (data != null) {
            VertexConsumer buffer = renderer.getBuffer(Sheets.translucentCullBlockSheet());
            renderObject(data, multiblock.valves, tile.getBlockPos(), matrix, buffer, overlayLight, multiblock.prevScale);
        }
    }

    @Nullable
    private RenderData getRenderData(NuclearControlTankMultiblockData multiblock) {
        CurrentType currentType = multiblock.mergedTank.getCurrentType();
        if (currentType == CurrentType.EMPTY) {
            return null;
        }
        return (switch (currentType) {
            case FLUID -> RenderData.Builder.create(multiblock.getFluidTank().getFluid());
            case CHEMICAL -> RenderData.Builder.create(multiblock.getChemicalTank().getStack());
            default -> throw new IllegalStateException("Unknown current type.");
        }).of(multiblock).build();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.DYNAMIC_TANK;
    }

    @Override
    protected boolean shouldRender(TileEntityNuclearControlTank tile, NuclearControlTankMultiblockData multiblock, Vec3 camera) {
        return super.shouldRender(tile, multiblock, camera) && !multiblock.isEmpty();
    }
}
