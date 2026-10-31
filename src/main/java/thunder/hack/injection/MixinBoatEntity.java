package thunder.hack.injection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.Entity.PositionUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BoatEntity.class})
public class MixinBoatEntity {
   @Unique
   private float prevYaw;
   @Unique
   private float prevHeadYaw;

   @Inject(
      method = {"updatePassengerPosition"},
      at = {@At("HEAD")}
   )
   protected void updatePassengerPositionHookPre(Entity passenger, PositionUpdater positionUpdater, CallbackInfo ci) {
   }

   @Inject(
      method = {"updatePassengerPosition"},
      at = {@At("RETURN")}
   )
   protected void updatePassengerPositionHookPost(Entity passenger, PositionUpdater positionUpdater, CallbackInfo ci) {
   }
}
