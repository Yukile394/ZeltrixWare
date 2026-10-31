package thunder.hack.features.modules.client;

import meteordevelopment.orbit.EventHandler;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventAttack;
import thunder.hack.events.impl.EventDeath;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.setting.Setting;

public final class SoundFX extends Module {
   public final Setting<Integer> volume = new Setting<>("Volume", 100, 0, 100);
   public final Setting<SoundFX.OnOffSound> enableMode = new Setting<>("EnableMode", SoundFX.OnOffSound.Inertia);
   public final Setting<SoundFX.OnOffSound> disableMode = new Setting<>("DisableMode", SoundFX.OnOffSound.Inertia);
   public final Setting<SoundFX.HitSound> hitSound = new Setting<>("HitSound", SoundFX.HitSound.OFF);
   public final Setting<SoundFX.KillSound> killSound = new Setting<>("KillSound", SoundFX.KillSound.OFF);
   public final Setting<SoundFX.ScrollSound> scrollSound = new Setting<>("ScrollSound", SoundFX.ScrollSound.KeyBoard);

   public SoundFX() {
      super("SoundFX", Module.Category.CLIENT);
   }

   @EventHandler
   public void onAttack(@NotNull EventAttack event) {
      if (!event.isPre()) {
         Managers.SOUND.playHitSound(this.hitSound.getValue());
      }
   }

   @EventHandler
   public void onDeath(EventDeath e) {
      if (Aura.target != null && Aura.target == e.getPlayer() && this.killSound.is(SoundFX.KillSound.Custom)) {
         Managers.SOUND.playSound("kill");
      }
   }

   public static enum HitSound {
      UWU,
      MOAN,
      SKEET,
      RIFK,
      KEYBOARD,
      CUTIE,
      CUSTOM,
      OFF;
   }

   public static enum KillSound {
      Custom,
      OFF;
   }

   public static enum OnOffSound {
      Custom,
      Inertia,
      OFF;
   }

   public static enum ScrollSound {
      Custom,
      OFF,
      KeyBoard;
   }
}
