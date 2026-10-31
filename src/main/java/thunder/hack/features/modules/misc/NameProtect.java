package thunder.hack.features.modules.misc;

import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class NameProtect extends Module {
   public static Setting<String> newName = new Setting<>("name", "Hell_Raider");
   public static Setting<Boolean> hideFriends = new Setting<>("Hide friends", true);

   public NameProtect() {
      super("NameProtect", Module.Category.MISC);
   }

   public static String getCustomName() {
      return ModuleManager.nameProtect.isEnabled() ? newName.getValue().replaceAll("&", "\u00a7") : mc.getGameProfile().getName();
   }
}
