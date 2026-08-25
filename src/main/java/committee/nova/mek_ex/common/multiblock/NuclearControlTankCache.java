package committee.nova.mek_ex.common.multiblock;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;


public final class NuclearControlTankCache extends MultiblockCache<NuclearControlTankMultiblockData> {

    private ContainerEditMode editMode = ContainerEditMode.BOTH;

    @Override
    public void merge(MultiblockCache<NuclearControlTankMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        editMode = ((NuclearControlTankCache) mergeCache).editMode;
    }

    @Override
    public void apply(HolderLookup.Provider provider, NuclearControlTankMultiblockData data) {
        super.apply(provider, data);
        data.editMode = editMode;
    }

    @Override
    public void sync(NuclearControlTankMultiblockData data) {
        super.sync(data);
        editMode = data.editMode;
    }

    @Override
    public void load(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.load(provider, nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, SerializationConstants.EDIT_MODE, ContainerEditMode.BY_ID, mode -> editMode = mode);
    }

    @Override
    public void save(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.save(provider, nbtTags);
        NBTUtils.writeEnum(nbtTags, SerializationConstants.EDIT_MODE, editMode);
    }
}
