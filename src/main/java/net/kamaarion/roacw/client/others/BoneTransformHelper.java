package net.kamaarion.roacw.client.others;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.GeoBone;

public final class BoneTransformHelper {

    private BoneTransformHelper() {
    }

    public static Vec3 getWorldPosition(GeoBone bone) {

        if (bone == null) {
            return Vec3.ZERO;
        }

        Vector3d position = bone.getWorldPosition();

        if (position == null) {
            return Vec3.ZERO;
        }

        return new Vec3(
                position.x,
                position.y,
                position.z
        );
    }
}