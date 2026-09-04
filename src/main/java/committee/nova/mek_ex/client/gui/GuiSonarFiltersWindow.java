package committee.nova.mek_ex.client.gui;

import committee.nova.mek_ex.common.content.sonar.SonarFilter;
import committee.nova.mek_ex.common.content.sonar.SonarItemStackFilter;
import committee.nova.mek_ex.common.content.sonar.SonarModIDFilter;
import committee.nova.mek_ex.common.content.sonar.SonarTagFilter;
import committee.nova.mek_ex.common.gear.config.ModuleSonarFiltersConfig;
import committee.nova.mek_ex.init.enums.MEXLang;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.gear.IModule;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketUpdateModuleSettings;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GuiSonarFiltersWindow extends GuiWindow {
    private final IModule<?> module;
    private final int slotIndex;
    private final Consumer<ModuleSonarFiltersConfig> onSaved;
    private final List<SonarFilter<?>> filters = new ArrayList<>();
    private int selected = -1;
    private GuiTextField textField;
    private Mode mode = Mode.LIST;

    public GuiSonarFiltersWindow(IGuiWrapper gui, int x, int y, IModule<?> module, int slotIndex, List<SonarFilter<?>> current,
          Consumer<ModuleSonarFiltersConfig> onSaved) {
        super(gui, x, y, 176, 166, WindowType.UNSPECIFIED);
        this.module = module;
        this.slotIndex = slotIndex;
        this.onSaved = onSaved;
        for (SonarFilter<?> filter : current) {
            filters.add(filter.clone());
        }
        interactionStrategy = InteractionStrategy.ALL;
        initChildren();
    }

    private void initChildren() {
        addChild(new GuiInnerScreen(gui(), relativeX + 6, relativeY + 16, 164, 100));
        addChild(new TranslationButton(gui(), relativeX + 6, relativeY + 120, 52, 16, MEXLang.SONAR_ADD_ITEM, (e, mx, my) -> {
            mode = Mode.ADD_ITEM;
            showTextField();
            return true;
        }));
        addChild(new TranslationButton(gui(), relativeX + 62, relativeY + 120, 52, 16, MEXLang.SONAR_ADD_TAG, (e, mx, my) -> {
            mode = Mode.ADD_TAG;
            showTextField();
            return true;
        }));
        addChild(new TranslationButton(gui(), relativeX + 118, relativeY + 120, 52, 16, MEXLang.SONAR_ADD_MODID, (e, mx, my) -> {
            mode = Mode.ADD_MODID;
            showTextField();
            return true;
        }));
        addChild(new TranslationButton(gui(), relativeX + 6, relativeY + 140, 52, 16, MEXLang.SONAR_TOGGLE, (e, mx, my) -> {
            if (selected >= 0 && selected < filters.size()) {
                SonarFilter<?> filter = filters.get(selected);
                filter.setEnabled(!filter.isEnabled());
                save();
            }
            return true;
        }));
        addChild(new TranslationButton(gui(), relativeX + 62, relativeY + 140, 52, 16, MEXLang.SONAR_REMOVE, (e, mx, my) -> {
            if (selected >= 0 && selected < filters.size()) {
                filters.remove(selected);
                selected = -1;
                save();
            }
            return true;
        }));
        addChild(new TranslationButton(gui(), relativeX + 118, relativeY + 140, 52, 16, MEXLang.SONAR_DONE, (e, mx, my) -> {
            close();
            return true;
        }));
        textField = addChild(new GuiTextField(gui(), relativeX + 8, relativeY + 102, 160, 12));
        textField.setMaxLength(64);
        textField.setInputValidator(InputValidator.RESOURCE_LOCATION.or(InputValidator.WILDCARD_CHARS));
        textField.setVisible(false);
        textField.configureDigitalBorderInput(this::applyText);
    }

    private void showTextField() {
        if (textField != null) {
            textField.setVisible(true);
            textField.setFocused(true);
        }
    }

    private void applyText() {
        String text = textField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        if (mode == Mode.ADD_ITEM) {
            ResourceLocation id = ResourceLocation.tryParse(text);
            if (id == null) {
                return;
            }
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id);
            if (item.isEmpty() || !(item.get() instanceof BlockItem)) {
                return;
            }
            SonarItemStackFilter filter = new SonarItemStackFilter();
            filter.setItemStack(new ItemStack(item.get()));
            filters.add(filter);
            save();
        } else if (mode == Mode.ADD_TAG) {
            SonarTagFilter filter = new SonarTagFilter();
            filter.setTagName(text);
            filters.add(filter);
            save();
        } else if (mode == Mode.ADD_MODID) {
            SonarModIDFilter filter = new SonarModIDFilter();
            filter.setModID(text);
            filters.add(filter);
            save();
        }
        textField.setText("");
        textField.setVisible(false);
        mode = Mode.LIST;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listLeft = gui().getGuiLeft() + relativeX + 10;
        int listTop = gui().getGuiTop() + relativeY + 20;
        if (mouseX >= listLeft && mouseX <= listLeft + 156) {
            int index = (int) ((mouseY - listTop) / 10);
            if (index >= 0 && index < filters.size()) {
                selected = index;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void save() {
        ModuleSonarFiltersConfig config = ModuleSonarFiltersConfig.DEFAULT.withClonedFilters(filters);
        PacketUtils.sendToServer(PacketUpdateModuleSettings.create(slotIndex, module.getDataHolder(), module.getInstalledCount(), config));
        onSaved.accept(config);
    }

    @Override
    public void renderForeground(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderForeground(graphics, mouseX, mouseY);
        drawTitleText(graphics, MEXLang.SONAR_FILTERS.translate(), 6);
        int y = relativeY + 20;
        for (int i = 0; i < filters.size(); i++) {
            SonarFilter<?> filter = filters.get(i);
            int color = i == selected ? 0xFF80FFFF : filter.isEnabled() ? 0xFF20E0D0 : 0xFF808080;
            graphics.drawString(font(), describe(filter), relativeX + 10, y, color, false);
            y += 10;
            if (y > relativeY + 108) {
                break;
            }
        }
    }

    private static Component describe(SonarFilter<?> filter) {
        String prefix = filter.isEnabled() ? "[ON] " : "[OFF] ";
        return switch (filter) {
            case SonarItemStackFilter item -> {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(item.getItemStack().getItem());
                yield Component.literal(prefix + "Item: " + key);
            }
            case SonarTagFilter tag -> Component.literal(prefix + "Tag: " + tag.getTagName());
            case SonarModIDFilter mod -> Component.literal(prefix + "Mod: " + mod.getModID());
            default -> Component.literal(prefix + filter.getType().getSerializedName());
        };
    }

    private enum Mode {
        LIST,
        ADD_ITEM,
        ADD_TAG,
        ADD_MODID
    }
}
