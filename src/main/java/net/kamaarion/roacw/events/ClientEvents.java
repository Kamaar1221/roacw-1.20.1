package net.kamaarion.roacw.events;

import net.kamaarion.roacw.client.others.CustomAnimatedParticle;
import net.kamaarion.roacw.entity.mob.earthen_paladin.EarthenPaladinRenderer;
import net.kamaarion.roacw.entity.projectile.belladonna_petal.BelladonnaPetalRenderer;
import net.kamaarion.roacw.entity.projectile.god_killer_dart.GodKillerDartRenderer;
import net.kamaarion.roacw.entity.projectile.plague_charger_stinger.PlagueChargerStingerRenderer;
import net.kamaarion.roacw.entity.projectile.plague_cloud.PlagueCloudRenderer;
import net.kamaarion.roacw.entity.projectile.plague_nuke.PlagueNukeRenderer;
import net.kamaarion.roacw.entity.projectile.plague_rocket.PlagueRocketRenderer;
import net.kamaarion.roacw.entity.spells.burning_meteor.BurningMeteorRenderer;
import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueAoERenderer;
import net.kamaarion.roacw.entity.spells.earthly_virtue.EarthlyVirtueShardsRenderer;
import net.kamaarion.roacw.entity.spells.impaling_column.ImpalingColumnShardsRenderer;
import net.kamaarion.roacw.entity.summon.belladonna_spirit.BelladonnaSpiritRenderer;
import net.kamaarion.roacw.entity.summon.dark_raven.DarkRavenRenderer;
import net.kamaarion.roacw.entity.summon.plague_charger.PlagueChargerRenderer;
import net.kamaarion.roacw.items.curios.high_ruler_shield.HighRulerShield;
import net.kamaarion.roacw.items.curios.high_ruler_shield.HighRulerShieldCurioRenderer;
import net.kamaarion.roacw.particle.AuricChargeLightningParticle;
import net.kamaarion.roacw.particle.BoosterExhaustParticle;
import net.kamaarion.roacw.particle.PlagueCloudParticle;
import net.kamaarion.roacw.particle.PlagueNanoParticle;
import net.kamaarion.roacw.registeries.ROACWEntityRegistry;
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.isEntityUpsideDown;

public class ClientEvents {

  @Mod.EventBusSubscriber(modid = "roacw", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
  public static class ForgeBusEvents {

    @SubscribeEvent
    public static void renderArm(RenderArmEvent event) {
      if (event.getArm() != event.getPlayer().getMainArm()) return;

      CuriosApi.getCuriosInventory(event.getPlayer()).ifPresent(handler -> {
        handler.findCurios(ROACWItemRegistry.ELEMENTAL_GAUNTLET.get()).forEach(slotResult -> {

          ItemStack itemStack = slotResult.stack();
          SlotContext slotContext = slotResult.slotContext();

          EntityRenderer<?> entityRenderer = Minecraft.getInstance()
                  .getEntityRenderDispatcher()
                  .getRenderer(event.getPlayer());

          if (!(entityRenderer instanceof PlayerRenderer playerRenderer))
            return;

          CuriosRendererRegistry.getRenderer(itemStack.getItem()).ifPresent(renderer -> {

            AbstractClientPlayer player = event.getPlayer();
            float partialTick = Minecraft.getInstance().getPartialTick();

            boolean shouldSit = player.isPassenger()
                    && player.getVehicle() != null
                    && player.getVehicle().shouldRiderSit();

            float limbSwingAmount = 0.0F;
            float limbSwing = 0.0F;

            if (!shouldSit && player.isAlive()) {
              limbSwingAmount = player.walkAnimation.speed(partialTick);
              limbSwing = player.walkAnimation.position(partialTick);

              if (player.isBaby())
                limbSwing *= 3.0F;

              limbSwingAmount = Math.min(limbSwingAmount, 1.0F);
            }

            float ageInTicks = player.tickCount + partialTick;

            float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
            float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
            float netHeadYaw = headYaw - bodyYaw;

            if (shouldSit && player.getVehicle() instanceof LivingEntity vehicle) {

              bodyYaw = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
              netHeadYaw = headYaw - bodyYaw;

              float wrapped = Mth.wrapDegrees(netHeadYaw);
              wrapped = Mth.clamp(wrapped, -85.0F, 85.0F);

              bodyYaw = headYaw - wrapped;

              if (wrapped * wrapped > 2500.0F)
                bodyYaw += wrapped * 0.2F;

              netHeadYaw = headYaw - bodyYaw;
            }

            float headPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());

            if (isEntityUpsideDown(player)) {
              headPitch *= -1.0F;
              netHeadYaw *= -1.0F;
            }

            PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
            model.setAllVisible(false);

            if (player.getMainArm() == HumanoidArm.RIGHT)
              model.rightArm.visible = true;
            else
              model.leftArm.visible = true;

            model.crouching = false;
            model.attackTime = 0;
            model.swimAmount = 0;
            model.setupAnim(player, 0, 0, 0, 0, 0);

            renderer.render(
                    itemStack,
                    slotContext,
                    event.getPoseStack(),
                    playerRenderer,
                    event.getMultiBufferSource(),
                    event.getPackedLight(),
                    limbSwing,
                    limbSwingAmount,
                    partialTick,
                    ageInTicks,
                    netHeadYaw,
                    headPitch
            );
          });
        });
      });
    }

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {

      Player player = event.getEntity();

      if (!player.isUsingItem()) return;
      if (player.getUsedItemHand() != InteractionHand.OFF_HAND) return;

      ItemStack offhand = player.getOffhandItem();

      if (!(offhand.getItem() instanceof HighRulerShield))
        return;

      PlayerRenderer renderer = event.getRenderer();
      PlayerModel<AbstractClientPlayer> model = renderer.getModel();

      if (player.getMainArm() == HumanoidArm.RIGHT) {
        model.leftArm.xRot = (float) Math.toRadians(-90);
        model.leftArm.yRot = (float) Math.toRadians(15);
        model.leftArm.zRot = (float) Math.toRadians(10);
      } else {
        model.rightArm.xRot = (float) Math.toRadians(-90);
        model.rightArm.yRot = (float) Math.toRadians(-15);
        model.rightArm.zRot = (float) Math.toRadians(-10);
      }
    }
  }


