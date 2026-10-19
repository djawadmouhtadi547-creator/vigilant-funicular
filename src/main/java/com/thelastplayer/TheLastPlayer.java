package com.thelastplayer;

import com.thelastplayer.command.EchoCommand;
import com.thelastplayer.registry.ModBlocks;
import com.thelastplayer.registry.ModEntities;
import com.thelastplayer.registry.ModItems;
import com.thelastplayer.registry.ModSounds;
import com.thelastplayer.network.EchoNetwork;
import com.thelastplayer.story.StoryEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * THE LAST PLAYER - psychological horror / drama mod for Minecraft 1.16.5 (Forge 36.2.x).
 */
@Mod(TheLastPlayer.MODID)
public class TheLastPlayer {
    public static final String MODID = "thelastplayer";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public TheLastPlayer() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModSounds.SOUNDS.register(bus);
        EchoNetwork.register();
        bus.addListener(ModEntities::onAttributes);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EchoConfig.SPEC, "thelastplayer-common.toml");

        // static @SubscribeEvent handlers
        MinecraftForge.EVENT_BUS.register(StoryEvents.class);
        MinecraftForge.EVENT_BUS.register(EchoCommand.class);
    }
}
