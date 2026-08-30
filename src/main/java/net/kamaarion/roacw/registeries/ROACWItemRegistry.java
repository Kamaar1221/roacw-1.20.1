package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.client.others.UntintedSpawnEggItem;
import net.kamaarion.roacw.items.armor.arsenalt1armorset.ArsenalT1ArmorItem;
import net.kamaarion.roacw.items.armor.auricteslaarmorset.AuricTeslaArmorItem;
import net.kamaarion.roacw.items.armor.earthenpaladinset.EarthenPaladinArmorItem;
import net.kamaarion.roacw.items.armor.fearmongerarmorset.FearmongerArmorItem;
import net.kamaarion.roacw.items.armor.marsarmorset.MarsArmorItem;
import net.kamaarion.roacw.items.armor.plaguebringerarmorset.PlaguebringerArmorItem;
import net.kamaarion.roacw.items.curios.alchemical_decanter.AlchemicalDecanter;
import net.kamaarion.roacw.items.curios.auric_codex.AuricCodex;
import net.kamaarion.roacw.items.curios.burst_sheath.BurstSheath;
import net.kamaarion.roacw.items.curios.earthen_paladin_oath.EarthenPaladinOath;
import net.kamaarion.roacw.items.curios.elemental_gauntlet.ElementalGauntlet;
import net.kamaarion.roacw.items.curios.evasion_scarf.EvasionScarf;
import net.kamaarion.roacw.items.curios.high_ruler_shield.HighRulerShield;
import net.kamaarion.roacw.items.curios.nightmare_tome.NightmareTome;
import net.kamaarion.roacw.items.curios.stasis_curse.StatisCurse;
import net.kamaarion.roacw.items.weapons.belladonna_spirit_staff.BelladonnaStaff;
import net.kamaarion.roacw.items.weapons.corvid_harbinger_staff.CorvidHarbinger;
import net.kamaarion.roacw.items.weapons.earth_splitter.EarthSplitter;
import net.kamaarion.roacw.items.weapons.endo_hydra_staff.EndoHydraStaff;
import net.kamaarion.roacw.items.weapons.high_ruler_sword.HighRulerSword;
import net.kamaarion.roacw.items.weapons.murasama_blade.MurasamaBlade;
import net.kamaarion.roacw.items.weapons.phaseblade.Phaseblade;
import net.kamaarion.roacw.items.weapons.phaseslayer.Phaseslayer;
import net.kamaarion.roacw.items.weapons.the_burning_sky.BurningSky;
import net.kamaarion.roacw.items.weapons.the_hive.TheHive;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;

