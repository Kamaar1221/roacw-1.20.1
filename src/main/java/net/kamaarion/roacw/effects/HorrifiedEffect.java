package net.kamaarion.roacw.effects;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HorrifiedEffect extends MobEffect {

  private static final Map<UUID, UUID> FEAR_SOURCES = new ConcurrentHashMap<>();

  private static final double FLEE_DISTANCE = 12.0D;
  private static final double FLEE_SPEED = 0.8D;

  public HorrifiedEffect() {
    super(MobEffectCategory.HARMFUL, 0x24002E);
  }

  public static void setFearSource(LivingEntity victim, ServerPlayer player) {
    FEAR_SOURCES.put(victim.getUUID(), player.getUUID());
  }

  private static UUID getFearSource(LivingEntity victim) {
    return FEAR_SOURCES.get(victim.getUUID());
  }

  private static void clearFearSource(LivingEntity victim) {
    FEAR_SOURCES.remove(victim.getUUID());
  }

  @Override
  public boolean isDurationEffectTick(int duration, int amplifier) {
    return true;
  }

  @Override
  public void applyEffectTick(LivingEntity entity, int amplifier) {
    if (!(entity instanceof Mob mob)) return;
    if (entity.level().isClientSide) return;

    UUID sourceId = getFearSource(entity);
    if (sourceId == null) return;

    if (!(entity.level().getPlayerByUUID(sourceId) instanceof ServerPlayer player)) {
      clearFearSource(entity);
      return;
    }

    if (!player.isAlive()) {
      clearFearSource(entity);
      return;
    }

    mob.setTarget(null);

    Vec3 fleeDirection = entity.position().subtract(player.position());

    List<LivingEntity> nearbyEntities = entity.level().getEntitiesOfClass(
            LivingEntity.class,
            entity.getBoundingBox().inflate(FLEE_DISTANCE),
            target -> target.isAlive()
                    && target instanceof IMagicSummon summon
                    && summon.getSummoner() == player
    );

    for (LivingEntity nearbyEntity : nearbyEntities) {
      Vec3 awayFromSummon = entity.position().subtract(nearbyEntity.position());

      if (awayFromSummon.lengthSqr() > 0.0001D) {
        double distance = Math.max(awayFromSummon.length(), 1.0D);
        fleeDirection = fleeDirection.add(
                awayFromSummon.normalize().scale(1.0D / distance)
        );
      }
    }

    if (fleeDirection.lengthSqr() < 0.0001D) {
      fleeDirection = new Vec3(1.0D, 0.0D, 0.0D);
    }

    fleeDirection = fleeDirection.normalize();

    Vec3 targetPosition = entity.position().add(fleeDirection.scale(FLEE_DISTANCE));
    BlockPos targetBlock = BlockPos.containing(targetPosition);

    if (mob.getNavigation().createPath(targetBlock, 0) != null) {
      mob.getNavigation().moveTo(
              targetBlock.getX(),
              targetBlock.getY(),
              targetBlock.getZ(),
              FLEE_SPEED
      );
    } else {
      mob.getNavigation().moveTo(
              targetPosition.x,
              targetPosition.y,
              targetPosition.z,
              FLEE_SPEED
      );
    }
  }

  public static boolean isSummonFromFearSource(LivingEntity entity, IMagicSummon summon) {
    UUID sourceId = getFearSource(entity);
    if (sourceId == null) return false;

    return summon.getSummoner() instanceof ServerPlayer player
            && player.getUUID().equals(sourceId);
  }

  @Override
  public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributes, int amplifier) {
    super.removeAttributeModifiers(entity, attributes, amplifier);
    clearFearSource(entity);
  }
}