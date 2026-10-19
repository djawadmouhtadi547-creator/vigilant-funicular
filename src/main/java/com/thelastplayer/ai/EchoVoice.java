package com.thelastplayer.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thelastplayer.EchoConfig;
import com.thelastplayer.TheLastPlayer;
import com.thelastplayer.network.EchoNetwork;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Optional Gemini text-to-speech. Audio is generated off-thread and only the PCM is sent to the player. */
public final class EchoVoice {
    private static final int MAX_AUDIO_BYTES = 900_000;
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "echo-voice-api");
        t.setDaemon(true);
        return t;
    });
    private static final int MAX_CACHE_BYTES = 4_000_000;
    private static final Map<String, byte[]> CACHE = new LinkedHashMap<String, byte[]>(16, 0.75f, true);
    private static int cacheBytes;
    private static volatile long retryAfterMillis;

    private EchoVoice() { }

    public static void speak(ServerPlayerEntity player, String text) {
        if (player == null || text == null || text.trim().isEmpty() || !EchoConfig.VOICE_ENABLED.get()) return;
        if (System.currentTimeMillis() < retryAfterMillis) return;
        final String apiKey = EchoConfig.voiceApiKey();
        if (apiKey.isEmpty()) return; // Existing in-game sound effects remain the zero-API fallback.
        final String model = EchoConfig.VOICE_MODEL.get().trim();
        final String voice = EchoConfig.VOICE_NAME.get().trim();
        if (model.isEmpty() || voice.isEmpty()) return;

        final MinecraftServer server = player.getServer();
        if (server == null) return;
        final UUID playerId = player.getUUID();
        final String clippedText = text.length() > EchoConfig.AI_MAX_CHARS.get()
                ? text.substring(0, EchoConfig.AI_MAX_CHARS.get()) : text;
        final int timeout = EchoConfig.VOICE_TIMEOUT.get() * 1000;
        final String cacheKey = model + "\n---VOICE---\n" + voice + "\n---TEXT---\n" + clippedText;
        byte[] cached = getCached(cacheKey);
        if (cached != null) {
            sendToPlayer(server, playerId, cached);
            return;
        }

        EXEC.submit(() -> {
            try {
                byte[] pcm = requestPcm(apiKey, model, voice, clippedText, timeout);
                if (pcm.length == 0 || pcm.length > MAX_AUDIO_BYTES) return;
                putCached(cacheKey, pcm);
                retryAfterMillis = 0L;
                sendToPlayer(server, playerId, pcm);
            } catch (Exception ex) {
                // Back off after failures so a revoked key/unavailable model does not cause one request per reply.
                // Do not log the request URL or key because Google API keys are sent in the query string.
                retryAfterMillis = System.currentTimeMillis() + 60_000L;
                TheLastPlayer.LOGGER.warn("Optional ECHO voice request failed; voice API will pause for 60 seconds ({})",
                        ex.getClass().getSimpleName());
            }
        });
    }


    private static void sendToPlayer(MinecraftServer server, UUID playerId, byte[] pcm) {
        server.execute(() -> {
            ServerPlayerEntity current = server.getPlayerList().getPlayer(playerId);
            if (current != null && current.isAlive()) EchoNetwork.sendVoice(current, pcm);
        });
    }

    private static byte[] getCached(String key) {
        synchronized (CACHE) {
            byte[] value = CACHE.get(key);
            return value == null ? null : value.clone();
        }
    }

    private static void putCached(String key, byte[] value) {
        if (value == null || value.length > MAX_CACHE_BYTES) return;
        synchronized (CACHE) {
            byte[] old = CACHE.put(key, value.clone());
            if (old != null) cacheBytes -= old.length;
            cacheBytes += value.length;
            Iterator<Map.Entry<String, byte[]>> iterator = CACHE.entrySet().iterator();
            while (cacheBytes > MAX_CACHE_BYTES && iterator.hasNext()) {
                Map.Entry<String, byte[]> eldest = iterator.next();
                cacheBytes -= eldest.getValue().length;
                iterator.remove();
            }
        }
    }

    private static byte[] requestPcm(String key, String model, String voice, String text, int timeout) throws Exception {
        HttpURLConnection con = null;
        try {
            JsonObject body = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject prompt = new JsonObject();
            prompt.addProperty("text", "Read aloud naturally and warmly, in the same language as the dialogue. "
                    + "Do not translate, explain, or add any words. Dialogue: " + text);
            parts.add(prompt);
            content.add("parts", parts);
            contents.add(content);
            body.add("contents", contents);

            JsonObject prebuilt = new JsonObject();
            prebuilt.addProperty("voiceName", voice);
            JsonObject voiceConfig = new JsonObject();
            voiceConfig.add("prebuiltVoiceConfig", prebuilt);
            JsonObject speechConfig = new JsonObject();
            speechConfig.add("voiceConfig", voiceConfig);
            JsonObject generation = new JsonObject();
            JsonArray modalities = new JsonArray();
            modalities.add("AUDIO");
            generation.add("responseModalities", modalities);
            generation.add("speechConfig", speechConfig);
            body.add("generationConfig", generation);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + URLEncoder.encode(model, "UTF-8").replace("+", "%20")
                    + ":generateContent?key=" + URLEncoder.encode(key, "UTF-8");
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("Accept", "application/json");
            try (OutputStream os = con.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = con.getResponseCode();
            String response = read(code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream());
            if (code < 200 || code >= 300) throw new RuntimeException("Voice API returned HTTP " + code);

            JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray candidates = obj.getAsJsonArray("candidates");
            if (candidates == null || candidates.size() == 0) throw new RuntimeException("Voice API returned no candidates");
            JsonObject responseContent = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
            if (responseContent == null || responseContent.getAsJsonArray("parts") == null) {
                throw new RuntimeException("Voice API returned no audio parts");
            }
            ByteArrayOutputStream audio = new ByteArrayOutputStream();
            for (JsonElement element : responseContent.getAsJsonArray("parts")) {
                JsonObject part = element.getAsJsonObject();
                if (!part.has("inlineData") || !part.get("inlineData").isJsonObject()) continue;
                JsonObject inline = part.getAsJsonObject("inlineData");
                if (!inline.has("data")) continue;
                byte[] chunk = Base64.getDecoder().decode(inline.get("data").getAsString());
                if (audio.size() + chunk.length > MAX_AUDIO_BYTES) throw new RuntimeException("Voice response too large");
                audio.write(chunk);
            }
            if (audio.size() == 0) throw new RuntimeException("Voice API returned no PCM data");
            return audio.toByteArray();
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private static String read(InputStream in) throws Exception {
        if (in == null) return "";
        try (InputStream input = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) out.write(buffer, 0, read);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