public class ROACWItemRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ROACW.MODID);

    public static final Rarity EXO_ENGINEERED = Rarity.create("roacw:exo_engineered", (style) -> style.withColor(16711722));
    public static final Rarity GOD_FORGED = Rarity.create("roacw:god_forged", (style) -> style.withColor(0xFF6D2DC8));
    public static final Rarity OTHERWORLDLY = Rarity.create("roacw:otherworldly", (style) -> style.withColor(0xFFCE84FF));
    public static final Rarity CALAMITOUS = Rarity.create("roacw:calamitous", (style) -> style.withColor(0xFFFF3636));

    // Armor Sets
    public static final RegistryObject<Item> MARS_VISOR = ITEMS.register("mars_visor", () -> new MarsArmorItem(ArmorItem.Type.HELMET, new Item.Properties().rarity(EXO_ENGINEERED).fireResistant()));
    public static final RegistryObject<Item> MARS_ENGINE = ITEMS.register("mars_engine", () -> new MarsArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(EXO_ENGINEERED).fireResistant()));
    public static final RegistryObject<Item> MARS_LEG_GUARDS = ITEMS.register("mars_leg_guards", () -> new MarsArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(EXO_ENGINEERED).fireResistant()));
    public static final RegistryObject<Item> MARS_BOOSTERS = ITEMS.register("mars_boosters", () -> new MarsArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().rarity(EXO_ENGINEERED).fireResistant()));

    public static final RegistryObject<Item> AURIC_TESLA_ROYAL_HELM = ITEMS.register("auric_tesla_royal_helm", () -> new AuricTeslaArmorItem(ArmorItem.Type.HELMET, new Item.Properties().rarity(GOD_FORGED).fireResistant()));
    public static final RegistryObject<Item> AURIC_TESLA_CUIRASS = ITEMS.register("auric_tesla_cuirass", () -> new AuricTeslaArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(GOD_FORGED).fireResistant()));
    public static final RegistryObject<Item> AURIC_TESLA_CUISSES = ITEMS.register("auric_tesla_cuisses", () -> new AuricTeslaArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(GOD_FORGED).fireResistant()));
    public static final RegistryObject<Item> AURIC_TESLA_BOOTS = ITEMS.register("auric_tesla_boots", () -> new AuricTeslaArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().rarity(GOD_FORGED).fireResistant()));

    public static final RegistryObject<Item> FEARMONGER_GREATHELM = ITEMS.register("fearmonger_greathelm", () -> new FearmongerArmorItem(ArmorItem.Type.HELMET, new Item.Properties().rarity(OTHERWORLDLY).fireResistant()));
    public static final RegistryObject<Item> FEARMONGER_PLATEMAIL = ITEMS.register("fearmonger_platemail", () -> new FearmongerArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(OTHERWORLDLY).fireResistant()));
    public static final RegistryObject<Item> FEARMONGER_LEGPLATES = ITEMS.register("fearmonger_legplates", () -> new FearmongerArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(OTHERWORLDLY).fireResistant()));
    public static final RegistryObject<Item> FEARMONGER_GREAVES = ITEMS.register("fearmonger_greaves", () -> new FearmongerArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().rarity(OTHERWORLDLY).fireResistant()));

    public static final RegistryObject<Item> EARTHEN_PALADIN_HELMET = ITEMS.register("earthen_paladin_helmet", () -> new EarthenPaladinArmorItem(ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> EARTHEN_PALADIN_CHESTPLATE = ITEMS.register("earthen_paladin_chestplate", () -> new EarthenPaladinArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> EARTHEN_PALADIN_LEGGINGS = ITEMS.register("earthen_paladin_leggings", () -> new EarthenPaladinArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> EARTHEN_PALADIN_GREAVES = ITEMS.register("earthen_paladin_greaves", () -> new EarthenPaladinArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> PLAGUEBRINGER_VISOR = ITEMS.register("plaguebringer_visor", () -> new PlaguebringerArmorItem(ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> PLAGUEBRINGER_FRAME = ITEMS.register("plaguebringer_frame", () -> new PlaguebringerArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> PLAGUEBRINGER_LEGGINGS = ITEMS.register("plaguebringer_leggings", () -> new PlaguebringerArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> PLAGUEBRINGER_PISTONS = ITEMS.register("plaguebringer_pistons", () -> new PlaguebringerArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<Item> ARSENAL_VISOR = ITEMS.register("arsenal_visor", () -> new ArsenalT1ArmorItem(ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> ARSENAL_CLOAK = ITEMS.register("arsenal_cloak", () -> new ArsenalT1ArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> ARSENAL_PANTS = ITEMS.register("arsenal_pants", () -> new ArsenalT1ArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> ARSENAL_BOOTS = ITEMS.register("arsenal_boots", () -> new ArsenalT1ArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));



    // Basic Materials
    public static final RegistryObject<Item> RAW_AURIC_CHUNK = ITEMS.register("raw_auric_chunk", () -> new Item(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));
    public static final RegistryObject<Item> AURIC_INGOT = ITEMS.register("auric_ingot", () -> new Item(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));
    public static final RegistryObject<Item> ASCENDANT_SPIRIT_ESSENCE = ITEMS.register("ascendant_spirit_essence", () -> new Item(new Item.Properties().rarity(ROACWItemRegistry.OTHERWORLDLY)));
    public static final RegistryObject<Item> CHARGED_AURIC_INGOT = ITEMS.register("charged_auric_ingot", () -> new Item(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));
    public static final RegistryObject<Item> INFECTED_PLATING = ITEMS.register("infected_plating", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> PLAGUE_CORE = ITEMS.register("plague_core", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> DIVINE_CHUNK = ITEMS.register("divine_chunk", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> DIVINE_PEBBLE = ITEMS.register("divine_pebble", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));


    // Food
    public static final RegistryObject<Item> FRIED_CHICKEN = ITEMS.register("fried_chicken", () -> new Item(new Item.Properties().food(ROACWFoodRegistry.FRIED_CHICKEN).rarity(GOD_FORGED)));

    // Curios
    public static final RegistryObject<Item> STATIS_CURSE = ITEMS.register("statis_curse", () -> new StatisCurse(new Item.Properties().rarity(ROACWItemRegistry.OTHERWORLDLY)));
    public static final RegistryObject<Item> EVASION_SCARF = ITEMS.register("evasion_scarf", () -> new EvasionScarf(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ELEMENTAL_GAUNTLET = ITEMS.register("elemental_gauntlet", () -> new ElementalGauntlet(new Item.Properties().rarity(ROACWItemRegistry.OTHERWORLDLY)));
    public static final RegistryObject<Item> BURST_SHEATH = ITEMS.register("burst_sheath", () -> new BurstSheath(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));
    public static final RegistryObject<Item> HIGH_RULER_SHIELD = ITEMS.register("high_ruler_shield", () -> new HighRulerShield(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ALCHEMICAL_DECANTER = ITEMS.register("alchemical_decanter", () -> new AlchemicalDecanter(new Item.Properties().rarity(Rarity.EPIC)));

    // Weapons
    public static final RegistryObject<Item> MURASAMA_BLADE = ITEMS.register("murasama_blade", () -> new MurasamaBlade(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));
    public static final RegistryObject<Item> EARTH_SPLITTER = ITEMS.register("earth_splitter", () -> new EarthSplitter(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> BURNING_SKY = ITEMS.register("the_burning_sky", () -> new BurningSky(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));
    public static final RegistryObject<Item> THE_HIVE = ITEMS.register("the_hive", () -> new TheHive(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> HIGH_RULER_SWORD = ITEMS.register("high_ruler_sword", () -> new HighRulerSword(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> PHASESLAYER = ITEMS.register("phaseslayer", () -> new Phaseslayer(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));
    public static final RegistryObject<Item> PHASEBLADE = ITEMS.register("phaseblade", () -> new Phaseblade(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));
    public static final RegistryObject<Item> CORVID_HARBINGER_STAFF = ITEMS.register("corvid_harbinger_staff", () -> new CorvidHarbinger(new Item.Properties().rarity(ROACWItemRegistry.OTHERWORLDLY)));
    public static final RegistryObject<Item> ENDO_HYDRA_STAFF = ITEMS.register("endo_hydra_staff", () -> new EndoHydraStaff(new Item.Properties().rarity(ROACWItemRegistry.OTHERWORLDLY)));
    public static final RegistryObject<Item> BELLADONNA_SPIRIT_STAFF = ITEMS.register("belladonna_spirit_staff", () -> new BelladonnaStaff(new Item.Properties().rarity(Rarity.RARE)));




    // Spellbooks
    public static final RegistryObject<Item> NIGHTMARE_TOME = ITEMS.register("nightmare_tome", () -> new NightmareTome(new Item.Properties().rarity(ROACWItemRegistry.OTHERWORLDLY)));
    public static final RegistryObject<Item> AURIC_CODEX = ITEMS.register("auric_codex", () -> new AuricCodex(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));
    public static final RegistryObject<Item> EARTHEN_PALADIN_OATH = ITEMS.register("earthen_paladin_oath", () -> new EarthenPaladinOath(new Item.Properties().rarity(Rarity.EPIC)));

    // Music Disc
    public static final RegistryObject<Item> REMNANTS_OF_A_CRUEL_WORLD_DISC = ITEMS.register("remnants_of_a_cruel_world_disc", () -> new RecordItem(15, ROACWSoundRegistry.REMNANTS_OF_A_CRUEL_WORLD_TITLE,
                    new Item.Properties().stacksTo(1).rarity(ROACWItemRegistry.CALAMITOUS), 4860));

    // Spawn Eggs / Summon Items
    public static final RegistryObject<Item> PLAGUE_CHARGER_SPAWN_EGG = ITEMS.register("plague_charger_spawn_egg",
            () -> new UntintedSpawnEggItem(ROACWEntityRegistry.PLAGUE_CHARGER, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static Collection<RegistryObject<Item>> getROACWItems() {
        return ITEMS.getEntries();
    }
}