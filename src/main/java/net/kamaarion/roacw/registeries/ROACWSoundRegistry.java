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

    public static RegistryObject<SoundEvent> AURIC_DASH_CHARGE = registerSoundEvent("auric_dash_charge");

    public static RegistryObject<SoundEvent> AURIC_REVIVE_EXPLOSION = registerSoundEvent("auric_revive_explosion");

    public static RegistryObject<SoundEvent> HIVE_NUKE_LAUNCH = registerSoundEvent("hive_nuke_launch");

    public static RegistryObject<SoundEvent> PLAGUE_NUKE_EXPLOSION = registerSoundEvent("plague_nuke_explosion");

    public static RegistryObject<SoundEvent> PLAGUE_ROCKET_EXPLOSION = registerSoundEvent("plague_rocket_explosion");

    public static RegistryObject<SoundEvent> HIVE_ROCKET_LAUNCH = registerSoundEvent("hive_rocket_launch");

    public static RegistryObject<SoundEvent> HIVE_NUKE_READY = registerSoundEvent("hive_nuke_ready");

    public static RegistryObject<SoundEvent> HIVE_NUKE_CHARGE = registerSoundEvent("hive_nuke_charge");

    public static RegistryObject<SoundEvent> PLAGUEBRINGER_JETS_ACTIVATED = registerSoundEvent("plaguebringer_jets_activated");

    public static RegistryObject<SoundEvent> ROACW_TITLE_INTRO = registerSoundEvent("roacw_title_intro");

    public static RegistryObject<SoundEvent> ROACW_TITLE_LOOP = registerSoundEvent("roacw_title_loop");

    public static RegistryObject<SoundEvent> DARK_RAVEN_IDLE = registerSoundEvent("dark_raven_idle");

    public static RegistryObject<SoundEvent> DARK_RAVEN_ATTACK = registerSoundEvent("dark_raven_attack");

    public static RegistryObject<SoundEvent> DARK_RAVEN_HURT = registerSoundEvent("dark_raven_hurt");

    public static RegistryObject<SoundEvent> DARK_RAVEN_SUMMON = registerSoundEvent("dark_raven_summon");



    public static final RegistryObject<SoundEvent> REMNANTS_OF_A_CRUEL_WORLD_TITLE = registerSoundEvent("remnants_of_a_cruel_world_title");





    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent
                (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, name)));
    }
}
