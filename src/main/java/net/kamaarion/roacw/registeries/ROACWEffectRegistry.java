package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.effects.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ROACWEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ROACW.MODID);

    public static final RegistryObject<MobEffect> AURIC_CHARGE =
            MOB_EFFECTS.register("auric_charge", AuricChargeEffect::new);

    public static final RegistryObject<MobEffect> AURIC_EXHAUSTION =
            MOB_EFFECTS.register("auric_exhaustion", AuricExhaustionEffect::new);

    public static final RegistryObject<MobEffect> SHADOWFLAME =
            MOB_EFFECTS.register("shadowflame", ShadowflameEffect::new);

    public static final RegistryObject<MobEffect> ELEMENTAL_MIX =
            MOB_EFFECTS.register("elemental_mix", ElementalMixEffect::new);

    public static final RegistryObject<MobEffect> EVASION_SCARF_BUFF =
            MOB_EFFECTS.register("evasion_scarf_buff", EvasionScarfBuffEffect::new);

    public static void register(IEventBus eventBus)
    {
        MOB_EFFECTS.register(eventBus);
    }
}
