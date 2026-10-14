package net.kamaarion.roacw.entity.mob.earthen_paladin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

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