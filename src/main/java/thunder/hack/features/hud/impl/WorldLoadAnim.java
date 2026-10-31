package thunder.hack.features.hud.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

public class WorldLoadAnim extends HudElement {
   private static final long FADE_IN = 450L;
   private static final long HOLD = 1400L;
   private static final long GLITCH_AT = 1000L;
   private static final long FADE_OUT = 700L;
   private static final long TOTAL = 2550L;
   private long lastJoinTime = 0L;
   private boolean wasNull = true;
   private final List<WorldLoadAnim.Particle> particles = new ArrayList<>();
   private boolean particlesSpawned = false;
   private float eltrixSlide = 0.0F;
   private float barProgress = 0.0F;

   public WorldLoadAnim() {
      super("WorldLoadAnim", 0, 0);
   }

   @Override
   public void onUpdate() {
      if (mc.world == null) {
         this.wasNull = true;
         this.particles.clear();
         this.particlesSpawned = false;
         this.eltrixSlide = 0.0F;
         this.barProgress = 0.0F;
      } else if (this.wasNull) {
         this.wasNull = false;
         this.lastJoinTime = System.currentTimeMillis();
      }
   }

   @Override
   public void onRender2D(DrawContext context) {
      if (mc.world != null && mc.player != null) {
         long elapsed = System.currentTimeMillis() - this.lastJoinTime;
         if (elapsed > 2550L) {
            this.particles.clear();
         } else {
            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();
            float cx = (float)sw * 0.5F;
            float cy = (float)sh * 0.5F;
            float alpha;
            if (elapsed < 450L) {
               alpha = (float)elapsed / 450.0F;
            } else if (elapsed < 1850L) {
               alpha = 1.0F;
            } else {
               float t = (float)(elapsed - 450L - 1400L) / 700.0F;
               alpha = 1.0F - t;
            }

            alpha = MathHelper.clamp(alpha, 0.0F, 1.0F);
            float exitScale = 1.0F;
            if (elapsed > 1850L) {
               float t = (float)(elapsed - 450L - 1400L) / 700.0F;
               exitScale = 1.0F + t * 0.18F;
            }

            float popScale = 1.0F;
            if (elapsed < 450L) {
               float t = (float)elapsed / 450.0F;
               popScale = easeOutBack(t);
            }

            float logoScale = popScale * exitScale;
            float holdProgress = (float)Math.max(0L, elapsed - 450L) / 1400.0F;
            this.eltrixSlide = MathHelper.clamp(Render2DEngine.interpolateFloat(this.eltrixSlide, alpha > 0.05F ? 1.0F : 0.0F, 0.25), 0.0F, 1.0F);
            this.barProgress = MathHelper.clamp(holdProgress * 1.05F, 0.0F, 1.0F);
            if (!this.particlesSpawned && (double)elapsed > 270.0) {
               this.spawnParticles(cx, cy);
               this.particlesSpawned = true;
            }

            MatrixStack ms = context.getMatrices();
            ms.push();
            int bgA = (int)(255.0F * alpha);
            this.drawFullBackground(ms, sw, sh, bgA);
            this.updateAndDrawParticles(ms, alpha);
            ms.push();
            ms.translate(cx, cy, 0.0F);
            ms.scale(logoScale, logoScale, 1.0F);
            ms.translate(-cx, -cy, 0.0F);
            this.drawZGlow(ms, cx, cy, alpha);
            int themeC = HudEditor.getColor(0).getRGB();
            String zStr = "Z";
            float zW = FontRenderers.sf_bold.getStringWidth(zStr) * 2.2F;
            float zH = 39.600002F;
            String eltrixStr = "eltrix";
            float eltrixW = FontRenderers.sf_bold.getStringWidth(eltrixStr) * 2.2F;
            float totalW = zW + eltrixW + 4.0F;
            float logoX = cx - totalW * 0.5F;
            float logoY = cy - zH * 0.5F - 4.0F;
            Render2DEngine.drawBlurredShadow(
               ms,
               logoX - 6.0F,
               logoY - 4.0F,
               zW + 12.0F,
               zH + 8.0F,
               12,
               new Color(themeC >> 16 & 0xFF, themeC >> 8 & 0xFF, themeC & 0xFF, (int)(160.0F * alpha))
            );
            ms.push();
            ms.translate(logoX + zW * 0.5F, logoY + zH * 0.5F, 0.0F);
            ms.scale(2.2F, 2.2F, 1.0F);
            ms.translate(-(logoX + zW * 0.5F), -(logoY + zH * 0.5F), 0.0F);
            FontRenderers.sf_bold.drawString(ms, zStr, (double)logoX, (double)logoY, withAlpha(themeC, (int)(255.0F * alpha)));
            ms.pop();
            float slideOff = (1.0F - this.eltrixSlide) * 35.0F;
            float eltrixX = logoX + zW + 4.0F + slideOff;
            float eltrixAlpha = this.eltrixSlide * alpha;
            ms.push();
            ms.translate(eltrixX + eltrixW * 0.5F, logoY + zH * 0.5F, 0.0F);
            ms.scale(2.2F, 2.2F, 1.0F);
            ms.translate(-(eltrixX + eltrixW * 0.5F), -(logoY + zH * 0.5F), 0.0F);
            FontRenderers.sf_bold.drawString(ms, eltrixStr, (double)eltrixX, (double)logoY, withAlpha(16777215, (int)(255.0F * eltrixAlpha)));
            ms.pop();
            float lineY = logoY + zH + 6.0F;
            float lineHalf = totalW * 0.5F * this.eltrixSlide;
            this.drawGradientLine(ms, cx - lineHalf, lineY, cx + lineHalf, lineY + 0.9F, themeC, alpha);
            ms.pop();
            float barW = Math.min((float)sw * 0.35F, 220.0F);
            float barH = 2.5F;
            float barX = cx - barW * 0.5F;
            float barY = cy + 40.0F;
            this.drawProgressBar(ms, barX, barY, barW, barH, this.barProgress, themeC, alpha);
            String sub = this.barProgress < 0.99F ? "Loading world..." : "Ready!";
            float subW = FontRenderers.sf_bold_mini.getStringWidth(sub);
            FontRenderers.sf_bold_mini
               .drawString(ms, sub, (double)(cx - subW * 0.5F), (double)(barY + 6.0F), withAlpha(12303291, (int)(200.0F * alpha * this.eltrixSlide)));
            if (elapsed > 1000L && elapsed < 1080L) {
               float gT = (float)(elapsed - 1000L) / 80.0F;
               float gA = (float)Math.sin((double)gT * Math.PI) * 0.18F * alpha;
               Render2DEngine.drawRect(
                  ms, 0.0F, 0.0F, (float)sw, (float)sh, new Color(themeC >> 16 & 0xFF, themeC >> 8 & 0xFF, themeC & 0xFF, (int)(255.0F * gA))
               );
            }

            ms.pop();
         }
      }
   }

