#!/usr/bin/env python3
"""
Generates ALL placeholder assets for THE LAST PLAYER:
  * textures (entity, blocks incl. animated ones, items)
  * block/item models, blockstates, lang files, sounds.json, pack.mcmeta
  * synthesized placeholder sounds + music (.ogg via ffmpeg)

Run:  python3 tools/generate_assets.py [resources_dir]
Needs: numpy, pillow, ffmpeg (with libvorbis) for the sounds.
Replace any generated texture/sound with your own file of the same name and it will just work.
"""
import json, math, os, random, subprocess, sys, wave
import numpy as np
from PIL import Image, ImageDraw

OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
A = os.path.join(OUT, "assets", "thelastplayer")
random.seed(7); np.random.seed(7)

def w(path, data, mode="w"):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, mode, **({} if "b" in mode else {"encoding": "utf-8"})) as f:
        f.write(data)

def wj(path, obj):
    w(path, json.dumps(obj, ensure_ascii=False, indent=2))

# ============================================================ textures
def noise_fill(img, box, base, var=10):
    x1, y1, x2, y2 = box
    px = img.load()
    for x in range(x1, x2):
        for y in range(y1, y2):
            n = random.randint(-var, var)
            px[x, y] = tuple(max(0, min(255, c + n)) if i < 3 else c for i, c in enumerate(base))

def entity_textures():
    d = os.path.join(A, "textures", "entity"); os.makedirs(d, exist_ok=True)
    tex = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    rects = [(0, 0, 32, 16), (0, 16, 8, 22), (10, 16, 18, 23), (20, 16, 28, 23), (30, 16, 38, 22), (40, 16, 48, 22), (50, 16, 58, 22)]
    for r in rects:
        noise_fill(tex, r, (110, 190, 255, 150), 14)
    px = tex.load()
    for r in rects:  # lighter glassy edges
        x1, y1, x2, y2 = r
        for x in range(x1, x2):
            for y in range(y1, y2):
                if x in (x1, x2 - 1) or y in (y1, y2 - 1):
                    px[x, y] = (190, 235, 255, 185)
    # heart region (visible through glass): soft pale core
    noise_fill(tex, (32, 0, 44, 6), (220, 245, 255, 200), 8)
    # eye sockets on body front face (u 8..16, v 8..16)
    for (ex, ey) in ((9, 11), (13, 11)):
        for x in range(ex, ex + 2):
            for y in range(ey, ey + 3):
                px[x, y] = (20, 45, 110, 235)
    tex.save(os.path.join(d, "echo.png"))
    glow = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    g = glow.load()
    for x in range(32, 44):
        for y in range(0, 6):
            g[x, y] = (255, 255, 255, 255)
    for (ex, ey) in ((9, 11), (13, 11)):
        for x in range(ex, ex + 2):
            for y in range(ey, ey + 3):
                g[x, y] = (255, 255, 255, 255)
    glow.save(os.path.join(d, "echo_glow.png"))

def metal(size=16, base=(88, 92, 100), var=8):
    im = Image.new("RGBA", (size, size), base + (255,))
    noise_fill(im, (0, 0, size, size), base + (255,), var)
    return im

def border(im, col=(40, 44, 52, 255)):
    px = im.load(); n = im.size[0]; h = im.size[1]
    for i in range(n):
        px[i, 0] = col; px[i, h - 1] = col
    for j in range(h):
        px[0, j] = col; px[n - 1, j] = col

def anim_strip(frames, name, meta=None):
    d = os.path.join(A, "textures", "block"); os.makedirs(d, exist_ok=True)
    strip = Image.new("RGBA", (16, 16 * len(frames)))
    for i, f in enumerate(frames):
        strip.paste(f, (0, 16 * i))
    strip.save(os.path.join(d, name + ".png"))
    if meta:
        wj(os.path.join(d, name + ".png.mcmeta"), {"animation": {"frametime": meta}})

