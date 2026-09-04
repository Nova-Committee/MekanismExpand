package committee.nova.mek_ex.common.integration.lookingat.jade;

import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;

/**
 * Removes Jade's NeoForge/universal energy bar so Mekanism-style energy can replace it.
 */
public class SkateboardJadeBuiltinRemover implements IEntityComponentProvider {
    static final SkateboardJadeBuiltinRemover INSTANCE = new SkateboardJadeBuiltinRemover();

    private SkateboardJadeBuiltinRemover() {
    }

    @Override
    public ResourceLocation getUid() {
        return JadeConstants.REMOVE_BUILTIN;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains(JadeConstants.ENERGY)) {
            tooltip.remove(JadeIds.UNIVERSAL_ENERGY_STORAGE);
        }
    }

    @Override
    public int getDefaultPriority() {
        // Run after providers that add the universal NeoForge energy display.
        return TooltipPosition.TAIL;
    }
}
