package com.thelastplayer.music;

import com.thelastplayer.EchoConfig;
import com.thelastplayer.registry.ModSounds;
import com.thelastplayer.story.StoryData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SStopSoundPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/** Picks background music from the story state (plays a track every few minutes, never loops). */
public final class MusicDirector {
    private static final Map<UUID, Long> NEXT = new HashMap<>();
    private static final Random RNG = new Random();

    private MusicDirector() {}

    public static void tick(ServerPlayerEntity p, StoryData d) {
        if (!EchoConfig.MUSIC_ENABLED.get()) return;
        long now = p.level.getGameTime();
        Long n = NEXT.get(p.getUUID());
        if (n != null && now < n) return;
        SoundEvent track = pick(d);
        if (track == null) return;
        p.playNotifySound(track, SoundCategory.MUSIC, 0.55F, 1.0F);
        NEXT.put(p.getUUID(), now + 2600 + RNG.nextInt(2400));
    }

    /** Stops what is playing and starts this track now (scene changes). */
    public static void force(ServerPlayerEntity p, SoundEvent track) {
        if (!EchoConfig.MUSIC_ENABLED.get()) return;
        p.connection.send(new SStopSoundPacket(null, SoundCategory.MUSIC));
        p.playNotifySound(track, SoundCategory.MUSIC, 0.6F, 1.0F);
        NEXT.put(p.getUUID(), p.level.getGameTime() + 3600);
    }

    private static SoundEvent pick(StoryData d) {
        if (!d.started) return null;
        if (d.finalPhase == 1 || d.finalPhase == 2) return ModSounds.MUSIC_FINAL.get();
        if (d.ending.equals("saved")) return ModSounds.MUSIC_SAVED.get();
        if (!d.ending.isEmpty()) return ModSounds.MUSIC_LOST.get();
        switch (d.day) {
            case 1: return ModSounds.MUSIC_DAY1.get();
            case 2: return ModSounds.MUSIC_DAY2.get();
            case 3: return ModSounds.MUSIC_LAB.get();
            default: return ModSounds.MUSIC_DAY4.get();
        }
    }
}
