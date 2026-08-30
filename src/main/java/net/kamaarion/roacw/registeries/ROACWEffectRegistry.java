package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.effects.*;
import net.kamaarion.roacw.effects.ArmorCrunchEffect;
import net.kamaarion.roacw.effects.SanctifiedBedrockEffect;
import net.kamaarion.roacw.effects.PlagueEffect;              // add this import
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
    public static final RegistryObject<MobEffect> PLAGUE = MOB_EFFECTS.register("plague", PlagueEffect::new);
    public static final RegistryObject<MobEffect> PESTILENCE_CLOAK_BUFF = MOB_EFFECTS.register("pestilence_cloak_buff", PestilenceCloakBuffEffect::new);
    public static final RegistryObject<MobEffect> GOD_SLAYER_INFERNO = MOB_EFFECTS.register("god_slayer_inferno", GodSlayerInfernoEffect::new);


    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}