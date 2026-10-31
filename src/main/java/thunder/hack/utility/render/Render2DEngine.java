package thunder.hack.utility.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Stack;
import javax.imageio.ImageIO;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.Texture;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.shaders.ArcShader;
import thunder.hack.utility.render.shaders.BlurProgram;
import thunder.hack.utility.render.shaders.HudShader;
import thunder.hack.utility.render.shaders.MainMenuProgram;
import thunder.hack.utility.render.shaders.RectangleShader;
import thunder.hack.utility.render.shaders.TextureColorProgram;

public class Render2DEngine {
   public static TextureColorProgram TEXTURE_COLOR_PROGRAM;
   public static HudShader HUD_SHADER;
   public static RectangleShader RECTANGLE_SHADER;
   public static MainMenuProgram MAIN_MENU_PROGRAM;
   public static ArcShader ARC_PROGRAM;
   public static BlurProgram BLUR_PROGRAM;
   public static HashMap<Integer, Render2DEngine.BlurredShadow> shadowCache = new HashMap<>();
   public static HashMap<Integer, Render2DEngine.BlurredShadow> shadowCache1 = new HashMap<>();
   static final Stack<Render2DEngine.Rectangle> clipStack = new Stack<>();

   public static void addWindow(MatrixStack stack, Render2DEngine.Rectangle r1) {
      Matrix4f matrix = stack.peek().getPositionMatrix();
      Vector4f coord = new Vector4f(r1.x, r1.y, 0.0F, 1.0F);
      Vector4f end = new Vector4f(r1.x1, r1.y1, 0.0F, 1.0F);
      coord.mulTranspose(matrix);
      end.mulTranspose(matrix);
      float x = coord.x();
      float y = coord.y();
      float endX = end.x();
      float endY = end.y();
      Render2DEngine.Rectangle r = new Render2DEngine.Rectangle(x, y, endX, endY);
      if (clipStack.empty()) {
         clipStack.push(r);
         beginScissor((double)r.x, (double)r.y, (double)r.x1, (double)r.y1);
      } else {
         Render2DEngine.Rectangle lastClip = clipStack.peek();
         float lsx = lastClip.x;
         float lsy = lastClip.y;
         float lstx = lastClip.x1;
         float lsty = lastClip.y1;
         float nsx = MathHelper.clamp(r.x, lsx, lstx);
         float nsy = MathHelper.clamp(r.y, lsy, lsty);
         float nstx = MathHelper.clamp(r.x1, nsx, lstx);
         float nsty = MathHelper.clamp(r.y1, nsy, lsty);
         clipStack.push(new Render2DEngine.Rectangle(nsx, nsy, nstx, nsty));
         beginScissor((double)nsx, (double)nsy, (double)nstx, (double)nsty);
      }
   }

   public static void popWindow() {
      clipStack.pop();
      if (clipStack.empty()) {
         endScissor();
      } else {
         Render2DEngine.Rectangle r = clipStack.peek();
         beginScissor((double)r.x, (double)r.y, (double)r.x1, (double)r.y1);
      }
   }

   public static void beginScissor(double x, double y, double endX, double endY) {
      double width = endX - x;
      double height = endY - y;
      width = Math.max(0.0, width);
      height = Math.max(0.0, height);
      float d = (float)Render3DEngine.getScaleFactor();
      int ay = (int)(((double)Module.mc.getWindow().getScaledHeight() - (y + height)) * (double)d);
      RenderSystem.enableScissor((int)(x * (double)d), ay, (int)(width * (double)d), (int)(height * (double)d));
   }

   public static void endScissor() {
      RenderSystem.disableScissor();
   }

   public static void addWindow(MatrixStack stack, float x, float y, float x1, float y1, double animation_factor) {
      float h = y + y1;
      float h2 = (float)((double)h * (1.0 - MathUtility.clamp(animation_factor, 0.0, 1.0025F)));
      float y3 = y + h2;
      float x4 = x1;
      float y4 = y1 - h2;
      if (x1 < x) {
         x4 = x;
      }

      if (y4 < y3) {
         y4 = y3;
      }

      addWindow(stack, new Render2DEngine.Rectangle(x, y3, x4, y4));
   }

