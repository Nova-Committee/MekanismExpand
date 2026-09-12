package committee.nova.mek_ex.common.multiblock;

import committee.nova.mek_ex.common.chunk.MekanismHeartChunkManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidRelative;
import mekanism.common.lib.multiblock.IInternalMultiblock;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.MultiblockCache.CacheSubstance;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class MekanismHeartMultiblockData extends MultiblockData {

    public static final int DEFAULT_TRANSFER_RANGE = 18;
    public static final int MIN_TRANSFER_RANGE = 1;
    public static final int MAX_TRANSFER_RANGE = 128;

    public static final long CREATIVE_ENERGY = Long.MAX_VALUE;

    private final BasicEnergyContainer energyContainer;

    @ContainerSync
    private long clientEnergy;
    @ContainerSync
    private long lastOutput;
    @ContainerSync
    private int receiverCount;
    @ContainerSync
    private int syncedTransferRange = DEFAULT_TRANSFER_RANGE;

    private int transferRange = DEFAULT_TRANSFER_RANGE;
    private final Set<ResourceLocation> excludedMachines = new HashSet<>();

    private BlockPos center = BlockPos.ZERO;
    private int cellCount;
    private int providerCount;
    private int scanTicker;
    private final List<BlockPos> cachedReceivers = new ArrayList<>();
    private final List<BlockPos> cachedMultiblockAnchors = new ArrayList<>();

    public MekanismHeartMultiblockData(BlockEntity tile) {
        super(tile);
        energyContainers.add(energyContainer = VariableCapacityEnergyContainer.create(
              () -> CREATIVE_ENERGY,
              ConstantPredicates.alwaysTrue(),
              ConstantPredicates.alwaysTrue(),
              this
        ));
    }

    public void setHeartStats(BlockPos center, int cells, int providers) {
        this.center = center.immutable();
        this.cellCount = cells;
        this.providerCount = providers;
    }

    public BlockPos getCenter() {
        return center;
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public long getEnergy() {
        return isRemote() ? clientEnergy : energyContainer.getEnergy();
    }

    public long getLastOutput() {
        return lastOutput;
    }

    public int getReceiverCount() {
        return receiverCount;
    }

    public int getCellCount() {
        return cellCount;
    }

    public int getProviderCount() {
        return providerCount;
    }

    public int getTransferRange() {
        return transferRange;
    }

    public void setTransferRange(int range) {
        int clamped = Math.max(MIN_TRANSFER_RANGE, Math.min(MAX_TRANSFER_RANGE, range));
        if (transferRange != clamped) {
            transferRange = clamped;
            syncedTransferRange = clamped;
            cachedReceivers.clear();
            cachedMultiblockAnchors.clear();
            markDirty();
        }
    }

    public Set<ResourceLocation> getExcludedMachines() {
        return Set.copyOf(excludedMachines);
    }

    public void setExcludedMachines(Set<ResourceLocation> values) {
        excludedMachines.clear();
        excludedMachines.addAll(values);
        cachedReceivers.clear();
        cachedMultiblockAnchors.clear();
        markDirty();
    }

    public void toggleExcludedMachine(ResourceLocation id) {
        if (!excludedMachines.add(id)) excludedMachines.remove(id);
        cachedReceivers.clear();
        cachedMultiblockAnchors.clear();
        markDirty();
    }

    private boolean isExcluded(BlockEntity tile) {
        return excludedMachines.contains(BuiltInRegistries.BLOCK.getKey(tile.getBlockState().getBlock()));
    }

    @Override
    public void onCreated(Level world) {
        if (shouldCap(CacheSubstance.ENERGY)) {
            for (var container : getEnergyContainers(null)) {
                container.setEnergy(Math.min(container.getEnergy(), container.getMaxEnergy()));
            }
        }
        forceUpdateComparatorLevel();
    }

    @Override
    public void remove(Level world, Structure oldStructure) {
        inventoryID = null;
        setFormedForce(false);
        recheckStructure = false;
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);
        if (energyContainer.getEnergy() < CREATIVE_ENERGY) {
            energyContainer.setEnergy(CREATIVE_ENERGY);
        }
        if (world instanceof ServerLevel serverLevel) {
            MekanismHeartChunkManager.refresh(serverLevel, this);
            long outputted = distributeEnergy(serverLevel);
            if (outputted != lastOutput || clientEnergy != energyContainer.getEnergy()) {
                lastOutput = outputted;
                clientEnergy = energyContainer.getEnergy();
                needsPacket = true;
            }
        }
        return needsPacket;
    }

    private long distributeEnergy(ServerLevel world) {
        if (++scanTicker >= 20 || (cachedReceivers.isEmpty() && cachedMultiblockAnchors.isEmpty())) {
            scanTicker = 0;
            refreshReceivers(world);
        }
        long totalOutput = 0L;
        receiverCount = 0;
        Set<UUID> poweredIds = new HashSet<>();
        IdentityHashMap<MultiblockData, Boolean> poweredAnonymous = new IdentityHashMap<>();

        for (BlockPos pos : cachedMultiblockAnchors) {
            if (isInsideOwnBounds(pos)) {
                continue;
            }
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile == null) {
                continue;
            }
            MultiblockData target = resolveEnergyMultiblock(tile);
            if (target == null || target == this || target instanceof MekanismHeartMultiblockData) {
                continue;
            }
            if (target.inventoryID != null) {
                if (!poweredIds.add(target.inventoryID)) {
                    continue;
                }
            } else if (poweredAnonymous.put(target, Boolean.TRUE) != null) {
                continue;
            }
            long accepted = insertIntoMultiblock(target);
            if (accepted > 0) {
                receiverCount++;
                totalOutput += accepted;
                consumeOutput(accepted);
            }
        }

        for (BlockPos pos : cachedReceivers) {
            if (isInsideOwnBounds(pos)) {
                continue;
            }
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile == null || tile instanceof committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart) {
                continue;
            }
            MultiblockData owned = resolveEnergyMultiblock(tile);
            if (owned != null && owned != this && !(owned instanceof MekanismHeartMultiblockData)) {
                continue;
            }
            long accepted = insertInto(world, pos, tile);
            if (accepted > 0) {
                receiverCount++;
                totalOutput += accepted;
                consumeOutput(accepted);
            }
        }
        return totalOutput;
    }

    private void consumeOutput(long accepted) {
        energyContainer.extract(accepted, Action.EXECUTE, AutomationType.INTERNAL);
        if (energyContainer.isEmpty()) {
            energyContainer.setEnergy(CREATIVE_ENERGY);
        }
    }

    private void refreshReceivers(ServerLevel world) {
        cachedReceivers.clear();
        cachedMultiblockAnchors.clear();
        Set<UUID> seenIds = new HashSet<>();
        IdentityHashMap<MultiblockData, Boolean> seenAnonymous = new IdentityHashMap<>();
        AABB box = transferBox();
        int minChunkX = SectionPos.blockToSectionCoord((int) Math.floor(box.minX));
        int maxChunkX = SectionPos.blockToSectionCoord((int) Math.floor(box.maxX - 1));
        int minChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(box.minZ));
        int maxChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(box.maxZ - 1));
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (!world.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = world.getChunk(cx, cz);
                for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                    if (!box.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                        continue;
                    }
                    if (isInsideOwnBounds(pos)) {
                        continue;
                    }
                    BlockEntity be = chunk.getBlockEntity(pos);
                    if (be == null || be instanceof committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart) {
                        continue;
                    }
                    if (isExcluded(be)) {
                        continue;
                    }
                    MultiblockData multiblock = resolveEnergyMultiblock(be);
                    if (multiblock != null && multiblock != this && !(multiblock instanceof MekanismHeartMultiblockData)) {
                        if (multiblock.inventoryID != null) {
                            if (seenIds.add(multiblock.inventoryID)) {
                                cachedMultiblockAnchors.add(pos.immutable());
                            }
                        } else if (seenAnonymous.put(multiblock, Boolean.TRUE) == null) {
                            cachedMultiblockAnchors.add(pos.immutable());
                        }
                        continue;
                    }
                    if (hasEnergyHandler(world, pos, be)) {
                        cachedReceivers.add(pos.immutable());
                    }
                }
            }
        }
    }

    @Nullable
    private static MultiblockData resolveEnergyMultiblock(BlockEntity tile) {
        if (tile instanceof IMultiblock<?> multiblock) {
            MultiblockData data = multiblock.getMultiblock();
            if (data.isFormed() && !data.getEnergyContainers(null).isEmpty()) {
                return data;
            }
        }
        if (tile instanceof IInternalMultiblock internal) {
            MultiblockData data = internal.getMultiblock();
            if (data != null && data.isFormed() && !data.getEnergyContainers(null).isEmpty()) {
                return data;
            }
        }
        return null;
    }

    private long insertIntoMultiblock(MultiblockData target) {
        long accepted = 0L;
        for (IEnergyContainer container : target.getEnergyContainers(null)) {
            long remainder = container.insert(CREATIVE_ENERGY, Action.EXECUTE, AutomationType.INTERNAL);
            long got = CREATIVE_ENERGY - remainder;
            if (got > 0) {
                accepted += got;
            }
        }
        return accepted;
    }

    private boolean isInsideOwnBounds(BlockPos pos) {
        return getBounds() != null && getBounds().getRelativeLocation(pos) != CuboidRelative.OUTSIDE;
    }

    private boolean hasEnergyHandler(Level world, BlockPos pos, BlockEntity tile) {
        for (Direction side : Direction.values()) {
            if (EnergyCompatUtils.getStrictEnergyHandler(world, pos, tile.getBlockState(), tile, side) != null) {
                return true;
            }
        }
        return EnergyCompatUtils.getStrictEnergyHandler(world, pos, tile.getBlockState(), tile, null) != null;
    }

    private long insertInto(Level world, BlockPos pos, BlockEntity tile) {
        long accepted = 0L;
        for (Direction side : Direction.values()) {
            accepted += tryInsert(world, pos, tile, side);
            if (accepted < 0) {
                return Long.MAX_VALUE;
            }
        }
        accepted += tryInsert(world, pos, tile, null);
        return accepted;
    }

    private long tryInsert(Level world, BlockPos pos, BlockEntity tile, @Nullable Direction side) {
        IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(world, pos, tile.getBlockState(), tile, side);
        if (handler == null) {
            return 0L;
        }
        long accepted = 0L;
        for (int container = 0; container < handler.getEnergyContainerCount(); container++) {
            long remainder = handler.insertEnergy(container, CREATIVE_ENERGY, Action.EXECUTE);
            long got = CREATIVE_ENERGY - remainder;
            if (got > 0) {
                accepted += got;
            }
        }
        return accepted;
    }

    public AABB transferBox() {
        int half = transferRange / 2;
        return new AABB(
              center.getX() - half,
              center.getY() - half,
              center.getZ() - half,
              center.getX() + half + 1,
              center.getY() + half + 1,
              center.getZ() + half + 1
        );
    }

    @Override
    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.readUpdateTag(tag, provider);
        transferRange = Math.max(MIN_TRANSFER_RANGE, Math.min(MAX_TRANSFER_RANGE, tag.getInt("TransferRange")));
        syncedTransferRange = transferRange;
    }

    @Override
    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeUpdateTag(tag, provider);
        tag.putInt("TransferRange", transferRange);
    }

    public List<ChunkPos> getForcedChunks() {
        List<ChunkPos> chunks = new ArrayList<>();
        AABB box = transferBox();
        if (getBounds() != null) {
            BlockPos min = getBounds().getMinPos();
            BlockPos max = getBounds().getMaxPos();
            box = box.minmax(new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1));
        }
        int minChunkX = SectionPos.blockToSectionCoord((int) Math.floor(box.minX));
        int maxChunkX = SectionPos.blockToSectionCoord((int) Math.floor(box.maxX - 1));
        int minChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(box.minZ));
        int maxChunkZ = SectionPos.blockToSectionCoord((int) Math.floor(box.maxZ - 1));
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                chunks.add(new ChunkPos(cx, cz));
            }
        }
        return chunks;
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return 15;
    }
}
