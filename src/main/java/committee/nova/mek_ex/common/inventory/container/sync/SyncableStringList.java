package committee.nova.mek_ex.common.inventory.container.sync;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.inventory.container.sync.list.SyncableList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public final class SyncableStringList extends SyncableList<String> {

    public static SyncableStringList create(Supplier<@NotNull List<String>> getter, Consumer<@NotNull List<String>> setter) {
        return new SyncableStringList(getter, setter);
    }

    private SyncableStringList(Supplier<@NotNull List<String>> getter, Consumer<@NotNull List<String>> setter) {
        super(getter, setter);
    }

    @Override
    protected List<String> deserializeList(RegistryFriendlyByteBuf buffer) {
        return buffer.readList(buf -> buf.readUtf(32767));
    }

    @Override
    protected void serializeListElement(RegistryFriendlyByteBuf buffer, String value) {
        buffer.writeUtf(value, 32767);
    }
}
