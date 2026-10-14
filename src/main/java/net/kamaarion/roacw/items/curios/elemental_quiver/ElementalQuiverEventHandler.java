package net.kamaarion.roacw.items.curios.elemental_quiver;

import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = ROACW.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class ElementalQuiverEventHandler {

    private static final int ELEMENTAL_MIX_DURATION = 100;
    private static final int ELEMENTAL_MIX_AMPLIFIER = 0;

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();

        if (projectile.level().isClientSide) {
            return;
        }

        if (projectile instanceof AbstractMagicProjectile) {
            return;
        }

        Entity owner = projectile.getOwner();

        if (!(owner instanceof Player player)) {
            return;
        }

        if (!ElementalQuiver.isEquipped(player)) {
            return;
        }

        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)) {
            return;
        }

        Entity hitEntity = entityHitResult.getEntity();

        if (!(hitEntity instanceof LivingEntity livingEntity)) {
            return;
        }

        if (hitEntity == player) {
            return;
        }

        livingEntity.addEffect(new MobEffectInstance(
                ROACWEffectRegistry.ELEMENTAL_MIX.get(),
                ELEMENTAL_MIX_DURATION,
                ELEMENTAL_MIX_AMPLIFIER,
                false,
                true,
                true
        ));
    }
}