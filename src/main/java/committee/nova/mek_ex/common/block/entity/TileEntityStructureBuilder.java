package committee.nova.mek_ex.common.block.entity;

import committee.nova.mek_ex.common.structure.MultiblockBuildRecipe;
import committee.nova.mek_ex.common.structure.MultiblockRecipeIndex;
import committee.nova.mek_ex.common.structure.StructurePlacementEngine;
import committee.nova.mek_ex.common.structure.StructurePlacementEngine.PlaceResult;
import committee.nova.mek_ex.common.structure.StructurePlan;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.api.RelativeSide;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityStructureBuilder extends TileEntityConfigurableMachine {

    public static final long ENERGY_PER_BLOCK = 100L;
    public static final int BASE_BLOCKS_PER_TICK = 1;
    public static final int MATERIAL_SLOTS = 18;
    public static final int MATERIAL_SLOT_COLS = 9;
    public static final int MATERIAL_SLOT_ROWS = 2;

    public static final int ENERGY_SLOT_X = 152;
    public static final int ENERGY_SLOT_Y = 16;
    public static final int SIZE_PANEL_X = 7;
    public static final int SIZE_PANEL_Y = 70;
    public static final int SIZE_PANEL_W = 162;
    public static final int SIZE_PANEL_H = 18;
    public static final int SIZE_AXIS_W = 50;
    public static final int SIZE_AXIS_PAD = 6;
    public static final int SIZE_VISIBLE_AXES = 3;
    public static final int SIZE_TOTAL_AXES = 4;
    public static final int REQ_PANEL_X = 7;
    public static final int REQ_PANEL_Y = 100;
    public static final int REQ_PANEL_W = 162;
    public static final int REQ_PANEL_H = 28;
    public static final int REQ_VISIBLE_SLOTS = 7;
    public static final int REQ_SLOT_STEP = 20;
    public static final int MATERIAL_SLOT_X = 8;
    public static final int MATERIAL_SLOT_Y = 140;

    private MachineEnergyContainer<TileEntityStructureBuilder> energyContainer;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    private EnergyInventorySlot energySlot;
    private List<InputInventorySlot> materialSlots;

    private boolean running;
    private int progressIndex;
    private int rotationTurns;
    private int recipeIndex;
    private int sizeX = 5;
    private int sizeY = 5;
    private int sizeZ = 5;
    private int optionalCount;
    private int lastStatus;
    private int totalBlocks;
    private int placedBlocks;
    private int recipeCount;

    @Nullable
    private StructurePlan cachedPlan;
    private int cachedPlanKey = Integer.MIN_VALUE;

    public TileEntityStructureBuilder(BlockPos pos, BlockState state) {
        super(MEXBlocks.multiblocks_builder, pos, state);
        configComponent.setupItemIOConfig(new ArrayList<>(materialSlots), Collections.emptyList(), energySlot, true);
        ConfigInfo energyConfig = configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        energyConfig.addDisabledSides(RelativeSide.FRONT);
        ejectorComponent = new TileComponentEjector(this).setOutputData(configComponent, TransmissionType.ITEM);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this);
        materialSlots = new ArrayList<>();
        for (int row = 0; row < MATERIAL_SLOT_ROWS; row++) {
            for (int col = 0; col < MATERIAL_SLOT_COLS; col++) {
                InputInventorySlot slot = InputInventorySlot.at(listener,
                      MATERIAL_SLOT_X + col * 18,
                      MATERIAL_SLOT_Y + row * 18);
                materialSlots.add(builder.addSlot(slot));
            }
        }
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener,
              ENERGY_SLOT_X, ENERGY_SLOT_Y));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdate = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        recipeCount = MultiblockRecipeIndex.size(level);
        recipeIndex = MultiblockRecipeIndex.clampIndex(level, recipeIndex);
        clampSizesToRecipe();

        StructurePlan plan = getActivePlan();
        if (plan == null) {
            setActive(false);
            running = false;
            lastStatus = 5;
            totalBlocks = 0;
            return sendUpdate;
        }
        totalBlocks = plan.blockCount();

        if (!running) {
            setActive(false);
            if (progressIndex >= totalBlocks && totalBlocks > 0) {
                lastStatus = 4;
            } else if (lastStatus != 2 && lastStatus != 3) {
                lastStatus = 0;
            }
            return sendUpdate;
        }

        if (!canFunction()) {
            setActive(false);
            return sendUpdate;
        }

        long energyNeeded = energyContainer.getEnergyPerTick();
        if (energyContainer.getEnergy() < energyNeeded) {
            lastStatus = 6;
            setActive(false);
            return sendUpdate;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return sendUpdate;
        }

        Direction facing = getDirection();
        List<IInventorySlot> mats = new ArrayList<>(materialSlots);
        int attempts = 0;
        int maxAttempts = BASE_BLOCKS_PER_TICK;
        boolean didWork = false;

        while (attempts < maxAttempts && progressIndex < totalBlocks) {
            if (energyContainer.getEnergy() < energyNeeded) {
                lastStatus = 6;
                break;
            }
            PlaceResult result = StructurePlacementEngine.placeNext(
                  serverLevel, plan, worldPosition, facing, rotationTurns, progressIndex, mats, true
            );
            attempts++;
            switch (result) {
                case PLACED -> {
                    energyContainer.extract(energyNeeded, Action.EXECUTE, AutomationType.INTERNAL);
                    progressIndex++;
                    placedBlocks++;
                    didWork = true;
                    lastStatus = 1;
                }
                case SKIPPED -> {
                    progressIndex++;
                    lastStatus = 1;
                }
                case MISSING_MATERIAL -> {
                    lastStatus = 2;
                    running = false;
                    attempts = maxAttempts;
                }
                case BLOCKED -> {
                    lastStatus = 3;
                    running = false;
                    attempts = maxAttempts;
                }
                case DONE -> {
                    lastStatus = 4;
                    running = false;
                    attempts = maxAttempts;
                }
            }
        }

        if (progressIndex >= totalBlocks) {
            lastStatus = 4;
            running = false;
        }

        setActive(didWork);
        return sendUpdate || didWork;
    }

    @Nullable
    public StructurePlan getActivePlan() {
        MultiblockBuildRecipe recipe = getSelectedRecipe();
        if (recipe == null) {
            cachedPlan = null;
            cachedPlanKey = Integer.MIN_VALUE;
            return null;
        }
        int key = recipeIndex * 1_000_000 + sizeX * 10_000 + sizeY * 100 + sizeZ
              + optionalCount * 17 + (recipe.isParametric() ? 7 : 3);
        if (cachedPlan == null || cachedPlanKey != key) {
            try {
                cachedPlan = recipe.expand(sizeX, sizeY, sizeZ, optionalCount);
                cachedPlanKey = key;
            } catch (RuntimeException ex) {
                cachedPlan = null;
                cachedPlanKey = Integer.MIN_VALUE;
                return null;
            }
        }
        return cachedPlan;
    }

    public Direction getBuildFacing() {
        Direction facing = getDirection();
        return facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
    }

    public int getEffectiveRotationTurns() {
        return Math.floorMod(rotationTurns + StructurePlacementEngine.facingToTurns(getBuildFacing()), 4);
    }

    public AABB getPreviewBounds() {
        StructurePlan plan = getActivePlan();
        if (plan == null) {
            return null;
        }
        StructurePlan oriented = plan.rotateY(getEffectiveRotationTurns());
        return StructurePlacementEngine.previewBounds(worldPosition, getBuildFacing(), oriented);
    }

    private void invalidatePlanCache() {
        cachedPlan = null;
        cachedPlanKey = Integer.MIN_VALUE;
    }

    @Nullable
    public MultiblockBuildRecipe getSelectedRecipe() {
        RecipeHolder<MultiblockBuildRecipe> holder = MultiblockRecipeIndex.byIndex(level, recipeIndex);
        return holder == null ? null : holder.value();
    }

    @Nullable
    public RecipeHolder<MultiblockBuildRecipe> getSelectedHolder() {
        return MultiblockRecipeIndex.byIndex(level, recipeIndex);
    }

    public Component getSelectedRecipeName() {
        MultiblockBuildRecipe recipe = getSelectedRecipe();
        return recipe == null
              ? Component.translatable("gui.mek_ex.multiblocks_builder.no_recipe")
              : recipe.displayName();
    }

    public void handleGuiAction(int action, int value) {
        switch (action) {
            case 0 -> {
                if (running) {
                    running = false;
                    lastStatus = 0;
                } else {
                    StructurePlan plan = getActivePlan();
                    if (plan != null) {
                        running = true;
                        if (progressIndex >= plan.blockCount()) {
                            progressIndex = 0;
                            placedBlocks = 0;
                        }
                        lastStatus = 1;
                    }
                }
            }
            case 1 -> setSizeAxis(MultiblockBuildRecipe.SizeAxis.X, value);
            case 2 -> setSizeAxis(MultiblockBuildRecipe.SizeAxis.Y, value);
            case 3 -> setSizeAxis(MultiblockBuildRecipe.SizeAxis.Z, value);
            case 4 -> {
                rotationTurns = Math.floorMod(rotationTurns + value, 4);
                resetProgressKeepRecipe();
            }
            case 5 -> {
                running = false;
                lastStatus = 0;
                progressIndex = 0;
                placedBlocks = 0;
            }
            case 6 -> {
                int count = MultiblockRecipeIndex.size(level);
                if (count > 0) {
                    recipeIndex = Math.floorMod(recipeIndex + value, count);
                    applyRecipeDefaults();
                    resetProgressKeepRecipe();
                }
            }
            case 7 -> setOptionalCount(value);
            default -> {
            }
        }
        markForSave();
    }

    private void setOptionalCount(int value) {
        MultiblockBuildRecipe recipe = getSelectedRecipe();
        if (recipe == null || !recipe.hasOptionalCount()) {
            return;
        }
        int clamped = recipe.clampOptionalCount(sizeX, sizeY, sizeZ, value);
        if (clamped != optionalCount) {
            optionalCount = clamped;
            invalidatePlanCache();
            resetProgressKeepRecipe();
        }
    }

    private void setSizeAxis(MultiblockBuildRecipe.SizeAxis axis, int value) {
        MultiblockBuildRecipe recipe = getSelectedRecipe();
        if (recipe == null || !recipe.canChangeSize(axis)) {
            return;
        }
        int current = switch (axis) {
            case X -> sizeX;
            case Y -> sizeY;
            case Z -> sizeZ;
        };

        int clamped = recipe.clampSize(axis, value, Integer.compare(value, current));
        if (clamped == current) {
            return;
        }
        switch (axis) {
            case X -> sizeX = clamped;
            case Y -> sizeY = clamped;
            case Z -> sizeZ = clamped;
        }
        if (recipe.hasOptionalCount()) {
            optionalCount = recipe.clampOptionalCount(sizeX, sizeY, sizeZ, optionalCount);
        }
        invalidatePlanCache();
        resetProgressKeepRecipe();
    }

    private void applyRecipeDefaults() {
        MultiblockBuildRecipe recipe = getSelectedRecipe();
        if (recipe == null) {
            return;
        }
        sizeX = recipe.clampSize(MultiblockBuildRecipe.SizeAxis.X, recipe.defaultSizeX());
        sizeY = recipe.clampSize(MultiblockBuildRecipe.SizeAxis.Y, recipe.defaultSizeY());
        sizeZ = recipe.clampSize(MultiblockBuildRecipe.SizeAxis.Z, recipe.defaultSizeZ());
        optionalCount = recipe.defaultOptionalCount(sizeX, sizeY, sizeZ);
        invalidatePlanCache();
    }

    private void clampSizesToRecipe() {
        MultiblockBuildRecipe recipe = getSelectedRecipe();
        if (recipe == null) {
            return;
        }
        int nx = recipe.clampSize(MultiblockBuildRecipe.SizeAxis.X, sizeX);
        int ny = recipe.clampSize(MultiblockBuildRecipe.SizeAxis.Y, sizeY);
        int nz = recipe.clampSize(MultiblockBuildRecipe.SizeAxis.Z, sizeZ);
        int nOpt = recipe.hasOptionalCount()
              ? recipe.clampOptionalCount(nx, ny, nz, optionalCount)
              : recipe.defaultOptionalCount(nx, ny, nz);
        if (nx != sizeX || ny != sizeY || nz != sizeZ || nOpt != optionalCount) {
            sizeX = nx;
            sizeY = ny;
            sizeZ = nz;
            optionalCount = nOpt;
            invalidatePlanCache();
        }
    }

    public boolean canChangeSizeX() {
        MultiblockBuildRecipe recipe = getSelectedRecipe();
        return recipe != null && recipe.canChangeSizeX();
    }

    public boolean canChangeSizeY() {
        MultiblockBuildRecipe recipe = getSelectedRecipe();
        return recipe != null && recipe.canChangeSizeY();
    }

    public boolean canChangeSizeZ() {
        MultiblockBuildRecipe recipe = getSelectedRecipe();
        return recipe != null && recipe.canChangeSizeZ();
    }

    public boolean hasOptionalCount() {
        MultiblockBuildRecipe recipe = getSelectedRecipe();
        return recipe != null && recipe.hasOptionalCount();
    }

    public int getOptionalCount() {
        return optionalCount;
    }

    private void resetProgressKeepRecipe() {
        progressIndex = 0;
        placedBlocks = 0;
        running = false;
        if (getSelectedRecipe() != null) {
            lastStatus = 0;
        }
        invalidatePlanCache();
    }

    public boolean isOwnerOrTrusted(Player player) {
        return IBlockSecurityUtils.INSTANCE.canAccess(player, level, worldPosition, this);
    }

    public boolean isRunning() {
        return running;
    }

    public int getProgressIndex() {
        return progressIndex;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public int getPlacedBlocks() {
        return placedBlocks;
    }

    public int getLastStatus() {
        return lastStatus;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public int getRotationTurns() {
        return rotationTurns;
    }

    public int getRecipeIndex() {
        return recipeIndex;
    }

    public int getRecipeCount() {
        return recipeCount;
    }

    public boolean isParametricRecipe() {
        MultiblockBuildRecipe recipe = getSelectedRecipe();
        return recipe != null && recipe.isParametric();
    }

    public MachineEnergyContainer<TileEntityStructureBuilder> getEnergyContainer() {
        return energyContainer;
    }

    public Object2IntMap<Item> getRequiredMaterials() {
        StructurePlan plan = getActivePlan();
        if (plan == null) {
            return new Object2IntOpenHashMap<>();
        }
        return plan.materialCounts();
    }

    public int countMaterialInSlots(Item item) {
        if (materialSlots == null || item == null) {
            return 0;
        }
        int total = 0;
        for (InputInventorySlot slot : materialSlots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public List<InputInventorySlot> getMaterialSlots() {
        return materialSlots == null ? List.of() : Collections.unmodifiableList(materialSlots);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(() -> running, v -> running = v));
        container.track(SyncableInt.create(() -> progressIndex, v -> progressIndex = v));
        container.track(SyncableInt.create(() -> totalBlocks, v -> totalBlocks = v));
        container.track(SyncableInt.create(() -> placedBlocks, v -> placedBlocks = v));
        container.track(SyncableInt.create(() -> lastStatus, v -> lastStatus = v));
        container.track(SyncableInt.create(() -> sizeX, v -> sizeX = v));
        container.track(SyncableInt.create(() -> sizeY, v -> sizeY = v));
        container.track(SyncableInt.create(() -> sizeZ, v -> sizeZ = v));
        container.track(SyncableInt.create(() -> optionalCount, v -> {
            optionalCount = v;
            invalidatePlanCache();
        }));
        container.track(SyncableInt.create(() -> rotationTurns, v -> rotationTurns = v));
        container.track(SyncableInt.create(() -> recipeIndex, v -> {
            recipeIndex = v;
            invalidatePlanCache();
        }));
        container.track(SyncableInt.create(() -> recipeCount, v -> recipeCount = v));
    }

    @Override
    public void writeSustainedData(HolderLookup.Provider provider, CompoundTag data) {
        super.writeSustainedData(provider, data);
        data.putBoolean("running", running);
        data.putInt("progress", progressIndex);
        data.putInt("placed", placedBlocks);
        data.putInt("status", lastStatus);
        data.putInt("sizeX", sizeX);
        data.putInt("sizeY", sizeY);
        data.putInt("sizeZ", sizeZ);
        data.putInt("optionalCount", optionalCount);
        data.putInt("rotation", rotationTurns);
        data.putInt("recipeIndex", recipeIndex);
    }

    @Override
    public void readSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag data) {
        super.readSustainedData(provider, data);
        running = data.getBoolean("running");
        progressIndex = data.getInt("progress");
        placedBlocks = data.getInt("placed");
        lastStatus = data.getInt("status");
        sizeX = Mth.clamp(data.contains("sizeX") ? data.getInt("sizeX") : 5, 1, StructurePlan.MAX_SIZE);
        sizeY = Mth.clamp(data.contains("sizeY") ? data.getInt("sizeY") : 5, 1, StructurePlan.MAX_SIZE);
        sizeZ = Mth.clamp(data.contains("sizeZ") ? data.getInt("sizeZ") : 5, 1, StructurePlan.MAX_SIZE);
        optionalCount = Math.max(0, data.contains("optionalCount") ? data.getInt("optionalCount") : 0);
        rotationTurns = Math.floorMod(data.getInt("rotation"), 4);
        recipeIndex = Math.max(0, data.getInt("recipeIndex"));
        invalidatePlanCache();
    }
}
