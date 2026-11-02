package net.kamaarion.roacw.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class BrimstoneSlashParticle extends TextureSheetParticle {
    private static final int FRAME_COUNT = 6;
    private static final int VARIANT_COUNT = 2;
    private static final int LIFETIME_TICKS = 6;
    private final SpriteSet sprites;
    private final Vec3 forward;
    private final boolean mirror;
    private final boolean vertical;
    private final int variant;
    private final Vector3f[] localVertices;

    BrimstoneSlashParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            SpriteSet sprites,
            double xd,
            double yd,
            double zd,
            BrimstoneSlashParticleOptions options
    ) {
        super(level, x, y, z, 0, 0, 0);

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.lifetime = LIFETIME_TICKS;
        this.gravity = 0;
        this.sprites = sprites;
        this.quadSize = options.scale * 3.75f;
        this.forward = new Vec3(options.xf, options.yf, options.zf).normalize();
        this.mirror = options.mirror;
        this.variant = this.random.nextInt(VARIANT_COUNT);
        this.vertical = options.vertical;
        this.localVertices = calculateVertices();
        this.friction = 1;
        this.hasPhysics = false;
        this.setFrame(0);
    }

    private void setFrame(int frame) {
        int index = this.variant * FRAME_COUNT + frame;
        this.setSprite(this.sprites.get(index, FRAME_COUNT * VARIANT_COUNT - 1));
    }

    private Vector3f[] calculateVertices() {
        Vec3 up = new Vec3(0, 1, 0);

        if (forward.dot(up) > 0.999) {
            up = new Vec3(1, 0, 0);
        }

        Vec3 right = forward.cross(up);

        up = up.subtract(proj(forward, up)).normalize();
        right = right.subtract(proj(forward, right))
                .subtract(proj(up, right))
                .normalize();

        Vec3 primary;
        Vec3 secondary;

        if (!vertical) {
            primary = forward;
            secondary = right;
        } else {
            primary = forward;
            secondary = up;
        }

        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for (int i = 0; i < 4; i++) {
            float x = (float) (primary.x * vertices[i].x + secondary.x * vertices[i].y);
            float y = (float) (primary.y * vertices[i].x + secondary.y * vertices[i].y);
            float z = (float) (primary.z * vertices[i].x + secondary.z * vertices[i].y);

            vertices[i] = new Vector3f(x, y, z);
        }

        return vertices;
    }

    public Vec3 proj(Vec3 u, Vec3 v) {
        return u.scale(v.dot(u) / u.lengthSqr());
    }

    @Override
    public void tick() {
        this.age++;

        if (this.age >= this.lifetime) {
            this.remove();
            return;
        }

        int frame = Math.min(this.age * FRAME_COUNT / this.lifetime, FRAME_COUNT - 1);
        this.setFrame(frame);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        boolean mirrored = !this.mirror;

        Vec3 cameraPosition = camera.getPosition();

        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPosition.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPosition.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPosition.z());

        Vector3f[] vertices = new Vector3f[4];

        for (int i = 0; i < 4; i++) {
            Vector3f localVertex = this.localVertices[i];

            vertices[i] = new Vector3f(localVertex.x, localVertex.y, localVertex.z);
            vertices[i].mul(this.getQuadSize(partialTick));
            vertices[i].add(x, y, z);
        }

        int light = this.getLightColor(partialTick);

        this.makeCornerVertex(buffer, vertices[0], this.getU1(), mirrored ? this.getV0() : this.getV1(), light);
        this.makeCornerVertex(buffer, vertices[1], this.getU1(), mirrored ? this.getV1() : this.getV0(), light);
        this.makeCornerVertex(buffer, vertices[2], this.getU0(), mirrored ? this.getV1() : this.getV0(), light);
        this.makeCornerVertex(buffer, vertices[3], this.getU0(), mirrored ? this.getV0() : this.getV1(), light);

        this.makeCornerVertex(buffer, vertices[3], this.getU0(), mirrored ? this.getV0() : this.getV1(), light);
        this.makeCornerVertex(buffer, vertices[2], this.getU0(), mirrored ? this.getV1() : this.getV0(), light);
        this.makeCornerVertex(buffer, vertices[1], this.getU1(), mirrored ? this.getV1() : this.getV0(), light);
        this.makeCornerVertex(buffer, vertices[0], this.getU1(), mirrored ? this.getV0() : this.getV1(), light);
    }

    private void makeCornerVertex(
            VertexConsumer consumer,
            Vector3f vertex,
            float u,
            float v,
            int light
    ) {
        consumer.vertex(vertex.x(), vertex.y(), vertex.z())
                .uv(u, v)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light)
                .endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<BrimstoneSlashParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                @NotNull BrimstoneSlashParticleOptions options,
                @NotNull ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            BrimstoneSlashParticle particle = new BrimstoneSlashParticle(
                    level,
                    x,
                    y,
                    z,
                    sprites,
                    xSpeed,
                    ySpeed,
                    zSpeed,
                    options
            );

            particle.setAlpha(1.0F);

            return particle;
        }
    }
}