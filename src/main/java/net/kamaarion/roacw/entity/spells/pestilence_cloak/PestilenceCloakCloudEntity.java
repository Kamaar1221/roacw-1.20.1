package net.kamaarion.roacw.entity.spells.pestilence_cloak;

import net.kamaarion.roacw.entity.projectile.plague_cloud.PlagueCloudEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class PestilenceCloakCloudEntity extends PlagueCloudEntity {

    // Vertical reach of the owner-buff hitbox, decoupled from radius on
    // purpose - radius scales with spell level and drives horizontal
    // spread, but the visible cloud is only ever ~3.5 blocks tall, so a
    // uniform inflate() would let the owner stay buffed many blocks above
    // the actual visible cloud at high radius.
    private static final double CLOUD_VERTICAL_REACH = 3.5D;
    private static final double CLOUD_VERTICAL_FLOOR = 1.0D;

    private static final int OWNER_EFFECT_DURATION = 10; // ticks; refreshed each tick while inside

    public PestilenceCloakCloudEntity(EntityType<? extends PestilenceCloakCloudEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            applyOwnerBuff();
        }
    }

    private AABB getCloudArea() {
        float r = getRadius();
        return new AABB(
                getX() - r, getY() - CLOUD_VERTICAL_FLOOR, getZ() - r,
                getX() + r, getY() + CLOUD_VERTICAL_REACH, getZ() + r
        );
    }

    @Override
    protected DamageSource getDamageSource() {
        return damageSources().magic(); // or a custom nature/plague-specific DamageSource if ROACW registers one
    }

    private void applyOwnerBuff() {

        LivingEntity owner = getOwner();

        if (owner == null || !owner.isAlive()) {
            return;
        }

        boolean inCloud = getCloudArea().contains(owner.getX(), owner.getY(), owner.getZ());

        if (!inCloud) {
            owner.removeEffect(MobEffects.NIGHT_VISION);
            owner.removeEffect(ROACWEffectRegistry.PESTILENCE_CLOAK_BUFF.get());
            return;
        }

        owner.addEffect(new MobEffectInstance(ROACWEffectRegistry.PESTILENCE_CLOAK_BUFF.get(), OWNER_EFFECT_DURATION, 0, true, false, false));
        owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, OWNER_EFFECT_DURATION, 0, true, false, false));
    }

    // Safety net: if the cloud is discarded (LIFETIME hit, or anything
    // else) while the owner is still standing in it, cut both immediately
    // instead of letting their last refreshed duration linger.
    @Override
    public void onRemovedFromWorld() {
        LivingEntity owner = getOwner();
        if (owner != null) {
            owner.removeEffect(MobEffects.NIGHT_VISION);
            owner.removeEffect(ROACWEffectRegistry.PESTILENCE_CLOAK_BUFF.get());
        }
        super.onRemovedFromWorld();
    }
}