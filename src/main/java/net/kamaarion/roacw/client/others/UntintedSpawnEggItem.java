package net.kamaarion.roacw.client.others;

import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class UntintedSpawnEggItem extends ForgeSpawnEggItem {

    public UntintedSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, Item.Properties props) {
        // Colors are irrelevant here since getColor() is overridden below,
        // but ForgeSpawnEggItem's constructor still requires values
        super(type, 0xFFFFFF, 0xFFFFFF, props);
    }

    @Override
    public int getColor(int tintIndex) {
        return 0xFFFFFFFF; // always full white, full alpha = no tint applied to any layer
    }
}