   private void drawFullBackground(MatrixStack ms, int sw, int sh, int bgA) {
      Render2DEngine.drawRect(ms, 0.0F, 0.0F, (float)sw, (float)sh, new Color(8, 8, 14, bgA));
      int vigA = (int)((float)bgA * 0.45F);
      Render2DEngine.drawBlurredShadow(ms, (float)sw * 0.1F, (float)sh * 0.1F, (float)sw * 0.8F, (float)sh * 0.8F, 40, new Color(0, 0, 0, 0));
   }

   private void drawZGlow(MatrixStack ms, float cx, float cy, float alpha) {
      double pulse = 0.5 + 0.5 * Math.sin((double)System.currentTimeMillis() / 380.0);
      int themeC = HudEditor.getColor(0).getRGB();
      int r = themeC >> 16 & 0xFF;
      int g = themeC >> 8 & 0xFF;
      int b = themeC & 0xFF;

      for (int i = 5; i >= 1; i--) {
         float size = 55.0F + (float)i * 18.0F;
         float ga = (float)(0.012 * (double)(6 - i) * (double)alpha * (0.7 + 0.3 * pulse));
         Render2DEngine.drawBlurredShadow(ms, cx - size, cy - size, size * 2.0F, size * 2.0F, 22 - i * 2, new Color(r, g, b, (int)(255.0F * ga)));
      }
   }

