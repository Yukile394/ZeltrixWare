package thunder.hack.utility.render;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.client.util.math.MatrixStack;

public final class BlockAnimationUtility {
   private static final Map<BlockAnimationUtility.BlockRenderData, Long> blocks = new ConcurrentHashMap<>();

   public static void onRender(MatrixStack matrixStack) {
      blocks.forEach((animation, time) -> {
         if ((float)(System.currentTimeMillis() - time) > 300.0F) {
            blocks.remove(animation);
         } else {
            animation.renderWithTime(System.currentTimeMillis() - time, matrixStack);
         }
      });
   }

   public static void renderBlock(
      BlockPos pos,
      Color lineColor,
      int lineWidth,
      Color fillColor,
      BlockAnimationUtility.BlockAnimationMode animationMode,
      BlockAnimationUtility.BlockRenderMode renderMode
   ) {
      if (renderMode != BlockAnimationUtility.BlockRenderMode.None) {
         blocks.put(new BlockAnimationUtility.BlockRenderData(pos, lineColor, lineWidth, fillColor, animationMode, renderMode), System.currentTimeMillis());
      }
   }

   public static boolean isRendering(BlockPos pos) {
      return blocks.keySet().stream().anyMatch(blockRenderData -> blockRenderData.pos().equals(pos));
   }

   public static enum BlockAnimationMode {
      Fade,
      Hover,
      Decrease,
      Static,
      Flash,
      Grow,
      Fill,
      TNT,
      Pull;
   }

