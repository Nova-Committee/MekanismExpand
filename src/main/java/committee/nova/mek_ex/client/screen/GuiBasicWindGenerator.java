package committee.nova.mek_ex.client.screen;

import committee.nova.mek_ex.common.block.entity.TileEntityBasicWindGenerator;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiBasicWindGenerator extends GuiAbstractWindGenerator<TileEntityBasicWindGenerator> {

    public GuiBasicWindGenerator(MekanismTileContainer<TileEntityBasicWindGenerator> container, Inventory inv, Component title) {
        super(container, inv, title);
    }
}
