package thunder.hack.core.manager.client;

import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.handler.NetworkStateTransitions.InboundConfigurer;
import net.minecraft.network.handler.NetworkStateTransitions.OutboundConfigurer;
import thunder.hack.core.manager.IManager;

public class ProxyManager implements IManager {
   private final List<ProxyManager.ThProxy> proxies = new ArrayList<>();
   private ProxyManager.ThProxy activeProxy;

   public boolean isActive() {
      return this.getActiveProxy() != null;
   }

   public ProxyManager.ThProxy getActiveProxy() {
      return this.activeProxy;
   }

   public void addProxy(ProxyManager.ThProxy p) {
      this.proxies.add(p);
   }

   public void removeProxy(ProxyManager.ThProxy p) {
      this.proxies.remove(p);
   }

   public List<ProxyManager.ThProxy> getProxies() {
      return this.proxies;
   }

   public void setActiveProxy(ProxyManager.ThProxy proxy) {
      this.activeProxy = proxy;
   }

   public void onLoad() {
      try {
         File file = new File("ThunderHackRecode/misc/proxies.txt");
         if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
               while (reader.ready()) {
                  String[] line = reader.readLine().split(":");
                  String name = line[0];
                  String ip = line[1];
                  String port = line[2];
                  String login = line[3];
                  String password = line[4];
                  String active = line[5];
                  int p = 80;

                  try {
                     p = Integer.parseInt(port);
                  } catch (Exception var13) {
                     LogUtils.getLogger().warn(var13.getMessage());
                  }

                  ProxyManager.ThProxy proxy = new ProxyManager.ThProxy(name, ip, p, login, password);
                  this.addProxy(proxy);
                  if (Objects.equals(active, "true")) {
                     this.setActiveProxy(proxy);
                  }
               }
            }
         }
      } catch (Exception var15) {
      }
   }

   public void saveProxies() {
      File file = new File("ThunderHackRecode/misc/proxies.txt");

      try {
         new File("ThunderHackRecode").mkdirs();
         file.createNewFile();
      } catch (Exception var6) {
      }

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
         for (ProxyManager.ThProxy proxy : this.proxies) {
            writer.write(
               proxy.getName()
                  + ":"
                  + proxy.getIp()
                  + ":"
                  + proxy.getPort()
                  + ":"
                  + proxy.getL()
                  + ":"
                  + proxy.getP()
                  + ":"
                  + (this.getActiveProxy() == proxy)
                  + "\n"
            );
         }
      } catch (Exception var8) {
      }
   }

   public void checkPing(ProxyManager.ThProxy proxy) {
      long now = System.currentTimeMillis();
      proxy.setPing(-2);
      new Thread(
            () -> {
               try {
                  NioEventLoopGroup group = new NioEventLoopGroup();
                  Bootstrap bootstrap = new Bootstrap();
                  ((Bootstrap)((Bootstrap)bootstrap.group(group)).channel(NioSocketChannel.class))
                     .handler(
                        new ChannelInitializer<SocketChannel>() {
                           protected void initChannel(SocketChannel ch) {
                              ch.pipeline()
                                 .addLast(new ChannelHandler[]{new FlowControlHandler()})
                                 .addLast("timeout_proxy_checker", new ReadTimeoutHandler(8))
                                 .addLast("inbound_proxy_checker", new InboundConfigurer())
                                 .addLast("outbound_proxy_checker", new OutboundConfigurer())
                                 .addLast(
                                    new ChannelHandler[]{
                                       new Socks5ProxyHandler(new InetSocketAddress(proxy.getIp(), proxy.getPort()), proxy.getL(), proxy.getP())
                                    }
                                 )
                                 .addLast(new ChannelHandler[]{new ProxyManager.ProxyHandler()});
                           }
                        }
                     );
                  ChannelFuture future = bootstrap.connect("mcfunny.su", 25565).sync();
                  future.await();
                  if (future.isSuccess()) {
                     proxy.setPing((int)(System.currentTimeMillis() - now));
                  } else {
                     proxy.setPing(-1);
                  }

                  group.shutdownGracefully();
               } catch (Exception var7) {
                  proxy.setPing(-1);
               }
            }
         )
         .start();
   }

   private static class ProxyHandler extends ChannelInboundHandlerAdapter {
      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
         super.channelRead(ctx, msg);
      }

      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
         ctx.close();
      }
   }

   public static class ThProxy {
      private String name;
      private String ip;
      private String l;
      private String p;
      private int port;
      private int ping;

      public ThProxy(String name, String ip, int port, String l, String p) {
         this.ip = ip;
         this.l = l;
         this.p = p;
         this.name = name;
         this.port = port;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getIp() {
         return this.ip;
      }

      public void setIp(String ip) {
         this.ip = ip;
      }

      public String getL() {
         return this.l;
      }

      public void setL(String l) {
         this.l = l;
      }

      public String getP() {
         return this.p;
      }

      public void setP(String p) {
         this.p = p;
      }

      public void setPort(int port) {
         this.port = port;
      }

      public int getPort() {
         return this.port;
      }

      public void setPing(int ping) {
         this.ping = ping;
      }

      public int getPing() {
         return this.ping;
      }
   }
}
