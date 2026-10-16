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
import net.kamaarion.roacw.entity.summon.virili.ViriliEntity;
import net.kamaarion.roacw.items.curios.alchemical_decanter.AlchemicalDecanterEvents;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SummonViriliSpell extends AbstractSpell {

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(
            "roacw",
            "summon_virili"
    );

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(CSSchoolRegistry.TECHNOMANCY_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(240)
            .setAllowCrafting(false)
            .build();

    private static final float VIRILI_BASE_ATTACK_DAMAGE = 30.0F;
    private static final float VIRILI_BASE_SPELL_POWER = 4.0F;
    private static final float VIRILI_BASE_HEALTH = 100.0F;
    private static final float VIRILI_HEALTH_SCALING_PER_POWER = 0.10F;
    private static final float VIRILI_MISSILE_MULTIPLIER = 2.0F;
    private static final float BABY_PLAGUEBRINGER_NORMAL_MULTIPLIER = 0.625F;
    private static final float BABY_PLAGUEBRINGER_FINAL_MULTIPLIER = 1.875F;
    private static final float PLAGUE_BEE_MULTIPLIER = 0.75F;

    public SummonViriliSpell() {
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
        List<MutableComponent> info = new ArrayList<>();

        info.add(
                Component.translatable(
                        "ui.irons_spellbooks.hp",
                        Utils.stringTruncation(
                                getViriliHealth(spellLevel, caster),
                                1
                        )
                )
        );

        info.add(
                Component.translatable(
                        "ui.irons_spellbooks.damage",
                        Utils.stringTruncation(
                                getViriliAttackDamage(spellLevel, caster),
                                1
                        )
                )
        );

        info.add(
                Component.translatable(
                        "ui.roacw.virili_homing_missile_damage",
                        Utils.stringTruncation(
                                getHomingMissileDamage(spellLevel, caster),
                                1
                        )
                )
        );

        info.add(
                Component.translatable(
                        "ui.roacw.virili_baby_plaguebringer_health",
                        Utils.stringTruncation(
                                getBabyPlaguebringerHealth(),
                                1
                        )
                )
        );

        info.add(
                Component.translatable(
                        "ui.roacw.virili_baby_plaguebringer_charge_damage",
                        Utils.stringTruncation(
                                getBabyPlaguebringerChargeDamage(
                                        spellLevel,
                                        caster
                                ),
                                1
                        )
                )
        );

        info.add(
                Component.translatable(
                        "ui.roacw.virili_baby_plaguebringer_final_charge_damage",
                        Utils.stringTruncation(
                                getBabyPlaguebringerFinalChargeDamage(
                                        spellLevel,
                                        caster
                                ),
                                1
                        )
                )
        );

        info.add(
                Component.translatable(
                        "ui.roacw.virili_plague_bee_damage",
                        Utils.stringTruncation(
                                getPlagueBeeDamage(
                                        spellLevel,
                                        caster
                                ),
                                1
                        )
                )
        );

        return info;
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
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(ROACWSoundRegistry.PLAGUE_ENTITY_SPAWN.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
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

            removeExistingVirili(serverLevel, entity.getUUID());

            SummonedEntitiesCastData summonedEntitiesCastData =
                    new SummonedEntitiesCastData();

            int summonTime = 20 * 60 * 10;

            ViriliEntity virili = new ViriliEntity(
                    ROACWEntityRegistry.VIRILI.get(),
                    serverLevel
            );

            virili.moveTo(
                    entity.getX(),
                    entity.getY() + 2.0D,
                    entity.getZ(),
                    entity.getYRot(),
                    0.0F
            );

            virili.finalizeSpawn(
                    serverLevel,
                    world.getCurrentDifficultyAt(virili.blockPosition()),
                    MobSpawnType.MOB_SUMMONED,
                    null,
                    null
            );

            float damage = getViriliDamage(spellLevel, entity);
            float viriliAttackDamage = getViriliAttackDamage(spellLevel, entity);

            virili.getAttribute(Attributes.MAX_HEALTH).setBaseValue(
                    getViriliHealth(spellLevel, entity)
            );

            virili.setHealth(virili.getMaxHealth());

            virili.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(
                    viriliAttackDamage
            );

            virili.setOwnerUUID(entity.getUUID());
            virili.setSpellPower(damage);

            serverLevel.addFreshEntity(virili);

            AlchemicalDecanterEvents.tagAsPlagueSummon(virili);

            serverLevel.sendParticles(
                    ROACWParticleRegistry.PLAGUE_CLOUD.get(),
                    virili.getX(),
                    virili.getY() + 0.5D,
                    virili.getZ(),
                    20,
                    0.3D,
                    0.4D,
                    0.3D,
                    0.02D
            );

            SummonManager.initSummon(
                    entity,
                    virili,
                    summonTime,
                    summonedEntitiesCastData
            );

            RecastInstance recastInstance = new RecastInstance(
                    this.getSpellId(),
                    spellLevel,
                    getRecastCount(spellLevel, entity),
                    summonTime,
                    castSource,
                    summonedEntitiesCastData
            );

            recasts.addRecast(recastInstance, playerMagicData);
        }

        super.onCast(
                world,
                spellLevel,
                entity,
                castSource,
                playerMagicData
        );
    }

    private void removeExistingVirili(
            ServerLevel currentLevel,
            UUID ownerUUID
    ) {
        List<ViriliEntity> existingVirili = new ArrayList<>();

        for (ServerLevel level : currentLevel.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof ViriliEntity virili)) {
                    continue;
                }

                if (virili.isRemoved()) {
                    continue;
                }

                UUID viriliOwner = virili.getOwnerUUID();

                if (ownerUUID.equals(viriliOwner)) {
                    existingVirili.add(virili);
                }
            }
        }

        for (ViriliEntity virili : existingVirili) {
            virili.discard();
        }
    }

    private float getViriliHealth(
            int spellLevel,
            @Nullable LivingEntity caster
    ) {
        float spellPower = getViriliDamage(spellLevel, caster);
        float additionalPower = Math.max(
                0.0F,
                spellPower - VIRILI_BASE_SPELL_POWER
        );

        float healthMultiplier = 1.0F
                + additionalPower * VIRILI_HEALTH_SCALING_PER_POWER;

        return VIRILI_BASE_HEALTH * healthMultiplier;
    }

    private float getViriliDamage(
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

    private float getViriliAttackDamage(
            int spellLevel,
            @Nullable LivingEntity caster
    ) {
        float spellPower = getViriliDamage(spellLevel, caster);
        float scalingMultiplier = spellPower / VIRILI_BASE_SPELL_POWER;

        return VIRILI_BASE_ATTACK_DAMAGE * scalingMultiplier;
    }

    private float getBabyPlaguebringerHealth() {
        return 20.0F;
    }

    private float getBabyPlaguebringerChargeDamage(
            int spellLevel,
            @Nullable LivingEntity caster
    ) {
        return getViriliDamage(
                spellLevel,
                caster
        ) * BABY_PLAGUEBRINGER_NORMAL_MULTIPLIER;
    }

    private float getBabyPlaguebringerFinalChargeDamage(
            int spellLevel,
            @Nullable LivingEntity caster
    ) {
        return getViriliDamage(
                spellLevel,
                caster
        ) * BABY_PLAGUEBRINGER_FINAL_MULTIPLIER;
    }

    private float getPlagueBeeDamage(
            int spellLevel,
            @Nullable LivingEntity caster
    ) {
        return getViriliDamage(
                spellLevel,
                caster
        ) * PLAGUE_BEE_MULTIPLIER;
    }

    private float getHomingMissileDamage(
            int spellLevel,
            @Nullable LivingEntity caster
    ) {
        return getViriliDamage(
                spellLevel,
                caster
        ) * VIRILI_MISSILE_MULTIPLIER;
    }
}