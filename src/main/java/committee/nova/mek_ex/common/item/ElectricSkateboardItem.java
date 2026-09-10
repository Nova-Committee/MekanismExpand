package committee.nova.mek_ex.common.item;

import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import committee.nova.mek_ex.init.registry.MEXEntityTypes;
import java.util.List;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class ElectricSkateboardItem extends Item {

    public ElectricSkateboardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Entity entity = MEXEntityTypes.ELECTRIC_SKATEBOARD.get().create(level);
        if (entity == null) {
            throw new IllegalStateException("Unable to create electric skateboard entity");
        }
        var pos = context.getClickLocation().add(0, 0.1, 0);
        entity.setPos(pos.x, pos.y, pos.z);
        entity.setYRot(context.getPlayer() == null ? 0 : context.getPlayer().getYRot());
        var bike = (EntityElectricSkateboard) entity;
        CustomData stored = context.getItemInHand().get(DataComponents.CUSTOM_DATA);
        if (stored == null || stored.isEmpty()) {
            if (context.getPlayer() != null) {
                bike.setOwnerUUID(context.getPlayer().getUUID());
            }
        } else {
            bike.loadItemData(stored.copyTag());
        }
        level.addFreshEntity(entity);
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
          @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        long energy = getStoredEnergy(stack);
        long max = EntityElectricSkateboard.MAX_ENERGY;
        tooltip.add(MekanismLang.STORED_ENERGY.translateColored(
              EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.of(energy, max)));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        long energy = getStoredEnergy(stack);
        long max = EntityElectricSkateboard.MAX_ENERGY;
        if (max <= 0L) {
            return 0;
        }
        return MathUtils.clampToInt(Math.round(13.0F * MathUtils.divideToLevel(energy, max)));
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    /**
     * Energy is persisted on the item via CUSTOM_DATA (same tag the entity uses when picked up / placed).
     */
    public static long getStoredEnergy(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || data.isEmpty()) {
            return 0L;
        }
        CompoundTag tag = data.copyTag();
        return tag.contains("Energy") ? tag.getLong("Energy") : 0L;
    }
}
