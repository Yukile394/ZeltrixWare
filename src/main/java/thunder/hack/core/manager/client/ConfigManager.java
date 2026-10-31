package thunder.hack.core.manager.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.logging.LogUtils;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.EnumConverter;
import thunder.hack.setting.impl.ItemSelectSetting;
import thunder.hack.setting.impl.PositionSetting;
import thunder.hack.setting.impl.SettingGroup;

public class ConfigManager implements IManager {
   public static final String CONFIG_FOLDER_NAME = "ThunderHackRecode";
   public static final File MAIN_FOLDER = new File(mc.runDirectory, "ThunderHackRecode");
   public static final File CONFIGS_FOLDER = new File(MAIN_FOLDER, "configs");
   public static final File TEMP_FOLDER = new File(MAIN_FOLDER, "temp");
   public static final File MISC_FOLDER = new File(MAIN_FOLDER, "misc");
   public static final File SOUNDS_FOLDER = new File(MISC_FOLDER, "sounds");
   public static final File IMAGES_FOLDER = new File(MISC_FOLDER, "images");
   public static final File TABPARSER_FOLDER = new File(MISC_FOLDER, "tabparser");
   public static final File STASHLOGGER_FOLDER = new File(MISC_FOLDER, "stashlogger");
   public File currentConfig = null;
   public static boolean firstLaunch = false;

   public ConfigManager() {
      firstLaunch = !MAIN_FOLDER.exists();
      this.createDirs(MAIN_FOLDER, CONFIGS_FOLDER, TEMP_FOLDER, MISC_FOLDER, SOUNDS_FOLDER, IMAGES_FOLDER, TABPARSER_FOLDER, STASHLOGGER_FOLDER);
   }

   private void createDirs(File... dirs) {
      for (File dir : dirs) {
         if (!dir.exists()) {
            dir.mkdirs();
         }
      }
   }

