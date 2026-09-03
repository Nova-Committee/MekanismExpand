package committee.nova.mek_ex.common.content.sonar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum SonarFilterType implements StringRepresentable {
    ITEMSTACK,
    TAG,
    MODID;

    public static final Codec<SonarFilterType> CODEC = StringRepresentable.fromEnum(SonarFilterType::values);
    public static final IntFunction<SonarFilterType> BY_ID = ByIdMap.continuous(SonarFilterType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, SonarFilterType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, SonarFilterType::ordinal);

    private final String serializedName = name().toLowerCase(Locale.ROOT);

    public MapCodec<? extends SonarFilter<?>> codec() {
        return switch (this) {
            case ITEMSTACK -> SonarItemStackFilter.CODEC;
            case TAG -> SonarTagFilter.CODEC;
            case MODID -> SonarModIDFilter.CODEC;
        };
    }

    public StreamCodec<? super RegistryFriendlyByteBuf, ? extends SonarFilter<?>> streamCodec() {
        return switch (this) {
            case ITEMSTACK -> SonarItemStackFilter.STREAM_CODEC;
            case TAG -> SonarTagFilter.STREAM_CODEC;
            case MODID -> SonarModIDFilter.STREAM_CODEC;
        };
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
