package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class TargetStrafe extends Module {
   public final Setting<Float> range = new Setting<>("Range", 3.0F, 0.1F, 10.0F);
   public final Setting<Boolean> autoJump = new Setting<>("AutoJump", true);
   public final Setting<Boolean> autoTarget = new Setting<>("AutoTarget", true);
   private PlayerEntity target = null;
   private int direction = 1;

   public TargetStrafe() {
      super("TargetStrafe", Module.Category.MOVEMENT);
   }

   @Override
   public void onDisable() {
      this.target = null;
   }

   @EventHandler
   public void onSync(EventSync e) {
      if (mc.player != null && mc.world != null) {
         this.target = null;

         for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player
               && player.isAlive()
               && !(mc.player.distanceTo(player) > 15.0F)
               && (this.target == null || mc.player.distanceTo(player) < mc.player.distanceTo(this.target))) {
               this.target = player;
            }
         }
      }
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent e) {
      if (mc.player != null && this.target != null) {
         if (mc.player.horizontalCollision) {
            this.direction = -this.direction;
         }

         if (this.autoJump.getValue() && mc.player.isOnGround() && mc.player.distanceTo(this.target) <= this.range.getValue()) {
            mc.player.jump();
         }
      }
   }

   @EventHandler
   public void onMove(EventMove e) {
      if (mc.player != null && this.target != null) {
         double distance = (double)mc.player.distanceTo(this.target);
         if (this.autoTarget.getValue() || distance <= (double)this.range.getValue().floatValue()) {
            float moveForward = 1.0F;
            float moveStrafe = (float)this.direction;
            if (distance <= (double)this.range.getValue().floatValue()) {
               moveForward = 0.0F;
            }

            float yaw = this.getRotations(this.target)[0];
            double currentSpeed = Math.max(MovementUtility.getSpeed(), MovementUtility.getBaseMoveSpeed());
            double[] dir = this.getDirection(yaw, currentSpeed, moveForward, moveStrafe);
            e.setX(dir[0]);
            e.setZ(dir[1]);
            e.cancel();
            mc.player.setVelocity(e.getX(), mc.player.getVelocity().y, e.getZ());
         }
      }
   }

   private double[] getDirection(float yaw, double speed, float moveForward, float moveStrafe) {
      if (moveForward == 0.0F && moveStrafe == 0.0F) {
         return new double[]{0.0, 0.0};
      } else {
         if (moveForward != 0.0F) {
            if (moveStrafe >= 1.0F) {
               yaw += (float)(moveForward > 0.0F ? -45 : 45);
               moveStrafe = 0.0F;
            } else if (moveStrafe <= -1.0F) {
               yaw += (float)(moveForward > 0.0F ? 45 : -45);
               moveStrafe = 0.0F;
            }

            if (moveForward > 0.0F) {
               moveForward = 1.0F;
            } else if (moveForward < 0.0F) {
               moveForward = -1.0F;
            }
         }

         double cos = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
         double sin = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
         return new double[]{
            (double)moveForward * speed * cos + (double)moveStrafe * speed * sin, (double)moveForward * speed * sin - (double)moveStrafe * speed * cos
         };
      }
   }

   private float[] getRotations(Entity entity) {
      double dx = entity.getX() - mc.player.getX();
      double dz = entity.getZ() - mc.player.getZ();
      double dy = entity.getY() + (double)entity.getStandingEyeHeight() - (mc.player.getY() + (double)mc.player.getStandingEyeHeight());
      double dist = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      return new float[]{yaw, pitch};
   }
}
