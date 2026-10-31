package thunder.hack.features.modules.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.EntityPose;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventHeldItemRenderer;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class Chams extends Module {
   public final Setting<Boolean> handItems = new Setting<>("HandItems", false);
   private final Setting<ColorSetting> handItemsColor = new Setting<>(
      "HandItemsColor", new ColorSetting(new Color(-1827152291, true)), v -> this.handItems.getValue()
   );
   public final Setting<Boolean> crystals = new Setting<>("Crystals", false);
   private final Setting<ColorSetting> crystalColor = new Setting<>(
      "CrystalColor", new ColorSetting(new Color(-1825711896, true)), v -> this.crystals.getValue()
   );
   private final Setting<Boolean> staticCrystal = new Setting<>("StaticCrystal", true, v -> this.crystals.getValue());
   private final Setting<Chams.CMode> crystalMode = new Setting<>("CrystalMode", Chams.CMode.One, v -> this.crystals.getValue());
   public final Setting<Boolean> players = new Setting<>("Players", false);
   private final Setting<ColorSetting> playerColor = new Setting<>("PlayerColor", new ColorSetting(new Color(-1825711896, true)), v -> this.players.getValue());
   private final Setting<ColorSetting> friendColor = new Setting<>("FriendColor", new ColorSetting(new Color(-1825707984, true)), v -> this.players.getValue());
   private final Setting<Boolean> playerTexture = new Setting<>("PlayerTexture", true, v -> this.players.getValue());
   private final Setting<Boolean> simple = new Setting<>("Simple", false, v -> this.players.getValue());
   private final Setting<Boolean> alternativeBlending = new Setting<>("AlternativeBlending", true);
   private final Identifier crystalTexture = Identifier.of("textures/entity/end_crystal/end_crystal.png");
   private static final float SINE_45_DEGREES = (float)Math.sin(Math.PI / 4);

   public Chams() {
      super("Chams", Module.Category.RENDER);
   }

   public void renderCrystal(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, int i, ModelPart core, ModelPart frame) {
      RenderSystem.enableBlend();
      if (this.alternativeBlending.getValue()) {
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      } else {
         RenderSystem.defaultBlendFunc();
      }

      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      BufferBuilder buffer;
      if (this.crystalMode.getValue() != Chams.CMode.One) {
         if (this.crystalMode.getValue() == Chams.CMode.Three) {
            RenderSystem.setShaderTexture(0, this.crystalTexture);
         } else {
            RenderSystem.setShaderTexture(0, TextureStorage.crystalTexture2);
         }

         RenderSystem.setShader(GameRenderer::getPositionTexProgram);
         buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      } else {
         RenderSystem.setShader(GameRenderer::getPositionProgram);
         buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION);
      }

      matrixStack.push();
      float h = this.staticCrystal.getValue() ? -1.4F : EndCrystalEntityRenderer.getYOffset(endCrystalEntity, g);
      float j = ((float)endCrystalEntity.endCrystalAge + g) * 3.0F;
      matrixStack.push();
      RenderSystem.setShaderColor(
         this.crystalColor.getValue().getGlRed(),
         this.crystalColor.getValue().getGlGreen(),
         this.crystalColor.getValue().getGlBlue(),
         this.crystalColor.getValue().getGlAlpha()
      );
      matrixStack.scale(2.0F, 2.0F, 2.0F);
      matrixStack.translate(0.0F, -0.5F, 0.0F);
      int k = OverlayTexture.DEFAULT_UV;
      matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
      matrixStack.translate(0.0F, 1.5F + h / 2.0F, 0.0F);
      matrixStack.multiply(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
      frame.render(matrixStack, buffer, i, k);
      matrixStack.scale(0.875F, 0.875F, 0.875F);
      matrixStack.multiply(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
      matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
      frame.render(matrixStack, buffer, i, k);
      matrixStack.scale(0.875F, 0.875F, 0.875F);
      matrixStack.multiply(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
      matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
      core.render(matrixStack, buffer, i, k);
      matrixStack.pop();
      matrixStack.pop();
      Render2DEngine.endBuilding(buffer);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
   }

   public void renderPlayer(PlayerEntity pe, float f, float g, MatrixStack matrixStack, int i, EntityModel model, CallbackInfo ci, Runnable post) {
      RenderSystem.enableBlend();
      if (this.alternativeBlending.getValue()) {
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      } else {
         RenderSystem.defaultBlendFunc();
      }

      RenderSystem.enableCull();
      RenderSystem.disableDepthTest();
      BufferBuilder buffer;
      if (!this.simple.getValue()) {
         RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity)pe).getSkinTextures().texture());
         RenderSystem.setShader(GameRenderer::getPositionTexProgram);
         buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      } else {
         RenderSystem.setShader(GameRenderer::getPositionProgram);
         buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION);
      }

      matrixStack.push();
      if (Managers.FRIEND.isFriend(pe)) {
         RenderSystem.setShaderColor(
            this.friendColor.getValue().getGlRed(),
            this.friendColor.getValue().getGlGreen(),
            this.friendColor.getValue().getGlBlue(),
            this.friendColor.getValue().getGlAlpha()
         );
      } else {
         RenderSystem.setShaderColor(
            this.playerColor.getValue().getGlRed(),
            this.playerColor.getValue().getGlGreen(),
            this.playerColor.getValue().getGlBlue(),
            this.playerColor.getValue().getGlAlpha()
         );
      }

      model.handSwingProgress = pe.getHandSwingProgress(g);
      model.riding = pe.hasVehicle();
      model.child = false;
      float h = MathHelper.lerpAngleDegrees(g, pe.prevBodyYaw, pe.bodyYaw);
      float j = MathHelper.lerpAngleDegrees(g, pe.prevHeadYaw, pe.headYaw);
      float k = j - h;
      Entity entity;
      if (pe.hasVehicle() && (entity = pe.getVehicle()) instanceof LivingEntity) {
         LivingEntity livingEntity2 = (LivingEntity)entity;
         h = MathHelper.lerpAngleDegrees(g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
         k = j - h;
         float l = MathHelper.wrapDegrees(k);
         if (l < -85.0F) {
            l = -85.0F;
         }

         if (l >= 85.0F) {
            l = 85.0F;
         }

         h = j - l;
         if (l * l > 2500.0F) {
            h += l * 0.2F;
         }

         k = j - h;
      }

      float m = MathHelper.lerp(g, pe.prevPitch, pe.getPitch());
      if (LivingEntityRenderer.shouldFlipUpsideDown(pe)) {
         m *= -1.0F;
         k *= -1.0F;
      }

      Direction direction;
      if (pe.isInPose(EntityPose.SLEEPING) && (direction = pe.getSleepingDirection()) != null) {
         float n = pe.getEyeHeight(EntityPose.STANDING) - 0.1F;
         matrixStack.translate((float)(-direction.getOffsetX()) * n, 0.0F, (float)(-direction.getOffsetZ()) * n);
      }

      float lx = (float)pe.age + g;
      this.setupTransforms1(pe, matrixStack, lx, h, g);
      matrixStack.scale(-1.0F, -1.0F, 1.0F);
      matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
      matrixStack.translate(0.0F, -1.501F, 0.0F);
      float n = 0.0F;
      float o = 0.0F;
      if (!pe.hasVehicle() && pe.isAlive()) {
         n = pe.limbAnimator.getSpeed(g);
         o = pe.limbAnimator.getPos(g);
         if (pe.isBaby()) {
            o *= 3.0F;
         }

         if (n > 1.0F) {
            n = 1.0F;
         }
      }

      model.animateModel(pe, o, n, g);
      model.setAngles(pe, o, n, lx, k, m);
      int p = LivingEntityRenderer.getOverlay(pe, 0.0F);
      model.render(matrixStack, buffer, i, p);
      Render2DEngine.endBuilding(buffer);
      RenderSystem.disableBlend();
      RenderSystem.disableCull();
      matrixStack.pop();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableDepthTest();
      if (!this.playerTexture.getValue()) {
         ci.cancel();
         post.run();
      }
   }

   public void setupTransforms1(PlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h) {
      float j = abstractClientPlayerEntity.getLeaningPitch(h);
      float k = abstractClientPlayerEntity.getPitch(h);
      if (abstractClientPlayerEntity.isFallFlying()) {
         this.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
         float l = (float)abstractClientPlayerEntity.getFallFlyingTicks() + h;
         float m = MathHelper.clamp(l * l / 100.0F, 0.0F, 1.0F);
         if (!abstractClientPlayerEntity.isUsingRiptide()) {
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m * (-90.0F - k)));
         }

         Vec3d vec3d = abstractClientPlayerEntity.getRotationVec(h);
         Vec3d vec3d2 = abstractClientPlayerEntity.getVelocity();
         double d = vec3d2.horizontalLengthSquared();
         double e = vec3d.horizontalLengthSquared();
         if (d > 0.0 && e > 0.0) {
            double n = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
            double o = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float)(Math.signum(o) * Math.acos(n))));
         }
      } else if (j > 0.0F) {
         this.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
         float lx = abstractClientPlayerEntity.isTouchingWater() ? -90.0F - k : -90.0F;
         float mx = MathHelper.lerp(j, 0.0F, lx);
         matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mx));
         if (abstractClientPlayerEntity.isInSwimmingPose()) {
            matrixStack.translate(0.0F, -1.0F, 0.3F);
         }
      } else {
         this.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
      }
   }

   private void setupTransforms(PlayerEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
      if (!entity.isInPose(EntityPose.SLEEPING)) {
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - bodyYaw));
      }

      if (entity.deathTime > 0) {
         float f = ((float)entity.deathTime + tickDelta - 1.0F) / 20.0F * 1.6F;
         f = MathHelper.sqrt(f);
         if (f > 1.0F) {
            f = 1.0F;
         }

         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 90.0F));
      } else if (entity.isUsingRiptide()) {
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F - entity.getPitch()));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float)entity.age + tickDelta) * -75.0F));
      } else if (entity.isInPose(EntityPose.SLEEPING)) {
         Direction direction = entity.getSleepingDirection();
         float g = direction != null ? getYaw(direction) : bodyYaw;
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0F));
      }
   }

   private static float getYaw(Direction direction) {
      return switch (direction) {
         case NORTH -> 270.0F;
         case SOUTH -> 90.0F;
         case EAST -> 180.0F;
         default -> 0.0F;
      };
   }

   @EventHandler
   public void onRenderHands(EventHeldItemRenderer e) {
      if (this.handItems.getValue()) {
         RenderSystem.setShaderColor(
            (float)this.handItemsColor.getValue().getRed() / 255.0F,
            (float)this.handItemsColor.getValue().getGreen() / 255.0F,
            (float)this.handItemsColor.getValue().getBlue() / 255.0F,
            (float)this.handItemsColor.getValue().getAlpha() / 255.0F
         );
      }
   }

   private static enum CMode {
      One,
      Two,
      Three;
   }
}
