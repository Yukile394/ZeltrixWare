package thunder.hack.features.modules.combat;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.PredictUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.world.ExplosionUtility;

public final class AutoTotem extends Module {
   private final Setting<AutoTotem.Mode> mode = new Setting<>("Mode", AutoTotem.Mode.Matrix);
   private final Setting<AutoTotem.OffHand> offhand = new Setting<>("Item", AutoTotem.OffHand.Totem);
   private final Setting<BooleanSettingGroup> bindSwap = new Setting<>(
      "BindSwap", new BooleanSettingGroup(false), v -> this.offhand.is(AutoTotem.OffHand.Totem)
   );
   private final Setting<Bind> swapButton = new Setting<>("SwapButton", new Bind(280, false, false)).addToGroup(this.bindSwap);
   private final Setting<AutoTotem.Swap> swapMode = new Setting<>("Swap", AutoTotem.Swap.GappleShield).addToGroup(this.bindSwap);
   private final Setting<Boolean> ncpStrict = new Setting<>("NCPStrict", false);
   private final Setting<Float> healthF = new Setting<>("HP", 16.0F, 0.0F, 36.0F);
   private final Setting<Float> healthS = new Setting<>("ShieldGappleHp", 16.0F, 0.0F, 20.0F, v -> this.offhand.getValue() == AutoTotem.OffHand.Shield);
   private final Setting<Boolean> calcAbsorption = new Setting<>("CalcAbsorption", true);
   private final Setting<Boolean> stopMotion = new Setting<>("StopMotion", false);
   private final Setting<Boolean> resetAttackCooldown = new Setting<>("ResetAttackCooldown", false);
   private final Setting<SettingGroup> safety = new Setting<>("Safety", new SettingGroup(false, 0));
   private final Setting<Boolean> hotbarFallBack = new Setting<>("HotbarFallback", false).addToGroup(this.safety);
   private final Setting<Boolean> fallBackCalc = new Setting<>("FallBackCalc", true, v -> this.hotbarFallBack.getValue()).addToGroup(this.safety);
   private final Setting<Boolean> onElytra = new Setting<>("OnElytra", true).addToGroup(this.safety);
   private final Setting<Boolean> onFall = new Setting<>("OnFall", true).addToGroup(this.safety);
   private final Setting<Boolean> onCrystal = new Setting<>("OnCrystal", true).addToGroup(this.safety);
   private final Setting<Boolean> onObsidianPlace = new Setting<>("OnObsidianPlace", false).addToGroup(this.safety);
   private final Setting<Boolean> onCrystalInHand = new Setting<>("OnCrystalInHand", false).addToGroup(this.safety);
   private final Setting<Boolean> onMinecartTnt = new Setting<>("OnMinecartTNT", true).addToGroup(this.safety);
   private final Setting<Boolean> onCreeper = new Setting<>("OnCreeper", true).addToGroup(this.safety);
   private final Setting<Boolean> onAnchor = new Setting<>("OnAnchor", true).addToGroup(this.safety);
   private final Setting<Boolean> onTnt = new Setting<>("OnTNT", true).addToGroup(this.safety);
   public final Setting<AutoTotem.RCGap> rcGap = new Setting<>("RightClickGapple", AutoTotem.RCGap.Off);
   private final Setting<Boolean> crappleSpoof = new Setting<>("CrappleSpoof", true, v -> this.offhand.getValue() == AutoTotem.OffHand.GApple);
   private int delay;
   private Timer bindDelay = new Timer();
   private Item prevItem;

   public AutoTotem() {
      super("AutoTotem", Module.Category.COMBAT);
   }

   @EventHandler
   public void onSync(EventSync e) {
      if (mc.player != null && mc.world != null) {
         this.swapTo(this.getItemSlot());
         if (this.rcGap.not(AutoTotem.RCGap.Off)
            && mc.player.getMainHandStack().getItem() instanceof BowItem
            && mc.options.useKey.isPressed()
            && !mc.player.isUsingItem()) {
            ((IMinecraftClient)mc).idoItemUse();
         }

         this.delay--;
      }
   }

