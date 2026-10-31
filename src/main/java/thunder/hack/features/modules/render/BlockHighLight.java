package thunder.hack.features.modules.render;

import net.minecraft.util.math.Box;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.HitResult.Type;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class BlockHighLight extends Module {
   private final Setting<BlockHighLight.Mode> mode = new Setting<>("Mode", BlockHighLight.Mode.Outline);
   private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(-1));
   private final Setting<Float> lineWidth = new Setting<>("LineWidth", 1.0F, 0.0F, 5.0F);

   public BlockHighLight() {
      super("BlockHighLight", Module.Category.RENDER);
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      if (mc.crosshairTarget != null) {
         if (mc.crosshairTarget.getType() == Type.BLOCK) {
            if (mc.crosshairTarget instanceof BlockHitResult bhr) {
               switch ((BlockHighLight.Mode)this.mode.getValue()) {
                  case Both:
                     Render3DEngine.drawBoxOutline(
                        new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(this.color.getValue().getColorObject(), 255), this.lineWidth.getValue()
                     );
                     Render3DEngine.drawFilledBox(stack, new Box(bhr.getBlockPos()), this.color.getValue().getColorObject());
                     break;
                  case BothSide:
                     Render3DEngine.drawSideOutline(
                        new Box(bhr.getBlockPos()),
                        Render2DEngine.injectAlpha(this.color.getValue().getColorObject(), 255),
                        this.lineWidth.getValue(),
                        bhr.getSide()
                     );
                     Render3DEngine.drawFilledSide(stack, new Box(bhr.getBlockPos()), this.color.getValue().getColorObject(), bhr.getSide());
                     break;
                  case Fill:
                     Render3DEngine.drawFilledBox(stack, new Box(bhr.getBlockPos()), this.color.getValue().getColorObject());
                     break;
                  case FilledSide:
                     Render3DEngine.drawFilledSide(stack, new Box(bhr.getBlockPos()), this.color.getValue().getColorObject(), bhr.getSide());
                     break;
                  case Outline:
                     Render3DEngine.drawBoxOutline(
                        new Box(bhr.getBlockPos()), Render2DEngine.injectAlpha(this.color.getValue().getColorObject(), 255), this.lineWidth.getValue()
                     );
                     break;
                  case OutlinedSide:
                     Render3DEngine.drawSideOutline(
                        new Box(bhr.getBlockPos()),
                        Render2DEngine.injectAlpha(this.color.getValue().getColorObject(), 255),
                        this.lineWidth.getValue(),
                        bhr.getSide()
                     );
               }
            }
         }
      }
   }

   private static enum Mode {
      Both,
      BothSide,
      Fill,
      FilledSide,
      Outline,
      OutlinedSide;
   }
}
