package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.AnimationUtility;

public class Crosshair extends Module {
   private final Setting<Crosshair.Mode> mode = new Setting<>("Mode", Crosshair.Mode.Circle);
   private final Setting<Boolean> animated = new Setting<>("Animated", true, v -> this.mode.is(Crosshair.Mode.Default));
   private final Setting<Boolean> dot = new Setting<>("Dot", false, v -> this.mode.is(Crosshair.Mode.Default));
   private final Setting<Boolean> t = new Setting<>("T", false, v -> this.mode.is(Crosshair.Mode.Default));
   private final Setting<Crosshair.ColorMode> colorMode = new Setting<>("ColorMode", Crosshair.ColorMode.Sync);
   public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(575714484));
   private final Setting<Boolean> dynamic = new Setting<>("Dynamic", true);
   private final Setting<Float> range = new Setting<>("Range", 30.0F, 0.1F, 120.0F);
   private final Setting<Float> speed = new Setting<>("Speed", 3.0F, 0.1F, 20.0F);
   private final Setting<Float> backSpeed = new Setting<>("BackSpeed", 5.0F, 0.1F, 20.0F);
   private float xAnim;
   private float yAnim;
   private float prevPitch;
   private float prevProgress;

   public Crosshair() {
      super("Crosshair", Module.Category.HUD);
   }

   @Override
   public void onRender2D(DrawContext context) {
      if (mc.options.getPerspective().isFirstPerson()) {
         float midX = (float)mc.getWindow().getScaledWidth() / 2.0F;
         float midY = (float)mc.getWindow().getScaledHeight() / 2.0F;
         float yawDelta = mc.player.prevHeadYaw - mc.player.getHeadYaw();
         float pitchDelta = this.prevPitch - mc.player.getPitch();
         if (yawDelta > 0.0F) {
            this.xAnim = AnimationUtility.fast(this.xAnim, midX - this.range.getValue(), this.speed.getValue());
         } else if (yawDelta < 0.0F) {
            this.xAnim = AnimationUtility.fast(this.xAnim, midX + this.range.getValue(), this.speed.getValue());
         } else {
            this.xAnim = AnimationUtility.fast(this.xAnim, midX, this.backSpeed.getValue());
         }

         if (pitchDelta > 0.0F) {
            this.yAnim = AnimationUtility.fast(this.yAnim, midY - this.range.getValue(), this.speed.getValue());
         } else if (pitchDelta < 0.0F) {
            this.yAnim = AnimationUtility.fast(this.yAnim, midY + this.range.getValue(), this.speed.getValue());
         } else {
            this.yAnim = AnimationUtility.fast(this.yAnim, midY, this.backSpeed.getValue());
         }

         this.prevPitch = mc.player.getPitch();
         if (!this.dynamic.getValue()) {
            this.xAnim = midX;
            this.yAnim = midY;
         }

         float progress = 360.0F * mc.player.getAttackCooldownProgress(0.5F);
         progress = progress == 0.0F ? 360.0F : progress;
         switch ((Crosshair.Mode)this.mode.getValue()) {
            case Circle:
               Color c1 = this.colorMode.getValue() == Crosshair.ColorMode.Sync
                  ? HudEditor.hcolor1.getValue().getColorObject()
                  : this.color.getValue().getColorObject();
               Color c2 = this.colorMode.getValue() == Crosshair.ColorMode.Sync
                  ? HudEditor.acolor.getValue().getColorObject()
                  : this.color.getValue().getColorObject();
               Render2DEngine.drawArc(
                  context.getMatrices(),
                  this.xAnim - 25.0F,
                  this.yAnim - 25.0F,
                  50.0F,
                  50.0F,
                  0.05F,
                  0.12F,
                  0.0F,
                  Render2DEngine.interpolateFloat(this.prevProgress, progress, (double)Render3DEngine.getTickDelta()),
                  c1,
                  c2
               );
               this.prevProgress = progress;
               break;
            case WiseTree: {
               Color color = this.color.getValue().getColorObject();
               context.getMatrices().push();
               context.getMatrices().translate(this.xAnim, this.yAnim, 0.0F);
               context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotation((float)(System.currentTimeMillis() % 70000L) / 70000.0F * 360.0F));
               context.getMatrices().translate(-this.xAnim, -this.yAnim, 0.0F);
               Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 0.75F, this.yAnim - 5.0F, 1.5F, 10.0F, color);
               Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 5.0F, this.yAnim - 0.75F, 10.0F, 1.5F, color);
               Render2DEngine.drawRect(context.getMatrices(), this.xAnim, this.yAnim - 5.0F, 5.0F, 1.5F, color);
               Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 5.0F, this.yAnim + 4.0F, 5.25F, 1.5F, color);
               Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 5.0F, this.yAnim - 5.0F, 1.5F, 4.25F, color);
               Render2DEngine.drawRect(context.getMatrices(), this.xAnim + 3.5F, this.yAnim, 1.5F, 5.5F, color);
               context.getMatrices().pop();
               break;
            }
            case Dot:
               context.getMatrices().push();
               context.getMatrices().translate(this.xAnim + 4.0F, this.yAnim + 4.0F, 0.0F);
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
               RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
               BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
               RenderSystem.setShaderTexture(0, TextureStorage.firefly);
               Color color1 = this.colorMode.getValue() == Crosshair.ColorMode.Sync ? HudEditor.getColor(1) : this.color.getValue().getColorObject();
               Matrix4f posMatrix = context.getMatrices().peek().getPositionMatrix();
               bufferBuilder.vertex(posMatrix, 0.0F, -8.0F, 0.0F).texture(0.0F, 1.0F).color(color1.getRGB());
               bufferBuilder.vertex(posMatrix, -8.0F, -8.0F, 0.0F).texture(1.0F, 1.0F).color(color1.getRGB());
               bufferBuilder.vertex(posMatrix, -8.0F, 0.0F, 0.0F).texture(1.0F, 0.0F).color(color1.getRGB());
               bufferBuilder.vertex(posMatrix, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(color1.getRGB());
               BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableBlend();
               context.getMatrices().pop();
               break;
            case Default: {
               Color color = this.color.getValue().getColorObject();
               float offset = this.animated.getValue()
                  ? -3.0F + Render2DEngine.interpolateFloat(this.prevProgress, progress, (double)Render3DEngine.getTickDelta()) / 100.0F
                  : 0.0F;
               this.prevProgress = progress;
               if (!this.t.getValue()) {
                  Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 1.0F, this.yAnim - 6.0F + offset, 2.0F, 4.0F, Color.BLACK);
                  Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 0.5F, this.yAnim - 5.5F + offset, 1.0F, 3.0F, color);
               }

               Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 1.0F, this.yAnim + 2.0F - offset, 2.0F, 4.0F, Color.BLACK);
               Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 0.5F, this.yAnim + 2.5F - offset, 1.0F, 3.0F, color);
               Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 6.0F + offset, this.yAnim - 1.0F, 4.0F, 2.0F, Color.BLACK);
               Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 5.5F + offset, this.yAnim - 0.5F, 3.0F, 1.0F, color);
               Render2DEngine.drawRect(context.getMatrices(), this.xAnim + 2.0F - offset, this.yAnim - 1.0F, 4.0F, 2.0F, Color.BLACK);
               Render2DEngine.drawRect(context.getMatrices(), this.xAnim + 2.5F - offset, this.yAnim - 0.5F, 3.0F, 1.0F, color);
               if (this.dot.getValue()) {
                  Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 1.0F, this.yAnim - 1.0F, 2.0F, 2.0F, Color.BLACK);
                  Render2DEngine.drawRect(context.getMatrices(), this.xAnim - 0.5F, this.yAnim - 0.5F, 1.0F, 1.0F, color);
               }
            }
         }
      }
   }

   public float getAnimatedPosX() {
      return this.xAnim == 0.0F ? (float)mc.getWindow().getScaledWidth() / 2.0F : this.xAnim;
   }

   public float getAnimatedPosY() {
      return this.yAnim == 0.0F ? (float)mc.getWindow().getScaledHeight() / 2.0F : this.yAnim;
   }

   private static enum ColorMode {
      Custom,
      Sync;
   }

   private static enum Mode {
      Circle,
      WiseTree,
      Dot,
      Default;
   }
}
