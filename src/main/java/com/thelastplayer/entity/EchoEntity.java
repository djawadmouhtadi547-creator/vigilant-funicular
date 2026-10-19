package com.thelastplayer.entity;

import com.thelastplayer.registry.ModSounds;
import com.thelastplayer.story.StoryManager;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.EnumSet;

/** ECHO - the floating blue-glass creature. */
public class EchoEntity extends CreatureEntity {
    public static final int MOOD_HAPPY = 0, MOOD_CURIOUS = 1, MOOD_SAD = 2, MOOD_SCARED = 3, MOOD_GLITCH = 4, MOOD_DIM = 5;

    private static final DataParameter<Integer> MOOD = EntityDataManager.defineId(EchoEntity.class, DataSerializers.INT);
    private static final DataParameter<Boolean> WAITING = EntityDataManager.defineId(EchoEntity.class, DataSerializers.BOOLEAN);

    private int gazeTicks = 0;

    public EchoEntity(EntityType<? extends CreatureEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlyingMovementController(this, 20, true);
        this.setNoGravity(true);
        this.setPersistenceRequired();
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return CreatureEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MOOD, MOOD_HAPPY);
        this.entityData.define(WAITING, false);
    }

    @Override
    protected PathNavigator createNavigation(World world) {
        FlyingPathNavigator nav = new FlyingPathNavigator(this, world);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FollowPlayerGoal(this));
        this.goalSelector.addGoal(2, new LookAtGoal(this, PlayerEntity.class, 10.0F));
        this.goalSelector.addGoal(3, new LookRandomlyGoal(this));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (gazeTicks > 0) {
            gazeTicks--;
            this.getLookControl().setLookAt(getX(), getY() + 40.0D, getZ());
        }
    }

    // ---------- story-facing API ----------
    public int getMood() { return this.entityData.get(MOOD); }
    public void setMood(int mood) { this.entityData.set(MOOD, mood); }
    public boolean isWaiting() { return this.entityData.get(WAITING); }
    public void setWaiting(boolean w) { this.entityData.set(WAITING, w); }
    public void gazeAtSky(int ticks) { this.gazeTicks = ticks; }

    public void playVoice(SoundEvent sound) {
        if (sound != null && !level.isClientSide) {
            level.playSound(null, getX(), getY(), getZ(), sound, SoundCategory.VOICE, 1.2F, 1.0F);
        }
    }

    // ---------- interaction ----------
    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity sp = (ServerPlayerEntity) player;
            if (!stack.isEmpty() && (stack.getItem().is(ItemTags.SMALL_FLOWERS) || stack.getItem() == Items.ROSE_BUSH)) {
                if (!player.abilities.instabuild) stack.shrink(1);
                StoryManager.onGift(sp, this);
                return ActionResultType.CONSUME;
            }
            if (stack.isEmpty() && hand == Hand.MAIN_HAND) {
                StoryManager.onPoke(sp, this);
                return ActionResultType.CONSUME;
            }
        }
        return ActionResultType.PASS;
    }

    // ---------- ECHO is a story character: it cannot die by normal means ----------
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.isBypassInvul()) return super.hurt(source, amount);
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    public boolean canBeLeashed(PlayerEntity player) { return false; }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntitySize size) { return 0.5F; }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Mood", getMood());
        tag.putBoolean("Waiting", isWaiting());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);
        setMood(tag.getInt("Mood"));
        setWaiting(tag.getBoolean("Waiting"));
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // ---------- goals ----------
    static class FollowPlayerGoal extends Goal {
        private final EchoEntity echo;
        private PlayerEntity target;
        private int recalc;

        FollowPlayerGoal(EchoEntity echo) {
            this.echo = echo;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (echo.isWaiting()) return false;
            target = echo.level.getNearestPlayer(echo, 64.0D);
            return target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !echo.isWaiting() && target != null && target.isAlive();
        }

        @Override
        public void tick() {
            if (target == null) return;
            double dist = echo.distanceTo(target);
            if (dist > 24.0D) {
                echo.moveTo(target.getX() + 1.0D, target.getY() + 1.2D, target.getZ() + 1.0D, echo.yRot, echo.xRot);
                echo.getNavigation().stop();
                return;
            }
            if (--recalc <= 0) {
                recalc = 8;
                if (dist > 3.5D) {
                    echo.getNavigation().moveTo(target.getX(), target.getY() + 1.4D, target.getZ(), 1.1D);
                } else if (dist < 2.0D) {
                    echo.getNavigation().stop();
                }
            }
        }

        @Override
        public void stop() {
            echo.getNavigation().stop();
        }
    }
}
