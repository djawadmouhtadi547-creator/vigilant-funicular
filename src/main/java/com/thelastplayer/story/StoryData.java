package com.thelastplayer.story;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Everything the story needs to remember, saved with the world. */
public class StoryData extends WorldSavedData {
    public static final String NAME = "thelastplayer_story";

    public boolean roomBuilt, started, hiddenQueued, labQueued;
    public int day, bond, finalPhase, placed, gyDevice, gyLab;
    public long dayStartTime;
    public UUID player, echo;
    public BlockPos devicePos, hiddenA, hiddenB, labPos, terminalPos, pedestalPos, homePos;
    public String pending = "";
    public String ending = "";
    public final Set<String> flags = new HashSet<>();
    public final Set<Integer> logs = new HashSet<>();
    public final List<String> memories = new ArrayList<>();
    public final List<Long> lamps = new ArrayList<>();

    public StoryData() {
        super(NAME);
    }

    public static StoryData get(MinecraftServer server) {
        ServerWorld w = server.overworld();
        return w.getDataStorage().computeIfAbsent(StoryData::new, NAME);
    }

    public boolean has(String f) { return flags.contains(f); }

    public void set(String f) {
        if (flags.add(f)) setDirty();
    }

    /** Remember an event once (also flags it). */
    public void remember(String key, String text) {
        if (flags.add("mem_" + key)) {
            memories.add(text);
            if (memories.size() > 20) memories.remove(0);
            setDirty();
        }
    }

    private static void putPos(CompoundNBT n, String k, BlockPos p) {
        if (p != null) n.put(k, NBTUtil.writeBlockPos(p));
    }

    private static BlockPos getPos(CompoundNBT n, String k) {
        return n.contains(k) ? NBTUtil.readBlockPos(n.getCompound(k)) : null;
    }

    @Override
    public void load(CompoundNBT n) {
        roomBuilt = n.getBoolean("roomBuilt");
        started = n.getBoolean("started");
        hiddenQueued = n.getBoolean("hiddenQueued");
        labQueued = n.getBoolean("labQueued");
        day = n.getInt("day");
        bond = n.getInt("bond");
        finalPhase = n.getInt("finalPhase");
        placed = n.getInt("placed");
        gyDevice = n.getInt("gyDevice");
        gyLab = n.getInt("gyLab");
        dayStartTime = n.getLong("dayStartTime");
        player = n.hasUUID("player") ? n.getUUID("player") : null;
        echo = n.hasUUID("echo") ? n.getUUID("echo") : null;
        devicePos = getPos(n, "devicePos");
        hiddenA = getPos(n, "hiddenA");
        hiddenB = getPos(n, "hiddenB");
        labPos = getPos(n, "labPos");
        terminalPos = getPos(n, "terminalPos");
        pedestalPos = getPos(n, "pedestalPos");
        homePos = getPos(n, "homePos");
        pending = n.getString("pending");
        ending = n.getString("ending");
        flags.clear();
        ListNBT fl = n.getList("flags", 8);
        for (int i = 0; i < fl.size(); i++) flags.add(fl.getString(i));
        logs.clear();
        for (int v : n.getIntArray("logs")) logs.add(v);
        memories.clear();
        ListNBT ml = n.getList("memories", 8);
        for (int i = 0; i < ml.size(); i++) memories.add(ml.getString(i));
        lamps.clear();
        for (long l : n.getLongArray("lamps")) lamps.add(l);
    }

    @Override
    public CompoundNBT save(CompoundNBT n) {
        n.putBoolean("roomBuilt", roomBuilt);
        n.putBoolean("started", started);
        n.putBoolean("hiddenQueued", hiddenQueued);
        n.putBoolean("labQueued", labQueued);
        n.putInt("day", day);
        n.putInt("bond", bond);
        n.putInt("finalPhase", finalPhase);
        n.putInt("placed", placed);
        n.putInt("gyDevice", gyDevice);
        n.putInt("gyLab", gyLab);
        n.putLong("dayStartTime", dayStartTime);
        if (player != null) n.putUUID("player", player);
        if (echo != null) n.putUUID("echo", echo);
        putPos(n, "devicePos", devicePos);
        putPos(n, "hiddenA", hiddenA);
        putPos(n, "hiddenB", hiddenB);
        putPos(n, "labPos", labPos);
        putPos(n, "terminalPos", terminalPos);
        putPos(n, "pedestalPos", pedestalPos);
        putPos(n, "homePos", homePos);
        n.putString("pending", pending);
        n.putString("ending", ending);
        ListNBT fl = new ListNBT();
        for (String f : flags) fl.add(StringNBT.valueOf(f));
        n.put("flags", fl);
        int[] la = new int[logs.size()];
        int i = 0;
        for (int v : logs) la[i++] = v;
        n.putIntArray("logs", la);
        ListNBT ml = new ListNBT();
        for (String m : memories) ml.add(StringNBT.valueOf(m));
        n.put("memories", ml);
        long[] lp = new long[lamps.size()];
        for (int j = 0; j < lp.length; j++) lp[j] = lamps.get(j);
        n.putLongArray("lamps", lp);
        return n;
    }
}
