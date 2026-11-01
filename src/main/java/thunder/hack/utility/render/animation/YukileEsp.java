package thunder.hack.utility.render.animation;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import thunder.hack.ThunderHack;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

/**
 * Yukile target ESPs for Aura.
 *
 *  - renderHelix : double-strand DNA helix climbing the target with fading ribbon trails and glow nodes
 *  - renderOrbit : three tilted gyroscope rings with fading comet tails and a pulsing ground halo
 *
 * Both are time-driven (System.currentTimeMillis) and interpolated with the render tick delta,
 * so they stay smooth at any FPS and need no tick() hook.
 */
public final class YukileEsp {
   private YukileEsp() {
   }

   // ───────────────────────────── HELIX ─────────────────────────────

   private static final int HELIX_STRANDS = 2;
   private static final int HELIX_SEGMENTS = 96;
   private static final int HELIX_TRAIL = 22;
   private static final float HELIX_TURNS = 2.5F;
   private static final float HELIX_RADIUS_SCALE = 0.85F;

   public static void renderHelix(MatrixStack stack, Entity target) {
      if (target == null || Module.mc.player == null) {
         return;
      }

      Vec3 pos = interpolatedPos(target);
      float height = target.getHeight();
      float radius = target.getWidth() * HELIX_RADIUS_SCALE + 0.1F;
      double time = (System.currentTimeMillis() - ThunderHack.initTime) / 1000.0;
      float spin = (float)(time * 2.2);
      float breathe = 1.0F + 0.06F * (float)Math.sin(time * 3.0);

      stack.push();
      stack.translate(pos.x, pos.y, pos.z);
      Render3DEngine.setupRender();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);

      Matrix4f matrix = stack.peek().getPositionMatrix();

      // ribbon between the two strands (rungs of the helix)
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder rungs = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      for (int i = 0; i < HELIX_SEGMENTS; i += 4) {
         float t = i / (float)(HELIX_SEGMENTS - 1);
         float y = t * height;
         float ang = spin + t * HELIX_TURNS * MathHelper.TAU;
         float rr = radius * breathe * envelope(t);
         float x1 = (float)Math.cos(ang) * rr;
         float z1 = (float)Math.sin(ang) * rr;
         float x2 = (float)Math.cos(ang + Math.PI) * rr;
         float z2 = (float)Math.sin(ang + Math.PI) * rr;
         int a = (int)(110 * envelope(t));
         int c1 = Render2DEngine.injectAlpha(HudEditor.getColor(i * 3), a).getRGB();
         int c2 = Render2DEngine.injectAlpha(HudEditor.getColor(i * 3 + 90), a).getRGB();
         float th = 0.012F;
         rungs.vertex(matrix, x1, y - th, z1).color(c1);
         rungs.vertex(matrix, x1, y + th, z1).color(c1);
         rungs.vertex(matrix, x2, y + th, z2).color(c2);
         rungs.vertex(matrix, x2, y - th, z2).color(c2);
      }
      Render2DEngine.endBuilding(rungs);

