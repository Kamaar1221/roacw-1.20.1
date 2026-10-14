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
import net.kamaarion.roacw.entity.summon.mini_plaguebringer.MiniPlaguebringerEntity;
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

public class SummonMiniPlaguebringerSpell extends AbstractSpell {

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(
            "roacw",
            "summon_mini_plaguebringer"
    );

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(CSSchoolRegistry.TECHNOMANCY_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(240)
            .build();

    private static final float NORMAL_MISSILE_DAMAGE_MULTIPLIER = 1.0F;
    private static final float NORMAL_SPLASH_DAMAGE_MULTIPLIER = 0.25F;
    private static final float HOMING_MISSILE_DAMAGE_MULTIPLIER = 0.5F;

    public SummonMiniPlaguebringerSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 25;
        this.baseManaCost = 70;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(
            int spellLevel,
            LivingEntity caster
    ) {
        float spellDamage = getMiniPlaguebringerDamage(spellLevel, caster);

        float normalMissileDamage =
                spellDamage * NORMAL_MISSILE_DAMAGE_MULTIPLIER;

        float normalSplashDamage =
                spellDamage * NORMAL_SPLASH_DAMAGE_MULTIPLIER;

        float homingMissileDamage =
                spellDamage * HOMING_MISSILE_DAMAGE_MULTIPLIER;

        return List.of(
                Component.translatable(
                        "ui.irons_spellbooks.summon_count",
                        getSummonCountForLevel(spellLevel)
                ),
                Component.translatable(
                        "ui.irons_spellbooks.hp",
                        Utils.stringTruncation(
                                getMiniPlaguebringerHealth(spellLevel, caster),
                                1
                        )
                ),
                Component.translatable(
                        "ui.roacw.mini_plaguebringer_normal_missile_damage",
                        Utils.stringTruncation(normalMissileDamage, 1)
                ),
                Component.translatable(
                        "ui.roacw.mini_plaguebringer_splash_damage",
                        Utils.stringTruncation(normalSplashDamage, 1)
                ),
                Component.translatable(
                        "ui.roacw.mini_plaguebringer_homing_missile_damage",
                        Utils.stringTruncation(homingMissileDamage, 1)
                )
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
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ANIMATION_LONG_CAST_FINISH;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public int getRecastCount(
            int spellLevel,
            @Nullable LivingEntity entity
    ) {
        return 2;
    }

    @Override
    public void onRecastFinished(
            net.minecraft.server.level.ServerPlayer serverPlayer,
            RecastInstance recastInstance,
            RecastResult recastResult,
            ICastDataSerializable castDataSerializable
    ) {
        if (SummonManager.recastFinishedHelper(
                serverPlayer,
                recastInstance,
                recastResult,
                castDataSerializable
        )) {
            super.onRecastFinished(
                    serverPlayer,
                    recastInstance,
                    recastResult,
                    castDataSerializable
            );
        }
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new SummonedEntitiesCastData();
    }

    @Override
    public void onCast(
            Level world,
            int spellLevel,
            LivingEntity entity,
            CastSource castSource,
            MagicData playerMagicData
    ) {
        PlayerRecasts recasts = playerMagicData.getPlayerRecasts();

        if (!recasts.hasRecastForSpell(this)) {
            if (!(world instanceof ServerLevel serverLevel)) {
                super.onCast(
                        world,
                        spellLevel,
                        entity,
                        castSource,
                        playerMagicData
                );
                return;
            }

            SummonedEntitiesCastData summonedEntitiesCastData =
                    new SummonedEntitiesCastData();

            int summonTime = 20 * 60 * 10;
            int summonCount = getSummonCountForLevel(spellLevel);

            for (int i = 0; i < summonCount; i++) {
                MiniPlaguebringerEntity plaguebringer =
                        new MiniPlaguebringerEntity(
                                ROACWEntityRegistry.MINI_PLAGUEBRINGER.get(),
                                serverLevel
                        );

                plaguebringer.setFormation(i, summonCount);

                plaguebringer.moveTo(
                        entity.getX(),
                        entity.getY() + 2.0D,
                        entity.getZ(),
                        entity.getYRot(),
                        0.0F
                );

                plaguebringer.finalizeSpawn(
                        serverLevel,
                        world.getCurrentDifficultyAt(
                                plaguebringer.blockPosition()
                        ),
                        MobSpawnType.MOB_SUMMONED,
                        null,
                        null
                );

                plaguebringer.getAttribute(Attributes.MAX_HEALTH).setBaseValue(
                        getMiniPlaguebringerHealth(spellLevel, entity)
                );

                plaguebringer.setHealth(plaguebringer.getMaxHealth());

                plaguebringer.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(
                        getMiniPlaguebringerMeleeDamage(spellLevel, entity)
                );

                plaguebringer.setSpellPower(
                        getMiniPlaguebringerDamage(spellLevel, entity)
                );

                serverLevel.addFreshEntity(plaguebringer);

                AlchemicalDecanterEvents.tagAsPlagueSummon(plaguebringer);

                serverLevel.sendParticles(
                        ROACWParticleRegistry.PLAGUE_CLOUD.get(),
                        plaguebringer.getX(),
                        plaguebringer.getY() + 0.5D,
                        plaguebringer.getZ(),
                        20,
                        0.3D,
                        0.4D,
                        0.3D,
                        0.02D
                );

                SummonManager.initSummon(
                        entity,
                        plaguebringer,
                        summonTime,
                        summonedEntitiesCastData
                );
            }

            RecastInstance recastInstance = new RecastInstance(
                    this.getSpellId(),
                    spellLevel,
                    getRecastCount(spellLevel, entity),
                    summonTime,
                    castSource,
                    summonedEntitiesCastData
            );

            recasts.addRecast(
                    recastInstance,
                    playerMagicData
            );
        }

        super.onCast(
                world,
                spellLevel,
                entity,
                castSource,
                playerMagicData
        );
    }

    private int getSummonCountForLevel(int spellLevel) {
        return spellLevel;
    }

    private float getMiniPlaguebringerHealth(
            int spellLevel,
            LivingEntity caster
    ) {
        return (20 + spellLevel * 5) * getEntityPowerMultiplier(caster);
    }

    private float getMiniPlaguebringerMeleeDamage(
            int spellLevel,
            LivingEntity caster
    ) {
        return getMiniPlaguebringerDamage(spellLevel, caster) * 0.5F;
    }

    private float getMiniPlaguebringerDamage(
            int spellLevel,
            @Nullable LivingEntity caster
    ) {
        if (caster == null) {
            return this.baseSpellPower
                    + ((spellLevel - 1) * this.spellPowerPerLevel);
        }

        float baseDamage = getSpellPower(spellLevel, caster);

        float naturePower = (float) SchoolRegistry.NATURE.get()
                .getPowerFor(caster);

        float natureBonus = 1.0F + ((naturePower - 1.0F) * 0.5F);

        return baseDamage * natureBonus;
    }
}