package thunder.hack.features.hud.impl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat.DrawMode;
import thunder.hack.core.Managers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.MSAAFramebuffer;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class RadarRewrite extends HudElement {
   private final Setting<Boolean> glow = new Setting<>("Glow", false);
   private final Setting<Float> width = new Setting<>("Height", 2.28F, 0.1F, 5.0F);
   private final Setting<Float> down = new Setting<>("Down", 3.63F, 0.1F, 20.0F);
   private final Setting<Float> tracerWidth = new Setting<>("Width", 0.44F, 0.0F, 8.0F);
   private final Setting<Integer> xOffset = new Setting<>("TracerRadius", 68, 20, 100);
   private final Setting<Integer> pitchLock = new Setting<>("PitchLock", 42, 0, 90);
   private final Setting<RadarRewrite.triangleModeEn> triangleMode = new Setting<>("TracerCMode", RadarRewrite.triangleModeEn.Astolfo);
   private final Setting<Float> CRadius = new Setting<>("CompassRadius", 47.0F, 0.1F, 70.0F);
   private final Setting<ColorSetting> ciColor = new Setting<>("Circle", new ColorSetting(new Color(16777215)));
   private final Setting<ColorSetting> colorf = new Setting<>("Friend", new ColorSetting(new Color(59392)));
   private final Setting<ColorSetting> colors = new Setting<>("Tracer", new ColorSetting(new Color(16776960)));

   public RadarRewrite() {
      super("AkrienRadar", 50, 50);
   }

   public static float getRotations(Entity entity) {
      if (mc.player == null) {
         return 0.0F;
      } else {
         double x = interp(entity.getPos().x, entity.prevX) - interp(mc.player.getPos().x, mc.player.prevX);
         double z = interp(entity.getPos().z, entity.prevZ) - interp(mc.player.getPos().z, mc.player.prevZ);
         return (float)(-(Math.atan2(x, z) * (180.0 / Math.PI)));
      }
   }

   public static double interp(double d, double d2) {
      return d2 + (d - d2) * (double)Render3DEngine.getTickDelta();
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      if (!fullNullCheck()) {
         float middleW = (float)mc.getWindow().getScaledWidth() * this.getX();
         float middleH = (float)mc.getWindow().getScaledHeight() * this.getY();
         MSAAFramebuffer.use(
            false,
            () -> {
               context.getMatrices().push();
               this.renderCompass(context.getMatrices(), middleW + this.CRadius.getValue(), middleH + this.CRadius.getValue());
               context.getMatrices().pop();
               int color = 0;
               context.getMatrices().push();
               context.getMatrices().translate(middleW + this.CRadius.getValue(), middleH + this.CRadius.getValue(), 0.0F);
               context.getMatrices()
                  .multiply(
                     RotationAxis.POSITIVE_X
                        .rotationDegrees(
                           90.0F / Math.abs(90.0F / MathUtility.clamp(mc.player.getPitch(), (float)this.pitchLock.getValue().intValue(), 90.0F))
                              - 102.0F
                        )
                  );
               context.getMatrices().translate(-(middleW + this.CRadius.getValue()), -(middleH + this.CRadius.getValue()), 0.0F);

               for (PlayerEntity e : Lists.newArrayList(mc.world.getPlayers())) {
                  if (e != mc.player) {
                     context.getMatrices().push();
                     float yaw = getRotations(e) - mc.player.getYaw();
                     context.getMatrices().translate(middleW + this.CRadius.getValue(), middleH + this.CRadius.getValue(), 0.0F);
                     context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
                     context.getMatrices().translate(-(middleW + this.CRadius.getValue()), -(middleH + this.CRadius.getValue()), 0.0F);
                     if (Managers.FRIEND.isFriend(e)) {
                        color = this.colorf.getValue().getColor();
                     } else {
                        color = switch ((RadarRewrite.triangleModeEn)this.triangleMode.getValue()) {
                           case Custom -> this.colors.getValue().getColor();
                           case Astolfo -> Render2DEngine.astolfo(false, 1).getRGB();
                        };
                     }

                     Render2DEngine.drawTracerPointer(
                        context.getMatrices(),
                        middleW + this.CRadius.getValue(),
                        middleH - (float)this.xOffset.getValue().intValue() + this.CRadius.getValue(),
                        this.width.getValue() * 5.0F,
                        this.tracerWidth.getValue(),
                        this.down.getValue(),
                        true,
                        this.glow.getValue(),
                        color
                     );
                     context.getMatrices().translate(middleW + this.CRadius.getValue(), middleH + this.CRadius.getValue(), 0.0F);
                     context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
                     context.getMatrices().translate(-(middleW + this.CRadius.getValue()), -(middleH + this.CRadius.getValue()), 0.0F);
                     context.getMatrices().pop();
                  }
               }

               context.getMatrices().pop();
            }
         );
         this.setBounds(this.getPosX(), this.getPosY(), (float)((int)(this.CRadius.getValue() * 2.0F)), (float)((int)(this.CRadius.getValue() * 2.0F)));
      }
   }

   public void renderCompass(MatrixStack matrices, float x, float y) {
      float pitchFactor = Math.abs(90.0F / MathUtility.clamp(mc.player.getPitch(), (float)this.pitchLock.getValue().intValue(), 90.0F));
      this.drawEllipsCompas(
         matrices, -((int)mc.player.getYaw()), x, y, pitchFactor, 1.0F, -2.0F, 1.0F, this.ciColor.getValue().getColorObject(), false
      );
      this.drawEllipsCompas(matrices, -((int)mc.player.getYaw()), x, y, pitchFactor, 1.0F, 0.0F, 3.0F, Color.WHITE, true);
   }

   public void drawEllipsCompas(MatrixStack matrices, int yaw, float x, float y, float x2, float y2, float margin, float width, Color color, boolean Dir) {
      this.drawElipse(matrices, x, y, x2, y2, (float)(15 + yaw), (float)(75 + yaw), margin, width, color, Dir ? "W" : "");
      this.drawElipse(matrices, x, y, x2, y2, (float)(105 + yaw), (float)(165 + yaw), margin, width, color, Dir ? "N" : "");
      this.drawElipse(matrices, x, y, x2, y2, (float)(195 + yaw), (float)(255 + yaw), margin, width, color, Dir ? "E" : "");
      this.drawElipse(matrices, x, y, x2, y2, (float)(285 + yaw), (float)(345 + yaw), margin, width, color, Dir ? "S" : "");
   }

   public void drawElipse(
      MatrixStack matrices, float x, float y, float rx, float ry, float start, float end, float margin, float width, Color color, String direction
   ) {
      if (start > end) {
         float endOffset = end;
         end = start;
         start = endOffset;
      }

      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
      float radius = this.CRadius.getValue() - margin;

      for (float i = start; i <= end; i += 6.0F) {
         float stage = (i - start) / 360.0F;
         if (!Objects.equals(direction, "")) {
            color = HudEditor.getColor((int)(stage * 500.0F));
         }

         float cos = (float)Math.cos((double)i * Math.PI / 180.0);
         float sin = (float)Math.sin((double)i * Math.PI / 180.0);
         bufferBuilder.vertex(x + cos * (radius / ry), y + sin * (radius / rx), 0.0F).color(color.getRGB());
         bufferBuilder.vertex(x + cos * ((radius - width) / ry), y + sin * ((radius - width) / rx), 0.0F).color(color.getRGB());
      }

      Render2DEngine.endBuilding(bufferBuilder);
      RenderSystem.disableBlend();
      if (!Objects.equals(direction, "")) {
         FontRenderers.getModulesRenderer()
            .drawString(
               matrices,
               direction,
               (double)(x - 2.0F) + Math.cos((double)(start - 15.0F) * Math.PI / 180.0) * (double)(radius / ry),
               (double)(y - 1.0F) + Math.sin((double)(start - 15.0F) * Math.PI / 180.0) * (double)(radius / rx),
               -1
            );
      }
   }

   public static enum mode2 {
      Custom,
      Astolfo,
      TwoColor;
   }

   public static enum triangleModeEn {
      Custom,
      Astolfo;
   }
}
