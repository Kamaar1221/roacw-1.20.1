package net.kamaarion.roacw.client.firevisuals;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CustomFireRegistry {

    private static final List<CustomFireType> TYPES = new ArrayList<>();

    private CustomFireRegistry() {
    }

    public static void register(CustomFireType type) {
        TYPES.add(type);
    }

    public static List<CustomFireType> all() {
        return Collections.unmodifiableList(TYPES);
    }

    @Nullable
    public static CustomFireType getActive(LivingEntity entity) {
        for (CustomFireType type : TYPES) {
            if (type.appliesTo(entity)) {
                return type;
            }
        }
        return null;
    }
}