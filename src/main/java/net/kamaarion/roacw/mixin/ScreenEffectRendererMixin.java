package net.kamaarion.roacw.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kamaarion.roacw.client.firevisuals.CustomFireScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @Inject(method = "renderScreenEffect", at = @At("TAIL"))
    private static void roacw$renderCustomFire(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
        CustomFireScreenRenderer.render(minecraft, poseStack);
    }
}