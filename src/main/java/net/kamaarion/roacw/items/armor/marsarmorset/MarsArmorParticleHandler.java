package net.kamaarion.roacw.items.armor.marsarmorset;

import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "roacw", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MarsArmorParticleHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && player.level() != null && player.getAbilities().flying) {
            if (hasFullMarsSet(player)) {
                spawnBoosterParticles(player);
            }
        }
    }

    private static void spawnBoosterParticles(Player player) {
        Vec3 pos = player.position();

        float yaw = player.getYRot();
        double radians = Math.toRadians(yaw);

        double sideAngle = radians + (Math.PI / 2);

        double sideX = Math.sin(sideAngle) * 0.18;
        double sideZ = Math.cos(sideAngle) * 0.18;

        double lookX = -Math.sin(radians) * 0.05;
        double lookZ = Math.cos(radians) * 0.05;

        double bootY = pos.y - 0.15;

        double spreadX1 = (Math.random() - 0.5) * 0.08;
        double spreadZ1 = (Math.random() - 0.5) * 0.08;
        double spreadX2 = (Math.random() - 0.5) * 0.08;
        double spreadZ2 = (Math.random() - 0.5) * 0.08;

        player.level().addParticle(ParticleTypes.FLAME,
                pos.x - sideX + lookX + spreadX1, bootY, pos.z + sideZ + lookZ + spreadZ1,
                spreadX1 * 0.4, -0.18, spreadZ1 * 0.4);
        player.level().addParticle(ParticleTypes.SMOKE,
                pos.x - sideX + lookX + spreadX1, bootY, pos.z + sideZ + lookZ + spreadZ1,
                spreadX1 * 0.6, -0.10, spreadZ1 * 0.6);

        player.level().addParticle(ParticleTypes.FLAME,
                pos.x + sideX + lookX + spreadX2, bootY, pos.z - sideZ + lookZ + spreadZ2,
                spreadX2 * 0.4, -0.18, spreadZ2 * 0.4);
        player.level().addParticle(ParticleTypes.SMOKE,
                pos.x + sideX + lookX + spreadX2, bootY, pos.z - sideZ + lookZ + spreadZ2,
                spreadX2 * 0.6, -0.10, spreadZ2 * 0.6);
    }




    private static boolean hasFullMarsSet(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        return helmet.getItem() == ROACWItemRegistry.MARS_VISOR.get() &&
                chest.getItem() == ROACWItemRegistry.MARS_ENGINE.get() &&
                legs.getItem() == ROACWItemRegistry.MARS_LEG_GUARDS.get() &&
                boots.getItem() == ROACWItemRegistry.MARS_BOOSTERS.get();
    }
}
