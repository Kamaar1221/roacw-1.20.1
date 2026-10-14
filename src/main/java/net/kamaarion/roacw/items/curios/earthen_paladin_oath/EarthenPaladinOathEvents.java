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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = ROACW.MODID)
public class EarthenPaladinOathEvents {

    private static final Map<UUID, Long> PROC_COOLDOWNS = new ConcurrentHashMap<>();

    private static final int PROC_COOLDOWN_TICKS = 20;
    private static final double RETALIATION_RADIUS = 5.0D;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();

        if (!(victim.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (victim instanceof EarthenPaladinEntity) {
            return;
        }

        DamageSource source = event.getSource();
        Entity attackerEntity = source.getEntity();

        if (!(attackerEntity instanceof LivingEntity attacker) || attacker == victim) {
            return;
        }

        boolean[] wearingOath = {false};

        CuriosApi.getCuriosInventory(victim).ifPresent(handler ->
                wearingOath[0] = handler.isEquipped(
                        ROACWItemRegistry.EARTHEN_PALADIN_OATH.get()
                )
        );

        if (!wearingOath[0]) {
            return;
        }

        long now = victim.level().getGameTime();
        Long lastProc = PROC_COOLDOWNS.get(victim.getUUID());

        if (lastProc != null && now - lastProc < PROC_COOLDOWN_TICKS) {
            return;
        }

        PROC_COOLDOWNS.put(victim.getUUID(), now);

        float incomingDamage = event.getAmount();

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                victim.getBoundingBox().inflate(RETALIATION_RADIUS),
                target -> target.isAlive()
                        && target != victim
                        && !target.isAlliedTo(victim)
        );

        if (!targets.contains(attacker)) {
            targets.add(attacker);
        }

        for (LivingEntity target : targets) {
            spawnRetaliationStalagmite(
                    serverLevel,
                    victim,
                    target,
                    incomingDamage
            );
        }
    }

    private static void spawnRetaliationStalagmite(
            ServerLevel serverLevel,
            LivingEntity victim,
            LivingEntity target,
            float incomingDamage
    ) {
        ImpalingColumnShards shard =
                ROACWEntityRegistry.IMPALING_COLUMN_SHARDS.get().create(serverLevel);

        if (shard == null) {
            return;
        }

        float baseDamage = incomingDamage * 2.0F;
        float scaledDamage = applySchoolScaling(baseDamage, victim);

        Vec3 pos = target.position();

        shard.moveTo(
                pos.x,
                pos.y,
                pos.z,
                target.getYRot(),
                0.0F
        );

        shard.setTilted(false);
        shard.setOwner(victim);
        shard.setDamage(scaledDamage);
        shard.setSpawnDelay(0);
        shard.setScale(1.2f);
        shard.setApplyKnockup(true);
        shard.setKnockupStrength(0.6D);

        serverLevel.addFreshEntity(shard);
    }

    private static float applySchoolScaling(float baseDamage, LivingEntity wearer) {
        float holyPower = (float) io.redspace.ironsspellbooks.api.registry.SchoolRegistry.HOLY
                .get()
                .getPowerFor(wearer);

        float holyBonus = 1.0F + ((holyPower - 1.0F) * 0.5F);

        float geoPower = (float) wearer.getAttributeValue(
                com.gametechbc.gtbcs_geomancy_plus.api.init.GGAttributes.GEO_SPELL_POWER.get()
        );

        float geoBonus = 1.0F + ((geoPower - 1.0F) * 0.5F);

        return baseDamage * holyBonus * geoBonus;
    }
}