package net.kamaarion.roacw.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.kamaarion.roacw.ROACW;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyMappings {
    public static final String KEY_CATEGORY_ROACW = "key.categories.roacw";

    public static final KeyMapping DASH_KEY = new KeyMapping(
            "key.roacw.dash",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY_ROACW
    );

    // Completely new key mapping added without modifying your original Scarf setup
    public static final KeyMapping AURIC_DASH_KEY = new KeyMapping(
            "key.roacw.auric_dash",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X, // Defaulting to the 'X' key natively
            KEY_CATEGORY_ROACW
    );

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(DASH_KEY);
        event.register(AURIC_DASH_KEY); // Registers the second key cleanly into Minecraft options
    }
}
