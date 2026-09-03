package committee.nova.mek_ex.common.item;

import committee.nova.mek_ex.init.registry.MEXEntityTypes;
import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public final class ElectricSkateboardItem extends Item {
    public ElectricSkateboardItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        Entity entity = MEXEntityTypes.ELECTRIC_SKATEBOARD.get().create(level);
        if (entity == null) throw new IllegalStateException("Unable to create electric skateboard entity");
        var pos = context.getClickLocation().add(0, 0.1, 0);
        entity.setPos(pos.x, pos.y, pos.z);
        entity.setYRot(context.getPlayer() == null ? 0 : context.getPlayer().getYRot());
        var bike = (EntityElectricSkateboard) entity;
        CustomData stored = context.getItemInHand().get(DataComponents.CUSTOM_DATA);
        if (stored == null || stored.isEmpty()) {
            if (context.getPlayer() != null) bike.setOwnerUUID(context.getPlayer().getUUID());
        } else {
            bike.loadItemData(stored.copyTag());
        }
        level.addFreshEntity(entity);
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
