package thunder.hack.injection;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.manager.IManager;

@Mixin({AbstractBlock.class})
public abstract class MixinAbstractBlock {
   @Inject(
      method = {"calcBlockBreakingDelta"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void calcBlockBreakingDeltaHook(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> ci) {
   }

   public float getDigSpeed(BlockState state, ItemStack stack) {
      double str = (double)stack.getMiningSpeedMultiplier(state);
      int effect = EnchantmentHelper.getLevel(
         (RegistryEntry)IManager.mc.world.getRegistryManager().get(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY).get(), stack
      );
      return (float)Math.max(str + (str > 1.0 ? (double)(effect * effect) + 1.0 : 0.0), 0.0);
   }
}
