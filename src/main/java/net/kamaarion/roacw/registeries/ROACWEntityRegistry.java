package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.armorset_summon.railgun_cannon.RailgunCannonEntity;
import net.kamaarion.roacw.entity.armorset_summon.unstable_energy_cannon.UnstableEnergyCannonEntity;
import net.kamaarion.roacw.entity.mob.earthen_paladin.EarthenPaladinEntity;
import net.kamaarion.roacw.entity.projectile.belladonna_petal.BelladonnaPetalEntity;
import net.kamaarion.roacw.entity.projectile.endo_hydra_beam.EndoHydraBeamEntity;
import net.kamaarion.roacw.entity.projectile.energy_cannon_beam.EnergyCannonBeamEntity;
import net.kamaarion.roacw.entity.projectile.god_killer_dart.GodKillerDartEntity;
import net.kamaarion.roacw.entity.projectile.homing_plague_missile.HomingPlagueMissileEntity;
import net.kamaarion.roacw.entity.projectile.mars_missile.MarsMissileEntity;
import net.kamaarion.roacw.entity.projectile.plague_bee.PlagueBeeEntity;
import net.kamaarion.roacw.entity.projectile.plague_cloud.PlagueCloudEntity;
import net.kamaarion.roacw.entity.projectile.plague_missile.PlagueMissileEntity;
import net.kamaarion.roacw.entity.projectile.plague_nuke.PlagueNukeEntity;
import net.kamaarion.roacw.entity.projectile.plague_rocket.PlagueRocketEntity;
import net.kamaarion.roacw.entity.spells.burning_meteor.BurningMeteorEntity;
import net.kamaarion.roacw.entity.spells.dread.DreadPulse;
import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueAoE;
import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueShards;
import net.kamaarion.roacw.entity.spells.final_rend.FinalRendAoE;
import net.kamaarion.roacw.entity.spells.impaling_column.ImpalingColumnShards;
import net.kamaarion.roacw.entity.spells.pestilence_cloak.PestilenceCloakCloudEntity;
import net.kamaarion.roacw.entity.spells.quick_strike.QuickStrikeAoE;
import net.kamaarion.roacw.entity.summon.baby_plaguebringer.BabyPlaguebringerEntity;
import net.kamaarion.roacw.entity.summon.belladonna_spirit.BelladonnaSpiritEntity;
import net.kamaarion.roacw.entity.summon.dark_raven.DarkRavenEntity;
import net.kamaarion.roacw.entity.summon.endo_hydra.EndoHydraEntity;
import net.kamaarion.roacw.entity.summon.hydra.HydraBodyEntity;
import net.kamaarion.roacw.entity.summon.hydra.HydraHead;
import net.kamaarion.roacw.entity.summon.mini_plaguebringer.MiniPlaguebringerEntity;
import net.kamaarion.roacw.entity.mob.plague_charger.PlagueChargerEntity;
import net.kamaarion.roacw.entity.summon.virili.ViriliEntity;
import net.kamaarion.roacw.progression.BossGateData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ROACWEntityRegistry {

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ROACW.MODID);

    public static final RegistryObject<EntityType<QuickStrikeAoE>> QUICK_STRIKE =
            ENTITIES.register("quick_strike",
                    () -> EntityType.Builder.<QuickStrikeAoE>of(QuickStrikeAoE::new, MobCategory.MISC)
                            .sized(5f, 1f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "quick_strike").toString())
            );

    public static final RegistryObject<EntityType<FinalRendAoE>> FINAL_REND =
            ENTITIES.register("final_rend",
                    () -> EntityType.Builder.<FinalRendAoE>of(FinalRendAoE::new, MobCategory.MISC)
                            .sized(12f, 1f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "final_rend").toString())
            );

    public static final RegistryObject<EntityType<ImpalingColumnShards>> IMPALING_COLUMN_SHARDS =
            ENTITIES.register("impaling_column_shards",
                    () -> EntityType.Builder.<ImpalingColumnShards>of(ImpalingColumnShards::new, MobCategory.MISC)
                            .sized(2.0f, 1.5f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "impaling_column_shards").toString())
            );

    public static final RegistryObject<EntityType<EarthlyVirtueShards>> EARTHLY_VIRTUE_SHARDS =
            ENTITIES.register("earthly_virtue_shards",
                    () -> EntityType.Builder.<EarthlyVirtueShards>of(EarthlyVirtueShards::new, MobCategory.MISC)
                            .sized(2.0f, 1.5f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "earthly_virtue_shards").toString())
            );

    public static final RegistryObject<EntityType<EarthlyVirtueAoE>> EARTHLY_VIRTUE_AOE =
            ENTITIES.register("earthly_virtue_aoe",
                    () -> EntityType.Builder.<EarthlyVirtueAoE>of(EarthlyVirtueAoE::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "earthly_virtue_aoe").toString())
            );

    public static final RegistryObject<EntityType<PlagueRocketEntity>> PLAGUE_ROCKET =
            ENTITIES.register("plague_rocket",
                    () -> EntityType.Builder.<PlagueRocketEntity>of(PlagueRocketEntity::new, MobCategory.MISC)
                            .sized(0.35f, 0.35f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "plague_rocket").toString())
            );

    public static final RegistryObject<EntityType<PlagueNukeEntity>> PLAGUE_NUKE =
            ENTITIES.register("plague_nuke",
                    () -> EntityType.Builder.<PlagueNukeEntity>of(PlagueNukeEntity::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "plague_nuke").toString())
            );

    public static final RegistryObject<EntityType<PlagueCloudEntity>> PLAGUE_CLOUD =
            ENTITIES.register("plague_cloud",
                    () -> EntityType.Builder.<PlagueCloudEntity>of(PlagueCloudEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "plague_cloud").toString())
            );

    public static final RegistryObject<EntityType<PestilenceCloakCloudEntity>> PESTILENCE_CLOAK_CLOUD =
            ENTITIES.register("pestilence_cloak_cloud",
                    () -> EntityType.Builder.<PestilenceCloakCloudEntity>of(PestilenceCloakCloudEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "pestilence_cloak_cloud").toString())
            );

    public static final RegistryObject<EntityType<BelladonnaSpiritEntity>> BELLADONNA_SPIRIT =
            ENTITIES.register("belladonna_spirit",
                    () -> EntityType.Builder.<BelladonnaSpiritEntity>of(BelladonnaSpiritEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 0.9f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "belladonna_spirit").toString())
            );

    public static final RegistryObject<EntityType<BelladonnaPetalEntity>> BELLADONNA_PETAL =
            ENTITIES.register("belladonna_petal",
                    () -> EntityType.Builder.<BelladonnaPetalEntity>of(BelladonnaPetalEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(20)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "belladonna_petal").toString())
            );

    public static final RegistryObject<EntityType<PlagueChargerEntity>> PLAGUE_CHARGER =
            ENTITIES.register("plague_charger",
                    () -> EntityType.Builder.<PlagueChargerEntity>of(PlagueChargerEntity::new, MobCategory.MONSTER)
                            .sized(0.9f, 1.1f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "plague_charger").toString())
            );

    public static final RegistryObject<EntityType<GodKillerDartEntity>> GOD_KILLER_DART =
            ENTITIES.register("god_killer_dart",
                    () -> EntityType.Builder.<GodKillerDartEntity>of(GodKillerDartEntity::new, MobCategory.MISC)
                            .sized(0.35f, 0.35f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "god_killer_dart").toString())
            );

    public static final RegistryObject<EntityType<BurningMeteorEntity>> BURNING_METEOR =
            ENTITIES.register("burning_meteor",
                    () -> EntityType.Builder.<BurningMeteorEntity>of(BurningMeteorEntity::new, MobCategory.MISC)
                            .sized(0.8f, 0.8f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "burning_meteor").toString())
            );

    public static final RegistryObject<EntityType<DarkRavenEntity>> DARK_RAVEN =
            ENTITIES.register("dark_raven",
                    () -> EntityType.Builder.<DarkRavenEntity>of(DarkRavenEntity::new, MobCategory.MONSTER)
                            .sized(0.7f, 0.7f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "dark_raven").toString())
            );

    public static final RegistryObject<EntityType<EarthenPaladinEntity>> EARTHEN_PALADIN =
            ENTITIES.register("earthen_paladin_knight",
                    () -> EntityType.Builder.<EarthenPaladinEntity>of(EarthenPaladinEntity::new, MobCategory.MONSTER)
                            .sized(0.9f, 2.9f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "earthen_paladin_knight").toString())
            );

    public static final RegistryObject<EntityType<HydraBodyEntity>> HYDRA_BODY =
            ENTITIES.register("hydra_body",
                    () -> EntityType.Builder.<HydraBodyEntity>of(HydraBodyEntity::new, MobCategory.MONSTER)
                            .sized(1.6f, 2.4f)
                            .clientTrackingRange(12)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "hydra_body").toString())
            );

    public static final RegistryObject<EntityType<HydraHead>> HYDRA_HEAD =
            ENTITIES.register("hydra_head",
                    () -> EntityType.Builder.<HydraHead>of(HydraHead::new, MobCategory.MONSTER)
                            .sized(0.8f, 0.8f)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "hydra_head").toString())
            );

    public static final RegistryObject<EntityType<EndoHydraEntity>> ENDO_HYDRA =
            ENTITIES.register("endo_hydra",
                    () -> EntityType.Builder.<EndoHydraEntity>of(EndoHydraEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "endo_hydra").toString())
            );

    public static final RegistryObject<EntityType<EndoHydraBeamEntity>> ENDO_HYDRA_BEAM =
            ENTITIES.register("endo_hydra_beam",
                    () -> EntityType.Builder.<EndoHydraBeamEntity>of(EndoHydraBeamEntity::new, MobCategory.MISC)
                            .sized(20.0f, 1.5f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "endo_hydra_beam").toString())
            );

    public static final RegistryObject<EntityType<EnergyCannonBeamEntity>> ENERGY_CANNON_BEAM =
            ENTITIES.register("energy_cannon_beam",
                    () -> EntityType.Builder.<EnergyCannonBeamEntity>of(EnergyCannonBeamEntity::new, MobCategory.MISC)
                            .sized(20.0f, 1.5f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "energy_cannon_beam").toString())
            );

    public static final RegistryObject<EntityType<MiniPlaguebringerEntity>> MINI_PLAGUEBRINGER =
            ENTITIES.register("mini_plaguebringer",
                    () -> EntityType.Builder.<MiniPlaguebringerEntity>of(MiniPlaguebringerEntity::new, MobCategory.MISC)
                            .sized(0.9f, 1.1f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "mini_plaguebringer").toString())
            );

    public static final RegistryObject<EntityType<PlagueMissileEntity>> PLAGUE_MISSILE =
            ENTITIES.register("plague_missile",
                    () -> EntityType.Builder.<PlagueMissileEntity>of(PlagueMissileEntity::new, MobCategory.MISC)
                            .sized(0.45f, 0.45f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "plague_missile").toString())
            );

    public static final RegistryObject<EntityType<HomingPlagueMissileEntity>> HOMING_PLAGUE_MISSILE =
            ENTITIES.register("homing_plague_missile",
                    () -> EntityType.Builder.<HomingPlagueMissileEntity>of(HomingPlagueMissileEntity::new, MobCategory.MISC)
                            .sized(0.45f, 0.45f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "homing_plague_missile").toString())
            );

    public static final RegistryObject<EntityType<PlagueBeeEntity>> PLAGUE_BEE =
            ENTITIES.register("plague_bee",
                    () -> EntityType.Builder.<PlagueBeeEntity>of(PlagueBeeEntity::new, MobCategory.MISC)
                            .sized(0.6f, 0.6f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "plague_bee").toString())
            );

    public static final RegistryObject<EntityType<ViriliEntity>> VIRILI =
            ENTITIES.register("virili",
                    () -> EntityType.Builder.<ViriliEntity>of(ViriliEntity::new, MobCategory.MONSTER)
                            .sized(0.9f, 1.9f)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "virili").toString())
            );

    public static final RegistryObject<EntityType<BabyPlaguebringerEntity>> BABY_PLAGUEBRINGER =
            ENTITIES.register("baby_plaguebringer",
                    () -> EntityType.Builder.<BabyPlaguebringerEntity>of(BabyPlaguebringerEntity::new, MobCategory.MISC)
                            .sized(0.9f, 1.1f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "baby_plaguebringer").toString())
            );

    public static final RegistryObject<EntityType<RailgunCannonEntity>> RAILGUN_CANNON =
            ENTITIES.register("railgun_cannon",
                    () -> EntityType.Builder.<RailgunCannonEntity>of(RailgunCannonEntity::new, MobCategory.MISC)
                            .sized(0.8f, 0.8f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "railgun_cannon").toString())
            );

    public static final RegistryObject<EntityType<UnstableEnergyCannonEntity>> UNSTABLE_ENERGY_CANNON =
            ENTITIES.register("unstable_energy_cannon",
                    () -> EntityType.Builder.<UnstableEnergyCannonEntity>of(UnstableEnergyCannonEntity::new, MobCategory.MISC)
                            .sized(0.8f, 0.8f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "unstable_energy_cannon").toString())
            );

    public static final RegistryObject<EntityType<MarsMissileEntity>> MARS_MISSILE =
            ENTITIES.register("mars_missile",
                    () -> EntityType.Builder.<MarsMissileEntity>of(MarsMissileEntity::new, MobCategory.MISC)
                            .sized(0.45f, 0.45f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "mars_missile").toString())
            );

    public static final RegistryObject<EntityType<DreadPulse>> DREAD_PULSE =
            ENTITIES.register("dread_pulse",
                    () -> EntityType.Builder.<DreadPulse>of(DreadPulse::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "dread_pulse").toString())
            );

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
        eventBus.addListener(ROACWEntityRegistry::registerSpawnPlacements);
    }

    private static final ResourceLocation HARBINGER_ID =
            ResourceLocation.fromNamespaceAndPath("cataclysm", "the_harbinger");

    private static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
                PLAGUE_CHARGER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) ->
                        level.getDifficulty() != Difficulty.PEACEFUL
                                && BossGateData.hasKilled(level, HARBINGER_ID)
                                && Mob.checkMobSpawnRules(type, level, spawnType, pos, random),
                SpawnPlacementRegisterEvent.Operation.AND
        );

        event.register(
                EARTHEN_PALADIN.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EarthenPaladinEntity::checkEarthenPaladinSpawnRules,
                SpawnPlacementRegisterEvent.Operation.AND
        );
    }
}