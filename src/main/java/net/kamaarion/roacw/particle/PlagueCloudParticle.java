package net.kamaarion.roacw.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlagueCloudParticle extends TextureSheetParticle {

    private static final int TOTAL_FRAMES = 10;

    // Max opacity this particle ever reaches. Was implicitly 1.0 (fully
    // opaque) before - with many of these overlapping in a lingering cloud,
    // that made it nearly impossible to see through while standing inside
    // it. Lower this further if it's still too thick, or raise it if the
    // cloud starts looking too faint to read as a hazard.
    private static final float PEAK_ALPHA = 0.4F;

    private final SpriteSet sprites;

    protected PlagueCloudParticle(ClientLevel level,
                                  double x,
                                  double y,
                                  double z,
                                  double xd,
                                  double yd,
                                  double zd,
                                  SpriteSet sprites) {

        super(level, x, y, z, xd, yd, zd);

        this.sprites = sprites;

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.hasPhysics = false;

        this.quadSize *= 2.8F + random.nextFloat() * 1.2F;

        this.lifetime = 45 + random.nextInt(20);

        this.alpha = PEAK_ALPHA;

        updateSprite();
    }

    @Override
    public void tick() {

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (++this.age >= this.lifetime) {
            remove();
            return;
        }

        move(this.xd, this.yd, this.zd);

        // Slow drifting
        this.xd += (random.nextDouble() - 0.5D) * 0.0008D;
        this.zd += (random.nextDouble() - 0.5D) * 0.0008D;

        // Gentle rise
        this.yd += 0.00025D;

        // Very slow expansion
        this.quadSize *= 1.01F;

        // Fade out over final 25% of life
        float fadeStart = lifetime * 0.75F;

        if (age > fadeStart) {
            alpha = PEAK_ALPHA * (1F - ((age - fadeStart) / (lifetime - fadeStart)));
        }

        updateSprite();
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    private void updateSprite() {
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
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

            return new PlagueCloudParticle(
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