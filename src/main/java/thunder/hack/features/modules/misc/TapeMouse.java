package thunder.hack.features.modules.misc;

import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;

public class TapeMouse extends Module {
   private final Setting<Integer> delay = new Setting<>("Delay", 600, 0, 10000);
   private final Setting<BooleanSettingGroup> randomize = new Setting<>("Randomize", new BooleanSettingGroup(false));
   private final Setting<Integer> randomizeValue = new Setting<>("Value", 600, 0, 10000).addToGroup(this.randomize);
   private final Setting<TapeMouse.Mode> mode = new Setting<>("Mode", TapeMouse.Mode.Left);
   private final Setting<Boolean> legit = new Setting<>("Legit", false, v -> this.mode.getValue() == TapeMouse.Mode.Left);
   private final Timer timer = new Timer();

   public TapeMouse() {
      super("TapeMouse", Module.Category.MISC);
   }

   @Override
   public void onUpdate() {
      if (this.timer
         .every(
            (long)(
               (float)this.delay.getValue().intValue()
                  + (this.randomize.getValue().isEnabled() ? MathUtility.random(0.0F, (float)this.randomizeValue.getValue().intValue()) : 0.0F)
            )
         )) {
         if (this.mode.getValue() == TapeMouse.Mode.Left) {
            if (!this.legit.getValue()) {
               HitResult hr = mc.crosshairTarget;
               if (hr != null) {
                  if (hr instanceof EntityHitResult ehr && ehr.getEntity() != null) {
                     mc.interactionManager.attackEntity(mc.player, ehr.getEntity());
                     mc.player.swingHand(Hand.MAIN_HAND);
                     return;
                  }

                  if (hr instanceof BlockHitResult bhr
                     && bhr.getBlockPos() != null
                     && bhr.getSide() != null
                     && !mc.world.isAir(bhr.getBlockPos())) {
                     mc.interactionManager.attackBlock(bhr.getBlockPos(), bhr.getSide());
                     mc.player.swingHand(Hand.MAIN_HAND);
                  }
               }
            } else {
               ((IMinecraftClient)mc).idoAttack();
            }
         } else {
            ((IMinecraftClient)mc).idoItemUse();
         }
      }
   }

   private static enum Mode {
      Right,
      Left;
   }
}
