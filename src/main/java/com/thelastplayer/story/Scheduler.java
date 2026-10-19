package com.thelastplayer.story;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Tiny tick scheduler used for scripted scenes (delays are in server ticks). */
public final class Scheduler {
    private static final class Task {
        final long due;
        final Runnable run;
        Task(long due, Runnable run) { this.due = due; this.run = run; }
    }

    private static final List<Task> TASKS = new ArrayList<>();
    private static long now = 0;

    private Scheduler() {}

    public static long now() { return now; }

    public static void after(int ticks, Runnable r) {
        TASKS.add(new Task(now + Math.max(0, ticks), r));
    }

    public static void clear() { TASKS.clear(); }

    public static void tick() {
        now++;
        if (TASKS.isEmpty()) return;
        List<Runnable> due = new ArrayList<>();
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (t.due <= now) {
                due.add(t.run);
                it.remove();
            }
        }
        for (Runnable r : due) {
            try {
                r.run();
            } catch (Exception ex) {
                com.thelastplayer.TheLastPlayer.LOGGER.error("Scene task failed", ex);
            }
        }
    }
}
