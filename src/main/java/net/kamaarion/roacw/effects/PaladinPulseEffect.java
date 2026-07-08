package net.kamaarion.roacw.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.UUID;

public class PaladinPulseEffect extends MobEffect {
    private static final UUID ARMOR_UUID = UUID.fromString("8b2f1a10-1111-4a1a-9a1a-000000000001");
    private static final UUID TOUGHNESS_UUID = UUID.fromString("8b2f1a10-1111-4a1a-9a1a-000000000002");
    private static final UUID SPEED_UUID = UUID.fromString("8b2f1a10-1111-4a1a-9a1a-000000000003");

    public PaladinPulseEffect() {
        // Category.BENEFICIAL makes it a positive buff (blue/green particles)
        // Color is represented in Hexadecimal (0xD4AF37 is Paladin Gold)
        super(MobEffectCategory.BENEFICIAL, 0xD4AF37);
        
        // Add attribute modifiers natively handled by Minecraft's effect engine
        // Amplifier 0 (Level 1) gives +50% armor/toughness and -30% speed via MULTIPLY_BASE
        this.addAttributeModifier(Attributes.ARMOR, ARMOR_UUID.toString(), 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, TOUGHNESS_UUID.toString(), 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_UUID.toString(), -0.3D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
