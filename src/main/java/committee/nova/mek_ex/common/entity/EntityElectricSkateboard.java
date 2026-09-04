package committee.nova.mek_ex.common.entity;

import committee.nova.mek_ex.MekEXMod;
import committee.nova.mek_ex.client.util.ESBLightUtil;
import committee.nova.mek_ex.init.registry.MEXItems;
import committee.nova.mek_ex.init.registry.MEXContainerTypes;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.registries.MekanismDataSerializers;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.security.EntitySecurityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EntityElectricSkateboard extends Entity implements HasCustomInventoryScreen, IMekanismInventory, IMekanismStrictEnergyHandler, ISecurityObject {
    public static final float BOARD_LENGTH = 35.0F / 16.0F;
    public static final float BOARD_WIDTH = 10.0F / 16.0F;
    public static final float BOARD_HEIGHT = 3.0F / 16.0F;
    public static final long MAX_ENERGY = 1_000_000L;
    public static final int MIN_GEAR = 1;
    public static final int MAX_GEAR = 3;
    private static final int PART_COUNT = 5;
    private static final double ACCELERATION = 0.035D;
    private static final double BRAKING = 0.14D;
    private static final double COASTING_DAMPING = 0.91D;
    private static final float STEERING_DEGREES_PER_TICK = 4.5F;
    private static final long INPUT_TIMEOUT_TICKS = 5L;
    private static final int MAX_GLIDE_TICKS = 60;
    private static final double GLIDE_FALL_SPEED = -0.025D;
    private static final int DYNAMIC_LIGHT_LEVEL = 14;
    private static final int DYNAMIC_LIGHT_REFRESH_INTERVAL = 20;
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(EntityElectricSkateboard.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<SecurityMode> SECURITY = SynchedEntityData.defineId(EntityElectricSkateboard.class, MekanismDataSerializers.SECURITY.value());
    private static final EntityDataAccessor<Float> STEERING = SynchedEntityData.defineId(EntityElectricSkateboard.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> ENERGY = SynchedEntityData.defineId(EntityElectricSkateboard.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> GEAR = SynchedEntityData.defineId(EntityElectricSkateboard.class, EntityDataSerializers.INT);

    private final BasicEnergyContainer energyContainer = BasicEnergyContainer.input(MAX_ENERGY, this);
    private final EnergyInventorySlot batterySlot = EnergyInventorySlot.fill(energyContainer, this, 136, 32);
    private final List<IInventorySlot> inventorySlots = List.of(batterySlot);
    private final ElectricSkateboardPart[] parts = new ElectricSkateboardPart[PART_COUNT];
    private UUID owner;
    private SecurityMode securityMode = SecurityMode.PUBLIC;
    private boolean inputForward;
    private boolean inputBack;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputJump;
    private long lastInputTick = Long.MIN_VALUE;
    private UUID inputDriver;
    private int glideTicks;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private double serverLastX;
    private double serverLastZ;
    private boolean serverPositionInitialized;
    @Nullable
    private BlockPos dynamicLightPos;

    public EntityElectricSkateboard(EntityType<? extends EntityElectricSkateboard> type, Level level) {
        super(type, level);
        noCulling = false;
        for (int i = 0; i < PART_COUNT; i++) {
            parts[i] = new ElectricSkateboardPart(this, BOARD_WIDTH, BOARD_HEIGHT);
        }
        setYRot(0);
        setBoundingBox(makeBoundingBox());
        updatePartPositions();
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < parts.length; i++) {
            parts[i].setId(id + i + 1);
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return parts;
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        float half = BOARD_WIDTH * 0.5F;
        return new AABB(getX() - half, getY(), getZ() - half, getX() + half, getY() + BOARD_HEIGHT, getZ() + half);
    }

    @Override
    public float getPickRadius() {
        return (BOARD_LENGTH - BOARD_WIDTH) * 0.5F;
    }

    @Override
    public void setYRot(float yRot) {
        float previous = getYRot();
        super.setYRot(yRot);
        if (previous != yRot) {
            setBoundingBox(makeBoundingBox());
            updatePartPositions();
        }
    }

    @Override
    public void absMoveTo(double x, double y, double z, float yRot, float xRot) {
        super.absMoveTo(x, y, z, yRot, xRot);
        setBoundingBox(makeBoundingBox());
        updatePartPositions();
    }

    private void updatePartPositions() {
        float yaw = getYRot() * Mth.DEG_TO_RAD;
        double forwardX = -Mth.sin(yaw);
        double forwardZ = Mth.cos(yaw);
        double first = -BOARD_LENGTH * 0.5D + BOARD_WIDTH * 0.5D;
        double last = BOARD_LENGTH * 0.5D - BOARD_WIDTH * 0.5D;
        for (int i = 0; i < parts.length; i++) {
            ElectricSkateboardPart part = parts[i];
            double t = parts.length == 1 ? 0.5D : (double) i / (parts.length - 1);
            double along = first + (last - first) * t;
            double px = getX() + forwardX * along;
            double py = getY();
            double pz = getZ() + forwardZ * along;
            part.xo = part.getX();
            part.yo = part.getY();
            part.zo = part.getZ();
            part.setPos(px, py, pz);
        }
    }

    @Override
    public void move(@NotNull MoverType type, @NotNull Vec3 requested) {
        if (noPhysics) {
            setPos(getX() + requested.x, getY() + requested.y, getZ() + requested.z);
            updatePartPositions();
            return;
        }
        if (type == MoverType.PISTON) {
            requested = limitPistonMovement(requested);
            if (requested.equals(Vec3.ZERO)) {
                return;
            }
        }
        if (stuckSpeedMultiplier.lengthSqr() > 1.0E-7) {
            requested = requested.multiply(stuckSpeedMultiplier);
            stuckSpeedMultiplier = Vec3.ZERO;
            setDeltaMovement(Vec3.ZERO);
        }

        Vec3 allowed = collideOriented(requested);
        if (allowed.lengthSqr() > 1.0E-7) {
            setPos(getX() + allowed.x, getY() + allowed.y, getZ() + allowed.z);
            updatePartPositions();
        }

        boolean hitX = !Mth.equal(requested.x, allowed.x);
        boolean hitZ = !Mth.equal(requested.z, allowed.z);
        horizontalCollision = hitX || hitZ;
        verticalCollision = requested.y != allowed.y;
        verticalCollisionBelow = verticalCollision && requested.y < 0.0D;
        minorHorizontalCollision = false;
        setOnGroundWithMovement(verticalCollisionBelow, allowed);

        if (horizontalCollision) {
            Vec3 delta = getDeltaMovement();
            setDeltaMovement(hitX ? 0.0D : delta.x, delta.y, hitZ ? 0.0D : delta.z);
        }

        BlockPos onPos = getOnPosLegacy();
        BlockState onState = level().getBlockState(onPos);
        checkFallDamage(allowed.y, onGround(), onState, onPos);
        if (!isRemoved()) {
            if (requested.y != allowed.y) {
                onState.getBlock().updateEntityAfterFallOn(level(), this);
            }
            if (onGround()) {
                onState.getBlock().stepOn(level(), onPos, onState, this);
            }
            tryCheckInsideBlocks();
        }
    }

    private void applySteeringYaw(float deltaYaw) {
        float oldYaw = getYRot();
        float newYaw = Mth.wrapDegrees(oldYaw + deltaYaw);
        if (!intersectsSolid(getX(), getY(), getZ(), oldYaw) && intersectsSolid(getX(), getY(), getZ(), newYaw)) {
            float low = 0.0F;
            float high = 1.0F;
            for (int i = 0; i < 8; i++) {
                float mid = (low + high) * 0.5F;
                float testYaw = Mth.wrapDegrees(oldYaw + deltaYaw * mid);
                if (intersectsSolid(getX(), getY(), getZ(), testYaw)) {
                    high = mid;
                } else {
                    low = mid;
                }
            }
            newYaw = Mth.wrapDegrees(oldYaw + deltaYaw * low);
        }
        setYRot(newYaw);
        setYBodyRot(newYaw);
    }

    private Vec3 collideOriented(Vec3 motion) {
        double startX = getX();
        double startY = getY();
        double startZ = getZ();
        float yaw = getYRot();

        double moveY = clipAxis(startX, startY, startZ, yaw, 0.0D, motion.y, 0.0D);
        double moveX = clipAxis(startX, startY + moveY, startZ, yaw, motion.x, 0.0D, 0.0D);
        double moveZ = clipAxis(startX + moveX, startY + moveY, startZ, yaw, 0.0D, 0.0D, motion.z);

        if (maxUpStep() > 0.0F
              && (moveX != motion.x || moveZ != motion.z)
              && (onGround() || moveY != motion.y && motion.y < 0.0D)) {
            double step = maxUpStep();
            double up = clipAxis(startX, startY, startZ, yaw, 0.0D, step, 0.0D);
            double stepX = clipAxis(startX, startY + up, startZ, yaw, motion.x, 0.0D, 0.0D);
            double stepZ = clipAxis(startX + stepX, startY + up, startZ, yaw, 0.0D, 0.0D, motion.z);
            double down = clipAxis(startX + stepX, startY + up, startZ + stepZ, yaw, 0.0D, -up, 0.0D);
            double steppedHoriz = stepX * stepX + stepZ * stepZ;
            double flatHoriz = moveX * moveX + moveZ * moveZ;
            if (steppedHoriz > flatHoriz) {
                moveX = stepX;
                moveY = up + down;
                moveZ = stepZ;
            }
        }
        return new Vec3(moveX, moveY, moveZ);
    }

    private double clipAxis(double x, double y, double z, float yaw, double dx, double dy, double dz) {
        double move = dx != 0.0D ? dx : dy != 0.0D ? dy : dz;
        if (move == 0.0D) {
            return 0.0D;
        }
        if (!intersectsSolid(x, y, z, yaw) && intersectsSolid(x + dx, y + dy, z + dz, yaw)) {
            double low = 0.0D;
            double high = 1.0D;
            for (int i = 0; i < 8; i++) {
                double mid = (low + high) * 0.5D;
                if (intersectsSolid(x + dx * mid, y + dy * mid, z + dz * mid, yaw)) {
                    high = mid;
                } else {
                    low = mid;
                }
            }
            move *= low;
        } else if (intersectsSolid(x + dx, y + dy, z + dz, yaw)) {
            return 0.0D;
        }
        return move;
    }

    private boolean intersectsSolid(double x, double y, double z, float yaw) {
        AABB search = orientedBoardAabb(x, y, z, yaw).inflate(1.0E-4D);
        int minX = Mth.floor(search.minX);
        int maxX = Mth.floor(search.maxX);
        int minY = Mth.floor(search.minY);
        int maxY = Mth.floor(search.maxY);
        int minZ = Mth.floor(search.minZ);
        int maxZ = Mth.floor(search.maxZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    cursor.set(bx, by, bz);
                    BlockState state = level().getBlockState(cursor);
                    if (state.isAir()) {
                        continue;
                    }
                    VoxelShape shape = state.getCollisionShape(level(), cursor);
                    if (shape.isEmpty()) {
                        continue;
                    }
                    for (AABB localBox : shape.toAabbs()) {
                        if (obbIntersectsAabb(x, y, z, yaw, localBox.move(cursor))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Yaw-oriented board OBB vs block AABB via separating axes (world X/Y/Z + board right/forward). */
    private static boolean obbIntersectsAabb(double x, double y, double z, float yawDegrees, AABB block) {
        double halfLength = BOARD_LENGTH * 0.5D;
        double halfWidth = BOARD_WIDTH * 0.5D;
        double halfHeight = BOARD_HEIGHT * 0.5D;
        double boardCY = y + halfHeight;

        double aabbCX = (block.minX + block.maxX) * 0.5D;
        double aabbCY = (block.minY + block.maxY) * 0.5D;
        double aabbCZ = (block.minZ + block.maxZ) * 0.5D;
        double extentX = (block.maxX - block.minX) * 0.5D;
        double extentY = (block.maxY - block.minY) * 0.5D;
        double extentZ = (block.maxZ - block.minZ) * 0.5D;

        double dx = x - aabbCX;
        double dy = boardCY - aabbCY;
        double dz = z - aabbCZ;

        float yaw = yawDegrees * Mth.DEG_TO_RAD;
        double forwardX = -Mth.sin(yaw);
        double forwardZ = Mth.cos(yaw);
        double rightX = Mth.cos(yaw);
        double rightZ = Mth.sin(yaw);

        if (Math.abs(dx) > extentX + halfWidth * Math.abs(rightX) + halfLength * Math.abs(forwardX)) {
            return false;
        }
        if (Math.abs(dy) > extentY + halfHeight) {
            return false;
        }
        if (Math.abs(dz) > extentZ + halfWidth * Math.abs(rightZ) + halfLength * Math.abs(forwardZ)) {
            return false;
        }
        if (Math.abs(dx * rightX + dz * rightZ) > extentX * Math.abs(rightX) + extentZ * Math.abs(rightZ) + halfWidth) {
            return false;
        }
        if (Math.abs(dx * forwardX + dz * forwardZ) > extentX * Math.abs(forwardX) + extentZ * Math.abs(forwardZ) + halfLength) {
            return false;
        }
        return true;
    }

    private static AABB orientedBoardAabb(double x, double y, double z, float yRotDegrees) {
        float yaw = yRotDegrees * Mth.DEG_TO_RAD;
        double forwardX = -Mth.sin(yaw);
        double forwardZ = Mth.cos(yaw);
        double rightX = Mth.cos(yaw);
        double rightZ = Mth.sin(yaw);
        double halfLength = BOARD_LENGTH * 0.5D;
        double halfWidth = BOARD_WIDTH * 0.5D;
        double minX = x;
        double maxX = x;
        double minZ = z;
        double maxZ = z;
        for (int lengthSign = -1; lengthSign <= 1; lengthSign += 2) {
            for (int widthSign = -1; widthSign <= 1; widthSign += 2) {
                double cornerX = x + forwardX * halfLength * lengthSign + rightX * halfWidth * widthSign;
                double cornerZ = z + forwardZ * halfLength * lengthSign + rightZ * halfWidth * widthSign;
                minX = Math.min(minX, cornerX);
                maxX = Math.max(maxX, cornerX);
                minZ = Math.min(minZ, cornerZ);
                maxZ = Math.max(maxZ, cornerZ);
            }
        }
        return new AABB(minX, y, minZ, maxX, y + BOARD_HEIGHT, maxZ);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, Optional.empty());
        builder.define(SECURITY, SecurityMode.PUBLIC);
        builder.define(STEERING, 0F);
        builder.define(ENERGY, 0L);
        builder.define(GEAR, 1);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        if (tag.hasUUID("Owner")) setOwnerUUID(tag.getUUID("Owner"));
        if (tag.contains("Security", 8)) {
            try { setSecurityMode(SecurityMode.valueOf(tag.getString("Security").toUpperCase())); }
            catch (IllegalArgumentException ex) { throw new IllegalStateException("Invalid electric skateboard security mode", ex); }
        }
        energyContainer.setEnergy(tag.getLong("Energy"));
        syncEnergyData();
        batterySlot.deserializeNBT(registryAccess(), tag.getCompound("Battery"));
        if (tag.contains("Gear")) {
            setGear(tag.getInt("Gear"));
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putString("Security", securityMode.getSerializedName());
        tag.putLong("Energy", energyContainer.getEnergy());
        tag.put("Battery", batterySlot.serializeNBT(registryAccess()));
        tag.putInt("Gear", getGear());
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (ENERGY.equals(key) && level().isClientSide) {
            energyContainer.setEnergy(entityData.get(ENERGY));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            updateDynamicLight();
        }
        tickLerp();
        if (!level().isClientSide) {
            tickServerState();
        } else if (isControlledByLocalInstance()) {
            tickLocalPhysics();
        } else {
            setDeltaMovement(Vec3.ZERO);
        }
        setBoundingBox(makeBoundingBox());
        updatePartPositions();
    }

    private void tickServerState() {
        batterySlot.fillContainer();
        if (!serverPositionInitialized) {
            serverLastX = getX();
            serverLastZ = getZ();
            serverPositionInitialized = true;
        }
        double movedSqr = Math.pow(getX() - serverLastX, 2) + Math.pow(getZ() - serverLastZ, 2);
        serverLastX = getX();
        serverLastZ = getZ();
        Entity controller = getPassengers().isEmpty() ? null : getPassengers().get(0);
        if (controller == null) {
            clearDriverInput();
            Vec3 motion = getDeltaMovement();
            if (!onGround()) {
                motion = motion.add(0, -0.08D, 0);
            }
            motion = new Vec3(motion.x * 0.94D, motion.y, motion.z * 0.94D);
            setDeltaMovement(motion);
            move(MoverType.SELF, motion);
            return;
        }
        if (!controller.getUUID().equals(inputDriver)
              || lastInputTick == Long.MIN_VALUE || level().getGameTime() - lastInputTick > INPUT_TIMEOUT_TICKS) {
            clearDriverInput();
        }
        long driveCost = getDriveEnergyPerTick();
        if (controller instanceof Player player && canAccess(player)
              && energyContainer.getEnergy() >= driveCost
              && (inputForward || inputBack)
              && movedSqr > 0.0001D) {
            energyContainer.extract(driveCost, Action.EXECUTE, AutomationType.INTERNAL);
        }
        if (onGround()) {
            glideTicks = 0;
            setDeltaMovement(Vec3.ZERO);
        } else if (inputJump && glideTicks < MAX_GLIDE_TICKS) {
            glideTicks++;
        } else {
            setDeltaMovement(Vec3.ZERO);
        }
    }

    private void tickLocalPhysics() {
        Entity controller = getPassengers().isEmpty() ? null : getPassengers().get(0);
        if (controller == null) {
            Vec3 current = getDeltaMovement();
            setDeltaMovement(current.x * 0.94D, current.y, current.z * 0.94D);
        } else if (controller instanceof Player player && canAccess(player)) {
            moveFromInput();
        } else {
            setDeltaMovement(getDeltaMovement().scale(0.75));
        }
        if (onGround()) {
            glideTicks = 0;
        } else {
            setDeltaMovement(getDeltaMovement().add(0, -0.08D, 0));
            if (inputJump && getDeltaMovement().y < 0 && glideTicks < MAX_GLIDE_TICKS) {
                Vec3 motion = getDeltaMovement();
                setDeltaMovement(motion.x, Math.max(motion.y, GLIDE_FALL_SPEED), motion.z);
                glideTicks++;
            }
        }
        Vec3 motion = getDeltaMovement();
        double horizontalSpeed = motion.horizontalDistance();
        double maxForward = getMaxForwardSpeed();
        if (horizontalSpeed > maxForward) {
            double scale = maxForward / horizontalSpeed;
            setDeltaMovement(motion.x * scale, motion.y, motion.z * scale);
        }
        move(MoverType.SELF, getDeltaMovement());
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYRot = yRot;
        lerpXRot = xRot;
        lerpSteps = 10;
    }

    @Override
    public double lerpTargetX() {
        return lerpSteps > 0 ? lerpX : getX();
    }

    @Override
    public double lerpTargetY() {
        return lerpSteps > 0 ? lerpY : getY();
    }

    @Override
    public double lerpTargetZ() {
        return lerpSteps > 0 ? lerpZ : getZ();
    }

    @Override
    public float lerpTargetYRot() {
        return lerpSteps > 0 ? (float) lerpYRot : getYRot();
    }

    @Override
    public float lerpTargetXRot() {
        return lerpSteps > 0 ? (float) lerpXRot : getXRot();
    }

    private void tickLerp() {
        if (isControlledByLocalInstance()) {
            lerpSteps = 0;
            syncPacketPositionCodec(getX(), getY(), getZ());
        } else if (lerpSteps > 0) {
            lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, lerpXRot);
            lerpSteps--;
        }
    }

    private boolean moveFromInput() {
        Vec3 current = getDeltaMovement();
        double horizontalSpeed = Math.sqrt(current.x * current.x + current.z * current.z);
        double maxForward = getMaxForwardSpeed();
        double maxReverse = getMaxReverseSpeed();

        // Positive yaw turns right in Minecraft; A (left) must decrease yaw.
        float steering = (inputRight ? 1F : 0F) - (inputLeft ? 1F : 0F);
        entityData.set(STEERING, steering);
        if (steering != 0F) {
            float speedFactor = (float) Mth.clamp(horizontalSpeed / maxForward, 0D, 1D);
            float turn = STEERING_DEGREES_PER_TICK * (0.35F + speedFactor * 0.65F);
            applySteeringYaw(steering * turn);
        }

        Vec3 heading = getForward();
        double signedSpeed = current.x * heading.x + current.z * heading.z;
        boolean hasDriveEnergy = energyContainer.getEnergy() >= getDriveEnergyPerTick();
        float throttleInput = hasDriveEnergy ? (inputForward ? 1F : 0F) - (inputBack ? 1F : 0F) : 0F;
        boolean throttle = throttleInput != 0F;
        double targetSpeed = throttleInput > 0F ? maxForward : throttleInput < 0F ? -maxReverse : 0D;
        double nextSpeed;
        if (throttle) {
            boolean reversingDirection = signedSpeed != 0D && Math.signum(signedSpeed) != Math.signum(targetSpeed);
            double step = reversingDirection ? BRAKING : ACCELERATION;
            nextSpeed = approach(signedSpeed, targetSpeed, step);
        } else {
            nextSpeed = signedSpeed * COASTING_DAMPING;
        }
        nextSpeed = Mth.clamp(nextSpeed, -maxReverse, maxForward);

        setDeltaMovement(heading.x * nextSpeed, current.y, heading.z * nextSpeed);
        hasImpulse = true;
        return Math.abs(nextSpeed) > 0.01D;
    }

    public int getGear() {
        return entityData.get(GEAR);
    }

    public void setGear(int gear) {
        entityData.set(GEAR, Mth.clamp(gear, MIN_GEAR, MAX_GEAR));
    }

    public void cycleGear(int delta) {
        setGear(getGear() + delta);
    }

    public int getMaxSpeedKmh() {
        return switch (getGear()) {
            case 1 -> 35;
            case 2 -> 55;
            default -> 75;
        };
    }

    public double getMaxForwardSpeed() {
        return getMaxSpeedKmh() / 72.0D;
    }

    public double getMaxReverseSpeed() {
        return getMaxForwardSpeed() * 0.55D;
    }

    public long getDriveEnergyPerTick() {
        return switch (getGear()) {
            case 1 -> 40L;
            case 2 -> 80L;
            default -> 150L;
        };
    }

    private static double approach(double value, double target, double step) {
        if (value < target) return Math.min(value + step, target);
        if (value > target) return Math.max(value - step, target);
        return target;
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        if (getPassengers().isEmpty()) {
            return null;
        }
        Entity passenger = getPassengers().get(0);
        return passenger instanceof LivingEntity living ? living : null;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return super.isControlledByLocalInstance();
    }


    public void setDriverInput(Player sender, boolean forward, boolean back, boolean left, boolean right, boolean jump) {
        if (level().isClientSide || getControllingPassenger() != sender) {
            MekEXMod.LOGGER.warn("Rejected driver input for electric skateboard {}", getId());
            return;
        }
        inputForward = forward;
        inputBack = back;
        inputLeft = left;
        inputRight = right;
        inputJump = jump;
        entityData.set(STEERING, (right ? 1F : 0F) - (left ? 1F : 0F));
        inputDriver = sender.getUUID();
        lastInputTick = level().getGameTime();
    }

    public void setLocalDriverInput(boolean forward, boolean back, boolean left, boolean right, boolean jump) {
        if (!level().isClientSide) {
            return;
        }
        inputForward = forward;
        inputBack = back;
        inputLeft = left;
        inputRight = right;
        inputJump = jump;
        entityData.set(STEERING, (right ? 1F : 0F) - (left ? 1F : 0F));
    }

    private void clearDriverInput() {
        inputForward = false;
        inputBack = false;
        inputLeft = false;
        inputRight = false;
        inputJump = false;
        entityData.set(STEERING, 0F);
        inputDriver = null;
        lastInputTick = Long.MIN_VALUE;
    }

    public float getSteeringInput() {
        return entityData.get(STEERING);
    }

    @Override
    protected void positionRider(@NotNull Entity passenger, @NotNull MoveFunction callback) {
        int index = getPassengers().indexOf(passenger);
        double offset = index == 0 ? 0.0D : -0.55D;
        Vec3 pos = position().add(0, 0.05D, 0).add(getForward().scale(offset));
        callback.accept(passenger, pos.x, pos.y, pos.z);
        clampPassengerRotation(passenger);
    }

    private void clampPassengerRotation(Entity passenger) {
        passenger.setYBodyRot(getYRot());
        float delta = Mth.wrapDegrees(passenger.getYRot() - getYRot());
        float clamped = Mth.clamp(delta, -105.0F, 105.0F);
        passenger.yRotO += clamped - delta;
        passenger.setYRot(passenger.getYRot() + clamped - delta);
        passenger.setYHeadRot(passenger.getYRot());
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger) { return getPassengers().size() < 2; }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSpectator()) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            if (!level().isClientSide && canAccess(player)) openConfigurationMenu(player);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!level().isClientSide && !canAccess(player)) {
            ISecurityUtils.INSTANCE.displayNoAccess(player);
            return InteractionResult.FAIL;
        }
        if (!level().isClientSide) player.startRiding(this);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean isPickable() { return true; }

    @Override
    public float maxUpStep() {
        return 1.0F;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (level().isClientSide) {
            return;
        }
        if (!canAccess(player)) {
            ISecurityUtils.INSTANCE.displayNoAccess(player);
            return;
        }
        if (!isVehicle() || hasPassenger(player)) {
            openConfigurationMenu(player);
        }
    }

    public void openConfigurationMenu(Player player) {
        var provider = MEXContainerTypes.ELECTRIC_SKATEBOARD.getProvider(net.minecraft.network.chat.Component.translatable("container.mek_ex.electric_skateboard"), this, true);
        if (provider != null) player.openMenu(provider, buf -> buf.writeVarInt(getId()));
    }

    public boolean canAccess(Player player) {
        return ISecurityUtils.INSTANCE.canAccessObject(player, this);
    }

    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public void addContainerTrackers(mekanism.common.inventory.container.MekanismContainer container) {
        container.track(mekanism.common.inventory.container.sync.SyncableLong.create(energyContainer::getEnergy, energyContainer::setEnergy));
        container.track(mekanism.common.inventory.container.sync.SyncableInt.create(this::getGear, this::setGear));
    }

    @Override public @Nullable UUID getOwnerUUID() { return entityData.get(OWNER).orElse(owner); }
    @Override public @Nullable String getOwnerName() { return owner == null ? null : level().getPlayerByUUID(owner) == null ? null : level().getPlayerByUUID(owner).getName().getString(); }
    @Override public void setOwnerUUID(@Nullable UUID owner) { this.owner = owner; entityData.set(OWNER, Optional.ofNullable(owner)); }
    @Override public SecurityMode getSecurityMode() { return entityData.get(SECURITY); }
    @Override public void setSecurityMode(SecurityMode mode) { if (mode == null) throw new IllegalArgumentException("Security mode cannot be null"); SecurityMode old = getSecurityMode(); securityMode = mode; entityData.set(SECURITY, mode); if (old != mode && !level().isClientSide) EntitySecurityUtils.get().securityChanged(java.util.Set.of(), this, old, mode); }
    @Override
    public void onContentsChanged() {
        syncEnergyData();
    }
    @Override public List<IInventorySlot> getInventorySlots(@Nullable Direction side) { return inventorySlots; }
    @Override public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) { return List.of(energyContainer); }
    public void loadItemData(CompoundTag tag) { readAdditionalSaveData(tag); }

    private ItemStack createDropStack() {
        ItemStack stack = MEXItems.electric_skateboard.toStack();
        CompoundTag tag = new CompoundTag();
        addAdditionalSaveData(tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    private void updateDynamicLight() {
        if (energyContainer.getEnergy() <= 0) {
            clearDynamicLight();
            return;
        }

        BlockPos current = blockPosition().above().immutable();
        ESBLightUtil.update(level(), getId(), dynamicLightPos, current, DYNAMIC_LIGHT_LEVEL);
        dynamicLightPos = current;
        if (tickCount % DYNAMIC_LIGHT_REFRESH_INTERVAL == 0) {
            ESBLightUtil.refresh(level(), current);
        }
    }

    private void clearDynamicLight() {
        dynamicLightPos = null;
        if (level().isClientSide) {
            ESBLightUtil.remove(level(), getId());
        }
    }

    private void syncEnergyData() {
        if (!level().isClientSide) {
            entityData.set(ENERGY, energyContainer.getEnergy());
        }
    }

    @Override
    public void onClientRemoval() {
        clearDynamicLight();
        super.onClientRemoval();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (level().isClientSide) {
            clearDynamicLight();
        }
        super.remove(reason);
    }

    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!level().isClientSide && source.getEntity() instanceof Player player && !canAccess(player)) return false;
        if (!level().isClientSide) {
            ItemStack drop = createDropStack();
            net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(level(), getX(), getY() + 0.3, getZ(), drop);
            level().addFreshEntity(item);
            discard();
        }
        return true;
    }

}
