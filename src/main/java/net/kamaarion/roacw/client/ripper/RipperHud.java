package net.kamaarion.roacw.client.ripper;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.kamaarion.roacw.ClientConfig;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.client.others.ModKeyMappings;
import net.kamaarion.roacw.items.curios_additions.wings.FlightWings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public class RipperHud {
    private static final ResourceLocation RAGE_METER = texture("rage_meter");
    private static final ResourceLocation RAGE_BAR = texture("rage_bar");
    private static final ResourceLocation ADRENALINE_METER = texture("adrenaline_meter");
    private static final ResourceLocation ADRENALINE_BAR = texture("adrenaline_bar");

    private static final int RAGE_WIDTH = 64;
    private static final int RAGE_HEIGHT = 19;
    private static final int ADRENALINE_WIDTH = 72;
    private static final int ADRENALINE_HEIGHT = 13;

    private static final int FILL_U = 6;
    private static final int FILL_WIDTH = 52;
    private static final int RAGE_FILL_V = 10;
    private static final int RAGE_FILL_HEIGHT = 4;
    private static final int ADRENALINE_FILL_V = 5;
    private static final int ADRENALINE_FILL_HEIGHT = 3;

    private static final int RAGE_EYES_U = 28;
    private static final int RAGE_EYES_V = 5;
    private static final int RAGE_EYES_WIDTH = 8;
    private static final int RAGE_EYES_HEIGHT = 3;

    private static final int SCREEN_BORDER_MARGIN = 20;
    private static final int HOTBAR_INSET = 8;
    private static final int ABOVE_HOTBAR_BOTTOM = 53;

    private static final int RAGE_HINT_COLOR = 0xFFFFB347;
    private static final int ADRENALINE_HINT_COLOR = 0xFF7FE0B0;

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(ROACW.MODID, "textures/gui/" + name + ".png");
    }

    private static int anchorX(RipperMeterAnchor anchor, int screenWidth, int meterWidth) {
        return switch (anchor) {
            case ABOVE_HOTBAR_LEFT -> screenWidth / 2 - 91 + HOTBAR_INSET;
            case ABOVE_HOTBAR_RIGHT -> screenWidth / 2 + 91 - HOTBAR_INSET - meterWidth;
            case ABOVE_HOTBAR_CENTER, CENTER -> screenWidth / 2 - meterWidth / 2;
            case TOP_LEFT, BOTTOM_LEFT -> SCREEN_BORDER_MARGIN;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - SCREEN_BORDER_MARGIN - meterWidth;
        };
    }

    private static int anchorY(RipperMeterAnchor anchor, int screenHeight, int meterHeight) {
        return switch (anchor) {
            case ABOVE_HOTBAR_LEFT, ABOVE_HOTBAR_RIGHT, ABOVE_HOTBAR_CENTER -> screenHeight - ABOVE_HOTBAR_BOTTOM - meterHeight;
            case CENTER -> screenHeight / 2 + 16;
            case TOP_LEFT, TOP_RIGHT -> SCREEN_BORDER_MARGIN;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - SCREEN_BORDER_MARGIN - meterHeight;
        };
    }

    private static boolean shouldDraw(FlightWings.MeterDisplay display, boolean hasValue) {
        if (display == FlightWings.MeterDisplay.NEVER) {
            return false;
        }
        if (display == FlightWings.MeterDisplay.ALWAYS) {
            return true;
        }
        return hasValue;
    }

    public static void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.player.isSpectator()) {
            return;
        }

        boolean showRage = shouldDraw(ClientConfig.RAGE_METER_DISPLAY.get(), ClientRipperData.shouldShowRage());
        boolean showAdrenaline = shouldDraw(ClientConfig.ADRENALINE_METER_DISPLAY.get(), ClientRipperData.shouldShowAdrenaline());
        if (!showRage && !showAdrenaline) {
            return;
        }

        float pulse = 0.5F + 0.5F * Mth.sin((minecraft.player.tickCount + partialTick) * 0.4F);
        Font font = minecraft.font;

        if (showRage) {
            RipperMeterAnchor anchor = ClientConfig.RAGE_METER_ANCHOR.get();
            int x = anchorX(anchor, width, RAGE_WIDTH) + ClientConfig.RAGE_METER_OFFSET_X.get();
            int y = anchorY(anchor, height, RAGE_HEIGHT) + ClientConfig.RAGE_METER_OFFSET_Y.get();
            drawRage(guiGraphics, font, x, y, pulse);
        }
        if (showAdrenaline) {
            RipperMeterAnchor anchor = ClientConfig.ADRENALINE_METER_ANCHOR.get();
            int x = anchorX(anchor, width, ADRENALINE_WIDTH) + ClientConfig.ADRENALINE_METER_OFFSET_X.get();
            int y = anchorY(anchor, height, ADRENALINE_HEIGHT) + ClientConfig.ADRENALINE_METER_OFFSET_Y.get();
            drawAdrenaline(guiGraphics, font, x, y, pulse);
        }
    }

    private static void drawRage(GuiGraphics g, Font font, int x, int y, float pulse) {
        boolean ready = ClientRipperData.isRageReady();
        boolean active = ClientRipperData.isRageActive();
        boolean lit = ready || active;

        g.blit(RAGE_METER, x, y, 0, 0, RAGE_WIDTH, RAGE_HEIGHT, RAGE_WIDTH, RAGE_HEIGHT);

        int filled = fillWidth(ClientRipperData.rageFraction());
        if (filled > 0) {
            g.blit(RAGE_BAR, x + FILL_U, y + RAGE_FILL_V, FILL_U, RAGE_FILL_V,
                    filled, RAGE_FILL_HEIGHT, RAGE_WIDTH, RAGE_HEIGHT);
            if (lit) {
                glow(g, RAGE_BAR, x + FILL_U, y + RAGE_FILL_V, FILL_U, RAGE_FILL_V,
                        filled, RAGE_FILL_HEIGHT, RAGE_WIDTH, RAGE_HEIGHT, 0.45F * pulse);
            }
        }

        if (lit) {
            g.setColor(1.0F, 1.0F, 1.0F, 0.5F + 0.5F * pulse);
            g.blit(RAGE_BAR, x + RAGE_EYES_U, y + RAGE_EYES_V, RAGE_EYES_U, RAGE_EYES_V,
                    RAGE_EYES_WIDTH, RAGE_EYES_HEIGHT, RAGE_WIDTH, RAGE_HEIGHT);
            g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if (ready) {
            g.drawCenteredString(font, keyHint(ModKeyMappings.RAGE_KEY), x + RAGE_WIDTH / 2, y - 10, RAGE_HINT_COLOR);
        }
    }

    private static void drawAdrenaline(GuiGraphics g, Font font, int x, int y, float pulse) {
        boolean ready = ClientRipperData.isAdrenalineReady();
        boolean active = ClientRipperData.isAdrenalineActive();

        g.blit(ADRENALINE_METER, x, y, 0, 0, ADRENALINE_WIDTH, ADRENALINE_HEIGHT, ADRENALINE_WIDTH, ADRENALINE_HEIGHT);

        int filled = fillWidth(ClientRipperData.adrenalineFraction());
        if (filled > 0) {
            g.blit(ADRENALINE_BAR, x + FILL_U, y + ADRENALINE_FILL_V, FILL_U, ADRENALINE_FILL_V,
                    filled, ADRENALINE_FILL_HEIGHT, ADRENALINE_WIDTH, ADRENALINE_HEIGHT);
            if (ready || active) {
                glow(g, ADRENALINE_BAR, x + FILL_U, y + ADRENALINE_FILL_V, FILL_U, ADRENALINE_FILL_V,
                        filled, ADRENALINE_FILL_HEIGHT, ADRENALINE_WIDTH, ADRENALINE_HEIGHT, 0.45F * pulse);
            }
        }

        if (ready) {
            g.drawCenteredString(font, keyHint(ModKeyMappings.ADRENALINE_KEY), x + ADRENALINE_WIDTH / 2, y - 10, ADRENALINE_HINT_COLOR);
        }
    }

    private static int fillWidth(float fraction) {
        return Mth.clamp(Math.round(FILL_WIDTH * fraction), 0, FILL_WIDTH);
    }

    private static void glow(GuiGraphics g, ResourceLocation texture, int x, int y, int u, int v,
                             int w, int h, int textureWidth, int textureHeight, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        g.setColor(1.0F, 1.0F, 1.0F, alpha);
        g.blit(texture, x, y, u, v, w, h, textureWidth, textureHeight);
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }

    private static Component keyHint(KeyMapping key) {
        return Component.literal("[").append(key.getTranslatedKeyMessage()).append("]");
    }
}