package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;
import net.minecraft.item.Items;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.PostPlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;

public final class AutoGApple extends Module {
   public final Setting<Integer> Delay = new Setting<>("UseDelay", 0, 0, 2000);
   private final Setting<Float> health = new Setting<>("health", 15.0F, 1.0F, 36.0F);
   public Setting<Boolean> absorption = new Setting<>("Absorption", false);
   public Setting<Boolean> autoTotemIntegration = new Setting<>("AutoTotemIntegration", true);
   private boolean isActive;
   private final Timer useDelay = new Timer();

   public AutoGApple() {
      super("AutoGApple", Module.Category.COMBAT);
   }

   @EventHandler
   public void onUpdate(PostPlayerUpdateEvent e) {
      if (!fullNullCheck()) {
         if (this.GapInOffHand()) {
            if (mc.player.getHealth() + (this.absorption.getValue() ? mc.player.getAbsorptionAmount() : 0.0F) <= this.health.getValue()
               && this.useDelay.passedMs((long)this.Delay.getValue().intValue())) {
               this.isActive = true;
               if (mc.currentScreen != null && !mc.player.isUsingItem()) {
                  ((IMinecraftClient)mc).idoItemUse();
               } else {
                  mc.options.useKey.setPressed(true);
               }
            } else if (this.isActive) {
               this.isActive = false;
               mc.options.useKey.setPressed(false);
            }
         } else if (this.isActive) {
            this.isActive = false;
            mc.options.useKey.setPressed(false);
         }
      }
   }

   private boolean GapInOffHand() {
      if (this.autoTotemIntegration.getValue()
         && ModuleManager.autoTotem.isEnabled()
         && InventoryUtility.findItemInHotBar(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE).found()) {
         if (!ModuleManager.autoTotem.rcGap.is(AutoTotem.RCGap.Off)) {
            return true;
         }

         this.sendMessage(
            Formatting.RED + (ClientSettings.isRu() ? "\u0412\u043a\u043b\u044e\u0447\u0438 RcGap \u0432 AutoTotem!" : "Enable RcGap in AutoTotem")
         );
      }

      return !mc.player.getOffHandStack().isEmpty()
         && (mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE || mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE);
   }
}
