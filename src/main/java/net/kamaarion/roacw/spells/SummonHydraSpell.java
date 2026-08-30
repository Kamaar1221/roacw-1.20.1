package net.kamaarion.roacw.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.*;
import net.acetheeldritchking.cataclysm_spellbooks.registries.CSSchoolRegistry;
import net.acetheeldritchking.cataclysm_spellbooks.spells.CSSpellAnimations;
import net.kamaarion.roacw.entity.summon.hydra.HydraBodyEntity;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SummonHydraSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath("roacw", "summon_hydra");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(CSSchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(240)
            .build();

    public SummonHydraSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 25;
        this.baseManaCost = 70;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.summon_count", 1)
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

            int summonCount = 1;

            for (int i = 0; i < summonCount; i++) {
                HydraBodyEntity hydra = new HydraBodyEntity(ROACWEntityRegistry.HYDRA_BODY.get(), serverLevel);
                hydra.setInitialHeadCount(spellLevel);
                hydra.moveTo(entity.getX() + 2, entity.getY(), entity.getZ() + 2, entity.yBodyRot, 0);
                serverLevel.addFreshEntity(hydra);

                SummonManager.initSummon(
                        entity,
                        hydra,
                        summonTime,
                        summonedEntitiesCastData
                );
            }

            RecastInstance recastInstance = new RecastInstance(this.getSpellId(), spellLevel, getRecastCount(spellLevel, entity), summonTime, castSource, summonedEntitiesCastData);
            recasts.addRecast(recastInstance, playerMagicData);
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}