def block_textures():
    d = os.path.join(A, "textures", "block"); os.makedirs(d, exist_ok=True)
    # --- echo device: dark metal + animated heartbeat screen
    side = metal(base=(60, 64, 74)); border(side); side.save(os.path.join(d, "echo_device_side.png"))
    top = metal(base=(52, 56, 66)); border(top); ImageDraw.Draw(top).rectangle((5, 5, 10, 10), outline=(90, 200, 255, 255)); top.save(os.path.join(d, "echo_device_top.png"))
    frames = []
    for k in range(8):
        im = metal(base=(46, 50, 60)); border(im)
        dr = ImageDraw.Draw(im); dr.rectangle((2, 3, 13, 12), fill=(6, 20, 38, 255), outline=(90, 200, 255, 255))
        pts = []
        for x in range(3, 13):
            y = 7.5 + math.sin((x + k * 1.6) * 0.9) * (2.4 if (k % 4) != 3 else 0.6)
            pts.append((x, y))
        dr.line(pts, fill=(120, 240, 255, 255))
        # glowing little heart dot
        c = 255 if k % 4 < 2 else 140
        dr.rectangle((7, 10, 8, 11), fill=(c, 240, 255, 255))
        frames.append(im)
    anim_strip(frames, "echo_device_front", 5)
    # --- archive terminal (amber text scrolling)
    s2 = metal(base=(70, 66, 58)); border(s2); s2.save(os.path.join(d, "archive_terminal_side.png"))
    t2 = metal(base=(62, 58, 52)); border(t2); t2.save(os.path.join(d, "archive_terminal_top.png"))
    frames = []
    for k in range(6):
        im = metal(base=(58, 56, 52)); border(im)
        dr = ImageDraw.Draw(im); dr.rectangle((2, 2, 13, 11), fill=(14, 12, 6, 255), outline=(30, 30, 30, 255))
        for row in range(4):
            ln = 3 + (row * 5 + k * 3) % 8
            y = 3 + row * 2 - (k % 2)
            dr.line((3, y, 3 + ln, y), fill=(255, 190, 60, 255))
        dr.rectangle((3 + (k * 2) % 9, 10, 4 + (k * 2) % 9, 10), fill=(255, 220, 120, 255))
        dr.rectangle((5, 13, 10, 14), fill=(30, 30, 34, 255))
        frames.append(im)
    anim_strip(frames, "archive_terminal_front", 8)
    # --- reset terminal (red)
    s3 = metal(base=(54, 50, 54)); border(s3, (90, 20, 20, 255)); s3.save(os.path.join(d, "reset_terminal_side.png"))
    t3 = metal(base=(48, 44, 48)); border(t3, (90, 20, 20, 255)); t3.save(os.path.join(d, "reset_terminal_top.png"))
    frames = []
    for k in range(6):
        im = metal(base=(50, 46, 50)); border(im, (90, 20, 20, 255))
        dr = ImageDraw.Draw(im); dr.rectangle((2, 2, 13, 9), fill=(24, 4, 4, 255), outline=(150, 30, 30, 255))
        dr.rectangle((3, 4, 3 + (10 - k * 2) % 10 + 1, 6), fill=(255, 60, 60, 255))
        dr.text((3, 10), "RST", fill=(255, 90, 90, 255)) if False else None
        on = 255 if k % 2 == 0 else 100
        dr.rectangle((3, 11, 12, 13), fill=(on, 30, 30, 255))
        frames.append(im)
    anim_strip(frames, "reset_terminal_front", 10)
    # --- lab blocks
    on = Image.new("RGBA", (16, 16), (210, 240, 255, 255)); noise_fill(on, (0, 0, 16, 16), (210, 240, 255, 255), 6); border(on, (150, 200, 230, 255)); on.save(os.path.join(d, "lab_lamp_on.png"))
    off = Image.new("RGBA", (16, 16), (50, 56, 64, 255)); noise_fill(off, (0, 0, 16, 16), (50, 56, 64, 255), 5); border(off, (30, 34, 40, 255)); off.save(os.path.join(d, "lab_lamp_off.png"))
    tile = Image.new("RGBA", (16, 16), (170, 176, 182, 255)); noise_fill(tile, (0, 0, 16, 16), (170, 176, 182, 255), 7)
    dt = ImageDraw.Draw(tile); dt.line((0, 7, 15, 7), fill=(110, 116, 124, 255)); dt.line((7, 0, 7, 15), fill=(110, 116, 124, 255)); tile.save(os.path.join(d, "lab_tile.png"))
    pan = Image.new("RGBA", (16, 16), (214, 220, 226, 255)); noise_fill(pan, (0, 0, 16, 16), (214, 220, 226, 255), 5)
    dp = ImageDraw.Draw(pan); dp.rectangle((0, 7, 15, 8), fill=(70, 150, 190, 255)); [dp.point(p, fill=(120, 126, 134, 255)) for p in ((1, 1), (14, 1), (1, 14), (14, 14))]; border(pan, (150, 156, 164, 255)); pan.save(os.path.join(d, "lab_panel.png"))

