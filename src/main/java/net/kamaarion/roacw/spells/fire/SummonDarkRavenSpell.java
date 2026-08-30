package net.kamaarion.roacw.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.*;
import net.kamaarion.roacw.entity.summon.dark_raven.DarkRavenEntity;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Summons a group of Dark Ravens around the caster.
 *
 * Raven scaling follows the same general pattern as the Plague Charger:
 *
 * - Damage scales from spell power.
 * - Health scales with spell level and the caster's entity power multiplier.
 * - Each raven receives its calculated damage and health directly when
 *   spawned.
 * - Summon lifetime and dismissal are handled through SummonManager.
 */
public class SummonDarkRavenSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            ResourceLocation.fromNamespaceAndPath(
                    "roacw",
                    "summon_dark_raven"
            );

    private final DefaultConfig defaultConfig =
            new DefaultConfig()
                    .setMinRarity(SpellRarity.LEGENDARY)
                    .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
                    .setMaxLevel(3)
                    .setCooldownSeconds(180)
                    .build();

    /**
     * How strongly the caster's Eldritch Spell Power contributes to
     * raven damage, relative to the spell's primary Fire scaling.
     *
     * 0.5 = Eldritch Spell Power counts at half effectiveness, added
     * on top of (not instead of) the normal Fire-based spell power.
     */
    private static final float ELDRITCH_SPELL_POWER_SCALING = 0.5F;

    public SummonDarkRavenSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(
            int spellLevel,
            LivingEntity caster
    ) {
        return List.of(
                Component.translatable(
                        "ui.irons_spellbooks.summon_count",
                        getSummonCountForLevel(spellLevel)
                ),

                Component.translatable(
                        "ui.irons_spellbooks.hp",
                        Utils.stringTruncation(
                                getRavenHealth(spellLevel, caster),
                                1
                        )
                ),

                Component.translatable(
                        "ui.irons_spellbooks.damage",
                        Utils.stringTruncation(
                                getRavenDamage(spellLevel, caster),
                                1
                        )
                )
        );
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
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(
                ROACWSoundRegistry.DARK_RAVEN_SUMMON.get()
        );
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
            ServerPlayer serverPlayer,
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

        PlayerRecasts recasts =
                playerMagicData.getPlayerRecasts();

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

            int summonTime =
                    20 * 60 * 10;

            int summonCount =
                    getSummonCountForLevel(spellLevel);

            float ravenDamage =
                    getRavenDamage(
                            spellLevel,
                            entity
                    );

            float ravenHealth =
                    getRavenHealth(
                            spellLevel,
                            entity
                    );

            for (int i = 0; i < summonCount; i++) {

                DarkRavenEntity raven =
                        new DarkRavenEntity(
                                ROACWEntityRegistry.DARK_RAVEN.get(),
                                serverLevel
                        );

                /*
                 * -----------------------------------------------------
                 * SPAWN FORMATION
                 * -----------------------------------------------------
                 *
                 * Evenly space the ravens around the caster.
                 */
                double angle =
                        (Math.PI * 2.0D / summonCount) * i;

                double radius = 2.0D;

                double spawnX =
                        entity.getX()
                                + Math.cos(angle) * radius;

                double spawnY =
                        entity.getEyeY() + 0.5D;

                double spawnZ =
                        entity.getZ()
                                + Math.sin(angle) * radius;

                raven.moveTo(
                        spawnX,
                        spawnY,
                        spawnZ,
                        entity.getYRot(),
                        0.0F
                );

                raven.finalizeSpawn(
                        serverLevel,
                        world.getCurrentDifficultyAt(
                                raven.blockPosition()
                        ),
                        MobSpawnType.MOB_SUMMONED,
                        null,
                        null
                );

                /*
                 * -----------------------------------------------------
                 * HEALTH SCALING
                 * -----------------------------------------------------
                 *
                 * Match the Plague Charger's health formula:
                 *
                 *     (20 + spellLevel * 5)
                 *          * entityPowerMultiplier
                 *
                 * Level 1 = 25 HP
                 * Level 2 = 30 HP
                 * Level 3 = 35 HP
                 */
                raven.getAttribute(
                        Attributes.MAX_HEALTH
                ).setBaseValue(ravenHealth);

                raven.setHealth(
                        raven.getMaxHealth()
                );

                /*
                 * -----------------------------------------------------
                 * DAMAGE SCALING
                 * -----------------------------------------------------
                 *
                 * The Raven's charge uses spellPower as its actual
                 * damage value, so give the raven the same calculated
                 * value here.
                 */
                raven.getAttribute(
                        Attributes.ATTACK_DAMAGE
                ).setBaseValue(ravenDamage);

                raven.setSpellPower(
                        ravenDamage
                );

                /*
                 * -----------------------------------------------------
                 * SPAWN
                 * -----------------------------------------------------
                 */
                serverLevel.addFreshEntity(
                        raven
                );

                SummonManager.initSummon(
                        entity,
                        raven,
                        summonTime,
                        summonedEntitiesCastData
                );
            }

            RecastInstance recastInstance =
                    new RecastInstance(
                            this.getSpellId(),
                            spellLevel,
                            getRecastCount(
                                    spellLevel,
                                    entity
                            ),
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

    /**
     * Raven summon count.
     *
     * Level 1 = 2
     * Level 2 = 4
     * Level 3 = 6
     */
    private int getSummonCountForLevel(
            int spellLevel
    ) {
        return 2 + (
                2 * Math.max(
                        0,
                        spellLevel - 1
                )
        );
    }

    /**
     * Raven health scaling.
     *
     * This follows the same health progression used by the
     * Plague Charger:
     *
     *     (20 + spellLevel * 5)
     *          * entity power multiplier
     *
     * If no caster is available, the entity power multiplier
     * defaults to 1.0 so the spell can safely be queried by
     * interfaces such as the Inscription Table.
     */
    private float getRavenHealth(
            int spellLevel,
            @Nullable LivingEntity caster
    ) {
        float entityPowerMultiplier =
                caster == null
                        ? 1.0F
                        : getEntityPowerMultiplier(caster);

        return (
                20.0F
                        + spellLevel * 5.0F
        ) * entityPowerMultiplier;
    }

    /**
     * Raven damage scaling.
     *
     * Fire is the primary school because this spell uses
     * SchoolRegistry.FIRE_RESOURCE.
     *
     * getSpellPower() therefore provides the Fire-scaled spell power
     * used by the raven's charge. On top of that, the caster's
     * Eldritch Spell Power contributes as a secondary multiplier, at
     * half effectiveness.
     *
     * If no caster is available, the spell returns its raw base
     * spell-power progression instead of attempting to query
     * school power from a null entity.
     */
    private float getRavenDamage(
            int spellLevel,
            @Nullable LivingEntity caster
    ) {
        if (caster == null) {
            return this.baseSpellPower
                    + ((spellLevel - 1) * this.spellPowerPerLevel);
        }

        float baseDamage =
                getSpellPower(
                        spellLevel,
                        caster
                );

        float eldritchPower =
                (float) SchoolRegistry.ELDRITCH
                        .get()
                        .getPowerFor(caster);

        float eldritchBonus =
                1.0F
                        + (
                        (eldritchPower - 1.0F)
                                * ELDRITCH_SPELL_POWER_SCALING
                );

        return baseDamage
                * eldritchBonus;
    }
}

