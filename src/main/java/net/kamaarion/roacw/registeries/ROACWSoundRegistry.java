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

    public static RegistryObject<SoundEvent> DASH_WHOOSH = registerSoundEvent("dash_whoosh");

    public static RegistryObject<SoundEvent> AURIC_DASH_CHARGE = registerSoundEvent("auric_dash_charge");

    public static RegistryObject<SoundEvent> AURIC_REVIVE_EXPLOSION = registerSoundEvent("auric_revive_explosion");

    public static RegistryObject<SoundEvent> HIVE_NUKE_LAUNCH = registerSoundEvent("hive_nuke_launch");

    public static RegistryObject<SoundEvent> PLAGUE_NUKE_EXPLOSION = registerSoundEvent("plague_nuke_explosion");

    public static RegistryObject<SoundEvent> PLAGUE_ROCKET_EXPLOSION = registerSoundEvent("plague_rocket_explosion");

    public static RegistryObject<SoundEvent> HIVE_ROCKET_LAUNCH = registerSoundEvent("hive_rocket_launch");

    public static RegistryObject<SoundEvent> PLAGUE_MISSILE_EXPLOSION = registerSoundEvent("plague_missile_explosion");

    public static RegistryObject<SoundEvent> PLAGUE_MISSILE_LAUNCH = registerSoundEvent("plague_missile_launch");

    public static RegistryObject<SoundEvent> HOMING_PLAGUE_MISSILE_EXPLOSION = registerSoundEvent("homing_plague_missile_explosion");

    public static RegistryObject<SoundEvent> HIVE_NUKE_READY = registerSoundEvent("hive_nuke_ready");

    public static RegistryObject<SoundEvent> HIVE_NUKE_CHARGE = registerSoundEvent("hive_nuke_charge");

    public static RegistryObject<SoundEvent> PLAGUEBRINGER_JETS_ACTIVATED = registerSoundEvent("plaguebringer_jets_activated");

    public static RegistryObject<SoundEvent> ROACW_TITLE_INTRO = registerSoundEvent("roacw_title_intro");

    public static RegistryObject<SoundEvent> ROACW_TITLE_LOOP = registerSoundEvent("roacw_title_loop");

    public static RegistryObject<SoundEvent> DARK_RAVEN_IDLE = registerSoundEvent("dark_raven_idle");

    public static RegistryObject<SoundEvent> DARK_RAVEN_ATTACK = registerSoundEvent("dark_raven_attack");

    public static RegistryObject<SoundEvent> DARK_RAVEN_HURT = registerSoundEvent("dark_raven_hurt");

    public static RegistryObject<SoundEvent> DARK_RAVEN_SUMMON = registerSoundEvent("dark_raven_summon");

    public static RegistryObject<SoundEvent> ENDO_HYDRA_SUMMON = registerSoundEvent("endo_hydra_summon");

    public static RegistryObject<SoundEvent> ENDO_HYDRA_FIRING = registerSoundEvent("endo_hydra_firing");

    public static RegistryObject<SoundEvent> DRAEDON_LAUGH = registerSoundEvent("draedon_laugh");

    public static RegistryObject<SoundEvent> WEAPON_SWING = registerSoundEvent("weapon_swing");

    public static RegistryObject<SoundEvent> MARS_MISSILE_EXPLOSION_1 = registerSoundEvent("mars_missile_explosion_1");

    public static RegistryObject<SoundEvent> MARS_MISSILE_EXPLOSION_2 = registerSoundEvent("mars_missile_explosion_2");

    public static RegistryObject<SoundEvent> MARS_MISSILE_EXPLOSION_3 = registerSoundEvent("mars_missile_explosion_3");

    public static RegistryObject<SoundEvent> ENERGY_CANNON_CHARGE = registerSoundEvent("energy_cannon_charge");

    public static RegistryObject<SoundEvent> ENERGY_CANNON_FIRE = registerSoundEvent("energy_cannon_fire");

    public static RegistryObject<SoundEvent> TYRANT_ROAR = registerSoundEvent("tyrant_roar");

    public static RegistryObject<SoundEvent> PHEONIX_REBIRTH = registerSoundEvent("pheonix_rebirth");

    public static RegistryObject<SoundEvent> VIRILI_CHARGE = registerSoundEvent("virili_charge");

    public static RegistryObject<SoundEvent> VIRILI_SUMMON = registerSoundEvent("virili_summon");

    public static RegistryObject<SoundEvent> VIRILI_PHASE_1 = registerSoundEvent("virili_phase_switch_1");

    public static RegistryObject<SoundEvent> VIRILI_PHASE_2 = registerSoundEvent("virili_phase_switch_2");

    public static RegistryObject<SoundEvent> PLAGUE_ENTITY_SPAWN = registerSoundEvent("plague_entity_spawn");

    public static RegistryObject<SoundEvent> BURNING_METEOR_IMPACT = registerSoundEvent("burning_meteor_impact");

    public static RegistryObject<SoundEvent> BURNING_SKY_CAST = registerSoundEvent("burning_sky_cast");

    public static final RegistryObject<SoundEvent> REMNANTS_OF_A_CRUEL_WORLD_TITLE = registerSoundEvent("remnants_of_a_cruel_world_title");

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent
                (ResourceLocation.fromNamespaceAndPath(ROACW.MODID, name)));
    }
}
