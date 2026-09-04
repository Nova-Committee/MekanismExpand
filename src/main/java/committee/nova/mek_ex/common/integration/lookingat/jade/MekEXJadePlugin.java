package committee.nova.mek_ex.common.integration.lookingat.jade;

import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade compatibility mirroring Mekanism's looking-at energy display for entities like the Robit:
 * show Mekanism-style green energy and hide Jade's universal NeoForge energy bar.
 *
 * <p>Do not call {@code addConfig(LookingAtUtils.ENERGY)} here — Mekanism already registers that
 * option. Re-registering it crashes Jade during resource reload and prevents our client components
 * from being registered, leaving Jade's default NeoForge energy bar in place.
 */
@WailaPlugin
public class MekEXJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(SkateboardJadeDataProvider.INSTANCE, EntityElectricSkateboard.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(SkateboardJadeTooltipRenderer.INSTANCE, EntityElectricSkateboard.class);
        registration.registerEntityComponent(SkateboardJadeBuiltinRemover.INSTANCE, EntityElectricSkateboard.class);
    }
}
