package net.kamaarion.roacw.effects.effect_extras;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.kamaarion.roacw.entity.mob.plague_charger.PlagueChargerEntity;
import net.kamaarion.roacw.entity.summon.baby_plaguebringer.BabyPlaguebringerEntity;
import net.kamaarion.roacw.entity.summon.mini_plaguebringer.MiniPlaguebringerEntity;
import net.kamaarion.roacw.entity.summon.virili.ViriliEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class PlagueEvents {

    private static final double SPREAD_RADIUS = 5.0D;

    private static final Map<UUID, UUID> PLAGUE_SOURCES = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        MobEffectInstance added = event.getEffectInstance();
        if (added == null || added.getEffect() != ROACWEffectRegistry.PLAGUE.get()) return;

        LivingEntity sourceOwner = resolveSourceOwner(event.getEffectSource());
        if (sourceOwner != null && sourceOwner != entity) {
            PLAGUE_SOURCES.put(entity.getUUID(), sourceOwner.getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance expired = event.getEffectInstance();
        if (expired != null && expired.getEffect() == ROACWEffectRegistry.PLAGUE.get()) {
            PLAGUE_SOURCES.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == ROACWEffectRegistry.PLAGUE.get()) {
            PLAGUE_SOURCES.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide) {
            PLAGUE_SOURCES.remove(living.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        if (!victim.hasEffect(ROACWEffectRegistry.PLAGUE.get())) return;

        LivingEntity sourceOwner = resolveSourceOwner(event.getSource().getEntity());
        if (sourceOwner != null && sourceOwner != victim) {
            PLAGUE_SOURCES.putIfAbsent(victim.getUUID(), sourceOwner.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dying = event.getEntity();
        Level level = dying.level();
        if (level.isClientSide) return;

        UUID trackedSource = PLAGUE_SOURCES.remove(dying.getUUID());

        MobEffectInstance plague = dying.getEffect(ROACWEffectRegistry.PLAGUE.get());
        if (plague == null) return;

        AABB spreadArea = dying.getBoundingBox().inflate(SPREAD_RADIUS);

        Entity attacker = event.getSource().getEntity();

        LivingEntity sourceOwner = resolveSourceOwner(attacker);

        if (sourceOwner == null && trackedSource != null && level instanceof ServerLevel serverLevel) {
            sourceOwner = serverLevel.getEntity(trackedSource) instanceof LivingEntity tracked
                    ? tracked
                    : null;
        }

        if (sourceOwner == null) {
            sourceOwner = resolveSourceOwner(dying.getLastHurtByMob());
        }

        final LivingEntity finalSourceOwner = sourceOwner;

        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, spreadArea,
                e -> e != dying && e != attacker && e.isAlive()
                        && !(e instanceof Player)
                        && !(e instanceof PlagueChargerEntity)
                        && !isAlliedSummon(e, finalSourceOwner));

        for (LivingEntity target : nearby) {
            target.addEffect(new MobEffectInstance(
                    ROACWEffectRegistry.PLAGUE.get(),
                    plague.getDuration(),
                    plague.getAmplifier(),
                    false,
                    false
            ));

            if (finalSourceOwner != null) {
                PLAGUE_SOURCES.put(target.getUUID(), finalSourceOwner.getUUID());
            }
        }
    }

    @Nullable
    private static LivingEntity resolveSourceOwner(@Nullable Entity attacker) {
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return null;
        }

        if (isSummon(livingAttacker)) {
            LivingEntity owner = getRootOwner(livingAttacker);
            return owner != null ? owner : livingAttacker;
        }

        return livingAttacker;
    }

    private static boolean isSummon(Entity entity) {
        return entity instanceof IMagicSummon
                || entity instanceof MiniPlaguebringerEntity
                || entity instanceof BabyPlaguebringerEntity
                || entity instanceof ViriliEntity;
    }

    @Nullable
    private static LivingEntity getSummonOwner(Entity entity) {
        if (entity instanceof MiniPlaguebringerEntity mini) {
            return mini.getSummonOwner();
        }

        if (entity instanceof ViriliEntity virili) {
            return virili.getOwner();
        }

        if (entity instanceof BabyPlaguebringerEntity baby) {
            return baby.getOwner();
        }

        if (entity instanceof IMagicSummon) {
            Entity owner = SummonManager.getOwner(entity);

            if (owner instanceof LivingEntity living) {
                return living;
            }
        }

        return null;
    }

    @Nullable
    private static LivingEntity getRootOwner(Entity entity) {
        LivingEntity current = getSummonOwner(entity);

        for (int depth = 0; depth < 4 && current != null && isSummon(current); depth++) {
            LivingEntity next = getSummonOwner(current);

            if (next == null) {
                break;
            }

            current = next;
        }

        return current;
    }

    private static boolean isAlliedSummon(LivingEntity candidate, @Nullable LivingEntity sourceOwner) {
        if (!isSummon(candidate)) {
            return false;
        }

        LivingEntity candidateOwner = getRootOwner(candidate);

        if (candidateOwner == null || candidateOwner instanceof Player) {
            return true;
        }

        if (sourceOwner == null) {
            return false;
        }

        if (candidateOwner.getUUID().equals(sourceOwner.getUUID())) {
            return true;
        }

        return candidateOwner.isAlliedTo(sourceOwner)
                || sourceOwner.isAlliedTo(candidateOwner)
                || candidate.isAlliedTo(sourceOwner)
                || sourceOwner.isAlliedTo(candidate);
    }
}