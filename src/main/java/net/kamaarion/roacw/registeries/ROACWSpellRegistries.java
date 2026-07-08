package net.kamaarion.roacw.registeries;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.spells.exo.FinalRendSpell;
import net.kamaarion.roacw.spells.exo.QuickStrikeSpell;
import net.kamaarion.roacw.spells.geo.ImpalingColumnSpell;
import net.kamaarion.roacw.spells.holy.EarthlyVirtueSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ROACWSpellRegistries {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, ROACW.MODID);

    public static RegistryObject<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    // Quick Strike
    public static final RegistryObject<AbstractSpell> QUICK_STRIKE = registerSpell(new QuickStrikeSpell());

    // Final Rend
    public static final RegistryObject<AbstractSpell> FINAL_REND = registerSpell(new FinalRendSpell());

    public static final RegistryObject<AbstractSpell> IMPALING_COLUMN = registerSpell(new ImpalingColumnSpell());
    public static final RegistryObject<AbstractSpell> EARTHLY_VIRTUE = registerSpell(new EarthlyVirtueSpell());

    // --- CLEANED SETUP HOOK ---
    // Iron's Spells 1.20.1 automatically handles config and scroll initialization
    // for everything bound to the DeferredRegister. We don't need manual config hooks!
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            System.out.println("[ROACW] Initializing custom spell components successfully.");
        });
    }

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }
}
