package net.kamaarion.roacw.events;

import net.kamaarion.roacw.items.curios.high_ruler_shield.HighRulerShieldCurioRenderer; // Added Import
import net.kamaarion.roacw.registeries.ROACWItemRegistry;
import net.kamaarion.roacw.registeries.ROACWParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent; // Added Import
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
      CuriosApi
              .getCuriosInventory(event.getPlayer())
              .ifPresent((iCuriosItemHandler -> {
                iCuriosItemHandler.findCurios(ROACWItemRegistry.ELEMENTAL_GAUNTLET.get()).forEach((slotResult -> {
                  ItemStack itemStack = slotResult.stack();
                  SlotContext slotContext = slotResult.slotContext();
                  EntityRenderer<?> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(event.getPlayer());
                  if (entityRenderer instanceof PlayerRenderer playerRenderer) {
                    CuriosRendererRegistry.getRenderer(itemStack.getItem()).ifPresent((renderer) -> {
                      AbstractClientPlayer player = event.getPlayer();
                      float partialTick = Minecraft.getInstance().getPartialTick();
                      boolean shouldSit = player.isPassenger() && (player.getVehicle() != null && player.getVehicle().shouldRiderSit());
                      float limbSwingAmount = 0.0F;
                      float limbSwing = 0.0F;
                      if (!shouldSit && player.isAlive()) {
                        limbSwingAmount = player.walkAnimation.speed(partialTick);
                        limbSwing = player.walkAnimation.position(partialTick);
                        if (player.isBaby()) {
                          limbSwing *= 3.0F;
                        }
                        if (limbSwingAmount > 1.0F) {
                          limbSwingAmount = 1.0F;
                        }
                      }
                      float ageInTicks = player.tickCount + partialTick;
                      float f = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
                      float f1 = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
                      float netHeadYaw = f1 - f;
                      if (shouldSit && player.getVehicle() instanceof LivingEntity livingentity) {
                        f = Mth.rotLerp(partialTick, livingentity.yBodyRotO, livingentity.yBodyRot);
                        netHeadYaw = f1 - f;
                        float f3 = Mth.wrapDegrees(netHeadYaw);
                        if (f3 < -85.0F) {
                          f3 = -85.0F;
                        }
                        if (f3 >= 85.0F) {
                          f3 = 85.0F;
                        }
                        f = f1 - f3;
                        if (f3 * f3 > 2500.0F) {
                          f += f3 * 0.2F;
                        }
                        netHeadYaw = f1 - f;
                      }
                      float headPitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
                      if (isEntityUpsideDown(player)) {
                        headPitch *= -1.0F;
                        netHeadYaw *= -1.0F;
                      }
                      PlayerModel<AbstractClientPlayer> playerModel = playerRenderer.getModel();
                      playerModel.setAllVisible(false);
                      if (event.getPlayer().getMainArm() == HumanoidArm.RIGHT) {
                        playerModel.rightArm.visible = true;
                      } else if (event.getPlayer().getMainArm() == HumanoidArm.LEFT) {
                        playerModel.leftArm.visible = true;
                      }
                      playerModel.crouching = false;
                      playerModel.attackTime = 0;
                      playerModel.swimAmount = 0;
                      playerModel.setupAnim(player, 0, 0, 0, 0, 0);
                      renderer.render(
                              itemStack, slotContext, event.getPoseStack(), playerRenderer, event.getMultiBufferSource(), event.getPackedLight(), limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch
                      );
                    });
                  }
                }));
              }));
    }
  }

  @Mod.EventBusSubscriber(modid = "roacw", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class ModBusEvents {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet(
              ROACWParticleRegistry.SHADOWFLAME.get(), net.kamaarion.roacw.client.CustomAnimatedParticle.Provider::new
      );
    }

    // Added Hook: Handles Curios Layer Rendering Engine Initialization safely on the main thread
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> {
        CuriosRendererRegistry.register(
                ROACWItemRegistry.HIGH_RULER_SHIELD.get(),
                HighRulerShieldCurioRenderer::new
        );
      });
    }
  }
}

