package com.thelastplayer.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thelastplayer.EchoConfig;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Chat client for OpenAI-compatible APIs (OpenAI/Groq/custom) and Google's Gemini REST API. */
public final class AiClient {
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "echo-ai");
        t.setDaemon(true);
        return t;
    });

    private AiClient() { }

    /** history = list of {role, content}. Completes exceptionally on failure. */
    public static CompletableFuture<String> chat(String system, List<String[]> history, String userMsg) {
        final EchoConfig.AIProvider provider = EchoConfig.AI_PROVIDER.get();
        final String endpoint = EchoConfig.endpoint();
        final String key = EchoConfig.apiKey();
        final String model = EchoConfig.model();
        final int timeout = EchoConfig.AI_TIMEOUT.get() * 1000;

        return CompletableFuture.supplyAsync(() -> {
            if (model.isEmpty()) throw new RuntimeException("AI model is empty");
            if (endpoint.isEmpty()) throw new RuntimeException("AI endpoint is empty");
            if (provider == EchoConfig.AIProvider.GEMINI) {
                return requestGemini(endpoint, key, model, system, history, userMsg, timeout);
            }
            return requestOpenAiCompatible(endpoint, key, model, system, history, userMsg, timeout);
        }, EXEC);
    }

    private static String requestOpenAiCompatible(String endpoint, String key, String model,
                                                  String system, List<String[]> history,
                                                  String userMsg, int timeout) {
        HttpURLConnection con = null;
        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", model);
            body.addProperty("max_tokens", 160);
            body.addProperty("temperature", 0.85);
            JsonArray msgs = new JsonArray();
            msgs.add(openAiMessage("system", system));
            for (String[] h : history) {
                if (h != null && h.length >= 2 && h[0] != null && h[1] != null) {
                    String role = "assistant".equals(h[0]) ? "assistant" : "user";
                    msgs.add(openAiMessage(role, h[1]));
                }
            }
            msgs.add(openAiMessage("user", userMsg));
            body.add("messages", msgs);

            con = (HttpURLConnection) new URL(endpoint).openConnection();
            preparePost(con, timeout);
            if (!key.isEmpty()) con.setRequestProperty("Authorization", "Bearer " + key);
            writeBody(con, body.toString());
            int code = con.getResponseCode();
            String response = read(code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream());
            if (code < 200 || code >= 300) throw new RuntimeException("AI endpoint returned HTTP " + code + errorHint(response));

            JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray choices = obj.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) throw new RuntimeException("AI endpoint returned no choices");
            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null || !message.has("content") || message.get("content").isJsonNull()) {
                throw new RuntimeException("AI endpoint returned an empty message");
            }
            return message.get("content").getAsString();
        } catch (Exception ex) {
            throw ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private static String requestGemini(String endpointBase, String key, String model,
                                        String system, List<String[]> history,
                                        String userMsg, int timeout) {
        if (key.isEmpty()) throw new RuntimeException("Gemini API key is missing");
        HttpURLConnection con = null;
        try {
            JsonObject body = new JsonObject();
            JsonObject systemInstruction = new JsonObject();
            JsonArray systemParts = new JsonArray();
            JsonObject systemPart = new JsonObject();
            systemPart.addProperty("text", system);
            systemParts.add(systemPart);
            systemInstruction.add("parts", systemParts);
            body.add("systemInstruction", systemInstruction);

            JsonArray contents = new JsonArray();
            for (String[] h : history) {
                if (h == null || h.length < 2 || h[0] == null || h[1] == null) continue;
                contents.add(geminiContent("assistant".equals(h[0]) ? "model" : "user", h[1]));
            }
            contents.add(geminiContent("user", userMsg));
            body.add("contents", contents);

            JsonObject generation = new JsonObject();
            generation.addProperty("temperature", 0.85);
            generation.addProperty("maxOutputTokens", 160);
            body.add("generationConfig", generation);

            String encodedModel = URLEncoder.encode(model, "UTF-8").replace("+", "%20");
            String url = endpointBase + encodedModel + ":generateContent?key=" + URLEncoder.encode(key, "UTF-8");
            con = (HttpURLConnection) new URL(url).openConnection();
            preparePost(con, timeout);
            writeBody(con, body.toString());
            int code = con.getResponseCode();
            String response = read(code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream());
            if (code < 200 || code >= 300) throw new RuntimeException("Gemini returned HTTP " + code + errorHint(response));

            JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray candidates = obj.getAsJsonArray("candidates");
            if (candidates == null || candidates.size() == 0) throw new RuntimeException("Gemini returned no candidates");
            JsonObject candidate = candidates.get(0).getAsJsonObject();
            JsonObject content = candidate.getAsJsonObject("content");
            if (content == null || content.getAsJsonArray("parts") == null) throw new RuntimeException("Gemini returned no text parts");
            StringBuilder result = new StringBuilder();
            for (JsonElement partElement : content.getAsJsonArray("parts")) {
                JsonObject part = partElement.getAsJsonObject();
                if (part.has("text") && !part.get("text").isJsonNull()) {
                    if (result.length() > 0) result.append(' ');
                    result.append(part.get("text").getAsString());
                }
            }
            if (result.length() == 0) throw new RuntimeException("Gemini returned an empty text response");
            return result.toString();
        } catch (Exception ex) {
            throw ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private static JsonObject openAiMessage(String role, String content) {
        JsonObject m = new JsonObject();
        m.addProperty("role", role);
        m.addProperty("content", content);
        return m;
    }

    private static JsonObject geminiContent(String role, String text) {
        JsonObject content = new JsonObject();
        content.addProperty("role", role);
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        parts.add(part);
        content.add("parts", parts);
        return content;
    }

    private static void preparePost(HttpURLConnection con, int timeout) throws Exception {
        con.setRequestMethod("POST");
        con.setConnectTimeout(timeout);
        con.setReadTimeout(timeout);
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        con.setRequestProperty("Accept", "application/json");
    }

    private static void writeBody(HttpURLConnection con, String body) throws Exception {
        try (OutputStream os = con.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String read(InputStream in) throws Exception {
        if (in == null) return "";
        try (InputStream input = in; java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = input.read(buf)) > 0) bos.write(buf, 0, n);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /** Include only a short server-provided message; never include request URLs (Gemini URLs contain API keys). */
    private static String errorHint(String response) {
        if (response == null || response.isEmpty()) return "";
        try {
            JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
            JsonObject error = obj.has("error") && obj.get("error").isJsonObject() ? obj.getAsJsonObject("error") : null;
            if (error != null && error.has("message")) {
                String message = error.get("message").getAsString().replace('\n', ' ').replace('\r', ' ');
                if (message.length() > 180) message = message.substring(0, 180);
                return ": " + message;
            }
        } catch (Exception ignored) { }
        return "";
    }
}
