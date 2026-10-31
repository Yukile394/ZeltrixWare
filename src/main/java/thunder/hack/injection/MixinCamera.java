package thunder.hack.injection;

import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import thunder.hack.core.manager.client.ModuleManager;

@Mixin({Camera.class})
public abstract class MixinCamera {
   @Shadow
   private boolean thirdPerson;

   @Shadow
   protected abstract float clipToSpace(float var1);

   @ModifyArgs(
      method = {"update"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/Camera;moveBy(FFF)V",
         ordinal = 0
      )
   )
   private void modifyCameraDistance(Args args) {
   }

   @Inject(
      method = {"clipToSpace"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onClipToSpace(float f, CallbackInfoReturnable<Float> cir) {
   }

   @Inject(
      method = {"update"},
      at = {@At("TAIL")}
   )
   private void updateHook(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
      if (ModuleManager.freeCam.isEnabled()) {
         this.thirdPerson = true;
      }
   }

   @ModifyArgs(
      method = {"update"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"
      )
   )
   private void setRotationHook(Args args) {
      if (ModuleManager.freeCam.isEnabled()) {
         args.setAll(new Object[]{ModuleManager.freeCam.getFakeYaw(), ModuleManager.freeCam.getFakePitch()});
      }
   }

   @ModifyArgs(
      method = {"update"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"
      )
   )
   private void setPosHook(Args args) {
      if (ModuleManager.freeCam.isEnabled()) {
         args.setAll(new Object[]{ModuleManager.freeCam.getFakeX(), ModuleManager.freeCam.getFakeY(), ModuleManager.freeCam.getFakeZ()});
      }
   }
}
