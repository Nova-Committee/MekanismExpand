package committee.nova.mek_ex.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.client.model.ElectricSkateboardModel;
import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class RenderElectricSkateboard extends EntityRenderer<EntityElectricSkateboard> {
    private static final ResourceLocation TEXTURE = MekEXMod.rl("textures/entity/electric_skateboard.png");
    private final ElectricSkateboardModel model;

    public RenderElectricSkateboard(EntityRendererProvider.Context context) {
        super(context);
        model = new ElectricSkateboardModel(context.bakeLayer(ElectricSkateboardModel.LAYER_LOCATION));
        shadowRadius = 1.1F;
    }

    @Override
    public void render(EntityElectricSkateboard entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0, 1.5, 0);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        pose.mulPose(Axis.XP.rotationDegrees(180.0F));
        model.setupAnim(entity, 0, 0, partialTick, yaw, 0);
        model.renderToBuffer(pose, buffers.getBuffer(model.renderType(TEXTURE)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityElectricSkateboard entity) { return TEXTURE; }
}
