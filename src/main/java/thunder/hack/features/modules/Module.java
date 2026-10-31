package thunder.hack.features.modules;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.util.Formatting;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.CommandManager;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.client.UnHook;
import thunder.hack.features.modules.client.Windows;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;

public abstract class Module {
   private final Setting<Bind> bind = new Setting<>("Keybind", new Bind(-1, false, false));
   private final Setting<Boolean> drawn = new Setting<>("Drawn", true);
   private final Setting<Boolean> enabled = new Setting<>("Enabled", false);
   private final String description;
   private final Module.Category category;
   private final String displayName;
   private final List<String> ignoreSoundList = Arrays.asList("ClickGui", "ThunderGui", "HudEditor");
   public static final MinecraftClient mc = MinecraftClient.getInstance();

   public Module(@NotNull String name, @NotNull Module.Category category) {
      this.displayName = name;
      this.description = "descriptions." + category.getName().toLowerCase() + "." + name.toLowerCase();
      this.category = category;
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onLogin() {
   }

   public void onLogout() {
   }

   public void onUpdate() {
   }

   public void onRender2D(DrawContext event) {
   }

   public void onRender3D(MatrixStack event) {
   }

   public void onUnload() {
   }

   public boolean isToggleable() {
      return true;
   }

   protected void sendPacket(Packet<?> packet) {
      if (mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().sendPacket(packet);
      }
   }

   protected void sendPacketSilent(Packet<?> packet) {
      if (mc.getNetworkHandler() != null) {
         ThunderHack.core.silentPackets.add(packet);
         mc.getNetworkHandler().sendPacket(packet);
      }
   }

   protected void sendSequencedPacket(SequencedPacketCreator packetCreator) {
      if (mc.getNetworkHandler() != null && mc.world != null) {
         PendingUpdateManager pendingUpdateManager = mc.world.getPendingUpdateManager().incrementSequence();

         try {
            int i = pendingUpdateManager.getSequence();
            mc.getNetworkHandler().sendPacket(packetCreator.predict(i));
         } catch (Throwable var6) {
            if (pendingUpdateManager != null) {
               try {
                  pendingUpdateManager.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (pendingUpdateManager != null) {
            pendingUpdateManager.close();
         }
      }
   }

   public String getDisplayInfo() {
      return null;
   }

   public boolean isOn() {
      return this.enabled.getValue();
   }

   public boolean isOff() {
      return !this.enabled.getValue();
   }

   public void setEnabled(boolean enabled) {
      this.enabled.setValue(enabled);
   }

   public void onThread() {
   }

   public void enable() {
      if (!(this instanceof UnHook)) {
         this.enabled.setValue(true);
      }

      if (!fullNullCheck() || this instanceof UnHook || this instanceof Windows) {
         this.onEnable();
      }

      if (this.isOn()) {
         ThunderHack.EVENT_BUS.subscribe(this);
      }

      if (!fullNullCheck()) {
         LogUtils.getLogger().info("[ThunderHack] enabled " + this.getName());
         Managers.MODULE.sortModules();
         if (!this.ignoreSoundList.contains(this.getDisplayName())) {
            Managers.NOTIFICATION
               .publicity(
                  this.getDisplayName(),
                  ClientSettings.isRu() ? "\u041c\u043e\u0434\u0443\u043b\u044c \u0432\u043a\u043b\u044e\u0447\u0435\u043d!" : "Was Enabled!",
                  2,
                  Notification.Type.ENABLED
               );
            Managers.SOUND.playEnable();
            ModuleManager.clientSound.onModuleEnable();
         }
      }
   }

   public void disable(String reason) {
      this.sendMessage(reason);
      this.disable();
   }

   public void disable() {
      try {
         ThunderHack.EVENT_BUS.unsubscribe(this);
      } catch (Exception var2) {
      }

      this.enabled.setValue(false);
      Managers.MODULE.sortModules();
      if (!fullNullCheck()) {
         this.onDisable();
         ThunderHack.LOGGER.info("[ThunderHack] disabled {}", this.getName());
         if (!this.ignoreSoundList.contains(this.getDisplayName())) {
            Managers.NOTIFICATION
               .publicity(
                  this.getDisplayName(),
                  ClientSettings.isRu() ? "\u041c\u043e\u0434\u0443\u043b\u044c \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d!" : "Was Disabled!",
                  2,
                  Notification.Type.DISABLED
               );
            Managers.SOUND.playDisable();
            ModuleManager.clientSound.onModuleDisable();
         }
      }
   }

   public void toggle() {
      if (this.enabled.getValue()) {
         this.disable();
      } else {
         this.enable();
      }
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getDescription() {
      return this.description;
   }

   public boolean isDrawn() {
      return this.drawn.getValue();
   }

   public void setDrawn(boolean d) {
      this.drawn.setValue(d);
   }

   public Module.Category getCategory() {
      return this.category;
   }

   public Bind getBind() {
      return this.bind.getValue();
   }

   public void setBind(int key, boolean mouse, boolean hold) {
      this.setBind(new Bind(key, mouse, hold));
   }

   public void setBind(Bind b) {
      this.bind.setValue(b);
   }

   public boolean listening() {
      return this.isOn();
   }

   public String getFullArrayString() {
      return this.getDisplayName()
         + Formatting.GRAY
         + (this.getDisplayInfo() != null ? " [" + Formatting.WHITE + this.getDisplayInfo() + Formatting.GRAY + "]" : "");
   }

   public static boolean fullNullCheck() {
      return mc.player == null || mc.world == null || ModuleManager.unHook.isEnabled();
   }

   public String getName() {
      return this.getDisplayName();
   }

   public List<Setting<?>> getSettings() {
      ArrayList<Setting<?>> settingList = new ArrayList<>();

      for (Class<?> currentSuperclass = this.getClass(); currentSuperclass != null; currentSuperclass = currentSuperclass.getSuperclass()) {
         for (Field field : currentSuperclass.getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
               try {
                  field.setAccessible(true);
                  settingList.add((Setting<?>)field.get(this));
               } catch (IllegalAccessException var8) {
                  ThunderHack.LOGGER.warn(var8.getMessage());
               }
            }
         }
      }

      settingList.forEach(s -> s.setModule(this));
      return settingList;
   }

   public boolean isEnabled() {
      return this.isOn();
   }

   public boolean isDisabled() {
      return !this.isEnabled();
   }

   public static void clickSlot(int id) {
      if (id != -1 && mc.interactionManager != null && mc.player != null) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, 0, SlotActionType.PICKUP, mc.player);
      }
   }

   public static void clickSlot(int id, SlotActionType type) {
      if (id != -1 && mc.interactionManager != null && mc.player != null) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, 0, type, mc.player);
      }
   }

