package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.common.upgrade.MEXUpgrades;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityDigitalMiner.class, remap = false)
public abstract class TileEntityDigitalMinerMixin {

    @Inject(method = "setReplace", at = @At("HEAD"), cancellable = true)
    private void mek_ex$voidKeepOre(BlockState state, BlockPos pos, MinerFilter<?> filter,
          CallbackInfoReturnable<Boolean> cir) {
        TileEntityDigitalMiner self = (TileEntityDigitalMiner) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        TileComponentUpgrade component = ((TileEntityMekanism) self).getComponent();
        if (component == null) {
            return;
        }
        int installed = component.getUpgrades(MEXUpgrades.voidUpgrade());
        if (installed <= 0) {
            return;
        }
        float chance = Math.min(1.0F, installed / 8.0F);
        if (level.random.nextFloat() < chance) {
            cir.setReturnValue(true);
        }
    }
}
