package thunder.hack.gui.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Util;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

public class CreditsScreen extends Screen {
   public ArrayList<CreditsScreen.Contributor> contributors = new ArrayList<>();
   private static int scroll;
   private static final int SCROLL_SPEED = 1;
   private static CreditsScreen INSTANCE = new CreditsScreen();

   protected CreditsScreen() {
      super(Text.of("CreditsScreen"));
      INSTANCE = this;

      for (String line : ThunderHack.contributors) {
         if (line != null) {
            String name = line.split(";")[0];
            String avatar = line.split(";")[1];
            String role = line.split(";")[2];
            String description = line.split(";")[3];
            String clickAction = line.split(";")[4];
            getInstance().contributors.add(new CreditsScreen.Contributor(name, getAvatar(avatar), role, description.replace('\u0442', '\n'), clickAction));
         }
      }
   }

   public static CreditsScreen getInstance() {
      scroll = 150;
      if (INSTANCE == null) {
         INSTANCE = new CreditsScreen();
      }

      return INSTANCE;
   }

   public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
      float halfOfWidth = (float)Module.mc.getWindow().getScaledWidth() / 2.0F;
      float halfOfHeight = (float)Module.mc.getWindow().getScaledHeight() / 2.0F;
      float globalOffset = (float)(this.contributors.size() * 150) / 2.0F;
      this.renderBackground(context, mouseX, mouseY, delta);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      int offset = 0;

