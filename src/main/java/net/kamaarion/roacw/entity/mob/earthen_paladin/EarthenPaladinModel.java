package net.kamaarion.roacw.entity.mob.earthen_paladin;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;

public class EarthenPaladinModel extends AbstractSpellCastingMobModel {

    private static final ResourceLocation ANIMATIONS =
            ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/entity/mob/earthen_paladin.animation.json");

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob mob) {
        return AbstractSpellCastingMob.textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(AbstractSpellCastingMob animatable) {
        return ANIMATIONS;
    }
}