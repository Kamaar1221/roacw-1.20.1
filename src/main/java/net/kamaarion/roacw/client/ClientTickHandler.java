package net.kamaarion.roacw.client;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.network.ModMessages;
import net.kamaarion.roacw.network.PacketDashC2S;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ROACW.MODID, value = Dist.CLIENT)
public class ClientTickHandler {
    private static boolean scarfWasPressed = false;
    private static boolean auricWasPressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;

        // Handle Original Scarf Dash (V Key) - Passes Type 1
        if (ModKeyMappings.DASH_KEY.isDown()) {
            if (!scarfWasPressed) {
                ModMessages.sendToServer(new PacketDashC2S(1));
                scarfWasPressed = true;
            }
        } else {
            scarfWasPressed = false;
        }

        // Handle New Auric Tesla Dash (X Key) - Passes Type 2
        if (ModKeyMappings.AURIC_DASH_KEY.isDown()) {
            if (!auricWasPressed) {
                ModMessages.sendToServer(new PacketDashC2S(2));
                auricWasPressed = true;
            }
        } else {
            auricWasPressed = false;
        }
    }
}

