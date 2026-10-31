package thunder.hack.features.modules.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventAttack;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;

public class HitBubbles extends Module {
   public final Setting<Integer> lifeTime = new Setting<>("LifeTime", 30, 1, 150);
   private final ArrayList<HitBubbles.HitBubble> bubbles = new ArrayList<>();

   public HitBubbles() {
      super("HitBubbles", Module.Category.RENDER);
   }

   @EventHandler
   public void onHit(EventAttack e) {
      Vec3d point = Managers.PLAYER
         .getRtxPoint(
            ((IClientPlayerEntity)mc.player).getLastYaw(), ((IClientPlayerEntity)mc.player).getLastPitch(), ModuleManager.aura.attackRange.getValue()
         );
      if (point != null && !e.isPre()) {
         this.bubbles
            .add(
               new HitBubbles.HitBubble(
                  (float)point.x,
                  (float)point.y,
                  (float)point.z,
                  -((IClientPlayerEntity)mc.player).getLastYaw(),
                  ((IClientPlayerEntity)mc.player).getLastPitch(),
                  new Timer()
               )
            );
      }
   }

   @Override
   public void onRender3D(MatrixStack matrixStack) {
      RenderSystem.disableDepthTest();
      ArrayList<HitBubbles.HitBubble> bubblesCopy = Lists.newArrayList(this.bubbles);
      bubblesCopy.forEach(
         b -> {
            matrixStack.push();
            matrixStack.translate(
               (double)b.x - mc.getEntityRenderDispatcher().camera.getPos().getX(),
               (double)b.y - mc.getEntityRenderDispatcher().camera.getPos().getY(),
               (double)b.z - mc.getEntityRenderDispatcher().camera.getPos().getZ()
            );
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(b.yaw));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(b.pitch));
            Render2DEngine.drawBubble(matrixStack, (float)(-b.life.getPassedTimeMs()) / 4.0F, (float)b.life.getPassedTimeMs() / 1500.0F);
            matrixStack.pop();
         }
      );
      RenderSystem.enableDepthTest();
      this.bubbles.removeIf(b -> b.life.passedMs((long)(this.lifeTime.getValue() * 50)));
   }

   public static record HitBubble(float x, float y, float z, float yaw, float pitch, Timer life) {
   }
}
