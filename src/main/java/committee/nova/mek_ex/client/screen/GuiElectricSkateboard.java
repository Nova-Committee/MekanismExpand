package committee.nova.mek_ex.client.screen;

import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import committee.nova.mek_ex.common.inventory.container.ElectricSkateboardContainer;
import committee.nova.mek_ex.common.network.ElectricSkateboardGearPayload;
import committee.nova.mek_ex.init.enums.MEXLang;
import java.util.List;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public final class GuiElectricSkateboard extends GuiMekanism<ElectricSkateboardContainer> {
    private final EntityElectricSkateboard skateboard;

    public GuiElectricSkateboard(ElectricSkateboardContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        skateboard = container.getEntity();
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiSecurityTab(this, skateboard, 34));
        addRenderableWidget(new GuiHorizontalPowerBar(this, skateboard.getEnergyContainer(), 61, 64, 56));
        addRenderableWidget(new GuiInnerScreen(this, 48, 16, 80, 44, () -> List.of(
              MEXLang.SKATEBOARD_GEAR.translate(skateboard.getGear()),
              MEXLang.SKATEBOARD_MAX_SPEED.translate(skateboard.getMaxSpeedKmh()),
              EnergyDisplay.of(skateboard.getEnergyContainer()).getTextComponent(),
              MEXLang.SKATEBOARD_DRIVE_COST.translate(EnergyDisplay.of(skateboard.getDriveEnergyPerTick()))
        )));
        addRenderableWidget(new TranslationButton(this, 8, 20, 20, 16, MEXLang.SKATEBOARD_GEAR_DOWN, (element, mouseX, mouseY) -> {
            sendGear(skateboard.getGear() - 1);
            return true;
        }));
        addRenderableWidget(new TranslationButton(this, 8, 40, 20, 16, MEXLang.SKATEBOARD_GEAR_UP, (element, mouseX, mouseY) -> {
            sendGear(skateboard.getGear() + 1);
            return true;
        }));
    }

    private void sendGear(int gear) {
        int clamped = Math.max(EntityElectricSkateboard.MIN_GEAR, Math.min(EntityElectricSkateboard.MAX_GEAR, gear));
        if (clamped != skateboard.getGear()) {
            PacketDistributor.sendToServer(new ElectricSkateboardGearPayload(skateboard.getId(), clamped));
            skateboard.setGear(clamped);
        }
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        renderTitleText(graphics);
        super.drawForegroundText(graphics, mouseX, mouseY);
    }
}
