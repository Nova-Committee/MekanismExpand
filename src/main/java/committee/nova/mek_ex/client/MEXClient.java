package committee.nova.mek_ex.client;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.client.render.RenderEnvironmentalRadiationGenerator;
import committee.nova.mek_ex.client.render.RenderWindGenerator;
import committee.nova.mek_ex.client.render.RenderNuclearControlTank;
import committee.nova.mek_ex.client.render.item.RenderElectricSkateboardItem;
import committee.nova.mek_ex.client.render.item.RenderWindGeneratorItem;
import committee.nova.mek_ex.client.screen.*;
import committee.nova.mek_ex.common.entity.EntityElectricSkateboard;
import committee.nova.mek_ex.common.network.ElectricSkateboardInputPayload;
import committee.nova.mek_ex.init.registry.MEXItems;

import committee.nova.mek_ex.client.render.RenderPotionNebulizer;
import committee.nova.mek_ex.client.render.RenderElectricSkateboard;
import committee.nova.mek_ex.client.model.ElectricSkateboardModel;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import committee.nova.mek_ex.init.registry.MEXContainerTypes;
import committee.nova.mek_ex.init.registry.MEXGenTileEntityTypes;
import committee.nova.mek_ex.init.registry.MEXEntityTypes;
import mekanism.client.ClientRegistrationUtil;
import mekanism.client.render.RenderPropertiesProvider;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MekEXMod.MOD_ID, value = Dist.CLIENT)
public class MEXClient {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ElectricSkateboardHud::render);
        NeoForge.EVENT_BUS.addListener(SonarDetectionClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(SonarDetectionClient::render);
    }

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
        event.registerEntityRenderer(MEXEntityTypes.ELECTRIC_SKATEBOARD.get(), RenderElectricSkateboard::new);
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
        ClientRegistrationUtil.registerScreen(event, MEXContainerTypes.ELECTRIC_SKATEBOARD, GuiElectricSkateboard::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ElectricSkateboardModel.LAYER_LOCATION, ElectricSkateboardModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(RenderWindGeneratorItem.RENDERER);
        event.registerReloadListener(RenderElectricSkateboardItem.RENDERER);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new RenderPropertiesProvider.MekRenderProperties(RenderWindGeneratorItem.RENDERER),
              MEXBlocks.basic_wind_generator.getItemHolder(), MEXBlocks.advanced_wind_generator.getItemHolder(),
              MEXBlocks.elite_wind_generator.getItemHolder(), MEXBlocks.ultimate_wind_generator.getItemHolder());
        event.registerItem(new RenderPropertiesProvider.MekRenderProperties(RenderElectricSkateboardItem.RENDERER),
              MEXItems.electric_skateboard);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof EntityElectricSkateboard bike)) {
            return;
        }

        boolean acceptsMovementInput = minecraft.screen == null;
        boolean forward = acceptsMovementInput && minecraft.options.keyUp.isDown();
        boolean back = acceptsMovementInput && minecraft.options.keyDown.isDown();
        boolean left = acceptsMovementInput && minecraft.options.keyLeft.isDown();
        boolean right = acceptsMovementInput && minecraft.options.keyRight.isDown();
        boolean jump = acceptsMovementInput && minecraft.options.keyJump.isDown();
        if (bike.getControllingPassenger() == minecraft.player) {
            bike.setLocalDriverInput(forward, back, left, right, jump);
            PacketDistributor.sendToServer(new ElectricSkateboardInputPayload(bike.getId(),
                    forward, back, left, right, jump));
        } else {
            bike.setLocalDriverInput(false, false, false, false, false);
        }
    }
}
