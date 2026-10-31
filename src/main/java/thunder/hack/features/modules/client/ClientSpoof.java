package thunder.hack.features.modules.client;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class ClientSpoof extends Module {
   private final Setting<ClientSpoof.Mode> mode = new Setting<>("Mode", ClientSpoof.Mode.Vanilla);
   private final Setting<String> custom = new Setting<>("Client", "feather", v -> this.mode.getValue() == ClientSpoof.Mode.Custom);

   public ClientSpoof() {
      super("ClientSpoof", Module.Category.CLIENT);
   }

   public String getClientName() {
      return switch ((ClientSpoof.Mode)this.mode.getValue()) {
         case Vanilla -> "vanilla";
         case Lunar1_20_4 -> "lunarclient:1.20.4";
         case Lunar1_20_1 -> "lunarclient:1.20.1";
         case Custom -> this.custom.getValue().toString();
         default -> null;
      };
   }

   public static enum Mode {
      Vanilla,
      Lunar1_20_4,
      Lunar1_20_1,
      Custom,
      Null;
   }
}
