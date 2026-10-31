package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventTick;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class NoWeb extends Module {
   public static final Setting<NoWeb.Mode> mode = new Setting<>("Mode", NoWeb.Mode.Zeltrix);
   private final Setting<Float> legitFactor = new Setting<>("LegitFactor", 0.55F, 0.1F, 0.99F, v -> mode.getValue() == NoWeb.Mode.Legit);
   private final Setting<Float> speed = new Setting<>("Speed", 0.633F, 0.1F, 2.0F, v -> mode.getValue() == NoWeb.Mode.Zeltrix);
   private final Setting<Float> motionY = new Setting<>("MotionY", 1.3F, 0.1F, 3.0F, v -> mode.getValue() == NoWeb.Mode.Zeltrix);

   public static boolean isNoWebEnabled() {
      return ModuleManager.noWeb != null && ModuleManager.noWeb.isEnabled();
   }

   public static NoWeb.Mode getNoWebMode() {
      return ModuleManager.noWeb == null ? NoWeb.Mode.Cancel : mode.getValue();
   }

   public static float getLegitFactor() {
      return ModuleManager.noWeb == null ? 0.55F : ModuleManager.noWeb.legitFactor.getValue();
   }

   public NoWeb() {
      super("NoWeb", Module.Category.PLAYER);
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (mc.player != null && mc.world != null) {
         if (mode.getValue() == NoWeb.Mode.Spoof) {
            if (this.isInWebLocal()) {
               Vec3d vel = mc.player.getVelocity();
               double newX = vel.x * 1.85;
               double newZ = vel.z * 1.85;
               mc.player.setVelocity(newX, vel.y, newZ);
            }
         } else if (mode.getValue() == NoWeb.Mode.Zeltrix && this.isInWebLocal()) {
            double mY = mc.options.jumpKey.isPressed()
               ? (double)this.motionY.getValue().floatValue()
               : (mc.options.sneakKey.isPressed() ? (double)(-this.motionY.getValue()) : 0.0);
            float yaw = mc.player.getYaw() * (float) (Math.PI / 180.0);
            float moveForward = mc.player.input != null ? mc.player.input.movementForward : mc.player.forwardSpeed;
            float moveSideways = mc.player.input != null ? mc.player.input.movementSideways : mc.player.sidewaysSpeed;
            float f = moveForward * this.speed.getValue();
            float s = moveSideways * this.speed.getValue();
            if (f == 0.0F && s == 0.0F) {
               mc.player.setVelocity(0.0, mY, 0.0);
            } else {
               mc.player
                  .setVelocity(
                     (double)(-MathHelper.sin(yaw) * f + MathHelper.cos(yaw) * s),
                     mY,
                     (double)(MathHelper.cos(yaw) * f + MathHelper.sin(yaw) * s)
                  );
            }
         }
      }
   }

   public boolean isInWebLocal() {
      if (mc.player != null && mc.world != null) {
         Box pBox = mc.player.getBoundingBox();
         BlockPos pBlockPos = BlockPos.ofFloored(mc.player.getPos());

         for (int x = pBlockPos.getX() - 1; x <= pBlockPos.getX() + 1; x++) {
            for (int y = pBlockPos.getY() - 1; y <= pBlockPos.getY() + 2; y++) {
               for (int z = pBlockPos.getZ() - 1; z <= pBlockPos.getZ() + 1; z++) {
                  BlockPos bp = new BlockPos(x, y, z);
                  if (pBox.intersects(new Box(bp)) && mc.world.getBlockState(bp).getBlock() == Blocks.COBWEB) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static enum Mode {
      Cancel,
      Spoof,
      Legit,
      Zeltrix;
   }
}
