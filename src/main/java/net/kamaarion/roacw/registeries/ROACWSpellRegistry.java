package net.kamaarion.roacw.registeries;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.spells.exo.FinalRendSpell;
import net.kamaarion.roacw.spells.exo.QuickStrikeSpell;
import net.kamaarion.roacw.spells.fire.BurningMeteorSpell;
import net.kamaarion.roacw.spells.fire.DreadSpell;
import net.kamaarion.roacw.spells.fire.SummonDarkRavenSpell;
import net.kamaarion.roacw.spells.geo.ImpalingColumnSpell;
import net.kamaarion.roacw.spells.holy.EarthlyVirtueSpell;
import net.kamaarion.roacw.spells.ice.SummonEndoHydraSpell;
import net.kamaarion.roacw.spells.nature.PestilenceCloakSpell;
import net.kamaarion.roacw.spells.nature.SummonBelladonnaSpiritSpell;
import net.kamaarion.roacw.spells.technomancy.SummonMiniPlaguebringerSpell;
import net.kamaarion.roacw.spells.technomancy.SummonViriliSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ROACWSpellRegistry {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, ROACW.MODID);

    public static RegistryObject<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    public static final RegistryObject<AbstractSpell> QUICK_STRIKE = registerSpell(new QuickStrikeSpell());

    public static final RegistryObject<AbstractSpell> FINAL_REND = registerSpell(new FinalRendSpell());

    public static final RegistryObject<AbstractSpell> IMPALING_COLUMN = registerSpell(new ImpalingColumnSpell());

    public static final RegistryObject<AbstractSpell> EARTHLY_VIRTUE = registerSpell(new EarthlyVirtueSpell());

    public static final RegistryObject<AbstractSpell> PESTILENCE_CLOAK = registerSpell(new PestilenceCloakSpell());

    public static final RegistryObject<AbstractSpell> SUMMON_BELLADONNA_SPIRIT = registerSpell(new SummonBelladonnaSpiritSpell());

    public static final RegistryObject<AbstractSpell> SUMMON_DARK_RAVEN = registerSpell(new SummonDarkRavenSpell());

    public static final RegistryObject<AbstractSpell> BURNING_SKY = registerSpell(new BurningMeteorSpell());

    public static final RegistryObject<AbstractSpell> SUMMON_ENDO_HYDRA = registerSpell(new SummonEndoHydraSpell());

    public static final RegistryObject<AbstractSpell> SUMMON_MINI_PLAGUEBRINGER = registerSpell(new SummonMiniPlaguebringerSpell());

    public static final RegistryObject<AbstractSpell> SUMMON_VIRILI = registerSpell(new SummonViriliSpell());

    //public static final RegistryObject<AbstractSpell> DREAD = registerSpell(new DreadSpell());


    /*public static final RegistryObject<AbstractSpell> SUMMON_HYDRA = registerSpell(new SummonHydraSpell());*/




    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            System.out.println("[ROACW] Initializing custom spell components successfully.");
        });
    }

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }
}