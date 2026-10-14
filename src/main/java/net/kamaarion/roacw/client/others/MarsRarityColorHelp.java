package net.kamaarion.roacw.client.others;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class MarsRarityColorHelp {

    private static final int[] RAINBOW = {
            0xD3EB6C,
            0xFF6B6B,
            0x7DC4E1
    };

    private static final long WAVE_DURATION_MS = 4000L;

    public static MutableComponent createRainbowWave(Component originalName) {
        String text = originalName.getString();

        MutableComponent result = Component.empty();

        long currentTime = System.currentTimeMillis();
        float waveTime = (currentTime % WAVE_DURATION_MS)
                / (float) WAVE_DURATION_MS;

        int length = text.length();

        for (int i = 0; i < length; i++) {
            char character = text.charAt(i);

            float wave = getWave(i, length, waveTime);
            int color = getRainbowColor(wave);

            result.append(
                    Component.literal(String.valueOf(character))
                            .setStyle(Style.EMPTY.withColor(color))
            );
        }

        return result;
    }

    private static float getWave(int charIndex, int totalLength, float time) {
        if (totalLength <= 1) {
            return time;
        }

        float position =
                (charIndex / (float) (totalLength - 1)) * 0.4F;

        return (position + time) % 1.0F;
    }

    private static int getRainbowColor(float progress) {
        progress = Math.max(0.0F, Math.min(1.0F, progress));

        float scaled = progress * RAINBOW.length;

        int index = (int) scaled;
        float localT = scaled - index;

        int color1 = RAINBOW[index % RAINBOW.length];
        int color2 = RAINBOW[(index + 1) % RAINBOW.length];

        return interpolateColor(color1, color2, localT);
    }

    private static int interpolateColor(
            int color1,
            int color2,
            float ratio
    ) {
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        r = Math.min(255, (int) (r * 1.10F));
        g = Math.min(255, (int) (g * 1.10F));
        b = Math.min(255, (int) (b * 1.10F));

        return (r << 16) | (g << 8) | b;
    }

    private MarsRarityColorHelp() {
    }
}