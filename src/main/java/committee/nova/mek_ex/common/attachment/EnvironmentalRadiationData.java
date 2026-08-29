package committee.nova.mek_ex.common.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mek_ex.common.block.entity.TileEntityEnvironmentalRadiationGenerator;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EnvironmentalRadiationData(
      double storedRadiation,
      double generationFraction,
      double conversionGenerationFraction,
      int conversionPhase,
      int stableTicks,
      double cycleConversionQuota,
      double cycleConverted
) {

    public static final Codec<EnvironmentalRadiationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.doubleRange(0D, TileEntityEnvironmentalRadiationGenerator.RADIATION_CAPACITY)
                .fieldOf("stored_radiation").forGetter(EnvironmentalRadiationData::storedRadiation),
          Codec.doubleRange(0D, 1D).fieldOf("generation_fraction").forGetter(EnvironmentalRadiationData::generationFraction),
          Codec.doubleRange(0D, 1D).fieldOf("conversion_generation_fraction")
                .forGetter(EnvironmentalRadiationData::conversionGenerationFraction),
          Codec.intRange(0, 2).fieldOf("conversion_phase").forGetter(EnvironmentalRadiationData::conversionPhase),
          Codec.intRange(0, TileEntityEnvironmentalRadiationGenerator.STABLE_TICKS_REQUIRED)
                .fieldOf("stable_ticks").forGetter(EnvironmentalRadiationData::stableTicks),
          Codec.doubleRange(0D, TileEntityEnvironmentalRadiationGenerator.MAX_CYCLE_CONVERSION_FE)
                .fieldOf("cycle_conversion_quota").forGetter(EnvironmentalRadiationData::cycleConversionQuota),
          Codec.doubleRange(0D, TileEntityEnvironmentalRadiationGenerator.MAX_CYCLE_CONVERSION_FE)
                .fieldOf("cycle_converted").forGetter(EnvironmentalRadiationData::cycleConverted)
    ).apply(instance, EnvironmentalRadiationData::new));

    public static final StreamCodec<ByteBuf, EnvironmentalRadiationData> STREAM_CODEC =
          StreamCodec.ofMember(EnvironmentalRadiationData::encode, EnvironmentalRadiationData::decode);

    public EnvironmentalRadiationData {
        requireRange("stored radiation", storedRadiation, 0D, TileEntityEnvironmentalRadiationGenerator.RADIATION_CAPACITY, true);
        requireRange("generation fraction", generationFraction, 0D, 1D, false);
        requireRange("conversion generation fraction", conversionGenerationFraction, 0D, 1D, false);
        if (conversionPhase < 0 || conversionPhase > 2) {
            throw new IllegalArgumentException("Invalid environmental radiation conversion phase: " + conversionPhase);
        }
        if (stableTicks < 0 || stableTicks > TileEntityEnvironmentalRadiationGenerator.STABLE_TICKS_REQUIRED) {
            throw new IllegalArgumentException("Invalid environmental radiation stable ticks: " + stableTicks);
        }
        if (conversionPhase == 0 && stableTicks != 0) {
            throw new IllegalArgumentException("Absorbing environmental radiation data cannot have stable ticks");
        }
        if (conversionPhase == 1 && stableTicks >= TileEntityEnvironmentalRadiationGenerator.STABLE_TICKS_REQUIRED) {
            throw new IllegalArgumentException("Stabilizing environmental radiation data has completed its stable period");
        }
        if (conversionPhase == 2 && stableTicks != TileEntityEnvironmentalRadiationGenerator.STABLE_TICKS_REQUIRED) {
            throw new IllegalArgumentException("Converting environmental radiation data requires a completed stable period");
        }
        requireRange("cycle conversion quota", cycleConversionQuota, 0D,
              TileEntityEnvironmentalRadiationGenerator.MAX_CYCLE_CONVERSION_FE, true);
        requireRange("cycle converted energy", cycleConverted, 0D,
              TileEntityEnvironmentalRadiationGenerator.MAX_CYCLE_CONVERSION_FE, true);
        if (cycleConverted > cycleConversionQuota) {
            throw new IllegalArgumentException("Environmental radiation converted energy exceeds its cycle quota");
        }
    }

    private void encode(ByteBuf buffer) {
        buffer.writeDouble(storedRadiation);
        buffer.writeDouble(generationFraction);
        buffer.writeDouble(conversionGenerationFraction);
        ByteBufCodecs.VAR_INT.encode(buffer, conversionPhase);
        ByteBufCodecs.VAR_INT.encode(buffer, stableTicks);
        buffer.writeDouble(cycleConversionQuota);
        buffer.writeDouble(cycleConverted);
    }

    private static EnvironmentalRadiationData decode(ByteBuf buffer) {
        return new EnvironmentalRadiationData(
              buffer.readDouble(),
              buffer.readDouble(),
              buffer.readDouble(),
              ByteBufCodecs.VAR_INT.decode(buffer),
              ByteBufCodecs.VAR_INT.decode(buffer),
              buffer.readDouble(),
              buffer.readDouble()
        );
    }

    private static void requireRange(String name, double value, double min, double max, boolean inclusiveMax) {
        if (!Double.isFinite(value) || value < min || (inclusiveMax ? value > max : value >= max)) {
            throw new IllegalArgumentException("Invalid environmental radiation " + name + ": " + value);
        }
    }
}
