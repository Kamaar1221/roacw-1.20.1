package net.kamaarion.roacw.registeries;

import net.kamaarion.roacw.ROACW;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class ROACWDamageTypes {

    public static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(
                Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(ROACW.MODID, name)
        );
    }


    // =========================================================
    // Magic
    // =========================================================

    public static final ResourceKey<DamageType> EXO_MAGIC =
            register("exo_magic");

    public static final ResourceKey<DamageType> BRIMSTONE_MAGIC =
            register("brimstone_magic");


    // =========================================================
    // Bootstrap
    // =========================================================

    public static void bootstrap(BootstapContext<DamageType> context) {

        context.register(
                EXO_MAGIC,
                new DamageType(
                        EXO_MAGIC.location().getPath(),
                        DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                        0F
                )
        );

        context.register(
                BRIMSTONE_MAGIC,
                new DamageType(
                        BRIMSTONE_MAGIC.location().getPath(),
                        DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                        0F
                )
        );
    }
}