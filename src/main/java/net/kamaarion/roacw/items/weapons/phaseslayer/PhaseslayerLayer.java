package net.kamaarion.roacw.items.weapons.phaseslayer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.kamaarion.roacw.client.shader_and_glowmask.ROACWShaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import java.lang.reflect.Method;

public class PhaseslayerLayer extends AutoGlowingGeoLayer<Phaseslayer> {

    private static final Method GET_EMISSIVE_RESOURCE;

    static {
        Method method = null;

        try {
            method = AutoGlowingTexture.class.getDeclaredMethod(
                    "getEmissiveResource",
                    ResourceLocation.class
            );

            method.setAccessible(true);
        } catch (Exception ignored) {
        }

        GET_EMISSIVE_RESOURCE = method;
    }

    public PhaseslayerLayer(GeoRenderer<Phaseslayer> renderer) {
        super(renderer);
    }

    private static RenderType createPhaseslayerEmissive(
            ResourceLocation texture
    ) {
        RenderType.CompositeState state =
                RenderType.CompositeState.builder()
                        .setTextureState(
                                new RenderStateShard.TextureStateShard(
                                        texture,
                                        false,
                                        false
                                )
                        )
                        .setShaderState(
                                PhaseslayerRenderType.EMISSIVE_SHADER
                        )
                        .setCullState(
                                PhaseslayerRenderType.NO_CULL
                        )
                        .setLightmapState(
                                PhaseslayerRenderType.LIGHTMAP_STATE
                        )
                        .setOverlayState(
                                PhaseslayerRenderType.OVERLAY
                        )
                        .setWriteMaskState(
                                PhaseslayerRenderType.COLOR_WRITE
                        )
                        .createCompositeState(false);

        return RenderType.create(
                "roacw_phaseslayer_emissive",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                state
        );
    }

    @Override
    protected RenderType getRenderType(Phaseslayer animatable) {
        if (GET_EMISSIVE_RESOURCE == null) {
            return null;
        }

        try {
            ResourceLocation baseTexture =
                    getTextureResource(animatable);

            ResourceLocation emissiveTexture =
                    (ResourceLocation) GET_EMISSIVE_RESOURCE.invoke(
                            null,
                            baseTexture
                    );

            if (emissiveTexture == null) {
                return null;
            }

            return createPhaseslayerEmissive(
                    emissiveTexture
            );

        } catch (Exception ignored) {
            return null;
        }
    }

    private static class PhaseslayerRenderType extends RenderType {

        private PhaseslayerRenderType() {
            super(
                    "roacw_phaseslayer_render_type",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    () -> {},
                    () -> {}
            );
        }

        private static final ShaderStateShard EMISSIVE_SHADER =
                new ShaderStateShard(
                        () -> ROACWShaders.emissiveCutoutShader
                );

        private static final CullStateShard NO_CULL =
                RenderStateShard.NO_CULL;

        private static final LightmapStateShard LIGHTMAP_STATE =
                RenderStateShard.NO_LIGHTMAP;

        private static final OverlayStateShard OVERLAY_STATE =
                RenderStateShard.OVERLAY;

        private static final WriteMaskStateShard COLOR_WRITE_STATE =
                RenderStateShard.COLOR_DEPTH_WRITE;

        private static final OverlayStateShard OVERLAY =
                OVERLAY_STATE;

        private static final WriteMaskStateShard COLOR_WRITE =
                COLOR_WRITE_STATE;
    }
}