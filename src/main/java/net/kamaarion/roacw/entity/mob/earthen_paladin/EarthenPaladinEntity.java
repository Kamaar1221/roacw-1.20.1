package net.kamaarion.roacw.entity.mob.earthen_paladin;

import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.kamaarion.roacw.progression.BossGateData;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.UUID;

public class EarthenPaladinEntity extends AbstractSpellCastingMob implements Enemy {

    private static final int MAX_SPAWN_Y = 50;

    private static final EntityDataAccessor<Boolean> HAS_OATH =
            SynchedEntityData.defineId(EarthenPaladinEntity.class, EntityDataSerializers.BOOLEAN);

    private static final UUID ARMOR_CANCELLATION_UUID =
            UUID.fromString("a78e9d31-5c62-4b70-91a3-3ef48b620001");

    private double lastArmorCancellation = Double.NaN;

    public static final RawAnimation SCYTHE_DOWNSLASH_SIDESLASH =
            RawAnimation.begin().thenPlay("scythe_downslash_sideslash");

    public static final RawAnimation SCYTHE_LOW_RIGHTWARD_SWEEP =
            RawAnimation.begin().thenPlay("scythe_low_rightward_sweep");

    public EarthenPaladinEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 20;

        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, -1.0F);

        equipDefaultLoadout();
    }

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

        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
        this.setDropChance(EquipmentSlot.LEGS, 0.0F);
        this.setDropChance(EquipmentSlot.FEET, 0.0F);
    }

    private void updateArmorCancellation() {
        AttributeInstance armorAttribute = this.getAttribute(Attributes.ARMOR);

        if (armorAttribute == null) {
            return;
        }

        double armorToCancel = 0.0D;

        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = this.getItemBySlot(slot);

            if (stack.isEmpty()) {
                continue;
            }

            Multimap<Attribute, AttributeModifier> modifiers =
                    stack.getAttributeModifiers(slot);

            for (AttributeModifier modifier : modifiers.get(Attributes.ARMOR)) {
                if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                    armorToCancel += modifier.getAmount();
                }
            }
        }

        if (Double.compare(this.lastArmorCancellation, armorToCancel) == 0) {
            return;
        }

        armorAttribute.removeModifier(ARMOR_CANCELLATION_UUID);

        if (armorToCancel != 0.0D) {
            armorAttribute.addTransientModifier(
                    new AttributeModifier(
                            ARMOR_CANCELLATION_UUID,
                            "Earthen Paladin armor cancellation",
                            -armorToCancel,
                            AttributeModifier.Operation.ADDITION
                    )
            );
        }

        this.lastArmorCancellation = armorToCancel;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            updateArmorCancellation();
        }
    }

    @Override
    protected GroundPathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
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
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(ForgeMod.ENTITY_REACH.get(), 3.5D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HAS_OATH, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(
                1,
                new EarthenPaladinAttackGoal(this, 1.0D, 40, 60, 6.0D)
        );

        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(
                1,
                new EarthenPaladinHurtByTargetGoal(this)
        );

        this.targetSelector.addGoal(
                2,
                new EarthenPaladinTargetGoal(this)
        );
    }

    private static boolean hasEnoughOpenSpace(
            ServerLevelAccessor level,
            BlockPos pos
    ) {
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

    public static boolean checkEarthenPaladinSpawnRules(
            EntityType<EarthenPaladinEntity> entityType,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        if (!BossGateData.hasKilled(level, EntityType.WITHER)) {
            return false;
        }

        if (pos.getY() >= MAX_SPAWN_Y) {
            return false;
        }

        if (level.canSeeSky(pos)) {
            return false;
        }

        if (level.getBrightness(LightLayer.SKY, pos) > 0) {
            return false;
        }

        if (!level.getFluidState(pos).isEmpty()) {
            return false;
        }

        if (!hasEnoughOpenSpace(level, pos)) {
            return false;
        }

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
            @Nullable CompoundTag dataTag
    ) {
        SpawnGroupData spawnGroupData = super.finalizeSpawn(
                level,
                difficulty,
                reason,
                spawnData,
                dataTag
        );

        this.setHasOath(level.getRandom().nextFloat() < 0.25F);

        return spawnGroupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(
            RandomSource random,
            DifficultyInstance difficulty
    ) {
    }

    @Override
    protected void dropCustomDeathLoot(
            net.minecraft.world.damagesource.DamageSource source,
            int looting,
            boolean recentlyHit
    ) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        if (this.hasOath()) {
            this.spawnAtLocation(
                    new ItemStack(ROACWItemRegistry.EARTHEN_PALADIN_OATH.get())
            );
        }
    }

    public boolean hasOath() {
        return this.entityData.get(HAS_OATH);
    }

    public void setHasOath(boolean hasOath) {
        this.entityData.set(HAS_OATH, hasOath);
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        super.registerControllers(controllers);

        controllers.add(
                new AnimationController<EarthenPaladinEntity>(
                        this,
                        "action",
                        0,
                        state -> PlayState.STOP
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