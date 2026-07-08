package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.effects.*;
import net.kamaarion.roacw.effects.ArmorCrunchEffect;
import net.kamaarion.roacw.effects.SanctifiedBedrockEffect;
import net.kamaarion.roacw.effects.PaladinPulseEffect;       // Explicit import for clarity
import net.kamaarion.roacw.effects.PaladinProtectionEffect;  // Explicit import for clarity
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ROACWEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ROACW.MODID);

    public static final RegistryObject<MobEffect> AURIC_CHARGE = MOB_EFFECTS.register("auric_charge", AuricChargeEffect::new);
    public static final RegistryObject<MobEffect> AURIC_EXHAUSTION = MOB_EFFECTS.register("auric_exhaustion", AuricExhaustionEffect::new);
    public static final RegistryObject<MobEffect> SHADOWFLAME = MOB_EFFECTS.register("shadowflame", ShadowflameEffect::new);
    public static final RegistryObject<MobEffect> ELEMENTAL_MIX = MOB_EFFECTS.register("elemental_mix", ElementalMixEffect::new);
    public static final RegistryObject<MobEffect> EVASION_SCARF_BUFF = MOB_EFFECTS.register("evasion_scarf_buff", EvasionScarfBuffEffect::new);
    public static final RegistryObject<MobEffect> SANCTIFIED_BEDROCK = MOB_EFFECTS.register("sanctified_bedrock", SanctifiedBedrockEffect::new);
    public static final RegistryObject<MobEffect> ARMOR_CRUNCH = MOB_EFFECTS.register("armor_crunch", ArmorCrunchEffect::new);

    // NEW EFFECT REGISTRATIONS
    // Used for the caster player (Self-buff: +50% armor/toughness, -30% speed)
    public static final RegistryObject<MobEffect> PALADIN_PULSE = MOB_EFFECTS.register("paladin_pulse", PaladinPulseEffect::new);

    // Used for nearby allied players (Ally-buff: +25% armor/toughness, no speed penalty)
    public static final RegistryObject<MobEffect> PALADIN_PROTECTION = MOB_EFFECTS.register("paladin_protection", PaladinProtectionEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
