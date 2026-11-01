package net.kamaarion.roacw.spells.brimstone;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.particle.BrimstoneSlashParticleOptions;
import net.kamaarion.roacw.registeries.ROACWSchoolRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BrimstoneLacerationSpell extends AbstractSpell {

    private static final ResourceLocation SPELL_ID = ROACW.id("brimstone_laceration");

    private static final float RADIUS = 3.25F;
    private static final float DISTANCE = 1.9F;
    private static final float PARTICLE_SCALE = 1.0F;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(ROACWSchoolRegistry.BRIMSTONE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    public BrimstoneLacerationSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 2;
        this.castTime = 10;
        this.baseManaCost = 30;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable(
                        "ui.irons_spellbooks.damage",
                        getDamageText(spellLevel, caster)
                )
        );
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.FLAMING_STRIKE_UPSWING.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.FLAMING_STRIKE_SWING.get());
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return SPELL_ID;
    }

    @Override
    public boolean canBeInterrupted(@Nullable Player player) {
        return false;
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        return getCastTime(spellLevel);
    }

    @Override
    public void onCast(
            Level level,
            int spellLevel,
            LivingEntity entity,
            CastSource castSource,
            MagicData playerMagicData
    ) {
        if (!level.isClientSide) {
            Vec3 forward = entity.getForward();

            Vec3 hitLocation = entity.position()
                    .add(0, entity.getBbHeight() * 0.3F, 0)
                    .add(forward.scale(DISTANCE));

            AABB hitbox = AABB.ofSize(
                    hitLocation,
                    RADIUS * 2,
                    RADIUS,
                    RADIUS * 2
            );

            var damageSource = this.getDamageSource(entity).setFireTicks(60);

            for (Entity target : level.getEntities(entity, hitbox)) {
                if (!(target instanceof LivingEntity livingTarget)) {
                    continue;
                }

                if (!livingTarget.isAlive() || !livingTarget.isPickable()) {
                    continue;
                }

                if (entity.distanceToSqr(livingTarget) >= RADIUS * RADIUS) {
                    continue;
                }

                Vec3 offset = livingTarget.getBoundingBox()
                        .getCenter()
                        .subtract(entity.getEyePosition());

                if (offset.dot(forward) < 0) {
                    continue;
                }

                if (!Utils.hasLineOfSight(
                        level,
                        entity.getEyePosition(),
                        livingTarget.getBoundingBox().getCenter(),
                        true
                )) {
                    continue;
                }

                float damage = getDamage(spellLevel, entity);

                if (DamageSources.applyDamage(livingTarget, damage, damageSource)) {
                    MagicManager.spawnParticles(
                            level,
                            ParticleHelper.FIRE,
                            livingTarget.getX(),
                            livingTarget.getY() + livingTarget.getBbHeight() * 0.5F,
                            livingTarget.getZ(),
                            30,
                            livingTarget.getBbWidth() * 0.5,
                            livingTarget.getBbHeight() * 0.5,
                            livingTarget.getBbWidth() * 0.5,
                            0.03,
                            false
                    );

                    if (level instanceof ServerLevel) {
                        EnchantmentHelper.doPostHurtEffects(livingTarget, entity);
                        EnchantmentHelper.doPostDamageEffects(entity, livingTarget);
                    }
                }
            }

            boolean mirrored = playerMagicData.getCastingEquipmentSlot()
                    .equals(SpellSelectionManager.OFFHAND);

            Vec3 slashRight = forward.cross(new Vec3(0, 1, 0));

            if (slashRight.lengthSqr() < 1.0E-6) {
                slashRight = new Vec3(1, 0, 0);
            }

            Vec3 slashNormal = slashRight.normalize().cross(forward).normalize();

            Vec3 particleLocation = hitLocation.add(slashNormal.scale(0.5));

            MagicManager.spawnParticles(
                    level,
                    new BrimstoneSlashParticleOptions(
                            (float) forward.x,
                            (float) forward.y,
                            (float) forward.z,
                            mirrored,
                            false,
                            PARTICLE_SCALE
                    ),
                    particleLocation.x,
                    particleLocation.y,
                    particleLocation.z,
                    1,
                    0,
                    0,
                    0,
                    0,
                    true
            );
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity) + getAdditionalDamage(entity);
    }

    private float getAdditionalDamage(LivingEntity entity) {
        float weaponDamage = Utils.getWeaponDamage(entity, MobType.UNDEFINED);
        weaponDamage += EnchantmentHelper.getFireAspect(entity);
        return weaponDamage;
    }

    private String getDamageText(int spellLevel, LivingEntity entity) {
        float weaponDamage = Utils.getWeaponDamage(entity, MobType.UNDEFINED);

        String additionalDamage = "";

        if (weaponDamage > 0) {
            additionalDamage = String.format(
                    " (+%s)",
                    Utils.stringTruncation(weaponDamage, 1)
            );
        }

        return Utils.stringTruncation(
                getDamage(spellLevel, entity),
                1
        ) + additionalDamage;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }
}