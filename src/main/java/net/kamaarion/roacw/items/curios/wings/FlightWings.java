package net.kamaarion.roacw.items.curios.wings;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.curios.api.CuriosApi;

public interface FlightWings {
    MeterSprite DEFAULT_METER_SPRITE = new MeterSprite(
            ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "textures/gui/flight_meter_gui.png"), 27, 40,
            0, 0, 23, 40,
            24, 8, 3, 28,
            10, 7);

    double SLOW_FALL_SPEED = 0.30D;

    int getMaxWingTime();

    default MeterSprite getMeterSprite() {
        return DEFAULT_METER_SPRITE;
    }

    default double getMaxRiseSpeed() {
        return 0.30D;
    }

    default double getThrustAccel() {
        return 0.15D;
    }

    default float getHorizontalBoost() {
        return 0.08F;
    }

    static FlightWings getWorn(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof FlightWings))
                .map(result -> (FlightWings) result.stack().getItem())
                .orElse(null);
    }

    enum FallMode {
        GLIDE,
        SLOW_FALL,
        NONE
    }

    enum MeterDisplay {
        NEVER,
        ALWAYS,
        CONTEXTUAL
    }

    enum MeterAnchor {
        HOTBAR_LEFT,
        HOTBAR_RIGHT,
        CENTER,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    record MeterSprite(ResourceLocation texture, int textureWidth, int textureHeight,
                       int frameU, int frameV, int frameWidth, int frameHeight,
                       int fillU, int fillV, int fillWidth, int fillHeight,
                       int fillX, int fillY) {}
}