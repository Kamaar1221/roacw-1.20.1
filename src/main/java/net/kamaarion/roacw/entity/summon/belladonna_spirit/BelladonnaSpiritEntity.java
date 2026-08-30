package net.kamaarion.roacw.entity.summon.belladonna_spirit;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.kamaarion.roacw.entity.projectile.belladonna_petal.BelladonnaPetalEntity;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class BelladonnaSpiritEntity extends PathfinderMob implements RangedAttackMob, GeoEntity, IMagicSummon {

    private static final DustParticleOptions BELLADONNA_DUST =
            new DustParticleOptions(new org.joml.Vector3f(0.58F, 0.29F, 0.72F), 1.0F);

    private static final int ATTACK_RELEASE_TICK = 12; // TODO tune to match the animation's actual throw frame

    // Only re-adopt the owner's last-hurt-mob as our target if they hit it within this many ticks.
    private static final int OWNER_TARGET_FRESHNESS_TICKS = 100; // 5s

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private float spellPower = 2.0F;

    @Nullable
    private LivingEntity pendingAttackTarget;
    private int attackWindupTicks = -1;

    public BelladonnaSpiritEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.moveControl = new BelladonnaSpiritMoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BelladonnaSpiritRangedAttackGoal(this, 0.3D, 20, 14.0F)); // TODO tune speed
        this.goalSelector.addGoal(2, new BelladonnaSpiritWanderGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    public void setSpellPower(float spellPower) {
        this.spellPower = spellPower;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("SpellPower", this.spellPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SpellPower")) {
            this.spellPower = tag.getFloat("SpellPower");
        }
    }

    @Nullable
    public LivingEntity getSummonOwner() {
        var owner = SummonManager.getOwner(this);
        return owner instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);

        if (!this.level().isClientSide) {
            LivingEntity owner = getSummonOwner();
            if (owner != null && this.getTarget() == null) {
                LivingEntity ownerTarget = owner.getLastHurtMob();

                boolean recentlyHurt = owner.tickCount - owner.getLastHurtMobTimestamp()
                        <= OWNER_TARGET_FRESHNESS_TICKS;

                if (ownerTarget != null && recentlyHurt && ownerTarget.isAlive() && this.distanceToSqr(ownerTarget) < 900) {
                    this.setTarget(ownerTarget);
                }
            }

            if (this.pendingAttackTarget != null) {
                if (!this.pendingAttackTarget.isAlive()) {
                    this.pendingAttackTarget = null;
                    this.attackWindupTicks = -1;
                } else {
                    this.attackWindupTicks++;
                    if (this.attackWindupTicks >= ATTACK_RELEASE_TICK) {
                        releasePetal(this.pendingAttackTarget);
                        this.pendingAttackTarget = null;
                        this.attackWindupTicks = -1;
                    }
                }
            }

            /*if (this.tickCount % 10 == 0 && this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(BELLADONNA_DUST,
                        this.getX(), this.getY(0.6D), this.getZ(),
                        2, 0.2D, 0.2D, 0.2D, 0.0D);
            }*/
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        // called by the goal to START an attack - triggers the windup animation,
        // actual petal fire is delayed to match the animation via tick()
        this.triggerAnim("controller", "attack");
        this.pendingAttackTarget = target;
        this.attackWindupTicks = 0;
    }

    private static final float PETAL_SPEED = 0.6F; // TODO tune - compare against PlagueRocketEntity's shoot() velocity for reference

    private void releasePetal(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        LivingEntity owner = getSummonOwner();

        Vec3 look = this.getLookAngle().normalize();
        Vec3 right = new Vec3(-look.z, 0.0D, look.x).normalize();

        Vec3 spawnPos = this.position()
                .add(0.0D, 0.55D, 0.0D)
                .add(right.scale(0.15D))
                .add(look.scale(0.65D));

        Vec3 direction = target.getEyePosition()
                .subtract(spawnPos)
                .normalize()
                .scale(PETAL_SPEED); // scale down from a 1.0-magnitude unit vector to an actual usable speed

        BelladonnaPetalEntity petal = new BelladonnaPetalEntity(
                ROACWEntityRegistry.BELLADONNA_PETAL.get(),
                this.level(),
                this,
                owner,
                spawnPos,
                direction
        );
        petal.setOldPosAndRot();
        petal.setDamage(this.spellPower);

        serverLevel.playSound(null, this.blockPosition(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                SoundSource.NEUTRAL, 0.5F, 1.4F);
        serverLevel.addFreshEntity(petal);
    }


    @Override
    public void onUnSummon() {
        // called by SummonManager when duration expires, or on anti-magic dispel / manual recast-dismiss
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(BELLADONNA_DUST,
                    this.getX(), this.getY(1.0D), this.getZ(),
                    24, 0.3D, 0.4D, 0.3D, 0.02D);
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.SWEET_BERRY_BUSH_BREAK,
                    SoundSource.NEUTRAL, 0.7F, 0.6F);
        }
        this.discard();
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        onRemovedHelper(this); // cleans up SummonManager's tracking maps for ANY removal (killed, unloaded, etc.)
    }

    @Override
    public void die(DamageSource damageSource) {
        onDeathHelper(); // sends the summoner a death message, if applicable
        super.die(damageSource);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (shouldIgnoreDamage(source)) {
            return false; // blocks friendly fire from the owner/allied summons when CAN_ATTACK_OWN_SUMMONS is off
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSq) {
        return false; // SummonManager controls removal via duration, not distance
    }

    // --- GeckoLib ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<BelladonnaSpiritEntity> controller =
                new AnimationController<>(this, "controller", 5, this::animationPredicate);
        controller.triggerableAnim("attack", RawAnimation.begin().thenPlay("attack"));
        controllers.add(controller);
    }

    private PlayState animationPredicate(AnimationState<BelladonnaSpiritEntity> state) {
        state.getController().setAnimation(RawAnimation.begin().thenLoop("Idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}