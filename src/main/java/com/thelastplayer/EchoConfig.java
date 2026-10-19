package com.thelastplayer;

import net.minecraftforge.common.ForgeConfigSpec;

/** Configuration for ECHO's optional AI and voice features. */
public final class EchoConfig {
    public enum AIProvider {
        OPENAI,
        GROQ,
        GEMINI,
        CUSTOM
    }

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue AI_ENABLED;
    public static final ForgeConfigSpec.EnumValue<AIProvider> AI_PROVIDER;
    public static final ForgeConfigSpec.ConfigValue<String> AI_ENDPOINT;
    public static final ForgeConfigSpec.ConfigValue<String> AI_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> AI_MODEL;
    public static final ForgeConfigSpec.ConfigValue<String> GROQ_ENDPOINT;
    public static final ForgeConfigSpec.ConfigValue<String> GROQ_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> GROQ_MODEL;
    public static final ForgeConfigSpec.ConfigValue<String> GEMINI_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> GEMINI_MODEL;
    public static final ForgeConfigSpec.IntValue AI_TIMEOUT;
    public static final ForgeConfigSpec.IntValue AI_COOLDOWN;
    public static final ForgeConfigSpec.IntValue AI_MAX_CHARS;

    public static final ForgeConfigSpec.BooleanValue VOICE_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<String> VOICE_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> VOICE_MODEL;
    public static final ForgeConfigSpec.ConfigValue<String> VOICE_NAME;
    public static final ForgeConfigSpec.IntValue VOICE_TIMEOUT;

    public static final ForgeConfigSpec.BooleanValue MUSIC_ENABLED;
    public static final ForgeConfigSpec.BooleanValue HINT_SIGNAL;
    public static final ForgeConfigSpec.BooleanValue SACRIFICE_INVENTORY;
    public static final ForgeConfigSpec.IntValue FORCE_DAY_TICKS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("ECHO AI chat. The main story scenes are always scripted.")
                .push("ai");
        AI_ENABLED = b.comment("Enable AI side replies. If disabled or the API fails, ECHO uses written fallback lines.")
                .define("enabled", true);
        AI_PROVIDER = b.comment("AI provider: OPENAI, GROQ, GEMINI (Google AI Studio key), or CUSTOM (OpenAI-compatible endpoint).")
                .defineEnum("provider", AIProvider.OPENAI);

        AI_ENDPOINT = b.comment("OpenAI-compatible endpoint used by OPENAI and CUSTOM providers.")
                .define("endpoint", "https://api.openai.com/v1/chat/completions");
        AI_API_KEY = b.comment("OpenAI/custom API key. Leave empty to use ECHO_API_KEY from the environment.")
                .define("apiKey", "");
        AI_MODEL = b.comment("Model used by OPENAI and CUSTOM providers.")
                .define("model", "gpt-4o-mini");

        GROQ_ENDPOINT = b.comment("Groq chat-completions endpoint.")
                .define("groqEndpoint", "https://api.groq.com/openai/v1/chat/completions");
        GROQ_API_KEY = b.comment("Groq API key. You can also set GROQ_API_KEY or ECHO_GROQ_API_KEY in the environment.")
                .define("groqApiKey", "");
        GROQ_MODEL = b.comment("Model used by the Groq provider; change it if Groq retires or renames a model.")
                .define("groqModel", "llama-3.3-70b-versatile");

        GEMINI_API_KEY = b.comment("Google AI Studio API key. You can also set GEMINI_API_KEY or ECHO_GEMINI_API_KEY in the environment.")
                .define("geminiApiKey", "");
        GEMINI_MODEL = b.comment("Gemini text model name. Set provider=GEMINI to use Google's generateContent API.")
                .define("geminiModel", "gemini-2.5-flash");

        AI_TIMEOUT = b.comment("Seconds before giving up on an AI request.").defineInRange("timeoutSeconds", 20, 3, 120);
        AI_COOLDOWN = b.comment("Minimum seconds between two AI replies.").defineInRange("cooldownSeconds", 3, 0, 60);
        AI_MAX_CHARS = b.comment("Maximum characters in a reply displayed in chat.").defineInRange("maxChars", 220, 40, 600);
        b.pop();

