package net.kamaarion.roacw.mixin;

import io.redspace.ironsspellbooks.item.curios.AffinityRing;
import net.kamaarion.roacw.client.affinityring.ROACWAffinityRingClientExtension;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = AffinityRing.class, remap = false)
public class AffinityRingMixin {

    @Inject(
            method = "initializeClient",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void roacw$initializeClient(
            Consumer<IClientItemExtensions> consumer,
            CallbackInfo ci
    ) {
        consumer.accept(ROACWAffinityRingClientExtension.INSTANCE);
        ci.cancel();
    }
}