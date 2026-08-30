package net.kamaarion.roacw.client.music;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class ROACWLoopSound extends SimpleSoundInstance {

    public ROACWLoopSound(SoundEvent sound) {
        super(
                sound.getLocation(),
                SoundSource.MUSIC,
                1.0F,
                1.0F,
                RandomSource.create(),
                true,   // repeat
                0,      // repeat delay
                Attenuation.NONE,
                0.0D,
                0.0D,
                0.0D,
                true    // relative to listener
        );
    }
}