package net.kamaarion.roacw.items.armor.marsarmorset;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MarsArmorRenderer extends GeoArmorRenderer<MarsArmorItem> {

    public MarsArmorRenderer(MarsArmorModel marsArmorModel) {
        super(new MarsArmorModel());

        this.addRenderLayer(
                new MarsArmorLayer(this)
        );
    }

    @Override
    public RenderType getRenderType(
            MarsArmorItem animatable,
            ResourceLocation texture,
            MultiBufferSource bufferSource,
            float partialTick
    ) {
        return RenderType.entityTranslucent(
                this.getTextureLocation(animatable)
        );
    }

    @Override
    public void renderToBuffer(
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        if (this.currentSlot == EquipmentSlot.CHEST
                && this.currentEntity instanceof Player wearer
                && this.baseModel != null) {
            MarsArmorParticleHandler.recordJetSample(
                    wearer,
                    poseStack.last().pose(),
                    this.baseModel.body
            );
        }

        super.renderToBuffer(
                poseStack,
                buffer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );
    }

    @Override
    public void renderRecursively(
            PoseStack poseStack,
            MarsArmorItem animatable,
            GeoBone bone,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        if (!isReRender
                && this.currentSlot == EquipmentSlot.CHEST
                && this.currentEntity instanceof Player wearer
                && "core".equals(bone.getName())) {
            MarsArmorParticleHandler.recordCoreSample(
                    wearer,
                    poseStack
            );
        }

        super.renderRecursively(
                poseStack,
                animatable,
                bone,
                renderType,
                bufferSource,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );
    }
}