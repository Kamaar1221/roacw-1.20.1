package net.kamaarion.roacw.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.spells.burning_meteor.BurningMeteorEntity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BurningMeteorSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            ResourceLocation.fromNamespaceAndPath(
                    ROACW.MODID,
                    "burning_meteor"
            );

    /*
     * Base number of meteors created per cast interval.
     *
     * The actual number increases with spell level.
     *
     * Level 1 = 4 meteors
     * Level 2 = 5 meteors
     * Level 3 = 6 meteors
     * Level 4 = 7 meteors
     * Level 5 = 8 meteors
     */
    private static final int BASE_METEORS_PER_CAST = 3;

    /*
     * Radius of the area where meteors can spawn.
     */
    private static final double METEOR_RADIUS = 12.0D;

    /*
     * Minimum and maximum height above the target.
     */
    private static final double METEOR_MIN_HEIGHT = 20.0D;
    private static final double METEOR_MAX_HEIGHT = 30.0D;

    /*
     * Maximum distance from the main target that an individual
     * meteor is allowed to aim.
     *
     * This keeps the meteors loosely converging instead of
     * having every meteor strike the exact same location.
     */
    private static final double LOOSE_TARGET_RADIUS = 6.0D;

    private final DefaultConfig defaultConfig =
            new DefaultConfig()
                    .setMinRarity(SpellRarity.LEGENDARY)
                    .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
                    .setMaxLevel(5)
                    .setCooldownSeconds(12)
                    .build();

    public BurningMeteorSpell() {
        this.manaCostPerLevel = 4;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 3;
        this.castTime = 20;
        this.baseManaCost = 10;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(
            int spellLevel,
            LivingEntity caster
    ) {
        return List.of(
                Component.translatable(
                        "ui.irons_spellbooks.damage",
                        Utils.stringTruncation(
                                getDamage(spellLevel, caster),
                                1
                        )
                ),

                Component.translatable(
                        "ui.irons_spellbooks.aoe_damage",
                        Utils.stringTruncation(
                                getAoeDamage(spellLevel, caster),
                                1
                        )
                ),

                Component.translatable(
                        "ui.irons_spellbooks.radius",
                        Utils.stringTruncation(
                                getExplosionRadius(caster),
                                1
                        )
                )
        );
    }

    @Override
    public int getCastTime(int spellLevel) {
        return castTime + 5 * spellLevel;
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
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
    public boolean checkPreCastConditions(
            Level level,
            int spellLevel,
            LivingEntity entity,
            MagicData playerMagicData
    ) {
        Utils.preCastTargetHelper(
                level,
                entity,
                playerMagicData,
                this,
                32,
                0.35F,
                false
        );

        return true;
    }

    @Override
    public void onCast(
            Level level,
            int spellLevel,
            LivingEntity entity,
            CastSource castSource,
            MagicData playerMagicData
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetPos = null;

        /*
         * Try to use Iron's Spell Books target data first.
         */
        ICastData castData =
                playerMagicData.getAdditionalCastData();

        if (castData instanceof TargetEntityCastData targetData) {
            targetPos =
                    targetData.getTargetPosition(serverLevel);
        }

        /*
         * If there is no entity target, perform a raycast.
         */
        if (targetPos == null) {

            HitResult raycast =
                    Utils.raycastForEntity(
                            level,
                            entity,
                            32.0F,
                            true
                    );

            if (raycast.getType() == HitResult.Type.ENTITY) {

                targetPos =
                        ((EntityHitResult) raycast)
                                .getEntity()
                                .position();

            } else {

                targetPos =
                        Utils.moveToRelativeGroundLevel(
                                level,
                                raycast.getLocation(),
                                5
                        );
            }
        }

        if (targetPos == null) {
            return;
        }

        /*
         * ---------------------------------------------------------
         * METEOR SHOWER
         * ---------------------------------------------------------
         *
         * Higher spell levels create more meteors rather than
         * simply making every meteor overwhelmingly powerful.
         */
        int meteorCount =
                getMeteorsPerCast(spellLevel);

        for (int i = 0; i < meteorCount; i++) {

            /*
             * -----------------------------------------------------
             * RANDOM SPAWN POSITION
             * -----------------------------------------------------
             *
             * Pick a random point inside a circle around the
             * target.
             */
            double spawnAngle =
                    entity.getRandom().nextDouble()
                            * Math.PI * 2.0D;

            double spawnDistance =
                    Math.sqrt(
                            entity.getRandom().nextDouble()
                    ) * METEOR_RADIUS;

            double offsetX =
                    Math.cos(spawnAngle) * spawnDistance;

            double offsetZ =
                    Math.sin(spawnAngle) * spawnDistance;

            /*
             * Randomize the meteor's starting height.
             *
             * This prevents all meteors from arriving at exactly
             * the same time.
             */
            double meteorHeight =
                    METEOR_MIN_HEIGHT
                            + entity.getRandom().nextDouble()
                            * (
                            METEOR_MAX_HEIGHT
                                    - METEOR_MIN_HEIGHT
                    );

            /*
             * -----------------------------------------------------
             * LOOSE CONVERGENCE
             * -----------------------------------------------------
             *
             * Rather than targeting the exact center, each meteor
             * gets its own nearby impact point.
             *
             * This gives enemies surrounding the primary target
             * a chance to get hit as well.
             */
            double targetAngle =
                    entity.getRandom().nextDouble()
                            * Math.PI * 2.0D;

            double targetDistance =
                    Math.sqrt(
                            entity.getRandom().nextDouble()
                    ) * LOOSE_TARGET_RADIUS;

            double targetOffsetX =
                    Math.cos(targetAngle) * targetDistance;

            double targetOffsetZ =
                    Math.sin(targetAngle) * targetDistance;

            /*
             * The actual impact point for this meteor.
             */
            Vec3 meteorTarget =
                    targetPos.add(
                            targetOffsetX,
                            0.0D,
                            targetOffsetZ
                    );

            /*
             * Spawn the meteor above and around its individual
             * impact point.
             */
            Vec3 spawnPos =
                    meteorTarget.add(
                            offsetX,
                            meteorHeight,
                            offsetZ
                    );

            /*
             * Create the meteor.
             */
            BurningMeteorEntity meteor =
                    new BurningMeteorEntity(
                            serverLevel,
                            entity,
                            spawnPos,
                            meteorTarget
                    );

            meteor.setPos(spawnPos);

            /*
             * Set spell-scaled direct-hit damage.
             */
            meteor.setDamage(
                    getDamage(
                            spellLevel,
                            entity
                    )
            );

            /*
             * Set spell-scaled AOE damage.
             */
            meteor.setAoeDamage(
                    getAoeDamage(
                            spellLevel,
                            entity
                    )
            );

            /*
             * Set the AOE radius.
             */
            meteor.setAoeRadius(
                    getExplosionRadius(entity)
            );

            /*
             * Add the meteor to the world.
             */
            serverLevel.addFreshEntity(meteor);
        }

        /*
         * Finish the spell cast.
         */
        super.onCast(
                level,
                spellLevel,
                entity,
                castSource,
                playerMagicData
        );
    }

    /*
     * Number of meteors created per continuous-cast interval.
     *
     * Level 1 = 4
     * Level 2 = 5
     * Level 3 = 6
     * Level 4 = 7
     * Level 5 = 8
     */
    private int getMeteorsPerCast(int spellLevel) {
        return BASE_METEORS_PER_CAST + spellLevel;
    }

    @Override
    public SpellDamageSource getDamageSource(
            @Nullable Entity projectile,
            Entity attacker
    ) {
        return super.getDamageSource(
                        projectile,
                        attacker
                )
                .setFireTicks(10)
                .setIFrames(0);
    }

    /*
     * Direct meteor impact damage.
     *
     * Reduced from 0.75F to 0.45F so level 1 doesn't hit
     * excessively hard, while still scaling with spell power.
     */
    private float getDamage(
            int spellLevel,
            LivingEntity caster
    ) {
        return getSpellPower(
                spellLevel,
                caster
        ) * 0.45F;
    }

    /*
     * AOE damage.
     *
     * Reduced substantially at lower levels so the spell's
     * overall power comes primarily from the meteor shower
     * rather than each individual explosion.
     */
    public float getAoeDamage(
            int spellLevel,
            LivingEntity caster
    ) {
        return 1.0F
                + getSpellPower(
                spellLevel,
                caster
        ) * 0.12F;
    }

    /*
     * Explosion radius remains constant.
     *
     * The spell becomes stronger through damage and meteor count
     * rather than progressively larger explosions.
     */
    private float getExplosionRadius(
            LivingEntity caster
    ) {
        return 2.5F;
    }
}