   private void drawGradientLine(MatrixStack ms, float x1, float y, float x2, float yb, int themeC, float alpha) {
      int r = themeC >> 16 & 0xFF;
      int g = themeC >> 8 & 0xFF;
      int b = themeC & 0xFF;
      float mid = (x1 + x2) * 0.5F;
      Color transparent = new Color(r, g, b, 0);
      Color solid = new Color(r, g, b, (int)(255.0F * alpha));
      Render2DEngine.horizontalGradient(ms, x1, y, mid, yb, transparent, solid);
      Render2DEngine.horizontalGradient(ms, mid, y, x2, yb, solid, transparent);
   }

   private void drawProgressBar(MatrixStack ms, float x, float y, float w, float h, float progress, int themeC, float alpha) {
      int a = (int)(255.0F * alpha);
      Render2DEngine.drawRound(ms, x, y, w, h, h * 0.5F, new Color(30, 30, 40, (int)(180.0F * alpha)));
      if (progress > 0.01F) {
         float filled = w * progress;
         int r = themeC >> 16 & 0xFF;
         int g = themeC >> 8 & 0xFF;
         int b = themeC & 0xFF;
         Render2DEngine.drawRound(ms, x, y, filled, h, h * 0.5F, new Color(r, g, b, a));
         Render2DEngine.drawBlurredShadow(ms, x + filled - 3.0F, y - 2.0F, 6.0F, h + 4.0F, 6, new Color(r, g, b, (int)(180.0F * alpha)));
      }
   }

   private void spawnParticles(float cx, float cy) {
      Random rng = new Random();
      int themeC = HudEditor.getColor(0).getRGB();

      for (int i = 0; i < 28; i++) {
         double angle = Math.toRadians((double)i * 12.857142857142858 + (double)(rng.nextFloat() * 12.0F));
         float speed = 0.4F + rng.nextFloat() * 1.6F;
         float vx = (float)(Math.cos(angle) * (double)speed);
         float vy = (float)(Math.sin(angle) * (double)speed);
         float radius = 1.2F + rng.nextFloat() * 2.2F;
         float life = 0.5F + rng.nextFloat() * 0.5F;
         Color pc;
         if (rng.nextBoolean()) {
            pc = new Color(themeC >> 16 & 0xFF, themeC >> 8 & 0xFF, themeC & 0xFF, 255);
         } else {
            pc = new Color(200, 180, 255, 255);
         }

         this.particles.add(new WorldLoadAnim.Particle(cx, cy, vx, vy, radius, life, pc));
      }
   }

   private void updateAndDrawParticles(MatrixStack ms, float globalAlpha) {
      long now = System.currentTimeMillis();
      this.particles.removeIf(px -> px.isDead(now));

      for (WorldLoadAnim.Particle p : this.particles) {
         p.update(now);
         float pa = p.getAlpha(now) * globalAlpha;
         if (!(pa < 0.01F)) {
            Color c = p.color;
            Render2DEngine.drawRound(
               ms,
               p.x - p.radius,
               p.y - p.radius,
               p.radius * 2.0F,
               p.radius * 2.0F,
               p.radius,
               new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(255.0F * pa))
            );
         }
      }
   }

   private static float easeOutBack(float t) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow((double)(t - 1.0F), 3.0) + c1 * (float)Math.pow((double)(t - 1.0F), 2.0);
   }

   private static int withAlpha(int rgb, int alpha) {
      alpha = MathHelper.clamp(alpha, 0, 255);
      return alpha << 24 | rgb & 16777215;
   }

   private static class Particle {
      float x;
      float y;
      final float vx;
      final float vy;
      final float radius;
      final float lifetime;
      final Color color;
      final long born;

      Particle(float x, float y, float vx, float vy, float radius, float lifetime, Color color) {
         this.x = x;
         this.y = y;
         this.vx = vx;
         this.vy = vy;
         this.radius = radius;
         this.lifetime = lifetime;
         this.color = color;
         this.born = System.currentTimeMillis();
      }

      boolean isDead(long now) {
         return now - this.born >= (long)(this.lifetime * 1000.0F);
      }

      void update(long now) {
         float t = (float)(now - this.born) / 1000.0F;
         this.x = this.x + this.vx;
         this.y = this.y + this.vy - t * 0.12F;
      }

      float getAlpha(long now) {
         float t = (float)(now - this.born) / (this.lifetime * 1000.0F);
         return t < 0.15F ? t / 0.15F : 1.0F - (t - 0.15F) / 0.85F;
      }
   }
}
