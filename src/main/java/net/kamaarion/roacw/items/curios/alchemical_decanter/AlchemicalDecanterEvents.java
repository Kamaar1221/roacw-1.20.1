package net.kamaarion.roacw.items.curios.alchemical_decanter;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.acetheeldritchking.cataclysm_spellbooks.registries.CSSchoolRegistry;
import net.kamaarion.roacw.Utils;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class AlchemicalDecanterEvents {

    private static final int EFFECT_DURATION_TICKS = 180; // ~9s
    private static final int CAST_WINDOW_TICKS = 200; // 10s — covers slower Technomancy projectiles
    private static final String PLAGUE_SUMMON_TAG = "roacw_plague_summon";

    private static final Map<UUID, SchoolType> recentCastSchool = new HashMap<>();
    private static final Map<UUID, Long> recentCastTick = new HashMap<>();

    @SubscribeEvent
    public static void onSpellCast(SpellOnCastEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isTechnomancyOrNature(event.getSchoolType())) return;

        recentCastSchool.put(player.getUUID(), event.getSchoolType());
        recentCastTick.put(player.getUUID(), player.level().getGameTime());
    }

    // Tags a freshly-spawned summon if it belongs to a player who just cast a
    // qualifying spell — lets us later confirm THIS summon came from Technomancy/Nature,
    // since IMagicSummon itself carries no school info.
    @SubscribeEvent
    public static void onSummonSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof IMagicSummon summon)) return;
        if (!(summon.getSummoner() instanceof ServerPlayer player)) return;

        Long castTick = recentCastTick.get(player.getUUID());
        if (castTick == null) return;

        long elapsed = player.level().getGameTime() - castTick;
        if (elapsed > CAST_WINDOW_TICKS) return;

        event.getEntity().getPersistentData().putBoolean(PLAGUE_SUMMON_TAG, true);
    }

    /**
     * Tags a summon as Plague-eligible directly, bypassing the
     * recent-cast-window correlation in onSummonSpawn() above entirely.
     * That correlation depends on SpellOnCastEvent having already fired
     * for the player before the summon's EntityJoinLevelEvent does - an
     * assumption that's fragile to rely on (e.g. if a spell calls
     * super.onCast() only after already spawning its summons, the window
     * check silently fails every time). A Nature/Technomancy summon spell
     * already knows its own school by construction, so call this directly
     * right after spawning instead of relying on that timing to line up.
     */
    public static void tagAsPlagueSummon(Entity entity) {
        entity.getPersistentData().putBoolean(PLAGUE_SUMMON_TAG, true);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity directEntity = event.getSource().getDirectEntity();
        Entity trueEntity = event.getSource().getEntity();

        // --- Path 1: caster directly cast a spell that hit ---
        if (trueEntity instanceof ServerPlayer caster) {
            if (directEntity == caster) return; // plain melee, not a spell hit — don't proc

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

        // --- Path 2: a tagged Technomancy/Nature summon owned by the caster hit the target ---
        IMagicSummon summon = null;
        if (directEntity instanceof IMagicSummon directSummon) {
            summon = directSummon;
        } else if (trueEntity instanceof IMagicSummon trueSummon) {
            summon = trueSummon;
        } else if (directEntity instanceof Projectile projectile && projectile.getOwner() instanceof IMagicSummon projSummon) {
            summon = projSummon;
        } else if (directEntity instanceof OwnableEntity ownable && ownable.getOwner() instanceof IMagicSummon ownedSummon) {
            summon = ownedSummon;
        }

        if (summon != null && summon.getSummoner() instanceof ServerPlayer summoner) {
            Entity summonEntity = (Entity) summon;
            if (!summonEntity.getPersistentData().getBoolean(PLAGUE_SUMMON_TAG)) return; // not a qualifying summon

            if (isWearingDecanter(summoner)) {
                applyPlague(summoner, event.getEntity());
            }
        }
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
        ));
    }

    private static boolean isTechnomancyOrNature(SchoolType schoolType) {
        if (schoolType == SchoolRegistry.NATURE.get()) return true;
        if (schoolType == CSSchoolRegistry.TECHNOMANCY.get()) return true;
        return false;
    }

    private static boolean isWearingDecanter(Player caster) {
        return Utils.hasCurio(caster, ROACWItemRegistry.ALCHEMICAL_DECANTER.get());
    }

    private static float getNatureSpellPowerBonusPercent(LivingEntity caster) {
        var attr = caster.getAttribute(AttributeRegistry.NATURE_SPELL_POWER.get());
        float multiplier = attr != null ? (float) attr.getValue() : 1.0F;
        return Math.max(0F, (multiplier - 1.0F) * 100F);
    }
}