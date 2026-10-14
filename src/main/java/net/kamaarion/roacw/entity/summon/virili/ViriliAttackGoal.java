package net.kamaarion.roacw.entity.summon.virili;

import io.redspace.ironsspellbooks.damage.DamageSources;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ViriliAttackGoal extends Goal {

    private static final int FORWARD_CHARGES_BEFORE_BARRAGE = 6;

    private static final int CIRCLE_MIN_TICKS = 20;
    private static final double CIRCLE_MIN_RADIUS = 12.0D;
    private static final double CIRCLE_MAX_RADIUS = 12.0D;
    private static final double CIRCLE_SPEED = 0.10D;
    private static final double CIRCLE_RADIUS_CORRECTION = 0.12D;

    private static final double CHARGE_PASS_DISTANCE = 10.0D;
    private static final double CHARGE_SPEED = 8.5D;
    private static final double CHARGE_DESTINATION_THRESHOLD = 0.15D;
    private static final double CHARGE_HITBOX_EXPANSION = 0.5D;
    private static final double CHARGE_HEIGHT_ADJUST_SPEED = 1.25D;
    private static final int CHARGE_HEIGHT_ADJUST_MAX_TICKS = 20;
    private static final int CHARGE_WINDUP_TICKS = 8;
    private static final int CHARGE_ACTIVE_MAX_TICKS = 12;

    private static final float DIVE_CHANCE = 0.20F;
    private static final double DIVE_HEIGHT_ABOVE = 5.0D;
    private static final double DIVE_BEHIND_DISTANCE = 10.0D;
    private static final double DIVE_APPROACH_SPEED = 1.0D;
    private static final double DIVE_APPROACH_DESTINATION_THRESHOLD = 0.5D;
    private static final int DIVE_APPROACH_MAX_TICKS = 100;
    private static final double DIVE_CENTER_REACHED_THRESHOLD = 0.6D;

    private static final int BARRAGE_CHARGE_COUNT = 6;
    private static final double BARRAGE_CHARGE_DISTANCE = 16.0D;
    private static final double BARRAGE_HALF_DISTANCE = BARRAGE_CHARGE_DISTANCE * 0.5D;
    private static final double BARRAGE_TARGET_SIDE_DISTANCE = BARRAGE_HALF_DISTANCE;
    private static final double BARRAGE_CHARGE_SPEED = 1.75D;
    private static final double BARRAGE_DESTINATION_THRESHOLD = 0.20D;
    private static final int BARRAGE_CHARGE_MAX_TICKS = 20;
    private static final int BARRAGE_PAUSE_TICKS = 20;
    private static final double BARRAGE_HEIGHT_ABOVE_TARGET = 6.0D;
    private static final double BARRAGE_APPROACH_SPEED = 1.2D;
    private static final double BARRAGE_APPROACH_THRESHOLD = 1.0D;
    private static final int BARRAGE_APPROACH_MAX_TICKS = 60;
    private static final int BARRAGE_MISSILES_PER_CHARGE = 5;
    private static final double BARRAGE_FINAL_MISSILE_DISTANCE =
            CIRCLE_MIN_RADIUS + BARRAGE_TARGET_SIDE_DISTANCE;
    private static final double BARRAGE_MISSILE_SPACING =
            BARRAGE_FINAL_MISSILE_DISTANCE / BARRAGE_MISSILES_PER_CHARGE;
    private static final int BARRAGE_FINISH_SNAP_TICKS = 2;

    private static final double SWARM_HOVER_HEIGHT = 5.0D;
    private static final double SWARM_HOVER_DISTANCE = CIRCLE_MIN_RADIUS;
    private static final double SWARM_HOVER_SPEED = 1.25D;
    private static final double SWARM_HOVER_THRESHOLD = 0.20D;
    private static final int SWARM_DURATION_TICKS = 80;
    private static final int SWARM_BEE_INTERVAL_TICKS = 20;
    private static final double SWARM_ORBIT_SPEED = 0.1D;
    private static final double SWARM_MOVEMENT_SMOOTHING = 0.35D;
    private static final int PLAGUE_DURATION = 200;
    private static final int PLAGUE_AMPLIFIER = 0;
    private static final int SWARM_BABY_PLAGUEBRINGER_INTERVAL_TICKS = 40;

    private final ViriliEntity mob;
    private final Set<UUID> chargeHitEntities = new HashSet<>();

    private LivingEntity target;
    private Phase phase = Phase.CIRCLE;
    private int phaseTicks;
    private int forwardChargeCount;
    private int barrageChargeCount;
    private int barrageMissilesFired;
    private int barragePauseTicks;
    private int circleDirection = 1;
    private int swarmActiveTicks;

    private double circleAngle;
    private double circleRadius;

    private Vec3 barrageApproachDestination = Vec3.ZERO;
    private Vec3 barrageApproachDirection = Vec3.ZERO;
    private Vec3 barrageChargeStartPosition = Vec3.ZERO;
    private Vec3 barrageChargeDestination = Vec3.ZERO;
    private Vec3 barrageChargeDirection = Vec3.ZERO;
    private Vec3 barrageInitialChargePosition = Vec3.ZERO;
    private Vec3 swarmHoverOffset = Vec3.ZERO;
    private Vec3 chargeStartPosition = Vec3.ZERO;
    private Vec3 chargeDestination = Vec3.ZERO;
    private Vec3 chargeDirection = Vec3.ZERO;
    private Vec3 chargeHeightAdjustPosition = Vec3.ZERO;
    private Vec3 diveStartPosition = Vec3.ZERO;
    private Vec3 diveTargetCenter = Vec3.ZERO;

    private float barrageLockedYaw;
    private double barrageDistanceTravelled;

    private boolean chargeIsDive;
    private boolean chargeDivePastCenter;
    private boolean swarmActive;

    private enum Phase {
        CIRCLE,
        CHARGE_APPROACH,
        CHARGE_WINDUP,
        CHARGE_ACTIVE,
        CHARGE_END,
        BARRAGE_APPROACH,
        BARRAGE_CHARGE,
        BARRAGE_PAUSE,
        BARRAGE_FINISH,
        SWARM,
        REST
    }

    public ViriliAttackGoal(ViriliEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();

        if (target == null || !target.isAlive() || target == this.mob.getOwner()) {
            return false;
        }

        if (this.mob.distanceToSqr(target) > 24.0D * 24.0D) {
            return false;
        }

        this.target = target;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();

        if (target == null || !target.isAlive() || target == this.mob.getOwner()) {
            return false;
        }

        if (this.mob.distanceToSqr(target) > 24.0D * 24.0D) {
            return false;
        }

        this.target = target;
        return true;
    }

    @Override
    public void start() {
        this.mob.resetTransientAiState();

        this.target = this.mob.getTarget();
        this.phase = Phase.CIRCLE;
        this.phaseTicks = 0;
        this.forwardChargeCount = 0;
        this.barrageChargeCount = 0;
        this.barrageDistanceTravelled = 0.0D;
        this.barrageMissilesFired = 0;
        this.barragePauseTicks = 0;
        this.barrageLockedYaw = this.mob.getYRot();
        this.swarmActive = false;
        this.swarmActiveTicks = 0;
        this.swarmHoverOffset = Vec3.ZERO;
        this.chargeHitEntities.clear();

        this.initializeCircleState(this.target);
    }

    @Override
    public void stop() {
        this.target = null;
        this.phase = Phase.CIRCLE;
        this.phaseTicks = 0;
        this.forwardChargeCount = 0;
        this.barrageChargeCount = 0;
        this.barrageDistanceTravelled = 0.0D;
        this.barrageMissilesFired = 0;
        this.barragePauseTicks = 0;
        this.swarmActive = false;
        this.swarmActiveTicks = 0;
        this.swarmHoverOffset = Vec3.ZERO;

        this.mob.setDeltaMovement(Vec3.ZERO);
        this.mob.setFlightControlLocked(false);
        this.mob.setChargeWindupActive(false);
        this.mob.finishChargeAnimation();
        this.mob.finishRemoteChargeAnimation();
        this.mob.setRemoteCharging(false);
        this.mob.setAttackRotationLocked(false);
        this.mob.resetTransientAiState();

        this.chargeHitEntities.clear();
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();

        if (target == null || !target.isAlive() || target == this.mob.getOwner()
                || this.mob.distanceToSqr(target) > 24.0D * 24.0D) {
            this.mob.setTarget(null);
            this.mob.resetTransientAiState();
            this.phase = Phase.CIRCLE;
            this.phaseTicks = 0;
            this.forwardChargeCount = 0;
            this.barrageChargeCount = 0;
            this.barrageDistanceTravelled = 0.0D;
            this.barrageMissilesFired = 0;
            this.barragePauseTicks = 0;
            this.swarmActive = false;
            this.swarmActiveTicks = 0;
            this.swarmHoverOffset = Vec3.ZERO;
            this.chargeHitEntities.clear();
            return;
        }

        this.target = target;
        this.phaseTicks++;

        switch (this.phase) {
            case CIRCLE -> this.tickCircle(target);
            case CHARGE_APPROACH -> this.tickChargeApproach(target);
            case CHARGE_WINDUP -> this.tickChargeWindup(target);
            case CHARGE_ACTIVE -> this.tickChargeActive(target);
            case CHARGE_END -> this.tickChargeEnd(target);
            case BARRAGE_APPROACH -> this.tickBarrageApproach(target);
            case BARRAGE_CHARGE -> this.tickBarrageCharge(target);
            case BARRAGE_PAUSE -> this.tickBarragePause(target);
            case BARRAGE_FINISH -> this.tickBarrageFinish(target);
            case SWARM -> this.tickSwarm(target);
            case REST -> this.tickRest(target);
        }
    }

    private void initializeCircleState(LivingEntity target) {
        if (target == null) {
            return;
        }

        Vec3 offset = this.mob.position().subtract(target.position());
        double horizontalDistance = Math.sqrt(offset.x * offset.x + offset.z * offset.z);

        if (horizontalDistance > 0.001D) {
            this.circleAngle = Math.atan2(offset.z, offset.x);
            this.circleRadius = Mth.clamp(horizontalDistance, CIRCLE_MIN_RADIUS, CIRCLE_MAX_RADIUS);
        } else {
            this.circleAngle = this.mob.getYRot() * Mth.DEG_TO_RAD;
            this.circleRadius = CIRCLE_MIN_RADIUS;
        }
    }

    private void tickCircle(LivingEntity target) {
        this.mob.setAttackRotationLocked(false);

        if (this.phaseTicks == 1) {
            this.initializeCircleState(target);
        }

        Vec3 targetPosition = target.position();
        this.circleAngle += CIRCLE_SPEED * this.circleDirection;

        double desiredX = targetPosition.x + Math.cos(this.circleAngle) * this.circleRadius;
        double desiredZ = targetPosition.z + Math.sin(this.circleAngle) * this.circleRadius;
        double desiredY = targetPosition.y + BARRAGE_HEIGHT_ABOVE_TARGET;

        Vec3 desiredPosition = new Vec3(desiredX, desiredY, desiredZ);
        Vec3 difference = desiredPosition.subtract(this.mob.position());
        Vec3 movement = difference.scale(CIRCLE_RADIUS_CORRECTION);

        if (movement.lengthSqr() > 0.0001D) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.75D).add(movement));
        }

        this.faceTarget(target);

        if (this.phaseTicks < CIRCLE_MIN_TICKS) {
            return;
        }

        if (this.forwardChargeCount < FORWARD_CHARGES_BEFORE_BARRAGE) {
            this.prepareCharge(target);
            return;
        }

        this.prepareBarrage(target);
    }

    private void faceTarget(LivingEntity target) {
        Vec3 direction = target.position().subtract(this.mob.position());
        this.setRotationImmediately(direction);
    }

    private void setRotationImmediately(Vec3 direction) {
        if (direction.lengthSqr() < 0.0001D) {
            return;
        }

        Vec3 normalized = direction.normalize();
        float yaw = this.getYawFromDirection(normalized);
        float pitch = this.getPitchFromDirection(normalized);

        this.mob.setYRot(yaw);
        this.mob.yRotO = yaw;
        this.mob.setYHeadRot(yaw);
        this.mob.yHeadRotO = yaw;
        this.mob.setXRot(pitch);
        this.mob.xRotO = pitch;
        this.mob.yBodyRot = yaw;
        this.mob.yBodyRotO = yaw;
    }

    private void snapToTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }

        Vec3 direction = target.position().subtract(this.mob.position());

        if (direction.lengthSqr() < 0.0001D) {
            return;
        }

        float targetYaw = this.getYawFromDirection(direction.normalize());

        this.mob.setAttackRotationLocked(true, targetYaw);
        this.mob.setYRot(targetYaw);
        this.mob.setYHeadRot(targetYaw);
        this.mob.yBodyRot = targetYaw;
        this.mob.yRotO = targetYaw;
        this.mob.yHeadRotO = targetYaw;
        this.mob.yBodyRotO = targetYaw;
        this.mob.forceAttackRotation();
        this.mob.yRotO = targetYaw;
        this.mob.yHeadRotO = targetYaw;
        this.mob.yBodyRotO = targetYaw;
    }

    private void prepareCharge(LivingEntity target) {
        Vec3 mobPosition = this.mob.position();
        Vec3 targetPosition = target.position();

        double directionX = targetPosition.x - mobPosition.x;
        double directionZ = targetPosition.z - mobPosition.z;
        double horizontalLength = Math.sqrt(directionX * directionX + directionZ * directionZ);

        if (horizontalLength < 0.001D) {
            directionX = -Mth.sin(this.mob.getYRot() * Mth.DEG_TO_RAD);
            directionZ = Mth.cos(this.mob.getYRot() * Mth.DEG_TO_RAD);
            horizontalLength = Math.sqrt(directionX * directionX + directionZ * directionZ);
        }

        directionX /= horizontalLength;
        directionZ /= horizontalLength;

        this.chargeDirection = new Vec3(directionX, 0.0D, directionZ);

        double targetCenterY = target.getY() + target.getBbHeight() * 0.5D;
        double desiredViriliY = targetCenterY - this.mob.getBbHeight() * 0.5D;

        this.chargeHeightAdjustPosition = new Vec3(mobPosition.x, desiredViriliY, mobPosition.z);
        this.chargeStartPosition = mobPosition;
        this.chargeDestination = new Vec3(
                targetPosition.x + directionX * CHARGE_PASS_DISTANCE,
                desiredViriliY,
                targetPosition.z + directionZ * CHARGE_PASS_DISTANCE
        );

        this.chargeIsDive = this.mob.getRandom().nextFloat() < DIVE_CHANCE;

        this.diveStartPosition = new Vec3(
                targetPosition.x - directionX * DIVE_BEHIND_DISTANCE,
                targetCenterY + DIVE_HEIGHT_ABOVE,
                targetPosition.z - directionZ * DIVE_BEHIND_DISTANCE
        );

        this.chargeHitEntities.clear();
        this.mob.setFlightControlLocked(true);
        this.mob.setAttackRotationLocked(false);
        this.mob.setDeltaMovement(Vec3.ZERO);
        this.mob.finishChargeAnimation();
        this.mob.finishRemoteChargeAnimation();
        this.mob.setRemoteCharging(false);

        this.phase = Phase.CHARGE_APPROACH;
        this.phaseTicks = 0;
    }

    private void tickChargeApproach(LivingEntity target) {
        if (this.chargeIsDive) {
            this.tickDiveApproach(target);
            return;
        }

        double targetCenterY = target.getY() + target.getBbHeight() * 0.5D;
        double desiredViriliY = targetCenterY - this.mob.getBbHeight() * 0.5D;
        Vec3 current = this.mob.position();
        double verticalDifference = desiredViriliY - current.y;

        this.updateChargeAim(target, desiredViriliY);
        this.faceTarget(target);

        if (Math.abs(verticalDifference) <= 0.15D) {
            this.mob.setPos(current.x, desiredViriliY, current.z);
            this.mob.setDeltaMovement(Vec3.ZERO);
            this.updateChargeAim(target, desiredViriliY);
            this.beginChargeWindup();
            return;
        }

        double verticalMovement = Mth.clamp(
                verticalDifference,
                -CHARGE_HEIGHT_ADJUST_SPEED,
                CHARGE_HEIGHT_ADJUST_SPEED
        );

        Vec3 movement = new Vec3(0.0D, verticalMovement, 0.0D);
        this.mob.move(MoverType.SELF, movement);
        this.mob.setDeltaMovement(movement);

        if (this.phaseTicks >= CHARGE_HEIGHT_ADJUST_MAX_TICKS) {
            Vec3 position = this.mob.position();
            this.mob.setPos(position.x, desiredViriliY, position.z);
            this.mob.setDeltaMovement(Vec3.ZERO);
            this.updateChargeAim(target, desiredViriliY);
            this.beginChargeWindup();
        }
    }

    private void tickDiveApproach(LivingEntity target) {
        Vec3 current = this.mob.position();
        Vec3 destination = this.diveStartPosition;
        Vec3 difference = destination.subtract(current);
        double distance = difference.length();

        this.faceTarget(target);

        if (distance <= DIVE_APPROACH_DESTINATION_THRESHOLD || this.phaseTicks >= DIVE_APPROACH_MAX_TICKS) {
            this.mob.setPos(destination.x, destination.y, destination.z);
            this.mob.setDeltaMovement(Vec3.ZERO);
            this.beginChargeWindup();
            return;
        }

        Vec3 direction = difference.normalize();
        double approachSpeed = Math.min(DIVE_APPROACH_SPEED, distance);
        Vec3 movement = direction.scale(approachSpeed);

        this.mob.move(MoverType.SELF, movement);
        this.mob.setDeltaMovement(movement);
    }

    private void updateChargeAim(LivingEntity target, double desiredY) {
        Vec3 current = this.mob.position();
        Vec3 targetPosition = target.position();

        double directionX = targetPosition.x - current.x;
        double directionZ = targetPosition.z - current.z;
        double horizontalLength = Math.sqrt(directionX * directionX + directionZ * directionZ);

        if (horizontalLength < 0.001D) {
            return;
        }

        directionX /= horizontalLength;
        directionZ /= horizontalLength;

        this.chargeDirection = new Vec3(directionX, 0.0D, directionZ);
        this.chargeDestination = new Vec3(
                targetPosition.x + directionX * CHARGE_PASS_DISTANCE,
                desiredY,
                targetPosition.z + directionZ * CHARGE_PASS_DISTANCE
        );
    }

    private void beginChargeWindup() {
        this.phase = Phase.CHARGE_WINDUP;
        this.phaseTicks = 0;
        this.mob.setFlightControlLocked(true);
        this.mob.setDeltaMovement(Vec3.ZERO);
        this.mob.setChargeWindupActive(true);
        this.mob.beginChargeAnimation();
        this.setRotationImmediately(this.chargeDirection);
        this.forwardChargeCount++;
    }

    private void tickChargeWindup(LivingEntity target) {
        this.mob.setDeltaMovement(Vec3.ZERO);

        Vec3 current = this.mob.position();
        Vec3 targetPosition = target.position();

        double directionX = targetPosition.x - current.x;
        double directionZ = targetPosition.z - current.z;
        double horizontalLength = Math.sqrt(directionX * directionX + directionZ * directionZ);

        if (horizontalLength < 0.001D) {
            directionX = -Mth.sin(this.mob.getYRot() * Mth.DEG_TO_RAD);
            directionZ = Mth.cos(this.mob.getYRot() * Mth.DEG_TO_RAD);
            horizontalLength = Math.sqrt(directionX * directionX + directionZ * directionZ);
        }

        directionX /= horizontalLength;
        directionZ /= horizontalLength;

        if (this.chargeIsDive) {
            double targetCenterY = targetPosition.y + target.getBbHeight() * 0.5D;
            double floorY = targetPosition.y;

            Vec3 targetCenter = new Vec3(targetPosition.x, targetCenterY, targetPosition.z);
            this.diveTargetCenter = targetCenter;

            Vec3 throughDirection = targetCenter.subtract(current).normalize();
            double distanceToCenter = targetCenter.subtract(current).length();
            double totalDistance = distanceToCenter + CHARGE_PASS_DISTANCE;
            Vec3 rawDestination = current.add(throughDirection.scale(totalDistance));

            if (rawDestination.y < floorY && throughDirection.y < -0.0001D) {
                double clampedDistance = (current.y - floorY) / -throughDirection.y;
                rawDestination = current.add(throughDirection.scale(clampedDistance));
            }

            this.chargeDestination = rawDestination;
            this.chargeDirection = throughDirection;
        } else {
            this.chargeDirection = new Vec3(directionX, 0.0D, directionZ);
            this.chargeDestination = new Vec3(
                    targetPosition.x + directionX * CHARGE_PASS_DISTANCE,
                    current.y,
                    targetPosition.z + directionZ * CHARGE_PASS_DISTANCE
            );
        }

        this.setRotationImmediately(this.chargeDirection);

        if (this.phaseTicks >= CHARGE_WINDUP_TICKS) {
            this.beginActiveCharge();
        }
    }

    private void beginActiveCharge() {
        this.phase = Phase.CHARGE_ACTIVE;
        this.phaseTicks = 0;
        this.mob.setChargeWindupActive(false);
        this.chargeDivePastCenter = false;
        this.chargeDirection = this.chargeDirection.normalize();

        float yaw = this.getYawFromDirection(this.chargeDirection);
        float pitch = this.chargeIsDive ? this.getPitchFromDirection(this.chargeDirection) : 0.0F;

        this.mob.setAttackRotationLocked(true, yaw, pitch);
        this.mob.setDeltaMovement(this.chargeDirection.scale(CHARGE_SPEED));
    }

    private void tickChargeActive(LivingEntity target) {
        this.mob.forceAttackRotation();
        Vec3 current = this.mob.position();

        if (this.chargeIsDive && !this.chargeDivePastCenter) {
            double distanceToCenter = this.diveTargetCenter.subtract(current).length();

            if (distanceToCenter <= DIVE_CENTER_REACHED_THRESHOLD) {
                this.chargeDivePastCenter = true;

                Vec3 horizontalOnly = new Vec3(this.chargeDirection.x, 0.0D, this.chargeDirection.z);

                if (horizontalOnly.lengthSqr() < 0.0001D) {
                    horizontalOnly = new Vec3(
                            -Mth.sin(this.mob.getYRot() * Mth.DEG_TO_RAD),
                            0.0D,
                            Mth.cos(this.mob.getYRot() * Mth.DEG_TO_RAD)
                    );
                } else {
                    horizontalOnly = horizontalOnly.normalize();
                }

                double floorY = this.diveTargetCenter.y - target.getBbHeight() * 0.5D;

                this.mob.setPos(current.x, floorY, current.z);
                this.checkChargeHit(current, new Vec3(0.0D, floorY - current.y, 0.0D));

                current = new Vec3(current.x, floorY, current.z);

                this.chargeDestination = new Vec3(
                        this.diveTargetCenter.x + horizontalOnly.x * CHARGE_PASS_DISTANCE,
                        floorY,
                        this.diveTargetCenter.z + horizontalOnly.z * CHARGE_PASS_DISTANCE
                );

                this.chargeDirection = this.chargeDestination.subtract(current).normalize();

                float levelYaw = this.getYawFromDirection(this.chargeDirection);
                float levelPitch = this.getPitchFromDirection(this.chargeDirection);

                this.mob.setAttackRotationLocked(true, levelYaw, levelPitch);
            }
        }

        Vec3 toDestination = this.chargeDestination.subtract(current);
        double remainingDistance = toDestination.length();

        if (remainingDistance <= CHARGE_DESTINATION_THRESHOLD || this.phaseTicks >= CHARGE_ACTIVE_MAX_TICKS) {
            this.mob.setPos(this.chargeDestination.x, this.chargeDestination.y, this.chargeDestination.z);
            this.mob.setDeltaMovement(Vec3.ZERO);
            this.beginChargeEnd();
            return;
        }

        double movementDistance = Math.min(CHARGE_SPEED, remainingDistance);
        Vec3 movement = this.chargeDirection.scale(movementDistance);
        Vec3 previousPosition = current;

        this.mob.move(MoverType.SELF, movement);
        this.mob.setDeltaMovement(movement);
        this.mob.forceAttackRotation();
        this.checkChargeHit(previousPosition, movement);
    }

    private void checkChargeHit(Vec3 previousPosition, Vec3 movementThisTick) {
        double halfWidth = this.mob.getBbWidth() * 0.5D;
        double height = this.mob.getBbHeight();

        AABB previousBox = new AABB(
                previousPosition.x - halfWidth,
                previousPosition.y,
                previousPosition.z - halfWidth,
                previousPosition.x + halfWidth,
                previousPosition.y + height,
                previousPosition.z + halfWidth
        ).inflate(CHARGE_HITBOX_EXPANSION);

        AABB chargeBox = previousBox.expandTowards(movementThisTick);

        for (LivingEntity entity : this.mob.level().getEntitiesOfClass(LivingEntity.class, chargeBox)) {
            if (entity == this.mob || !entity.isAlive() || this.chargeHitEntities.contains(entity.getUUID())) {
                continue;
            }

            if (entity == this.mob.getOwner()) {
                continue;
            }

            this.chargeHitEntities.add(entity.getUUID());

            float damage = this.mob.getAssaultChargeDamage();
            var source = this.mob.getAssaultChargeDamageSource(ROACWSpellRegistry.SUMMON_VIRILI.get());

            boolean hurt = DamageSources.applyDamage(entity, damage, source);

            if (hurt) {
                entity.addEffect(
                        new MobEffectInstance(
                                ROACWEffectRegistry.PLAGUE.get(),
                                PLAGUE_DURATION,
                                PLAGUE_AMPLIFIER,
                                false,
                                false,
                                true
                        ),
                        this.mob
                );
            }
        }
    }

    private void beginChargeEnd() {
        this.phase = Phase.CHARGE_END;
        this.phaseTicks = 0;
        this.mob.setDeltaMovement(Vec3.ZERO);
        this.mob.forceAttackRotation();
        this.mob.beginChargeEndAnimation();
    }

    private void tickChargeEnd(LivingEntity target) {
        this.mob.setDeltaMovement(Vec3.ZERO);
        this.mob.forceAttackRotation();

        if (this.phaseTicks >= 10) {
            this.mob.finishChargeAnimation();
            this.mob.setFlightControlLocked(false);
            this.mob.setAttackRotationLocked(false);
            this.chargeHitEntities.clear();

            if (this.forwardChargeCount < FORWARD_CHARGES_BEFORE_BARRAGE) {
                this.phase = Phase.CIRCLE;
                this.phaseTicks = 0;
                this.initializeCircleState(target);
            } else {
                this.prepareBarrage(target);
            }
        }
    }

    private void prepareBarrage(LivingEntity target) {
        if (this.forwardChargeCount < FORWARD_CHARGES_BEFORE_BARRAGE) {
            this.phase = Phase.CIRCLE;
            this.phaseTicks = 0;
            this.initializeCircleState(target);
            return;
        }

        Vec3 targetPosition = target.position();
        this.barrageChargeStartPosition = this.mob.position();

        Vec3 horizontalOffset = this.barrageChargeStartPosition.subtract(targetPosition);
        horizontalOffset = new Vec3(horizontalOffset.x, 0.0D, horizontalOffset.z);

        if (horizontalOffset.lengthSqr() < 0.0001D) {
            horizontalOffset = new Vec3(
                    -Mth.sin(this.mob.getYRot() * Mth.DEG_TO_RAD),
                    0.0D,
                    Mth.cos(this.mob.getYRot() * Mth.DEG_TO_RAD)
            );
        }

        this.barrageApproachDirection = horizontalOffset.normalize();
        this.barrageApproachDestination = targetPosition.add(
                this.barrageApproachDirection.scale(CIRCLE_MIN_RADIUS)
        );

        this.barrageApproachDestination = new Vec3(
                this.barrageApproachDestination.x,
                targetPosition.y + BARRAGE_HEIGHT_ABOVE_TARGET,
                this.barrageApproachDestination.z
        );

        this.barrageChargeCount = 0;
        this.barrageDistanceTravelled = 0.0D;
        this.barrageMissilesFired = 0;
        this.barragePauseTicks = 0;

        this.mob.finishChargeAnimation();
        this.mob.finishRemoteChargeAnimation();
        this.mob.setRemoteCharging(false);
        this.mob.setChargeWindupActive(false);
        this.mob.setFlightControlLocked(true);
        this.mob.setAttackRotationLocked(false);
        this.mob.setDeltaMovement(Vec3.ZERO);

        this.phase = Phase.BARRAGE_APPROACH;
        this.phaseTicks = 0;
    }

    private void tickBarrageApproach(LivingEntity target) {
        this.mob.setFlightControlLocked(true);
        this.mob.setAttackRotationLocked(false);

        Vec3 targetPosition = target.position();
        this.barrageApproachDestination = targetPosition.add(
                this.barrageApproachDirection.scale(CIRCLE_MIN_RADIUS)
        );

        this.barrageApproachDestination = new Vec3(
                this.barrageApproachDestination.x,
                targetPosition.y + BARRAGE_HEIGHT_ABOVE_TARGET,
                this.barrageApproachDestination.z
        );

        Vec3 current = this.mob.position();
        Vec3 difference = this.barrageApproachDestination.subtract(current);
        double distance = difference.length();

        if (distance <= BARRAGE_APPROACH_THRESHOLD || this.phaseTicks >= BARRAGE_APPROACH_MAX_TICKS) {
            if (distance > 0.001D) {
                this.mob.setPos(
                        this.barrageApproachDestination.x,
                        this.barrageApproachDestination.y,
                        this.barrageApproachDestination.z
                );
            }

            this.mob.setDeltaMovement(Vec3.ZERO);
            this.barrageChargeStartPosition = this.mob.position();
            this.beginBarrageFromApproach(target);
            return;
        }

        Vec3 direction = difference.normalize();
        double movementDistance = Math.min(BARRAGE_APPROACH_SPEED, distance);
        Vec3 movement = direction.scale(movementDistance);

        this.mob.move(MoverType.SELF, movement);
        this.mob.setDeltaMovement(movement);
        this.faceTarget(target);
    }

    private void beginBarrageFromApproach(LivingEntity target) {
        Vec3 targetPosition = target.position();

        Vec3 horizontalOffset = this.barrageChargeStartPosition.subtract(targetPosition);
        horizontalOffset = new Vec3(horizontalOffset.x, 0.0D, horizontalOffset.z);

        if (horizontalOffset.lengthSqr() < 0.0001D) {
            horizontalOffset = this.barrageApproachDirection;
        }

        Vec3 outwardDirection = horizontalOffset.normalize();

        Vec3 oppositeSide = targetPosition.add(
                outwardDirection.scale(-BARRAGE_TARGET_SIDE_DISTANCE)
        );

        this.barrageChargeDestination = new Vec3(
                oppositeSide.x,
                targetPosition.y + BARRAGE_HEIGHT_ABOVE_TARGET,
                oppositeSide.z
        );

        this.prepareCurrentBarrageDirection();
        this.barrageInitialChargePosition = this.barrageChargeStartPosition;
        this.barrageDistanceTravelled = 0.0D;
        this.barrageMissilesFired = 0;
        this.barragePauseTicks = 0;

        this.mob.setDeltaMovement(Vec3.ZERO);
        this.mob.setFlightControlLocked(true);
        this.mob.setAttackRotationLocked(false);

        this.beginBarrageCharge();
    }

    private void prepareCurrentBarrageDirection() {
        Vec3 direction = this.barrageChargeDestination.subtract(this.barrageChargeStartPosition);

        if (direction.lengthSqr() < 0.0001D) {
            return;
        }

        this.barrageChargeDirection = direction.normalize();
        this.barrageLockedYaw = this.getYawFromDirection(this.barrageChargeDirection);
    }

    private void beginBarrageCharge() {
        this.phase = Phase.BARRAGE_CHARGE;
        this.phaseTicks = 0;
        this.barrageDistanceTravelled = 0.0D;
        this.barrageMissilesFired = 0;

        this.mob.setFlightControlLocked(true);
        this.mob.setChargeWindupActive(false);
        this.mob.beginRemoteChargeAnimation();
        this.mob.setRemoteCharging(true);

        this.prepareCurrentBarrageDirection();
        this.mob.setAttackRotationLocked(true, this.barrageLockedYaw);
        this.mob.forceAttackRotation();
        this.mob.setDeltaMovement(Vec3.ZERO);
    }

    private void tickBarrageCharge(LivingEntity target) {
        this.mob.setAttackRotationLocked(true, this.barrageLockedYaw);
        this.mob.forceAttackRotation();

        Vec3 currentBeforeMove = this.mob.position();
        Vec3 toDestination = this.barrageChargeDestination.subtract(currentBeforeMove);
        double remainingDistance = toDestination.length();

        if (remainingDistance <= BARRAGE_DESTINATION_THRESHOLD || this.phaseTicks >= BARRAGE_CHARGE_MAX_TICKS) {
            while (this.barrageMissilesFired < BARRAGE_MISSILES_PER_CHARGE) {
                this.fireBarrageMissile(target);
            }

            this.mob.setPos(
                    this.barrageChargeDestination.x,
                    this.barrageChargeDestination.y,
                    this.barrageChargeDestination.z
            );

            this.mob.setDeltaMovement(Vec3.ZERO);
            this.mob.forceAttackRotation();
            this.finishBarrageCharge(target);
            return;
        }

        double movementDistance = Math.min(BARRAGE_CHARGE_SPEED, remainingDistance);
        Vec3 movement = this.barrageChargeDirection.scale(movementDistance);

        this.mob.move(MoverType.SELF, movement);

        Vec3 currentAfterMove = this.mob.position();
        double actualMovementDistance = currentAfterMove.distanceTo(currentBeforeMove);

        this.barrageDistanceTravelled += actualMovementDistance;
        this.mob.setDeltaMovement(movement);

        while (this.barrageMissilesFired < BARRAGE_MISSILES_PER_CHARGE) {
            double nextMissileDistance = (this.barrageMissilesFired + 1) * BARRAGE_MISSILE_SPACING;

            if (this.barrageDistanceTravelled < nextMissileDistance) {
                break;
            }

            this.fireBarrageMissile(target);
        }

        this.mob.setAttackRotationLocked(true, this.barrageLockedYaw);
        this.mob.forceAttackRotation();
    }

    private void fireBarrageMissile(LivingEntity target) {
        if (this.barrageMissilesFired >= BARRAGE_MISSILES_PER_CHARGE) {
            return;
        }

        this.mob.fireHomingPlagueMissileAt(target);
        this.barrageMissilesFired++;
    }

    private void finishBarrageCharge(LivingEntity target) {
        this.barrageChargeCount++;
        this.mob.setDeltaMovement(Vec3.ZERO);
        this.barrageDistanceTravelled = 0.0D;
        this.barrageMissilesFired = 0;

        if (this.barrageChargeCount >= BARRAGE_CHARGE_COUNT) {
            this.finishEntireBarrage(target);
            return;
        }

        this.mob.setRemoteCharging(false);
        this.mob.finishRemoteChargeAnimation();
        this.barragePauseTicks = 0;
        this.phase = Phase.BARRAGE_PAUSE;
        this.phaseTicks = 0;
    }

    private void tickBarragePause(LivingEntity target) {
        this.mob.setFlightControlLocked(false);
        this.mob.setAttackRotationLocked(false);

        Vec3 targetPosition = target.position();

        if (this.phaseTicks == 1) {
            Vec3 currentOffset = this.mob.position().subtract(targetPosition);
            double horizontalDistance = Math.sqrt(currentOffset.x * currentOffset.x + currentOffset.z * currentOffset.z);

            if (horizontalDistance > 0.001D) {
                this.circleAngle = Math.atan2(currentOffset.z, currentOffset.x);
                this.circleRadius = Mth.clamp(horizontalDistance, CIRCLE_MIN_RADIUS, CIRCLE_MAX_RADIUS);
            }
        }

        this.circleAngle += CIRCLE_SPEED * this.circleDirection;

        double desiredX = targetPosition.x + Math.cos(this.circleAngle) * this.circleRadius;
        double desiredZ = targetPosition.z + Math.sin(this.circleAngle) * this.circleRadius;
        double desiredY = targetPosition.y + BARRAGE_HEIGHT_ABOVE_TARGET;

        Vec3 desiredPosition = new Vec3(desiredX, desiredY, desiredZ);
        Vec3 difference = desiredPosition.subtract(this.mob.position());
        Vec3 movement = difference.scale(CIRCLE_RADIUS_CORRECTION);

        if (movement.lengthSqr() > 0.0001D) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.75D).add(movement));
        }

        this.faceTarget(target);

        this.barragePauseTicks++;

        if (this.barragePauseTicks >= BARRAGE_PAUSE_TICKS) {
            this.beginNextBarrageCharge(target);
        }
    }

    private void beginNextBarrageCharge(LivingEntity target) {
        this.barrageChargeStartPosition = this.mob.position();

        Vec3 targetPosition = target.position();
        Vec3 horizontalOffset = this.barrageChargeStartPosition.subtract(targetPosition);
        horizontalOffset = new Vec3(horizontalOffset.x, 0.0D, horizontalOffset.z);

        if (horizontalOffset.lengthSqr() < 0.0001D) {
            horizontalOffset = new Vec3(
                    -Mth.sin(this.mob.getYRot() * Mth.DEG_TO_RAD),
                    0.0D,
                    Mth.cos(this.mob.getYRot() * Mth.DEG_TO_RAD)
            );
        }

        Vec3 outwardDirection = horizontalOffset.normalize();

        Vec3 oppositeSide = targetPosition.add(
                outwardDirection.scale(-BARRAGE_TARGET_SIDE_DISTANCE)
        );

        this.barrageChargeDestination = new Vec3(
                oppositeSide.x,
                targetPosition.y + BARRAGE_HEIGHT_ABOVE_TARGET,
                oppositeSide.z
        );

        this.prepareCurrentBarrageDirection();
        this.mob.setDeltaMovement(Vec3.ZERO);
        this.mob.setAttackRotationLocked(true, this.barrageLockedYaw);
        this.mob.forceAttackRotation();
        this.beginBarrageCharge();
    }

    private void finishEntireBarrage(LivingEntity target) {
        Vec3 currentPosition = this.mob.position();
        Vec3 targetPosition = target.position();
        Vec3 currentOffset = currentPosition.subtract(targetPosition);
        Vec3 horizontalOffset = new Vec3(currentOffset.x, 0.0D, currentOffset.z);

        if (horizontalOffset.lengthSqr() < 0.0001D) {
            horizontalOffset = new Vec3(
                    -Mth.sin(this.mob.getYRot() * Mth.DEG_TO_RAD),
                    0.0D,
                    Mth.cos(this.mob.getYRot() * Mth.DEG_TO_RAD)
            ).scale(SWARM_HOVER_DISTANCE);
        }

        this.swarmHoverOffset = horizontalOffset;
        this.mob.setDeltaMovement(Vec3.ZERO);
        this.mob.setRemoteCharging(false);
        this.mob.finishRemoteChargeAnimation();
        this.mob.setAttackRotationLocked(false);
        this.mob.setFlightControlLocked(false);
        this.barrageChargeCount = 0;
        this.barrageDistanceTravelled = 0.0D;
        this.barrageMissilesFired = 0;
        this.barragePauseTicks = 0;
        this.forwardChargeCount = 0;
        this.phase = Phase.BARRAGE_FINISH;
        this.phaseTicks = 0;
        this.swarmActive = false;
    }

    private void tickBarrageFinish(LivingEntity target) {
        this.mob.setDeltaMovement(Vec3.ZERO);
        this.mob.setRemoteCharging(false);
        this.mob.finishRemoteChargeAnimation();
        this.mob.setAttackRotationLocked(false);
        this.faceTarget(target);

        if (this.phaseTicks >= BARRAGE_FINISH_SNAP_TICKS) {
            Vec3 currentPosition = this.mob.position();
            Vec3 targetPosition = target.position();
            Vec3 horizontalOffset = currentPosition.subtract(targetPosition);
            horizontalOffset = new Vec3(horizontalOffset.x, 0.0D, horizontalOffset.z);

            if (horizontalOffset.lengthSqr() < 0.0001D) {
                horizontalOffset = new Vec3(
                        -Mth.sin(this.mob.getYRot() * Mth.DEG_TO_RAD),
                        0.0D,
                        Mth.cos(this.mob.getYRot() * Mth.DEG_TO_RAD)
                ).scale(SWARM_HOVER_DISTANCE);
            }

            this.swarmHoverOffset = horizontalOffset;
            this.mob.setAttackRotationLocked(false);
            this.phase = Phase.SWARM;
            this.phaseTicks = 0;
            this.swarmActive = false;
            this.mob.setFlightControlLocked(true);
            this.mob.setDeltaMovement(Vec3.ZERO);
        }
    }

    private void tickSwarm(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            this.phase = Phase.CIRCLE;
            this.phaseTicks = 0;
            this.swarmActive = false;
            this.swarmHoverOffset = Vec3.ZERO;
            this.mob.setFlightControlLocked(false);
            this.mob.setDeltaMovement(Vec3.ZERO);
            return;
        }

        this.mob.setFlightControlLocked(true);
        this.mob.setAttackRotationLocked(false);

        if (!this.swarmActive) {
            Vec3 desiredHoverPosition = this.getInitialSwarmHoverPosition(target);
            Vec3 difference = desiredHoverPosition.subtract(this.mob.position());
            double distance = difference.length();

            if (distance > SWARM_HOVER_THRESHOLD) {
                Vec3 direction = difference.normalize();
                double movementDistance = Math.min(SWARM_HOVER_SPEED, distance);
                Vec3 movement = direction.scale(movementDistance);

                this.mob.move(MoverType.SELF, movement);
                this.mob.setDeltaMovement(movement);
                this.faceTarget(target);
                this.phaseTicks = 0;
                return;
            }

            this.mob.setDeltaMovement(Vec3.ZERO);
            this.swarmActive = true;
            this.phaseTicks = 0;
            this.faceTarget(target);
            return;
        }

        if (this.phaseTicks == 1 || (this.phaseTicks > 1 && (this.phaseTicks - 1) % SWARM_BEE_INTERVAL_TICKS == 0)) {
            this.mob.spawnSwarmSupport(target);
        }

        if (this.phaseTicks == 1 || (this.phaseTicks > 1 && (this.phaseTicks - 1) % SWARM_BABY_PLAGUEBRINGER_INTERVAL_TICKS == 0)) {
            this.mob.spawnBabyPlaguebringer(target);
        }

        double currentOffsetX = this.swarmHoverOffset.x;
        double currentOffsetZ = this.swarmHoverOffset.z;
        double currentAngle = Mth.atan2(currentOffsetZ, currentOffsetX);

        currentAngle += SWARM_ORBIT_SPEED;

        double horizontalDistance = Math.sqrt(currentOffsetX * currentOffsetX + currentOffsetZ * currentOffsetZ);

        if (horizontalDistance < 0.001D) {
            horizontalDistance = SWARM_HOVER_DISTANCE;
        }

        double orbitX = Math.cos(currentAngle) * horizontalDistance;
        double orbitZ = Math.sin(currentAngle) * horizontalDistance;

        Vec3 targetPosition = target.position();

        Vec3 activeHoverPosition = new Vec3(
                targetPosition.x + orbitX,
                targetPosition.y + SWARM_HOVER_HEIGHT,
                targetPosition.z + orbitZ
        );

        this.swarmHoverOffset = new Vec3(orbitX, 0.0D, orbitZ);

        Vec3 activeDifference = activeHoverPosition.subtract(this.mob.position());
        double activeDistance = activeDifference.length();

        if (activeDistance > SWARM_HOVER_THRESHOLD) {
            Vec3 direction = activeDifference.normalize();
            double movementDistance = Math.min(SWARM_HOVER_SPEED, activeDistance);
            Vec3 movement = direction.scale(movementDistance);

            this.mob.move(MoverType.SELF, movement);

            Vec3 currentVelocity = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(
                    currentVelocity.scale(1.0D - SWARM_MOVEMENT_SMOOTHING)
                            .add(movement.scale(SWARM_MOVEMENT_SMOOTHING))
            );
        } else {
            Vec3 currentVelocity = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(currentVelocity.scale(0.90D));
        }

        this.faceTarget(target);

        if (this.phaseTicks >= SWARM_DURATION_TICKS) {
            this.phase = Phase.CIRCLE;
            this.phaseTicks = 0;
            this.swarmActive = false;
            this.circleDirection = -this.circleDirection;
            this.initializeCircleState(target);
            this.swarmHoverOffset = Vec3.ZERO;
            this.mob.setDeltaMovement(Vec3.ZERO);
            this.mob.setFlightControlLocked(false);
        }
    }

    private Vec3 getInitialSwarmHoverPosition(LivingEntity target) {
        Vec3 targetPosition = target.position();

        return new Vec3(
                targetPosition.x + this.swarmHoverOffset.x,
                targetPosition.y + SWARM_HOVER_HEIGHT,
                targetPosition.z + this.swarmHoverOffset.z
        );
    }

    private void tickRest(LivingEntity target) {
        this.mob.setAttackRotationLocked(false);
        this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.85D));

        if (target != null && target.isAlive()) {
            this.faceTarget(target);
        }

        if (this.phaseTicks >= 20) {
            this.phase = Phase.CIRCLE;
            this.phaseTicks = 0;
            this.initializeCircleState(target);
        }
    }

    private float getYawFromDirection(Vec3 direction) {
        return (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90.0F;
    }

    private float getPitchFromDirection(Vec3 direction) {
        double horizontalLength = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        return (float) (-Mth.atan2(direction.y, horizontalLength) * Mth.RAD_TO_DEG);
    }
}