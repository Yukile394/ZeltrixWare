package thunder.hack.features.modules.render;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class SmoothTab extends Module {
   public final Setting<Float> animationSpeed = new Setting<>("Speed", 6.0F, 1.0F, 20.0F);

   public SmoothTab() {
      super("SmoothTab", Module.Category.RENDER);
   }
}