   public static void clickSlot(int id, int button, SlotActionType type) {
      if (id != -1 && mc.interactionManager != null && mc.player != null) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, button, type, mc.player);
      }
   }

   public void sendMessage(String message) {
      if (!fullNullCheck() && ClientSettings.clientMessages.getValue() && !ModuleManager.unHook.isEnabled()) {
         if (mc.isOnThread()) {
            mc.player
               .sendMessage(
                  Text.of(
                     CommandManager.getClientMessage()
                        + " "
                        + Formatting.GRAY
                        + "["
                        + Formatting.DARK_PURPLE
                        + this.getDisplayName()
                        + Formatting.GRAY
                        + "] "
                        + message
                  )
               );
         } else {
            mc.executeSync(
               () -> mc.player
                     .sendMessage(
                        Text.of(
                           CommandManager.getClientMessage()
                              + " "
                              + Formatting.GRAY
                              + "["
                              + Formatting.DARK_PURPLE
                              + this.getDisplayName()
                              + Formatting.GRAY
                              + "] "
                              + message
                        )
                     )
            );
         }
      }
   }

   public void sendChatMessage(String message) {
      if (!fullNullCheck()) {
         mc.getNetworkHandler().sendChatMessage(message);
      }
   }

   public void sendChatCommand(String command) {
      if (!fullNullCheck()) {
         mc.getNetworkHandler().sendChatCommand(command);
      }
   }

   public void debug(String message) {
      if (!fullNullCheck() && ClientSettings.debug.getValue()) {
         if (mc.isOnThread()) {
            mc.player
               .sendMessage(
                  Text.of(
                     CommandManager.getClientMessage()
                        + " "
                        + Formatting.GRAY
                        + "["
                        + Formatting.DARK_PURPLE
                        + this.getDisplayName()
                        + Formatting.GRAY
                        + "] [\ud83d\udd27] "
                        + message
                  )
               );
         } else {
            mc.executeSync(
               () -> mc.player
                     .sendMessage(
                        Text.of(
                           CommandManager.getClientMessage()
                              + " "
                              + Formatting.GRAY
                              + "["
                              + Formatting.DARK_PURPLE
                              + this.getDisplayName()
                              + Formatting.GRAY
                              + "] [\ud83d\udd27] "
                              + message
                        )
                     )
            );
         }
      }
   }

   public boolean isKeyPressed(int button) {
      if (button != -1 && !ModuleManager.unHook.isEnabled()) {
         if (Managers.MODULE.activeMouseKeys.contains(button)) {
            Managers.MODULE.activeMouseKeys.clear();
            return true;
         } else {
            return button < 10 ? false : InputUtil.isKeyPressed(mc.getWindow().getHandle(), button);
         }
      } else {
         return false;
      }
   }

   public boolean isKeyPressed(Setting<Bind> bind) {
      return bind.getValue().getKey() != -1 && !ModuleManager.unHook.isEnabled() ? this.isKeyPressed(bind.getValue().getKey()) : false;
   }

   @Nullable
   public Setting<?> getSettingByName(String name) {
      for (Setting<?> setting : this.getSettings()) {
         if (setting.getName().equalsIgnoreCase(name)) {
            return setting;
         }
      }

      return null;
   }

   public static class Category {
      private final String name;
      private static final Map<String, Module.Category> CATEGORIES = new LinkedHashMap<>();
      public static final Module.Category COMBAT = new Module.Category("Combat");
      public static final Module.Category MISC = new Module.Category("Misc");
      public static final Module.Category RENDER = new Module.Category("Render");
      public static final Module.Category MOVEMENT = new Module.Category("Movement");
      public static final Module.Category PLAYER = new Module.Category("Player");
      public static final Module.Category CLIENT = new Module.Category("Client");
      public static final Module.Category HUD = new Module.Category("HUD");

      private Category(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public static Module.Category getCategory(String name) {
         return CATEGORIES.computeIfAbsent(name, Module.Category::new);
      }

      public static Collection<Module.Category> values() {
         return CATEGORIES.values();
      }

      public static boolean isCustomCategory(Module.Category category) {
         Set<String> predefinedCategoryNames = Set.of("Combat", "Misc", "Render", "Movement", "Player", "Client", "HUD");
         return !predefinedCategoryNames.contains(category.getName());
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Module.Category category = (Module.Category)o;
            return Objects.equals(this.name, category.name);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.name);
      }

      static {
         CATEGORIES.put("Combat", COMBAT);
         CATEGORIES.put("Misc", MISC);
         CATEGORIES.put("Render", RENDER);
         CATEGORIES.put("Movement", MOVEMENT);
         CATEGORIES.put("Player", PLAYER);
         CATEGORIES.put("Client", CLIENT);
         CATEGORIES.put("HUD", HUD);
      }
   }
}
