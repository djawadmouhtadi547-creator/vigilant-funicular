package com.thelastplayer.story;

import com.thelastplayer.EchoConfig;
import com.thelastplayer.ai.EchoBrain;
import com.thelastplayer.entity.EchoEntity;
import com.thelastplayer.music.MusicDirector;
import com.thelastplayer.registry.ModEntities;
import com.thelastplayer.registry.ModItems;
import com.thelastplayer.registry.ModSounds;
import com.thelastplayer.story.Dialogue.L;
import com.thelastplayer.structure.StoryStructures;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Random;
import java.util.UUID;

/** Central story state machine (days 1-4). Final scene lives in FinalScene. */
public final class StoryManager {
    private static final Random RNG = new Random();
    /** ECHO stays silent (AI chat ignored) until this game time. */
    public static long silentUntil = 0;

    private StoryManager() {}

    public static StoryData data(MinecraftServer s) { return StoryData.get(s); }

    public static ServerPlayerEntity pl(MinecraftServer s, UUID id) {
        return id == null ? null : s.getPlayerList().getPlayer(id);
    }

    // ------------------------------------------------------------------ echo entity
    public static EchoEntity findEcho(MinecraftServer s) {
        StoryData d = data(s);
        if (d.echo == null) return null;
        for (ServerWorld w : s.getAllLevels()) {
            Entity e = w.getEntity(d.echo);
            if (e instanceof EchoEntity && e.isAlive()) return (EchoEntity) e;
        }
        return null;
    }

    public static EchoEntity spawnEcho(ServerWorld w, double x, double y, double z) {
        StoryData d = data(w.getServer());
        EchoEntity old = findEcho(w.getServer());
        if (old != null) old.remove();
        EchoEntity e = ModEntities.ECHO.get().create(w);
        e.moveTo(x, y, z, 0.0F, 0.0F);
        e.setPersistenceRequired();
        w.addFreshEntity(e);
        d.echo = e.getUUID();
        d.setDirty();
        return e;
    }

    public static EchoEntity ensureEcho(ServerWorld w, double x, double y, double z) {
        EchoEntity e = findEcho(w.getServer());
        if (e == null) return spawnEcho(w, x, y, z);
        e.moveTo(x, y, z, e.yRot, e.xRot);
        return e;
    }

