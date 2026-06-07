package net.kamaarion.roacw.registeries;

import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.kamaarion.roacw.items.armor.fearmongerarmorset.FearmongerArmorItem;
import net.kamaarion.roacw.items.curios.burst_sheath.BurstSheath;
import net.kamaarion.roacw.items.curios.high_ruler_shield.HighRulerShield;
import net.kamaarion.roacw.items.curios.nightmare_tome.NightmareTome;
import net.kamaarion.roacw.items.weapons.murasama_blade.MurasamaBladeItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.armor.auricteslaarmorset.AuricTeslaArmorItem;
import net.kamaarion.roacw.items.armor.marsarmorset.MarsArmorItem;
import net.kamaarion.roacw.items.curios.elemental_gauntlet.ElementalGauntlet;
import net.kamaarion.roacw.items.curios.evasion_scarf.EvasionScarf;
import net.kamaarion.roacw.items.curios.stasis_curse.StatisCurse;
import java.util.Collection;

public class ROACWItemRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ROACW.MODID);

    public static final Rarity EXO_ENGINEERED = Rarity.create("roacw:exo_engineered", (style) -> style.withColor(16711722));
    public static final Rarity GOD_FORGED = Rarity.create("roacw:god_forged", (style) -> style.withColor(0xFF6D2DC8));
    public static final Rarity OTHERWORLDLY = Rarity.create("roacw:otherworldly", (style) -> style.withColor(0xFFCE84FF));

    // Armor Sets
    public static final RegistryObject<Item> MARS_VISOR = ITEMS.register("mars_visor", () -> new MarsArmorItem(ArmorItem.Type.HELMET, ItemPropertiesHelper.equipment().rarity(EXO_ENGINEERED).fireResistant()));
    public static final RegistryObject<Item> MARS_ENGINE = ITEMS.register("mars_engine", () -> new MarsArmorItem(ArmorItem.Type.CHESTPLATE, ItemPropertiesHelper.equipment().rarity(EXO_ENGINEERED).fireResistant()));
    public static final RegistryObject<Item> MARS_LEG_GUARDS = ITEMS.register("mars_leg_guards", () -> new MarsArmorItem(ArmorItem.Type.LEGGINGS, ItemPropertiesHelper.equipment().rarity(EXO_ENGINEERED).fireResistant()));
    public static final RegistryObject<Item> MARS_BOOSTERS = ITEMS.register("mars_boosters", () -> new MarsArmorItem(ArmorItem.Type.BOOTS, ItemPropertiesHelper.equipment().rarity(EXO_ENGINEERED).fireResistant()));

    public static final RegistryObject<Item> AURIC_TESLA_ROYAL_HELM = ITEMS.register("auric_tesla_royal_helm", () -> new AuricTeslaArmorItem(ArmorItem.Type.HELMET, ItemPropertiesHelper.equipment().rarity(GOD_FORGED).fireResistant()));
    public static final RegistryObject<Item> AURIC_TESLA_CUIRASS = ITEMS.register("auric_tesla_cuirass", () -> new AuricTeslaArmorItem(ArmorItem.Type.CHESTPLATE, ItemPropertiesHelper.equipment().rarity(GOD_FORGED).fireResistant()));
    public static final RegistryObject<Item> AURIC_TESLA_CUISSES = ITEMS.register("auric_tesla_cuisses", () -> new AuricTeslaArmorItem(ArmorItem.Type.LEGGINGS, ItemPropertiesHelper.equipment().rarity(GOD_FORGED).fireResistant()));
    public static final RegistryObject<Item> AURIC_TESLA_BOOTS = ITEMS.register("auric_tesla_boots", () -> new AuricTeslaArmorItem(ArmorItem.Type.BOOTS, ItemPropertiesHelper.equipment().rarity(GOD_FORGED).fireResistant()));

    public static final RegistryObject<Item> FEARMONGER_GREATHELM = ITEMS.register("fearmonger_greathelm", () -> new FearmongerArmorItem(ArmorItem.Type.HELMET, ItemPropertiesHelper.equipment().rarity(OTHERWORLDLY).fireResistant()));
    public static final RegistryObject<Item> FEARMONGER_PLATEMAIL = ITEMS.register("fearmonger_platemail", () -> new FearmongerArmorItem(ArmorItem.Type.CHESTPLATE, ItemPropertiesHelper.equipment().rarity(OTHERWORLDLY).fireResistant()));
    public static final RegistryObject<Item> FEARMONGER_LEGPLATES = ITEMS.register("fearmonger_legplates", () -> new FearmongerArmorItem(ArmorItem.Type.LEGGINGS, ItemPropertiesHelper.equipment().rarity(OTHERWORLDLY).fireResistant()));
    public static final RegistryObject<Item> FEARMONGER_GREAVES = ITEMS.register("fearmonger_greaves", () -> new FearmongerArmorItem(ArmorItem.Type.BOOTS, ItemPropertiesHelper.equipment().rarity(OTHERWORLDLY).fireResistant()));

    // Basic Materials / Consumables
    public static final RegistryObject<Item> RAW_AURIC_CHUNK = ITEMS.register("raw_auric_chunk", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> AURIC_INGOT = ITEMS.register("auric_ingot", () -> new Item(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));
    public static final RegistryObject<Item> CHARGED_AURIC_INGOT = ITEMS.register("charged_auric_ingot", () -> new Item(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));
    public static final RegistryObject<Item> FRIED_CHICKEN = ITEMS.register("fried_chicken", () -> new Item(new Item.Properties().food(ROACWFoodRegistry.FRIED_CHICKEN).rarity(GOD_FORGED)));

    // Curios
    public static final RegistryObject<Item> STATIS_CURSE = ITEMS.register("statis_curse", () -> new StatisCurse(new Item.Properties().rarity(ROACWItemRegistry.OTHERWORLDLY)));
    public static final RegistryObject<Item> EVASION_SCARF = ITEMS.register("evasion_scarf", () -> new EvasionScarf(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ELEMENTAL_GAUNTLET = ITEMS.register("elemental_gauntlet", () -> new ElementalGauntlet(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> BURST_SHEATH = ITEMS.register("burst_sheath", () -> new BurstSheath(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));


    // Weapons
    public static final RegistryObject<Item> MURASAMA_BLADE = ITEMS.register("murasama_blade", () -> new MurasamaBladeItem(new Item.Properties().rarity(ROACWItemRegistry.GOD_FORGED)));

    // Spellbooks
    public static final RegistryObject<Item> NIGHTMARE_TOME = ITEMS.register("nightmare_tome", () -> new NightmareTome(new Item.Properties().rarity(ROACWItemRegistry.OTHERWORLDLY)));

    // High Ruler Shield
    public static final RegistryObject<Item> HIGH_RULER_SHIELD = ITEMS.register("high_ruler_shield", () -> new HighRulerShield(new Item.Properties().rarity(ROACWItemRegistry.OTHERWORLDLY)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static Collection<RegistryObject<Item>> getROACWItems() {
        return ITEMS.getEntries();
    }
}

