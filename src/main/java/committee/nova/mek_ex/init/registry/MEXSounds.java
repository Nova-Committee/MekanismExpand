package committee.nova.mek_ex.init.registry;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.SoundEventDeferredRegister;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;

public final class MEXSounds {

    public static final SoundEventDeferredRegister SOUND_EVENTS = new SoundEventDeferredRegister(Mekanism.MODID);

    public static final SoundEventRegistryObject<SoundEvent> ENVIRONMENTAL_RADIATION_GENERATOR = SOUND_EVENTS.register(
          "tile.machine.mek_ex_environmental_radiation_generator");

    private MEXSounds() {
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
