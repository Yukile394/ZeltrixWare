package thunder.hack.gui.thundergui.components;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Util;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.core.Core;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.AnimationUtility;

public class FriendComponent {
   float scroll_animation = 0.0F;
   private Identifier head = null;
   private final String name;
   private int posX;
   private int posY;
   private int progress;
   private int fade;
   private final int index;
   private boolean first_open = true;
   private float scrollPosY;
   private float prevPosY;

   public FriendComponent(String name, int posX, int posY, int index) {
      this.name = name;
      this.posX = posX;
      this.posY = posY;
      this.fade = 0;
      this.index = index * 5;
      this.loadHead(name);
      this.scrollPosY = (float)posY;
      this.scroll_animation = 0.0F;
   }

   public void loadHead(String name) {
      if (Core.HEADS.containsKey(name)) {
         this.head = Core.HEADS.get(name);
      }

      Util.getMainWorkerExecutor().execute(() -> {
         try {
            NativeImageBackedTexture nIBT = getHeadFromURL("https://minotar.net/helm/" + name + "/22.png");
            this.head = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("th-heads-" + name, nIBT);
            Core.HEADS.put(name, this.head);
         } catch (Exception var3) {
            this.head = null;
         }
      });
   }

   public static NativeImageBackedTexture getHeadFromURL(String HeadStringURL) {
      try {
         URL capeURL = new URL(HeadStringURL);
         return getHeadFromStream(capeURL.openStream());
      } catch (IOException var2) {
         return null;
      }
   }

   public static NativeImageBackedTexture getHeadFromStream(InputStream image) {
      NativeImage Head = null;

      try {
         Head = NativeImage.read(image);
      } catch (IOException var3) {
         var3.printStackTrace();
      }

      return Head != null ? new NativeImageBackedTexture(parseHead(Head)) : null;
   }

