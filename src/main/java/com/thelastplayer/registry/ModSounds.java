package com.thelastplayer.registry;

import com.thelastplayer.TheLastPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TheLastPlayer.MODID);

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(TheLastPlayer.MODID, name)));
    }

    // ECHO voice
    public static final RegistryObject<SoundEvent> ECHO_AWAKE = reg("echo_awake");
    public static final RegistryObject<SoundEvent> ECHO_LAUGH = reg("echo_laugh");
    public static final RegistryObject<SoundEvent> ECHO_CHIRP = reg("echo_chirp");
    public static final RegistryObject<SoundEvent> ECHO_HUM = reg("echo_hum");
    public static final RegistryObject<SoundEvent> ECHO_SAD = reg("echo_sad");
    public static final RegistryObject<SoundEvent> ECHO_FEAR = reg("echo_fear");
    public static final RegistryObject<SoundEvent> ECHO_WHISPER = reg("echo_whisper");
    public static final RegistryObject<SoundEvent> ECHO_BREAKDOWN = reg("echo_breakdown");
    public static final RegistryObject<SoundEvent> ECHO_CRY = reg("echo_cry");
    public static final RegistryObject<SoundEvent> ECHO_GLITCH = reg("echo_glitch");

    // World / machines
    public static final RegistryObject<SoundEvent> TERMINAL_BEEP = reg("terminal_beep");
    public static final RegistryObject<SoundEvent> OLD_DEVICE_NOISE = reg("old_device_noise");
    public static final RegistryObject<SoundEvent> RESET_START = reg("reset_start");
    public static final RegistryObject<SoundEvent> POWER_DOWN = reg("power_down");

    // Music (played by MusicDirector)
    public static final RegistryObject<SoundEvent> MUSIC_DAY1 = reg("music_day1");
    public static final RegistryObject<SoundEvent> MUSIC_DAY2 = reg("music_day2");
    public static final RegistryObject<SoundEvent> MUSIC_LAB = reg("music_lab");
    public static final RegistryObject<SoundEvent> MUSIC_DAY4 = reg("music_day4");
    public static final RegistryObject<SoundEvent> MUSIC_FINAL = reg("music_final");
    public static final RegistryObject<SoundEvent> MUSIC_SAVED = reg("music_saved");
    public static final RegistryObject<SoundEvent> MUSIC_LOST = reg("music_lost");
}
