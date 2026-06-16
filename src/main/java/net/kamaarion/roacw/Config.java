package net.kamaarion.roacw;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SERVER_SPEC;

    // --- AURIC TESLA SET ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_AURIC_TESLA_DASH;
    public static final ForgeConfigSpec.BooleanValue ENABLE_AURIC_REVIVE;

    // --- MARS SET ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_MARS_FLIGHT;

    static {
        SERVER_BUILDER.push("Auric Tesla Armor Abilities");
        ENABLE_AURIC_TESLA_DASH = SERVER_BUILDER.comment("Should the Auric Tesla Armor's high-damage directional dash be enabled?")
                .define("enableAuricTeslaDash", true);
        ENABLE_AURIC_REVIVE = SERVER_BUILDER.comment("Should the Auric Tesla Armor's healing explosion revive trigger upon fatal damage be enabled?")
                .define("enableAuricRevive", true);
        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Mars Armor Abilities");
        ENABLE_MARS_FLIGHT = SERVER_BUILDER.comment("Should wearing the full Mars Armor set grant the player creative-style flight capabilities?")
                .define("enableMarsFlight", true);
        SERVER_BUILDER.pop();

        SERVER_SPEC = SERVER_BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SERVER_SPEC) {
            // Automatically handles loading your toggles on startup
        }
    }
}


