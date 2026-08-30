package net.kamaarion.roacw.events;

import net.kamaarion.roacw.entity.summon.plague_charger.PlagueChargerEntity;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class PlagueEvents {

    private static final double SPREAD_RADIUS = 5.0D;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dying = event.getEntity();
        Level level = dying.level();
        if (level.isClientSide) return;

        MobEffectInstance plague = dying.getEffect(ROACWEffectRegistry.PLAGUE.get());
        if (plague == null) return;

        AABB spreadArea = dying.getBoundingBox().inflate(SPREAD_RADIUS);

        // getSource().getEntity() resolves to the credited attacker even
        // for indirect/thrown damage (e.g. a stinger projectile whose
        // owner is the charger that fired it) - without this, a charger
        // standing next to its own kill (which it almost always is, being
        // melee/short-range) falls straight into the spread radius and
        // re-infects itself with the thing it just killed.
        Entity attacker = event.getSource().getEntity();

        // Plague Chargers are plague creatures themselves - immune to the
        // spread outright, not just protected from re-infecting themselves
        // on their own kill. Other nearby chargers (not just the attacker)
        // were still catching it before this.
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, spreadArea,
                e -> e != dying && e != attacker && e.isAlive()
                        && !(e instanceof Player)
                        && !(e instanceof PlagueChargerEntity));

        for (LivingEntity target : nearby) {
            target.addEffect(new MobEffectInstance(
                    ROACWEffectRegistry.PLAGUE.get(),
                    plague.getDuration(),
                    plague.getAmplifier(),
                    false,
                    false
            ));
        }
    }
}