package net.kamaarion.roacw.items.weapons.the_hive;

import net.kamaarion.roacw.entity.projectile.plague_rocket.PlagueRocketEntity;
import net.kamaarion.roacw.entity.projectile.plague_nuke.PlagueNukeEntity;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWSoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class TheHive extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final int NUKE_CHARGE_TICKS = 80;
    private static final int NUKE_CHARGE_SOUND_START = 5;

    private static final int ROCKET_COUNT = 4;
    private static final float ROCKET_VELOCITY = 1.5F;
    private static final float NUKE_VELOCITY = 0.9F;

    private static final float DIAGONAL_SPREAD_DEGREES = 3.5355F;

    private static final float[][] ROCKET_SPREAD_OFFSETS = {
            {-DIAGONAL_SPREAD_DEGREES, DIAGONAL_SPREAD_DEGREES},
            {DIAGONAL_SPREAD_DEGREES, DIAGONAL_SPREAD_DEGREES},
            {-DIAGONAL_SPREAD_DEGREES, -DIAGONAL_SPREAD_DEGREES},
            {DIAGONAL_SPREAD_DEGREES, -DIAGONAL_SPREAD_DEGREES}
    };

    private static final double MUZZLE_FORWARD = 1.1D;
    private static final double MUZZLE_RIGHT = 0.35D;
    private static final double MUZZLE_UP = -0.2D;

    private static final int ROCKET_COOLDOWN_TICKS = 60;
    private static final int NUKE_COOLDOWN_TICKS = 100;

    public TheHive(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private TheHiveRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new TheHiveRenderer();
                }

                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseTicks) {
        super.onUseTick(level, living, stack, remainingUseTicks);

        int ticksHeld = getUseDuration(stack) - remainingUseTicks;

        if (ticksHeld == NUKE_CHARGE_SOUND_START && !level.isClientSide) {
            level.playSound(
                    null,
                    living.getX(),
                    living.getY(),
                    living.getZ(),
                    ROACWSoundRegistry.HIVE_NUKE_CHARGE.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        if (ticksHeld == NUKE_CHARGE_TICKS && !level.isClientSide) {
            level.playSound(
                    null,
                    living.getX(),
                    living.getY(),
                    living.getZ(),
                    ROACWSoundRegistry.HIVE_NUKE_READY.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) {
            return;
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return;
        }

        int ticksHeld = getUseDuration(stack) - timeLeft;
        boolean fireNuke = ticksHeld >= NUKE_CHARGE_TICKS;

        if (!level.isClientSide) {
            if (fireNuke) {
                fireNuke(level, player);
                player.getCooldowns().addCooldown(this, NUKE_COOLDOWN_TICKS);
            } else {
                fireRocketBurst(level, player);
                player.getCooldowns().addCooldown(this, ROCKET_COOLDOWN_TICKS);
            }
        }
    }

    private static Vec3[] computeLocalBasis(Player player) {
        Vec3 forward = player.getLookAngle().normalize();
        Vec3 worldUp = Math.abs(forward.y) > 0.99D
                ? new Vec3(1.0D, 0.0D, 0.0D)
                : new Vec3(0.0D, 1.0D, 0.0D);

        Vec3 right = forward.cross(worldUp).normalize();
        Vec3 up = right.cross(forward).normalize();

        return new Vec3[]{forward, right, up};
    }

    private static Vec3 computeMuzzlePos(Player player, Vec3 forward, Vec3 right, Vec3 up) {
        return new Vec3(player.getX(), player.getEyeY(), player.getZ())
                .add(forward.scale(MUZZLE_FORWARD))
                .add(right.scale(MUZZLE_RIGHT))
                .add(up.scale(MUZZLE_UP));
    }

    private void fireRocketBurst(Level level, Player player) {
        Vec3[] basis = computeLocalBasis(player);
        Vec3 forward = basis[0];
        Vec3 right = basis[1];
        Vec3 up = basis[2];
        Vec3 muzzle = computeMuzzlePos(player, forward, right, up);

        for (int i = 0; i < ROCKET_COUNT; i++) {
            PlagueRocketEntity rocket = new PlagueRocketEntity(
                    ROACWEntityRegistry.PLAGUE_ROCKET.get(),
                    level,
                    player
            );

            rocket.setPos(muzzle.x, muzzle.y, muzzle.z);

            float[] offset = ROCKET_SPREAD_OFFSETS[i];
            double rightRad = Math.toRadians(offset[0]);
            double upRad = Math.toRadians(offset[1]);

            Vec3 direction = forward
                    .add(right.scale(Math.sin(rightRad)))
                    .add(up.scale(Math.sin(upRad)))
                    .normalize();

            rocket.shoot(
                    direction.x,
                    direction.y,
                    direction.z,
                    ROCKET_VELOCITY,
                    0.0F
            );

            level.addFreshEntity(rocket);
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ROACWSoundRegistry.HIVE_ROCKET_LAUNCH.get(),
                SoundSource.PLAYERS,
                1.0F,
                0.9F
        );
    }

    private void fireNuke(Level level, Player player) {
        Vec3[] basis = computeLocalBasis(player);
        Vec3 forward = basis[0];
        Vec3 muzzle = computeMuzzlePos(player, forward, basis[1], basis[2]);

        PlagueNukeEntity nuke = new PlagueNukeEntity(
                ROACWEntityRegistry.PLAGUE_NUKE.get(),
                level,
                player
        );

        nuke.setPos(muzzle.x, muzzle.y, muzzle.z);
        nuke.shoot(
                forward.x,
                forward.y,
                forward.z,
                NUKE_VELOCITY,
                0.0F
        );

        level.addFreshEntity(nuke);

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ROACWSoundRegistry.HIVE_NUKE_LAUNCH.get(),
                SoundSource.PLAYERS,
                1.0F,
                0.6F
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        if (level != null && level.isClientSide()) {
            net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
            int maxTooltipWidth = 240;

            Component loreText = Component.translatable("item.roacw.the_hive.desc")
                    .withStyle(ChatFormatting.GREEN);

            font.getSplitter()
                    .splitLines(loreText, maxTooltipWidth, loreText.getStyle())
                    .forEach(formattedText ->
                            tooltipComponents.add(
                                    Component.literal(formattedText.getString())
                                            .withStyle(loreText.getStyle())
                            )
                    );

            tooltipComponents.add(Component.empty());

            Component tooltipText = Component.translatable("item.roacw.the_hive.tooltip")
                    .withStyle(ChatFormatting.GRAY);

            font.getSplitter()
                    .splitLines(tooltipText, maxTooltipWidth, tooltipText.getStyle())
                    .forEach(formattedText ->
                            tooltipComponents.add(
                                    Component.literal(formattedText.getString())
                                            .withStyle(tooltipText.getStyle())
                            )
                    );
        } else {
            super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        }
    }
}