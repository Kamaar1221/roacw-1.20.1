package net.kamaarion.roacw.entity.projectile.mars_missile;

import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class MarsMissileEntity extends AbstractHurtingProjectile implements GeoEntity {

    private static final EntityDataAccessor<Float> DATA_MISSILE_DAMAGE =
            SynchedEntityData.defineId(
                    MarsMissileEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Boolean> DATA_REFLECTED =
            SynchedEntityData.defineId(
                    MarsMissileEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final float MAX_TURN_DEGREES_PER_TICK = 25.0F;
    private static final double SPEED = 1.0D;
    private static final int MAX_LIFETIME = 200;
    private static final int HOMING_DELAY_TICKS = 20;
    private static final float DEFAULT_DAMAGE = 10.0F;
    private static final double EXPLOSION_RADIUS = 4.0D;
    private static final float EXPLOSION_DAMAGE_MULTIPLIER = 0.5F;
    private static final double VISUAL_CENTER_HEIGHT = 0.0D;
    private static final double TRAIL_FLAME_BACK_DISTANCE = 1.08D;
    private static final double TRAIL_SMOKE_BACK_DISTANCE = 1.28D;
    private static final double TRAIL_SPARK_BACK_DISTANCE = 1.13D;
    private static final double TRAIL_FLAME_SPREAD = 0.035D;
    private static final double TRAIL_SMOKE_SPREAD = 0.06D;
    private static final double TRAIL_SPARK_SPREAD = 0.025D;
    private static final double TRAIL_FLAME_SPEED = 0.08D;
    private static final double TRAIL_SMOKE_SPEED = 0.035D;
    private static final double TRAIL_SPARK_SPEED = 0.12D;
    private static final float ELECTRIC_SPARK_CHANCE = 0.22F;

    @Nullable
    private LivingEntity target;

    private int life;

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    public MarsMissileEntity(
            EntityType<? extends MarsMissileEntity> type,
            Level level
    ) {
        super(type, level);
        this.noPhysics = false;
    }

    public MarsMissileEntity(
            Level level,
            LivingEntity shooter,
            LivingEntity target,
            float damage,
            Vec3 spawnPosition
    ) {
        this(level, shooter, target, damage, spawnPosition, null);
    }

    public MarsMissileEntity(
            Level level,
            LivingEntity shooter,
            LivingEntity target,
            float damage,
            Vec3 spawnPosition,
            @Nullable Vec3 launchDirection
    ) {
        this(ROACWEntityRegistry.MARS_MISSILE.get(), level);

        this.setOwner(shooter);
        this.target = target;
        this.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);
        this.setMissileDamage(damage);
        this.setReflected(false);

        Vec3 direction;

        if (launchDirection != null && launchDirection.lengthSqr() > 1.0E-8D) {
            direction = launchDirection;
        } else if (target != null && target.isAlive()) {
            Vec3 targetPosition = target.position().add(
                    0.0D,
                    target.getBbHeight() * 0.5D,
                    0.0D
            );

            direction = targetPosition.subtract(spawnPosition);
        } else {
            direction = shooter.getLookAngle();
        }

        if (direction.lengthSqr() < 1.0E-8D) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        }

        this.setDeltaMovement(direction.normalize().scale(SPEED));
        this.updateRotationFromVelocity();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(DATA_MISSILE_DAMAGE, DEFAULT_DAMAGE);
        this.entityData.define(DATA_REFLECTED, false);
    }

    public float getMissileDamage() {
        return this.entityData.get(DATA_MISSILE_DAMAGE);
    }

    public void setMissileDamage(float damage) {
        this.entityData.set(DATA_MISSILE_DAMAGE, damage);
    }

    public boolean isReflected() {
        return this.entityData.get(DATA_REFLECTED);
    }

    public void setReflected(boolean reflected) {
        this.entityData.set(DATA_REFLECTED, reflected);
    }

    @Override
    public void tick() {
        this.clearFire();
        super.baseTick();

        if (this.level().isClientSide) {
            Vec3 clientMovement = this.getDeltaMovement();

            if (clientMovement.lengthSqr() > 1.0E-8D) {
                this.move(MoverType.SELF, clientMovement);
                this.updateRotationFromVelocity();
            }

            return;
        }

        this.life++;

        if (this.life > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (!this.isReflected() && this.life > HOMING_DELAY_TICKS) {
            if (!this.isValidTarget(this.target)) {
                this.target = null;
            }

            if (this.target != null) {
                this.homeTowards(this.target);
            }
        }

        Vec3 movement = this.getDeltaMovement();

        if (movement.lengthSqr() < 1.0E-8D) {
            movement = new Vec3(0.0D, 0.0D, SPEED);
            this.setDeltaMovement(movement);
        }

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(
                this,
                this::canHitEntity
        );

        Vec3 nextPosition = this.position().add(movement);

        if (hitResult.getType() != HitResult.Type.MISS) {
            nextPosition = hitResult.getLocation();
        }

        this.setPos(nextPosition.x, nextPosition.y, nextPosition.z);
        this.updateRotationFromVelocity();

        movement = this.getDeltaMovement();

        if (movement.lengthSqr() > 1.0E-8D) {
            this.setDeltaMovement(movement.normalize().scale(SPEED));
        }

        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
            return;
        }

        this.spawnTrailParticles();
    }

    private void homeTowards(@Nullable LivingEntity target) {
        if (!this.isValidTarget(target)) {
            return;
        }

        Vec3 currentVelocity = this.getDeltaMovement();

        if (currentVelocity.lengthSqr() < 1.0E-8D) {
            return;
        }

        Vec3 targetPosition = target.position().add(
                0.0D,
                target.getBbHeight() * 0.5D,
                0.0D
        );

        Vec3 toTarget = targetPosition.subtract(this.position());

        if (toTarget.lengthSqr() < 1.0E-8D) {
            return;
        }

        Vec3 currentDirection = currentVelocity.normalize();
        Vec3 targetDirection = toTarget.normalize();

        double dot = Mth.clamp(
                currentDirection.dot(targetDirection),
                -1.0D,
                1.0D
        );

        double angle = Math.acos(dot);
        double maxTurnRadians = Math.toRadians(MAX_TURN_DEGREES_PER_TICK);
        Vec3 newDirection;

        if (angle <= maxTurnRadians) {
            newDirection = targetDirection;
        } else {
            Vec3 axis = currentDirection.cross(targetDirection);

            if (axis.lengthSqr() < 1.0E-8D) {
                newDirection = currentDirection;
            } else {
                axis = axis.normalize();

                double cos = Math.cos(maxTurnRadians);
                double sin = Math.sin(maxTurnRadians);

                newDirection = currentDirection
                        .scale(cos)
                        .add(axis.cross(currentDirection).scale(sin))
                        .add(axis.scale(
                                axis.dot(currentDirection) * (1.0D - cos)
                        ))
                        .normalize();
            }
        }

        this.setDeltaMovement(newDirection.scale(SPEED));
    }

    private boolean isValidTarget(@Nullable LivingEntity entity) {
        return entity != null
                && entity.isAlive()
                && !entity.isRemoved()
                && entity != this.getOwner();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) {
            return false;
        }

        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        if (livingEntity == this.getOwner()) {
            return false;
        }

        if (!this.isReflected()) {
            return livingEntity == this.target;
        }

        return true;
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY
                && result instanceof EntityHitResult entityHitResult
                && entityHitResult.getEntity() instanceof LivingEntity livingEntity) {

            if (!this.isReflected()
                    && livingEntity.isBlocking()
                    && livingEntity.getUseItem().is(Items.SHIELD)) {
                this.reflectFromShield(livingEntity);
                return;
            }

            if (this.isReflected() || livingEntity == this.target) {
                this.hitTarget(livingEntity);
                return;
            }
        }

        this.discard();
    }

    private void reflectFromShield(LivingEntity blocker) {
        Vec3 incoming = this.getDeltaMovement();

        if (incoming.lengthSqr() < 1.0E-8D) {
            incoming = blocker.getLookAngle().scale(-1.0D);
        }

        incoming = incoming.normalize();

        Vec3 normal = blocker.getLookAngle();

        if (normal.lengthSqr() < 1.0E-8D) {
            normal = new Vec3(0.0D, 0.0D, 1.0D);
        }

        normal = normal.normalize();

        Vec3 reflected = incoming.subtract(
                normal.scale(2.0D * incoming.dot(normal))
        );

        if (reflected.lengthSqr() < 1.0E-8D) {
            reflected = normal.scale(-1.0D);
        }

        reflected = reflected.normalize();

        this.target = null;
        this.setReflected(true);
        this.setDeltaMovement(reflected.scale(SPEED));
        this.updateRotationFromVelocity();
    }

    private void hitTarget(LivingEntity target) {
        target.invulnerableTime = 0;

        LivingEntity owner = this.getOwner() instanceof LivingEntity livingOwner
                ? livingOwner
                : null;

        target.hurt(
                this.damageSources().mobProjectile(this, owner),
                this.getMissileDamage()
        );

        this.explode(target);
    }

    private void explode(LivingEntity directTarget) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.discard();
            return;
        }

        float areaDamage = this.getMissileDamage()
                * EXPLOSION_DAMAGE_MULTIPLIER;

        LivingEntity owner = this.getOwner() instanceof LivingEntity livingOwner
                ? livingOwner
                : null;

        for (LivingEntity nearby : serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(EXPLOSION_RADIUS),
                entity -> entity.isAlive()
                        && !entity.isRemoved()
                        && entity != owner
                        && entity != directTarget
        )) {
            double distance = this.distanceTo(nearby);

            if (distance > EXPLOSION_RADIUS) {
                continue;
            }

            float falloff = (float) (1.0D - distance / EXPLOSION_RADIUS);
            float damage = areaDamage * falloff;

            if (damage <= 0.0F) {
                continue;
            }

            nearby.invulnerableTime = 0;

            nearby.hurt(
                    this.damageSources().mobProjectile(this, owner),
                    damage
            );
        }

        SoundEvent impactSound = switch (this.random.nextInt(3)) {
            case 0 -> ROACWSoundRegistry.MARS_MISSILE_EXPLOSION_1.get();
            case 1 -> ROACWSoundRegistry.MARS_MISSILE_EXPLOSION_2.get();
            default -> ROACWSoundRegistry.MARS_MISSILE_EXPLOSION_3.get();
        };

        serverLevel.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                impactSound,
                SoundSource.HOSTILE,
                2.0F,
                1.0F
        );

        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                this.getX(),
                this.getY(),
                this.getZ(),
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );

        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                this.getX(),
                this.getY(),
                this.getZ(),
                12,
                0.35D,
                0.35D,
                0.35D,
                0.08D
        );

        this.discard();
    }

    private void spawnTrailParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 velocity = this.getDeltaMovement();

        if (velocity.lengthSqr() < 1.0E-8D) {
            return;
        }

        Vec3 forward = velocity.normalize();
        Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = forward.cross(worldUp);

        if (right.lengthSqr() < 1.0E-8D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        Vec3 up = right.cross(forward).normalize();

        Vec3 visualOrigin = this.position().add(
                up.scale(VISUAL_CENTER_HEIGHT)
        );

        Vec3 flamePosition = visualOrigin.subtract(
                forward.scale(TRAIL_FLAME_BACK_DISTANCE)
        );

        flamePosition = flamePosition.add(
                right.scale((this.random.nextDouble() - 0.5D) * TRAIL_FLAME_SPREAD)
        );

        flamePosition = flamePosition.add(
                up.scale((this.random.nextDouble() - 0.5D) * TRAIL_FLAME_SPREAD)
        );

        Vec3 flameVelocity = forward.scale(-TRAIL_FLAME_SPEED);

        serverLevel.sendParticles(
                ParticleTypes.FLAME,
                flamePosition.x,
                flamePosition.y,
                flamePosition.z,
                1,
                flameVelocity.x,
                flameVelocity.y,
                flameVelocity.z,
                0.0D
        );

        Vec3 smokePosition = visualOrigin.subtract(
                forward.scale(TRAIL_SMOKE_BACK_DISTANCE)
        );

        smokePosition = smokePosition.add(
                right.scale((this.random.nextDouble() - 0.5D) * TRAIL_SMOKE_SPREAD)
        );

        smokePosition = smokePosition.add(
                up.scale((this.random.nextDouble() - 0.5D) * TRAIL_SMOKE_SPREAD)
        );

        Vec3 smokeVelocity = forward.scale(-TRAIL_SMOKE_SPEED);

        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                smokePosition.x,
                smokePosition.y,
                smokePosition.z,
                1,
                smokeVelocity.x,
                smokeVelocity.y,
                smokeVelocity.z,
                0.0D
        );

        if (this.random.nextFloat() < ELECTRIC_SPARK_CHANCE) {
            Vec3 sparkPosition = visualOrigin.subtract(
                    forward.scale(TRAIL_SPARK_BACK_DISTANCE)
            );

            sparkPosition = sparkPosition.add(
                    right.scale((this.random.nextDouble() - 0.5D) * TRAIL_SPARK_SPREAD)
            );

            sparkPosition = sparkPosition.add(
                    up.scale((this.random.nextDouble() - 0.5D) * TRAIL_SPARK_SPREAD)
            );

            Vec3 sparkVelocity = forward.scale(-TRAIL_SPARK_SPEED);

            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    sparkPosition.x,
                    sparkPosition.y,
                    sparkPosition.z,
                    1,
                    sparkVelocity.x,
                    sparkVelocity.y,
                    sparkVelocity.z,
                    0.0D
            );
        }
    }

    private void updateRotationFromVelocity() {
        Vec3 velocity = this.getDeltaMovement();

        if (velocity.lengthSqr() < 1.0E-8D) {
            return;
        }

        double horizontal = Math.sqrt(
                velocity.x * velocity.x + velocity.z * velocity.z
        );

        float yaw = (float) (
                Mth.atan2(velocity.z, velocity.x) * (180.0D / Math.PI) - 90.0D
        );

        float pitch = (float) (
                -Mth.atan2(velocity.y, horizontal) * (180.0D / Math.PI)
        );

        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public void lerpTo(
            double x,
            double y,
            double z,
            float yRot,
            float xRot,
            int lerpSteps,
            boolean teleport
    ) {
        this.setPos(x, y, z);
        this.setYRot(yRot);
        this.setXRot(xRot);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("MissileDamage", this.getMissileDamage());
        tag.putBoolean("Reflected", this.isReflected());
        tag.putInt("Life", this.life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setMissileDamage(tag.getFloat("MissileDamage"));
        this.setReflected(tag.getBoolean("Reflected"));
        this.life = tag.getInt("Life");
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }
}