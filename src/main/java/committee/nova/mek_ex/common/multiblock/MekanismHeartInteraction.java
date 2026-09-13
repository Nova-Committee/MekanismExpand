package committee.nova.mek_ex.common.multiblock;

import committee.nova.mek_ex.common.block.entity.TileEntityMekanismHeart;
import committee.nova.mek_ex.common.multiblock.MekanismHeartTemplate.HeartPart;
import committee.nova.mek_ex.init.registry.MEXBlockTypes;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public final class MekanismHeartInteraction {

    private MekanismHeartInteraction() {
    }

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        if (player.isShiftKeyDown() || MekanismUtils.canUseAsWrench(player.getItemInHand(event.getHand()))) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!isHeartStructureBlock(level.getBlockState(pos))) {
            return;
        }
        TileEntityMekanismHeart controller = findFormedController(level, pos);
        if (controller == null) {
            return;
        }
        if (level.isClientSide()) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }
        InteractionResult result = controller.openGui(player);
        if (result.consumesAction()) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    private static boolean isHeartStructureBlock(BlockState state) {
        Block block = state.getBlock();
        return BlockType.is(block, MEXBlockTypes.BLOCK_ANTIMATTER)
              || state.is(MEXBlocks.block_antimatter.get())
              || BlockType.is(block, MekanismBlockTypes.STRUCTURAL_GLASS)
              || state.is(MekanismBlocks.STRUCTURAL_GLASS.get())
              || BlockType.is(block, MekanismBlockTypes.ULTIMATE_INDUCTION_CELL)
              || state.is(MekanismBlocks.ULTIMATE_INDUCTION_CELL.get())
              || BlockType.is(block, MekanismBlockTypes.ULTIMATE_INDUCTION_PROVIDER)
              || state.is(MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER.get());
    }

    @Nullable
    private static TileEntityMekanismHeart findFormedController(Level level, BlockPos clicked) {
        int minChunkX = SectionPos.blockToSectionCoord(clicked.getX() - MekanismHeartTemplate.SIZE_X);
        int maxChunkX = SectionPos.blockToSectionCoord(clicked.getX() + MekanismHeartTemplate.SIZE_X);
        int minChunkZ = SectionPos.blockToSectionCoord(clicked.getZ() - MekanismHeartTemplate.SIZE_Z);
        int maxChunkZ = SectionPos.blockToSectionCoord(clicked.getZ() + MekanismHeartTemplate.SIZE_Z);
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(cx, cz);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof TileEntityMekanismHeart heart)) {
                        continue;
                    }
                    MekanismHeartMultiblockData data = heart.getMultiblock();
                    if (!data.isFormed() || data.getBounds() == null) {
                        continue;
                    }
                    BlockPos min = data.getBounds().getMinPos();
                    if (!heart.getBlockPos().equals(min.offset(MekanismHeartTemplate.CONTROLLER_OFFSET))) {
                        continue;
                    }
                    int rx = clicked.getX() - min.getX();
                    int ry = clicked.getY() - min.getY();
                    int rz = clicked.getZ() - min.getZ();
                    if (MekanismHeartTemplate.getPart(rx, ry, rz) != HeartPart.AIR) {
                        return heart;
                    }
                }
            }
        }
        return null;
    }
}
