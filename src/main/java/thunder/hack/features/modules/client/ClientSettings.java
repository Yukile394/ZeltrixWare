package thunder.hack.features.modules.client;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public final class ClientSettings extends Module {
   public static Setting<Boolean> futureCompatibility = new Setting<>("FutureCompatibility", false);
   public static Setting<Boolean> customMainMenu = new Setting<>("CustomMainMenu", true);
   public static Setting<Boolean> customPanorama = new Setting<>("CustomPanorama", true);
   public static Setting<Boolean> customLoadingScreen = new Setting<>("CustomLoadingScreen", true);
   public static Setting<Boolean> scaleFactorFix = new Setting<>("ScaleFactorFix", false);
   public static Setting<Float> scaleFactorFixValue = new Setting<>("ScaleFactorFixValue", 2.0F, 0.0F, 4.0F);
   public static Setting<Boolean> renderRotations = new Setting<>("RenderRotations", true);
   public static Setting<Boolean> skullEmoji = new Setting<>("SkullEmoji", true);
   public static Setting<Boolean> clientMessages = new Setting<>("ClientMessages", true);
   public static Setting<Boolean> debug = new Setting<>("Debug", false);
   public static Setting<Boolean> customBob = new Setting<>("CustomBob", true);
   public static Setting<Boolean> telemetry = new Setting<>("Telemetry", true);
   public static Setting<ClientSettings.Language> language = new Setting<>("Language", ClientSettings.Language.ENG);
   public static Setting<String> prefix = new Setting<>("Prefix", "@");
   public static Setting<ClientSettings.ClipCommandMode> clipCommandMode = new Setting<>("ClipCommandMode", ClientSettings.ClipCommandMode.Matrix);

   public ClientSettings() {
      super("ClientSettings", Module.Category.CLIENT);
   }

   public static boolean isRu() {
      return language.is(ClientSettings.Language.RU);
   }

   @Override
   public boolean isToggleable() {
      return false;
   }

   public static enum ClipCommandMode {
      Default,
      Matrix;
   }

   public static enum Language {
      RU,
      ENG;
   }
}
