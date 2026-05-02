package net.rev.marsarmor.registeries;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class MarsRarityColorHelp {
    private static final int[] RAINBOW = {
            0xC2FCF1,
            0x8DDEEC,
            0x6AF0DC,
            0x408090,
            0x4863B6,
            0x455880,
            0x3A3374,
            0xB9FCF3,
            0xD5EC6C,
            0xA9F16D,
            0x368F68,
            0xFFFFF0,
            0xFAFD72,
            0xF2B62A,
            0xF2B62D,
            0xE9BEA8,
            0xDE95A5,
            0x916091,
            0x8C2D54,
            0x872E59,
            0xF0734A
    };

    public static MutableComponent createRainbowWave(String text) {
        MutableComponent result = Component.literal("");

        long currentTime = System.currentTimeMillis();
        float waveTime = (currentTime % 12000L) / 12000.0F; // slower, smoother wave

        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            float wave = getWave(i, length, waveTime);
            int color = getRainbowColor(wave);

            result.append(Component.literal(String.valueOf(c))
                    .setStyle(Style.EMPTY.withColor(color)));
        }

        return result;
    }

    private static float getWave(float charIndex, int totalLength, float time) {
        float position = (charIndex / (float)Math.max(1, totalLength - 1)) * 0.2F;

        float wavePhase = (position + time) % 1.0F;

        float wave = (float)(Math.sin(wavePhase * Math.PI * 1.0) * 0.5 + 0.5);

        return (float)Math.pow(wave, 1.3);
    }

    private static int getRainbowColor(float progress) {
        progress = Math.max(0f, Math.min(1f, progress));

        float scaled = progress * (RAINBOW.length - 1);
        int index = (int) scaled;
        float localT = scaled - index;

        int c1 = RAINBOW[index];
        int c2 = RAINBOW[Math.min(index + 1, RAINBOW.length - 1)];

        return interpolateColor(c1, c2, localT);
    }

    private static int interpolateColor(int color1, int color2, float ratio) {
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));

        int r1 = (color1 >> 16) & 255;
        int g1 = (color1 >> 8) & 255;
        int b1 = color1 & 255;

        int r2 = (color2 >> 16) & 255;
        int g2 = (color2 >> 8) & 255;
        int b2 = color2 & 255;

        int r = (int)(r1 + (r2 - r1) * ratio);
        int g = (int)(g1 + (g2 - g1) * ratio);
        int b = (int)(b1 + (b2 - b1) * ratio);

        // 🔥 slight brightness boost for glow effect
        r = Math.min(255, (int)(r * 1.1f));
        g = Math.min(255, (int)(g * 1.1f));
        b = Math.min(255, (int)(b * 1.1f));

        return (r << 16) | (g << 8) | b;
    }
}
