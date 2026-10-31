package thunder.hack.gui.clickui.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.StringHelper;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.clickui.AbstractButton;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

public class SearchBar extends AbstractButton {
   public static String moduleName = "";
   public static boolean listening;

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      Render2DEngine.drawGuiBase(
         context.getMatrices(),
         this.x + 4.0F,
         this.y + 1.0F,
         this.width - 8.0F,
         this.height - 2.0F,
         1.0F,
         Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y, (double)this.width, (double)this.height) ? 0.8F : 0.0F
      );
      if (!listening) {
         FontRenderers.sf_medium.drawGradientString(context.getMatrices(), "Search...", this.x + 7.0F, this.y + this.height / 2.0F - 3.0F, 2);
      } else {
         FontRenderers.sf_medium
            .drawGradientString(
               context.getMatrices(),
               moduleName + (Module.mc.player != null && Module.mc.player.age / 10 % 2 != 0 ? "_" : " "),
               this.x + 7.0F,
               this.y + 5.0F,
               2
            );
      }

      if (Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y, (double)this.width, (double)this.height)) {
         if (GLFW.glfwGetPlatform() != 393219) {
            GLFW.glfwSetCursor(Module.mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(221186));
         }

         ClickGUI.anyHovered = true;
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      super.mouseClicked(mouseX, mouseY, button);
      boolean isHovered = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y, (double)this.width, (double)this.height);
      if (isHovered) {
         listening = true;
      } else {
         moduleName = "";
         listening = false;
      }

      if (listening) {
         ThunderHack.currentKeyListener = ThunderHack.KeyListening.Search;
      }
   }

   @Override
   public void charTyped(char key, int keyCode) {
      if (StringHelper.isValidChar(key) && listening) {
         moduleName = moduleName + key;
      }
   }

   @Override
   public void keyTyped(int keyCode) {
      super.keyTyped(keyCode);
      if (keyCode != 70
         || !InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 341) && !InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 345)) {
         if (ThunderHack.currentKeyListener == ThunderHack.KeyListening.Search) {
            if (listening) {
               switch (keyCode) {
                  case 32:
                     moduleName = moduleName + " ";
                     break;
                  case 256:
                  case 257:
                     listening = false;
                     moduleName = "";
                     break;
                  case 259:
                     moduleName = SliderElement.removeLastChar(moduleName);
               }
            }
         }
      } else {
         listening = !listening;
         ThunderHack.currentKeyListener = ThunderHack.KeyListening.Search;
      }
   }
}
