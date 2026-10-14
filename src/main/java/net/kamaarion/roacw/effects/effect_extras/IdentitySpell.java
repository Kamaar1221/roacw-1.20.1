package net.kamaarion.roacw.effects.effect_extras;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public final class IdentitySpell extends AbstractSpell {

    private final ResourceLocation resource;
    private final Supplier<SchoolType> schoolType;
    private final String deathMessageId;

    public IdentitySpell(ResourceLocation resource, Supplier<SchoolType> schoolType, String deathMessageId) {
        this.resource = resource;
        this.schoolType = schoolType;
        this.deathMessageId = deathMessageId;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return resource;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public SchoolType getSchoolType() {
        return schoolType.get();
    }

    @Override
    public String getDeathMessageId() {
        return deathMessageId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {

        throw new UnsupportedOperationException(getSpellId() + " is an identity spell and is never cast or configured.");
    }
}