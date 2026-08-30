package net.kamaarion.roacw.entity.mob.earthen_paladin;

import io.redspace.ironsspellbooks.entity.mobs.goals.WarlockAttackGoal;
import net.kamaarion.roacw.registeries.ROACWSpellRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Fully self-contained attack decision - deliberately never delegates to
 * WarlockAttackGoal/WizardAttackGoal's own handleAttackLogic() at all. That
 * inherited logic is built around switching between two movement MODES
 * (melee-chase vs. ranged-kite/strafe), and every previous version of this
 * goal that tried to phase between the two ran into some flavor of the same
 * problem: the inherited spellcasting timer (spellAttackDelay) and our own
 * phase tracking could get out of sync, causing casts to trigger at the
 * wrong moment relative to melee swings.
 *
 * wantsToMelee is pinned true for the goal's whole lifetime (movement/
 * doMovement, still WarlockAttackGoal's own, always stays in melee-chase
 * mode - it never backs off to spellcasting range). Every
 * MELEE_HITS_BEFORE_CAST real swings, instead of swinging again we call
 * initiateCastSpell() directly ourselves, wait for it to finish (isCasting()
 * gate) and for the last swing's animation to actually complete
 * (swingRecoveryTimer), then resume melee.
 *
 * Melee attack selection cycles through ATTACK_PATTERN - sweep is the
 * default/dominant attack, with downslash-sideslash used periodically as a
 * heavier finisher. It's a genuine two-strike combo (confirmed via keyframe
 * data - two distinct swing extremes around 1.25s and ~2.0s), so it
 * schedules two deferred hits. All hit timings are eyeballed from keyframe
 * data, not frame-exact - tune once you see them land in-game.
 */
public class EarthenPaladinAttackGoal extends WarlockAttackGoal {

    private enum AttackType { SWEEP, DOWNSLASH_SIDESLASH }

    // Sweep-dominant pattern: 2 sweeps for every 1 downslash-sideslash.
    // Cycles by index, wrapping - easy to retune the ratio/order here.
    private static final AttackType[] ATTACK_PATTERN = {
            AttackType.SWEEP, AttackType.SWEEP, AttackType.DOWNSLASH_SIDESLASH
    };

    private final EarthenPaladinEntity paladin;
    private final double meleeTriggerRangeSqr; // used only for the pending-hit whiff check below

    private static final int MELEE_HITS_BEFORE_CAST = 4;
    private int meleeHitsSinceLastCast;
    private int attackPatternIndex;

    // Real clip lengths (from earthen_paladin.animation.json), used to gate
    // the cast-trigger branch below on the last swing's animation actually
    // finishing, not just its damage/cooldown timers.
    private static final int SWEEP_LENGTH_TICKS = 40; // 2.0s
    private static final int DOWNSLASH_SIDESLASH_LENGTH_TICKS = 58; // 2.875s
    private int swingRecoveryTimer;

    // Eyeballed from keyframe data, not frame-exact - tune to taste.
    private static final int SWEEP_HIT_DELAY_TICKS = 25; // ~1.25s, single strike
    private static final int DOWNSLASH_SIDESLASH_FIRST_HIT_DELAY_TICKS = 25; // ~1.25s, the downslash
    private static final int DOWNSLASH_SIDESLASH_SECOND_HIT_DELAY_TICKS = 40; // ~2.0s, the sideslash

    private static class PendingHit {
        final LivingEntity target;
        int ticksRemaining;

        PendingHit(LivingEntity target, int ticksRemaining) {
            this.target = target;
            this.ticksRemaining = ticksRemaining;
        }
    }

    private final List<PendingHit> pendingHits = new ArrayList<>();

