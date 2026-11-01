package net.kamaarion.roacw.client.ripper;

import net.kamaarion.roacw.ripper.RipperManager;

public class ClientRipperData {
    private static int rage;
    private static int adrenaline;
    private static int rageActiveTicks;
    private static int adrenalineActiveTicks;

    public static void set(int newRage, int newAdrenaline, int newRageActiveTicks, int newAdrenalineActiveTicks) {
        rage = newRage;
        adrenaline = newAdrenaline;
        rageActiveTicks = newRageActiveTicks;
        adrenalineActiveTicks = newAdrenalineActiveTicks;
    }

    public static void reset() {
        set(0, 0, 0, 0);
    }

    public static float rageFraction() {
        return Math.min(1.0F, rage / (float) RipperManager.RAGE_MAX);
    }

    public static float adrenalineFraction() {
        return Math.min(1.0F, adrenaline / (float) RipperManager.ADRENALINE_MAX);
    }

    public static boolean isRageActive() {
        return rageActiveTicks > 0;
    }

    public static boolean isAdrenalineActive() {
        return adrenalineActiveTicks > 0;
    }

    public static boolean isRageReady() {
        return rage >= RipperManager.RAGE_MAX && !isRageActive();
    }

    public static boolean isAdrenalineReady() {
        return adrenaline >= RipperManager.ADRENALINE_MAX && !isAdrenalineActive();
    }

    public static boolean shouldShowRage() {
        return rage > 0 || isRageActive();
    }

    public static boolean shouldShowAdrenaline() {
        return adrenaline > 0 || isAdrenalineActive();
    }
}