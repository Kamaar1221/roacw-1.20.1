package net.rev.roacw.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.rev.roacw.ROACW;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> AURIC_ORE_PLACED_KEY = registerKey("auric_ore_placed");
    public static final ResourceKey<PlacedFeature> RAW_AURIC_BLOCK_PLACED_KEY = registerKey("raw_auric_block_placed");

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, AURIC_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_AURIC_ORE_KEY),
                ModOrePlacement.commonOrePlacement(9,
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-25), VerticalAnchor.absolute(50))));
        register(context, RAW_AURIC_BLOCK_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_RAW_AURIC_BLOCK_KEY),
                ModOrePlacement.commonOrePlacement(9,
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-61), VerticalAnchor. absolute(-56))));
    }


    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(ROACW.MODID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
