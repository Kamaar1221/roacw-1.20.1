package net.kamaarion.roacw.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

public class LootBagItem extends Item {
    private final ResourceLocation lootTable;
    private final ParticleOptions particle;

    public LootBagItem(Item.Properties properties, ResourceLocation lootTable) {
        this(properties, lootTable, ParticleTypes.HAPPY_VILLAGER);
    }

    public LootBagItem(Item.Properties properties, ResourceLocation lootTable, ParticleOptions particle) {
        super(properties);
        this.lootTable = lootTable;
        this.particle = particle;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            LootTable table = serverLevel.getServer().getLootData().getLootTable(this.lootTable);
            LootParams params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, player.damageSources().playerAttack(player))
                    .withOptionalParameter(LootContextParams.KILLER_ENTITY, player)
                    .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, player)
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.ENTITY);

            for (ItemStack reward : table.getRandomItems(params)) {
                player.drop(reward, true);
            }

            spawnRing(serverLevel, player);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (!player.isCreative()) {
            held.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    private void spawnRing(ServerLevel level, Player player) {
        int count = 8;
        double radius = 0.55D;
        double height = 0.5D;
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0D / count) * i;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            level.sendParticles(this.particle, x, player.getY() + height, z, 1, 0.0D, 0.1D, 0.0D, 0.02D);
        }
    }
}