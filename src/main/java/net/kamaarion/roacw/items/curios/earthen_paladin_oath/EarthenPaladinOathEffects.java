package net.kamaarion.roacw.items.curios.earthen_paladin_oath;

import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.entity.mob.earthen_paladin.EarthenPaladinEntity;
import net.kamaarion.roacw.entity.spells.impaling_column.ImpalingColumnShards;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// findEquippedCurio and setEquippedCurio (used in EarthenPaladinEntity) are
// both deprecated as of Curios 1.20.1 (forRemoval, but not yet removed) in
// favor of ICuriosItemHandler's equivalents - still functional, confirmed
// against the actual ICuriosHelper interface source.
@Mod.EventBusSubscriber(modid = ROACW.MODID)
public class EarthenPaladinOathEffects {

    private static final Map<UUID, Long> PROC_COOLDOWNS = new ConcurrentHashMap<>();
    private static final int PROC_COOLDOWN_TICKS = 20; // 1s, tune to taste

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel serverLevel)) return;

        // The Earthen Paladin Knight wears this same curio (see
        // EarthenPaladinEntity.equipCurios()) purely for its stat bonuses and
        // spell access - the retaliation proc is a player-facing curio
        // ability, not something the boss itself should trigger when hit.
        if (victim instanceof EarthenPaladinEntity) return;

        DamageSource source = event.getSource();
        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker) || attacker == victim) return;

        boolean wearingOath = CuriosApi.getCuriosHelper()
                .findEquippedCurio(ROACWItemRegistry.EARTHEN_PALADIN_OATH.get(), victim)
                .isPresent();
        if (!wearingOath) return;

        long now = victim.level().getGameTime();
        Long lastProc = PROC_COOLDOWNS.get(victim.getUUID());
        if (lastProc != null && now - lastProc < PROC_COOLDOWN_TICKS) return;
        PROC_COOLDOWNS.put(victim.getUUID(), now);

        spawnRetaliationStalagmite(serverLevel, victim, attacker, event.getAmount());
    }

    private static void spawnRetaliationStalagmite(ServerLevel serverLevel, LivingEntity victim, LivingEntity attacker, float incomingDamage) {
        ImpalingColumnShards shard = ROACWEntityRegistry.IMPALING_COLUMN_SHARDS.get().create(serverLevel);
        if (shard == null) return;

        float baseDamage = incomingDamage * 2.0F; // bumped from 0.5F - tune to taste
        float scaledDamage = applySchoolScaling(baseDamage, victim);

        Vec3 pos = attacker.position();
        // Renderer ignores the pitch component entirely - setTilted(false) is
        // the only thing that makes this stand straight up (confirmed against
        // ImpalingColumnShardsRenderer, which gates a hardcoded 35F rotation
        // behind isTilted() and never reads getXRot()).
        shard.moveTo(pos.x, pos.y, pos.z, attacker.getYRot(), 0.0F);
        shard.setTilted(false);
        shard.setOwner(victim); // damage attributed to the wearer, so `target == caster` skip protects the wearer, not the attacker
        shard.setDamage(scaledDamage);
        shard.setSpawnDelay(0);
        shard.setScale(1.2f);
        shard.setApplyKnockup(true);
        shard.setKnockupStrength(1.6D); // stronger than the 0.9D base default

        serverLevel.addFreshEntity(shard);
    }

    // Same holy-power bonus curve as ImpalingColumnSpell.getDamage() (1.0 power
    // = neutral, above/below scales at half rate), plus an equivalent geo-power
    // bonus stacked multiplicatively. Geo reads as a plain vanilla attribute
    // (gtbcs_geomancy_plus's GGAttributes.GEO_SPELL_POWER) rather than through
    // ISS's SchoolRegistry.getPowerFor() like holy does - different API shape
    // between the two addons, not a mistake.
    private static float applySchoolScaling(float baseDamage, LivingEntity wearer) {
        float holyPower = (float) io.redspace.ironsspellbooks.api.registry.SchoolRegistry.HOLY.get().getPowerFor(wearer);
        float holyBonus = 1.0F + ((holyPower - 1.0F) * 0.5F);

        float geoPower = (float) wearer.getAttributeValue(com.gametechbc.gtbcs_geomancy_plus.api.init.GGAttributes.GEO_SPELL_POWER.get());
        float geoBonus = 1.0F + ((geoPower - 1.0F) * 0.5F);

        return baseDamage * holyBonus * geoBonus;
    }
}