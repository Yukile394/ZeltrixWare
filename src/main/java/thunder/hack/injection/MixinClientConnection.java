package thunder.hack.injection;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.Socks5ProxyHandler;
import java.net.InetSocketAddress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.handler.PacketSizeLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.client.ProxyManager;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;

@Mixin({ClientConnection.class})
public class MixinClientConnection {
   @Inject(
      method = {"exceptionCaught"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void exceptionCaughtHook(ChannelHandlerContext context, Throwable t, CallbackInfo ci) {
      if (ModuleManager.antiPacketException.isEnabled()) {
         ModuleManager.antiPacketException.sendChatMessage(t.getMessage());
         ci.cancel();
      }
   }

   @Inject(
      method = {"handlePacket"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo info) {
      if (!Module.fullNullCheck()) {
         if (packet instanceof BundleS2CPacket packs) {
            packs.getPackets().forEach(p -> {
               PacketEvent.Receive eventx = new PacketEvent.Receive((Packet<?>)p);
               ThunderHack.EVENT_BUS.post(eventx);
               if (eventx.isCancelled()) {
                  info.cancel();
               }
            });
         } else {
            PacketEvent.Receive event = new PacketEvent.Receive(packet);
            ThunderHack.EVENT_BUS.post(event);
            if (event.isCancelled()) {
               info.cancel();
            }
         }
      }
   }

   @Inject(
      method = {"handlePacket"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private static <T extends PacketListener> void onHandlePacketPost(Packet<T> packet, PacketListener listener, CallbackInfo info) {
      if (!Module.fullNullCheck()) {
         if (packet instanceof BundleS2CPacket packs) {
            packs.getPackets().forEach(p -> {
               PacketEvent.ReceivePost eventx = new PacketEvent.ReceivePost((Packet<?>)p);
               ThunderHack.EVENT_BUS.post(eventx);
               if (eventx.isCancelled()) {
                  info.cancel();
               }
            });
         } else {
            PacketEvent.ReceivePost event = new PacketEvent.ReceivePost(packet);
            ThunderHack.EVENT_BUS.post(event);
            if (event.isCancelled()) {
               info.cancel();
            }
         }
      }
   }

   @Inject(
      method = {"send(Lnet/minecraft/network/packet/Packet;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacketPre(Packet<?> packet, CallbackInfo info) {
      if (!Module.fullNullCheck()) {
         if (ThunderHack.core.silentPackets.contains(packet)) {
            ThunderHack.core.silentPackets.remove(packet);
         } else {
            PacketEvent.Send event = new PacketEvent.Send(packet);
            ThunderHack.EVENT_BUS.post(event);
            if (event.isCancelled()) {
               info.cancel();
            }
         }
      }
   }

   @Inject(
      method = {"send(Lnet/minecraft/network/packet/Packet;)V"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onSendPacketPost(Packet<?> packet, CallbackInfo info) {
      if (!Module.fullNullCheck()) {
         PacketEvent.SendPost event = new PacketEvent.SendPost(packet);
         ThunderHack.EVENT_BUS.post(event);
         if (event.isCancelled()) {
            info.cancel();
         }
      }
   }

   @Inject(
      method = {"addHandlers"},
      at = {@At("RETURN")}
   )
   private static void addHandlersHook(ChannelPipeline pipeline, NetworkSide side, boolean local, PacketSizeLogger packetSizeLogger, CallbackInfo ci) {
      ProxyManager.ThProxy proxy = Managers.PROXY.getActiveProxy();
      if (proxy != null && side == NetworkSide.CLIENTBOUND && !local) {
         pipeline.addFirst(new ChannelHandler[]{new Socks5ProxyHandler(new InetSocketAddress(proxy.getIp(), proxy.getPort()), proxy.getL(), proxy.getP())});
      }
   }
}
