package committee.nova.mek_ex.client.screen;

import committee.nova.mek_ex.common.block.entity.TileEntityEnvironmentalRadiationGenerator;
import committee.nova.mek_ex.init.enums.MEXLang;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiEnvironmentalRadiationGenerator extends GuiConfigurableTile<TileEntityEnvironmentalRadiationGenerator, MekanismTileContainer<TileEntityEnvironmentalRadiationGenerator>> {

    public GuiEnvironmentalRadiationGenerator(MekanismTileContainer<TileEntityEnvironmentalRadiationGenerator> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 18, 16, 136, 62, () -> {
            List<Component> info = new ArrayList<>();
            info.add(EnergyDisplay.of(tile.getEnergyContainer()).getTextComponent());
            info.add(MEXLang.RADIATION_STORAGE.translate(
                  UnitDisplayUtils.getDisplayShort(tile.getRadiationStored(), RadiationUnit.SV, 2),
                  UnitDisplayUtils.getDisplayShort(tile.getRadiationCapacity(), RadiationUnit.SV, 2)));
            info.add(MEXLang.ENVIRONMENTAL_RADIATION.translate(UnitDisplayUtils.getDisplayShort(tile.getEnvironmentalRadiation(), RadiationUnit.SVH, 2)));
            info.add(MEXLang.GENERATION_RATE.translate(MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(tile.getGenerationRate()))));
            info.add(MEXLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput())));
            return info;
        }).spacing(1));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addRenderableWidget(new GuiEnergyTab(this, () -> List.of(
              EnergyDisplay.of(tile.getEnergyContainer()).getTextComponent(),
              MEXLang.GENERATION_RATE.translate(MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(tile.getGenerationRate()))),
              MEXLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput()))
        )));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}
