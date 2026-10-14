package net.kamaarion.roacw.entity.projectile.energy_cannon_beam;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.acetheeldritchking.cataclysm_spellbooks.registries.CSAttributeRegistry;
import net.kamaarion.roacw.entity.armorset_summon.unstable_energy_cannon.UnstableEnergyCannonEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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

public class EnergyCannonBeamEntity extends Entity implements GeoEntity {

    private static final EntityDataAccessor<Float> DATA_START_X =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Float> DATA_START_Y =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Float> DATA_START_Z =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Float> DATA_DIRECTION_X =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Float> DATA_DIRECTION_Y =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Float> DATA_DIRECTION_Z =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Float> DATA_LENGTH =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Float> DATA_TARGET_X =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Float> DATA_TARGET_Y =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Float> DATA_TARGET_Z =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Integer> DATA_OWNER_ID =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Float> DATA_DAMAGE =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.FLOAT
            );

    private static final EntityDataAccessor<Integer> DATA_CANNON_ID =
            SynchedEntityData.defineId(
                    EnergyCannonBeamEntity.class,
                    EntityDataSerializers.INT
            );

    private static final double MAX_BEAM_LENGTH = 24.0D;

    private static final double BEAM_RADIUS = 0.75D;

    private static final int BEAM_LIFETIME = 30;

    private static final double BEAM_ANIMATION_SPEED = 1.5D;

    private static final int BEAM_ANIMATION_VISIBLE_TICKS =
            (int) Math.ceil(0.55D * 20.0D / BEAM_ANIMATION_SPEED);

    private static final double VISUAL_SMOOTHING = 0.35D;

    private static final float DEFAULT_DAMAGE = 30.0F;

    private static final double TECHNOMANCY_POWER_SCALING = 0.25D;
    private static final double LIGHTNING_POWER_SCALING = 0.25D;

    private static final RawAnimation BEAM_PEW =
            RawAnimation.begin()
                    .thenPlay("animation.beam.pew");

    private int beamAge = 0;

    private boolean beamProcessed = false;

    private boolean beamAnimationStarted = false;
    private boolean beamAnimationFinished = false;
    private int beamAnimationStartTick = 0;

    @Nullable
    private UnstableEnergyCannonEntity cannon;

    @Nullable
    private LivingEntity owner;

    @Nullable
    private Vec3 clientVisualStart;

    @Nullable
    private Vec3 clientVisualDirection;

    private double clientVisualLength = 0.0D;

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    public EnergyCannonBeamEntity(
            EntityType<? extends EnergyCannonBeamEntity> type,
            Level level
    ) {
        super(type, level);

        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData() {

        this.entityData.define(
                DATA_START_X,
                0.0F
        );

        this.entityData.define(
                DATA_START_Y,
                0.0F
        );

        this.entityData.define(
                DATA_START_Z,
                0.0F
        );

        this.entityData.define(
                DATA_DIRECTION_X,
                0.0F
        );

        this.entityData.define(
                DATA_DIRECTION_Y,
                0.0F
        );

        this.entityData.define(
                DATA_DIRECTION_Z,
                0.0F
        );

        this.entityData.define(
                DATA_LENGTH,
                0.0F
        );

        this.entityData.define(
                DATA_TARGET_X,
                0.0F
        );

        this.entityData.define(
                DATA_TARGET_Y,
                0.0F
        );

        this.entityData.define(
                DATA_TARGET_Z,
                0.0F
        );

        this.entityData.define(
                DATA_OWNER_ID,
                -1
        );

        this.entityData.define(
                DATA_DAMAGE,
                DEFAULT_DAMAGE
        );

        this.entityData.define(
                DATA_CANNON_ID,
                -1
        );
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {

        controllers.add(
                new AnimationController<>(
                        this,
                        "beam_pew_controller",
                        0,
                        this::animationPredicate
                ).setAnimationSpeed(
                        BEAM_ANIMATION_SPEED
                )
        );
    }

    private software.bernie.geckolib.core.object.PlayState animationPredicate(
            AnimationState<EnergyCannonBeamEntity> state
    ) {

        if (beamAnimationFinished) {
            return software.bernie.geckolib.core.object.PlayState.STOP;
        }

        AnimationController<EnergyCannonBeamEntity> controller =
                state.getController();

        if (beamAnimationStarted
                && this.tickCount > beamAnimationStartTick + 1
                && controller.getAnimationState()
                == AnimationController.State.STOPPED) {

            beamAnimationFinished = true;

            return software.bernie.geckolib.core.object.PlayState.STOP;
        }

        if (!beamAnimationStarted) {

            beamAnimationStarted = true;
            beamAnimationStartTick = this.tickCount;

            controller.setAnimation(
                    BEAM_PEW
            );
        }

        return software.bernie.geckolib.core.object.PlayState.CONTINUE;
    }

    public boolean isBeamAnimationFinished() {
        return beamAnimationFinished
                || (beamAnimationStarted
                && this.tickCount - beamAnimationStartTick
                >= BEAM_ANIMATION_VISIBLE_TICKS);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void setupBeam(
            @Nullable LivingEntity owner,
            Vec3 start,
            Vec3 direction,
            Vec3 targetPosition
    ) {
        setupBeam(
                owner,
                start,
                direction,
                targetPosition,
                scaleDamageForOwner(
                        owner,
                        DEFAULT_DAMAGE
                )
        );
    }

    private static float scaleDamageForOwner(
            @Nullable LivingEntity owner,
            float baseDamage
    ) {

        if (owner == null) {
            return baseDamage;
        }

        double technomancyPower =
                getSpellPower(
                        owner,
                        CSAttributeRegistry.TECHNOMANCY_MAGIC_POWER.get()
                );

        double lightningPower =
                getSpellPower(
                        owner,
                        AttributeRegistry.LIGHTNING_SPELL_POWER.get()
                );

        double powerScaling =
                1.0D
                        + (technomancyPower * TECHNOMANCY_POWER_SCALING)
                        + (lightningPower * LIGHTNING_POWER_SCALING);

        return (float) (baseDamage * powerScaling);
    }

    private static double getSpellPower(
            LivingEntity owner,
            Attribute attribute
    ) {

        if (!owner.getAttributes().hasAttribute(attribute)) {
            return 1.0D;
        }

        return owner.getAttributeValue(attribute);
    }

    public void setupBeam(
            @Nullable LivingEntity owner,
            Vec3 start,
            Vec3 direction,
            Vec3 targetPosition,
            float damage
    ) {

        this.owner = owner;

        if (owner != null) {

            this.entityData.set(
                    DATA_OWNER_ID,
                    owner.getId()
            );
        }

        this.entityData.set(
                DATA_DAMAGE,
                Math.max(
                        0.0F,
                        damage
                )
        );

        this.entityData.set(
                DATA_TARGET_X,
                (float) targetPosition.x
        );

        this.entityData.set(
                DATA_TARGET_Y,
                (float) targetPosition.y
        );

        this.entityData.set(
                DATA_TARGET_Z,
                (float) targetPosition.z
        );

        Vec3 normalizedDirection;

        if (direction.lengthSqr() > 0.000001D) {

            normalizedDirection =
                    direction.normalize();

        } else {

            normalizedDirection =
                    Vec3.ZERO;
        }

        this.entityData.set(
                DATA_START_X,
                (float) start.x
        );

        this.entityData.set(
                DATA_START_Y,
                (float) start.y
        );

        this.entityData.set(
                DATA_START_Z,
                (float) start.z
        );

        this.entityData.set(
                DATA_DIRECTION_X,
                (float) normalizedDirection.x
        );

        this.entityData.set(
                DATA_DIRECTION_Y,
                (float) normalizedDirection.y
        );

        this.entityData.set(
                DATA_DIRECTION_Z,
                (float) normalizedDirection.z
        );

        this.setPos(
                start.x,
                start.y,
                start.z
        );

        Vec3 end =
                calculateBeamEnd(
                        start,
                        normalizedDirection
                );

        this.entityData.set(
                DATA_LENGTH,
                (float) start.distanceTo(end)
        );
    }

    public void setupBeam(
            UnstableEnergyCannonEntity cannon,
            Vec3 start,
            Vec3 direction,
            Vec3 targetPosition
    ) {
        this.cannon = cannon;

        this.entityData.set(
                DATA_CANNON_ID,
                cannon.getId()
        );

        setupBeam(
                cannon.getOwner(),
                start,
                direction,
                targetPosition
        );
    }

    @Nullable
    private UnstableEnergyCannonEntity resolveCannonClient() {

        if (!level().isClientSide) {
            return cannon;
        }

        int cannonId =
                this.entityData.get(
                        DATA_CANNON_ID
                );

        if (cannonId < 0) {
            return null;
        }

        Entity entity =
                level().getEntity(
                        cannonId
                );

        if (entity instanceof UnstableEnergyCannonEntity resolved) {

            this.cannon = resolved;

            return resolved;
        }

        return cannon;
    }

    private Vec3 resolveVisualSourceStart(
            float partialTick
    ) {

        UnstableEnergyCannonEntity clientCannon =
                resolveCannonClient();

        if (clientCannon != null && clientCannon.isAlive()) {

            Vec3 muzzle =
                    clientCannon.getInterpolatedClientMuzzlePosition(
                            partialTick
                    );

            if (muzzle != null
                    && !muzzle.equals(Vec3.ZERO)) {

                return muzzle;
            }
        }

        return getRawSyncedStart();
    }

    @Nullable
    private LivingEntity resolveOwner() {

        if (this.owner != null
                && this.owner.isAlive()) {

            return this.owner;
        }

        int ownerId =
                this.entityData.get(
                        DATA_OWNER_ID
                );

        if (ownerId < 0) {
            return null;
        }

        Entity entity =
                level().getEntity(
                        ownerId
                );

        if (entity instanceof LivingEntity livingEntity) {

            this.owner =
                    livingEntity;

            return livingEntity;
        }

        return null;
    }

    public void updateClientVisualPosition(
            float partialTick
    ) {

        if (!level().isClientSide) {
            return;
        }

        Vec3 visualSourceStart =
                resolveVisualSourceStart(
                        partialTick
                );

        Vec3 target =
                getBeamTargetPosition();

        if (clientVisualStart == null) {

            clientVisualStart =
                    visualSourceStart;

        } else {

            clientVisualStart =
                    clientVisualStart.lerp(
                            visualSourceStart,
                            VISUAL_SMOOTHING
                    );
        }

        Vec3 toTarget =
                target.subtract(
                        clientVisualStart
                );

        double distance =
                toTarget.length();

        if (distance <= 0.0001D) {
            return;
        }

        Vec3 direction =
                toTarget.normalize();

        clientVisualDirection =
                direction;

        clientVisualLength =
                Math.min(
                        distance,
                        MAX_BEAM_LENGTH
                );

        Vec3 visualEnd =
                clientVisualStart.add(
                        direction.scale(
                                clientVisualLength
                        )
                );

        var blockHit =
                level().clip(
                        new ClipContext(
                                clientVisualStart,
                                visualEnd,
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                this
                        )
                );

        if (blockHit.getType()
                == net.minecraft.world.phys.HitResult.Type.BLOCK) {

            clientVisualLength =
                    clientVisualStart.distanceTo(
                            blockHit.getLocation()
                    );
        }
    }

    public boolean isClientVisualReady() {

        return clientVisualStart != null
                && clientVisualDirection != null
                && clientVisualLength > 0.0001D;
    }

    public Vec3 getBeamStart() {

        if (level().isClientSide
                && clientVisualStart != null) {

            return clientVisualStart;
        }

        return getRawSyncedStart();
    }

    public Vec3 getBeamDirection() {

        if (level().isClientSide
                && clientVisualDirection != null) {

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

        Vec3 start =
                getBeamStart();

        Vec3 direction =
                getBeamDirection();

        double length =
                getBeamLength();

        if (direction.lengthSqr()
                <= 0.000001D) {

            return start;
        }

        return start.add(
                direction.normalize()
                        .scale(length)
        );
    }

    public Vec3 getBeamTargetPosition() {

        return new Vec3(
                this.entityData.get(
                        DATA_TARGET_X
                ),
                this.entityData.get(
                        DATA_TARGET_Y
                ),
                this.entityData.get(
                        DATA_TARGET_Z
                )
        );
    }

    public float getDamage() {

        return this.entityData.get(
                DATA_DAMAGE
        );
    }

    @Nullable
    public LivingEntity getOwner() {

        return resolveOwner();
    }

    private Vec3 getRawSyncedStart() {

        return new Vec3(
                this.entityData.get(
                        DATA_START_X
                ),
                this.entityData.get(
                        DATA_START_Y
                ),
                this.entityData.get(
                        DATA_START_Z
                )
        );
    }

    private Vec3 getRawSyncedDirection() {

        return new Vec3(
                this.entityData.get(
                        DATA_DIRECTION_X
                ),
                this.entityData.get(
                        DATA_DIRECTION_Y
                ),
                this.entityData.get(
                        DATA_DIRECTION_Z
                )
        );
    }

    private double getRawSyncedLength() {

        return this.entityData.get(
                DATA_LENGTH
        );
    }

    private Vec3 calculateBeamEnd(
            Vec3 start,
            Vec3 direction
    ) {

        if (direction.lengthSqr()
                <= 0.000001D) {

            return start;
        }

        Vec3 maximumEnd =
                start.add(
                        direction.scale(
                                MAX_BEAM_LENGTH
                        )
                );

        var blockHit =
                level().clip(
                        new ClipContext(
                                start,
                                maximumEnd,
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                this
                        )
                );

        if (blockHit.getType()
                == net.minecraft.world.phys.HitResult.Type.BLOCK) {

            return blockHit.getLocation();
        }

        return maximumEnd;
    }

    @Override
    public void tick() {

        super.tick();

        if (!level().isClientSide) {

            if (!beamProcessed) {

                beamProcessed = true;

                Vec3 start =
                        getRawSyncedStart();

                Vec3 direction =
                        getRawSyncedDirection();

                Vec3 end =
                        calculateBeamEnd(
                                start,
                                direction
                        );

                this.entityData.set(
                        DATA_LENGTH,
                        (float) start.distanceTo(end)
                );

                damageEntitiesAlongBeam(
                        start,
                        end
                );
            }
        }

        beamAge++;

        if (beamAge >= BEAM_LIFETIME) {
            discard();
        }
    }

    private void damageEntitiesAlongBeam(
            Vec3 start,
            Vec3 end
    ) {

        Vec3 beam =
                end.subtract(start);

        if (beam.lengthSqr()
                <= 0.000001D) {
            return;
        }

        AABB beamBox =
                new AABB(
                        start,
                        end
                ).inflate(
                        BEAM_RADIUS
                );

        List<LivingEntity> entities =
                level().getEntitiesOfClass(
                        LivingEntity.class,
                        beamBox,
                        this::isValidTarget
                );

        for (LivingEntity entity : entities) {

            AABB hitBox =
                    entity.getBoundingBox()
                            .inflate(
                                    BEAM_RADIUS
                            );

            if (!hitBox.clip(
                    start,
                    end
            ).isPresent()) {
                continue;
            }

            LivingEntity attacker =
                    resolveOwner();

            if (attacker == null) {
                continue;
            }

            DamageSource source =
                    attacker instanceof Player player
                            ? level().damageSources().playerAttack(player)
                            : level().damageSources().mobAttack(attacker);

            entity.hurt(
                    source,
                    getDamage()
            );
        }
    }

    private boolean isValidTarget(
            LivingEntity entity
    ) {

        if (!entity.isAlive()) {
            return false;
        }

        LivingEntity attacker =
                resolveOwner();

        if (attacker == null) {
            return true;
        }

        if (entity == attacker) {
            return false;
        }

        if (entity.getUUID().equals(
                attacker.getUUID()
        )) {
            return false;
        }

        return !isFriendly(
                entity,
                attacker
        );
    }

    private boolean isFriendly(
            LivingEntity entity,
            LivingEntity attacker
    ) {

        if (entity == attacker) {
            return true;
        }

        if (attacker.isAlliedTo(entity)) {
            return true;
        }

        if (attacker.getTeam() != null
                && attacker.getTeam() == entity.getTeam()) {
            return true;
        }

        return false;
    }

    @Override
    protected void readAdditionalSaveData(
            CompoundTag tag
    ) {

        if (tag.contains("BeamStartX")) {

            this.entityData.set(
                    DATA_START_X,
                    tag.getFloat(
                            "BeamStartX"
                    )
            );

            this.entityData.set(
                    DATA_START_Y,
                    tag.getFloat(
                            "BeamStartY"
                    )
            );

            this.entityData.set(
                    DATA_START_Z,
                    tag.getFloat(
                            "BeamStartZ"
                    )
            );
        }

        if (tag.contains("BeamDirectionX")) {

            this.entityData.set(
                    DATA_DIRECTION_X,
                    tag.getFloat(
                            "BeamDirectionX"
                    )
            );

            this.entityData.set(
                    DATA_DIRECTION_Y,
                    tag.getFloat(
                            "BeamDirectionY"
                    )
            );

            this.entityData.set(
                    DATA_DIRECTION_Z,
                    tag.getFloat(
                            "BeamDirectionZ"
                    )
            );
        }

        if (tag.contains("BeamLength")) {

            this.entityData.set(
                    DATA_LENGTH,
                    tag.getFloat(
                            "BeamLength"
                    )
            );
        }

        if (tag.contains("BeamDamage")) {

            this.entityData.set(
                    DATA_DAMAGE,
                    tag.getFloat(
                            "BeamDamage"
                    )
            );
        }

        if (tag.contains("BeamTargetX")) {

            this.entityData.set(
                    DATA_TARGET_X,
                    tag.getFloat(
                            "BeamTargetX"
                    )
            );

            this.entityData.set(
                    DATA_TARGET_Y,
                    tag.getFloat(
                            "BeamTargetY"
                    )
            );

            this.entityData.set(
                    DATA_TARGET_Z,
                    tag.getFloat(
                            "BeamTargetZ"
                    )
            );
        }

        if (tag.contains("CannonId")) {

            this.entityData.set(
                    DATA_CANNON_ID,
                    tag.getInt(
                            "CannonId"
                    )
            );
        }

        if (tag.contains("OwnerId")) {

            this.entityData.set(
                    DATA_OWNER_ID,
                    tag.getInt(
                            "OwnerId"
                    )
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(
            CompoundTag tag
    ) {

        tag.putFloat(
                "BeamStartX",
                this.entityData.get(
                        DATA_START_X
                )
        );

        tag.putFloat(
                "BeamStartY",
                this.entityData.get(
                        DATA_START_Y
                )
        );

        tag.putFloat(
                "BeamStartZ",
                this.entityData.get(
                        DATA_START_Z
                )
        );

        tag.putFloat(
                "BeamDirectionX",
                this.entityData.get(
                        DATA_DIRECTION_X
                )
        );

        tag.putFloat(
                "BeamDirectionY",
                this.entityData.get(
                        DATA_DIRECTION_Y
                )
        );

        tag.putFloat(
                "BeamDirectionZ",
                this.entityData.get(
                        DATA_DIRECTION_Z
                )
        );

        tag.putFloat(
                "BeamLength",
                this.entityData.get(
                        DATA_LENGTH
                )
        );

        tag.putFloat(
                "BeamTargetX",
                this.entityData.get(
                        DATA_TARGET_X
                )
        );

        tag.putFloat(
                "BeamTargetY",
                this.entityData.get(
                        DATA_TARGET_Y
                )
        );

        tag.putFloat(
                "BeamTargetZ",
                this.entityData.get(
                        DATA_TARGET_Z
                )
        );

        tag.putInt(
                "CannonId",
                this.entityData.get(
                        DATA_CANNON_ID
                )
        );

        tag.putInt(
                "OwnerId",
                this.entityData.get(
                        DATA_OWNER_ID
                )
        );

        tag.putFloat(
                "BeamDamage",
                this.entityData.get(
                        DATA_DAMAGE
                )
        );
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
    public boolean canCollideWith(
            Entity entity
    ) {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}