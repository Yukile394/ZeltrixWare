package thunder.hack.features.modules.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class JumpCircle extends Module {
   private final Setting<JumpCircle.Mode> mode = new Setting<>("Mode", JumpCircle.Mode.Default);
   private final Setting<Boolean> easeOut = new Setting<>("EaseOut", true);
   private final Setting<Float> rotateSpeed = new Setting<>("RotateSpeed", 2.0F, 0.5F, 5.0F);
   private final Setting<Float> circleScale = new Setting<>("CircleScale", 1.0F, 0.5F, 5.0F);
   private final Setting<Boolean> onlySelf = new Setting<>("OnlySelf", false);
   private final List<JumpCircle.Circle> circles = new ArrayList<>();
   private final List<PlayerEntity> cache = new CopyOnWriteArrayList<>();
   private Identifier custom;

   public JumpCircle() {
      super("JumpCircle", Module.Category.RENDER);
   }

   @Override
   public void onEnable() {
      try {
         this.custom = ThunderUtility.getCustomImg("circle");
      } catch (Exception var2) {
         this.sendMessage(var2.getMessage());
      }
   }

   @Override
   public void onUpdate() {
      if (this.mode.is(JumpCircle.Mode.Custom) && this.custom == null) {
         try {
            this.custom = ThunderUtility.getCustomImg("circle");
         } catch (Exception var3) {
            this.sendMessage(".minecraft -> ThunderHackRecode -> misc -> images -> circle.png");
         }
      }

      for (PlayerEntity pl : mc.world.getPlayers()) {
         if (!this.cache.contains(pl) && pl.isOnGround() && (mc.player == pl || !this.onlySelf.getValue())) {
            this.cache.add(pl);
         }
      }

      this.cache
         .forEach(
            plx -> {
               if (plx != null && !plx.isOnGround()) {
                  this.circles
                     .add(
                        new JumpCircle.Circle(
                           new Vec3d(plx.getX(), (double)((float)((int)Math.floor(plx.getY())) + 0.001F), plx.getZ()), new Timer()
                        )
                     );
                  this.cache.remove(plx);
               }
            }
         );
      this.circles.removeIf(c -> c.timer.passedMs(this.easeOut.getValue() ? 5000L : 6000L));
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      Collections.reverse(this.circles);
      RenderSystem.disableDepthTest();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      switch ((JumpCircle.Mode)this.mode.getValue()) {
         case Default:
            RenderSystem.setShaderTexture(0, TextureStorage.default_circle);
            break;
         case Portal:
            RenderSystem.setShaderTexture(0, TextureStorage.bubble);
            break;
         case Custom:
            RenderSystem.setShaderTexture(0, Objects.requireNonNullElse(this.custom, TextureStorage.default_circle));
      }

      RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

      for (JumpCircle.Circle c : this.circles) {
         float colorAnim = (float)c.timer.getPassedTimeMs() / 6000.0F;
         float sizeAnim = this.circleScale.getValue()
            - (float)Math.pow((double)(1.0F - (float)c.timer.getPassedTimeMs() * (this.easeOut.getValue() ? 2.0F : 1.0F) / 5000.0F), 4.0);
         stack.push();
         stack.translate(
            c.pos().x - mc.getEntityRenderDispatcher().camera.getPos().getX(),
            c.pos().y - mc.getEntityRenderDispatcher().camera.getPos().getY(),
            c.pos().z - mc.getEntityRenderDispatcher().camera.getPos().getZ()
         );
         stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
         stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sizeAnim * this.rotateSpeed.getValue() * 1000.0F));
         float scale = sizeAnim * 2.0F;
         Matrix4f matrix = stack.peek().getPositionMatrix();
         buffer.vertex(matrix, -sizeAnim, -sizeAnim + scale, 0.0F)
            .texture(0.0F, 1.0F)
            .color(Render2DEngine.applyOpacity(HudEditor.getColor(270), 1.0F - colorAnim).getRGB());
         buffer.vertex(matrix, -sizeAnim + scale, -sizeAnim + scale, 0.0F)
            .texture(1.0F, 1.0F)
            .color(Render2DEngine.applyOpacity(HudEditor.getColor(0), 1.0F - colorAnim).getRGB());
         buffer.vertex(matrix, -sizeAnim + scale, -sizeAnim, 0.0F)
            .texture(1.0F, 0.0F)
            .color(Render2DEngine.applyOpacity(HudEditor.getColor(180), 1.0F - colorAnim).getRGB());
         buffer.vertex(matrix, -sizeAnim, -sizeAnim, 0.0F)
            .texture(0.0F, 0.0F)
            .color(Render2DEngine.applyOpacity(HudEditor.getColor(90), 1.0F - colorAnim).getRGB());
         stack.pop();
      }

      Render2DEngine.endBuilding(buffer);
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableDepthTest();
      Collections.reverse(this.circles);
   }

   public static record Circle(Vec3d pos, Timer timer) {
   }

   public static enum Mode {
      Default,
      Portal,
      Custom;
   }
}
