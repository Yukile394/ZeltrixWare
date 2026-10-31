package thunder.hack.features.modules.render;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public class ChatFucker extends Module {
   public Setting<String> text = new Setting<>("Text", "ChatFucker!");
   public Setting<Float> delay = new Setting<>("Delay", 3.0F, 0.1F, 20.0F);
   private final Timer timer = new Timer();

   public ChatFucker() {
      super("ChatFucker", Module.Category.RENDER);
   }

   @Override
   public void onUpdate() {
      if (this.timer.passedMs((long)(this.delay.getValue() * 1000.0F))) {
         String msg = this.text.getValue();
         if (msg != null && !msg.isEmpty()) {
            if (msg.startsWith("/")) {
               mc.player.networkHandler.sendCommand(msg.substring(1));
            } else {
               mc.player.networkHandler.sendChatMessage(msg);
            }
         }

         this.timer.reset();
      }
   }
}
