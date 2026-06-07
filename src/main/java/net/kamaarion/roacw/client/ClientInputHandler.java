package net.kamaarion.roacw.client;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.network.ModMessages;
import net.kamaarion.roacw.network.PacketDashC2S;
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
                    ModMessages.sendToServer(new PacketDashC2S(1));
                }
            });
        }

        // Handle New Auric Tesla Dash (X Key)
        if (ModKeyMappings.AURIC_DASH_KEY.consumeClick()) {
            // FIX: Aligned the equipment validations to match the server-side registration checks exactly
            boolean hasFullSet = player.getItemBySlot(EquipmentSlot.HEAD).is(ROACWItemRegistry.AURIC_TESLA_ROYAL_HELM.get()) &&
                    player.getItemBySlot(EquipmentSlot.CHEST).is(ROACWItemRegistry.AURIC_TESLA_CUIRASS.get()) &&
                    player.getItemBySlot(EquipmentSlot.LEGS).is(ROACWItemRegistry.AURIC_TESLA_CUISSES.get()) &&
                    player.getItemBySlot(EquipmentSlot.FEET).is(ROACWItemRegistry.AURIC_TESLA_BOOTS.get());

            if (hasFullSet) {
                ModMessages.sendToServer(new PacketDashC2S(2));
            }
        }
    }
}
