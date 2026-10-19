package com.thelastplayer.story;

import com.thelastplayer.structure.BuildQueue;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/** Forge event hooks -> story. */
public class StoryEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Scheduler.tick();
        MinecraftServer s = ServerLifecycleHooks.getCurrentServer();
        if (s != null) BuildQueue.tick(s);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != LogicalSide.SERVER) return;
        if (e.player.tickCount % 20 != 0 || !(e.player instanceof ServerPlayerEntity)) return;
        StoryManager.tick((ServerPlayerEntity) e.player);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getPlayer() instanceof ServerPlayerEntity) StoryManager.onLogin((ServerPlayerEntity) e.getPlayer());
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent e) {
        StoryManager.onChat(e.getPlayer(), e.getMessage());
    }

    @SubscribeEvent
    public static void onWake(PlayerWakeUpEvent e) {
        if (e.getPlayer() instanceof ServerPlayerEntity) StoryManager.onWake((ServerPlayerEntity) e.getPlayer());
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        if (e.getPlayer() instanceof ServerPlayerEntity) StoryManager.onBreak((ServerPlayerEntity) e.getPlayer(), e.getState().getBlock());
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (e.getEntity() instanceof ServerPlayerEntity) StoryManager.onPlace((ServerPlayerEntity) e.getEntity());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (e.getEntityLiving() instanceof ServerPlayerEntity) StoryManager.onDeath((ServerPlayerEntity) e.getEntityLiving());
    }

    @SubscribeEvent
    public static void onStopped(FMLServerStoppedEvent e) {
        Scheduler.clear();
        BuildQueue.clear();
    }
}
