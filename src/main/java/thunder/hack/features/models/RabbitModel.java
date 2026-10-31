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

public class RabbitModel {
   private final ModelPart root;
   private final ModelPart rabbitBone;
   private final ModelPart rabbitHead;
   private final ModelPart rabbitLarm;
   private final ModelPart rabbitRarm;
   private final ModelPart rabbitLleg;
   private final ModelPart rabbitRleg;

   public RabbitModel() {
      ModelData modelData = new ModelData();
      ModelPartData rootData = modelData.getRoot();
      ModelPartData boneData = rootData.addChild(
         "rabbitBone",
         ModelPartBuilder.create().uv(28, 45).cuboid(-5.0F, -13.0F, -5.0F, 10.0F, 11.0F, 8.0F),
         ModelTransform.pivot(0.0F, 24.0F, 0.0F)
      );
      boneData.addChild(
         "rabbitHead",
         ModelPartBuilder.create()
            .uv(0, 0)
            .cuboid(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 6.0F)
            .uv(56, 0)
            .cuboid(-5.0F, -9.0F, -5.0F, 2.0F, 3.0F, 2.0F)
            .uv(56, 0)
            .cuboid(3.0F, -9.0F, -5.0F, 2.0F, 3.0F, 2.0F)
            .uv(0, 45)
            .cuboid(-4.0F, -11.0F, -4.0F, 8.0F, 11.0F, 8.0F)
            .uv(46, 0)
            .cuboid(1.0F, -20.0F, 0.0F, 3.0F, 9.0F, 1.0F)
            .uv(46, 0)
            .cuboid(-4.0F, -20.0F, 0.0F, 3.0F, 9.0F, 1.0F),
         ModelTransform.pivot(0.0F, -14.0F, -1.0F)
      );
      boneData.addChild(
         "rabbitLarm",
         ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, 0.0F, -2.0F, 2.0F, 8.0F, 4.0F),
         ModelTransform.pivot(5.0F, -13.0F, -1.0F)
      );
      boneData.addChild(
         "rabbitRarm",
         ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, 0.0F, -2.0F, 2.0F, 8.0F, 4.0F),
         ModelTransform.pivot(-5.0F, -13.0F, -1.0F)
      );
      boneData.addChild(
         "rabbitLleg",
         ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F),
         ModelTransform.pivot(3.0F, -2.0F, -1.0F)
      );
      boneData.addChild(
         "rabbitRleg",
         ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F),
         ModelTransform.pivot(-3.0F, -2.0F, -1.0F)
      );
      ModelPart modelRoot = TexturedModelData.of(modelData, 64, 64).createModel();
      this.root = modelRoot;
      this.rabbitBone = modelRoot.getChild("rabbitBone");
      this.rabbitHead = this.rabbitBone.getChild("rabbitHead");
      this.rabbitLarm = this.rabbitBone.getChild("rabbitLarm");
      this.rabbitRarm = this.rabbitBone.getChild("rabbitRarm");
      this.rabbitLleg = this.rabbitBone.getChild("rabbitLleg");
      this.rabbitRleg = this.rabbitBone.getChild("rabbitRleg");
   }

   public void setAngles(PlayerEntity player, BipedEntityModel<PlayerEntity> baseModel, float tickDelta) {
      boolean reversed = ModuleManager.customModel.changeZ.getValue();
      this.rabbitHead.pitch = baseModel.head.pitch;
      this.rabbitHead.yaw = reversed ? -baseModel.head.yaw : baseModel.head.yaw;
      this.rabbitHead.roll = baseModel.head.roll;
      this.rabbitLarm.pitch = baseModel.leftArm.pitch;
      this.rabbitLarm.yaw = reversed ? -baseModel.leftArm.yaw : baseModel.leftArm.yaw;
      this.rabbitLarm.roll = baseModel.leftArm.roll;
      this.rabbitRarm.pitch = baseModel.rightArm.pitch;
      this.rabbitRarm.yaw = reversed ? -baseModel.rightArm.yaw : baseModel.rightArm.yaw;
      this.rabbitRarm.roll = baseModel.rightArm.roll;
      this.rabbitLleg.pitch = baseModel.leftLeg.pitch;
      this.rabbitLleg.yaw = reversed ? -baseModel.leftLeg.yaw : baseModel.leftLeg.yaw;
      this.rabbitLleg.roll = baseModel.leftLeg.roll;
      this.rabbitRleg.pitch = baseModel.rightLeg.pitch;
      this.rabbitRleg.yaw = reversed ? -baseModel.rightLeg.yaw : baseModel.rightLeg.yaw;
      this.rabbitRleg.roll = baseModel.rightLeg.roll;
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light) {
      this.root.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
   }
}
