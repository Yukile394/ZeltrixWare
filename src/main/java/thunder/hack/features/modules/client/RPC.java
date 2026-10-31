package thunder.hack.features.modules.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.discord.DiscordEventHandlers;
import thunder.hack.utility.discord.DiscordRPC;
import thunder.hack.utility.discord.DiscordRichPresence;

public final class RPC extends Module {
   private static final DiscordRPC rpc = DiscordRPC.INSTANCE;
   public static Setting<String> appId = new Setting<>("AppID", "1541752948806254654");
   public static Setting<RPC.Mode> mode = new Setting<>("Picture", RPC.Mode.Zeltix);
   public static Setting<RPC.dMode> dmode = new Setting<>("DetailsMode", RPC.dMode.Server);
   public static Setting<String> detailsText = new Setting<>("CustomDetails", "Playing ZeltixWare", v -> dmode.getValue() == RPC.dMode.Custom);
   public static Setting<RPC.sMode> smode = new Setting<>("StateMode", RPC.sMode.CreatedBy);
   public static Setting<String> authorText = new Setting<>("AuthorText", "Created By Zaltre", v -> smode.getValue() == RPC.sMode.CreatedBy);
   public static Setting<String> state = new Setting<>("CustomState", "Beta? Recode? NextGen?", v -> smode.getValue() == RPC.sMode.Custom);
   public static Setting<Boolean> showIP = new Setting<>("ShowIP", true, v -> smode.getValue() == RPC.sMode.Server || dmode.getValue() == RPC.dMode.Server);
   public static Setting<Boolean> nickname = new Setting<>("Nickname", true);
   private static final String DOWNLOAD_LABEL = "Discord";
   private static final String DOWNLOAD_URL = "https://discord.gg/zenobest";
   public static Setting<Boolean> secondButton = new Setting<>("SecondButton", true);
   public static Setting<String> button2Label = new Setting<>("Button2Label", "Discord", v -> secondButton.getValue());
   public static Setting<String> button2Url = new Setting<>("Button2URL", "https://discord.gg/4SYP6U5Uct", v -> secondButton.getValue());
   public static DiscordRichPresence presence = new DiscordRichPresence();
   public static boolean started;
   static String String1 = "none";
   private final Timer timer_delay = new Timer();
   private static Thread thread;
   private static String lastInitializedAppId = "";

   public RPC() {
      super("DiscordRPC", Module.Category.CLIENT);
   }

