package net.kamaarion.roacw.client.others;

import com.mojang.blaze3d.platform.InputConstants;
import net.kamaarion.roacw.ROACW;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
        modid = ROACW.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ModKeyMappings {

    public static final String KEY_CATEGORY_ROACW =
            "key.categories.roacw";

    public static final KeyMapping DASH_KEY =
            new KeyMapping(
                    "key.roacw.evasion_scarf_dash",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_V,
                    KEY_CATEGORY_ROACW
            );

    public static final KeyMapping AURIC_DASH_KEY =
            new KeyMapping(
                    "key.roacw.auric_dash",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_X,
                    KEY_CATEGORY_ROACW
            );

    public static final KeyMapping ARMOR_PULSE_KEY =
            new KeyMapping(
                    "key.roacw.armor_pulse",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_X,
                    KEY_CATEGORY_ROACW
            );

    public static final KeyMapping PLAGUEBRINGER_JET_BOOST_KEY =
            new KeyMapping(
                    "key.roacw.plaguebringer_jet_boost",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_X,
                    KEY_CATEGORY_ROACW
            );

    public static final KeyMapping MARS_CANNONS_KEY =
            new KeyMapping(
                    "key.roacw.mars_cannons",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_X,
                    KEY_CATEGORY_ROACW
            );

    public static final KeyMapping SHRIMP_TIDAL_SURGE_KEY =
            new KeyMapping(
                    "key.roacw.shrimp_tidal_surge",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_X,
                    KEY_CATEGORY_ROACW
            );

    public static final KeyMapping DISTORTED_ENVOY_FACE_KEY =
            new KeyMapping(
                    "key.roacw.distorted_envoy_face",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_B,
                    KEY_CATEGORY_ROACW
            );

    public static final KeyMapping RAGE_KEY =
            new KeyMapping(
                    "key.roacw.rage_mode",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_Z,
                    KEY_CATEGORY_ROACW
            );

    public static final KeyMapping ADRENALINE_KEY =
            new KeyMapping(
                    "key.roacw.adrenaline_mode",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_C,
                    KEY_CATEGORY_ROACW
            );

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(DASH_KEY);
        event.register(AURIC_DASH_KEY);
        event.register(ARMOR_PULSE_KEY);
        event.register(PLAGUEBRINGER_JET_BOOST_KEY);
        event.register(MARS_CANNONS_KEY);
        event.register(SHRIMP_TIDAL_SURGE_KEY);
        event.register(DISTORTED_ENVOY_FACE_KEY);
        event.register(RAGE_KEY);
        event.register(ADRENALINE_KEY);
    }
}