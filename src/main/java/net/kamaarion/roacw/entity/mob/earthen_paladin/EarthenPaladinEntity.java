package net.kamaarion.roacw.entity.mob.earthen_paladin;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.ForgeMod;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import org.jetbrains.annotations.Nullable;


public class EarthenPaladinEntity extends AbstractSpellCastingMob implements Enemy {

    private static final EntityDataAccessor<Integer> PHASE =
            SynchedEntityData.defineId(EarthenPaladinEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> HAS_OATH =
            SynchedEntityData.defineId(EarthenPaladinEntity.class, EntityDataSerializers.BOOLEAN);


    private static final float PHASE_2_HEALTH_PCT = 0.66F;
    private static final float PHASE_3_HEALTH_PCT = 0.33F;

    public static final RawAnimation PHASE_TRANSITION =
            RawAnimation.begin().thenPlay("phase_transition");

    public static final RawAnimation SCYTHE_DOWNSLASH_SIDESLASH =
            RawAnimation.begin().thenPlay("scythe_downslash_sideslash");

    public static final RawAnimation SCYTHE_LOW_RIGHTWARD_SWEEP =
            RawAnimation.begin().thenPlay("scythe_low_rightward_sweep");

    public EarthenPaladinEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 20;

        this.getNavigation().setCanFloat(true);

        // Water is traversable.
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);

        // Lava should be avoided.
        this.setPathfindingMalus(BlockPathTypes.LAVA, -1.0F);