   public static NativeImage parseHead(NativeImage image) {
      int imageWidth = 22;
      int imageSrcWidth = image.getWidth();
      int srcHeight = image.getHeight();
      int imageSrcHeight = image.getHeight();

      int imageHeight;
      for (imageHeight = 22; imageWidth < imageSrcWidth || imageHeight < imageSrcHeight; imageHeight *= 2) {
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

   public void render(DrawContext context, int MouseX, int MouseY) {
      if (this.scrollPosY != (float)this.posY) {
         this.scroll_animation = AnimationUtility.fast(this.scroll_animation, 1.0F, 15.0F);
         this.posY = (int)Render2DEngine.interpolate((double)this.prevPosY, (double)this.scrollPosY, (double)this.scroll_animation);
      }

      if (this.posY <= ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height && this.posY >= ThunderGui.getInstance().main_posY) {
         Render2DEngine.drawRound(
            context.getMatrices(),
            (float)(this.posX + 5),
            (float)this.posY,
            285.0F,
            30.0F,
            4.0F,
            Render2DEngine.applyOpacity(new Color(44, 35, 52, 255), this.getFadeFactor())
         );
         if (this.first_open) {
            Render2DEngine.addWindow(
               context.getMatrices(), (float)(this.posX + 5), (float)this.posY, (float)(this.posX + 5 + 285), (float)(this.posY + 30), 1.0
            );
            Render2DEngine.drawBlurredShadow(
               context.getMatrices(),
               (float)(MouseX - 20),
               (float)(MouseY - 20),
               40.0F,
               40.0F,
               60,
               Render2DEngine.applyOpacity(new Color(-1017816450, true), this.getFadeFactor())
            );
            Render2DEngine.popWindow();
            this.first_open = false;
         }

         if (this.isHovered(MouseX, MouseY)) {
            Render2DEngine.addWindow(
               context.getMatrices(), (float)(this.posX + 5), (float)this.posY, (float)(this.posX + 5 + 285), (float)(this.posY + 30), 1.0
            );
            Render2DEngine.drawBlurredShadow(
               context.getMatrices(),
               (float)(MouseX - 20),
               (float)(MouseY - 20),
               40.0F,
               40.0F,
               60,
               Render2DEngine.applyOpacity(new Color(-1017816450, true), this.getFadeFactor())
            );
            Render2DEngine.popWindow();
         }

         Render2DEngine.drawRound(
            context.getMatrices(),
            (float)(this.posX + 266),
            (float)(this.posY + 8),
            14.0F,
            14.0F,
            2.0F,
            Render2DEngine.applyOpacity(new Color(25, 20, 30, 255), this.getFadeFactor())
         );
         if (Render2DEngine.isHovered((double)MouseX, (double)MouseY, (double)(this.posX + 268), (double)(this.posY + 10), 10.0, 10.0)) {
            Render2DEngine.drawRound(
               context.getMatrices(),
               (float)(this.posX + 268),
               (float)(this.posY + 10),
               10.0F,
               10.0F,
               2.0F,
               Render2DEngine.applyOpacity(new Color(65, 1, 13, 255), this.getFadeFactor())
            );
         } else {
            Render2DEngine.drawRound(
               context.getMatrices(),
               (float)(this.posX + 268),
               (float)(this.posY + 10),
               10.0F,
               10.0F,
               2.0F,
               Render2DEngine.applyOpacity(new Color(94, 1, 18, 255), this.getFadeFactor())
            );
         }

         FontRenderers.icons
            .drawString(context.getMatrices(), "w", (double)(this.posX + 268), (double)(this.posY + 13), Render2DEngine.applyOpacity(-1, this.getFadeFactor()));
         context.drawTexture(Objects.requireNonNullElse(this.head, TextureStorage.crackedSkin), this.posX + 10, this.posY + 3, 0.0F, 0.0F, 22, 22, 22, 22);
         FontRenderers.modules
            .drawString(
               context.getMatrices(), this.name, (double)(this.posX + 37), (double)(this.posY + 6), Render2DEngine.applyOpacity(-1, this.getFadeFactor())
            );
         boolean online = Module.mc.player.networkHandler.getPlayerList().stream().map(p -> p.getProfile().getName()).toList().contains(this.name);
         FontRenderers.settings
            .drawString(
               context.getMatrices(),
               online ? "online" : "offline",
               (double)(this.posX + 37),
               (double)(this.posY + 17),
               online
                  ? Render2DEngine.applyOpacity(new Color(-16025088, true).getRGB(), this.getFadeFactor())
                  : Render2DEngine.applyOpacity(new Color(-4342339, true).getRGB(), this.getFadeFactor())
            );
      }
   }

   private float getFadeFactor() {
      return (float)this.fade / (5.0F + (float)this.index);
   }

   public void onTick() {
      if (this.progress > 4) {
         this.progress = 0;
      }

      this.progress++;
      if (this.fade < 10 + this.index) {
         this.fade++;
      }
   }

   private boolean isHovered(int mouseX, int mouseY) {
      return mouseX > this.posX && mouseX < this.posX + 295 && mouseY > this.posY && mouseY < this.posY + 30;
   }

   public void movePosition(float deltaX, float deltaY) {
      this.posY = (int)((float)this.posY + deltaY);
      this.posX = (int)((float)this.posX + deltaX);
      this.scrollPosY = (float)this.posY;
   }

   public void mouseClicked(int MouseX, int MouseY, int clickedButton) {
      if (this.posY <= ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height && this.posY >= ThunderGui.getInstance().main_posY) {
         if (Render2DEngine.isHovered((double)MouseX, (double)MouseY, (double)(this.posX + 268), (double)(this.posY + 10), 10.0, 10.0)) {
            Managers.FRIEND.removeFriend(this.name);
            ThunderGui.getInstance().loadFriends();
         }
      }
   }

   public double getPosX() {
      return (double)this.posX;
   }

   public double getPosY() {
      return (double)this.posY;
   }

   public void scrollElement(float deltaY) {
      this.scroll_animation = 0.0F;
      this.prevPosY = (float)this.posY;
      this.scrollPosY += deltaY;
   }
}
