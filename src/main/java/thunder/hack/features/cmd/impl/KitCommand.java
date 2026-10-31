package thunder.hack.features.cmd.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import net.minecraft.util.Formatting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.ClientSettings;

public class KitCommand extends Command {
   private static final String PATH = "ThunderHackRecode/misc/AutoGear.json";

   public KitCommand() {
      super("kit");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(literal("list").executes(context -> {
         this.listMessage();
         return 1;
      }));
      builder.then(literal("create").then(arg("name", StringArgumentType.word()).executes(context -> {
         this.save((String)context.getArgument("name", String.class));
         return 1;
      })));
      builder.then(literal("set").then(arg("name", StringArgumentType.word()).executes(context -> {
         this.set((String)context.getArgument("name", String.class));
         return 1;
      })));
      builder.then(literal("del").then(arg("name", StringArgumentType.word()).executes(context -> {
         this.delete((String)context.getArgument("name", String.class));
         return 1;
      })));
      builder.executes(context -> {
         sendMessage("kit <create/set/del/list> <name>");
         return 1;
      });
   }

   public static String getSelectedKit() {
      try {
         JsonObject json = new JsonParser().parse(new FileReader("ThunderHackRecode/misc/AutoGear.json")).getAsJsonObject();
         if (!json.get("selected").getAsString().equals("none")) {
            return json.get("selected").getAsString();
         }
      } catch (Exception var1) {
      }

      sendMessage(ClientSettings.isRu() ? "\u041a\u0438\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d" : "Kit not found");
      return "";
   }

   public static String getKitItems(String kit) {
      try {
         JsonObject json = new JsonParser().parse(new FileReader("ThunderHackRecode/misc/AutoGear.json")).getAsJsonObject();
         return json.get(kit).getAsString();
      } catch (Exception var2) {
         sendMessage(ClientSettings.isRu() ? "\u041a\u0438\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d" : "Kit not found");
         return "";
      }
   }

   private void listMessage() {
      try {
         JsonObject json = new JsonParser().parse(new FileReader("ThunderHackRecode/misc/AutoGear.json")).getAsJsonObject();
         sendMessage(ClientSettings.isRu() ? "\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0435 \u043a\u0438\u0442\u044b:" : "Available kits:");

         for (int i = 0; i < json.entrySet().size(); i++) {
            String item = json.entrySet().toArray()[i].toString().split("=")[0];
            sendMessage(
               Formatting.GRAY
                  + "-> "
                  + item
                  + (item.equals("selected") ? (ClientSettings.isRu() ? "(\u0412\u044b\u0431\u0440\u0430\u043d)" : " (Selected)") : "")
            );
         }
      } catch (Exception var4) {
         sendMessage(
            ClientSettings.isRu()
               ? "\u041f\u0440\u043e\u0431\u043b\u0435\u043c\u0430 \u0441 \u043a\u043e\u043d\u0444\u0438\u0433\u0443\u0440\u0430\u0446\u0438\u0435\u0439 \u043a\u0438\u0442\u043e\u0432!"
               : "Error with kit cfg!"
         );
      }
   }

   private void delete(String name) {
      try {
         JsonObject json = new JsonParser().parse(new FileReader("ThunderHackRecode/misc/AutoGear.json")).getAsJsonObject();
         if (json.get(name) != null && !name.equals("selected")) {
            json.remove(name);
            if (json.get("selected").getAsString().equals(name)) {
               json.addProperty("selected", "none");
            }

            this.saveFile(json, name, ClientSettings.isRu() ? "\u0443\u0434\u0430\u043b\u0435\u043d" : "deleted");
         } else {
            sendMessage("Kit not found");
         }
      } catch (Exception var3) {
         sendMessage(ClientSettings.isRu() ? "\u041a\u0438\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d" : "Kit not found");
      }
   }

   private void set(String name) {
      try {
         JsonObject json = new JsonParser().parse(new FileReader("ThunderHackRecode/misc/AutoGear.json")).getAsJsonObject();
         if (json.get(name) != null && !name.equals("selected")) {
            json.addProperty("selected", name);
            this.saveFile(json, name, ClientSettings.isRu() ? "\u0432\u044b\u0431\u0440\u0430\u043d" : "selected");
         } else {
            sendMessage(ClientSettings.isRu() ? "\u041a\u0438\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d" : "Kit not found");
         }
      } catch (Exception var3) {
         sendMessage(ClientSettings.isRu() ? "\u041a\u0438\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d" : "Kit not found");
      }
   }

   private void save(String name) {
      JsonObject json = new JsonObject();

      try {
         json = new JsonParser().parse(new FileReader("ThunderHackRecode/misc/AutoGear.json")).getAsJsonObject();
         if (json.get(name) != null && !name.equals("selected")) {
            sendMessage(
               ClientSettings.isRu()
                  ? "\u042d\u0442\u043e\u0442 \u043a\u0438\u0442 \u0443\u0436\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442"
                  : "This kit arleady exist"
            );
            return;
         }
      } catch (IOException var6) {
         json.addProperty("selected", "none");
      }

      StringBuilder jsonInventory = new StringBuilder();

      for (ItemStack item : mc.player.getInventory().main) {
         jsonInventory.append(
               item.getItem() instanceof PotionItem
                  ? item.getItem().getTranslationKey() + ((PotionContentsComponent)item.getItem().getComponents().get(DataComponentTypes.POTION_CONTENTS)).getColor()
                  : item.getItem().getTranslationKey()
            )
            .append(" ");
      }

      json.addProperty(name, jsonInventory.toString());
      this.saveFile(json, name, ClientSettings.isRu() ? "\u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d" : "saved");
   }

   private void saveFile(@NotNull JsonObject completeJson, String name, String operation) {
      try {
         File file = new File("ThunderHackRecode/misc/AutoGear.json");

         try {
            file.createNewFile();
         } catch (Exception var6) {
         }

         BufferedWriter bw = new BufferedWriter(new FileWriter("ThunderHackRecode/misc/AutoGear.json"));
         bw.write(completeJson.toString());
         bw.close();
         sendMessage((ClientSettings.isRu() ? "\u041a\u0438\u0442 " : "Kit ") + Formatting.AQUA + name + Formatting.RESET + " " + operation);
      } catch (IOException var7) {
         sendMessage(
            ClientSettings.isRu()
               ? "\u041e\u0448\u0438\u0431\u043a\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u044f \u0444\u0430\u0439\u043b\u0430"
               : "Error saving the file"
         );
      }
   }
}