   public static void horizontalGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix, x1, y1, 0.0F).color(startColor.getRGB());
      buffer.vertex(matrix, x1, y2, 0.0F).color(startColor.getRGB());
      buffer.vertex(matrix, x2, y2, 0.0F).color(endColor.getRGB());
      buffer.vertex(matrix, x2, y1, 0.0F).color(endColor.getRGB());
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      endRender();
   }

   public static void verticalGradient(MatrixStack matrices, float left, float top, float right, float bottom, Color startColor, Color endColor) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix, left, top, 0.0F).color(startColor.getRGB());
      buffer.vertex(matrix, left, bottom, 0.0F).color(endColor.getRGB());
      buffer.vertex(matrix, right, bottom, 0.0F).color(endColor.getRGB());
      buffer.vertex(matrix, right, top, 0.0F).color(startColor.getRGB());
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      endRender();
   }

   public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color c) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix, x, y + height, 0.0F).color(c.getRGB());
      buffer.vertex(matrix, x + width, y + height, 0.0F).color(c.getRGB());
      buffer.vertex(matrix, x + width, y, 0.0F).color(c.getRGB());
      buffer.vertex(matrix, x, y, 0.0F).color(c.getRGB());
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      endRender();
   }

   public static void drawRectWithOutline(MatrixStack matrices, float x, float y, float width, float height, Color c, Color c2) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix, x, y + height, 0.0F).color(c.getRGB());
      buffer.vertex(matrix, x + width, y + height, 0.0F).color(c.getRGB());
      buffer.vertex(matrix, x + width, y, 0.0F).color(c.getRGB());
      buffer.vertex(matrix, x, y, 0.0F).color(c.getRGB());
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix, x, y + height, 0.0F).color(c2.getRGB());
      buffer.vertex(matrix, x + width, y + height, 0.0F).color(c2.getRGB());
      buffer.vertex(matrix, x + width, y, 0.0F).color(c2.getRGB());
      buffer.vertex(matrix, x, y, 0.0F).color(c2.getRGB());
      buffer.vertex(matrix, x, y + height, 0.0F).color(c2.getRGB());
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      endRender();
   }

   public static void drawRectDumbWay(MatrixStack matrices, float x, float y, float x1, float y1, Color c1) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix, x, y1, 0.0F).color(c1.getRGB());
      buffer.vertex(matrix, x1, y1, 0.0F).color(c1.getRGB());
      buffer.vertex(matrix, x1, y, 0.0F).color(c1.getRGB());
      buffer.vertex(matrix, x, y, 0.0F).color(c1.getRGB());
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      endRender();
   }

   public static void setRectPoints(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float x1, float y1, Color c1, Color c2, Color c3, Color c4) {
      bufferBuilder.vertex(matrix, x, y1, 0.0F).color(c1.getRGB());
      bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(c2.getRGB());
      bufferBuilder.vertex(matrix, x1, y, 0.0F).color(c3.getRGB());
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(c4.getRGB());
   }

   public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
      return mouseX >= x && mouseX - width <= x && mouseY >= y && mouseY - height <= y;
   }

   public static void drawBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
      if (HudEditor.glow.getValue()) {
         width += (float)(blurRadius * 2);
         height += (float)(blurRadius * 2);
         x -= (float)blurRadius;
         y -= (float)blurRadius;
         int identifier = (int)(width * height + width * (float)blurRadius);
         if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
            setupRender();
            RenderSystem.setShaderColor(
               (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F
            );
            renderTexture(
               matrices, (double)x, (double)y, (double)width, (double)height, 0.0F, 0.0F, (double)width, (double)height, (double)width, (double)height
            );
            endRender();
         } else {
            BufferedImage original = new BufferedImage((int)width, (int)height, 2);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int)(width - (float)(blurRadius * 2)), (int)(height - (float)(blurRadius * 2)));
            g.dispose();
            GaussianFilter op = new GaussianFilter((float)blurRadius);
            BufferedImage blurred = op.filter(original, null);
            shadowCache.put(identifier, new Render2DEngine.BlurredShadow(blurred));
         }
      }
   }

   public static void drawGradientBlurredShadow(
      MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4
   ) {
      if (HudEditor.glow.getValue()) {
         width += (float)(blurRadius * 2);
         height += (float)(blurRadius * 2);
         x -= (float)blurRadius;
         y -= (float)blurRadius;
         int identifier = (int)(width * height + width * (float)blurRadius);
         if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
            setupRender();
            renderGradientTexture(
               matrices,
               (double)x,
               (double)y,
               (double)width,
               (double)height,
               0.0F,
               0.0F,
               (double)width,
               (double)height,
               (double)width,
               (double)height,
               color1,
               color2,
               color3,
               color4
            );
            endRender();
         } else {
            BufferedImage original = new BufferedImage((int)width, (int)height, 2);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int)(width - (float)(blurRadius * 2)), (int)(height - (float)(blurRadius * 2)));
            g.dispose();
            GaussianFilter op = new GaussianFilter((float)blurRadius);
            BufferedImage blurred = op.filter(original, null);
            shadowCache.put(identifier, new Render2DEngine.BlurredShadow(blurred));
         }
      }
   }

   public static void drawGradientBlurredShadow1(
      MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4
   ) {
      if (HudEditor.glow.getValue()) {
         width += (float)(blurRadius * 2);
         height += (float)(blurRadius * 2);
         x -= (float)blurRadius;
         y -= (float)blurRadius;
         int identifier = (int)(width * height + width * (float)blurRadius);
         if (shadowCache1.containsKey(identifier)) {
            shadowCache1.get(identifier).bind();
            setupRender();
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
            renderGradientTexture(
               matrices,
               (double)x,
               (double)y,
               (double)width,
               (double)height,
               0.0F,
               0.0F,
               (double)width,
               (double)height,
               (double)width,
               (double)height,
               color1,
               color2,
               color3,
               color4
            );
            endRender();
         } else {
            BufferedImage original = new BufferedImage((int)width, (int)height, 2);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int)(width - (float)(blurRadius * 2)), (int)(height - (float)(blurRadius * 2)));
            g.dispose();
            BufferedImage blurred = new GaussianFilter((float)blurRadius).filter(original, null);
            BufferedImage black = new BufferedImage((int)width + blurRadius * 2, (int)height + blurRadius * 2, 2);
            Graphics g2 = black.getGraphics();
            g2.setColor(new Color(0));
            g2.fillRect(0, 0, (int)width + blurRadius * 2, (int)height + blurRadius * 2);
            g2.dispose();
            BufferedImage combined = new BufferedImage((int)width, (int)height, 2);
            Graphics g1 = combined.getGraphics();
            g1.drawImage(black, -blurRadius, -blurRadius, null);
            g1.drawImage(blurred, 0, 0, null);
            g1.dispose();
            shadowCache1.put(identifier, new Render2DEngine.BlurredShadow(combined));
         }
      }
   }

   public static void registerBufferedImageTexture(Texture i, BufferedImage bi) {
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ImageIO.write(bi, "png", baos);
         byte[] bytes = baos.toByteArray();
         registerTexture(i, bytes);
      } catch (Exception var4) {
      }
   }

   private static void registerTexture(Texture i, byte[] content) {
      try {
         ByteBuffer data = BufferUtils.createByteBuffer(content.length).put(content);
         data.flip();
         NativeImageBackedTexture tex = new NativeImageBackedTexture(NativeImage.read(data));
         Module.mc.execute(() -> Module.mc.getTextureManager().registerTexture(i.getId(), tex));
      } catch (Exception var4) {
      }
   }

   public static void renderTexture(
      MatrixStack matrices,
      double x0,
      double y0,
      double width,
      double height,
      float u,
      float v,
      double regionWidth,
      double regionHeight,
      double textureWidth,
      double textureHeight
   ) {
      double x1 = x0 + width;
      double y1 = y0 + height;
      double z = 0.0;
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      buffer.vertex(matrix, (float)x0, (float)y1, (float)z).texture(u / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight);
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z)
         .texture((u + (float)regionWidth) / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight);
      buffer.vertex(matrix, (float)x1, (float)y0, (float)z).texture((u + (float)regionWidth) / (float)textureWidth, v / (float)textureHeight);
      buffer.vertex(matrix, (float)x0, (float)y0, (float)z).texture(u / (float)textureWidth, (v + 0.0F) / (float)textureHeight);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }

   public static void renderGradientTexture(
      MatrixStack matrices,
      double x0,
      double y0,
      double width,
      double height,
      float u,
      float v,
      double regionWidth,
      double regionHeight,
      double textureWidth,
      double textureHeight,
      Color c1,
      Color c2,
      Color c3,
      Color c4
   ) {
      RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      renderGradientTextureInternal(buffer, matrices, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }

   public static void renderGradientTextureInternal(
      BufferBuilder buff,
      MatrixStack matrices,
      double x0,
      double y0,
      double width,
      double height,
      float u,
      float v,
      double regionWidth,
      double regionHeight,
      double textureWidth,
      double textureHeight,
      Color c1,
      Color c2,
      Color c3,
      Color c4
   ) {
      double x1 = x0 + width;
      double y1 = y0 + height;
      double z = 0.0;
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      buff.vertex(matrix, (float)x0, (float)y1, (float)z)
         .texture(u / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight)
         .color(c1.getRGB());
      buff.vertex(matrix, (float)x1, (float)y1, (float)z)
         .texture((u + (float)regionWidth) / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight)
         .color(c2.getRGB());
      buff.vertex(matrix, (float)x1, (float)y0, (float)z)
         .texture((u + (float)regionWidth) / (float)textureWidth, v / (float)textureHeight)
         .color(c3.getRGB());
      buff.vertex(matrix, (float)x0, (float)y0, (float)z)
         .texture(u / (float)textureWidth, (v + 0.0F) / (float)textureHeight)
         .color(c4.getRGB());
   }

   public static void renderRoundedGradientRect(
      MatrixStack matrices, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float Radius
   ) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      RenderSystem.colorMask(false, false, false, true);
      RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
      RenderSystem.clear(16384, false);
      RenderSystem.colorMask(true, true, true, true);
      drawRound(matrices, x, y, width, height, Radius, color1);
      setupRender();
      RenderSystem.blendFunc(772, 773);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
      bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(color1.getRGB());
      bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(color2.getRGB());
      bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(color3.getRGB());
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(color4.getRGB());
      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      endRender();
   }

   public static void drawRound(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
      renderRoundedQuad(matrices, color, (double)x, (double)y, (double)(width + x), (double)(height + y), (double)radius, 4.0);
   }

   public static void renderRoundedQuad(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
      setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      renderRoundedQuadInternal(
         matrices.peek().getPositionMatrix(),
         (float)c.getRed() / 255.0F,
         (float)c.getGreen() / 255.0F,
         (float)c.getBlue() / 255.0F,
         (float)c.getAlpha() / 255.0F,
         fromX,
         fromY,
         toX,
         toY,
         radius,
         samples
      );
      endRender();
   }

   public static void renderRoundedQuad2(
      MatrixStack matrices, Color c, Color c2, Color c3, Color c4, double fromX, double fromY, double toX, double toY, double radius
   ) {
      setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      renderRoundedQuadInternal2(
         matrices.peek().getPositionMatrix(),
         (float)c.getRed() / 255.0F,
         (float)c.getGreen() / 255.0F,
         (float)c.getBlue() / 255.0F,
         (float)c.getAlpha() / 255.0F,
         (float)c2.getRed() / 255.0F,
         (float)c2.getGreen() / 255.0F,
         (float)c2.getBlue() / 255.0F,
         (float)c2.getAlpha() / 255.0F,
         (float)c3.getRed() / 255.0F,
         (float)c3.getGreen() / 255.0F,
         (float)c3.getBlue() / 255.0F,
         (float)c3.getAlpha() / 255.0F,
         (float)c4.getRed() / 255.0F,
         (float)c4.getGreen() / 255.0F,
         (float)c4.getBlue() / 255.0F,
         (float)c4.getAlpha() / 255.0F,
         fromX,
         fromY,
         toX,
         toY,
         radius
      );
      endRender();
   }

   public static void renderRoundedQuadInternal(
      Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples
   ) {
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
      double[][] map = new double[][]{
         {toX - radius, toY - radius, radius},
         {toX - radius, fromY + radius, radius},
         {fromX + radius, fromY + radius, radius},
         {fromX + radius, toY - radius, radius}
      };

      for (int i = 0; i < 4; i++) {
         double[] current = map[i];
         double rad = current[2];
         double r = (double)i * 90.0;

         while (r < 90.0 + (double)i * 90.0) {
            float rad1 = (float)Math.toRadians(r);
            float sin = (float)(Math.sin((double)rad1) * rad);
            float cos = (float)(Math.cos((double)rad1) * rad);
            bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr, cg, cb, ca);
            r += 90.0 / samples;
         }

         float rad1 = (float)Math.toRadians(90.0 + (double)i * 90.0);
         float sin = (float)(Math.sin((double)rad1) * rad);
         float cos = (float)(Math.cos((double)rad1) * rad);
         bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr, cg, cb, ca);
      }

      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
   }

   public static void renderRoundedQuadInternal2(
      Matrix4f matrix,
      float cr,
      float cg,
      float cb,
      float ca,
      float cr1,
      float cg1,
      float cb1,
      float ca1,
      float cr2,
      float cg2,
      float cb2,
      float ca2,
      float cr3,
      float cg3,
      float cb3,
      float ca3,
      double fromX,
      double fromY,
      double toX,
      double toY,
      double radC1
   ) {
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
      double[][] map = new double[][]{
         {toX - radC1, toY - radC1, radC1}, {toX - radC1, fromY + radC1, radC1}, {fromX + radC1, fromY + radC1, radC1}, {fromX + radC1, toY - radC1, radC1}
      };

      for (int i = 0; i < 4; i++) {
         double[] current = map[i];
         double rad = current[2];

         for (double r = (double)(i * 90); r < (double)(90 + i * 90); r += 10.0) {
            float rad1 = (float)Math.toRadians(r);
            float sin = (float)(Math.sin((double)rad1) * rad);
            float cos = (float)(Math.cos((double)rad1) * rad);
            switch (i) {
               case 0:
                  bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr1, cg1, cb1, ca1);
                  break;
               case 1:
                  bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr, cg, cb, ca);
                  break;
               case 2:
                  bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr2, cg2, cb2, ca2);
                  break;
               default:
                  bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr3, cg3, cb3, ca3);
            }
         }
      }

      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
   }

   public static void draw2DGradientRect(
      MatrixStack matrices,
      float left,
      float top,
      float right,
      float bottom,
      Color leftBottomColor,
      Color leftTopColor,
      Color rightBottomColor,
      Color rightTopColor
   ) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      bufferBuilder.vertex(matrix, right, top, 0.0F).color(rightTopColor.getRGB());
      bufferBuilder.vertex(matrix, left, top, 0.0F).color(leftTopColor.getRGB());
      bufferBuilder.vertex(matrix, left, bottom, 0.0F).color(leftBottomColor.getRGB());
      bufferBuilder.vertex(matrix, right, bottom, 0.0F).color(rightBottomColor.getRGB());
      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      endRender();
   }

   public static void setupRender() {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static void drawTracerPointer(
      MatrixStack matrices, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color
   ) {
      switch ((HudEditor.ArrowsStyle)HudEditor.arrowsStyle.getValue()) {
         case Default:
            drawDefaultArrow(matrices, x, y, size, tracerWidth, downHeight, down, glow, color);
            break;
         case New:
            drawNewArrow(matrices, x, y, size + 8.0F, new Color(color));
      }
   }

   public static void drawNewArrow(MatrixStack matrices, float x, float y, float size, Color color) {
      RenderSystem.setShaderTexture(0, TextureStorage.arrow);
      setupRender();
      RenderSystem.setShaderColor(
         (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F
      );
      RenderSystem.disableDepthTest();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      bufferBuilder.vertex(matrix, x - size / 2.0F, y + size, 0.0F).texture(0.0F, 1.0F);
      bufferBuilder.vertex(matrix, x + size / 2.0F, y + size, 0.0F).texture(1.0F, 1.0F);
      bufferBuilder.vertex(matrix, x + size / 2.0F, y, 0.0F).texture(1.0F, 0.0F);
      bufferBuilder.vertex(matrix, x - size / 2.0F, y, 0.0F).texture(0.0F, 0.0F);
      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      endRender();
   }

   public static void drawDefaultArrow(
      MatrixStack matrices, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color
   ) {
      if (glow) {
         drawBlurredShadow(matrices, x - size * tracerWidth, y, x + size * tracerWidth - (x - size * tracerWidth), size, 10, injectAlpha(new Color(color), 140));
      }

      matrices.push();
      setupRender();
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(color);
      bufferBuilder.vertex(matrix, x - size * tracerWidth, y + size, 0.0F).color(color);
      bufferBuilder.vertex(matrix, x, y + size - downHeight, 0.0F).color(color);
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(color);
      color = darker(new Color(color), 0.8F).getRGB();
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(color);
      bufferBuilder.vertex(matrix, x, y + size - downHeight, 0.0F).color(color);
      bufferBuilder.vertex(matrix, x + size * tracerWidth, y + size, 0.0F).color(color);
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(color);
      if (down) {
         color = darker(new Color(color), 0.6F).getRGB();
         bufferBuilder.vertex(matrix, x - size * tracerWidth, y + size, 0.0F).color(color);
         bufferBuilder.vertex(matrix, x + size * tracerWidth, y + size, 0.0F).color(color);
         bufferBuilder.vertex(matrix, x, y + size - downHeight, 0.0F).color(color);
         bufferBuilder.vertex(matrix, x - size * tracerWidth, y + size, 0.0F).color(color);
      }

      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      endRender();
      matrices.pop();
   }

   public static void endRender() {
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static void drawGradientRound(
      MatrixStack ms, float v, float v1, float i, float i1, float v2, Color darker, Color darker1, Color darker2, Color darker3
   ) {
      renderRoundedQuad2(ms, darker, darker1, darker2, darker3, (double)v, (double)v1, (double)(v + i), (double)(v1 + i1), (double)v2);
   }

   public static float scrollAnimate(float endPoint, float current, float speed) {
      boolean shouldContinueAnimation = endPoint > current;
      if (speed < 0.0F) {
         speed = 0.0F;
      } else if (speed > 1.0F) {
         speed = 1.0F;
      }

      float dif = Math.max(endPoint, current) - Math.min(endPoint, current);
      float factor = dif * speed;
      return current + (shouldContinueAnimation ? factor : -factor);
   }

   public static Color injectAlpha(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
   }

   public static Color TwoColoreffect(Color cl1, Color cl2, double speed, double count) {
      int angle = (int)(((double)System.currentTimeMillis() / speed + count) % 360.0);
      angle = (angle >= 180 ? 360 - angle : angle) * 2;
      return interpolateColorC(cl1, cl2, (float)angle / 360.0F);
   }

   public static Color astolfo(boolean clickgui, int yOffset) {
      float speed = clickgui ? 3500.0F : 3000.0F;
      float hue = (float)(System.currentTimeMillis() % (long)((int)speed) + (long)yOffset);
      if (hue > speed) {
         hue -= speed;
      }

      hue /= speed;
      if (hue > 0.5F) {
         hue = 0.5F - (hue - 0.5F);
      }

      hue += 0.5F;
      return Color.getHSBColor(hue, 0.4F, 1.0F);
   }

   public static Color rainbow(int delay, float saturation, float brightness) {
      double rainbow = Math.ceil((double)((float)(System.currentTimeMillis() + (long)delay) / 16.0F));
      rainbow %= 360.0;
      return Color.getHSBColor((float)(rainbow / 360.0), saturation, brightness);
   }

   public static Color skyRainbow(int speed, int index) {
      int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
      int var3;
      return Color.getHSBColor(
         (double)((float)((double)(var3 = angle % 360) / 360.0)) < 0.5 ? -((float)((double)var3 / 360.0)) : (float)((double)var3 / 360.0), 0.5F, 1.0F
      );
   }

   public static Color fade(int speed, int index, Color color, float alpha) {
      float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
      int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
      angle = (angle > 180 ? 360 - angle : angle) + 180;
      Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], (float)angle / 360.0F));
      return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, (int)(alpha * 255.0F))));
   }

   public static Color getAnalogousColor(Color color) {
      float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
      float degree = 0.84F;
      float newHueSubtracted = hsb[0] - degree;
      return new Color(Color.HSBtoRGB(newHueSubtracted, hsb[1], hsb[2]));
   }

   public static Color applyOpacity(Color color, float opacity) {
      opacity = Math.min(1.0F, Math.max(0.0F, opacity));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * opacity));
   }

   public static int applyOpacity(int color_int, float opacity) {
      opacity = Math.min(1.0F, Math.max(0.0F, opacity));
      Color color = new Color(color_int);
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * opacity)).getRGB();
   }

   public static Color darker(Color color, float factor) {
      return new Color(
         Math.max((int)((float)color.getRed() * factor), 0),
         Math.max((int)((float)color.getGreen() * factor), 0),
         Math.max((int)((float)color.getBlue() * factor), 0),
         color.getAlpha()
      );
   }

   public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
      int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
      float hue = (float)angle / 360.0F;
      Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, (int)(opacity * 255.0F))));
   }

   public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
      int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
      angle = (angle >= 180 ? 360 - angle : angle) * 2;
      return trueColor ? interpolateColorHue(start, end, (float)angle / 360.0F) : interpolateColorC(start, end, (float)angle / 360.0F);
   }

   public static Color interpolateColorC(Color color1, Color color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      return new Color(
         interpolateInt(color1.getRed(), color2.getRed(), (double)amount),
         interpolateInt(color1.getGreen(), color2.getGreen(), (double)amount),
         interpolateInt(color1.getBlue(), color2.getBlue(), (double)amount),
         interpolateInt(color1.getAlpha(), color2.getAlpha(), (double)amount)
      );
   }

   public static Color interpolateColorHue(Color color1, Color color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
      float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
      Color resultColor = Color.getHSBColor(
         interpolateFloat(color1HSB[0], color2HSB[0], (double)amount),
         interpolateFloat(color1HSB[1], color2HSB[1], (double)amount),
         interpolateFloat(color1HSB[2], color2HSB[2], (double)amount)
      );
      return new Color(
         resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), interpolateInt(color1.getAlpha(), color2.getAlpha(), (double)amount)
      );
   }

   public static double interpolate(double oldValue, double newValue, double interpolationValue) {
      return oldValue + (newValue - oldValue) * interpolationValue;
   }

   public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
      return (float)interpolate((double)oldValue, (double)newValue, (double)((float)interpolationValue));
   }

   public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
      return (int)interpolate((double)oldValue, (double)newValue, (double)((float)interpolationValue));
   }

   public static void drawArc(
      MatrixStack matrices, float x, float y, float width, float height, float radius, float thickness, float start, float end, Color c1, Color c2
   ) {
      BufferBuilder bb = preShaderDraw(matrices, x - width / 2.0F, y - height / 2.0F, x + width / 2.0F, y + height / 2.0F);
      ARC_PROGRAM.setParameters(x, y, width, height, radius, thickness, start, end, c1, c2);
      ARC_PROGRAM.use();
      BufferRenderer.drawWithGlobalProgram(bb.end());
      endRender();
   }

   public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, float radius, float alpha) {
      BufferBuilder bb = preShaderDraw(matrices, x - 10.0F, y - 10.0F, width + 20.0F, height + 20.0F);
      RECTANGLE_SHADER.setParameters(x, y, width, height, radius, alpha);
      RECTANGLE_SHADER.use();
      BufferRenderer.drawWithGlobalProgram(bb.end());
      endRender();
   }

   public static void drawRect(
      MatrixStack matrices, float x, float y, float width, float height, float radius, float alpha, Color c1, Color c2, Color c3, Color c4
   ) {
      BufferBuilder bb = preShaderDraw(matrices, x - 10.0F, y - 10.0F, width + 20.0F, height + 20.0F);
      RECTANGLE_SHADER.setParameters(x, y, width, height, radius, alpha, c1, c2, c3, c4);
      RECTANGLE_SHADER.use();
      BufferRenderer.drawWithGlobalProgram(bb.end());
      endRender();
   }

   public static void drawHudBase(MatrixStack matrices, float x, float y, float width, float height, float radius) {
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         drawRoundedBlur(matrices, x, y, width, height, radius, HudEditor.blurColor.getValue().getColorObject());
      } else {
         BufferBuilder bb = preShaderDraw(matrices, x - 10.0F, y - 10.0F, width + 20.0F, height + 20.0F);
         HUD_SHADER.setParameters(x, y, width, height, radius, HudEditor.alpha.getValue(), HudEditor.alpha.getValue());
         HUD_SHADER.use();
         BufferRenderer.drawWithGlobalProgram(bb.end());
         endRender();
      }
   }

   public static void drawHudBase2(
      MatrixStack matrices, float x, float y, float width, float height, float radius, float blurStrenth, float blurOpacity, float animationFactor
   ) {
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         blurStrenth *= animationFactor;
         blurOpacity = (float)interpolate(1.0, (double)blurOpacity, (double)animationFactor);
         Color c = interpolateColorC(Color.BLACK, HudEditor.blurColor.getValue().getColorObject(), animationFactor);
         drawRoundedBlur(matrices, x, y, width, height, radius, c, blurStrenth, blurOpacity);
      } else {
         BufferBuilder bb = preShaderDraw(matrices, x - 10.0F, y - 10.0F, width + 20.0F, height + 20.0F);
         HUD_SHADER.setParameters(x, y, width, height, radius, HudEditor.alpha.getValue(), HudEditor.alpha.getValue());
         HUD_SHADER.use();
         BufferRenderer.drawWithGlobalProgram(bb.end());
         endRender();
      }
   }

   public static void drawHudBase(MatrixStack matrices, float x, float y, float width, float height, float radius, boolean hud) {
      BufferBuilder bb = preShaderDraw(matrices, x - 10.0F, y - 10.0F, width + 20.0F, height + 20.0F);
      HUD_SHADER.setParameters(x, y, width, height, radius, HudEditor.alpha.getValue(), HudEditor.alpha.getValue());
      HUD_SHADER.use();
      BufferRenderer.drawWithGlobalProgram(bb.end());
      endRender();
   }

   public static void drawRoundedBlur(MatrixStack matrices, float x, float y, float width, float height, float radius, Color c1) {
      drawRoundedBlur(matrices, x, y, width, height, radius, c1, HudEditor.blurStrength.getValue(), HudEditor.blurOpacity.getValue());
   }

   public static void drawRoundedBlur(
      MatrixStack matrices, float x, float y, float width, float height, float radius, Color c1, float blurStrenth, float blurOpacity
   ) {
      BufferBuilder bb = preShaderDraw(matrices, x - 10.0F, y - 10.0F, width + 20.0F, height + 20.0F);
      BLUR_PROGRAM.setParameters(x, y, width, height, radius, c1, blurStrenth, blurOpacity);
      BLUR_PROGRAM.use();
      BufferRenderer.drawWithGlobalProgram(bb.end());
      endRender();
   }

   public static void drawHudBase(MatrixStack matrices, float x, float y, float width, float height, float radius, float alpha) {
      BufferBuilder bb = preShaderDraw(matrices, x - 10.0F, y - 10.0F, width + 20.0F, height + 20.0F);
      HUD_SHADER.setParameters(x, y, width, height, radius, alpha, HudEditor.alpha.getValue());
      HUD_SHADER.use();
      BufferRenderer.drawWithGlobalProgram(bb.end());
      endRender();
   }

   public static void drawGuiBase(MatrixStack matrices, float x, float y, float width, float height, float radius, float opacity) {
      BufferBuilder bb = preShaderDraw(matrices, x - 10.0F, y - 10.0F, width + 20.0F, height + 20.0F);
      HUD_SHADER.setParameters(x, y, width, height, radius, 1.0F, opacity);
      HUD_SHADER.use();
      BufferRenderer.drawWithGlobalProgram(bb.end());
      endRender();
   }

   public static void drawMainMenuShader(MatrixStack matrices, float x, float y, float width, float height) {
      BufferBuilder bb = preShaderDraw(matrices, x, y, width, height);
      MAIN_MENU_PROGRAM.setParameters(x, y, width, height);
      MAIN_MENU_PROGRAM.use();
      BufferRenderer.drawWithGlobalProgram(bb.end());
      endRender();
   }

   public static BufferBuilder preShaderDraw(MatrixStack matrices, float x, float y, float width, float height) {
      setupRender();
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION);
      setRectanglePoints(buffer, matrix, x, y, x + width, y + height);
      return buffer;
   }

   public static void setRectanglePoints(BufferBuilder buffer, Matrix4f matrix, float x, float y, float x1, float y1) {
      buffer.vertex(matrix, x, y, 0.0F);
      buffer.vertex(matrix, x, y1, 0.0F);
      buffer.vertex(matrix, x1, y1, 0.0F);
      buffer.vertex(matrix, x1, y, 0.0F);
   }

   public static void drawOrbiz(MatrixStack matrices, float z, double r, Color c) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

      for (int i = 0; i <= 20; i++) {
         float x2 = (float)(Math.sin((double)((float)i * 56.548656F / 180.0F)) * r);
         float y2 = (float)(Math.cos((double)((float)i * 56.548656F / 180.0F)) * r);
         bufferBuilder.vertex(matrix, x2, y2, z)
            .color((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, 0.4F);
      }

      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      endRender();
   }

   public static void drawStar(MatrixStack matrices, Color c, float scale) {
      setupRender();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShaderTexture(0, TextureStorage.star);
      RenderSystem.setShaderColor((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, (float)c.getAlpha() / 255.0F);
      renderGradientTexture(matrices, 0.0, 0.0, (double)scale, (double)scale, 0.0F, 0.0F, 128.0, 128.0, 128.0, 128.0, c, c, c, c);
      endRender();
   }

   public static void drawHeart(MatrixStack matrices, Color c, float scale) {
      setupRender();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShaderTexture(0, TextureStorage.heart);
      RenderSystem.setShaderColor((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, (float)c.getAlpha() / 255.0F);
      renderGradientTexture(matrices, 0.0, 0.0, (double)scale, (double)scale, 0.0F, 0.0F, 128.0, 128.0, 128.0, 128.0, c, c, c, c);
      endRender();
   }

   public static void drawBloom(MatrixStack matrices, Color c, float scale) {
      setupRender();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShaderTexture(0, TextureStorage.firefly);
      RenderSystem.setShaderColor((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, (float)c.getAlpha() / 255.0F);
      renderGradientTexture(matrices, 0.0, 0.0, (double)scale, (double)scale, 0.0F, 0.0F, 128.0, 128.0, 128.0, 128.0, c, c, c, c);
      endRender();
   }

   public static void drawBubble(MatrixStack matrices, float angle, float factor) {
      setupRender();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShaderTexture(0, TextureStorage.bubble);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
      float scale = factor * 2.0F;
      renderGradientTexture(
         matrices,
         (double)(-scale / 2.0F),
         (double)(-scale / 2.0F),
         (double)scale,
         (double)scale,
         0.0F,
         0.0F,
         128.0,
         128.0,
         128.0,
         128.0,
         applyOpacity(HudEditor.getColor(270), 1.0F - factor),
         applyOpacity(HudEditor.getColor(0), 1.0F - factor),
         applyOpacity(HudEditor.getColor(180), 1.0F - factor),
         applyOpacity(HudEditor.getColor(90), 1.0F - factor)
      );
      endRender();
   }

   public static void drawLine(float x, float y, float x1, float y1, int color) {
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
      bufferBuilder.vertex(x, y, 0.0F).color(color);
      bufferBuilder.vertex(x1, y1, 0.0F).color(color);
      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
   }

   public static boolean isDark(Color color) {
      return isDark((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F);
   }

   public static boolean isDark(float r, float g, float b) {
      return colorDistance(r, g, b, 0.0F, 0.0F, 0.0F) < colorDistance(r, g, b, 1.0F, 1.0F, 1.0F);
   }

   public static float colorDistance(float r1, float g1, float b1, float r2, float g2, float b2) {
      float a = r2 - r1;
      float b = g2 - g1;
      float c = b2 - b1;
      return (float)Math.sqrt((double)(a * a + b * b + c * c));
   }

   public static void initShaders() {
      HUD_SHADER = new HudShader();
      MAIN_MENU_PROGRAM = new MainMenuProgram();
      TEXTURE_COLOR_PROGRAM = new TextureColorProgram();
      ARC_PROGRAM = new ArcShader();
      RECTANGLE_SHADER = new RectangleShader();
      BLUR_PROGRAM = new BlurProgram();
   }

   @NotNull
   public static Color getColor(@NotNull Color start, @NotNull Color end, float progress, boolean smooth) {
      if (!smooth) {
         return (double)progress >= 0.95 ? end : start;
      } else {
         int rDiff = end.getRed() - start.getRed();
         int gDiff = end.getGreen() - start.getGreen();
         int bDiff = end.getBlue() - start.getBlue();
         int aDiff = end.getAlpha() - start.getAlpha();
         return new Color(
            fixColorValue(start.getRed() + (int)((float)rDiff * progress)),
            fixColorValue(start.getGreen() + (int)((float)gDiff * progress)),
            fixColorValue(start.getBlue() + (int)((float)bDiff * progress)),
            fixColorValue(start.getAlpha() + (int)((float)aDiff * progress))
         );
      }
   }

   private static int fixColorValue(int colorVal) {
      return colorVal > 255 ? 255 : Math.max(colorVal, 0);
   }

   public static void endBuilding(BufferBuilder bb) {
      BuiltBuffer builtBuffer = bb.endNullable();
      if (builtBuffer != null) {
         BufferRenderer.drawWithGlobalProgram(builtBuffer);
      }
   }

   public static class BlurredShadow {
      Texture id = new Texture("texture/remote/" + RandomStringUtils.randomAlphanumeric(16));

      public BlurredShadow(BufferedImage bufferedImage) {
         Render2DEngine.registerBufferedImageTexture(this.id, bufferedImage);
      }

      public void bind() {
         RenderSystem.setShaderTexture(0, this.id.getId());
      }
   }

   public static record Rectangle(float x, float y, float x1, float y1) {
      public boolean contains(double x, double y) {
         return x >= (double)this.x && x <= (double)this.x1 && y >= (double)this.y && y <= (double)this.y1;
      }
   }
}
