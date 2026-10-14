package net.kamaarion.roacw.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import net.kamaarion.roacw.entity.summon.endo_hydra.EndoHydraEntity;
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
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SummonEndoHydraSpell extends AbstractSpell {

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(
            "roacw",
            "summon_endo_hydra"
    );

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(2)
            .setCooldownSeconds(180)
            .build();

    private static final float ELDRITCH_SPELL_POWER_SCALING = 0.5F;
    private static final float BEAM_BASE_DAMAGE = 15.0F;
    private static final float BEAM_DAMAGE_PER_LEVEL = 5.0F;

    public SummonEndoHydraSpell() {
        this.manaCostPerLevel = 50;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 200;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable(
                        "ui.roacw.endo_hydra_heads",
                        getHeadCountForLevel(spellLevel)
                ),
                Component.translatable(
                        "ui.irons_spellbooks.damage",
                        Utils.stringTruncation(
                                getHydraBeamDamage(spellLevel, caster),
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
        return Optional.of(ROACWSoundRegistry.ENDO_HYDRA_SUMMON.get());
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
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

            SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
            int summonTime = 20 * 60 * 10;

            float hydraBeamDamage = getHydraBeamDamage(spellLevel, entity);

            EndoHydraEntity hydra = new EndoHydraEntity(
                    ROACWEntityRegistry.ENDO_HYDRA.get(),
                    serverLevel
            );

            hydra.setSpellLevel(spellLevel);
            hydra.setSpellPower(hydraBeamDamage);

            hydra.moveTo(
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    entity.getYRot(),
                    0.0F
            );

            hydra.finalizeSpawn(
                    serverLevel,
                    world.getCurrentDifficultyAt(hydra.blockPosition()),
                    MobSpawnType.MOB_SUMMONED,
                    null,
                    null
            );

            serverLevel.addFreshEntity(hydra);

            SummonManager.initSummon(
                    entity,
                    hydra,
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

    private float getHydraBeamDamage(int spellLevel, @Nullable LivingEntity caster) {
        float baseDamage = BEAM_BASE_DAMAGE
                + ((spellLevel - 1) * BEAM_DAMAGE_PER_LEVEL);

        if (caster == null) {
            return baseDamage;
        }

        float icePower = (float) getSchoolType().getPowerFor(caster);
        float eldritchPower = (float) SchoolRegistry.ELDRITCH.get().getPowerFor(caster);

        float eldritchBonus = 1.0F
                + ((eldritchPower - 1.0F) * ELDRITCH_SPELL_POWER_SCALING);

        return baseDamage * icePower * eldritchBonus;
    }

    private int getHeadCountForLevel(int spellLevel) {
        if (spellLevel <= 1) {
            return 1;
        }

        if (spellLevel == 2) {
            return 3;
        }

        return 5;
    }
}