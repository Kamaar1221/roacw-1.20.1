package net.kamaarion.roacw.entity.projectile.endo_hydra_beam;

import net.kamaarion.roacw.client.shader_and_glowmask.ROACWRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import java.lang.reflect.Method;

public class EndoHydraBeamGlowLayer
        extends AutoGlowingGeoLayer<EndoHydraBeamEntity> {

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

    public EndoHydraBeamGlowLayer(
            GeoRenderer<EndoHydraBeamEntity> renderer
    ) {
        super(renderer);
    }

    @Override
    protected RenderType getRenderType(
            EndoHydraBeamEntity animatable
    ) {
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

            if (emissiveTexture != null) {
                return ROACWRenderTypes.entityTranslucentEmissiveDepthWrite(
                        emissiveTexture
                );
            }

        } catch (Exception ignored) {
        }

        return null;
    }
}