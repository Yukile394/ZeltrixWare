package thunder.hack.features.modules.player;

import net.minecraft.util.Hand;
import net.minecraft.entity.LivingEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

public class ElytraHelper extends Module {
   private static ElytraHelper INSTANCE = new ElytraHelper();
   private final Setting<Bind> switchButton = new Setting<>("SwitchButton", new Bind(-1, false, false));
   private final Setting<Bind> fireWorkButton = new Setting<>("FireWorkButton", new Bind(-1, false, false));
   private final Setting<ElytraHelper.FireWorkMode> fireWorkMode;
   private final Setting<Boolean> autoFirework;
   private final Setting<Bind> legitFireworkKey;
   private final Setting<Boolean> slotReturn;
   private final Setting<Integer> delay;
   private final Setting<Boolean> delayBypass;
   private final Setting<Float> overshoot;
   private final Setting<Boolean> swing;
   private final Setting<Boolean> fakeRotation;
   private final Setting<Boolean> predict;
   private final Setting<Integer> predictTicks;
   private final Setting<Integer> predictLength;
   private final Setting<Float> driveBoost;
   private final Timer switchTimer;
   private final Timer fireworkTimer;
   private final Timer returnTimer;
   private int returnSlot;
   private int returnInvSlot;
   public static boolean swapping = false;
   public static boolean hasUsedFirework = false;
   public static long lastElytraAttackTime = 0L;
   private int bypassDelayOffset = 0;
   private int fakeRotState = 0;
   private int predictTimer = 0;
   private double currentOverrun = 0.0;

