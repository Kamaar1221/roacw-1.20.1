package net.kamaarion.roacw.registeries;

import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;

public class ROACWAnimationRegistry {

    public static final AnimationHolder OVERHEAD_SWING_START =
            new AnimationHolder(
                    ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "overhead_swing_start"),
                    false,
                    false
            );

    public static final AnimationHolder OVERHEAD_SWING_FINISH =
            new AnimationHolder(
                    ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "overhead_swing_finish"),
                    true,
                    false
            );

    public static final AnimationHolder OVERHEAD_SWORD_SLAM =
            new AnimationHolder(
                    ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "overhead_sword_slam"),
                    true,
                    false
            );

    public static final AnimationHolder TYRANT_ROAR =
            new AnimationHolder(
                    ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "roar"),
                    true,
                    false
            );
}