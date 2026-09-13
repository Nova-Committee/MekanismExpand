package committee.nova.mek_ex.client.screen;

import committee.nova.mek_ex.client.screen.element.GuiHeartMachineList;
import committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart;
import committee.nova.mek_ex.common.inventory.container.MekanismHeartContainer;
import committee.nova.mek_ex.common.multiblock.MekanismHeartMultiblockData;
import committee.nova.mek_ex.common.network.MekanismHeartActionPayload;
import committee.nova.mek_ex.init.enums.MEXLang;
import java.util.List;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MekanismButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class GuiMekanismHeart extends GuiMekanismTile<TileEntityMekanismHeart, MekanismHeartContainer> {

    public GuiMekanismHeart(MekanismHeartContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = 176;
        imageHeight = 186;
        titleLabelY = 5;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 8, 16, 160, 36, () -> {
            MekanismHeartMultiblockData data = tile.getMultiblock();
            return List.of(
                  MEXLang.HEART_TRANSFER_RANGE.translate(data.getTransferRange()),
                  MEXLang.HEART_RECEIVERS.translate(data.getReceiverCount()),
                  MEXLang.HEART_EXCLUDED_COUNT.translate(data.getExcludedMachines().size())
            );
        }).spacing(2));

        addRenderableWidget(new MekanismButton(this, 23, 56, 28, 16, Component.literal("-10"), (e, x, y) -> {
            send(MekanismHeartActionPayload.SET_RANGE, tile.getMultiblock().getTransferRange() - 10);
            return true;
        }));
        addRenderableWidget(new MekanismButton(this, 55, 56, 28, 16, Component.literal("-"), (e, x, y) -> {
            send(MekanismHeartActionPayload.SET_RANGE, tile.getMultiblock().getTransferRange() - 1);
            return true;
        }));
        addRenderableWidget(new MekanismButton(this, 97, 56, 28, 16, Component.literal("+"), (e, x, y) -> {
            send(MekanismHeartActionPayload.SET_RANGE, tile.getMultiblock().getTransferRange() + 1);
            return true;
        }));
        addRenderableWidget(new MekanismButton(this, 129, 56, 28, 16, Component.literal("+10"), (e, x, y) -> {
            send(MekanismHeartActionPayload.SET_RANGE, tile.getMultiblock().getTransferRange() + 10);
            return true;
        }));

        addRenderableWidget(new GuiHeartMachineList(this, 8, 78, 160, 98,
              tile::getMultiblock, this::toggleMachineAt));
    }

    private void toggleMachineAt(int index) {
        List<ResourceLocation> machines = tile.getMultiblock().getDiscoveredMachines();
        if (index < 0 || index >= machines.size()) {
            return;
        }
        send(MekanismHeartActionPayload.TOGGLE_MACHINE, 0, machines.get(index).toString());
    }

    private void send(int action, int value) {
        send(action, value, "");
    }

    private void send(int action, int value, String machineId) {
        PacketDistributor.sendToServer(new MekanismHeartActionPayload(tile.getBlockPos(), action, value, machineId));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        renderTitleText(graphics);
        super.drawForegroundText(graphics, mouseX, mouseY);
    }
}