      // the strands themselves: triangle-strip ribbons that fade toward the tail
      for (int s = 0; s < HELIX_STRANDS; s++) {
         float phase = s * (float)Math.PI;
         BufferBuilder ribbon = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         for (int i = 0; i < HELIX_SEGMENTS; i++) {
            float t = i / (float)(HELIX_SEGMENTS - 1);
            float y = t * height;
            float ang = spin + phase + t * HELIX_TURNS * MathHelper.TAU;
            float rr = radius * breathe * envelope(t);
            float x = (float)Math.cos(ang) * rr;
            float z = (float)Math.sin(ang) * rr;
            float thick = 0.05F + 0.035F * (float)Math.sin(t * Math.PI);
            // head of the strand glows, tail fades
            float head = (float)((time * 0.45 + s * 0.5) % 1.0);
            float dist = Math.abs(t - head);
            dist = Math.min(dist, 1.0F - dist);
            float glow = MathHelper.clamp(1.0F - dist * 3.5F, 0.0F, 1.0F);
            int alpha = (int)MathHelper.clamp(70 + 185 * glow, 0, 255);
            Color col = Render2DEngine.injectAlpha(HudEditor.getColor(i * 4 + s * 180), alpha);
            ribbon.vertex(matrix, x, y - thick, z).color(col.getRGB());
            ribbon.vertex(matrix, x, y + thick, z).color(col.getRGB());
         }
         Render2DEngine.endBuilding(ribbon);

         // comet trail behind the strand head
         float headT = (float)((time * 0.45 + s * 0.5) % 1.0);
         BufferBuilder trail = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         for (int k = 0; k < HELIX_TRAIL; k++) {
            float f = k / (float)(HELIX_TRAIL - 1);
            float t = MathHelper.clamp(headT - f * 0.12F, 0.0F, 1.0F);
            float y = t * height;
            float ang = spin + phase + t * HELIX_TURNS * MathHelper.TAU;
            float rr = radius * breathe * envelope(t);
            float x = (float)Math.cos(ang) * rr;
            float z = (float)Math.sin(ang) * rr;
            float w = 0.11F * (1.0F - f);
            int alpha = (int)(190 * (1.0F - f) * (1.0F - f));
            Color col = Render2DEngine.injectAlpha(HudEditor.getColor(k * 6 + s * 180), alpha);
            trail.vertex(matrix, x, y - w, z).color(col.getRGB());
            trail.vertex(matrix, x, y + w, z).color(col.getRGB());
         }
         Render2DEngine.endBuilding(trail);

         // glowing node at the strand head
         float ang = spin + phase + headT * HELIX_TURNS * MathHelper.TAU;
         float rr = radius * breathe * envelope(headT);
         drawNode(matrix, (float)Math.cos(ang) * rr, headT * height, (float)Math.sin(ang) * rr, 0.14F,
            HudEditor.getColor(s * 180), 230);
      }

