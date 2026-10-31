package thunder.hack.setting.impl;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

public class EnumConverter extends Converter<Enum, JsonElement> {
   private final Class<? extends Enum> clazz;

   public EnumConverter(Class<? extends Enum> clazz) {
      this.clazz = clazz;
   }

   public static int currentEnum(Enum clazz) {
      Enum[] all = (Enum[])clazz.getDeclaringClass().getEnumConstants();
      for (int i = 0; i < all.length; i++) {
         if (all[i].name().equalsIgnoreCase(clazz.name())) {
            return i;
         }
      }

      return -1;
   }

   public static Enum increaseEnum(Enum clazz) {
      Enum[] all = (Enum[])clazz.getDeclaringClass().getEnumConstants();
      int index = currentEnum(clazz);
      return index + 1 < all.length ? all[index + 1] : all[0];
   }

   public static Enum<?> setEnumInt(@NotNull Enum<?> clazz, int id) {
      Enum<?>[] all = (Enum<?>[])clazz.getDeclaringClass().getEnumConstants();
      return id >= 0 && id < all.length ? all[id] : all[0];
   }

   public static String[] getNames(Enum clazz) {
      Enum[] all = (Enum[])clazz.getDeclaringClass().getEnumConstants();
      String[] names = new String[all.length];
      for (int i = 0; i < all.length; i++) {
         names[i] = all[i].name();
      }

      return names;
   }

   public static String getProperName(Enum clazz) {
      return clazz.name();
   }

   public JsonElement doForward(Enum anEnum) {
      return new JsonPrimitive(anEnum.toString());
   }

   public Enum doBackward(JsonElement jsonElement) {
      try {
         return Enum.valueOf(this.clazz, jsonElement.getAsString());
      } catch (IllegalArgumentException var3) {
         return null;
      }
   }
}
