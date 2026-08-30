package net.kamaarion.roacw.items.weapons;

import com.gametechbc.gtbcs_geomancy_plus.api.init.GGAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.IronsWeaponTier;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import net.kamaarion.roacw.registeries.ROACWAttributeRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;


public class ROACWWeaponTiers extends StaffTier implements IronsWeaponTier {
    public static ROACWWeaponTiers MURASAMABLADE = new ROACWWeaponTiers(new Item.Properties().stacksTo(1).rarity(ROACWItemRegistry.GOD_FORGED), 15F, -2.5F,
            new AttributeContainer(ROACWAttributeRegistry.EXO_MAGIC_POWER, .2, AttributeModifier.Operation.MULTIPLY_TOTAL),
            new AttributeContainer(AttributeRegistry.SPELL_POWER, .15, AttributeModifier.Operation.MULTIPLY_TOTAL)

    );
    public static ROACWWeaponTiers EARTH_SPLITTER = new ROACWWeaponTiers(new Item.Properties().stacksTo(1).rarity(ROACWItemRegistry.GOD_FORGED), 20F, -3.0F,
            new AttributeContainer(GGAttributes.GEO_SPELL_POWER, .15, AttributeModifier.Operation.MULTIPLY_TOTAL),
            new AttributeContainer(AttributeRegistry.HOLY_SPELL_POWER, .15, AttributeModifier.Operation.MULTIPLY_TOTAL)

    );

    public static ROACWWeaponTiers BURNING_SKY = new ROACWWeaponTiers(new Item.Properties().stacksTo(1).rarity(ROACWItemRegistry.GOD_FORGED), 18F, -2.5F,
            new AttributeContainer(AttributeRegistry.FIRE_SPELL_POWER, .15, AttributeModifier.Operation.MULTIPLY_TOTAL),
            new AttributeContainer(AttributeRegistry.HOLY_SPELL_POWER, .15, AttributeModifier.Operation.MULTIPLY_TOTAL)

    );

    public static ROACWWeaponTiers PHASESLAYER = new ROACWWeaponTiers(new Item.Properties().stacksTo(1).rarity(ROACWItemRegistry.GOD_FORGED), 20F, -2.5F,
            new AttributeContainer(ROACWAttributeRegistry.EXO_MAGIC_POWER, .2, AttributeModifier.Operation.MULTIPLY_TOTAL),
            new AttributeContainer(AttributeRegistry.SPELL_POWER, .15, AttributeModifier.Operation.MULTIPLY_TOTAL)

    );

    public static ROACWWeaponTiers PHASEBLADE = new ROACWWeaponTiers(new Item.Properties().stacksTo(1).rarity(ROACWItemRegistry.GOD_FORGED), 10F, -2.0F,
            new AttributeContainer(ROACWAttributeRegistry.EXO_MAGIC_POWER, .2, AttributeModifier.Operation.MULTIPLY_TOTAL),
            new AttributeContainer(AttributeRegistry.SPELL_POWER, .15, AttributeModifier.Operation.MULTIPLY_TOTAL)

    );

    public static ROACWWeaponTiers CORVID_HARBINGER = new ROACWWeaponTiers(new Item.Properties().stacksTo(1).rarity(ROACWItemRegistry.OTHERWORLDLY), 5F, -3.0F,
            new AttributeContainer(AttributeRegistry.FIRE_SPELL_POWER, .2, AttributeModifier.Operation.MULTIPLY_TOTAL),
            new AttributeContainer(AttributeRegistry.ELDRITCH_SPELL_POWER, .1, AttributeModifier.Operation.MULTIPLY_TOTAL)
    );

    public static ROACWWeaponTiers ENDO_HYDRA = new ROACWWeaponTiers(new Item.Properties().stacksTo(1).rarity(ROACWItemRegistry.OTHERWORLDLY), 5F, -3.0F,
            new AttributeContainer(AttributeRegistry.ICE_SPELL_POWER, .2, AttributeModifier.Operation.MULTIPLY_TOTAL),
            new AttributeContainer(AttributeRegistry.ELDRITCH_SPELL_POWER, .1, AttributeModifier.Operation.MULTIPLY_TOTAL)
    );

    public static ROACWWeaponTiers BELLADONNA_STAFF = new ROACWWeaponTiers(new Item.Properties().stacksTo(1).rarity(ROACWItemRegistry.OTHERWORLDLY), 5F, -3.0F,
            new AttributeContainer(AttributeRegistry.NATURE_SPELL_POWER, .15, AttributeModifier.Operation.MULTIPLY_TOTAL)
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