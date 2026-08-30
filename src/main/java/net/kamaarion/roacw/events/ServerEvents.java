package net.kamaarion.roacw.events;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.kamaarion.roacw.Utils;
import net.kamaarion.roacw.items.curios.high_ruler_shield.HighRulerShield;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class ServerEvents {

    @SubscribeEvent
    public void DeathEvent(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (tryAuricTeslaRevive(event.getEntity())) {
                event.setCanceled(true);
            }
        }
    }

    private boolean tryAuricTeslaRevive(LivingEntity living) {
        if (!net.kamaarion.roacw.Config.ENABLE_AURIC_REVIVE.get()) {
            return false;
        }

        ItemStack chestplate = living.getItemBySlot(EquipmentSlot.CHEST);
        if ((living.level() instanceof ServerLevel serverLevel) &&
                chestplate.getItem() == ROACWItemRegistry.AURIC_TESLA_CUIRASS.get() &&
                !living.hasEffect(ROACWEffectRegistry.AURIC_EXHAUSTION.get())) {

            living.setHealth(10.0F);
            serverLevel.playSound(
                    null, living.getX(), living.getY(), living.getZ(),
                    ROACWSoundRegistry.AURIC_REVIVE_EXPLOSION.get(), living.getSoundSource(), 2.00f, 1.0F
            );

            living.addEffect(new MobEffectInstance(ROACWEffectRegistry.AURIC_CHARGE.get(), 300, 0, false, false, true));
            return true;
        }
        return false;
    }

    @Mod.EventBusSubscriber(modid = "roacw", bus = Mod.EventBusSubscriber.Bus.FORGE)
    public class AuricReviveCooldownHandler {
        @SubscribeEvent
        public static void onEffectExpiry(MobEffectEvent.Expired event) {
            handleChargeRemoval(event.getEntity(), event.getEffectInstance());
        }

        @SubscribeEvent
        public static void onEffectRemove(MobEffectEvent.Remove event) {
            handleChargeRemoval(event.getEntity(), event.getEffectInstance());
        }

        private static void handleChargeRemoval(LivingEntity entity, @Nullable MobEffectInstance instance) {
            if (instance == null || entity.level().isClientSide()) return;
            if (instance.getEffect() == ROACWEffectRegistry.AURIC_CHARGE.get()) {
                entity.addEffect(new MobEffectInstance(ROACWEffectRegistry.AURIC_EXHAUSTION.get(), 6000, 0, false, false, true));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        // --- HighRulerShield: 5% passive block chance when equipped as a curio ---
        if (event.getEntity() instanceof Player player) {
            // Only proc if not already actively blocking it in offhand
            ItemStack offhand = player.getOffhandItem();
            boolean blockingInHand = offhand.getItem() instanceof HighRulerShield && player.isUsingItem();

            if (!blockingInHand) {
                CuriosApi.getCuriosHelper()
                        .findFirstCurio(player, s -> s.getItem() instanceof HighRulerShield)
                        .ifPresent(result -> {
                            if (player.getRandom().nextFloat() < 0.5f) {
                                event.setCanceled(true);
                                player.level().playSound(
                                        null,
                                        player.blockPosition(),
                                        SoundEvents.SHIELD_BLOCK,
                                        SoundSource.PLAYERS,
                                        1.0f,
                                        0.8f + player.getRandom().nextFloat() * 0.4f
                                );
                            }
                        });
            }
            // --- HighRulerShield: 10% AOE burst when actively blocking a hit ---
            if (blockingInHand && player.getRandom().nextFloat() < 0.50f) {
                double radius = 3.0;
                float damage = 4.0f;
                float knockbackStrength = 1.5f;

                player.level().getEntitiesOfClass(LivingEntity.class,
                                player.getBoundingBox().inflate(radius),
                                target -> target != player && target.isAlive())
                        .forEach(target -> {
                            target.hurt(player.level().damageSources().playerAttack(player), damage);

                            double dx = target.getX() - player.getX();
                            double dz = target.getZ() - player.getZ();
                            double dist = Math.sqrt(dx * dx + dz * dz);
                            if (dist > 0) {
                                target.setDeltaMovement(
                                        target.getDeltaMovement()
                                                .add(dx / dist * knockbackStrength, 0.4, dz / dist * knockbackStrength)
                                );
                                target.hurtMarked = true;
                            }
                        });

                // Play impact sound
                player.level().playSound(null, player.blockPosition(),
                        SoundEvents.SHIELD_BLOCK,
                        SoundSource.PLAYERS,
                        1.5f, 0.5f);

                // Spawn blastwave ring at player's feet
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            new BlastwaveParticleOptions(
                                    new Vector3f(0.3F, 0.4F, 0.9F),
                                    (float) radius
                            ),
                            player.getX(),
                            player.getY() + 0.1,
                            player.getZ(),
                            1, 0, 0, 0, 0
                    );
                }
            }
        }

        // --- Statis Curse: summon shadowflame proc ---
        Entity directSource = event.getSource().getDirectEntity();
        Entity trueSource = event.getSource().getEntity();
        IMagicSummon summon = null;

        if (directSource instanceof IMagicSummon directSummon) {
            summon = directSummon;
        } else if (trueSource instanceof IMagicSummon trueSummon) {
            summon = trueSummon;
        } else if (directSource instanceof Projectile projectile && projectile.getOwner() instanceof IMagicSummon projSummon) {
            summon = projSummon;
        } else if (directSource instanceof OwnableEntity ownable && ownable.getOwner() instanceof IMagicSummon ownedSummon) {
            summon = ownedSummon;
        }

        if (summon != null && summon.getSummoner() instanceof Player summoner) {
            if (Utils.hasCurio(summoner, ROACWItemRegistry.STATIS_CURSE.get())) {
                LivingEntity target = event.getEntity();
                MobEffect shadowFlame = ROACWEffectRegistry.SHADOWFLAME.get();

                if (target.level().getRandom().nextFloat() >= 0.1f) {
                    return;
                }

                int currentAmplifier = 0;
                if (target.hasEffect(shadowFlame)) {
                    MobEffectInstance activeEffect = target.getEffect(shadowFlame);
                    if (activeEffect != null) {
                        currentAmplifier = Math.min(activeEffect.getAmplifier() + 1, 4);
                    }
                }

                target.addEffect(new MobEffectInstance(shadowFlame, 200, currentAmplifier, false, false, true));
            }
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof Player player && Utils.hasCurio(player, ROACWItemRegistry.ELEMENTAL_GAUNTLET.get())) {
            event.getEntity().addEffect(new MobEffectInstance(
                    ROACWEffectRegistry.ELEMENTAL_MIX.get(),
                    200,
                    0,
                    false,
                    false,
                    true
            ));
        }
    }

    @SubscribeEvent
    public static void onLivingHealEvent(LivingHealEvent event) {
        MobEffectInstance ShadowFlameEffect = event.getEntity().getEffect(ROACWEffectRegistry.SHADOWFLAME.get());
        if (ShadowFlameEffect != null) {
            int effectLevel = ShadowFlameEffect.getAmplifier();
            event.setAmount((float) (event.getAmount() * (1 - effectLevel * 0.2)));
        }

        MobEffectInstance ElementalMixEffect = event.getEntity().getEffect(ROACWEffectRegistry.ELEMENTAL_MIX.get());
        if (ElementalMixEffect != null) {
            int effectLevel = ElementalMixEffect.getAmplifier();
            event.setAmount((float) (event.getAmount() * (1 - effectLevel * 0.3)));
        }
    }
}