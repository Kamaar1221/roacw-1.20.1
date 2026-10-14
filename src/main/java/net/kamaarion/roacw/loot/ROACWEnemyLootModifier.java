package net.kamaarion.roacw.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ROACWEnemyLootModifier extends LootModifier {
    public record LootEntry(
            ResourceLocation item,
            float chance,
            int minCount,
            int maxCount
    ) {
        public static final Codec<LootEntry> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        ResourceLocation.CODEC.fieldOf("item")
                                .forGetter(LootEntry::item),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("chance")
                                .forGetter(LootEntry::chance),
                        Codec.intRange(1, Integer.MAX_VALUE).fieldOf("min_count")
                                .forGetter(LootEntry::minCount),
                        Codec.intRange(1, Integer.MAX_VALUE).fieldOf("max_count")
                                .forGetter(LootEntry::maxCount)
                ).apply(instance, LootEntry::new));
    }

    private final ResourceLocation lootTable;
    private final List<LootEntry> entries;

    public static final Codec<ROACWEnemyLootModifier> CODEC =
            RecordCodecBuilder.create(instance ->
                    codecStart(instance)
                            .and(instance.group(
                                    ResourceLocation.CODEC.fieldOf("loot_table")
                                            .forGetter(modifier -> modifier.lootTable),
                                    LootEntry.CODEC.listOf()
                                            .fieldOf("entries")
                                            .forGetter(modifier -> modifier.entries)
                            ))
                            .apply(instance, ROACWEnemyLootModifier::new)
            );

    protected ROACWEnemyLootModifier(
            LootItemCondition[] conditions,
            ResourceLocation lootTable,
            List<LootEntry> entries
    ) {
        super(conditions);
        this.lootTable = lootTable;
        this.entries = entries;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context
    ) {
        if (!lootTable.equals(context.getQueriedLootTableId())) {
            return generatedLoot;
        }

        for (LootEntry entry : entries) {
            if (entry.maxCount() < entry.minCount()) {
                continue;
            }

            if (context.getRandom().nextFloat() >= entry.chance()) {
                continue;
            }

            Item item = BuiltInRegistries.ITEM.get(entry.item());

            if (item == Items.AIR) {
                continue;
            }

            int count = entry.minCount()
                    + context.getRandom().nextInt(
                    entry.maxCount() - entry.minCount() + 1
            );

            generatedLoot.add(new ItemStack(item, count));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}