   @EventHandler
   public void onPacketReceive(@NotNull PacketEvent.Receive e) {
      Packet<?> packet = e.getPacket();
      if (packet instanceof EntitySpawnS2CPacket spawn
         && spawn.getEntityType() == EntityType.END_CRYSTAL
         && this.getPlayerPos().squaredDistanceTo(spawn.getX(), spawn.getY(), spawn.getZ()) < 36.0) {
         if (this.hotbarFallBack.getValue()) {
            if (this.fallBackCalc.getValue()
               && ExplosionUtility.getExplosionDamageWPredict(
                     new Vec3d(spawn.getX(), spawn.getY(), spawn.getZ()),
                     mc.player,
                     PredictUtility.createBox(this.getPlayerPos(), mc.player),
                     false
                  )
                  < this.getTriggerHealth() + 4.0F) {
               return;
            }

            this.runInstant();
         }

         if (this.onCrystal.getValue()
            && (double)(
                  this.getTriggerHealth()
                     - ExplosionUtility.getExplosionDamageWPredict(
                        new Vec3d(spawn.getX(), spawn.getY(), spawn.getZ()),
                        mc.player,
                        PredictUtility.createBox(this.getPlayerPos(), mc.player),
                        false
                     )
               )
               < 0.5) {
            int slot = -1;

            for (int i = 9; i < 45; i++) {
               if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem().equals(Items.TOTEM_OF_UNDYING)) {
                  slot = i >= 36 ? i - 36 : i;
                  break;
               }
            }

            this.swapTo(slot);
            this.debug("spawn switch");
         }
      }

