package net.kamaarion.roacw.items.armor.plaguebringerarmorset;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.network.PacketPlaguebringerJetBoostStateS2C;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ROACW.MODID, value = Dist.CLIENT)
public class PlaguebringerJetpackController {

    // ===== Movement tuning =====

    private static final double ASCEND_SPEED = 0.035D;
    private static final double DESCEND_SPEED = 0.030D;

    private static final double FORWARD_ACCEL = 0.035D;
    private static final double STRAFE_ACCEL = 0.025D;

    private static final double MAX_HORIZONTAL_SPEED = 1.0D;


    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null)
            return;

        LocalPlayer player = mc.player;

        boolean active = PacketPlaguebringerJetBoostStateS2C.isJetBoostActive(player.getUUID());

        if (!active)
            return;

        Vec3 velocity = player.getDeltaMovement();


        if (mc.options.keyJump.isDown()) {

            velocity = velocity.add(0, ASCEND_SPEED, 0);

        }
        else if (mc.options.keyShift.isDown()) {

            velocity = velocity.add(0, -DESCEND_SPEED, 0);

        }
        else {

            velocity = new Vec3(
                    velocity.x,
                    velocity.y * 0.90D,
                    velocity.z
            );

            if (Math.abs(velocity.y) < 0.005D) {
                velocity = new Vec3(
                        velocity.x,
                        0,
                        velocity.z
                );
            }
        }

        //
        // Horizontal movement
        //

        Vec3 look = player.getLookAngle();

        Vec3 forward = new Vec3(
                look.x,
                0,
                look.z
        ).normalize();

        Vec3 right = new Vec3(
                -forward.z,
                0,
                forward.x
        );

        if (mc.options.keyUp.isDown()) {
            velocity = velocity.add(forward.scale(FORWARD_ACCEL));
        }

        if (mc.options.keyDown.isDown()) {
            velocity = velocity.subtract(forward.scale(FORWARD_ACCEL));
        }

        if (mc.options.keyLeft.isDown()) {
            velocity = velocity.subtract(right.scale(STRAFE_ACCEL));
        }

        if (mc.options.keyRight.isDown()) {
            velocity = velocity.add(right.scale(STRAFE_ACCEL));
        }

        //
        // Speed cap
        //

        Vec3 horizontal = new Vec3(
                velocity.x,
                0,
                velocity.z
        );

        if (horizontal.length() > MAX_HORIZONTAL_SPEED) {

            horizontal = horizontal.normalize().scale(MAX_HORIZONTAL_SPEED);

            velocity = new Vec3(
                    horizontal.x,
                    velocity.y,
                    horizontal.z
            );
        }

        player.setDeltaMovement(velocity);
        player.fallDistance = 0;
    }
}