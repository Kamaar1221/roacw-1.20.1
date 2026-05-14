package net.rev.roacw.registeries;

import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.rev.roacw.ROACW;
import net.rev.roacw.auricteslaarmorset.AuricTeslaArmorItem;
import net.rev.roacw.marsarmorset.MarsArmorItem;
import net.rev.roacw.stasiscurse.StatisCurseItem;

import java.util.Collection;

public class ROACWItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ROACW.MODID);

    public static final Rarity EXO_ENGINEERED = Rarity.create("roacw:exo_engineered", (style) -> style.withColor(16711722));
    public static final Rarity GOD_FORGED = Rarity.create("roacw:god_forged", (style) -> style.withColor(0x6d2dc8));

    public static final RegistryObject<Item> MARS_VISOR = ITEMS.register("mars_visor", () -> new MarsArmorItem
            (ArmorItem.Type.HELMET, ItemPropertiesHelper.equipment().rarity(EXO_ENGINEERED).fireResistant()));
    public static final RegistryObject<Item> MARS_ENGINE = ITEMS.register("mars_engine", () -> new MarsArmorItem
            (ArmorItem.Type.CHESTPLATE, ItemPropertiesHelper.equipment().rarity(EXO_ENGINEERED).fireResistant()));
    public static final RegistryObject<Item> MARS_LEG_GUARDS = ITEMS.register("mars_leg_guards", () -> new MarsArmorItem
            (ArmorItem.Type.LEGGINGS, ItemPropertiesHelper.equipment().rarity(EXO_ENGINEERED).fireResistant()));
    public static final RegistryObject<Item> MARS_BOOSTERS = ITEMS.register("mars_boosters", () -> new MarsArmorItem
            (ArmorItem.Type.BOOTS, ItemPropertiesHelper.equipment().rarity(EXO_ENGINEERED).fireResistant()));

    public static final RegistryObject<Item> AURIC_TESLA_ROYAL_HELM = ITEMS.register("auric_tesla_royal_helm", () -> new AuricTeslaArmorItem
            (ArmorItem.Type.HELMET, ItemPropertiesHelper.equipment().rarity(GOD_FORGED).fireResistant()));
    public static final RegistryObject<Item> AURIC_TESLA_CUIRASS = ITEMS.register("auric_tesla_cuirass", () -> new AuricTeslaArmorItem
            (ArmorItem.Type.CHESTPLATE, ItemPropertiesHelper.equipment().rarity(GOD_FORGED).fireResistant()));
    public static final RegistryObject<Item> AURIC_TESLA_LEGGINGS = ITEMS.register("auric_tesla_leggings", () -> new AuricTeslaArmorItem
            (ArmorItem.Type.LEGGINGS, ItemPropertiesHelper.equipment().rarity(GOD_FORGED).fireResistant()));
    public static final RegistryObject<Item> AURIC_TESLA_GREAVES = ITEMS.register("auric_tesla_greaves", () -> new AuricTeslaArmorItem
            (ArmorItem.Type.BOOTS, ItemPropertiesHelper.equipment().rarity(GOD_FORGED).fireResistant()));

    public static final RegistryObject<Item> RAW_AURIC_CHUNK = ITEMS.register("raw_auric_chunk", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> AURIC_INGOT = ITEMS.register("auric_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHARGED_AURIC_INGOT = ITEMS.register("charged_auric_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FRIED_CHICKEN = ITEMS.register("fried_chicken", () -> new Item(new Item.Properties().food(ROACWFood.FRIED_CHICKEN).rarity(GOD_FORGED)));

    public static final RegistryObject<Item> STATIS_CURSE =
            ITEMS.register("statis_curse",
                    () -> new StatisCurseItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
    public static Collection<RegistryObject<Item>> getROACWItems()
    {
        return ITEMS.getEntries();
    }
}
