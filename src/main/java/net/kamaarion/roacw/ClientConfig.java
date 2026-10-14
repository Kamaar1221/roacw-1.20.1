package net.kamaarion.roacw;

import net.kamaarion.roacw.items.curios.wings.FlightWings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue REMOVE_CAPE;
    public static final ForgeConfigSpec.EnumValue<FlightWings.MeterDisplay> WINGS_METER_DISPLAY;
    public static final ForgeConfigSpec.EnumValue<FlightWings.MeterAnchor> WINGS_METER_ANCHOR;
    public static final ForgeConfigSpec.IntValue WINGS_METER_OFFSET_X;
    public static final ForgeConfigSpec.IntValue WINGS_METER_OFFSET_Y;

    static {
        BUILDER.push("Visual Settings");
        REMOVE_CAPE = BUILDER.comment("Set to true to hide the Auric Tesla cape on your client screen.")
                .define("removeCape", false);
        BUILDER.pop();

        BUILDER.push("Flight Wings HUD");
        WINGS_METER_DISPLAY = BUILDER.comment("When to show the wing flight meter. NEVER hides it. ALWAYS shows it whenever flight wings are worn. CONTEXTUAL shows it only while the meter is not full or you are in the air.")
                .defineEnum("wingMeterDisplay", FlightWings.MeterDisplay.CONTEXTUAL);
        WINGS_METER_ANCHOR = BUILDER.comment("Which part of the screen the wing flight meter is positioned from.")
                .defineEnum("wingMeterAnchor", FlightWings.MeterAnchor.HOTBAR_LEFT);
        WINGS_METER_OFFSET_X = BUILDER.comment("Horizontal offset of the wing flight meter. Positive moves it right, negative moves it left.")
                .defineInRange("wingMeterOffsetX", 0, -4000, 4000);
        WINGS_METER_OFFSET_Y = BUILDER.comment("Vertical offset of the wing flight meter. Positive moves it down, negative moves it up.")
                .defineInRange("wingMeterOffsetY", 0, -4000, 4000);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private static boolean lastRemoveCape = false;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            lastRemoveCape = REMOVE_CAPE.get();
        }
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != SPEC) return;

        boolean removeCape = REMOVE_CAPE.get();
        if (removeCape == lastRemoveCape) return;
        lastRemoveCape = removeCape;

        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().reloadResourcePacks();
            }
        });
    }
}