package thunder.hack.features.modules.movement;

import net.minecraft.util.math.Vec3d;
import net.minecraft.client.network.ClientPlayerEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class ElytraBooster extends Module {
   private static ElytraBooster INSTANCE = new ElytraBooster();
   public final Setting<Float> boost = new Setting<>("Boost", 1.0F, 0.0F, 2.0F);
   private Vec3d capturedVelocity = null;

   public ElytraBooster() {
      super("ElytraBooster", Module.Category.MOVEMENT);
      INSTANCE = this;
   }

   public static ElytraBooster getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ElytraBooster();
      }

      return INSTANCE;
   }

   public void captureVelocity(ClientPlayerEntity player) {
      if (this.isEnabled()) {
         this.capturedVelocity = player.getVelocity();
      }
   }

   public void amplifyBoost(ClientPlayerEntity player) {
      if (this.isEnabled()) {
         if (this.capturedVelocity != null) {
            float b = this.boost.getValue();
            if (b <= 0.01F) {
               player.setVelocity(this.capturedVelocity);
               this.capturedVelocity = null;
            } else {
               Vec3d velAfter = player.getVelocity();
               Vec3d delta = velAfter.subtract(this.capturedVelocity);
               player.setVelocity(
                  this.capturedVelocity.x + delta.x * (double)b,
                  this.capturedVelocity.y + delta.y * (double)b,
                  this.capturedVelocity.z + delta.z * (double)b
               );
               this.capturedVelocity = null;
            }
         }
      }
   }

   @Override
   public void onUpdate() {
      if (mc.player != null && mc.player.isFallFlying()) {
         float b = this.boost.getValue();
         if (!(Math.abs(b - 1.0F) < 0.01F)) {
            Vec3d vel = mc.player.getVelocity();
            double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            if (!(hSpeed < 0.05)) {
               double multiplier;
               if (b > 1.0F) {
                  double factor = (double)(b - 1.0F);
                  multiplier = 1.0 + factor * 0.005;
               } else {
                  double factor = (double)(1.0F - b);
                  multiplier = 1.0 - factor * 0.06;
               }

               mc.player.setVelocity(vel.x * multiplier, vel.y, vel.z * multiplier);
            }
         }
      }
   }
}
