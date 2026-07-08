package net.kamaarion.roacw.spells.geo;

import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import com.gametechbc.gtbcs_geomancy_plus.api.init.GGSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import net.kamaarion.roacw.registeries.ROACWAnimationRegistry;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.entity.spells.impaling_column.ImpalingColumnShards;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class ImpalingColumnSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath("roacw", "impaling_column");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(GGSchools.GEO_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(30.0)
            .build();

    public ImpalingColumnSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 5;
        this.castTime = 15;
        this.baseManaCost = 40;
    }

    @Override
    public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.OVERHEAD_MELEE_SWING_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() { return AnimationHolder.pass(); }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData magicData) {
        if (world.isClientSide || !(world instanceof ServerLevel serverLevel)) return;
        if (entity == null) return;

        Vec3 lookAngle = entity.getLookAngle().normalize();

        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), MMSounds.EFFECT_GEOMANCY_RUMBLE_1.get(), SoundSource.PLAYERS, 2.0F, 0.6F);
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.8F);

        int shakeDuration = 15 + (spellLevel * 2);
        float shakeRadius = 15.0F + spellLevel;
        CameraShakeManager.addCameraShake(
                new CameraShakeData(world, shakeDuration, entity.position(), shakeRadius)
        );

        Vec3 casterGround = Utils.moveToRelativeGroundLevel(world, entity.position(), 64);

        int spikeCount = 5 + spellLevel;
        float spacing = 2.5f;
        float startOffset = 2.0f;

        Vec3 perp = lookAngle.cross(new Vec3(0, 1, 0)).normalize();

        for (int i = 0; i < spikeCount; i++) {
            float distanceFromPlayer = startOffset + (i * spacing);

            double lateralOffset = (entity.getRandom().nextDouble() - 0.5) * 1.2;

            Vec3 spawnPos = casterGround
                    .add(lookAngle.x * distanceFromPlayer, 0, lookAngle.z * distanceFromPlayer)
                    .add(perp.scale(lateralOffset));

            spawnPos = Utils.moveToRelativeGroundLevel(world, spawnPos, 10);

            if (!world.getBlockState(BlockPos.containing(spawnPos).below()).isAir()) {
                ImpalingColumnShards shard = ROACWEntityRegistry.IMPALING_COLUMN_SHARDS.get().create(serverLevel);
                if (shard != null) {
                    float yawVariation = entity.getYRot() + entity.getRandom().nextIntBetweenInclusive(-20, 20);
                    shard.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, yawVariation, -35.0F);
                    shard.setOwner(entity);
                    shard.setDamage(getDamage(spellLevel, entity));

                    int delay = (i * 3) + entity.getRandom().nextIntBetweenInclusive(0, 3);
                    shard.setSpawnDelay(delay);

                    float scale = 1.0f + ((float) i / (spikeCount - 1)) * 1.3f;
                    shard.setScale(scale);

                    shard.setInvisible(true);
                    serverLevel.addFreshEntity(shard);
                }
            }
        }

        super.onCast(world, spellLevel, entity, castSource, magicData);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        if (entity == null) {
            return this.baseSpellPower + ((spellLevel - 1) * this.spellPowerPerLevel);
        }

        float baseDamage = getSpellPower(spellLevel, entity);
        float holyPower = (float) SchoolRegistry.HOLY.get().getPowerFor(entity);
        float holyBonus = 1.0F + ((holyPower - 1.0F) * 0.5F);

        return baseDamage * holyBonus;
    }
}