package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import thunder.hack.ThunderHack;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.EventTravel;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.ILivingEntity;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;

public class ElytraRecast extends Module {
   public Setting<ElytraRecast.Exploit> exploit = new Setting<>("Exploit", ElytraRecast.Exploit.None);
   public Setting<Boolean> changePitch = new Setting<>("ChangePitch", true);
   public Setting<Float> pitchValue = new Setting<>("PitchValue", 55.0F, -90.0F, 90.0F, v -> this.changePitch.getValue());
   public Setting<Boolean> autoWalk = new Setting<>("AutoWalk", true);
   public Setting<Boolean> autoJump = new Setting<>("AutoJump", true);
   public Setting<Boolean> allowBroken = new Setting<>("AllowBroken", true);
   private float prevClientPitch;
   private float prevClientYaw;
   private float jitter;

   public ElytraRecast() {
      super("ElytraRecast", Module.Category.MOVEMENT);
   }

   @EventHandler
   public void onSync(EventSync e) {
      if (this.changePitch.getValue()) {
         mc.player.setPitch(this.pitchValue.getValue());
      }

      switch ((ElytraRecast.Exploit)this.exploit.getValue()) {
         case None:
         default:
            break;
         case Strict:
            mc.player.setYaw(mc.player.getYaw() + this.jitter);
            break;
         case Strong:
            mc.player.setPitch(this.pitchValue.getValue() - Math.abs(this.jitter / 2.0F));
      }
   }

   @EventHandler
   public void modifyVelocity(EventTravel e) {
      if (this.changePitch.getValue()) {
         if (e.isPre()) {
            this.prevClientPitch = mc.player.getPitch();
            this.prevClientYaw = mc.player.getYaw();
            mc.player.setPitch(this.pitchValue.getValue());
            switch ((ElytraRecast.Exploit)this.exploit.getValue()) {
               case None:
               default:
                  break;
               case Strict:
                  mc.player.setYaw(mc.player.getYaw() + this.jitter);
                  break;
               case Strong:
                  mc.player.setPitch(this.pitchValue.getValue() - Math.abs(this.jitter / 2.0F));
            }
         } else {
            mc.player.setPitch(this.prevClientPitch);
            if (this.exploit.getValue() == ElytraRecast.Exploit.Strict) {
               mc.player.setYaw(this.prevClientYaw);
            }
         }
      }
   }

   @Override
   public void onDisable() {
      if (!InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode())) {
         mc.options.forwardKey.setPressed(false);
      }

      if (!InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode())) {
         mc.options.jumpKey.setPressed(false);
      }
   }

   @Override
   public void onUpdate() {
      if (this.autoJump.getValue()) {
         mc.options.jumpKey.setPressed(true);
      }

      if (this.autoWalk.getValue()) {
         mc.options.forwardKey.setPressed(true);
      }

      if (!mc.player.isFallFlying() && mc.player.fallDistance > 0.0F && this.checkElytra() && !mc.player.isFallFlying()) {
         this.castElytra();
      }

      this.jitter = 20.0F * MathUtility.sin((float)(System.currentTimeMillis() - ThunderHack.initTime) / 50.0F);
      ((ILivingEntity)mc.player).setLastJumpCooldown(0);
   }

   public boolean castElytra() {
      if (this.checkElytra() && this.check()) {
         this.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
         return true;
      } else {
         return false;
      }
   }

   private boolean checkElytra() {
      if (mc.player.input.jumping && !mc.player.getAbilities().flying && !mc.player.hasVehicle() && !mc.player.isClimbing()) {
         ItemStack is = mc.player.getEquippedStack(EquipmentSlot.CHEST);
         return is.isOf(Items.ELYTRA) && (ElytraItem.isUsable(is) || this.allowBroken.getValue());
      } else {
         return false;
      }
   }

   private boolean check() {
      if (!mc.player.isTouchingWater() && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
         ItemStack is = mc.player.getEquippedStack(EquipmentSlot.CHEST);
         if (is.isOf(Items.ELYTRA) && (ElytraItem.isUsable(is) || this.allowBroken.getValue())) {
            mc.player.startFallFlying();
            return true;
         }
      }

      return false;
   }

   private static enum Exploit {
      None,
      Strict,
      Strong;
   }
}
