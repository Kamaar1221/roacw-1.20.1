package net.kamaarion.roacw.progression;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kamaarion.roacw.ROACW;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.MobSpawnType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SpawnGateManager extends SimpleJsonResourceReloadListener {
    public record Gate(ResourceLocation boss, Set<MobSpawnType> spawnTypes) {}

    private static final Gson GSON = new GsonBuilder().create();
    private static final Set<MobSpawnType> DEFAULT_TYPES = EnumSet.of(
            MobSpawnType.NATURAL,
            MobSpawnType.CHUNK_GENERATION,
            MobSpawnType.STRUCTURE,
            MobSpawnType.PATROL,
            MobSpawnType.REINFORCEMENT
    );

    private static volatile Map<ResourceLocation, Gate> gates = Map.of();

    public SpawnGateManager() {
        super(GSON, "spawn_gates");
    }

    public static Gate get(ResourceLocation entityId) {
        return gates.get(entityId);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, Gate> loaded = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> file : files.entrySet()) {
            try {
                JsonObject json = GsonHelper.convertToJsonObject(file.getValue(), "spawn gate");
                ResourceLocation boss = ResourceLocation.parse(GsonHelper.getAsString(json, "boss"));
                Set<MobSpawnType> types = json.has("spawn_types")
                        ? parseTypes(GsonHelper.getAsJsonArray(json, "spawn_types"))
                        : DEFAULT_TYPES;
                Gate gate = new Gate(boss, types);

                for (JsonElement element : GsonHelper.getAsJsonArray(json, "entities")) {
                    loaded.put(ResourceLocation.parse(GsonHelper.convertToString(element, "entity")), gate);
                }
            } catch (RuntimeException e) {
                ROACW.LOGGER.error("Failed to load spawn gate {}", file.getKey(), e);
            }
        }

        gates = Map.copyOf(loaded);
    }

    private static Set<MobSpawnType> parseTypes(JsonArray array) {
        Set<MobSpawnType> types = EnumSet.noneOf(MobSpawnType.class);

        for (JsonElement element : array) {
            types.add(MobSpawnType.valueOf(GsonHelper.convertToString(element, "spawn type").toUpperCase(Locale.ROOT)));
        }

        return types;
    }
}