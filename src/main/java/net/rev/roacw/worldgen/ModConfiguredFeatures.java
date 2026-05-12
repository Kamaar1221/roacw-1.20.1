package net.rev.roacw.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.rev.roacw.ROACW;
import net.rev.roacw.registeries.ROACWBlocks;

import java.util.List;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_AURIC_ORE_KEY = registerKey("auric_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_RAW_AURIC_BLOCK_KEY = registerKey("raw_auric_block");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceable = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplacables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<OreConfiguration.TargetBlockState> overworldAuricOres = List.of(OreConfiguration.target(stoneReplaceable,
                ROACWBlocks.AURIC_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplacables, ROACWBlocks.DEEPSLATE_AURIC_ORE.get().defaultBlockState()));
        List<OreConfiguration.TargetBlockState> rawAuric = List.of(
                OreConfiguration.target(deepslateReplacables,
                        ROACWBlocks.RAW_AURIC_BLOCK.get().defaultBlockState())
        );

        register(context, OVERWORLD_AURIC_ORE_KEY, Feature.ORE, new OreConfiguration(overworldAuricOres, 4));
        register(context, OVERWORLD_RAW_AURIC_BLOCK_KEY, Feature.ORE, new OreConfiguration(rawAuric, 3));
    }


    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(ROACW.MODID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
