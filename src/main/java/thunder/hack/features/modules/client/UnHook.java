package thunder.hack.features.modules.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Formatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.util.Icons;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ConfigManager;
import thunder.hack.features.modules.Module;
import thunder.hack.utility.math.MathUtility;

public class UnHook extends Module {
   List<Module> list;
   public int code = 0;

   public UnHook() {
      super("UnHook", Module.Category.CLIENT);
   }

   @Override
   public void onEnable() {
      this.code = (int)MathUtility.random(10.0F, 99.0F);

      for (int i = 0; i < 20; i++) {
         this.sendMessage(
            ClientSettings.isRu()
               ? Formatting.RED
                  + "\u0429\u0430 \u0432\u0441\u0435 \u0441\u0432\u0435\u0440\u043d\u0435\u0442\u0441\u044f, \u043d\u0430\u043f\u0438\u0448\u0438 \u0432 \u0447\u0430\u0442 "
                  + Formatting.WHITE
                  + this.code
                  + Formatting.RED
                  + " \u0447\u0442\u043e\u0431\u044b \u0432\u0441\u0435 \u0432\u0435\u0440\u043d\u0443\u0442\u044c!"
               : Formatting.RED
                  + "It's all close now, write to the chat "
                  + Formatting.WHITE
                  + this.code
                  + Formatting.RED
                  + " to return everything!"
         );
      }

      this.list = Managers.MODULE.getEnabledModules();
      mc.setScreen(null);
      Managers.ASYNC
         .run(
            () -> mc.executeSync(
                  () -> {
                     for (Module module : this.list) {
                        if (!module.equals(this)) {
                           module.disable();
                        }
                     }

                     ClientSettings.customMainMenu.setValue(false);

                     try {
                        mc.getWindow()
                           .setIcon(mc.getDefaultResourcePack(), SharedConstants.getGameVersion().isStable() ? Icons.RELEASE : Icons.SNAPSHOT);
                     } catch (Exception var10) {
                     }

                     mc.inGameHud.getChatHud().clear(true);
                     this.setEnabled(true);

                     try {
                        File file = new File(mc.runDirectory + File.separator + "logs" + File.separator + "latest.log");
                        FileInputStream fis = new FileInputStream(file);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                        ArrayList<String> lines = new ArrayList<>();

                        String line;
                        while ((line = reader.readLine()) != null) {
                           if (!line.contains("thunderhack")
                              && !line.contains("ThunderHack")
                              && !line.contains("$$")
                              && !line.contains("\\______/")
                              && !line.contains("By pan4ur, 06ED")
                              && !line.contains("\u26a1")
                              && !line.contains("thunder.hack")) {
                              lines.add(line);
                           }
                        }

                        fis.close();

                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                           for (String s : lines) {
                              writer.write(s + "\n");
                           }
                        } catch (Exception var12) {
                        }

                        ConfigManager.MAIN_FOLDER.renameTo(new File("XaeroWaypoints_BACKUP092738"));
                     } catch (IOException var13) {
                     }
                  }
               ),
            5000L
         );
   }

   @Override
   public void onDisable() {
      if (this.list != null) {
         for (Module module : this.list) {
            if (!module.equals(this)) {
               module.enable();
            }
         }

         ClientSettings.customMainMenu.setValue(true);

         try {
            new File("XaeroWaypoints_BACKUP092738").renameTo(new File("ThunderHackRecode"));
         } catch (Exception var3) {
         }
      }
   }
}
