package net.kamaarion.roacw.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class DreadEffect extends MobEffect {

    public static final String DREAD_SOURCE_TAG = "ROACW_DreadSource";

    private static final UUID DREAD_ARMOR_MODIFIER_UUID =
            UUID.fromString("7E8D8F5C-7D7C-4B4B-9B4A-1E3D3F5C8A21");

    private static final UUID DREAD_SPEED_MODIFIER_UUID =
            UUID.fromString("A4D9C3B7-5F2E-4F4E-8A3B-6D7E9C2B1F54");

    private static final double BASE_ARMOR_REDUCTION = 0.25D;
    private static final double ARMOR_REDUCTION_PER_AMPLIFIER = 0.10D;
    private static final double MOVEMENT_SPEED_REDUCTION = 0.30D;
    private static final double FLEE_DISTANCE = 16.0D;

    public DreadEffect() {
        super(MobEffectCategory.HARMFUL, 0x21142E);
    }

    @Override
    public void addAttributeModifiers(
            LivingEntity entity,
            AttributeMap attributes,
            int amplifier
    ) {
        super.addAttributeModifiers(entity, attributes, amplifier);

        double armorReduction = getArmorReduction(amplifier);

        AttributeInstance armor = attributes.getInstance(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(DREAD_ARMOR_MODIFIER_UUID);
            armor.addTransientModifier(new AttributeModifier(
                    DREAD_ARMOR_MODIFIER_UUID,
                    "Dread armor reduction",
                    -armorReduction,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }

        AttributeInstance movementSpeed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(DREAD_SPEED_MODIFIER_UUID);
            movementSpeed.addTransientModifier(new AttributeModifier(
                    DREAD_SPEED_MODIFIER_UUID,
                    "Dread movement speed reduction",
                    -MOVEMENT_SPEED_REDUCTION,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }

        if (entity instanceof Mob mob) {
            disableAttackGoals(mob);
        }
    }

    @Override
    public void removeAttributeModifiers(
            LivingEntity entity,
            AttributeMap attributes,
            int amplifier
    ) {
        AttributeInstance armor = attributes.getInstance(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(DREAD_ARMOR_MODIFIER_UUID);
        }

        AttributeInstance movementSpeed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(DREAD_SPEED_MODIFIER_UUID);
        }

        if (entity instanceof Mob mob) {
            enableAttackGoals(mob);
        }

        entity.getPersistentData().remove(DREAD_SOURCE_TAG);

        super.removeAttributeModifiers(entity, attributes, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }

        if (entity instanceof Player) {
            return;
        }

        if (!(entity instanceof Mob mob)) {
            return;
        }

        disableAttackGoals(mob);

        LivingEntity fearSource = findFearSource(entity);

        if (fearSource == null || !fearSource.isAlive()) {
            mob.setAggressive(false);
            mob.getNavigation().stop();
            return;
        }

        mob.setAggressive(false);

        fleeFromSource(mob, fearSource);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public static LivingEntity getFearSource(LivingEntity entity) {
        if (!entity.getPersistentData().hasUUID(DREAD_SOURCE_TAG)) {
            return null;
        }

        UUID sourceUUID = entity.getPersistentData()
                .getUUID(DREAD_SOURCE_TAG);

        if (entity.level() instanceof ServerLevel serverLevel) {
            Entity source = serverLevel.getEntity(sourceUUID);

            if (source instanceof LivingEntity living
                    && living.isAlive()) {
                return living;
            }
        }

        return null;
    }

    private LivingEntity findFearSource(LivingEntity entity) {
        return getFearSource(entity);
    }

    private void disableAttackGoals(Mob mob) {
        mob.goalSelector.disableControlFlag(Goal.Flag.MOVE);
        mob.goalSelector.disableControlFlag(Goal.Flag.LOOK);
    }

    private void enableAttackGoals(Mob mob) {
        mob.goalSelector.enableControlFlag(Goal.Flag.MOVE);
        mob.goalSelector.enableControlFlag(Goal.Flag.LOOK);
    }

    private void fleeFromSource(Mob mob, LivingEntity source) {
        Vec3 awayDirection = mob.position().subtract(source.position());

        if (awayDirection.lengthSqr() < 0.0001D) {
            awayDirection = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            awayDirection = awayDirection.normalize();
        }

        double targetX = mob.getX() + awayDirection.x * FLEE_DISTANCE;
        double targetY = mob.getY();
        double targetZ = mob.getZ() + awayDirection.z * FLEE_DISTANCE;

        mob.getNavigation().moveTo(
                targetX,
                targetY,
                targetZ,
                1.0D
        );
    }

    private double getArmorReduction(int amplifier) {
        return BASE_ARMOR_REDUCTION
                + (ARMOR_REDUCTION_PER_AMPLIFIER * amplifier);
    }
}