        b.comment("Optional text-to-speech. Gemini TTS uses the Google AI Studio key; its free quota and model availability depend on Google's current terms.")
                .push("voice");
        VOICE_ENABLED = b.comment("Enable generated speech for ECHO's replies. Disabled by default to avoid API usage until you opt in.")
                .define("enabled", false);
        VOICE_API_KEY = b.comment("Optional separate voice API key. If blank, the Gemini API key is used; ECHO_VOICE_API_KEY can also be set in the environment.")
                .define("apiKey", "");
        VOICE_MODEL = b.comment("Gemini text-to-speech model. This is configurable because preview model names may change.")
                .define("model", "gemini-2.5-flash-preview-tts");
        VOICE_NAME = b.comment("Gemini prebuilt voice name, for example Kore, Aoede, Puck, or Zephyr.")
                .define("voiceName", "Kore");
        VOICE_TIMEOUT = b.comment("Maximum seconds to wait for a voice response.").defineInRange("timeoutSeconds", 35, 5, 120);
        b.pop();

        b.push("story");
        MUSIC_ENABLED = b.define("music", true);
        HINT_SIGNAL = b.comment("Show a faint directional hint to the secret room before the story starts.")
                .define("hintSignal", true);
        SACRIFICE_INVENTORY = b.comment("If true, the final sacrifice takes the whole inventory except the three story items. If false, it only takes XP and hunger.")
                .define("sacrificeInventory", true);
        FORCE_DAY_TICKS = b.comment("A story day is force-advanced after this many ticks even if the player never sleeps.")
                .defineInRange("forceDayTicks", 36000, 12000, 240000);
        b.pop();

        SPEC = b.build();
    }

    private EchoConfig() { }

    public static String apiKey() {
        AIProvider provider = AI_PROVIDER.get();
        if (provider == AIProvider.GEMINI) {
            return firstNonBlank(GEMINI_API_KEY.get(), System.getenv("GEMINI_API_KEY"),
                    System.getenv("ECHO_GEMINI_API_KEY"), AI_API_KEY.get(), System.getenv("ECHO_API_KEY"));
        }
        if (provider == AIProvider.GROQ) {
            return firstNonBlank(GROQ_API_KEY.get(), System.getenv("GROQ_API_KEY"),
                    System.getenv("ECHO_GROQ_API_KEY"), AI_API_KEY.get(), System.getenv("ECHO_API_KEY"));
        }
        return firstNonBlank(AI_API_KEY.get(), System.getenv("ECHO_API_KEY"));
    }

    public static String model() {
        switch (AI_PROVIDER.get()) {
            case GEMINI: return GEMINI_MODEL.get().trim();
            case GROQ: return GROQ_MODEL.get().trim();
            default: return AI_MODEL.get().trim();
        }
    }

    public static String endpoint() {
        switch (AI_PROVIDER.get()) {
            case GROQ: return GROQ_ENDPOINT.get().trim();
            case GEMINI: return "https://generativelanguage.googleapis.com/v1beta/models/";
            default: return AI_ENDPOINT.get().trim();
        }
    }

    /** True when credentials/settings look sufficient to attempt a request. */
    public static boolean canUseAI() {
        if (!AI_ENABLED.get()) return false;
        switch (AI_PROVIDER.get()) {
            case GEMINI:
            case GROQ:
                return !apiKey().isEmpty();
            case OPENAI:
                // Don't send anonymous requests to the public OpenAI API. Custom/local endpoints can be keyless.
                return !apiKey().isEmpty() || !AI_ENDPOINT.get().contains("api.openai.com");
            case CUSTOM:
                // Useful for local servers such as Ollama that do not require a key.
                return true;
            default:
                return false;
        }
    }

    public static String voiceApiKey() {
        String geminiKey = firstNonBlank(GEMINI_API_KEY.get(), System.getenv("GEMINI_API_KEY"),
                System.getenv("ECHO_GEMINI_API_KEY"));
        // Only reuse the shared AI key automatically when Gemini is the selected chat provider;
        // otherwise ai.apiKey might actually be a Groq/OpenAI key.
        if (geminiKey.isEmpty() && AI_PROVIDER.get() == AIProvider.GEMINI) {
            geminiKey = firstNonBlank(AI_API_KEY.get(), System.getenv("ECHO_API_KEY"));
        }
        return firstNonBlank(VOICE_API_KEY.get(), System.getenv("ECHO_VOICE_API_KEY"), geminiKey);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return "";
    }
}
