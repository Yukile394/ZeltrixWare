package thunder.hack.injection;

import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.fluid.FlowableFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({FlowableFluid.class})
public abstract class MixinFlowableFluid {
   @Shadow
   protected abstract boolean isFlowBlocked(BlockView var1, BlockPos var2, Direction var3);
}
