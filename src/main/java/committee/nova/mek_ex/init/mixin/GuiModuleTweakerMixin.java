package committee.nova.mek_ex.init.mixin;

import committee.nova.mek_ex.client.gui.GuiSonarFiltersWindow;
import committee.nova.mek_ex.common.content.sonar.SonarFilter;
import committee.nova.mek_ex.common.gear.config.ModuleSonarFiltersConfig;
import committee.nova.mek_ex.init.enums.MEXLang;
import committee.nova.mek_ex.init.registry.MEXModules;
import java.util.List;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiModuleTweaker;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.custom.module.GuiModuleScreen;
import mekanism.common.content.gear.Module;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiModuleTweaker.class)
public abstract class GuiModuleTweakerMixin extends GuiMekanism<ModuleTweakerContainer> {
    @Shadow(remap = false)
    private GuiModuleScreen moduleScreen;
    @Shadow(remap = false)
    private int selected;

    @Unique
    private TranslationButton mek_ex$sonarFilterButton;

    protected GuiModuleTweakerMixin() {
        super(null, null, null);
    }

    @Inject(method = "addGuiElements", at = @At("TAIL"), remap = false)
    private void mek_ex$addSonarFilterButton(CallbackInfo ci) {
        mek_ex$sonarFilterButton = addRenderableWidget(new TranslationButton(this, 152, 158, 104, 16, MEXLang.SONAR_CONFIGURE_FILTERS, (element, mouseX, mouseY) -> {
            mek_ex$openSonarFilters();
            return true;
        }));
        mek_ex$sonarFilterButton.active = false;
        mek_ex$sonarFilterButton.visible = false;
    }

    @Inject(method = "onModuleSelected", at = @At("TAIL"), remap = false)
    private void mek_ex$updateSonarFilterButton(Module<?> module, CallbackInfo ci) {
        boolean sonar = module != null && module.getUntypedData() == MEXModules.SONAR_DETECTION_UNIT.get();
        if (mek_ex$sonarFilterButton != null) {
            mek_ex$sonarFilterButton.visible = sonar;
            mek_ex$sonarFilterButton.active = sonar;
        }
    }

    @Unique
    private void mek_ex$openSonarFilters() {
        if (moduleScreen == null || selected < 0) {
            return;
        }
        IModule<?> module = moduleScreen.getCurrentModule();
        if (module == null || module.getUntypedData() != MEXModules.SONAR_DETECTION_UNIT.get()) {
            return;
        }
        ModuleConfig<List<SonarFilter<?>>> config = module.getConfig(ModuleSonarFiltersConfig.NAME);
        if (config == null) {
            return;
        }
        Slot slot = menu.slots.get(selected);
        int slotIndex = slot.getSlotIndex();
        addWindow(new GuiSonarFiltersWindow(this, (getXSize() - 176) / 2, 20, module, slotIndex, config.get(), updated -> {
        }));
    }
}
