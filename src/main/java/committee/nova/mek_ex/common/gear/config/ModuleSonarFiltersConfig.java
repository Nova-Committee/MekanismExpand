package committee.nova.mek_ex.common.gear.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.content.sonar.SonarFilter;
import committee.nova.mek_ex.common.content.sonar.SonarTagFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.api.gear.config.ModuleConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class ModuleSonarFiltersConfig extends ModuleConfig<List<SonarFilter<?>>> {
    public static final ResourceLocation NAME = MekEXMod.rl("sonar_filters");

    public static final ModuleSonarFiltersConfig DEFAULT = new ModuleSonarFiltersConfig(NAME, List.of(new SonarTagFilter("c:ores")));

    public static final Codec<ModuleSonarFiltersConfig> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance)
          .and(SonarFilter.GENERIC_CODEC.listOf().fieldOf("value").forGetter(ModuleSonarFiltersConfig::get))
          .apply(instance, ModuleSonarFiltersConfig::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ModuleSonarFiltersConfig> STREAM_CODEC = StreamCodec.composite(
          ResourceLocation.STREAM_CODEC, ModuleConfig::name,
          SonarFilter.GENERIC_STREAM_CODEC.apply(ByteBufCodecs.list()), ModuleSonarFiltersConfig::get,
          ModuleSonarFiltersConfig::new
    );

    private final List<SonarFilter<?>> filters;

    public ModuleSonarFiltersConfig(ResourceLocation name, List<SonarFilter<?>> filters) {
        super(name);
        this.filters = List.copyOf(filters);
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ModuleConfig<List<SonarFilter<?>>>> namedStreamCodec(ResourceLocation name) {
        return SonarFilter.GENERIC_STREAM_CODEC.apply(ByteBufCodecs.list())
              .map(list -> new ModuleSonarFiltersConfig(name, list), ModuleConfig::get);
    }

    @Override
    public List<SonarFilter<?>> get() {
        return filters;
    }

    @Override
    public ModuleSonarFiltersConfig with(List<SonarFilter<?>> value) {
        Objects.requireNonNull(value, "Value cannot be null.");
        return filters.equals(value) ? this : new ModuleSonarFiltersConfig(name(), value);
    }

    public ModuleSonarFiltersConfig withClonedFilters(List<SonarFilter<?>> value) {
        List<SonarFilter<?>> cloned = new ArrayList<>(value.size());
        for (SonarFilter<?> filter : value) {
            cloned.add(filter.clone());
        }
        return with(cloned);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return filters.equals(((ModuleSonarFiltersConfig) obj).filters);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + filters.hashCode();
    }
}
