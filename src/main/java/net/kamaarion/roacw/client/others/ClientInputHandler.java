package net.kamaarion.roacw.client.others;

import com.mojang.logging.LogUtils;
import net.kamaarion.roacw.Config;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.armor.marsarmorset.MarsArmorFlightHandler;
import net.kamaarion.roacw.items.armor.marsarmorset.MarsArmorParticleHandler;
import net.kamaarion.roacw.network.ModMessages;
import net.kamaarion.roacw.network.armorset_abilities.MarsCannonsSummonC2S;
import net.kamaarion.roacw.network.armorset_abilities.PacketAuricTeslaDashC2S;
import net.kamaarion.roacw.network.armorset_abilities.PacketEarthenPaladinPulseC2S;
import net.kamaarion.roacw.network.armorset_abilities.PacketEvasionScarfDashC2S;
import net.kamaarion.roacw.network.armorset_abilities.PacketPlaguebringerJetBoostC2S;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(
        modid = ROACW.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class ClientInputHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean CORE_CALIBRATION_LOG = true;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

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

        if (ModKeyMappings.AURIC_DASH_KEY.consumeClick()) {
            if (!Config.ENABLE_AURIC_TESLA_DASH.get()) {
                return;
            }

            boolean hasFullSet =
                    player.getItemBySlot(EquipmentSlot.HEAD)
                            .is(ROACWItemRegistry.AURIC_TESLA_ROYAL_HELM.get())
                            && player.getItemBySlot(EquipmentSlot.CHEST)
                            .is(ROACWItemRegistry.AURIC_TESLA_CUIRASS.get())
                            && player.getItemBySlot(EquipmentSlot.LEGS)
                            .is(ROACWItemRegistry.AURIC_TESLA_CUISSES.get())
                            && player.getItemBySlot(EquipmentSlot.FEET)
                            .is(ROACWItemRegistry.AURIC_TESLA_BOOTS.get());

            if (hasFullSet) {
                ModMessages.sendToServer(new PacketAuricTeslaDashC2S());
            }
        }

        if (ModKeyMappings.ARMOR_PULSE_KEY.consumeClick()) {
            ModMessages.sendToServer(new PacketEarthenPaladinPulseC2S());
        }

        if (ModKeyMappings.PLAGUEBRINGER_JET_BOOST_KEY.consumeClick()) {
            ModMessages.sendToServer(new PacketPlaguebringerJetBoostC2S());
        }

        if (ModKeyMappings.MARS_CANNONS_KEY.consumeClick()) {
            if (!MarsArmorFlightHandler.isWearingFullSet(player)) {
                return;
            }

            Vec3 corePosition = MarsArmorParticleHandler.getCorePosition(player);

            if (CORE_CALIBRATION_LOG) {
                logCoreCalibration(player, corePosition);
            }

            if (corePosition == null) {
                corePosition = fallbackCorePosition(player);
            }

            ModMessages.sendToServer(new MarsCannonsSummonC2S(corePosition));
        }
    }

    private static void logCoreCalibration(
            LocalPlayer player,
            Vec3 corePosition
    ) {
        if (corePosition == null) {
            LOGGER.info("[MarsCore] no fresh core sample - switch to third person (F5) and try again");
            return;
        }

        float yawRad = player.yBodyRot * Mth.DEG_TO_RAD;

        Vec3 local = corePosition
                .subtract(player.position())
                .yRot(yawRad);

        LOGGER.info(
                "[MarsCore] body-local core offset: side(+left)={} up={} forward={}",
                String.format("%.3f", local.x),
                String.format("%.3f", local.y),
                String.format("%.3f", local.z)
        );
    }

    private static Vec3 fallbackCorePosition(LocalPlayer player) {
        double heightOffset = 0.65D;
        double forwardOffset = 0.35D;

        float yawRad = (float) Math.toRadians(player.yBodyRot);

        double forwardX = -Math.sin(yawRad) * forwardOffset;
        double forwardZ = Math.cos(yawRad) * forwardOffset;

        return player.position().add(forwardX, heightOffset, forwardZ);
    }
}