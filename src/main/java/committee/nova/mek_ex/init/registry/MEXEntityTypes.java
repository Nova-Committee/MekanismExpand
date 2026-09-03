package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;

public final class MEXEntityTypes {
    private MEXEntityTypes() { }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MekEXMod.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<EntityElectricSkateboard>> ELECTRIC_SKATEBOARD = ENTITY_TYPES.register("electric_skateboard",
          () -> EntityType.Builder.of(EntityElectricSkateboard::new, MobCategory.MISC)
                .sized(EntityElectricSkateboard.BOARD_WIDTH, EntityElectricSkateboard.BOARD_HEIGHT)
                .clientTrackingRange(10)
                .updateInterval(1)
                .build(MekEXMod.rl("electric_skateboard").toString()));

    public static void register(IEventBus bus) { ENTITY_TYPES.register(bus); }
}