   public static ElytraHelper getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ElytraHelper();
      }

      return INSTANCE;
   }

   public ElytraHelper() {
      super("ElytraHelper", Module.Category.PLAYER);
      this.fireWorkMode = new Setting<>("FireWorkMode", ElytraHelper.FireWorkMode.Normal);
      this.autoFirework = new Setting<>("AutoFirework", false);
      this.legitFireworkKey = new Setting<>("Legit Key", new Bind(-1, false, false), v -> this.fireWorkMode.getValue() == ElytraHelper.FireWorkMode.Legit);
      this.slotReturn = new Setting<>("SlotReturn", true, v -> this.fireWorkMode.getValue() == ElytraHelper.FireWorkMode.Normal);
      this.delay = new Setting<>(
         "Delay",
         100,
         1,
         1000,
         v -> this.fireWorkMode.getValue() == ElytraHelper.FireWorkMode.Normal && this.slotReturn.getValue() || this.autoFirework.getValue()
      );
      this.delayBypass = new Setting<>(
         "Delay-Bypass",
         false,
         v -> this.fireWorkMode.getValue() == ElytraHelper.FireWorkMode.Normal && this.slotReturn.getValue() || this.autoFirework.getValue()
      );
      this.overshoot = new Setting<>("Overshoot", 3.5F, 3.0F, 15.0F, v -> this.autoFirework.getValue());
      this.swing = new Setting<>("Swing", true);
      this.fakeRotation = new Setting<>("Fake Rotation", false);
      this.predict = new Setting<>("Predict", false, v -> this.autoFirework.getValue());
      this.predictTicks = new Setting<>("Predict Ticks", 2, 1, 5, v -> this.autoFirework.getValue() && this.predict.getValue());
      this.predictLength = new Setting<>("Predict Length", 2, 1, 20, v -> this.autoFirework.getValue() && this.predict.getValue());
      this.driveBoost = new Setting<>("Drive Boost", 1.5F, 0.1F, 5.0F, v -> this.autoFirework.getValue() && this.predict.getValue());
      this.switchTimer = new Timer();
      this.fireworkTimer = new Timer();
      this.returnTimer = new Timer();
      this.returnSlot = -1;
      this.returnInvSlot = -1;
      INSTANCE = this;
   }

   @Override
   public void onUpdate() {
      if (mc.player != null && mc.world != null) {
         if (mc.currentScreen == null) {
            if (this.switchButton.getValue().getKey() != -1 && this.isKeyPressed(this.switchButton.getValue().getKey())) {
               this.swapChest(false);
            }

            if (this.fireWorkButton.getValue().getKey() != -1 && this.isKeyPressed(this.fireWorkButton.getValue().getKey()) && mc.player.isFallFlying()) {
               this.useFireWork();
            }

            if (this.legitFireworkKey.getValue().getKey() != -1 && this.isKeyPressed(this.legitFireworkKey.getValue().getKey()) && mc.player.isFallFlying()) {
               this.useFireWork();
            }
         }

         if (!mc.player.isFallFlying()) {
            hasUsedFirework = false;
         }

         if (this.autoFirework.getValue() && mc.player.isFallFlying() && mc.currentScreen == null) {
            float tps = Managers.SERVER.getTPS();
            float tpsFactor = 20.0F / (tps <= 0.0F ? 20.0F : tps);
            long baseDelay = (long)((float)this.delay.getValue().intValue() * tpsFactor);
            long syncedDelay = this.delayBypass.getValue() ? baseDelay + (long)this.bypassDelayOffset : baseDelay;
            boolean attackCooldownOk = true;
            Vec3d vel = mc.player.getVelocity();
            double horizSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            if (this.predict.getValue()) {
               this.predictTimer++;
               if (this.predictTimer >= this.predictLength.getValue()) {
                  this.predictTimer = 0;
                  this.currentOverrun = this.calculateOverrun(this.overshoot.getValue(), this.driveBoost.getValue());
               }

               double prevSpeed = Math.sqrt(
                  Math.pow(mc.player.getX() - mc.player.prevX, 2.0)
                     + Math.pow(mc.player.getZ() - mc.player.prevZ, 2.0)
               );
               double accel = horizSpeed - prevSpeed;
               if (accel < 0.0) {
                  horizSpeed += accel * (double)this.predictTicks.getValue().intValue();
               }
            } else {
               this.predictTimer = 0;
               this.currentOverrun = (double)this.overshoot.getValue().floatValue();
            }

            boolean overshootOk = horizSpeed <= this.currentOverrun;
            boolean skipFirework = false;
            if (attackCooldownOk && overshootOk && !skipFirework && this.fireworkTimer.passedMs(syncedDelay)) {
               this.useFireWork();
               this.fireworkTimer.reset();
            }
         }

         if (this.returnSlot != -1
            && this.returnTimer.passedMs((long)this.delay.getValue().intValue() + (long)(this.delayBypass.getValue() ? this.bypassDelayOffset : 0))) {
            if (this.returnInvSlot != -1) {
               mc.interactionManager
                  .clickSlot(
                     mc.player.playerScreenHandler.syncId, this.returnInvSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player
                  );
               this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
               this.returnInvSlot = -1;
            }

            this.returnSlot = -1;
         }
      }
   }

   private void advanceBypassState() {
      if (this.delayBypass.getValue()) {
         this.bypassDelayOffset++;
         if (this.bypassDelayOffset > 100) {
            this.bypassDelayOffset = 0;
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      hasUsedFirework = false;
      this.predictTimer = 0;
      this.currentOverrun = 0.0;
   }

   public void useFireWork() {
      hasUsedFirework = true;
      SearchInvResult fireWorkResult = InventoryUtility.findItemInInventory(Items.FIREWORK_ROCKET);
      if (!fireWorkResult.found()) {
         this.sendMessage(
            ClientSettings.isRu()
               ? "\u0423 \u0442\u0435\u0431\u044f \u043d\u0435\u0442 \u0444\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u043e\u0432!"
               : "You've got no fireworks!"
         );
      } else {
         float oldYaw = mc.player.getYaw();
         float oldPitch = mc.player.getPitch();
         boolean applyFakeRot = this.fakeRotation.getValue() && !this.swing.getValue();
         if (applyFakeRot) {
            float fireYaw = oldYaw;
            float firePitch = oldPitch;
            switch (this.fakeRotState) {
               case 0:
                  fireYaw = oldYaw - 25.0F;
                  break;
               case 1:
                  fireYaw = oldYaw + 25.0F;
                  break;
               case 2:
                  fireYaw = oldYaw;
                  break;
               case 3:
                  fireYaw = oldYaw - 25.0F;
                  firePitch = oldPitch - 20.0F;
                  break;
               case 4:
                  fireYaw = oldYaw + 25.0F;
                  firePitch = oldPitch - 20.0F;
            }

            this.fakeRotState = (this.fakeRotState + 1) % 5;
            mc.player.setYaw(fireYaw);
            mc.player.setPitch(firePitch);
         }

         if (this.fireWorkMode.getValue() == ElytraHelper.FireWorkMode.HvH) {
            if (mc.player.getOffHandStack().getItem() == Items.FIREWORK_ROCKET) {
               InteractionUtility.sendSequencedPacket(
                  id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, mc.player.getYaw(), mc.player.getPitch())
               );
               if (this.swing.getValue()) {
                  mc.player.swingHand(Hand.OFF_HAND);
               }
            } else {
               SearchInvResult hotbarFireWorkResult = InventoryUtility.findItemInHotBar(Items.FIREWORK_ROCKET);
               if (hotbarFireWorkResult.found()) {
                  int currentSlot = mc.player.getInventory().selectedSlot;
                  int fireworkSlot = hotbarFireWorkResult.slot();
                  mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(fireworkSlot));
                  InteractionUtility.sendSequencedPacket(
                     id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch())
                  );
                  if (this.swing.getValue()) {
                     mc.player.swingHand(Hand.MAIN_HAND);
                  }

                  mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(currentSlot));
               } else {
                  int epSlot = fireWorkResult.slot();
                  mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, epSlot, 40, SlotActionType.SWAP, mc.player);
                  InteractionUtility.sendSequencedPacket(
                     id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, mc.player.getYaw(), mc.player.getPitch())
                  );
                  if (this.swing.getValue()) {
                     mc.player.swingHand(Hand.OFF_HAND);
                  }

                  mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, epSlot, 40, SlotActionType.SWAP, mc.player);
               }
            }
         } else if (this.fireWorkMode.getValue() == ElytraHelper.FireWorkMode.Legit) {
            int currentSlot = mc.player.getInventory().selectedSlot;
            int fireworkSlot = fireWorkResult.slot();
            if (fireworkSlot == currentSlot + 36) {
               mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
               if (this.swing.getValue()) {
                  mc.player.swingHand(Hand.MAIN_HAND);
               }
            } else {
               mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, fireworkSlot, currentSlot, SlotActionType.SWAP, mc.player);
               InteractionUtility.sendSequencedPacket(
                  id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch())
               );
               if (this.swing.getValue()) {
                  mc.player.swingHand(Hand.MAIN_HAND);
               }

               mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, fireworkSlot, currentSlot, SlotActionType.SWAP, mc.player);
            }
         } else {
            SearchInvResult hotbarFireWorkResult = InventoryUtility.findItemInHotBar(Items.FIREWORK_ROCKET);
            if (this.fireWorkMode.getValue() == ElytraHelper.FireWorkMode.Normal && this.slotReturn.getValue()) {
               this.returnSlot = mc.player.getInventory().selectedSlot;
               this.returnInvSlot = hotbarFireWorkResult.found() ? -1 : (fireWorkResult.found() ? fireWorkResult.slot() : -1);
               this.returnTimer.reset();
            }

            InventoryUtility.saveSlot();
            if (hotbarFireWorkResult.found()) {
               hotbarFireWorkResult.switchTo();
            } else {
               mc.interactionManager
                  .clickSlot(
                     mc.player.playerScreenHandler.syncId, fireWorkResult.slot(), mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player
                  );
               this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
            }

            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            if (this.swing.getValue()) {
               mc.player.swingHand(Hand.MAIN_HAND);
            }

            if (this.fireWorkMode.getValue() == ElytraHelper.FireWorkMode.Silent) {
               InventoryUtility.returnSlot();
               if (!hotbarFireWorkResult.found()) {
                  mc.interactionManager
                     .clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        fireWorkResult.slot(),
                        mc.player.getInventory().selectedSlot,
                        SlotActionType.SWAP,
                        mc.player
                     );
                  this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
               }
            }
         }

         if (applyFakeRot) {
            mc.player.setYaw(oldYaw);
            mc.player.setPitch(oldPitch);
         }

         if (this.swing.getValue()) {
            this.tryAuraAttackOnFirework();
         }

         this.advanceBypassState();
      }
   }

   private void tryAuraAttackOnFirework() {
      try {
         Aura aura = ModuleManager.aura;
         if (aura == null || !aura.isEnabled()) {
            return;
         }

         if (Aura.target == null) {
            return;
         }

         if (!(Aura.target instanceof LivingEntity)) {
            return;
         }

         float dist = mc.player.distanceTo(Aura.target);
         float range = aura.elytra.getValue() ? aura.elytraAttackRange.getValue() : aura.attackRange.getValue();
         if (dist > range) {
            return;
         }

         boolean[] state = aura.preAttack();
         aura.attack();
         aura.postAttack(state[0], state[1]);
      } catch (Exception var5) {
      }
   }

   private double calculateOverrun(float overshootVal, float driveBoostVal) {
      return (double)(overshootVal * driveBoostVal);
   }

   public static int getChestPlateSlot() {
      for (Item item : new Item[]{
         Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.IRON_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.LEATHER_CHESTPLATE
      }) {
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

         clickSlot(slot);
         clickSlot(6);
         clickSlot(slot);
         this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
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

         clickSlot(result.slot());
         clickSlot(6);
         clickSlot(result.slot());
         this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
      }

      if (disable) {
         this.disable(
            ClientSettings.isRu() ? "\u0421\u0432\u0430\u043f\u043d\u0443\u043b! \u041e\u0442\u043a\u043b\u044e\u0447\u0430\u044e.." : "Swapped! Disabling.."
         );
      }
   }

   public String getFireWorkMode() {
      return this.fireWorkMode.getValue().name();
   }

   public boolean isSwingEnabled() {
      return this.swing.getValue();
   }

   public static enum FireWorkMode {
      Normal,
      Silent,
      Legit,
      HvH;

      private static ElytraHelper.FireWorkMode[] $values() {
         return new ElytraHelper.FireWorkMode[]{Normal, Silent, Legit, HvH};
      }
   }
}
