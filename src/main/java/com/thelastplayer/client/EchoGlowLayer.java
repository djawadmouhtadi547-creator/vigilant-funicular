package com.thelastplayer.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.thelastplayer.TheLastPlayer;
import com.thelastplayer.entity.EchoEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/** Emissive heart + eyes. The pulse and colour change with ECHO's mood ("animated texture"). */
public class EchoGlowLayer extends LayerRenderer<EchoEntity, EchoModel> {
    private static final ResourceLocation GLOW = new ResourceLocation(TheLastPlayer.MODID, "textures/entity/echo_glow.png");

    public EchoGlowLayer(IEntityRenderer<EchoEntity, EchoModel> parent) {
        super(parent);
    }

    @Override
    public void render(MatrixStack ms, IRenderTypeBuffer buffers, int light, EchoEntity e, float limbSwing, float limbSwingAmount,
                       float partial, float age, float netHeadYaw, float headPitch) {
        if (e.isInvisible()) return;
        float r, g, b, a;
        switch (e.getMood()) {
            case EchoEntity.MOOD_SAD:
                r = 0.35F; g = 0.55F; b = 1.0F; a = 0.55F + 0.25F * MathHelper.sin(age * 0.08F);
                break;
            case EchoEntity.MOOD_SCARED:
                r = 0.85F; g = 0.4F; b = 1.0F; a = 0.6F + 0.4F * MathHelper.sin(age * 0.6F);
                break;
            case EchoEntity.MOOD_GLITCH:
                boolean off = ((int) (age * 2.0F) % 4) == 0;
                r = 1.0F; g = 0.35F; b = 0.45F; a = off ? 0.05F : 0.9F;
                break;
            case EchoEntity.MOOD_DIM:
                r = 0.25F; g = 0.35F; b = 0.7F; a = 0.18F + 0.1F * MathHelper.sin(age * 0.05F);
                break;
            case EchoEntity.MOOD_CURIOUS:
                r = 0.55F; g = 0.95F; b = 1.0F; a = 0.7F + 0.3F * MathHelper.sin(age * 0.12F);
                break;
            default:
                r = 0.6F; g = 1.0F; b = 1.0F; a = 0.8F + 0.2F * MathHelper.sin(age * 0.18F);
                break;
        }
        IVertexBuilder vb = buffers.getBuffer(RenderType.eyes(GLOW));
        this.getParentModel().renderToBuffer(ms, vb, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r * a, g * a, b * a, 1.0F);
    }
}
