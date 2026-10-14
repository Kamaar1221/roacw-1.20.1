package net.kamaarion.roacw.client.shader_and_glowmask;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.kamaarion.roacw.ROACW;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ROACW.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ROACWShaders {

    private ROACWShaders() {}

    public static ShaderInstance emissiveCutoutShader;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(ROACW.MODID, "rendertype_roacw_emissive_cutout"),
                        DefaultVertexFormat.NEW_ENTITY
                ),
                shader -> emissiveCutoutShader = shader
        );
    }
}