      endState();
      stack.pop();
   }

   /** Radius envelope: pinched at the feet and the head so the helix looks like it wraps the body. */
   private static float envelope(float t) {
      return 0.55F + 0.45F * (float)Math.sin(t * Math.PI);
   }

   // ───────────────────────────── ORBIT ─────────────────────────────

   private static final int ORBIT_RINGS = 3;
   private static final int ORBIT_SEGMENTS = 72;
   private static final int ORBIT_TAIL = 28;

   public static void renderOrbit(MatrixStack stack, Entity target) {
      if (target == null || Module.mc.player == null) {
         return;
      }

      Vec3 pos = interpolatedPos(target);
      float height = target.getHeight();
      float radius = target.getWidth() * 1.05F + 0.25F;
      double time = (System.currentTimeMillis() - ThunderHack.initTime) / 1000.0;

      stack.push();
      stack.translate(pos.x, pos.y + height * 0.5F, pos.z);
      Render3DEngine.setupRender();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);

      // ground halo (drawn in its own space at the feet)
      stack.push();
      stack.translate(0.0, -height * 0.5F + 0.02F, 0.0);
      drawHalo(stack.peek().getPositionMatrix(), target.getWidth() * 1.3F, time);
      stack.pop();

      for (int r = 0; r < ORBIT_RINGS; r++) {
         stack.push();

         // each ring has its own tilt and spin axis, giving the gyroscope look
         float baseTilt = r * (180.0F / ORBIT_RINGS);
         float tiltWobble = (float)Math.sin(time * 0.8 + r * 1.7) * 14.0F;
         stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(baseTilt + (float)(time * 28.0)));
         stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(58.0F + tiltWobble));

         Matrix4f matrix = stack.peek().getPositionMatrix();
         float ringRadius = radius * (1.0F + r * 0.12F);
         float speed = (float)(time * (1.9 + r * 0.55) * (r % 2 == 0 ? 1.0 : -1.0));

         // faint full ring
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         BufferBuilder ring = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
         for (int i = 0; i <= ORBIT_SEGMENTS; i++) {
            double a = i * MathHelper.TAU / ORBIT_SEGMENTS;
            float x = (float)Math.cos(a) * ringRadius;
            float z = (float)Math.sin(a) * ringRadius;
            Color col = Render2DEngine.injectAlpha(HudEditor.getColor(i * 5 + r * 120), 55);
            ring.vertex(matrix, x, -0.012F, z).color(col.getRGB());
            ring.vertex(matrix, x, 0.012F, z).color(col.getRGB());
         }
         Render2DEngine.endBuilding(ring);

         // comet tail
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         BufferBuilder tail = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
         for (int k = 0; k < ORBIT_TAIL; k++) {
            float f = k / (float)(ORBIT_TAIL - 1);
            double a = speed - f * 1.15F * Math.signum(speed == 0 ? 1 : speed);
            float x = (float)Math.cos(a) * ringRadius;
            float z = (float)Math.sin(a) * ringRadius;
            float w = 0.06F * (1.0F - f * 0.7F);
            int alpha = (int)(230 * Math.pow(1.0F - f, 1.8));
            Color col = Render2DEngine.injectAlpha(HudEditor.getColor(k * 7 + r * 120), alpha);
            tail.vertex(matrix, x, -w, z).color(col.getRGB());
            tail.vertex(matrix, x, w, z).color(col.getRGB());
         }
         Render2DEngine.endBuilding(tail);

         // orb at the head of the comet
         drawNode(matrix, (float)Math.cos(speed) * ringRadius, 0.0F, (float)Math.sin(speed) * ringRadius, 0.12F,
            HudEditor.getColor(r * 120), 240);

         stack.pop();
      }

      endState();
      stack.pop();
   }

   private static void drawHalo(Matrix4f matrix, float radius, double time) {
      float pulse = 0.85F + 0.15F * (float)Math.sin(time * 3.2);
      int steps = 48;
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder halo = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
      for (int i = 0; i <= steps; i++) {
         double a = i * MathHelper.TAU / steps;
         float cos = (float)Math.cos(a);
         float sin = (float)Math.sin(a);
         Color inner = Render2DEngine.injectAlpha(HudEditor.getColor(i * 7), 0);
         Color outer = Render2DEngine.injectAlpha(HudEditor.getColor(i * 7), (int)(120 * pulse));
         halo.vertex(matrix, cos * radius * 0.55F * pulse, 0.0F, sin * radius * 0.55F * pulse).color(inner.getRGB());
         halo.vertex(matrix, cos * radius * pulse, 0.0F, sin * radius * pulse).color(outer.getRGB());
      }
      Render2DEngine.endBuilding(halo);
   }

   // ───────────────────────────── SHARED ─────────────────────────────

   /** Camera-facing glow: a soft additive disc made from a fan, billboarded against the current view. */
   private static void drawNode(Matrix4f matrix, float x, float y, float z, float size, Color color, int alpha) {
      Camera camera = Module.mc.gameRenderer.getCamera();
      float yaw = (float)Math.toRadians(-camera.getYaw());
      float pitch = (float)Math.toRadians(camera.getPitch());
      // billboard basis (right / up) relative to the camera
      float rx = (float)Math.cos(yaw);
      float rz = (float)Math.sin(yaw);
      float ux = (float)(Math.sin(pitch) * -Math.sin(yaw));
      float uy = (float)Math.cos(pitch);
      float uz = (float)(Math.sin(pitch) * Math.cos(yaw));

      int steps = 20;
      for (int layer = 0; layer < 2; layer++) {
         float s = layer == 0 ? size : size * 0.45F;
         int a = layer == 0 ? alpha / 3 : alpha;
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         BufferBuilder fan = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
         fan.vertex(matrix, x, y, z).color(Render2DEngine.injectAlpha(color, a).getRGB());
         for (int i = 0; i <= steps; i++) {
            double ang = i * MathHelper.TAU / steps;
            float c = (float)Math.cos(ang) * s;
            float sn = (float)Math.sin(ang) * s;
            fan.vertex(matrix, x + rx * c + ux * sn, y + uy * sn, z + rz * c + uz * sn)
               .color(Render2DEngine.injectAlpha(color, 0).getRGB());
         }
         Render2DEngine.endBuilding(fan);
      }
   }

   private static void endState() {
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      Render3DEngine.endRender();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private static Vec3 interpolatedPos(Entity target) {
      Camera camera = Module.mc.gameRenderer.getCamera();
      float delta = Render3DEngine.getTickDelta();
      return new Vec3(
         Render2DEngine.interpolate(target.prevX, target.getX(), delta) - camera.getPos().x,
         Render2DEngine.interpolate(target.prevY, target.getY(), delta) - camera.getPos().y,
         Render2DEngine.interpolate(target.prevZ, target.getZ(), delta) - camera.getPos().z
      );
   }

   private record Vec3(double x, double y, double z) {
   }
}
