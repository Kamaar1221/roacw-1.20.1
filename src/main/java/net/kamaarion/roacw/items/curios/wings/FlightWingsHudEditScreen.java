package net.kamaarion.roacw.items.curios.wings;

import net.kamaarion.roacw.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FlightWingsHudEditScreen extends Screen {
    private final Screen parent;
    private FlightWings.MeterAnchor anchor = ClientConfig.WINGS_METER_ANCHOR.get();
    private int offsetX = ClientConfig.WINGS_METER_OFFSET_X.get();
    private int offsetY = ClientConfig.WINGS_METER_OFFSET_Y.get();
    private boolean dragging;
    private int grabX;
    private int grabY;

    public FlightWingsHudEditScreen(Screen parent) {
        super(Component.translatable("screen.roacw.flight_meter_editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.roacw.flight_meter_editor.reset"), button -> {
            anchor = FlightWings.MeterAnchor.HOTBAR_LEFT;
            offsetX = 0;
            offsetY = 0;
        }).bounds(centerX - 105, this.height / 2 + 30, 100, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(centerX + 5, this.height / 2 + 30, 100, 20).build());
    }

    private FlightWings.MeterSprite sprite() {
        Minecraft minecraft = Minecraft.getInstance();
        FlightWings wings = minecraft.player == null ? null : FlightWings.getWorn(minecraft.player);
        return wings != null ? wings.getMeterSprite() : FlightWings.DEFAULT_METER_SPRITE;
    }

    private int meterX() {
        return FlightWingsHud.anchorX(anchor, this.width, sprite().frameWidth()) + offsetX;
    }

    private int meterY() {
        return FlightWingsHud.anchorY(anchor, this.height, sprite().frameHeight()) + offsetY;
    }

    private boolean isOverMeter(double mouseX, double mouseY) {
        FlightWings.MeterSprite sprite = sprite();
        return mouseX >= meterX() && mouseX < meterX() + sprite.frameWidth()
                && mouseY >= meterY() && mouseY < meterY() + sprite.frameHeight();
    }

    private void moveTo(int x, int y) {
        FlightWings.MeterSprite sprite = sprite();
        FlightWings.MeterAnchor nearest = anchor;
        long best = Long.MAX_VALUE;
        for (FlightWings.MeterAnchor candidate : FlightWings.MeterAnchor.values()) {
            long dx = x - FlightWingsHud.anchorX(candidate, this.width, sprite.frameWidth());
            long dy = y - FlightWingsHud.anchorY(candidate, this.height, sprite.frameHeight());
            long distance = dx * dx + dy * dy;
            if (distance < best) {
                best = distance;
                nearest = candidate;
            }
        }
        anchor = nearest;
        offsetX = x - FlightWingsHud.anchorX(anchor, this.width, sprite.frameWidth());
        offsetY = y - FlightWingsHud.anchorY(anchor, this.height, sprite.frameHeight());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.roacw.flight_meter_editor.hint"),
                this.width / 2, this.height / 2 - 15, 0xAAAAAA);

        FlightWings.MeterSprite sprite = sprite();
        FlightWingsHud.draw(graphics, meterX(), meterY(), sprite, 1.0F);
        int outline = dragging || isOverMeter(mouseX, mouseY) ? 0xFFFFFFFF : 0x80FFFFFF;
        graphics.renderOutline(meterX() - 1, meterY() - 1, sprite.frameWidth() + 2, sprite.frameHeight() + 2, outline);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isOverMeter(mouseX, mouseY)) {
            dragging = true;
            grabX = (int) mouseX - meterX();
            grabY = (int) mouseY - meterY();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            FlightWings.MeterSprite sprite = sprite();
            int newX = Math.max(0, Math.min(this.width - sprite.frameWidth(), (int) mouseX - grabX));
            int newY = Math.max(0, Math.min(this.height - sprite.frameHeight(), (int) mouseY - grabY));
            moveTo(newX, newY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        ClientConfig.WINGS_METER_ANCHOR.set(anchor);
        ClientConfig.WINGS_METER_OFFSET_X.set(offsetX);
        ClientConfig.WINGS_METER_OFFSET_Y.set(offsetY);
        ClientConfig.SPEC.save();
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}