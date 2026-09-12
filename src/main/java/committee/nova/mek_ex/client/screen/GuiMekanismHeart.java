package committee.nova.mek_ex.client.screen;

import committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart;
import committee.nova.mek_ex.common.network.MekanismHeartActionPayload;
import committee.nova.mek_ex.common.inventory.container.MekanismHeartContainer;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class GuiMekanismHeart extends GuiMekanismTile<TileEntityMekanismHeart, MekanismHeartContainer> {
    public GuiMekanismHeart(MekanismHeartContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight = 150;
        inventoryLabelY = imageHeight - 94;
    }
    @Override protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 8, 16, 140, 46, () -> List.of(
              Component.literal("Transfer range: " + tile.getMultiblock().getTransferRange()),
              Component.literal("Excluded machines: " + tile.getMultiblock().getExcludedMachines().size())
        )));
        addRenderableWidget(new MekanismButton(this, 8, 68, 40, 18, Component.literal("-"), (e, x, y) -> {
            send(MekanismHeartActionPayload.SET_RANGE, tile.getMultiblock().getTransferRange() - 1); return true;
        }));
        addRenderableWidget(new MekanismButton(this, 54, 68, 40, 18, Component.literal("+"), (e, x, y) -> {
            send(MekanismHeartActionPayload.SET_RANGE, tile.getMultiblock().getTransferRange() + 1); return true;
        }));
        GuiTextField machine = addRenderableWidget(new GuiTextField(this, 8, 92, 140, 18));
        machine.setMaxLength(128);
        machine.setInputValidator(InputValidator.RESOURCE_LOCATION);
        addRenderableWidget(new MekanismButton(this, 8, 112, 140, 18, Component.literal("Toggle machine"), (e, x, y) -> {
            String id = machine.getText().trim();
            if (!id.isEmpty()) {
                send(MekanismHeartActionPayload.TOGGLE_MACHINE, 0, id);
                machine.setText("");
            }
            return true;
        }));
    }
    private void send(int action, int value) {
        send(action, value, "");
    }
    private void send(int action, int value, String machineId) {
        PacketDistributor.sendToServer(new MekanismHeartActionPayload(tile.getBlockPos(), action, value, machineId));
    }
    @Override protected void drawForegroundText(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        renderTitleText(graphics); renderInventoryText(graphics); super.drawForegroundText(graphics, mouseX, mouseY);
    }
}
