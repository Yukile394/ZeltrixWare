package thunder.hack.features.modules.render;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class InventoryAnimation extends Module {
   public static InventoryAnimation INSTANCE;
   public final Setting<InventoryAnimation.AnimationType> animationType = new Setting<>("Type", InventoryAnimation.AnimationType.Scale);
   public final Setting<Float> speed = new Setting<>("Speed", 10.0F, 1.0F, 30.0F);
   public final Setting<Boolean> inventoryOnly = new Setting<>("InventoryOnly", false);

   public InventoryAnimation() {
      super("InventoryAnimation", Module.Category.RENDER);
      INSTANCE = this;
   }

   public static enum AnimationType {
      Scale,
      Bounce,
      SlideUp,
      SlideDown,
      SlideLeft,
      SlideRight,
      Flip,
      Warp,
      Glitch;
   }
}
