package thunder.hack.injection;

import java.net.URI;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.text.Text;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.gui.mainmenu.MainMenuScreen;
import thunder.hack.gui.misc.DialogScreen;
import thunder.hack.utility.render.TextureStorage;

@Mixin({TitleScreen.class})
public class MixinTitleScreen extends Screen {
   private static boolean firstOpen = true;

   protected MixinTitleScreen(Text title) {
      super(title);
   }

   @Inject(
      method = {"init"},
      at = {@At("RETURN")}
   )
   public void postInitHook(CallbackInfo ci) {
      if (firstOpen) {
         firstOpen = false;
         Util.getOperatingSystem().open("https://discord.gg/zenobest");
      }

      if (ClientSettings.customMainMenu.getValue() && !MainMenuScreen.getInstance().confirm && ModuleManager.clickGui.getBind().getKey() != -1) {
         Module.mc.setScreen(MainMenuScreen.getInstance());
      }

      if (ModuleManager.clickGui.getBind().getKey() == -1) {
         DialogScreen dialogScreen2 = new DialogScreen(
            TextureStorage.cutie,
            ClientSettings.isRu()
               ? "\u0421\u043f\u0430\u0441\u0438\u0431\u043e \u0447\u0442\u043e \u0441\u043a\u0430\u0447\u0430\u043b\u0438 ThunderHack!"
               : "Thank you for downloading ThunderHack!",
            ClientSettings.isRu()
               ? "\u041c\u0435\u043d\u044e \u0441 \u0444\u0443\u043d\u043a\u0446\u0438\u044f\u043c\u0438 \u043a\u043b\u0438\u0435\u043d\u0442\u0430 \u043e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u0442\u0441\u044f \u043d\u0430 \u043a\u043b\u0430\u0432\u0438\u0448\u0443 - P"
               : "Menu with client modules is opened with the key - P",
            ClientSettings.isRu() ? "\u0417\u0430\u0439\u0442\u0438 \u0432 \u043c\u0430\u0439\u043d" : "Join on minecraft",
            ClientSettings.isRu() ? "\u0417\u0430\u043a\u0440\u044b\u0442\u044c \u043c\u0430\u0439\u043d" : "Close minecraft",
            () -> {
               ModuleManager.clickGui.setBind(InputUtil.fromTranslationKey("key.keyboard.p").getCode(), false, false);
               Module.mc.setScreen(MainMenuScreen.getInstance());
            },
            () -> {
               ModuleManager.clickGui.setBind(InputUtil.fromTranslationKey("key.keyboard.p").getCode(), false, false);
               Module.mc.stop();
            }
         );
         DialogScreen dialogScreen1 = new DialogScreen(
            TextureStorage.questionPic, "Hello!", "What's your language?", "\u0420\u0443\u0441\u0441\u043a\u0438\u0439", "English", () -> {
               ClientSettings.language.setValue(ClientSettings.Language.RU);
               Module.mc.setScreen(dialogScreen2);
            }, () -> {
               ClientSettings.language.setValue(ClientSettings.Language.ENG);
               Module.mc.setScreen(dialogScreen2);
            }
         );
         Module.mc.setScreen(dialogScreen1);
      }

      if (ThunderHack.isOutdated && !FabricLoader.getInstance().isDevelopmentEnvironment()) {
         Module.mc
            .setScreen(
               new ConfirmScreen(
                  confirm -> {
                     if (confirm) {
                        Util.getOperatingSystem()
                           .open(URI.create("https://github.com/Pan4ur/ThunderHack-Recode/releases/download/latest/thunderhack-1.7.jar/"));
                     } else {
                        Module.mc.stop();
                     }
                  },
                  Text.of(Formatting.RED + "You are using an outdated version of ThunderHack Recode"),
                  Text.of("Please update to the latest release"),
                  Text.of("Download"),
                  Text.of("Quit Game")
               )
            );
      }
   }
}
