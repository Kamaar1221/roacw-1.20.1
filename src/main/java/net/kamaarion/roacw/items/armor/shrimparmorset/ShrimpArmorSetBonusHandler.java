package net.kamaarion.roacw.items.armor.shrimparmorset;

import net.kamaarion.roacw.Config;
import net.kamaarion.roacw.ROACW;
import net.kamaarion.roacw.items.armor.ROACWArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = ROACW.MODID)
public class ShrimpArmorSetBonusHandler {

    private static final UUID ATTACK_DAMAGE_ID = UUID.fromString("6f1c2a54-3b7e-4d9a-8c15-a0e7b2d43f01");
    private static final UUID ATTACK_SPEED_ID = UUID.fromString("9b2d7e18-5a43-4c06-b1f9-d38c6a50e702");
    private static final UUID SWIM_SPEED_ID = UUID.fromString("c47a0e93-8d21-4f5b-a6c8-1e92f7b3d503");
    private static final UUID WATER_ARMOR_ID = UUID.fromString("e18b5d06-2f74-4a39-9c80-7d4a1b6e9f04");

    private static final double ATTACK_DAMAGE_BONUS = 0.25;
    private static final double ATTACK_SPEED_BONUS = 0.25;
    private static final double SWIM_SPEED_BONUS = 0.5;
    private static final double WATER_ARMOR_BONUS = 0.2;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        boolean active = Config.ENABLE_SHRIMP_SET_BONUS.get() && hasFullSet(player);
        boolean inWater = active && player.isInWater();

        sync(player.getAttribute(Attributes.ATTACK_DAMAGE), ATTACK_DAMAGE_ID, "Shrimp armor attack damage", ATTACK_DAMAGE_BONUS, active);
        sync(player.getAttribute(Attributes.ATTACK_SPEED), ATTACK_SPEED_ID, "Shrimp armor attack speed", ATTACK_SPEED_BONUS, active);
        sync(player.getAttribute(ForgeMod.SWIM_SPEED.get()), SWIM_SPEED_ID, "Shrimp armor swim speed", SWIM_SPEED_BONUS, active);
        sync(player.getAttribute(Attributes.ARMOR), WATER_ARMOR_ID, "Shrimp armor water armor", WATER_ARMOR_BONUS, inWater);
    }

    public static boolean hasFullSet(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof ArmorItem armor) || armor.getMaterial() != ROACWArmorMaterials.SHRIMP_ARMOR) {
                return false;
            }
        }
        return true;
    }

    private static void sync(AttributeInstance instance, UUID id, String name, double amount, boolean active) {
        if (instance == null) {
            return;
        }
        AttributeModifier existing = instance.getModifier(id);
        if (active) {
            if (existing == null) {
                instance.addTransientModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        } else if (existing != null) {
            instance.removeModifier(id);
        }
    }
}