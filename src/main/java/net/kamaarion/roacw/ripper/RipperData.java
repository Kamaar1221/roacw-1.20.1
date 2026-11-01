package net.kamaarion.roacw.ripper;

public class RipperData {
    public float rage;
    public float adrenaline;
    public int rageActiveTicks;
    public int adrenalineActiveTicks;
    public int combatTicks;
    public int bossCombatTicks;
    public int rageIdleTicks;
    public int ticksSinceHit = RipperManager.ADRENALINE_PENALTY_DURATION;
    public boolean bossNearby;
    public boolean bossEngaged;
    public boolean hostileEngaged;
    public boolean dirty;

    public boolean isRageActive() {
        return rageActiveTicks > 0;
    }

    public boolean isAdrenalineActive() {
        return adrenalineActiveTicks > 0;
    }
}