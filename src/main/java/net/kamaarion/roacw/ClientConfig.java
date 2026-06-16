package net.kamaarion.roacw;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue REMOVE_CAPE;

    static {
        BUILDER.push("Visual Settings");
        REMOVE_CAPE = BUILDER.comment("Set to true to hide the Auric Tesla cape on your client screen.")
                .define("removeCape", false);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(ROACW.MODID)) {
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().level != null) {
                    Minecraft.getInstance().reloadResourcePacks();
                }
            });
        }
    }
}
