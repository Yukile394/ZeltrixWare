package thunder.hack.features.hud.impl;

import com.google.common.collect.Lists;
import java.awt.Color;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class CrosshairArrows extends HudElement {
   public static Setting<Boolean> glow = new Setting<>("Glow", false);
   private final Setting<Float> width = new Setting<>("Height", 2.28F, 0.1F, 5.0F);
   private final Setting<BooleanSettingGroup> down = new Setting<>("Down", new BooleanSettingGroup(false));
   private final Setting<Float> downHeight = new Setting<>("DownHeight", 3.63F, 0.1F, 20.0F).addToGroup(this.down);
   private final Setting<Float> tracerWidth = new Setting<>("Width", 0.44F, 0.0F, 8.0F);
   private final Setting<Integer> xOffset = new Setting<>("TracerRadius", 68, 20, 100);
   private final Setting<Integer> pitchLock = new Setting<>("PitchLock", 42, 0, 90);
   private final Setting<CrosshairArrows.triangleModeEn> triangleMode = new Setting<>("TracerCMode", CrosshairArrows.triangleModeEn.Astolfo);
   private final Setting<ColorSetting> colorf = new Setting<>("Friend", new ColorSetting(new Color(59392)));
   private final Setting<ColorSetting> colors = new Setting<>("Tracer", new ColorSetting(new Color(16776960)));
   private float smoothYaw = 0.0F;

   public CrosshairArrows() {
      super("CrosshairArrows", 0, 0);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      if (!fullNullCheck()) {
         float middleW = ModuleManager.crosshair.getAnimatedPosX();
         float middleH = ModuleManager.crosshair.getAnimatedPosY();
         int color = 0;
         context.getMatrices().push();
         context.getMatrices().translate(middleW, middleH, 0.0F);
         context.getMatrices()
            .multiply(
               RotationAxis.POSITIVE_X
                  .rotationDegrees(
                     90.0F / Math.abs(90.0F / MathUtility.clamp(mc.player.getPitch(), (float)this.pitchLock.getValue().intValue(), 90.0F)) - 102.0F
                  )
            );
         context.getMatrices().translate(-middleW, -middleH, 0.0F);
         this.smoothYaw = AnimationUtility.fast(this.smoothYaw, mc.player.getYaw(), 13.0F);

         for (PlayerEntity e : Lists.newArrayList(mc.world.getPlayers())) {
            if (e != mc.player) {
               context.getMatrices().push();
               float yaw = RadarRewrite.getRotations(e) - this.smoothYaw;
               context.getMatrices().translate(middleW, middleH, 0.0F);
               context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
               context.getMatrices().translate(-middleW, -middleH, 0.0F);
               if (Managers.FRIEND.isFriend(e)) {
                  color = this.colorf.getValue().getColor();
               } else {
                  color = switch ((CrosshairArrows.triangleModeEn)this.triangleMode.getValue()) {
                     case Custom -> this.colors.getValue().getColor();
                     case Astolfo -> Render2DEngine.astolfo(false, 1).getRGB();
                  };
               }

               Render2DEngine.drawTracerPointer(
                  context.getMatrices(),
                  middleW,
                  middleH - (float)this.xOffset.getValue().intValue(),
                  this.width.getValue() * 5.0F,
                  this.tracerWidth.getValue(),
                  this.downHeight.getValue(),
                  this.down.getValue().isEnabled(),
                  glow.getValue(),
                  color
               );
               context.getMatrices().translate(middleW, middleH, 0.0F);
               context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
               context.getMatrices().translate(-middleW, -middleH, 0.0F);
               context.getMatrices().pop();
            }
         }

         context.getMatrices().pop();
      }
   }

   public static enum triangleModeEn {
      Custom,
      Astolfo;
   }
}
