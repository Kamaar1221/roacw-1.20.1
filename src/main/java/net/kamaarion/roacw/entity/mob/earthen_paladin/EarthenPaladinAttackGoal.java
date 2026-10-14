package net.kamaarion.roacw.entity.mob.earthen_paladin;

import io.redspace.ironsspellbooks.entity.mobs.goals.WarlockAttackGoal;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EarthenPaladinAttackGoal extends WarlockAttackGoal {

    private enum AttackType {
        SWEEP,
        DOWNSLASH_SIDESLASH
    }

    private static final AttackType[] ATTACK_PATTERN = {
            AttackType.SWEEP,
            AttackType.SWEEP,
            AttackType.DOWNSLASH_SIDESLASH
    };

    private static final int MELEE_HITS_BEFORE_CAST = 4;
    private static final int SWEEP_LENGTH_TICKS = 40;
    private static final int DOWNSLASH_SIDESLASH_LENGTH_TICKS = 58;
    private static final int SWEEP_HIT_DELAY_TICKS = 25;
    private static final int DOWNSLASH_SIDESLASH_FIRST_HIT_DELAY_TICKS = 25;
    private static final int DOWNSLASH_SIDESLASH_SECOND_HIT_DELAY_TICKS = 40;

    private final EarthenPaladinEntity paladin;
    private final double meleeTriggerRangeSqr;
    private final List<PendingHit> pendingHits = new ArrayList<>();

    private int meleeHitsSinceLastCast;
    private int attackPatternIndex;
    private int swingRecoveryTimer;

    private static class PendingHit {
        private final LivingEntity target;
        private int ticksRemaining;

        private PendingHit(LivingEntity target, int ticksRemaining) {
            this.target = target;
            this.ticksRemaining = ticksRemaining;
        }
    }

    public EarthenPaladinAttackGoal(
            EarthenPaladinEntity paladin,
            double speedModifier,
            int minAttackInterval,
            int maxAttackInterval,
            double meleeTriggerRange
    ) {
        super(paladin, speedModifier, minAttackInterval, maxAttackInterval);
        this.paladin = paladin;
        this.meleeTriggerRangeSqr = meleeTriggerRange * meleeTriggerRange;
    }

    @Override
    public void start() {
        super.start();
        this.wantsToMelee = true;
        meleeHitsSinceLastCast = 0;
        pendingHits.clear();
        swingRecoveryTimer = 0;
    }

    @Override
    public void stop() {
        super.stop();
        pendingHits.clear();
        swingRecoveryTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();
        this.wantsToMelee = true;

        if (swingRecoveryTimer > 0) {
            swingRecoveryTimer--;
        }

        Iterator<PendingHit> iterator = pendingHits.iterator();

        while (iterator.hasNext()) {
            PendingHit hit = iterator.next();

            if (--hit.ticksRemaining <= 0) {
                iterator.remove();

                if (hit.target.isAlive()
                        && paladin.distanceToSqr(hit.target) <= meleeTriggerRangeSqr) {
                    double distanceSquared = paladin.distanceToSqr(
                            hit.target.getX(),
                            hit.target.getY(),
                            hit.target.getZ()
                    );

                    paladin.doHurtTarget(hit.target);
                    resetMeleeAttackInterval(distanceSquared);
                }
            }
        }
    }

    @Override
    protected void handleAttackLogic(double distanceSquared) {
        if (paladin.isCasting()) {
            return;
        }

        if (meleeHitsSinceLastCast >= MELEE_HITS_BEFORE_CAST) {
            if (swingRecoveryTimer > 0) {
                return;
            }

            LivingEntity target = paladin.getTarget();

            if (target != null) {
                int spellLevel = Math.max(
                        1,
                        Math.round(
                                5 * Mth.lerp(
                                        paladin.getRandom().nextFloat(),
                                        0.5f,
                                        1.0f
                                )
                        )
                );

                paladin.initiateCastSpell(
                        ROACWSpellRegistry.IMPALING_COLUMN.get(),
                        spellLevel
                );

                meleeHitsSinceLastCast = 0;
            }

            return;
        }

        float meleeRangeNow = meleeRange();

        if (distanceSquared <= meleeRangeNow * meleeRangeNow) {
            if (--this.meleeAttackDelay <= 0) {
                paladin.swing(InteractionHand.MAIN_HAND);
                doMeleeAction();
            }
        }
    }

    @Override
    protected void doMeleeAction() {
        AttackType attack = ATTACK_PATTERN[
                attackPatternIndex % ATTACK_PATTERN.length
                ];

        attackPatternIndex++;

        LivingEntity target = paladin.getTarget();

        switch (attack) {
            case DOWNSLASH_SIDESLASH -> {
                paladin.triggerAnim("action", "scythe_downslash_sideslash");

                if (target != null) {
                    pendingHits.add(
                            new PendingHit(
                                    target,
                                    DOWNSLASH_SIDESLASH_FIRST_HIT_DELAY_TICKS
                            )
                    );

                    pendingHits.add(
                            new PendingHit(
                                    target,
                                    DOWNSLASH_SIDESLASH_SECOND_HIT_DELAY_TICKS
                            )
                    );
                }

                swingRecoveryTimer = DOWNSLASH_SIDESLASH_LENGTH_TICKS;
            }

            default -> {
                paladin.triggerAnim("action", "scythe_low_rightward_sweep");

                if (target != null) {
                    pendingHits.add(
                            new PendingHit(
                                    target,
                                    SWEEP_HIT_DELAY_TICKS
                            )
                    );
                }

                swingRecoveryTimer = SWEEP_LENGTH_TICKS;
            }
        }

        meleeHitsSinceLastCast++;

        this.meleeAttackDelay = SWEEP_HIT_DELAY_TICKS + 10;
    }
}