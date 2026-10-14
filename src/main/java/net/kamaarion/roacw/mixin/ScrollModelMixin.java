package net.kamaarion.roacw.mixin;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.render.ScrollModel;
import net.kamaarion.roacw.client.scroll.ROACWScrollTextureRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(
        value = ScrollModel.class,
        remap = false
)
public abstract class ScrollModelMixin {

    @Inject(
            method = "getModelFromTag",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void roacw$getCustomScrollModel(
            ItemStack stack,
            CompoundTag tag,
            CallbackInfoReturnable<Optional<ResourceLocation>> cir
    ) {
        if (!ISpellContainer.isSpellContainer(stack)) {
            return;
        }

        ISpellContainer spellContainer =
                ISpellContainer.get(stack);

        if (spellContainer == null) {
            return;
        }

        SpellData spellData =
                spellContainer.getSpellAtIndex(0);

        if (spellData == null) {
            return;
        }

        AbstractSpell spell =
                spellData.getSpell();

        if (spell == null) {
            return;
        }

        ResourceLocation customModel =
                ROACWScrollTextureRegistry.getModel(spell);

        if (customModel != null) {
            cir.setReturnValue(
                    Optional.of(customModel)
            );
        }
    }
}