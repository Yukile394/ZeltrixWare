package thunder.hack.injection;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import net.minecraft.text.Style;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.CommandManager;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.client.ProxyManager;
import thunder.hack.events.impl.ClientClickEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.gui.misc.DialogScreen;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

@Mixin({Screen.class})
public abstract class MixinScreen {
   @Shadow
   public abstract void init(MinecraftClient var1, int var2, int var3);

   @Inject(
      method = {"handleTextClick"},
      at = {@At(
         value = "INVOKE",
         target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V",
         ordinal = 1,
         remap = false
      )},
      cancellable = true
   )
   private void onRunCommand(Style style, CallbackInfoReturnable<Boolean> cir) {
      if (Objects.requireNonNull(style.getClickEvent()) instanceof ClientClickEvent clientClickEvent
         && clientClickEvent.getValue().startsWith(Managers.COMMAND.getPrefix())) {
         try {
            CommandManager manager = Managers.COMMAND;
            manager.getDispatcher().execute(style.getClickEvent().getValue().substring(Managers.COMMAND.getPrefix().length()), manager.getSource());
            cir.setReturnValue(true);
         } catch (CommandSyntaxException var5) {
         }
      }
   }

   @Inject(
      method = {"filesDragged"},
      at = {@At("HEAD")}
   )
   public void filesDragged(List<Path> paths, CallbackInfo ci) {
      String configPath = paths.get(0).toString();
      File cfgFile = new File(configPath);
      String fileName = cfgFile.getName();
      if (fileName.contains(".th")) {
         DialogScreen dialogScreen = new DialogScreen(
            TextureStorage.questionPic,
            ClientSettings.isRu() ? "\u041e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d \u043a\u043e\u043d\u0444\u0438\u0433!" : "Config detected!",
            ClientSettings.isRu()
               ? "\u0422\u044b \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0442\u0435\u043b\u044c\u043d\u043e \u0445\u043e\u0447\u0435\u0448\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c "
                  + fileName
                  + "?"
               : "Are you sure you want to load " + fileName + "?",
            ClientSettings.isRu() ? "\u0414\u0430" : "Yes",
            ClientSettings.isRu() ? "\u041d\u0435\u0442" : "No",
            () -> {
               Managers.MODULE.onUnload("none");
               Managers.CONFIG.load(cfgFile);
               Managers.MODULE.onLoad("none");
               Module.mc.setScreen(null);
            },
            () -> Module.mc.setScreen(null)
         );
         Module.mc.setScreen(dialogScreen);
      } else if (fileName.contains(".txt")) {
         DialogScreen dialogScreen2 = new DialogScreen(
            TextureStorage.questionPic,
            ClientSettings.isRu()
               ? "\u041e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d \u0442\u0435\u043a\u0441\u0442\u043e\u0432\u044b\u0439 \u0444\u0430\u0439\u043b!"
               : "Text file detected!",
            ClientSettings.isRu()
               ? "\u0418\u043c\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u0444\u0430\u0439\u043b " + fileName + " \u043a\u0430\u043a"
               : "Import file " + fileName + " as",
            ClientSettings.isRu() ? "\u041f\u0440\u043e\u043a\u0441\u0438" : "Proxies",
            ClientSettings.isRu() ? "\u0417\u0430\u0431\u0438\u0442\u044c" : "Cancel",
            () -> {
               try (BufferedReader reader = new BufferedReader(new FileReader(cfgFile))) {
                  while (reader.ready()) {
                     String[] line = reader.readLine().split(":");
                     String ip = line[0];
                     String port = line[1];
                     String login = line[2];
                     String password = line[3];
                     int p = 80;

                     try {
                        p = Integer.parseInt(port);
                     } catch (Exception var10) {
                        LogUtils.getLogger().warn(var10.getMessage());
                     }

                     Managers.PROXY.addProxy(new ProxyManager.ThProxy("Proxy" + (int)MathUtility.random(0.0F, 10000.0F), ip, p, login, password));
                  }
               } catch (Exception var12) {
               }

               Module.mc.setScreen(null);
            },
            () -> Module.mc.setScreen(null)
         );
         Module.mc.setScreen(dialogScreen2);
      }
   }

   @Inject(
      method = {"renderPanoramaBackground"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void renderPanoramaBackgroundHook(DrawContext context, float delta, CallbackInfo ci) {
      if (ClientSettings.customPanorama.getValue() && Module.mc.world == null) {
         ci.cancel();
         Render2DEngine.drawMainMenuShader(
            context.getMatrices(), 0.0F, 0.0F, (float)Module.mc.getWindow().getScaledWidth(), (float)Module.mc.getWindow().getScaledHeight()
         );
      }
   }

   @Inject(
      method = {"renderInGameBackground"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderInGameBackground(CallbackInfo info) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.disableGuiBackGround.getValue()) {
         info.cancel();
      }
   }

   @Inject(
      method = {"renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onRenderBackground(DrawContext context, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.disableGuiBackGround.getValue() && Module.mc.world != null) {
         ci.cancel();
      }
   }
}
