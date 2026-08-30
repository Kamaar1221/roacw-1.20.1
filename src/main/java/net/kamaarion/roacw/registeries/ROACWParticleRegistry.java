package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ROACWParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ROACW.MODID);

    public static final RegistryObject<SimpleParticleType> SHADOWFLAME =
            PARTICLE_TYPES.register("shadowflame",
                    () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> AURIC_CHARGE_LIGHTNING =
            PARTICLE_TYPES.register("auric_charge_lightning",
                    () -> new SimpleParticleType(false));


    // NEW
    public static final RegistryObject<SimpleParticleType> PLAGUE_NANO_GREEN =
            PARTICLE_TYPES.register("plague_nano_green",
                    () -> new SimpleParticleType(true));

    // NEW
    public static final RegistryObject<SimpleParticleType> PLAGUE_NANO_RED =
            PARTICLE_TYPES.register("plague_nano_red",
                    () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> PLAGUE_CLOUD =
            PARTICLE_TYPES.register("plague_cloud",
                    () -> new SimpleParticleType(true));

    // NEW - Plaguebringer jet boost booster exhaust
    public static final RegistryObject<SimpleParticleType> BOOSTER_EXHAUST =
            PARTICLE_TYPES.register("booster_exhaust",
                    () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}