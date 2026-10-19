package com.thelastplayer.story;

import com.thelastplayer.EchoConfig;
import com.thelastplayer.entity.EchoEntity;
import com.thelastplayer.music.MusicDirector;
import com.thelastplayer.registry.ModItems;
import com.thelastplayer.registry.ModSounds;
import com.thelastplayer.story.Dialogue.L;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/** The last scene in the lab + all endings. Every important line here is fully scripted. */
public final class FinalScene {
    private static final Random RNG = new Random();
    private static int token = 0;

    private FinalScene() {}

    private static ServerPlayerEntity pl(MinecraftServer s, UUID id) { return StoryManager.pl(s, id); }

    // --------------------------------------------------------------- part 1: ECHO talks
    public static void start(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        StoryData d = StoryData.get(s);
        ServerWorld w = p.getLevel();
        UUID pid = p.getUUID();
        BlockPos ped = d.pedestalPos;

        d.finalPhase = 1;
        d.setDirty();
        EchoEntity e = StoryManager.ensureEcho(w, ped.getX() + 0.5D, ped.getY() + 2.0D, ped.getZ() + 0.5D);
        e.setWaiting(true);
        e.setMood(EchoEntity.MOOD_SAD);
        MusicDirector.force(p, ModSounds.MUSIC_FINAL.get());
        w.playSound(null, d.terminalPos, ModSounds.RESET_START.get(), SoundCategory.BLOCKS, 1.0F, 0.8F);

        Dialogue.seq(s, pid, () -> {
            ServerPlayerEntity q = pl(s, pid);
            if (q != null) Dialogue.choices(q, "strange",
                    new String[]{"strange_no", "\u0644\u0627... \u0645\u0648 \u063A\u0631\u064A\u0628"},
                    new String[]{"strange_yes", "\u063A\u0631\u064A\u0628... \u0628\u0633 \u0623\u0646\u0627 \u0647\u0646\u0627"});
        },
                L.sys("\u00A7c[ RESET SEQUENCE \u2014 ARMED ]", 20),
                L.sys("\u00A77T-00:03:00", 30),
                L.of(EchoTexts.F_1, ModSounds.ECHO_SAD, EchoEntity.MOOD_SAD, 60),
                L.of(EchoTexts.F_2, ModSounds.ECHO_HUM, EchoEntity.MOOD_SAD, 90),
                L.of(EchoTexts.F_HOUSE, ModSounds.ECHO_CHIRP, EchoEntity.MOOD_CURIOUS, 100),
                L.of(EchoTexts.F_FLOWER, ModSounds.ECHO_CHIRP, EchoEntity.MOOD_CURIOUS, 100),
                L.of(EchoTexts.F_NIGHT, ModSounds.ECHO_HUM, EchoEntity.MOOD_CURIOUS, 100),
                L.of(EchoTexts.F_3, ModSounds.ECHO_SAD, EchoEntity.MOOD_SAD, 110),
                L.of(EchoTexts.F_4, ModSounds.ECHO_SAD, EchoEntity.MOOD_SAD, 70),
                L.of(EchoTexts.F_5, ModSounds.ECHO_WHISPER, EchoEntity.MOOD_SAD, 80),
                L.of(EchoTexts.F_6, ModSounds.ECHO_SAD, EchoEntity.MOOD_DIM, 120),
                L.of(EchoTexts.F_7, ModSounds.ECHO_SAD, EchoEntity.MOOD_DIM, 90),
                L.of(EchoTexts.F_8, ModSounds.ECHO_WHISPER, EchoEntity.MOOD_DIM, 90),
                L.of(EchoTexts.F_9, ModSounds.ECHO_WHISPER, EchoEntity.MOOD_DIM, 100));
    }

    public static void afterStrange(ServerPlayerEntity p, boolean yes) {
        MinecraftServer s = p.getServer();
        UUID pid = p.getUUID();
        Dialogue.seq(s, pid, () -> {
            ServerPlayerEntity q = pl(s, pid);
            if (q != null) Dialogue.choices(q, "attempt",
                    new String[]{"attempt_sacrifice", "\u0623\u062D\u0627\u0648\u0644 \u0625\u0646\u0642\u0627\u0630\u0647 \u2014 \u0623\u0636\u062D\u0651\u064A \u0628\u0643\u0644 \u0645\u0627 \u0623\u0645\u0644\u0643"},
                    new String[]{"attempt_stay", "\u0623\u0628\u0642\u0649 \u0628\u062C\u0627\u0646\u0628\u0647 \u0641\u0642\u0637"});
        },
                L.of(EchoTexts.F_STRANGE_REPLY, ModSounds.ECHO_HUM, EchoEntity.MOOD_SAD, 30),
                L.sys("\u00A7e[ POWER REQUIRED: SESSION ENERGY ]", 60),
                L.sys("\u00A77\u0627\u0644\u062C\u0647\u0627\u0632 \u064A\u0633\u062A\u0645\u062F \u0627\u0644\u0637\u0627\u0642\u0629 \u0645\u0646 \u0643\u0644 \u0645\u0627 \u062A\u0645\u0644\u0643\u0647 \u0641\u064A \u0647\u0630\u0647 \u0627\u0644\u062C\u0644\u0633\u0629.", 20));
    }

