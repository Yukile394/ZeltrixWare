package thunder.hack.injection.accesors;

import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.features.modules.movement.ElytraBooster;

@Mixin({FireworkRocketEntity.class})
public class MixinFireworkRocketEntity {
   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void onTickHead(CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null) {
         FireworkRocketEntity self = (FireworkRocketEntity)(Object)this;
         if (self.getOwner() instanceof ClientPlayerEntity player && player == mc.player) {
            ElytraBooster.getInstance().captureVelocity(player);
            return;
         }
      }
   }

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void onTickTail(CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null) {
         FireworkRocketEntity self = (FireworkRocketEntity)(Object)this;
         if (self.getOwner() instanceof ClientPlayerEntity player && player == mc.player) {
            ElytraBooster.getInstance().amplifyBoost(player);
            return;
         }
      }
   }
}