   private static record BlockRenderData(
      BlockPos pos,
      Color lineColor,
      int lineWidth,
      Color fillColor,
      BlockAnimationUtility.BlockAnimationMode animationMode,
      BlockAnimationUtility.BlockRenderMode renderMode
   ) {
      void renderWithTime(Long time, MatrixStack stack) {
         switch (this.animationMode) {
            case Fade: {
               Box box = new Box(this.pos);
               renderBox(time, stack, box, this.renderMode, this.lineColor, this.lineWidth, this.fillColor);
               break;
            }
            case Hover: {
               float scale = 1.0F + (float)time.longValue() / 1500.0F;
               Box box = new Box(
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ(),
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ()
               );
               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Line) {
                  Render3DEngine.drawBoxOutline(
                     box.shrink((double)scale, (double)scale, (double)scale)
                        .offset(0.5 + (double)(scale * 0.5F), 0.5 + (double)scale * 0.5, 0.5 + (double)scale * 0.5),
                     Render2DEngine.injectAlpha(this.lineColor, (int)((float)this.lineColor.getAlpha() * (1.0F - (float)time.longValue() / 300.0F))),
                     (float)this.lineWidth
                  );
               }

               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Fill) {
                  Render3DEngine.drawFilledBox(
                     stack,
                     box.shrink((double)scale, (double)scale, (double)scale)
                        .offset(0.5 + (double)scale * 0.5, 0.5 + (double)scale * 0.5, 0.5 + (double)scale * 0.5),
                     Render2DEngine.injectAlpha(this.fillColor, (int)((float)this.fillColor.getAlpha() * (1.0F - (float)time.longValue() / 300.0F)))
                  );
               }
               break;
            }
            case Decrease: {
               float scalexxxxxx = 1.0F - (float)time.longValue() / 300.0F;
               Box box = new Box(
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ(),
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ()
               );
               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Line) {
                  Render3DEngine.drawBoxOutline(
                     box.shrink((double)scalexxxxxx, (double)scalexxxxxx, (double)scalexxxxxx)
                        .offset(0.5 + (double)scalexxxxxx * 0.5, 0.5 + (double)scalexxxxxx * 0.5, 0.5 + (double)scalexxxxxx * 0.5),
                     this.lineColor,
                     (float)this.lineWidth
                  );
               }

               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Fill) {
                  Render3DEngine.drawFilledBox(
                     stack,
                     box.shrink((double)scalexxxxxx, (double)scalexxxxxx, (double)scalexxxxxx)
                        .offset(0.5 + (double)scalexxxxxx * 0.5, 0.5 + (double)scalexxxxxx * 0.5, 0.5 + (double)scalexxxxxx * 0.5),
                     Render2DEngine.injectAlpha(this.fillColor, (int)((float)this.fillColor.getAlpha() * (1.0F - (float)time.longValue() / 300.0F)))
                  );
               }
               break;
            }
            case Static:
               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Line) {
                  Render3DEngine.drawBoxOutline(new Box(this.pos), this.lineColor, (float)this.lineWidth);
               }

               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Fill) {
                  Render3DEngine.drawFilledBox(stack, new Box(this.pos), this.fillColor);
               }
               break;
            case Flash: {
               float scalexxxx;
               if (time > 100L) {
                  scalexxxx = 1.0F - (float)(time - 100L) / 400.0F;
               } else {
                  scalexxxx = (float)time.longValue() / 100.0F;
               }

               Box box = new Box(
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ(),
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ()
               );
               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Line) {
                  Render3DEngine.drawBoxOutline(
                     box.shrink((double)scalexxxx, (double)scalexxxx, (double)scalexxxx)
                        .offset(0.5 + (double)scalexxxx * 0.5, 0.5 + (double)scalexxxx * 0.5, 0.5 + (double)scalexxxx * 0.5),
                     this.lineColor,
                     (float)this.lineWidth
                  );
               }

               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Fill) {
                  Render3DEngine.drawFilledBox(
                     stack,
                     box.shrink((double)scalexxxx, (double)scalexxxx, (double)scalexxxx)
                        .offset(0.5 + (double)scalexxxx * 0.5, 0.5 + (double)scalexxxx * 0.5, 0.5 + (double)scalexxxx * 0.5),
                     Render2DEngine.injectAlpha(this.fillColor, (int)((float)this.fillColor.getAlpha() * scalexxxx))
                  );
               }
               break;
            }
            case Grow: {
               float scalexxx = (float)time.longValue() / 300.0F;
               Box box = new Box(
                  (double)this.pos.getX(),
                  (double)((float)this.pos.getY() + scalexxx),
                  (double)this.pos.getZ(),
                  (double)(this.pos.getX() + 1),
                  (double)this.pos.getY(),
                  (double)(this.pos.getZ() + 1)
               );
               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Line) {
                  Render3DEngine.drawBoxOutline(box, this.lineColor, (float)this.lineWidth);
               }

               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Fill) {
                  Render3DEngine.drawFilledBox(
                     stack, box, Render2DEngine.injectAlpha(this.fillColor, (int)((float)this.fillColor.getAlpha() * ((float)time.longValue() / 300.0F)))
                  );
               }
               break;
            }
            case Fill: {
               float scalexxxxx = (float)time.longValue() / 300.0F;
               Box box = new Box(
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ(),
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ()
               );
               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Line) {
                  Render3DEngine.drawBoxOutline(
                     box.shrink((double)scalexxxxx, (double)scalexxxxx, (double)scalexxxxx)
                        .offset(0.5 + (double)scalexxxxx * 0.5, 0.5 + (double)scalexxxxx * 0.5, 0.5 + (double)scalexxxxx * 0.5),
                     this.lineColor,
                     (float)this.lineWidth
                  );
               }

               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Fill) {
                  Render3DEngine.drawFilledBox(
                     stack,
                     box.shrink((double)scalexxxxx, (double)scalexxxxx, (double)scalexxxxx)
                        .offset(0.5 + (double)scalexxxxx * 0.5, 0.5 + (double)scalexxxxx * 0.5, 0.5 + (double)scalexxxxx * 0.5),
                     Render2DEngine.injectAlpha(this.fillColor, (int)((float)this.fillColor.getAlpha() * ((float)time.longValue() / 300.0F)))
                  );
               }
               break;
            }
            case TNT: {
               float scalexx;
               if (time < 200L) {
                  scalexx = 1.0F;
               } else {
                  scalexx = 1.0F + ((float)time.longValue() - 200.0F) / 400.0F;
               }

               Box box = new Box(
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ(),
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ()
               );
               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Line) {
                  Render3DEngine.drawBoxOutline(
                     box.shrink((double)scalexx, (double)scalexx, (double)scalexx)
                        .offset(0.5 + (double)(scalexx * 0.5F), 0.5 + (double)scalexx * 0.5, 0.5 + (double)scalexx * 0.5),
                     this.lineColor,
                     (float)this.lineWidth
                  );
               }

               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Fill) {
                  Render3DEngine.drawFilledBox(
                     stack,
                     box.shrink((double)scalexx, (double)scalexx, (double)scalexx)
                        .offset(0.5 + (double)scalexx * 0.5, 0.5 + (double)scalexx * 0.5, 0.5 + (double)scalexx * 0.5),
                     Render2DEngine.injectAlpha(this.fillColor, (int)((float)this.fillColor.getAlpha() * scalexx))
                  );
               }
               break;
            }
            case Pull: {
               float scalex;
               if (time < 200L) {
                  scalex = 1.5F - (float)time.longValue() / 200.0F * 0.5F;
               } else {
                  scalex = 1.0F;
               }

               Box box = new Box(
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ(),
                  (double)this.pos.getX(),
                  (double)this.pos.getY(),
                  (double)this.pos.getZ()
               );
               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Line) {
                  Render3DEngine.drawBoxOutline(
                     box.shrink((double)scalex, (double)scalex, (double)scalex)
                        .offset(0.5 + (double)(scalex * 0.5F), 0.5 + (double)scalex * 0.5, 0.5 + (double)scalex * 0.5),
                     this.lineColor,
                     (float)this.lineWidth
                  );
               }

               if (this.renderMode == BlockAnimationUtility.BlockRenderMode.All || this.renderMode == BlockAnimationUtility.BlockRenderMode.Fill) {
                  Render3DEngine.drawFilledBox(
                     stack,
                     box.shrink((double)scalex, (double)scalex, (double)scalex)
                        .offset(0.5 + (double)scalex * 0.5, 0.5 + (double)scalex * 0.5, 0.5 + (double)scalex * 0.5),
                     Render2DEngine.injectAlpha(this.fillColor, (int)((float)this.fillColor.getAlpha() * scalex))
                  );
               }
            }
         }
      }

      private static void renderBox(
         Long time, MatrixStack stack, Box box, BlockAnimationUtility.BlockRenderMode renderMode, Color lineColor, int lineWidth, Color fillColor
      ) {
         if (renderMode == BlockAnimationUtility.BlockRenderMode.All || renderMode == BlockAnimationUtility.BlockRenderMode.Line) {
            Render3DEngine.drawBoxOutline(
               box, Render2DEngine.injectAlpha(lineColor, (int)((float)fillColor.getAlpha() * (1.0F - (float)time.longValue() / 300.0F))), (float)lineWidth
            );
         }

         if (renderMode == BlockAnimationUtility.BlockRenderMode.All || renderMode == BlockAnimationUtility.BlockRenderMode.Fill) {
            Render3DEngine.drawFilledBox(
               stack, box, Render2DEngine.injectAlpha(fillColor, (int)((float)fillColor.getAlpha() * (1.0F - (float)time.longValue() / 300.0F)))
            );
         }
      }
   }

   public static enum BlockRenderMode {
      Fill,
      Line,
      All,
      None;
   }
}
