package thunder.hack.injection;

import net.minecraft.world.BlockView;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.features.modules.player.NoWeb;

@Mixin({Block.class})
public abstract class MixinBlock {
   @Inject(
      method = {"shouldDrawSide"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void shouldDrawSideHook(
      BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir
   ) {
   }

   @Inject(
      method = {"getVelocityMultiplier"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getVelocityMultiplierHook(CallbackInfoReturnable<Float> cir) {
      if (NoWeb.isNoWebEnabled()) {
         if ((Object)this instanceof CobwebBlock) {
            NoWeb.Mode mode = NoWeb.getNoWebMode();
            if (mode == NoWeb.Mode.Cancel || mode == NoWeb.Mode.Spoof || mode == NoWeb.Mode.Zeltrix) {
               cir.setReturnValue(1.0F);
            } else if (mode == NoWeb.Mode.Legit) {
               cir.setReturnValue(0.7F + NoWeb.getLegitFactor() * 0.3F);
            }
         }
      }
   }

   @Inject(
      method = {"getSlipperiness"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getSlipperinessHook(CallbackInfoReturnable<Float> cir) {
   }
}
