package net.kamaarion.roacw.particle;

import io.redspace.ironsspellbooks.particle.FierySmokeParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoosterExhaustParticle extends FierySmokeParticle {

    protected BoosterExhaustParticle(ClientLevel level, double x, double y, double z,
                                     SpriteSet sprites, double xd, double yd, double zd) {

        super(level, x, y, z, sprites, xd, yd, zd);

        // ISS's own tick() does `yd += rand/100 - gravity` every tick.
        // Their gravity is -0.0025F (negative = an upward push, since it's
        // subtracted). Flipping the sign here turns that same inherited
        // logic into a downward pull instead - no tick() override needed.
        this.gravity = 0.02F; // TODO: tune - higher sinks faster

        // Optional: shorten lifetime so exhaust dissipates before it could
        // drift back toward the booster regardless of gravity direction.
        this.lifetime = 8 + (int) (Math.random() * 10); // was 10 + rand*30
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            return new BoosterExhaustParticle(level, x, y, z, this.sprites, xd, yd, zd);
        }
    }
}