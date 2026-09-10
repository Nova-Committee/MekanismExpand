package committee.nova.mek_ex.common.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import committee.nova.mek_ex.init.registry.MEXRecipeSerializers;
import committee.nova.mek_ex.init.registry.MEXRecipeTypes;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class MultiblockBuildRecipe implements Recipe<RecipeInput> {

    public enum Mode implements StringRepresentable {
        HOLLOW_CUBOID,
        FIXED,
        SPS,
        FUSION_REACTOR,
        INDUSTRIAL_TURBINE,
        FISSION_REACTOR,
        THERMOELECTRIC_BOILER,
        INDUCTION_MATRIX,
        THERMAL_EVAPORATION,
        DYNAMIC_TANK,
        NUCLEAR_CONTROL_TANK,
        MEKANISM_HEART;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public boolean isParametric() {
            return this != FIXED && this != SPS && this != FUSION_REACTOR && this != MEKANISM_HEART;
        }
    }

    public enum SizeAxis {
        X, Y, Z
    }

    public record PortSpec(Direction face, Block block) {
        public static final Codec<PortSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              Direction.CODEC.fieldOf("face").forGetter(PortSpec::face),
              BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(PortSpec::block)
        ).apply(instance, PortSpec::new));
    }

    private final Mode mode;
    private final Optional<String> titleKey;
    private final int minSize;
    private final int maxSize;
    private final int defaultSizeX;
    private final int defaultSizeY;
    private final int defaultSizeZ;
    private final Optional<Block> frame;
    private final Optional<Block> wall;
    private final Optional<Block> face;
    private final Optional<Block> coil;
    private final boolean useGlass;
    private final List<PortSpec> ports;
    private final Optional<StructurePlan> fixedPlan;

    public MultiblockBuildRecipe(
          Mode mode,
          Optional<String> titleKey,
          int minSize,
          int maxSize,
          int defaultSizeX,
          int defaultSizeY,
          int defaultSizeZ,
          Optional<Block> frame,
          Optional<Block> wall,
          Optional<Block> face,
          Optional<Block> coil,
          boolean useGlass,
          List<PortSpec> ports,
          Optional<StructurePlan> fixedPlan
    ) {
        this.mode = mode;
        this.titleKey = titleKey;
        this.minSize = Mth.clamp(minSize, 1, StructurePlan.MAX_SIZE);
        this.maxSize = Mth.clamp(maxSize, this.minSize, StructurePlan.MAX_SIZE);
        this.defaultSizeX = Mth.clamp(defaultSizeX, this.minSize, this.maxSize);
        this.defaultSizeY = Mth.clamp(defaultSizeY, this.minSize, this.maxSize);
        this.defaultSizeZ = Mth.clamp(defaultSizeZ, this.minSize, this.maxSize);
        this.frame = frame;
        this.wall = wall;
        this.face = face;
        this.coil = coil;
        this.useGlass = useGlass;
        this.ports = List.copyOf(ports);
        this.fixedPlan = fixedPlan;
        validate();
    }

    private void validate() {
        if (mode == Mode.HOLLOW_CUBOID && (frame.isEmpty() || wall.isEmpty())) {
            throw new IllegalArgumentException("hollow_cuboid multiblock recipe requires frame and wall blocks");
        }
        if (mode == Mode.FIXED && fixedPlan.isEmpty()) {
            throw new IllegalArgumentException("fixed multiblock recipe requires a plan");
        }
    }

    public Mode mode() {
        return mode;
    }

    public boolean isParametric() {
        return canChangeSizeX() || canChangeSizeY() || canChangeSizeZ();
    }

    public int minSize() {
        return minSize;
    }

    public int maxSize() {
        return maxSize;
    }

    public int defaultSizeX() {
        return defaultSizeX;
    }

    public int defaultSizeY() {
        return defaultSizeY;
    }

    public int defaultSizeZ() {
        return defaultSizeZ;
    }

    public int minSize(SizeAxis axis) {
        return switch (mode) {
            case FIXED -> switch (axis) {
                case X -> fixedPlan.map(StructurePlan::sizeX).orElse(minSize);
                case Y -> fixedPlan.map(StructurePlan::sizeY).orElse(minSize);
                case Z -> fixedPlan.map(StructurePlan::sizeZ).orElse(minSize);
            };
            case SPS -> 7;
            case FUSION_REACTOR -> 5;
            case MEKANISM_HEART -> switch (axis) {
                case X, Z -> 9;
                case Y -> 18;
            };
            case THERMAL_EVAPORATION -> switch (axis) {
                case X, Z -> 4;
                case Y -> Math.max(3, minSize);
            };
            case INDUSTRIAL_TURBINE -> switch (axis) {
                case X, Z -> 5;
                case Y -> Math.max(5, minSize);
            };
            case FISSION_REACTOR -> switch (axis) {
                case Y -> Math.max(4, minSize);
                default -> Math.max(3, minSize);
            };
            case THERMOELECTRIC_BOILER -> switch (axis) {
                case Y -> Math.max(4, minSize);
                default -> Math.max(3, minSize);
            };
            default -> minSize;
        };
    }

    public int maxSize(SizeAxis axis) {
        return switch (mode) {
            case FIXED -> minSize(axis);
            case SPS, FUSION_REACTOR, MEKANISM_HEART -> minSize(axis);
            case THERMAL_EVAPORATION -> switch (axis) {
                case X, Z -> 4;
                case Y -> Math.min(18, maxSize);
            };
            case INDUSTRIAL_TURBINE -> switch (axis) {
                case X, Z -> 17;
                case Y -> Math.min(18, maxSize);
            };
            default -> maxSize;
        };
    }

    public boolean canChangeSizeX() {
        return minSize(SizeAxis.X) < maxSize(SizeAxis.X);
    }

    public boolean canChangeSizeY() {
        return minSize(SizeAxis.Y) < maxSize(SizeAxis.Y);
    }

    public boolean canChangeSizeZ() {
        return minSize(SizeAxis.Z) < maxSize(SizeAxis.Z);
    }

    public boolean canChangeSize(SizeAxis axis) {
        return minSize(axis) < maxSize(axis);
    }

    public int clampSize(SizeAxis axis, int value) {
        int min = minSize(axis);
        int max = maxSize(axis);
        int clamped = Mth.clamp(value, min, max);
        if (mode == Mode.INDUSTRIAL_TURBINE && axis != SizeAxis.Y && clamped % 2 == 0) {
            if (clamped + 1 <= max) {
                clamped++;
            } else if (clamped - 1 >= min) {
                clamped--;
            }
        }
        return clamped;
    }

    public Component displayName() {
        return titleKey.map(Component::translatable)
              .orElseGet(() -> Component.translatable("recipe.mek_ex.multiblock_build.unnamed"));
    }

    public StructurePlan expand(int sizeX, int sizeY, int sizeZ) {
        return expand(sizeX, sizeY, sizeZ, defaultOptionalCount(sizeX, sizeY, sizeZ));
    }

    public StructurePlan expand(int sizeX, int sizeY, int sizeZ, int optionalCount) {
        int sx = clampSize(SizeAxis.X, sizeX);
        int sy = clampSize(SizeAxis.Y, sizeY);
        int sz = clampSize(SizeAxis.Z, sizeZ);
        int opt = clampOptionalCount(sx, sy, sz, optionalCount);
        return switch (mode) {
            case FIXED -> fixedPlan.orElseThrow();
            case HOLLOW_CUBOID -> StructureAssemblers.hollowCuboid(
                  sx, sy, sz,
                  frame.orElseThrow().defaultBlockState(),
                  wall.orElseThrow().defaultBlockState(),
                  face.orElse(wall.orElseThrow()).defaultBlockState(),
                  ports
            );
            case SPS -> StructureAssemblers.sps(
                  coil.orElseGet(() -> requireBlock("mekanism:supercharged_coil")),
                  face.orElseGet(() -> wall.orElseGet(() -> requireBlock("mekanism:structural_glass"))),
                  opt
            );
            case FUSION_REACTOR -> StructureAssemblers.fusionReactor();
            case INDUSTRIAL_TURBINE -> StructureAssemblers.industrialTurbine(sx, sy, sz, opt);
            case FISSION_REACTOR -> StructureAssemblers.fissionReactor(sx, sy, sz);
            case THERMOELECTRIC_BOILER -> StructureAssemblers.thermoelectricBoiler(sx, sy, sz, opt);
            case INDUCTION_MATRIX -> StructureAssemblers.inductionMatrix(sx, sy, sz, opt);
            case THERMAL_EVAPORATION -> StructureAssemblers.thermalEvaporation(sy);
            case DYNAMIC_TANK -> StructureAssemblers.dynamicTank(sx, sy, sz, useGlass);
            case NUCLEAR_CONTROL_TANK -> StructureAssemblers.nuclearControlTank(sx, sy, sz, useGlass);
            case MEKANISM_HEART -> StructureAssemblers.mekanismHeart();
        };
    }

    public boolean hasOptionalCount() {
        return switch (mode) {
            case SPS, INDUSTRIAL_TURBINE, THERMOELECTRIC_BOILER, INDUCTION_MATRIX -> true;
            default -> false;
        };
    }

    public int minOptionalCount() {
        return switch (mode) {
            case SPS, INDUCTION_MATRIX -> 0;
            case INDUSTRIAL_TURBINE, THERMOELECTRIC_BOILER -> 1;
            default -> 0;
        };
    }

    public int maxOptionalCount(int sizeX, int sizeY, int sizeZ) {
        int sx = clampSize(SizeAxis.X, sizeX);
        int sy = clampSize(SizeAxis.Y, sizeY);
        int sz = clampSize(SizeAxis.Z, sizeZ);
        return switch (mode) {
            case SPS -> StructureAssemblers.SPS_MAX_COILS;
            case INDUSTRIAL_TURBINE -> StructureAssemblers.maxTurbineRotors(sx, sy, sz);
            case THERMOELECTRIC_BOILER -> StructureAssemblers.maxBoilerHeaters(sx, sy, sz);
            case INDUCTION_MATRIX -> StructureAssemblers.maxMatrixInterior(sx, sy, sz);
            default -> 0;
        };
    }

    public int defaultOptionalCount(int sizeX, int sizeY, int sizeZ) {
        return switch (mode) {
            case SPS -> StructureAssemblers.SPS_MAX_COILS;
            case INDUSTRIAL_TURBINE -> StructureAssemblers.maxTurbineRotors(sizeX, sizeY, sizeZ);
            case THERMOELECTRIC_BOILER -> StructureAssemblers.maxBoilerHeaters(sizeX, sizeY, sizeZ);
            case INDUCTION_MATRIX -> 0;
            default -> 0;
        };
    }

    public int clampOptionalCount(int sizeX, int sizeY, int sizeZ, int value) {
        return Mth.clamp(value, minOptionalCount(), maxOptionalCount(sizeX, sizeY, sizeZ));
    }

    public Object2IntMap<Item> materialCounts(int sizeX, int sizeY, int sizeZ) {
        return expand(sizeX, sizeY, sizeZ).materialCounts();
    }

    public Object2IntMap<Item> materialCounts(int sizeX, int sizeY, int sizeZ, int optionalCount) {
        return expand(sizeX, sizeY, sizeZ, optionalCount).materialCounts();
    }

    public List<ItemStack> previewCatalysts() {
        List<ItemStack> stacks = new ArrayList<>();
        StructurePlan preview = expand(defaultSizeX, defaultSizeY, defaultSizeZ);
        for (var entry : preview.materialCounts().object2IntEntrySet()) {
            stacks.add(new ItemStack(entry.getKey(), Math.min(64, Math.max(1, entry.getIntValue()))));
            if (stacks.size() >= 12) {
                break;
            }
        }
        return stacks;
    }

    @Override
    public boolean matches(@NotNull RecipeInput input, @NotNull Level level) {
        return false;
    }

    @Override
    @NotNull
    public ItemStack assemble(@NotNull RecipeInput input, @NotNull HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    @NotNull
    public ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    @NotNull
    public RecipeSerializer<?> getSerializer() {
        return MEXRecipeSerializers.MULTIBLOCK_BUILD.get();
    }

    @Override
    @NotNull
    public RecipeType<?> getType() {
        return MEXRecipeTypes.MULTIBLOCK_BUILD.get();
    }

    public static final MapCodec<MultiblockBuildRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
          Mode.CODEC.fieldOf("mode").forGetter(MultiblockBuildRecipe::mode),
          Codec.STRING.optionalFieldOf("title_key").forGetter(r -> r.titleKey),
          Codec.intRange(1, StructurePlan.MAX_SIZE).optionalFieldOf("min_size", 3).forGetter(MultiblockBuildRecipe::minSize),
          Codec.intRange(1, StructurePlan.MAX_SIZE).optionalFieldOf("max_size", 18).forGetter(MultiblockBuildRecipe::maxSize),
          Codec.intRange(1, StructurePlan.MAX_SIZE).optionalFieldOf("default_size_x", 5).forGetter(MultiblockBuildRecipe::defaultSizeX),
          Codec.intRange(1, StructurePlan.MAX_SIZE).optionalFieldOf("default_size_y", 5).forGetter(MultiblockBuildRecipe::defaultSizeY),
          Codec.intRange(1, StructurePlan.MAX_SIZE).optionalFieldOf("default_size_z", 5).forGetter(MultiblockBuildRecipe::defaultSizeZ),
          BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("frame").forGetter(r -> r.frame),
          BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("wall").forGetter(r -> r.wall),
          BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("face").forGetter(r -> r.face),
          BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("coil").forGetter(r -> r.coil),
          Codec.BOOL.optionalFieldOf("use_glass", true).forGetter(r -> r.useGlass),
          PortSpec.CODEC.listOf().optionalFieldOf("ports", List.of()).forGetter(r -> r.ports),
          StructurePlan.CODEC.optionalFieldOf("plan").forGetter(r -> r.fixedPlan)
    ).apply(instance, MultiblockBuildRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiblockBuildRecipe> STREAM_CODEC = StreamCodec.of(
          MultiblockBuildRecipe::toNetwork, MultiblockBuildRecipe::fromNetwork
    );

    private static void toNetwork(RegistryFriendlyByteBuf buf, MultiblockBuildRecipe recipe) {
        buf.writeVarInt(recipe.mode.ordinal());
        buf.writeBoolean(recipe.titleKey.isPresent());
        recipe.titleKey.ifPresent(buf::writeUtf);
        buf.writeVarInt(recipe.minSize);
        buf.writeVarInt(recipe.maxSize);
        buf.writeVarInt(recipe.defaultSizeX);
        buf.writeVarInt(recipe.defaultSizeY);
        buf.writeVarInt(recipe.defaultSizeZ);
        writeOptionalBlock(buf, recipe.frame);
        writeOptionalBlock(buf, recipe.wall);
        writeOptionalBlock(buf, recipe.face);
        writeOptionalBlock(buf, recipe.coil);
        buf.writeBoolean(recipe.useGlass);
        buf.writeVarInt(recipe.ports.size());
        for (PortSpec port : recipe.ports) {
            buf.writeEnum(port.face());
            buf.writeVarInt(BuiltInRegistries.BLOCK.getId(port.block()));
        }
        buf.writeBoolean(recipe.fixedPlan.isPresent());
        recipe.fixedPlan.ifPresent(plan -> StructurePlan.STREAM_CODEC.encode(buf, plan));
    }

    private static MultiblockBuildRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        Mode mode = Mode.values()[Mth.clamp(buf.readVarInt(), 0, Mode.values().length - 1)];
        Optional<String> titleKey = buf.readBoolean() ? Optional.of(buf.readUtf()) : Optional.empty();
        int minSize = buf.readVarInt();
        int maxSize = buf.readVarInt();
        int dx = buf.readVarInt();
        int dy = buf.readVarInt();
        int dz = buf.readVarInt();
        Optional<Block> frame = readOptionalBlock(buf);
        Optional<Block> wall = readOptionalBlock(buf);
        Optional<Block> face = readOptionalBlock(buf);
        Optional<Block> coil = readOptionalBlock(buf);
        boolean useGlass = buf.readBoolean();
        int portCount = buf.readVarInt();
        List<PortSpec> ports = new ArrayList<>(portCount);
        for (int i = 0; i < portCount; i++) {
            Direction faceDir = buf.readEnum(Direction.class);
            Block block = BuiltInRegistries.BLOCK.byId(buf.readVarInt());
            ports.add(new PortSpec(faceDir, block));
        }
        Optional<StructurePlan> plan = buf.readBoolean() ? Optional.of(StructurePlan.STREAM_CODEC.decode(buf)) : Optional.empty();
        return new MultiblockBuildRecipe(mode, titleKey, minSize, maxSize, dx, dy, dz, frame, wall, face, coil, useGlass, ports, plan);
    }

    private static void writeOptionalBlock(RegistryFriendlyByteBuf buf, Optional<Block> block) {
        buf.writeBoolean(block.isPresent());
        block.ifPresent(value -> buf.writeVarInt(BuiltInRegistries.BLOCK.getId(value)));
    }

    private static Optional<Block> readOptionalBlock(RegistryFriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return Optional.empty();
        }
        return Optional.of(BuiltInRegistries.BLOCK.byId(buf.readVarInt()));
    }

    private static Block requireBlock(String id) {
        return BuiltInRegistries.BLOCK.getOptional(net.minecraft.resources.ResourceLocation.parse(id))
              .orElseThrow(() -> new IllegalStateException("Missing block: " + id));
    }

    public static class Serializer implements RecipeSerializer<MultiblockBuildRecipe> {
        @Override
        @NotNull
        public MapCodec<MultiblockBuildRecipe> codec() {
            return CODEC;
        }

        @Override
        @NotNull
        public StreamCodec<RegistryFriendlyByteBuf, MultiblockBuildRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
