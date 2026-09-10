package committee.nova.mek_ex.common.structure;

import committee.nova.mek_ex.common.multiblock.MekanismHeartTemplate;
import committee.nova.mek_ex.common.multiblock.MekanismHeartTemplate.HeartPart;
import committee.nova.mek_ex.init.registry.MEXBlocks;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class StructureAssemblers {

    public static final byte[][] SPS_GRID = {
          {0, 0, 1, 1, 1, 0, 0},
          {0, 1, 2, 2, 2, 1, 0},
          {1, 2, 2, 2, 2, 2, 1},
          {1, 2, 2, 2, 2, 2, 1},
          {1, 2, 2, 2, 2, 2, 1},
          {0, 1, 2, 2, 2, 1, 0},
          {0, 0, 1, 1, 1, 0, 0}
    };

    public static final byte[][] FUSION_GRID = {
          {0, 0, 1, 0, 0},
          {0, 1, 2, 1, 0},
          {1, 2, 2, 2, 1},
          {0, 1, 2, 1, 0},
          {0, 0, 1, 0, 0}
    };

    public static final int SPS_MAX_COILS = 6;
    public static final int TURBINE_BLADES_PER_COIL = 4;

    private StructureAssemblers() {
    }

    public static StructurePlan hollowCuboid(
          int sizeX, int sizeY, int sizeZ,
          BlockState frame, BlockState wall, BlockState face,
          List<MultiblockBuildRecipe.PortSpec> ports
    ) {
        List<StructureBlockEntry> entries = new ArrayList<>();
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    boolean onX = x == 0 || x == sizeX - 1;
                    boolean onY = y == 0 || y == sizeY - 1;
                    boolean onZ = z == 0 || z == sizeZ - 1;
                    if (!onX && !onY && !onZ) {
                        continue;
                    }
                    boolean edge = (onX && onY) || (onX && onZ) || (onY && onZ);
                    BlockState state = edge ? frame : (face != null ? face : wall);
                    entries.add(entry(x, y, z, state));
                }
            }
        }
        applyPorts(entries, sizeX, sizeY, sizeZ, ports);
        return plan(sizeX, sizeY, sizeZ, entries);
    }

    public static StructurePlan faceGridShell(
          int size,
          byte[][] grid,
          BlockState frame,
          BlockState wall,
          List<MultiblockBuildRecipe.PortSpec> ports,
          List<StructureBlockEntry> extras
    ) {
        if (grid.length != size) {
            throw new IllegalArgumentException("face grid height must match size");
        }
        List<StructureBlockEntry> entries = new ArrayList<>();
        for (int y = 0; y < size; y++) {
            for (int z = 0; z < size; z++) {
                for (int x = 0; x < size; x++) {
                    boolean onX = x == 0 || x == size - 1;
                    boolean onY = y == 0 || y == size - 1;
                    boolean onZ = z == 0 || z == size - 1;
                    int faces = (onX ? 1 : 0) + (onY ? 1 : 0) + (onZ ? 1 : 0);
                    if (faces == 0) {
                        continue;
                    }
                    int requirement = maxFaceRequirement(grid, size, x, y, z, onX, onY, onZ);
                    if (requirement <= 0) {
                        continue;
                    }
                    BlockState state = requirement == 1 ? frame : wall;
                    entries.add(entry(x, y, z, state));
                }
            }
        }
        applyPorts(entries, size, size, size, ports);
        for (StructureBlockEntry extra : extras) {
            replaceOrAdd(entries, extra.x(), extra.y(), extra.z(), extra.state(), extra.auxCount(), extra.auxItem());
        }
        return plan(size, size, size, entries);
    }

    private static int maxFaceRequirement(byte[][] grid, int size, int x, int y, int z,
          boolean onX, boolean onY, boolean onZ) {
        int best = 0;
        if (onX) {
            best = Math.max(best, gridValue(grid, z, y));
        }
        if (onY) {
            best = Math.max(best, gridValue(grid, x, z));
        }
        if (onZ) {
            best = Math.max(best, gridValue(grid, x, y));
        }
        return best;
    }

    private static int gridValue(byte[][] grid, int a, int b) {
        if (a < 0 || b < 0 || b >= grid.length || a >= grid[b].length) {
            return 0;
        }
        return grid[b][a] & 0xFF;
    }

    public static StructurePlan sps(Block coilBlock, Block wallBlock, int coilCount) {
        Block casing = block("mekanism:sps_casing");
        Block port = block("mekanism:sps_port");
        BlockState frame = casing.defaultBlockState();
        BlockState wall = wallBlock.defaultBlockState();
        int coilsWanted = Mth.clamp(coilCount, 0, SPS_MAX_COILS);
        int c = 3;
        Direction[] portFaces = {
              Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP
        };
        StructureBlockEntry[] coilSlots = {
              entry(c, c, 1, facing(coilBlock, Direction.SOUTH)),
              entry(c, c, 5, facing(coilBlock, Direction.NORTH)),
              entry(1, c, c, facing(coilBlock, Direction.EAST)),
              entry(5, c, c, facing(coilBlock, Direction.WEST)),
              entry(c, 1, c, facing(coilBlock, Direction.UP)),
              entry(c, 5, c, facing(coilBlock, Direction.DOWN))
        };
        List<MultiblockBuildRecipe.PortSpec> ports = new ArrayList<>(Math.max(coilsWanted, 1));
        List<StructureBlockEntry> coils = new ArrayList<>(coilsWanted);
        for (int i = 0; i < coilsWanted; i++) {
            ports.add(new MultiblockBuildRecipe.PortSpec(portFaces[i], port));
            coils.add(coilSlots[i]);
        }
        if (coilsWanted == 0) {
            ports.add(new MultiblockBuildRecipe.PortSpec(Direction.NORTH, port));
        }
        return faceGridShell(7, SPS_GRID, frame, wall, ports, coils);
    }

    public static StructurePlan sps(Block coilBlock, Block wallBlock) {
        return sps(coilBlock, wallBlock, SPS_MAX_COILS);
    }

    public static StructurePlan fusionReactor() {
        Block frameB = block("mekanismgenerators:fusion_reactor_frame");
        Block port = block("mekanismgenerators:fusion_reactor_port");
        Block controller = block("mekanismgenerators:fusion_reactor_controller");
        Block laser = block("mekanismgenerators:laser_focus_matrix");
        Block glass = block("mekanismgenerators:reactor_glass");
        BlockState frame = frameB.defaultBlockState();
        BlockState wall = glass.defaultBlockState();
        List<MultiblockBuildRecipe.PortSpec> ports = List.of(
              new MultiblockBuildRecipe.PortSpec(Direction.NORTH, port),
              new MultiblockBuildRecipe.PortSpec(Direction.SOUTH, port),
              new MultiblockBuildRecipe.PortSpec(Direction.EAST, port),
              new MultiblockBuildRecipe.PortSpec(Direction.WEST, port)
        );
        List<StructureBlockEntry> extras = new ArrayList<>();
        extras.add(entry(2, 4, 2, controller.defaultBlockState()));
        extras.add(entry(2, 0, 2, laser.defaultBlockState()));
        return faceGridShell(5, FUSION_GRID, frame, wall, ports, extras);
    }

    public static int maxTurbineRotors(int sizeX, int sizeY, int sizeZ) {
        int sx = oddClamp(sizeX, 5, 17);
        int sy = Mth.clamp(sizeY, 5, 18);
        int sz = oddClamp(sizeZ, 5, 17);
        int innerX = sx - 2;
        int innerY = sy - 2;
        int innerZ = sz - 2;
        int innerRadius = (Math.min(innerX, innerZ) - 1) / 2;
        return Math.max(1, Math.min((innerRadius + 1) * 4 - 3, innerY - 2));
    }

    public static StructurePlan industrialTurbine(int sizeX, int sizeY, int sizeZ) {
        return industrialTurbine(sizeX, sizeY, sizeZ, -1);
    }

    public static StructurePlan industrialTurbine(int sizeX, int sizeY, int sizeZ, int rotorCountHint) {
        sizeX = oddClamp(sizeX, 5, 17);
        sizeZ = oddClamp(sizeZ, 5, 17);
        sizeY = Mth.clamp(sizeY, 5, 18);

        Block casing = block("mekanismgenerators:turbine_casing");
        Block valve = block("mekanismgenerators:turbine_valve");
        Block vent = block("mekanismgenerators:turbine_vent");
        Block rotor = block("mekanismgenerators:turbine_rotor");
        Block complex = block("mekanismgenerators:rotational_complex");
        Block coil = block("mekanismgenerators:electromagnetic_coil");
        Block condenser = block("mekanismgenerators:saturating_condenser");
        Block disperser = block("mekanism:pressure_disperser");
        Item blade = item("mekanismgenerators:turbine_blade");

        BlockState frame = casing.defaultBlockState();
        BlockState wall = casing.defaultBlockState();
        BlockState ventState = vent.defaultBlockState();
        BlockState coilState = coil.defaultBlockState();
        BlockState condenserState = condenser.defaultBlockState();
        BlockState disperserState = disperser.defaultBlockState();

        int cx = (sizeX - 1) / 2;
        int cz = (sizeZ - 1) / 2;
        int maxRotors = maxTurbineRotors(sizeX, sizeY, sizeZ);
        int rotors = rotorCountHint < 0 ? maxRotors : Mth.clamp(rotorCountHint, 1, maxRotors);
        int complexY = rotors + 1;
        int coilStartY = complexY + 1;

        List<StructureBlockEntry> entries = new ArrayList<>();
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    boolean onX = x == 0 || x == sizeX - 1;
                    boolean onY = y == 0 || y == sizeY - 1;
                    boolean onZ = z == 0 || z == sizeZ - 1;
                    if (!onX && !onY && !onZ) {
                        continue;
                    }
                    boolean edge = (onX && onY) || (onX && onZ) || (onY && onZ);
                    BlockState state = edge ? frame : wall;
                    if (!edge && onY && y == sizeY - 1) {
                        state = ventState;
                    } else if (!edge && y >= complexY && (onX || onZ)) {
                        state = ventState;
                    }
                    entries.add(entry(x, y, z, state));
                }
            }
        }

        int valveY = Mth.clamp(Math.max(1, complexY / 2), 1, Math.max(1, complexY - 1));
        replaceOrAdd(entries, cx, valveY, 0, valve.defaultBlockState(), 0, Items.AIR);
        replaceOrAdd(entries, cx, valveY, sizeZ - 1, valve.defaultBlockState(), 0, Items.AIR);
        replaceOrAdd(entries, 0, valveY, cz, valve.defaultBlockState(), 0, Items.AIR);
        replaceOrAdd(entries, sizeX - 1, valveY, cz, valve.defaultBlockState(), 0, Items.AIR);

        for (int y = 1; y <= rotors; y++) {
            entries.add(new StructureBlockEntry(cx, y, cz, rotor.defaultBlockState(), 2, blade));
        }

        for (int x = 1; x < sizeX - 1; x++) {
            for (int z = 1; z < sizeZ - 1; z++) {
                if (x == cx && z == cz) {
                    entries.add(entry(x, complexY, z, complex.defaultBlockState()));
                } else {
                    entries.add(entry(x, complexY, z, disperserState));
                }
            }
        }

        int blades = rotors * 2;
        int neededCoils = Math.max(1, (blades + TURBINE_BLADES_PER_COIL - 1) / TURBINE_BLADES_PER_COIL);
        placeTurbineCoilsAndCondensers(entries, sizeX, sizeY, sizeZ, cx, cz, coilStartY,
              neededCoils, coilState, condenserState);

        return plan(sizeX, sizeY, sizeZ, entries);
    }

    private static void placeTurbineCoilsAndCondensers(
          List<StructureBlockEntry> entries,
          int sizeX, int sizeY, int sizeZ,
          int cx, int cz, int startY,
          int neededCoils,
          BlockState coilState, BlockState condenserState
    ) {
        List<int[]> upperSlots = new ArrayList<>();
        for (int y = startY; y < sizeY - 1; y++) {
            for (int ring = 0; ring <= Math.max(sizeX, sizeZ); ring++) {
                for (int x = 1; x < sizeX - 1; x++) {
                    for (int z = 1; z < sizeZ - 1; z++) {
                        if (Math.max(Math.abs(x - cx), Math.abs(z - cz)) != ring) {
                            continue;
                        }
                        upperSlots.add(new int[]{x, y, z});
                    }
                }
            }
        }
        int coilsPlaced = 0;
        for (int[] slot : upperSlots) {
            if (coilsPlaced < neededCoils) {
                entries.add(entry(slot[0], slot[1], slot[2], coilState));
                coilsPlaced++;
            } else {
                entries.add(entry(slot[0], slot[1], slot[2], condenserState));
            }
        }
        if (coilsPlaced == 0) {
            entries.add(entry(cx, startY, cz, coilState));
        }
    }

    public static StructurePlan fissionReactor(int sizeX, int sizeY, int sizeZ) {
        sizeX = Mth.clamp(sizeX, 3, 18);
        sizeY = Mth.clamp(sizeY, 4, 18);
        sizeZ = Mth.clamp(sizeZ, 3, 18);
        Block casing = block("mekanismgenerators:fission_reactor_casing");
        Block port = block("mekanismgenerators:fission_reactor_port");
        Block glass = block("mekanismgenerators:reactor_glass");
        Block fuel = block("mekanismgenerators:fission_fuel_assembly");
        Block rod = block("mekanismgenerators:control_rod_assembly");

        List<StructureBlockEntry> entries = new ArrayList<>();
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    boolean onX = x == 0 || x == sizeX - 1;
                    boolean onY = y == 0 || y == sizeY - 1;
                    boolean onZ = z == 0 || z == sizeZ - 1;
                    if (!onX && !onY && !onZ) {
                        continue;
                    }
                    boolean edge = (onX && onY) || (onX && onZ) || (onY && onZ);
                    BlockState state = edge || onY ? casing.defaultBlockState() : glass.defaultBlockState();
                    entries.add(entry(x, y, z, state));
                }
            }
        }
        applyPorts(entries, sizeX, sizeY, sizeZ, List.of(
              new MultiblockBuildRecipe.PortSpec(Direction.NORTH, port),
              new MultiblockBuildRecipe.PortSpec(Direction.SOUTH, port),
              new MultiblockBuildRecipe.PortSpec(Direction.EAST, port),
              new MultiblockBuildRecipe.PortSpec(Direction.WEST, port)
        ));

        int pillarMax = sizeY - 3;
        if (pillarMax >= 1) {
            for (int z = 1; z < sizeZ - 1; z++) {
                boolean evenZ = z % 2 == 0;
                for (int x = 1; x < sizeX - 1; x++) {
                    if (evenZ != (x % 2 == 0)) {
                        continue;
                    }
                    for (int y = 1; y <= pillarMax; y++) {
                        entries.add(entry(x, y, z, fuel.defaultBlockState()));
                    }
                    entries.add(entry(x, pillarMax + 1, z, rod.defaultBlockState()));
                }
            }
        }
        return plan(sizeX, sizeY, sizeZ, entries);
    }

    public static int maxBoilerHeaters(int sizeX, int sizeY, int sizeZ) {
        int sx = Mth.clamp(sizeX, 3, 18);
        int sy = Mth.clamp(sizeY, 4, 18);
        int sz = Mth.clamp(sizeZ, 3, 18);
        int innerX = sx - 2;
        int innerZ = sz - 2;
        int innerY = sy - 2;
        int adjustable = Math.max(0, innerY - 2);
        return Math.max(1, (adjustable + 1) * innerX * innerZ);
    }

    public static StructurePlan thermoelectricBoiler(int sizeX, int sizeY, int sizeZ) {
        return thermoelectricBoiler(sizeX, sizeY, sizeZ, -1);
    }

    public static StructurePlan thermoelectricBoiler(int sizeX, int sizeY, int sizeZ, int heaterCountHint) {
        sizeX = Mth.clamp(sizeX, 3, 18);
        sizeY = Mth.clamp(sizeY, 4, 18);
        sizeZ = Mth.clamp(sizeZ, 3, 18);
        Block casing = block("mekanism:boiler_casing");
        Block valve = block("mekanism:boiler_valve");
        Block glass = block("mekanism:structural_glass");
        Block disperser = block("mekanism:pressure_disperser");
        Block heater = block("mekanism:superheating_element");

        List<StructureBlockEntry> entries = new ArrayList<>();
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    boolean onX = x == 0 || x == sizeX - 1;
                    boolean onY = y == 0 || y == sizeY - 1;
                    boolean onZ = z == 0 || z == sizeZ - 1;
                    if (!onX && !onY && !onZ) {
                        continue;
                    }
                    boolean edge = (onX && onY) || (onX && onZ) || (onY && onZ);
                    BlockState state = edge || onY ? casing.defaultBlockState() : glass.defaultBlockState();
                    entries.add(entry(x, y, z, state));
                }
            }
        }
        applyPorts(entries, sizeX, sizeY, sizeZ, List.of(
              new MultiblockBuildRecipe.PortSpec(Direction.NORTH, valve),
              new MultiblockBuildRecipe.PortSpec(Direction.SOUTH, valve),
              new MultiblockBuildRecipe.PortSpec(Direction.EAST, valve),
              new MultiblockBuildRecipe.PortSpec(Direction.WEST, valve)
        ));

        int innerX = sizeX - 2;
        int innerZ = sizeZ - 2;
        int maxHeaters = maxBoilerHeaters(sizeX, sizeY, sizeZ);
        int heatersWanted = heaterCountHint < 0 ? maxHeaters : Mth.clamp(heaterCountHint, 1, maxHeaters);
        int layersNeeded = Math.max(1, (heatersWanted + innerX * innerZ - 1) / (innerX * innerZ));
        int maxWaterLayers = Math.max(1, sizeY - 3);
        layersNeeded = Math.min(layersNeeded, maxWaterLayers);
        int dispY = layersNeeded + 1;

        for (int x = 1; x < sizeX - 1; x++) {
            for (int z = 1; z < sizeZ - 1; z++) {
                entries.add(entry(x, dispY, z, disperser.defaultBlockState()));
            }
        }

        int placed = 0;
        outer:
        for (int y = 1; y < dispY; y++) {
            for (int x = 1; x < sizeX - 1; x++) {
                for (int z = 1; z < sizeZ - 1; z++) {
                    if (placed >= heatersWanted) {
                        break outer;
                    }
                    entries.add(entry(x, y, z, heater.defaultBlockState()));
                    placed++;
                }
            }
        }
        return plan(sizeX, sizeY, sizeZ, entries);
    }

    public static int maxMatrixInterior(int sizeX, int sizeY, int sizeZ) {
        int sx = Mth.clamp(sizeX, 3, 18);
        int sy = Mth.clamp(sizeY, 3, 18);
        int sz = Mth.clamp(sizeZ, 3, 18);
        return Math.max(0, (sx - 2) * (sy - 2) * (sz - 2));
    }

    public static StructurePlan inductionMatrix(int sizeX, int sizeY, int sizeZ) {
        return inductionMatrix(sizeX, sizeY, sizeZ, 0);
    }

    public static StructurePlan inductionMatrix(int sizeX, int sizeY, int sizeZ, int interiorCount) {
        sizeX = Mth.clamp(sizeX, 3, 18);
        sizeY = Mth.clamp(sizeY, 3, 18);
        sizeZ = Mth.clamp(sizeZ, 3, 18);
        Block casing = block("mekanism:induction_casing");
        Block port = block("mekanism:induction_port");
        Block cell = block("mekanism:ultimate_induction_cell");
        Block provider = block("mekanism:ultimate_induction_provider");
        Block glass = block("mekanism:structural_glass");

        List<StructureBlockEntry> entries = new ArrayList<>();
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    boolean onX = x == 0 || x == sizeX - 1;
                    boolean onY = y == 0 || y == sizeY - 1;
                    boolean onZ = z == 0 || z == sizeZ - 1;
                    if (!onX && !onY && !onZ) {
                        continue;
                    }
                    boolean edge = (onX && onY) || (onX && onZ) || (onY && onZ);
                    BlockState state = edge || onY ? casing.defaultBlockState() : glass.defaultBlockState();
                    entries.add(entry(x, y, z, state));
                }
            }
        }
        applyPorts(entries, sizeX, sizeY, sizeZ, List.of(
              new MultiblockBuildRecipe.PortSpec(Direction.NORTH, port),
              new MultiblockBuildRecipe.PortSpec(Direction.SOUTH, port)
        ));

        int maxInterior = maxMatrixInterior(sizeX, sizeY, sizeZ);
        int toPlace = Mth.clamp(interiorCount, 0, maxInterior);
        int placed = 0;
        outer:
        for (int y = 1; y < sizeY - 1; y++) {
            for (int z = 1; z < sizeZ - 1; z++) {
                for (int x = 1; x < sizeX - 1; x++) {
                    if (placed >= toPlace) {
                        break outer;
                    }
                    boolean providerPos = (x + z + y) % 3 == 0;
                    entries.add(entry(x, y, z, (providerPos ? provider : cell).defaultBlockState()));
                    placed++;
                }
            }
        }
        return plan(sizeX, sizeY, sizeZ, entries);
    }

    public static StructurePlan thermalEvaporation(int height) {
        int sizeX = 4;
        int sizeZ = 4;
        int sizeY = Mth.clamp(height, 3, 18);
        Block block = block("mekanism:thermal_evaporation_block");
        Block valve = block("mekanism:thermal_evaporation_valve");
        Block controller = block("mekanism:thermal_evaporation_controller");

        List<StructureBlockEntry> entries = new ArrayList<>();
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    boolean onX = x == 0 || x == sizeX - 1;
                    boolean onZ = z == 0 || z == sizeZ - 1;
                    boolean onBottom = y == 0;
                    boolean onTop = y == sizeY - 1;
                    if (onTop) {
                        boolean onEdge = onX || onZ;
                        boolean corner = onX && onZ;
                        if (!onEdge || corner) {
                            continue;
                        }
                        entries.add(entry(x, y, z, block.defaultBlockState()));
                        continue;
                    }
                    if (onBottom || onX || onZ) {
                        entries.add(entry(x, y, z, block.defaultBlockState()));
                    }
                }
            }
        }
        replaceOrAdd(entries, 1, sizeY - 1, 0, controller.defaultBlockState(), 0, Items.AIR);
        replaceOrAdd(entries, 2, sizeY - 1, 0, block.defaultBlockState(), 0, Items.AIR);
        replaceOrAdd(entries, 0, Math.max(1, sizeY / 2), 1, valve.defaultBlockState(), 0, Items.AIR);
        replaceOrAdd(entries, sizeX - 1, Math.max(1, sizeY / 2), 1, valve.defaultBlockState(), 0, Items.AIR);
        return plan(sizeX, sizeY, sizeZ, entries);
    }

    public static StructurePlan dynamicTank(int sizeX, int sizeY, int sizeZ, boolean useGlass) {
        Block tank = block("mekanism:dynamic_tank");
        Block valve = block("mekanism:dynamic_valve");
        Block glass = block("mekanism:structural_glass");
        BlockState frame = tank.defaultBlockState();
        BlockState face = useGlass ? glass.defaultBlockState() : tank.defaultBlockState();
        return hollowCuboid(sizeX, sizeY, sizeZ, frame, frame, face, List.of(
              new MultiblockBuildRecipe.PortSpec(Direction.NORTH, valve),
              new MultiblockBuildRecipe.PortSpec(Direction.SOUTH, valve)
        ));
    }

    public static StructurePlan nuclearControlTank(int sizeX, int sizeY, int sizeZ, boolean useGlass) {
        Block tank = MEXBlocks.nuclear_control_tank.get();
        Block valve = MEXBlocks.nuclear_control_valve.get();
        Block glass = block("mekanism:structural_glass");
        BlockState frame = tank.defaultBlockState();
        BlockState face = useGlass ? glass.defaultBlockState() : tank.defaultBlockState();
        return hollowCuboid(sizeX, sizeY, sizeZ, frame, frame, face, List.of(
              new MultiblockBuildRecipe.PortSpec(Direction.NORTH, valve),
              new MultiblockBuildRecipe.PortSpec(Direction.SOUTH, valve)
        ));
    }

    public static StructurePlan mekanismHeart() {
        Block antimatter = MEXBlocks.block_antimatter.get();
        Block glass = block("mekanism:structural_glass");
        Block cell = block("mekanism:ultimate_induction_cell");
        Block provider = block("mekanism:ultimate_induction_provider");
        int sx = MekanismHeartTemplate.SIZE_X;
        int sy = MekanismHeartTemplate.SIZE_Y;
        int sz = MekanismHeartTemplate.SIZE_Z;
        List<StructureBlockEntry> entries = new ArrayList<>();
        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                for (int x = 0; x < sx; x++) {
                    HeartPart part = MekanismHeartTemplate.getPart(x, y, z);
                    BlockState state = switch (part) {
                        case AIR -> null;
                        case CONTROLLER, CASING -> antimatter.defaultBlockState();
                        case GLASS -> glass.defaultBlockState();
                        case CELL -> cell.defaultBlockState();
                        case PROVIDER -> provider.defaultBlockState();
                    };
                    if (state != null) {
                        entries.add(entry(x, y, z, state));
                    }
                }
            }
        }
        return plan(sx, sy, sz, entries);
    }

    private static StructurePlan plan(int sx, int sy, int sz, List<StructureBlockEntry> entries) {
        if (entries.size() > StructurePlan.MAX_BLOCKS) {
            throw new IllegalArgumentException("Structure exceeds max blocks: " + entries.size());
        }
        return new StructurePlan(sx, sy, sz, entries);
    }

    private static StructureBlockEntry entry(int x, int y, int z, BlockState state) {
        return new StructureBlockEntry(x, y, z, state, 0, Items.AIR);
    }

    private static BlockState facing(Block block, Direction dir) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.setValue(BlockStateProperties.FACING, dir);
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, dir);
        }
        return state;
    }

    static void applyPorts(List<StructureBlockEntry> entries, int sizeX, int sizeY, int sizeZ,
          List<MultiblockBuildRecipe.PortSpec> ports) {
        for (MultiblockBuildRecipe.PortSpec port : ports) {
            int px;
            int py;
            int pz;
            switch (port.face()) {
                case DOWN -> {
                    px = sizeX / 2;
                    py = 0;
                    pz = sizeZ / 2;
                }
                case UP -> {
                    px = sizeX / 2;
                    py = sizeY - 1;
                    pz = sizeZ / 2;
                }
                case NORTH -> {
                    px = sizeX / 2;
                    py = sizeY / 2;
                    pz = 0;
                }
                case SOUTH -> {
                    px = sizeX / 2;
                    py = sizeY / 2;
                    pz = sizeZ - 1;
                }
                case WEST -> {
                    px = 0;
                    py = sizeY / 2;
                    pz = sizeZ / 2;
                }
                case EAST -> {
                    px = sizeX - 1;
                    py = sizeY / 2;
                    pz = sizeZ / 2;
                }
                default -> {
                    continue;
                }
            }
            replaceOrAdd(entries, px, py, pz, port.block().defaultBlockState(), 0, Items.AIR);
        }
    }

    static void replaceOrAdd(List<StructureBlockEntry> entries, int x, int y, int z, BlockState state, int aux, Item auxItem) {
        for (int i = 0; i < entries.size(); i++) {
            StructureBlockEntry e = entries.get(i);
            if (e.x() == x && e.y() == y && e.z() == z) {
                entries.set(i, new StructureBlockEntry(x, y, z, state, aux, auxItem == null ? Items.AIR : auxItem));
                return;
            }
        }
        entries.add(new StructureBlockEntry(x, y, z, state, aux, auxItem == null ? Items.AIR : auxItem));
    }

    private static int oddClamp(int v, int min, int max) {
        int c = Mth.clamp(v, min, max);
        if (c % 2 == 0) {
            c = c > min ? c - 1 : c + 1;
        }
        return Mth.clamp(c, min, max);
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id))
              .filter(b -> b != Blocks.AIR)
              .orElseThrow(() -> new IllegalStateException("Missing block for structure recipe: " + id));
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id))
              .filter(i -> i != Items.AIR)
              .orElseThrow(() -> new IllegalStateException("Missing item for structure recipe: " + id));
    }

    public static Object2IntMap<Item> materialCounts(StructurePlan plan) {
        Object2IntOpenHashMap<Item> counts = new Object2IntOpenHashMap<>();
        for (StructureBlockEntry entry : plan.blocks()) {
            Item item = entry.state().getBlock().asItem();
            if (item != null && item != Items.AIR) {
                counts.addTo(item, 1);
            }
            if (entry.auxCount() > 0 && entry.auxItem() != null && entry.auxItem() != Items.AIR) {
                counts.addTo(entry.auxItem(), entry.auxCount());
            }
        }
        return counts;
    }
}
