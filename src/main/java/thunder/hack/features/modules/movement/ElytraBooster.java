package thunder.hack.features.modules.movement;

import net.minecraft.util.math.Vec3d;
import net.minecraft.client.network.ClientPlayerEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

/**
 * Only changes the <i>magnitude</i> of the horizontal speed. The heading is owned by Aura / ElytraTarget, so the
 * booster never rotates the velocity and never boosts while the heading is changing quickly.
 */
public class ElytraBooster extends Module {
   private static ElytraBooster INSTANCE = new ElytraBooster();
   private static final double STABLE_COS = Math.cos(Math.toRadians(20.0));
   public final Setting<Float> boost = new Setting<>("Boost", 1.0F, 0.0F, 2.0F);
   public final Setting<Float> maxSpeed = new Setting<>("Max Speed", 3.5F, 1.0F, 8.0F);
   private Vec3d capturedVelocity = null;
   private Vec3d lastHeading = null;

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

   @Override
   public void onDisable() {
      super.onDisable();
      this.capturedVelocity = null;
      this.lastHeading = null;
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
               double nx = this.capturedVelocity.x + delta.x * (double)b;
               double nz = this.capturedVelocity.z + delta.z * (double)b;
               double hSpeed = Math.sqrt(nx * nx + nz * nz);
               double cap = (double)this.maxSpeed.getValue().floatValue();
               double before = Math.sqrt(this.capturedVelocity.x * this.capturedVelocity.x + this.capturedVelocity.z * this.capturedVelocity.z);
               if (hSpeed > cap && hSpeed > before) {
                  double k = Math.max(cap, before) / hSpeed;
                  nx *= k;
                  nz *= k;
               }

               player.setVelocity(nx, this.capturedVelocity.y + delta.y * (double)b, nz);
               this.capturedVelocity = null;
            }
         }
      }
   }

   @Override
   public void onUpdate() {
      if (mc.player == null || !mc.player.isFallFlying()) {
         this.lastHeading = null;
         this.capturedVelocity = null;
         return;
      }

      Vec3d vel = mc.player.getVelocity();
      double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
      if (hSpeed < 0.05) {
         this.lastHeading = null;
         return;
      }

      Vec3d heading = new Vec3d(vel.x / hSpeed, 0.0, vel.z / hSpeed);
      boolean stable = this.lastHeading == null || heading.dotProduct(this.lastHeading) >= STABLE_COS;
      this.lastHeading = heading;
      float b = this.boost.getValue();
      if (!stable || Math.abs(b - 1.0F) < 0.01F) {
         return;
      }

      double multiplier;
      if (b > 1.0F) {
         double cap = (double)this.maxSpeed.getValue().floatValue();
         if (hSpeed >= cap) {
            return;
         }

         multiplier = Math.min(1.0 + (double)(b - 1.0F) * 0.005, cap / hSpeed);
      } else {
         multiplier = 1.0 - (double)(1.0F - b) * 0.06;
      }

      mc.player.setVelocity(vel.x * multiplier, vel.y, vel.z * multiplier);
   }
}
