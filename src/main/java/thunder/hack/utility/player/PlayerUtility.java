package thunder.hack.utility.player;

import java.util.Objects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.hit.HitResult.Type;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.modules.Module;
import thunder.hack.utility.world.ExplosionUtility;

public final class PlayerUtility {
   public static boolean isInHell() {
      return Module.mc.world == null ? false : Objects.equals(Module.mc.world.getRegistryKey().getValue().getPath(), "the_nether");
   }

   public static boolean isInEnd() {
      return Module.mc.world == null ? false : Objects.equals(Module.mc.world.getRegistryKey().getValue().getPath(), "the_end");
   }

   public static boolean isInOver() {
      return Module.mc.world == null ? false : Objects.equals(Module.mc.world.getRegistryKey().getValue().getPath(), "overworld");
   }

   public static boolean isEating() {
      return Module.mc.player == null
         ? false
         : (
               Module.mc.player.getMainHandStack().getComponents().contains(DataComponentTypes.FOOD)
                  || Module.mc.player.getOffHandStack().getComponents().contains(DataComponentTypes.FOOD)
            )
            && Module.mc.player.isUsingItem();
   }

   public static boolean isMining() {
      return Module.mc.interactionManager == null ? false : Module.mc.interactionManager.isBreakingBlock();
   }

   public static float squaredDistanceFromEyes(@NotNull Vec3d targetPos) {
      if (Module.mc.player == null) {
         return 0.0F;
      } else {
         double dx = targetPos.x - Module.mc.player.getX();
         double dy = targetPos.y
            - (Module.mc.player.getY() + (double)Module.mc.player.getEyeHeight(Module.mc.player.getPose()));
         double dz = targetPos.z - Module.mc.player.getZ();
         return (float)(dx * dx + dy * dy + dz * dz);
      }
   }

   public static float squaredDistance2d(@NotNull Vec2f point) {
      if (Module.mc.player == null) {
         return 0.0F;
      } else {
         double d = Module.mc.player.getX() - (double)point.x;
         double f = Module.mc.player.getZ() - (double)point.y;
         return (float)(d * d + f * f);
      }
   }

   public static ClientPlayerEntity getPlayer() {
      return Module.mc.player;
   }

   public static float calculatePercentage(@NotNull ItemStack stack) {
      float durability = (float)(stack.getMaxDamage() - stack.getDamage());
      return durability / (float)stack.getMaxDamage() * 100.0F;
   }

   public static float fixAngle(float angle) {
      return (float)Math.round(angle / (float)((double)getGCD() * 0.15)) * (float)((double)getGCD() * 0.15);
   }

   public static float getGCD() {
      double sensitivity = (Double)Module.mc.options.getMouseSensitivity().getValue();
      double value = sensitivity * 0.6 + 0.2;
      double result = Math.pow(value, 3.0) * 8.0;
      return (float)result;
   }

   public static float squaredDistance2d(double x, double z) {
      if (Module.mc.player == null) {
         return 0.0F;
      } else {
         double d = Module.mc.player.getX() - x;
         double f = Module.mc.player.getZ() - z;
         return (float)(d * d + f * f);
      }
   }

   public static float getSquaredDistance2D(Vec3d vec) {
      double d0 = Module.mc.player.getX() - vec.getX();
      double d2 = Module.mc.player.getZ() - vec.getZ();
      return (float)(d0 * d0 + d2 * d2);
   }

   public static boolean canSee(Vec3d pos) {
      Vec3d vec3d = new Vec3d(Module.mc.player.getX(), Module.mc.player.getEyeY(), Module.mc.player.getZ());
      return pos.distanceTo(vec3d) > 128.0 ? false : ExplosionUtility.raycast(vec3d, pos, false) == Type.MISS;
   }

   public static boolean isFalling() {
      return Module.mc.player == null
         ? false
         : !Module.mc.player.isOnGround() && !Module.mc.player.isCreative() && Module.mc.player.getVelocity().y < 0.0;
   }
}
