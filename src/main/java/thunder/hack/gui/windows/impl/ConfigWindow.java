package thunder.hack.gui.windows.impl;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.StringHelper;
import net.minecraft.client.util.InputUtil;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.clickui.impl.SliderElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.windows.WindowBase;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.PositionSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class ConfigWindow extends WindowBase {
   private static ConfigWindow instance;
   private ArrayList<ConfigWindow.ConfigPlate> configPlates = new ArrayList<>();
   private int listeningId = -1;
   private String search = "Search";
   private String addName = "Name";

   public ConfigWindow(float x, float y, float width, float height, Setting<PositionSetting> position) {
      super(x, y, width, height, "Config", position, TextureStorage.configIcon);
      this.refresh();
   }

   public static ConfigWindow get(float x, float y, Setting<PositionSetting> position) {
      if (instance == null) {
         instance = new ConfigWindow(x, y, 200.0F, 180.0F, position);
      }

      instance.refresh();
      return instance;
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY) {
      super.render(context, mouseX, mouseY);
      Color color = new Color(-986500301, true);
      Color color2 = new Color(-983868581, true);
      Color hoveredColor = new Color(-985052855, true);
      int textColor = new Color(12434877).getRGB();
      boolean hover1 = Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)(this.getX() + this.getWidth() - 90.0F), (double)(this.getY() + 3.0F), 70.0, 10.0
      );
      Render2DEngine.drawRectWithOutline(
         context.getMatrices(), this.getX() + this.getWidth() - 90.0F, this.getY() + 3.0F, 70.0F, 10.0F, hover1 ? hoveredColor : color, color2
      );
      FontRenderers.sf_medium_mini
         .drawString(
            context.getMatrices(), this.search, (double)(this.getX() + this.getWidth() - 86.0F), (double)(this.getY() + 7.0F), new Color(14013909).getRGB()
         );
      if (this.configPlates.isEmpty()) {
         FontRenderers.sf_medium
            .drawCenteredString(
               context.getMatrices(),
               ClientSettings.isRu() ? "\u0422\u0443\u0442 \u043f\u043e\u043a\u0430 \u043f\u0443\u0441\u0442\u043e" : "It's empty here yet",
               (double)(this.getX() + this.getWidth() / 2.0F),
               (double)(this.getY() + this.getHeight() / 2.0F),
               new Color(12434877).getRGB()
            );
      }

      String blink = System.currentTimeMillis() / 240L % 2L == 0L ? "" : "   <<<<";
      String blink2 = System.currentTimeMillis() / 240L % 2L == 0L ? "" : "l";
      boolean hover2 = Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)(this.getX() + 11.0F), (double)(this.getY() + 19.0F), (double)(this.getWidth() - 28.0F), 11.0
      );
      Render2DEngine.drawRectWithOutline(
         context.getMatrices(), this.getX() + 11.0F, this.getY() + 19.0F, this.getWidth() - 28.0F, 11.0F, hover2 ? hoveredColor : color, color2
      );
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addName + (this.listeningId == -3 ? blink2 : ""),
            (double)(this.getX() + 13.0F),
            (double)(this.getY() + 23.0F),
            new Color(12434877).getRGB()
         );
      boolean hover5 = Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)(this.getX() + this.getWidth() - 15.0F), (double)(this.getY() + 19.0F), 11.0, 11.0
      );
      Render2DEngine.drawRectWithOutline(
         context.getMatrices(), this.getX() + this.getWidth() - 15.0F, this.getY() + 19.0F, 11.0F, 11.0F, hover5 ? hoveredColor : color, color2
      );
      FontRenderers.categories.drawString(context.getMatrices(), "+", (double)(this.getX() + this.getWidth() - 12.0F), (double)(this.getY() + 23.0F), -1);
      Render2DEngine.horizontalGradient(
         context.getMatrices(),
         this.getX() + 2.0F,
         this.getY() + 33.0F,
         this.getX() + 2.0F + this.getWidth() / 2.0F - 2.0F,
         this.getY() + 33.5F,
         Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0),
         HudEditor.textColor.getValue().getColorObject()
      );
      Render2DEngine.horizontalGradient(
         context.getMatrices(),
         this.getX() + 2.0F + this.getWidth() / 2.0F - 2.0F,
         this.getY() + 33.0F,
         this.getX() + 2.0F + this.getWidth() - 4.0F,
         this.getY() + 33.5F,
         HudEditor.textColor.getValue().getColorObject(),
         Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0)
      );
      Render2DEngine.addWindow(
         context.getMatrices(), this.getX(), this.getY() + 38.0F, this.getX() + this.getWidth(), this.getY() + this.getHeight() - 1.0F, 1.0
      );
      int plateIndex = 0;

      for (ConfigWindow.ConfigPlate configPlate : this.configPlates) {
         plateIndex++;
         if (!((float)((int)(configPlate.offset + this.getY() + 50.0F)) + this.getScrollOffset() > this.getY() + this.getHeight())
            && !(configPlate.offset + this.getScrollOffset() + this.getY() < this.getY())) {
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               this.getX() + 11.0F,
               configPlate.offset + this.getY() + 36.0F + this.getScrollOffset(),
               this.getWidth() - 52.0F,
               11.0F,
               color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  configPlate.name() + (Objects.equals(configPlate.name() + ".th", Managers.CONFIG.currentConfig.getName()) ? blink : ""),
                  (double)(this.getX() + 13.0F),
                  (double)(configPlate.offset + this.getY() + 40.0F + this.getScrollOffset()),
                  textColor
               );
            boolean hover3 = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)(this.getX() + this.getWidth() - 39.0F),
               (double)(configPlate.offset + this.getY() + 36.0F + this.getScrollOffset()),
               22.0,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               this.getX() + this.getWidth() - 39.0F,
               configPlate.offset + this.getY() + 36.0F + this.getScrollOffset(),
               22.0F,
               11.0F,
               hover3 ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  "Load",
                  (double)(this.getX() + this.getWidth() - 37.0F),
                  (double)(configPlate.offset + this.getY() + 40.0F + this.getScrollOffset()),
                  new Color(12434877).getRGB()
               );
            boolean hover5x = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)(this.getX() + this.getWidth() - 15.0F),
               (double)(configPlate.offset + this.getY() + 36.0F + this.getScrollOffset()),
               11.0,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               this.getX() + this.getWidth() - 15.0F,
               configPlate.offset + this.getY() + 36.0F + this.getScrollOffset(),
               11.0F,
               11.0F,
               hover5x ? hoveredColor : color,
               color2
            );
            FontRenderers.icons
               .drawString(
                  context.getMatrices(),
                  "w",
                  (double)(this.getX() + this.getWidth() - 15.0F),
                  (double)(configPlate.offset + this.getY() + 40.0F + this.getScrollOffset()),
                  -1
               );
            FontRenderers.sf_medium_mini
               .drawString(
                  context.getMatrices(),
                  plateIndex + ".",
                  (double)(this.getX() + 3.0F),
                  (double)(configPlate.offset + this.getY() + 41.0F + this.getScrollOffset()),
                  textColor
               );
         }
      }

      this.setMaxElementsHeight((float)(this.configPlates.size() * 20));
      Render2DEngine.popWindow();
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int button) {
      super.mouseClicked(mouseX, mouseY, button);
      if (Render2DEngine.isHovered(mouseX, mouseY, (double)(this.getX() + this.getWidth() - 90.0F), (double)(this.getY() + 3.0F), 70.0, 10.0)) {
         this.listeningId = -2;
         this.search = "";
      }

      if (Render2DEngine.isHovered(mouseX, mouseY, (double)(this.getX() + this.getWidth() - 15.0F), (double)(this.getY() + 3.0F), 10.0, 10.0)) {
         Module.mc.setScreen(ClickGUI.getClickGui());
      }

      boolean hoveringName = Render2DEngine.isHovered(
         mouseX, mouseY, (double)(this.getX() + 11.0F), (double)(this.getY() + 19.0F), (double)(this.getWidth() - 28.0F), 11.0
      );
      boolean hoveringAdd = Render2DEngine.isHovered(mouseX, mouseY, (double)(this.getX() + this.getWidth() - 15.0F), (double)(this.getY() + 19.0F), 11.0, 11.0);
      if (hoveringName) {
         this.addName = "";
         this.listeningId = -3;
      }

      if (hoveringAdd && !this.addName.isEmpty()) {
         Managers.CONFIG.save(this.addName);
         this.addName = "";
         this.refresh();
      }

      for (ConfigWindow.ConfigPlate configPlate : Lists.newArrayList(this.configPlates)) {
         if (!((float)((int)(configPlate.offset + this.getY() + 50.0F)) + this.getScrollOffset() > this.getY() + this.getHeight())) {
            boolean hoveringRemove = Render2DEngine.isHovered(
               mouseX,
               mouseY,
               (double)(this.getX() + this.getWidth() - 15.0F),
               (double)(configPlate.offset + this.getY() + 36.0F + this.getScrollOffset()),
               11.0,
               11.0
            );
            boolean hoverLoad = Render2DEngine.isHovered(
               mouseX,
               mouseY,
               (double)(this.getX() + this.getWidth() - 39.0F),
               (double)(configPlate.offset + this.getY() + 36.0F + this.getScrollOffset()),
               22.0,
               11.0
            );
            if (hoverLoad) {
               Managers.CONFIG.load(configPlate.name());
            }

            if (hoveringRemove) {
               Managers.CONFIG.delete(configPlate.name());
               this.refresh();
            }
         }
      }
   }

   @Override
   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode != 70
         || !InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 341) && !InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 345)) {
         if (this.listeningId != -1) {
            switch (keyCode) {
               case 32:
                  if (this.listeningId == -2) {
                     this.search = this.search + " ";
                  }
                  break;
               case 256:
                  if (this.listeningId == -2) {
                     this.search = "Search";
                  }

                  this.listeningId = -1;
                  this.refresh();
                  break;
               case 257:
                  if (this.listeningId != -2) {
                     this.listeningId = -1;
                  }
                  break;
               case 259:
                  if (this.listeningId == -2) {
                     this.search = SliderElement.removeLastChar(this.search);
                     this.refresh();
                     if (Objects.equals(this.search, "")) {
                        this.listeningId = -1;
                        this.search = "Search";
                     }

                     return;
                  }

                  if (this.listeningId == -3) {
                     this.addName = SliderElement.removeLastChar(this.addName);
                  }
            }
         }
      } else {
         this.listeningId = -2;
      }
   }

   @Override
   public void charTyped(char key, int keyCode) {
      if (StringHelper.isValidChar(key) && this.listeningId != -1) {
         if (this.listeningId == -2) {
            this.search = this.search + key;
         }

         if (this.listeningId == -3) {
            this.addName = this.addName + key;
         }

         this.refresh();
      }
   }

   private void refresh() {
      this.resetScroll();
      this.configPlates.clear();
      int id1 = 0;

      for (String s : Managers.CONFIG.getConfigList()) {
         if (this.search.equals("Search") || this.search.isEmpty() || s.contains(this.search)) {
            this.configPlates.add(new ConfigWindow.ConfigPlate(id1, (float)(id1 * 20 + 8), s));
            id1++;
         }
      }
   }

   private static record ConfigPlate(int id, float offset, String name) {
   }
}
