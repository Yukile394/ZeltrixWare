package thunder.hack.features.hud.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

public class KeyBinds extends HudElement {
   public final Setting<Boolean> customColor = new Setting<>("CustomColor", false);
   public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(-16718337), v -> this.customColor.getValue());
   public final Setting<Boolean> onlyEnabled = new Setting<>("OnlyEnabled", true);
   public final Setting<Boolean> glow = new Setting<>("Glow", true);
   public final Setting<KeyBinds.ThemeStyle> themeStyle = new Setting<>("Style", KeyBinds.ThemeStyle.Neon);
   private final Map<String, Float> animationMap = new HashMap<>();
   private float animatedHeight = 0.0F;
   private float alpha = 0.0F;

   public KeyBinds() {
      super("KeyBinds", 95, 30);
   }

   private int getThemeColor() {
      return this.customColor.getValue() ? this.color.getValue().getColor() : HudEditor.getColor(0).getRGB();
   }

   @EventHandler
   @Override
   public void onMouse(@NotNull EventMouse event) {
      if (event.getAction() == 1 && event.getButton() == 1 && this.isHovering()) {
         KeyBinds.ThemeStyle[] styles = KeyBinds.ThemeStyle.values();
         this.themeStyle.setValue(styles[(this.themeStyle.getValue().ordinal() + 1) % styles.length]);
      }

      super.onMouse(event);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      if (mc.player != null) {
         boolean isGui = mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui;
         List<KeyBinds.ModuleBind> activeBinds = new ArrayList<>();

         for (Module m : Managers.MODULE.modules) {
            if (m != ModuleManager.clickGui && !Objects.equals(m.getBind().getBind(), "None")) {
               boolean active = !this.onlyEnabled.getValue() || m.isEnabled();
               float currentAnim = this.animationMap.getOrDefault(m.getName(), 0.0F);
               float targetAnim = active ? 1.0F : 0.0F;
               float newAnim = Render2DEngine.interpolateFloat(currentAnim, targetAnim, 0.18F);
               if (Math.abs(newAnim - targetAnim) < 0.01F) {
                  newAnim = targetAnim;
               }

               if (newAnim > 0.001F) {
                  this.animationMap.put(m.getName(), newAnim);
                  activeBinds.add(new KeyBinds.ModuleBind(m.getName(), getShortKeyName(m), newAnim));
               } else {
                  this.animationMap.remove(m.getName());
               }
            }
         }

         if (activeBinds.isEmpty() && isGui) {
            activeBinds.add(new KeyBinds.ModuleBind("Aura", "R", 1.0F));
            activeBinds.add(new KeyBinds.ModuleBind("Velocity", "V", 1.0F));
            activeBinds.add(new KeyBinds.ModuleBind("Flight", "F", 1.0F));
         }

         boolean hasAnyVisible = !activeBinds.isEmpty() || isGui;
         float targetAlpha = hasAnyVisible ? 1.0F : 0.0F;
         this.alpha = MathHelper.clamp(Render2DEngine.interpolateFloat(this.alpha, targetAlpha, 0.22F), 0.0F, 1.0F);
         if (this.alpha <= 0.01F) {
            this.setBounds(this.getPosX(), this.getPosY(), 0.0F, 0.0F);
         } else {
            float x = this.getPosX();
            float y = this.getPosY();
            float panelWidth = 95.0F;
            float titleHeight = 24.0F;
            float rowHeight = 12.5F;
            float statusPanelWidth = 16.0F;
            float mainPanelWidth = panelWidth - statusPanelWidth - 11.0F;
            float totalAnimHeight = 0.0F;

            for (KeyBinds.ModuleBind bind : activeBinds) {
               totalAnimHeight += rowHeight * bind.anim();
            }

            float contentHeight = totalAnimHeight;
            float totalHeight = titleHeight + 5.0F + totalAnimHeight + (totalAnimHeight > 0.5F ? 4.5F : 0.0F);
            this.animatedHeight = Render2DEngine.interpolateFloat(this.animatedHeight, totalHeight, 0.18F);
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            int themeColor = this.getThemeColor();
            KeyBinds.ThemeStyle style = this.themeStyle.getValue();
            Color bgOuter = style == KeyBinds.ThemeStyle.Glass
               ? new Color(14, 14, 18, (int)(100.0F * this.alpha))
               : new Color(14, 14, 18, (int)(235.0F * this.alpha));
            Color bgInner = style == KeyBinds.ThemeStyle.Glass
               ? new Color(28, 28, 38, (int)(60.0F * this.alpha))
               : new Color(28, 28, 38, (int)(120.0F * this.alpha));
            if (style == KeyBinds.ThemeStyle.Neon || this.glow.getValue()) {
               Render2DEngine.drawBlurredShadow(
                  matrices,
                  x - 2.0F,
                  y - 2.0F,
                  panelWidth + 4.0F,
                  this.animatedHeight + 4.0F,
                  10,
                  new Color(themeColor & 16777215 | (int)(45.0F * this.alpha) << 24, true)
               );
            }

            Render2DEngine.drawRound(matrices, x, y, panelWidth, this.animatedHeight, 7.0F, bgOuter);
            Render2DEngine.drawRound(matrices, x + 4.5F, y + 4.5F, panelWidth - 9.0F, titleHeight - 9.0F, 3.5F, bgInner);
            int drawAlpha = (int)(255.0F * this.alpha);
            FontRenderers.sf_bold.drawString(matrices, "Binds", (double)(x + 10.0F), (double)(y + 9.0F), drawAlpha << 24 | 16777215);
            FontRenderers.sf_bold.drawString(matrices, "D", (double)(x + panelWidth - 16.0F), (double)(y + 9.0F), drawAlpha << 24 | 10395294);
            float currentY = y + titleHeight + 2.0F;
            if (totalAnimHeight > 1.0F) {
               Render2DEngine.drawRound(matrices, x + 4.5F, currentY, mainPanelWidth, totalAnimHeight, 3.5F, bgInner);
               float statusPanelX = x + 4.5F + mainPanelWidth + 2.5F;
               Render2DEngine.drawRound(matrices, statusPanelX, currentY, statusPanelWidth, totalAnimHeight, 3.5F, bgInner);
               Render2DEngine.beginScissor((double)x, (double)currentY, (double)(x + panelWidth), (double)(currentY + totalAnimHeight));
               float itemY = currentY;

               for (KeyBinds.ModuleBind bind : activeBinds) {
                  float anim = bind.anim();
                  float itemH = rowHeight * anim;
                  if (!(itemH < 0.5F)) {
                     float effectiveAlpha = anim * this.alpha;
                     int textAlpha = (int)(255.0F * effectiveAlpha);
                     float barWidth = 1.3F;
                     float barHeight = 7.0F * anim;
                     float barX = x + 4.5F + 2.8F;
                     float barY = itemY + (rowHeight - barHeight) / 2.0F;
                     Render2DEngine.drawRound(matrices, barX, barY, barWidth, barHeight, 0.9F, new Color(themeColor & 16777215 | textAlpha << 24, true));
                     float nameX = barX + barWidth + 3.5F;
                     float nameY = itemY + 4.0F;
                     Render2DEngine.beginScissor((double)(x + 4.5F), (double)currentY, (double)(x + 4.5F + mainPanelWidth), (double)(currentY + contentHeight));
                     FontRenderers.sf_bold_mini.drawString(matrices, bind.name(), (double)nameX, (double)nameY, textAlpha << 24 | 16777215);
                     Render2DEngine.endScissor();
                     String keyName = bind.key();
                     float keyTextWidth = FontRenderers.sf_bold_mini.getStringWidth(keyName);
                     float maxWidth = statusPanelWidth - 2.0F;
                     if (keyTextWidth > maxWidth) {
                        float scale = maxWidth / keyTextWidth;
                        matrices.push();
                        float keyX = statusPanelX + 1.0F;
                        float keyY = itemY + 4.0F;
                        matrices.translate(keyX, keyY, 0.0F);
                        matrices.scale(scale, scale, 1.0F);
                        FontRenderers.sf_bold_mini.drawString(matrices, keyName, 0.0, 0.0, textAlpha << 24 | themeColor & 16777215);
                        matrices.pop();
                     } else {
                        float keyX = statusPanelX + (statusPanelWidth - keyTextWidth) / 2.0F;
                        float keyY = itemY + 4.0F;
                        FontRenderers.sf_bold_mini.drawString(matrices, keyName, (double)keyX, (double)keyY, textAlpha << 24 | themeColor & 16777215);
                     }

                     itemY += itemH;
                  }
               }

               Render2DEngine.endScissor();
            }

            matrices.pop();
            this.setBounds(x, y, panelWidth, this.animatedHeight);
         }
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
         case "MOUSE_BUTTON_4" -> "M4";
         case "MOUSE_BUTTON_5" -> "M5";
         case "BACKSPACE" -> "Back";
         case "DELETE" -> "Del";
         case "INSERT" -> "Ins";
         case "RETURN" -> "Enter";
         default -> sbind.startsWith("NUMPAD") ? "N" + sbind.replace("NUMPAD", "") : sbind.toUpperCase();
      };
   }

   private static record ModuleBind(String name, String key, float anim) {
   }

   public static enum ThemeStyle {
      Solid,
      Neon,
      Glass;
   }
}
