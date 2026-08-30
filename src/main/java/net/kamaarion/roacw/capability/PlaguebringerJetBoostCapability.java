package net.kamaarion.roacw.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = "roacw", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlaguebringerJetBoostCapability {
    public static final Capability<Data> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation KEY = new ResourceLocation("roacw", "plaguebringer_jet_boost_cooldown");

    public static class Data {
        private long nextAvailableTick = 0L;
        private boolean jetBoostActive = false;

        public long getNextAvailableTick() {
            return this.nextAvailableTick;
        }

        public void setNextAvailableTick(long tick) {
            this.nextAvailableTick = tick;
        }

        public boolean isJetBoostActive() {
            return this.jetBoostActive;
        }

        public void setJetBoostActive(boolean active) {
            this.jetBoostActive = active;
        }

        public void saveNBT(CompoundTag tag) {
            tag.putLong("NextAvailableTick", nextAvailableTick);
            tag.putBoolean("JetBoostActive", jetBoostActive);
        }

        public void loadNBT(CompoundTag tag) {
            this.nextAvailableTick = tag.getLong("NextAvailableTick");
            this.jetBoostActive = tag.getBoolean("JetBoostActive");
        }
    }

    public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final Data data = new Data();
        private final LazyOptional<Data> optional = LazyOptional.of(() -> data);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == INSTANCE ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            data.saveNBT(tag);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.loadNBT(nbt);
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(Data.class);
    }

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(KEY, new Provider());
        }
    }
}