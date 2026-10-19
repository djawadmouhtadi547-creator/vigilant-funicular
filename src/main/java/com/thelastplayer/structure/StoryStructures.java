package com.thelastplayer.structure;

import com.thelastplayer.registry.ModBlocks;
import com.thelastplayer.registry.ModItems;
import com.thelastplayer.story.StoryData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

/**
 * All the builders: the secret device room (before the story), the two hidden sector rooms (day 2)
 * and the abandoned lab (day 3). Every builder is deterministic from the stored positions so an
 * interrupted build can simply be re-queued (see resume()).
 */
public final class StoryStructures {
    private static final Random RNG = new Random();

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState BRICK = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState MOSSY = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    private static final BlockState CRACKED = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
    private static final BlockState MOSSY_COBBLE = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    private static final BlockState PBB = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState IRON = Blocks.IRON_BLOCK.defaultBlockState();
    private static final BlockState GLASS_BLUE = Blocks.BLUE_STAINED_GLASS.defaultBlockState();
    private static final BlockState GLASS_DEAD = Blocks.BLACK_STAINED_GLASS.defaultBlockState();
    private static final BlockState COBWEB = Blocks.COBWEB.defaultBlockState();
    private static final BlockState BOOKSHELF = Blocks.BOOKSHELF.defaultBlockState();

    private StoryStructures() {}

