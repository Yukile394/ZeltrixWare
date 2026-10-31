package thunder.hack.gui.clickui.impl;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class BooleanElement extends AbstractElement {
   float animation = 0.0F;
   float animation2 = 0.0F;

   public BooleanElement(Setting setting) {
      super(setting);
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      Render2DEngine.drawRound(
         context.getMatrices(),
         this.x + this.width - 21.0F,
         this.y + this.height / 2.0F - 4.0F,
         15.0F,
         8.0F,
         1.0F,
         7.0F * this.animation > 4.0F ? HudEditor.getColor(0) : new Color(687865855, true)
      );
      this.animation = AnimationUtility.fast(this.animation, this.setting.getValue() ? 1.0F : 0.0F, 20.0F);
      this.animation2 = AnimationUtility.fast(this.animation2, this.setting.getValue() ? 1.0F : 0.0F, 8.0F);
      Render2DEngine.drawRound(
         context.getMatrices(), this.x + this.width - 20.0F + 7.0F * this.animation, this.y + this.height / 2.0F - 3.0F, 6.0F, 6.0F, 1.0F, new Color(-1)
      );
      Render2DEngine.drawRound(
         context.getMatrices(), this.x + this.width - 20.0F + 7.0F * this.animation2, this.y + this.height / 2.0F - 3.0F, 6.0F, 6.0F, 1.0F, new Color(-1)
      );
      if (7.0F * this.animation > 4.0F) {
         FontRenderers.sf_bold_mini
            .drawString(
               context.getMatrices(), "v", (double)(this.x + this.width - 19.0F), (double)(this.y + this.height / 2.0F - 2.0F), new Color(-1).getRGB()
            );
      } else {
         FontRenderers.sf_bold_mini
            .drawString(
               context.getMatrices(), "x", (double)(this.x + this.width - 12.0F), (double)(this.y + this.height / 2.0F - 2.0F), new Color(-1).getRGB()
            );
      }

      if (Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)(this.x + this.width - 21.0F), (double)(this.y + this.height / 2.0F - 4.0F), 15.0, 8.0
      )) {
         if (GLFW.glfwGetPlatform() != 393219) {
            GLFW.glfwSetCursor(IManager.mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(221188));
         }

         ClickGUI.anyHovered = true;
      }

      FontRenderers.sf_medium_mini
         .drawString(
            context.getMatrices(),
            this.setting.getName(),
            (double)((this.setting.group != null ? 2.0F : 0.0F) + this.x + 6.0F),
            (double)(this.y + this.height / 2.0F - 3.0F + 2.0F),
            new Color(-1).getRGB()
         );
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (this.hovered && button == 0) {
         this.setting.setValue(!(Boolean)this.setting.getValue());
         Managers.SOUND.playBoolean();
      }

      super.mouseClicked(mouseX, mouseY, button);
   }
}
