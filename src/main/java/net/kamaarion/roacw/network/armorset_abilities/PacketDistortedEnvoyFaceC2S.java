package net.kamaarion.roacw.network.armorset_abilities;

import net.kamaarion.roacw.items.armor.distortedenvoyarmorset.DistortedEnvoyArmorItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketDistortedEnvoyFaceC2S {

    public PacketDistortedEnvoyFaceC2S() {
    }

    public PacketDistortedEnvoyFaceC2S(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();

        if (player == null) {
            return;
        }

        List<ItemStack> pieces = new ArrayList<>();

        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof DistortedEnvoyArmorItem) {
                pieces.add(stack);
            }
        }

        if (pieces.isEmpty()) {
            return;
        }

        int count = DistortedEnvoyArmorItem.VARIANT_MAW + 1;
        int next = (DistortedEnvoyArmorItem.getVariant(pieces.get(0)) + 1) % count;

        long gameTime = player.level().getGameTime();

        for (ItemStack stack : pieces) {
            DistortedEnvoyArmorItem.setVariant(stack, next, gameTime);
        }
    }
}