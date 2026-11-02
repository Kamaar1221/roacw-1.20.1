package net.kamaarion.roacw.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.kamaarion.roacw.progression.BossGateData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class BossKilledCondition implements LootItemCondition {
    private final ResourceLocation boss;

    public BossKilledCondition(ResourceLocation boss) {
        this.boss = boss;
    }

    @Override
    public LootItemConditionType getType() {
        return ROACWLootConditions.BOSS_KILLED.get();
    }

    @Override
    public boolean test(LootContext context) {
        return BossGateData.hasKilled(context.getLevel(), boss);
    }

    public static class ConditionSerializer implements Serializer<BossKilledCondition> {
        @Override
        public void serialize(JsonObject json, BossKilledCondition condition, JsonSerializationContext context) {
            json.addProperty("boss", condition.boss.toString());
        }

        @Override
        public BossKilledCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return new BossKilledCondition(ResourceLocation.parse(GsonHelper.getAsString(json, "boss")));
        }
    }
}