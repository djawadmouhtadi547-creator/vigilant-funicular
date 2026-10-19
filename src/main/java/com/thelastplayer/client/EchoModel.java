package com.thelastplayer.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.thelastplayer.entity.EchoEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

/**
 * ECHO: a floating glass creature. Body cube, crystal antenna, two floating crystal arms,
 * three hanging tail shards and a heart-light inside the chest (emissive layer draws it).
 * Texture layout (64x64): body 0,0 | heart 32,0 | crystal 0,16 | armL 10,16 | armR 20,16 | tails 30/40/50,16
 */
public class EchoModel extends EntityModel<EchoEntity> {
    private final ModelRenderer root;
    private final ModelRenderer crystal;
    private final ModelRenderer armL, armR;
    private final ModelRenderer tail1, tail2, tail3;

    public EchoModel() {
        texWidth = 64;
        texHeight = 64;

        root = new ModelRenderer(this);
        root.setPos(0.0F, 24.0F, 0.0F);

        ModelRenderer body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(0, 0).addBox(-4.0F, -14.0F, -4.0F, 8.0F, 8.0F, 8.0F);
        root.addChild(body);

        ModelRenderer heart = new ModelRenderer(this);
        heart.setPos(0.0F, 0.0F, 0.0F);
        heart.texOffs(32, 0).addBox(-2.0F, -12.0F, -1.0F, 4.0F, 4.0F, 2.0F);
        root.addChild(heart);

        crystal = new ModelRenderer(this);
        crystal.setPos(0.0F, -14.0F, 0.0F);
        crystal.texOffs(0, 16).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F);
        root.addChild(crystal);

        armL = new ModelRenderer(this);
        armL.setPos(5.5F, -11.0F, 0.0F);
        armL.texOffs(10, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F);
        root.addChild(armL);

        armR = new ModelRenderer(this);
        armR.setPos(-5.5F, -11.0F, 0.0F);
        armR.texOffs(20, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F);
        root.addChild(armR);

        tail1 = new ModelRenderer(this);
        tail1.setPos(-2.0F, -6.0F, 0.0F);
        tail1.texOffs(30, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F);
        root.addChild(tail1);

        tail2 = new ModelRenderer(this);
        tail2.setPos(2.0F, -6.0F, 1.0F);
        tail2.texOffs(40, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F);
        root.addChild(tail2);

        tail3 = new ModelRenderer(this);
        tail3.setPos(0.0F, -6.0F, -2.0F);
        tail3.texOffs(50, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F);
        root.addChild(tail3);
    }

    @Override
    public void setupAnim(EchoEntity e, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        int mood = e.getMood();
        float bobSpeed = mood == EchoEntity.MOOD_SCARED ? 0.35F : 0.1F;
        root.y = 24.0F + MathHelper.sin(age * bobSpeed) * 1.2F;
        root.x = 0.0F;
        if (mood == EchoEntity.MOOD_SCARED) root.x = MathHelper.sin(age * 2.3F) * 0.6F;
        if (mood == EchoEntity.MOOD_GLITCH) root.x = (((int) (age * 3.0F)) % 5 == 0) ? MathHelper.sin(age * 9.0F) * 1.4F : 0.0F;

        // head-ish turning: the whole body follows the look direction a little
        root.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.35F;

        float arm = mood == EchoEntity.MOOD_DIM ? 0.95F : mood == EchoEntity.MOOD_SAD ? 0.65F : 0.3F;
        float wave = mood == EchoEntity.MOOD_HAPPY ? 0.35F : 0.12F;
        armL.zRot = -(arm + MathHelper.sin(age * 0.15F) * wave);
        armR.zRot = arm + MathHelper.sin(age * 0.15F + 1.7F) * wave;

        crystal.yRot = age * 0.05F;
        crystal.xRot = MathHelper.sin(age * 0.07F) * 0.12F;

        tail1.xRot = MathHelper.sin(age * 0.12F) * 0.25F;
        tail2.zRot = MathHelper.sin(age * 0.11F + 1.0F) * 0.25F;
        tail3.xRot = -MathHelper.sin(age * 0.13F + 2.0F) * 0.25F;
    }

    @Override
    public void renderToBuffer(MatrixStack ms, IVertexBuilder buf, int light, int overlay, float r, float g, float b, float a) {
        root.render(ms, buf, light, overlay, r, g, b, a);
    }
}
