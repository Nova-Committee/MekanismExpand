package committee.nova.mek_ex.client.screen;

import committee.nova.mek_ex.client.jei.MEXJEI;
import committee.nova.mek_ex.common.block.entity.TileEntityPotionNebulizer;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiPotionNebulizer extends GuiConfigurableTile<TileEntityPotionNebulizer, MekanismTileContainer<TileEntityPotionNebulizer>> {
    public GuiPotionNebulizer(MekanismTileContainer<TileEntityPotionNebulizer> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiHorizontalPowerBar(this, tile.getEnergyContainer(), 115, 75));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addRenderableWidget(new GuiFluidGauge(() -> tile.inputFluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 10, 13));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.steamTank, () -> tile.getChemicalTanks(null), GaugeType.STANDARD, this, 35, 13));
        addRenderableWidget(new GuiFluidGauge(() -> tile.outputFluidTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 120, 13));
        addRenderableWidget(new GuiProgress(tile::getActive, ProgressType.LARGE_RIGHT, this, 62, 39)
              .recipeViewerCategories(MEXJEI.POTION_NEBULIZING_VIEWER));
    }
}
