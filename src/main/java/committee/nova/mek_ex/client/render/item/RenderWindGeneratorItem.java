package committee.nova.mek_ex.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.MekanismISTER;
import mekanism.generators.client.model.ModelWindGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


public final class RenderWindGeneratorItem extends MekanismISTER {

    public static final RenderWindGeneratorItem RENDERER = new RenderWindGeneratorItem();

    private static final int SPEED = 16;
    private static int lastTicksUpdated;
    private static int angle;

    private ModelWindGenerator windGenerator;

    private RenderWindGeneratorItem() {
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        windGenerator = new ModelWindGenerator(getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix,
          @NotNull MultiBufferSource renderer, int light, int overlayLight) {
        if (windGenerator == null) {
            throw new IllegalStateException("Wind generator item renderer was used before model reload");
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean runningNormally = MekanismRenderer.isRunningNormally();
        if (runningNormally && minecraft.level != null) {
            int ticks = minecraft.levelRenderer.getTicks();
            if (lastTicksUpdated != ticks) {
                angle = (angle + SPEED) % 360;
                lastTicksUpdated = ticks;
            }
        }
        float renderAngle = angle;
        if (runningNormally) {
            renderAngle = (renderAngle + SPEED * MekanismRenderer.getPartialTick()) % 360;
        }

        ResourceLocation texture = textureFor(stack);
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Axis.ZP.rotationDegrees(180));
        MultiBufferSource texturedRenderer = ignored -> renderer.getBuffer(RenderType.entitySolid(texture));
        windGenerator.render(matrix, texturedRenderer, renderAngle, light, overlayLight, stack.hasFoil());
        matrix.popPose();
    }

    private static ResourceLocation textureFor(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            throw new IllegalArgumentException("Unexpected item for wind generator renderer: " + stack.getItem());
        }
        if (MEXBlocks.basic_wind_generator.is(blockItem.getBlock())) {
            return MekEXMod.rl("textures/block/wind_basic.png");
        }
        if (MEXBlocks.advanced_wind_generator.is(blockItem.getBlock())) {
            return MekEXMod.rl("textures/block/wind_advanced.png");
        }
        if (MEXBlocks.elite_wind_generator.is(blockItem.getBlock())) {
            return MekEXMod.rl("textures/block/wind_elite.png");
        }
        if (MEXBlocks.ultimate_wind_generator.is(blockItem.getBlock())) {
            return MekEXMod.rl("textures/block/wind_ultimate.png");
        }
        throw new IllegalArgumentException("Unexpected block for wind generator renderer: " + blockItem.getBlock());
    }
}
