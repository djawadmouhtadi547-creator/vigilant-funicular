package com.thelastplayer.client;

import com.thelastplayer.TheLastPlayer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.AudioSystem;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Plays Gemini's 24 kHz, 16-bit mono PCM outside Minecraft's registered sound-event system. */
public final class EchoVoiceClient {
    private static final ExecutorService AUDIO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "echo-voice-playback");
        t.setDaemon(true);
        return t;
    });

    private EchoVoiceClient() { }

    public static void play(byte[] pcm) {
        if (pcm == null || pcm.length < 2) return;
        final byte[] safeCopy = pcm.clone();
        AUDIO.submit(() -> playNow(safeCopy));
    }

    private static void playNow(byte[] pcm) {
        Clip clip = null;
        try {
            // Gemini's raw audio/L16 response is little-endian PCM at 24 kHz unless the API metadata says otherwise.
            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 24000.0f, 16, 1, 2, 24000.0f, false);
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(format, pcm, 0, pcm.length);
            clip.start();
            // Wait for the full clip duration; some Java Sound providers briefly report !isRunning immediately after start().
            long durationMs = Math.max(100L, clip.getMicrosecondLength() / 1000L);
            Thread.sleep(Math.min(durationMs + 200L, 30_000L));
        } catch (Exception ex) {
            // Devices without a Java Sound mixer, or muted/unsupported output formats, simply keep the normal game SFX.
            TheLastPlayer.LOGGER.debug("Could not play generated ECHO voice on this client ({})", ex.getClass().getSimpleName());
        } finally {
            if (clip != null) {
                try { clip.stop(); } catch (Exception ignored) { }
                try { clip.close(); } catch (Exception ignored) { }
            }
        }
    }
}
