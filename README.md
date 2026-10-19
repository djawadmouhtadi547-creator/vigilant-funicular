# THE LAST PLAYER

Minecraft Java **1.16.5 / Forge 36.2.39** psychological horror + drama mod.
You find a hidden room, switch on a strange device, and meet **ECHO** - a small floating creature of blue glass
with a light in its chest. He is funny and kind, and slowly you learn what he is afraid of.

> Main scenes are fully scripted and always happen. AI (OpenAI-compatible APIs, Groq, or Gemini) only handles side chat,
> and replies are filtered to protect the story. If the API is unavailable, ECHO falls back to written lines.

## Install / play
1. Install Forge 1.16.5 (36.2.39+), drop `TheLastPlayer-1.16.5-*.jar` in `mods/`.
2. New world. After ~3 minutes a faint signal appears in the action bar pointing to a **blue glass pillar**. Open the hatch next to it, climb down, right-click the device.
3. Talk to ECHO in normal chat when he is near. Click the choices that appear in chat.

## AI and voice setup (optional)

Run the mod once, then open the mod's **Config** screen from Forge's Mods list, or edit `config/thelastplayer-common.toml` while Minecraft is closed. Settings are grouped under `[ai]` and `[voice]`.

### AI chat providers

Choose `provider` in `[ai]`:

- `OPENAI` — uses `endpoint`, `apiKey`, and `model` (the defaults preserve the original setup).
- `GROQ` — uses `groqEndpoint`, `groqApiKey`, and `groqModel`. Groq's OpenAI-compatible chat API is handled directly.
- `GEMINI` — uses `geminiApiKey` and `geminiModel`. This uses Google's Gemini `generateContent` format, not an OpenAI-compatible endpoint. Create the key in Google AI Studio.
- `CUSTOM` — uses `endpoint`, `apiKey`, and `model` for OpenAI-compatible services, including local services such as Ollama.

Example settings (keep only the provider you use and edit the values in your actual config):

```toml
[ai]
  enabled = true
  provider = "GROQ" # OPENAI, GROQ, GEMINI, or CUSTOM
  groqApiKey = ""
  groqModel = "llama-3.3-70b-versatile"
  geminiApiKey = ""
  geminiModel = "gemini-2.5-flash"
```

API keys can be entered in the Config screen. For better secrecy, leave key fields blank and use environment variables instead: `ECHO_API_KEY`, `GROQ_API_KEY` (or `ECHO_GROQ_API_KEY`), and `GEMINI_API_KEY` (or `ECHO_GEMINI_API_KEY`). Never publish a config file containing your keys. The selected provider may have rate limits and free-tier availability can change.

### Optional generated voice (text-to-speech)

The `[voice]` section adds Gemini-generated speech for ECHO's side-chat replies. It is **off by default**. Set `enabled = true`, then put a Gemini/Google AI Studio key in `apiKey` or reuse `geminiApiKey` under `[ai]`. `model` and `voiceName` can be changed if Google changes the available preview models or voice names. The mod requests audio in the background and sends only the generated audio to the player; the API key stays on the server. If voice generation fails or is disabled, the existing built-in ECHO sounds still play.

This is intended for the provider's available free quota, not a promise of unlimited free usage: Google may impose daily limits, change model availability, or require a different model for your account. TTS audio is played by the client's Java Sound output and may not work on systems without an available audio mixer. In multiplayer, configure the API on the server.

Never commit API keys to Git.

## Story at a glance
| Day | What happens |
|---|---|
| 1 | Device, ECHO wakes, gifts/tips, dusk question "هل أنت دائمًا تلعب وحدك؟" |
| 2 | ECHO acts strange, stares at the sky, asks if you'd take him to a new world. Hidden sector rooms + old terminals (PROJECT ECHO) |
| 3 | ECHO vanishes. A book gives the lab coordinates. Read the logs (`SESSION ENDED - SUBJECT ABANDONED`) |
| 4 | Return home: ECHO is waiting. Comfort / reject / keep searching. Then the last room of the lab |

Endings: **saved** (secret, needs shard + developer letter + hidden memory log, and a strong bond), **lost** (protocol fails),
**abandoned** (you leave / time runs out), **farewell** (you stay beside him). After a non-saved ending, go back to the device room...

## Build
GitHub Actions builds the JAR on every push (Actions tab -> artifact `TheLastPlayer-jar`). Tag `v0.2.0` to publish it as a release.
Local: `gradle build` with JDK 8 (or `gradle genEclipseRuns` / `genIntellijRuns` for dev).

## Testing commands (ops)
`/echo dev day <1-4>` · `/echo dev tp <device|a|b|lab|final>` · `/echo dev items` · `/echo dev unlockfinal` · `/echo dev bond <n>` · `/echo dev status`

## Project layout
See `docs/ARCHITECTURE.md`.
