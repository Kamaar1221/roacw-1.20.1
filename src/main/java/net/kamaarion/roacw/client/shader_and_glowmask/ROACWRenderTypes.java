package net.kamaarion.roacw.client.shader_and_glowmask;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ROACWRenderTypes {

    private ROACWRenderTypes() {
    }

    private static final Map<ResourceLocation, RenderType> GLOW_DEPTH_WRITE_CACHE =
            new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> GLOW_COLOR_ONLY_CACHE =
            new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> OUTLINE_CACHE =
            new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> OUTLINE_NO_CULL_CACHE =
            new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, RenderType> EYES_NO_CULL_CACHE =
            new ConcurrentHashMap<>();

    /**
     * Shared shader state for the ROACW emissive render types.
     *
     * The supplier runs each time a render type is set up, so it follows shader
     * packs being toggled at runtime.
     *
     * - No shader pack: the custom ROACW emissive cutout shader.
     * - Shader pack active, rendering in the world: the vanilla
     *   entity_translucent_emissive shader, fetched through GameRenderer so
     *   Iris/Oculus recognises it by name and swaps in the pack's matching
     *   program.
     * - Shader pack active, rendering a GUI item icon: the custom shader again,
     *   since the pack's world programs don't apply to the inventory.
     */
    public static final RenderStateShard.ShaderStateShard EMISSIVE_CUTOUT_SHADER =
            new RenderStateShard.ShaderStateShard(
                    () -> IrisCompat.isShaderPackActive() && !isGuiProjection()
                            ? GameRenderer.getRendertypeEntityTranslucentEmissiveShader()
                            : ROACWShaders.emissiveCutoutShader
            );

    /**
     * True while the GUI is being drawn. The shader supplier runs when the
     * buffer is flushed, so this reflects the projection in use at draw time,
     * not when the layer was built. GUI rendering sets an orthographic
     * projection with ORTHOGRAPHIC_Z sorting; the world uses a perspective one.
     */
    private static boolean isGuiProjection() {
        return RenderSystem.getVertexSorting() == VertexSorting.ORTHOGRAPHIC_Z;
    }

    private static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY =
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

    private static final RenderStateShard.CullStateShard NO_CULL =
            new RenderStateShard.CullStateShard(false);

    private static final RenderStateShard.CullStateShard CULL =
            new RenderStateShard.CullStateShard(true);

    private static final RenderStateShard.LightmapStateShard NO_LIGHTMAP =
            new RenderStateShard.LightmapStateShard(false);

    private static final RenderStateShard.OverlayStateShard OVERLAY =
            new RenderStateShard.OverlayStateShard(true);

    private static final RenderStateShard.WriteMaskStateShard COLOR_DEPTH_WRITE =
            new RenderStateShard.WriteMaskStateShard(true, true);

    private static final RenderStateShard.WriteMaskStateShard COLOR_ONLY_WRITE =
            new RenderStateShard.WriteMaskStateShard(true, false);

    private static final RenderStateShard.DepthTestStateShard LEQUAL_DEPTH_TEST =
            new RenderStateShard.DepthTestStateShard("lequal", 515);

    private static final RenderStateShard.ShaderStateShard EYES_SHADER =
            new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEyesShader);

    private static final RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard(
                    "roacw_additive_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(
                                GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE
                        );
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }
            );

    public static RenderType eyesNoCull(ResourceLocation texture) {
        return EYES_NO_CULL_CACHE.computeIfAbsent(texture, tex -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(EYES_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_ONLY_WRITE)
                    .createCompositeState(false);

            return RenderType.create(
                    "roacw_eyes_no_cull",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    state
            );
        });
    }

    public static RenderType entityTranslucentEmissiveDepthWrite(ResourceLocation texture) {
        return GLOW_DEPTH_WRITE_CACHE.computeIfAbsent(texture, tex -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(EMISSIVE_CUTOUT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard(
                            "roacw_emissive_transparency",
                            () -> {
                                RenderSystem.enableBlend();
                                RenderSystem.blendFuncSeparate(
                                        GlStateManager.SourceFactor.SRC_ALPHA,
                                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                        GlStateManager.SourceFactor.ONE,
                                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                                );
                            },
                            () -> {
                                RenderSystem.disableBlend();
                                RenderSystem.defaultBlendFunc();
                            }
                    ))
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false);

            return RenderType.create(
                    "roacw_entity_emissive_depth_write",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    state
            );
        });
    }

    public static RenderType entityTranslucentEmissiveColorOnly(ResourceLocation texture) {
        return GLOW_COLOR_ONLY_CACHE.computeIfAbsent(texture, tex -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(EMISSIVE_CUTOUT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_ONLY_WRITE)
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
        });
    }

    public static RenderType entityTranslucentEmissiveOutline(ResourceLocation texture) {
        return OUTLINE_CACHE.computeIfAbsent(texture, tex -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(EMISSIVE_CUTOUT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_ONLY_WRITE)
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
        });
    }

    public static RenderType entityTranslucentEmissiveOutlineNoCull(ResourceLocation texture) {
        return OUTLINE_NO_CULL_CACHE.computeIfAbsent(texture, tex -> {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(EMISSIVE_CUTOUT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_ONLY_WRITE)
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
        });
    }

    /**
     * Looks up the Iris/Oculus API once, then only calls isShaderPackInUse()
     * per check. (The previous version resolved the class and methods on every
     * call, which runs every time a render type is set up.)
     */
    private static final class IrisCompat {

        private static final String[] API_CLASS_NAMES = {
                "net.irisshaders.iris.api.v0.IrisApi",
                "net.coderbot.iris.api.v0.IrisApi"
        };

        private static boolean resolved;
        private static Object apiInstance;
        private static Method isShaderPackInUse;

        private IrisCompat() {
        }

        private static void resolve() {
            resolved = true;

            if (!ModList.get().isLoaded("oculus")) {
                return;
            }

            for (String className : API_CLASS_NAMES) {
                try {
                    Class<?> apiClass = Class.forName(
                            className,
                            false,
                            ROACWRenderTypes.class.getClassLoader()
                    );

                    Object instance = apiClass.getMethod("getInstance").invoke(null);
                    Method method = apiClass.getMethod("isShaderPackInUse");

                    apiInstance = instance;
                    isShaderPackInUse = method;
                    return;
                } catch (ClassNotFoundException | NoSuchMethodException exception) {
                    // try the next known package
                } catch (IllegalAccessException | InvocationTargetException | LinkageError exception) {
                    return;
                }
            }
        }

        static boolean isShaderPackActive() {
            if (!resolved) {
                resolve();
            }

            if (isShaderPackInUse == null) {
                return false;
            }

            try {
                return Boolean.TRUE.equals(isShaderPackInUse.invoke(apiInstance));
            } catch (IllegalAccessException | InvocationTargetException exception) {
                return false;
            }
        }
    }
}