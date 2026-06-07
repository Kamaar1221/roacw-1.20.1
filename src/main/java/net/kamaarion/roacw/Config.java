package net.kamaarion.roacw;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue REMOVE_CAPE;

    static {
        BUILDER.push("Armor Settings");
        REMOVE_CAPE = BUILDER.comment("Set to true to use the alternate armor geo file (auric_tesla_armor_capeless.geo.json).")
                .define("removeCape", false);
        BUILDER.pop();
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(ROACW.MODID)) {
            // Safely schedules a resource manager reload on the client thread
            // This cleanly flushes and rebuilds GeckoLib models mid-game without crashing
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().level != null) {
                    Minecraft.getInstance().reloadResourcePacks();
                }
            });
        }
    }
}

