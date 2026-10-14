package net.kamaarion.roacw.registeries;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.kamaarion.roacw.ROACW;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static io.redspace.ironsspellbooks.api.registry.SchoolRegistry.SCHOOL_REGISTRY_KEY;

public class ROACWSchoolRegistry {

    private static final DeferredRegister<SchoolType> ROACWSchools =
            DeferredRegister.create(SCHOOL_REGISTRY_KEY, ROACW.MODID);

    public static void register(IEventBus eventBus) {
        ROACWSchools.register(eventBus);
    }

    // EXO

    public static final ResourceLocation EXO_RESOURCE = ROACW.id("exo");

    public static final Supplier<SchoolType> EXO = ROACWSchools.register("exo", () -> new SchoolType(
            EXO_RESOURCE,
            ROACWTags.EXO_FOCUS,
            Component.translatable("school.roacw.exo")
                    .withStyle(Style.EMPTY.withColor(0xCC4723)),
            ROACWAttributeRegistry.EXO_MAGIC_POWER,
            ROACWAttributeRegistry.EXO_MAGIC_RESIST,
            SoundRegistry.EVOCATION_CAST,
            ROACWDamageTypes.EXO_MAGIC
    ));

    // BRIMSTONE

    public static final ResourceLocation BRIMSTONE_RESOURCE = ROACW.id("brimstone");

    public static final Supplier<SchoolType> BRIMSTONE = ROACWSchools.register("brimstone", () -> new SchoolType(
            BRIMSTONE_RESOURCE,
            ROACWTags.BRIMSTONE_FOCUS,
            Component.translatable("school.roacw.brimstone")
                    .withStyle(Style.EMPTY.withColor(0xFF3636)),
            ROACWAttributeRegistry.BRIMSTONE_MAGIC_POWER,
            ROACWAttributeRegistry.BRIMSTONE_MAGIC_RESIST,
            SoundRegistry.EVOCATION_CAST,
            ROACWDamageTypes.BRIMSTONE_MAGIC
    ));
}