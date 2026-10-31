package thunder.hack.features.models;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
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

public class YakupTVModel {
   private final ModelPart head;
   public static final Identifier YAKUP_TV_TEXTURE = Identifier.of("thunderhack", "textures/yakuptv.png");

   public YakupTVModel() {
      ModelData modelData = new ModelData();
      ModelPartData root = modelData.getRoot();
      root.addChild(
         "head",
         ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F),
         ModelTransform.pivot(0.0F, 0.0F, 0.0F)
      );
      ModelPart modelRoot = TexturedModelData.of(modelData, 64, 64).createModel();
      this.head = modelRoot.getChild("head");
   }

   public void setAngles(PlayerEntity player, BipedEntityModel<PlayerEntity> baseModel, float tickDelta) {
      boolean reversed = ModuleManager.customModel.changeZ.getValue();
      float headYaw = reversed ? -baseModel.head.yaw : baseModel.head.yaw;
      this.head.pitch = baseModel.head.pitch;
      this.head.yaw = headYaw;
      this.head.roll = baseModel.head.roll;
   }

   public void render(MatrixStack matrices, VertexConsumer vertices, int light) {
      this.head.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
   }
}
