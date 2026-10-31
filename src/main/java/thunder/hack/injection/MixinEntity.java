package thunder.hack.injection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventFixVelocity;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.HitBox;
import thunder.hack.utility.interfaces.IEntity;

@Mixin({Entity.class})
public abstract class MixinEntity implements IEntity {
   @Shadow
   private Box boundingBox;

   @Shadow
   protected abstract BlockPos getVelocityAffectingPos();

   @Override
   public BlockPos thunderHack_Recode$getVelocityBP() {
      return this.getVelocityAffectingPos();
   }

   @ModifyArgs(
      method = {"pushAwayFrom"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"
      )
   )
   public void pushAwayFromHook(Args args) {
   }

   @Inject(
      method = {"updateVelocity"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void updateVelocityHook(float speed, Vec3d movementInput, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         if ((Object)this == Module.mc.player) {
            ci.cancel();
            EventFixVelocity event = new EventFixVelocity(
               movementInput, speed, Module.mc.player.getYaw(), movementInputToVelocityC(movementInput, speed, Module.mc.player.getYaw())
            );
            ThunderHack.EVENT_BUS.post(event);
            Module.mc.player.setVelocity(Module.mc.player.getVelocity().add(event.getVelocity()));
         }
      }
   }

   @Unique
   private static Vec3d movementInputToVelocityC(Vec3d movementInput, float speed, float yaw) {
      double d = movementInput.lengthSquared();
      if (d < 1.0E-7) {
         return Vec3d.ZERO;
      } else {
         Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply((double)speed);
         float f = MathHelper.sin(yaw * (float) (Math.PI / 180.0));
         float g = MathHelper.cos(yaw * (float) (Math.PI / 180.0));
         return new Vec3d(
            vec3d.x * (double)g - vec3d.z * (double)f, vec3d.y, vec3d.z * (double)g + vec3d.x * (double)f
         );
      }
   }

   @Inject(
      method = {"getBoundingBox"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public final void getBoundingBox(CallbackInfoReturnable<Box> cir) {
      if (ModuleManager.hitBox.isEnabled()
         && Module.mc != null
         && Module.mc.player != null
         && ((Entity)(Object)this).getId() != Module.mc.player.getId()
         && (ModuleManager.aura.isDisabled() || HitBox.affectToAura.getValue())) {
         cir.setReturnValue(
            new Box(
               this.boundingBox.minX - (double)(HitBox.XZExpand.getValue() / 2.0F),
               this.boundingBox.minY - (double)(HitBox.YExpand.getValue() / 2.0F),
               this.boundingBox.minZ - (double)(HitBox.XZExpand.getValue() / 2.0F),
               this.boundingBox.maxX + (double)(HitBox.XZExpand.getValue() / 2.0F),
               this.boundingBox.maxY + (double)(HitBox.YExpand.getValue() / 2.0F),
               this.boundingBox.maxZ + (double)(HitBox.XZExpand.getValue() / 2.0F)
            )
         );
      }
   }

   @Inject(
      method = {"isGlowing"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void isGlowingHook(CallbackInfoReturnable<Boolean> cir) {
   }

   @Inject(
      method = {"isOnFire"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void isOnFireHook(CallbackInfoReturnable<Boolean> cir) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.fireEntity.getValue()) {
         cir.setReturnValue(false);
      }
   }

   @Inject(
      method = {"isInvisibleTo"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void isInvisibleToHook(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
   }

   @Inject(
      method = {"isInLava"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void isInLavaHook(CallbackInfoReturnable<Boolean> cir) {
   }

   @Inject(
      method = {"isTouchingWater"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void isTouchingWaterHook(CallbackInfoReturnable<Boolean> cir) {
   }

   @Inject(
      method = {"setSwimming"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void setSwimmingHook(boolean swimming, CallbackInfo ci) {
   }

   @ModifyVariable(
      method = {"changeLookDirection"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private double changeLookDirectionHook0(double value) {
      return value;
   }

   @ModifyVariable(
      method = {"changeLookDirection"},
      at = @At("HEAD"),
      ordinal = 1,
      argsOnly = true
   )
   private double changeLookDirectionHook1(double value) {
      return value;
   }
}
