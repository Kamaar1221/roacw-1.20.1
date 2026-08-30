package net.kamaarion.roacw.registeries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.kamaarion.roacw.ROACW;

public class RoaCWCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ROACW.MODID);

    public static final RegistryObject<CreativeModeTab> ROACW_ARMOR = CREATIVE_MODE_TAB.register("roacw_armor",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ROACWItemRegistry.AURIC_TESLA_ROYAL_HELM.get()))
                    .title(Component.translatable("creativetab.roacw.armor"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ROACWItemRegistry.MARS_VISOR.get());
                        output.accept(ROACWItemRegistry.MARS_ENGINE.get());
                        output.accept(ROACWItemRegistry.MARS_LEG_GUARDS.get());
                        output.accept(ROACWItemRegistry.MARS_BOOSTERS.get());
                        output.accept(ROACWItemRegistry.AURIC_TESLA_ROYAL_HELM.get());
                        output.accept(ROACWItemRegistry.AURIC_TESLA_CUIRASS.get());
                        output.accept(ROACWItemRegistry.AURIC_TESLA_CUISSES.get());
                        output.accept(ROACWItemRegistry.AURIC_TESLA_BOOTS.get());
                        output.accept(ROACWItemRegistry.FEARMONGER_GREATHELM.get());
                        output.accept(ROACWItemRegistry.FEARMONGER_PLATEMAIL.get());
                        output.accept(ROACWItemRegistry.FEARMONGER_LEGPLATES.get());
                        output.accept(ROACWItemRegistry.FEARMONGER_GREAVES.get());
                        output.accept(ROACWItemRegistry.EARTHEN_PALADIN_HELMET.get());
                        output.accept(ROACWItemRegistry.EARTHEN_PALADIN_CHESTPLATE.get());
                        output.accept(ROACWItemRegistry.EARTHEN_PALADIN_LEGGINGS.get());
                        output.accept(ROACWItemRegistry.EARTHEN_PALADIN_GREAVES.get());
                        output.accept(ROACWItemRegistry.PLAGUEBRINGER_VISOR.get());
                        output.accept(ROACWItemRegistry.PLAGUEBRINGER_FRAME.get());
                        output.accept(ROACWItemRegistry.PLAGUEBRINGER_LEGGINGS.get());
                        output.accept(ROACWItemRegistry.PLAGUEBRINGER_PISTONS.get());

                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> ROACW_EQUIPMENT = CREATIVE_MODE_TAB.register("roacw_equipment",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ROACWItemRegistry.THE_HIVE.get()))
                    .title(Component.translatable("creativetab.roacw.equipment"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ROACWItemRegistry.EARTH_SPLITTER.get());
                        output.accept(ROACWItemRegistry.HIGH_RULER_SWORD.get());
                        output.accept(ROACWItemRegistry.THE_HIVE.get());
                        output.accept(ROACWItemRegistry.BURNING_SKY.get());
                        output.accept(ROACWItemRegistry.CORVID_HARBINGER_STAFF.get());
                        output.accept(ROACWItemRegistry.ENDO_HYDRA_STAFF.get());
                        output.accept(ROACWItemRegistry.BELLADONNA_SPIRIT_STAFF.get());
                        output.accept(ROACWItemRegistry.HIGH_RULER_SHIELD.get());
                        output.accept(ROACWItemRegistry.STATIS_CURSE.get());
                        output.accept(ROACWItemRegistry.EVASION_SCARF.get());
                        output.accept(ROACWItemRegistry.ELEMENTAL_GAUNTLET.get());
                        output.accept(ROACWItemRegistry.ALCHEMICAL_DECANTER.get());
                        output.accept(ROACWItemRegistry.NIGHTMARE_TOME.get());
                        output.accept(ROACWItemRegistry.AURIC_CODEX.get());
                        output.accept(ROACWItemRegistry.EARTHEN_PALADIN_OATH.get());

                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> ROACW_MISC = CREATIVE_MODE_TAB.register("roacw_misc",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ROACWItemRegistry.ASCENDANT_SPIRIT_ESSENCE.get()))
                    .title(Component.translatable("creativetab.roacw.misc"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ROACWBlocks.AURIC_ORE.get());
                        output.accept(ROACWBlocks.DEEPSLATE_AURIC_ORE.get());
                        output.accept(ROACWBlocks.RAW_AURIC_BLOCK.get());
                        output.accept(ROACWBlocks.AURIC_BLOCK.get());
                        output.accept(ROACWItemRegistry.FRIED_CHICKEN.get());
                        output.accept(ROACWItemRegistry.RAW_AURIC_CHUNK.get());
                        output.accept(ROACWItemRegistry.AURIC_INGOT.get());
                        output.accept(ROACWItemRegistry.CHARGED_AURIC_INGOT.get());
                        output.accept(ROACWItemRegistry.ASCENDANT_SPIRIT_ESSENCE.get());
                        output.accept(ROACWItemRegistry.INFECTED_PLATING.get());
                        output.accept(ROACWItemRegistry.PLAGUE_CORE.get());
                        output.accept(ROACWItemRegistry.DIVINE_CHUNK.get());
                        output.accept(ROACWItemRegistry.DIVINE_PEBBLE.get());
                        output.accept(ROACWItemRegistry.PLAGUE_CHARGER_SPAWN_EGG.get());
                        output.accept(ROACWItemRegistry.REMNANTS_OF_A_CRUEL_WORLD_DISC.get());
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> ROACW_WIP = CREATIVE_MODE_TAB.register("roacw_wip",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ROACWItemRegistry.MURASAMA_BLADE.get()))
                    .title(Component.translatable("creativetab.roacw.wip"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ROACWItemRegistry.MURASAMA_BLADE.get());
                        output.accept(ROACWItemRegistry.BURST_SHEATH.get());
                        output.accept(ROACWItemRegistry.PHASESLAYER.get());
                        output.accept(ROACWItemRegistry.PHASEBLADE.get());
                        output.accept(ROACWItemRegistry.ARSENAL_VISOR.get());
                        output.accept(ROACWItemRegistry.ARSENAL_CLOAK.get());
                        output.accept(ROACWItemRegistry.ARSENAL_PANTS.get());
                        output.accept(ROACWItemRegistry.ARSENAL_BOOTS.get());



                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> ROACW_SPELLS = CREATIVE_MODE_TAB.register("roacw_spells",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ROACWItemRegistry.AURIC_CODEX.get()))
                    .title(Component.translatable("creativetab.roacw.spells"))
                    .displayItems((itemDisplayParameters, output) -> {
                        for (var spellEntry : ROACWSpellRegistry.SPELLS.getEntries()) {
                            var spell = spellEntry.get();
                            int maxLevel = spell.getMaxLevel();
                            for (int level = 1; level <= maxLevel; level++) {
                                ItemStack scrollStack = new ItemStack(io.redspace.ironsspellbooks.registries.ItemRegistry.SCROLL.get());
                                io.redspace.ironsspellbooks.api.spells.ISpellContainer.createScrollContainer(spell, level, scrollStack);
                                output.accept(scrollStack);
                            }
                        }
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}