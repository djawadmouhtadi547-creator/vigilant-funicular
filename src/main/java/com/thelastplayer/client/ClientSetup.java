package com.thelastplayer.client;

import com.thelastplayer.TheLastPlayer;
import com.thelastplayer.registry.ModBlocks;
import com.thelastplayer.registry.ModEntities;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TheLastPlayer.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.ECHO.get(), EchoRenderer::new);
        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(ModBlocks.LAB_LAMP.get(), RenderType.cutout()));
    }
}
