package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ROACWSoundRegistry {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ROACW.MODID);

    public static RegistryObject<SoundEvent> ELECTRIC_SWORD_SWING = registerSoundEvent("electric_sword_swing");
    // Cleanly added your dash sound event right below your existing sound
    public static RegistryObject<SoundEvent> DASH_WHOOSH = registerSoundEvent("dash_whoosh");

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent
                (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, name)));
    }
}
