package committee.nova.mek_ex.common.inventory.container;

import committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart;
import committee.nova.mek_ex.init.registry.MEXContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Inventory;

public class MekanismHeartContainer extends MekanismTileContainer<TileEntityMekanismHeart> {
    public MekanismHeartContainer(int id, Inventory inventory, TileEntityMekanismHeart tile) {
        super(MEXContainerTypes.MEKANISM_HEART, id, inventory, tile);
        tile.addContainerTrackers(this);
    }
}
