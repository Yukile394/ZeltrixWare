package thunder.hack.injection;

import net.minecraft.block.AbstractBlock.AbstractBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractBlockState.class})
public class MixinAbstractBlockState {
   @Inject(
      method = {"getLuminance"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getLuminanceHook(CallbackInfoReturnable<Integer> cir) {
   }
}
