package net.kamaarion.roacw.items.weapons.the_hive;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class TheHiveRenderer extends GeoItemRenderer<TheHive> {

    public TheHiveRenderer() {
        super(new TheHiveModel());
    }

    @Override
    public void renderByItem(ItemStack stack,
                             ItemDisplayContext transformType,
                             PoseStack poseStack,
                             MultiBufferSource bufferSource,
                             int packedLight,
                             int packedOverlay) {

        boolean isFirstPerson = transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        boolean isLeftHand = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        if (isFirstPerson) {
            // Mirror sign for left hand so offsets push the correct direction
            // instead of clipping into the arm/screen.
            float xSign = isLeftHand ? -1F : 1F;

            if (isLocalPlayerCharging(stack)) {
                poseStack.translate(0.15D * xSign, -0.05D, 0.15D);
                poseStack.mulPose(Axis.XP.rotationDegrees(10.0F));
            } else {
                poseStack.translate(0.2D * xSign, -0.08D, 0.10D);
                poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F));
            }
        }

        super.renderByItem(stack, transformType, poseStack,
                bufferSource, packedLight, packedOverlay);
    }

    /**
     * The item model JSON's display block has no concept of "currently in
     * use" - it only defines static per-context poses - so detecting the
     * charging state has to happen here in Java instead.
     */
    private boolean isLocalPlayerCharging(ItemStack stack) {
        Player player = Minecraft.getInstance().player;
        return player != null
                && player.isUsingItem()
                && player.getUseItem().getItem() == stack.getItem();
    }
}