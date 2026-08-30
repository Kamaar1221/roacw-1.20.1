package net.kamaarion.roacw.client.music;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class ROACWMusicSound extends AbstractTickableSoundInstance {

    public ROACWMusicSound(
            SoundEvent sound,
            boolean looping,
            float initialVolume
    ) {
        super(
                sound,
                SoundSource.MUSIC,
                RandomSource.create()
        );

        /*
         * Minecraft handles looping when this is true.
         */
        this.looping = looping;

        this.delay = 0;
        this.volume = initialVolume;
        this.pitch = 1.0F;

        /*
         * Music isn't affected by distance from a position.
         */
        this.relative = true;
    }

    @Override
    public void tick() {
        /*
         * Volume is controlled by ROACWTitleMusic.
         */
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return true;
    }

    public void setMusicVolume(float volume) {
        this.volume = Math.max(
                0.0F,
                Math.min(1.0F, volume)
        );
    }
}