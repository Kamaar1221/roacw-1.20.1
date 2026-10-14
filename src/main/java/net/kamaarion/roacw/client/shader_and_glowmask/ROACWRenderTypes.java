package net.kamaarion.roacw.client.shader_and_glowmask;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ROACWRenderTypes {

    private ROACWRenderTypes() {
    }

    private static final Map<ResourceLocation, RenderType>
            GLOW_DEPTH_WRITE_CACHE =
            new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType>
            GLOW_COLOR_ONLY_CACHE =
            new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType>
            OUTLINE_CACHE =
            new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType>
            OUTLINE_NO_CULL_CACHE =
            new ConcurrentHashMap<>();

    private static final RenderStateShard.ShaderStateShard
            EMISSIVE_CUTOUT_SHADER =
            new RenderStateShard.ShaderStateShard(
                    () -> ROACWShaders.emissiveCutoutShader
            );

    private static final RenderStateShard.TransparencyStateShard
            TRANSLUCENT_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard(
                    "roacw_translucent_transparency",
                    () -> {
                        RenderSystem.enableBlend();

                        RenderSystem.blendFunc(
                                GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                        );
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }
            );

    private static final RenderStateShard.CullStateShard
            NO_CULL =
            new RenderStateShard.CullStateShard(false);

    private static final RenderStateShard.CullStateShard
            CULL =
            new RenderStateShard.CullStateShard(true);

    private static final RenderStateShard.LightmapStateShard
            NO_LIGHTMAP =
            new RenderStateShard.LightmapStateShard(false);

    private static final RenderStateShard.OverlayStateShard
            OVERLAY =
            new RenderStateShard.OverlayStateShard(true);

    private static final RenderStateShard.WriteMaskStateShard
            COLOR_DEPTH_WRITE =
            new RenderStateShard.WriteMaskStateShard(
                    true,
                    true
            );

    private static final RenderStateShard.WriteMaskStateShard
            COLOR_ONLY_WRITE =
            new RenderStateShard.WriteMaskStateShard(
                    true,
                    false
            );

    public static RenderType entityTranslucentEmissiveDepthWrite(
            ResourceLocation texture
    ) {
        return GLOW_DEPTH_WRITE_CACHE.computeIfAbsent(
                texture,
                tex -> {
                    RenderType.CompositeState state =
                            RenderType.CompositeState.builder()
                                    .setShaderState(
                                            EMISSIVE_CUTOUT_SHADER
                                    )
                                    .setTextureState(
                                            new RenderStateShard.TextureStateShard(
                                                    tex,
                                                    false,
                                                    false
                                            )
                                    )
                                    .setTransparencyState(
                                            TRANSLUCENT_TRANSPARENCY
                                    )
                                    .setCullState(
                                            NO_CULL
                                    )
                                    .setLightmapState(
                                            NO_LIGHTMAP
                                    )
                                    .setOverlayState(
                                            OVERLAY
                                    )
                                    .setWriteMaskState(
                                            COLOR_DEPTH_WRITE
                                    )
                                    .createCompositeState(true);

                    return RenderType.create(
                            "roacw_entity_translucent_emissive_depth_write",
                            DefaultVertexFormat.NEW_ENTITY,
                            VertexFormat.Mode.QUADS,
                            256,
                            true,
                            true,
                            state
                    );
                }
        );
    }

    public static RenderType entityTranslucentEmissiveColorOnly(
            ResourceLocation texture
    ) {
        return GLOW_COLOR_ONLY_CACHE.computeIfAbsent(
                texture,
                tex -> {
                    RenderType.CompositeState state =
                            RenderType.CompositeState.builder()
                                    .setShaderState(
                                            EMISSIVE_CUTOUT_SHADER
                                    )
                                    .setTextureState(
                                            new RenderStateShard.TextureStateShard(
                                                    tex,
                                                    false,
                                                    false
                                            )
                                    )
                                    .setTransparencyState(
                                            TRANSLUCENT_TRANSPARENCY
                                    )
                                    .setCullState(
                                            NO_CULL
                                    )
                                    .setLightmapState(
                                            NO_LIGHTMAP
                                    )
                                    .setOverlayState(
                                            OVERLAY
                                    )
                                    .setWriteMaskState(
                                            COLOR_ONLY_WRITE
                                    )
                                    .createCompositeState(true);

                    return RenderType.create(
                            "roacw_entity_translucent_emissive_color_only",
                            DefaultVertexFormat.NEW_ENTITY,
                            VertexFormat.Mode.QUADS,
                            256,
                            true,
                            true,
                            state
                    );
                }
        );
    }

    public static RenderType entityTranslucentEmissiveOutline(
            ResourceLocation texture
    ) {
        return OUTLINE_CACHE.computeIfAbsent(
                texture,
                tex -> {
                    RenderType.CompositeState state =
                            RenderType.CompositeState.builder()
                                    .setShaderState(
                                            EMISSIVE_CUTOUT_SHADER
                                    )
                                    .setTextureState(
                                            new RenderStateShard.TextureStateShard(
                                                    tex,
                                                    false,
                                                    false
                                            )
                                    )
                                    .setTransparencyState(
                                            TRANSLUCENT_TRANSPARENCY
                                    )
                                    .setDepthTestState(
                                            new RenderStateShard.DepthTestStateShard(
                                                    "lequal",
                                                    515
                                            )
                                    )
                                    .setCullState(
                                            CULL
                                    )
                                    .setLightmapState(
                                            NO_LIGHTMAP
                                    )
                                    .setOverlayState(
                                            OVERLAY
                                    )
                                    .setWriteMaskState(
                                            COLOR_ONLY_WRITE
                                    )
                                    .createCompositeState(true);

                    return RenderType.create(
                            "roacw_entity_translucent_emissive_outline",
                            DefaultVertexFormat.NEW_ENTITY,
                            VertexFormat.Mode.QUADS,
                            256,
                            true,
                            true,
                            state
                    );
                }
        );
    }

    public static RenderType entityTranslucentEmissiveOutlineNoCull(
            ResourceLocation texture
    ) {
        return OUTLINE_NO_CULL_CACHE.computeIfAbsent(
                texture,
                tex -> {
                    RenderType.CompositeState state =
                            RenderType.CompositeState.builder()
                                    .setShaderState(
                                            EMISSIVE_CUTOUT_SHADER
                                    )
                                    .setTextureState(
                                            new RenderStateShard.TextureStateShard(
                                                    tex,
                                                    false,
                                                    false
                                            )
                                    )
                                    .setTransparencyState(
                                            TRANSLUCENT_TRANSPARENCY
                                    )
                                    .setDepthTestState(
                                            new RenderStateShard.DepthTestStateShard(
                                                    "lequal",
                                                    515
                                            )
                                    )
                                    .setCullState(
                                            NO_CULL
                                    )
                                    .setLightmapState(
                                            NO_LIGHTMAP
                                    )
                                    .setOverlayState(
                                            OVERLAY
                                    )
                                    .setWriteMaskState(
                                            COLOR_ONLY_WRITE
                                    )
                                    .createCompositeState(true);

                    return RenderType.create(
                            "roacw_entity_translucent_emissive_outline_no_cull",
                            DefaultVertexFormat.NEW_ENTITY,
                            VertexFormat.Mode.QUADS,
                            256,
                            true,
                            true,
                            state
                    );
                }
        );
    }
}