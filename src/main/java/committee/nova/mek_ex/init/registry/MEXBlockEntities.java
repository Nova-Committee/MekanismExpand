package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MEXBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MekEXMod.MOD_ID);

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
