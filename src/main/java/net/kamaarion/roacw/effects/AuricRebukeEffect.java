package net.kamaarion.roacw.effects;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuricRebukeEffect extends MobEffect {
  private static final Map<UUID, UUID> SOURCES = new ConcurrentHashMap<>();
  private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

  private static final float BASE_DAMAGE = 2.0F;
  private static final float MOVING_MULTIPLIER = 2.5F;
  private static final float LIGHTNING_DAMAGE = 10.0F;
  private static final int COOLDOWN_TICKS = 300;

  public AuricRebukeEffect() {
    super(MobEffectCategory.HARMFUL, 0xD4AF37);
  }

  public static void setSource(LivingEntity target, ServerPlayer source) {
    SOURCES.put(target.getUUID(), source.getUUID());
  }

  public static UUID getSource(LivingEntity target) {
    return SOURCES.get(target.getUUID());
  }

  public static void clearSource(LivingEntity target) {
    SOURCES.remove(target.getUUID());
  }

  public static boolean isOnCooldown(LivingEntity target) {
    Long cooldownEnd = COOLDOWNS.get(target.getUUID());

    if (cooldownEnd == null) {
      return false;
    }

    if (target.level().getGameTime() >= cooldownEnd) {
      COOLDOWNS.remove(target.getUUID());
      return false;
    }

    return true;
  }

  public static void startCooldown(LivingEntity target) {
    COOLDOWNS.put(target.getUUID(), target.level().getGameTime() + COOLDOWN_TICKS);
  }

  @Override
  public boolean isDurationEffectTick(int duration, int amplifier) {
    return duration % 20 == 0;
  }

  @Override
  public void applyEffectTick(LivingEntity entity, int amplifier) {
    if (entity.level().isClientSide()) return;

    UUID sourceId = getSource(entity);
    if (sourceId == null) return;

    if (!(entity.level().getPlayerByUUID(sourceId) instanceof ServerPlayer source) || !source.isAlive()) {
      clearSource(entity);
      return;
    }

    double lightningPower = source.getAttributeValue(AttributeRegistry.LIGHTNING_SPELL_POWER.get());
    float damage = (float) (BASE_DAMAGE * lightningPower);

    Vec3 movement = entity.getDeltaMovement();
    boolean moving = movement.horizontalDistanceSqr() > 0.0001D;

    if (moving) {
      damage *= MOVING_MULTIPLIER;
    }

    entity.hurt(entity.damageSources().magic(), damage);
  }

  @Override
  public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
    if (!entity.level().isClientSide()) {
      UUID sourceId = getSource(entity);

      if (sourceId != null && entity.level() instanceof ServerLevel serverLevel) {
        if (serverLevel.getPlayerByUUID(sourceId) instanceof ServerPlayer source && source.isAlive()) {
          double lightningPower = source.getAttributeValue(AttributeRegistry.LIGHTNING_SPELL_POWER.get());
          float damage = (float) (LIGHTNING_DAMAGE * lightningPower);

          LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);

          if (lightning != null) {
            lightning.moveTo(entity.getX(), entity.getY(), entity.getZ());
            lightning.setVisualOnly(true);
            serverLevel.addFreshEntity(lightning);
            entity.hurt(entity.damageSources().lightningBolt(), damage);
          }

          startCooldown(entity);
        }
      }
    }

    clearSource(entity);
    super.removeAttributeModifiers(entity, attributeMap, amplifier);
  }
}