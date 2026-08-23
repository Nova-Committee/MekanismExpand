package committee.nova.mek_ex.client.screen;

import committee.nova.mek_ex.common.block.entity.TileEntityEliteWindGenerator;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiEliteWindGenerator extends GuiAbstractWindGenerator<TileEntityEliteWindGenerator> {

    public GuiEliteWindGenerator(MekanismTileContainer<TileEntityEliteWindGenerator> container, Inventory inv, Component title) {
        super(container, inv, title);
    }
}
