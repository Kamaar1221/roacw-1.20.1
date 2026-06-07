package net.kamaarion.roacw.items.weapons;

import com.github.L_Ender.cataclysm.init.ModItems;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.ExtendedWeaponTier;
import io.redspace.ironsspellbooks.item.weapons.IronsWeaponTier;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.kamaarion.roacw.registeries.ROACWAttributeRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;


public class ROACWWeaponTiers extends StaffTier implements IronsWeaponTier {
    public static ROACWWeaponTiers MURASAMABLADE = new ROACWWeaponTiers(ItemPropertiesHelper.equipment().stacksTo(1).rarity(ROACWItemRegistry.GOD_FORGED), 15F, -2.5F,
            new AttributeContainer(ROACWAttributeRegistry.EXO_MAGIC_POWER, .2, AttributeModifier.Operation.MULTIPLY_TOTAL),
            new AttributeContainer(AttributeRegistry.SPELL_POWER, .15, AttributeModifier.Operation.MULTIPLY_TOTAL)

    );

    float damage;
    float speed;
    AttributeContainer[] attributes;



    public ROACWWeaponTiers(Item.Properties rarity, float damage, float speed, AttributeContainer... attributes) {
        super(damage, speed, attributes);
        this.damage = damage;
        this.speed = speed;
        this.attributes = attributes;
    }

    public float getAttackDamageBonus() {
        return this.damage;
    }

    public float getSpeed() {
        return this.speed;
    }

    public AttributeContainer[] getAdditionalAttributes() {
        return this.attributes;
    }
}