package thunder.hack.gui.clickui.impl;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.StringHelper;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

public class StringElement extends AbstractElement {
   public boolean listening;
   private String currentString = "";

   public StringElement(Setting setting) {
      super(setting);
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      Render2DEngine.drawRect(context.getMatrices(), this.getX() + 5.0F, this.getY() + 2.0F, this.getWidth() - 11.0F, 10.0F, new Color(-1811939328, true));
      FontRenderers.sf_medium_mini
         .drawString(
            context.getMatrices(),
            this.listening
               ? this.currentString + (Module.mc.player != null && Module.mc.player.age % 5 != 0 ? "" : "_")
               : (String)this.setting.getValue(),
            (double)(this.x + 6.0F),
            (double)(this.y + this.height / 2.0F),
            -1
         );
      if (Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)(this.getX() + 5.0F), (double)(this.getY() + 2.0F), (double)(this.getWidth() - 11.0F), 10.0
      )) {
         if (GLFW.glfwGetPlatform() != 393219) {
            GLFW.glfwSetCursor(Module.mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(221186));
         }

         ClickGUI.anyHovered = true;
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (this.hovered && button == 0) {
         this.listening = !this.listening;
      }

      if (this.listening) {
         ThunderHack.currentKeyListener = ThunderHack.KeyListening.Strings;
         this.currentString = (String)this.setting.getValue();
      }

      super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public void charTyped(char key, int keyCode) {
      if (StringHelper.isValidChar(key)) {
         this.currentString = this.currentString + key;
      }
   }

   @Override
   public void keyTyped(int keyCode) {
      if (ThunderHack.currentKeyListener == ThunderHack.KeyListening.Strings) {
         if (this.listening) {
            switch (keyCode) {
               case 32:
                  this.currentString = this.currentString + " ";
               case 256:
               default:
                  break;
               case 257:
                  this.setting.setValue(this.currentString != null && !this.currentString.isEmpty() ? this.currentString : this.setting.getDefaultValue());
                  this.currentString = "";
                  this.listening = !this.listening;
                  break;
               case 259:
                  this.currentString = SliderElement.removeLastChar(this.currentString);
            }
         }
      }
   }
}
