package committee.nova.mek_ex.common.content.sonar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import mekanism.common.lib.WildcardMatcher;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

public class SonarTagFilter extends SonarFilter<SonarTagFilter> {
    public static final MapCodec<SonarTagFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseCodec(instance)
          .and(Codec.STRING.fieldOf("tag").forGetter(SonarTagFilter::getTagName))
          .apply(instance, SonarTagFilter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SonarTagFilter> STREAM_CODEC = StreamCodec.composite(
          baseStreamCodec(SonarTagFilter::new), Function.identity(),
          ByteBufCodecs.STRING_UTF8, SonarTagFilter::getTagName,
          (filter, tagName) -> {
              filter.tagName = tagName;
              return filter;
          }
    );

    private String tagName = "";

    public SonarTagFilter() {
    }

    public SonarTagFilter(String tagName) {
        this(true, tagName);
    }

    private SonarTagFilter(boolean enabled, String tagName) {
        super(enabled);
        this.tagName = tagName;
    }

    public SonarTagFilter(SonarTagFilter other) {
        super(other);
        this.tagName = other.tagName;
    }

    @Override
    public boolean canFilter(BlockState state) {
        return !tagName.isEmpty() && state.getTags().anyMatch(tag -> WildcardMatcher.matches(tagName, tag));
    }

    @Override
    public SonarFilterType getType() {
        return SonarFilterType.TAG;
    }

    @Override
    public SonarTagFilter clone() {
        return new SonarTagFilter(this);
    }

    @Override
    public boolean hasFilter() {
        return !tagName.isEmpty();
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + tagName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return tagName.equals(((SonarTagFilter) obj).tagName);
    }
}
