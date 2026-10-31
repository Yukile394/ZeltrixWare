package thunder.hack.core.manager.client;

import java.io.File;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.Registries;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.client.SoundFX;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;

public class SoundManager implements IManager {
   public final Identifier KEYPRESS_SOUND = Identifier.of("thunderhack:keypress");
   public SoundEvent KEYPRESS_SOUNDEVENT = SoundEvent.of(this.KEYPRESS_SOUND);
   public final Identifier KEYRELEASE_SOUND = Identifier.of("thunderhack:keyrelease");
   public SoundEvent KEYRELEASE_SOUNDEVENT = SoundEvent.of(this.KEYRELEASE_SOUND);
   public final Identifier UWU_SOUND = Identifier.of("thunderhack:uwu");
   public SoundEvent UWU_SOUNDEVENT = SoundEvent.of(this.UWU_SOUND);
   public final Identifier ENABLE_SOUND = Identifier.of("thunderhack:enable");
   public SoundEvent ENABLE_SOUNDEVENT = SoundEvent.of(this.ENABLE_SOUND);
   public final Identifier DISABLE_SOUND = Identifier.of("thunderhack:disable");
   public SoundEvent DISABLE_SOUNDEVENT = SoundEvent.of(this.DISABLE_SOUND);
   public final Identifier MOAN1_SOUND = Identifier.of("thunderhack:moan1");
   public SoundEvent MOAN1_SOUNDEVENT = SoundEvent.of(this.MOAN1_SOUND);
   public final Identifier MOAN2_SOUND = Identifier.of("thunderhack:moan2");
   public SoundEvent MOAN2_SOUNDEVENT = SoundEvent.of(this.MOAN2_SOUND);
   public final Identifier MOAN3_SOUND = Identifier.of("thunderhack:moan3");
   public SoundEvent MOAN3_SOUNDEVENT = SoundEvent.of(this.MOAN3_SOUND);
   public final Identifier MOAN4_SOUND = Identifier.of("thunderhack:moan4");
   public SoundEvent MOAN4_SOUNDEVENT = SoundEvent.of(this.MOAN4_SOUND);
   public final Identifier SKEET_SOUND = Identifier.of("thunderhack:skeet");
   public SoundEvent SKEET_SOUNDEVENT = SoundEvent.of(this.SKEET_SOUND);
   public final Identifier ORTHODOX_SOUND = Identifier.of("thunderhack:orthodox");
   public SoundEvent ORTHODOX_SOUNDEVENT = SoundEvent.of(this.ORTHODOX_SOUND);
   public final Identifier BOOLEAN_SOUND = Identifier.of("thunderhack:boolean");
   public SoundEvent BOOLEAN_SOUNDEVENT = SoundEvent.of(this.BOOLEAN_SOUND);
   public final Identifier SCROLL_SOUND = Identifier.of("thunderhack:scroll");
   public SoundEvent SCROLL_SOUNDEVENT = SoundEvent.of(this.SCROLL_SOUND);
   public final Identifier SWIPEIN_SOUND = Identifier.of("thunderhack:swipein");
   public SoundEvent SWIPEIN_SOUNDEVENT = SoundEvent.of(this.SWIPEIN_SOUND);
   public final Identifier SWIPEOUT_SOUND = Identifier.of("thunderhack:swipeout");
   public SoundEvent SWIPEOUT_SOUNDEVENT = SoundEvent.of(this.SWIPEOUT_SOUND);
   public final Identifier ALERT_SOUND = Identifier.of("thunderhack:alert");
   public SoundEvent ALERT_SOUNDEVENT = SoundEvent.of(this.ALERT_SOUND);
   public final Identifier PM_SOUND = Identifier.of("thunderhack:pmsound");
   public SoundEvent PM_SOUNDEVENT = SoundEvent.of(this.PM_SOUND);
   public final Identifier RIFK_SOUND = Identifier.of("thunderhack:rifk");
   public SoundEvent RIFK_SOUNDEVENT = SoundEvent.of(this.RIFK_SOUND);
   public final Identifier CUTIE_SOUND = Identifier.of("thunderhack:cutie");
   public SoundEvent CUTIE_SOUNDEVENT = SoundEvent.of(this.CUTIE_SOUND);
   private final Timer scrollTimer = new Timer();

   public void registerSounds() {
      Registry.register(Registries.SOUND_EVENT, this.KEYPRESS_SOUND, this.KEYPRESS_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.KEYRELEASE_SOUND, this.KEYRELEASE_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.ENABLE_SOUND, this.ENABLE_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.DISABLE_SOUND, this.DISABLE_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.MOAN1_SOUND, this.MOAN1_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.MOAN2_SOUND, this.MOAN2_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.MOAN3_SOUND, this.MOAN3_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.MOAN4_SOUND, this.MOAN4_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.UWU_SOUND, this.UWU_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.SKEET_SOUND, this.SKEET_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.ORTHODOX_SOUND, this.ORTHODOX_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.SCROLL_SOUND, this.SCROLL_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.BOOLEAN_SOUND, this.BOOLEAN_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.SWIPEIN_SOUND, this.SWIPEIN_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.SWIPEOUT_SOUND, this.SWIPEOUT_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.ALERT_SOUND, this.ALERT_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.PM_SOUND, this.PM_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.RIFK_SOUND, this.RIFK_SOUNDEVENT);
      Registry.register(Registries.SOUND_EVENT, this.CUTIE_SOUND, this.CUTIE_SOUNDEVENT);
   }

