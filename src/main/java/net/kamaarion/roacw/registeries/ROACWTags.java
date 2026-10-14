package net.kamaarion.roacw.registeries;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.kamaarion.roacw.ROACW;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class ROACWTags {

    // Item Tags

    public static final TagKey<Item> CRAFTABLE_WITH_AURIC_HELM =
            ItemTags.create(
                    ResourceLocation.fromNamespaceAndPath(
                            ROACW.MODID,
                            "craftable_with_auric_helm"
                    )
            );

    public static final TagKey<Item> CRAFTABLE_WITH_AURIC_CHEST =
            ItemTags.create(
                    ResourceLocation.fromNamespaceAndPath(
                            ROACW.MODID,
                            "craftable_with_auric_chest"
                    )
            );

    public static final TagKey<Item> EXO_FOCUS =
            ItemTags.create(
                    ResourceLocation.fromNamespaceAndPath(
                            ROACW.MODID,
                            "exo_focus"
                    )
            );

    public static final TagKey<Item> BRIMSTONE_FOCUS =
            ItemTags.create(
                    ResourceLocation.fromNamespaceAndPath(
                            ROACW.MODID,
                            "brimstone_focus"
                    )
            );


    // Entity Type Tags

    public static final TagKey<EntityType<?>> BOSSES =
            TagKey.create(
                    Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            ROACW.MODID,
                            "god_slayer_inferno_bosses"
                    )
            );

    public static final TagKey<EntityType<?>> DREAD_IMMUNE_ENTITIES =
            TagKey.create(
                    Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            ROACW.MODID,
                            "dread_immune_entities"
                    )
            );


    // Spell Scroll/Affinity Ring Tags

    public static final TagKey<AbstractSpell> PLAGUE_SPELLS =
            SpellRegistry.REGISTRY.get()
                    .tags()
                    .createTagKey(
                            ResourceLocation.fromNamespaceAndPath(
                                    ROACW.MODID,
                                    "plague_spells"
                            )
                    );


    public static final TagKey<AbstractSpell> DRAGONFIRE_SPELLS =
            SpellRegistry.REGISTRY.get()
                    .tags()
                    .createTagKey(
                            ResourceLocation.fromNamespaceAndPath(
                                    ROACW.MODID,
                                    "dragonfire_spells"
                            )
                    );


    public static final TagKey<AbstractSpell> EARTHEN_SPELLS =
            SpellRegistry.REGISTRY.get()
                    .tags()
                    .createTagKey(
                            ResourceLocation.fromNamespaceAndPath(
                                    ROACW.MODID,
                                    "earthen_spells"
                            )
                    );
}