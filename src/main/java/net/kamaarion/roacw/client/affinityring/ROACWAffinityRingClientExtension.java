package net.kamaarion.roacw.client.affinityring;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class ROACWAffinityRingClientExtension
        implements IClientItemExtensions {

    public static final ROACWAffinityRingClientExtension INSTANCE =
            new ROACWAffinityRingClientExtension();

    private ROACWAffinityRingClientExtension() {
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        Minecraft minecraft = Minecraft.getInstance();

        return new ROACWAffinityRingRenderer(
                minecraft.getItemRenderer(),
                minecraft.getEntityModels()
        );
    }
}