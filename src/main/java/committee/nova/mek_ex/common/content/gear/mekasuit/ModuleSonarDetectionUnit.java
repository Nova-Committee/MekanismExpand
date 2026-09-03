package committee.nova.mek_ex.common.content.gear.mekasuit;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.common.content.sonar.SonarFilter;
import committee.nova.mek_ex.common.gear.config.ModuleSonarFiltersConfig;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@ParametersAreNotNullByDefault
public record ModuleSonarDetectionUnit(List<SonarFilter<?>> filters) implements ICustomModule<ModuleSonarDetectionUnit> {
    public static final ResourceLocation HUD_ICON = MekEXMod.rl("textures/gui/hud/sonar_detection_unit.png");
    public static final long ENERGY_PER_TICK = 100L;

    public ModuleSonarDetectionUnit(IModule<ModuleSonarDetectionUnit> module) {
        this(module.<List<SonarFilter<?>>>getConfigOrThrow(ModuleSonarFiltersConfig.NAME).get());
    }

    public static int getSideLength(int installedCount) {
        if (installedCount < 1) {
            return 0;
        }
        return 4 << Math.min(installedCount, 4) - 1;
    }

    public boolean matches(BlockState state) {
        for (SonarFilter<?> filter : filters) {
            if (filter.isEnabled() && filter.hasFilter() && filter.canFilter(state)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void tickServer(IModule<ModuleSonarDetectionUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        long cost = ENERGY_PER_TICK * module.getInstalledCount();
        module.useEnergy(player, stack, cost);
    }

    @Override
    public void addHUDElements(IModule<ModuleSonarDetectionUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player,
          Consumer<IHUDElement> hudElementAdder) {
        int side = getSideLength(module.getInstalledCount());
        boolean powered = module.getContainerEnergy(stack) >= ENERGY_PER_TICK;
        IHUDElement.HUDColor color = !module.isEnabled() || !powered ? IHUDElement.HUDColor.FADED : IHUDElement.HUDColor.REGULAR;
        hudElementAdder.accept(IModuleHelper.INSTANCE.hudElement(HUD_ICON, Component.literal(side + "x" + side + "x" + side), color));
    }
}
