package thunder.hack.features.modules.client;

import baritone.api.BaritoneAPI;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public final class BaritoneSettings extends Module {
   public final Setting<Boolean> allowBreakBlock = new Setting<>("AllowBreakBlock", true);
   public final Setting<Boolean> allowPlace = new Setting<>("AllowPlace", true);
   public final Setting<Boolean> allowSprint = new Setting<>("AllowSprint", true);
   public final Setting<Boolean> debug = new Setting<>("Debug", false);
   public final Setting<Boolean> enterPortal = new Setting<>("EnterPortal", false);
   public final Setting<Boolean> desktopNotifications = new Setting<>("DesktopNotifications", false);

   public BaritoneSettings() {
      super("BaritoneSettings", Module.Category.CLIENT);
   }

   @EventHandler
   public void onSettingChange(EventSetting e) {
      if (!ThunderHack.baritone) {
         this.sendMessage(
            ClientSettings.isRu()
               ? "\u0411\u0430\u0440\u0438\u0442\u043e\u043d \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d (\u043c\u043e\u0436\u0435\u0448\u044c \u0441\u043a\u0430\u0447\u0430\u0442\u044c \u043d\u0430 https://meteorclient.com)"
               : "Baritone not found (you can download it at https://meteorclient.com)"
         );
      } else {
         BaritoneAPI.getSettings().allowBreak.value = this.allowBreakBlock.getValue();
         BaritoneAPI.getSettings().allowPlace.value = this.allowPlace.getValue();
         BaritoneAPI.getSettings().allowSprint.value = this.allowSprint.getValue();
         BaritoneAPI.getSettings().chatDebug.value = this.debug.getValue();
         BaritoneAPI.getSettings().enterPortal.value = this.enterPortal.getValue();
         BaritoneAPI.getSettings().desktopNotifications.value = this.desktopNotifications.getValue();
      }
   }

   @Override
   public boolean isToggleable() {
      return false;
   }
}
