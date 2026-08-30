package net.kamaarion.roacw.entity.spells.earthly_virtue;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EarthlyVirtueAoE extends Entity {
    private LivingEntity owner;
    private UUID ownerUUID;
    private float damage;
    private float healing;
    private int duration = 400; // 20 seconds
    private int pulseInterval = 40; // every second
    private int ticks;

    // Tracks entities who have already received the 20-second buff from this specific zone
    private final Set<UUID> buffedTargets = new HashSet<>();

    // Locked ground position set on spawn — entity stays here regardless of owner movement
    private double groundX, groundY, groundZ;
    private boolean groundPosSet = false;
    private static final float RADIUS = 5.0F;

    public EarthlyVirtueAoE(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        if (owner != null) this.ownerUUID = owner.getUUID();
    }

    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.ownerUUID);
            if (entity instanceof LivingEntity living) this.owner = living;
        }
        return this.owner;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setHealing(float healing) {
        this.healing = healing;
    }

    @Override
    public void tick() {
        super.tick();

        // Lock position to spawn point — prevents entity from following player when airborne
        if (!groundPosSet) {
            groundX = this.getX();
            groundY = this.getY();
            groundZ = this.getZ();
            groundPosSet = true;
        }
        this.setPos(groundX, groundY, groundZ);

        ticks++; // Increments on both client and server to keep ticks in sync

        if (!level().isClientSide) {
            if (ticks > 0 && ticks % pulseInterval == 0) {
                pulse();
            }
            if (ticks >= duration) {
                discard();
            }
        }
    }

    private void pulse() {
        LivingEntity caster = getOwner();
        if (caster == null) {
            System.out.println("[EarthlyVirtue] Pulse skipped — caster is null!");
            return;
        }

        System.out.println("[EarthlyVirtue] Pulse fired at tick=" + ticks + " pos=(" + groundX + ", " + groundY + ", " + groundZ + ")" + " healing=" + healing + " damage=" + damage);
        ServerLevel server = (ServerLevel) level();

        // Holy blastwave ring — size matches RADIUS
        server.sendParticles(
                new BlastwaveParticleOptions(
                        new Vector3f(1.0F, 1.0F, 0.9F),
                        RADIUS
                ),
                groundX, groundY + 0.1D, groundZ,
                1, 0, 0, 0, 0
        );

        AABB aoe = new AABB(
                new Vec3(groundX, groundY, groundZ),
                new Vec3(groundX, groundY, groundZ)
        ).inflate(RADIUS, RADIUS * 2, RADIUS); // double Y to catch targets above/below ground level

        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, aoe)) {
            double distSqr = target.distanceToSqr(groundX, groundY, groundZ);
            if (distSqr > (RADIUS * RADIUS)) continue;

            // Determine if target should be treated as an ally.
            // Caster is explicitly forced true here so they receive heals/buffs alongside allies inside the radius.
            boolean shouldHeal = (target == caster) ||
                    Utils.shouldHealEntity(caster, target) ||
                    (target instanceof net.minecraft.world.entity.player.Player && caster instanceof net.minecraft.world.entity.player.Player);

            System.out.println("[EarthlyVirtue] Target: " + target.getName().getString() + " | shouldHeal=" + shouldHeal + " | healing=" + healing + " | distSqr=" + distSqr);

            if (shouldHeal) {
                // Apply healing strictly inside the boundary loop
                target.heal(healing);

                MobEffectInstance targetEffect = target.getEffect(ROACWEffectRegistry.SANCTIFIED_BEDROCK.get());

                // Refresh armor shield only when missing, whether it's an ally or the caster itself
                if (targetEffect == null) {
                    target.addEffect(new MobEffectInstance(
                            ROACWEffectRegistry.SANCTIFIED_BEDROCK.get(),
                            400,
                            0,
                            false,
                            false,
                            true
                    ));
                    System.out.println("[EarthlyVirtue] Granted fresh shield to: " + target.getName().getString());
                }
            } else {
                // Damage enemy targets inside the boundary loop
                var source = ROACWSpellRegistry.EARTHLY_VIRTUE
                        .get()
                        .getDamageSource(this, caster);
                DamageSources.applyDamage(target, damage, source);
            }
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) this.ownerUUID = tag.getUUID("Owner");
        if (tag.contains("Damage")) this.damage = tag.getFloat("Damage");
        if (tag.contains("Healing")) this.healing = tag.getFloat("Healing");
        if (tag.contains("Ticks")) this.ticks = tag.getInt("Ticks");
        if (tag.contains("GroundX")) {
            this.groundX = tag.getDouble("GroundX");
            this.groundY = tag.getDouble("GroundY");
            this.groundZ = tag.getDouble("GroundZ");
            this.groundPosSet = true;
        }

        // Load tracking list from NBT data
        if (tag.contains("BuffedTargets", Tag.TAG_LIST)) {
            this.buffedTargets.clear();
            ListTag list = tag.getList("BuffedTargets", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag targetTag = list.getCompound(i);
                if (targetTag.hasUUID("UUID")) {
                    this.buffedTargets.add(targetTag.getUUID("UUID"));
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) tag.putUUID("Owner", this.ownerUUID);
        tag.putFloat("Damage", this.damage);
        tag.putFloat("Healing", this.healing);
        tag.putInt("Ticks", this.ticks);
        tag.putDouble("GroundX", this.groundX);
        tag.putDouble("GroundY", this.groundY);
        tag.putDouble("GroundZ", this.groundZ);

        // Save tracking list to NBT data
        ListTag list = new ListTag();
        for (UUID uuid : this.buffedTargets) {
            CompoundTag targetTag = new CompoundTag();
            targetTag.putUUID("UUID", uuid);
            list.add(targetTag);
        }
        tag.put("BuffedTargets", list);
    }
}
