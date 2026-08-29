package committee.nova.mek_ex.common.item;

import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class AntimatterSuperchargedCoilItem extends ItemBlockTooltip<BlockTile<?, ?>> {

    public AntimatterSuperchargedCoilItem(BlockTile<?, ?> block, Item.Properties properties) {
        super(block, true, properties);
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        return TextComponentUtil.build(EnumColor.PURPLE, super.getName(stack));
    }
}
