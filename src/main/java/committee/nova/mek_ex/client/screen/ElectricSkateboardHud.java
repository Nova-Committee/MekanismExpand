package committee.nova.mek_ex.client.screen;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import mekanism.common.content.gear.HUDElement;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class ElectricSkateboardHud {
    private static final ResourceLocation SPEED_ICON = MekanismUtils.getResource(ResourceType.GUI_HUD, "gravitational_modulation_unit.png");
    private static final ResourceLocation MODE_ICON = MekanismUtils.getResource(ResourceType.GUI_HUD, "hud_mekasuit_boots.png");
    private static final ResourceLocation ENERGY_ICON = ResourceLocation.fromNamespaceAndPath(MekEXMod.MOD_ID, "textures/gui/hud/hud_energy.png");
    private static final int LINE_HEIGHT = 18;
    private static final int ICON_SIZE = 16;

    private ElectricSkateboardHud() {
    }

    public static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || !(player.getVehicle() instanceof EntityElectricSkateboard bike)) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = minecraft.font;
        long energy = Mth.clamp(bike.getEnergyContainer().getEnergy(), 0L, EntityElectricSkateboard.MAX_ENERGY);
        double ratio = energy / (double) EntityElectricSkateboard.MAX_ENERGY;
        HUDElement.HUDColor energyColor = ratio < 0.25D ? HUDElement.HUDColor.DANGER
              : ratio < 0.5D ? HUDElement.HUDColor.WARNING : HUDElement.HUDColor.REGULAR;

        int left = 8;
        int textBottom = graphics.guiHeight() - 32;

        drawHudLine(graphics, font, SPEED_ICON, Component.literal(formatSpeed(bike) + " / " + bike.getMaxSpeedKmh() + " km/h"),
              left, textBottom - LINE_HEIGHT * 2, HUDElement.HUDColor.REGULAR.getColorARGB());
        drawHudLine(graphics, font, MODE_ICON, Component.literal("G" + bike.getGear() + " " + modeLabel(bike)),
              left, textBottom - LINE_HEIGHT, modeColor(bike));
        drawHudLine(graphics, font, ENERGY_ICON, EnergyDisplay.of(energy, EntityElectricSkateboard.MAX_ENERGY).getTextComponent(),
              left, textBottom, energyColor.getColorARGB());
    }

    private static void drawHudLine(GuiGraphics graphics, Font font, ResourceLocation icon, Component text, int x, int y, int color) {
        graphics.blit(icon, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        graphics.drawString(font, text, x + ICON_SIZE + 2, y + 5, color, false);
    }

    private static String formatSpeed(EntityElectricSkateboard bike) {
        double horizontal = Math.sqrt(bike.getDeltaMovement().x * bike.getDeltaMovement().x + bike.getDeltaMovement().z * bike.getDeltaMovement().z);
        return String.format(java.util.Locale.ROOT, "%d", Math.round(horizontal * 20D * 3.6D));
    }

    private static String modeLabel(EntityElectricSkateboard bike) {
        double signed = signedSpeed(bike);
        if (Math.abs(signed) < 0.02D) {
            return "IDLE";
        }
        if (signed < 0D) {
            return "REV";
        }
        String turn = turnLabel(bike);
        return turn == null ? "FWD" : turn;
    }

    private static int modeColor(EntityElectricSkateboard bike) {
        double signed = signedSpeed(bike);
        if (Math.abs(signed) < 0.02D) {
            return HUDElement.HUDColor.FADED.getColorARGB();
        }
        if (signed < 0D) {
            return HUDElement.HUDColor.WARNING.getColorARGB();
        }
        return HUDElement.HUDColor.REGULAR.getColorARGB();
    }

    private static String turnLabel(EntityElectricSkateboard bike) {
        Vec3 movement = bike.getDeltaMovement();
        if (movement.horizontalDistanceSqr() < 0.0004D) {
            return null;
        }
        double yaw = Math.toRadians(bike.getYRot());
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        double cross = forwardX * movement.z - forwardZ * movement.x;
        if (Math.abs(cross) < 0.01D) {
            return null;
        }
        return cross > 0D ? "LEFT" : "RIGHT";
    }

    private static double signedSpeed(EntityElectricSkateboard bike) {
        Vec3 movement = bike.getDeltaMovement();
        double yaw = Math.toRadians(bike.getYRot());
        return movement.x * -Math.sin(yaw) + movement.z * Math.cos(yaw);
    }
}
