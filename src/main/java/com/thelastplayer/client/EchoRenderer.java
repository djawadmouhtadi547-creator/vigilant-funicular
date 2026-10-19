package com.thelastplayer.client;

import com.thelastplayer.TheLastPlayer;
import com.thelastplayer.entity.EchoEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class EchoRenderer extends MobRenderer<EchoEntity, EchoModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TheLastPlayer.MODID, "textures/entity/echo.png");

    public EchoRenderer(EntityRendererManager manager) {
        super(manager, new EchoModel(), 0.25F);
        this.addLayer(new EchoGlowLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(EchoEntity entity) {
        return TEXTURE;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(EchoEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }
}
