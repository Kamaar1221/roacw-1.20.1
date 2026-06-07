package net.kamaarion.roacw.events;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DashInvincibilityHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Item dashItem = ROACWItemRegistry.EVASION_SCARF.get();

            if (player.getCooldowns().isOnCooldown(dashItem)) {
                float remainingTicks = player.getCooldowns().getCooldownPercent(dashItem, 0.0F) * 200;

                if (remainingTicks > 190.0F) {
                    event.setCanceled(true);

                    // Spawn a subtle deflection particle to show they dodged it
                    if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                                player.getX(), player.getY() + 1.0, player.getZ(),
                                3, 0.1, 0.1, 0.1, 0.05
                        );
                    }
                }
            }
        }
    }
}