    // ------------------------------------------------------------------ site picking
    private static BlockPos groundAt(ServerWorld w, int x, int z) {
        w.getChunk(x >> 4, z >> 4); // forces load/generation
        int h = w.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, h - 1, z);
    }

    /** Ground block (top solid block) of a good dry site, at the given angle/distance from center. */
    private static BlockPos pickSite(ServerWorld w, BlockPos center, double angle, double minD, double maxD) {
        BlockPos g = center;
        for (int i = 0; i < 18; i++) {
            double a = angle + (i == 0 ? 0 : (RNG.nextDouble() - 0.5D) * 1.6D);
            double dist = minD + RNG.nextDouble() * (maxD - minD);
            int x = center.getX() + (int) Math.round(Math.cos(a) * dist);
            int z = center.getZ() + (int) Math.round(Math.sin(a) * dist);
            g = groundAt(w, x, z);
            BlockState st = w.getBlockState(g);
            if (st.getFluidState().isEmpty() && !(st.getBlock() instanceof LeavesBlock) && g.getY() >= 62 && g.getY() <= 160) return g;
        }
        return g;
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    // ------------------------------------------------------------------ public API
    public static void queueDeviceRoom(ServerWorld w, BlockPos near) {
        StoryData d = StoryData.get(w.getServer());
        if (d.devicePos == null) {
            BlockPos g = pickSite(w, near, RNG.nextDouble() * Math.PI * 2, 45, 70);
            d.gyDevice = g.getY();
            d.devicePos = new BlockPos(g.getX(), deviceFloor(g.getY()) + 1, g.getZ() + 4);
            d.setDirty();
        }
        BuildQueue.enqueue(deviceRoom(d));
    }

    public static void queueHiddenRooms(ServerWorld w) {
        StoryData d = StoryData.get(w.getServer());
        if (d.hiddenA == null) {
            double a = RNG.nextDouble() * Math.PI * 2;
            d.hiddenA = pickSite(w, d.devicePos, a, 45, 75);
            d.hiddenB = pickSite(w, d.devicePos, a + Math.PI * (0.6D + RNG.nextDouble() * 0.8D), 70, 110);
            d.hiddenQueued = true;
            d.setDirty();
        }
        BuildQueue.enqueue(sector(d, d.hiddenA, true));
        BuildQueue.enqueue(sector(d, d.hiddenB, false));
    }

    public static void queueLab(ServerWorld w) {
        StoryData d = StoryData.get(w.getServer());
        if (d.labPos == null) {
            BlockPos g = pickSite(w, d.devicePos, RNG.nextDouble() * Math.PI * 2, 650, 850);
            d.gyLab = g.getY();
            d.labPos = new BlockPos(g.getX(), labFloor(g.getY()), g.getZ() + 6);
            d.labQueued = true;
            d.setDirty();
        }
        BuildQueue.enqueue(lab(d));
    }

    /** Re-queues builds that were interrupted by a server stop. */
    public static void resume(MinecraftServer s) {
        if (BuildQueue.busy()) return;
        StoryData d = StoryData.get(s);
        if (d.roomBuilt && d.devicePos != null && !d.has("done_device")) {
            BuildQueue.enqueue(deviceRoom(d));
        }
        if (d.hiddenQueued && d.hiddenA != null && !d.has("done_hidden")) {
            BuildQueue.enqueue(sector(d, d.hiddenA, true));
            BuildQueue.enqueue(sector(d, d.hiddenB, false));
        }
        if (d.labQueued && d.labPos != null && !d.has("done_lab")) {
            BuildQueue.enqueue(lab(d));
        }
    }

    private static int deviceFloor(int gy) { return clamp(gy - 18, 10, gy - 10); }
    private static int labFloor(int gy) { return clamp(gy - 28, 8, gy - 14); }

    // ------------------------------------------------------------------ shared: ladder shaft + hatch
    /** Shaft whose top hatch is at relative (0,hy,sz). Ceiling of the room is at ceilY. */
    private static void shaft(Blueprint bp, int sz, int ceilY, int hy) {
        bp.fill(-1, ceilY, sz - 1, 1, hy, sz + 1, BRICK);
        bp.sprinkle(-1, ceilY, sz - 1, 1, hy, sz + 1, MOSSY, 0.25);
        bp.fill(0, ceilY, sz, 0, hy - 1, sz, AIR);
        for (int y = 0; y <= hy - 1; y++) {
            bp.set(0, y, sz, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        }
        bp.fill(-1, hy, sz - 1, 1, hy, sz + 1, MOSSY_COBBLE);
        bp.fill(-1, hy + 1, sz - 1, 1, hy + 3, sz + 1, AIR);
        bp.set(0, hy, sz, Blocks.DARK_OAK_TRAPDOOR.defaultBlockState()
                .setValue(TrapDoorBlock.HALF, Half.TOP).setValue(TrapDoorBlock.FACING, Direction.NORTH));
    }

    // ------------------------------------------------------------------ 1. device room
    private static Blueprint deviceRoom(StoryData d) {
        int sx = d.devicePos.getX(), sz = d.devicePos.getZ() - 4, ry = d.devicePos.getY() - 1;
        int hy = d.gyDevice - ry;
        Blueprint bp = new Blueprint(new BlockPos(sx, ry, sz + 4));

        bp.fill(-5, -1, -5, 5, 4, 5, BRICK);
        bp.sprinkle(-5, -1, -5, 5, 4, 5, MOSSY, 0.22);
        bp.sprinkle(-5, -1, -5, 5, 4, 5, CRACKED, 0.15);
        bp.fill(-4, 0, -4, 4, 3, 4, AIR);
        bp.fill(-4, -1, -4, 4, -1, 4, PBB);
        shaft(bp, -4, 4, hy);

        // surface marker: a blue glass pillar so the room can be found
        bp.fill(3, hy - 2, -1, 3, hy, -1, BRICK);
        bp.fill(3, hy + 1, -1, 3, hy + 6, -1, GLASS_BLUE);
        bp.set(3, hy + 7, -1, Blocks.GLOWSTONE.defaultBlockState());

        // pedestal + device
        bp.set(0, 0, 0, PBB);
        bp.set(0, 1, 0, ModBlocks.ECHO_DEVICE.get().defaultBlockState().setValue(HorizontalBlock.FACING, Direction.NORTH));
        for (int sxn : new int[]{-3, 3}) {
            for (int szn : new int[]{-3, 3}) {
                bp.fill(sxn, 0, szn, sxn, 2, szn, PBB);
                bp.lamp(sxn, 3, szn);
            }
        }
        bp.terminal(4, 0, 0, Direction.WEST, 0);
        bp.fill(-4, 0, 1, -4, 1, 3, BOOKSHELF);
        bp.set(-4, 3, -4, COBWEB);
        bp.set(4, 3, 4, COBWEB);
        bp.set(-3, 3, 4, COBWEB);

        d.lamps.addAll(bp.lamps);
        d.setDirty();
        bp.onDone(() -> d.set("done_device"));
        return bp;
    }

    // ------------------------------------------------------------------ 2. hidden sector rooms
    private static Blueprint sector(StoryData d, BlockPos surface, boolean a) {
        int ry = deviceFloor(surface.getY()) + (a ? 0 : -2);
        int hy = surface.getY() - ry;
        Blueprint bp = new Blueprint(new BlockPos(surface.getX(), ry, surface.getZ() + 3));

        bp.fill(-4, -1, -4, 4, 4, 4, BRICK);
        bp.sprinkle(-4, -1, -4, 4, 4, 4, MOSSY, 0.3);
        bp.sprinkle(-4, -1, -4, 4, 4, 4, CRACKED, 0.15);
        bp.fill(-3, 0, -3, 3, 3, 3, AIR);
        bp.fill(-3, -1, -3, 3, -1, 3, PBB);
        shaft(bp, -3, 4, hy);

        bp.lamp(-2, 3, 2);
        bp.lamp(2, 3, 2);
        if (a) {
            bp.terminal(3, 0, -1, Direction.WEST, 1);
            bp.terminal(3, 0, 1, Direction.WEST, 2);
            bp.fill(-3, 0, 0, -3, 1, 2, BOOKSHELF);
        } else {
            bp.terminal(3, 0, 0, Direction.WEST, 3);
            bp.chest(-3, 0, 0, Direction.EAST, new ItemStack(ModItems.OLD_DEVELOPER_LETTER.get()));
            bp.fill(-3, 0, -2, -3, 1, -2, BOOKSHELF);
        }
        bp.set(-3, 3, -2, COBWEB);
        bp.set(3, 3, 3, COBWEB);

        d.lamps.addAll(bp.lamps);
        d.setDirty();
        bp.onDone(() -> { if (!a) d.set("done_hidden"); });
        return bp;
    }

    // ------------------------------------------------------------------ 3. the lab
    private static void room(Blueprint bp, int x1, int z1, int x2, int z2, int h, boolean shell) {
        if (shell) {
            bp.fill(x1 - 1, -1, z1 - 1, x2 + 1, h, z2 + 1, ModBlocks.LAB_PANEL.get().defaultBlockState());
            bp.sprinkle(x1 - 1, -1, z1 - 1, x2 + 1, h, z2 + 1, BRICK, 0.12);
            bp.sprinkle(x1 - 1, -1, z1 - 1, x2 + 1, h, z2 + 1, CRACKED, 0.08);
        } else {
            bp.fill(x1, 0, z1, x2, h - 1, z2, AIR);
            bp.fill(x1, -1, z1, x2, -1, z2, ModBlocks.LAB_TILE.get().defaultBlockState());
        }
    }

    private static void lampGrid(Blueprint bp, int x1, int z1, int x2, int z2, int h, int step) {
        for (int x = x1 + 1; x <= x2 - 1; x += step)
            for (int z = z1 + 1; z <= z2 - 1; z += step)
                bp.lamp(x, h - 1, z);
    }

    private static Blueprint lab(StoryData d) {
        BlockPos o = d.labPos;
        int ly = o.getY();
        int hy = d.gyLab - ly;
        Blueprint bp = new Blueprint(o);

        // rooms: x1,z1,x2,z2,h
        int[][] rooms = {
                {-6, -6, 6, 6, 5},          // 0 lobby
                {7, -1, 14, 1, 4},          // 1 corridor east
                {15, -6, 26, 6, 5},         // 2 archive
                {-14, -1, -7, 1, 4},        // 3 corridor west
                {-26, -6, -15, 6, 5},       // 4 server room
                {-30, -1, -28, 1, 4},       // 5 secret closet
                {-1, 7, 1, 14, 4},          // 6 corridor south
                {-9, 15, 9, 27, 5},         // 7 containment hall
                {-1, 28, 1, 34, 4},         // 8 corridor south 2
                {-11, 35, 11, 57, 9}        // 9 final chamber
        };
        for (int[] r : rooms) room(bp, r[0], r[1], r[2], r[3], r[4], true);
        for (int[] r : rooms) room(bp, r[0], r[1], r[2], r[3], r[4], false);

        // entrance shaft from the lobby ceiling up to the surface
        shaft(bp, -6, 5, hy);

        // ruined building on the surface
        Random rr = bp.rnd();
        for (int x = -4; x <= 4; x++) {
            for (int z = -10; z <= -2; z++) {
                boolean edge = Math.abs(x) == 4 || z == -10 || z == -2;
                if (!edge || (Math.abs(x) <= 1 && z >= -7 && z <= -5)) continue;
                if (rr.nextDouble() < 0.7D) {
                    int h = 1 + rr.nextInt(3);
                    bp.fill(x, hy - 2, z, x, hy, z, BRICK);
                    bp.fill(x, hy + 1, z, x, hy + h, z, rr.nextBoolean() ? MOSSY : CRACKED);
                }
            }
        }

        // lamps
        lampGrid(bp, -6, -6, 6, 6, 5, 4);
        for (int x = 9; x <= 13; x += 4) bp.lamp(x, 3, 0);
        lampGrid(bp, 15, -6, 26, 6, 5, 5);
        for (int x = -12; x <= -8; x += 4) bp.lamp(x, 3, 0);
        lampGrid(bp, -26, -6, -15, 6, 5, 5);
        for (int z = 9; z <= 13; z += 4) bp.lamp(0, 3, z);
        lampGrid(bp, -9, 15, 9, 27, 5, 6);
        bp.lamp(0, 3, 31);
        lampGrid(bp, -11, 35, 11, 57, 9, 6);

        // lobby
        bp.terminal(6, 0, -4, Direction.WEST, 4);
        bp.fill(-3, 0, 3, 3, 0, 3, IRON);
        // archive
        bp.fill(16, 0, -6, 25, 1, -6, BOOKSHELF);
        bp.fill(16, 0, 6, 25, 1, 6, BOOKSHELF);
        bp.terminal(26, 0, -3, Direction.WEST, 5);
        bp.terminal(26, 0, 0, Direction.WEST, 6);
        bp.terminal(26, 0, 3, Direction.WEST, 7);
        // server room: racks + terminals
        for (int x : new int[]{-24, -21, -18}) {
            bp.fill(x, 0, -4, x, 3, -2, IRON);
            bp.fill(x, 0, 2, x, 3, 4, IRON);
        }
        bp.terminal(-26, 0, -4, Direction.EAST, 8);
        bp.terminal(-26, 0, -3, Direction.EAST, 11);
        bp.terminal(-26, 0, 4, Direction.EAST, 9);
        bp.terminal(-20, 0, 6, Direction.NORTH, 10);
        // secret: cracked wall + closet with the hidden memory log
        bp.fill(-27, 0, -1, -27, 2, 1, CRACKED);
        bp.chest(-30, 0, 0, Direction.EAST, new ItemStack(ModItems.MEMORY_LOG.get()));
        // containment hall: 40 dead tubes + the last blue one (ECHO-07)
        for (int x : new int[]{-9, -7, -5, -3, 3, 5, 7, 9}) {
            for (int z = 16; z <= 24; z += 2) {
                bp.set(x, -1, z, IRON);
                bp.fill(x, 0, z, x, 3, z, GLASS_DEAD);
            }
        }
        bp.fill(0, 0, 26, 0, 3, 26, GLASS_BLUE);
        bp.lamp(0, -1, 26);
        bp.terminal(9, 0, 26, Direction.WEST, 12);
        // final chamber: pedestal, glass columns, floor lamps, terminal, last note
        bp.set(0, 0, 46, PBB);
        for (int sx : new int[]{-3, 3}) {
            for (int sz : new int[]{43, 49}) bp.fill(sx, 0, sz, sx, 4, sz, GLASS_BLUE);
        }
        bp.lamp(-2, -1, 46);
        bp.lamp(2, -1, 46);
        bp.lamp(0, -1, 44);
        bp.lamp(0, -1, 48);
        bp.set(0, 0, 52, ModBlocks.RESET_TERMINAL.get().defaultBlockState().setValue(HorizontalBlock.FACING, Direction.NORTH));
        bp.terminal(-10, 0, 50, Direction.EAST, 13);

        d.lamps.addAll(bp.lamps);
        d.setDirty();
        bp.onDone(() -> d.set("done_lab"));
        return bp;
    }
}
