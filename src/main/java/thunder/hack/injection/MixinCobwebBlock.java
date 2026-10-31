package thunder.hack.injection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.modules.player.NoWeb;

@Mixin({CobwebBlock.class})
public class MixinCobwebBlock {
   @Inject(
      method = {"onEntityCollision"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onEntityCollisionHook(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
      if (IManager.mc != null && IManager.mc.player != null) {
         if (entity instanceof PlayerEntity && entity == IManager.mc.player) {
            if (NoWeb.isNoWebEnabled()) {
               NoWeb.Mode mode = NoWeb.getNoWebMode();
               if (mode == NoWeb.Mode.Cancel || mode == NoWeb.Mode.Spoof || mode == NoWeb.Mode.Zeltrix) {
                  ci.cancel();
               } else if (mode == NoWeb.Mode.Legit) {
                  ci.cancel();
                  float factor = NoWeb.getLegitFactor();
                  Vec3d vel = entity.getVelocity();
                  entity.setVelocity(vel.x * (double)factor, vel.y * 0.05, vel.z * (double)factor);
               }
            }
         }
      }
   }
}
