package net.kamaarion.roacw.ripper;

import com.gametechbc.spelllib.init.GSLAttributeRegistry;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.kamaarion.roacw.Config;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.network.ModMessages;
import net.kamaarion.roacw.network.misc.PacketRipperStateS2C;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RipperManager {
    public static final int RAGE_MAX = 1000;
    public static final int ADRENALINE_MAX = 1000;
    public static final int RAGE_DURATION = 300;
    public static final int ADRENALINE_DURATION = 300;

    public static final float ADRENALINE_GAIN = 3.0F;
    public static final float ADRENALINE_DECAY = 0.5F;
    public static final int ADRENALINE_PENALTY_DURATION = 400;
    public static final float ADRENALINE_PENALTY_FLOOR = 0.1F;
    public static final int ADRENALINE_PENALTY_SOUND_COOLDOWN = 20;
    public static final int COMBAT_TIMEOUT = 200;

    public static final float RAGE_GAIN = 1.5F;
    public static final float RAGE_DECAY = 2.0F;
    public static final int RAGE_DECAY_DELAY = 100;
    public static final int BOSS_COMBAT_TIMEOUT = 200;

    public static final double BOSS_RANGE = 48.0D;
    public static final double ENGAGE_RANGE = 32.0D;
    public static final int SCAN_INTERVAL = 10;
    public static final int SYNC_INTERVAL = 2;

    public static final double ADRENALINE_MOVE_SPEED = 0.3D;
    public static final double ADRENALINE_MANA_REGEN = 1.0D;
    public static final double ADRENALINE_CAST_TIME_REDUCTION = 0.3D;
    public static final float ADRENALINE_HEALTH_REGEN = 1.0F;
    public static final int ADRENALINE_HEALTH_REGEN_INTERVAL = 10;
    public static final float ADRENALINE_DAMAGE_REDUCTION = 0.3F;

    public static final double RAGE_ATTACK_SPEED = 0.3D;
    public static final double RAGE_ATTACK_DAMAGE = 0.3D;
    public static final double RAGE_SPELL_POWER = 0.3D;
    public static final double RAGE_CRIT_CHANCE = 0.25D;
    public static final double RAGE_SPELL_CRIT_CHANCE = 0.25D;

    public static final float READY_SOUND_VOLUME = 0.8F;
    public static final float ACTIVATE_SOUND_VOLUME = 1.0F;
    public static final float END_SOUND_VOLUME = 0.8F;
    public static final float PENALTY_SOUND_VOLUME = 0.8F;

    private static final UUID ADRENALINE_SPEED_ID = UUID.fromString("5b0e8f3a-6c1d-4a52-9f0b-1d7a3c8e4b21");
    private static final UUID ADRENALINE_MANA_REGEN_ID = UUID.fromString("a2c4d6e8-1f3b-4c5d-8e7f-9a0b1c2d3e4f");
    private static final UUID ADRENALINE_CAST_TIME_ID = UUID.fromString("c7d9e1f3-2a4b-4d6c-b8e0-3f5a7b9c1d2e");
    private static final UUID RAGE_ATTACK_SPEED_ID = UUID.fromString("e3f5a7b9-4c6d-4e8f-a0b2-5d7e9f1a3b4c");
    private static final UUID RAGE_ATTACK_DAMAGE_ID = UUID.fromString("19283746-5a6b-4c7d-8e9f-0a1b2c3d4e5f");
    private static final UUID RAGE_SPELL_POWER_ID = UUID.fromString("f0e1d2c3-b4a5-4968-8778-695a4b3c2d1e");
    private static final UUID RAGE_KNOCKBACK_ID = UUID.fromString("7a8b9c0d-1e2f-4a3b-9c4d-5e6f7a8b9c0d");
    private static final UUID RAGE_CRIT_CHANCE_ID = UUID.fromString("3c5e7a9b-2d4f-4b6a-8c0e-1f3a5b7c9d2e");
    private static final UUID RAGE_SPELL_CRIT_CHANCE_ID = UUID.fromString("8d1f3b5a-7c9e-4e2a-a4c6-0b2d4f6a8c1e");

    private static final TagKey<EntityType<?>> RAGE_BOSS_TAG =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(ROACW.MODID, "rage_bosses"));

    private static final Map<UUID, RipperData> DATA = new ConcurrentHashMap<>();

    private RipperManager() {
    }

    public static RipperData get(ServerPlayer player) {
        return DATA.computeIfAbsent(player.getUUID(), id -> new RipperData());
    }

    public static boolean isRageActive(ServerPlayer player) {
        RipperData data = DATA.get(player.getUUID());
        return data != null && data.isRageActive();
    }

    private static boolean isAdrenalineReady(RipperData data) {
        return data.adrenaline >= ADRENALINE_MAX && !data.isAdrenalineActive();
    }

    private static boolean isRageReady(RipperData data) {
        return data.rage >= RAGE_MAX && !data.isRageActive();
    }

    public static void remove(ServerPlayer player) {
        applyModifiers(player, false, false);
        DATA.remove(player.getUUID());
    }

    public static void sync(ServerPlayer player) {
        RipperData data = get(player);
        ModMessages.sendToPlayer(new PacketRipperStateS2C(
                Math.round(data.rage), Math.round(data.adrenaline),
                data.rageActiveTicks, data.adrenalineActiveTicks), player);
        data.dirty = false;
    }

    private static void playPersonal(ServerPlayer player, SoundEvent sound, float volume) {
        player.playNotifySound(sound, SoundSource.PLAYERS, volume, 1.0F);
    }

    private static void playNearby(ServerPlayer player, SoundEvent sound, float volume) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, volume, 1.0F);
    }

    public static void tick(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) {
            return;
        }
        RipperData data = get(player);
        boolean adrenalineEnabled = Config.ENABLE_ADRENALINE.get();
        boolean rageEnabled = Config.ENABLE_RAGE.get();

        int rageBefore = Math.round(data.rage);
        int adrenalineBefore = Math.round(data.adrenaline);
        int rageActiveBefore = data.rageActiveTicks;
        int adrenalineActiveBefore = data.adrenalineActiveTicks;
        boolean rageReadyBefore = isRageReady(data);
        boolean adrenalineReadyBefore = isAdrenalineReady(data);

        if ((adrenalineEnabled || rageEnabled) && player.tickCount % SCAN_INTERVAL == 0) {
            scan(player, data);
        }

        if (data.combatTicks > 0) {
            data.combatTicks--;
        }
        if (data.bossCombatTicks > 0) {
            data.bossCombatTicks--;
        }
        data.ticksSinceHit = Math.min(data.ticksSinceHit + 1, ADRENALINE_PENALTY_DURATION);

        if (adrenalineEnabled) {
            tickAdrenaline(data);
        } else {
            data.adrenaline = 0.0F;
            data.adrenalineActiveTicks = 0;
        }
        if (rageEnabled) {
            tickRage(data);
        } else {
            data.rage = 0.0F;
            data.rageActiveTicks = 0;
            data.rageIdleTicks = 0;
        }
        applyModifiers(player, data.isAdrenalineActive(), data.isRageActive());

        if (!adrenalineReadyBefore && isAdrenalineReady(data)) {
            playPersonal(player, ROACWSoundRegistry.ADRENALINE_READY.get(), READY_SOUND_VOLUME);
        }

        if (rageActiveBefore > 0 && !data.isRageActive()) {
            playPersonal(player, ROACWSoundRegistry.RAGE_END.get(), END_SOUND_VOLUME);
        } else if (!rageReadyBefore && isRageReady(data)) {
            playPersonal(player, ROACWSoundRegistry.RAGE_READY.get(), READY_SOUND_VOLUME);
        }

        if (data.isAdrenalineActive()
                && player.tickCount % ADRENALINE_HEALTH_REGEN_INTERVAL == 0
                && player.getHealth() < player.getMaxHealth()) {
            player.heal(ADRENALINE_HEALTH_REGEN);
        }

        if (rageBefore != Math.round(data.rage) || adrenalineBefore != Math.round(data.adrenaline)
                || rageActiveBefore != data.rageActiveTicks || adrenalineActiveBefore != data.adrenalineActiveTicks) {
            data.dirty = true;
        }
        if (data.dirty && player.tickCount % SYNC_INTERVAL == 0) {
            sync(player);
        }
    }

    private static void scan(ServerPlayer player, RipperData data) {
        boolean boss = false;
        boolean bossEngaged = false;
        boolean hostileEngaged = false;
        double engageRangeSq = ENGAGE_RANGE * ENGAGE_RANGE;
        AABB area = player.getBoundingBox().inflate(BOSS_RANGE);
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
            boolean targetingPlayer = entity instanceof Mob mob && mob.getTarget() == player && !entity.isAlliedTo(player);
            if (entity.getType().is(RAGE_BOSS_TAG)) {
                boss = true;
                if (targetingPlayer) {
                    bossEngaged = true;
                }
            }
            if (targetingPlayer && player.distanceToSqr(entity) <= engageRangeSq) {
                hostileEngaged = true;
            }
        }
        data.bossNearby = boss;
        data.bossEngaged = bossEngaged;
        data.hostileEngaged = hostileEngaged || bossEngaged;
    }

    private static void tickAdrenaline(RipperData data) {
        if (data.isAdrenalineActive()) {
            data.adrenalineActiveTicks--;
            data.adrenaline = (float) ADRENALINE_MAX * data.adrenalineActiveTicks / ADRENALINE_DURATION;
            return;
        }
        boolean inCombat = data.combatTicks > 0 || data.hostileEngaged;
        if (inCombat) {
            float recovery = (float) data.ticksSinceHit / ADRENALINE_PENALTY_DURATION;
            float factor = ADRENALINE_PENALTY_FLOOR + (1.0F - ADRENALINE_PENALTY_FLOOR) * recovery;
            data.adrenaline = Math.min(ADRENALINE_MAX, data.adrenaline + ADRENALINE_GAIN * factor);
        } else if (data.adrenaline > 0.0F) {
            data.adrenaline = Math.max(0.0F, data.adrenaline - ADRENALINE_DECAY);
        }
    }

    private static void tickRage(RipperData data) {
        if (data.isRageActive()) {
            data.rageActiveTicks--;
            data.rage = (float) RAGE_MAX * data.rageActiveTicks / RAGE_DURATION;
            return;
        }
        boolean bossCombat = data.bossNearby && (data.bossEngaged || data.bossCombatTicks > 0);
        if (bossCombat) {
            data.rageIdleTicks = 0;
            data.rage = Math.min(RAGE_MAX, data.rage + RAGE_GAIN);
        } else {
            data.rageIdleTicks++;
            if (data.rageIdleTicks > RAGE_DECAY_DELAY && data.rage > 0.0F) {
                data.rage = Math.max(0.0F, data.rage - RAGE_DECAY);
            }
        }
    }

    private static void applyModifiers(ServerPlayer player, boolean adrenaline, boolean rage) {
        setModifier(player, Attributes.MOVEMENT_SPEED, ADRENALINE_SPEED_ID, "roacw_adrenaline_speed",
                ADRENALINE_MOVE_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL, adrenaline);
        setModifier(player, AttributeRegistry.MANA_REGEN.get(), ADRENALINE_MANA_REGEN_ID, "roacw_adrenaline_mana_regen",
                ADRENALINE_MANA_REGEN, AttributeModifier.Operation.MULTIPLY_BASE, adrenaline);
        setModifier(player, AttributeRegistry.CAST_TIME_REDUCTION.get(), ADRENALINE_CAST_TIME_ID, "roacw_adrenaline_cast_time",
                ADRENALINE_CAST_TIME_REDUCTION, AttributeModifier.Operation.MULTIPLY_BASE, adrenaline);
        setModifier(player, Attributes.ATTACK_SPEED, RAGE_ATTACK_SPEED_ID, "roacw_rage_attack_speed",
                RAGE_ATTACK_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL, rage);
        setModifier(player, Attributes.ATTACK_DAMAGE, RAGE_ATTACK_DAMAGE_ID, "roacw_rage_attack_damage",
                RAGE_ATTACK_DAMAGE, AttributeModifier.Operation.MULTIPLY_TOTAL, rage);
        setModifier(player, AttributeRegistry.SPELL_POWER.get(), RAGE_SPELL_POWER_ID, "roacw_rage_spell_power",
                RAGE_SPELL_POWER, AttributeModifier.Operation.MULTIPLY_BASE, rage);
        setModifier(player, ALObjects.Attributes.CRIT_CHANCE.get(), RAGE_CRIT_CHANCE_ID, "roacw_rage_crit_chance",
                RAGE_CRIT_CHANCE, AttributeModifier.Operation.ADDITION, rage);
        setModifier(player, GSLAttributeRegistry.SPELL_CRIT_CHANCE.get(), RAGE_SPELL_CRIT_CHANCE_ID, "roacw_rage_spell_crit_chance",
                RAGE_SPELL_CRIT_CHANCE, AttributeModifier.Operation.ADDITION, rage);
        setModifier(player, Attributes.KNOCKBACK_RESISTANCE, RAGE_KNOCKBACK_ID, "roacw_rage_knockback",
                1.0D, AttributeModifier.Operation.ADDITION, rage);
    }

    private static void setModifier(ServerPlayer player, Attribute attribute, UUID id, String name,
                                    double amount, AttributeModifier.Operation operation, boolean enabled) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier existing = instance.getModifier(id);
        if (!enabled) {
            if (existing != null) {
                instance.removeModifier(id);
            }
            return;
        }
        if (existing == null) {
            instance.addTransientModifier(new AttributeModifier(id, name, amount, operation));
        }
    }

    public static void activate(ServerPlayer player, boolean adrenaline) {
        if (!player.isAlive() || player.isSpectator()) {
            return;
        }
        RipperData data = get(player);
        if (adrenaline) {
            if (!Config.ENABLE_ADRENALINE.get()) {
                return;
            }
            if (isAdrenalineReady(data)) {
                data.adrenalineActiveTicks = ADRENALINE_DURATION;
                playNearby(player, ROACWSoundRegistry.ADRENALINE_ACTIVATE.get(), ACTIVATE_SOUND_VOLUME);
            }
        } else {
            if (!Config.ENABLE_RAGE.get()) {
                return;
            }
            if (isRageReady(data)) {
                data.rageActiveTicks = RAGE_DURATION;
                playNearby(player, ROACWSoundRegistry.RAGE_ACTIVATE.get(), ACTIVATE_SOUND_VOLUME);
            }
        }
        sync(player);
    }

    public static float onPlayerAttack(ServerPlayer attacker, LivingEntity victim, DamageSource source, float amount) {
        if (victim == attacker || victim.isAlliedTo(attacker)) {
            return amount;
        }
        RipperData data = get(attacker);
        data.combatTicks = COMBAT_TIMEOUT;
        if (victim.getType().is(RAGE_BOSS_TAG)) {
            data.bossCombatTicks = BOSS_COMBAT_TIMEOUT;
        }
        return amount;
    }

    public static float onPlayerHurt(ServerPlayer victim, DamageSource source, float amount) {
        RipperData data = get(victim);
        if (data.isAdrenalineActive()) {
            amount *= 1.0F - ADRENALINE_DAMAGE_REDUCTION;
        } else if (amount > 0.0F
                && Config.ENABLE_ADRENALINE.get()
                && data.ticksSinceHit >= ADRENALINE_PENALTY_SOUND_COOLDOWN) {
            playPersonal(victim, ROACWSoundRegistry.ADRENALINE_PENALTY.get(), PENALTY_SOUND_VOLUME);
        }
        data.ticksSinceHit = 0;
        Entity attacker = source.getEntity();
        if (attacker != null) {
            data.combatTicks = COMBAT_TIMEOUT;
            if (attacker.getType().is(RAGE_BOSS_TAG)) {
                data.bossCombatTicks = BOSS_COMBAT_TIMEOUT;
            }
        }
        data.dirty = true;
        return amount;
    }
}