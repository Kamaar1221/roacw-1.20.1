package net.kamaarion.roacw.spells.holy;

import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import com.gametechbc.gtbcs_geomancy_plus.api.init.GGSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueAoE;
import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueShards;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSpellRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static dev.shadowsoffire.placebo.PlaceboClient.ticks;

public class EarthlyVirtueSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath("roacw", "earthly_virtue");

    private static final float AOE_RADIUS = 5.0F;
    private static final int RING_COUNT = 12;
    private static final int BUFF_DURATION_TICKS = 400; // 10 seconds

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(60.0)
            .build();

    public EarthlyVirtueSpell() {
        this.manaCostPerLevel = 3;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 15;
        this.baseManaCost = 15;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        float rawDamage = getDamage(spellLevel, caster);
        float rawHealing = getHealing(spellLevel, caster);
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(rawDamage, 1)),
                Component.translatable("ui.irons_spellbooks.healing", Utils.stringTruncation(rawHealing, 1))
        );
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.STOMP;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData magicData) {
        if (world.isClientSide) return;
        if (entity == null) return;

        float damage = getDamage(spellLevel, entity);
        float healing = getHealing(spellLevel, entity);

        world.playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                MMSounds.EFFECT_GEOMANCY_RUMBLE_1.get(),
                SoundSource.PLAYERS,
                2.0F,
                0.6F
        );

        Vec3 center = Utils.moveToRelativeGroundLevel(
                world,
                entity.position(),
                12
        );

        int shakeDuration = 12 + (spellLevel * 2);
        float shakeRadius = 10 + spellLevel;

        CameraShakeManager.addCameraShake(
                new CameraShakeData(
                        world,
                        shakeDuration,
                        center,
                        shakeRadius
                )
        );

        for (int i = 0; i < RING_COUNT; i++) {

            double angle = (2 * Math.PI / RING_COUNT) * i;

            double x = center.x + AOE_RADIUS * Math.cos(angle);
            double z = center.z + AOE_RADIUS * Math.sin(angle);

            Vec3 groundPos = Utils.moveToRelativeGroundLevel(
                    world,
                    new Vec3(x, center.y, z),
                    12
            );

            double y = groundPos.y;

            EarthlyVirtueShards marker =
                    new EarthlyVirtueShards(
                            ROACWEntityRegistry.EARTHLY_VIRTUE_SHARDS.get(),
                            world
                    );

            marker.setOwner(entity);
            marker.setPos(x, y - 0.5D, z);

            float yaw = (float) Math.toDegrees(angle);

            marker.setYRot(yaw);
            marker.setXRot(90.0F);

            marker.yRotO = yaw;
            marker.xRotO = 90.0F;

            world.addFreshEntity(marker);
        }

        AABB aoe = new AABB(center, center).inflate(AOE_RADIUS);

        System.out.println(
                "Pulse fired | tick=" + ticks
        );

        for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, aoe)) {

            if (target == entity) continue;
            if (!target.isPickable()) continue;

            if (target.distanceToSqr(center) > (AOE_RADIUS * AOE_RADIUS))
                continue;

            if (Utils.shouldHealEntity(entity, target)
                    || (target instanceof net.minecraft.world.entity.player.Player
                    && entity instanceof net.minecraft.world.entity.player.Player)) {

                target.heal(healing);

                target.addEffect(new MobEffectInstance(
                        ROACWEffectRegistry.SANCTIFIED_BEDROCK.get(),
                        BUFF_DURATION_TICKS,
                        0,
                        false,
                        false,
                        true
                ));


            } else {

                var damageSource = ROACWSpellRegistries.EARTHLY_VIRTUE
                        .get()
                        .getDamageSource(entity, entity);

                if (DamageSources.applyDamage(
                        target,
                        damage,
                        damageSource
                )) {

                    target.setDeltaMovement(
                            target.getDeltaMovement().add(
                                    0.0D,
                                    0.8D,
                                    0.0D
                            )
                    );

                    target.hurtMarked = true;
                }
            }
        }
        EarthlyVirtueAoE pulseZone =
                new EarthlyVirtueAoE(
                        ROACWEntityRegistry.EARTHLY_VIRTUE_AOE.get(),
                        world
                );

        pulseZone.setOwner(entity);

        pulseZone.setDamage(damage * 0.5F);
        pulseZone.setHealing(healing * 0.5F);

        pulseZone.setPos(
                center.x,
                center.y,
                center.z
        );

        world.addFreshEntity(pulseZone);

        super.onCast(world, spellLevel, entity, castSource, magicData);
    }
    private float getDamage(int spellLevel, LivingEntity entity) {

        if (entity == null) {
            return this.baseSpellPower +
                    ((spellLevel - 1) * this.spellPowerPerLevel);
        }

        float holyPower =
                (float) SchoolRegistry.HOLY.get().getPowerFor(entity);

        float geoPower =
                (float) GGSchools.GEO.get().getPowerFor(entity);

        float baseDamage =
                this.baseSpellPower +
                        ((spellLevel - 1) * this.spellPowerPerLevel);

        float hybridMultiplier =
                (holyPower * 0.5F) +
                        (geoPower * 0.5F);

        return (baseDamage * hybridMultiplier) * 1.5F;
    }

    private float getHealing(int spellLevel, LivingEntity entity) {

        if (entity == null) {
            return this.baseSpellPower +
                    ((spellLevel - 1) * this.spellPowerPerLevel);
        }

        float holyPower =
                (float) SchoolRegistry.HOLY.get().getPowerFor(entity);

        float baseHealing =
                this.baseSpellPower +
                        ((spellLevel - 1) * this.spellPowerPerLevel);

        return (baseHealing * holyPower) * 1.2F;
    }
}