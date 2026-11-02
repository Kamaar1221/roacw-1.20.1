package net.kamaarion.roacw.loot;

import net.kamaarion.roacw.ROACW;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ROACWLootConditions {
    public static final DeferredRegister<LootItemConditionType> CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, ROACW.MODID);

    public static final RegistryObject<LootItemConditionType> BOSS_KILLED =
            CONDITIONS.register("boss_killed", () -> new LootItemConditionType(new BossKilledCondition.ConditionSerializer()));

    public static void register(IEventBus eventBus) {
        CONDITIONS.register(eventBus);
    }
}