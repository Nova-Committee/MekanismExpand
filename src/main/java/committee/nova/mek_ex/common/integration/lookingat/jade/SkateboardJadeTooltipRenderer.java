package committee.nova.mek_ex.common.integration.lookingat.jade;

import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;

/**
 * Renders Mekanism-style green energy for the electric skateboard.
 */
public class SkateboardJadeTooltipRenderer implements IEntityComponentProvider {
    static final SkateboardJadeTooltipRenderer INSTANCE = new SkateboardJadeTooltipRenderer();

    private SkateboardJadeTooltipRenderer() {
    }

    @Override
    public ResourceLocation getUid() {
        return JadeConstants.TOOLTIP_RENDERER;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(JadeConstants.ENERGY) || !config.get(LookingAtUtils.ENERGY)) {
            return;
        }
        long energy = data.getLong(JadeConstants.ENERGY);
        long maxEnergy = data.getLong(JadeConstants.MAX_ENERGY);
        tooltip.add(new EnergyBarElement(new EnergyElement(energy, maxEnergy)).tag(LookingAtUtils.ENERGY));
    }

    private static final class EnergyBarElement extends Element {
        private final EnergyElement element;

        private EnergyBarElement(EnergyElement element) {
            this.element = element;
        }

        @Override
        public Vec2 getSize() {
            return new Vec2(element.getWidth(), element.getHeight() + 2);
        }

        @Override
        public void render(GuiGraphics guiGraphics, float rawX, float rawY, float maxX, float maxY) {
            element.render(guiGraphics, Mth.floor(rawX), Mth.floor(rawY) + 1);
        }
    }
}
