package committee.nova.mek_ex.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.client.model.ElectricSkateboardModel;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class RenderElectricSkateboardItem extends MekanismISTER {
    public static final RenderElectricSkateboardItem RENDERER = new RenderElectricSkateboardItem();
    private static final ResourceLocation TEXTURE = MekEXMod.rl("textures/entity/electric_skateboard.png");

    private ElectricSkateboardModel model;

    private RenderElectricSkateboardItem() {
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        model = new ElectricSkateboardModel(getEntityModels().bakeLayer(ElectricSkateboardModel.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack pose,
          @NotNull MultiBufferSource buffers, int light, int overlayLight) {
        if (model == null) {
            throw new IllegalStateException("Electric skateboard item renderer was used before model reload");
        }
        pose.pushPose();
        pose.translate(0.5D, 1.5D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.mulPose(Axis.XP.rotationDegrees(180.0F));
        model.renderToBuffer(pose, buffers.getBuffer(model.renderType(TEXTURE)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
