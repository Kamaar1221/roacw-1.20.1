package net.kamaarion.roacw.client.others;

import net.kamaarion.roacw.Config; // Added config import
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.network.*;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // Handle Original Scarf Dash (V Key)
        if (ModKeyMappings.DASH_KEY.consumeClick()) {
            Item dashItem = ROACWItemRegistry.EVASION_SCARF.get();
            if (player.getCooldowns().isOnCooldown(dashItem)) {
                return;
            }

            CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
                if (!inv.findCurios(dashItem).isEmpty()) {
                    ModMessages.sendToServer(new PacketEvasionScarfDashC2S());
                }
            });
        }

        // Handle New Auric Tesla Dash (X Key)
        if (ModKeyMappings.AURIC_DASH_KEY.consumeClick()) {

            // FIX: Block the click immediately on the client if the config is disabled
            if (!Config.ENABLE_AURIC_TESLA_DASH.get()) {
                return;
            }

            // Aligned equipment validations matching your current registry item names exactly
            boolean hasFullSet = player.getItemBySlot(EquipmentSlot.HEAD).is(ROACWItemRegistry.AURIC_TESLA_ROYAL_HELM.get()) &&
                    player.getItemBySlot(EquipmentSlot.CHEST).is(ROACWItemRegistry.AURIC_TESLA_CUIRASS.get()) &&
                    player.getItemBySlot(EquipmentSlot.LEGS).is(ROACWItemRegistry.AURIC_TESLA_CUISSES.get()) &&
                    player.getItemBySlot(EquipmentSlot.FEET).is(ROACWItemRegistry.AURIC_TESLA_BOOTS.get());

            if (hasFullSet) {
                ModMessages.sendToServer(new PacketAuricTeslaDashC2S());
            }
        }

        // Handle Earthen Paladin Armor Pulse (G Key)
        if (ModKeyMappings.ARMOR_PULSE_KEY.consumeClick()) {
            ModMessages.sendToServer(new PacketEarthenPaladinPulseC2S());
        }

        // Handle Plaguebringer Jet Boost (H Key)
        if (ModKeyMappings.PLAGUEBRINGER_JET_BOOST_KEY.consumeClick()) {
            ModMessages.sendToServer(new PacketPlaguebringerJetBoostC2S());
        }
    }
}