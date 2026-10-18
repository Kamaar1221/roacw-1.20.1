package net.kamaarion.roacw.entity.summon.belladonna_spirit;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.kamaarion.roacw.entity.projectile.belladonna_petal.BelladonnaPetalEntity;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
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

    private static final int ATTACK_RELEASE_TICK = 16;
    private static final int OWNER_TARGET_FRESHNESS_TICKS = 100;
    private static final int ATTACK_INTERVAL_TICKS = 18;
    private static final float PETAL_SPEED = 1.0F;

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
        this.goalSelector.addGoal(1, new BelladonnaSpiritAttackGoal(this, 0.3D, ATTACK_INTERVAL_TICKS, 14.0F));
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
        Entity owner = SummonManager.getOwner(this);
        return owner instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);

        if (!this.level().isClientSide) {
            LivingEntity owner = this.getSummonOwner();

            if (owner != null && this.getTarget() == null) {
                LivingEntity ownerTarget = owner.getLastHurtMob();

                boolean recentlyHurt = owner.tickCount - owner.getLastHurtMobTimestamp()
                        <= OWNER_TARGET_FRESHNESS_TICKS;

                if (ownerTarget != null
                        && recentlyHurt
                        && ownerTarget.isAlive()
                        && this.distanceToSqr(ownerTarget) < 900.0D) {
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
                        this.releasePetal(this.pendingAttackTarget);
                        this.pendingAttackTarget = null;
                        this.attackWindupTicks = -1;
                    }
                }
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        this.triggerAnim("controller", "attack");
        this.pendingAttackTarget = target;
        this.attackWindupTicks = 0;
    }

    private void releasePetal(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity owner = this.getSummonOwner();

        Vec3 look = this.getLookAngle().normalize();
        Vec3 right = new Vec3(-look.z, 0.0D, look.x).normalize();

        Vec3 spawnPos = this.position()
                .add(0.0D, 0.55D, 0.0D)
                .add(right.scale(0.15D))
                .add(look.scale(0.65D));

        Vec3 direction = target.getEyePosition()
                .subtract(spawnPos)
                .normalize()
                .scale(PETAL_SPEED);

        BelladonnaPetalEntity petal = new BelladonnaPetalEntity(
                ROACWEntityRegistry.BELLADONNA_PETAL.get(),
                this.level(),
                this,
                owner,
                spawnPos,
                direction,
                ROACWSpellRegistry.SUMMON_BELLADONNA_SPIRIT.get()
        );

        petal.setOldPosAndRot();
        petal.setDamage(this.spellPower);

        serverLevel.playSound(
                null,
                this.blockPosition(),
                SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                SoundSource.NEUTRAL,
                0.5F,
                1.4F
        );

        serverLevel.addFreshEntity(petal);
    }

    @Override
    public void onUnSummon() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(
                            ParticleTypes.BLOCK,
                            Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState()
                    ),
                    this.getX(),
                    this.getY(1.0D),
                    this.getZ(),
                    30,
                    0.35D,
                    0.45D,
                    0.35D,
                    0.05D
            );

            serverLevel.sendParticles(
                    ParticleTypes.COMPOSTER,
                    this.getX(),
                    this.getY(1.0D),
                    this.getZ(),
                    12,
                    0.3D,
                    0.4D,
                    0.3D,
                    0.02D
            );
        }

        this.discard();
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        onRemovedHelper(this);
    }

    @Override
    public void die(DamageSource damageSource) {
        onDeathHelper();
        super.die(damageSource);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (shouldIgnoreDamage(source)) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSq) {
        return false;
    }

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
        return this.cache;
    }
}