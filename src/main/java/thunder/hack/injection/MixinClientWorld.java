package thunder.hack.injection;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity.RemovalReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventEntityRemoved;
import thunder.hack.events.impl.EventEntitySpawn;
import thunder.hack.events.impl.EventEntitySpawnPost;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.render.WorldTweaks;
import thunder.hack.setting.impl.ColorSetting;

@Mixin({ClientWorld.class})
public class MixinClientWorld {
   @Inject(
      method = {"addEntity"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void addEntityHook(Entity entity, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         EventEntitySpawn ees = new EventEntitySpawn(entity);
         ThunderHack.EVENT_BUS.post(ees);
         if (ees.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"addEntity"},
      at = {@At("RETURN")},
      cancellable = true
   )
   public void addEntityHookPost(Entity entity, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         EventEntitySpawnPost ees = new EventEntitySpawnPost(entity);
         ThunderHack.EVENT_BUS.post(ees);
         if (ees.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"removeEntity"},
      at = {@At("HEAD")}
   )
   public void removeEntityHook(int entityId, RemovalReason removalReason, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         EventEntityRemoved eer = new EventEntityRemoved(Module.mc.world.getEntityById(entityId));
         ThunderHack.EVENT_BUS.post(eer);
      }
   }

   @Inject(
      method = {"getSkyColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void getSkyColorHook(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
      if (ModuleManager.ambience.isEnabled()) {
         ColorSetting c = ModuleManager.ambience.color.getValue();
         cir.setReturnValue(new Vec3d((double)c.getGlRed(), (double)c.getGlGreen(), (double)c.getGlBlue()));
      } else if (ModuleManager.worldTweaks.isEnabled() && WorldTweaks.fogModify.getValue().isEnabled()) {
         ColorSetting c = WorldTweaks.fogColor.getValue();
         cir.setReturnValue(new Vec3d((double)c.getGlRed(), (double)c.getGlGreen(), (double)c.getGlBlue()));
      }
   }
}
