package committee.nova.mek_ex.common.block.entity;

import committee.nova.mek_ex.common.chunk.MekanismHeartChunkManager;
import committee.nova.mek_ex.common.multiblock.MekanismHeartFormer;
import committee.nova.mek_ex.common.multiblock.MekanismHeartManager;
import committee.nova.mek_ex.common.multiblock.MekanismHeartMultiblockData;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class TileEntityMekanismHeart extends TileEntityMultiblock<MekanismHeartMultiblockData> {

    public TileEntityMekanismHeart(BlockPos pos, BlockState state) {
        super(MEXBlocks.block_antimatter, pos, state);
    }

    @Override
    public MultiblockManager<MekanismHeartMultiblockData> getManager() {
        return MekanismHeartManager.MANAGER;
    }

    @Override
    @NotNull
    public MekanismHeartMultiblockData createMultiblock() {
        return new MekanismHeartMultiblockData(this);
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }

    @Override
    protected boolean onUpdateServer(MekanismHeartMultiblockData multiblock) {
        if (ticker >= 5 && ticker % 10 == 0) {
            MekanismHeartFormer.tick(this, multiblock);
        }
        return false;
    }

    @Override
    public void setRemoved() {
        if (!isRemote() && level instanceof ServerLevel serverLevel) {
            MekanismHeartChunkManager.release(serverLevel, getCacheID());
        }
        super.setRemoved();
    }

    public AABB getRenderBoundingBox() {
        return new AABB(
              Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
              Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
        );
    }
}
