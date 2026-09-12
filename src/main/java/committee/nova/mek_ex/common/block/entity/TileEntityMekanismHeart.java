package committee.nova.mek_ex.common.block.entity;

import committee.nova.mek_ex.common.chunk.MekanismHeartChunkManager;
import committee.nova.mek_ex.common.multiblock.MekanismHeartFormer;
import committee.nova.mek_ex.common.multiblock.MekanismHeartManager;
import committee.nova.mek_ex.common.multiblock.MekanismHeartMultiblockData;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    public ItemInteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack) {
        if (!player.isShiftKeyDown() && getMultiblock().isFormed()) {
            return switch (openGui(player)) {
                case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
                case CONSUME -> ItemInteractionResult.CONSUME;
                case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
                case FAIL -> ItemInteractionResult.FAIL;
                default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            };
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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
