package net.kamaarion.roacw.progression;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public class BossGateData extends SavedData {

    private static final String DATA_NAME = "roacw_boss_gates";
    private static final String KILLED_KEY = "KilledBosses";

    private final Set<ResourceLocation> killedBosses = new HashSet<>();

    public static BossGateData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                BossGateData::load,
                BossGateData::new,
                DATA_NAME
        );
    }

    public static boolean hasKilled(ServerLevelAccessor level, EntityType<?> bossType) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(bossType);

        return id != null && hasKilled(level, id);
    }

    public static boolean hasKilled(ServerLevelAccessor level, ResourceLocation bossId) {
        MinecraftServer server = level.getLevel().getServer();

        return server != null && get(server).killedBosses.contains(bossId);
    }

    public static void markKilled(MinecraftServer server, ResourceLocation bossId) {
        BossGateData data = get(server);

        if (data.killedBosses.add(bossId)) {
            data.setDirty();
        }
    }

    private static BossGateData load(CompoundTag tag) {
        BossGateData data = new BossGateData();
        ListTag list = tag.getList(KILLED_KEY, Tag.TAG_STRING);

        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));

            if (id != null) {
                data.killedBosses.add(id);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();

        for (ResourceLocation id : this.killedBosses) {
            list.add(StringTag.valueOf(id.toString()));
        }

        tag.put(KILLED_KEY, list);
        return tag;
    }
}