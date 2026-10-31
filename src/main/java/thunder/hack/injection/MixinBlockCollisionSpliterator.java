package thunder.hack.injection;

import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.world.BlockCollisionSpliterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {BlockCollisionSpliterator.class},
   priority = 800
)
public abstract class MixinBlockCollisionSpliterator {
   @Redirect(
      method = {"computeNext"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/BlockView;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"
      )
   )
   private BlockState computeNextHook(BlockView instance, BlockPos blockPos) {
      return instance.getBlockState(blockPos);
   }
}