  @Mod.EventBusSubscriber(modid = "roacw", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class ModBusEvents {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet(
              ROACWParticleRegistry.SHADOWFLAME.get(),
              CustomAnimatedParticle.Provider::new
      );
      event.registerSpriteSet(
              ROACWParticleRegistry.AURIC_CHARGE_LIGHTNING.get(),
              AuricChargeLightningParticle.Provider::new
      );
      event.registerSpriteSet(
              ROACWParticleRegistry.PLAGUE_NANO_GREEN.get(),
              PlagueNanoParticle.GreenProvider::new);

      event.registerSpriteSet(
              ROACWParticleRegistry.PLAGUE_NANO_RED.get(),
              PlagueNanoParticle.RedProvider::new);

      event.registerSpriteSet(
              ROACWParticleRegistry.PLAGUE_CLOUD.get(),
              PlagueCloudParticle.Provider::new
      );

      event.registerSpriteSet(
              ROACWParticleRegistry.BOOSTER_EXHAUST.get(),
              BoosterExhaustParticle.Provider::new
      );
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(
              ROACWEntityRegistry.IMPALING_COLUMN_SHARDS.get(),
              ImpalingColumnShardsRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.EARTHLY_VIRTUE_SHARDS.get(),
              EarthlyVirtueShardsRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.EARTHLY_VIRTUE_AOE.get(),
              EarthlyVirtueAoERenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.PLAGUE_ROCKET.get(),
              PlagueRocketRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.PLAGUE_NUKE.get(),
              PlagueNukeRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.PLAGUE_CLOUD.get(),
              PlagueCloudRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.PESTILENCE_CLOAK_CLOUD.get(),
              PlagueCloudRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.BELLADONNA_SPIRIT.get(),
              BelladonnaSpiritRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.BELLADONNA_PETAL.get(),
              BelladonnaPetalRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.PLAGUE_CHARGER.get(),
              PlagueChargerRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.PLAGUE_CHARGER_STINGER.get(),
              PlagueChargerStingerRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.EARTHEN_PALADIN.get(),
              EarthenPaladinRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.GOD_KILLER_DART.get(),
              GodKillerDartRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.DARK_RAVEN.get(),
              DarkRavenRenderer::new
      );

      event.registerEntityRenderer(
              ROACWEntityRegistry.BURNING_METEOR.get(),
              BurningMeteorRenderer::new
      );

    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() ->
              CuriosRendererRegistry.register(
                      ROACWItemRegistry.HIGH_RULER_SHIELD.get(),
                      HighRulerShieldCurioRenderer::new
              )
      );
    }
  }
}