package thunder.hack.utility.render.animation;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.Entity;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

public class CaptureMark {
   private static float espValue = 1.0F;
   private static float prevEspValue;
   private static float espSpeed = 1.0F;
   private static boolean flipSpeed;

   public static void render(Entity target) {
      Camera camera = Module.mc.gameRenderer.getCamera();
      double tPosX = Render2DEngine.interpolate(target.prevX, target.getX(), (double)Render3DEngine.getTickDelta())
         - camera.getPos().x;
      double tPosY = Render2DEngine.interpolate(target.prevY, target.getY(), (double)Render3DEngine.getTickDelta())
         - camera.getPos().y;
      double tPosZ = Render2DEngine.interpolate(target.prevZ, target.getZ(), (double)Render3DEngine.getTickDelta())
         - camera.getPos().z;
      MatrixStack matrices = new MatrixStack();
      RenderSystem.disableDepthTest();
      RenderSystem.disableCull();
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
      matrices.translate(tPosX, tPosY + (double)(target.getEyeHeight(target.getPose()) / 2.0F), tPosZ);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      matrices.multiply(
         RotationAxis.POSITIVE_Z.rotationDegrees(Render2DEngine.interpolateFloat(prevEspValue, espValue, (double)Render3DEngine.getTickDelta()))
      );
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShaderTexture(0, TextureStorage.capture);
      matrices.translate(-0.75, -0.75, -0.01);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      buffer.vertex(matrix, 0.0F, 1.5F, 0.0F).texture(0.0F, 1.0F).color(HudEditor.getColor(90).getRGB());
      buffer.vertex(matrix, 1.5F, 1.5F, 0.0F).texture(1.0F, 1.0F).color(HudEditor.getColor(0).getRGB());
      buffer.vertex(matrix, 1.5F, 0.0F, 0.0F).texture(1.0F, 0.0F).color(HudEditor.getColor(180).getRGB());
      buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(HudEditor.getColor(270).getRGB());
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static void tick() {
      prevEspValue = espValue;
      espValue = espValue + espSpeed;
      if (espSpeed > 25.0F) {
         flipSpeed = true;
      }

      if (espSpeed < -25.0F) {
         flipSpeed = false;
      }

      espSpeed = flipSpeed ? espSpeed - 0.5F : espSpeed + 0.5F;
   }
}
