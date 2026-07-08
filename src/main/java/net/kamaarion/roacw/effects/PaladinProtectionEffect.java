package net.kamaarion.roacw.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.UUID;

public class PaladinProtectionEffect extends MobEffect {
    private static final UUID ALLY_ARMOR_UUID = UUID.fromString("8b2f1a10-2222-4a1a-9a1a-000000000001");
    private static final UUID ALLY_TOUGHNESS_UUID = UUID.fromString("8b2f1a10-2222-4a1a-9a1a-000000000002");

    public PaladinProtectionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00FFCC); // Clear cyan/blue paladin shield color

        // Allies get +25% armor and toughness (0.25D) with absolutely no speed penalties
        this.addAttributeModifier(Attributes.ARMOR, ALLY_ARMOR_UUID.toString(), 0.25D, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, ALLY_TOUGHNESS_UUID.toString(), 0.25D, AttributeModifier.Operation.MULTIPLY_BASE);
    }
}
