# Architecture (4 parts)

| Part | Folder | Content |
|---|---|---|
| 1 Logic | `story/ ai/ entity/ block/ item/ registry/ command/ music/` | story state machine, scripted dialogue, AI client, ECHO entity, blocks/items, commands |
| 2 Models | `client/` + `assets/` | ECHO model/renderer/emissive layer, textures (animated), models, blockstates, lang, sounds, music |
| 3 Structures | `structure/` | Blueprint + BuildQueue, device room, hidden sector rooms, the lab (see `lab_layout.md`) |
| 4 Final | gradle, workflow, README | build + packaging (`assemble.py` merges the parts into one project and zips it) |

Key rules
* Scripted scenes: `Dialogue.seq(...)` + `Scheduler` (ticks). Text lives in `EchoTexts`.
* Choices: clickable chat -> `/echo choice <group>_<option>` -> `StoryManager.onChoice`.
* Story memory: `StoryData` (saved with the world): day, bond score, flags, logs read, memories fed to the AI prompt.
* AI is only called in `EchoBrain.allowed()` states (day 1-2, day 4 after the reunion, saved ending).
* Endings: `FinalScene`. Bond score decides if the DAWN protocol works (>=4 sure, 2-3 50%, otherwise fails).
* Structures are placed gradually by `BuildQueue`, and re-queued after a crash by `StoryStructures.resume`.
