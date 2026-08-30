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

    /*
     * The full soundtrack begins introducing the loop
     * approximately 38.75 seconds into the intro.
     */
    private static final double CROSSFADE_START_SECONDS = 38.75D;

    /*
     * Intro is approximately 42.3029 seconds.
     *
     * 42.3029 - 38.75 = ~3.5529 seconds
     */
    private static final double CROSSFADE_DURATION_SECONDS = 3.5529D;

    private static ROACWMusicSound introSound;
    private static ROACWMusicSound loopSound;

    /*
     * High-resolution timing for the intro -> loop transition.
     */
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

        /*
         * Once the player actually enters a world,
         * stop the ROACW soundtrack.
         */
        if (minecraft.level != null) {

            if (soundtrackStarted) {
                stopMusic();
            }

            return;
        }

        /*
         * Prevent vanilla Minecraft music from playing
         * underneath the ROACW soundtrack.
         */
        minecraft.getMusicManager().stopPlaying();

        /*
         * Start the soundtrack if it hasn't started yet.
         */
        if (!soundtrackStarted) {
            startIntro();
            return;
        }

        long now = System.nanoTime();

        /*
         * INTRO
         *
         * Wait until the loop's starting point.
         */
        if (introSound != null && !crossfading) {

            double elapsedSeconds =
                    (now - introStartTime) / 1_000_000_000.0D;

            if (elapsedSeconds >= CROSSFADE_START_SECONDS) {
                startLoop();
            }
        }

        /*
         * CROSSFADE
         *
         * The loop is already at full volume.
         * Only the intro fades out.
         */
        if (crossfading) {
            updateCrossfade(now);
        }
    }

    private static void startIntro() {

        Minecraft minecraft = Minecraft.getInstance();

        stopCurrentSounds();

        /*
         * Stop vanilla Minecraft's own music.
         */
        minecraft.getMusicManager().stopPlaying();

        /*
         * Start the ROACW intro at full volume.
         */
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

        /*
         * Start the loop at FULL volume.
         *
         * The second argument is true, which tells
         * Minecraft's sound engine to automatically loop it.
         */
        loopSound = new ROACWMusicSound(
                ROACWSoundRegistry.ROACW_TITLE_LOOP.get(),
                true,
                1.0F
        );

        minecraft.getSoundManager().play(loopSound);

        loopStartTime = System.nanoTime();

        /*
         * Begin fading out the intro.
         */
        crossfading = true;
    }

    private static void updateCrossfade(long now) {

        if (introSound == null || loopSound == null) {
            return;
        }

        double elapsedSinceLoopStart =
                (now - loopStartTime) / 1_000_000_000.0D;

        double progress =
                elapsedSinceLoopStart /
                        CROSSFADE_DURATION_SECONDS;

        progress = Math.max(
                0.0D,
                Math.min(1.0D, progress)
        );

        /*
         * Smoothstep interpolation.
         *
         * This makes the intro fade out smoothly.
         */
        double smoothProgress =
                progress * progress *
                        (3.0D - 2.0D * progress);

        /*
         * Fade the INTRO out.
         *
         * The LOOP remains at 100% volume the entire time.
         */
        introSound.setMusicVolume(
                (float) (1.0D - smoothProgress)
        );

        /*
         * Crossfade finished.
         */
        if (progress >= 1.0D) {

            introSound.setMusicVolume(0.0F);

            Minecraft.getInstance()
                    .getSoundManager()
                    .stop(introSound);

            introSound = null;

            /*
             * The loop is already playing at full volume.
             */
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