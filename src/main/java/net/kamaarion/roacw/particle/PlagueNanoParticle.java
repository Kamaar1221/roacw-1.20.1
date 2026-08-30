package net.kamaarion.roacw.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class PlagueNanoParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected PlagueNanoParticle(ClientLevel level,
                                 double x, double y, double z,
                                 double xd, double yd, double zd,
                                 SpriteSet sprites) {

        super(level, x, y, z, xd, yd, zd);

        this.sprites = sprites;

        this.setSpriteFromAge(sprites);

        this.gravity = 0F;
        this.friction = 0.96F;

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.quadSize = 0.08F + random.nextFloat() * 0.04F;

        this.lifetime = 30 + random.nextInt(25);

        this.alpha = 0.0F;
    }

    @Override
    public void tick() {
        super.tick();

        this.setSpriteFromAge(sprites);

        // Gentle drifting motion
        this.xd += (random.nextDouble() - 0.5D) * 0.002D;
        this.yd += (random.nextDouble() - 0.5D) * 0.001D;
        this.zd += (random.nextDouble() - 0.5D) * 0.002D;

        float agePercent = (float)this.age / (float)this.lifetime;

        if (agePercent < 0.2F) {
            this.alpha = agePercent / 0.2F;
        } else if (agePercent > 0.8F) {
            this.alpha = (1.0F - agePercent) / 0.2F;
        } else {
            this.alpha = 1.0F;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    // ===========================
    // GREEN PROVIDER
    // ===========================

    public static class GreenProvider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public GreenProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type,
                                       ClientLevel level,
                                       double x,
                                       double y,
                                       double z,
                                       double xd,
                                       double yd,
                                       double zd) {

            return new PlagueNanoParticle(
                    level,
                    x,
                    y,
                    z,
                    xd,
                    yd,
                    zd,
                    sprites
            );
        }
    }

    // ===========================
    // RED PROVIDER
    // ===========================

    public static class RedProvider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public RedProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type,
                                       ClientLevel level,
                                       double x,
                                       double y,
                                       double z,
                                       double xd,
                                       double yd,
                                       double zd) {

            return new PlagueNanoParticle(
                    level,
                    x,
                    y,
                    z,
                    xd,
                    yd,
                    zd,
                    sprites
            );
        }
    }
}