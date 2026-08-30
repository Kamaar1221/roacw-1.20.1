package net.kamaarion.roacw.entity.mob.earthen_paladin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.kamaarion.roacw.client.entity.mob.EarthenPaladinModel;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

/**
 * No overrides needed for spell effects — inherits every spell-related
 * render layer (charge swirl, glowing eyes, spell targeting, etc.) from
 * AbstractSpellCastingMobRenderer for free.
 *
 * SCALE: two prior guesses at GeckoLib's scale hook (LivingEntity#getScale(),
 * then GeoRenderer#getWidthScale/getHeightScale) both turned out wrong for
 * this GeckoLib version - neither exists as an overridable method here.
 * Rather than guess a third time, this wraps the whole render() call in a
 * manual PoseStack scale, which works regardless of GeckoLib's internal API:
 * pushPose/scale before delegating to the real render logic, popPose after.
 * This scales the model, held items, and every render layer uniformly around
 * the entity's own origin, since the PoseStack is already translated to the
 * entity's world position by the time render() is called.
 *
 * PENDING (separate issue): whether armor needs its own render layer here
 * depends on whether HumanoidRenderer's ItemArmorGeoLayer already handles it
 * - confirmed it does, based on the HumanoidRenderer source you shared
 * (ItemArmorGeoLayer maps HELMET/CHESTPLATE/etc. bones to armor slots
 * automatically), so no extra layer needed for that.
 *
 * EARTHEN PALADIN OATH: rendered here as a plain static ItemStack, NOT
 * queried from the Curios inventory. Originally this was going through an
 * actual Curios equip (EarthenPaladinEntity.equipCurios(), now removed) so
 * it could render via the same system as player-worn curios - but that also
 * meant the Oath's attribute bonuses (+holy/geo power, +max mana) and its
 * on-hit retaliation proc (EarthenPaladinOathEffects) were genuinely active
 * on the boss, which wasn't wanted - only the visual was. Decoupling
 * entirely avoids that: this layer always renders the book regardless of any
 * Curios state, and the entity never actually equips it into a curio slot,
 * so there's nothing for the attribute system or the proc's
 * findEquippedCurio() check to find.
 *
 * Attached to the "torso" bone - same bone ISS uses for its own hip-sheathed
 * weapon rendering in HumanoidRenderer, safe to reuse since that logic only
 * fires when shouldSheathSword() is true, which defaults to false and is
 * never overridden for this entity. Position/rotation/scale below are purely
 * cosmetic guesses for a book-at-the-hip look - adjust freely once you see
 * it in-game.
 */
public class EarthenPaladinRenderer extends AbstractSpellCastingMobRenderer {

    private static final float VISUAL_SCALE = 1.5F;

    public EarthenPaladinRenderer(EntityRendererProvider.Context context) {
        super(context, new EarthenPaladinModel());

        addRenderLayer(new BlockAndItemGeoLayer<>(this) {

            @Nullable
            @Override
            protected ItemStack getStackForBone(
                    GeoBone bone,
                    AbstractSpellCastingMob animatable) {

                EarthenPaladinEntity paladin =
                        (EarthenPaladinEntity) animatable;

                // Only render the Oath if this Paladin randomly spawned with it.
                return bone.getName().equals("torso") && paladin.hasOath()
                        ? new ItemStack(ROACWItemRegistry.EARTHEN_PALADIN_OATH.get())
                        : null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(
                    GeoBone bone,
                    ItemStack stack,
                    AbstractSpellCastingMob animatable) {

                return ItemDisplayContext.NONE;
            }

            @Override
            protected void renderStackForBone(
                    PoseStack poseStack,
                    GeoBone bone,
                    ItemStack stack,
                    AbstractSpellCastingMob animatable,
                    MultiBufferSource bufferSource,
                    float partialTick,
                    int packedLight,
                    int packedOverlay) {

                poseStack.pushPose();

                // Y offset pushed further negative (down) - the "torso"
                // bone's pivot turned out to sit up near the neck/head, not
                // chest-center as assumed, so the book was rendering right
                // at face height.
                poseStack.translate(-0.35, -0.475, -0.0);

                poseStack.mulPose(Axis.XP.rotationDegrees(360f));

                poseStack.scale(0.9f, 0.9f, 0.9f);

                super.renderStackForBone(
                        poseStack,
                        bone,
                        stack,
                        animatable,
                        bufferSource,
                        partialTick,
                        packedLight,
                        packedOverlay
                );

                poseStack.popPose();
            }
        });
    }

    @Override
    public void render(
            AbstractSpellCastingMob entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight) {

        poseStack.pushPose();

        poseStack.scale(
                VISUAL_SCALE,
                VISUAL_SCALE,
                VISUAL_SCALE
        );

        super.render(
                entity,
                entityYaw,
                partialTick,
                poseStack,
                bufferSource,
                packedLight
        );

        poseStack.popPose();
    }
}