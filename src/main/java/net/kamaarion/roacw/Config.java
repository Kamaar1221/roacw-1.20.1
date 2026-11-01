package net.kamaarion.roacw;

import net.kamaarion.roacw.items.curios_additions.wings.FlightWings;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_AURIC_TESLA_DASH;
    public static final ForgeConfigSpec.BooleanValue ENABLE_AURIC_REVIVE;

    public static final ForgeConfigSpec.BooleanValue ENABLE_EARTHEN_PALADIN_PULSE;

    public static final ForgeConfigSpec.BooleanValue ENABLE_PLAGUEBRINGER_JET_BOOST;

    public static final ForgeConfigSpec.BooleanValue ENABLE_MARS_FLIGHT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MARS_CANNONS;

    public static final ForgeConfigSpec.BooleanValue ENABLE_SHRIMP_SET_BONUS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SHRIMP_TIDAL_SURGE;

    public static final ForgeConfigSpec.BooleanValue ENABLE_ADRENALINE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_RAGE;

    public static final ForgeConfigSpec.EnumValue<FlightWings.FallMode> WINGS_FALL_MODE;

    static {
        SERVER_BUILDER.push("Auric Tesla Armor Abilities");
        ENABLE_AURIC_TESLA_DASH = SERVER_BUILDER.comment("Enables/disables Dimensional Drive Dash.")
                .define("enableAuricTeslaDash", true);
        ENABLE_AURIC_REVIVE = SERVER_BUILDER.comment("Enables/disables Auric Tesla Revive.")
                .define("enableAuricRevive", true);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Mars Armor Abilities");
        ENABLE_MARS_FLIGHT = SERVER_BUILDER.comment("Enables/disables Mars Creative Flight.")
                .define("enableMarsFlight", true);
        ENABLE_MARS_CANNONS = SERVER_BUILDER.comment("Enables/disables Mars Arsenal.")
                .define("enableMarsCannons", true);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Earthen Paladin Armor Abilities");
        ENABLE_EARTHEN_PALADIN_PULSE = SERVER_BUILDER.comment("Enables/disables Earthen Paladin Armor Pulse.")
                .define("enableEarthenPaladinPulse", true);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Plaguebringer Armor Abilities");
        ENABLE_PLAGUEBRINGER_JET_BOOST = SERVER_BUILDER.comment("Enables/disables Plaguebringer Jet Boost.")
                .define("enablePlaguebringerJetBoost", true);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Shrimp Armor Abilities");
        ENABLE_SHRIMP_SET_BONUS = SERVER_BUILDER.comment("Enables/disables Shrimp Armor Set Bonus.")
                .define("enableShrimpSetBonus", true);
        ENABLE_SHRIMP_TIDAL_SURGE = SERVER_BUILDER.comment("Enables/disables Shrimp Tidal Surge.")
                .define("enableShrimpTidalSurge", true);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Adrenaline Meter");
        ENABLE_ADRENALINE = SERVER_BUILDER.comment("Enables/disables the Adrenaline Meter and Adrenaline Mode.")
                .define("enableAdrenaline", true);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Rage Meter");
        ENABLE_RAGE = SERVER_BUILDER.comment("Enables/disables the Rage Meter and Rage Mode.")
                .define("enableRage", true);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Flight Wings");
        WINGS_FALL_MODE = SERVER_BUILDER.comment("What flight wings do once wing time runs out. GLIDE glides like an elytra. SLOW_FALL slows your fall while you hold jump. NONE does nothing.")
                .defineEnum("wingsFallMode", FlightWings.FallMode.GLIDE);
        SERVER_BUILDER.pop();

        SERVER_SPEC = SERVER_BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SERVER_SPEC) {
        }
    }
}