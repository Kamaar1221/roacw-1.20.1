package net.kamaarion.roacw.registeries;

import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.kamaarion.roacw.ROACW;

public class ROACWAnimationRegistry {

    public static final AnimationHolder OVERHEAD_SWING_START =
            new AnimationHolder(ROACW.MODID + ":overhead_swing_start", false);

    public static final AnimationHolder OVERHEAD_SWING_FINISH =
            new AnimationHolder(ROACW.MODID + ":overhead_swing_finish", true);

    public static final AnimationHolder OVERHEAD_SWORD_SLAM =
            new AnimationHolder(ROACW.MODID + ":overhead_sword_slam", true);
}