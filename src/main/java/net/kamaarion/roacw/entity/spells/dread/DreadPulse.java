package net.kamaarion.roacw.entity.spells.dread;

import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.kamaarion.roacw.effects.DreadEffect;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DreadPulse extends Entity {

    private LivingEntity owner;
    private UUID ownerUUID;

    private float radius = 12.0F;
    private float healthThreshold = 20.0F;

    private int dreadDuration = 100;
    private int dreadAmplifier = 0;

    private float currentRadius = 0.0F;

    private int ticks;
    private static final int MAX_TICKS = 8;

    private static final float RADIUS_PER_TICK = 1.5F;
    private static final int FIRE_PARTICLES = 96;

    private final Set<UUID> affectedTargets = new HashSet<>();

    public DreadPulse(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;

        if (owner != null) {
            this.ownerUUID = owner.getUUID();
        }
    }

    public LivingEntity getOwner() {
        if (this.owner == null
                && this.ownerUUID != null
                && this.level() instanceof ServerLevel serverLevel) {

            Entity entity = serverLevel.getEntity(this.ownerUUID);

            if (entity instanceof LivingEntity living) {
                this.owner = living;
            }
        }

        return this.owner;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setHealthThreshold(float healthThreshold) {
        this.healthThreshold = healthThreshold;
    }

    public void setDreadDuration(int dreadDuration) {
        this.dreadDuration = dreadDuration;
    }

    public void setDreadAmplifier(int dreadAmplifier) {
        this.dreadAmplifier = dreadAmplifier;
    }

    @Override
    public void tick() {
        super.tick();

        this.setPos(
                this.getX(),
                this.getY(),
                this.getZ()
        );

        ticks++;

        if (level().isClientSide) {
            return;
        }

        ServerLevel server = (ServerLevel) level();

        float previousRadius = currentRadius;

        currentRadius = Math.min(
                radius,
                currentRadius + RADIUS_PER_TICK
        );

        spawnFireRing(server);

        applyDreadToTargets(
                previousRadius,
                currentRadius
        );

        if (ticks >= MAX_TICKS) {
            discard();
        }
    }

    private void spawnFireRing(ServerLevel server) {
        if (currentRadius <= 0.0F) {
            return;
        }

        for (int i = 0; i < FIRE_PARTICLES; i++) {
            double angle =
                    (Math.PI * 2.0D * i) / FIRE_PARTICLES;

            double x =
                    getX()
                            + Math.cos(angle) * currentRadius;

            double z =
                    getZ()
                            + Math.sin(angle) * currentRadius;

            server.sendParticles(
                    ParticleRegistry.DRAGON_FIRE_PARTICLE.get(),
                    x,
                    getY() + 0.05D,
                    z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private void applyDreadToTargets(
            float previousRadius,
            float newRadius
    ) {
        LivingEntity caster = getOwner();

        if (caster == null) {
            return;
        }

        double maxRadius = newRadius;

        AABB searchBox = new AABB(
                getX() - maxRadius,
                getY() - maxRadius,
                getZ() - maxRadius,
                getX() + maxRadius,
                getY() + maxRadius,
                getZ() + maxRadius
        );

        for (LivingEntity target : level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox
        )) {
            if (!canAffect(target, caster)) {
                continue;
            }

            if (affectedTargets.contains(target.getUUID())) {
                continue;
            }

            double distanceSqr = target.distanceToSqr(
                    getX(),
                    getY(),
                    getZ()
            );

            if (distanceSqr > newRadius * newRadius) {
                continue;
            }

            if (distanceSqr <= previousRadius * previousRadius) {
                continue;
            }

            if (!meetsDreadRequirement(target)) {
                continue;
            }

            applyDread(target, caster);

            affectedTargets.add(target.getUUID());
        }
    }

    private boolean canAffect(
            LivingEntity target,
            LivingEntity caster
    ) {
        if (target == caster) {
            return false;
        }

        if (!target.isAlive()) {
            return false;
        }

        if (target.getType().is(ROACWTags.DREAD_IMMUNE_ENTITIES)) {
            return false;
        }

        return true;
    }

    private boolean meetsDreadRequirement(
            LivingEntity target
    ) {
        return target.getMaxHealth() <= healthThreshold
                || target.getHealth() <= healthThreshold;
    }

    private void applyDread(
            LivingEntity target,
            LivingEntity caster
    ) {
        target.getPersistentData().putUUID(
                DreadEffect.DREAD_SOURCE_TAG,
                caster.getUUID()
        );

        target.addEffect(
                new MobEffectInstance(
                        ROACWEffectRegistry.DREAD.get(),
                        dreadDuration,
                        dreadAmplifier,
                        false,
                        false,
                        true
                )
        );
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(
            CompoundTag tag
    ) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }

        if (tag.contains("Radius")) {
            this.radius = tag.getFloat("Radius");
        }

        if (tag.contains("HealthThreshold")) {
            this.healthThreshold = tag.getFloat("HealthThreshold");
        }

        if (tag.contains("DreadDuration")) {
            this.dreadDuration = tag.getInt("DreadDuration");
        }

        if (tag.contains("DreadAmplifier")) {
            this.dreadAmplifier = tag.getInt("DreadAmplifier");
        }

        if (tag.contains("CurrentRadius")) {
            this.currentRadius = tag.getFloat("CurrentRadius");
        }

        if (tag.contains("Ticks")) {
            this.ticks = tag.getInt("Ticks");
        }

        if (tag.contains("AffectedTargets")) {
            this.affectedTargets.clear();

            for (int i = 0; i < tag.getList(
                    "AffectedTargets",
                    8
            ).size(); i++) {
                this.affectedTargets.add(
                        UUID.fromString(
                                tag.getList(
                                        "AffectedTargets",
                                        8
                                ).getString(i)
                        )
                );
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(
            CompoundTag tag
    ) {
        if (this.ownerUUID != null) {
            tag.putUUID(
                    "Owner",
                    this.ownerUUID
            );
        }

        tag.putFloat(
                "Radius",
                this.radius
        );

        tag.putFloat(
                "HealthThreshold",
                this.healthThreshold
        );

        tag.putInt(
                "DreadDuration",
                this.dreadDuration
        );

        tag.putInt(
                "DreadAmplifier",
                this.dreadAmplifier
        );

        tag.putFloat(
                "CurrentRadius",
                this.currentRadius
        );

        tag.putInt(
                "Ticks",
                this.ticks
        );
    }
}