def item_textures():
    d = os.path.join(A, "textures", "item"); os.makedirs(d, exist_ok=True)
    # blue glass shard
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); dr = ImageDraw.Draw(im)
    dr.polygon([(8, 1), (12, 6), (10, 14), (6, 13), (4, 6)], fill=(120, 200, 255, 230), outline=(200, 240, 255, 255))
    dr.line((8, 2, 7, 12), fill=(235, 250, 255, 255)); dr.point((9, 5), fill=(255, 255, 255, 255))
    im.save(os.path.join(d, "blue_glass_shard.png"))
    # old letter
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); dr = ImageDraw.Draw(im)
    dr.rectangle((2, 3, 13, 12), fill=(222, 205, 160, 255), outline=(120, 95, 60, 255))
    dr.line((2, 3, 8, 8), fill=(160, 135, 90, 255)); dr.line((13, 3, 8, 8), fill=(160, 135, 90, 255))
    dr.ellipse((6, 7, 9, 10), fill=(150, 40, 40, 255))
    im.save(os.path.join(d, "old_developer_letter.png"))
    # memory log (chip)
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); dr = ImageDraw.Draw(im)
    dr.rectangle((3, 3, 12, 12), fill=(30, 40, 60, 255), outline=(90, 200, 255, 255))
    dr.rectangle((6, 6, 9, 9), fill=(120, 240, 255, 255))
    for x in (4, 6, 9, 11): dr.point((x, 2), fill=(200, 200, 200, 255)); dr.point((x, 13), fill=(200, 200, 200, 255))
    im.save(os.path.join(d, "memory_log.png"))

