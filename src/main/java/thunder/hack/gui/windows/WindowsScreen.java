package thunder.hack.gui.windows;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class WindowsScreen extends Screen {
   private List<WindowBase> windows = new ArrayList<>();
   public static WindowBase lastClickedWindow;
   public static WindowBase draggingWindow;
   private static final Identifier clickGuiIcon = Identifier.of("thunderhack", "textures/gui/elements/clickgui.png");

   public WindowsScreen(WindowBase... windows) {
      super(Text.of("THWindows"));
      this.windows.clear();
      lastClickedWindow = null;
      this.windows = Arrays.stream(windows).toList();
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      if (Module.fullNullCheck()) {
         this.renderBackground(context, mouseX, mouseY, delta);
      }

      MatrixStack matrices = context.getMatrices();
      int i = IManager.mc.getWindow().getScaledWidth() / 2;
      float offset = (float)this.windows.size() * 20.0F / -2.0F - 23.0F;
      Render2DEngine.drawHudBase(
         matrices,
         (float)i + offset - 1.5F,
         (float)(IManager.mc.getWindow().getScaledHeight() - 25),
         (float)this.windows.size() * 20.0F + 23.0F,
         19.0F,
         HudEditor.hudRound.getValue()
      );
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(
         1.0F,
         1.0F,
         1.0F,
         Render2DEngine.isHovered(
               (double)mouseX, (double)mouseY, (double)((float)i + offset + 1.0F), (double)(IManager.mc.getWindow().getScaledHeight() - 23), 15.0, 15.0
            )
            ? 0.95F
            : 0.7F
      );
      context.drawTexture(clickGuiIcon, (int)((float)i + offset) + 1, IManager.mc.getWindow().getScaledHeight() - 23, 15, 15, 0.0F, 0.0F, 15, 15, 15, 15);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
      Render2DEngine.drawLine(
         (float)i + offset + 20.0F,
         (float)(IManager.mc.getWindow().getScaledHeight() - 23),
         (float)i + offset + 20.0F,
         (float)(IManager.mc.getWindow().getScaledHeight() - 9),
         Color.GRAY.getRGB()
      );
      offset += 23.0F;

      for (WindowBase w : this.windows) {
         Color c = Render2DEngine.isHovered(
               (double)mouseX, (double)mouseY, (double)((float)i + offset), (double)(IManager.mc.getWindow().getScaledHeight() - 24), 17.0, 17.0
            )
            ? new Color(2083467055, true)
            : (!w.isVisible() ? new Color(2082348574, true) : new Color(2084256571, true));
         Render2DEngine.drawRect(
            matrices, (float)i + offset, (float)(IManager.mc.getWindow().getScaledHeight() - 24), 17.0F, 17.0F, HudEditor.hudRound.getValue(), 0.7F, c, c, c, c
         );
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.setShaderColor(
            1.0F,
            1.0F,
            1.0F,
            Render2DEngine.isHovered(
                  (double)mouseX, (double)mouseY, (double)((float)i + offset + 1.0F), (double)(IManager.mc.getWindow().getScaledHeight() - 23), 15.0, 15.0
               )
               ? 0.95F
               : 0.7F
         );
         context.drawTexture(
            w.getIcon() != null ? w.getIcon() : TextureStorage.configIcon,
            (int)((float)i + offset) + 3,
            IManager.mc.getWindow().getScaledHeight() - 21,
            11,
            11,
            0.0F,
            0.0F,
            11,
            11,
            11,
            11
         );
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
         offset += 20.0F;
      }

      this.windows.stream().filter(WindowBase::isVisible).forEach(wx -> {
         if (wx != lastClickedWindow) {
            wx.render(context, mouseX, mouseY);
         }
      });
      if (lastClickedWindow != null && lastClickedWindow.isVisible()) {
         lastClickedWindow.render(context, mouseX, mouseY);
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.windows.forEach(w -> w.mouseReleased(mouseX, mouseY, button));
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.windows.stream().filter(WindowBase::isVisible).forEach(wx -> wx.mouseClicked(mouseX, mouseY, button));
      int i = IManager.mc.getWindow().getScaledWidth() / 2;
      float offset = (float)this.windows.size() * 20.0F / -2.0F - 23.0F;
      if (Render2DEngine.isHovered(mouseX, mouseY, (double)((float)i + offset + 1.0F), (double)(IManager.mc.getWindow().getScaledHeight() - 23), 15.0, 15.0)) {
         IManager.mc.setScreen(ClickGUI.getClickGui());
      }

      offset += 23.0F;

      for (WindowBase w : this.windows) {
         if (Render2DEngine.isHovered(mouseX, mouseY, (double)((float)i + offset), (double)(IManager.mc.getWindow().getScaledHeight() - 24), 17.0, 17.0)) {
            w.setVisible(!w.isVisible());
         }

         offset += 20.0F;
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.windows.stream().filter(WindowBase::isVisible).forEach(w -> w.keyPressed(keyCode, scanCode, modifiers));
      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean charTyped(char key, int keyCode) {
      this.windows.stream().filter(WindowBase::isVisible).forEach(w -> w.charTyped(key, keyCode));
      return super.charTyped(key, keyCode);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.windows.stream().filter(WindowBase::isVisible).forEach(w -> w.mouseScrolled((int)(verticalAmount * 5.0)));
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }
}
