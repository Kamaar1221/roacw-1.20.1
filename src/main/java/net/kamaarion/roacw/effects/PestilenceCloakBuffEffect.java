package net.kamaarion.roacw.effects;

import com.gametechbc.spelllib.init.GSLAttributeRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class PestilenceCloakBuffEffect extends MobEffect {

    private static final UUID SPEED_MODIFIER_ID =
            UUID.fromString("a1b2c3d4-1111-2222-3333-444455556666");
    private static final UUID PROJECTILE_DAMAGE_MODIFIER_ID =
            UUID.fromString("7d3f2b1a-4e6c-4a9d-9c1e-2f8b6a5d3c7e");

    public PestilenceCloakBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4CAF50);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_ID.toString(),
                0.2D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        this.addAttributeModifier(
                GSLAttributeRegistry.MAGIC_PROJECTILE_DAMAGE.get(),
                PROJECTILE_DAMAGE_MODIFIER_ID.toString(),
                0.5D,
                AttributeModifier.Operation.ADDITION
        );
    }
}