package com.thelastplayer.story;

import com.thelastplayer.entity.EchoEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Scripted lines: ECHO speech, system messages, clickable choices and titles. */
public final class Dialogue {
    private Dialogue() {}

    /** Scripted ticks until which the AI chat must stay quiet. */
    public static long busyUntil = 0;

    public static final class L {
        public final String text;
        public final Supplier<SoundEvent> sound;
        public final int mood;
        public final int delay;
        public final boolean system;

        private L(String text, Supplier<SoundEvent> sound, int mood, int delay, boolean system) {
            this.text = text; this.sound = sound; this.mood = mood; this.delay = delay; this.system = system;
        }

        /** ECHO line. delay = ticks to wait BEFORE this line (after the previous one). */
        public static L of(String text, Supplier<SoundEvent> sound, int mood, int delay) {
            return new L(text, sound, mood, delay, false);
        }

        public static L sys(String text, int delay) {
            return new L(text, null, -1, delay, true);
        }
    }

    public static void say(ServerPlayerEntity p, L l) {
        MinecraftServer s = p.getServer();
        if (l.system) {
            p.sendMessage(new StringTextComponent(l.text), Util.NIL_UUID);
            return;
        }
        echoSay(p, l.text, l.sound == null ? null : l.sound.get(), l.mood);
    }

    public static void echoSay(ServerPlayerEntity p, String text, SoundEvent sound, int mood) {
        EchoEntity e = StoryManager.findEcho(p.getServer());
        if (e != null) {
            if (mood >= 0) e.setMood(mood);
            e.playVoice(sound);
        } else if (sound != null) {
            p.playNotifySound(sound, SoundCategory.VOICE, 1.0F, 1.0F);
        }
        IFormattableTextComponent c = new StringTextComponent("[ECHO] ").withStyle(TextFormatting.AQUA)
                .append(new StringTextComponent(text).withStyle(TextFormatting.WHITE));
        p.sendMessage(c, Util.NIL_UUID);
    }

    /** Plays lines one after another. Returns total ticks used. */
    public static int seq(MinecraftServer s, UUID pid, Runnable done, L... lines) {
        int t = 0;
        for (L l : lines) {
            t += l.delay;
            final L fl = l;
            Scheduler.after(t, () -> {
                ServerPlayerEntity p = s.getPlayerList().getPlayer(pid);
                if (p != null) say(p, fl);
            });
        }
        busyUntil = Math.max(busyUntil, Scheduler.now() + t + 40);
        if (done != null) Scheduler.after(t + 12, done);
        return t;
    }

    /** Clickable choices: pairs of {id, label}. Group = text before the first underscore of each id. */
    public static void choices(ServerPlayerEntity p, String group, String[]... opts) {
        StoryData d = StoryData.get(p.getServer());
        d.pending = group;
        d.setDirty();
        IFormattableTextComponent line = new StringTextComponent("  ");
        for (String[] o : opts) {
            final String id = o[0], label = o[1];
            IFormattableTextComponent b = new StringTextComponent("[ " + label + " ]").withStyle(st -> st
                    .withColor(TextFormatting.AQUA)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/echo choice " + id))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(label))));
            line.append(b).append(new StringTextComponent("   "));
        }
        p.sendMessage(new StringTextComponent("\u00A78\u00BB \u00A77\u0627\u062E\u062A\u0631:"), Util.NIL_UUID);
        p.sendMessage(line, Util.NIL_UUID);
    }

    public static void title(ServerPlayerEntity p, String main, String sub, int in, int stay, int out) {
        p.connection.send(new STitlePacket(in, stay, out));
        if (sub != null) p.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, new StringTextComponent(sub)));
        p.connection.send(new STitlePacket(STitlePacket.Type.TITLE, new StringTextComponent(main)));
    }

    public static void actionbar(ServerPlayerEntity p, String text) {
        p.displayClientMessage(new StringTextComponent(text), true);
    }

    public static void sys(ServerPlayerEntity p, String text) {
        p.sendMessage(new StringTextComponent(text), Util.NIL_UUID);
    }
}
