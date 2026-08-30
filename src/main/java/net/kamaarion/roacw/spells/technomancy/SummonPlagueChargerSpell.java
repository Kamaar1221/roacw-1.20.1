package net.kamaarion.roacw.spells.technomancy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.*;
import net.acetheeldritchking.cataclysm_spellbooks.registries.CSSchoolRegistry;
import net.acetheeldritchking.cataclysm_spellbooks.spells.CSSpellAnimations;
import net.kamaarion.roacw.entity.summon.plague_charger.PlagueChargerEntity;
import net.kamaarion.roacw.items.curios.alchemical_decanter.AlchemicalDecanterEvents;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SummonPlagueChargerSpell extends AbstractSpell {

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath("roacw", "summon_plague_charger");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(CSSchoolRegistry.TECHNOMANCY_RESOURCE) // primary school - Nature applied as scaling bonus below
            .setMaxLevel(4)
            .setCooldownSeconds(240)
            .build();

    public SummonPlagueChargerSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 25;
        this.baseManaCost = 70;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.summon_count", getSummonCountForLevel(spellLevel)),
                Component.translatable("ui.irons_spellbooks.hp", Utils.stringTruncation(getChargerHealth(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getChargerDamage(spellLevel, caster), 1))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return CSSpellAnimations.ANIMATION_CONSTRUCT_SUMMON;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() { return SpellAnimations.ANIMATION_LONG_CAST_FINISH; }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public void onRecastFinished(net.minecraft.server.level.ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (SummonManager.recastFinishedHelper(serverPlayer, recastInstance, recastResult, castDataSerializable)) {
            super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
        }
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new SummonedEntitiesCastData();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        PlayerRecasts recasts = playerMagicData.getPlayerRecasts();
        if (!recasts.hasRecastForSpell(this)) {
            if (!(world instanceof ServerLevel serverLevel)) {
                super.onCast(world, spellLevel, entity, castSource, playerMagicData);
                return;
            }

            SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
            int summonTime = 20 * 60 * 10;

            int summonCount = getSummonCountForLevel(spellLevel);

            for (int i = 0; i < summonCount; i++) {

                double angle = (Math.PI * 2D / summonCount) * i;
                double radius = 2.5D;

                double x = entity.getX() + Math.cos(angle) * radius;
                double z = entity.getZ() + Math.sin(angle) * radius;
                double y = entity.getY();

                PlagueChargerEntity charger =
                        new PlagueChargerEntity(
                                ROACWEntityRegistry.PLAGUE_CHARGER.get(),
                                serverLevel);

                // NEW
                charger.setFormationIndex(i);

                if (i % 2 == 0) {
                    charger.setCombatRole(PlagueChargerEntity.CombatRole.CHARGER);
                } else {
                    charger.setCombatRole(PlagueChargerEntity.CombatRole.RANGED);
                }

                charger.moveTo(x, y, z, entity.getYRot(), 0);

                charger.finalizeSpawn(
                        serverLevel,
                        world.getCurrentDifficultyAt(charger.blockPosition()),
                        MobSpawnType.MOB_SUMMONED,
                        null,
                        null
                );

                charger.getAttribute(Attributes.ATTACK_DAMAGE)
                        .setBaseValue(getChargerMeleeDamage(spellLevel, entity));

                charger.getAttribute(Attributes.MAX_HEALTH)
                        .setBaseValue(getChargerHealth(spellLevel, entity));

                charger.setHealth(charger.getMaxHealth());

                charger.setSpellPower(getChargerDamage(spellLevel, entity));

                serverLevel.addFreshEntity(charger);
                AlchemicalDecanterEvents.tagAsPlagueSummon(charger);

                serverLevel.sendParticles(
                        ROACWParticleRegistry.PLAGUE_CLOUD.get(),
                        x,
                        y + 0.5D,
                        z,
                        20,
                        0.3D,
                        0.4D,
                        0.3D,
                        0.02D
                );

                SummonManager.initSummon(
                        entity,
                        charger,
                        summonTime,
                        summonedEntitiesCastData
                );
            }

            RecastInstance recastInstance = new RecastInstance(this.getSpellId(), spellLevel, getRecastCount(spellLevel, entity), summonTime, castSource, summonedEntitiesCastData);
            recasts.addRecast(recastInstance, playerMagicData);
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private int getSummonCountForLevel(int spellLevel) {
        return spellLevel;
    }

    private float getChargerHealth(int spellLevel, LivingEntity caster) {
        return (20 + spellLevel * 5) * getEntityPowerMultiplier(caster);
    }

    // Reuses getChargerDamage()'s already dual-scaled (Technomancy + Nature)
    // figure rather than recomputing from getSpellPower() alone - previously
    // this only scaled with Technomancy, so the charge dash (which adds this
    // as its ATTACK_DAMAGE base) was missing the Nature bonus that the
    // stinger (which reads spellPower directly) already got.
    private float getChargerMeleeDamage(int spellLevel, LivingEntity caster) {
        return getChargerDamage(spellLevel, caster) * 0.5F;
    }

    // Dual-school scaling: Technomancy is the primary/base school power (via getSpellPower,
    // which reads whatever DefaultConfig.setSchoolResource is set to - now Technomancy),
    // Nature is a secondary multiplicative bonus - mirrors ImpalingColumnSpell's Geo+Holy pattern.
    private float getChargerDamage(int spellLevel, LivingEntity caster) {
        if (caster == null) {
            return this.baseSpellPower + ((spellLevel - 1) * this.spellPowerPerLevel);
        }

        float baseDamage = getSpellPower(spellLevel, caster);
        float naturePower = (float) SchoolRegistry.NATURE.get().getPowerFor(caster);
        float natureBonus = 1.0F + ((naturePower - 1.0F) * 0.5F);

        return baseDamage * natureBonus;
    }
}