    // --------------------------------------------------------------- part 2: the attempt
    private static boolean isStoryItem(Item i) {
        return i == ModItems.BLUE_GLASS_SHARD.get() || i == ModItems.OLD_DEVELOPER_LETTER.get() || i == ModItems.MEMORY_LOG.get();
    }

    private static boolean hasAll(ServerPlayerEntity p) {
        return p.inventory.countItem(ModItems.BLUE_GLASS_SHARD.get()) > 0
                && p.inventory.countItem(ModItems.OLD_DEVELOPER_LETTER.get()) > 0
                && p.inventory.countItem(ModItems.MEMORY_LOG.get()) > 0;
    }

    private static void consume(ServerPlayerEntity p, Item it) {
        for (int i = 0; i < p.inventory.getContainerSize(); i++) {
            ItemStack st = p.inventory.getItem(i);
            if (st.getItem() == it) { st.shrink(1); return; }
        }
    }

    public static void sacrifice(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        StoryData d = StoryData.get(s);
        UUID pid = p.getUUID();
        if (EchoConfig.SACRIFICE_INVENTORY.get()) {
            for (int i = 0; i < p.inventory.getContainerSize(); i++) {
                ItemStack st = p.inventory.getItem(i);
                if (!st.isEmpty() && !isStoryItem(st.getItem())) p.inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        p.setExperienceLevels(0);
        p.getFoodData().setFoodLevel(2);
        d.set("sacrificed");
        d.remember("sacrifice", "\u0636\u062D\u0651\u0649 \u0627\u0644\u0644\u0627\u0639\u0628 \u0628\u0643\u0644 \u0645\u0627 \u064A\u0645\u0644\u0643 \u0644\u0625\u0646\u0642\u0627\u0630 \u0625\u064A\u0643\u0648");
        p.level.playSound(null, d.terminalPos, ModSounds.TERMINAL_BEEP.get(), SoundCategory.BLOCKS, 1.0F, 0.4F);

        List<L> lines = new ArrayList<>();
        lines.add(L.sys("\u00A7e[ SESSION ENERGY: DRAINING... ]", 30));
        lines.add(L.sys("\u00A7c[ POWER FAILURE ]", 70));
        lines.add(L.of("\u0645\u0627... \u0645\u0627 \u0627\u0634\u062A\u063A\u0644\u061F", ModSounds.ECHO_FEAR, EchoEntity.MOOD_SCARED, 50));
        String name = p.getName().getString();
        int[] delays = {50, 40, 50, 50, 40, 80, 50, 60, 70, 90, 50, 70, 70, 60, 60, 80};
        for (int i = 0; i < EchoTexts.BREAKDOWN.length; i++) {
            String t = EchoTexts.BREAKDOWN[i];
            if (t.contains("%s")) t = String.format(t, name);
            L l;
            if (i <= 4) l = L.of(t, i >= 3 ? ModSounds.ECHO_BREAKDOWN : ModSounds.ECHO_FEAR, EchoEntity.MOOD_SCARED, delays[i]);
            else if (i <= 8) l = L.of(t, ModSounds.ECHO_SAD, EchoEntity.MOOD_SAD, delays[i]);
            else if (i <= 11) l = L.of(t, ModSounds.ECHO_CRY, EchoEntity.MOOD_GLITCH, delays[i]);
            else if (i <= 14) l = L.of(t, ModSounds.ECHO_CRY, EchoEntity.MOOD_GLITCH, delays[i]);
            else l = L.of(t, ModSounds.ECHO_BREAKDOWN, EchoEntity.MOOD_GLITCH, delays[i]);
            lines.add(l);
        }
        Dialogue.seq(s, pid, () -> blackout(s, pid), lines.toArray(new L[0]));
    }

    private static void blackout(MinecraftServer s, UUID pid) {
        StoryData d = StoryData.get(s);
        ServerWorld w = s.overworld();
        Lamps.set(w, d, false);
        ServerPlayerEntity p = pl(s, pid);
        if (p == null) return;
        w.playSound(null, d.terminalPos, ModSounds.POWER_DOWN.get(), SoundCategory.BLOCKS, 1.5F, 0.7F);
        EchoEntity e = StoryManager.findEcho(s);
        if (e != null) e.setMood(EchoEntity.MOOD_DIM);
        Dialogue.title(p, "\u00A7cRESET", "\u00A77FINAL SEQUENCE ACTIVE", 5, 50, 20);
        Scheduler.after(60, () -> {
            ServerPlayerEntity q = pl(s, pid);
            if (q != null) lastChoice(q);
        });
    }

    /** The 20 second window: activate DAWN (if the 3 items are there) or leave. */
    public static void lastChoice(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        StoryData d = StoryData.get(s);
        UUID pid = p.getUUID();
        d.finalPhase = 2;
        final int myToken = ++token;
        boolean all = hasAll(p);
        if (all) {
            Dialogue.sys(p, "\u00A7b[ DAWN: \u0643\u0644 \u0627\u0644\u0645\u0643\u0648\u0651\u0646\u0627\u062A \u0645\u0648\u062C\u0648\u062F\u0629 ]");
            Dialogue.choices(p, "last",
                    new String[]{"last_protocol", "\u062A\u0641\u0639\u064A\u0644 \u0628\u0631\u0648\u062A\u0648\u0643\u0648\u0644 DAWN"},
                    new String[]{"last_leave", "\u062A\u0631\u0643 \u0627\u0644\u0623\u0645\u0631"});
        } else {
            Dialogue.sys(p, "\u00A78[ DAWN: \u0645\u0643\u0648\u0651\u0646\u0627\u062A \u0646\u0627\u0642\u0635\u0629 ]");
            Dialogue.choices(p, "last", new String[]{"last_leave", "\u062A\u0631\u0643 \u0627\u0644\u0623\u0645\u0631"});
        }
        d.setDirty();
        for (int sec = 0; sec <= 20; sec++) {
            final int left = 20 - sec;
            Scheduler.after(sec * 20, () -> {
                StoryData dd = StoryData.get(s);
                if (myToken != token || !dd.pending.equals("last") || dd.finalPhase != 2) return;
                ServerPlayerEntity q = pl(s, pid);
                if (q == null) return;
                if (left > 0) {
                    Dialogue.actionbar(q, "\u00A7cRESET IN 00:" + (left < 10 ? "0" : "") + left);
                    if (left <= 5) q.playNotifySound(ModSounds.TERMINAL_BEEP.get(), SoundCategory.BLOCKS, 0.7F, 0.5F);
                } else {
                    dd.pending = "";
                    dd.setDirty();
                    leave(q);
                }
            });
        }
    }

    public static void protocol(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        StoryData d = StoryData.get(s);
        UUID pid = p.getUUID();
        if (!hasAll(p)) { leave(p); return; }
        consume(p, ModItems.BLUE_GLASS_SHARD.get());
        consume(p, ModItems.OLD_DEVELOPER_LETTER.get());
        consume(p, ModItems.MEMORY_LOG.get());
        d.set("protocol_used");
        token++;
        boolean ok = d.bond >= 4 || (d.bond >= 2 && RNG.nextBoolean());
        Dialogue.seq(s, pid, () -> {
            ServerPlayerEntity q = pl(s, pid);
            if (q == null) return;
            if (ok) saved(q); else lost(q);
        },
                L.sys("\u00A7b[ DAWN: AUTHORIZATION ... OK ]", 20),
                L.sys("\u00A7b[ DAWN: CORE FRAGMENT ... OK ]", 40),
                L.sys("\u00A7b[ DAWN: MEMORY LOG ... OK ]", 40),
                L.sys("\u00A77[ REBOOTING CORE ... 12% ... 47% ... 83% ]", 60),
                L.of("...", ModSounds.ECHO_GLITCH, EchoEntity.MOOD_GLITCH, 50));
    }

    // --------------------------------------------------------------- endings
    private static void saved(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        StoryData d = StoryData.get(s);
        UUID pid = p.getUUID();
        ServerWorld w = s.overworld();
        Lamps.set(w, d, true);
        EchoEntity e = StoryManager.findEcho(s);
        if (e != null) { e.setMood(EchoEntity.MOOD_CURIOUS); e.playVoice(ModSounds.ECHO_AWAKE.get()); }
        MusicDirector.force(p, ModSounds.MUSIC_SAVED.get());
        d.pending = "";
        d.setDirty();
        Dialogue.seq(s, pid, () -> {
            ServerPlayerEntity q = pl(s, pid);
            if (q != null) Dialogue.choices(q, "return",
                    new String[]{"return_explain", "\u0623\u0634\u0631\u062D \u0644\u0647 \u0643\u0644 \u0634\u064A\u0621"},
                    new String[]{"return_fresh", "\u0646\u0628\u062F\u0623 \u0645\u0646 \u062C\u062F\u064A\u062F"});
        },
                L.sys("\u00A7b[ PROTOCOL DAWN: SUCCESS ]", 20),
                L.of(EchoTexts.SAVED_1, ModSounds.ECHO_AWAKE, EchoEntity.MOOD_CURIOUS, 80));
    }

    public static void afterReturn(ServerPlayerEntity p, boolean explain) {
        MinecraftServer s = p.getServer();
        StoryData d = StoryData.get(s);
        UUID pid = p.getUUID();
        d.bond += 1;
        d.setDirty();
        Dialogue.seq(s, pid, () -> {
            ServerPlayerEntity q = pl(s, pid);
            if (q == null) return;
            d.ending = "saved";
            d.finalPhase = 3;
            d.pending = "";
            d.set("ending_saved");
            d.setDirty();
            EchoEntity e = StoryManager.findEcho(s);
            if (e != null) { e.setWaiting(false); e.setMood(EchoEntity.MOOD_HAPPY); }
            Dialogue.title(q, "\u00A7bECHO-07", "\u00A77MEMORY RESTORED: PARTIAL", 20, 100, 40);
            Dialogue.sys(q, "\u00A7bECHO-07 - MEMORY RESTORED: PARTIAL");
        },
                L.of(explain ? EchoTexts.SAVED_EXPLAIN : EchoTexts.SAVED_FRESH, ModSounds.ECHO_HUM, EchoEntity.MOOD_CURIOUS, 40),
                L.of(EchoTexts.SAVED_TRUST, ModSounds.ECHO_CHIRP, EchoEntity.MOOD_HAPPY, 100));
    }

    private static void lost(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        UUID pid = p.getUUID();
        Dialogue.seq(s, pid, () -> finish(s, pid, "lost", "\u00A7cSESSION ENDED", "\u00A77SUBJECT ABANDONED"),
                L.sys("\u00A7c[ PROTOCOL FAILED ]", 20),
                L.of(EchoTexts.LOST_1, ModSounds.ECHO_WHISPER, EchoEntity.MOOD_DIM, 80),
                L.of(EchoTexts.LOST_2, ModSounds.ECHO_GLITCH, EchoEntity.MOOD_GLITCH, 90));
    }

    public static void leave(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        StoryData d = StoryData.get(s);
        UUID pid = p.getUUID();
        d.pending = "";
        d.finalPhase = 2;
        token++;
        Dialogue.seq(s, pid, () -> finish(s, pid, "abandoned", "\u00A7cSESSION ENDED", "\u00A77SUBJECT ABANDONED"),
                L.of(EchoTexts.ABANDON_1, ModSounds.ECHO_SAD, EchoEntity.MOOD_DIM, 40),
                L.of(EchoTexts.ABANDON_2, ModSounds.ECHO_WHISPER, EchoEntity.MOOD_DIM, 100));
    }

    public static void stayBeside(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        UUID pid = p.getUUID();
        StoryData d = StoryData.get(s);
        Lamps.set(s.overworld(), d, false);
        s.overworld().playSound(null, d.terminalPos, ModSounds.POWER_DOWN.get(), SoundCategory.BLOCKS, 1.5F, 0.7F);
        Dialogue.seq(s, pid, () -> finish(s, pid, "farewell", "\u00A7bSESSION ENDED", "\u00A77SUBJECT REMEMBERED"),
                L.of(EchoTexts.FAREWELL_1, ModSounds.ECHO_HUM, EchoEntity.MOOD_SAD, 60),
                L.of(EchoTexts.FAREWELL_2, ModSounds.ECHO_SAD, EchoEntity.MOOD_DIM, 100),
                L.of(EchoTexts.FAREWELL_3, ModSounds.ECHO_WHISPER, EchoEntity.MOOD_DIM, 100));
    }

    private static void finish(MinecraftServer s, UUID pid, String ending, String title, String sub) {
        StoryData d = StoryData.get(s);
        ServerWorld w = s.overworld();
        EchoEntity e = StoryManager.findEcho(s);
        double x = d.pedestalPos.getX() + 0.5D, y = d.pedestalPos.getY() + 1.2D, z = d.pedestalPos.getZ() + 0.5D;
        if (e != null) { x = e.getX(); y = e.getY(); z = e.getZ(); }
        StoryManager.vanishEcho(w);
        // the blue shard stays in the lab
        w.addFreshEntity(new ItemEntity(w, x, y, z, new ItemStack(ModItems.BLUE_GLASS_SHARD.get())));
        d.ending = ending;
        d.finalPhase = 3;
        d.pending = "";
        d.setDirty();
        ServerPlayerEntity p = pl(s, pid);
        if (p != null) {
            MusicDirector.force(p, ModSounds.MUSIC_LOST.get());
            Dialogue.title(p, title, sub, 20, 120, 40);
            Dialogue.sys(p, "\u00A78" + "SESSION ENDED - " + sub.replaceAll("\u00A7.", ""));
        }
        Scheduler.after(240, () -> Lamps.set(w, d, true));
    }
}
