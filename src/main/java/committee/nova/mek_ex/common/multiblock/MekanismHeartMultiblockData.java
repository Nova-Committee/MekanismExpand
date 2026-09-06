package committee.nova.mek_ex.common.multiblock;

import committee.nova.mek_ex.common.chunk.MekanismHeartChunkManager;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidRelative;
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
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class MekanismHeartMultiblockData extends MultiblockData {

    public static final long CREATIVE_ENERGY = Long.MAX_VALUE;

    private final BasicEnergyContainer energyContainer;

    @ContainerSync
    private long clientEnergy;
    @ContainerSync
    private long lastOutput;
    @ContainerSync
    private int receiverCount;

    private BlockPos center = BlockPos.ZERO;
    private int cellCount;
    private int providerCount;
    private int scanTicker;
    private final List<BlockPos> cachedReceivers = new ArrayList<>();

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
        if (++scanTicker >= 20 || cachedReceivers.isEmpty()) {
            scanTicker = 0;
            refreshReceivers(world);
        }
        long totalOutput = 0L;
        receiverCount = 0;
        for (BlockPos pos : cachedReceivers) {
            if (getBounds() != null && getBounds().getRelativeLocation(pos) != CuboidRelative.OUTSIDE) {
                continue;
            }
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile == null || tile instanceof committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart) {
                continue;
            }
            long accepted = insertInto(world, pos, tile);
            if (accepted > 0) {
                receiverCount++;
                totalOutput += accepted;
                energyContainer.extract(accepted, Action.EXECUTE, AutomationType.INTERNAL);
                if (energyContainer.isEmpty()) {
                    energyContainer.setEnergy(CREATIVE_ENERGY);
                }
            }
        }
        return totalOutput;
    }

    private void refreshReceivers(ServerLevel world) {
        cachedReceivers.clear();
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
                    BlockEntity be = chunk.getBlockEntity(pos);
                    if (be == null || be instanceof committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart) {
                        continue;
                    }
                    if (hasEnergyHandler(world, pos, be)) {
                        cachedReceivers.add(pos.immutable());
                    }
                }
            }
        }
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
        int half = MekanismHeartTemplate.TRANSFER_RANGE / 2;
        return new AABB(
              center.getX() - half,
              center.getY() - half,
              center.getZ() - half,
              center.getX() + half + 1,
              center.getY() + half + 1,
              center.getZ() + half + 1
        );
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
