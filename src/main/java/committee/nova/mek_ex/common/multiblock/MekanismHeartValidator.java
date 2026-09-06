package committee.nova.mek_ex.common.multiblock;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public final class MekanismHeartValidator extends CuboidStructureValidator<MekanismHeartMultiblockData> {

    private static final VoxelCuboid BOUNDS = new VoxelCuboid(
          MekanismHeartTemplate.SIZE_X,
          MekanismHeartTemplate.SIZE_Y,
          MekanismHeartTemplate.SIZE_Z
    );

    public MekanismHeartValidator() {
        super(BOUNDS, BOUNDS);
    }

    @Override
    public boolean precheck() {
        return false;
    }

    @Override
    protected CasingType getCasingType(BlockState state) {
        return CasingType.INVALID;
    }

    @Override
    public FormationResult postcheck(MekanismHeartMultiblockData data, Long2ObjectMap<ChunkAccess> chunkMap) {
        return FormationResult.FAIL;
    }
}
