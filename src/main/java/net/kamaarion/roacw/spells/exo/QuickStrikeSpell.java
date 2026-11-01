package net.kamaarion.roacw.spells.exo;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.acetheeldritchking.aces_spell_utils.spells.ASSpellAnimations;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.spells.quick_strike.QuickStrikeAoE;
import net.kamaarion.roacw.registeries.ROACWSchoolRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class QuickStrikeSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "quick_strike");
    private boolean mirrored = false;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(ROACWSchoolRegistry.EXO_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(16)
            .build();

    public QuickStrikeSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 3;
        this.castTime = 6;
        this.baseManaCost = 50;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public boolean canBeCraftedBy(Player player) {
        return false;
    }

    @Override
    public boolean allowLooting() {
        return false;
    }

    @Override
    public boolean allowCrafting() {
        return false;
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
    public AnimationHolder getCastStartAnimation() {
        return mirrored
                ? ASSpellAnimations.ANIMATION_LEFT_HORIZONTAL_SWORD_SLASH
                : ASSpellAnimations.ANIMATION_RIGHT_HORIZONTAL_SWORD_SLASH;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return spellLevel;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.DIVINE_SMITE_WINDUP.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(ROACWSoundRegistry.ELECTRIC_SWORD_SWING.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId())) {
            playerMagicData.getPlayerRecasts().addRecast(
                    new RecastInstance(
                            getSpellId(),
                            spellLevel,
                            getRecastCount(spellLevel, entity),
                            15 * 20,
                            castSource,
                            null
                    ),
                    playerMagicData
            );
        }

        float radius = 3.25F;
        float distance = 2.2F;

        Vec3 hitLocation = entity.position()
                .add(0, entity.getBbHeight() * 0.3F, 0)
                .add(entity.getForward().multiply(distance, 0.35F, distance));

        var entities = level.getEntities(
                entity,
                AABB.ofSize(hitLocation, radius * 2, radius, radius * 2)
        );

        for (Entity target : entities) {
            if (target instanceof LivingEntity livingTarget
                    && livingTarget.isPickable()
                    && entity.distanceToSqr(livingTarget) < radius * radius
                    && Utils.hasLineOfSight(
                    level,
                    entity.getEyePosition(),
                    livingTarget.getBoundingBox().getCenter(),
                    true
            )) {

                if (DamageSources.applyDamage(
                        livingTarget,
                        getDamage(spellLevel, entity) + getBonusDamage(spellLevel, entity),
                        getDamageSource(entity)
                )) {
                    MagicManager.spawnParticles(
                            level,
                            ParticleHelper.BLOOD,
                            livingTarget.getX(),
                            livingTarget.getY() + livingTarget.getBbHeight() * 0.5F,
                            livingTarget.getZ(),
                            50,
                            livingTarget.getBbWidth() * 0.5F,
                            livingTarget.getBbHeight() * 0.5F,
                            livingTarget.getBbWidth() * 0.5F,
                            0.03,
                            false
                    );

                    EnchantmentHelper.doPostDamageEffects(entity, livingTarget);
                }
            }
        }

        mirrored = playerMagicData.getPlayerRecasts()
                .getRemainingRecastsForSpell(spellId.toString()) % 2 == 0;

        QuickStrikeAoE swipe = new QuickStrikeAoE(level, mirrored);
        swipe.moveTo(hitLocation);
        swipe.setYRot(entity.getYRot());

        level.addFreshEntity(swipe);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker)
                .setIFrames(0)
                .setLifestealPercent(0.35F);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return (getSpellPower(spellLevel, entity) / 1.5F)
                + Utils.getWeaponDamage(entity, MobType.UNDEFINED);
    }

    private float getBonusDamage(int spellLevel, LivingEntity caster) {
        return getRecastCount(spellLevel, caster);
    }

    private String getDamageText(int spellLevel, LivingEntity caster) {
        if (caster != null) {
            float weaponDamage = Utils.getWeaponDamage(caster, MobType.UNDEFINED);

            String plus = "";

            if (weaponDamage > 0) {
                plus = String.format(
                        " (+%s)",
                        Utils.stringTruncation(weaponDamage, 1)
                );
            }

            String damage = Utils.stringTruncation(getDamage(spellLevel, caster), 1);

            return damage + plus;
        }

        return "" + getSpellPower(spellLevel, caster);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable(
                        "ui.irons_spellbooks.damage",
                        getDamageText(spellLevel, caster)
                ),
                Component.translatable(
                        "ui.ROACW.recast_bonus_damage",
                        Utils.stringTruncation(getBonusDamage(spellLevel, caster), 1)
                )
        );
    }
}