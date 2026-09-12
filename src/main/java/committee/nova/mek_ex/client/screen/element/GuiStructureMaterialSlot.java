package committee.nova.mek_ex.client.screen.element;

import committee.nova.mek_ex.init.enums.MEXLang;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerIngredientHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;






public class GuiStructureMaterialSlot extends GuiElement implements IRecipeViewerIngredientHelper {

    private final Supplier<Item> itemSupplier;
    private final IntSupplier needSupplier;
    private final IntSupplier haveSupplier;
    @Nullable
    private Item lastItem;
    private int lastNeed = Integer.MIN_VALUE;
    private int lastHave = Integer.MIN_VALUE;

    public GuiStructureMaterialSlot(IGuiWrapper gui, int x, int y,
          Supplier<Item> itemSupplier, IntSupplier needSupplier, IntSupplier haveSupplier) {
        super(gui, x, y, 16, 16);
        this.itemSupplier = itemSupplier;
        this.needSupplier = needSupplier;
        this.haveSupplier = haveSupplier;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    }

    public void renderIcon(@NotNull GuiGraphics guiGraphics) {
        Item item = itemSupplier.get();
        if (item == null || item == Items.AIR) {
            return;
        }
        gui().renderItem(guiGraphics, new ItemStack(item), relativeX, relativeY);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        Item item = itemSupplier.get();
        int need = needSupplier.getAsInt();
        int have = haveSupplier.getAsInt();
        if (item == lastItem && need == lastNeed && have == lastHave) {
            return;
        }
        lastItem = item;
        lastNeed = need;
        lastHave = have;
        if (item == null || item == Items.AIR) {
            clearTooltip();
            return;
        }
        Component name = new ItemStack(item).getHoverName();
        int color = have >= need ? 0x55FF55 : 0xFF5555;
        Component counts = MEXLang.STRUCTURE_BUILDER_MATERIAL_COUNT.translate(have, need).copy()
              .withStyle(style -> style.withColor(color));
        setTooltip(TooltipUtils.create(List.of(name, counts)));
    }

    public boolean hasItem() {
        Item item = itemSupplier.get();
        return item != null && item != Items.AIR;
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        Item item = itemSupplier.get();
        if (item == null || item == Items.AIR) {
            return Optional.empty();
        }
        return Optional.of(new ItemStack(item));
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        return new Rect2i(getX(), getY(), width, height);
    }
}
