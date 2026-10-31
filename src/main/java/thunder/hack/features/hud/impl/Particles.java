package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class Particles {
   public double x;
   public double y;
   public double deltaX;
   public double deltaY;
   public double size;
   public double opacity;
   public Color color;

   public static Color mixColors(Color color1, Color color2, double percent) {
      double inverse_percent = 1.0 - percent;
      int redPart = (int)((double)color1.getRed() * percent + (double)color2.getRed() * inverse_percent);
      int greenPart = (int)((double)color1.getGreen() * percent + (double)color2.getGreen() * inverse_percent);
      int bluePart = (int)((double)color1.getBlue() * percent + (double)color2.getBlue() * inverse_percent);
      return new Color(redPart, greenPart, bluePart);
   }

   public void render2D(MatrixStack matrixStack) {
      this.drawStar(matrixStack, (float)this.x, (float)this.y, this.color);
   }

   public void drawStar(MatrixStack matrices, float x, float y, Color c) {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShaderTexture(0, TextureStorage.star);
      RenderSystem.setShaderColor((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, (float)(this.opacity / 255.0));
      Render2DEngine.renderTexture(
         matrices, (double)x + this.size / 2.0, (double)y + this.size / 2.0, this.size, this.size, 0.0F, 0.0F, 256.0, 256.0, 256.0, 256.0
      );
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void updatePosition() {
      this.x = this.x + this.deltaX;
      this.y = this.y + this.deltaY;
      this.deltaY *= 0.95;
      this.deltaX *= 0.95;
      this.opacity -= 2.0;
      this.size /= 1.01;
      if (this.opacity < 1.0) {
         this.opacity = 1.0;
      }
   }

   public void init(double x, double y, double deltaX, double deltaY, double size, Color color) {
      this.x = x;
      this.y = y;
      this.deltaX = deltaX;
      this.deltaY = deltaY;
      this.size = size;
      this.opacity = 254.0;
      this.color = color;
   }
}
