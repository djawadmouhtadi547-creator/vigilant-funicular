package com.thelastplayer.registry;

import com.thelastplayer.TheLastPlayer;
import com.thelastplayer.entity.EchoEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, TheLastPlayer.MODID);

    public static final RegistryObject<EntityType<EchoEntity>> ECHO = ENTITIES.register("echo",
            () -> EntityType.Builder.<EchoEntity>of(EchoEntity::new, EntityClassification.CREATURE)
                    .sized(0.7F, 0.9F)
                    .clientTrackingRange(12)
                    .fireImmune()
                    .build(TheLastPlayer.MODID + ":echo"));

    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ECHO.get(), EchoEntity.createAttributes().build());
    }
}
