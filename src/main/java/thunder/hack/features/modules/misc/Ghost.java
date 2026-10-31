package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;

public class Ghost extends Module {
   private boolean bypass = false;

   public Ghost() {
      super("Ghost", Module.Category.MISC);
   }

   @Override
   public void onEnable() {
      this.bypass = false;
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         mc.player.requestRespawn();
      }

      this.bypass = false;
   }

   @Override
   public void onUpdate() {
      if (mc.player != null && mc.world != null) {
         if (mc.player.getHealth() == 0.0F) {
            mc.player.setHealth(20.0F);
            this.bypass = true;
            mc.setScreen(null);
            mc.player.setPosition(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            this.sendMessage(
               ClientSettings.isRu()
                  ? "\u0414\u043b\u044f \u0432\u043e\u0437\u0440\u043e\u0436\u0434\u0435\u043d\u0438\u044f \u0432\u044b\u043a\u043b\u044e\u0447\u0438 \u043c\u043e\u0434\u0443\u043b\u044c!"
                  : "To revive, turn off the module!"
            );
         }
      }
   }

   @EventHandler
   public void onPacketSend(PacketEvent.Send event) {
      if (this.bypass && event.getPacket() instanceof PlayerMoveC2SPacket) {
         event.cancel();
      }
   }
}
