package committee.nova.mek_ex.client.screen.element;

import committee.nova.mek_ex.common.multiblock.MekanismHeartMultiblockData;
import committee.nova.mek_ex.init.enums.MEXLang;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.scroll.GuiScrollList;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.IFancyFontRenderer.TextAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class GuiHeartMachineList extends GuiScrollList {

    private static final int ROW_HEIGHT = 18;

    private final Supplier<MekanismHeartMultiblockData> dataSupplier;
    private final IntConsumer onToggle;
    private int selected = -1;

    public GuiHeartMachineList(IGuiWrapper gui, int x, int y, int width, int height,
          Supplier<MekanismHeartMultiblockData> dataSupplier, IntConsumer onToggle) {
        super(gui, x, y, width, height, ROW_HEIGHT, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE);
        this.dataSupplier = dataSupplier;
        this.onToggle = onToggle;
    }

    @Override
    protected int getMaxElements() {
        return dataSupplier.get().getDiscoveredMachines().size();
    }

    @Override
    public boolean hasSelection() {
        return selected >= 0 && selected < getMaxElements();
    }

    @Override
    protected void setSelected(int index) {
        selected = index;
        if (index >= 0 && index < getMaxElements()) {
            onToggle.accept(index);
        }
    }

    public int getSelection() {
        return selected;
    }

    @Override
    public void clearSelection() {
        selected = -1;
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        MekanismHeartMultiblockData data = dataSupplier.get();
        List<ResourceLocation> machines = data.getDiscoveredMachines();
        if (machines.isEmpty()) {
            drawScaledScrollingString(guiGraphics, MEXLang.HEART_NO_MACHINES.translate(), 0, 4,
                  TextAlignment.LEFT, screenTextColor(), barXShift, 2, false, 0.8F);
            return;
        }
        int scrollIndex = getCurrentSelection();
        int focused = getFocusedElements();
        for (int i = 0; i < focused; i++) {
            int index = scrollIndex + i;
            if (index >= machines.size()) {
                break;
            }
            ResourceLocation id = machines.get(index);
            int rowY = 1 + ROW_HEIGHT * i;
            ItemStack stack = stackFor(id);
            if (!stack.isEmpty()) {
                gui().renderItem(guiGraphics, stack, relativeX + 2, relativeY + rowY + 1);
            }
            boolean excluded = data.isMachineExcluded(id);
            Component name = stack.isEmpty() ? Component.literal(id.toString()) : stack.getHoverName();
            int count = data.getDiscoveredCount(index);
            Component label = count > 0
                  ? Component.empty().append(name).append(" x" + count)
                  : name;
            int color = excluded ? 0xFF6B6B : 0x6BFF8A;
            drawScaledScrollingString(guiGraphics, label, 18, rowY + 5, TextAlignment.LEFT,
                  color, barXShift - 40, 2, false, 0.75F);
            Component status = excluded ? MEXLang.HEART_POWER_OFF.translate() : MEXLang.HEART_POWER_ON.translate();
            drawScaledScrollingString(guiGraphics, status, barXShift - 38, rowY + 5, TextAlignment.LEFT,
                  color, 36, 0, false, 0.7F);
        }
    }

    @Override
    protected void renderElements(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int scrollIndex = getCurrentSelection();
        if (selected != -1 && selected >= scrollIndex && selected <= scrollIndex + getFocusedElements() - 1) {
            guiGraphics.blit(getResource(), relativeX + 1, relativeY + 1 + (selected - scrollIndex) * ROW_HEIGHT,
                  barXShift - 2, ROW_HEIGHT, 4, 2, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        MekanismHeartMultiblockData data = dataSupplier.get();
        List<ResourceLocation> machines = data.getDiscoveredMachines();
        int scrollIndex = getCurrentSelection();
        int focused = getFocusedElements();
        for (int i = 0; i < focused; i++) {
            int index = scrollIndex + i;
            if (index >= machines.size()) {
                break;
            }
            int rowY = getY() + 1 + ROW_HEIGHT * i;
            if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT && mouseX >= getX() + 1 && mouseX < getX() + barXShift - 1) {
                ResourceLocation id = machines.get(index);
                ItemStack stack = stackFor(id);
                Component name = stack.isEmpty() ? Component.literal(id.toString()) : stack.getHoverName();
                boolean excluded = data.isMachineExcluded(id);
                setTooltip(TooltipUtils.create(List.of(
                      name,
                      Component.literal(id.toString()),
                      excluded ? MEXLang.HEART_CLICK_ENABLE.translate() : MEXLang.HEART_CLICK_DISABLE.translate()
                )));
                return;
            }
        }
        clearTooltip();
    }

    @Override
    public void syncFrom(GuiElement element) {
        GuiHeartMachineList old = (GuiHeartMachineList) element;
        selected = old.selected;
        super.syncFrom(element);
    }

    private static ItemStack stackFor(ResourceLocation id) {
        var block = BuiltInRegistries.BLOCK.get(id);
        if (block == Blocks.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(block.asItem());
    }
}
