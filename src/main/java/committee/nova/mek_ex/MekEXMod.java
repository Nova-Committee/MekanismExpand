package committee.nova.mek_ex;

import committee.nova.mek_ex.common.upgrade.MEXUpgrades;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import committee.nova.mek_ex.init.registry.MEXContainerTypes;
import committee.nova.mek_ex.init.registry.MEXCreativeTabs;
import committee.nova.mek_ex.init.registry.MEXDataComponents;
import committee.nova.mek_ex.init.registry.MEXGenTileEntityTypes;
import committee.nova.mek_ex.init.registry.MEXItems;
import committee.nova.mek_ex.init.registry.MEXSounds;
import mekanism.api.Upgrade;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.minecraft.resources.ResourceLocation;

@Mod(MekEXMod.MOD_ID)
public class MekEXMod {

    public static final String MOD_ID = "mek_ex";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static Upgrade CAPACITY_UPGRADE_TYPE;

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public MekEXMod(IEventBus modEventBus, ModContainer modContainer) {

        Upgrade.values();
        CAPACITY_UPGRADE_TYPE = MEXUpgrades.capacity();
        MEXBlocks.register(modEventBus);
        MEXItems.register(modEventBus);
        MEXContainerTypes.CONTAINER_TYPES.register(modEventBus);
        MEXDataComponents.DATA_COMPONENTS.register(modEventBus);
        MEXGenTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        MEXSounds.register(modEventBus);
        MEXCreativeTabs.CREATIVE_TABS.register(modEventBus);
    }
}
