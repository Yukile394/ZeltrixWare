package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;

public class TotemPopCounter extends Module {
   public Setting<Boolean> notification = new Setting<>("Notification", true);

   public TotemPopCounter() {
      super("TotemPopCounter", Module.Category.MISC);
   }

   @EventHandler
   public void onTotemPop(@NotNull TotemPopEvent event) {
      if (event.getEntity() != mc.player) {
         String s;
         if (ClientSettings.isRu()) {
            s = Formatting.GREEN
               + event.getEntity().getName().getString()
               + Formatting.WHITE
               + " \u043f\u043e\u043f\u043d\u0443\u043b "
               + Formatting.AQUA
               + (
                  event.getPops() > 1
                     ? "" + event.getPops() + Formatting.WHITE + " \u0442\u043e\u0442\u0435\u043c\u043e\u0432!"
                     : Formatting.WHITE + "\u0442\u043e\u0442\u0435\u043c!"
               );
         } else {
            s = Formatting.GREEN
               + event.getEntity().getName().getString()
               + Formatting.WHITE
               + " popped "
               + Formatting.AQUA
               + (event.getPops() > 1 ? "" + event.getPops() + Formatting.WHITE + " totems!" : Formatting.WHITE + " a totem!");
         }

         this.sendMessage(s);
         if (this.notification.getValue()) {
            Managers.NOTIFICATION.publicity("TotemPopCounter", s, 2, Notification.Type.INFO);
         }
      }
   }

   @Override
   public void onUpdate() {
      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player != mc.player && !(player.getHealth() > 0.0F) && Managers.COMBAT.popList.containsKey(player.getName().getString())) {
            String s;
            if (ClientSettings.isRu()) {
               s = Formatting.GREEN
                  + player.getName().getString()
                  + Formatting.WHITE
                  + " \u043f\u043e\u043f\u043d\u0443\u043b "
                  + (
                     Managers.COMBAT.popList.get(player.getName().getString()) > 1
                        ? ""
                           + Managers.COMBAT.popList.get(player.getName().getString())
                           + Formatting.WHITE
                           + " \u0442\u043e\u0442\u0435\u043c\u043e\u0432 \u0438 \u0441\u0434\u043e\u0445!"
                        : Formatting.WHITE + "\u0442\u043e\u0442\u0435\u043c \u0438 \u0441\u0434\u043e\u0445!"
                  );
            } else {
               s = Formatting.GREEN
                  + player.getName().getString()
                  + Formatting.WHITE
                  + " popped "
                  + (
                     Managers.COMBAT.popList.get(player.getName().getString()) > 1
                        ? "" + Managers.COMBAT.popList.get(player.getName().getString()) + Formatting.WHITE + " totems and died EZ LMAO!"
                        : Formatting.WHITE + "totem and died EZ LMAO!"
                  );
            }

            this.sendMessage(s);
            if (this.notification.getValue()) {
               Managers.NOTIFICATION.publicity("TotemPopCounter", s, 2, Notification.Type.INFO);
            }
         }
      }
   }
}
