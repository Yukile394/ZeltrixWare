package thunder.hack.features.modules.render;

import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class ClientSound extends Module {
   private static ClientSound INSTANCE = new ClientSound();
   public final Setting<ClientSound.EnableSnd> enableSound = new Setting<>("EnableSound", ClientSound.EnableSnd.Ding);
   public final Setting<ClientSound.DisableSnd> disableSound = new Setting<>("DisableSound", ClientSound.DisableSnd.Swoosh);
   public final Setting<ClientSound.ClickSnd> clickSound = new Setting<>("ClickSound", ClientSound.ClickSnd.Boolean);

   public ClientSound() {
      super("ClientSound", Module.Category.RENDER);
      INSTANCE = this;
   }

   public static ClientSound getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ClientSound();
      }

      return INSTANCE;
   }

   public void onModuleEnable() {
      if (this.isEnabled()) {
         switch ((ClientSound.EnableSnd)this.enableSound.getValue()) {
            case Ding:
               Managers.SOUND.playSound(Managers.SOUND.ENABLE_SOUNDEVENT);
               break;
            case Inertia:
               Managers.SOUND.playSound(Managers.SOUND.ENABLE_SOUNDEVENT);
               break;
            case Swipe:
               Managers.SOUND.playSwipeIn();
               break;
            case Alert:
               Managers.SOUND.playSound(Managers.SOUND.ALERT_SOUNDEVENT);
               break;
            case Custom:
               Managers.SOUND.playSound("enable");
            case OFF:
         }
      }
   }

   public void onModuleDisable() {
      if (this.isEnabled()) {
         switch ((ClientSound.DisableSnd)this.disableSound.getValue()) {
            case Swoosh:
               Managers.SOUND.playSwipeOut();
               break;
            case Inertia:
               Managers.SOUND.playSound(Managers.SOUND.DISABLE_SOUNDEVENT);
               break;
            case Alert:
               Managers.SOUND.playSound(Managers.SOUND.ALERT_SOUNDEVENT);
               break;
            case Custom:
               Managers.SOUND.playSound("disable");
            case OFF:
         }
      }
   }

   public void onRightClick() {
      if (this.isEnabled()) {
         switch ((ClientSound.ClickSnd)this.clickSound.getValue()) {
            case Boolean:
               Managers.SOUND.playBoolean();
               break;
            case KeyBoard:
               Managers.SOUND.playSound(Managers.SOUND.KEYPRESS_SOUNDEVENT);
               break;
            case Scroll:
               Managers.SOUND.playSlider();
               break;
            case Orthodox:
               Managers.SOUND.playSound(Managers.SOUND.ORTHODOX_SOUNDEVENT);
               break;
            case Custom:
               Managers.SOUND.playSound("click");
            case OFF:
         }
      }
   }

   public static enum ClickSnd {
      Boolean,
      KeyBoard,
      Scroll,
      Orthodox,
      Custom,
      OFF;
   }

   public static enum DisableSnd {
      Swoosh,
      Inertia,
      Alert,
      Custom,
      OFF;
   }

   public static enum EnableSnd {
      Ding,
      Inertia,
      Swipe,
      Alert,
      Custom,
      OFF;
   }
}
