package committee.nova.mek_ex.common.item;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.upgrade.MEXUpgrades;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.item.ItemUpgrade;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CapacityUpgradeItem extends ItemUpgrade {


    public CapacityUpgradeItem(Properties properties) {
        super(getUpgradeTypeForceLoad(), properties);
    }

    private static Upgrade getUpgradeTypeForceLoad() {
        Upgrade.values();
        return MEXUpgrades.capacity();
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull net.minecraft.world.item.Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            tooltip.add(Component.translatable("upgrade.mek_ex.capacity.desc"));
            tooltip.add(Component.translatable("upgrade.mekanism.max_installed", 4));
        } else {
            tooltip.add(
                    MekanismLang.HOLD_FOR_DETAILS
                            .translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()})
            );
        }
    }
}
