package committee.nova.mek_ex.common.item;

import committee.nova.mek_ex.init.enums.MEXWindTier;
import mekanism.api.tier.ITier;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import net.minecraft.world.item.Item;

/**
 * Wind generator block item that uses Mekanism's tier color for its display name.
 */
public final class WindGeneratorItem extends ItemBlockTooltip<BlockTile<?, ?>> {

    public WindGeneratorItem(BlockTile<?, ?> block, Item.Properties properties) {
        super(block, true, properties);
    }

    @Override
    public ITier getTier() {
        return Attribute.getTier(getBlock(), MEXWindTier.class);
    }
}