# ============================================================ models / states / lang / sounds.json
def json_assets():
    M = "thelastplayer"
    def orient(name):
        wj(os.path.join(A, "models", "block", name + ".json"), {
            "parent": "minecraft:block/orientable",
            "textures": {"top": f"{M}:block/{name}_top", "front": f"{M}:block/{name}_front", "side": f"{M}:block/{name}_side"}})
        wj(os.path.join(A, "models", "item", name + ".json"), {"parent": f"{M}:block/{name}"})
    for n in ("echo_device", "archive_terminal", "reset_terminal"):
        orient(n)
    for n, t in (("lab_lamp_on", "lab_lamp_on"), ("lab_lamp_off", "lab_lamp_off"), ("lab_tile", "lab_tile"), ("lab_panel", "lab_panel")):
        wj(os.path.join(A, "models", "block", n + ".json"), {"parent": "minecraft:block/cube_all", "textures": {"all": f"{M}:block/{t}"}})
    wj(os.path.join(A, "models", "item", "lab_lamp.json"), {"parent": f"{M}:block/lab_lamp_on"})
    wj(os.path.join(A, "models", "item", "lab_tile.json"), {"parent": f"{M}:block/lab_tile"})
    wj(os.path.join(A, "models", "item", "lab_panel.json"), {"parent": f"{M}:block/lab_panel"})
    for n in ("blue_glass_shard", "old_developer_letter", "memory_log"):
        wj(os.path.join(A, "models", "item", n + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{M}:item/{n}"}})
    rot = {"north": 0, "east": 90, "south": 180, "west": 270}
    wj(os.path.join(A, "blockstates", "echo_device.json"), {"variants": {f"facing={f}": {"model": f"{M}:block/echo_device", "y": y} for f, y in rot.items()}})
    wj(os.path.join(A, "blockstates", "reset_terminal.json"), {"variants": {f"facing={f}": {"model": f"{M}:block/reset_terminal", "y": y} for f, y in rot.items()}})
    wj(os.path.join(A, "blockstates", "archive_terminal.json"), {"variants": {f"facing={f},log={i}": {"model": f"{M}:block/archive_terminal", "y": y} for f, y in rot.items() for i in range(16)}})
    wj(os.path.join(A, "blockstates", "lab_lamp.json"), {"variants": {"lit=true": {"model": f"{M}:block/lab_lamp_on"}, "lit=false": {"model": f"{M}:block/lab_lamp_off"}}})
    wj(os.path.join(A, "blockstates", "lab_tile.json"), {"variants": {"": {"model": f"{M}:block/lab_tile"}}})
    wj(os.path.join(A, "blockstates", "lab_panel.json"), {"variants": {"": {"model": f"{M}:block/lab_panel"}}})

    en = {"itemGroup.thelastplayer": "THE LAST PLAYER", "entity.thelastplayer.echo": "ECHO",
          "block.thelastplayer.echo_device": "Strange Device", "block.thelastplayer.archive_terminal": "Old Terminal",
          "block.thelastplayer.reset_terminal": "Reset Terminal", "block.thelastplayer.lab_lamp": "Lab Lamp",
          "block.thelastplayer.lab_tile": "Lab Tile", "block.thelastplayer.lab_panel": "Lab Panel",
          "item.thelastplayer.blue_glass_shard": "Blue Glass Shard", "item.thelastplayer.old_developer_letter": "Old Developer Letter",
          "item.thelastplayer.memory_log": "Memory Log"}
    ar = {"itemGroup.thelastplayer": "THE LAST PLAYER", "entity.thelastplayer.echo": "إيكو",
          "block.thelastplayer.echo_device": "جهاز غريب", "block.thelastplayer.archive_terminal": "محطة قديمة",
          "block.thelastplayer.reset_terminal": "محطة إعادة الضبط", "block.thelastplayer.lab_lamp": "مصباح المختبر",
          "block.thelastplayer.lab_tile": "بلاطة المختبر", "block.thelastplayer.lab_panel": "لوح المختبر",
          "item.thelastplayer.blue_glass_shard": "شظية زجاج زرقاء", "item.thelastplayer.old_developer_letter": "رسالة مطوّر قديمة",
          "item.thelastplayer.memory_log": "سجل ذاكرة"}
    for ev in SOUND_EVENTS:
        en["subtitles.thelastplayer." + ev] = ev.replace("_", " ").title()
        ar["subtitles.thelastplayer." + ev] = ev.replace("_", " ").title()
    wj(os.path.join(A, "lang", "en_us.json"), en)
    wj(os.path.join(A, "lang", "ar_sa.json"), ar)
    wj(os.path.join(OUT, "pack.mcmeta"), {"pack": {"description": "THE LAST PLAYER resources", "pack_format": 6}})
    sj = {}
    for ev, (folder, fname, _) in SOUND_EVENTS.items():
        entry = {"sounds": [{"name": f"{M}:{folder}/{fname}", **({"stream": True} if folder == "music" else {})}]}
        entry["subtitle"] = "subtitles.thelastplayer." + ev
        sj[ev] = entry
    wj(os.path.join(A, "sounds.json"), sj)

# ============================================================ audio synthesis
SR = 22050
def t_(dur): return np.linspace(0, dur, int(SR * dur), endpoint=False)
def env(n, a=0.02, r=0.2):
    e = np.ones(n); na = max(1, int(SR * a)); nr = max(1, int(SR * r))
    e[:na] = np.linspace(0, 1, na); e[-nr:] *= np.linspace(1, 0, nr); return e
def sine(f, dur, vib=0.0, vibf=5.0):
    t = t_(dur); f = np.asarray(f) if not np.isscalar(f) else f
    ph = 2 * np.pi * np.cumsum((f + vib * np.sin(2 * np.pi * vibf * t)) * np.ones_like(t)) / SR
    return np.sin(ph)
def sweep(f0, f1, dur, vib=0.0, vibf=5.0):
    t = t_(dur); f = np.linspace(f0, f1, len(t)); return sine(f, dur, vib, vibf)
def glass(f, dur):  # bell-ish glass tone
    t = t_(dur); x = sum(a * np.sin(2 * np.pi * f * m * t) * np.exp(-t * d) for m, a, d in ((1, 1, 3), (2.76, .5, 5), (5.4, .25, 8)))
    return x * env(len(t), 0.003, 0.05)
def noise(dur): return np.random.uniform(-1, 1, int(SR * dur))
def lowpass(x, k=30): return np.convolve(x, np.ones(k) / k, mode="same")
def bandpass(x, lo, hi):
    X = np.fft.rfft(x); fr = np.fft.rfftfreq(len(x), 1 / SR); X[(fr < lo) | (fr > hi)] = 0; return np.fft.irfft(X, len(x))
def echo_fx(x, delay=0.18, fb=0.4, n=3):
    out = np.copy(x); d = int(SR * delay)
    for i in range(1, n + 1):
        sh = np.zeros_like(x); sh[i * d:] = x[:len(x) - i * d] * (fb ** i) if i * d < len(x) else 0; out += sh
    return out
def cat(*xs): return np.concatenate(xs)
def silence(d): return np.zeros(int(SR * d))
def norm(x, peak=0.8): m = np.max(np.abs(x)) or 1; return x / m * peak

def s_awake(): return norm(echo_fx(cat(*[glass(f, 0.5) for f in (392, 523, 659, 784, 1047)]), 0.2, .45, 3))
def s_laugh():
    parts = []
    for i, f in enumerate((520, 600, 560, 680, 640)):
        parts += [sweep(f, f * 1.25, 0.11, 8, 30) * env(int(SR * .11), .005, .04), silence(0.07)]
    return norm(cat(*parts))
def s_chirp(): return norm(cat(sweep(700, 1100, .08) * env(int(SR * .08), .005, .03), sweep(1100, 900, .1) * env(int(SR * .1), .005, .05)))
def s_hum(): return norm(sweep(210, 250, 1.3, 3, 1.5) * env(int(SR * 1.3), .2, .5) + .3 * sweep(420, 500, 1.3) * env(int(SR * 1.3), .3, .5), .6)
def s_sad(): return norm(echo_fx(cat(glass(494, .8), glass(392, 1.1)) + 0.0, .25, .35, 2) * 1.0, .7)
def s_fear():
    n = int(SR * 1.3); x = sweep(900, 1200, 1.3, 40, 11) * (0.6 + 0.4 * np.sin(2 * np.pi * 14 * t_(1.3)))
    return norm((x + .15 * bandpass(noise(1.3), 2000, 5000)) * env(n, .05, .3), .7)
def s_whisper():
    n = noise(1.6); x = bandpass(n, 900, 3500) * (0.5 + 0.5 * np.sin(2 * np.pi * 2.3 * t_(1.6)) ** 2)
    return norm(x * env(len(x), .15, .6), .55)
def s_breakdown():
    segs = []
    for i in range(9):
        d = random.choice((.08, .12, .2)); f = random.choice((200, 380, 760, 1500, 2300))
        s = sweep(f, f * random.choice((.5, 1.5, 2)), d, 30, 20); s = np.round(s * 6) / 6
        segs += [s * env(len(s), .003, .02), silence(random.choice((.02, .05))), bandpass(noise(.05), 800, 6000) * .6]
    return norm(cat(*segs), .75)
def s_cry():
    d = 2.0; t = t_(d); f = np.linspace(620, 330, len(t)); sob = 0.55 + 0.45 * np.abs(np.sin(2 * np.pi * 3.2 * t))
    return norm(sine(f, d, 14, 6.5) * sob * env(len(t), .1, .6), .7)
def s_glitch():
    segs = []
    for i in range(12):
        d = random.choice((.02, .04, .06)); segs += [bandpass(noise(d), 500, 8000) * random.uniform(.4, 1), silence(random.choice((0, .01, .03)))]
    return norm(cat(*segs), .7)
def s_beep(): return norm(np.sign(sine(880, .13)) * .5 * env(int(SR * .13), .004, .05), .5)
def s_old_device():
    d = 3.2; t = t_(d); hum = np.sin(2 * np.pi * 55 * t) * .5 + np.sin(2 * np.pi * 110 * t) * .25
    crack = np.zeros_like(t)
    for _ in range(30):
        i = random.randrange(0, len(t) - 400); crack[i:i + random.randint(20, 300)] += np.random.uniform(-1, 1, 1) * np.random.uniform(-1, 1, len(crack[i:i + 1]))[0:1]
    modem = np.zeros_like(t); i = int(SR * 1.3)
    for k in range(6):
        seg = sine(random.choice((1200, 1800, 2200)), .12); modem[i:i + len(seg)] += seg * .25; i += len(seg)
    x = hum + .3 * lowpass(noise(d), 4) + crack * .5 + modem
    return norm(x * env(len(t), .3, .6), .6)
def s_reset_start(): return norm(sweep(200, 900, 2.4, 20, 4) * env(int(SR * 2.4), .2, .5) * (0.7 + .3 * np.sin(2 * np.pi * 6 * t_(2.4))), .7)
def s_power_down(): return norm((sweep(900, 40, 2.2) + .4 * bandpass(noise(2.2), 100, 900)) * env(int(SR * 2.2), .05, .8), .75)

def pad(freqs, dur, vol=.2, lfo=.07, det=.003):
    t = t_(dur); x = np.zeros_like(t)
    for f in freqs:
        for dd in (1 - det, 1, 1 + det):
            x += np.sin(2 * np.pi * f * dd * t + random.random() * 6.28) * vol / 3
    x *= .6 + .4 * np.sin(2 * np.pi * lfo * t + random.random() * 6.28)
    return x * env(len(t), 4, 6)
def bells(notes, dur, every, vol=.25, decay=1):
    x = np.zeros(int(SR * dur)); t = 0.0
    while t < dur - 3:
        f = random.choice(notes); g = glass(f, 3) * vol * decay; i = int(SR * t); x[i:i + len(g)] += g[:len(x) - i]; t += random.uniform(*every)
    return x
def drone_low(f, dur, vol=.35):
    t = t_(dur); return (np.sin(2 * np.pi * f * t) + .5 * np.sin(2 * np.pi * f * 2.01 * t)) * vol * (.7 + .3 * np.sin(2 * np.pi * .05 * t))
def mus(kind, dur=64):
    n = int(SR * dur); N = lambda m: 440 * 2 ** ((m - 69) / 12)
    if kind == "day1":
        x = pad([N(60), N(64), N(67), N(71)], dur) + bells([N(m) for m in (72, 76, 79, 81, 84)], dur, (2, 4), .22)
    elif kind == "day2":
        x = pad([N(57), N(60), N(63), N(66)], dur, .22, .05, .008) + bells([N(m) for m in (81, 82, 87)], dur, (4, 8), .2) + .08 * bandpass(noise(dur), 3000, 6000) * (np.sin(2 * np.pi * .2 * t_(dur)) > .8)
    elif kind == "lab":
        x = drone_low(41, dur) + pad([N(45), N(51)], dur, .15, .04, .01)
        for _ in range(8):
            i = random.randrange(0, n - SR * 3); h = glass(random.choice((180, 233, 311)), 2.5) * .35; x[i:i + len(h)] += h
        x += .05 * lowpass(noise(dur), 12)
    elif kind == "day4":
        x = pad([N(57), N(60), N(64)], dur, .17, .04) + bells([N(m) for m in (69, 72, 76, 74, 67)], dur, (3, 6), .3)
    elif kind == "final":
        t = t_(dur); sw = np.sin(2 * np.pi * np.cumsum(np.linspace(55, 110, len(t))) / SR) * .3
        pulse = (np.sin(2 * np.pi * (np.linspace(.8, 2.4, len(t)) * t)) > 0.2) * np.sin(2 * np.pi * 55 * t) * .25
        x = sw + pulse + pad([N(48), N(51), N(54)], dur, .15, .1, .012) + bells([N(m) for m in (84, 85, 90)], dur, (2, 3), .18)
        x *= np.linspace(.5, 1, len(t))
    elif kind == "saved":
        x = pad([N(60), N(64), N(67), N(72)], dur, .22, .06) + bells([N(m) for m in (79, 84, 88, 91)], dur, (1.5, 3), .26)
    else:  # lost
        x = pad([N(57), N(64)], dur, .12, .03)
        for t0 in (4, 14, 26, 41):
            g = glass(N(69 if t0 < 30 else 64), 6) * .35; i = int(SR * t0); x[i:i + len(g)] += g[:n - i]
    return norm(x, .6)

# event -> (folder, filename, generator)
SOUND_EVENTS = {
    "echo_awake": ("echo", "awake", s_awake), "echo_laugh": ("echo", "laugh", s_laugh), "echo_chirp": ("echo", "chirp", s_chirp),
    "echo_hum": ("echo", "hum", s_hum), "echo_sad": ("echo", "sad", s_sad), "echo_fear": ("echo", "fear", s_fear),
    "echo_whisper": ("echo", "whisper", s_whisper), "echo_breakdown": ("echo", "breakdown", s_breakdown),
    "echo_cry": ("echo", "cry", s_cry), "echo_glitch": ("echo", "glitch", s_glitch),
    "terminal_beep": ("world", "terminal_beep", s_beep), "old_device_noise": ("world", "old_device_noise", s_old_device),
    "reset_start": ("world", "reset_start", s_reset_start), "power_down": ("world", "power_down", s_power_down),
    "music_day1": ("music", "day1", lambda: mus("day1")), "music_day2": ("music", "day2", lambda: mus("day2")),
    "music_lab": ("music", "lab", lambda: mus("lab")), "music_day4": ("music", "day4", lambda: mus("day4")),
    "music_final": ("music", "final", lambda: mus("final")), "music_saved": ("music", "saved", lambda: mus("saved")),
    "music_lost": ("music", "lost", lambda: mus("lost")),
}

def audio():
    base = os.path.join(A, "sounds")
    for ev, (folder, fname, gen) in SOUND_EVENTS.items():
        x = np.clip(gen(), -1, 1)
        d = os.path.join(base, folder); os.makedirs(d, exist_ok=True)
        wav = os.path.join(d, fname + ".wav"); ogg = os.path.join(d, fname + ".ogg")
        with wave.open(wav, "wb") as f:
            f.setnchannels(1); f.setsampwidth(2); f.setframerate(SR); f.writeframes((x * 32767).astype(np.int16).tobytes())
        r = subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-c:a", "libvorbis", "-q:a", "3", ogg])
        os.remove(wav)
        if r.returncode != 0:
            print("ffmpeg failed for", ev); sys.exit(1)
        print("sound", ev)

if __name__ == "__main__":
    entity_textures(); block_textures(); item_textures(); json_assets(); audio()
    print("done ->", os.path.abspath(OUT))
