package thunder.hack.gui.clickui.impl;

import java.awt.Color;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.AnimationUtility;

public class ModeElement extends AbstractElement {
   public Setting setting2;
   private boolean open;
   private double wheight;
   private String prevMode;
   private float animation;
   private float animation2;

   public ModeElement(Setting setting) {
      super(setting);
      this.setting2 = setting;
      this.prevMode = setting.currentEnumName();
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.animation = AnimationUtility.fast(this.animation, this.open ? 0.0F : 1.0F, 15.0F);
      this.animation2 = AnimationUtility.fast(this.animation2, 1.0F, 10.0F);
      float tx = this.x + this.width - 11.0F;
      float ty = this.y + 7.5F;
      MatrixStack matrixStack = context.getMatrices();
      float thetaRotation = -180.0F * this.animation;
      matrixStack.push();
      matrixStack.translate(tx, ty, 0.0F);
      matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(thetaRotation));
      matrixStack.translate(-tx, -ty, 0.0F);
      matrixStack.translate(this.x + this.width - 14.0F, this.y + 4.5F, 0.0F);
      context.drawTexture(TextureStorage.guiArrow, 0, 0, 0.0F, 0.0F, 6, 6, 6, 6);
      matrixStack.translate(-(this.x + this.width - 14.0F), -this.y - 4.5F, 0.0F);
      matrixStack.pop();
      if (this.setting.group != null) {
         Render2DEngine.drawRect(context.getMatrices(), this.x + 4.0F, this.y, 1.0F, 17.0F, HudEditor.getColor(1));
      }

      FontRenderers.sf_medium_mini
         .drawString(
            matrixStack,
            this.setting2.getName(),
            (double)((this.setting.group != null ? 2.0F : 0.0F) + this.x + 6.0F),
            (double)this.y + this.wheight / 2.0 - 3.0 + 3.0,
            new Color(-1).getRGB()
         );
      if ((double)this.animation2 < 0.99 && !Objects.equals(this.setting2.currentEnumName(), this.prevMode)) {
         FontRenderers.sf_medium_mini
            .drawString(
               matrixStack,
               this.prevMode,
               (double)((int)(this.x + this.width - 18.0F - FontRenderers.sf_medium_mini.getStringWidth(this.prevMode))),
               3.0 + ((double)this.y + this.wheight / 2.0 - 3.0) - (double)(this.animation2 * 5.0F),
               Render2DEngine.applyOpacity(new Color(-1), this.animation2)
            );
         FontRenderers.sf_medium_mini
            .drawString(
               matrixStack,
               this.setting2.currentEnumName(),
               (double)(this.x + this.width - 18.0F - FontRenderers.sf_medium_mini.getStringWidth(this.setting2.currentEnumName())),
               3.0 + ((double)this.y + this.wheight / 2.0 - 3.0) - (double)(this.animation2 * 5.0F) + 5.0,
               Render2DEngine.applyOpacity(new Color(-1), 1.0F - this.animation2)
            );
      } else {
         FontRenderers.sf_medium_mini
            .drawString(
               matrixStack,
               this.setting2.currentEnumName(),
               (double)(this.x + this.width - 18.0F - FontRenderers.sf_medium_mini.getStringWidth(this.setting.currentEnumName())),
               3.0 + ((double)this.y + this.wheight / 2.0 - 3.0),
               new Color(-1).getRGB()
            );
      }

      if (this.open) {
         Color color = HudEditor.getColor(0);
         double offsetY = 0.0;

         for (int i = 0; i <= this.setting2.getModes().length - 1; i++) {
            FontRenderers.sf_medium_mini
               .drawString(
                  matrixStack,
                  this.setting2.getModes()[i],
                  (double)(this.x + this.width / 2.0F - FontRenderers.sf_medium_mini.getStringWidth(this.setting2.getModes()[i]) / 2.0F),
                  (double)this.y + this.wheight + 2.0 + offsetY,
                  this.setting2.currentEnumName().equalsIgnoreCase(this.setting2.getModes()[i]) ? color.getRGB() : new Color(-1).getRGB()
               );
            offsetY += 12.0;
         }
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y, (double)this.width, this.wheight)) {
         if (button == 0) {
            this.prevMode = this.setting2.currentEnumName();
            this.animation2 = 0.0F;
            this.setting2.increaseEnum();
            Managers.SOUND.playBoolean();
         } else {
            this.open = !this.open;
            if (this.open) {
               Managers.SOUND.playSwipeIn();
            } else {
               Managers.SOUND.playSwipeOut();
            }
         }
      }

      if (this.open) {
         double offsetY = 0.0;

         for (int i = 0; i <= this.setting2.getModes().length - 1; i++) {
            if (Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y + this.wheight + offsetY, (double)this.width, 12.0)
               && button == 0) {
               this.prevMode = this.setting2.currentEnumName();
               this.animation2 = 0.0F;
               this.setting2.setEnumByNumber(i);
               Managers.SOUND.playBoolean();
            }

            offsetY += 12.0;
         }
      }

      super.mouseClicked(mouseX, mouseY, button);
   }

   public void setWHeight(double height) {
      this.wheight = height;
   }

   public boolean isOpen() {
      return this.open;
   }
}
