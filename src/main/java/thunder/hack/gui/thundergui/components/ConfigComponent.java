package thunder.hack.gui.thundergui.components;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.client.ThunderHackGui;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class ConfigComponent {
   float scroll_animation = 0.0F;
   private final String name;
   private final String date;
   private int posX;
   private int posY;
   private int progress;
   private int fade;
   private final int index;
   private boolean first_open = true;
   private float scrollPosY;
   private float prevPosY;

   public ConfigComponent(String name, String date, int posX, int posY, int index) {
      this.name = name;
      this.date = date;
      this.posX = posX;
      this.posY = posY;
      this.fade = 0;
      this.index = index * 5;
      this.scrollPosY = (float)posY;
      this.scroll_animation = 0.0F;
   }

   public void render(DrawContext context, int MouseX, int MouseY) {
      if (this.scrollPosY != (float)this.posY) {
         this.scroll_animation = AnimationUtility.fast(this.scroll_animation, 1.0F, 15.0F);
         this.posY = (int)Render2DEngine.interpolate((double)this.prevPosY, (double)this.scrollPosY, (double)this.scroll_animation);
      }

      if (this.posY <= ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height && this.posY >= ThunderGui.getInstance().main_posY) {
         if (Managers.CONFIG.currentConfig.getName().equals(this.name + ".th")) {
            Render2DEngine.drawGradientRound(
               context.getMatrices(),
               (float)(this.posX + 5),
               (float)this.posY,
               285.0F,
               30.0F,
               4.0F,
               Render2DEngine.applyOpacity(new Color(55, 44, 66, 255), this.getFadeFactor()),
               Render2DEngine.applyOpacity(new Color(25, 20, 30, 255), this.getFadeFactor()),
               Render2DEngine.applyOpacity(ThunderHackGui.onColor1.getValue().getColorObject(), this.getFadeFactor()),
               Render2DEngine.applyOpacity(ThunderHackGui.onColor2.getValue().getColorObject(), this.getFadeFactor())
            );
         } else {
            Render2DEngine.drawRound(
               context.getMatrices(),
               (float)(this.posX + 5),
               (float)this.posY,
               285.0F,
               30.0F,
               4.0F,
               Render2DEngine.applyOpacity(new Color(44, 35, 52, 255), this.getFadeFactor())
            );
         }

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
            (float)(this.posX + 250),
            (float)(this.posY + 8),
            30.0F,
            14.0F,
            2.0F,
            Render2DEngine.applyOpacity(new Color(25, 20, 30, 255), this.getFadeFactor())
         );
         if (Render2DEngine.isHovered((double)MouseX, (double)MouseY, (double)(this.posX + 252), (double)(this.posY + 10), 10.0, 10.0)) {
            Render2DEngine.drawRound(
               context.getMatrices(),
               (float)(this.posX + 252),
               (float)(this.posY + 10),
               10.0F,
               10.0F,
               2.0F,
               Render2DEngine.applyOpacity(new Color(21, 58, 0, 255), this.getFadeFactor())
            );
         } else {
            Render2DEngine.drawRound(
               context.getMatrices(),
               (float)(this.posX + 252),
               (float)(this.posY + 10),
               10.0F,
               10.0F,
               2.0F,
               Render2DEngine.applyOpacity(new Color(32, 89, 0, 255), this.getFadeFactor())
            );
         }

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
            .drawString(context.getMatrices(), "x", (double)(this.posX + 252), (double)(this.posY + 13), Render2DEngine.applyOpacity(-1, this.getFadeFactor()));
         FontRenderers.icons
            .drawString(context.getMatrices(), "w", (double)(this.posX + 268), (double)(this.posY + 13), Render2DEngine.applyOpacity(-1, this.getFadeFactor()));
         FontRenderers.mid_icons
            .drawString(context.getMatrices(), "u", (double)(this.posX + 7), (double)(this.posY + 5), Render2DEngine.applyOpacity(-1, this.getFadeFactor()));
         FontRenderers.modules
            .drawString(
               context.getMatrices(), this.name, (double)(this.posX + 37), (double)(this.posY + 6), Render2DEngine.applyOpacity(-1, this.getFadeFactor())
            );
         FontRenderers.settings
            .drawString(
               context.getMatrices(),
               "updated on: " + this.date,
               (double)(this.posX + 37),
               (double)(this.posY + 17),
               Render2DEngine.applyOpacity(new Color(-4342339, true).getRGB(), this.getFadeFactor())
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
         if (Render2DEngine.isHovered((double)MouseX, (double)MouseY, (double)(this.posX + 252), (double)(this.posY + 10), 10.0, 10.0)) {
            Managers.CONFIG.load(this.name);
         }

         if (Render2DEngine.isHovered((double)MouseX, (double)MouseY, (double)(this.posX + 268), (double)(this.posY + 10), 10.0, 10.0)) {
            Managers.CONFIG.delete(this.name);
            ThunderGui.getInstance().loadConfigs();
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
