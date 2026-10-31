package thunder.hack.features.modules.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.concurrent.CopyOnWriteArrayList;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IEntity;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public final class TotemPop extends Module {
   private final Setting<TotemPop.Mode> mode = new Setting<>("Mode", TotemPop.Mode.Textured);
   private final Setting<Boolean> secondLayer = new Setting<>("SecondLayer", true);
   private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(-2013200640));
   private final Setting<Integer> ySpeed = new Setting<>("YSpeed", 3, -10, 10);
   private final Setting<Integer> aSpeed = new Setting<>("AlphaSpeed", 5, 1, 100);
   private final Setting<Float> rotSpeed = new Setting<>("RotationSpeed", 0.25F, 0.0F, 6.0F);
   private final CopyOnWriteArrayList<TotemPop.Person> popList = new CopyOnWriteArrayList<>();

   public TotemPop() {
      super("TotemPop", Module.Category.RENDER);
   }

   @Override
   public void onUpdate() {
      this.popList.forEach(person -> person.update(this.popList));
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      if (this.mode.is(TotemPop.Mode.Simple)) {
         RenderSystem.defaultBlendFunc();
      } else {
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      }

      this.popList.forEach(person -> this.renderEntity(stack, person.player, person.modelPlayer, person.getTexture(), person.getAlpha()));
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   }

   @EventHandler
   private void onTotemPop(@NotNull TotemPopEvent e) {
      if (!e.getEntity().equals(mc.player) && mc.world != null) {
         PlayerEntity entity = new PlayerEntity(
            mc.world,
            BlockPos.ORIGIN,
            e.getEntity().bodyYaw,
            new GameProfile(e.getEntity().getUuid(), e.getEntity().getName().getString())
         ) {
            public boolean isSpectator() {
               return false;
            }

            public boolean isCreative() {
               return false;
            }
         };
         entity.copyPositionAndRotation(e.getEntity());
         entity.bodyYaw = e.getEntity().bodyYaw;
         entity.headYaw = e.getEntity().headYaw;
         entity.handSwingProgress = e.getEntity().handSwingProgress;
         entity.handSwingTicks = e.getEntity().handSwingTicks;
         entity.setSneaking(e.getEntity().isSneaking());
         entity.limbAnimator.setSpeed(e.getEntity().limbAnimator.getSpeed());
         entity.limbAnimator.pos = e.getEntity().limbAnimator.getPos();
         this.popList.add(new TotemPop.Person(entity, ((AbstractClientPlayerEntity)e.getEntity()).getSkinTextures().texture()));
      }
   }

   private void renderEntity(@NotNull MatrixStack matrices, @NotNull LivingEntity entity, @NotNull PlayerEntityModel<PlayerEntity> modelBase, Identifier texture, int alpha) {
      modelBase.leftPants.visible = this.secondLayer.getValue();
      modelBase.rightPants.visible = this.secondLayer.getValue();
      modelBase.leftSleeve.visible = this.secondLayer.getValue();
      modelBase.rightSleeve.visible = this.secondLayer.getValue();
      modelBase.jacket.visible = this.secondLayer.getValue();
      modelBase.hat.visible = this.secondLayer.getValue();
      double x = entity.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
      double y = entity.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
      double z = entity.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
      matrices.push();
      matrices.translate((float)x, (float)y, (float)z);
      float yRotYaw = (float)alpha / 255.0F * 360.0F * this.rotSpeed.getValue();
      yRotYaw = yRotYaw == 0.0F
         ? 0.0F
         : Render2DEngine.interpolateFloat(
            yRotYaw, yRotYaw - (float)this.aSpeed.getValue().intValue() / 255.0F * 360.0F * this.rotSpeed.getValue(), (double)Render3DEngine.getTickDelta()
         );
      matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtility.rad(180.0F - entity.bodyYaw + yRotYaw)));
      prepareScale(matrices);
      modelBase.animateModel((PlayerEntity)entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), Render3DEngine.getTickDelta());
      float limbSpeed = Math.min(entity.limbAnimator.getSpeed(), 1.0F);
      modelBase.setAngles(
         (PlayerEntity)entity,
         entity.limbAnimator.getPos(),
         limbSpeed,
         (float)entity.age,
         entity.headYaw - entity.bodyYaw,
         entity.getPitch()
      );
      BufferBuilder buffer;
      if (this.mode.is(TotemPop.Mode.Textured)) {
         RenderSystem.setShaderTexture(0, texture);
         RenderSystem.setShader(GameRenderer::getPositionTexProgram);
         buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      } else {
         RenderSystem.setShader(GameRenderer::getPositionProgram);
         buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION);
      }

      RenderSystem.setShaderColor(
         this.color.getValue().getGlRed(), this.color.getValue().getGlGreen(), this.color.getValue().getGlBlue(), (float)alpha / 255.0F
      );
      modelBase.render(matrices, buffer, 10, 0);
      Render2DEngine.endBuilding(buffer);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      matrices.pop();
   }

   private static void prepareScale(@NotNull MatrixStack matrixStack) {
      matrixStack.scale(-1.0F, -1.0F, 1.0F);
      matrixStack.scale(1.6F, 1.8F, 1.6F);
      matrixStack.translate(0.0F, -1.501F, 0.0F);
   }

   private static enum Mode {
      Simple,
      Textured;
   }

   private class Person {
      private final PlayerEntity player;
      private final PlayerEntityModel<PlayerEntity> modelPlayer;
      private Identifier texture;
      private int alpha;

      public Person(PlayerEntity player, Identifier texture) {
         this.player = player;
         this.modelPlayer = new PlayerEntityModel(
            new Context(
                  Module.mc.getEntityRenderDispatcher(),
                  Module.mc.getItemRenderer(),
                  Module.mc.getBlockRenderManager(),
                  Module.mc.getEntityRenderDispatcher().getHeldItemRenderer(),
                  Module.mc.getResourceManager(),
                  Module.mc.getEntityModelLoader(),
                  Module.mc.textRenderer
               )
               .getPart(EntityModelLayers.PLAYER),
            false
         );
         this.modelPlayer.getHead().scale(new Vector3f(-0.3F, -0.3F, -0.3F));
         this.alpha = TotemPop.this.color.getValue().getAlpha();
         this.texture = texture;
      }

      public void update(CopyOnWriteArrayList<TotemPop.Person> arrayList) {
         if (this.alpha <= 0) {
            arrayList.remove(this);
            this.player.kill();
            this.player.remove(RemovalReason.KILLED);
            this.player.onRemoved();
         } else {
            this.alpha = this.alpha - TotemPop.this.aSpeed.getValue();
            ((IEntity)this.player).setPos(this.player.getPos().add(0.0, (double)TotemPop.this.ySpeed.getValue().intValue() / 20.0, 0.0));
         }
      }

      public int getAlpha() {
         return MathUtility.clamp(this.alpha, 0, 255);
      }

      public Identifier getTexture() {
         return this.texture;
      }
   }
}
