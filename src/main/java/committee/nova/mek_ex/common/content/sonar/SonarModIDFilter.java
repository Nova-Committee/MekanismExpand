package committee.nova.mek_ex.common.content.sonar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.util.RegistryUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class SonarModIDFilter extends SonarFilter<SonarModIDFilter> {
    public static final MapCodec<SonarModIDFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseCodec(instance)
          .and(Codec.STRING.fieldOf("modid").forGetter(SonarModIDFilter::getModID))
          .apply(instance, SonarModIDFilter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SonarModIDFilter> STREAM_CODEC = StreamCodec.composite(
          baseStreamCodec(SonarModIDFilter::new), Function.identity(),
          ByteBufCodecs.STRING_UTF8, SonarModIDFilter::getModID,
          (filter, modID) -> {
              filter.modID = modID;
              return filter;
          }
    );

    private String modID = "";

    public SonarModIDFilter() {
    }

    private SonarModIDFilter(boolean enabled, String modID) {
        super(enabled);
        this.modID = modID;
    }

    public SonarModIDFilter(SonarModIDFilter other) {
        super(other);
        this.modID = other.modID;
    }

    @Override
    public boolean canFilter(BlockState state) {
        ResourceLocation name = RegistryUtils.getName(state.getBlockHolder());
        return name != null && !modID.isEmpty() && WildcardMatcher.matches(modID, name.getNamespace());
    }

    @Override
    public SonarFilterType getType() {
        return SonarFilterType.MODID;
    }

    @Override
    public SonarModIDFilter clone() {
        return new SonarModIDFilter(this);
    }

    @Override
    public boolean hasFilter() {
        return !modID.isEmpty();
    }

    public String getModID() {
        return modID;
    }

    public void setModID(String modID) {
        this.modID = modID;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + modID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return modID.equals(((SonarModIDFilter) obj).modID);
    }
}
