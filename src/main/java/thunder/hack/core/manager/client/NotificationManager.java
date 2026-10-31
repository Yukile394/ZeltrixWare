package thunder.hack.core.manager.client;

import com.google.common.collect.Lists;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Formatting;
import net.minecraft.client.gui.DrawContext;
import org.apache.commons.lang3.SystemUtils;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.Notifications;
import thunder.hack.gui.notification.Notification;

public class NotificationManager implements IManager {
   private final List<Notification> notifications = new ArrayList<>();
   private TrayIcon trayIcon;

   public void publicity(String title, String content, int second, Notification.Type type) {
      if (ModuleManager.notifications.mode.getValue() == Notifications.Mode.Text) {
         Command.sendMessage(Formatting.GRAY + "[" + Formatting.DARK_PURPLE + title + Formatting.GRAY + "] " + type.getColor() + content);
      }

      if (!mc.isWindowFocused()) {
         this.nativeNotification(content, title);
      }

      this.notifications.add(new Notification(title, content, type, second * 1000));
   }

   public void onRender2D(DrawContext context) {
      if (ModuleManager.notifications.isEnabled()) {
         float startY = isDefault() ? (float)mc.getWindow().getScaledHeight() - 36.0F : (float)mc.getWindow().getScaledHeight() / 2.0F + 25.0F;
         if (this.notifications.size() > 8) {
            this.notifications.removeFirst();
         }

         this.notifications.removeIf(Notification::shouldDelete);

         for (Notification n : Lists.newArrayList(this.notifications)) {
            startY = (float)((double)startY - n.getHeight() - 3.0);
            n.renderShaders(context.getMatrices(), startY + (float)(isDefault() ? 0 : this.notifications.size() * 16));
            n.render(context.getMatrices(), startY + (float)(isDefault() ? 0 : this.notifications.size() * 16));
         }
      }
   }

   public void onUpdate() {
      if (ModuleManager.notifications.isEnabled()) {
         this.notifications.forEach(Notification::onUpdate);
      }
   }

   public static boolean isDefault() {
      return ModuleManager.notifications.mode.getValue() == Notifications.Mode.Default;
   }

   private void nativeNotification(String message, String title) {
      if (SystemUtils.IS_OS_WINDOWS) {
         this.windows(message, title);
      } else if (SystemUtils.IS_OS_LINUX) {
         this.linux(message);
      } else if (SystemUtils.IS_OS_MAC) {
         this.mac(message);
      } else {
         ThunderHack.LOGGER.error("Unsupported OS: {}", SystemUtils.OS_NAME);
      }
   }

   private void windows(String message, String title) {
      if (SystemTray.isSupported()) {
         try {
            if (this.trayIcon == null) {
               SystemTray tray = SystemTray.getSystemTray();
               Image image = Toolkit.getDefaultToolkit().createImage("resources/icon.png");
               this.trayIcon = new TrayIcon(image, "ThunderHack");
               this.trayIcon.setImageAutoSize(true);
               this.trayIcon.setToolTip("ThunderHack");
               tray.add(this.trayIcon);
            }

            this.trayIcon.displayMessage(title, message, MessageType.INFO);
         } catch (Exception var5) {
            ThunderHack.LOGGER.error(var5.getMessage());
         }
      } else {
         ThunderHack.LOGGER.error("SystemTray is not supported");
      }
   }

   private void mac(String message) {
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command("osascript", "-e", "display notification \"" + message + "\" with title \"ThunderHack\"");

      try {
         processBuilder.start();
      } catch (IOException var4) {
         ThunderHack.LOGGER.error(var4.getMessage());
      }
   }

   private void linux(String message) {
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.command("notify-send", "-a", "ThunderHack", message);

      try {
         processBuilder.start();
      } catch (IOException var4) {
         ThunderHack.LOGGER.error(var4.getMessage());
      }
   }
}