        // Equip the Paladin's fixed loadout immediately on construction
        // rather than waiting for finalizeSpawn/populateDefaultEquipmentSlots.
        // This ensures loot-mod/JEI/EMI icon rendering (which instantiates
        // the entity directly without running a full spawn cycle) still
        // shows the correct armor and weapon instead of the bare model.
        equipDefaultLoadout();
    }

    /**
     * Equips the Paladin's fixed armor and weapon. Called from the
     * constructor so equipment is present even for entities that never
     * go through finalizeSpawn (e.g. loot-viewer icon entities).
     */
    private void equipDefaultLoadout() {
        this.setItemSlot(
                EquipmentSlot.MAINHAND,
                new ItemStack(ROACWItemRegistry.EARTH_SPLITTER.get())
        );

        this.setItemSlot(
                EquipmentSlot.HEAD,
                new ItemStack(ROACWItemRegistry.EARTHEN_PALADIN_HELMET.get())
        );

        this.setItemSlot(
                EquipmentSlot.CHEST,
                new ItemStack(ROACWItemRegistry.EARTHEN_PALADIN_CHESTPLATE.get())
        );

        this.setItemSlot(
                EquipmentSlot.LEGS,
                new ItemStack(ROACWItemRegistry.EARTHEN_PALADIN_LEGGINGS.get())
        );

        this.setItemSlot(
                EquipmentSlot.FEET,
                new ItemStack(ROACWItemRegistry.EARTHEN_PALADIN_GREAVES.get())
        );

        // Prevent the weapon from being dropped through
        // Minecraft's normal equipment-drop system.
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected GroundPathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);

        // Allow the Paladin to navigate through water.
        navigation.setCanFloat(true);

        return navigation;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public float getScale() {
        return 1.5F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(ForgeMod.ENTITY_REACH.get(), 3.5D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(PHASE, 1);
        this.entityData.define(HAS_OATH, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(
                1,
                new EarthenPaladinAttackGoal(this, 1.0D, 40, 60, 6.0D)
        );

        this.goalSelector.addGoal(
                2,
                new RandomStrollGoal(this, 0.8D)
        );

        this.goalSelector.addGoal(
                3,
                new RandomLookAroundGoal(this)
        );

        this.targetSelector.addGoal(
                1,
                new EarthenPaladinHurtByTargetGoal(this)
        );

        this.targetSelector.addGoal(
                2,
                new EarthenPaladinTargetGoal(this)
        );
    }

    /**
     * Controls where the Earthen Paladin is allowed to naturally spawn.
     *
     * The Paladin must:
     * - Be spawning underground
     * - Not have direct access to the sky
     * - Be in complete block darkness
     * - Be below Y=20
     * - Pass Minecraft's normal mob spawning rules
     */
    /**
     * Checks whether there is enough open space for the Paladin to spawn.
     */
    private static boolean hasEnoughOpenSpace(
            ServerLevelAccessor level,
            BlockPos pos) {

        // The Paladin is 2.9 blocks tall, so only check
        // that the blocks directly above its spawn position
        // aren't solid.
        for (int y = 0; y <= 2; y++) {
            BlockPos checkPos = pos.above(y);

            if (!level.getBlockState(checkPos)
                    .getCollisionShape(level, checkPos)
                    .isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Controls where the Earthen Paladin is allowed to naturally spawn.
     */
    public static boolean checkEarthenPaladinSpawnRules(
            EntityType<EarthenPaladinEntity> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {

        // Must be underground.
        if (level.canSeeSky(pos)) {
            return false;
        }

        // Don't spawn in water or lava.
        if (!level.getFluidState(pos).isEmpty()) {
            return false;
        }

        // Make sure there is enough vertical clearance
        // for the Paladin's 2.9 block tall hitbox.
        if (!hasEnoughOpenSpace(level, pos)) {
            return false;
        }

        // Let Minecraft handle the normal monster spawning rules.
        return Mob.checkMobSpawnRules(
                entityType,
                level,
                spawnType,
                pos,
                random
        );
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData,
            @Nullable CompoundTag dataTag) {

        SpawnGroupData spawnGroupData = super.finalizeSpawn(
                level,
                difficulty,
                reason,
                spawnData,
                dataTag
        );

        // Equipment is already applied in the constructor via
        // equipDefaultLoadout(), so it doesn't need to be repeated here.

        // 25% chance to spawn with the Oath.
        this.setHasOath(
                level.getRandom().nextFloat() < 0.25F
        );

        return spawnGroupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(
            RandomSource random,
            DifficultyInstance difficulty) {

        // Equipment is already applied in the constructor via
        // equipDefaultLoadout(); nothing further needed here.
    }

    @Override
    protected void dropCustomDeathLoot(
            net.minecraft.world.damagesource.DamageSource source,
            int looting,
            boolean recentlyHit) {

        super.dropCustomDeathLoot(
                source,
                looting,
                recentlyHit
        );

        if (this.hasOath()) {
            this.spawnAtLocation(
                    new ItemStack(
                            ROACWItemRegistry.EARTHEN_PALADIN_OATH.get()
                    )
            );
        }
    }

    public boolean hasOath() {
        return this.entityData.get(HAS_OATH);
    }

    public void setHasOath(boolean hasOath) {
        this.entityData.set(HAS_OATH, hasOath);
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    private void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
        this.triggerAnim("action", "phase_transition");
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            updatePhase();
        }
    }

    private void updatePhase() {
        float healthPct = this.getHealth() / this.getMaxHealth();
        int currentPhase = getPhase();

        if (healthPct <= PHASE_3_HEALTH_PCT && currentPhase < 3) {
            setPhase(3);
        } else if (healthPct <= PHASE_2_HEALTH_PCT && currentPhase < 2) {
            setPhase(2);
        }
    }


    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers) {

        super.registerControllers(controllers);

        controllers.add(
                new AnimationController<EarthenPaladinEntity>(
                        this,
                        "action",
                        0,
                        state -> PlayState.STOP
                )
                        .triggerableAnim(
                                "phase_transition",
                                PHASE_TRANSITION
                        )
                        .triggerableAnim(
                                "scythe_downslash_sideslash",
                                SCYTHE_DOWNSLASH_SIDESLASH
                        )
                        .triggerableAnim(
                                "scythe_low_rightward_sweep",
                                SCYTHE_LOW_RIGHTWARD_SWEEP
                        )
        );
    }
}