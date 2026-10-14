package net.kamaarion.roacw.client.firevisuals;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class ClientFireEffectCache {

    private static final Map<Integer, Set<ResourceLocation>> ACTIVE = new HashMap<>();

    private ClientFireEffectCache() {
    }

    public static void set(int entityId, ResourceLocation effectId, boolean active) {
        if (active) {
            ACTIVE.computeIfAbsent(entityId, k -> new HashSet<>()).add(effectId);
        } else {
            Set<ResourceLocation> set = ACTIVE.get(entityId);
            if (set != null) {
                set.remove(effectId);
                if (set.isEmpty()) {
                    ACTIVE.remove(entityId);
                }
            }
        }
    }

    public static boolean has(int entityId, ResourceLocation effectId) {
        Set<ResourceLocation> set = ACTIVE.get(entityId);
        return set != null && set.contains(effectId);
    }

    public static void clear(int entityId) {
        ACTIVE.remove(entityId);
    }
}