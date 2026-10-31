package thunder.hack.features.models;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.movement.CustomModel;

public class CustomModelRenderer {
   private final AmogusModel amogusModel = new AmogusModel();
   private final RabbitModel rabbitModel = new RabbitModel();
   private final YakupTVModel yakupTVModel = new YakupTVModel();

   public void render(
      PlayerEntity player,
      float yaw,
      float tickDelta,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      CustomModel.Mode mode,
      BipedEntityModel<PlayerEntity> baseModel
   ) {
      switch (mode) {
         case Amogus:
            this.renderAmogus(player, yaw, tickDelta, matrices, vertexConsumers, light, baseModel);
            break;
         case Rabbit:
            this.renderRabbit(player, yaw, tickDelta, matrices, vertexConsumers, light, baseModel);
            break;
         case YakupTV:
            this.renderYakupTV(player, yaw, tickDelta, matrices, vertexConsumers, light, baseModel);
            break;
         case Tavsan:
            this.renderRabbit(player, yaw, tickDelta, matrices, vertexConsumers, light, baseModel);
            break;
         case Boykisser:
            this.renderRabbit(player, yaw, tickDelta, matrices, vertexConsumers, light, baseModel);
            break;
         case Paimon:
            this.renderYakupTV(player, yaw, tickDelta, matrices, vertexConsumers, light, baseModel);
            break;
         case Cutie:
            this.renderYakupTV(player, yaw, tickDelta, matrices, vertexConsumers, light, baseModel);
      }
   }

   private void renderAmogus(
      PlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityModel<PlayerEntity> baseModel
   ) {
      matrices.push();
      if (player.isSneaking()) {
         matrices.scale(0.5F, 0.5F, 0.5F);
         matrices.translate(0.0F, 1.5F, 0.0F);
      }

      Color bodyColor = ModuleManager.customModel.bodyColor.getValue().getColorObject();
      Color eyeColor = ModuleManager.customModel.eyeColor.getValue().getColorObject();
      Color legsColor = ModuleManager.customModel.legsColor.getValue().getColorObject();
      if (ModuleManager.customModel.friendHighlight.getValue() && Managers.FRIEND.isFriend(player.getName().getString())) {
         bodyColor = Color.GREEN;
         eyeColor = Color.WHITE;
         legsColor = Color.GREEN;
      }

      this.amogusModel.setAngles(player, baseModel, tickDelta);
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntitySmoothCutout(Identifier.of("thunderhack", "textures/amogus.png")));
      RenderSystem.setShaderColor(
         (float)bodyColor.getRed() / 255.0F, (float)bodyColor.getGreen() / 255.0F, (float)bodyColor.getBlue() / 255.0F, (float)bodyColor.getAlpha() / 255.0F
      );
      this.amogusModel.renderBody(matrices, vertexConsumer, light);
      RenderSystem.setShaderColor(
         (float)eyeColor.getRed() / 255.0F, (float)eyeColor.getGreen() / 255.0F, (float)eyeColor.getBlue() / 255.0F, (float)eyeColor.getAlpha() / 255.0F
      );
      this.amogusModel.renderEye(matrices, vertexConsumer, light);
      RenderSystem.setShaderColor(
         (float)legsColor.getRed() / 255.0F, (float)legsColor.getGreen() / 255.0F, (float)legsColor.getBlue() / 255.0F, (float)legsColor.getAlpha() / 255.0F
      );
      this.amogusModel.renderLegs(matrices, vertexConsumer, light);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      matrices.pop();
   }

   private void renderRabbit(
      PlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityModel<PlayerEntity> baseModel
   ) {
      matrices.push();
      if (player.isSneaking()) {
         matrices.scale(0.5F, 0.5F, 0.5F);
         matrices.translate(0.0F, 1.5F, 0.0F);
      }

      this.rabbitModel.setAngles(player, baseModel, tickDelta);
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntitySmoothCutout(Identifier.of("thunderhack", "textures/rabbit.png")));
      this.rabbitModel.render(matrices, vertexConsumer, light);
      matrices.pop();
   }

   private void renderYakupTV(
      PlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityModel<PlayerEntity> baseModel
   ) {
      matrices.push();
      if (player.isSneaking()) {
         matrices.scale(0.5F, 0.5F, 0.5F);
         matrices.translate(0.0F, 1.5F, 0.0F);
      }

      this.yakupTVModel.setAngles(player, baseModel, tickDelta);
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntitySmoothCutout(YakupTVModel.YAKUP_TV_TEXTURE));
      this.yakupTVModel.render(matrices, vertexConsumer, light);
      matrices.pop();
   }
}
