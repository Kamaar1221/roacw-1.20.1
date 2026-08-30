package net.kamaarion.roacw.registeries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.kamaarion.roacw.ROACW;

public class ROACWTags {
    public static final TagKey<Item> CRAFTABLE_WITH_AURIC_HELM = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "craftable_with_auric_helm"));
    public static final TagKey<Item> CRAFTABLE_WITH_AURIC_CHEST = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "craftable_with_auric_chest"));
    public static final TagKey<Item> EXO_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "exo_focus"));


    public static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "god_slayer_inferno_bosses"));
}