      for (CreditsScreen.Contributor contributor : this.contributors) {
         float cX = halfOfWidth + (float)offset - globalOffset + (float)scroll;
         float cY = halfOfHeight - 120.0F;
         Render2DEngine.drawHudBase(context.getMatrices(), cX, cY, 140.0F, 240.0F, 20.0F, false);
         FontRenderers.sf_medium
            .drawGradientString(
               context.getMatrices(), contributor.name, cX + 70.0F - FontRenderers.sf_medium.getStringWidth(contributor.name) / 2.0F, halfOfHeight - 57.0F, 30
            );
         FontRenderers.sf_medium
            .drawCenteredString(context.getMatrices(), contributor.role, (double)(cX + 70.0F), (double)(halfOfHeight - 48.0F), new Color(8487297).getRGB());
         Render2DEngine.horizontalGradient(
            context.getMatrices(), cX + 2.0F, cY + 90.0F, cX + 70.0F, cY + 91.0F, Render2DEngine.injectAlpha(new Color(-1), 0), new Color(-1)
         );
         Render2DEngine.horizontalGradient(
            context.getMatrices(), cX + 70.0F, cY + 90.0F, cX + 138.0F, cY + 91.0F, new Color(-1), Render2DEngine.injectAlpha(new Color(-1), 0)
         );
         Render2DEngine.drawRound(context.getMatrices(), cX + 5.0F, cY + 100.0F, 130.0F, 130.0F, 8.0F, new Color(1929379840, true));
         FontRenderers.sf_medium
            .drawString(context.getMatrices(), contributor.description, (double)(cX + 10.0F), (double)(cY + 108.0F), new Color(8487298).getRGB());
         if (contributor.avatar != null) {
            context.drawTexture(contributor.avatar, (int)(cX + 70.0F - 24.0F), (int)(halfOfHeight - 110.0F), 48, 48, 0.0F, 0.0F, 96, 96, 96, 96);
         }

         if (Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)cX, (double)cY, 140.0, 240.0) && !Objects.equals(contributor.clickAction, "")) {
            Render2DEngine.drawRound(context.getMatrices(), cX, cY, 140.0F, 240.0F, 8.0F, new Color(100663295, true));
         }

         offset += 150;
      }

      RenderSystem.disableBlend();
      Render2DEngine.drawHudBase(
         context.getMatrices(),
         (float)(Module.mc.getWindow().getScaledWidth() - 40),
         (float)(Module.mc.getWindow().getScaledHeight() - 40),
         30.0F,
         30.0F,
         5.0F,
         Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)(Module.mc.getWindow().getScaledWidth() - 60),
               (double)(Module.mc.getWindow().getScaledHeight() - 60),
               40.0,
               40.0
            )
            ? 0.7F
            : 1.0F
      );
      RenderSystem.setShaderColor(
         1.0F,
         1.0F,
         1.0F,
         Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)(Module.mc.getWindow().getScaledWidth() - 40),
               (double)(Module.mc.getWindow().getScaledHeight() - 40),
               30.0,
               30.0
            )
            ? 0.7F
            : 1.0F
      );
      context.drawTexture(
         TextureStorage.thTeam, Module.mc.getWindow().getScaledWidth() - 40, Module.mc.getWindow().getScaledHeight() - 40, 30, 30, 0.0F, 0.0F, 30, 30, 30, 30
      );
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      float halfOfWidth = (float)Module.mc.getWindow().getScaledWidth() / 2.0F;
      float halfOfHeight = (float)Module.mc.getWindow().getScaledHeight() / 2.0F;
      float globalOffset = (float)(this.contributors.size() * 150) / 2.0F;
      int offset = 0;

      for (CreditsScreen.Contributor contributor : this.contributors) {
         float cX = (float)(
            (double)(halfOfWidth + (float)offset - globalOffset)
               + Render2DEngine.interpolate((double)scroll, (double)(scroll + 1), (double)Render3DEngine.getTickDelta())
         );
         float cY = halfOfHeight - 120.0F;
         if (Render2DEngine.isHovered(mouseX, mouseY, (double)cX, (double)cY, 140.0, 240.0) && !Objects.equals(contributor.clickAction, "none")) {
            Util.getOperatingSystem().open(URI.create(contributor.clickAction));
         }

         offset += 150;
      }

      if (Render2DEngine.isHovered(
         mouseX, mouseY, (double)(Module.mc.getWindow().getScaledWidth() - 40), (double)(Module.mc.getWindow().getScaledHeight() - 40), 40.0, 40.0
      )) {
         Module.mc.setScreen(MainMenuScreen.getInstance());
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public static Identifier getAvatar(String name) {
      try {
         NativeImageBackedTexture nIBT = getAvatarFromURL("https://cdn.discordapp.com/avatars/" + name + ".png?size=96");
         return nIBT != null ? MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("th-contributors-" + (int)MathUtility.random(0.0F, 1000000.0F), nIBT) : null;
      } catch (Exception var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static NativeImageBackedTexture getAvatarFromURL(String HeadStringURL) {
      try {
         return getAvatarFromStream(new URL(HeadStringURL).openStream());
      } catch (IOException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static NativeImageBackedTexture getAvatarFromStream(InputStream image) {
      NativeImage pic = null;

      try {
         pic = NativeImage.read(image);
      } catch (IOException var3) {
         var3.printStackTrace();
      }

      return pic != null ? new NativeImageBackedTexture(parseAvatar(pic)) : null;
   }

   public static NativeImage parseAvatar(NativeImage image) {
      NativeImage imgNew = new NativeImage(96, 96, true);

      for (int x = 0; x < 96; x++) {
         for (int y = 0; y < 96; y++) {
            if (Math.hypot((double)(x - 48), (double)(y - 48)) > 45.0) {
               imgNew.setColor(
                  x,
                  y,
                  Render2DEngine.injectAlpha(
                        new Color(image.getColor(x, y)), (int)((float)(48.0 - Math.hypot((double)(x - 48), (double)(y - 48))) / 3.0F * 255.0F)
                     )
                     .getRGB()
               );
            } else {
               imgNew.setColor(x, y, image.getColor(x, y));
            }
         }
      }

      image.close();
      return imgNew;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      scroll += (int)(verticalAmount * 5.0);
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public void tick() {
      scroll--;
      if (scroll <= -(this.contributors.size() * 150) + 100) {
         scroll = 0;
      }
   }

   public static record Contributor(String name, Identifier avatar, String role, String description, String clickAction) {
   }
}
