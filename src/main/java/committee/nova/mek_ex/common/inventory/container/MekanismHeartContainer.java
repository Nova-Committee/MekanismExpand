package committee.nova.mek_ex.common.inventory.container;

import committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart;
import committee.nova.mek_ex.common.inventory.container.sync.SyncableStringList;
import committee.nova.mek_ex.common.multiblock.MekanismHeartMultiblockData;
import committee.nova.mek_ex.init.registry.MEXContainerTypes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MekanismHeartContainer extends MekanismTileContainer<TileEntityMekanismHeart> implements IEmptyContainer {

    public MekanismHeartContainer(int id, Inventory inventory, TileEntityMekanismHeart tile) {
        super(MEXContainerTypes.MEKANISM_HEART, id, inventory, tile);
    }

    @Override
    protected void addContainerTrackers() {
        super.addContainerTrackers();
        track(SyncableStringList.create(this::encodeDiscovered, this::decodeDiscovered));
        track(SyncableStringList.create(this::encodeExcluded, this::decodeExcluded));
    }

    private List<String> encodeDiscovered() {
        MekanismHeartMultiblockData data = tile.getMultiblock();
        List<ResourceLocation> machines = data.getDiscoveredMachines();
        List<String> encoded = new ArrayList<>(machines.size());
        for (int i = 0; i < machines.size(); i++) {
            encoded.add(machines.get(i) + "#" + data.getDiscoveredCount(i));
        }
        return encoded;
    }

    private void decodeDiscovered(List<String> encoded) {
        List<ResourceLocation> machines = new ArrayList<>(encoded.size());
        List<Integer> counts = new ArrayList<>(encoded.size());
        for (String entry : encoded) {
            int split = entry.lastIndexOf('#');
            String idPart = split >= 0 ? entry.substring(0, split) : entry;
            int count = 0;
            if (split >= 0) {
                try {
                    count = Integer.parseInt(entry.substring(split + 1));
                } catch (NumberFormatException ignored) {
                    count = 0;
                }
            }
            ResourceLocation id = ResourceLocation.tryParse(idPart);
            if (id != null) {
                machines.add(id);
                counts.add(count);
            }
        }
        tile.getMultiblock().setClientDiscoveredMachines(machines, counts);
    }

    private List<String> encodeExcluded() {
        List<String> encoded = new ArrayList<>();
        for (ResourceLocation id : tile.getMultiblock().getExcludedMachines()) {
            encoded.add(id.toString());
        }
        encoded.sort(String::compareTo);
        return encoded;
    }

    private void decodeExcluded(List<String> encoded) {
        Set<ResourceLocation> values = new HashSet<>();
        for (String entry : encoded) {
            ResourceLocation id = ResourceLocation.tryParse(entry);
            if (id != null) {
                values.add(id);
            }
        }
        tile.getMultiblock().setClientExcludedMachines(values);
    }
}
