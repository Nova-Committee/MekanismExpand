package committee.nova.mek_ex.client;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.client.render.RenderEnvironmentalRadiationGenerator;
import committee.nova.mek_ex.client.render.RenderWindGenerator;
import committee.nova.mek_ex.client.render.RenderNuclearControlTank;
import committee.nova.mek_ex.client.render.item.RenderWindGeneratorItem;
import committee.nova.mek_ex.client.screen.GuiAdvancedWindGenerator;
import committee.nova.mek_ex.client.screen.GuiBasicWindGenerator;
import committee.nova.mek_ex.client.screen.GuiEliteWindGenerator;
import committee.nova.mek_ex.client.screen.GuiUltimateWindGenerator;
import committee.nova.mek_ex.client.screen.GuiNuclearControlTank;
import committee.nova.mek_ex.client.screen.GuiNeutronActivator;
import committee.nova.mek_ex.client.screen.GuiEnvironmentalRadiationGenerator;
import committee.nova.mek_ex.client.screen.GuiPotionNebulizer;
import committee.nova.mek_ex.client.render.RenderPotionNebulizer;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import committee.nova.mek_ex.init.registry.MEXContainerTypes;
import committee.nova.mek_ex.init.registry.MEXGenTileEntityTypes;
import mekanism.client.ClientRegistrationUtil;
import mekanism.client.render.RenderPropertiesProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = MekEXMod.MOD_ID, value = Dist.CLIENT)
public class MEXClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MEXGenTileEntityTypes.BASIC_WIND_GENERATOR.get(), context -> new RenderWindGenerator<>(context, MekEXMod.rl("textures/block/wind_basic.png")));
        event.registerBlockEntityRenderer(MEXGenTileEntityTypes.ADVANCED_WIND_GENERATOR.get(), context -> new RenderWindGenerator<>(context, MekEXMod.rl("textures/block/wind_advanced.png")));
        event.registerBlockEntityRenderer(MEXGenTileEntityTypes.ELITE_WIND_GENERATOR.get(), context -> new RenderWindGenerator<>(context, MekEXMod.rl("textures/block/wind_elite.png")));
        event.registerBlockEntityRenderer(MEXGenTileEntityTypes.ULTIMATE_WIND_GENERATOR.get(), context -> new RenderWindGenerator<>(context, MekEXMod.rl("textures/block/wind_ultimate.png")));
        event.registerBlockEntityRenderer(MEXGenTileEntityTypes.ENVIRONMENTAL_RADIATION_GENERATOR.get(), RenderEnvironmentalRadiationGenerator::new);
        event.registerBlockEntityRenderer(MEXGenTileEntityTypes.POTION_NEBULIZER.get(), RenderPotionNebulizer::new);
        ClientRegistrationUtil.bindTileEntityRenderer(event, RenderNuclearControlTank::new,
              MEXGenTileEntityTypes.NUCLEAR_CONTROL_TANK, MEXGenTileEntityTypes.NUCLEAR_CONTROL_VALVE);
    }

    @SuppressWarnings("Convert2MethodRef")
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        ClientRegistrationUtil.registerScreen(event, MEXContainerTypes.BASIC_WIND_GENERATOR, GuiBasicWindGenerator::new);
        ClientRegistrationUtil.registerScreen(event, MEXContainerTypes.ADVANCED_WIND_GENERATOR, GuiAdvancedWindGenerator::new);
        ClientRegistrationUtil.registerScreen(event, MEXContainerTypes.ELITE_WIND_GENERATOR, GuiEliteWindGenerator::new);
        ClientRegistrationUtil.registerScreen(event, MEXContainerTypes.ULTIMATE_WIND_GENERATOR, GuiUltimateWindGenerator::new);
        ClientRegistrationUtil.registerScreen(event, MEXContainerTypes.NUCLEAR_CONTROL_TANK, GuiNuclearControlTank::new);
        ClientRegistrationUtil.registerScreen(event, MEXContainerTypes.NEUTRON_ACTIVATOR, GuiNeutronActivator::new);
        ClientRegistrationUtil.registerScreen(event, MEXContainerTypes.ENVIRONMENTAL_RADIATION_GENERATOR, GuiEnvironmentalRadiationGenerator::new);
        ClientRegistrationUtil.registerScreen(event, MEXContainerTypes.POTION_NEBULIZER, GuiPotionNebulizer::new);
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(RenderWindGeneratorItem.RENDERER);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new RenderPropertiesProvider.MekRenderProperties(RenderWindGeneratorItem.RENDERER),
              MEXBlocks.basic_wind_generator.getItemHolder(), MEXBlocks.advanced_wind_generator.getItemHolder(),
              MEXBlocks.elite_wind_generator.getItemHolder(), MEXBlocks.ultimate_wind_generator.getItemHolder());
    }
}
