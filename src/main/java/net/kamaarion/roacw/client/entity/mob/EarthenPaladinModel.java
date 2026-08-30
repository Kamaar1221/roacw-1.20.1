package net.kamaarion.roacw.client.entity.mob;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;

/**
 * Deliberately does NOT override getModelResource(): stays pointed at ISS's
 * shared abstract_casting_mob.geo.json (see AbstractSpellCastingMob
 * .modelResource). Same rig/bone names, so no reason to duplicate it.
 *
 * ALSO doesn't override getTextureResource() with a custom skin - falls back
 * to AbstractSpellCastingMob.textureResource (ISS's own default), since the
 * full 4-piece Earthen Paladin armor set (equipped in
 * EarthenPaladinEntity.populateDefaultEquipmentSlots) covers head, torso+arms
 * (via HumanoidRenderer's RIGHT_SLEEVE/LEFT_SLEEVE -> chestplate mapping),
 * legs, and feet. Almost nothing of the base skin is actually visible, so
 * there's no need to block on authoring a dedicated texture for it.
 *
 * DOES override getAnimationResource(), pointing at our own file - a
 * trimmed set of exactly 3 clips: the 2 chosen melee swings plus
 * "overhead_two_handed_swing", confirmed (via SpellAnimations.java) to be
 * the literal clip name behind ImpalingColumnSpell's
 * OVERHEAD_MELEE_SWING_ANIMATION.
 */
public class EarthenPaladinModel extends AbstractSpellCastingMobModel {

    private static final ResourceLocation ANIMATIONS =
            ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "animations/entity/earthen_paladin.animation.json");

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob mob) {
        return AbstractSpellCastingMob.textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(AbstractSpellCastingMob animatable) {
        return ANIMATIONS;
    }
}