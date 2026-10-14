package net.kamaarion.roacw.loot;

import com.mojang.serialization.Codec;
import net.kamaarion.roacw.ROACW;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ROACWLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(
                    ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    ROACW.MODID
            );

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ROACW_BOSS_LOOT =
            LOOT_MODIFIER_SERIALIZERS.register(
                    "roacw_boss_loot",
                    () -> ROACWBossLootModifier.CODEC
            );

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ROACW_MINIBOSS_LOOT =
            LOOT_MODIFIER_SERIALIZERS.register(
                    "roacw_miniboss_loot",
                    () -> ROACWMiniBossLootModifier.CODEC
            );

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ROACW_ENEMY_LOOT =
            LOOT_MODIFIER_SERIALIZERS.register(
                    "roacw_enemy_loot",
                    () -> ROACWEnemyLootModifier.CODEC
            );

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ROACW_STRUCTURE_LOOT =
            LOOT_MODIFIER_SERIALIZERS.register(
                    "roacw_structure_loot",
                    () -> ROACWStructureLootModifier.CODEC
            );

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
}