   public void playHitSound(SoundFX.HitSound value) {
      switch (value) {
         case UWU:
            this.playSound(this.UWU_SOUNDEVENT);
            break;
         case SKEET:
            this.playSound(this.SKEET_SOUNDEVENT);
            break;
         case KEYBOARD:
            this.playSound(this.KEYPRESS_SOUNDEVENT);
            break;
         case CUTIE:
            this.playSound(this.CUTIE_SOUNDEVENT);
            break;
         case MOAN:
            SoundEvent sound = switch ((int)MathUtility.random(0.0F, 3.0F)) {
               case 0 -> this.MOAN1_SOUNDEVENT;
               case 1 -> this.MOAN2_SOUNDEVENT;
               case 2 -> this.MOAN3_SOUNDEVENT;
               default -> this.MOAN4_SOUNDEVENT;
            };
            this.playSound(sound);
            break;
         case RIFK:
            this.playSound(this.RIFK_SOUNDEVENT);
            break;
         case CUSTOM:
            this.playSound("hit");
      }
   }

   public void playEnable() {
      if (ModuleManager.soundFX.enableMode.getValue() == SoundFX.OnOffSound.Inertia) {
         this.playSound(this.ENABLE_SOUNDEVENT);
      } else if (ModuleManager.soundFX.enableMode.getValue() == SoundFX.OnOffSound.Custom) {
         this.playSound("enable");
      }
   }

   public void playDisable() {
      if (ModuleManager.soundFX.disableMode.getValue() == SoundFX.OnOffSound.Inertia) {
         this.playSound(this.DISABLE_SOUNDEVENT);
      } else if (ModuleManager.soundFX.disableMode.getValue() == SoundFX.OnOffSound.Custom) {
         this.playSound("disable");
      }
   }

   public void playScroll() {
      if (this.scrollTimer.every(50L)) {
         if (ModuleManager.soundFX.scrollSound.getValue() == SoundFX.ScrollSound.KeyBoard) {
            this.playSound(this.KEYPRESS_SOUNDEVENT);
         } else if (ModuleManager.soundFX.scrollSound.getValue() == SoundFX.ScrollSound.Custom) {
            this.playSound("scroll");
         }
      }
   }

   public void playSound(SoundEvent sound) {
      if (mc.player != null && mc.world != null) {
         mc.world
            .playSound(
               mc.player,
               mc.player.getBlockPos(),
               sound,
               SoundCategory.BLOCKS,
               (float)ModuleManager.soundFX.volume.getValue().intValue() / 100.0F,
               1.0F
            );
      }
   }

   public void playSound(String name) {
      try {
         Clip clip = AudioSystem.getClip();
         clip.open(AudioSystem.getAudioInputStream(new File(ConfigManager.SOUNDS_FOLDER, name + ".wav").getAbsoluteFile()));
         FloatControl floatControl = (FloatControl)clip.getControl(Type.MASTER_GAIN);
         floatControl.setValue(
            floatControl.getMaximum()
               - floatControl.getMinimum() * ((float)ModuleManager.soundFX.volume.getValue().intValue() / 100.0F)
               + floatControl.getMinimum()
         );
         clip.start();
      } catch (Exception var4) {
         Command.sendMessage(
            (
                  ClientSettings.isRu()
                     ? "\u041e\u0448\u0438\u0431\u043a\u0430 \u0432\u043e\u0441\u043f\u0440\u043e\u0438\u0437\u0432\u0435\u0434\u0435\u043d\u0438\u044f \u0437\u0432\u0443\u043a\u0430! \u041f\u0440\u043e\u0432\u0435\u0440\u044c "
                     : "Error with playing sound! Check "
               )
               + new File(ConfigManager.SOUNDS_FOLDER, name + ".wav").getAbsolutePath()
         );
      }
   }

   public void playSlider() {
      this.playSound(this.SCROLL_SOUNDEVENT);
   }

   public void playBoolean() {
      this.playSound(this.BOOLEAN_SOUNDEVENT);
   }

   public void playSwipeIn() {
      this.playSound(this.SWIPEIN_SOUNDEVENT);
   }

   public void playSwipeOut() {
      this.playSound(this.SWIPEOUT_SOUNDEVENT);
   }

   public void playPmSound() {
      this.playSound(this.PM_SOUNDEVENT);
   }
}
