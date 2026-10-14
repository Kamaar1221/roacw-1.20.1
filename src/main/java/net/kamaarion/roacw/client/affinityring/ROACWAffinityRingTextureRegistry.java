package net.kamaarion.roacw.client.affinityring;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.registeries.ROACWTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ROACWAffinityRingTextureRegistry {

    private static final Map<TagKey<AbstractSpell>, ResourceLocation> TAG_MODELS =
            new LinkedHashMap<>();

    private ROACWAffinityRingTextureRegistry() {
    }

    public static void register() {
        TAG_MODELS.clear();

        registerTag(
                ROACWTags.PLAGUE_SPELLS,
                "item/affinity_ring_plague"
        );

        registerTag(
                ROACWTags.DRAGONFIRE_SPELLS,
                "item/affinity_ring_dragonfire"
        );

        registerTag(
                ROACWTags.EARTHEN_SPELLS,
                "item/affinity_ring_earthen"
        );
    }

    private static void registerTag(
            TagKey<AbstractSpell> tag,
            String modelPath
    ) {
        ResourceLocation modelLocation =
                ResourceLocation.fromNamespaceAndPath(
                        ROACW.MODID,
                        modelPath
                );

        TAG_MODELS.put(
                tag,
                modelLocation
        );
    }

    public static ResourceLocation getModel(
            AbstractSpell spell
    ) {
        if (spell == null) {
            return null;
        }

        for (Map.Entry<TagKey<AbstractSpell>, ResourceLocation> entry :
                TAG_MODELS.entrySet()) {

            if (SpellRegistry.REGISTRY
                    .get()
                    .tags()
                    .getTag(entry.getKey())
                    .contains(spell)) {

                return entry.getValue();
            }
        }

        return null;
    }

    public static Map<TagKey<AbstractSpell>, ResourceLocation> getRegisteredModels() {
        return Map.copyOf(TAG_MODELS);
    }
}