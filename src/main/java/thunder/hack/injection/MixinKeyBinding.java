package thunder.hack.injection;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({KeyBinding.class})
public abstract class MixinKeyBinding {
   @Shadow
   public abstract boolean equals(KeyBinding var1);

   @Shadow
   public abstract boolean isPressed();

   @Inject(
      method = {"isPressed"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void pressHook(CallbackInfoReturnable<Boolean> cir) {
   }
}
