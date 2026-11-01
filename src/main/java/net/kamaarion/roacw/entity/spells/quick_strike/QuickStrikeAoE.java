package net.kamaarion.roacw.entity.spells.quick_strike;

import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;

import java.util.Optional;

public class QuickStrikeAoE extends AoeEntity {
    private static final EntityDataAccessor<Boolean> DATA_IS_MIRRORED =
            SynchedEntityData.defineId(QuickStrikeAoE.class, EntityDataSerializers.BOOLEAN);

    public final int ticksPerFrame = 2;
    public final int deathTime = ticksPerFrame * 4;

    public QuickStrikeAoE(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public QuickStrikeAoE(Level level, boolean mirrored) {
        this(ROACWEntityRegistry.QUICK_STRIKE.get(), level);
        this.getEntityData().set(DATA_IS_MIRRORED, mirrored);
    }

    @Override
    public void applyEffect(LivingEntity target) {
    }

    @Override
    public void tick() {
        if (!firstTick) {
            checkHits();
            firstTick = true;
        }

        if (tickCount >= deathTime) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().invulnerableTime = 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_IS_MIRRORED, false);
    }

    public boolean isMirrored() {
        return this.getEntityData().get(DATA_IS_MIRRORED);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public float getParticleCount() {
        return 0;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}