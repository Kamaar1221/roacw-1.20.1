package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueAoE;
import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueShards;
import net.kamaarion.roacw.entity.spells.impaling_column.ImpalingColumnShards;
import net.kamaarion.roacw.entity.spells.final_rend.FinalRendAoE;
import net.kamaarion.roacw.entity.spells.quick_strike.QuickStrikeAoE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ROACWEntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ROACW.MODID);

    // Quick Strike
    public static final RegistryObject<EntityType<QuickStrikeAoE>> QUICK_STRIKE =
            ENTITIES.register("quick_strike",
                    () -> EntityType.Builder.<QuickStrikeAoE>of(QuickStrikeAoE::new, MobCategory.MISC)
                            .sized(5f, 1f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "quick_strike").toString())
            );

    // Final Rend
    public static final RegistryObject<EntityType<FinalRendAoE>> FINAL_REND =
            ENTITIES.register("final_rend",
                    () -> EntityType.Builder.<FinalRendAoE>of(FinalRendAoE::new, MobCategory.MISC)
                            .sized(12f, 1f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "final_rend").toString())
            );

    // Impaling Column Shards
    public static final RegistryObject<EntityType<ImpalingColumnShards>> IMPALING_COLUMN_SHARDS =
            ENTITIES.register("impaling_column_shards",
                    () -> EntityType.Builder.<ImpalingColumnShards>of(ImpalingColumnShards::new, MobCategory.MISC)
                            .sized(2.0f, 1.5f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "impaling_column_shards").toString())
            );

    // Earthly Virtue Shards
    public static final RegistryObject<EntityType<EarthlyVirtueShards>> EARTHLY_VIRTUE_SHARDS =
            ENTITIES.register("earthly_virtue_shards",
                    () -> EntityType.Builder.<EarthlyVirtueShards>of(EarthlyVirtueShards::new, MobCategory.MISC)
                            .sized(2.0f, 1.5f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "earthly_virtue_shards").toString())
            );

    // Earthly Virtue AoE controller
    public static final RegistryObject<EntityType<EarthlyVirtueAoE>> EARTHLY_VIRTUE_AOE =
            ENTITIES.register("earthly_virtue_aoe",
                    () -> EntityType.Builder.<EarthlyVirtueAoE>of(EarthlyVirtueAoE::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "earthly_virtue_aoe").toString())
            );

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}