package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.effects.AuricChargeEffect;
import net.kamaarion.roacw.effects.AuricExhaustionEffect;
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

    public static void register(IEventBus eventBus)
    {
        MOB_EFFECTS.register(eventBus);
    }
}
