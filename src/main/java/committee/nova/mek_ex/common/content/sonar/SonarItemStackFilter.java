package committee.nova.mek_ex.common.content.sonar;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SonarItemStackFilter extends SonarFilter<SonarItemStackFilter> {
    public static final MapCodec<SonarItemStackFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseCodec(instance)
          .and(ItemStack.SINGLE_ITEM_CODEC.fieldOf("target_stack").forGetter(SonarItemStackFilter::getItemStack))
          .apply(instance, SonarItemStackFilter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SonarItemStackFilter> STREAM_CODEC = StreamCodec.composite(
          baseStreamCodec(SonarItemStackFilter::new), Function.identity(),
          ItemStack.OPTIONAL_STREAM_CODEC, SonarItemStackFilter::getItemStack,
          (filter, stack) -> {
              filter.itemType = stack;
              return filter;
          }
    );

    private ItemStack itemType = ItemStack.EMPTY;

    public SonarItemStackFilter() {
    }

    private SonarItemStackFilter(boolean enabled, ItemStack itemType) {
        super(enabled);
        this.itemType = itemType;
    }

    public SonarItemStackFilter(SonarItemStackFilter other) {
        super(other);
        this.itemType = other.itemType.copy();
    }

    @Override
    public boolean canFilter(BlockState state) {
        ItemStack stack = new ItemStack(state.getBlock());
        return !stack.isEmpty() && !itemType.isEmpty() && itemType.is(stack.getItemHolder());
    }

    @Override
    public SonarFilterType getType() {
        return SonarFilterType.ITEMSTACK;
    }

    @Override
    public SonarItemStackFilter clone() {
        return new SonarItemStackFilter(this);
    }

    @Override
    public boolean hasFilter() {
        return !itemType.isEmpty();
    }

    public ItemStack getItemStack() {
        return itemType;
    }

    public void setItemStack(ItemStack stack) {
        itemType = stack;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + itemType.getItem().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return itemType.getItem() == ((SonarItemStackFilter) obj).itemType.getItem();
    }
}
