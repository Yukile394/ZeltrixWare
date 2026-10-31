package thunder.hack.features.modules.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

public class TargetESP extends Module {
   public final Setting<TargetESP.Mode> mode = new Setting<>("Mode", TargetESP.Mode.Ghost);
   public final Setting<Float> ghostSpeed = new Setting<>(
      "GhostSpeed", 1.0F, 0.5F, 2.0F, v -> this.mode.getValue() == TargetESP.Mode.Ghost || this.mode.getValue() == TargetESP.Mode.Prizraki
   );
   public final Setting<Integer> ghostV2Trails = new Setting<>("V2Trails", 3, 1, 5, v -> this.mode.getValue() == TargetESP.Mode.GhostV2);
   public final Setting<Integer> ghostV2Length = new Setting<>("V2Length", 32, 10, 60, v -> this.mode.getValue() == TargetESP.Mode.GhostV2);
   public final Setting<Float> ghostV2Speed = new Setting<>("V2Speed", 2.0F, 0.5F, 6.0F, v -> this.mode.getValue() == TargetESP.Mode.GhostV2);
   public final Setting<Float> ghostV2Radius = new Setting<>("V2Radius", 0.75F, 0.2F, 2.0F, v -> this.mode.getValue() == TargetESP.Mode.GhostV2);
   public final Setting<Float> ghostV2Spread = new Setting<>("V2Spread", 0.85F, 0.1F, 2.0F, v -> this.mode.getValue() == TargetESP.Mode.GhostV2);
   public final Setting<Float> ghostV2Wobble = new Setting<>("V2Wobble", 0.35F, 0.0F, 1.0F, v -> this.mode.getValue() == TargetESP.Mode.GhostV2);
   public final Setting<Float> ghostV2Size = new Setting<>("V2Size", 0.38F, 0.1F, 0.8F, v -> this.mode.getValue() == TargetESP.Mode.GhostV2);
   public final Setting<Boolean> ghostV2Rainbow = new Setting<>("V2Rainbow", false, v -> this.mode.getValue() == TargetESP.Mode.GhostV2);
   public final Setting<Float> prizrakiSize = new Setting<>("PrizrakiSize", 0.4F, 0.1F, 1.0F, v -> this.mode.getValue() == TargetESP.Mode.Prizraki);
   public final Setting<Float> kolcoSpeed = new Setting<>("KolcoSpeed", 0.01F, 0.05F, 0.3F, v -> this.mode.getValue() == TargetESP.Mode.Kolco);
   public final Setting<Boolean> kolcoRainbow = new Setting<>("KolcoRainbow", true, v -> this.mode.getValue() == TargetESP.Mode.Kolco);
   public final Setting<Boolean> kolcoDamageRed = new Setting<>("DamageRed", true, v -> this.mode.getValue() == TargetESP.Mode.Kolco);
   public final Setting<Boolean> kolcoThroughWalls = new Setting<>("ThroughWalls", false, v -> this.mode.getValue() == TargetESP.Mode.Kolco);
   public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(135, 80, 255).getRGB()));
   public final Setting<Boolean> drawBox = new Setting<>("DrawBox", true);
   public final Setting<ColorSetting> boxColor = new Setting<>("BoxColor", new ColorSetting(new Color(255, 255, 255).getRGB()), v -> this.drawBox.getValue());
   public final Setting<Boolean> predict = new Setting<>("Predict", false);
   public final Setting<Float> predictTicks = new Setting<>("PredictTicks", 10.0F, 1.0F, 40.0F, v -> this.predict.getValue());
   public final Setting<ColorSetting> predictColor = new Setting<>(
      "PredictColor", new ColorSetting(new Color(255, 0, 0).getRGB()), v -> this.predict.getValue()
   );
   private final TargetESP.SimpleAnimation animation = new TargetESP.SimpleAnimation(500L);
   private Entity lastTarget = null;
   private float ghostAnimTime = 0.0F;
   private long lastGhostTimestamp = 0L;
   private final TargetESP.SimpleAnimation ghostV2Anim = new TargetESP.SimpleAnimation(350L);
   private Entity ghostV2LastTarget = null;
   private float ghostV2AnimTime = 0.0F;
   private long lastGhostV2Timestamp = 0L;
   private static long lastPrizrakTime = System.currentTimeMillis();
   private long lastKolcoUpdateTime = 0L;
   private double kolcoStep = 0.0;
   private float kolcoAnimProgress = 0.0F;
   private long lastDamageTime = 0L;
   private float damageFlashIntensity = 0.0F;
   private final ArrayList<TargetESP.Particle> particles = new ArrayList<>();
   private float spawnAccumulator = 0.0F;
   private long lastCubeTime = 0L;
   private final TargetESP.SimpleAnimation crystalsAnim = new TargetESP.SimpleAnimation(350L);
   private Entity crystalsLastTarget = null;
   private final List<TargetESP.RiderParticle> riderParticles = new ArrayList<>();
   private final TargetESP.SimpleAnimation riderAnim = new TargetESP.SimpleAnimation(300L);
   private float riderMovingAngle = 0.0F;

   public TargetESP() {
      super("TargetESP", Module.Category.RENDER);
   }

   @Override
   public void onRender3D(MatrixStack matrices) {
      Camera camera = mc.gameRenderer.getCamera();
      float tickDelta = Render3DEngine.getTickDelta();
      Entity target = Aura.target;
      if (target != null && target != mc.player && !(target instanceof ArmorStandEntity)) {
         this.lastTarget = target;
         this.animation.run(1.0F);
      } else {
         this.animation.run(0.0F);
         if (this.animation.getValue() == 0.0F) {
            this.lastTarget = null;
         }
      }

      if (target != null && this.lastTarget != null) {
         switch ((TargetESP.Mode)this.mode.getValue()) {
            case Ghost:
               this.renderGhost(matrices, camera, (LivingEntity)this.lastTarget, tickDelta);
               break;
            case GhostV2:
               this.ghostV2LastTarget = this.lastTarget;
               this.ghostV2Anim.run(1.0F);
               float v2Anim = this.ghostV2Anim.getValue();
               if (v2Anim > 0.001F) {
                  this.renderGhostV2(matrices, camera, (LivingEntity)this.ghostV2LastTarget, v2Anim, tickDelta);
               }
               break;
            case Crystals:
               this.crystalsLastTarget = this.lastTarget;
               this.crystalsAnim.run(1.0F);
               float anim = this.crystalsAnim.getValue();
               if (anim > 0.001F) {
                  this.renderCrystals(matrices, camera, (LivingEntity)this.crystalsLastTarget, false, anim, tickDelta);
               }
               break;
            case Crystals2:
               this.crystalsLastTarget = this.lastTarget;
               this.crystalsAnim.run(1.0F);
               float anim2 = this.crystalsAnim.getValue();
               if (anim2 > 0.001F) {
                  this.renderCrystals(matrices, camera, (LivingEntity)this.crystalsLastTarget, true, anim2, tickDelta);
               }
               break;
            case Cubes:
               this.renderCubes(matrices, camera, (LivingEntity)this.lastTarget, tickDelta);
               break;
            case Prizraki:
               this.renderPrizraki(matrices, camera, (LivingEntity)this.lastTarget, tickDelta);
               break;
            case Kolco:
               this.kolcoAnimProgress = Math.min(1.0F, this.kolcoAnimProgress + 0.1F);
               if (this.lastTarget instanceof LivingEntity living && living.hurtTime > 0) {
                  this.lastDamageTime = System.currentTimeMillis();
               }

               long now = System.currentTimeMillis();
               if (this.lastKolcoUpdateTime == 0L) {
                  this.lastKolcoUpdateTime = now;
               }

               float dt = Math.min((float)(now - this.lastKolcoUpdateTime) / 1000.0F, 0.1F);
               this.lastKolcoUpdateTime = now;
               this.kolcoStep = this.kolcoStep + (double)(this.kolcoSpeed.getValue() * dt) * 60.0;
               this.renderKolco(matrices, camera, (LivingEntity)this.lastTarget, tickDelta, this.kolcoAnimProgress);
               break;
            case GhostRider:
               this.renderGhostRider(matrices, camera, (LivingEntity)this.lastTarget, tickDelta);
         }

         if (this.predict.getValue()) {
            this.renderPredictBox(matrices, camera, (LivingEntity)this.lastTarget, tickDelta);
         }

         if (this.drawBox.getValue() && this.lastTarget instanceof LivingEntity) {
            Box box = this.lastTarget.getBoundingBox();
            Render3DEngine.drawFilledBox(matrices, box, Render2DEngine.injectAlpha(this.boxColor.getValue().getColorObject(), 50));
            Render3DEngine.drawBoxOutline(box, this.boxColor.getValue().getColorObject(), 2.0F);
         }
      } else {
         this.lastGhostTimestamp = 0L;
         this.ghostAnimTime = 0.0F;
         this.lastCubeTime = 0L;
         this.spawnAccumulator = 0.0F;
         this.riderParticles.clear();
         if (this.mode.getValue() == TargetESP.Mode.GhostV2) {
            this.ghostV2Anim.run(0.0F);
            float animx = this.ghostV2Anim.getValue();
            if (animx > 0.001F && this.ghostV2LastTarget != null) {
               this.renderGhostV2(matrices, camera, (LivingEntity)this.ghostV2LastTarget, animx, tickDelta);
            } else if (animx <= 0.001F) {
               this.ghostV2LastTarget = null;
               this.lastGhostV2Timestamp = 0L;
            }
         }

         if (this.mode.getValue() == TargetESP.Mode.Crystals || this.mode.getValue() == TargetESP.Mode.Crystals2) {
            this.crystalsAnim.run(0.0F);
            float animx = this.crystalsAnim.getValue();
            if (animx > 0.001F && this.crystalsLastTarget != null) {
               this.renderCrystals(matrices, camera, (LivingEntity)this.crystalsLastTarget, this.mode.getValue() == TargetESP.Mode.Crystals2, animx, tickDelta);
            } else if (animx <= 0.001F) {
               this.crystalsLastTarget = null;
            }
         }

         if (this.mode.getValue() == TargetESP.Mode.Kolco) {
            this.kolcoAnimProgress = Math.max(0.0F, this.kolcoAnimProgress - 0.05F);
            if (this.kolcoAnimProgress > 0.0F && this.lastTarget != null) {
               long now = System.currentTimeMillis();
               if (this.lastKolcoUpdateTime == 0L) {
                  this.lastKolcoUpdateTime = now;
               }

               float dt = Math.min((float)(now - this.lastKolcoUpdateTime) / 1000.0F, 0.1F);
               this.lastKolcoUpdateTime = now;
               this.kolcoStep = this.kolcoStep + (double)(this.kolcoSpeed.getValue() * dt) * 60.0;
               this.renderKolco(matrices, camera, (LivingEntity)this.lastTarget, tickDelta, this.kolcoAnimProgress);
            }
         } else {
            this.lastKolcoUpdateTime = 0L;
         }

         if (this.mode.getValue() == TargetESP.Mode.Cubes && !this.particles.isEmpty()) {
            this.renderCubes(matrices, camera, null, tickDelta);
         }
      }
   }

   private void renderGhost(MatrixStack matrices, Camera camera, LivingEntity target, float partialTicks) {
      Vec3d camPos = camera.getPos();
      float camYaw = camera.getYaw();
      float camPitch = camera.getPitch();
      double tx = MathHelper.lerp((double)partialTicks, target.lastRenderX, target.getX());
      double ty = MathHelper.lerp((double)partialTicks, target.lastRenderY, target.getY());
      double tz = MathHelper.lerp((double)partialTicks, target.lastRenderZ, target.getZ());
      double x = tx - camPos.x;
      double y = ty - camPos.y;
      double z = tz - camPos.z;
      long now = System.currentTimeMillis();
      if (this.lastGhostTimestamp == 0L) {
         this.lastGhostTimestamp = now;
      }

      this.ghostAnimTime = this.ghostAnimTime + 4.0F * (float)(now - this.lastGhostTimestamp) / 600.0F * this.ghostSpeed.getValue();
      this.lastGhostTimestamp = now;
      Color c = this.color.getValue().getColorObject();
      float r = (float)c.getRed() / 255.0F;
      float g = (float)c.getGreen() / 255.0F;
      float b = (float)c.getBlue() / 255.0F;
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      boolean hasGhost = false;
      float radiusMod = 1.3F;
      float spriteSize = 0.5F;
      int totalSprites = 25;

      for (int ring = 0; ring < 9; ring += 3) {
         for (int si = 0; si < totalSprites; si++) {
            float ao = (float)si * 0.1F;
            float ro = (float)Math.pow((double)ring, 2.0);
            double sx = x + (double)(radiusMod * 0.6F * MathHelper.sin(this.ghostAnimTime + ao + ro));
            double sy = y + 0.4 + (double)(radiusMod * 0.3F * MathHelper.sin(this.ghostAnimTime + (float)si * 0.2F)) + (double)(0.2F * (float)ring);
            double sz = z + (double)(radiusMod * 0.6F * MathHelper.cos(this.ghostAnimTime + ao - ro));
            float progress = (float)si / (float)(totalSprites - 1);
            float scale = spriteSize * (0.2F + progress * 0.8F);
            float alpha = 1.0F - progress * 0.9F;
            if (!(scale < 0.01F) && !(alpha < 0.01F)) {
               float cr = Math.min(1.0F, r * 1.5F);
               float cg = Math.min(1.0F, g * 1.5F);
               float cb = Math.min(1.0F, b * 1.5F);
               this.putBloomQuad(builder, matrices, sx, sy, sz, scale * 4.0F, r, g, b, alpha * 0.15F, camYaw, camPitch);
               this.putBloomQuad(builder, matrices, sx, sy, sz, scale * 2.0F, r, g, b, alpha * 0.35F, camYaw, camPitch);
               this.putBloomQuad(builder, matrices, sx, sy, sz, scale * 0.8F, cr, cg, cb, alpha, camYaw, camPitch);
               hasGhost = true;
            }
         }
      }

      if (hasGhost) {
         BufferRenderer.drawWithGlobalProgram(builder.end());
      }

      this.restoreRenderState();
   }

   private void renderGhostV2(MatrixStack matrices, Camera camera, LivingEntity target, float animProgress, float partialTicks) {
      if (target != null && !(animProgress <= 0.001F)) {
         Vec3d camPos = camera.getPos();
         float camYaw = camera.getYaw();
         float camPitch = camera.getPitch();
         double tx = MathHelper.lerp((double)partialTicks, target.lastRenderX, target.getX()) - camPos.x;
         double ty = MathHelper.lerp((double)partialTicks, target.lastRenderY, target.getY()) - camPos.y;
         double tz = MathHelper.lerp((double)partialTicks, target.lastRenderZ, target.getZ()) - camPos.z;
         long now = System.currentTimeMillis();
         if (this.lastGhostV2Timestamp == 0L) {
            this.lastGhostV2Timestamp = now;
         }

         float dt = Math.min((float)(now - this.lastGhostV2Timestamp) / 1000.0F, 0.1F);
         this.lastGhostV2Timestamp = now;
         this.ghostV2AnimTime = this.ghostV2AnimTime + dt * this.ghostV2Speed.getValue() * 120.0F;
         int trails = this.ghostV2Trails.getValue();
         int length = this.ghostV2Length.getValue();
         float radiusBase = (target.getWidth() * 0.5F + this.ghostV2Radius.getValue()) * animProgress;
         float baseSize = this.ghostV2Size.getValue();
         float wobble = this.ghostV2Wobble.getValue();
         float spread = this.ghostV2Spread.getValue();
         float entityHeight = target.getHeight();
         Color baseColor = this.color.getValue().getColorObject();
         boolean isRainbow = this.ghostV2Rainbow.getValue();
         float hurtBoost = target.hurtTime > 0 ? (float)target.hurtTime / 10.0F : 0.0F;
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.setShaderTexture(0, TextureStorage.firefly);
         RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

         for (int j = 0; j < trails; j++) {
            float trailProgress = trails > 1 ? (float)j / (float)(trails - 1) : 0.5F;
            double trailBaseY = ty + (double)entityHeight * 0.12 + (double)(entityHeight * spread * trailProgress);
            float baseAngle = this.ghostV2AnimTime + (float)j * (360.0F / (float)trails);

            for (int i = 0; i <= length; i++) {
               float t = (float)i / (float)length;
               float arcAngle = baseAngle - (1.0F - t) * 115.0F;
               double radAngle = Math.toRadians((double)arcAngle);
               double wave = Math.sin(Math.toRadians((double)(this.ghostV2AnimTime * 2.2F + (float)i * 5.0F + (float)j * 120.0F))) * (double)wobble * 0.22;
               double rad = (double)radiusBase + Math.cos(Math.toRadians((double)(this.ghostV2AnimTime * 1.5F + (float)j * 90.0F))) * (double)wobble * 0.08;
               double px = tx + Math.cos(radAngle) * rad;
               double py = trailBaseY + wave;
               double pz = tz + Math.sin(radAngle) * rad;
               float quadScale = baseSize * (0.22F + 0.78F * t) * animProgress;
               float quadAlpha = (0.1F + 0.9F * t) * animProgress;
               float r;
               float g;
               float b;
               if (isRainbow) {
                  float hue = ((float)(now % 3000L) / 3000.0F + (float)j * 0.25F + t * 0.1F) % 1.0F;
                  int rgb = Color.HSBtoRGB(hue, 0.75F, 1.0F);
                  r = (float)(rgb >> 16 & 0xFF) / 255.0F;
                  g = (float)(rgb >> 8 & 0xFF) / 255.0F;
                  b = (float)(rgb & 0xFF) / 255.0F;
               } else {
                  r = (float)baseColor.getRed() / 255.0F;
                  g = (float)baseColor.getGreen() / 255.0F;
                  b = (float)baseColor.getBlue() / 255.0F;
               }

               if (hurtBoost > 0.05F) {
                  r = MathHelper.lerp(hurtBoost * 0.7F, r, 1.0F);
                  g = MathHelper.lerp(hurtBoost * 0.7F, g, 0.2F);
                  b = MathHelper.lerp(hurtBoost * 0.7F, b, 0.3F);
               }

               this.putTexBloomQuad(builder, matrices, px, py, pz, quadScale * 2.2F, r, g, b, quadAlpha * 0.32F, camYaw, camPitch);
               this.putTexBloomQuad(builder, matrices, px, py, pz, quadScale * 1.2F, r, g, b, quadAlpha * 0.75F, camYaw, camPitch);
               float coreBrightness = t * t * 0.9F;
               float cr = Math.min(1.0F, r + coreBrightness * 0.65F);
               float cg = Math.min(1.0F, g + coreBrightness * 0.65F);
               float cb = Math.min(1.0F, b + coreBrightness * 0.65F);
               this.putTexBloomQuad(builder, matrices, px, py, pz, quadScale * 0.6F, cr, cg, cb, quadAlpha * (0.85F + 0.15F * t), camYaw, camPitch);
            }
         }

         BufferRenderer.drawWithGlobalProgram(builder.end());
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   private void putTexBloomQuad(
      BufferBuilder builder, MatrixStack matrices, double x, double y, double z, float size, float r, float g, float b, float a, float yaw, float pitch
   ) {
      matrices.push();
      matrices.translate(x, y, z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      float half = size / 2.0F;
      builder.vertex(matrix, -half, half, 0.0F).texture(0.0F, 1.0F).color(r, g, b, a);
      builder.vertex(matrix, half, half, 0.0F).texture(1.0F, 1.0F).color(r, g, b, a);
      builder.vertex(matrix, half, -half, 0.0F).texture(1.0F, 0.0F).color(r, g, b, a);
      builder.vertex(matrix, -half, -half, 0.0F).texture(0.0F, 0.0F).color(r, g, b, a);
      matrices.pop();
   }

   private void renderPrizraki(MatrixStack matrices, Camera camera, LivingEntity target, float partialTicks) {
      Vec3d camPos = camera.getPos();
      float camYaw = camera.getYaw();
      float camPitch = camera.getPitch();
      double tx = MathHelper.lerp((double)partialTicks, target.lastRenderX, target.getX());
      double ty = MathHelper.lerp((double)partialTicks, target.lastRenderY, target.getY()) + 0.38 + (double)target.getHeight() / 2.0;
      double tz = MathHelper.lerp((double)partialTicks, target.lastRenderZ, target.getZ());
      double rx = tx - camPos.x + 0.2;
      double ry = ty - camPos.y;
      double rz = tz - camPos.z;
      double radius = 0.4 + (double)target.getWidth() / 2.0;
      float speed = 30.0F / this.ghostSpeed.getValue();
      float size = this.prizrakiSize.getValue();
      double distance = 6.0;
      int length = 34;
      long now = System.currentTimeMillis();
      Color c = this.color.getValue().getColorObject();
      float r = (float)c.getRed() / 255.0F;
      float g = (float)c.getGreen() / 255.0F;
      float b = (float)c.getBlue() / 255.0F;
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

      for (int trail = 0; trail < 3; trail++) {
         for (int i = 0; i < length; i++) {
            double angle = 0.05 * ((double)(now - lastPrizrakTime) - (double)i * distance) / (double)speed;
            double s = Math.sin(angle * Math.PI) * radius;
            double cos = Math.cos(angle * Math.PI) * radius;
            double o = trail == 0 ? Math.cos(angle * Math.PI) * radius : Math.sin(angle * Math.PI) * radius;
            float t = (float)i / (float)(length - 1);
            float curSize = size * (1.0F - t * 0.5F);
            float alpha = 1.0F - t * 0.9F;
            double px = rx + (trail == 1 ? -s : s);
            double py = ry + o;
            double pz = rz + (trail == 2 ? cos : -cos);
            float cr = Math.min(1.0F, r * 1.5F);
            float cg = Math.min(1.0F, g * 1.5F);
            float cb = Math.min(1.0F, b * 1.5F);
            this.putBloomQuad(buffer, matrices, px, py, pz, curSize * 0.6F, r, g, b, alpha * 0.15F, camYaw, camPitch);
            this.putBloomQuad(buffer, matrices, px, py, pz, curSize * 0.35F, r, g, b, alpha * 0.35F, camYaw, camPitch);
            this.putBloomQuad(buffer, matrices, px, py, pz, curSize * 0.15F, cr, cg, cb, alpha, camYaw, camPitch);
         }
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
      this.restoreRenderState();
   }

   private void renderKolco(MatrixStack matrices, Camera camera, LivingEntity target, float partialTicks, float animProgress) {
      if (!(animProgress <= 0.0F) && target != null) {
         Vec3d camPos = camera.getPos();
         float camYaw = camera.getYaw();
         float camPitch = camera.getPitch();
         double tx = MathHelper.lerp((double)partialTicks, target.lastRenderX, target.getX());
         double ty = MathHelper.lerp((double)partialTicks, target.lastRenderY, target.getY());
         double tz = MathHelper.lerp((double)partialTicks, target.lastRenderZ, target.getZ());
         double rx = tx - camPos.x;
         double ry = ty - camPos.y;
         double rz = tz - camPos.z;
         float entityWidth = target.getWidth() * 0.9F;
         float entityHeight = target.getHeight();
         float eased = easeOutCubic(animProgress);
         boolean canSee = mc.player != null && mc.player.canSee(target);
         if (this.kolcoThroughWalls.getValue() || canSee) {
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            boolean hasKolco = false;
            double golovkaY = this.absSin(this.kolcoStep) * (double)entityHeight;
            float spriteSize = 0.12F;
            int totalPoints = 72;
            long now = System.currentTimeMillis();

            for (int i = 0; i < totalPoints; i++) {
               double angle = (Math.PI * 2) * (double)i / (double)totalPoints;
               float xOff = (float)(Math.cos(angle) * (double)entityWidth);
               float zOff = (float)(Math.sin(angle) * (double)entityWidth);
               int baseColor = this.getKolcoColor(i * (360 / totalPoints), now);
               float cr = (float)(baseColor >> 16 & 0xFF) / 255.0F;
               float cg = (float)(baseColor >> 8 & 0xFF) / 255.0F;
               float cb = (float)(baseColor & 0xFF) / 255.0F;
               float ss = eased * spriteSize;
               if (!(ss < 0.01F)) {
                  this.putBloomQuad(
                     builder, matrices, rx + (double)xOff, ry + golovkaY, rz + (double)zOff, ss * 4.0F, cr, cg, cb, eased * 0.15F, camYaw, camPitch
                  );
                  this.putBloomQuad(
                     builder, matrices, rx + (double)xOff, ry + golovkaY, rz + (double)zOff, ss * 2.0F, cr, cg, cb, eased * 0.35F, camYaw, camPitch
                  );
                  this.putBloomQuad(
                     builder,
                     matrices,
                     rx + (double)xOff,
                     ry + golovkaY,
                     rz + (double)zOff,
                     ss * 0.8F,
                     Math.min(1.0F, cr * 1.5F),
                     Math.min(1.0F, cg * 1.5F),
                     Math.min(1.0F, cb * 1.5F),
                     eased,
                     camYaw,
                     camPitch
                  );
                  hasKolco = true;
               }
            }

            if (hasKolco) {
               BufferRenderer.drawWithGlobalProgram(builder.end());
            }

            this.restoreRenderState();
         }
      }
   }

   private double absSin(double input) {
      return Math.abs(1.0 + Math.sin(input)) / 2.0;
   }

   private int getKolcoColor(int offsetAngle, long now) {
      int c;
      if (this.kolcoRainbow.getValue()) {
         float timeFactor = (float)(now % 3000L) / 3000.0F;
         int hue = (int)(timeFactor * 360.0F) + offsetAngle;
         c = Color.HSBtoRGB((float)(hue % 360) / 360.0F, 0.7F, 1.0F);
         c = 0xFF000000 | c & 16777215;
      } else {
         c = this.color.getValue().getColor();
         if ((c >> 24 & 0xFF) == 0) {
            c = 0xFF000000 | c & 16777215;
         }
      }

      return this.applyDamageFlash(c);
   }

   private int applyDamageFlash(int c) {
      if (!this.kolcoDamageRed.getValue()) {
         return c;
      } else {
         long timeSince = System.currentTimeMillis() - this.lastDamageTime;
         float target = 0.0F;
         if (timeSince < 500L) {
            target = 1.0F - easeOutCubic((float)timeSince / 500.0F);
         }

         this.damageFlashIntensity = MathHelper.lerp(0.1F, this.damageFlashIntensity, target);
         if (this.damageFlashIntensity < 0.05F) {
            return c;
         } else {
            int a = c >> 24 & 0xFF;
            int r = c >> 16 & 0xFF;
            int g = c >> 8 & 0xFF;
            int b = c & 0xFF;
            return a << 24
               | (int)(MathHelper.lerp(this.damageFlashIntensity, (float)r / 255.0F, 1.0F) * 255.0F) << 16
               | (int)(MathHelper.lerp(this.damageFlashIntensity, (float)g / 255.0F, 0.2F) * 255.0F) << 8
               | (int)(MathHelper.lerp(this.damageFlashIntensity, (float)b / 255.0F, 0.2F) * 255.0F);
         }
      }
   }

   private void renderCrystals(MatrixStack matrices, Camera camera, LivingEntity target, boolean sharp, float anim, float partialTicks) {
      if (target != null && mc.player != null) {
         float eased = easeOutCubic(anim);
         float time = ((float)mc.player.age + partialTicks) * 6.0F;
         Vec3d camPos = camera.getPos();
         double tx = MathHelper.lerp((double)partialTicks, target.lastRenderX, target.getX());
         double ty = MathHelper.lerp((double)partialTicks, target.lastRenderY, target.getY());
         double tz = MathHelper.lerp((double)partialTicks, target.lastRenderZ, target.getZ());
         float camYaw = camera.getYaw();
         float camPitch = camera.getPitch();
         float entityHeight = target.getHeight();
         float halfWidth = target.getWidth() * 0.5F;
         Color baseColor = this.color.getValue().getColorObject();
         float r = Math.min(1.0F, (float)baseColor.getRed() / 255.0F * 1.3F);
         float g = Math.min(1.0F, (float)baseColor.getGreen() / 255.0F * 1.3F);
         float b = Math.min(1.0F, (float)baseColor.getBlue() / 255.0F * 1.3F);
         matrices.push();
         matrices.translate(tx - camPos.x, ty - camPos.y, tz - camPos.z);
         int crystalCount = 18;
         float pelvisY = entityHeight * 0.35F;
         float torsoY = entityHeight * 0.55F;
         float neckY = entityHeight * 0.74F;
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
         BufferBuilder buf = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
         boolean hasCrystals = false;

         for (int i = 0; i < crystalCount; i++) {
            float s1 = (float)Math.sin((double)((float)i * 1.7F + 0.3F)) * 0.5F + 0.5F;
            float s2 = (float)Math.cos((double)((float)i * 2.3F + 0.7F)) * 0.5F + 0.5F;
            float s3 = (float)Math.sin((double)((float)i * 3.1F + 1.1F)) * 0.5F + 0.5F;
            float angle = time + (float)i * (360.0F / (float)crystalCount) + s1 * 12.0F;
            float radius = halfWidth + 0.25F + s3 * 0.15F;
            float cx = radius * (float)Math.cos(Math.toRadians((double)angle));
            float cz = radius * (float)Math.sin(Math.toRadians((double)angle));
            float cy = s2 * entityHeight;
            float scale = 0.18F * eased;
            if (!(scale < 0.001F)) {
               float lookY = this.getCrystalLookY(cy, entityHeight, pelvisY, torsoY, neckY);
               float dx = -cx;
               float dy = lookY - cy;
               float dz = -cz;
               float yaw = (float)Math.toDegrees(Math.atan2((double)dz, (double)dx));
               float pitch = (float)Math.toDegrees(Math.atan2((double)dy, Math.sqrt((double)(dx * dx + dz * dz))));
               this.drawCrystalShape(
                  buf, matrices, cx, cy, cz, scale, yaw, pitch, (int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F), (int)(200.0F * anim), sharp
               );
               hasCrystals = true;
            }
         }

         if (hasCrystals) {
            BufferRenderer.drawWithGlobalProgram(buf.end());
         }

         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         matrices.pop();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   private float getCrystalLookY(float cy, float h, float pelvis, float torso, float neck) {
      float n = cy / h;
      if (n < 0.33F) {
         return pelvis;
      } else {
         return n < 0.6F ? torso : neck;
      }
   }

   private void drawCrystalShape(
      BufferBuilder buf, MatrixStack ms, float x, float y, float z, float scale, float yaw, float pitch, int r, int g, int b, int a, boolean sharp
   ) {
      ms.push();
      ms.translate(x, y, z);
      ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
      ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
      ms.scale(scale, scale, scale);
      Matrix4f m = ms.peek().getPositionMatrix();
      float w = sharp ? 0.35F : 0.7F;
      float h = sharp ? 1.2F : 1.0F;
      this.tri(buf, m, h, 0.0F, 0.0F, 0.0F, w, 0.0F, 0.0F, 0.0F, w, r, g, b, a);
      this.tri(buf, m, h, 0.0F, 0.0F, 0.0F, 0.0F, w, 0.0F, -w, 0.0F, r, g, b, a);
      this.tri(buf, m, h, 0.0F, 0.0F, 0.0F, -w, 0.0F, 0.0F, 0.0F, -w, r, g, b, a);
      this.tri(buf, m, h, 0.0F, 0.0F, 0.0F, 0.0F, -w, 0.0F, w, 0.0F, r, g, b, a);
      this.tri(buf, m, -h, 0.0F, 0.0F, 0.0F, w, 0.0F, 0.0F, 0.0F, w, r, g, b, a);
      this.tri(buf, m, -h, 0.0F, 0.0F, 0.0F, 0.0F, w, 0.0F, -w, 0.0F, r, g, b, a);
      this.tri(buf, m, -h, 0.0F, 0.0F, 0.0F, -w, 0.0F, 0.0F, 0.0F, -w, r, g, b, a);
      this.tri(buf, m, -h, 0.0F, 0.0F, 0.0F, 0.0F, -w, 0.0F, w, 0.0F, r, g, b, a);
      ms.pop();
   }

   private void tri(
      BufferBuilder buf, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int b, int a
   ) {
      buf.vertex(m, x1, y1, z1).color(r, g, b, a);
      buf.vertex(m, x2, y2, z2).color(r, g, b, a);
      buf.vertex(m, x3, y3, z3).color(r, g, b, a);
   }

   private void renderCubes(MatrixStack matrices, Camera camera, LivingEntity target, float partialTicks) {
      long now = System.currentTimeMillis();
      if (this.lastCubeTime == 0L) {
         this.lastCubeTime = now;
      }

      float dt = Math.min((float)(now - this.lastCubeTime) / 1000.0F, 0.1F);
      this.lastCubeTime = now;
      if (target != null) {
         this.spawnAccumulator += dt;

         while (this.spawnAccumulator >= 0.017F) {
            this.spawnAccumulator -= 0.017F;

            for (int i = 0; i < 1; i++) {
               double rand = Math.random() * 360.0;
               double px = Math.cos(Math.toRadians(rand)) * 0.7;
               double py = 0.04 + Math.random() * 0.16;
               double pz = Math.sin(Math.toRadians(rand)) * 0.7;
               this.particles.add(new TargetESP.Particle(target, px, py, pz));
            }
         }
      }

      float camYaw = camera.getYaw();
      float camPitch = camera.getPitch();
      Iterator<TargetESP.Particle> it = this.particles.iterator();
      ArrayList<TargetESP.Particle> toRender = new ArrayList<>();

      while (it.hasNext()) {
         TargetESP.Particle p = it.next();
         p.update(dt);
         if (now - p.time > 1000L) {
            it.remove();
         } else {
            toRender.add(p);
         }
      }

      Color c = this.color.getValue().getColorObject();

      for (TargetESP.Particle p : toRender) {
         p.renderCube(matrices, camera, c.getRGB(), partialTicks);
      }

      if (!toRender.isEmpty()) {
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         boolean hasBloom = false;

         for (TargetESP.Particle p : toRender) {
            if (p.renderBloom(builder, matrices, camera, c.getRGB(), camYaw, camPitch, partialTicks)) {
               hasBloom = true;
            }
         }

         if (hasBloom) {
            BufferRenderer.drawWithGlobalProgram(builder.end());
         }

         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
      }
   }

   private void renderPredictBox(MatrixStack matrices, Camera camera, LivingEntity target, float partialTicks) {
      double velX = target.getX() - target.lastRenderX;
      double velY = target.getY() - target.lastRenderY;
      double velZ = target.getZ() - target.lastRenderZ;
      double speedSq = velX * velX + velY * velY + velZ * velZ;
      if (!(speedSq < 0.001)) {
         double tx = MathHelper.lerp((double)partialTicks, target.lastRenderX, target.getX());
         double ty = MathHelper.lerp((double)partialTicks, target.lastRenderY, target.getY());
         double tz = MathHelper.lerp((double)partialTicks, target.lastRenderZ, target.getZ());
         double predX = tx + velX * (double)this.predictTicks.getValue().floatValue();
         double predY = ty + velY * (double)this.predictTicks.getValue().floatValue() + (double)target.getHeight() / 2.0;
         double predZ = tz + velZ * (double)this.predictTicks.getValue().floatValue();
         Vec3d camPos = camera.getPos();
         double x = predX - camPos.x;
         double y = predY - camPos.y;
         double z = predZ - camPos.z;
         float camYaw = camera.getYaw();
         float camPitch = camera.getPitch();
         Color c = this.predictColor.getValue().getColorObject();
         float r = (float)c.getRed() / 255.0F;
         float g = (float)c.getGreen() / 255.0F;
         float b = (float)c.getBlue() / 255.0F;
         float a = 0.6F;
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         float size = 0.5F;
         this.putBloomQuad(builder, matrices, x, y, z, size, r, g, b, a, camYaw, camPitch);
         this.putBloomQuad(builder, matrices, x, y, z, size * 0.8F, r, g, b, 1.0F, camYaw, camPitch);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         this.restoreRenderState();
      }
   }

   private void renderGhostRider(MatrixStack matrices, Camera camera, LivingEntity target, float partialTicks) {
      while (this.riderParticles.size() < 3) {
         this.riderParticles
            .add(
               new TargetESP.RiderParticle(
                  new Vec3d(target.getX(), target.getY() + (double)target.getHeight() / 2.0, target.getZ())
               )
            );
      }

      this.riderMovingAngle = (this.riderMovingAngle + 20.0F) % 36000.0F;
      this.riderAnim.run(target.hurtTime > 7 ? 1.0F : 0.0F);
      Vec3d camPos = camera.getPos();
      float camYaw = camera.getYaw();
      float camPitch = camera.getPitch();
      double tx = MathHelper.lerp((double)partialTicks, target.lastRenderX, target.getX());
      double ty = MathHelper.lerp((double)partialTicks, target.lastRenderY, target.getY());
      double tz = MathHelper.lerp((double)partialTicks, target.lastRenderZ, target.getZ());
      Color c = this.color.getValue().getColorObject();
      float r = (float)c.getRed() / 255.0F;
      float g = (float)c.getGreen() / 255.0F;
      float b = (float)c.getBlue() / 255.0F;
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

      for (TargetESP.RiderParticle rider : this.riderParticles) {
         rider.update(target);
         double px = rider.pos.x - camPos.x;
         double py = rider.pos.y - camPos.y;
         double pz = rider.pos.z - camPos.z;
         this.putBloomQuad(builder, matrices, px, py, pz, 0.4F, r, g, b, 0.25F, camYaw, camPitch);
      }

      BufferRenderer.drawWithGlobalProgram(builder.end());
      this.restoreRenderState();
   }

   private void putBloomQuad(
      BufferBuilder builder, MatrixStack matrices, double x, double y, double z, float size, float r, float g, float b, float a, float yaw, float pitch
   ) {
      matrices.push();
      matrices.translate(x, y, z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      float half = size / 2.0F;
      builder.vertex(matrix, -half, -half, 0.0F).color(r, g, b, a);
      builder.vertex(matrix, -half, half, 0.0F).color(r, g, b, a);
      builder.vertex(matrix, half, half, 0.0F).color(r, g, b, a);
      builder.vertex(matrix, half, -half, 0.0F).color(r, g, b, a);
      matrices.pop();
   }

   private void restoreRenderState() {
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
   }

   private static float easeOutCubic(float x) {
      return (float)(1.0 - Math.pow(1.0 - (double)x, 3.0));
   }

   public static enum Mode {
      Ghost,
      GhostV2,
      Crystals,
      Crystals2,
      Cubes,
      Prizraki,
      Kolco,
      GhostRider;
   }

   private static class Particle {
      public long time = System.currentTimeMillis();
      public double x;
      public double y;
      public double z;
      public Entity target;

      public Particle(Entity target, double x, double y, double z) {
         this.target = target;
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public void update(float dt) {
         this.y += (double)dt * 0.2;
      }

      public void renderCube(MatrixStack matrices, Camera camera, int color, float partialTicks) {
      }

      public boolean renderBloom(BufferBuilder builder, MatrixStack matrices, Camera camera, int color, float yaw, float pitch, float partialTicks) {
         return false;
      }
   }

   private static class RiderParticle {
      public Vec3d pos;

      public RiderParticle(Vec3d pos) {
         this.pos = pos;
      }

      public void update(Entity target) {
         this.pos = new Vec3d(target.getX(), target.getY() + (double)target.getHeight() / 2.0, target.getZ());
      }
   }

   private static class SimpleAnimation {
      private final long duration;
      private long startTime;
      private float targetValue;
      private float startValue;

      public SimpleAnimation(long duration) {
         this.duration = duration;
         this.startTime = System.currentTimeMillis();
      }

      public void run(float target) {
         if (this.targetValue != target) {
            this.startValue = this.getValue();
            this.targetValue = target;
            this.startTime = System.currentTimeMillis();
         }
      }

      public float getValue() {
         long elapsed = System.currentTimeMillis() - this.startTime;
         if (elapsed >= this.duration) {
            return this.targetValue;
         } else {
            float t = (float)elapsed / (float)this.duration;
            float eased = TargetESP.easeOutCubic(t);
            return this.startValue + (this.targetValue - this.startValue) * eased;
         }
      }
   }
}
