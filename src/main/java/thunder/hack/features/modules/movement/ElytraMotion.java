package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.setting.Setting;

public class ElytraMotion extends Module {
   public final Setting<Float> attackDistance = new Setting<>("Distance", 3.0F, 0.1F, 5.0F);
   public boolean freeze;

   public ElytraMotion() {
      super("ElytraMotion", Module.Category.MOVEMENT);
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent event) {
      if (!mc.player.isFallFlying()) {
         this.freeze = false;
      } else if (this.check()) {
         mc.options.jumpKey.setPressed(false);
         this.freeze = true;
      } else {
         mc.options.jumpKey.setPressed(true);
         this.freeze = false;
      }
   }

   @EventHandler
   public void onMotion(EventMove eventMotion) {
      if (this.freeze) {
         eventMotion.setX(0.0);
         eventMotion.setY(0.0);
         eventMotion.setZ(0.0);
         eventMotion.cancel();
      }
   }

   public boolean check() {
      if (!ModuleManager.aura.isEnabled()) {
         return false;
      } else {
         Entity target = Aura.target;
         return target == null
            ? false
            : mc.player.squaredDistanceTo(target.getPos()) < (double)(this.attackDistance.getValue() * this.attackDistance.getValue())
               && mc.player.isFallFlying();
      }
   }

   @Override
   public void onDisable() {
      this.freeze = false;
      super.onDisable();
   }
}
