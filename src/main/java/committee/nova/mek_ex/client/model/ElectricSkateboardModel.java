package committee.nova.mek_ex.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public final class ElectricSkateboardModel extends EntityModel<EntityElectricSkateboard> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
          MekEXMod.rl("electric_skateboard"), "main");
    private final ModelPart main;

    public ElectricSkateboardModel(ModelPart root) {
        super(RenderType::entityCutout);
        main = root.getChild("main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition main = root.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0)
              .addBox(-5.0F, -5.0F, -17.5F, 10.0F, 2.0F, 35.0F, new CubeDeformation(0.0F)),
              PartPose.offset(0.0F, 24.0F, 0.0F));
        main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, 37)
              .addBox(-5.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
              PartPose.offsetAndRotation(0.0F, -4.4071F, -17.5071F, 2.3562F, 0.0F, 0.0F));
        main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 37)
              .addBox(-5.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
              PartPose.offsetAndRotation(0.0F, -4.4071F, 17.4929F, 0.7854F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(EntityElectricSkateboard entity, float limbSwing, float limbSwingAmount,
          float ageInTicks, float netHeadYaw, float headPitch) {
        main.yRot = 0.0F;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int packedLight,
          int packedOverlay, int color) {
        main.render(pose, consumer, packedLight, packedOverlay, color);
    }
}
