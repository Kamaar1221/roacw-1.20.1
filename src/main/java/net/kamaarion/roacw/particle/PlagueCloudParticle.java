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

        this.xd += (random.nextDouble() - 0.5D) * 0.0008D;
        this.zd += (random.nextDouble() - 0.5D) * 0.0008D;

        this.yd += 0.00025D;

        this.quadSize *= 1.01F;

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