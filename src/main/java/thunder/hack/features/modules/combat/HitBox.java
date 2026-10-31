package thunder.hack.features.modules.combat;

import thunder.hack.core.manager.client.ServerManager;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public final class HitBox extends Module {
   public static final Setting<Float> XZExpand = new Setting<>("XZExpand", 1.0F, 0.0F, 5.0F);
   public static final Setting<Float> YExpand = new Setting<>("YExpand", 0.0F, 0.0F, 5.0F);
   public static final Setting<Boolean> affectToAura = new Setting<>("AffectToAura", false);

   public HitBox() {
      super("HitBoxes", Module.Category.COMBAT);
   }

   @Override
   public String getDisplayInfo() {
      return "H: " + ServerManager.round2((double)XZExpand.getValue().floatValue()) + " V: " + ServerManager.round2((double)YExpand.getValue().floatValue());
   }
}
