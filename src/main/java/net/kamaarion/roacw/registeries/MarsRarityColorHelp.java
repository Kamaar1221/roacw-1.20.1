package net.kamaarion.roacw.registeries;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class MarsRarityColorHelp {
    // 0: Lime Green, 1: Red, 2: Blue
    private static final int[] RAINBOW = { 0xD3EB6C, 0xFF6B6B, 0x7DC4E1 };

    public static MutableComponent createRainbowWave(String text) {
        MutableComponent result = Component.literal("");
        long currentTime = System.currentTimeMillis();
        float waveTime = (currentTime % 4000L) / 4000.0F; // slower, smoother wave
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
        // Spreads the colors across the length of the string
        float position = (charIndex / (float)Math.max(1, totalLength - 1)) * 0.4F;

        // Continuous 0.0 to 1.0 loop, no bouncing back and forth
        return (position + time) % 1.0F;
    }

    private static int getRainbowColor(float progress) {
        progress = Math.max(0f, Math.min(1f, progress));

        // Multiplied by RAINBOW.length so it can transition from the last index back to index 0
        float scaled = progress * RAINBOW.length;
        int index = (int) scaled;
        float localT = scaled - index;

        int c1 = RAINBOW[index % RAINBOW.length];
        // Wraps perfectly back to the first color at the end of the cycle
        int c2 = RAINBOW[(index + 1) % RAINBOW.length];

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
