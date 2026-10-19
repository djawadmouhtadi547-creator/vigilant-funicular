package com.thelastplayer.structure;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayDeque;
import java.util.Deque;

/** Places queued blueprints gradually (a few thousand blocks per tick). */
public final class BuildQueue {
    private static final Deque<Blueprint> QUEUE = new ArrayDeque<>();
    private static final int PER_TICK = 4000;

    private BuildQueue() {}

    public static void enqueue(Blueprint bp) { QUEUE.add(bp); }

    public static void clear() { QUEUE.clear(); }

    public static boolean busy() { return !QUEUE.isEmpty(); }

    public static void tick(MinecraftServer server) {
        Blueprint b = QUEUE.peek();
        if (b == null) return;
        ServerWorld w = server.overworld();
        if (b.run(w, PER_TICK)) QUEUE.poll();
    }
}
