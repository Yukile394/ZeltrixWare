package thunder.hack.features.modules.render;

import java.awt.Color;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IWorldRenderer;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class BreakHighLight extends Module {
   private final Setting<BreakHighLight.Mode> mode = new Setting<>("Mode", BreakHighLight.Mode.Shrink);
   private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(-1862467584, true)));
   private final Setting<ColorSetting> color2 = new Setting<>("Color2", new ColorSetting(new Color(-196608, true)));
   private final Setting<ColorSetting> ocolor = new Setting<>("OutlineColor", new ColorSetting(new Color(-1874854656, true)));
   private final Setting<ColorSetting> ocolor2 = new Setting<>("OutlineColor2", new ColorSetting(new Color(-13697792, true)));
   private final Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(-1));
   private final Setting<Float> lineWidth = new Setting<>("LineWidth", 2.0F, 0.0F, 5.0F);
   private final Setting<Boolean> otherPlayer = new Setting<>("OtherPlayer", true);
   private float prevProgress;

   public BreakHighLight() {
      super("BreakHighLight", Module.Category.RENDER);
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      if (mc.interactionManager.isBreakingBlock() && mc.crosshairTarget != null && mc.crosshairTarget instanceof BlockHitResult bhr && !mc.world.isAir(bhr.getBlockPos())) {
         Box shrunkMineBox = new Box(
            (double)bhr.getBlockPos().getX(),
            (double)bhr.getBlockPos().getY(),
            (double)bhr.getBlockPos().getZ(),
            (double)bhr.getBlockPos().getX(),
            (double)bhr.getBlockPos().getY(),
            (double)bhr.getBlockPos().getZ()
         );

         float noom = switch ((BreakHighLight.Mode)this.mode.getValue()) {
            case Grow -> Render2DEngine.interpolateFloat(
            this.prevProgress, MathUtility.clamp(mc.interactionManager.currentBreakingProgress, 0.0F, 1.0F), (double)Render3DEngine.getTickDelta()
         );
            case Shrink -> 1.0F - Render2DEngine.interpolateFloat(this.prevProgress, mc.interactionManager.currentBreakingProgress, (double)Render3DEngine.getTickDelta());
            default -> 1.0F;
         };
         Render3DEngine.drawFilledBox(
            stack,
            shrunkMineBox.shrink((double)noom, (double)noom, (double)noom)
               .offset(0.5 + (double)noom * 0.5, 0.5 + (double)noom * 0.5, 0.5 + (double)noom * 0.5),
            Render2DEngine.interpolateColorC(this.color.getValue().getColorObject(), this.color2.getValue().getColorObject(), noom)
         );
         Render3DEngine.drawBoxOutline(
            shrunkMineBox.shrink((double)noom, (double)noom, (double)noom)
               .offset(0.5 + (double)noom * 0.5, 0.5 + (double)noom * 0.5, 0.5 + (double)noom * 0.5),
            Render2DEngine.interpolateColorC(this.ocolor.getValue().getColorObject(), this.ocolor2.getValue().getColorObject(), noom),
            this.lineWidth.getValue()
         );
         switch ((BreakHighLight.Mode)this.mode.getValue()) {
            case Grow:
               this.prevProgress = noom;
               break;
            case Shrink:
               this.prevProgress = 1.0F - noom;
               break;
            default:
               this.prevProgress = 1.0F;
         }
      }

      ((IWorldRenderer)mc.worldRenderer)
         .getBlockBreakingInfos()
         .forEach(
            (integer, destroyBlockProgress) -> {
               Entity object = mc.world.getEntityById(integer);
               if (object != null && this.otherPlayer.getValue() && !object.getName().equals(mc.player.getName())) {
                  BlockPos pos = destroyBlockProgress.getPos();
                  Render3DEngine.drawTextIn3D(
                     String.valueOf(object.getName().getString()), pos.toCenterPos(), 0.0, 0.1, 0.0, this.textColor.getValue().getColorObject()
                  );
                  Box shrunkMineBox = new Box(
                     (double)pos.getX(),
                     (double)pos.getY(),
                     (double)pos.getZ(),
                     (double)pos.getX(),
                     (double)pos.getY(),
                     (double)pos.getZ()
                  );

                  float noomx = switch ((BreakHighLight.Mode)this.mode.getValue()) {
                     case Grow -> MathUtility.clamp((float)destroyBlockProgress.getStage() / 10.0F, 0.0F, 1.0F);
                     case Shrink -> 1.0F - (float)destroyBlockProgress.getStage() / 10.0F;
                     default -> 1.0F;
                  };
                  Render3DEngine.drawFilledBox(
                     stack,
                     shrunkMineBox.shrink((double)noomx, (double)noomx, (double)noomx)
                        .offset(0.5 + (double)noomx * 0.5, 0.5 + (double)noomx * 0.5, 0.5 + (double)noomx * 0.5),
                     Render2DEngine.interpolateColorC(this.color.getValue().getColorObject(), this.color2.getValue().getColorObject(), noomx)
                  );
                  Render3DEngine.drawBoxOutline(
                     shrunkMineBox.shrink((double)noomx, (double)noomx, (double)noomx)
                        .offset(0.5 + (double)noomx * 0.5, 0.5 + (double)noomx * 0.5, 0.5 + (double)noomx * 0.5),
                     Render2DEngine.interpolateColorC(this.ocolor.getValue().getColorObject(), this.ocolor2.getValue().getColorObject(), noomx),
                     this.lineWidth.getValue()
                  );
               }
            }
         );
   }

   private static enum Mode {
      Grow,
      Shrink,
      Static;
   }
}
