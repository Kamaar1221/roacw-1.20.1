package net.kamaarion.roacw.entity.projectile.endo_hydra_beam;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.kamaarion.roacw.entity.summon.endo_hydra.EndoHydraEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;

public class EndoHydraBeamEntity extends Entity implements GeoEntity {

    private static final EntityDataAccessor<Float> DATA_START_X =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_START_Y =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_START_Z =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_DIRECTION_X =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_DIRECTION_Y =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_DIRECTION_Z =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_LENGTH =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_TARGET_X =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_TARGET_Y =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_TARGET_Z =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Integer> DATA_HEAD_INDEX =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_HYDRA_ID =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_TARGET_ENTITY_ID =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> DATA_DAMAGE =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Boolean> DATA_FULL_HEAD_ATTACK =
            SynchedEntityData.defineId(EndoHydraBeamEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double MAX_BEAM_LENGTH = 24.0D;
    private static final double BEAM_RADIUS = 0.75D;
    private static final int BEAM_LIFETIME = 30;

    private static final float VOIDFROST_CHANCE = 0.25F;
    private static final int VOIDFROST_DURATION = 100;
    private static final int VOIDFROST_MAX_AMPLIFIER = 2;

    private static final double MOUTH_FOLLOW_SMOOTHING = 0.35D;

    private static final RawAnimation BEAM_EXPANSION =
            RawAnimation.begin().thenPlay("BeamExpansion");

    private int beamAge;
    private boolean beamProcessed;

    @Nullable
    private EndoHydraEntity hydra;

    @Nullable
    private AbstractSpell damageSpell;

    @Nullable
    private Vec3 clientVisualStart;

    @Nullable
    private Vec3 clientVisualDirection;

    private double clientVisualLength;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public EndoHydraBeamEntity(EntityType<? extends EndoHydraBeamEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_START_X, 0.0F);
        this.entityData.define(DATA_START_Y, 0.0F);
        this.entityData.define(DATA_START_Z, 0.0F);
        this.entityData.define(DATA_DIRECTION_X, 0.0F);
        this.entityData.define(DATA_DIRECTION_Y, 0.0F);
        this.entityData.define(DATA_DIRECTION_Z, 0.0F);
        this.entityData.define(DATA_LENGTH, 0.0F);
        this.entityData.define(DATA_TARGET_X, 0.0F);
        this.entityData.define(DATA_TARGET_Y, 0.0F);
        this.entityData.define(DATA_TARGET_Z, 0.0F);
        this.entityData.define(DATA_HEAD_INDEX, -1);
        this.entityData.define(DATA_HYDRA_ID, -1);
        this.entityData.define(DATA_TARGET_ENTITY_ID, -1);
        this.entityData.define(DATA_DAMAGE, 15.0F);
        this.entityData.define(DATA_FULL_HEAD_ATTACK, false);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "beam_expansion_controller",
                0,
                this::animationPredicate
        ));
    }

    private software.bernie.geckolib.core.object.PlayState animationPredicate(
            AnimationState<EndoHydraBeamEntity> state
    ) {
        state.getController().setAnimation(BEAM_EXPANSION);
        return software.bernie.geckolib.core.object.PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void setupBeam(
            EndoHydraEntity hydra,
            Vec3 start,
            Vec3 direction,
            int headIndex,
            Vec3 targetPosition,
            float damage,
            boolean fullHeadAttack,
            AbstractSpell damageSpell
    ) {
        this.hydra = hydra;
        this.damageSpell = damageSpell;

        this.entityData.set(DATA_HYDRA_ID, hydra.getId());
        this.entityData.set(DATA_HEAD_INDEX, headIndex);
        this.entityData.set(DATA_DAMAGE, Math.max(0.0F, damage));
        this.entityData.set(DATA_FULL_HEAD_ATTACK, fullHeadAttack);

        this.entityData.set(DATA_TARGET_X, (float) targetPosition.x);
        this.entityData.set(DATA_TARGET_Y, (float) targetPosition.y);
        this.entityData.set(DATA_TARGET_Z, (float) targetPosition.z);

        Vec3 normalizedDirection = direction.lengthSqr() > 0.000001D
                ? direction.normalize()
                : Vec3.ZERO;

        this.entityData.set(DATA_START_X, (float) start.x);
        this.entityData.set(DATA_START_Y, (float) start.y);
        this.entityData.set(DATA_START_Z, (float) start.z);

        this.entityData.set(DATA_DIRECTION_X, (float) normalizedDirection.x);
        this.entityData.set(DATA_DIRECTION_Y, (float) normalizedDirection.y);
        this.entityData.set(DATA_DIRECTION_Z, (float) normalizedDirection.z);

        this.setPos(start.x, start.y, start.z);

        Vec3 end = calculateBeamEnd(start, normalizedDirection);
        this.entityData.set(DATA_LENGTH, (float) start.distanceTo(end));
    }

    @Nullable
    private EndoHydraEntity resolveHydraClient() {
        if (!level().isClientSide) {
            return hydra;
        }

        int hydraId = this.entityData.get(DATA_HYDRA_ID);

        if (hydraId < 0) {
            return null;
        }

        Entity entity = level().getEntity(hydraId);

        if (entity instanceof EndoHydraEntity resolved) {
            this.hydra = resolved;
            return resolved;
        }

        return hydra;
    }

    public void updateClientVisualPosition(float partialTick) {
        if (!level().isClientSide) {
            return;
        }

        EndoHydraEntity clientHydra = resolveHydraClient();

        if (clientHydra == null) {
            return;
        }

        int headIndex = getHeadIndex();

        if (headIndex < 0 || headIndex >= 5 || !clientHydra.isHeadActive(headIndex)) {
            return;
        }

        Vec3 interpolatedMouth = clientHydra.getInterpolatedClientMouthPosition(
                headIndex,
                partialTick
        );

        if (interpolatedMouth == null || interpolatedMouth.equals(Vec3.ZERO)) {
            return;
        }

        if (clientVisualStart == null) {
            clientVisualStart = interpolatedMouth;
        } else {
            clientVisualStart = clientVisualStart.lerp(
                    interpolatedMouth,
                    MOUTH_FOLLOW_SMOOTHING
            );
        }

        Vec3 target = getBeamTargetPosition();
        Vec3 toTarget = target.subtract(clientVisualStart);
        double distance = toTarget.length();

        if (distance <= 0.0001D) {
            return;
        }

        Vec3 direction = toTarget.normalize();
        clientVisualDirection = direction;
        clientVisualLength = Math.min(distance, MAX_BEAM_LENGTH);

        Vec3 visualEnd = clientVisualStart.add(direction.scale(clientVisualLength));

        HitResult blockHit = level().clip(new ClipContext(
                clientVisualStart,
                visualEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            clientVisualLength = clientVisualStart.distanceTo(blockHit.getLocation());
        }
    }

    public boolean isClientVisualReady() {
        return clientVisualStart != null
                && clientVisualDirection != null
                && clientVisualLength > 0.0001D;
    }

    public Vec3 getBeamStart() {
        if (level().isClientSide && clientVisualStart != null) {
            return clientVisualStart;
        }

        return getRawSyncedStart();
    }

    public Vec3 getBeamDirection() {
        if (level().isClientSide && clientVisualDirection != null) {
            return clientVisualDirection;
        }

        return getRawSyncedDirection();
    }

    public double getBeamLength() {
        if (level().isClientSide
                && clientVisualStart != null
                && clientVisualDirection != null) {
            return clientVisualLength;
        }

        return getRawSyncedLength();
    }

    public Vec3 getBeamEnd() {
        Vec3 start = getBeamStart();
        Vec3 direction = getBeamDirection();
        double length = getBeamLength();

        if (direction.lengthSqr() <= 0.000001D) {
            return start;
        }

        return start.add(direction.normalize().scale(length));
    }

    public Vec3 getBeamTargetPosition() {
        return new Vec3(
                this.entityData.get(DATA_TARGET_X),
                this.entityData.get(DATA_TARGET_Y),
                this.entityData.get(DATA_TARGET_Z)
        );
    }

    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    public boolean isFullHeadAttack() {
        return this.entityData.get(DATA_FULL_HEAD_ATTACK);
    }

    public int getHeadIndex() {
        return this.entityData.get(DATA_HEAD_INDEX);
    }

    @Nullable
    public EndoHydraEntity getHydra() {
        return hydra;
    }

    @Nullable
    public AbstractSpell getDamageSpell() {
        return damageSpell;
    }

    private Vec3 getRawSyncedStart() {
        return new Vec3(
                this.entityData.get(DATA_START_X),
                this.entityData.get(DATA_START_Y),
                this.entityData.get(DATA_START_Z)
        );
    }

    private Vec3 getRawSyncedDirection() {
        return new Vec3(
                this.entityData.get(DATA_DIRECTION_X),
                this.entityData.get(DATA_DIRECTION_Y),
                this.entityData.get(DATA_DIRECTION_Z)
        );
    }

    private double getRawSyncedLength() {
        return this.entityData.get(DATA_LENGTH);
    }

    private Vec3 calculateBeamEnd(Vec3 start, Vec3 direction) {
        if (direction.lengthSqr() <= 0.000001D) {
            return start;
        }

        Vec3 maximumEnd = start.add(direction.scale(MAX_BEAM_LENGTH));

        HitResult blockHit = level().clip(new ClipContext(
                start,
                maximumEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getLocation();
        }

        return maximumEnd;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && !beamProcessed) {
            beamProcessed = true;

            Vec3 start = getRawSyncedStart();
            Vec3 direction = getRawSyncedDirection();
            Vec3 end = calculateBeamEnd(start, direction);

            this.entityData.set(DATA_LENGTH, (float) start.distanceTo(end));

            damageEntitiesAlongBeam(start, end);
        }

        beamAge++;

        if (beamAge >= BEAM_LIFETIME) {
            discard();
        }
    }

    private void damageEntitiesAlongBeam(Vec3 start, Vec3 end) {
        Vec3 beam = end.subtract(start);

        if (beam.lengthSqr() <= 0.000001D) {
            return;
        }

        AABB beamBox = new AABB(start, end).inflate(BEAM_RADIUS);

        List<LivingEntity> entities = level().getEntitiesOfClass(
                LivingEntity.class,
                beamBox,
                this::isValidTarget
        );

        for (LivingEntity entity : entities) {
            AABB hitBox = entity.getBoundingBox().inflate(BEAM_RADIUS);

            if (hitBox.clip(start, end).isEmpty()) {
                continue;
            }

            if (damageSpell == null) {
                continue;
            }

            LivingEntity summoner = hydra != null ? hydra.getSummonOwner() : null;

            if (summoner == null) {
                continue;
            }

            var source = damageSpell.getDamageSource(this, summoner);

            if (isFullHeadAttack()) {
                entity.invulnerableTime = 0;
            }

            boolean damaged = DamageSources.applyDamage(entity, getDamage(), source);

            if (damaged) {
                applyVoidfrost(entity);
            }
        }
    }

    private void applyVoidfrost(LivingEntity entity) {
        if (level().isClientSide || level().random.nextFloat() > VOIDFROST_CHANCE) {
            return;
        }

        MobEffectInstance currentEffect = entity.getEffect(ROACWEffectRegistry.VOIDFROST.get());

        if (currentEffect == null) {
            entity.addEffect(new MobEffectInstance(
                    ROACWEffectRegistry.VOIDFROST.get(),
                    VOIDFROST_DURATION,
                    0,
                    false,
                    true,
                    false
            ));
            return;
        }

        int currentAmplifier = currentEffect.getAmplifier();

        if (currentAmplifier >= VOIDFROST_MAX_AMPLIFIER) {
            entity.addEffect(new MobEffectInstance(
                    ROACWEffectRegistry.VOIDFROST.get(),
                    VOIDFROST_DURATION,
                    VOIDFROST_MAX_AMPLIFIER,
                    false,
                    true,
                    false
            ));
            return;
        }

        entity.addEffect(new MobEffectInstance(
                ROACWEffectRegistry.VOIDFROST.get(),
                VOIDFROST_DURATION,
                currentAmplifier + 1,
                false,
                true,
                false
        ));
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (!entity.isAlive()) {
            return false;
        }

        if (hydra != null && entity == hydra) {
            return false;
        }

        return !isFriendly(entity);
    }

    private boolean isFriendly(LivingEntity entity) {
        if (hydra == null) {
            return false;
        }

        LivingEntity owner = hydra.getSummonOwner();

        if (owner == null) {
            return false;
        }

        return entity == owner || entity.getUUID().equals(owner.getUUID());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("BeamStartX")) {
            this.entityData.set(DATA_START_X, tag.getFloat("BeamStartX"));
            this.entityData.set(DATA_START_Y, tag.getFloat("BeamStartY"));
            this.entityData.set(DATA_START_Z, tag.getFloat("BeamStartZ"));
        }

        if (tag.contains("BeamDirectionX")) {
            this.entityData.set(DATA_DIRECTION_X, tag.getFloat("BeamDirectionX"));
            this.entityData.set(DATA_DIRECTION_Y, tag.getFloat("BeamDirectionY"));
            this.entityData.set(DATA_DIRECTION_Z, tag.getFloat("BeamDirectionZ"));
        }

        if (tag.contains("BeamLength")) {
            this.entityData.set(DATA_LENGTH, tag.getFloat("BeamLength"));
        }

        if (tag.contains("BeamDamage")) {
            this.entityData.set(DATA_DAMAGE, tag.getFloat("BeamDamage"));
        }

        if (tag.contains("FullHeadAttack")) {
            this.entityData.set(DATA_FULL_HEAD_ATTACK, tag.getBoolean("FullHeadAttack"));
        }

        if (tag.contains("BeamTargetX")) {
            this.entityData.set(DATA_TARGET_X, tag.getFloat("BeamTargetX"));
            this.entityData.set(DATA_TARGET_Y, tag.getFloat("BeamTargetY"));
            this.entityData.set(DATA_TARGET_Z, tag.getFloat("BeamTargetZ"));
        }

        if (tag.contains("HeadIndex")) {
            this.entityData.set(DATA_HEAD_INDEX, tag.getInt("HeadIndex"));
        }

        if (tag.contains("HydraId")) {
            this.entityData.set(DATA_HYDRA_ID, tag.getInt("HydraId"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("BeamStartX", this.entityData.get(DATA_START_X));
        tag.putFloat("BeamStartY", this.entityData.get(DATA_START_Y));
        tag.putFloat("BeamStartZ", this.entityData.get(DATA_START_Z));
        tag.putFloat("BeamDirectionX", this.entityData.get(DATA_DIRECTION_X));
        tag.putFloat("BeamDirectionY", this.entityData.get(DATA_DIRECTION_Y));
        tag.putFloat("BeamDirectionZ", this.entityData.get(DATA_DIRECTION_Z));
        tag.putFloat("BeamLength", this.entityData.get(DATA_LENGTH));
        tag.putFloat("BeamTargetX", this.entityData.get(DATA_TARGET_X));
        tag.putFloat("BeamTargetY", this.entityData.get(DATA_TARGET_Y));
        tag.putFloat("BeamTargetZ", this.entityData.get(DATA_TARGET_Z));
        tag.putInt("HeadIndex", this.entityData.get(DATA_HEAD_INDEX));
        tag.putInt("HydraId", this.entityData.get(DATA_HYDRA_ID));
        tag.putFloat("BeamDamage", this.entityData.get(DATA_DAMAGE));
        tag.putBoolean("FullHeadAttack", this.entityData.get(DATA_FULL_HEAD_ATTACK));
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}