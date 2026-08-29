package committee.nova.mek_ex.init.registry;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.attachment.EnvironmentalRadiationData;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.DataComponentDeferredRegister;
import net.minecraft.core.component.DataComponentType;
import com.mojang.serialization.Codec;

public final class MEXDataComponents {

    public static final DataComponentDeferredRegister DATA_COMPONENTS = new DataComponentDeferredRegister(MekEXMod.MOD_ID);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<EnvironmentalRadiationData>> ENVIRONMENTAL_RADIATION_DATA =
          DATA_COMPONENTS.simple("environmental_radiation_data", builder -> builder
                .persistent(EnvironmentalRadiationData.CODEC)
                .networkSynchronized(EnvironmentalRadiationData.STREAM_CODEC));

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> NEBULIZED_POTION =
          DATA_COMPONENTS.simple("nebulized_potion", builder -> builder
                .persistent(Codec.BOOL)
                .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL));

    private MEXDataComponents() {
    }
}
