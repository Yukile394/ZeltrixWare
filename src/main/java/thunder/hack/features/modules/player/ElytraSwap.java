package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

public class ElytraSwap extends Module {
   private final Setting<Boolean> delay = new Setting<>("Delay", false);
   private final Setting<ElytraSwap.Mode> mode = new Setting<>("Mode", ElytraSwap.Mode.Enable);
   private final Setting<Bind> switchButton = new Setting<>("SwitchButton", new Bind(-1, false, false), v -> this.mode.getValue() == ElytraSwap.Mode.Bind);
   private final Setting<Bind> fireWorkButton = new Setting<>("FireWorkButton", new Bind(-1, false, false), v -> this.mode.getValue() == ElytraSwap.Mode.Bind);
   private final Setting<Boolean> startFireWork = new Setting<>("StartFireWork", true, v -> this.mode.getValue() == ElytraSwap.Mode.Bind);
   private final Setting<ElytraSwap.FireWorkMode> fireWorkMode = new Setting<>(
      "FireWorkMode", ElytraSwap.FireWorkMode.Normal, v -> this.mode.getValue() == ElytraSwap.Mode.Bind
   );
   private final Timer switchTimer = new Timer();
   private final Timer fireworkTimer = new Timer();
   public static boolean swapping = false;

   public ElytraSwap() {
      super("ElytraSwap", Module.Category.PLAYER);
   }

   @Override
   public void onEnable() {
      if (this.mode.getValue() == ElytraSwap.Mode.Enable) {
         this.swapChest(true);
      }
   }

   @Override
   public void onUpdate() {
      if (this.mode.getValue() == ElytraSwap.Mode.Bind && mc.currentScreen == null) {
         if (this.switchButton.getValue().getKey() != -1 && this.isKeyPressed(this.switchButton.getValue().getKey()) && this.switchTimer.every(500L)) {
            this.swapChest(false);
         }

         if (this.fireWorkButton.getValue().getKey() != -1
            && this.isKeyPressed(this.fireWorkButton.getValue().getKey())
            && this.fireworkTimer.every(500L)
            && mc.player.isFallFlying()) {
            this.useFireWork();
         }
      }
   }

   @EventHandler
   public void onPacketSend(PacketEvent.SendPost e) {
      if (e.getPacket() instanceof ClientCommandC2SPacket command
         && command.getMode() == Mode.START_FALL_FLYING
         && this.mode.getValue() == ElytraSwap.Mode.Bind
         && this.startFireWork.getValue()) {
         this.useFireWork();
      }
   }

   public void useFireWork() {
      SearchInvResult hotbarFireWorkResult = InventoryUtility.findItemInHotBar(Items.FIREWORK_ROCKET);
      SearchInvResult fireWorkResult = InventoryUtility.findItemInInventory(Items.FIREWORK_ROCKET);
      InventoryUtility.saveSlot();
      if (hotbarFireWorkResult.found()) {
         hotbarFireWorkResult.switchTo();
      } else {
         if (!fireWorkResult.found()) {
            this.sendMessage(
               ClientSettings.isRu()
                  ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u0444\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u043e\u0432!"
                  : "You've got no fireworks!"
            );
            return;
         }

         mc.interactionManager
            .clickSlot(
               mc.player.currentScreenHandler.syncId, fireWorkResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.PICKUP, mc.player
            );
         this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
      }

      this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
      this.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
      if (this.fireWorkMode.getValue() == ElytraSwap.FireWorkMode.Silent) {
         InventoryUtility.returnSlot();
         if (!hotbarFireWorkResult.found()) {
            mc.interactionManager
               .clickSlot(
                  mc.player.currentScreenHandler.syncId, fireWorkResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.PICKUP, mc.player
               );
            this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
         }
      }
   }

   public static int getChestPlateSlot() {
      Item[] items = new Item[]{
         Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE
      };

      for (Item item : items) {
         SearchInvResult slot = InventoryUtility.findItemInInventory(item);
         if (slot.found()) {
            return slot.slot();
         }
      }

      return -1;
   }

   private void swapChest(boolean disable) {
      SearchInvResult result = InventoryUtility.findItemInInventory(Items.ELYTRA);
      if (mc.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
         int slot = getChestPlateSlot();
         if (slot == -1) {
            if (disable) {
               this.disable(
                  ClientSettings.isRu()
                     ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a\u0430!"
                     : "You don't have a chestplate!"
               );
            } else {
               this.sendMessage(
                  ClientSettings.isRu()
                     ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a\u0430!"
                     : "You don't have a chestplate!"
               );
            }

            return;
         }

         if (this.delay.getValue()) {
            Managers.ASYNC.run(() -> {
               swapping = true;
               clickSlot(slot);

               try {
                  Thread.sleep(200L);
               } catch (Exception var4) {
               }

               clickSlot(6);

               try {
                  Thread.sleep(200L);
               } catch (Exception var3x) {
               }

               clickSlot(slot);
               this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
               swapping = false;
            });
         } else {
            clickSlot(slot);
            clickSlot(6);
            clickSlot(slot);
            this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
         }
      } else {
         if (!result.found()) {
            if (disable) {
               this.disable(
                  ClientSettings.isRu()
                     ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u044d\u043b\u0438\u0442\u0440\u044b!"
                     : "You don't have an elytra!"
               );
            } else {
               this.sendMessage(
                  ClientSettings.isRu()
                     ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u044d\u043b\u0438\u0442\u0440\u044b!"
                     : "You don't have an elytra!"
               );
            }

            return;
         }

         if (this.delay.getValue()) {
            new Thread(() -> {
               swapping = true;
               clickSlot(result.slot());

               try {
                  Thread.sleep(200L);
               } catch (Exception var4) {
               }

               clickSlot(6);

               try {
                  Thread.sleep(200L);
               } catch (Exception var3x) {
               }

               clickSlot(result.slot());
               this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
               if (this.startFireWork.getValue() && mc.player.fallDistance > 0.0F) {
                  this.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
               }

               swapping = false;
            }).start();
         } else {
            clickSlot(result.slot());
            clickSlot(6);
            clickSlot(result.slot());
            this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            if (this.startFireWork.getValue() && mc.player.fallDistance > 0.0F) {
               this.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
            }
         }
      }

      if (disable) {
         this.disable(
            ClientSettings.isRu() ? "\u0421\u0432\u0430\u043f\u043d\u0443\u043b! \u041e\u0442\u043a\u043b\u044e\u0447\u0430\u044e.." : "Swapped! Disabling.."
         );
      }
   }

   private static enum FireWorkMode {
      Silent,
      Normal;
   }

   private static enum Mode {
      Enable,
      Bind;
   }
}
