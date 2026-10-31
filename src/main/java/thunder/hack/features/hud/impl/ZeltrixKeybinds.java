package thunder.hack.features.hud.impl;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class ZeltrixKeybinds extends HudElement {
   public final Setting<ZeltrixKeybinds.Mode> mode = new Setting<>("Mode", ZeltrixKeybinds.Mode.Neon);
   private float hAnimation = 95.0F;
   private float vAnimation = 50.0F;
   private float visibleValue = 0.0F;

   public ZeltrixKeybinds() {
      super("ZeltrixKeybinds", 100, 50);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      List<Module> activeModules = Managers.MODULE
         .modules
         .stream()
         .filter(modx -> modx.isOn() && !Objects.equals(modx.getBind().getBind(), "None"))
         .collect(Collectors.toList());
      boolean isEditing = mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui;
      boolean shouldShow = !activeModules.isEmpty() || isEditing;
      this.visibleValue = AnimationUtility.fast(this.visibleValue, shouldShow ? 1.0F : 0.0F, 15.0F);
      if (!(this.visibleValue <= 0.01F)) {
         float x = this.getPosX();
         float y = this.getPosY();
         float panelWidth = 90.0F;
         float titleHeight = 20.0F;
         float itemHeight = 13.0F;
         float spacing = -2.5F;
         float statusPanelWidth = 14.0F;
         float mainPanelWidth = panelWidth - statusPanelWidth - 11.5F;
         int moduleCount = activeModules.isEmpty() && isEditing ? 3 : activeModules.size();
         float contentHeight = Math.max(16.0F, (float)moduleCount * (itemHeight + spacing) - spacing);
         float totalHeight = titleHeight + 7.0F + contentHeight;
         this.hAnimation = AnimationUtility.fast(this.hAnimation, panelWidth, 15.0F);
         this.vAnimation = AnimationUtility.fast(this.vAnimation, totalHeight, 15.0F);
         context.getMatrices().push();
         if (this.visibleValue < 1.0F) {
            context.getMatrices().translate(x + panelWidth / 2.0F, y + totalHeight / 2.0F, 0.0F);
            context.getMatrices().scale(this.visibleValue, this.visibleValue, 1.0F);
            context.getMatrices().translate(-(x + panelWidth / 2.0F), -(y + totalHeight / 2.0F), 0.0F);
         }

         Color bgColor = this.mode.getValue() == ZeltrixKeybinds.Mode.Neon ? new Color(10, 10, 10, 200) : new Color(30, 30, 30, 90);
         Color outlineColor = this.mode.getValue() == ZeltrixKeybinds.Mode.Neon
            ? HudEditor.textColor.getValue().getColorObject()
            : new Color(255, 255, 255, 40);
         if (this.mode.getValue() == ZeltrixKeybinds.Mode.Neon) {
            Render2DEngine.drawRect(
               context.getMatrices(),
               x - 1.0F,
               y - 1.0F,
               this.hAnimation + 2.0F,
               this.vAnimation + 2.0F,
               10.0F,
               this.visibleValue,
               outlineColor,
               outlineColor,
               outlineColor,
               outlineColor
            );
         } else {
            Render2DEngine.drawRect(
               context.getMatrices(),
               x - 0.5F,
               y - 0.5F,
               this.hAnimation + 1.0F,
               this.vAnimation + 1.0F,
               10.0F,
               this.visibleValue,
               outlineColor,
               outlineColor,
               outlineColor,
               outlineColor
            );
         }

         Render2DEngine.drawRect(context.getMatrices(), x, y, this.hAnimation, this.vAnimation, 10.0F, this.visibleValue, bgColor, bgColor, bgColor, bgColor);
         Color innerBg = this.mode.getValue() == ZeltrixKeybinds.Mode.Neon ? new Color(0, 0, 0, 100) : new Color(255, 255, 255, 20);
         Render2DEngine.drawRect(
            context.getMatrices(), x + 4.5F, y + 4.5F, this.hAnimation - 9.0F, titleHeight - 9.0F, 3.5F, this.visibleValue, innerBg, innerBg, innerBg, innerBg
         );
         FontRenderers.icons.drawString(context.getMatrices(), "C", (double)(x + 9.0F), (double)(y + 8.5F), HudEditor.textColor.getValue().getColor());
         FontRenderers.sf_bold
            .drawString(
               context.getMatrices(),
               "Keybinds",
               (double)(x + 23.0F),
               (double)(y + 8.0F),
               new Color(255, 255, 255, (int)(255.0F * this.visibleValue)).getRGB()
            );
         float currentY = y + titleHeight - 2.0F;
         Render2DEngine.drawRect(
            context.getMatrices(), x + 4.5F, currentY, mainPanelWidth, contentHeight, 3.5F, this.visibleValue, innerBg, innerBg, innerBg, innerBg
         );
         float statusPanelX = x + 4.5F + mainPanelWidth + 2.5F;
         Render2DEngine.drawRect(
            context.getMatrices(), statusPanelX, currentY, statusPanelWidth, contentHeight, 3.5F, this.visibleValue, innerBg, innerBg, innerBg, innerBg
         );
         if (activeModules.isEmpty() && isEditing) {
            String[] examples = new String[]{"Aura", "Velocity", "Flight"};
            String[] keys = new String[]{"R", "V", "F"};

            for (int i = 0; i < 3; i++) {
               this.drawModuleBind(context, currentY, x, statusPanelX, statusPanelWidth, itemHeight, examples[i], keys[i], this.visibleValue);
               currentY += itemHeight + spacing;
            }
         } else {
            for (Module mod : activeModules) {
               this.drawModuleBind(context, currentY, x, statusPanelX, statusPanelWidth, itemHeight, mod.getName(), getShortKeyName(mod), this.visibleValue);
               currentY += itemHeight + spacing;
            }
         }

         context.getMatrices().pop();
         this.setBounds(x, y, this.hAnimation, this.vAnimation);
      }
   }

   private void drawModuleBind(
      DrawContext context, float currentY, float x, float statusPanelX, float statusPanelWidth, float itemHeight, String moduleName, String keyName, float alpha
   ) {
      float barWidth = 1.3F;
      float barHeight = 7.0F;
      float barX = x + 4.5F + 2.8F;
      float barY = currentY + 3.0F;
      Color sideBarColor = this.mode.getValue() == ZeltrixKeybinds.Mode.Neon ? HudEditor.textColor.getValue().getColorObject() : Color.WHITE;
      Render2DEngine.drawRect(context.getMatrices(), barX, barY, barWidth, barHeight, 0.5F, alpha, sideBarColor, sideBarColor, sideBarColor, sideBarColor);
      float nameX = barX + barWidth + 3.5F;
      FontRenderers.sf_bold_mini
         .drawString(context.getMatrices(), moduleName, (double)nameX, (double)(currentY + 4.5F), new Color(255, 255, 255, (int)(255.0F * alpha)).getRGB());
      float keyTextWidth = FontRenderers.sf_bold_mini.getStringWidth(keyName);
      float maxWidth = statusPanelWidth - 2.0F;
      if (keyTextWidth > maxWidth) {
         float scale = maxWidth / keyTextWidth;
         context.getMatrices().push();
         context.getMatrices().translate(statusPanelX + 1.0F, currentY + 4.5F, 0.0F);
         context.getMatrices().scale(scale, scale, 1.0F);
         FontRenderers.sf_bold_mini.drawString(context.getMatrices(), keyName, 0.0, 0.0, new Color(255, 255, 255, (int)(255.0F * alpha)).getRGB());
         context.getMatrices().pop();
      } else {
         float keyX = statusPanelX + (statusPanelWidth - keyTextWidth) / 2.0F;
         FontRenderers.sf_bold_mini
            .drawString(context.getMatrices(), keyName, (double)keyX, (double)(currentY + 4.5F), new Color(255, 255, 255, (int)(255.0F * alpha)).getRGB());
      }
   }

   @NotNull
   public static String getShortKeyName(Module feature) {
      String sbind = feature.getBind().getBind();

      return switch (sbind) {
         case "LEFT_CONTROL" -> "LCtrl";
         case "RIGHT_CONTROL" -> "RCtrl";
         case "LEFT_SHIFT" -> "LShift";
         case "RIGHT_SHIFT" -> "RShift";
         case "LEFT_ALT" -> "LAlt";
         case "RIGHT_ALT" -> "RAlt";
         default -> sbind.toUpperCase();
      };
   }

   public static enum Mode {
      Neon,
      Glass;
   }
}
