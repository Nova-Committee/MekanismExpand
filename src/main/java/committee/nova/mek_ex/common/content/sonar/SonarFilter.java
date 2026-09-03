package committee.nova.mek_ex.common.content.sonar;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SonarFilter<FILTER extends SonarFilter<FILTER>> {
    public static final Codec<SonarFilter<?>> GENERIC_CODEC = Codec.lazyInitialized(
          () -> SonarFilterType.CODEC.dispatch(SonarFilter::getType, SonarFilterType::codec));

    public static final StreamCodec<RegistryFriendlyByteBuf, SonarFilter<?>> GENERIC_STREAM_CODEC = new StreamCodec<>() {
        private StreamCodec<RegistryFriendlyByteBuf, SonarFilter<?>> delegate;

        private StreamCodec<RegistryFriendlyByteBuf, SonarFilter<?>> delegate() {
            if (delegate == null) {
                delegate = SonarFilterType.STREAM_CODEC
                      .<RegistryFriendlyByteBuf>cast()
                      .dispatch(SonarFilter::getType, SonarFilterType::streamCodec);
            }
            return delegate;
        }

        @Override
        public SonarFilter<?> decode(RegistryFriendlyByteBuf buffer) {
            return delegate().decode(buffer);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, SonarFilter<?> value) {
            delegate().encode(buffer, value);
        }
    };

    protected static <FILTER extends SonarFilter<FILTER>> P1<Mu<FILTER>, Boolean> baseCodec(Instance<FILTER> instance) {
        return instance.group(Codec.BOOL.optionalFieldOf("enabled", true).forGetter(SonarFilter::isEnabled));
    }

    protected static <FILTER extends SonarFilter<FILTER>> StreamCodec<ByteBuf, FILTER> baseStreamCodec(Supplier<FILTER> constructor) {
        return ByteBufCodecs.BOOL.map(enabled -> {
            FILTER filter = constructor.get();
            filter.setEnabled(enabled);
            return filter;
        }, SonarFilter::isEnabled);
    }

    private boolean enabled = true;

    protected SonarFilter() {
    }

    protected SonarFilter(boolean enabled) {
        this.enabled = enabled;
    }

    protected SonarFilter(FILTER other) {
        this(other.isEnabled());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public abstract boolean canFilter(BlockState state);

    public abstract SonarFilterType getType();

    public abstract FILTER clone();

    public abstract boolean hasFilter();

    @Override
    public int hashCode() {
        return 31 * getType().hashCode() + Boolean.hashCode(enabled);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return enabled == ((SonarFilter<?>) obj).enabled;
    }
}
