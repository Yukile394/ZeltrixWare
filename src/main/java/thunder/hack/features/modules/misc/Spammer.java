package thunder.hack.features.modules.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.io.IOUtils;
import thunder.hack.core.Managers;
import thunder.hack.features.hud.impl.StaffBoard;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

public class Spammer extends Module {
   public static ArrayList<String> SpamList = new ArrayList<>();
   public Setting<Spammer.Mode> mode = new Setting<>("mode", Spammer.Mode.Chat);
   public Setting<Spammer.Messages> messages = new Setting<>("messages", Spammer.Messages.File);
   public Setting<Spammer.WhisperPrefix> whisper_prefix = new Setting<>("prefix", Spammer.WhisperPrefix.W, v -> this.mode.getValue() == Spammer.Mode.Whispers);
   public Setting<Boolean> global = new Setting<>("global", true, v -> this.mode.getValue() == Spammer.Mode.Chat);
   public Setting<Boolean> antiSpam = new Setting<>("AntiSpam", false);
   public Setting<Float> delay = new Setting<>("delay", 5.0F, 0.0F, 30.0F);
   private final Timer timer_delay = new Timer();
   private final Random random = new Random();
   private String fact;

   public Spammer() {
      super("Spammer", Module.Category.MISC);
   }

   public static void loadSpammer() {
      try {
         File file = new File("ThunderHackRecode/misc/spammer.txt");
         if (!file.exists()) {
            file.createNewFile();
         }

         new Thread(() -> {
            try {
               FileInputStream fis = new FileInputStream(file);
               InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
               BufferedReader reader = new BufferedReader(isr);
               ArrayList<String> lines = new ArrayList<>();

               String line;
               while ((line = reader.readLine()) != null) {
                  lines.add(line);
               }

               boolean newline = false;

               for (String l : lines) {
                  if (l.equals("")) {
                     newline = true;
                     break;
                  }
               }

               SpamList.clear();
               ArrayList<String> spamList = new ArrayList<>();
               if (!newline) {
                  spamList.addAll(lines);
               } else {
                  StringBuilder spamChunk = new StringBuilder();

                  for (String lx : lines) {
                     if (lx.equals("")) {
                        if (!spamChunk.isEmpty()) {
                           spamList.add(spamChunk.toString());
                           spamChunk = new StringBuilder();
                        }
                     } else {
                        spamChunk.append(lx).append(" ");
                     }
                  }

                  spamList.add(spamChunk.toString());
               }

               SpamList = spamList;
            } catch (Exception var11) {
            }
         }).start();
      } catch (IOException var1) {
      }
   }

   public String getPlayerName() {
      try {
         List<String> list = StaffBoard.getOnlinePlayer();
         return list.isEmpty() ? "" : list.get(this.random.nextInt(0, list.size() - 1));
      } catch (NullPointerException var2) {
         return null;
      }
   }

   private void changeFact() {
      Managers.ASYNC
         .run(
            () -> {
               try {
                  String jsonResponse = IOUtils.toString(new URL("https://catfact.ninja/fact?max_length=200"), StandardCharsets.UTF_8);
                  JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
                  this.fact = jsonObject.get("fact").getAsString();
               } catch (IOException var3) {
                  this.disable(
                     ClientSettings.isRu()
                        ? "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0444\u0430\u043a\u0442, \u043c\u043e\u0436\u0435\u0442 \u0442\u044b \u0432\u043a\u043b\u044e\u0447\u0438\u0448\u044c \u0438\u043d\u0442\u0435\u0440\u043d\u0435\u0442?"
                        : "Failed to load the fact, can you turn on the Internet?"
                  );
               }
            }
         );
   }

   public static String generateRandomSymbol() {
      Random random = new Random();
      String randomSymbol = "[";
      randomSymbol = randomSymbol + (char)(random.nextInt(26) + 97);
      randomSymbol = randomSymbol + random.nextInt(10);
      randomSymbol = randomSymbol + (char)(random.nextInt(26) + 97);
      return randomSymbol + "]";
   }

   @Override
   public void onEnable() {
      loadSpammer();
      this.changeFact();
   }

   @Override
   public void onUpdate() {
      if (this.timer_delay.passedMs((long)(this.delay.getValue() * 1000.0F))) {
         String c;
         if (this.messages.getValue() == Spammer.Messages.File) {
            if (SpamList.isEmpty()) {
               this.disable(ClientSettings.isRu() ? "\u0424\u0430\u0439\u043b spammer \u043f\u0443\u0441\u0442\u043e\u0439!" : "The spammer file is empty!");
               return;
            }

            c = SpamList.get(new Random().nextInt(SpamList.size()));
         } else {
            if (this.fact == null) {
               return;
            }

            c = this.fact;
            this.changeFact();
         }

         if (this.antiSpam.getValue()) {
            c = c + generateRandomSymbol();
         }

         if (this.mode.getValue() == Spammer.Mode.Chat) {
            if (c.charAt(0) == '/') {
               c = c.replace("/", "");
               mc.player.networkHandler.sendCommand(c);
            } else {
               mc.player.networkHandler.sendChatMessage(this.global.getValue() ? "!" + c : c);
            }
         } else {
            try {
               String prefix = this.whisper_prefix.getValue().prefix;
               mc.player.networkHandler.sendCommand(prefix + this.getPlayerName() + " " + c);
            } catch (NullPointerException var3) {
            }
         }

         this.timer_delay.reset();
      }
   }

   private static enum Messages {
      File,
      CatFacts;
   }

   private static enum Mode {
      Chat,
      Whispers;
   }

   private static enum WhisperPrefix {
      W("w "),
      Msg("msg "),
      Tell("tell ");

      final String prefix;

      private WhisperPrefix(String p) {
         this.prefix = p;
      }
   }
}
