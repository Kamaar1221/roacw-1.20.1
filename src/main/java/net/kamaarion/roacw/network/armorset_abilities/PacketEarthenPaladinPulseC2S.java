package net.kamaarion.roacw.network.armorset_abilities;

import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.kamaarion.roacw.Config;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ROACW.MODID)
public class PacketEarthenPaladinPulseC2S {
    private static final int COOLDOWN_TICKS = 1200;
    private static final int DURATION_TICKS = 300;
    private static final double ALLY_RADIUS = 10.0D;

    private static final UUID SELF_ARMOR_UUID = UUID.fromString("8b2f1a10-1111-4a1a-9a1a-000000000001");
    private static final UUID SELF_TOUGHNESS_UUID = UUID.fromString("8b2f1a10-1111-4a1a-9a1a-000000000002");
    private static final UUID SELF_SPEED_UUID = UUID.fromString("8b2f1a10-1111-4a1a-9a1a-000000000003");
    private static final UUID ALLY_ARMOR_UUID = UUID.fromString("8b2f1a10-2222-4a1a-9a1a-000000000001");
    private static final UUID ALLY_TOUGHNESS_UUID = UUID.fromString("8b2f1a10-2222-4a1a-9a1a-000000000002");

    private static final Map<UUID, Long> SELF_BUFF_EXPIRY = new HashMap<>();
    private static final Map<UUID, Long> ALLY_BUFF_EXPIRY = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWN_TRACKER = new HashMap<>();

    public PacketEarthenPaladinPulseC2S() {
    }

    public PacketEarthenPaladinPulseC2S(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            ROACW.LOGGER.info("[AbilityDebug] Earthen Paladin pulse packet received, sender={}", player);

            if (player != null) {
                ROACW.LOGGER.info(
                        "[AbilityDebug] Earthen Paladin pulse checks: fullSet={} nextAvailableTick={} configEnabled={}",
                        isWearingFullEarthenPaladinSet(player),
                        COOLDOWN_TRACKER.getOrDefault(player.getUUID(), 0L),
                        Config.ENABLE_EARTHEN_PALADIN_PULSE.get()
                );
            }

            if (player == null || !Config.ENABLE_EARTHEN_PALADIN_PULSE.get() || !isWearingFullEarthenPaladinSet(player)) {
                return;
            }

            long currentServerTick = player.level().getGameTime();

            long nextAvailableTick = COOLDOWN_TRACKER.getOrDefault(player.getUUID(), 0L);

            if (currentServerTick < nextAvailableTick) {
                long remainingTicks = nextAvailableTick - currentServerTick;
                long remainingSeconds = (remainingTicks + 19) / 20;

                player.displayClientMessage(
                        Component.literal("§cAbility on cooldown! Wait " + remainingSeconds + "s"),
                        true
                );
                return;
            }

            COOLDOWN_TRACKER.put(player.getUUID(), currentServerTick + COOLDOWN_TICKS);

            applySelfBuff(player);
            SELF_BUFF_EXPIRY.put(player.getUUID(), currentServerTick + DURATION_TICKS);

            player.displayClientMessage(
                    Component.literal("§6Earthen Paladin Protection Active! (+50% Armor)"),
                    true
            );

            AABB area = player.getBoundingBox().inflate(ALLY_RADIUS);
            List<Player> nearby = player.level().getEntitiesOfClass(
                    Player.class,
                    area,
                    ally -> ally != player && !ally.isSpectator()
            );

            for (Player ally : nearby) {
                if (player.getTeam() == null
                        || ally.getTeam() == null
                        || !player.getTeam().isAlliedTo(ally.getTeam())) {
                    continue;
                }

                applyAllyBuff(ally);
                ALLY_BUFF_EXPIRY.put(ally.getUUID(), currentServerTick + DURATION_TICKS);

                ally.displayClientMessage(
                        Component.literal("§eReceived Earthen Paladin Protection! (+25% Armor)"),
                        true
                );
            }

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    MMSounds.EFFECT_GEOMANCY_RUMBLE_1.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.2F
            );

            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                BlastwaveParticleOptions options = new BlastwaveParticleOptions(
                        new Vector3f(0.83F, 0.68F, 0.21F),
                        (float) ALLY_RADIUS
                );

                serverLevel.sendParticles(
                        options,
                        player.getX(),
                        player.getY() + 0.1D,
                        player.getZ(),
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        });

        return true;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();

        if (server == null || server.overworld() == null) {
            return;
        }

        long now = server.overworld().getGameTime();

        SELF_BUFF_EXPIRY.entrySet().removeIf(entry -> {
            if (now < entry.getValue()) {
                return false;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player != null) {
                removeSelfBuff(player);
            }

            return true;
        });

        ALLY_BUFF_EXPIRY.entrySet().removeIf(entry -> {
            if (now < entry.getValue()) {
                return false;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player != null) {
                removeAllyBuff(player);
            }

            return true;
        });
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        COOLDOWN_TRACKER.clear();
    }

    private static void applySelfBuff(Player player) {
        addModifier(player, Attributes.ARMOR, SELF_ARMOR_UUID, "armor_pulse_self_armor", 0.5D);
        addModifier(player, Attributes.ARMOR_TOUGHNESS, SELF_TOUGHNESS_UUID, "armor_pulse_self_toughness", 0.5D);
        addModifier(player, Attributes.MOVEMENT_SPEED, SELF_SPEED_UUID, "armor_pulse_self_speed", -0.3D);
    }

    private static void removeSelfBuff(Player player) {
        removeModifier(player, Attributes.ARMOR, SELF_ARMOR_UUID);
        removeModifier(player, Attributes.ARMOR_TOUGHNESS, SELF_TOUGHNESS_UUID);
        removeModifier(player, Attributes.MOVEMENT_SPEED, SELF_SPEED_UUID);
    }

    private static void applyAllyBuff(Player ally) {
        addModifier(ally, Attributes.ARMOR, ALLY_ARMOR_UUID, "armor_pulse_ally_armor", 0.25D);
        addModifier(ally, Attributes.ARMOR_TOUGHNESS, ALLY_TOUGHNESS_UUID, "armor_pulse_ally_toughness", 0.25D);
    }

    private static void removeAllyBuff(Player ally) {
        removeModifier(ally, Attributes.ARMOR, ALLY_ARMOR_UUID);
        removeModifier(ally, Attributes.ARMOR_TOUGHNESS, ALLY_TOUGHNESS_UUID);
    }

    private static void addModifier(Player player, Attribute attribute, UUID uuid, String name, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        AttributeModifier existingModifier = instance.getModifier(uuid);

        if (existingModifier != null) {
            instance.removeModifier(existingModifier);
        }

        instance.addTransientModifier(
                new AttributeModifier(
                        uuid,
                        name,
                        amount,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );
    }

    private static void removeModifier(Player player, Attribute attribute, UUID uuid) {
        AttributeInstance instance = player.getAttribute(attribute);

        if (instance != null) {
            instance.removeModifier(uuid);
        }
    }

    private static boolean isWearingFullEarthenPaladinSet(ServerPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        return head.is(ROACWItemRegistry.EARTHEN_PALADIN_HELMET.get())
                && chest.is(ROACWItemRegistry.EARTHEN_PALADIN_CHESTPLATE.get())
                && legs.is(ROACWItemRegistry.EARTHEN_PALADIN_LEGGINGS.get())
                && feet.is(ROACWItemRegistry.EARTHEN_PALADIN_GREAVES.get());
    }
}