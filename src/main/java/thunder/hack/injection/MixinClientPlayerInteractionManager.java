package thunder.hack.injection;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventAttackBlock;
import thunder.hack.events.impl.EventBreakBlock;
import thunder.hack.events.impl.EventClickSlot;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.player.SpeedMine;

@Mixin({ClientPlayerInteractionManager.class})
public class MixinClientPlayerInteractionManager {
   @Shadow
   private int blockBreakingCooldown;

   @Redirect(
      method = {"updateBlockBreakingProgress"},
      at = @At(
         value = "FIELD",
         target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I",
         opcode = 180,
         ordinal = 0
      )
   )
   public int updateBlockBreakingProgressHook(ClientPlayerInteractionManager clientPlayerInteractionManager) {
      return ModuleManager.speedMine.isEnabled() ? 0 : this.blockBreakingCooldown;
   }

   @Inject(
      method = {"updateBlockBreakingProgress"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void updateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      if (ModuleManager.speedMine.isEnabled() && ModuleManager.speedMine.mode.getValue() == SpeedMine.Mode.Packet) {
         cir.setReturnValue(false);
      }
   }

   @Inject(
      method = {"attackBlock"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void attackBlockHook(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      if (!Module.fullNullCheck()) {
         EventAttackBlock event = new EventAttackBlock(pos, direction);
         ThunderHack.EVENT_BUS.post(event);
         if (event.isCancelled()) {
            cir.setReturnValue(false);
         }
      }
   }

   @Inject(
      method = {"breakBlock"},
      at = {@At("RETURN")},
      cancellable = true
   )
   public void breakBlockHook(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      if (!Module.fullNullCheck()) {
         EventBreakBlock event = new EventBreakBlock(pos);
         ThunderHack.EVENT_BUS.post(event);
         if (event.isCancelled()) {
            cir.setReturnValue(false);
         }
      }
   }

   @Inject(
      method = {"clickSlot"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void clickSlotHook(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         EventClickSlot event = new EventClickSlot(actionType, slotId, button, syncId);
         ThunderHack.EVENT_BUS.post(event);
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }
}
