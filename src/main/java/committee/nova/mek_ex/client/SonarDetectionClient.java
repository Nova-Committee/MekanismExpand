package committee.nova.mek_ex.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import committee.nova.mek_ex.common.content.gear.mekasuit.ModuleSonarDetectionUnit;
import committee.nova.mek_ex.init.registry.MEXModules;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public final class SonarDetectionClient {
    private static final Map<UUID, ScanData> SCANS = new ConcurrentHashMap<>();
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final int OUTLINE_R = 51;
    private static final int OUTLINE_G = 217;
    private static final int OUTLINE_B = 230;
    private static final int OUTLINE_A = 255;
    /** RGB used by vanilla glowing outline (`Entity#getTeamColor`). */
    public static final int OUTLINE_COLOR_RGB = (OUTLINE_R << 16) | (OUTLINE_G << 8) | OUTLINE_B;
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private static volatile Set<UUID> outlinedEntities = Set.of();

    private SonarDetectionClient() {
    }

    public static boolean isSonarOutlined(Entity entity) {
        return outlinedEntities.contains(entity.getUUID());
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        IModule<ModuleSonarDetectionUnit> module = IModuleHelper.INSTANCE.getModule(helmet, MEXModules.SONAR_DETECTION_UNIT);
        if (module == null || !module.isEnabled() || module.getContainerEnergy(helmet) < ModuleSonarDetectionUnit.ENERGY_PER_TICK) {
            clear(player.getUUID());
            return;
        }
        if (player.tickCount % SCAN_INTERVAL_TICKS != 0) {
            return;
        }
        ModuleSonarDetectionUnit custom = module.getCustomInstance();
        int side = ModuleSonarDetectionUnit.getSideLength(module.getInstalledCount());
        if (side <= 0 || custom == null) {
            clear(player.getUUID());
            return;
        }
        Level level = player.level();
        BlockPos center = player.blockPosition();
        int half = side / 2;
        int minX = center.getX() - half;
        int minZ = center.getZ() - half;
        int minY = center.getY() - side;
        int maxY = center.getY() + half;
        List<BlockPos> hits = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x < minX + side; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z < minZ + side; z++) {
                    cursor.set(x, y, z);
                    if (!level.isLoaded(cursor)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(cursor);
                    if (!state.isAir() && custom.matches(state)) {
                        hits.add(cursor.immutable());
                    }
                }
            }
        }
        AABB area = new AABB(minX, minY, minZ, minX + side, maxY + 1, minZ + side);
        List<Entity> entityHits = level.getEntities(player, area, custom::matches);
        Set<UUID> nextOutlined = new HashSet<>(entityHits.size());
        for (Entity entity : entityHits) {
            nextOutlined.add(entity.getUUID());
        }
        outlinedEntities = Set.copyOf(nextOutlined);
        SCANS.put(player.getUUID(), new ScanData(hits));
    }

    public static void clear(UUID playerId) {
        SCANS.remove(playerId);
        outlinedEntities = Set.of();
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }
        ScanData data = SCANS.get(player.getUUID());
        if (data == null || data.hits().isEmpty()) {
            return;
        }
        LevelRenderer levelRenderer = event.getLevelRenderer();
        if (!levelRenderer.shouldShowEntityOutlines()) {
            return;
        }

        OutlineBufferSource outlines = minecraft.renderBuffers().outlineBufferSource();
        outlines.setColor(OUTLINE_R, OUTLINE_G, OUTLINE_B, OUTLINE_A);
        VertexConsumer consumer = outlines.getBuffer(RenderType.outline(WHITE_TEXTURE));

        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = pose.last().pose();
        for (BlockPos pos : data.hits()) {
            renderBlockSilhouette(consumer, matrix, pos);
        }
        pose.popPose();

        levelRenderer.requestOutlineEffect();
    }

    private static void renderBlockSilhouette(VertexConsumer consumer, Matrix4f pose, BlockPos pos) {
        float x0 = pos.getX();
        float y0 = pos.getY();
        float z0 = pos.getZ();
        float x1 = x0 + 1.0F;
        float y1 = y0 + 1.0F;
        float z1 = z0 + 1.0F;

        vertex(consumer, pose, x0, y0, z0);
        vertex(consumer, pose, x0, y1, z0);
        vertex(consumer, pose, x0, y1, z1);
        vertex(consumer, pose, x0, y0, z1);

        vertex(consumer, pose, x1, y0, z1);
        vertex(consumer, pose, x1, y1, z1);
        vertex(consumer, pose, x1, y1, z0);
        vertex(consumer, pose, x1, y0, z0);

        vertex(consumer, pose, x0, y0, z1);
        vertex(consumer, pose, x1, y0, z1);
        vertex(consumer, pose, x1, y0, z0);
        vertex(consumer, pose, x0, y0, z0);

        vertex(consumer, pose, x0, y1, z0);
        vertex(consumer, pose, x1, y1, z0);
        vertex(consumer, pose, x1, y1, z1);
        vertex(consumer, pose, x0, y1, z1);

        vertex(consumer, pose, x1, y0, z0);
        vertex(consumer, pose, x1, y1, z0);
        vertex(consumer, pose, x0, y1, z0);
        vertex(consumer, pose, x0, y0, z0);

        vertex(consumer, pose, x0, y0, z1);
        vertex(consumer, pose, x0, y1, z1);
        vertex(consumer, pose, x1, y1, z1);
        vertex(consumer, pose, x1, y0, z1);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z) {
        consumer.addVertex(pose, x, y, z).setUv(0.0F, 0.0F);
    }

    private record ScanData(List<BlockPos> hits) {
        private ScanData {
            hits = hits == null ? List.of() : Collections.unmodifiableList(hits);
        }
    }
}
