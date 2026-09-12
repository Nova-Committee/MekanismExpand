package committee.nova.mek_ex.common.multiblock;

import mekanism.common.lib.multiblock.MultiblockCache;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import java.util.HashSet;
import java.util.Set;

public final class MekanismHeartCache extends MultiblockCache<MekanismHeartMultiblockData> {
    private int transferRange = MekanismHeartMultiblockData.DEFAULT_TRANSFER_RANGE;
    private final Set<ResourceLocation> excludedMachines = new HashSet<>();

    @Override
    public void merge(MultiblockCache<MekanismHeartMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        MekanismHeartCache other = (MekanismHeartCache) mergeCache;
        transferRange = other.transferRange;
        excludedMachines.clear();
        excludedMachines.addAll(other.excludedMachines);
    }

    @Override
    public void apply(HolderLookup.Provider provider, MekanismHeartMultiblockData data) {
        super.apply(provider, data);
        data.setTransferRange(transferRange);
        data.setExcludedMachines(excludedMachines);
    }

    @Override
    public void sync(MekanismHeartMultiblockData data) {
        super.sync(data);
        transferRange = data.getTransferRange();
        excludedMachines.clear();
        excludedMachines.addAll(data.getExcludedMachines());
    }

    @Override
    public void load(HolderLookup.Provider provider, CompoundTag tag) {
        super.load(provider, tag);
        transferRange = Math.max(MekanismHeartMultiblockData.MIN_TRANSFER_RANGE,
              Math.min(MekanismHeartMultiblockData.MAX_TRANSFER_RANGE, tag.getInt("TransferRange")));
        excludedMachines.clear();
        for (Tag value : tag.getList("ExcludedMachines", Tag.TAG_STRING)) {
            String id = value.getAsString();
            ResourceLocation parsed = ResourceLocation.tryParse(id);
            if (parsed != null) excludedMachines.add(parsed);
        }
    }

    @Override
    public void save(HolderLookup.Provider provider, CompoundTag tag) {
        super.save(provider, tag);
        tag.putInt("TransferRange", transferRange);
        var list = new net.minecraft.nbt.ListTag();
        for (ResourceLocation id : excludedMachines) list.add(net.minecraft.nbt.StringTag.valueOf(id.toString()));
        tag.put("ExcludedMachines", list);
    }
}
