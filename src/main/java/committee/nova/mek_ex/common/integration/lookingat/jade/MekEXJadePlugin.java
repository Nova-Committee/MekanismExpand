package committee.nova.mek_ex.common.integration.lookingat.jade;

import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;









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
