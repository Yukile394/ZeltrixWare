package thunder.hack.gui.mainmenu;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

public class MainMenuButton {
   private float posX;
   private float posY;
   private float width;
   private float height;
   private final String name;
   private final String subtitle;
   private final String icon;
   private final Runnable action;
   private float hoverProgress = 0.0F;

   public MainMenuButton(float posX, float posY, float width, float height, @NotNull String name, String subtitle, String icon, Runnable action) {
      this.posX = posX;
      this.posY = posY;
      this.width = width;
      this.height = height;
      this.name = name;
      this.subtitle = subtitle;
      this.icon = icon;
      this.action = action;
   }

   public void setBounds(float posX, float posY, float width, float height) {
      this.posX = posX;
      this.posY = posY;
      this.width = width;
      this.height = height;
   }

   public void onRender(DrawContext context, float mouseX, float mouseY) {
      boolean hovered = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.posX, (double)this.posY, (double)this.width, (double)this.height);
      this.hoverProgress = Render2DEngine.scrollAnimate(hovered ? 1.0F : 0.0F, this.hoverProgress, 0.28F);
      float slideX = this.hoverProgress * 3.5F;
      float drawX = this.posX + slideX;
      float drawW = this.width - slideX;
      Color bgColor = Render2DEngine.interpolateColorC(new Color(20, 16, 32, 190), new Color(44, 30, 72, 245), this.hoverProgress);
      Render2DEngine.drawRound(context.getMatrices(), drawX, this.posY, drawW, this.height, 7.0F, bgColor);
      if (this.hoverProgress > 0.02F) {
         Color barColor = Render2DEngine.interpolateColorC(new Color(0, 210, 255), new Color(155, 89, 182), this.hoverProgress);
         Render2DEngine.drawRound(
            context.getMatrices(), drawX + 2.0F, this.posY + 4.0F, 3.0F, this.height - 8.0F, 1.5F, Render2DEngine.applyOpacity(barColor, this.hoverProgress)
         );
      }

      float textStartX = drawX + 12.0F;
      if (this.icon != null && !this.icon.isEmpty()) {
         Color iconColor = Render2DEngine.interpolateColorC(new Color(140, 135, 160, 200), new Color(0, 210, 255, 255), this.hoverProgress);
         FontRenderers.sf_bold
            .drawString(context.getMatrices(), this.icon, (double)textStartX, (double)(this.posY + this.height / 2.0F - 4.5F), iconColor.getRGB());
         textStartX += 16.0F;
      }

      Color textColor = Render2DEngine.interpolateColorC(new Color(215, 215, 225), Color.WHITE, this.hoverProgress);
      if (this.subtitle != null && !this.subtitle.isEmpty()) {
         FontRenderers.sf_bold.drawString(context.getMatrices(), this.name, (double)textStartX, (double)(this.posY + 5.0F), textColor.getRGB());
         FontRenderers.sf_medium_mini
            .drawString(
               context.getMatrices(),
               this.subtitle,
               (double)textStartX,
               (double)(this.posY + 19.0F),
               Render2DEngine.applyOpacity(new Color(135, 130, 155).getRGB(), 0.75F + this.hoverProgress * 0.25F)
            );
      } else {
         FontRenderers.sf_bold
            .drawString(context.getMatrices(), this.name, (double)textStartX, (double)(this.posY + this.height / 2.0F - 4.5F), textColor.getRGB());
      }
   }

   public boolean onClick(int mouseX, int mouseY) {
      boolean hovered = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.posX, (double)this.posY, (double)this.width, (double)this.height);
      if (hovered && this.action != null) {
         this.action.run();
         return true;
      } else {
         return false;
      }
   }
}
