package net.kamaarion.roacw.spells.exo;

import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;
import com.github.L_Ender.cataclysm.init.ModParticle;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.acetheeldritchking.aces_spell_utils.spells.ASSpellAnimations;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.spells.final_rend.FinalRendAoE;
import net.kamaarion.roacw.registeries.ROACWAttributeRegistry;
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

public class FinalRendSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "final_rend");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(ROACWSchoolRegistry.EXO_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(16)
            .build();

    public FinalRendSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 25;
        this.spellPowerPerLevel = 1;
        this.castTime = 70;
        this.baseManaCost = 150;
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
        return ASSpellAnimations.ANIMATION_POWERFUL_SWORD_SLASH;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.SHOCKWAVE_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(ROACWSoundRegistry.ELECTRIC_SWORD_SWING.get());
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (entity.tickCount % 10 == 0) {
            spawnParticleRing(level, entity, 1.25F);
            spawnParticleRing(level, entity, 3.25F);
            spawnParticleRing(level, entity, 5.25F);
        }

        super.onServerCastTick(level, spellLevel, entity, playerMagicData);
    }

    private void spawnParticleRing(Level level, LivingEntity entity, float radius) {
        int count = 16;

        for (int i = 0; i < count; i++) {
            double theta = Math.toRadians(360.0 / count) * i;
            double x = Math.cos(theta) * radius;
            double z = Math.sin(theta) * radius;

            MagicManager.spawnParticles(
                    level,
                    ModParticle.SPARK.get(),
                    entity.getX() + x,
                    entity.getY(),
                    entity.getZ() + z,
                    1,
                    0,
                    0,
                    0,
                    0.1,
                    false
            );
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float radius = 3.25F;
        float distance = 2.15F;

        Vec3 hitLocation = entity.position()
                .add(0, entity.getBbHeight() * 0.3F, 0)
                .add(entity.getForward().multiply(distance, 0.35F, distance));

        var entities = level.getEntities(
                entity,
                AABB.ofSize(hitLocation, radius * 2, radius, radius * 2)
        );

        for (Entity target : entities) {
            if (target instanceof LivingEntity livingTarget
                    && entity.distanceToSqr(target) < radius * radius
                    && Utils.hasLineOfSight(level, entity.getEyePosition(), target.getBoundingBox().getCenter(), true)) {

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

        boolean mirrored = false;

        if (entity instanceof Player player) {
            var selection = new SpellSelectionManager(player).getSelection();

            if (selection != null) {
                mirrored = selection.slot.equals(SpellSelectionManager.OFFHAND);
            }
        }

        FinalRendAoE swipe = new FinalRendAoE(level, mirrored);
        swipe.moveTo(hitLocation);
        swipe.setYRot(entity.getYRot());

        level.addFreshEntity(swipe);

        ScreenShake_Entity.ScreenShake(
                level,
                entity.position(),
                6.0F,
                0.08F,
                20,
                20
        );

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker)
                .setIFrames(0)
                .setLifestealPercent(1.0F);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        float attributeDamage = (float) (
                getSpellPower(spellLevel, entity)
                        + entity.getAttributeValue(ROACWAttributeRegistry.EXO_MAGIC_POWER.get())
        );

        return (attributeDamage * 1.5F)
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
                )
        );
    }
}