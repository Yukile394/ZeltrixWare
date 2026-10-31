package thunder.hack.features.modules.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public class AntiCrash extends Module {
   public final Setting<Boolean> debug = new Setting<>("Debug", false);
   private Timer debugTimer = new Timer();

   public AntiCrash() {
      super("AntiCrash", Module.Category.CLIENT);
   }

   @EventHandler
   public void onPacketReceive(@NotNull PacketEvent.Receive receive) {
      if (receive.getPacket() instanceof ExplosionS2CPacket exp
         && (exp.getX() > 1.0E9 || exp.getY() > 1.0E9 || exp.getZ() > 1.0E9 || (double)exp.getRadius() > 1.0E9)) {
         if (this.debug.getValue() && this.debugTimer.passedMs(1000L)) {
            this.sendMessage("ExplosionS2CPacket canceled");
            this.debugTimer.reset();
         }

         receive.cancel();
         return;
      }

      if (receive.getPacket() instanceof ParticleS2CPacket p
         && (
            p.getX() > 1.0E9
               || p.getY() > 1.0E9
               || p.getZ() > 1.0E9
               || (double)p.getSpeed() > 1.0E9
               || (double)p.getOffsetX() > 1.0E9
               || (double)p.getOffsetY() > 1.0E9
               || (double)p.getOffsetZ() > 1.0E9
         )) {
         if (this.debug.getValue() && this.debugTimer.passedMs(1000L)) {
            this.sendMessage("ParticleS2CPacket canceled");
            this.debugTimer.reset();
         }

         receive.cancel();
         return;
      }

      if (receive.getPacket() instanceof PlayerPositionLookS2CPacket pos
         && (
            pos.getX() > 1.0E9
               || pos.getY() > 1.0E9
               || pos.getZ() > 1.0E9
               || (double)pos.getYaw() > 1.0E9
               || (double)pos.getPitch() > 1.0E9
         )) {
         if (this.debug.getValue() && this.debugTimer.passedMs(1000L)) {
            this.sendMessage("PlayerPositionLookS2CPacket canceled");
            this.debugTimer.reset();
         }

         receive.cancel();
      }
   }
}
