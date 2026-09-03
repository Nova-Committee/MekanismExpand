package committee.nova.mek_ex.client.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class ESBLightUtil {
    private static final Map<Level, LevelLights> LIGHTS_BY_LEVEL = new WeakHashMap<>();

    private ESBLightUtil() {
    }

    public static void update(Level level, int sourceId, @Nullable BlockPos previousPos, BlockPos currentPos, int lightLevel) {
        requireClientLevel(level);
        if (lightLevel < 1 || lightLevel > 15) {
            throw new IllegalArgumentException("Dynamic light level must be between 1 and 15");
        }
        long current = currentPos.asLong();
        long previous = previousPos == null ? current : previousPos.asLong();
        boolean previousChanged;
        boolean currentChanged;
        synchronized (LIGHTS_BY_LEVEL) {
            LevelLights levelLights = LIGHTS_BY_LEVEL.computeIfAbsent(level, ignored -> new LevelLights());
            Long registeredPos = levelLights.sourcePositions.put(sourceId, current);
            if (registeredPos != null) {
                previous = registeredPos;
            }
            int previousLight = getMaximumLight(levelLights.sourcesByPosition.get(previous));
            if (previous != current) {
                removeSource(levelLights.sourcesByPosition, previous, sourceId);
            }
            int currentLight = getMaximumLight(levelLights.sourcesByPosition.get(current));
            levelLights.sourcesByPosition.computeIfAbsent(current, ignored -> new HashMap<>()).put(sourceId, lightLevel);
            previousChanged = previous != current && previousLight != getMaximumLight(levelLights.sourcesByPosition.get(previous));
            currentChanged = currentLight != getMaximumLight(levelLights.sourcesByPosition.get(current));
        }
        if (previousChanged) {
            checkBlock(level, BlockPos.of(previous));
        }
        if (currentChanged) {
            checkBlock(level, currentPos);
        }
    }

    public static void remove(Level level, int sourceId) {
        requireClientLevel(level);
        boolean changed = false;
        long removedPosition = 0L;
        synchronized (LIGHTS_BY_LEVEL) {
            LevelLights levelLights = LIGHTS_BY_LEVEL.get(level);
            if (levelLights == null) {
                return;
            }
            Long registeredPos = levelLights.sourcePositions.remove(sourceId);
            if (registeredPos == null) {
                return;
            }
            removedPosition = registeredPos;
            int previousLight = getMaximumLight(levelLights.sourcesByPosition.get(registeredPos));
            removeSource(levelLights.sourcesByPosition, registeredPos, sourceId);
            changed = previousLight != getMaximumLight(levelLights.sourcesByPosition.get(registeredPos));
            if (levelLights.sourcesByPosition.isEmpty()) {
                LIGHTS_BY_LEVEL.remove(level);
            }
        }
        if (changed) {
            checkBlock(level, BlockPos.of(removedPosition));
        }
    }

    public static int getLightEmission(BlockGetter level, BlockPos pos) {
        if (!(level instanceof Level actualLevel) || !actualLevel.isClientSide()) {
            return 0;
        }
        synchronized (LIGHTS_BY_LEVEL) {
            LevelLights levelLights = LIGHTS_BY_LEVEL.get(actualLevel);
            return levelLights == null ? 0 : getMaximumLight(levelLights.sourcesByPosition.get(pos.asLong()));
        }
    }

    public static void refresh(Level level, BlockPos pos) {
        requireClientLevel(level);
        checkBlock(level, pos);
    }

    private static void removeSource(Map<Long, Map<Integer, Integer>> levelLights, long packedPos, int sourceId) {
        Map<Integer, Integer> sources = levelLights.get(packedPos);
        if (sources != null) {
            sources.remove(sourceId);
            if (sources.isEmpty()) {
                levelLights.remove(packedPos);
            }
        }
    }

    private static int getMaximumLight(@Nullable Map<Integer, Integer> sources) {
        if (sources == null || sources.isEmpty()) {
            return 0;
        }
        int maximum = 0;
        for (int light : sources.values()) {
            maximum = Math.max(maximum, light);
        }
        return maximum;
    }

    private static void checkBlock(Level level, BlockPos pos) {
        level.getLightEngine().checkBlock(pos);
    }

    private static void requireClientLevel(Level level) {
        if (!level.isClientSide()) {
            throw new IllegalArgumentException("Electric skateboard dynamic lights are client-side only");
        }
    }

    private static final class LevelLights {
        private final Map<Long, Map<Integer, Integer>> sourcesByPosition = new HashMap<>();
        private final Map<Integer, Long> sourcePositions = new HashMap<>();
    }
}
