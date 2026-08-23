package committee.nova.mek_ex.init.mixin;

import java.util.Optional;
import committee.nova.mek_ex.common.block.entity.AbstractWindGenerator;
import committee.nova.mek_ex.common.upgrade.WindGeneratorUpgradeData;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import mekanism.api.MekanismAPITags;
import mekanism.api.tier.BaseTier;
import mekanism.common.Mekanism;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.util.WorldUtils;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.api.security.IBlockSecurityUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemTierInstaller.class, remap = false)
public abstract class ItemTierInstallerMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void upgradeVanillaWindGenerator(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemTierInstaller installer = (ItemTierInstaller) (Object) this;
        if (installer.getFromTier() != null || installer.getToTier() != BaseTier.BASIC) {
            return;
        }

        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null || level.isClientSide()) {
            return;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(MekanismBlocks.BOUNDING_BLOCK)) {
            BlockPos mainPos = BlockBounding.getMainBlockPos(level, pos);
            if (mainPos != null) {
                pos = mainPos;
                state = level.getBlockState(pos);
            }
        }
        if (!state.is(GeneratorsBlocks.WIND_GENERATOR)) {
            return;
        }
        if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, level, pos)
              || state.is(MekanismAPITags.Blocks.BLACKLIST_INSTALLER_UPGRADEABLE)) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

        BlockEntity tileEntity = WorldUtils.getTileEntity(level, pos);
        if (!(tileEntity instanceof mekanism.generators.common.tile.TileEntityWindGenerator tile)) {
            Mekanism.logger.warn("Cannot upgrade vanilla wind generator at {} in {}: missing tile entity.", pos, level.dimension().location());
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        if (!tile.playersUsing.isEmpty()) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }

        WindGeneratorUpgradeData upgradeData = createUpgradeData(level.registryAccess(), tile);
        BlockState upgradeState = BlockStateHelper.copyStateData(state, MEXBlocks.basic_wind_generator.defaultState());
        AttributeHasBounding upgradeBounding = Attribute.get(upgradeState, AttributeHasBounding.class);
        if (upgradeBounding != null && !upgradeBounding.handle(level, pos, upgradeState, pos, (world, boundingPos, mainPos) -> {
            Optional<BlockState> blockState = WorldUtils.getBlockState(world, boundingPos);
            if (blockState.isEmpty()) {
                return false;
            }
            BlockState current = blockState.get();
            if (current.canBeReplaced()) {
                return true;
            }
            return current.is(MekanismBlocks.BOUNDING_BLOCK)
                  && mainPos.equals(BlockBounding.getMainBlockPos(world, boundingPos));
        })) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        if (!level.setBlockAndUpdate(pos, upgradeState)) {
            Mekanism.logger.warn("Failed to replace vanilla wind generator at {} in {}.", pos, level.dimension().location());
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        if (upgradeBounding != null) {
            upgradeBounding.placeBoundingBlocks(level, pos, upgradeState);
        }

        TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, level, pos);
        if (!(upgradedTile instanceof AbstractWindGenerator windGenerator)) {
            Mekanism.logger.warn("Replacement at {} in {} did not create a wind generator tile entity.", pos, level.dimension().location());
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        if (tile instanceof ITileDirectional directional && directional.isDirectional()) {
            windGenerator.setFacing(directional.getDirection(), false);
        }
        windGenerator.parseUpgradeData(level.registryAccess(), upgradeData);
        windGenerator.resyncMasterToBounding();
        windGenerator.sendUpdatePacket();
        windGenerator.setChanged();
        windGenerator.invalidateCapabilitiesFull();
        if (!player.isCreative()) {
            context.getItemInHand().shrink(1);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            MekanismCriteriaTriggers.USE_TIER_INSTALLER.value().trigger(serverPlayer, BaseTier.BASIC);
        }
        cir.setReturnValue(InteractionResult.CONSUME);
    }

    private static WindGeneratorUpgradeData createUpgradeData(HolderLookup.Provider provider,
          mekanism.generators.common.tile.TileEntityWindGenerator tile) {
        if (tile.getInventorySlots(null).size() != 1 || !(tile.getInventorySlots(null).get(0) instanceof EnergyInventorySlot energySlot)) {
            throw new IllegalStateException("Vanilla wind generator has an unexpected inventory layout");
        }
        return new WindGeneratorUpgradeData(provider, tile.isPowered(), tile.getControlType(), tile.getEnergyContainer(), energySlot, tile.getComponents());
    }
}
