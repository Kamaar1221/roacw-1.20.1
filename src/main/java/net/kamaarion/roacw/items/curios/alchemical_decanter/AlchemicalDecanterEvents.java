package net.kamaarion.roacw.items.curios.alchemical_decanter;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.acetheeldritchking.cataclysm_spellbooks.registries.CSSchoolRegistry;
import net.kamaarion.roacw.Utils;
import net.kamaarion.roacw.entity.summon.baby_plaguebringer.BabyPlaguebringerEntity;
import net.kamaarion.roacw.entity.summon.virili.ViriliEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class AlchemicalDecanterEvents {

    private static final int EFFECT_DURATION_TICKS = 180;
    private static final int CAST_WINDOW_TICKS = 200;
    private static final String PLAGUE_SUMMON_TAG = "roacw_plague_summon";

    private static final Map<UUID, SchoolType> recentCastSchool = new HashMap<>();
    private static final Map<UUID, Long> recentCastTick = new HashMap<>();

    @SubscribeEvent
    public static void onSpellCast(SpellOnCastEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!isTechnomancyOrNature(event.getSchoolType())) {
            return;
        }

        recentCastSchool.put(player.getUUID(), event.getSchoolType());
        recentCastTick.put(player.getUUID(), player.level().getGameTime());
    }

    @SubscribeEvent
    public static void onSummonSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return;
        }

        if (!isPlagueSummonType(event.getEntity())) {
            return;
        }

        if (!(getSummoner(event.getEntity()) instanceof ServerPlayer player)) {
            return;
        }

        Long castTick = recentCastTick.get(player.getUUID());

        if (castTick == null) {
            return;
        }

        long elapsed = player.level().getGameTime() - castTick;

        if (elapsed > CAST_WINDOW_TICKS) {
            return;
        }

        event.getEntity().getPersistentData().putBoolean(PLAGUE_SUMMON_TAG, true);
    }

    public static void tagAsPlagueSummon(Entity entity) {
        entity.getPersistentData().putBoolean(PLAGUE_SUMMON_TAG, true);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity directEntity = event.getSource().getDirectEntity();
        Entity trueEntity = event.getSource().getEntity();

        if (trueEntity instanceof ServerPlayer caster) {
            if (directEntity == caster) {
                return;
            }

            UUID id = caster.getUUID();
            Long castTick = recentCastTick.get(id);

            if (castTick != null) {
                long elapsed = caster.level().getGameTime() - castTick;

                if (elapsed <= CAST_WINDOW_TICKS && isWearingDecanter(caster)) {
                    applyPlague(caster, event.getEntity());
                    return;
                }
            }
        }

        Entity summonEntity = null;

        if (isPlagueSummonType(directEntity)) {
            summonEntity = directEntity;
        } else if (isPlagueSummonType(trueEntity)) {
            summonEntity = trueEntity;
        } else if (directEntity instanceof Projectile projectile
                && isPlagueSummonType(projectile.getOwner())) {
            summonEntity = projectile.getOwner();
        } else if (directEntity instanceof OwnableEntity ownable
                && isPlagueSummonType(ownable.getOwner())) {
            summonEntity = ownable.getOwner();
        }

        if (summonEntity == null
                || !(getSummoner(summonEntity) instanceof ServerPlayer summoner)) {
            return;
        }

        if (!summonEntity.getPersistentData().getBoolean(PLAGUE_SUMMON_TAG)) {
            return;
        }

        if (isWearingDecanter(summoner)) {
            applyPlague(summoner, event.getEntity());
        }
    }

    private static boolean isPlagueSummonType(@Nullable Entity entity) {
        return entity instanceof IMagicSummon
                || entity instanceof ViriliEntity
                || entity instanceof BabyPlaguebringerEntity;
    }

    @Nullable
    private static Entity getSummoner(Entity entity) {
        if (entity instanceof IMagicSummon summon) {
            return summon.getSummoner();
        }

        if (entity instanceof ViriliEntity virili) {
            return virili.getOwner();
        }

        if (entity instanceof BabyPlaguebringerEntity baby) {
            return baby.getOwner();
        }

        return null;
    }

    private static void applyPlague(ServerPlayer caster, LivingEntity target) {
        float bonusPercent = getNatureSpellPowerBonusPercent(caster);
        int amplifier = Math.min(5, Math.round(bonusPercent / 20F));

        target.addEffect(new MobEffectInstance(
                ROACWEffectRegistry.PLAGUE.get(),
                EFFECT_DURATION_TICKS,
                amplifier,
                false,
                false
        ), caster);
    }

    private static boolean isTechnomancyOrNature(SchoolType schoolType) {
        return schoolType == SchoolRegistry.NATURE.get()
                || schoolType == CSSchoolRegistry.TECHNOMANCY.get();
    }

    private static boolean isWearingDecanter(Player caster) {
        return Utils.hasCurio(
                caster,
                ROACWItemRegistry.ALCHEMICAL_DECANTER.get()
        );
    }

    private static float getNatureSpellPowerBonusPercent(LivingEntity caster) {
        var attribute = caster.getAttribute(AttributeRegistry.NATURE_SPELL_POWER.get());
        float multiplier = attribute != null ? (float) attribute.getValue() : 1.0F;

        return Math.max(0F, (multiplier - 1.0F) * 100F);
    }
}