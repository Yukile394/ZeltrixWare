package thunder.hack.features.models;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.model.ModelPart;
import thunder.hack.core.manager.client.ModuleManager;

public class AmogusModel {
   private final ModelPart body;
   private final ModelPart eye;
   private final ModelPart leftLeg;
   private final ModelPart rightLeg;
   private float limbSwing;
   private float limbSwingAmount;

   public AmogusModel() {
      ModelData modelData = new ModelData();
      ModelPartData root = modelData.getRoot();
      root.addChild(
         "body",
         ModelPartBuilder.create()
            .uv(34, 8)
            .cuboid(-4.0F, 6.0F, -3.0F, 8.0F, 12.0F, 6.0F)
            .uv(15, 10)
            .cuboid(-3.0F, 9.0F, 3.0F, 6.0F, 8.0F, 3.0F)
            .uv(26, 0)
            .cuboid(-3.0F, 5.0F, -3.0F, 6.0F, 1.0F, 6.0F),
         ModelTransform.pivot(0.0F, 0.0F, 0.0F)
      );
      root.addChild(
         "eye", ModelPartBuilder.create().uv(0, 10).cuboid(-3.0F, 7.0F, -4.0F, 6.0F, 4.0F, 1.0F), ModelTransform.pivot(0.0F, 0.0F, 0.0F)
      );
      root.addChild(
         "leftLeg", ModelPartBuilder.create().uv(0, 0).cuboid(2.9F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), ModelTransform.pivot(-2.0F, 18.0F, 0.0F)
      );
      root.addChild(
         "rightLeg",
         ModelPartBuilder.create().uv(13, 0).cuboid(-5.9F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
         ModelTransform.pivot(2.0F, 18.0F, 0.0F)
      );
      ModelPart modelRoot = TexturedModelData.of(modelData, 64, 64).createModel();
      this.body = modelRoot.getChild("body");
      this.eye = modelRoot.getChild("eye");
      this.leftLeg = modelRoot.getChild("leftLeg");
      this.rightLeg = modelRoot.getChild("rightLeg");
   }

   public void setAngles(PlayerEntity player, BipedEntityModel<PlayerEntity> baseModel, float tickDelta) {
      boolean reversed = ModuleManager.customModel.changeZ.getValue();
      this.limbSwing = player.limbAnimator.getPos(tickDelta);
      this.limbSwingAmount = player.limbAnimator.getSpeed(tickDelta);
      float legSwing = (float)Math.cos((double)(this.limbSwing * 0.6662F)) * 1.4F * this.limbSwingAmount;
      this.leftLeg.pitch = legSwing;
      this.rightLeg.pitch = -legSwing;
      float headYaw = reversed ? -baseModel.head.yaw : baseModel.head.yaw;
      float headPitch = baseModel.head.pitch;
      this.body.yaw = headYaw * 0.5F;
      this.eye.yaw = headYaw * 0.5F;
      this.body.pitch = headPitch * 0.5F;
      this.eye.pitch = headPitch * 0.5F;
   }

   public void renderBody(MatrixStack matrices, VertexConsumer vertices, int light) {
      this.body.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
   }

   public void renderEye(MatrixStack matrices, VertexConsumer vertices, int light) {
      this.eye.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
   }

   public void renderLegs(MatrixStack matrices, VertexConsumer vertices, int light) {
      this.leftLeg.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
      this.rightLeg.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
   }
}