   public static void readFile() {
      try {
         File file = new File("Zeltrix/misc/RPC.txt");
         if (!file.exists()) {
            file = new File("ThunderHackRecode/misc/RPC.txt");
         }

         if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
               while (reader.ready()) {
                  String1 = reader.readLine();
               }
            }
         }
      } catch (Exception var6) {
      }
   }

   public static void WriteFile(String url1, String url2) {
      File folder = new File("Zeltrix/misc");
      if (!folder.exists()) {
         folder.mkdirs();
      }

      File file = new File(folder, "RPC.txt");

      try {
         file.createNewFile();

         try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(url1 + "SEPARATOR" + url2 + "\n");
         } catch (Exception var9) {
         }
      } catch (Exception var10) {
      }
   }

   @Override
   public void disable() {
      this.setEnabled(true);
      if (!started) {
         this.startRpc();
      }
   }

   @Override
   public void onDisable() {
   }

   @Override
   public List<Setting<?>> getSettings() {
      return new ArrayList<>();
   }

   @Override
   public void onEnable() {
      this.startRpc();
   }

   @Override
   public void onUpdate() {
      if (!started) {
         this.startRpc();
      }
   }

   public synchronized void stopRpc() {
      started = false;
      if (thread != null && !thread.isInterrupted()) {
         thread.interrupt();
         thread = null;
      }

      if (rpc != null) {
         rpc.Discord_Shutdown();
      }

      lastInitializedAppId = "";
   }

   public synchronized void startRpc() {
      if (!this.isDisabled() && rpc != null) {
         String currentAppId = appId.getValue() != null && !appId.getValue().trim().isEmpty() ? appId.getValue().trim() : "1541752948806254654";
         if (started && !currentAppId.equals(lastInitializedAppId)) {
            this.stopRpc();
         }

         if (!started) {
            started = true;
            lastInitializedAppId = currentAppId;
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            rpc.Discord_Initialize(currentAppId, handlers, true, "");
            presence.startTimestamp = System.currentTimeMillis() / 1000L;
            presence.largeImageText = "ZeltixWare v1.7b2407 [" + ThunderHack.GITHUB_HASH + "]";
            rpc.Discord_UpdatePresence(presence);
            thread = new Thread(() -> {
               while (!Thread.currentThread().isInterrupted()) {
                  rpc.Discord_RunCallbacks();
                  presence.details = this.getDetails();
                  presence.state = this.getState();
                  if (nickname.getValue() && mc != null && mc.getSession() != null) {
                     presence.smallImageText = "logged as - " + mc.getSession().getUsername();
                     presence.smallImageKey = "https://minotar.net/helm/" + mc.getSession().getUsername() + "/100.png";
                  } else {
                     presence.smallImageText = "";
                     presence.smallImageKey = "";
                  }

                  presence.button_label_1 = "Discord";
                  presence.button_url_1 = "https://discord.gg/zenobest";
                  if (secondButton.getValue() && button2Url.getValue() != null && !button2Url.getValue().isEmpty()) {
                     presence.button_label_2 = button2Label.getValue();
                     presence.button_url_2 = button2Url.getValue();
                  } else {
                     presence.button_label_2 = "";
                     presence.button_url_2 = "";
                  }

                  presence.largeImageText = "ZeltixWare v1.7b2407 [" + ThunderHack.GITHUB_HASH + "]";
                  switch ((RPC.Mode)mode.getValue()) {
                     case Zeltix:
                        presence.largeImageKey = "https://i.imgur.com/yY0z2Uq.gif";
                        break;
                     case Recode:
                        presence.largeImageKey = "https://i.imgur.com/yY0z2Uq.gif";
                        break;
                     case MegaCute:
                        presence.largeImageKey = "https://media1.tenor.com/images/6bcbfcc0be97d029613b54f97845bc59/tenor.gif?itemid=26823781";
                        break;
                     case Custom:
                        readFile();
                        String[] parts = String1.split("SEPARATOR");
                        if (parts.length > 0) {
                           presence.largeImageKey = parts[0];
                        }

                        if (parts.length > 1 && !Objects.equals(parts[1], "none")) {
                           presence.smallImageKey = parts[1];
                        }
                  }

                  rpc.Discord_UpdatePresence(presence);

                  try {
                     Thread.sleep(2000L);
                  } catch (InterruptedException var2x) {
                     break;
                  }
               }
            }, "Zeltix-RPC-Handler");
            thread.start();
         }
      }
   }

   private String getDetails() {
      switch ((RPC.dMode)dmode.getValue()) {
         case Server:
            return this.getServerInfo();
         case ZeltixWare:
            return "Playing ZeltixWare";
         case Custom:
            return detailsText.getValue();
         default:
            return "Playing ZeltixWare";
      }
   }

   private String getState() {
      switch ((RPC.sMode)smode.getValue()) {
         case CreatedBy:
            return authorText.getValue();
         case Server:
            return this.getServerInfo();
         case Stats:
            return "Hacks: " + Managers.MODULE.getEnabledModules().size() + " / " + Managers.MODULE.modules.size();
         case Version:
            return "ZeltixWare v1.7b2407 for mc 1.21";
         case Custom:
            return state.getValue();
         default:
            return authorText.getValue();
      }
   }

   private String getServerInfo() {
      if (mc != null && mc.world != null && mc.player != null) {
         if (!mc.isInSingleplayer() && mc.getCurrentServerEntry() != null) {
            if (showIP.getValue()) {
               String ip = mc.getCurrentServerEntry().address;
               if (ip != null && !ip.isEmpty()) {
                  return "Playing on " + ip;
               }
            }

            return "Playing Multiplayer";
         } else {
            return "Singleplayer";
         }
      } else if (mc == null || mc.currentScreen == null) {
         return "In Main Menu";
      } else if (mc.currentScreen instanceof TitleScreen) {
         return "In Main Menu";
      } else if (mc.currentScreen instanceof MultiplayerScreen) {
         return "In Server List";
      } else {
         return mc.currentScreen instanceof AddServerScreen ? "Adding Server" : "In Menu";
      }
   }

   public static enum Mode {
      Zeltix,
      Recode,
      MegaCute,
      Custom;
   }

   public static enum dMode {
      Server,
      ZeltixWare,
      Custom;
   }

   public static enum sMode {
      CreatedBy,
      Server,
      Stats,
      Version,
      Custom;
   }
}
