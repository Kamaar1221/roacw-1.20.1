package net.kamaarion.roacw.items.curios.wings;

import com.mojang.blaze3d.systems.RenderSystem;
import net.kamaarion.roacw.ClientConfig;
import net.kamaarion.roacw.ROACW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = ROACW.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class FlightWingsHud {

    private static final int SCREEN_BORDER_MARGIN = 20;

    private FlightWingsHud() {
    }

    public static int anchorX(
            FlightWings.MeterAnchor anchor,
            int screenWidth,
            int meterWidth
    ) {
        return switch (anchor) {
            case HOTBAR_LEFT -> screenWidth / 2 - 91 - 55;
            case HOTBAR_RIGHT -> screenWidth / 2 + 91 + 33;
            case CENTER -> screenWidth / 2 - meterWidth / 2;
            case TOP_LEFT, BOTTOM_LEFT -> SCREEN_BORDER_MARGIN;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - SCREEN_BORDER_MARGIN - meterWidth;
        };
    }

    public static int anchorY(
            FlightWings.MeterAnchor anchor,
            int screenHeight,
            int meterHeight
    ) {
        return switch (anchor) {
            case HOTBAR_LEFT, HOTBAR_RIGHT -> screenHeight - 2 - meterHeight;
            case CENTER -> screenHeight / 2 + 16;
            case TOP_LEFT, TOP_RIGHT -> SCREEN_BORDER_MARGIN;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - SCREEN_BORDER_MARGIN - meterHeight;
        };
    }

    public static void draw(
            GuiGraphics graphics,
            int x,
            int y,
            FlightWings.MeterSprite sprite,
            float fraction
    ) {
        int fillHeight = Math.round(
                sprite.fillHeight()
                        * Math.max(0.0F, Math.min(1.0F, fraction))
        );

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        graphics.blit(
                sprite.texture(),
                x,
                y,
                sprite.frameU(),
                sprite.frameV(),
                sprite.frameWidth(),
                sprite.frameHeight(),
                sprite.textureWidth(),
                sprite.textureHeight()
        );

        if (fillHeight > 0) {
            int missing = sprite.fillHeight() - fillHeight;

            graphics.blit(
                    sprite.texture(),
                    x + sprite.fillX(),
                    y + sprite.fillY() + missing,
                    sprite.fillU(),
                    sprite.fillV() + missing,
                    sprite.fillWidth(),
                    fillHeight,
                    sprite.textureWidth(),
                    sprite.textureHeight()
            );
        }

        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("flight_wings_meter", FlightWingsHud::render);
    }

    private static void render(
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int width,
            int height
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null || minecraft.options.hideGui || player.isSpectator()) {
            return;
        }

        FlightWings.MeterDisplay display = ClientConfig.WINGS_METER_DISPLAY.get();

        if (display == FlightWings.MeterDisplay.NEVER) {
            return;
        }

        FlightWings wings = FlightWings.getWorn(player);

        if (wings == null) {
            return;
        }

        int wingTime = FlightWingsClient.getWingTime();

        if (display == FlightWings.MeterDisplay.CONTEXTUAL
                && wingTime >= wings.getMaxWingTime()
                && player.onGround()
                && !player.isFallFlying()) {
            return;
        }

        FlightWings.MeterSprite sprite = wings.getMeterSprite();
        FlightWings.MeterAnchor anchor = ClientConfig.WINGS_METER_ANCHOR.get();

        float fraction = wingTime / (float) wings.getMaxWingTime();

        int x = anchorX(anchor, width, sprite.frameWidth())
                + ClientConfig.WINGS_METER_OFFSET_X.get();

        int y = anchorY(anchor, height, sprite.frameHeight())
                + ClientConfig.WINGS_METER_OFFSET_Y.get();

        draw(graphics, x, y, sprite, fraction);
    }
}