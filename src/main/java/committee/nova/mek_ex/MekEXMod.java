package committee.nova.mek_ex;

import committee.nova.mek_ex.common.upgrade.MEXUpgrades;
import committee.nova.mek_ex.common.network.ElectricSkateboardGearPayload;
import committee.nova.mek_ex.common.network.ElectricSkateboardInputPayload;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import committee.nova.mek_ex.init.registry.MEXContainerTypes;
import committee.nova.mek_ex.init.registry.MEXCreativeTabs;
import committee.nova.mek_ex.init.registry.MEXDataComponents;
import committee.nova.mek_ex.init.registry.MEXGenTileEntityTypes;
import committee.nova.mek_ex.init.registry.MEXItems;
import committee.nova.mek_ex.init.registry.MEXEntityTypes;
import committee.nova.mek_ex.init.registry.MEXModules;
import committee.nova.mek_ex.init.registry.MEXSounds;
import mekanism.api.MekanismIMC;
import mekanism.api.Upgrade;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraft.resources.ResourceLocation;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

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
        MEXModules.register(modEventBus);
        MEXEntityTypes.register(modEventBus);
        MEXContainerTypes.CONTAINER_TYPES.register(modEventBus);
        MEXDataComponents.DATA_COMPONENTS.register(modEventBus);
        MEXGenTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
        MEXSounds.register(modEventBus);
        MEXCreativeTabs.CREATIVE_TABS.register(modEventBus);
        modEventBus.addListener(MekEXMod::registerCapabilities);
        modEventBus.addListener(MekEXMod::enqueueIMC);
        modEventBus.addListener(ElectricSkateboardInputPayload::register);
        modEventBus.addListener(ElectricSkateboardGearPayload::register);
    }

    private static void enqueueIMC(InterModEnqueueEvent event) {
        MekanismIMC.addMekaSuitHelmetModules(MEXModules.SONAR_DETECTION_UNIT);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(IEntitySecurityUtils.INSTANCE.ownerCapability(), MEXEntityTypes.ELECTRIC_SKATEBOARD.get(), (skateboard, ctx) -> skateboard);
        event.registerEntity(IEntitySecurityUtils.INSTANCE.securityCapability(), MEXEntityTypes.ELECTRIC_SKATEBOARD.get(), (skateboard, ctx) -> skateboard);
        EnergyCompatUtils.registerEntityCapabilities(event, MEXEntityTypes.ELECTRIC_SKATEBOARD.get(), (skateboard, ctx) -> skateboard);
    }
}
