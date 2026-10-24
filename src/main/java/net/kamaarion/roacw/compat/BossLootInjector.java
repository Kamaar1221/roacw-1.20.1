package net.kamaarion.roacw.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "roacw")
public class BossLootInjector {
    private static final ResourceLocation DOG_HEAD = ResourceLocation.fromNamespaceAndPath("dog", "entities/devourer_head");
    private static final ResourceLocation DOG_DRAGON = ResourceLocation.fromNamespaceAndPath("dog", "gameplay/dragon_devour");
    private static final ResourceLocation HEAD_INJECT = ResourceLocation.fromNamespaceAndPath("roacw", "inject/dog_devourer");
    private static final ResourceLocation DRAGON_INJECT = ResourceLocation.fromNamespaceAndPath("roacw", "inject/dog_dragon");

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        if (name.equals(DOG_HEAD)) {
            inject(event, HEAD_INJECT);
        } else if (name.equals(DOG_DRAGON)) {
            inject(event, DRAGON_INJECT);
        }
    }

    private static void inject(LootTableLoadEvent event, ResourceLocation table) {
        event.getTable().addPool(
            LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootTableReference.lootTableReference(table))
                .build()
        );
    }
}