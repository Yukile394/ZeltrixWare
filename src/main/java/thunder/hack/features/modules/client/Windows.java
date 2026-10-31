package thunder.hack.features.modules.client;

import thunder.hack.features.modules.Module;
import thunder.hack.gui.windows.WindowsScreen;
import thunder.hack.gui.windows.impl.ConfigWindow;
import thunder.hack.gui.windows.impl.FriendsWindow;
import thunder.hack.gui.windows.impl.MacroWindow;
import thunder.hack.gui.windows.impl.ProxyWindow;
import thunder.hack.gui.windows.impl.WaypointWindow;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.PositionSetting;

public class Windows extends Module {
   public final Setting<PositionSetting> macroPos = new Setting<>("macroPos", new PositionSetting(0.3F, 0.3F), v -> false);
   public final Setting<PositionSetting> configPos = new Setting<>("configPos", new PositionSetting(0.35F, 0.35F), v -> false);
   public final Setting<PositionSetting> friendPos = new Setting<>("friendPos", new PositionSetting(0.4F, 0.4F), v -> false);
   public final Setting<PositionSetting> waypointPos = new Setting<>("waypointPos", new PositionSetting(0.45F, 0.45F), v -> false);
   public final Setting<PositionSetting> proxyPos = new Setting<>("proxyPos", new PositionSetting(0.5F, 0.5F), v -> false);

   public Windows() {
      super("Windows", Module.Category.CLIENT);
   }

   @Override
   public void onEnable() {
      mc.setScreen(
         new WindowsScreen(
            MacroWindow.get(
               this.macroPos.getValue().getX() * (float)mc.getWindow().getScaledWidth(),
               this.macroPos.getValue().getY() * (float)mc.getWindow().getScaledHeight(),
               this.macroPos
            ),
            ConfigWindow.get(
               this.configPos.getValue().getX() * (float)mc.getWindow().getScaledWidth(),
               this.configPos.getValue().getY() * (float)mc.getWindow().getScaledHeight(),
               this.configPos
            ),
            FriendsWindow.get(
               this.friendPos.getValue().getX() * (float)mc.getWindow().getScaledWidth(),
               this.friendPos.getValue().getY() * (float)mc.getWindow().getScaledHeight(),
               this.friendPos
            ),
            WaypointWindow.get(
               this.waypointPos.getValue().getX() * (float)mc.getWindow().getScaledWidth(),
               this.waypointPos.getValue().getY() * (float)mc.getWindow().getScaledHeight(),
               this.waypointPos
            ),
            ProxyWindow.get(
               this.proxyPos.getValue().getX() * (float)mc.getWindow().getScaledWidth(),
               this.proxyPos.getValue().getY() * (float)mc.getWindow().getScaledHeight(),
               this.proxyPos
            )
         )
      );
      this.disable();
   }
}
