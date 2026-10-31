package thunder.hack.features.modules.misc;

import java.util.ArrayList;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventEntityRemoved;
import thunder.hack.events.impl.EventEntitySpawn;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;

public class VisualRange extends Module {
   private static final ArrayList<String> entities = new ArrayList<>();
   private final Setting<Boolean> leave = new Setting<>("Leave", true);
   private final Setting<Boolean> enter = new Setting<>("Enter", true);
   private final Setting<Boolean> friends = new Setting<>("Friends", true);
   private final Setting<Boolean> soundpl = new Setting<>("Sound", true);
   private final Setting<VisualRange.Mode> mode = new Setting<>("Mode", VisualRange.Mode.Notification);

   public VisualRange() {
      super("VisualRange", Module.Category.MISC);
   }

   @EventHandler
   public void onEntityAdded(EventEntitySpawn event) {
      if (this.isValid(event.getEntity())) {
         if (!entities.contains(event.getEntity().getName().getString())) {
            entities.add(event.getEntity().getName().getString());
            if (this.enter.getValue()) {
               this.notify(event.getEntity(), true);
            }
         }
      }
   }

   @EventHandler
   public void onEntityRemoved(EventEntityRemoved event) {
      if (this.isValid(event.entity)) {
         if (entities.contains(event.entity.getName().getString())) {
            entities.remove(event.entity.getName().getString());
            if (this.leave.getValue()) {
               this.notify(event.entity, false);
            }
         }
      }
   }

   public void notify(Entity entity, boolean enter) {
      String message = "";
      if (ModuleManager.nameProtect.isEnabled() && NameProtect.hideFriends.getValue()) {
         message = Formatting.AQUA + NameProtect.newName.getValue();
      }

      if (Managers.FRIEND.isFriend(entity.getName().getString())) {
         message = Formatting.AQUA + entity.getName().getString();
      } else {
         message = Formatting.GRAY + entity.getName().getString();
      }

      if (enter) {
         message = message + Formatting.GREEN + " was found!";
      } else {
         message = message + Formatting.RED + " left to X:" + (int)entity.getX() + " Z:" + (int)entity.getZ();
      }

      if (this.mode.is(VisualRange.Mode.Chat) || this.mode.is(VisualRange.Mode.Both)) {
         this.sendMessage(message);
      }

      if (this.mode.is(VisualRange.Mode.Notification) || this.mode.is(VisualRange.Mode.Both)) {
         Managers.NOTIFICATION.publicity("VisualRange", message, 2, Notification.Type.WARNING);
      }

      if (this.soundpl.getValue()) {
         try {
            if (enter) {
               mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            } else {
               mc.world.playSound(mc.player, mc.player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
         } catch (Exception var5) {
         }
      }
   }

   public boolean isValid(Entity entity) {
      return !(entity instanceof PlayerEntity)
         ? false
         : entity != mc.player && (!Managers.FRIEND.isFriend(entity.getName().getString()) || this.friends.getValue());
   }

   public static enum Mode {
      Chat,
      Notification,
      Both;
   }
}
