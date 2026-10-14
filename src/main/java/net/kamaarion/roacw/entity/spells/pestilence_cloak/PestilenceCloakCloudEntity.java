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
    private static final double CLOUD_VERTICAL_REACH = 3.5D;
    private static final double CLOUD_VERTICAL_FLOOR = 1.0D;
    private static final int OWNER_EFFECT_DURATION = 10;

    public PestilenceCloakCloudEntity(
            EntityType<? extends PestilenceCloakCloudEntity> type,
            Level level
    ) {
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
        float radius = getRadius();

        return new AABB(
                getX() - radius,
                getY() - CLOUD_VERTICAL_FLOOR,
                getZ() - radius,
                getX() + radius,
                getY() + CLOUD_VERTICAL_REACH,
                getZ() + radius
        );
    }

    @Override
    protected DamageSource getDamageSource() {
        return damageSources().magic();
    }

    private void applyOwnerBuff() {
        LivingEntity owner = getOwner();

        if (owner == null || !owner.isAlive()) {
            return;
        }

        boolean inCloud = getCloudArea().contains(
                owner.getX(),
                owner.getY(),
                owner.getZ()
        );

        if (!inCloud) {
            owner.removeEffect(MobEffects.NIGHT_VISION);
            owner.removeEffect(ROACWEffectRegistry.PESTILENCE_CLOAK_BUFF.get());
            return;
        }

        owner.addEffect(new MobEffectInstance(
                ROACWEffectRegistry.PESTILENCE_CLOAK_BUFF.get(),
                OWNER_EFFECT_DURATION,
                0,
                true,
                false,
                false
        ));

        owner.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                OWNER_EFFECT_DURATION,
                0,
                true,
                false,
                false
        ));
    }

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