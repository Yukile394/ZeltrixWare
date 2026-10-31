package thunder.hack.features.modules.client;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public final class Notifications extends Module {
   public final Setting<Notifications.Mode> mode = new Setting<>("Mode", Notifications.Mode.CrossHair);

   public Notifications() {
      super("Notifications", Module.Category.CLIENT);
   }

   public static enum Mode {
      Default,
      CrossHair,
      Text;
   }
}
