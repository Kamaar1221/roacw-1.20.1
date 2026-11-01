package net.kamaarion.roacw.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class BrimstoneSlashParticleOptions implements ParticleOptions {

    public final float xf;
    public final float yf;
    public final float zf;
    public final boolean mirror;
    public final boolean vertical;
    public final float scale;

    public BrimstoneSlashParticleOptions(float xf, float yf, float zf, boolean mirror, boolean vertical, float scale) {
        this.xf = xf;
        this.yf = yf;
        this.zf = zf;
        this.mirror = mirror;
        this.vertical = vertical;
        this.scale = scale;
    }

    public static final Codec<BrimstoneSlashParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("xf").forGetter(options -> options.xf),
                    Codec.FLOAT.fieldOf("yf").forGetter(options -> options.yf),
                    Codec.FLOAT.fieldOf("zf").forGetter(options -> options.zf),
                    Codec.BOOL.fieldOf("mirror").forGetter(options -> options.mirror),
                    Codec.BOOL.fieldOf("vertical").forGetter(options -> options.vertical),
                    Codec.FLOAT.fieldOf("scale").forGetter(options -> options.scale)
            ).apply(instance, BrimstoneSlashParticleOptions::new)
    );

    public static final Deserializer<BrimstoneSlashParticleOptions> DESERIALIZER = new Deserializer<>() {
        @Override
        public BrimstoneSlashParticleOptions fromCommand(
                ParticleType<BrimstoneSlashParticleOptions> type,
                StringReader reader
        ) throws CommandSyntaxException {
            reader.expect(' ');
            float xf = reader.readFloat();
            reader.expect(' ');
            float yf = reader.readFloat();
            reader.expect(' ');
            float zf = reader.readFloat();
            reader.expect(' ');
            boolean mirror = reader.readBoolean();
            reader.expect(' ');
            boolean vertical = reader.readBoolean();
            reader.expect(' ');
            float scale = reader.readFloat();

            return new BrimstoneSlashParticleOptions(xf, yf, zf, mirror, vertical, scale);
        }

        @Override
        public BrimstoneSlashParticleOptions fromNetwork(
                ParticleType<BrimstoneSlashParticleOptions> type,
                FriendlyByteBuf buffer
        ) {
            return new BrimstoneSlashParticleOptions(
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readFloat()
            );
        }
    };

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(xf);
        buffer.writeFloat(yf);
        buffer.writeFloat(zf);
        buffer.writeBoolean(mirror);
        buffer.writeBoolean(vertical);
        buffer.writeFloat(scale);
    }

    @Override
    public String writeToString() {
        return String.format("%s %s %s %s %s %s", xf, yf, zf, mirror, vertical, scale);
    }

    @Override
    public @NotNull ParticleType<BrimstoneSlashParticleOptions> getType() {
        return ROACWParticleRegistry.BRIMSTONE_SLASH.get();
    }
}