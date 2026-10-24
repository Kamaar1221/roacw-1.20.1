package net.kamaarion.roacw.registeries;

import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import net.kamaarion.roacw.ROACW;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DataPackRegistryEvent;

import com.mojang.serialization.Codec;

public class ROACWUpgradeOrbTypeRegistry {

    public static final ResourceKey<Registry<UpgradeOrbType>> UPGRADE_ORB_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ROACW.id("upgrade_orb_type"));

    public static final Codec<Holder<UpgradeOrbType>> UPGRADE_ORB_REGISTRY_CODEC =
            RegistryFixedCodec.create(UPGRADE_ORB_REGISTRY_KEY);

    public static final ResourceKey<UpgradeOrbType> EXO_SPELL_POWER =
            ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, ROACW.id("exo_power"));

    public static final ResourceKey<UpgradeOrbType> BRIMSTONE_SPELL_POWER =
            ResourceKey.create(UPGRADE_ORB_REGISTRY_KEY, ROACW.id("brimstone_power"));

    public static Registry<UpgradeOrbType> upgradeTypeRegistry(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(UPGRADE_ORB_REGISTRY_KEY);
    }

    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                UPGRADE_ORB_REGISTRY_KEY,
                UpgradeOrbType.CODEC,
                UpgradeOrbType.CODEC
        );
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ROACWUpgradeOrbTypeRegistry::registerDatapackRegistries);
    }

    public static void bootstrap(BootstapContext<UpgradeOrbType> bootstrap) {
    }
}