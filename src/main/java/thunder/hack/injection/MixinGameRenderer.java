package thunder.hack.injection;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.option.Perspective;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.render.BlockAnimationUtility;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.shaders.satin.impl.ReloadableShaderEffectManager;

@Mixin({GameRenderer.class})
public abstract class MixinGameRenderer {
   @Shadow
   private float zoom;
   @Shadow
   private float zoomX;
   @Shadow
   private float zoomY;
   @Shadow
   private float viewDistance;

   @Shadow
   public abstract void tick();

   @Inject(
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/profiler/Profiler;pop()V",
         ordinal = 1,
         shift = Shift.BEFORE
      )},
      method = {"render"}
   )
   void postHudRenderHook(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      FrameRateCounter.INSTANCE.recordFrame();
   }

   @Inject(
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z",
         opcode = 180,
         ordinal = 0
      )},
      method = {"renderWorld"}
   )
   void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         Camera camera = Module.mc.gameRenderer.getCamera();
         MatrixStack matrixStack = new MatrixStack();
         RenderSystem.getModelViewStack().pushMatrix().mul(matrixStack.peek().getPositionMatrix());
         matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
         matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
         RenderSystem.applyModelViewMatrix();
         Render3DEngine.lastProjMat.set(RenderSystem.getProjectionMatrix());
         Render3DEngine.lastModMat.set(RenderSystem.getModelViewMatrix());
         Render3DEngine.lastWorldSpaceMatrix.set(matrixStack.peek().getPositionMatrix());
         Managers.MODULE.onRender3D(matrixStack);
         BlockAnimationUtility.onRender(matrixStack);
         Render3DEngine.onRender3D(matrixStack);
         RenderSystem.getModelViewStack().popMatrix();
         RenderSystem.applyModelViewMatrix();
      }
   }

   @Inject(
      method = {"renderWorld"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V",
         shift = Shift.AFTER
      )}
   )
   public void postRender3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         Managers.SHADER.renderShaders();
      }
   }

   @Redirect(
      method = {"renderWorld"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"
      )
   )
   private float renderWorldHook(float delta, float first, float second) {
      return ModuleManager.noRender.isEnabled() && ModuleManager.noRender.nausea.getValue() ? 0.0F : MathHelper.lerp(delta, first, second);
   }

   @Inject(
      method = {"loadPrograms"},
      at = {@At("RETURN")}
   )
   private void loadSatinPrograms(ResourceFactory factory, CallbackInfo ci) {
      ReloadableShaderEffectManager.INSTANCE.reload(factory);
   }

   @Inject(
      method = {"updateCrosshairTarget"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/GameRenderer;findCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;"
      )},
      cancellable = true
   )
   private void onUpdateTargetedEntity(float tickDelta, CallbackInfo info) {
      if (!Module.fullNullCheck()) {
         if (ModuleManager.freeCam.isEnabled()) {
            Module.mc.getProfiler().pop();
            info.cancel();
            Module.mc.crosshairTarget = Managers.PLAYER
               .getRtxTarget(
                  ModuleManager.freeCam.getFakeYaw(),
                  ModuleManager.freeCam.getFakePitch(),
                  ModuleManager.freeCam.getFakeX(),
                  ModuleManager.freeCam.getFakeY(),
                  ModuleManager.freeCam.getFakeZ()
               );
         }
      }
   }

   @Inject(
      method = {"findCrosshairTarget"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void findCrosshairTargetHook(
      Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta, CallbackInfoReturnable<HitResult> cir
   ) {
   }

   @Inject(
      method = {"getBasicProjectionMatrix"},
      at = {@At("TAIL")},
      cancellable = true
   )
   public void getBasicProjectionMatrixHook(double fov, CallbackInfoReturnable<Matrix4f> cir) {
      if (ModuleManager.aspectRatio.isEnabled()) {
         MatrixStack matrixStack = new MatrixStack();
         matrixStack.peek().getPositionMatrix().identity();
         if (this.zoom != 1.0F) {
            matrixStack.translate(this.zoomX, -this.zoomY, 0.0F);
            matrixStack.scale(this.zoom, this.zoom, 1.0F);
         }

         matrixStack.peek()
            .getPositionMatrix()
            .mul(
               new Matrix4f()
                  .setPerspective((float)(fov * (float) (Math.PI / 180.0)), ModuleManager.aspectRatio.ratio.getValue(), 0.05F, this.viewDistance * 4.0F)
            );
         cir.setReturnValue(matrixStack.peek().getPositionMatrix());
      }
   }

   @Inject(
      method = {"getFov(Lnet/minecraft/client/render/Camera;FZ)D"},
      at = {@At("TAIL")},
      cancellable = true
   )
   public void getFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cb) {
      if (ModuleManager.fov.isEnabled()) {
         if ((Double)cb.getReturnValue() == 70.0 && !ModuleManager.fov.itemFov.getValue() && Module.mc.options.getPerspective() != Perspective.FIRST_PERSON) {
            return;
         }

         if (ModuleManager.fov.itemFov.getValue() && (Double)cb.getReturnValue() == 70.0) {
            cb.setReturnValue(ModuleManager.fov.itemFovModifier.getValue().doubleValue());
            return;
         }

         if (Module.mc.player.isSubmergedInWater()) {
            return;
         }

         cb.setReturnValue(ModuleManager.fov.fovModifier.getValue().doubleValue());
      }
   }

   @Inject(
      method = {"bobView"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void bobViewHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         if (ClientSettings.customBob.getValue()) {
            ThunderHack.core.bobView(matrices, tickDelta);
            ci.cancel();
         }
      }
   }

   @Unique
   private HitResult ensureTargetInRangeCustom(HitResult hitResult, Vec3d cameraPos, double interactionRange) {
      Vec3d vec3d = hitResult.getPos();
      if (!vec3d.isInRange(cameraPos, interactionRange)) {
         Vec3d vec3d2 = hitResult.getPos();
         Direction direction = Direction.getFacing(
            vec3d2.x - cameraPos.x, vec3d2.y - cameraPos.y, vec3d2.z - cameraPos.z
         );
         return BlockHitResult.createMissed(vec3d2, direction, BlockPos.ofFloored(vec3d2));
      } else {
         return hitResult;
      }
   }

   @Inject(
      method = {"showFloatingItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void showFloatingItemHook(ItemStack floatingItem, CallbackInfo info) {
      if (ModuleManager.totemAnimation.isEnabled()) {
         ModuleManager.totemAnimation.showFloatingItem(floatingItem);
         info.cancel();
      }
   }

   @Inject(
      method = {"renderFloatingItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderFloatingItemHook(DrawContext context, float tickDelta, CallbackInfo ci) {
      if (ModuleManager.totemAnimation.isEnabled()) {
         ModuleManager.totemAnimation.renderFloatingItem(tickDelta);
         ci.cancel();
      }
   }

   @Inject(
      method = {"tiltViewWhenHurt"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void tiltViewWhenHurtHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.hurtCam.getValue()) {
         ci.cancel();
      }
   }
}
