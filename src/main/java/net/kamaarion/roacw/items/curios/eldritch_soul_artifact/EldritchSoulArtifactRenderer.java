package net.kamaarion.roacw.items.curios.eldritch_soul_artifact;

import software.bernie.geckolib.renderer.GeoItemRenderer;

public class EldritchSoulArtifactRenderer
        extends GeoItemRenderer<EldritchSoulArtifact> {

    public EldritchSoulArtifactRenderer() {
        super(new EldritchSoulArtifactModel());

        addRenderLayer(
                new EldritchSoulArtifactGlowLayer(this)
        );
    }
}