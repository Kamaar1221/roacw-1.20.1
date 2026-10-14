package net.kamaarion.roacw.client.music;

import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "roacw",
        value = Dist.CLIENT
)
public class ROACWTitleMusic {
    private static final double CROSSFADE_START_SECONDS = 38.75D;
    private static final double CROSSFADE_DURATION_SECONDS = 3.5529D;

    private static ROACWMusicSound introSound;
    private static ROACWMusicSound loopSound;

    private static long introStartTime;
    private static long loopStartTime;

    private static boolean soundtrackStarted = false;
    private static boolean crossfading = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level != null) {
            if (soundtrackStarted) {
                stopMusic();
            }

            return;
        }

        minecraft.getMusicManager().stopPlaying();

        if (!soundtrackStarted) {
            startIntro();
            return;
        }

        long now = System.nanoTime();

        if (introSound != null && !crossfading) {
            double elapsedSeconds = (now - introStartTime) / 1_000_000_000.0D;

            if (elapsedSeconds >= CROSSFADE_START_SECONDS) {
                startLoop();
            }
        }

        if (crossfading) {
            updateCrossfade(now);
        }
    }

    private static void startIntro() {
        Minecraft minecraft = Minecraft.getInstance();

        stopCurrentSounds();
        minecraft.getMusicManager().stopPlaying();

        introSound = new ROACWMusicSound(
                ROACWSoundRegistry.ROACW_TITLE_INTRO.get(),
                false,
                1.0F
        );

        minecraft.getSoundManager().play(introSound);

        introStartTime = System.nanoTime();

        soundtrackStarted = true;
        crossfading = false;
    }

    private static void startLoop() {
        Minecraft minecraft = Minecraft.getInstance();

        if (loopSound != null) {
            return;
        }

        loopSound = new ROACWMusicSound(
                ROACWSoundRegistry.ROACW_TITLE_LOOP.get(),
                true,
                1.0F
        );

        minecraft.getSoundManager().play(loopSound);

        loopStartTime = System.nanoTime();
        crossfading = true;
    }

    private static void updateCrossfade(long now) {
        if (introSound == null || loopSound == null) {
            return;
        }

        double elapsedSinceLoopStart = (now - loopStartTime) / 1_000_000_000.0D;
        double progress = elapsedSinceLoopStart / CROSSFADE_DURATION_SECONDS;

        progress = Math.max(0.0D, Math.min(1.0D, progress));

        double smoothProgress = progress * progress * (3.0D - 2.0D * progress);

        introSound.setMusicVolume((float) (1.0D - smoothProgress));

        if (progress >= 1.0D) {
            introSound.setMusicVolume(0.0F);

            Minecraft.getInstance()
                    .getSoundManager()
                    .stop(introSound);

            introSound = null;
            loopSound.setMusicVolume(1.0F);
            crossfading = false;
        }
    }

    private static void stopCurrentSounds() {
        Minecraft minecraft = Minecraft.getInstance();

        if (introSound != null) {
            minecraft.getSoundManager().stop(introSound);
            introSound = null;
        }

        if (loopSound != null) {
            minecraft.getSoundManager().stop(loopSound);
            loopSound = null;
        }

        introStartTime = 0L;
        loopStartTime = 0L;
        crossfading = false;
    }

    private static void stopMusic() {
        stopCurrentSounds();
        soundtrackStarted = false;
    }
}