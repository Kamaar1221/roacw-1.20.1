package net.kamaarion.roacw.entity.projectile.plague_bee;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.kamaarion.roacw.entity.summon.baby_plaguebringer.BabyPlaguebringerEntity;
import net.kamaarion.roacw.entity.summon.mini_plaguebringer.MiniPlaguebringerEntity;
import net.kamaarion.roacw.entity.summon.virili.ViriliEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlagueBeeEntity extends Projectile implements GeoEntity {

    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(
                    PlagueBeeEntity.class,
                    EntityDataSerializers.INT
            );

    private static final double INITIAL_SPEED = 0.15D;
    private static final double MAX_SPEED = 0.25D;
    private static final double ACCELERATION = 0.005D;
    private static final double HOMING_STRENGTH = 0.12D;

    private static final int SPREAD_TICKS = 10;
    private static final double SPREAD_SPEED = 0.22D;
    private static final double SPREAD_ANGLE = 25.0D;

    private static final int MAX_LIFETIME = 200;
    private static final float BASE_DAMAGE = 3.0F;
    private static final float SPELL_POWER_SCALE = 0.25F;

    private static final int PLAGUE_DURATION = 200;
    private static final int PLAGUE_AMPLIFIER = 0;

    private static final RawAnimation IDLE_ANIMATION =
            RawAnimation.begin().thenLoop("PlagueBeeIdle");

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private int spreadIndex = 1;
    private float damage = BASE_DAMAGE;

    @Nullable
    private LivingEntity summonOwner;

    @Nullable
    private UUID summonOwnerUUID;

    public PlagueBeeEntity(
            EntityType<PlagueBeeEntity> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    public PlagueBeeEntity(
            EntityType<PlagueBeeEntity> entityType,
            Level level,
            LivingEntity owner,
            LivingEntity target
    ) {
        this(entityType, level, owner, target, 1);
    }

    public PlagueBeeEntity(
            EntityType<PlagueBeeEntity> entityType,
            Level level,
            LivingEntity owner,
            LivingEntity target,
            int spreadIndex
    ) {
        this(entityType, level);

        this.setOwner(owner);
        this.setTarget(target);
        this.resolveSummonOwner(owner);
        this.spreadIndex = Mth.clamp(spreadIndex, 0, 2);

        this.setPos(
                owner.getX(),
                owner.getEyeY() - 0.15D,
                owner.getZ()
        );

        Vec3 spreadDirection = this.getSpreadDirection(target);

        this.setDeltaMovement(spreadDirection.scale(SPREAD_SPEED));
        this.updateRotationFromMovement(spreadDirection);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_TARGET_ID, -1);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return this.damage;
    }

    public void setSpellPower(float spellPower) {
        this.damage = BASE_DAMAGE * spellPower * SPELL_POWER_SCALE;
    }

    public void setSummonOwner(@Nullable LivingEntity summonOwner) {
        this.summonOwner = summonOwner;
        this.summonOwnerUUID = summonOwner == null
                ? null
                : summonOwner.getUUID();
    }

    @Nullable
    public LivingEntity getSummonOwner() {
        if (this.summonOwner != null && this.summonOwner.isAlive()) {
            return this.summonOwner;
        }

        if (this.summonOwnerUUID != null
                && this.level() instanceof ServerLevel serverLevel) {
            Entity player = serverLevel.getServer()
                    .getPlayerList()
                    .getPlayer(this.summonOwnerUUID);

            if (player instanceof LivingEntity living) {
                this.summonOwner = living;
                return living;
            }

            Entity resolved = serverLevel.getEntity(this.summonOwnerUUID);

            if (resolved instanceof LivingEntity living) {
                this.summonOwner = living;
                return living;
            }
        }

        Entity projectileOwner = this.getOwner();

        if (projectileOwner != null) {
            this.resolveSummonOwner(projectileOwner);
            return this.summonOwner;
        }

        return null;
    }

    private void resolveSummonOwner(@Nullable Entity owner) {
        if (owner == null) {
            this.summonOwner = null;
            this.summonOwnerUUID = null;
            return;
        }

        if (owner instanceof ViriliEntity virili) {
            LivingEntity viriliOwner = virili.getOwner();

            if (viriliOwner != null) {
                this.summonOwner = viriliOwner;
                this.summonOwnerUUID = viriliOwner.getUUID();
                return;
            }
        }

        Entity resolvedOwner = SummonManager.getOwner(owner);

        if (resolvedOwner instanceof LivingEntity living) {
            this.summonOwner = living;
            this.summonOwnerUUID = living.getUUID();
            return;
        }

        if (owner instanceof LivingEntity living) {
            this.summonOwner = living;
            this.summonOwnerUUID = living.getUUID();
            return;
        }

        this.summonOwner = null;
        this.summonOwnerUUID = null;
    }

    public void setTarget(@Nullable LivingEntity target) {
        this.entityData.set(
                DATA_TARGET_ID,
                target == null ? -1 : target.getId()
        );
    }

    @Nullable
    public LivingEntity getTarget() {
        int targetId = this.entityData.get(DATA_TARGET_ID);

        if (targetId == -1) {
            return null;
        }

        Entity entity = this.level().getEntity(targetId);

        return entity instanceof LivingEntity livingEntity
                ? livingEntity
                : null;
    }

    private boolean isFriendlyEntity(Entity entity) {
        if (entity == this || entity == this.getOwner()) {
            return true;
        }

        LivingEntity actualSummoner = this.getSummonOwner();

        if (actualSummoner != null && entity == actualSummoner) {
            return true;
        }

        if (entity instanceof PlagueBeeEntity otherBee) {
            LivingEntity otherOwner = otherBee.getSummonOwner();

            if (actualSummoner != null
                    && otherOwner != null
                    && sameOwner(actualSummoner, otherOwner)) {
                return true;
            }

            return true;
        }

        if (entity instanceof ViriliEntity virili) {
            LivingEntity viriliOwner = virili.getOwner();

            if (actualSummoner != null
                    && viriliOwner != null
                    && sameOwner(actualSummoner, viriliOwner)) {
                return true;
            }
        }

        if (entity instanceof BabyPlaguebringerEntity baby) {
            LivingEntity babyOwner = baby.getOwner();

            if (actualSummoner != null
                    && babyOwner != null
                    && sameOwner(actualSummoner, babyOwner)) {
                return true;
            }
        }

        if (entity instanceof MiniPlaguebringerEntity mini) {
            LivingEntity miniOwner = mini.getSummonOwner();

            if (actualSummoner != null
                    && miniOwner != null
                    && sameOwner(actualSummoner, miniOwner)) {
                return true;
            }
        }

        if (entity instanceof IMagicSummon) {
            Entity magicSummonOwner = SummonManager.getOwner(entity);

            if (actualSummoner != null
                    && magicSummonOwner instanceof LivingEntity livingOwner
                    && sameOwner(actualSummoner, livingOwner)) {
                return true;
            }
        }

        if (actualSummoner != null) {
            if (actualSummoner.isAlliedTo(entity)) {
                return true;
            }

            if (entity instanceof LivingEntity living
                    && living.isAlliedTo(actualSummoner)) {
                return true;
            }
        }

        return false;
    }

    private boolean sameOwner(LivingEntity first, LivingEntity second) {
        return first == second || first.getUUID().equals(second.getUUID());
    }

    private boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        if (!living.isAlive() || living.isSpectator()) {
            return false;
        }

        return !this.isFriendlyEntity(entity);
    }

    private Vec3 getSpreadDirection(@Nullable LivingEntity target) {
        Vec3 baseDirection;

        if (target != null && target.isAlive()) {
            baseDirection = target.getEyePosition().subtract(this.position());
        } else {
            baseDirection = new Vec3(0.0D, 0.0D, 1.0D);
        }

        if (baseDirection.lengthSqr() < 0.0001D) {
            baseDirection = new Vec3(0.0D, 0.0D, 1.0D);
        }

        baseDirection = baseDirection.normalize();

        Vec3 horizontalForward = new Vec3(
                baseDirection.x,
                0.0D,
                baseDirection.z
        );

        if (horizontalForward.lengthSqr() < 0.0001D) {
            horizontalForward = new Vec3(0.0D, 0.0D, 1.0D);
        }

        horizontalForward = horizontalForward.normalize();

        Vec3 horizontalSide = new Vec3(
                -horizontalForward.z,
                0.0D,
                horizontalForward.x
        );

        double angleDegrees = switch (this.spreadIndex) {
            case 0 -> -SPREAD_ANGLE;
            case 2 -> SPREAD_ANGLE;
            default -> 0.0D;
        };

        double angleRadians = angleDegrees * Mth.DEG_TO_RAD;

        Vec3 spreadDirection = horizontalForward
                .scale(Math.cos(angleRadians))
                .add(horizontalSide.scale(Math.sin(angleRadians)));

        double verticalOffset = switch (this.spreadIndex) {
            case 0 -> 0.08D;
            case 2 -> -0.08D;
            default -> 0.0D;
        };

        spreadDirection = new Vec3(
                spreadDirection.x,
                spreadDirection.y + verticalOffset,
                spreadDirection.z
        );

        return spreadDirection.normalize();
    }

    @Override
    public void tick() {
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

        super.tick();

        if (this.tickCount >= MAX_LIFETIME) {
            this.discard();
            return;
        }

        LivingEntity target = this.getTarget();

        if (target != null && !this.isValidTarget(target)) {
            this.setTarget(null);
            target = null;
        }

        Vec3 movement = this.getDeltaMovement();

        if (this.tickCount <= SPREAD_TICKS) {
            Vec3 spreadDirection = this.getSpreadDirection(target);
            movement = spreadDirection.scale(SPREAD_SPEED);
            this.setDeltaMovement(movement);
        } else if (target != null && this.isValidTarget(target)) {
            Vec3 desiredDirection = target.getEyePosition()
                    .subtract(this.position())
                    .normalize();

            if (movement.lengthSqr() < 0.0001D) {
                movement = desiredDirection.scale(INITIAL_SPEED);
            } else {
                Vec3 currentDirection = movement.normalize();

                Vec3 newDirection = currentDirection
                        .lerp(desiredDirection, HOMING_STRENGTH)
                        .normalize();

                double newSpeed = Math.min(
                        movement.length() + ACCELERATION,
                        MAX_SPEED
                );

                movement = newDirection.scale(newSpeed);
            }

            this.setDeltaMovement(movement);
        }

        movement = this.getDeltaMovement();

        if (movement.lengthSqr() > 0.0001D) {
            this.updateRotationFromMovement(movement);
        }

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(
                this,
                this::canHitEntity
        );

        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
            return;
        }

        this.setPos(
                this.getX() + movement.x,
                this.getY() + movement.y,
                this.getZ() + movement.z
        );
    }

    private void updateRotationFromMovement(Vec3 movement) {
        if (movement.lengthSqr() < 0.0001D) {
            return;
        }

        double horizontalDistance = Math.sqrt(
                movement.x * movement.x + movement.z * movement.z
        );

        float yaw = (float) (
                Mth.atan2(movement.z, movement.x) * (180.0D / Math.PI)
        ) - 90.0F + 180.0F;

        float pitch = (float) (
                -Mth.atan2(movement.y, horizontalDistance)
                        * (180.0D / Math.PI)
        );

        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!this.isValidTarget(entity)) {
            return false;
        }

        return super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        Entity hitEntity = result.getEntity();

        if (!(hitEntity instanceof LivingEntity target)) {
            return;
        }

        if (!this.isValidTarget(target) || this.level().isClientSide) {
            return;
        }

        LivingEntity actualSummoner = this.getSummonOwner();

        if (actualSummoner == null) {
            Entity projectileOwner = this.getOwner();

            if (projectileOwner != null) {
                this.resolveSummonOwner(projectileOwner);
                actualSummoner = this.summonOwner;
            }
        }

        if (actualSummoner == null) {
            this.discard();
            return;
        }

        if (this.isFriendlyEntity(target)) {
            return;
        }

        DamageSource source = ROACWSpellRegistry.SUMMON_VIRILI
                .get()
                .getDamageSource(this, actualSummoner);

        target.invulnerableTime = 0;

        boolean damaged = DamageSources.applyDamage(
                target,
                this.damage,
                source
        );

        if (damaged) {
            target.addEffect(
                    new MobEffectInstance(
                            ROACWEffectRegistry.PLAGUE.get(),
                            PLAGUE_DURATION,
                            PLAGUE_AMPLIFIER,
                            false,
                            false,
                            true
                    ),
                    actualSummoner
            );
        }

        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt("TargetId", this.entityData.get(DATA_TARGET_ID));
        tag.putInt("SpreadIndex", this.spreadIndex);
        tag.putFloat("Damage", this.damage);

        if (this.summonOwnerUUID != null) {
            tag.putUUID("SummonOwnerUUID", this.summonOwnerUUID);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("TargetId")) {
            this.entityData.set(DATA_TARGET_ID, tag.getInt("TargetId"));
        }

        if (tag.contains("SpreadIndex")) {
            this.spreadIndex = Mth.clamp(tag.getInt("SpreadIndex"), 0, 2);
        }

        if (tag.contains("Damage")) {
            this.damage = tag.getFloat("Damage");
        }

        this.summonOwner = null;

        if (tag.hasUUID("SummonOwnerUUID")) {
            this.summonOwnerUUID = tag.getUUID("SummonOwnerUUID");

            if (this.level() instanceof ServerLevel serverLevel) {
                Entity owner = serverLevel.getServer()
                        .getPlayerList()
                        .getPlayer(this.summonOwnerUUID);

                if (owner instanceof LivingEntity living) {
                    this.summonOwner = living;
                } else {
                    Entity resolved = serverLevel.getEntity(this.summonOwnerUUID);

                    if (resolved instanceof LivingEntity living) {
                        this.summonOwner = living;
                    }
                }
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "plague_bee_controller",
                        0,
                        state -> {
                            state.setAndContinue(IDLE_ANIMATION);
                            return PlayState.CONTINUE;
                        }
                )
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}