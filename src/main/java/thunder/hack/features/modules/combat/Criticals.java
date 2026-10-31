package thunder.hack.features.modules.combat;

import io.netty.buffer.Unpooled;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.setting.Setting;

public final class Criticals extends Module {
   public final Setting<Criticals.Mode> mode = new Setting<>("Mode", Criticals.Mode.UpdatedNCP);
   public static boolean cancelCrit;

   public Criticals() {
      super("Criticals", Module.Category.COMBAT);
   }

   @EventHandler
   public void onPacketSend(@NotNull PacketEvent.Send event) {
      if (event.getPacket() instanceof PlayerInteractEntityC2SPacket && getInteractType(event.getPacket()) == Criticals.InteractType.ATTACK) {
         Entity ent = getEntity(event.getPacket());
         if (ent == null || ent instanceof EndCrystalEntity || cancelCrit) {
            return;
         }

         this.doCrit();
      }
   }

   public void doCrit() {
      if (!this.isDisabled() && mc.player != null && mc.world != null) {
         if ((mc.player.isOnGround() || mc.player.getAbilities().flying || this.mode.is(Criticals.Mode.Grim))
            && !mc.player.isInLava()
            && !mc.player.isSubmergedInWater()) {
            switch ((Criticals.Mode)this.mode.getValue()) {
               case Ncp:
                  this.critPacket(0.0625, false);
                  this.critPacket(0.0, false);
                  break;
               case Strict:
                  this.critPacket(0.062600301692775, false);
                  this.critPacket(0.07260029960661, false);
                  this.critPacket(0.0, false);
                  this.critPacket(0.0, false);
                  break;
               case OldNCP:
                  this.critPacket(1.058293536E-5, false);
                  this.critPacket(9.16580235E-6, false);
                  this.critPacket(1.0371854E-7, false);
                  break;
               case UpdatedNCP:
                  this.critPacket(2.71875E-7, false);
                  this.critPacket(0.0, false);
                  break;
               case Grim:
                  if (!mc.player.isOnGround()) {
                     this.critPacket(-1.0E-6, true);
                  }
            }
         }
      }
   }

   private void critPacket(double yDelta, boolean full) {
      if (!full) {
         this.sendPacket(new PositionAndOnGround(mc.player.getX(), mc.player.getY() + yDelta, mc.player.getZ(), false));
      } else {
         this.sendPacket(
            new Full(
               mc.player.getX(),
               mc.player.getY() + yDelta,
               mc.player.getZ(),
               ((IClientPlayerEntity)mc.player).getLastYaw(),
               ((IClientPlayerEntity)mc.player).getLastPitch(),
               false
            )
         );
      }
   }

   public static Entity getEntity(@NotNull PlayerInteractEntityC2SPacket packet) {
      PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
      packet.write(packetBuf);
      return mc.world.getEntityById(packetBuf.readVarInt());
   }

   public static Criticals.InteractType getInteractType(@NotNull PlayerInteractEntityC2SPacket packet) {
      PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
      packet.write(packetBuf);
      packetBuf.readVarInt();
      return (Criticals.InteractType)packetBuf.readEnumConstant(Criticals.InteractType.class);
   }

   public static enum InteractType {
      INTERACT,
      ATTACK,
      INTERACT_AT;
   }

   public static enum Mode {
      Ncp,
      Strict,
      OldNCP,
      UpdatedNCP,
      Grim;
   }
}
