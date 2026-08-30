package net.kamaarion.roacw.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.*;
import net.kamaarion.roacw.entity.summon.belladonna_spirit.BelladonnaSpiritEntity;
import net.kamaarion.roacw.items.curios.alchemical_decanter.AlchemicalDecanterEvents;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SummonBelladonnaSpiritSpell extends AbstractSpell {

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath("roacw", "summon_belladonna_spirit");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)               // TODO: confirm against your other Nature spells
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(5)                                // matches polar bear's max level; tune if you want fewer tiers
            .setCooldownSeconds(180)                        // TODO: tune - matches polar bear as a starting point
            .build();

    public SummonBelladonnaSpiritSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.summon_count", getSummonCountForLevel(spellLevel)),
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpiritDamage(spellLevel, caster), 1))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG; // TODO: switch to INSTANT if you'd rather this fire immediately, like PestilenceCloakSpell
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
        return Optional.of(SoundEvents.EVOKER_PREPARE_SUMMON); // TODO: swap for something more nature-flavored if you have one
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2; // TODO: verify what this controls exactly (recast slots for dismiss?) against SummonManager source
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
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
            int summonTime = 20 * 60 * 10; // 3 minutes; TODO tune, matches your original 60s-per-tier intent scaled up

            int summonCount = getSummonCountForLevel(spellLevel);
            float damage = getSpiritDamage(spellLevel, entity);

            for (int i = 0; i < summonCount; i++) {
                BelladonnaSpiritEntity spirit = new BelladonnaSpiritEntity(
                        ROACWEntityRegistry.BELLADONNA_SPIRIT.get(),
                        serverLevel
                );

                // Evenly space spirits around the player
                double angle = (Math.PI * 2.0D / summonCount) * i;

                double radius = 2.0D;

                double spawnX = entity.getX() + Math.cos(angle) * radius;
                double spawnY = entity.getEyeY() + 0.5D;
                double spawnZ = entity.getZ() + Math.sin(angle) * radius;

                spirit.moveTo(
                        spawnX,
                        spawnY,
                        spawnZ,
                        entity.getYRot(),
                        0.0F
                );

                spirit.finalizeSpawn(
                        serverLevel,
                        world.getCurrentDifficultyAt(spirit.blockPosition()),
                        MobSpawnType.MOB_SUMMONED,
                        null,
                        null
                );

                spirit.setSpellPower(damage);

                serverLevel.addFreshEntity(spirit);
                AlchemicalDecanterEvents.tagAsPlagueSummon(spirit);

                SummonManager.initSummon(
                        entity,
                        spirit,
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
        return 1 + Math.max(0, spellLevel - 1);
    }

    private float getSpiritDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }
}