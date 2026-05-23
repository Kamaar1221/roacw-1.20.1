package net.kamaarion.roacw;

import com.github.L_Ender.cataclysm.init.ModEffect;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWEffectRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.common.Mod;

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
        ItemStack chestplate = living.getItemBySlot(EquipmentSlot.CHEST);
        if ((living.level() instanceof ServerLevel serverLevel)&& chestplate.getItem() == ROACWItemRegistry.AURIC_TESLA_CUIRASS.get() && !living.hasEffect(ROACWEffectRegistry.AURIC_EXHAUSTION.get()) && !living.hasEffect(ModEffect.EFFECTGHOST_SICKNESS.get())) {
            living.setHealth(10.0F);
            serverLevel.playSound(
                    null,
                    living.getX(), living.getY(), living.getZ(),
                    SoundEvents.TOTEM_USE,
                    living.getSoundSource(),
                    1.25f,
                    1.0F
            );
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
            living.addEffect(new MobEffectInstance(ROACWEffectRegistry.AURIC_EXHAUSTION.get(), 1000, 0, false, true, true));
            living.addEffect(new MobEffectInstance(ROACWEffectRegistry.AURIC_CHARGE.get(), 300, 0, false, true, true));
            double d0 = living.getX();
            double d1 = living.getY() + 3F;
            double d2 = living.getZ();
            return true;
        }
        return false;
    }
    @SubscribeEvent
    public void effectGiver(LivingDamageEvent event){
        if(event.getSource().getEntity() instanceof IMagicSummon summon && summon.getSummoner() instanceof Player summoner && Utils.hasCurio(summoner, ROACWItemRegistry.STATIS_CURSE.get())){
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.BLINDNESS,100, 0));
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,200, 0));
            event.getEntity().addEffect(new MobEffectInstance(ModEffect.EFFECTABYSSAL_BURN.get(),200, 0));
        }
    }
    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event){
        if(event.getSource().getEntity() instanceof Player player && Utils.hasCurio(player, ROACWItemRegistry.ELEMENTAL_GAUNTLET.get())){
            event.getEntity().addEffect(new MobEffectInstance(ModEffect.EFFECTBLAZING_BRAND.get(),200, 0));
            event.getEntity().addEffect(new MobEffectInstance(MobEffectRegistry.CHILLED.get(),200, 0));
            event.getEntity().addEffect(new MobEffectInstance(MobEffectRegistry.BLIGHT.get(),200, 0));
            event.getEntity().setRemainingFireTicks(Math.max(event.getEntity().getRemainingFireTicks(), 200));
        }
    }
}

