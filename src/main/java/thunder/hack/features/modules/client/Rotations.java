package thunder.hack.features.modules.client;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventFixVelocity;
import thunder.hack.events.impl.EventKeyboardInput;
import thunder.hack.events.impl.EventPlayerJump;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.setting.Setting;

public class Rotations extends Module {
   private final Setting<Rotations.MoveFix> moveFix = new Setting<>("MoveFix", Rotations.MoveFix.Off);
   public final Setting<Boolean> clientLook = new Setting<>("ClientLook", false);
   public float fixRotation;
   private float jumpPrevYaw;
   private float travelPrevYaw;
   private float travelPrevPitch;

   public Rotations() {
      super("Rotations", Module.Category.CLIENT);
   }

   public void onJump(EventPlayerJump e) {
      if (!Float.isNaN(this.fixRotation) && this.moveFix.getValue() != Rotations.MoveFix.Off && !mc.player.isRiding()) {
         if (e.isPre()) {
            this.jumpPrevYaw = mc.player.getYaw();
            mc.player.setYaw(this.fixRotation);
         } else {
            mc.player.setYaw(this.jumpPrevYaw);
         }
      }
   }

   public void onPlayerMove(EventFixVelocity event) {
      if (this.moveFix.getValue() == Rotations.MoveFix.Free) {
         if (Float.isNaN(this.fixRotation) || mc.player.isRiding()) {
            return;
         }

         event.setVelocity(this.fix(this.fixRotation, event.getMovementInput(), event.getSpeed()));
      }
   }

   public void modifyVelocity(EventPlayerTravel e) {
      if (ModuleManager.aura.isEnabled()
         && Aura.target != null
         && ModuleManager.aura.rotationMode.not(Aura.Mode.None)
         && ModuleManager.aura.elytraTarget.getValue()
         && !Float.isNaN(this.fixRotation)
         && Managers.PLAYER.ticksElytraFlying > 5) {
         if (e.isPre()) {
            this.travelPrevYaw = mc.player.getYaw();
            this.travelPrevPitch = mc.player.getPitch();
            mc.player.setYaw(this.fixRotation);
            mc.player.setPitch(ModuleManager.aura.rotationPitch);
         } else {
            mc.player.setYaw(this.travelPrevYaw);
            mc.player.setPitch(this.travelPrevPitch);
         }
      } else {
         if (this.moveFix.getValue() == Rotations.MoveFix.Focused && !Float.isNaN(this.fixRotation) && !mc.player.isRiding()) {
            if (e.isPre()) {
               this.travelPrevYaw = mc.player.getYaw();
               mc.player.setYaw(this.fixRotation);
            } else {
               mc.player.setYaw(this.travelPrevYaw);
            }
         }
      }
   }

   public void onKeyInput(EventKeyboardInput e) {
      if (this.moveFix.getValue() == Rotations.MoveFix.Free) {
         if (Float.isNaN(this.fixRotation) || mc.player.isRiding()) {
            return;
         }

         float mF = mc.player.input.movementForward;
         float mS = mc.player.input.movementSideways;
         float delta = (mc.player.getYaw() - this.fixRotation) * (float) (Math.PI / 180.0);
         float cos = MathHelper.cos(delta);
         float sin = MathHelper.sin(delta);
         mc.player.input.movementSideways = (float)Math.round(mS * cos - mF * sin);
         mc.player.input.movementForward = (float)Math.round(mF * cos + mS * sin);
      }
   }

   private Vec3d fix(float yaw, Vec3d movementInput, float speed) {
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

   @Override
   public boolean isToggleable() {
      return false;
   }

   private static enum MoveFix {
      Off,
      Focused,
      Free;
   }
}
