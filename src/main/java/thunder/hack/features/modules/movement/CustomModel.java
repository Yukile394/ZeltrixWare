package thunder.hack.features.modules.movement;

import java.awt.Color;
import thunder.hack.features.models.CustomModelRenderer;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;

public class CustomModel extends Module {
   public final Setting<CustomModel.Mode> mode = new Setting<>("Mode", CustomModel.Mode.YakupTV);
   public final Setting<Boolean> changeZ = new Setting<>("ChangeZ", false);
   public final Setting<Boolean> friendHighlight = new Setting<>("FriendHighlight", true);
   public final Setting<ColorSetting> bodyColor = new Setting<>("BodyColor", new ColorSetting(new Color(255, 0, 0).getRGB()));
   public final Setting<ColorSetting> eyeColor = new Setting<>("EyeColor", new ColorSetting(new Color(255, 255, 255).getRGB()));
   public final Setting<ColorSetting> legsColor = new Setting<>("LegsColor", new ColorSetting(new Color(255, 0, 0).getRGB()));
   public final CustomModelRenderer customModelRenderer = new CustomModelRenderer();

   public CustomModel() {
      super("CustomModel", Module.Category.MOVEMENT);
   }

   public static enum Mode {
      Amogus,
      Rabbit,
      YakupTV,
      Tavsan,
      Boykisser,
      Paimon,
      Cutie;
   }
}
