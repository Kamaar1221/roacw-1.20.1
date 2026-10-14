package net.kamaarion.roacw.client.firevisuals;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.network.ModMessages;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FireEffectSyncHandler {
    private static final Map<Integer, Set<ResourceLocation>> LAST_SYNCED = new HashMap<>();

    private FireEffectSyncHandler() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        Set<ResourceLocation> current = new HashSet<>();

        for (MobEffectInstance instance : entity.getActiveEffects()) {
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(instance.getEffect());

            if (id != null) {
                current.add(id);
            }
        }

        Set<ResourceLocation> previous = LAST_SYNCED.getOrDefault(
                entity.getId(),
                Collections.emptySet()
        );

        if (current.equals(previous)) {
            return;
        }

        for (ResourceLocation id : current) {
            if (!previous.contains(id)) {
                ModMessages.sendToTracking(
                        new SyncMobEffectPacket(entity.getId(), id, true),
                        entity
                );
            }
        }

        for (ResourceLocation id : previous) {
            if (!current.contains(id)) {
                ModMessages.sendToTracking(
                        new SyncMobEffectPacket(entity.getId(), id, false),
                        entity
                );
            }
        }

        if (current.isEmpty()) {
            LAST_SYNCED.remove(entity.getId());
        } else {
            LAST_SYNCED.put(entity.getId(), current);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof LivingEntity living)) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        for (MobEffectInstance instance : living.getActiveEffects()) {
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(instance.getEffect());

            if (id != null) {
                ModMessages.sendToPlayer(
                        new SyncMobEffectPacket(living.getId(), id, true),
                        player
                );
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            LAST_SYNCED.remove(event.getEntity().getId());
        }
    }
}