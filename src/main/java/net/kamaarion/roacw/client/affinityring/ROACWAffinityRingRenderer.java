package net.kamaarion.roacw.client.affinityring;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.render.AffinityRingRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ROACWAffinityRingRenderer extends BlockEntityWithoutLevelRenderer {

    private final ItemRenderer renderer;

    public ROACWAffinityRingRenderer(
            ItemRenderer renderer,
            EntityModelSet entityModelSet
    ) {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                entityModelSet
        );

        this.renderer = renderer;
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();

        poseStack.translate(
                0.5F,
                0.5F,
                0.5F
        );

        BakedModel model = getModel(stack);

        if (displayContext == ItemDisplayContext.GUI) {
            Lighting.setupForFlatItems();

            renderer.render(
                    stack,
                    displayContext,
                    false,
                    poseStack,
                    bufferSource,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    model
            );

            Minecraft.getInstance()
                    .renderBuffers()
                    .bufferSource()
                    .endBatch();

            Lighting.setupFor3DItems();
        } else {
            boolean leftHand =
                    displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                            || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

            renderer.render(
                    stack,
                    displayContext,
                    leftHand,
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay,
                    model
            );
        }

        poseStack.popPose();
    }

    private BakedModel getModel(ItemStack stack) {
        BakedModel defaultModel =
                renderer
                        .getItemModelShaper()
                        .getModelManager()
                        .getModel(
                                ResourceLocation.fromNamespaceAndPath(
                                        "irons_spellbooks",
                                        "item/affinity_ring_evocation"
                                )
                        );

        if (!AffinityData.hasAffinityData(stack)) {
            return defaultModel;
        }

        AffinityData affinityData =
                AffinityData.getAffinityData(stack);

        ResourceLocation spellId =
                affinityData.affinityData()
                        .keySet()
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (spellId == null) {
            return defaultModel;
        }

        AbstractSpell spell =
                SpellRegistry.getSpell(spellId);

        if (spell == null) {
            return defaultModel;
        }

        ResourceLocation customModel =
                ROACWAffinityRingTextureRegistry.getModel(spell);

        if (customModel != null) {
            BakedModel bakedCustomModel =
                    renderer
                            .getItemModelShaper()
                            .getModelManager()
                            .getModel(customModel);

            BakedModel missingModel =
                    renderer
                            .getItemModelShaper()
                            .getModelManager()
                            .getMissingModel();

            if (bakedCustomModel != missingModel) {
                return bakedCustomModel;
            }
        }

        SchoolType school =
                spell.getSchoolType();

        if (school != null) {
            ResourceLocation schoolModel =
                    AffinityRingRenderer.getAffinityRingModelLocation(school);

            return renderer
                    .getItemModelShaper()
                    .getModelManager()
                    .getModel(schoolModel);
        }

        return defaultModel;
    }
}