      if (packet instanceof BlockUpdateS2CPacket blockUpdate
         && blockUpdate.getState().getBlock() == Blocks.OBSIDIAN
         && this.onObsidianPlace.getValue()
         && this.getPlayerPos().squaredDistanceTo(blockUpdate.getPos().toCenterPos()) < 36.0
         && this.delay <= 0) {
         this.runInstant();
      }
   }

   private float getTriggerHealth() {
      return mc.player.getHealth() + (this.calcAbsorption.getValue() ? mc.player.getAbsorptionAmount() : 0.0F);
   }

   private void runInstant() {
      SearchInvResult hotbarResult = InventoryUtility.findItemInHotBar(Items.TOTEM_OF_UNDYING);
      SearchInvResult invResult = InventoryUtility.findItemInInventory(Items.TOTEM_OF_UNDYING);
      if (hotbarResult.found()) {
         hotbarResult.switchTo();
         this.delay = 20;
      } else if (invResult.found()) {
         int slot = invResult.slot() >= 36 ? invResult.slot() - 36 : invResult.slot();
         if (!this.hotbarFallBack.getValue()) {
            this.swapTo(slot);
         } else {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
         }

         this.delay = 20;
      }
   }

   public void swapTo(int slot) {
      if (mc.player != null && mc.world != null) {
         if (slot != -1 && this.delay <= 0) {
            if (mc.currentScreen instanceof HandledScreen) {
               return;
            }

            if (this.stopMotion.getValue()) {
               mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
            }

            int nearestSlot = findNearestCurrentItem();
            int prevCurrentItem = mc.player.getInventory().selectedSlot;
            if (slot >= 9) {
               switch ((AutoTotem.Mode)this.mode.getValue()) {
                  case Default:
                     if (this.ncpStrict.getValue()) {
                        this.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                     }

                     clickSlot(slot);
                     clickSlot(40);
                     clickSlot(slot);
                     this.sendPacket(
                        new ClickSlotC2SPacket(
                           mc.player.playerScreenHandler.syncId,
                           mc.player.playerScreenHandler.getRevision(),
                           0,
                           0,
                           SlotActionType.PICKUP,
                           mc.player.playerScreenHandler.getCursorStack(),
                           new Int2ObjectOpenHashMap()
                        )
                     );
                     break;
                  case Alternative:
                     if (this.ncpStrict.getValue()) {
                        this.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                     }

                     clickSlot(slot, nearestSlot, SlotActionType.SWAP);
                     clickSlot(40, nearestSlot, SlotActionType.SWAP);
                     clickSlot(slot, nearestSlot, SlotActionType.SWAP);
                     this.sendPacket(
                        new ClickSlotC2SPacket(
                           mc.player.playerScreenHandler.syncId,
                           mc.player.playerScreenHandler.getRevision(),
                           0,
                           0,
                           SlotActionType.PICKUP,
                           mc.player.playerScreenHandler.getCursorStack(),
                           new Int2ObjectOpenHashMap()
                        )
                     );
                     break;
                  case Matrix:
                     if (this.ncpStrict.getValue()) {
                        this.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                     }

                     mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot, nearestSlot, SlotActionType.SWAP, mc.player);
                     this.debug(slot + " " + nearestSlot);
                     this.sendPacket(new UpdateSelectedSlotC2SPacket(nearestSlot));
                     mc.player.getInventory().selectedSlot = nearestSlot;
                     ItemStack itemstack = mc.player.getOffHandStack();
                     mc.player.setStackInHand(Hand.MAIN_HAND, mc.player.getMainHandStack());
                     mc.player.setStackInHand(Hand.OFF_HAND, itemstack);
                     this.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                     this.sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
                     mc.player.getInventory().selectedSlot = prevCurrentItem;
                     mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot, nearestSlot, SlotActionType.SWAP, mc.player);
                     this.sendPacket(
                        new ClickSlotC2SPacket(
                           mc.player.playerScreenHandler.syncId,
                           mc.player.playerScreenHandler.getRevision(),
                           0,
                           0,
                           SlotActionType.PICKUP,
                           mc.player.playerScreenHandler.getCursorStack(),
                           new Int2ObjectOpenHashMap()
                        )
                     );
                     if (this.resetAttackCooldown.getValue()) {
                        mc.player.resetLastAttackedTicks();
                     }
                     break;
                  case MatrixPick:
                     this.debug(slot + " pick");
                     this.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                     this.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                     int prevSlot = mc.player.getInventory().selectedSlot;
                     Managers.ASYNC.run(() -> mc.player.getInventory().selectedSlot = prevSlot, 300L);
                     break;
                  case NewVersion:
                     this.debug(slot + " swap");
                     mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot, 40, SlotActionType.SWAP, mc.player);
                     this.sendPacket(
                        new ClickSlotC2SPacket(
                           mc.player.playerScreenHandler.syncId,
                           mc.player.playerScreenHandler.getRevision(),
                           0,
                           0,
                           SlotActionType.PICKUP,
                           mc.player.playerScreenHandler.getCursorStack(),
                           new Int2ObjectOpenHashMap()
                        )
                     );
               }
            } else {
               this.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
               mc.player.getInventory().selectedSlot = slot;
               this.debug(slot + " select");
               this.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
               this.sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
               mc.player.getInventory().selectedSlot = prevCurrentItem;
               if (this.resetAttackCooldown.getValue()) {
                  mc.player.resetLastAttackedTicks();
               }
            }

            this.delay = (int)(2.0F + (float)Managers.SERVER.getPing() / 25.0F);
         }
      }
   }

   public static int findNearestCurrentItem() {
      int i = mc.player.getInventory().selectedSlot;
      if (i == 8) {
         return 7;
      } else {
         return i == 0 ? 1 : i - 1;
      }
   }

   public int getItemSlot() {
      if (mc.player != null && mc.world != null) {
         SearchInvResult gapple = InventoryUtility.findItemInInventory(Items.GOLDEN_APPLE);
         SearchInvResult crapple = InventoryUtility.findItemInInventory(Items.ENCHANTED_GOLDEN_APPLE);
         SearchInvResult shield = InventoryUtility.findItemInInventory(Items.SHIELD);
         Item offHandItem = mc.player.getOffHandStack().getItem();
         int itemSlot = -1;
         Item item = null;
         switch ((AutoTotem.OffHand)this.offhand.getValue()) {
            case Totem:
               if (offHandItem != Items.TOTEM_OF_UNDYING && !mc.player.getOffHandStack().isEmpty()) {
                  this.prevItem = offHandItem;
               }

               item = this.prevItem;
               if (this.bindSwap.getValue().isEnabled() && this.isKeyPressed(this.swapButton) && this.bindDelay.every(250L)) {
                  switch ((AutoTotem.Swap)this.swapMode.getValue()) {
                     case GappleShield:
                        if (!mc.player.getOffHandStack().isEmpty() && offHandItem != Items.SHIELD) {
                           item = Items.SHIELD;
                           break;
                        }

                        item = Items.ENCHANTED_GOLDEN_APPLE;
                        break;
                     case BallShield:
                        if (!mc.player.getOffHandStack().isEmpty() && offHandItem != Items.SHIELD) {
                           item = Items.SHIELD;
                           break;
                        }

                        item = Items.FIREWORK_ROCKET;
                        break;
                     case GappleBall:
                        if (!mc.player.getOffHandStack().isEmpty() && offHandItem != Items.ENCHANTED_GOLDEN_APPLE) {
                           item = Items.ENCHANTED_GOLDEN_APPLE;
                           break;
                        }

                        item = Items.FIREWORK_ROCKET;
                        break;
                     case BallTotem:
                        if (!mc.player.getOffHandStack().isEmpty() && offHandItem != Items.TOTEM_OF_UNDYING) {
                           item = Items.TOTEM_OF_UNDYING;
                        } else {
                           item = Items.FIREWORK_ROCKET;
                        }
                  }

                  this.prevItem = item;
               }
               break;
            case Crystal:
               item = Items.END_CRYSTAL;
               break;
            case GApple:
               if (this.crappleSpoof.getValue()) {
                  if (mc.player.hasStatusEffect(StatusEffects.REGENERATION) && mc.player.getStatusEffect(StatusEffects.REGENERATION).getDuration() > 2) {
                     if (crapple.found() || offHandItem == Items.ENCHANTED_GOLDEN_APPLE) {
                        item = Items.ENCHANTED_GOLDEN_APPLE;
                     } else if (gapple.found() || offHandItem == Items.GOLDEN_APPLE) {
                        item = Items.GOLDEN_APPLE;
                     }
                  } else if (gapple.found() || offHandItem == Items.GOLDEN_APPLE) {
                     item = Items.GOLDEN_APPLE;
                  } else if (crapple.found() || offHandItem == Items.ENCHANTED_GOLDEN_APPLE) {
                     item = Items.ENCHANTED_GOLDEN_APPLE;
                  }
               } else if (crapple.found() || offHandItem == Items.ENCHANTED_GOLDEN_APPLE) {
                  item = Items.ENCHANTED_GOLDEN_APPLE;
               } else if (gapple.found() || offHandItem == Items.GOLDEN_APPLE) {
                  item = Items.GOLDEN_APPLE;
               }
               break;
            case Shield:
               if (shield.found() || offHandItem == Items.SHIELD) {
                  if (this.getTriggerHealth() <= this.healthS.getValue()) {
                     if (crapple.found() || offHandItem == Items.ENCHANTED_GOLDEN_APPLE) {
                        item = Items.ENCHANTED_GOLDEN_APPLE;
                     } else if (gapple.found() || offHandItem == Items.GOLDEN_APPLE) {
                        item = Items.GOLDEN_APPLE;
                     }
                  } else if (!mc.player.getActiveItem().isOf(Items.SHIELD)) {
                     item = Items.SHIELD;
                  } else if (crapple.found() || offHandItem == Items.ENCHANTED_GOLDEN_APPLE) {
                     item = Items.ENCHANTED_GOLDEN_APPLE;
                  } else if (gapple.found() || offHandItem == Items.GOLDEN_APPLE) {
                     item = Items.GOLDEN_APPLE;
                  }
               } else if (crapple.found() || offHandItem == Items.ENCHANTED_GOLDEN_APPLE) {
                  item = Items.ENCHANTED_GOLDEN_APPLE;
               }
         }

         if (this.getTriggerHealth() <= this.healthF.getValue()
            && (InventoryUtility.findItemInInventory(Items.TOTEM_OF_UNDYING).found() || offHandItem == Items.TOTEM_OF_UNDYING)) {
            item = Items.TOTEM_OF_UNDYING;
         }

         if (!this.rcGap.is(AutoTotem.RCGap.Off)
            && mc.player.getMainHandStack().getItem() instanceof BowItem
            && mc.options.useKey.isPressed()
            && !(offHandItem instanceof PotionItem)
            && (this.rcGap.is(AutoTotem.RCGap.Always) || this.rcGap.is(AutoTotem.RCGap.OnlySafe) && this.getTriggerHealth() > this.healthF.getValue())) {
            if (crapple.found() || offHandItem == Items.ENCHANTED_GOLDEN_APPLE) {
               item = Items.ENCHANTED_GOLDEN_APPLE;
            }

            if (gapple.found() || offHandItem == Items.GOLDEN_APPLE) {
               item = Items.GOLDEN_APPLE;
            }
         }

         if (this.onFall.getValue() && (double)(this.getTriggerHealth() - ((mc.player.fallDistance - 3.0F) / 2.0F + 3.5F)) < 0.5) {
            item = Items.TOTEM_OF_UNDYING;
         }

         if (this.onElytra.getValue() && mc.player.isFallFlying()) {
            item = Items.TOTEM_OF_UNDYING;
         }

         if (this.onCrystalInHand.getValue()) {
            for (PlayerEntity pl : Managers.ASYNC.getAsyncPlayers()) {
               if (!Managers.FRIEND.isFriend(pl)
                  && pl != mc.player
                  && this.getPlayerPos().squaredDistanceTo(pl.getPos()) < 36.0
                  && (
                     pl.getMainHandStack().getItem() == Items.END_CRYSTAL
                        || pl.getMainHandStack().getItem() == Items.END_CRYSTAL
                        || pl.getOffHandStack().getItem() == Items.END_CRYSTAL
                        || pl.getOffHandStack().getItem() == Items.END_CRYSTAL
                  )) {
                  item = Items.TOTEM_OF_UNDYING;
               }
            }
         }

         for (Entity entity : mc.world.getEntities()) {
            if (entity != null && entity.isAlive() && !(this.getPlayerPos().squaredDistanceTo(entity.getPos()) > 36.0)) {
               if (this.onCrystal.getValue()
                  && entity instanceof EndCrystalEntity
                  && (double)(
                        this.getTriggerHealth()
                           - ExplosionUtility.getExplosionDamageWPredict(
                              entity.getPos(), mc.player, PredictUtility.createBox(this.getPlayerPos(), mc.player), false
                           )
                     )
                     < 0.5) {
                  item = Items.TOTEM_OF_UNDYING;
                  break;
               }

               if (this.onTnt.getValue() && entity instanceof TntEntity) {
                  item = Items.TOTEM_OF_UNDYING;
                  break;
               }

               if (this.onMinecartTnt.getValue() && entity instanceof TntMinecartEntity) {
                  item = Items.TOTEM_OF_UNDYING;
                  break;
               }

               if (this.onCreeper.getValue() && entity instanceof CreeperEntity) {
                  item = Items.TOTEM_OF_UNDYING;
                  break;
               }
            }
         }

         if (this.onAnchor.getValue()) {
            for (int x = -6; x <= 6; x++) {
               for (int y = -6; y <= 6; y++) {
                  for (int z = -6; z <= 6; z++) {
                     BlockPos bp = new BlockPos(x, y, z);
                     if (mc.world.getBlockState(bp).getBlock() == Blocks.RESPAWN_ANCHOR) {
                        item = Items.TOTEM_OF_UNDYING;
                        break;
                     }
                  }
               }
            }
         }

         for (int i = 9; i < 45; i++) {
            if (mc.player.getOffHandStack().getItem() == item) {
               return -1;
            }

            if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem().equals(item)) {
               itemSlot = i >= 36 ? i - 36 : i;
               break;
            }
         }

         return item == mc.player.getMainHandStack().getItem() && mc.options.useKey.isPressed() ? -1 : itemSlot;
      } else {
         return -1;
      }
   }

   private Vec3d getPlayerPos() {
      return mc.player.getPos();
   }

   private static enum Mode {
      Default,
      Alternative,
      Matrix,
      MatrixPick,
      NewVersion;

      private static AutoTotem.Mode[] $values() {
         return new AutoTotem.Mode[]{Default, Alternative, Matrix, MatrixPick, NewVersion};
      }
   }

   private static enum OffHand {
      Totem,
      Crystal,
      GApple,
      Shield;

      private static AutoTotem.OffHand[] $values() {
         return new AutoTotem.OffHand[]{Totem, Crystal, GApple, Shield};
      }
   }

   public static enum RCGap {
      Off,
      Always,
      OnlySafe;

      private static AutoTotem.RCGap[] $values() {
         return new AutoTotem.RCGap[]{Off, Always, OnlySafe};
      }
   }

   private static enum Swap {
      GappleShield,
      BallShield,
      GappleBall,
      BallTotem;

      private static AutoTotem.Swap[] $values() {
         return new AutoTotem.Swap[]{GappleShield, BallShield, GappleBall, BallTotem};
      }
   }
}
