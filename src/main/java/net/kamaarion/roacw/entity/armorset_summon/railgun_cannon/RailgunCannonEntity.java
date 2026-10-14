package net.kamaarion.roacw.entity.armorset_summon.railgun_cannon;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.acetheeldritchking.cataclysm_spellbooks.registries.CSAttributeRegistry;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.joml.Matrix4f;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RailgunCannonEntity extends Entity implements GeoEntity {

    private static final double SIDE_OFFSET = 1.50D;
    private static final double HEIGHT_OFFSET = 0.75D;
    private static final double FORWARD_OFFSET = 0.4D;

    private static final boolean DEBUG_ATTACKS = true;
    private static final Logger DEBUG_LOG = LogUtils.getLogger();

    private static final double TARGET_SEARCH_RADIUS = 12.0D;
    private static final float MELEE_DAMAGE = 15.0F;
    private static final int ATTACK_COOLDOWN = 10;

    private static final double ENGAGE_RANGE = 8.0D;

    private static final double ATTACK_START_MARGIN = 0.5D;

    private static final int ATTACKS_BEFORE_SPIN = 4;

    private static final double FRONT_HALF_ANGLE_DEGREES = 30.0D;
    private static final double FRONT_MAX_SIDEWAYS = 1.5D;
    private static final double ATTACK_VERTICAL_REACH = 2.5D;

    private static final int SPIN_CONTACT_START_TICK = 15;

    private static final int SPIN_CONTACT_END_TICK = 26;

    private static final double CONTACT_RADIUS = 0.9D;
    private static final double CONTACT_LENGTH = 3.41D;

    private static final int CONTACT_LENGTH_SAMPLES = 11;

    private static final int CONTACT_SUBSTEPS = 4;

    private static final int CONTACT_REHIT_TICKS = 1;

    private static final double CONTACT_SEARCH_RADIUS = 10.0D;

    private static final boolean CONTACT_KNOCKBACK = false;

    private static final double SPIN_PATH_X_SIGN = -1.0D;

    private static final double SPIN_CENTER_Y = 0.339D;
    private static final double SPIN_CENTER_Z = 0.266D;

    private static final double RENDER_SIDE_OFFSET = 2.1D;
    private static final double RENDER_FORWARD_OFFSET = 0.4D;
    private static final double RENDER_HEIGHT_OFFSET = 0.95D;
    private static final double RENDER_INWARD_ANGLE = 12.0D;

    private static final double[][] SPIN_360_POSITION = {
            {0.0000D, 0.00000D, 0.00000D, 0.00000D},
            {0.8340D, -0.31014D, -1.82506D, 0.75329D},
            {1.6660D, -0.80545D, -2.98361D, 2.13734D},
            {2.5000D, -1.28854D, -3.61974D, 3.73818D},
            {3.3340D, -1.69504D, -3.83290D, 5.42398D},
            {4.1660D, -1.98799D, -3.69671D, 7.12880D},
            {5.0000D, -2.13832D, -3.26730D, 8.81037D},
            {5.8340D, -2.11785D, -2.59096D, 10.43629D},
            {6.6660D, -1.89675D, -1.70508D, 11.97801D},
            {7.5000D, -1.43827D, -0.64338D, 13.40690D},
            {8.3340D, -0.69837D, 0.56296D, 14.69168D},
            {9.1660D, 0.38185D, 1.88262D, 15.79506D},
            {10.0000D, 1.88283D, 3.28037D, 16.66989D},
            {10.8340D, 3.92272D, 4.71118D, 17.25266D},
            {11.6660D, 6.68441D, 6.10657D, 17.45076D},
            {12.5000D, 10.48854D, 7.33370D, 17.11949D},
            {13.3340D, 16.00000D, 8.00000D, 16.00000D},
            {14.1660D, 21.41278D, 7.26944D, 11.73609D},
            {15.0000D, 23.52193D, 5.49825D, 1.52693D},
            {15.8340D, 21.30719D, 3.13481D, -15.60448D},
            {16.6660D, 13.49329D, 0.50000D, -39.39645D},
            {17.5000D, -1.26308D, -2.13481D, -65.05307D},
            {18.3340D, -23.79373D, -4.49825D, -81.04093D},
            {19.1660D, -53.52734D, -6.26944D, -78.30828D},
            {20.0000D, -88.19000D, -7.00000D, -58.08000D},
            {20.8340D, -111.02683D, -6.19617D, -30.75420D},
            {21.6660D, -111.46746D, -4.07219D, -4.28894D},
            {22.5000D, -95.59303D, -1.11849D, 18.96234D},
            {23.3340D, -69.34033D, 2.11849D, 35.67312D},
            {24.1660D, -38.57098D, 5.07219D, 42.44382D},
            {25.0000D, -9.14651D, 7.19617D, 37.40462D},
            {25.8340D, 13.00000D, 8.00000D, 21.00000D},
            {26.6660D, 21.92242D, 8.10924D, 7.14612D},
            {27.5000D, 25.39153D, 8.27534D, -0.72553D},
            {28.3340D, 26.85754D, 8.42848D, -6.21810D},
            {29.1660D, 27.18909D, 8.54734D, -10.29382D},
            {30.0000D, 26.77060D, 8.62033D, -13.38103D},
            {30.8340D, 25.81019D, 8.63894D, -15.70985D},
            {31.6660D, 24.43955D, 8.59530D, -17.41788D},
            {32.5000D, 22.74639D, 8.48159D, -18.59223D},
            {33.3340D, 20.79619D, 8.28891D, -19.28849D},
            {34.1660D, 18.63876D, 8.00732D, -19.53818D},
            {35.0000D, 16.31548D, 7.62467D, -19.35591D},
            {35.8340D, 13.86351D, 7.12578D, -18.73901D},
            {36.6660D, 11.31887D, 6.49215D, -17.66448D},
            {37.5000D, 8.72177D, 5.69917D, -16.08167D},
            {38.3340D, 6.12418D, 4.71371D, -13.89648D},
            {39.1660D, 3.60856D, 3.48869D, -10.92422D},
            {40.0000D, 1.34848D, 1.95498D, -6.75365D},
            {40.8340D, 0.00000D, 0.00000D, 0.00000D}
    };

    private static final double[][] SPIN_360_ROTATION = {
            {0.0000D, 0.00000D, 0.00000D, 0.00000D},
            {0.8340D, -5.05597D, 11.14553D, -3.21987D},
            {1.6660D, -8.54179D, 17.90426D, -9.01203D},
            {2.5000D, -10.28424D, 21.70510D, -15.78491D},
            {3.3340D, -10.08297D, 23.35470D, -23.08981D},
            {4.1660D, -7.73041D, 23.37542D, -30.71746D},
            {5.0000D, -3.00236D, 22.12496D, -38.54764D},
            {5.8340D, 4.31080D, 19.87534D, -46.49757D},
            {6.6660D, 14.38017D, 16.83892D, -54.50365D},
            {7.5000D, 27.25249D, 13.20020D, -62.50973D},
            {8.3340D, 42.78222D, 9.12819D, -70.45966D},
            {9.1660D, 60.49904D, 4.79594D, -78.28984D},
            {10.0000D, 79.54307D, 0.40350D, -85.91749D},
            {10.8340D, 98.70734D, -3.78380D, -93.22239D},
            {11.6660D, 116.64938D, -7.34665D, -99.99527D},
            {12.5000D, 132.19343D, -9.44082D, -105.78743D},
            {13.3340D, 144.55960D, -7.41440D, -109.00730D},
            {14.1660D, 151.88980D, -2.05828D, -109.00960D},
            {15.0000D, 151.27642D, 0.95759D, -109.01479D},
            {15.8340D, 140.08574D, 2.37041D, -109.02099D},
            {16.6660D, 115.19607D, 2.66758D, -109.02678D},
            {17.5000D, 75.55328D, 2.21517D, -109.03081D},
            {18.3340D, 29.23679D, 1.32898D, -109.03166D},
            {19.1660D, -7.28834D, 0.32787D, -109.02748D},
            {20.0000D, -25.82830D, -0.39870D, -109.01560D},
            {20.8340D, -42.99405D, -1.08587D, -109.00381D},
            {21.6660D, -76.02662D, -2.20015D, -109.00080D},
            {22.5000D, -167.07332D, -4.96125D, -109.00907D},
            {23.3340D, -201.24732D, -6.19954D, -109.01366D},
            {24.1660D, -221.33890D, -7.07593D, -109.01407D},
            {25.0000D, -229.39040D, -7.41440D, -109.00730D},
            {26.6660D, -234.20955D, -9.48246D, -106.04854D},
            {27.5000D, -241.14301D, -12.92587D, -100.92277D},
            {28.3340D, -249.11666D, -16.68435D, -95.01156D},
            {29.1660D, -257.68156D, -20.46431D, -88.67543D},
            {30.0000D, -266.59066D, -24.12050D, -82.07657D},
            {30.8340D, -275.68560D, -27.55631D, -75.30645D},
            {31.6660D, -284.85137D, -30.69414D, -68.42460D},
            {32.5000D, -293.99495D, -33.45919D, -61.47507D},
            {33.3340D, -303.03430D, -35.77282D, -54.49352D},
            {34.1660D, -311.88931D, -37.54509D, -47.51213D},
            {35.0000D, -320.47749D, -38.66614D, -40.56305D},
            {35.8340D, -328.70402D, -38.99466D, -33.68197D},
            {36.6660D, -336.45735D, -38.34078D, -26.91299D},
            {37.5000D, -343.59288D, -36.43340D, -20.31569D},
            {38.3340D, -349.91526D, -32.85607D, -13.98165D},
            {39.1660D, -355.13271D, -26.90707D, -8.07328D},
            {40.0000D, -358.78094D, -17.19014D, -2.95158D},
            {40.8340D, -360.00000D, 0.00000D, 0.00000D}
    };

    private static final int COMBAT_TIMEOUT_TICKS = 100;

    private static final int COMPRESS_TRANSITION_TICKS = 8;
    private static final int COMPRESSED_BEFORE_SPIN_TICKS = 20;
    private static final int COMPRESSED_AFTER_SPIN_TICKS = 20;
    private static final int COMBAT_GRACE_TICKS = 30;
    private static final int ANIMATION_BLEND_TICKS = 1;

    private static final int SPAWN_PARTICLE_DURATION = 8;

    private static final int SPAWN_ANIMATION_TICKS = 38;

    private static final double SPAWN_PARTICLE_WIDTH = 0.85D;
    private static final double SPAWN_PARTICLE_HEIGHT = 1.83D;
    private static final double SPAWN_PARTICLE_LENGTH = 3.41D;

    private static final double SPAWN_PARTICLE_Y_CENTER = 0.339D;
    private static final double SPAWN_PARTICLE_Z_CENTER = 0.266D;

    private static final int SPAWN_SMOKE_PARTICLES_PER_TICK = 16;
    private static final int SPAWN_ELECTRICITY_PARTICLES_PER_TICK = 8;

    private static final double SPAWN_SMOKE_RISE_SPEED = 0.02D;

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
            SynchedEntityData.defineId(
                    RailgunCannonEntity.class,
                    EntityDataSerializers.OPTIONAL_UUID
            );

    private static final EntityDataAccessor<Byte> DATA_STATE =
            SynchedEntityData.defineId(
                    RailgunCannonEntity.class,
                    EntityDataSerializers.BYTE
            );

    private static final EntityDataAccessor<Byte> DATA_ATTACK =
            SynchedEntityData.defineId(
                    RailgunCannonEntity.class,
                    EntityDataSerializers.BYTE
            );

    private static final EntityDataAccessor<Integer> DATA_ATTACK_SEQ =
            SynchedEntityData.defineId(
                    RailgunCannonEntity.class,
                    EntityDataSerializers.INT
            );

    public enum CannonState {

        UNCOMPRESSED,
        COMPRESSING,
        COMPRESSED,
        SPINNING;

        private static final CannonState[] VALUES = values();

        public static CannonState byId(int id) {
            return VALUES[
                    Mth.clamp(
                            id,
                            0,
                            VALUES.length - 1
                    )
                    ];
        }
    }

    public enum AttackType {

        LUNGE(
                "RailgunCompressedSpinLunge",
                1.4583D,
                6.5D,
                1.0D,
                false,
                1.75F,
                8,
                12
        ),

        SWIPE(
                "RailgunCompressedSpinSwipe",
                2.125D,
                5.0D,
                3.0D,
                false,
                1.5F,
                10,
                17
        ),

        SPIN_360(
                "RailgunCompressedSpin360",
                2.0417D,
                7.0D,
                0.0D,
                false,
                0.5F,
                12
        );

        public final String animationName;
        public final RawAnimation animation;

        private final double lengthSeconds;
        private final double reach;
        private final double halfWidth;
        private final boolean aoe;
        private final float damageMultiplier;
        private final int aimLockTick;
        private final int[] hitTicks;

        AttackType(
                String animationName,
                double lengthSeconds,
                double reach,
                double halfWidth,
                boolean aoe,
                float damageMultiplier,
                int aimLockTick,
                int... hitTicks
        ) {
            this.animationName = animationName;
            this.animation =
                    RawAnimation.begin()
                            .thenPlay(animationName);

            this.lengthSeconds = lengthSeconds;
            this.reach = reach;
            this.halfWidth = halfWidth;
            this.aoe = aoe;
            this.damageMultiplier = damageMultiplier;
            this.aimLockTick = aimLockTick;
            this.hitTicks = hitTicks;
        }

        private static final AttackType[] VALUES = values();

        @Nullable
        public static AttackType byId(int id) {
            return id <= 0 || id > VALUES.length
                    ? null
                    : VALUES[id - 1];
        }

        public byte networkId() {
            return (byte) (this.ordinal() + 1);
        }

        public int durationTicks() {
            return Mth.ceil(
                    this.lengthSeconds * 20.0D
            );
        }

        public boolean isMultiHit() {
            return this.hitTicks.length > 1;
        }
    }

    private static final RawAnimation RAILGUN_SPAWN =
            RawAnimation.begin()
                    .thenPlayAndHold("RailgunSpawn");

    private static final RawAnimation RAILGUN_UNCOMPRESSED_IDLE =
            RawAnimation.begin()
                    .thenLoop("RailgunUncompressedIdle");

    private static final RawAnimation RAILGUN_COMPRESS =
            RawAnimation.begin()
                    .thenPlayAndHold(
                            "RailgunUncompressedTransition"
                    );

    private static final RawAnimation RAILGUN_COMPRESSED =
            RawAnimation.begin()
                    .thenLoop("RailgunCompressed");

    private static final RawAnimation RAILGUN_COMPRESSED_SPIN =
            RawAnimation.begin()
                    .thenLoop("RailgunCompressedSpin");

    private static final String ATTACK_CONTROLLER =
            "attack_controller";

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private LivingEntity attackTarget;

    private int attackCooldown;
    private int attackTicks = -1;

    private int compressTicks;
    private int combatGraceTicks;
    private int stateTicks;

    private int lastSeenAttackSeq = -1;

    @Nullable
    private AttackType currentAttack;

    private int attacksSinceSpin;

    private final Map<UUID, Integer> contactHitTicks =
            new HashMap<>();

    private int spawnParticleTicks = 0;
    private boolean spawnEffectStarted = false;

    private Matrix4f clientModelPose = null;
    private double clientModelOriginX = 0.0D;
    private double clientModelOriginY = 0.0D;
    private double clientModelOriginZ = 0.0D;

    public RailgunCannonEntity(
            EntityType<? extends RailgunCannonEntity> type,
            Level level
    ) {
        super(type, level);

        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public RailgunCannonEntity(
            Level level,
            Player owner
    ) {
        this(
                ROACWEntityRegistry.RAILGUN_CANNON.get(),
                level
        );

        this.setOwner(owner);

        this.updateAttachmentPosition(
                owner,
                true
        );
    }

    @Override
    protected void defineSynchedData() {

        this.entityData.define(
                DATA_OWNER,
                Optional.empty()
        );

        this.entityData.define(
                DATA_STATE,
                (byte) CannonState.UNCOMPRESSED.ordinal()
        );

        this.entityData.define(
                DATA_ATTACK,
                (byte) 0
        );

        this.entityData.define(
                DATA_ATTACK_SEQ,
                0
        );
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void setOwner(Player owner) {

        this.entityData.set(
                DATA_OWNER,
                Optional.of(
                        owner.getUUID()
                )
        );
    }

    @Nullable
    public Player getOwner() {

        Optional<UUID> ownerUUID =
                this.entityData.get(
                        DATA_OWNER
                );

        return ownerUUID
                .map(
                        uuid ->
                                this.level()
                                        .getPlayerByUUID(uuid)
                )
                .orElse(null);
    }

    private void updateAttachmentPosition(
            Player owner
    ) {
        this.updateAttachmentPosition(
                owner,
                false
        );
    }

    private void updateAttachmentPosition(
            Player owner,
            boolean instant
    ) {

        float bodyYaw =
                owner.yBodyRot;

        double yawRadians =
                Math.toRadians(bodyYaw);

        Vec3 forward =
                new Vec3(
                        -Math.sin(yawRadians),
                        0.0D,
                        Math.cos(yawRadians)
                );

        Vec3 left =
                new Vec3(
                        Math.cos(yawRadians),
                        0.0D,
                        Math.sin(yawRadians)
                );

        Vec3 targetPosition =
                owner.position()
                        .add(
                                left.scale(
                                        SIDE_OFFSET
                                )
                        )
                        .add(
                                forward.scale(
                                        FORWARD_OFFSET
                                )
                        )
                        .add(
                                0.0D,
                                HEIGHT_OFFSET,
                                0.0D
                        );

        this.setPos(
                targetPosition.x,
                targetPosition.y,
                targetPosition.z
        );

        if (this.attackTicks < 0) {

            this.setYRot(bodyYaw);
            this.yRotO = bodyYaw;

            this.setXRot(0.0F);
            this.xRotO = 0.0F;
        }

        this.setDeltaMovement(
                Vec3.ZERO
        );

        this.hasImpulse = false;
    }

    @Override
    public void tick() {

        super.tick();

        Player owner =
                this.getOwner();

        if (owner == null
                || !owner.isAlive()) {

            if (!this.level().isClientSide) {
                this.discard();
            }

            return;
        }

        this.updateAttachmentPosition(
                owner
        );

        if (this.level().isClientSide) {

            if (!this.spawnEffectStarted
                    && this.clientModelPose != null) {

                this.spawnEffectStarted = true;
                this.spawnParticleTicks =
                        SPAWN_PARTICLE_DURATION;
            }

            if (this.spawnParticleTicks > 0) {

                this.spawnParticleEffect();

                this.spawnParticleTicks--;
            }

            return;
        }

        if (this.tickCount < SPAWN_ANIMATION_TICKS) {

            return;
        }

        this.updateAnimationState(owner);

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (this.attackTicks >= 0) {

            this.tickAttack(owner);

            return;
        }

        LivingEntity target =
                this.findTarget(owner);

        if (target == null) {

            if (DEBUG_ATTACKS
                    && this.tickCount % 40 == 0
                    && this.isOwnerInCombat(owner)) {

                DEBUG_LOG.info(
                        "[RailgunCannon] owner in combat but findTarget() = null (state={})",
                        this.getCannonState()
                );
            }

            this.attackTarget = null;

            return;
        }

        this.attackTarget = target;

        this.faceTarget(target);

        if (DEBUG_ATTACKS
                && this.tickCount % 20 == 0) {

            DEBUG_LOG.info(
                    "[RailgunCannon] target={} dist={} state={} stateTicks={} cooldown={} ready={} front={} chosen={}",
                    target.getType().getDescriptionId(),
                    String.format(
                            "%.2f",
                            Math.sqrt(
                                    this.distanceToSqr(target)
                            )
                    ),
                    this.getCannonState(),
                    this.stateTicks,
                    this.attackCooldown,
                    this.isReadyToAttack(),
                    this.isInFrontOfOwner(
                            owner,
                            target
                    ),
                    this.chooseAttack(
                            owner,
                            target
                    )
            );
        }

        if (this.attackCooldown <= 0
                && this.isReadyToAttack()) {

            AttackType type =
                    this.chooseAttack(
                            owner,
                            target
                    );

            if (type != null) {
                this.startAttack(type);
            }
        }
    }

    private void spawnParticleEffect() {

        if (!this.isAlive() || this.clientModelPose == null) {
            return;
        }

        double progress =
                (double) this.spawnParticleTicks
                        / SPAWN_PARTICLE_DURATION;

        double scale =
                0.35D
                        + (1.0D - progress)
                        * 0.65D;

        Vec3 center =
                this.modelPointToWorld(
                        0.0D,
                        SPAWN_PARTICLE_Y_CENTER,
                        SPAWN_PARTICLE_Z_CENTER
                );

        for (int i = 0; i < SPAWN_SMOKE_PARTICLES_PER_TICK; i++) {
            this.emitSpawnParticle(
                    ParticleTypes.LARGE_SMOKE,
                    scale,
                    center,
                    SPAWN_SMOKE_RISE_SPEED
            );
        }

        for (int i = 0; i < SPAWN_ELECTRICITY_PARTICLES_PER_TICK; i++) {
            this.emitSpawnParticle(
                    ParticleRegistry.ELECTRICITY_PARTICLE.get(),
                    scale,
                    center,
                    0.0D
            );
        }
    }

    private void emitSpawnParticle(
            ParticleOptions type,
            double scale,
            Vec3 center,
            double riseSpeed
    ) {
        double localX =
                (this.random.nextDouble() - 0.5D)
                        * SPAWN_PARTICLE_WIDTH
                        * scale;

        double localY =
                SPAWN_PARTICLE_Y_CENTER
                        + (this.random.nextDouble() - 0.5D)
                        * SPAWN_PARTICLE_HEIGHT
                        * scale;

        double localZ =
                SPAWN_PARTICLE_Z_CENTER
                        + (this.random.nextDouble() - 0.5D)
                        * SPAWN_PARTICLE_LENGTH
                        * scale;

        Vec3 particle =
                this.modelPointToWorld(localX, localY, localZ);

        Vec3 outward = particle.subtract(center);

        double length = outward.length();

        double velocityX = 0.0D;
        double velocityY = 0.0D;
        double velocityZ = 0.0D;

        if (length > 0.001D) {
            velocityX = (outward.x / length) * 0.015D;
            velocityY = (outward.y / length) * 0.015D;
            velocityZ = (outward.z / length) * 0.015D;
        }

        this.level().addParticle(
                type,
                particle.x,
                particle.y,
                particle.z,
                velocityX,
                velocityY + riseSpeed,
                velocityZ
        );
    }

    public void setClientModelPose(
            Matrix4f pose,
            double originX,
            double originY,
            double originZ
    ) {
        this.clientModelPose = pose;
        this.clientModelOriginX = originX;
        this.clientModelOriginY = originY;
        this.clientModelOriginZ = originZ;
    }

    private Vec3 modelPointToWorld(double x, double y, double z) {

        Vector4f point =
                new Vector4f((float) x, (float) y, (float) z, 1.0F);

        point.mul(this.clientModelPose);

        return new Vec3(
                this.clientModelOriginX + point.x(),
                this.clientModelOriginY + point.y(),
                this.clientModelOriginZ + point.z()
        );
    }

    private void tickAttack(
            Player owner
    ) {

        AttackType type =
                this.currentAttack;

        if (type == null) {

            this.stopAttack(owner);

            return;
        }

        this.attackTicks++;

        if (this.attackTicks
                >= type.durationTicks()) {

            this.stopAttack(owner);

            return;
        }

        if (this.attackTicks <= type.aimLockTick
                && this.isValidTarget(
                owner,
                this.attackTarget
        )) {

            this.faceTarget(
                    this.attackTarget
            );
        }

        if (type == AttackType.SPIN_360) {

            this.tickSpinContact(
                    owner,
                    type
            );
        }

        for (int hitTick : type.hitTicks) {

            if (type != AttackType.SPIN_360
                    && this.attackTicks == hitTick) {

                this.performAttackHit(
                        owner,
                        type
                );
            }
        }
    }

    @Nullable
    private LivingEntity findTarget(
            Player owner
    ) {

        LivingEntity lastHurt =
                owner.getLastHurtMob();

        if (this.isValidTarget(
                owner,
                lastHurt
        )) {

            return lastHurt;
        }

        LivingEntity closest =
                null;

        double closestDistance =
                Double.MAX_VALUE;

        for (
                LivingEntity entity :
                this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        owner.getBoundingBox()
                                .inflate(
                                        TARGET_SEARCH_RADIUS
                                ),
                        candidate ->
                                this.isValidTarget(
                                        owner,
                                        candidate
                                )
                )
        ) {

            double distance =
                    this.distanceToSqr(
                            entity
                    );

            if (distance
                    < closestDistance) {

                closestDistance =
                        distance;

                closest = entity;
            }
        }

        return closest;
    }

    private boolean isValidTarget(
            Player owner,
            @Nullable LivingEntity entity
    ) {

        if (entity == null
                || !entity.isAlive()
                || entity == owner
                || entity.isSpectator()) {

            return false;
        }

        ResourceLocation id =
                ForgeRegistries.ENTITY_TYPES.getKey(
                        entity.getType()
                );

        if (id != null
                && id.getNamespace()
                .equals("dummmmmmy")) {

            return true;
        }

        if (entity instanceof Monster) {
            return true;
        }

        if (entity instanceof Mob mob) {

            return mob.getTarget()
                    == owner;
        }

        return owner.getLastHurtMob()
                == entity;
    }

    private boolean isOwnerInCombat(
            Player owner
    ) {

        int ticksSinceDealtDamage =
                owner.tickCount
                        - owner.getLastHurtMobTimestamp();

        int ticksSinceTookDamage =
                owner.tickCount
                        - owner.getLastHurtByMobTimestamp();

        boolean recentlyDealtDamage =
                owner.getLastHurtMob() != null
                        && ticksSinceDealtDamage >= 0
                        && ticksSinceDealtDamage
                        <= COMBAT_TIMEOUT_TICKS;

        boolean recentlyTookDamage =
                owner.getLastHurtByMob() != null
                        && ticksSinceTookDamage >= 0
                        && ticksSinceTookDamage
                        <= COMBAT_TIMEOUT_TICKS;

        return recentlyDealtDamage
                || recentlyTookDamage;
    }

    private void faceTarget(
            LivingEntity target
    ) {

        double dx =
                target.getX()
                        - this.getX();

        double dy =
                target.getEyeY()
                        - this.getEyeY();

        double dz =
                target.getZ()
                        - this.getZ();

        double horizontalDistance =
                Math.sqrt(
                        dx * dx
                                + dz * dz
                );

        float yaw =
                (float) (
                        Math.toDegrees(
                                Math.atan2(
                                        dz,
                                        dx
                                )
                        )
                                - 90.0D
                );

        float pitch =
                (float) (
                        -Math.toDegrees(
                                Math.atan2(
                                        dy,
                                        horizontalDistance
                                )
                        )
                );

        this.setYRot(yaw);
        this.yRotO = yaw;

        this.setXRot(pitch);
        this.xRotO = pitch;
    }

    @Nullable
    private AttackType chooseAttack(
            Player owner,
            LivingEntity target
    ) {

        double distanceSqr =
                this.distanceToSqr(
                        target
                );

        if (this.attacksSinceSpin
                >= ATTACKS_BEFORE_SPIN) {

            return distanceSqr
                    <= AttackType.SPIN_360.reach
                    * AttackType.SPIN_360.reach
                    ? AttackType.SPIN_360
                    : null;
        }

        if (this.isInFrontOfOwner(
                owner,
                target
        )) {

            double lungeStart =
                    AttackType.LUNGE.reach
                            - ATTACK_START_MARGIN;

            return distanceSqr
                    <= lungeStart * lungeStart
                    ? AttackType.LUNGE
                    : null;
        }

        double swipeStart =
                AttackType.SWIPE.reach
                        - ATTACK_START_MARGIN;

        return distanceSqr
                <= swipeStart * swipeStart
                ? AttackType.SWIPE
                : null;
    }

    private boolean isInFrontOfOwner(
            Player owner,
            LivingEntity target
    ) {

        double dx =
                target.getX()
                        - owner.getX();

        double dz =
                target.getZ()
                        - owner.getZ();

        double length =
                Math.sqrt(
                        dx * dx
                                + dz * dz
                );

        if (length < 1.0E-4D) {
            return true;
        }

        double yawRadians =
                Math.toRadians(
                        owner.getYRot()
                );

        double forwardX =
                -Math.sin(yawRadians);

        double forwardZ =
                Math.cos(yawRadians);

        double along =
                dx * forwardX
                        + dz * forwardZ;

        double across =
                Math.abs(
                        -dx * forwardZ
                                + dz * forwardX
                );

        double cosAngle =
                along / length;

        return along > 0.0D
                && across <= FRONT_MAX_SIDEWAYS
                && cosAngle >= Math.cos(
                Math.toRadians(
                        FRONT_HALF_ANGLE_DEGREES
                )
        );
    }

    private boolean isReadyToAttack() {

        return this.getCannonState()
                == CannonState.SPINNING
                && this.stateTicks
                >= ANIMATION_BLEND_TICKS;
    }

    private void startAttack(
            AttackType type
    ) {

        this.currentAttack =
                type;

        this.attacksSinceSpin =
                type == AttackType.SPIN_360
                        ? 0
                        : this.attacksSinceSpin + 1;

        this.attackTicks = 0;

        this.contactHitTicks.clear();

        this.entityData.set(
                DATA_ATTACK,
                type.networkId()
        );

        this.entityData.set(
                DATA_ATTACK_SEQ,
                this.entityData.get(
                        DATA_ATTACK_SEQ
                ) + 1
        );

        if (DEBUG_ATTACKS) {

            DEBUG_LOG.info(
                    "[RailgunCannon] START {} (attacksSinceSpin={})",
                    type,
                    this.attacksSinceSpin
            );
        }
    }

    private void stopAttack(
            Player owner
    ) {

        this.attackTicks = -1;

        this.currentAttack = null;

        this.contactHitTicks.clear();

        this.entityData.set(
                DATA_ATTACK,
                (byte) 0
        );

        this.attackCooldown =
                ATTACK_COOLDOWN;

        this.attackTarget = null;

        this.updateAttachmentPosition(
                owner
        );
    }

    private void performAttackHit(
            Player owner,
            AttackType type
    ) {

        double yawRadians =
                Math.toRadians(
                        this.getYRot()
                );

        double forwardX =
                -Math.sin(yawRadians);

        double forwardZ =
                Math.cos(yawRadians);

        double searchRadius =
                type.reach
                        + type.halfWidth;

        List<LivingEntity> candidates =
                this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        this.getBoundingBox()
                                .inflate(
                                        searchRadius,
                                        ATTACK_VERTICAL_REACH,
                                        searchRadius
                                ),
                        candidate ->
                                this.isValidTarget(
                                        owner,
                                        candidate
                                )
                );

        float damage =
                this.getMeleeDamage()
                        * type.damageMultiplier;

        int landed = 0;

        for (LivingEntity victim : candidates) {

            double dx =
                    victim.getX()
                            - this.getX();

            double dz =
                    victim.getZ()
                            - this.getZ();

            double along =
                    dx * forwardX
                            + dz * forwardZ;

            double across =
                    Math.abs(
                            dx * -forwardZ
                                    + dz * forwardX
                    );

            if (along < -0.75D
                    || along > type.reach
                    || across > type.halfWidth) {

                continue;
            }

            if (type.isMultiHit()) {
                victim.invulnerableTime = 0;
            }

            victim.hurt(
                    this.damageSources()
                            .playerAttack(owner),
                    damage
            );

            landed++;
        }

        if (DEBUG_ATTACKS) {

            DEBUG_LOG.info(
                    "[RailgunCannon] HIT {} tick={} candidates={} landed={} damage={}",
                    type,
                    this.attackTicks,
                    candidates.size(),
                    landed,
                    damage
            );
        }
    }

    private void tickSpinContact(
            Player owner,
            AttackType type
    ) {

        if (this.attackTicks
                < SPIN_CONTACT_START_TICK
                || this.attackTicks
                > SPIN_CONTACT_END_TICK) {

            return;
        }

        double previousTick =
                Math.max(
                        0.0D,
                        this.attackTicks - 1.0D
                );

        double currentTick =
                this.attackTicks;

        List<LivingEntity> candidates =
                this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        this.getBoundingBox()
                                .inflate(
                                        CONTACT_SEARCH_RADIUS
                                ),
                        candidate ->
                                this.isValidTarget(
                                        owner,
                                        candidate
                                )
                );

        if (candidates.isEmpty()) {
            return;
        }

        float damage =
                this.getMeleeDamage()
                        * type.damageMultiplier;

        for (
                int step = 1;
                step <= CONTACT_SUBSTEPS;
                step++
        ) {

            double interpolation =
                    (double) step
                            / CONTACT_SUBSTEPS;

            double sampleTick =
                    previousTick
                            + (
                            currentTick
                                    - previousTick
                    )
                            * interpolation;

            SpinTransform transform =
                    this.sampleSpinTransform(
                            sampleTick
                    );

            Vec3 localAxis =
                    transform.localZAxis;

            Vec3 start =
                    transform.center.subtract(
                            localAxis.scale(
                                    CONTACT_LENGTH * 0.5D
                            )
                    );

            Vec3 end =
                    transform.center.add(
                            localAxis.scale(
                                    CONTACT_LENGTH * 0.5D
                            )
                    );

            for (
                    int lengthIndex = 0;
                    lengthIndex < CONTACT_LENGTH_SAMPLES;
                    lengthIndex++
            ) {

                double progress =
                        CONTACT_LENGTH_SAMPLES <= 1
                                ? 0.5D
                                : (double) lengthIndex
                                  / (
                                CONTACT_LENGTH_SAMPLES - 1
                        );

                Vec3 contactPoint =
                        start.lerp(
                                end,
                                progress
                        );

                for (
                        LivingEntity victim :
                        candidates
                ) {

                    if (!victim.isAlive()) {
                        continue;
                    }

                    Integer lastHit =
                            this.contactHitTicks.get(
                                    victim.getUUID()
                            );

                    if (lastHit != null
                            && this.attackTicks - lastHit
                            < CONTACT_REHIT_TICKS) {

                        continue;
                    }

                    if (distanceSqrToBox(
                            contactPoint,
                            victim.getBoundingBox()
                    ) > CONTACT_RADIUS
                            * CONTACT_RADIUS) {

                        continue;
                    }

                    victim.invulnerableTime = 0;

                    Vec3 motionBeforeHit =
                            victim.getDeltaMovement();

                    boolean damaged =
                            victim.hurt(
                                    this.damageSources()
                                            .playerAttack(owner),
                                    damage
                            );

                    if (!CONTACT_KNOCKBACK) {

                        victim.setDeltaMovement(
                                motionBeforeHit
                        );

                        victim.hurtMarked = false;
                    }

                    if (damaged) {

                        this.contactHitTicks.put(
                                victim.getUUID(),
                                this.attackTicks
                        );

                        if (DEBUG_ATTACKS) {

                            DEBUG_LOG.info(
                                    "[RailgunCannon] CONTACT {} tick={} target={} damage={} point={} axis={}",
                                    type,
                                    this.attackTicks,
                                    victim.getType()
                                            .getDescriptionId(),
                                    damage,
                                    contactPoint,
                                    localAxis
                            );
                        }
                    }
                }
            }
        }

        if (DEBUG_ATTACKS
                && this.level()
                instanceof ServerLevel serverLevel) {

            SpinTransform debugTransform =
                    this.sampleSpinTransform(
                            currentTick
                    );

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    debugTransform.center.x,
                    debugTransform.center.y,
                    debugTransform.center.z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private static class SpinTransform {

        private final Vec3 center;
        private final Vec3 localZAxis;

        private SpinTransform(
                Vec3 center,
                Vec3 localZAxis
        ) {
            this.center = center;
            this.localZAxis = localZAxis;
        }
    }

    private SpinTransform sampleSpinTransform(
            double tick
    ) {

        double[] position =
                sampleKeyframe(
                        SPIN_360_POSITION,
                        tick
                );

        double[] rotation =
                sampleKeyframe(
                        SPIN_360_ROTATION,
                        tick
                );

        Vec3 localBonePosition =
                new Vec3(
                        SPIN_PATH_X_SIGN
                                * position[0]
                                / 16.0D,

                        position[1]
                                / 16.0D,

                        position[2]
                                / 16.0D
                );

        Vec3 localCenterOffset =
                new Vec3(
                        0.0D,
                        SPIN_CENTER_Y,
                        SPIN_CENTER_Z
                );

        Vec3 rotatedCenterOffset =
                rotateModelVector(
                        localCenterOffset,
                        rotation[0],
                        rotation[1],
                        rotation[2]
                );

        Vec3 localCenter =
                localBonePosition.add(
                        rotatedCenterOffset
                );

        Player owner =
                this.getOwner();

        double originX =
                this.getX();

        double originY =
                this.getY();

        double originZ =
                this.getZ();

        double modelYawDegrees =
                180.0D - this.getYRot();

        if (owner != null) {

            double bodyYawRadians =
                    Math.toRadians(
                            owner.yBodyRot
                    );

            double forwardX =
                    -Math.sin(
                            bodyYawRadians
                    );

            double forwardZ =
                    Math.cos(
                            bodyYawRadians
                    );

            double leftX =
                    Math.cos(
                            bodyYawRadians
                    );

            double leftZ =
                    Math.sin(
                            bodyYawRadians
                    );

            originX =
                    owner.getX()
                            + leftX
                            * RENDER_SIDE_OFFSET
                            + forwardX
                            * RENDER_FORWARD_OFFSET;

            originY =
                    owner.getY()
                            + RENDER_HEIGHT_OFFSET;

            originZ =
                    owner.getZ()
                            + leftZ
                            * RENDER_SIDE_OFFSET
                            + forwardZ
                            * RENDER_FORWARD_OFFSET;

            modelYawDegrees =
                    180.0D
                            - owner.yBodyRot
                            - RENDER_INWARD_ANGLE;
        }

        double modelYaw =
                Math.toRadians(
                        modelYawDegrees
                );

        double sinYaw =
                Math.sin(modelYaw);

        double cosYaw =
                Math.cos(modelYaw);

        double worldX =
                localCenter.x * cosYaw
                        + localCenter.z * sinYaw;

        double worldZ =
                -localCenter.x * sinYaw
                        + localCenter.z * cosYaw;

        Vec3 worldCenter =
                new Vec3(
                        originX + worldX,
                        originY + localCenter.y,
                        originZ + worldZ
                );

        Vec3 localZ =
                rotateModelVector(
                        new Vec3(
                                0.0D,
                                0.0D,
                                1.0D
                        ),
                        rotation[0],
                        rotation[1],
                        rotation[2]
                ).normalize();

        double axisX =
                localZ.x * cosYaw
                        + localZ.z * sinYaw;

        double axisZ =
                -localZ.x * sinYaw
                        + localZ.z * cosYaw;

        Vec3 worldAxis =
                new Vec3(
                        axisX,
                        localZ.y,
                        axisZ
                ).normalize();

        return new SpinTransform(
                worldCenter,
                worldAxis
        );
    }

    private static Vec3 rotateModelVector(
            Vec3 vector,
            double rotationX,
            double rotationY,
            double rotationZ
    ) {

        double rx =
                Math.toRadians(
                        rotationX
                );

        double ry =
                Math.toRadians(
                        rotationY
                );

        double rz =
                Math.toRadians(
                        rotationZ
                );

        double sinX =
                Math.sin(rx);

        double cosX =
                Math.cos(rx);

        double sinY =
                Math.sin(ry);

        double cosY =
                Math.cos(ry);

        double sinZ =
                Math.sin(rz);

        double cosZ =
                Math.cos(rz);

        double x1 =
                vector.x;

        double y1 =
                vector.y * cosX
                        - vector.z * sinX;

        double z1 =
                vector.y * sinX
                        + vector.z * cosX;

        double x2 =
                x1 * cosY
                        + z1 * sinY;

        double y2 =
                y1;

        double z2 =
                -x1 * sinY
                        + z1 * cosY;

        double x3 =
                x2 * cosZ
                        - y2 * sinZ;

        double y3 =
                x2 * sinZ
                        + y2 * cosZ;

        double z3 =
                z2;

        return new Vec3(
                x3,
                y3,
                z3
        );
    }

    private static double[] sampleKeyframe(
            double[][] keyframes,
            double tick
    ) {

        if (tick <= keyframes[0][0]) {

            return new double[]{
                    keyframes[0][1],
                    keyframes[0][2],
                    keyframes[0][3]
            };
        }

        for (
                int i = 1;
                i < keyframes.length;
                i++
        ) {

            if (tick <= keyframes[i][0]) {

                double[] a =
                        keyframes[i - 1];

                double[] b =
                        keyframes[i];

                double span =
                        b[0] - a[0];

                double progress =
                        span <= 1.0E-6D
                                ? 1.0D
                                : (
                                tick - a[0]
                        )
                                  / span;

                progress =
                        Mth.clamp(
                                progress,
                                0.0D,
                                1.0D
                        );

                return new double[]{
                        Mth.lerp(
                                progress,
                                a[1],
                                b[1]
                        ),
                        Mth.lerp(
                                progress,
                                a[2],
                                b[2]
                        ),
                        Mth.lerp(
                                progress,
                                a[3],
                                b[3]
                        )
                };
            }
        }

        double[] last =
                keyframes[
                        keyframes.length - 1
                        ];

        return new double[]{
                last[1],
                last[2],
                last[3]
        };
    }

    private static double distanceSqrToBox(
            Vec3 point,
            AABB box
    ) {

        double dx =
                point.x
                        - Mth.clamp(
                        point.x,
                        box.minX,
                        box.maxX
                );

        double dy =
                point.y
                        - Mth.clamp(
                        point.y,
                        box.minY,
                        box.maxY
                );

        double dz =
                point.z
                        - Mth.clamp(
                        point.z,
                        box.minZ,
                        box.maxZ
                );

        return dx * dx
                + dy * dy
                + dz * dz;
    }

    protected float getMeleeDamage() {

        Player owner =
                this.getOwner();

        if (owner == null) {
            return MELEE_DAMAGE;
        }

        double technomancyPower =
                owner.getAttributeValue(
                        CSAttributeRegistry
                                .TECHNOMANCY_MAGIC_POWER
                                .get()
                );

        double lightningPower =
                owner.getAttributeValue(
                        AttributeRegistry
                                .LIGHTNING_SPELL_POWER
                                .get()
                );

        double powerScaling =
                1.0D
                        + (
                        technomancyPower
                                * 0.25D
                )
                        + (
                        lightningPower
                                * 0.25D
                );

        return (float) (
                MELEE_DAMAGE
                        * powerScaling
        );
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(
            net.minecraft.world.damagesource.DamageSource source
    ) {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(
            CompoundTag tag
    ) {

        this.entityData
                .get(DATA_OWNER)
                .ifPresent(
                        uuid ->
                                tag.putUUID(
                                        "Owner",
                                        uuid
                                )
                );
    }

    @Override
    protected void readAdditionalSaveData(
            CompoundTag tag
    ) {

        if (tag.hasUUID("Owner")) {

            this.entityData.set(
                    DATA_OWNER,
                    Optional.of(
                            tag.getUUID("Owner")
                    )
            );
        }
    }

    private <E extends RailgunCannonEntity> PlayState animationPredicate(
            AnimationState<E> state
    ) {

        if (state.getAnimatable().tickCount < SPAWN_ANIMATION_TICKS) {

            state.getController().transitionLength(0);

            return state.setAndContinue(
                    RAILGUN_SPAWN
            );
        }

        state.getController().transitionLength(ANIMATION_BLEND_TICKS);

        RawAnimation animation =
                switch (
                        state.getAnimatable()
                                .getCannonState()
                        ) {

                    case UNCOMPRESSED ->
                            RAILGUN_UNCOMPRESSED_IDLE;

                    case COMPRESSING ->
                            RAILGUN_COMPRESS;

                    case COMPRESSED ->
                            RAILGUN_COMPRESSED;

                    case SPINNING ->
                            RAILGUN_COMPRESSED_SPIN;
                };

        return state.setAndContinue(
                animation
        );
    }

    private <E extends RailgunCannonEntity> PlayState attackPredicate(
            AnimationState<E> state
    ) {

        AttackType type =
                state.getAnimatable()
                        .getSyncedAttack();

        if (type == null) {
            return PlayState.STOP;
        }

        int sequence =
                this.entityData.get(
                        DATA_ATTACK_SEQ
                );

        if (sequence
                != this.lastSeenAttackSeq) {

            this.lastSeenAttackSeq =
                    sequence;

            state.getController()
                    .forceAnimationReset();
        }

        return state.setAndContinue(
                type.animation
        );
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {

        controllers.add(
                new AnimationController<>(
                        this,
                        "railgun_controller",
                        ANIMATION_BLEND_TICKS,
                        this::animationPredicate
                )
        );

        AnimationController<RailgunCannonEntity>
                attackController =
                new AnimationController<>(
                        this,
                        ATTACK_CONTROLLER,
                        0,
                        this::attackPredicate
                );

        controllers.add(
                attackController
        );
    }

    public CannonState getCannonState() {

        return CannonState.byId(
                this.entityData.get(
                        DATA_STATE
                )
        );
    }

    @Nullable
    public AttackType getSyncedAttack() {

        return AttackType.byId(
                this.entityData.get(
                        DATA_ATTACK
                )
        );
    }

    private void setCannonState(
            CannonState state
    ) {

        this.entityData.set(
                DATA_STATE,
                (byte) state.ordinal()
        );

        this.stateTicks = 0;

        if (state
                == CannonState.UNCOMPRESSED) {

            this.attacksSinceSpin = 0;
        }
    }

    public boolean isCompressed() {

        return this.getCannonState()
                != CannonState.UNCOMPRESSED;
    }

    private void updateAnimationState(
            Player owner
    ) {

        this.stateTicks++;

        boolean attacking =
                this.attackTicks >= 0;

        boolean targetEngaged =
                this.attackTarget != null
                        && this.attackTarget.isAlive()
                        && this.distanceToSqr(
                        this.attackTarget
                )
                        <= ENGAGE_RANGE
                        * ENGAGE_RANGE;

        boolean combatNow =
                attacking
                        || targetEngaged
                        || this.isOwnerInCombat(
                        owner
                );

        if (combatNow) {

            this.combatGraceTicks =
                    COMBAT_GRACE_TICKS;

        } else if (
                this.combatGraceTicks > 0
        ) {

            this.combatGraceTicks--;
        }

        boolean inCombat =
                this.combatGraceTicks > 0;

        switch (
                this.getCannonState()
        ) {

            case UNCOMPRESSED -> {

                if (inCombat) {

                    this.compressTicks =
                            COMPRESS_TRANSITION_TICKS;

                    this.setCannonState(
                            CannonState.COMPRESSING
                    );
                }
            }

            case COMPRESSING -> {

                if (--this.compressTicks <= 0) {

                    this.setCannonState(
                            CannonState.COMPRESSED
                    );
                }
            }

            case COMPRESSED -> {

                if (inCombat) {

                    if (this.stateTicks
                            >= COMPRESSED_BEFORE_SPIN_TICKS) {

                        this.setCannonState(
                                CannonState.SPINNING
                        );
                    }

                } else if (
                        this.stateTicks
                                >= COMPRESSED_AFTER_SPIN_TICKS
                ) {

                    this.setCannonState(
                            CannonState.UNCOMPRESSED
                    );
                }
            }

            case SPINNING -> {

                if (!inCombat) {

                    this.setCannonState(
                            CannonState.COMPRESSED
                    );
                }
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}