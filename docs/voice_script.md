# ECHO voice script (replace the placeholder sounds with real recordings)

Files go in `src/main/resources/assets/thelastplayer/sounds/<folder>/<name>.ogg` (mono, 22-44 kHz, Vorbis).
Same file name = no code change needed. Keep each clip short; the story code decides when it plays.

| Event / file | Emotion & direction | Example performance |
|---|---|---|
| echo/awake.ogg | boot-up, glassy, surprised | rising chime + tiny gasp |
| echo/laugh.ogg | childish, bubbly "hehehe" | 4-5 short giggles, rising pitch |
| echo/chirp.ogg | happy blip / "hm?" | 0.3 s squeak |
| echo/hum.ogg | thoughtful, soft | "hmm..." 1.2 s |
| echo/sad.ogg | quiet, deflated | sigh / low glassy note |
| echo/fear.ogg | trembling, breathy | short shaky "ah—" |
| echo/whisper.ogg | almost silent, fragile | breath, "...please" (no clear word) |
| echo/breakdown.ogg | panicking, voice cracks, digital tearing | stuttered sobbing with glitch |
| echo/cry.ogg | crying, wobbling pitch | 2 s weeping, descending |
| echo/glitch.ogg | pure digital corruption | stutter / static burst |

Tip: use a text-to-speech / voice tool with a small, high, slightly processed voice, add reverb + a
little bit-crush on breakdown/glitch. Keep the on-screen text as the main "voice" - the sounds are emotion.
Music: `sounds/music/*.ogg` (day1 warm, day2 uneasy, lab cold, day4 sad, final tension, saved hopeful, lost empty).
