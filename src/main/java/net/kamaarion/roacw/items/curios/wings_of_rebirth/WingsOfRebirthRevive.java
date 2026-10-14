package net.kamaarion.roacw.items.curios.wings_of_rebirth;

import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ROACW.MODID)
public final class WingsOfRebirthRevive {
    private static final int REVIVE_PROTECTION_TICKS = 60;
    private static final int REVIVE_AURA_TICKS = 60;
    private static final double REVIVE_AURA_RADIUS = 0.9D;
    private static final Supplier<? extends ParticleOptions> REVIVE_PARTICLE = () -> ParticleRegistry.DRAGON_FIRE_PARTICLE.get();
    private static final String REVIVE_AURA_TAG = "roacw_wing_revive_aura";

    private WingsOfRebirthRevive() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        ItemStack wings = findChargedWings(player);
        if (wings.isEmpty()) return;

        WingsOfRebirth.setCharged(wings, false);
        event.setCanceled(true);

        player.setHealth(player.getMaxHealth());
        player.clearFire();
        player.fallDistance = 0.0F;
        player.getActiveEffects().stream()
                .filter(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                .map(MobEffectInstance::getEffect)
                .toList()
                .forEach(player::removeEffect);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, REVIVE_PROTECTION_TICKS, 4));

        player.level().playSound(null, player.blockPosition(), ROACWSoundRegistry.PHEONIX_REBIRTH.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        spawnReviveBurst(player);
        player.getPersistentData().putInt(REVIVE_AURA_TAG, REVIVE_AURA_TICKS);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;

        CompoundTag data = player.getPersistentData();
        int aura = data.getInt(REVIVE_AURA_TAG);
        if (aura <= 0) return;

        data.putInt(REVIVE_AURA_TAG, aura - 1);
        spawnReviveAura(player, aura);
    }

    private static void spawnReviveBurst(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int count = 40;
        for (int i = 0; i < count; i++) {
            double angle = i * (Math.PI * 2.0D / count);
            level.sendParticles(REVIVE_PARTICLE.get(), player.getX(), player.getY() + 0.9D, player.getZ(), 0,
                    Math.cos(angle), 0.2D, Math.sin(angle), 0.3D);
        }
    }

    private static void spawnReviveAura(ServerPlayer player, int ticksLeft) {
        ServerLevel level = player.serverLevel();
        for (int i = 0; i < 3; i++) {
            double angle = ticksLeft * 0.5D + i * (Math.PI * 2.0D / 3.0D);
            double x = player.getX() + Math.cos(angle) * REVIVE_AURA_RADIUS;
            double z = player.getZ() + Math.sin(angle) * REVIVE_AURA_RADIUS;
            double y = player.getY() + 0.1D + ((ticksLeft * 0.05D + i * 0.6D) % 1.8D);
            level.sendParticles(REVIVE_PARTICLE.get(), x, y, z, 0, 0.0D, 1.0D, 0.0D, 0.04D);
        }
    }

    private static ItemStack findChargedWings(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.findFirstCurio(WingsOfRebirth::isCharged))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }
}