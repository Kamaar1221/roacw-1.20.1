package net.kamaarion.roacw.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomAnimatedParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected CustomAnimatedParticle(ClientLevel level, double x, double y, double z,
                                     double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.quadSize *= 1.5F;
        this.lifetime = 30;    // Total particle life is 30 ticks (1.5 seconds)
        this.hasPhysics = true;

        // Set the initial frame index using our fast timing logic
        updateFastSprite();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        // Dynamically shift texture indexes at accelerated speeds on every game tick
        updateFastSprite();
    }

    private void updateFastSprite() {
        // CHANGED: 3.0F means the animation textures will flip 3 times faster than normal
        float animationSpeedMultiplier = 0.5F;

        // Total number of .png frames declared in your shadowflame.json asset file
        int totalFrames = 5;

        // Determine frame number based on age multiplied by speed, then loop back using modulo (%)
        int currentFrame = (int)(this.age * animationSpeedMultiplier) % totalFrames;

        this.setSprite(this.sprites.get(currentFrame, totalFrames));
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new CustomAnimatedParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}

