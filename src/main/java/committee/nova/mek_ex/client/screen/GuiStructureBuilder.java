package committee.nova.mek_ex.client.screen;

import committee.nova.mek_ex.client.screen.element.GuiStructureMaterialSlot;
import committee.nova.mek_ex.common.block.entity.TileEntityStructureBuilder;
import committee.nova.mek_ex.common.network.StructureBuilderActionPayload;
import committee.nova.mek_ex.common.structure.MultiblockBuildRecipe;
import committee.nova.mek_ex.init.enums.MEXLang;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class GuiStructureBuilder extends GuiConfigurableTile<TileEntityStructureBuilder, MekanismTileContainer<TileEntityStructureBuilder>> {

    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;
    private static final int AXIS_OPT = 3;

    private final List<MekanismButton> minusButtons = new ArrayList<>(TileEntityStructureBuilder.SIZE_VISIBLE_AXES);
    private final List<MekanismButton> plusButtons = new ArrayList<>(TileEntityStructureBuilder.SIZE_VISIBLE_AXES);
    private final List<GuiStructureMaterialSlot> materialSlots = new ArrayList<>(TileEntityStructureBuilder.REQ_VISIBLE_SLOTS);
    private final List<Object2IntMap.Entry<Item>> materialEntries = new ArrayList<>();
    private int materialScroll;
    private int sizeScroll;

    public GuiStructureBuilder(MekanismTileContainer<TileEntityStructureBuilder> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 110;
        inventoryLabelY = imageHeight - 94;
        titleLabelY = 4;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();

        addRenderableWidget(new GuiInnerScreen(this, 8, 14, 142, 34, () -> {
            List<Component> lines = new ArrayList<>(3);
            lines.add(tile.getSelectedRecipeName());
            lines.add(statusLine().copy().append("  ")
                  .append(MEXLang.STRUCTURE_BUILDER_PROGRESS.translate(
                        tile.getProgressIndex(), Math.max(tile.getTotalBlocks(), 0))));
            if (tile.isParametricRecipe()) {
                lines.add(MEXLang.STRUCTURE_BUILDER_SIZE.translate(
                      tile.getSizeX(), tile.getSizeY(), tile.getSizeZ()));
            } else if (tile.hasOptionalCount()) {
                lines.add(optionalInfoLine());
            } else {
                lines.add(MEXLang.STRUCTURE_BUILDER_ROTATION.translate(tile.getRotationTurns() * 90));
            }
            return lines;
        }).spacing(2));

        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 160, 34,32));
        addRenderableWidget(new GuiEnergyTab(this, () -> List.of(
              EnergyDisplay.of(tile.getEnergyContainer()).getTextComponent()
        )));

        int yBtn = 52;
        addRenderableWidget(btn(8, yBtn, 16, MEXLang.STRUCTURE_BUILDER_PREV_RECIPE,
              StructureBuilderActionPayload.ACTION_CYCLE_RECIPE, -1));
        addRenderableWidget(btn(26, yBtn, 16, MEXLang.STRUCTURE_BUILDER_NEXT_RECIPE,
              StructureBuilderActionPayload.ACTION_CYCLE_RECIPE, 1));
        addRenderableWidget(btn(48, yBtn, 40, MEXLang.STRUCTURE_BUILDER_TOGGLE,
              StructureBuilderActionPayload.ACTION_TOGGLE_RUNNING, 0));
        addRenderableWidget(btn(90, yBtn, 32, MEXLang.STRUCTURE_BUILDER_RESET,
              StructureBuilderActionPayload.ACTION_RESET_PROGRESS, 0));
        addRenderableWidget(btn(124, yBtn, 28, MEXLang.STRUCTURE_BUILDER_ROTATE,
              StructureBuilderActionPayload.ACTION_ROTATE, 1));

        addRenderableWidget(new GuiElementHolder(this,
              TileEntityStructureBuilder.SIZE_PANEL_X,
              TileEntityStructureBuilder.SIZE_PANEL_Y,
              TileEntityStructureBuilder.SIZE_PANEL_W,
              TileEntityStructureBuilder.SIZE_PANEL_H));

        int btnY = TileEntityStructureBuilder.SIZE_PANEL_Y + 2;
        for (int slot = 0; slot < TileEntityStructureBuilder.SIZE_VISIBLE_AXES; slot++) {
            int baseX = axisSlotBaseX(slot);
            int finalSlot = slot;
            minusButtons.add(addRenderableWidget(new MekanismButton(this, baseX + 12, btnY, 12, 14,
                  Component.literal("−"), (e, mx, my) -> {
                adjustVisibleAxis(finalSlot, -1);
                return true;
            })));
            plusButtons.add(addRenderableWidget(new MekanismButton(this, baseX + 36, btnY, 12, 14,
                  Component.literal("+"), (e, mx, my) -> {
                adjustVisibleAxis(finalSlot, 1);
                return true;
            })));
        }

        addRenderableWidget(new GuiElementHolder(this,
              TileEntityStructureBuilder.REQ_PANEL_X,
              TileEntityStructureBuilder.REQ_PANEL_Y,
              TileEntityStructureBuilder.REQ_PANEL_W,
              TileEntityStructureBuilder.REQ_PANEL_H));

        int baseX = TileEntityStructureBuilder.REQ_PANEL_X + 6;
        int baseY = TileEntityStructureBuilder.REQ_PANEL_Y + 6;
        for (int i = 0; i < TileEntityStructureBuilder.REQ_VISIBLE_SLOTS; i++) {
            final int index = i;
            materialSlots.add(addRenderableWidget(new GuiStructureMaterialSlot(
                  this, baseX + i * TileEntityStructureBuilder.REQ_SLOT_STEP, baseY,
                  () -> materialAt(index),
                  () -> needAt(index),
                  () -> haveAt(index)
            )));
        }
    }

    private static int axisSlotBaseX(int visibleSlot) {
        return TileEntityStructureBuilder.SIZE_PANEL_X + TileEntityStructureBuilder.SIZE_AXIS_PAD
              + visibleSlot * TileEntityStructureBuilder.SIZE_AXIS_W;
    }

    private int axisAt(int visibleSlot) {
        return sizeScroll + visibleSlot;
    }

    private int sizeAxisCount() {
        return tile.hasOptionalCount()
              ? TileEntityStructureBuilder.SIZE_TOTAL_AXES
              : TileEntityStructureBuilder.SIZE_TOTAL_AXES - 1;
    }

    private void adjustVisibleAxis(int visibleSlot, int delta) {
        int axis = axisAt(visibleSlot);
        if (axis < 0 || axis >= sizeAxisCount() || !canAdjustAxis(axis, delta)) {
            return;
        }
        MultiblockBuildRecipe recipe = tile.getSelectedRecipe();
        if (recipe == null) {
            return;
        }
        switch (axis) {
            case AXIS_X -> send(StructureBuilderActionPayload.ACTION_SET_SIZE_X,
                  recipe.adjustSize(MultiblockBuildRecipe.SizeAxis.X, tile.getSizeX(), delta));
            case AXIS_Y -> send(StructureBuilderActionPayload.ACTION_SET_SIZE_Y,
                  recipe.adjustSize(MultiblockBuildRecipe.SizeAxis.Y, tile.getSizeY(), delta));
            case AXIS_Z -> send(StructureBuilderActionPayload.ACTION_SET_SIZE_Z,
                  recipe.adjustSize(MultiblockBuildRecipe.SizeAxis.Z, tile.getSizeZ(), delta));
            case AXIS_OPT -> send(StructureBuilderActionPayload.ACTION_SET_OPTIONAL_COUNT,
                  recipe.clampOptionalCount(tile.getSizeX(), tile.getSizeY(), tile.getSizeZ(),
                        tile.getOptionalCount() + delta));
            default -> {
            }
        }
    }

    private boolean canAdjustAxis(int axis, int delta) {
        if (delta == 0 || !isAxisAdjustable(axis)) {
            return false;
        }
        MultiblockBuildRecipe recipe = tile.getSelectedRecipe();
        if (recipe == null) {
            return false;
        }
        return switch (axis) {
            case AXIS_X -> recipe.adjustSize(MultiblockBuildRecipe.SizeAxis.X, tile.getSizeX(), delta)
                  != tile.getSizeX();
            case AXIS_Y -> recipe.adjustSize(MultiblockBuildRecipe.SizeAxis.Y, tile.getSizeY(), delta)
                  != tile.getSizeY();
            case AXIS_Z -> recipe.adjustSize(MultiblockBuildRecipe.SizeAxis.Z, tile.getSizeZ(), delta)
                  != tile.getSizeZ();
            case AXIS_OPT -> {
                int next = recipe.clampOptionalCount(tile.getSizeX(), tile.getSizeY(), tile.getSizeZ(),
                      tile.getOptionalCount() + delta);
                yield next != tile.getOptionalCount();
            }
            default -> false;
        };
    }

    private boolean isAxisAdjustable(int axis) {
        return switch (axis) {
            case AXIS_X -> tile.canChangeSizeX();
            case AXIS_Y -> tile.canChangeSizeY();
            case AXIS_Z -> tile.canChangeSizeZ();
            case AXIS_OPT -> tile.hasOptionalCount();
            default -> false;
        };
    }

    private int axisValue(int axis) {
        return switch (axis) {
            case AXIS_X -> tile.getSizeX();
            case AXIS_Y -> tile.getSizeY();
            case AXIS_Z -> tile.getSizeZ();
            case AXIS_OPT -> tile.getOptionalCount();
            default -> 0;
        };
    }

    private String axisLabel(int axis) {
        return switch (axis) {
            case AXIS_X -> "X";
            case AXIS_Y -> "Y";
            case AXIS_Z -> "Z";
            case AXIS_OPT -> optionalAxisLabel();
            default -> "?";
        };
    }

    @Override
    public void containerTick() {
        super.containerTick();
        refreshMaterialEntries();
        clampMaterialScroll();
        clampSizeScroll();
        updateAxisButtons();
        for (int i = 0; i < materialSlots.size(); i++) {
            materialSlots.get(i).visible = materialScroll + i < materialEntries.size();
        }
    }

    private void updateAxisButtons() {
        int count = sizeAxisCount();
        for (int slot = 0; slot < TileEntityStructureBuilder.SIZE_VISIBLE_AXES; slot++) {
            int axis = axisAt(slot);
            boolean show = axis < count && isAxisAdjustable(axis);
            minusButtons.get(slot).visible = show;
            plusButtons.get(slot).visible = show;

            minusButtons.get(slot).active = show && canAdjustAxis(axis, -1);
            plusButtons.get(slot).active = show && canAdjustAxis(axis, 1);
        }
    }

    private void refreshMaterialEntries() {
        materialEntries.clear();
        Object2IntMap<Item> required = tile.getRequiredMaterials();
        List<Object2IntMap.Entry<Item>> entries = new ArrayList<>(required.object2IntEntrySet());
        entries.sort(Comparator.comparing(e -> e.getKey().getDescriptionId()));
        materialEntries.addAll(entries);
    }

    private void clampMaterialScroll() {
        int maxScroll = Math.max(0, materialEntries.size() - TileEntityStructureBuilder.REQ_VISIBLE_SLOTS);
        materialScroll = Mth.clamp(materialScroll, 0, maxScroll);
    }

    private void clampSizeScroll() {
        int maxScroll = Math.max(0, sizeAxisCount() - TileEntityStructureBuilder.SIZE_VISIBLE_AXES);
        sizeScroll = Mth.clamp(sizeScroll, 0, maxScroll);
    }

    private Item materialAt(int visibleIndex) {
        int index = materialScroll + visibleIndex;
        return index < materialEntries.size() ? materialEntries.get(index).getKey() : null;
    }

    private int needAt(int visibleIndex) {
        int index = materialScroll + visibleIndex;
        return index < materialEntries.size() ? materialEntries.get(index).getIntValue() : 0;
    }

    private int haveAt(int visibleIndex) {
        Item item = materialAt(visibleIndex);
        return item == null ? 0 : tile.countMaterialInSlots(item);
    }

    private TranslationButton btn(int x, int y, int w, mekanism.api.text.ILangEntry label, int action, int value) {
        return new TranslationButton(this, x, y, w, 14, label, (e, mx, my) -> {
            send(action, value);
            return true;
        });
    }

    private void send(int action, int value) {
        PacketDistributor.sendToServer(new StructureBuilderActionPayload(tile.getBlockPos(), action, value));
    }

    private boolean isOverPanel(double mouseX, double mouseY, int x, int y, int w, int h) {
        int left = getGuiLeft() + x;
        int top = getGuiTop() + y;
        return mouseX >= left && mouseX < left + w && mouseY >= top && mouseY < top + h;
    }

    private boolean tryHorizontalScroll(int scroll, int maxScroll, double scrollY, java.util.function.IntConsumer setter) {
        if (maxScroll <= 0) {
            return false;
        }
        int delta = scrollY < 0 ? 1 : scrollY > 0 ? -1 : 0;
        int next = Mth.clamp(scroll + delta, 0, maxScroll);
        if (next != scroll) {
            setter.accept(next);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isOverPanel(mouseX, mouseY,
              TileEntityStructureBuilder.SIZE_PANEL_X,
              TileEntityStructureBuilder.SIZE_PANEL_Y,
              TileEntityStructureBuilder.SIZE_PANEL_W,
              TileEntityStructureBuilder.SIZE_PANEL_H)) {
            int maxScroll = Math.max(0, sizeAxisCount() - TileEntityStructureBuilder.SIZE_VISIBLE_AXES);
            if (tryHorizontalScroll(sizeScroll, maxScroll, scrollY, v -> sizeScroll = v)) {
                updateAxisButtons();
                return true;
            }
        }
        if (isOverPanel(mouseX, mouseY,
              TileEntityStructureBuilder.REQ_PANEL_X,
              TileEntityStructureBuilder.REQ_PANEL_Y,
              TileEntityStructureBuilder.REQ_PANEL_W,
              TileEntityStructureBuilder.REQ_PANEL_H)
              && !materialEntries.isEmpty()) {
            int maxScroll = Math.max(0, materialEntries.size() - TileEntityStructureBuilder.REQ_VISIBLE_SLOTS);
            if (tryHorizontalScroll(materialScroll, maxScroll, scrollY, v -> materialScroll = v)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void drawCentered(GuiGraphics guiGraphics, String text, int centerX, int color, int y) {
        int w = font.width(text);
        guiGraphics.drawString(font, text, centerX - w / 2, y, color, false);
    }

    private void drawAxisCell(GuiGraphics guiGraphics, int visibleSlot, int axis, int labelColor) {
        int baseX = axisSlotBaseX(visibleSlot);
        int y = TileEntityStructureBuilder.SIZE_PANEL_Y + 5;
        boolean adjustable = isAxisAdjustable(axis);
        int color = adjustable ? labelColor : 0x808080;
        guiGraphics.drawString(font, axisLabel(axis), baseX + 2, y, color, false);
        drawCentered(guiGraphics, String.valueOf(axisValue(axis)), baseX + 30, color, y);
    }

    private void drawScrollHints(GuiGraphics guiGraphics, int panelX, int panelY, int panelW, int scroll, int maxScroll) {
        if (maxScroll <= 0) {
            return;
        }
        int hintY = panelY + 5;
        if (scroll > 0) {
            guiGraphics.drawString(font, "<", panelX + 2, hintY, 0xA0A0A0, false);
        }
        if (scroll < maxScroll) {
            guiGraphics.drawString(font, ">", panelX + panelW - 8, hintY, 0xA0A0A0, false);
        }
    }

    private Component statusLine() {
        return switch (tile.getLastStatus()) {
            case 1 -> MEXLang.STRUCTURE_BUILDER_STATUS_BUILDING.translate();
            case 2 -> MEXLang.STRUCTURE_BUILDER_STATUS_MISSING.translate();
            case 3 -> MEXLang.STRUCTURE_BUILDER_STATUS_BLOCKED.translate();
            case 4 -> MEXLang.STRUCTURE_BUILDER_STATUS_DONE.translate();
            case 5 -> MEXLang.STRUCTURE_BUILDER_STATUS_NO_RECIPE.translate();
            case 6 -> MEXLang.STRUCTURE_BUILDER_STATUS_NO_ENERGY.translate();
            default -> tile.isRunning()
                  ? MEXLang.STRUCTURE_BUILDER_STATUS_BUILDING.translate()
                  : MEXLang.STRUCTURE_BUILDER_STATUS_IDLE.translate();
        };
    }

    private Component optionalInfoLine() {
        MultiblockBuildRecipe recipe = tile.getSelectedRecipe();
        if (recipe == null) {
            return Component.empty();
        }
        int n = tile.getOptionalCount();
        return switch (recipe.mode()) {
            case SPS -> MEXLang.STRUCTURE_BUILDER_COILS.translate(n);
            case INDUSTRIAL_TURBINE -> MEXLang.STRUCTURE_BUILDER_ROTORS.translate(n);
            case THERMOELECTRIC_BOILER -> MEXLang.STRUCTURE_BUILDER_HEATERS.translate(n);
            case INDUCTION_MATRIX -> MEXLang.STRUCTURE_BUILDER_INTERIOR.translate(n);
            default -> MEXLang.STRUCTURE_BUILDER_OPTIONAL.translate(n);
        };
    }

    private String optionalAxisLabel() {
        MultiblockBuildRecipe recipe = tile.getSelectedRecipe();
        if (recipe == null) {
            return "N";
        }
        return switch (recipe.mode()) {
            case SPS -> "C";
            case INDUSTRIAL_TURBINE -> "R";
            case THERMOELECTRIC_BOILER -> "H";
            case INDUCTION_MATRIX -> "I";
            default -> "N";
        };
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);

        int label = titleTextColor();
        int axisCount = sizeAxisCount();
        for (int slot = 0; slot < TileEntityStructureBuilder.SIZE_VISIBLE_AXES; slot++) {
            int axis = axisAt(slot);
            if (axis < axisCount) {
                drawAxisCell(guiGraphics, slot, axis, label);
            }
        }
        drawScrollHints(guiGraphics,
              TileEntityStructureBuilder.SIZE_PANEL_X,
              TileEntityStructureBuilder.SIZE_PANEL_Y,
              TileEntityStructureBuilder.SIZE_PANEL_W,
              sizeScroll,
              Math.max(0, axisCount - TileEntityStructureBuilder.SIZE_VISIBLE_AXES));

        guiGraphics.drawString(font, MEXLang.STRUCTURE_BUILDER_MATERIALS.translate(),
              TileEntityStructureBuilder.REQ_PANEL_X + 4,
              TileEntityStructureBuilder.REQ_PANEL_Y - 10,
              label, false);

        for (GuiStructureMaterialSlot slot : materialSlots) {
            if (slot.visible) {
                slot.renderIcon(guiGraphics);
            }
        }

        drawScrollHints(guiGraphics,
              TileEntityStructureBuilder.REQ_PANEL_X,
              TileEntityStructureBuilder.REQ_PANEL_Y,
              TileEntityStructureBuilder.REQ_PANEL_W,
              materialScroll,
              Math.max(0, materialEntries.size() - TileEntityStructureBuilder.REQ_VISIBLE_SLOTS));

        guiGraphics.drawString(font, MEXLang.STRUCTURE_BUILDER_MATERIAL_SLOTS.translate(),
              TileEntityStructureBuilder.MATERIAL_SLOT_X,
              TileEntityStructureBuilder.MATERIAL_SLOT_Y - 11,
              label, false);

        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
