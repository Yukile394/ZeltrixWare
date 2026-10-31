package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.DataComponentTypes;
import thunder.hack.events.impl.EventPostSync;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.utility.Timer;

public final class AutoPotion extends Module {
   private final Setting<Boolean> strength = new Setting<>("Strength", true);
   private final Setting<Boolean> speed = new Setting<>("Speed", true);
   private final Setting<Boolean> fire = new Setting<>("FireResistance", true);
   private final Setting<BooleanSettingGroup> heal = new Setting<>("InstantHealing", new BooleanSettingGroup(true));
   private final Setting<Integer> healthH = new Setting<>("Health", 8, 0, 20).addToGroup(this.heal);
   private final Setting<BooleanSettingGroup> regen = new Setting<>("Regeneration", new BooleanSettingGroup(true));
   private final Setting<AutoPotion.TriggerOn> triggerOn = new Setting<>("Trigger", AutoPotion.TriggerOn.LackOfRegen).addToGroup(this.regen);
   private final Setting<Integer> healthR = new Setting<>("HP", 8, 0, 20, v -> this.triggerOn.is(AutoPotion.TriggerOn.Health)).addToGroup(this.regen);
   public Timer timer = new Timer();

   public AutoPotion() {
      super("AutoPotion", Module.Category.COMBAT);
   }

   public static int getPotionSlot(AutoPotion.Potions potion) {
      for (int i = 0; i < 9; i++) {
         if (isStackPotion(mc.player.getInventory().getStack(i), potion)) {
            return i;
         }
      }

      return -1;
   }

   public static boolean isPotionOnHotBar(AutoPotion.Potions potions) {
      return getPotionSlot(potions) != -1;
   }

   public static boolean isStackPotion(ItemStack stack, AutoPotion.Potions potion) {
      if (stack == null) {
         return false;
      } else {
         if (stack.getItem() instanceof SplashPotionItem) {
            PotionContentsComponent potionContentsComponent = (PotionContentsComponent)stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            RegistryEntry<StatusEffect> id = null;
            switch (potion) {
               case STRENGTH:
                  id = StatusEffects.STRENGTH;
                  break;
               case SPEED:
                  id = StatusEffects.SPEED;
                  break;
               case FIRERES:
                  id = StatusEffects.FIRE_RESISTANCE;
                  break;
               case HEAL:
                  id = StatusEffects.INSTANT_HEALTH;
                  break;
               case REGEN:
                  id = StatusEffects.REGENERATION;
            }

            for (StatusEffectInstance effect : potionContentsComponent.getEffects()) {
               if (effect.getEffectType() == id) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean shouldThrow() {
      return !mc.player.hasStatusEffect(StatusEffects.SPEED) && isPotionOnHotBar(AutoPotion.Potions.SPEED) && this.speed.getValue()
         || !mc.player.hasStatusEffect(StatusEffects.STRENGTH) && isPotionOnHotBar(AutoPotion.Potions.STRENGTH) && this.strength.getValue()
         || !mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && isPotionOnHotBar(AutoPotion.Potions.FIRERES) && this.fire.getValue()
         || mc.player.getHealth() + mc.player.getAbsorptionAmount() < (float)this.healthH.getValue().intValue()
            && isPotionOnHotBar(AutoPotion.Potions.HEAL)
            && this.heal.getValue().isEnabled()
         || !mc.player.hasStatusEffect(StatusEffects.REGENERATION)
            && this.triggerOn.is(AutoPotion.TriggerOn.LackOfRegen)
            && isPotionOnHotBar(AutoPotion.Potions.REGEN)
            && this.regen.getValue().isEnabled()
         || mc.player.getHealth() + mc.player.getAbsorptionAmount() < (float)this.healthR.getValue().intValue()
            && this.triggerOn.is(AutoPotion.TriggerOn.Health)
            && isPotionOnHotBar(AutoPotion.Potions.REGEN)
            && this.regen.getValue().isEnabled();
   }

   @EventHandler
   public void onPostSync(EventPostSync e) {
      if (mc.player.isOnGround() && mc.player.age > 80 && this.shouldThrow() && this.timer.passedMs(250L)) {
         if (!mc.player.hasStatusEffect(StatusEffects.SPEED) && isPotionOnHotBar(AutoPotion.Potions.SPEED) && this.speed.getValue()) {
            this.throwPotion(AutoPotion.Potions.SPEED);
         }

         if (!mc.player.hasStatusEffect(StatusEffects.STRENGTH) && isPotionOnHotBar(AutoPotion.Potions.STRENGTH) && this.strength.getValue()) {
            this.throwPotion(AutoPotion.Potions.STRENGTH);
         }

         if (!mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && isPotionOnHotBar(AutoPotion.Potions.FIRERES) && this.fire.getValue()) {
            this.throwPotion(AutoPotion.Potions.FIRERES);
         }

         if (mc.player.getHealth() + mc.player.getAbsorptionAmount() < (float)this.healthH.getValue().intValue()
            && this.heal.getValue().isEnabled()
            && isPotionOnHotBar(AutoPotion.Potions.HEAL)) {
            this.throwPotion(AutoPotion.Potions.HEAL);
         }

         if ((
               !mc.player.hasStatusEffect(StatusEffects.REGENERATION) && this.triggerOn.is(AutoPotion.TriggerOn.LackOfRegen)
                  || mc.player.getHealth() + mc.player.getAbsorptionAmount() < (float)this.healthR.getValue().intValue()
                     && this.triggerOn.is(AutoPotion.TriggerOn.Health)
            )
            && isPotionOnHotBar(AutoPotion.Potions.REGEN)
            && this.regen.getValue().isEnabled()) {
            this.throwPotion(AutoPotion.Potions.REGEN);
         }

         this.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
         this.timer.reset();
      }
   }

   public void throwPotion(AutoPotion.Potions potion) {
      int slot = getPotionSlot(potion);
      if (slot != -1) {
         this.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
         float yaw = mc.player.getYaw() + 180.0F;
         float pitch = 50.0F;
         this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, yaw, pitch));
      }
   }

   public static enum Potions {
      STRENGTH,
      SPEED,
      FIRERES,
      HEAL,
      REGEN;

      private static AutoPotion.Potions[] $values() {
         return new AutoPotion.Potions[]{STRENGTH, SPEED, FIRERES, HEAL, REGEN};
      }
   }

   public static enum TriggerOn {
      LackOfRegen,
      Health;

      private static AutoPotion.TriggerOn[] $values() {
         return new AutoPotion.TriggerOn[]{LackOfRegen, Health};
      }
   }
}
