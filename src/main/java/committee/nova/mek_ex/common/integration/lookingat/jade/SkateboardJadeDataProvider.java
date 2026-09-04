package committee.nova.mek_ex.common.integration.lookingat.jade;

import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import mekanism.api.energy.IEnergyContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public class SkateboardJadeDataProvider implements IServerDataProvider<EntityAccessor> {
    static final SkateboardJadeDataProvider INSTANCE = new SkateboardJadeDataProvider();

    private SkateboardJadeDataProvider() {
    }

    @Override
    public ResourceLocation getUid() {
        return JadeConstants.ENTITY_DATA;
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof EntityElectricSkateboard skateboard)) {
            return;
        }
        IEnergyContainer container = skateboard.getEnergyContainer();
        data.putLong(JadeConstants.ENERGY, container.getEnergy());
        data.putLong(JadeConstants.MAX_ENERGY, container.getMaxEnergy());
    }
}