    public EarthenPaladinAttackGoal(EarthenPaladinEntity paladin, double speedModifier,
                                    int minAttackInterval, int maxAttackInterval, double meleeTriggerRange) {
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
        // WarlockAttackGoal.tick() (called via super.tick() above) has its
        // own periodic health-biased reroll of wantsToMelee that would
        // otherwise occasionally flip it false. Reasserting it here, AFTER
        // super.tick() runs, is sufficient - wantsToMelee is only actually
        // read at the START of doMovement()/handleAttackLogic() on the NEXT
        // tick, so this always wins by the time it matters.
        this.wantsToMelee = true;

        if (swingRecoveryTimer > 0) {
            swingRecoveryTimer--;
        }

        Iterator<PendingHit> iterator = pendingHits.iterator();
        while (iterator.hasNext()) {
            PendingHit hit = iterator.next();
            if (--hit.ticksRemaining <= 0) {
                iterator.remove();
                if (hit.target.isAlive() && paladin.distanceToSqr(hit.target) <= meleeTriggerRangeSqr) {
                    double distanceSquared = paladin.distanceToSqr(hit.target.getX(), hit.target.getY(), hit.target.getZ());
                    paladin.doHurtTarget(hit.target);
                    resetMeleeAttackInterval(distanceSquared);
                }
                // Target dodged out of range during the wind-up - whiff, no
                // damage, matches a real dodge.
            }
        }
    }

    @Override
    protected void handleAttackLogic(double distanceSquared) {
        if (paladin.isCasting()) {
            return; // mid-cast - wait it out, doMovement() still holds melee range the whole time
        }

        if (meleeHitsSinceLastCast >= MELEE_HITS_BEFORE_CAST) {
            if (swingRecoveryTimer > 0) {
                return; // last swing's animation is still playing - wait for it to fully finish
            }
            LivingEntity target = paladin.getTarget();
            if (target != null) {
                int spellLevel = Math.max(1, Math.round(5 * Mth.lerp(paladin.getRandom().nextFloat(), 0.5f, 1.0f)));
                paladin.initiateCastSpell(ROACWSpellRegistry.IMPALING_COLUMN.get(), spellLevel);
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
        // Not in range yet - doMovement() (wantsToMelee is always true, so
        // this is always WarlockAttackGoal's melee-chase logic) is already
        // closing the distance. Nothing to do here but wait.
    }

    @Override
    protected void doMeleeAction() {
        AttackType attack = ATTACK_PATTERN[attackPatternIndex % ATTACK_PATTERN.length];
        attackPatternIndex++;
        LivingEntity target = paladin.getTarget();

        switch (attack) {
            case DOWNSLASH_SIDESLASH -> {
                paladin.triggerAnim("action", "scythe_downslash_sideslash");
                if (target != null) {
                    pendingHits.add(new PendingHit(target, DOWNSLASH_SIDESLASH_FIRST_HIT_DELAY_TICKS));
                    pendingHits.add(new PendingHit(target, DOWNSLASH_SIDESLASH_SECOND_HIT_DELAY_TICKS));
                }
                swingRecoveryTimer = DOWNSLASH_SIDESLASH_LENGTH_TICKS;
            }
            default -> { // SWEEP
                paladin.triggerAnim("action", "scythe_low_rightward_sweep");
                if (target != null) {
                    pendingHits.add(new PendingHit(target, SWEEP_HIT_DELAY_TICKS));
                }
                swingRecoveryTimer = SWEEP_LENGTH_TICKS;
            }
        }

        meleeHitsSinceLastCast++;

        // Immediate provisional cooldown - meleeAttackDelay otherwise only
        // gets properly reset once a deferred hit resolves in tick() (at
        // least SWEEP_HIT_DELAY_TICKS later), leaving it deeply negative in
        // the meantime. Since handleAttackLogic() checks it every tick, that
        // caused doMeleeAction() to re-fire on literally the next tick,
        // spamming swings once per tick. resetMeleeAttackInterval() (called
        // from the deferred block once a hit actually lands) still refines
        // this to the real value afterward - this is just a floor to
        // survive the gap until then.
        this.meleeAttackDelay = SWEEP_HIT_DELAY_TICKS + 10;
    }
}