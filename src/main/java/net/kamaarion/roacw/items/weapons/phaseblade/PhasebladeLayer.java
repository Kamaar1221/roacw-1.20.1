package net.kamaarion.roacw.items.weapons.phaseblade;

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

public class PhasebladeLayer extends AutoGlowingGeoLayer<Phaseblade> {

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

    public PhasebladeLayer(GeoRenderer<Phaseblade> renderer) {
        super(renderer);
    }

    private static RenderType createPhasebladeEmissive(
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
                                PhasebladeRenderType.EMISSIVE_SHADER
                        )
                        .setCullState(
                                PhasebladeRenderType.NO_CULL
                        )
                        .setLightmapState(
                                PhasebladeRenderType.LIGHTMAP_STATE
                        )
                        .setOverlayState(
                                PhasebladeRenderType.OVERLAY
                        )
                        .setWriteMaskState(
                                PhasebladeRenderType.COLOR_WRITE
                        )
                        .createCompositeState(false);

        return RenderType.create(
                "roacw_phaseblade_emissive",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                state
        );
    }

    @Override
    protected RenderType getRenderType(Phaseblade animatable) {
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

            return createPhasebladeEmissive(
                    emissiveTexture
            );

        } catch (Exception ignored) {
            return null;
        }
    }

    private static class PhasebladeRenderType extends RenderType {

        private PhasebladeRenderType() {
            super(
                    "roacw_phaseblade_render_type",
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
                new ShaderStateShard(() -> ROACWShaders.emissiveCutoutShader);

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