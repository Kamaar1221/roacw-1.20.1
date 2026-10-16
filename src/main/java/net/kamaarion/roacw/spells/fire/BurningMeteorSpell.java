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
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BurningMeteorSpell extends AbstractSpell {

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(
            ROACW.MODID,
            "burning_meteor"
    );

    private static final int BASE_METEORS_PER_CAST = 3;
    private static final double METEOR_RADIUS = 12.0D;
    private static final double METEOR_MIN_HEIGHT = 20.0D;
    private static final double METEOR_MAX_HEIGHT = 30.0D;
    private static final double LOOSE_TARGET_RADIUS = 6.0D;

    private static final float CAST_SOUND_VOLUME = 1.0F;
    private static final float CAST_SOUND_PITCH_MIN = 0.9F;
    private static final float CAST_SOUND_PITCH_MAX = 1.1F;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(30)
            .setAllowCrafting(false)
            .build();

    public BurningMeteorSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 3;
        this.castTime = 20;
        this.baseManaCost = 10;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable(
                        "ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 1)
                ),
                Component.translatable(
                        "ui.irons_spellbooks.aoe_damage",
                        Utils.stringTruncation(getAoeDamage(spellLevel, caster), 1)
                ),
                Component.translatable(
                        "ui.irons_spellbooks.radius",
                        Utils.stringTruncation(getExplosionRadius(caster), 1)
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


        playCastSound(serverLevel, entity);

        Vec3 targetPos = null;
        ICastData castData = playerMagicData.getAdditionalCastData();

        if (castData instanceof TargetEntityCastData targetData) {
            targetPos = targetData.getTargetPosition(serverLevel);
        }

        if (targetPos == null) {
            HitResult raycast = Utils.raycastForEntity(
                    level,
                    entity,
                    32.0F,
                    true
            );

            if (raycast.getType() == HitResult.Type.ENTITY) {
                targetPos = ((EntityHitResult) raycast).getEntity().position();
            } else {
                targetPos = Utils.moveToRelativeGroundLevel(
                        level,
                        raycast.getLocation(),
                        5
                );
            }
        }

        if (targetPos == null) {
            return;
        }

        int meteorCount = getMeteorsPerCast(spellLevel);

        for (int i = 0; i < meteorCount; i++) {
            double spawnAngle = entity.getRandom().nextDouble() * Math.PI * 2.0D;
            double spawnDistance = Math.sqrt(entity.getRandom().nextDouble()) * METEOR_RADIUS;

            double offsetX = Math.cos(spawnAngle) * spawnDistance;
            double offsetZ = Math.sin(spawnAngle) * spawnDistance;

            double meteorHeight = METEOR_MIN_HEIGHT
                    + entity.getRandom().nextDouble() * (METEOR_MAX_HEIGHT - METEOR_MIN_HEIGHT);

            double targetAngle = entity.getRandom().nextDouble() * Math.PI * 2.0D;
            double targetDistance = Math.sqrt(entity.getRandom().nextDouble()) * LOOSE_TARGET_RADIUS;

            double targetOffsetX = Math.cos(targetAngle) * targetDistance;
            double targetOffsetZ = Math.sin(targetAngle) * targetDistance;

            Vec3 meteorTarget = targetPos.add(
                    targetOffsetX,
                    0.0D,
                    targetOffsetZ
            );

            Vec3 spawnPos = meteorTarget.add(
                    offsetX,
                    meteorHeight,
                    offsetZ
            );

            BurningMeteorEntity meteor = new BurningMeteorEntity(
                    serverLevel,
                    entity,
                    spawnPos,
                    meteorTarget
            );

            meteor.setPos(spawnPos);
            meteor.setDamage(getDamage(spellLevel, entity));
            meteor.setAoeDamage(getAoeDamage(spellLevel, entity));
            meteor.setAoeRadius(getExplosionRadius(entity));

            serverLevel.addFreshEntity(meteor);
        }

        super.onCast(
                level,
                spellLevel,
                entity,
                castSource,
                playerMagicData
        );
    }

    private void playCastSound(ServerLevel level, LivingEntity caster) {
        float pitch = CAST_SOUND_PITCH_MIN
                + caster.getRandom().nextFloat() * (CAST_SOUND_PITCH_MAX - CAST_SOUND_PITCH_MIN);

        level.playSound(
                null,
                caster.getX(), caster.getY(), caster.getZ(),
                ROACWSoundRegistry.BURNING_SKY_CAST.get(),
                SoundSource.PLAYERS,
                CAST_SOUND_VOLUME,
                pitch
        );
    }

    private int getMeteorsPerCast(int spellLevel) {
        return BASE_METEORS_PER_CAST + spellLevel;
    }

    @Override
    public SpellDamageSource getDamageSource(
            @Nullable Entity projectile,
            Entity attacker
    ) {
        return super.getDamageSource(projectile, attacker)
                .setFireTicks(10)
                .setIFrames(0);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 0.45F;
    }

    public float getAoeDamage(int spellLevel, LivingEntity caster) {
        return 1.0F + getSpellPower(spellLevel, caster) * 0.12F;
    }

    private float getExplosionRadius(LivingEntity caster) {
        return 2.5F;
    }
}