    public static void vanishEcho(ServerWorld w) {
        MinecraftServer s = w.getServer();
        EchoEntity e = findEcho(s);
        StoryData d = data(s);
        if (e != null) {
            ServerWorld ew = (ServerWorld) e.level;
            ew.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY() + 0.5D, e.getZ(), 60, 0.4D, 0.5D, 0.4D, 0.06D);
            e.playVoice(ModSounds.ECHO_GLITCH.get());
            e.remove();
        }
        d.echo = null;
        d.setDirty();
    }

    private static boolean near(ServerPlayerEntity p, EchoEntity e, double r) {
        return e != null && e.level == p.level && e.distanceTo(p) < r;
    }

    // ------------------------------------------------------------------ login / start
    public static void onLogin(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        StoryData d = data(s);
        if (!d.roomBuilt && p.level.dimension() == World.OVERWORLD) {
            d.roomBuilt = true;
            d.setDirty();
            StoryStructures.queueDeviceRoom(p.getLevel(), p.blockPosition());
        }
        StoryStructures.resume(s);
        if (!d.started || !p.getUUID().equals(d.player)) return;

        UUID pid = p.getUUID();
        if (d.finalPhase == 1) { // was interrupted mid-scene: let the player re-trigger the terminal
            d.finalPhase = 0;
            d.setDirty();
        } else if (d.finalPhase == 2) {
            Scheduler.after(60, () -> {
                ServerPlayerEntity q = pl(s, pid);
                if (q != null) FinalScene.lastChoice(q);
            });
        } else if (d.day >= 1 && d.day <= 2 && d.finalPhase == 0) {
            Scheduler.after(80, () -> {
                ServerPlayerEntity q = pl(s, pid);
                EchoEntity e = findEcho(s);
                if (q != null && e != null && near(q, e, 40)) {
                    Dialogue.echoSay(q, EchoTexts.LOGIN_BACK, ModSounds.ECHO_CHIRP.get(), EchoEntity.MOOD_CURIOUS);
                }
            });
        }
    }

    public static void activateDevice(ServerPlayerEntity p, BlockPos pos) {
        MinecraftServer s = p.getServer();
        StoryData d = data(s);
        ServerWorld w = s.overworld();
        if (d.started) {
            Dialogue.actionbar(p, "\u00A78[ \u0627\u0644\u062C\u0647\u0627\u0632 \u0635\u0627\u0645\u062A. \u0644\u0627 \u0634\u064A\u0621 \u0628\u062F\u0627\u062E\u0644\u0647 \u0627\u0644\u0622\u0646. ]");
            return;
        }
        UUID pid = p.getUUID();
        d.started = true;
        d.day = 1;
        d.dayStartTime = w.getGameTime();
        d.player = pid;
        d.devicePos = pos;
        d.setDirty();
        w.playSound(null, pos, ModSounds.TERMINAL_BEEP.get(), SoundCategory.BLOCKS, 1.0F, 0.6F);
        MusicDirector.force(p, ModSounds.MUSIC_DAY1.get());
        Dialogue.sys(p, "\u00A78[ ECHO-07 :: BOOT SEQUENCE ... ]");

        Scheduler.after(60, () -> {
            ServerPlayerEntity q = pl(s, pid);
            EchoEntity e = spawnEcho(w, pos.getX() + 0.5D, pos.getY() + 1.7D, pos.getZ() + 0.5D);
            e.setWaiting(true);
            e.setMood(EchoEntity.MOOD_HAPPY);
            e.playVoice(ModSounds.ECHO_AWAKE.get());
            w.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY() + 0.4D, e.getZ(), 40, 0.3D, 0.3D, 0.3D, 0.05D);
            Dialogue.seq(s, pid, () -> {
                ServerPlayerEntity r = pl(s, pid);
                if (r != null) Dialogue.choices(r, "intro",
                        new String[]{"intro_stay", "\u0627\u0628\u0642\u064E \u0645\u0639\u064A"},
                        new String[]{"intro_unsure", "\u0644\u0627 \u0623\u0639\u0631\u0641 \u0628\u0639\u062F..."});
            },
                    L.of(EchoTexts.INTRO_1, ModSounds.ECHO_LAUGH, EchoEntity.MOOD_HAPPY, 30),
                    L.of(EchoTexts.INTRO_2, ModSounds.ECHO_CHIRP, EchoEntity.MOOD_CURIOUS, 90));
        });
    }

    private static void giveShard(ServerPlayerEntity p) {
        StoryData d = data(p.getServer());
        ItemHandlerHelper.giveItemToPlayer(p, new ItemStack(ModItems.BLUE_GLASS_SHARD.get()));
        d.set("shard");
        p.playNotifySound(ModSounds.TERMINAL_BEEP.get(), SoundCategory.PLAYERS, 0.6F, 1.6F);
        Dialogue.sys(p, "\u00A7b[ \u062D\u0635\u0644\u062A \u0639\u0644\u0649 \u0642\u0637\u0639\u0629 \u0632\u062C\u0627\u062C \u0632\u0631\u0642\u0627\u0621 ]");
    }

    // ------------------------------------------------------------------ choices
    public static void onChoice(ServerPlayerEntity p, String id) {
        MinecraftServer s = p.getServer();
        StoryData d = data(s);
        if (!p.getUUID().equals(d.player)) return;
        String group = id.contains("_") ? id.substring(0, id.indexOf('_')) : id;
        if (d.pending.isEmpty() || !group.equals(d.pending)) {
            Dialogue.sys(p, "\u00A78[ \u0647\u0630\u0627 \u0627\u0644\u062E\u064A\u0627\u0631 \u0644\u0645 \u064A\u0639\u062F \u0645\u062A\u0627\u062D\u064B\u0627 ]");
            return;
        }
        d.pending = "";
        d.setDirty();
        UUID pid = p.getUUID();
        EchoEntity echo = findEcho(s);

        switch (id) {
            case "intro_stay":
            case "intro_unsure": {
                boolean stay = id.equals("intro_stay");
                if (stay) { d.bond += 1; d.remember("intro", "\u0648\u0627\u0641\u0642 \u0627\u0644\u0644\u0627\u0639\u0628 \u0623\u0646 \u064A\u0628\u0642\u0649 \u0625\u064A\u0643\u0648 \u0645\u0639\u0647"); }
                d.setDirty();
                Dialogue.seq(s, pid, () -> {
                    ServerPlayerEntity q = pl(s, pid);
                    if (q != null) giveShard(q);
                    EchoEntity e = findEcho(s);
                    if (e != null) e.setWaiting(false);
                },
                        L.of(stay ? EchoTexts.INTRO_STAY : EchoTexts.INTRO_UNSURE,
                                stay ? ModSounds.ECHO_LAUGH : ModSounds.ECHO_SAD, stay ? EchoEntity.MOOD_HAPPY : EchoEntity.MOOD_CURIOUS, 20),
                        L.of(EchoTexts.SHARD_GIVE, ModSounds.ECHO_CHIRP, EchoEntity.MOOD_CURIOUS, 90));
                break;
            }
            case "alone_yes":
                d.set("alone_answered");
                d.remember("alone", "\u0642\u0627\u0644 \u0627\u0644\u0644\u0627\u0639\u0628 \u0625\u0646\u0647 \u064A\u0644\u0639\u0628 \u062F\u0627\u0626\u0645\u064B\u0627 \u0648\u062D\u062F\u0647");
                Dialogue.seq(s, pid, null, L.of(EchoTexts.ALONE_YES, ModSounds.ECHO_HUM, EchoEntity.MOOD_CURIOUS, 20));
                break;
            case "alone_no":
                d.set("alone_answered");
                Dialogue.seq(s, pid, null, L.of(EchoTexts.ALONE_NO, ModSounds.ECHO_SAD, EchoEntity.MOOD_SAD, 20));
                break;
            case "world_take":
                d.bond += 1; d.setDirty();
                Dialogue.seq(s, pid, null, L.of(EchoTexts.D2_TAKE, ModSounds.ECHO_HUM, EchoEntity.MOOD_CURIOUS, 20));
                break;
            case "world_unsure":
                Dialogue.seq(s, pid, null, L.of(EchoTexts.D2_UNSURE, ModSounds.ECHO_HUM, EchoEntity.MOOD_CURIOUS, 20));
                break;
            case "world_here":
                d.bond += 1; d.setDirty();
                Dialogue.seq(s, pid, null, L.of(EchoTexts.D2_HERE, ModSounds.ECHO_SAD, EchoEntity.MOOD_SAD, 20));
                break;
            case "reunion_comfort":
                d.bond += 2; d.set("comforted"); d.set("reunion_done");
                d.remember("comfort", "\u0648\u0627\u0633\u0649 \u0627\u0644\u0644\u0627\u0639\u0628 \u0625\u064A\u0643\u0648 \u0628\u0639\u062F \u0623\u0646 \u0631\u0623\u0649 \u0627\u0644\u0645\u062E\u062A\u0628\u0631");
                d.setDirty();
                Dialogue.seq(s, pid, () -> {
                    EchoEntity e = findEcho(s);
                    if (e != null) { e.setWaiting(false); e.setMood(EchoEntity.MOOD_CURIOUS); }
                },
                        L.of(EchoTexts.D4_COMFORT_1, ModSounds.ECHO_HUM, EchoEntity.MOOD_SAD, 30),
                        L.of(EchoTexts.D4_COMFORT_2, ModSounds.ECHO_CHIRP, EchoEntity.MOOD_CURIOUS, 80));
                break;
            case "reunion_reject":
                d.bond -= 2; d.set("rejected"); d.set("reunion_done");
                d.remember("reject", "\u0631\u0641\u0636 \u0627\u0644\u0644\u0627\u0639\u0628 \u0645\u0648\u0627\u0633\u0627\u0629 \u0625\u064A\u0643\u0648");
                d.setDirty();
                Dialogue.seq(s, pid, () -> {
                    EchoEntity e = findEcho(s);
                    if (e != null) e.setMood(EchoEntity.MOOD_DIM);
                },
                        L.of(EchoTexts.D4_REJECT, ModSounds.ECHO_SAD, EchoEntity.MOOD_SAD, 30));
                break;
            case "reunion_search":
                d.set("searched"); d.set("reunion_done"); d.setDirty();
                Dialogue.seq(s, pid, () -> {
                    EchoEntity e = findEcho(s);
                    if (e != null) { e.setWaiting(false); e.setMood(EchoEntity.MOOD_SAD); }
                },
                        L.of(EchoTexts.D4_SEARCH_1, ModSounds.ECHO_SAD, EchoEntity.MOOD_SAD, 30),
                        L.of(EchoTexts.D4_SEARCH_2, ModSounds.ECHO_WHISPER, EchoEntity.MOOD_SAD, 80));
                break;
            case "strange_no":
                FinalScene.afterStrange(p, false);
                break;
            case "strange_yes":
                d.bond += 1; d.setDirty();
                FinalScene.afterStrange(p, true);
                break;
            case "attempt_sacrifice":
                FinalScene.sacrifice(p);
                break;
            case "attempt_stay":
                FinalScene.stayBeside(p);
                break;
            case "last_protocol":
                FinalScene.protocol(p);
                break;
            case "last_leave":
                FinalScene.leave(p);
                break;
            case "return_explain":
                FinalScene.afterReturn(p, true);
                break;
            case "return_fresh":
                FinalScene.afterReturn(p, false);
                break;
            default:
                break;
        }
    }

    // ------------------------------------------------------------------ chat / events
    public static void onChat(ServerPlayerEntity p, String msg) {
        MinecraftServer s = p.getServer();
        StoryData d = data(s);
        if (!d.started || !p.getUUID().equals(d.player)) return;
        if (!d.pending.isEmpty()) {
            if (d.pending.equals("alone")) {
                String m = msg.toLowerCase();
                if (m.contains("\u0644\u062D\u0627\u0644") || m.contains("\u0648\u062D\u062F\u064A") || m.contains("alone") || m.contains("solo") || m.contains("\u0623\u064A\u0648\u0647") || m.contains("yes")) {
                    onChoice(p, "alone_yes");
                } else if (m.contains("\u0646\u0627\u0633") || m.contains("\u0623\u0635\u062F\u0642\u0627\u0621") || m.contains("friends") || m.contains("\u0644\u0627") || m.contains("no")) {
                    onChoice(p, "alone_no");
                }
            }
            return;
        }
        EchoEntity e = findEcho(s);
        if (near(p, e, 10) && !p.level.isDay()) {
            if (d.day <= 2) {
                nightTalk++;
                if (nightTalk >= 5) {
                    d.remember("night", "\u062A\u062D\u062F\u062B \u0627\u0644\u0644\u0627\u0639\u0628 \u0645\u0639 \u0625\u064A\u0643\u0648 \u0637\u0648\u0627\u0644 \u0627\u0644\u0644\u064A\u0644");
                    if (!d.has("night_bond")) { d.set("night_bond"); d.bond += 1; d.setDirty(); }
                }
            }
        }
        EchoBrain.handle(p, msg);
    }

    private static int nightTalk = 0;

    public static void onGift(ServerPlayerEntity p, EchoEntity e) {
        StoryData d = data(p.getServer());
        boolean first = !d.has("mem_flower");
        d.remember("flower", "\u0623\u0647\u062F\u0649 \u0627\u0644\u0644\u0627\u0639\u0628 \u0625\u064A\u0643\u0648 \u0648\u0631\u062F\u0629");
        if (first) { d.bond += 1; d.setDirty(); }
        ((ServerWorld) e.level).sendParticles(ParticleTypes.HEART, e.getX(), e.getY() + 0.8D, e.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.02D);
        Dialogue.echoSay(p, first
                        ? "\u0648\u0631\u062F\u0629\u061F! \u0647\u0630\u064A \u0644\u064A\u061F ...\u0645\u0627 \u0623\u0639\u0631\u0641 \u0634\u0648 \u0623\u0633\u0648\u064A \u0641\u064A\u0647\u0627... \u0628\u0633 \u0645\u0627 \u0631\u0627\u062D \u0623\u062A\u0631\u0643\u0647\u0627."
                        : "\u0634\u0643\u0631\u064B\u0627! ...\u0645\u0627 \u0623\u0639\u0631\u0641 \u0644\u064A\u0634 \u0623\u062D\u0628 \u0647\u0627\u0644\u0634\u064A \u0643\u062B\u064A\u0631.",
                ModSounds.ECHO_CHIRP.get(), EchoEntity.MOOD_HAPPY);
    }

    public static void onPoke(ServerPlayerEntity p, EchoEntity e) {
        StoryData d = data(p.getServer());
        if (Scheduler.now() < Dialogue.busyUntil) return;
        if (d.day >= 4 && !d.ending.equals("saved")) {
            e.playVoice(ModSounds.ECHO_HUM.get());
        } else {
            e.playVoice(ModSounds.ECHO_CHIRP.get());
            if (RNG.nextInt(3) == 0) Dialogue.echoSay(p, "\u0647\u0647\u0647\u0647! \u0644\u0627 \u062A\u062F\u063A\u062F\u063A\u0646\u064A!", null, EchoEntity.MOOD_HAPPY);
        }
    }

    public static void onWake(ServerPlayerEntity p) {
        StoryData d = data(p.getServer());
        if (!d.started || !p.getUUID().equals(d.player)) return;
        BlockPos rp = p.getRespawnPosition();
        if (rp != null && d.day <= 3) { d.homePos = rp; d.setDirty(); }
        if ((d.day == 1 && d.has("asked")) || d.day == 2) {
            d.set("woke");
            if (d.day == 1) {
                Scheduler.after(40, () -> {
                    ServerPlayerEntity q = p.getServer().getPlayerList().getPlayer(p.getUUID());
                    EchoEntity e = findEcho(p.getServer());
                    if (q != null && near(q, e, 20)) Dialogue.echoSay(q, EchoTexts.WAKE_1, ModSounds.ECHO_CHIRP.get(), EchoEntity.MOOD_HAPPY);
                });
            }
        }
    }

    public static void onBreak(ServerPlayerEntity p, Block b) {
        StoryData d = data(p.getServer());
        if (!d.started || d.day > 2 || !p.getUUID().equals(d.player)) return;
        EchoEntity e = findEcho(p.getServer());
        if (!near(p, e, 24) || Scheduler.now() < Dialogue.busyUntil) return;
        if (b == Blocks.COAL_ORE && !d.has("c_coal")) { d.set("c_coal"); Dialogue.echoSay(p, EchoTexts.COAL, ModSounds.ECHO_LAUGH.get(), EchoEntity.MOOD_HAPPY); }
        else if (b == Blocks.IRON_ORE && !d.has("c_iron")) { d.set("c_iron"); Dialogue.echoSay(p, EchoTexts.IRON, ModSounds.ECHO_CHIRP.get(), EchoEntity.MOOD_HAPPY); }
        else if (b == Blocks.DIAMOND_ORE && !d.has("c_dia")) { d.set("c_dia"); Dialogue.echoSay(p, EchoTexts.DIAMOND, ModSounds.ECHO_LAUGH.get(), EchoEntity.MOOD_HAPPY); }
    }

    public static void onPlace(ServerPlayerEntity p) {
        StoryData d = data(p.getServer());
        if (!d.started || d.day > 2 || !p.getUUID().equals(d.player)) return;
        EchoEntity e = findEcho(p.getServer());
        if (!near(p, e, 12)) return;
        d.placed++;
        if (d.placed == 60) {
            d.remember("house", "\u0628\u0646\u0649 \u0627\u0644\u0644\u0627\u0639\u0628 \u0634\u064A\u0626\u064B\u0627 \u0628\u062C\u0627\u0646\u0628 \u0625\u064A\u0643\u0648");
            d.bond += 1;
            Dialogue.echoSay(p, "\u0623\u0646\u062A \u062A\u0628\u0646\u064A \u0644\u064A\u061F ...\u0644\u0627 \u062A\u0631\u062F \u0639\u0644\u064A. \u0623\u0646\u0627 \u0628\u0633 \u0623\u062A\u0639\u0648\u062F \u0623\u0646 \u0623\u0639\u064A\u0634 \u0647\u0646\u0627.", ModSounds.ECHO_HUM.get(), EchoEntity.MOOD_CURIOUS);
        }
        d.setDirty();
    }

    public static void onDeath(ServerPlayerEntity p) {
        StoryData d = data(p.getServer());
        if (!d.started || d.day > 2 || !p.getUUID().equals(d.player)) return;
        UUID pid = p.getUUID();
        MinecraftServer s = p.getServer();
        Scheduler.after(120, () -> {
            ServerPlayerEntity q = pl(s, pid);
            EchoEntity e = findEcho(s);
            if (q != null && near(q, e, 40)) Dialogue.echoSay(q, "\u0623\u0646\u062A \u0628\u062E\u064A\u0631\u061F! ...\u0644\u0627 \u062A\u062E\u0648\u0641\u0646\u064A \u0647\u064A\u0643. \u0644\u0627 \u0623\u0628\u064A \u0623\u0628\u0642\u0649 \u0648\u062D\u062F\u064A.", ModSounds.ECHO_FEAR.get(), EchoEntity.MOOD_SCARED);
        });
    }

    // ------------------------------------------------------------------ terminals
    public static void readTerminal(ServerPlayerEntity p, int id, BlockPos pos) {
        MinecraftServer s = p.getServer();
        StoryData d = data(s);
        p.level.playSound(null, pos, ModSounds.TERMINAL_BEEP.get(), SoundCategory.BLOCKS, 0.8F, 1.0F);
        Dialogue.sys(p, "\u00A78\u2550\u2550\u2550\u2550 TERMINAL \u2550\u2550\u2550\u2550");
        if (id == 0) {
            if (d.day < 2 || d.hiddenA == null || d.hiddenB == null) {
                Dialogue.sys(p, "\u00A7c[ACCESS DENIED] \u0627\u0644\u062C\u0644\u0633\u0629 \u0644\u0645 \u062A\u0628\u0644\u063A \u0627\u0644\u0645\u0631\u062D\u0644\u0629 2.");
            } else {
                Dialogue.sys(p, "\u00A73SECTOR MAP \u2014 \u0642\u0637\u0627\u0639\u0627\u062A \u0645\u062A\u0635\u0644\u0629 \u0628\u0627\u0644\u0646\u0648\u0627\u0629:");
                Dialogue.sys(p, "\u00A7fSECTOR A: X " + d.hiddenA.getX() + "  Z " + d.hiddenA.getZ());
                Dialogue.sys(p, "\u00A7fSECTOR B: X " + d.hiddenB.getX() + "  Z " + d.hiddenB.getZ());
                Dialogue.sys(p, "\u00A77\u0627\u0628\u062D\u062B \u0639\u0646 \u062D\u0641\u0631\u0629 \u0645\u0648\u0635\u062F\u0629 \u062A\u062D\u062A \u062D\u062C\u0631 \u0645\u062D\u0634\u0651\u0634.");
                d.set("map_read");
            }
        } else {
            for (String line : EchoTexts.log(id)) Dialogue.sys(p, line.startsWith("\u00A7") ? line : "\u00A7f" + line);
            d.logs.add(id);
            d.setDirty();
            if (id == 8 && !d.has("truth")) {
                d.set("truth");
                BlockPos hp = d.homePos != null ? d.homePos : d.devicePos;
                if (hp != null) {
                    Scheduler.after(60, () -> Dialogue.sys(p, "\u00A77\u0644\u062F\u064A\u0643 \u0645\u0627 \u064A\u0643\u0641\u064A. \u0625\u064A\u0643\u0648 \u064A\u0646\u062A\u0638\u0631\u0643 \u0641\u064A \u0628\u064A\u062A\u0643... \u0639\u064F\u062F (X " + hp.getX() + "  Z " + hp.getZ() + ")."));
                }
            }
            if (id == 9) d.set("reset_known");
        }
        Dialogue.sys(p, "\u00A78\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
    }

    public static void useResetTerminal(ServerPlayerEntity p, BlockPos pos) {
        MinecraftServer s = p.getServer();
        StoryData d = data(s);
        p.level.playSound(null, pos, ModSounds.TERMINAL_BEEP.get(), SoundCategory.BLOCKS, 1.0F, 0.5F);
        if (!d.started || !p.getUUID().equals(d.player)) {
            Dialogue.actionbar(p, "\u00A78[ \u0627\u0644\u062C\u0647\u0627\u0632 \u0644\u0627 \u064A\u062A\u0639\u0631\u0651\u0641 \u0639\u0644\u064A\u0643 ]");
            return;
        }
        if (d.finalPhase >= 3) { Dialogue.actionbar(p, "\u00A78[ SESSION CLOSED ]"); return; }
        if (d.finalPhase >= 1) { Dialogue.actionbar(p, "\u00A78[ ... ]"); return; }
        if (!d.has("reunion_done")) {
            Dialogue.sys(p, "\u00A7c[ ACCESS DENIED ] \u0627\u0644\u0646\u0648\u0627\u0629 \u0644\u0627 \u062A\u0633\u062A\u062C\u064A\u0628. \u0639\u0644\u064A\u0643 \u0623\u0646 \u062A\u0648\u0627\u062C\u0647 \u0625\u064A\u0643\u0648 \u0623\u0648\u0644\u064B\u0627.");
            return;
        }
        d.terminalPos = pos;
        d.pedestalPos = pos.north(6);
        d.setDirty();
        FinalScene.start(p);
    }

    // ------------------------------------------------------------------ per-second tick for the story player
    public static void tick(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        StoryData d = data(s);
        if (!d.started) { hintSignal(p, d); return; }
        if (!p.getUUID().equals(d.player)) return;

        ServerWorld ow = s.overworld();
        long now = ow.getGameTime();
        long inDay = now - d.dayStartTime;
        long dayTime = ow.getDayTime() % 24000L;
        EchoEntity e = findEcho(s);
        boolean busy = Scheduler.now() < Dialogue.busyUntil;

        if (d.finalPhase == 0 && d.day == 1) day1(p, d, e, inDay, dayTime, busy);
        else if (d.finalPhase == 0 && d.day == 2) day2(p, d, e, inDay, now, busy);
        else if (d.finalPhase == 0 && d.day == 4 && !d.has("reunion") && e != null && near(p, e, 7.0D)) startReunion(p, d);

        labWatch(p, d);
        ghost(p, d);
        tryAdvance(p, d, inDay);
        MusicDirector.tick(p, d);
    }

    private static void day1(ServerPlayerEntity p, StoryData d, EchoEntity e, long inDay, long dayTime, boolean busy) {
        if (e == null || busy || !d.pending.isEmpty() || !near(p, e, 20)) return;
        if (inDay >= 3000 && !d.has("g1")) {
            d.set("g1");
            ItemHandlerHelper.giveItemToPlayer(p, new ItemStack(Items.TORCH, 16));
            ItemHandlerHelper.giveItemToPlayer(p, new ItemStack(Items.BREAD, 6));
            Dialogue.echoSay(p, EchoTexts.GIFT_1, ModSounds.ECHO_CHIRP.get(), EchoEntity.MOOD_HAPPY);
        } else if (inDay >= 7500 && !d.has("g2")) {
            d.set("g2");
            ItemHandlerHelper.giveItemToPlayer(p, new ItemStack(Items.IRON_INGOT, 3));
            ItemHandlerHelper.giveItemToPlayer(p, new ItemStack(Items.COAL, 8));
            Dialogue.echoSay(p, EchoTexts.GIFT_2, ModSounds.ECHO_HUM.get(), EchoEntity.MOOD_CURIOUS);
        } else if (dayTime >= 12500 && dayTime < 13500 && !d.has("dusk")) {
            d.set("dusk");
            Dialogue.echoSay(p, EchoTexts.DUSK_TIP, ModSounds.ECHO_CHIRP.get(), EchoEntity.MOOD_CURIOUS);
        } else if (inDay >= 9000 && dayTime >= 13000 && dayTime < 23000 && !d.has("asked")) {
            d.set("asked");
            Dialogue.seq(p.getServer(), p.getUUID(), () -> {
                ServerPlayerEntity q = pl(p.getServer(), p.getUUID());
                if (q != null) Dialogue.choices(q, "alone",
                        new String[]{"alone_yes", "\u0623\u064A\u0648\u0647\u060C \u062F\u0627\u0626\u0645\u064B\u0627 \u0644\u062D\u0627\u0644\u064A"},
                        new String[]{"alone_no", "\u0644\u0627\u060C \u0623\u0644\u0639\u0628 \u0645\u0639 \u0646\u0627\u0633"});
            }, L.of(EchoTexts.ASK_ALONE, ModSounds.ECHO_HUM, EchoEntity.MOOD_CURIOUS, 10));
        } else if (inDay >= 11000 && !d.has("g3")) {
            d.set("g3");
            ItemHandlerHelper.giveItemToPlayer(p, new ItemStack(Items.COOKED_BEEF, 8));
            Dialogue.echoSay(p, EchoTexts.GIFT_3, ModSounds.ECHO_LAUGH.get(), EchoEntity.MOOD_HAPPY);
        }
    }

    private static void day2(ServerPlayerEntity p, StoryData d, EchoEntity e, long inDay, long now, boolean busy) {
        MinecraftServer s = p.getServer();
        // mood recovery after a glitch moment
        if (e != null && now >= silentUntil && e.getMood() == EchoEntity.MOOD_GLITCH) e.setMood(EchoEntity.MOOD_CURIOUS);

        // ambient noises from the hidden sectors
        for (BlockPos hp : new BlockPos[]{d.hiddenA, d.hiddenB}) {
            if (hp != null && p.level.dimension() == World.OVERWORLD && p.distanceToSqr(hp.getX(), p.getY(), hp.getZ()) < 48 * 48 && RNG.nextInt(5) == 0) {
                p.level.playSound(null, hp, ModSounds.OLD_DEVICE_NOISE.get(), SoundCategory.AMBIENT, 1.4F, 0.8F + RNG.nextFloat() * 0.4F);
            }
        }
        if (e == null || busy || !d.pending.isEmpty() || !near(p, e, 20)) return;

        if (inDay >= 2400 && !d.has("d2_sky")) {
            d.set("d2_sky");
            e.gazeAtSky(260);
            e.setMood(EchoEntity.MOOD_GLITCH);
            Dialogue.seq(s, p.getUUID(), () -> {
                EchoEntity x = findEcho(s);
                if (x != null) x.setMood(EchoEntity.MOOD_CURIOUS);
            },
                    L.of(EchoTexts.D2_SKY_1, null, EchoEntity.MOOD_GLITCH, 20),
                    L.of(EchoTexts.D2_SKY_2, ModSounds.ECHO_SAD, EchoEntity.MOOD_CURIOUS, 120));
        } else if (inDay >= 6000 && !d.has("d2_ask")) {
            d.set("d2_ask");
            Dialogue.seq(s, p.getUUID(), () -> {
                ServerPlayerEntity q = pl(s, p.getUUID());
                if (q != null) Dialogue.choices(q, "world",
                        new String[]{"world_take", "\u0623\u0643\u064A\u062F\u060C \u0623\u0623\u062E\u0630\u0643 \u0645\u0639\u064A"},
                        new String[]{"world_unsure", "\u0645\u0627 \u0623\u0639\u0631\u0641 \u0625\u0630\u0627 \u0623\u0642\u062F\u0631"},
                        new String[]{"world_here", "\u0647\u0630\u0627 \u0627\u0644\u0639\u0627\u0644\u0645 \u064A\u0643\u0641\u064A\u0646\u0627"});
            }, L.of(EchoTexts.D2_ASK, ModSounds.ECHO_HUM, EchoEntity.MOOD_CURIOUS, 10));
        } else if (inDay >= 10000 && !d.has("d2_noise")) {
            d.set("d2_noise");
            Dialogue.seq(s, p.getUUID(), null, L.of(EchoTexts.D2_NOISE, ModSounds.ECHO_WHISPER, EchoEntity.MOOD_SCARED, 10));
        } else if (now >= silentUntil && RNG.nextInt(150) == 0) {
            silentUntil = now + 600;
            e.setMood(EchoEntity.MOOD_GLITCH);
            e.playVoice(ModSounds.ECHO_GLITCH.get());
        }
    }

    private static void startReunion(ServerPlayerEntity p, StoryData d) {
        MinecraftServer s = p.getServer();
        d.set("reunion");
        EchoEntity e = findEcho(s);
        if (e != null) { e.setWaiting(true); e.setMood(EchoEntity.MOOD_SAD); }
        MusicDirector.force(p, ModSounds.MUSIC_DAY4.get());
        Dialogue.seq(s, p.getUUID(), () -> {
            ServerPlayerEntity q = pl(s, p.getUUID());
            if (q != null) Dialogue.choices(q, "reunion",
                    new String[]{"reunion_comfort", "\u0623\u0648\u0627\u0633\u064A\u0647"},
                    new String[]{"reunion_reject", "\u0623\u0631\u0641\u0636\u0647"},
                    new String[]{"reunion_search", "\u0623\u0643\u0645\u0644 \u0627\u0644\u0628\u062D\u062B \u0639\u0646 \u0627\u0644\u062D\u0642\u064A\u0642\u0629"});
        },
                L.of(EchoTexts.D4_1, ModSounds.ECHO_SAD, EchoEntity.MOOD_SAD, 30),
                L.of(EchoTexts.D4_2, ModSounds.ECHO_FEAR, EchoEntity.MOOD_SCARED, 90),
                L.of(EchoTexts.D4_3, ModSounds.ECHO_CRY, EchoEntity.MOOD_SCARED, 80),
                L.of(EchoTexts.D4_4, ModSounds.ECHO_HUM, EchoEntity.MOOD_SAD, 90),
                L.of(EchoTexts.D4_5, ModSounds.ECHO_WHISPER, EchoEntity.MOOD_DIM, 140));
    }

    private static void labWatch(ServerPlayerEntity p, StoryData d) {
        if (d.labPos == null || d.has("labEntered") || p.level.dimension() != World.OVERWORLD) return;
        BlockPos lp = d.labPos;
        double dx = p.getX() - lp.getX(), dz = p.getZ() - lp.getZ(), dy = p.getY() - lp.getY();
        if (dx > -32 && dx < 32 && dz > -10 && dz < 60 && dy > -4 && dy < 12) {
            d.set("labEntered");
            d.remember("lab", "\u062F\u062E\u0644 \u0627\u0644\u0644\u0627\u0639\u0628 \u0645\u062E\u062A\u0628\u0631 PROJECT ECHO");
            MusicDirector.force(p, ModSounds.MUSIC_LAB.get());
            Dialogue.title(p, "\u00A7bPROJECT ECHO", "\u00A77FACILITY 02", 10, 70, 20);
            Dialogue.sys(p, "\u00A78[ \u062F\u062E\u0644\u062A\u064E \u0645\u0646\u0634\u0623\u0629 \u0645\u0647\u062C\u0648\u0631\u0629. \u0627\u0644\u0623\u0636\u0648\u0627\u0621 \u0644\u0627 \u062A\u0632\u0627\u0644 \u062A\u0639\u0645\u0644. ]");
        }
    }

    private static void ghost(ServerPlayerEntity p, StoryData d) {
        if (d.ending.isEmpty() || d.ending.equals("saved") || d.has("ghost") || d.devicePos == null) return;
        if (p.level.dimension() != World.OVERWORLD || p.distanceToSqr(d.devicePos.getX(), d.devicePos.getY(), d.devicePos.getZ()) > 16) return;
        d.set("ghost");
        MinecraftServer s = p.getServer();
        UUID pid = p.getUUID();
        BlockPos dp = d.devicePos;
        Scheduler.after(30, () -> {
            ServerPlayerEntity q = pl(s, pid);
            if (q == null) return;
            q.level.playSound(null, dp, ModSounds.ECHO_WHISPER.get(), SoundCategory.VOICE, 0.8F, 0.9F);
            Dialogue.sys(q, "\u00A78[ECHO] \u00A77" + EchoTexts.GHOST);
        });
        Scheduler.after(110, () -> {
            ServerPlayerEntity q = pl(s, pid);
            if (q == null) return;
            q.level.playSound(null, dp, ModSounds.ECHO_GLITCH.get(), SoundCategory.VOICE, 0.7F, 0.6F);
            Dialogue.sys(q, "\u00A78\u00A7k|||||||\u00A7r");
        });
    }

    private static void hintSignal(ServerPlayerEntity p, StoryData d) {
        if (!EchoConfig.HINT_SIGNAL.get() || !d.roomBuilt || d.devicePos == null) return;
        if (p.level.dimension() != World.OVERWORLD || p.tickCount < 3600 || (p.tickCount / 20) % 45 != 0) return;
        double dx = d.devicePos.getX() - p.getX(), dz = d.devicePos.getZ() - p.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 400) return;
        if (dist < 35) {
            Dialogue.actionbar(p, "\u00A7b[ \u0625\u0634\u0627\u0631\u0629 \u0642\u0648\u064A\u0629 ] \u00A77\u0627\u0628\u062D\u062B \u0639\u0646 \u0639\u0645\u0648\u062F \u0632\u062C\u0627\u062C \u0623\u0632\u0631\u0642");
            return;
        }
        String[] names = {"\u0634\u0631\u0642", "\u062C\u0646\u0648\u0628 \u0634\u0631\u0642", "\u062C\u0646\u0648\u0628", "\u062C\u0646\u0648\u0628 \u063A\u0631\u0628", "\u063A\u0631\u0628", "\u0634\u0645\u0627\u0644 \u063A\u0631\u0628", "\u0634\u0645\u0627\u0644", "\u0634\u0645\u0627\u0644 \u0634\u0631\u0642"};
        double ang = Math.atan2(dz, dx); // +x = east, +z = south
        int idx = (int) Math.round(ang / (Math.PI / 4.0D));
        idx = ((idx % 8) + 8) % 8;
        int rd = (int) (Math.round(dist / 10.0D) * 10);
        Dialogue.actionbar(p, "\u00A79[ \u0625\u0634\u0627\u0631\u0629 \u063A\u0627\u0645\u0636\u0629 ] \u00A77\u0627\u0644\u0627\u062A\u062C\u0627\u0647: \u00A7f" + names[idx] + " \u00A77\u2014 \u062A\u0642\u0631\u064A\u0628\u064B\u0627 \u00A7f" + rd + " \u00A77\u0628\u0644\u0648\u0643");
    }

    // ------------------------------------------------------------------ day progression
    private static void tryAdvance(ServerPlayerEntity p, StoryData d, long inDay) {
        int force = EchoConfig.FORCE_DAY_TICKS.get();
        MinecraftServer s = p.getServer();
        switch (d.day) {
            case 1:
                if (d.has("asked") && ((d.has("woke") && inDay >= 12000) || inDay >= force)) advance(s, d);
                break;
            case 2:
                if ((d.logs.size() >= 2 && d.has("woke") && inDay >= 12000) || inDay >= force) advance(s, d);
                break;
            case 3:
                if (d.has("truth") && d.homePos != null && p.level.dimension() == World.OVERWORLD) {
                    double dx = p.getX() - d.homePos.getX(), dz = p.getZ() - d.homePos.getZ();
                    if (dx * dx + dz * dz < 80 * 80) advance(s, d);
                }
                break;
            default:
                break;
        }
    }

    public static void advance(MinecraftServer s, StoryData d) {
        ServerWorld ow = s.overworld();
        d.day++;
        d.dayStartTime = ow.getGameTime();
        d.flags.remove("woke");
        d.setDirty();
        ServerPlayerEntity p = pl(s, d.player);
        onNewDay(s, d, ow, p);
    }

    private static void onNewDay(MinecraftServer s, StoryData d, ServerWorld ow, ServerPlayerEntity p) {
        UUID pid = d.player;
        switch (d.day) {
            case 2: {
                StoryStructures.queueHiddenRooms(ow);
                StoryStructures.queueLab(ow);
                if (p == null) return;
                MusicDirector.force(p, ModSounds.MUSIC_DAY2.get());
                Dialogue.title(p, "\u00A7b\u0627\u0644\u064A\u0648\u0645 \u0627\u0644\u062B\u0627\u0646\u064A", null, 10, 50, 20);
                Dialogue.seq(s, pid, null,
                        L.of(EchoTexts.D2_START_1, ModSounds.ECHO_HUM, EchoEntity.MOOD_CURIOUS, 60),
                        L.of(EchoTexts.D2_START_2, null, EchoEntity.MOOD_GLITCH, 90));
                break;
            }
            case 3: {
                BlockPos hp = p != null ? p.getRespawnPosition() : null;
                d.homePos = hp != null ? hp : (d.homePos != null ? d.homePos : d.devicePos);
                d.setDirty();
                if (p == null) return;
                MusicDirector.force(p, ModSounds.MUSIC_LAB.get());
                Dialogue.title(p, "\u00A7b\u0627\u0644\u064A\u0648\u0645 \u0627\u0644\u062B\u0627\u0644\u062B", null, 10, 50, 20);
                EchoEntity e = findEcho(s);
                if (e != null) { e.setMood(EchoEntity.MOOD_GLITCH); e.setWaiting(true); }
                Dialogue.seq(s, pid, () -> {
                    vanishEcho(ow);
                    ServerPlayerEntity q = pl(s, pid);
                    if (q == null) return;
                    ItemHandlerHelper.giveItemToPlayer(q, makeBook(d));
                    Dialogue.sys(q, "\u00A78[ \u0625\u064A\u0643\u0648 \u0644\u0645 \u064A\u0639\u062F \u0645\u0648\u062C\u0648\u062F\u064B\u0627. ]");
                    Dialogue.sys(q, "\u00A77\u0648\u062C\u062F\u062A \u0643\u062A\u0627\u0628\u064B\u0627 \u0645\u0644\u0642\u0649\u064B \u0639\u0644\u0649 \u0627\u0644\u0623\u0631\u0636 \u0645\u0643\u0627\u0646\u0647...");
                },
                        L.of(EchoTexts.D3_VANISH_1, ModSounds.ECHO_FEAR, EchoEntity.MOOD_GLITCH, 80),
                        L.of(EchoTexts.D3_VANISH_2, ModSounds.ECHO_GLITCH, EchoEntity.MOOD_GLITCH, 70));
                break;
            }
            case 4: {
                BlockPos hp = d.homePos != null ? d.homePos : d.devicePos;
                if (hp == null) return;
                EchoEntity e = spawnEcho(ow, hp.getX() + 0.5D, hp.getY() + 2.0D, hp.getZ() + 0.5D);
                e.setWaiting(true);
                e.setMood(EchoEntity.MOOD_SAD);
                if (p != null) Dialogue.title(p, "\u00A7b\u0627\u0644\u064A\u0648\u0645 \u0627\u0644\u0631\u0627\u0628\u0639", null, 10, 50, 20);
                break;
            }
            default:
                break;
        }
    }

    private static ItemStack makeBook(StoryData d) {
        ItemStack b = new ItemStack(Items.WRITTEN_BOOK);
        CompoundNBT tag = b.getOrCreateTag();
        tag.putString("title", "\u0645\u0644\u0627\u062D\u0638\u0627\u062A");
        tag.putString("author", "?");
        ListNBT pages = new ListNBT();
        BlockPos lp = d.labPos != null ? d.labPos : d.devicePos;
        String p1 = "\u0645\u0644\u0627\u062D\u0638\u0627\u062A \u2014 \u0645\u0634\u0631\u0648\u0639 ECHO\n\n\u0625\u0630\u0627 \u0648\u062C\u062F\u062A\u064E \u0647\u0630\u0627\u060C \u0641\u0642\u062F \u0648\u0635\u0644\u062A\u064E \u0628\u0639\u064A\u062F\u064B\u0627.\n\n\u0627\u0644\u0645\u062E\u062A\u0628\u0631 \u0644\u0627 \u064A\u0638\u0647\u0631 \u0639\u0644\u0649 \u0627\u0644\u062E\u0631\u0627\u0626\u0637.\n\n\u0627\u0644\u0625\u062D\u062F\u0627\u062B\u064A\u0627\u062A:\nX: " + lp.getX() + "\nZ: " + lp.getZ();
        String p2 = "\u062A\u062D\u0630\u064A\u0631:\n\u0644\u0627 \u062A\u062B\u0642 \u0628\u0627\u0644\u0623\u0628\u0648\u0627\u0628 \u0627\u0644\u0645\u0641\u062A\u0648\u062D\u0629.\n\n\u0643\u0644 \u062C\u0644\u0633\u0629 \u0633\u0627\u0628\u0642\u0629 \u0627\u0646\u062A\u0647\u062A \u0647\u0646\u0627\u0643.\n\n\u2014 \u062D.";
        pages.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent(p1))));
        pages.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(new StringTextComponent(p2))));
        tag.put("pages", pages);
        return b;
    }

    /** Dev helper: fast-forward the story to a given day. */
    public static void devSetDay(MinecraftServer s, int target) {
        StoryData d = data(s);
        while (d.day < target) advance(s, d);
    }
}