   @NotNull
   public static String getConfigDate(String name) {
      File file = new File(CONFIGS_FOLDER, name + ".th");
      return !file.exists() ? "none" : new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date(file.lastModified()));
   }

   public void load(String name, String category) {
      File file = new File(CONFIGS_FOLDER, name + ".th");
      if (!file.exists()) {
         Command.sendMessage(
            ClientSettings.isRu()
               ? "\u041a\u043e\u043d\u0444\u0438\u0433\u0430 " + name + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442!"
               : "Config " + name + " does not exist!"
         );
      } else {
         if (this.currentConfig != null) {
            this.save(this.currentConfig);
         }

         Managers.MODULE.onUnload(category);
         this.load(file, category);
         Managers.MODULE.onLoad(category);
      }
   }

   public void loadBinds(String name) {
      File file = new File(CONFIGS_FOLDER, name + ".th");
      if (!file.exists()) {
         Command.sendMessage(
            ClientSettings.isRu()
               ? "\u041a\u043e\u043d\u0444\u0438\u0433\u0430 " + name + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442!"
               : "Config " + name + " does not exist!"
         );
      } else {
         if (this.currentConfig != null) {
            this.save(this.currentConfig);
         }

         this.loadBinds(file);
      }
   }

   private void loadBinds(@NotNull File config) {
      if (!config.exists()) {
         this.save(config);
      }

      try (FileReader reader = new FileReader(config, StandardCharsets.UTF_8)) {
         JsonObject modulesObject = JsonParser.parseReader(reader).getAsJsonArray().get(0).getAsJsonObject();
         JsonArray modules = modulesObject.getAsJsonArray("Modules");
         if (modules != null) {
            for (JsonElement element : modules) {
               this.parseBinds(element.getAsJsonObject());
            }
         }

         Command.sendMessage(
            ClientSettings.isRu()
               ? "\u0417\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u044b \u0431\u0438\u043d\u0434\u044b \u0441 \u043a\u043e\u043d\u0444\u0438\u0433\u0430: "
                  + config.getName()
               : "Loaded bind from config: " + config.getName()
         );
      } catch (IOException var9) {
         LogUtils.getLogger().warn(var9.getMessage());
      }

      this.saveCurrentConfig();
   }

   public void load(String name) {
      File file = new File(CONFIGS_FOLDER, name + ".th");
      if (!file.exists()) {
         Command.sendMessage(
            ClientSettings.isRu()
               ? "\u041a\u043e\u043d\u0444\u0438\u0433\u0430 " + name + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442!"
               : "Config " + name + " does not exist!"
         );
      } else {
         if (this.currentConfig != null) {
            this.save(this.currentConfig);
         }

         Managers.MODULE.onUnload("none");
         this.load(file);
         Managers.MODULE.onLoad("none");
      }
   }

   public void loadCloud(String name) {
      Command.sendMessage(ClientSettings.isRu() ? "\u0417\u0430\u0433\u0440\u0443\u0436\u0430\u044e.." : "Downloading..");

      try (
         BufferedInputStream in = new BufferedInputStream(
            new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/configs/" + name + ".th").openStream()
         );
         FileOutputStream fileOutputStream = new FileOutputStream(new File(CONFIGS_FOLDER, name + ".th"));
      ) {
         byte[] dataBuffer = new byte[1024];

         int bytesRead;
         while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
         }

         Command.sendMessage(ClientSettings.isRu() ? "\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u043b!" : "Downloaded!");
         this.load(name);
      } catch (Exception var10) {
         Command.sendMessage(
            ClientSettings.isRu()
               ? "\u041f\u0440\u043e\u0438\u0437\u043e\u0448\u043b\u0430 \u043e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0435! \u041c\u043e\u0436\u0435\u0442 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043d\u0435\u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u043e\u0435?"
               : "There was an error downloading! Maybe the name is wrong?"
         );
      }
   }

   public void loadModuleOnly(String name, Module module) {
      File file = new File(CONFIGS_FOLDER, name + ".th");
      if (!file.exists()) {
         Command.sendMessage(
            ClientSettings.isRu()
               ? "\u041a\u043e\u043d\u0444\u0438\u0433\u0430 " + name + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442!"
               : "Config " + name + " does not exist!"
         );
      } else {
         if (module.isEnabled()) {
            ThunderHack.EVENT_BUS.unsubscribe(module);
            module.setEnabled(false);
         }

         this.loadModuleOnly(file, module);
         if (module.isEnabled()) {
            ThunderHack.EVENT_BUS.subscribe(module);
         }
      }
   }

   public void load(@NotNull File config) {
      this.load(config, "none");
   }

   private void load(@NotNull File config, String category) {
      if (!config.exists()) {
         this.save(config);
      }

      try {
         FileReader reader = new FileReader(config, StandardCharsets.UTF_8);
         JsonObject modulesObject = JsonParser.parseReader(reader).getAsJsonArray().get(0).getAsJsonObject();
         JsonArray modules = modulesObject.getAsJsonArray("Modules");
         if (modules != null) {
            for (JsonElement element : modules) {
               this.parseModule(element.getAsJsonObject(), category);
            }
         }

         Command.sendMessage(
            ClientSettings.isRu()
               ? "\u0417\u0430\u0433\u0440\u0443\u0436\u0435\u043d \u043a\u043e\u043d\u0444\u0438\u0433 " + config.getName()
               : "Loaded " + config.getName()
         );
      } catch (Exception var8) {
         var8.printStackTrace();
      }

      if (Objects.equals(category, "none")) {
         this.currentConfig = config;
      }

      this.saveCurrentConfig();
   }

   public void loadModuleOnly(File config, Module module) {
      try (FileReader reader = new FileReader(config)) {
         JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
         JsonObject modulesObject = array.get(0).getAsJsonObject();
         JsonArray modules = modulesObject.getAsJsonArray("Modules");
         if (modules != null) {
            for (JsonElement element : modules) {
               JsonObject moduleObject = element.getAsJsonObject();
               Module loadedModule = Managers.MODULE.modules.stream().filter(m -> moduleObject.getAsJsonObject(m.getName()) != null).findFirst().orElse(null);
               if (loadedModule != null && Objects.equals(module.getName(), loadedModule.getName())) {
                  this.parseModule(moduleObject, "none");
               }
            }
         }

         Command.sendMessage(
            ClientSettings.isRu()
               ? "\u0417\u0430\u0433\u0440\u0443\u0436\u0435\u043d \u043c\u043e\u0434\u0443\u043b\u044c "
                  + module.getName()
                  + " \u0441 \u043a\u043e\u043d\u0444\u0438\u0433\u0430 "
                  + config.getName()
               : "Loaded " + module.getName() + " from " + config.getName()
         );
      } catch (IOException var13) {
         LogUtils.getLogger().warn(var13.getMessage());
      }
   }

   public void save(String name) {
      File file = new File(CONFIGS_FOLDER, name + ".th");
      if (file.exists()) {
         Command.sendMessage(
            ClientSettings.isRu()
               ? "\u041f\u0435\u0440\u0435\u0437\u0430\u043f\u0438\u0441\u044b\u0432\u0430\u0435\u043c " + name + "..."
               : "Overwriting " + name + "..."
         );
         file.delete();
      } else {
         Command.sendMessage(
            ClientSettings.isRu()
               ? "\u041a\u043e\u043d\u0444\u0438\u0433 "
                  + name
                  + " \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d!"
               : "Config " + name + " successfully saved!"
         );
      }

      this.save(file);
   }

   public void save(@NotNull File config) {
      try {
         if (!config.exists()) {
            config.createNewFile();
         }

         JsonArray array = new JsonArray();
         JsonObject modulesObj = new JsonObject();
         modulesObj.add("Modules", this.getModuleArray());
         array.add(modulesObj);
         FileWriter writer = new FileWriter(config, StandardCharsets.UTF_8);
         new GsonBuilder().setPrettyPrinting().create().toJson(array, writer);
         writer.close();
      } catch (IOException var5) {
         LogUtils.getLogger().warn(var5.getMessage());
      }
   }

   private void parseModule(JsonObject object, String category) throws NullPointerException {
      Module module = Managers.MODULE.modules.stream().filter(m -> object.getAsJsonObject(m.getName()) != null).findFirst().orElse(null);
      if (module != null) {
         if (Objects.equals(category, "none") || module.getCategory().getName().equalsIgnoreCase(category)) {
            JsonObject mobject = object.getAsJsonObject(module.getName());

            for (Setting setting : module.getSettings()) {
               try {
                  if (!(setting.getValue() instanceof SettingGroup)) {
                     if (setting.getValue() instanceof Boolean) {
                        setting.setValue(mobject.getAsJsonPrimitive(setting.getName()).getAsBoolean());
                     } else if (setting.getValue() instanceof Float) {
                        setting.setValue(mobject.getAsJsonPrimitive(setting.getName()).getAsFloat());
                     } else if (setting.getValue() instanceof Integer) {
                        setting.setValue(mobject.getAsJsonPrimitive(setting.getName()).getAsInt());
                     } else if (setting.getValue() instanceof String) {
                        setting.setValue(mobject.getAsJsonPrimitive(setting.getName()).getAsString().replace("%%", " ").replace("++", "/"));
                     } else if (setting.getValue() instanceof Bind) {
                        JsonArray array = mobject.getAsJsonArray(setting.getName());
                        if (array.get(0).getAsString().contains("M")) {
                           setting.setValue(new Bind(Integer.parseInt(array.get(0).getAsString().replace("M", "")), true, array.get(1).getAsBoolean()));
                        } else {
                           setting.setValue(new Bind(Integer.parseInt(array.get(0).getAsString()), false, array.get(1).getAsBoolean()));
                        }
                     } else if (setting.getValue() instanceof ColorSetting colorSetting) {
                        JsonArray array = mobject.getAsJsonArray(setting.getName());
                        colorSetting.setColor(array.get(0).getAsInt());
                        colorSetting.setRainbow(array.get(1).getAsBoolean());
                     } else if (setting.getValue() instanceof PositionSetting posSetting) {
                        JsonArray array = mobject.getAsJsonArray(setting.getName());
                        posSetting.setX(array.get(0).getAsFloat());
                        posSetting.setY(array.get(1).getAsFloat());
                     } else if (setting.getValue() instanceof BooleanSettingGroup bGroup) {
                        bGroup.setEnabled(mobject.getAsJsonPrimitive(setting.getName()).getAsBoolean());
                     } else if (setting.getValue() instanceof ItemSelectSetting iSetting) {
                        JsonArray array = mobject.getAsJsonArray(setting.getName());

                        for (int i = 0; i < array.size(); i++) {
                           if (!iSetting.getItemsById().contains(array.get(i).getAsString())) {
                              iSetting.getItemsById().add(array.get(i).getAsString());
                           }
                        }
                     } else if (setting.getValue().getClass().isEnum()) {
                        Enum value = new EnumConverter((Class<? extends Enum>)((Enum)setting.getValue()).getClass())
                           .doBackward((JsonElement)mobject.getAsJsonPrimitive(setting.getName()));
                        setting.setValue(value == null ? setting.getDefaultValue() : value);
                     }
                  }
               } catch (Exception var13) {
                  LogUtils.getLogger().warn("[Thunderhack] Module: " + module.getName() + " Setting: " + setting.getName() + " Error: ");
                  var13.printStackTrace();
               }
            }
         }
      }
   }

   private void parseBinds(JsonObject object) throws NullPointerException {
      Module module = Managers.MODULE.modules.stream().filter(m -> object.getAsJsonObject(m.getName()) != null).findFirst().orElse(null);
      if (module != null) {
         JsonObject mobject = object.getAsJsonObject(module.getName());

         for (Setting setting : module.getSettings()) {
            try {
               if (setting.getValue() instanceof Bind) {
                  JsonArray array = mobject.getAsJsonArray(setting.getName());
                  if (array.get(0).getAsString().contains("M")) {
                     setting.setValue(new Bind(Integer.parseInt(array.get(0).getAsString().replace("M", "")), true, array.get(1).getAsBoolean()));
                  } else {
                     setting.setValue(new Bind(Integer.parseInt(array.get(0).getAsString()), false, array.get(1).getAsBoolean()));
                  }
               }
            } catch (Exception var7) {
               LogUtils.getLogger().warn("[Thunderhack] Module: " + module.getName() + " Setting: " + setting.getName() + " Error: ");
               var7.printStackTrace();
            }
         }
      }
   }

   @NotNull
   private JsonArray getModuleArray() {
      JsonArray modulesArray = new JsonArray();

      for (Module m : Managers.MODULE.modules) {
         modulesArray.add(this.getModuleObject(m));
      }

      return modulesArray;
   }

   public JsonObject getModuleObject(@NotNull Module m) {
      JsonObject attribs = new JsonObject();
      JsonParser jp = new JsonParser();

      for (Setting setting : m.getSettings()) {
         if (setting.getValue() instanceof ColorSetting color) {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(color.getRawColor()));
            array.add(new JsonPrimitive(color.isRainbow()));
            attribs.add(setting.getName(), array);
         } else if (setting.getValue() instanceof PositionSetting pos) {
            JsonArray array = new JsonArray();
            array.add(new JsonPrimitive(pos.getX()));
            array.add(new JsonPrimitive(pos.getY()));
            attribs.add(setting.getName(), array);
         } else if (setting.getValue() instanceof BooleanSettingGroup bGroup) {
            attribs.add(setting.getName(), jp.parse(String.valueOf(bGroup.isEnabled())));
         } else if (setting.getValue() instanceof Bind b) {
            JsonArray array = new JsonArray();
            if (b.isMouse()) {
               array.add(jp.parse(b.getBind()));
            } else {
               array.add(new JsonPrimitive(b.getKey()));
            }

            array.add(new JsonPrimitive(b.isHold()));
            attribs.add(setting.getName(), array);
         } else if (setting.getValue() instanceof String str) {
            try {
               attribs.add(setting.getName(), jp.parse(str.replace(" ", "%%").replace("/", "++")));
            } catch (Exception var16) {
            }
         } else if (!(setting.getValue() instanceof ItemSelectSetting iSelect)) {
            if (setting.isEnumSetting()) {
               attribs.add(
                  setting.getName(), new EnumConverter((Class<? extends Enum>)((Enum)setting.getValue()).getClass()).doForward((Enum)setting.getValue())
               );
            } else {
               try {
                  attribs.add(setting.getName(), jp.parse(setting.getValue().toString()));
               } catch (Exception var15) {
               }
            }
         } else {
            JsonArray array = new JsonArray();

            for (String id : iSelect.getItemsById()) {
               array.add(new JsonPrimitive(id));
            }

            attribs.add(setting.getName(), array);
         }
      }

      JsonObject moduleObject = new JsonObject();
      moduleObject.add(m.getName(), attribs);
      return moduleObject;
   }

   public void delete(@NotNull File file) {
      file.delete();
   }

   public void delete(String name) {
      File file = new File(CONFIGS_FOLDER, name + ".th");
      if (file.exists()) {
         this.delete(file);
      }
   }

   public List<String> getConfigList() {
      if (MAIN_FOLDER.exists() && MAIN_FOLDER.listFiles() != null) {
         List<String> list = new ArrayList<>();
         if (CONFIGS_FOLDER.listFiles() != null) {
            for (File file : Arrays.stream(Objects.requireNonNull(CONFIGS_FOLDER.listFiles())).filter(f -> f.getName().endsWith(".th")).toList()) {
               list.add(file.getName().replace(".th", ""));
            }
         }

         return list;
      } else {
         return null;
      }
   }

   public List<String> getCloudConfigs() {
      List<String> list = new ArrayList<>();

      try {
         URL url = new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/cloudConfigs.txt");
         BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

         String inputLine;
         while ((inputLine = in.readLine()) != null) {
            list.add(inputLine.trim());
         }
      } catch (Exception var5) {
      }

      return list;
   }

   public void saveCurrentConfig() {
      File file = new File("ThunderHackRecode/misc/currentcfg.txt");

      try {
         if (file.exists()) {
            FileWriter writer = new FileWriter(file);
            writer.write(this.currentConfig.getName().replace(".th", ""));
            writer.close();
         } else {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(this.currentConfig.getName().replace(".th", ""));
            writer.close();
         }
      } catch (Exception var3) {
         LogUtils.getLogger().warn(var3.getMessage());
      }
   }

   public File getCurrentConfig() {
      File file = new File("ThunderHackRecode/misc/currentcfg.txt");
      String name = "config";

      try {
         if (file.exists()) {
            Scanner reader = new Scanner(file);

            while (reader.hasNextLine()) {
               name = reader.nextLine();
            }

            reader.close();
         }
      } catch (Exception var4) {
         LogUtils.getLogger().warn(var4.getMessage());
      }

      this.currentConfig = new File(CONFIGS_FOLDER, name + ".th");
      return this.currentConfig;
   }
}
