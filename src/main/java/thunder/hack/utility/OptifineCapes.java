package thunder.hack.utility;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;

public final class OptifineCapes {
   public static void loadPlayerCape(GameProfile player, OptifineCapes.ReturnCapeTexture response) {
      try {
         String uuid = player.getId().toString();
         NativeImageBackedTexture nIBT = getCapeFromURL(String.format("http://s.optifine.net/capes/%s.png", player.getName()));
         Identifier capeTexture = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("th-cape-" + uuid, nIBT);
         response.response(capeTexture);
      } catch (Exception var5) {
      }
   }

   public static NativeImageBackedTexture getCapeFromURL(String capeStringURL) {
      try {
         URL capeURL = new URL(capeStringURL);
         return getCapeFromStream(capeURL.openStream());
      } catch (IOException var2) {
         return null;
      }
   }

   public static NativeImageBackedTexture getCapeFromStream(InputStream image) {
      NativeImage cape = null;

      try {
         cape = NativeImage.read(image);
      } catch (IOException var3) {
         var3.printStackTrace();
      }

      return cape != null ? new NativeImageBackedTexture(parseCape(cape)) : null;
   }

   public static NativeImage parseCape(NativeImage image) {
      int imageWidth = 64;
      int imageHeight = 32;
      int imageSrcWidth = image.getWidth();
      int srcHeight = image.getHeight();

      for (int imageSrcHeight = image.getHeight(); imageWidth < imageSrcWidth || imageHeight < imageSrcHeight; imageHeight *= 2) {
         imageWidth *= 2;
      }

      NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);

      for (int x = 0; x < imageSrcWidth; x++) {
         for (int y = 0; y < srcHeight; y++) {
            imgNew.setColor(x, y, image.getColor(x, y));
         }
      }

      image.close();
      return imgNew;
   }

   public interface ReturnCapeTexture {
      void response(Identifier var1);
   }
}
