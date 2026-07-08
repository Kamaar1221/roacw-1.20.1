package net.kamaarion.roacw.items.weapons;

import com.gametechbc.gtbcs_geomancy_plus.api.init.GGAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;
import java.util.UUID;
import java.util.function.Supplier;

public enum ROACWVanillaWeaponTiers implements Tier {
    // Add () -> right before Attributes.MOVEMENT_SPEED
    HIGH_RULER_SWORD(3000, 15F, 8.5F, 4, 25, () -> Ingredient.of(Items.NETHERITE_INGOT),
            () -> net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, .15, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private static final UUID ATTRIBUTE_UUID = UUID.fromString("6F8A5A12-B647-4CDE-9F71-92D0AE421C00");

    private final int uses;
    private final float speed;
    private final float damage;
    private final int level;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    // Properties to store our non-Iron's Spells custom attributes
    private final Supplier<? extends Attribute> extraAttribute;
    private final double attributeAmount;
    private final AttributeModifier.Operation attributeOperation;

    ROACWVanillaWeaponTiers(int uses, float speed, float damage, int level, int enchantmentValue,
                            Supplier<Ingredient> repairIngredient, Supplier<? extends Attribute> extraAttribute,
                            double attributeAmount, AttributeModifier.Operation attributeOperation) {
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.level = level;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
        this.extraAttribute = extraAttribute;
        this.attributeAmount = attributeAmount;
        this.attributeOperation = attributeOperation;
    }

    @Override public int getUses() { return this.uses; }
    @Override public float getSpeed() { return this.speed; }
    @Override public float getAttackDamageBonus() { return this.damage; }
    @Override public int getLevel() { return this.level; }
    @Override public int getEnchantmentValue() { return this.enchantmentValue; }
    @Override public Ingredient getRepairIngredient() { return this.repairIngredient.get(); }

    // Custom helper getters for the item class
    public Supplier<? extends Attribute> getExtraAttribute() { return this.extraAttribute; }
    public double getAttributeAmount() { return this.attributeAmount; }
    public AttributeModifier.Operation getAttributeOperation() { return this.attributeOperation; }
    public UUID getAttributeUuid() { return ATTRIBUTE_UUID; }
}
