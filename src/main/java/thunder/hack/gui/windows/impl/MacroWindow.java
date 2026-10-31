package thunder.hack.gui.windows.impl;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.lang.invoke.StringConcatFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.StringHelper;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.MacroManager;
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

public class MacroWindow extends WindowBase {
   private static MacroWindow instance;
   private ArrayList<MacroWindow.MacroPlate> macroPlates = new ArrayList<>();
   private int listeningId = -1;
   private int addBindKeyCode = -1;
   private MacroWindow.ListeningType listeningType;
   private String search = "Search";
   private String addName = "Name";
   private String addBind = "Bind";
   private String addText = "Text";

   public MacroWindow(float x, float y, float width, float height, Setting<PositionSetting> position) {
      super(x, y, width, height, "Macros", position, TextureStorage.macrosIcon);
      this.refresh();
   }

   public static MacroWindow get(float x, float y, Setting<PositionSetting> position) {
      if (instance == null) {
         instance = new MacroWindow(x, y, 200.0F, 180.0F, position);
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
      if (this.macroPlates.isEmpty()) {
         FontRenderers.sf_medium
            .drawCenteredString(
               context.getMatrices(),
               ClientSettings.isRu() ? "\u0422\u0443\u0442 \u043f\u043e\u043a\u0430 \u043f\u0443\u0441\u0442\u043e" : "It's empty here yet",
               (double)(this.getX() + this.getWidth() / 2.0F),
               (double)(this.getY() + this.getHeight() / 2.0F),
               new Color(12434877).getRGB()
            );
      }

      String blink = System.currentTimeMillis() / 240L % 2L == 0L ? "" : "l";
      float nameX = this.getX() + 11.0F;
      float nameWidth = this.getWidth() / 4.5F;
      float bindX = nameX + nameWidth + 2.0F;
      float bindWidth = 27.0F;
      float textX = bindX + bindWidth + 2.0F;
      float textWidth = this.getWidth() - (textX - this.getX()) - 17.0F;
      boolean hover2 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)nameX, (double)(this.getY() + 19.0F), (double)nameWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), nameX, this.getY() + 19.0F, nameWidth, 11.0F, hover2 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addName + (this.listeningId == -3 && this.listeningType == MacroWindow.ListeningType.Name ? blink : ""),
            (double)(nameX + 2.0F),
            (double)(this.getY() + 23.0F),
            new Color(12434877).getRGB()
         );
      boolean hover3 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)bindX, (double)(this.getY() + 19.0F), (double)bindWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), bindX, this.getY() + 19.0F, bindWidth, 11.0F, hover3 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawCenteredString(
            context.getMatrices(),
            this.getSbind(this.addBind) + (this.listeningId == -3 && this.listeningType == MacroWindow.ListeningType.Bind ? blink : ""),
            (double)(bindX + 13.5F),
            (double)(this.getY() + 23.0F),
            textColor
         );
      boolean hover4 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)textX, (double)(this.getY() + 19.0F), (double)textWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), textX, this.getY() + 19.0F, textWidth, 11.0F, hover4 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addText + (this.listeningId == -3 && this.listeningType == MacroWindow.ListeningType.Text ? blink : ""),
            (double)(textX + 2.0F),
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
      FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Name", (double)(nameX + nameWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      FontRenderers.sf_medium
         .drawCenteredString(context.getMatrices(), "l   Bind   l", (double)(bindX + bindWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Text", (double)(textX + textWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      Render2DEngine.addWindow(
         context.getMatrices(), this.getX(), this.getY() + 50.0F, this.getX() + this.getWidth(), this.getY() + this.getHeight() - 1.0F, 1.0
      );
      hover2 = (boolean)0;

      for (MacroWindow.MacroPlate macroPlate : this.macroPlates) {
         hover2++;
         if (!((float)((int)(macroPlate.offset + this.getY() + 25.0F)) + this.getScrollOffset() > this.getY() + this.getHeight())
            && !(macroPlate.offset + this.getScrollOffset() + this.getY() + 10.0F < this.getY())) {
            hover5 = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)nameX,
               (double)(macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset()),
               (double)nameWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               nameX,
               macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset(),
               nameWidth,
               11.0F,
               hover5 ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  macroPlate.macro().getName() + (macroPlate.id() == this.listeningId && this.listeningType == MacroWindow.ListeningType.Name ? blink : ""),
                  (double)(nameX + 2.0F),
                  (double)(macroPlate.offset + this.getY() + 40.0F + this.getScrollOffset()),
                  textColor
               );
            boolean hover3x = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)bindX,
               (double)(macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset()),
               (double)bindWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               bindX,
               macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset(),
               bindWidth,
               11.0F,
               hover3x ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawCenteredString(
                  context.getMatrices(),
                  this.getSbind(this.toString(macroPlate.macro().getBind()))
                     + (macroPlate.id() == this.listeningId && this.listeningType == MacroWindow.ListeningType.Bind ? blink : ""),
                  (double)(bindX + 13.5F),
                  (double)(macroPlate.offset + this.getY() + 40.0F + this.getScrollOffset()),
                  textColor
               );
            boolean hover4x = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)textX,
               (double)(macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset()),
               (double)textWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               textX,
               macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset(),
               textWidth,
               11.0F,
               hover4x ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  macroPlate.macro().getText() + (macroPlate.id() == this.listeningId && this.listeningType == MacroWindow.ListeningType.Text ? blink : ""),
                  (double)(textX + 2.0F),
                  (double)(macroPlate.offset + this.getY() + 40.0F + this.getScrollOffset()),
                  textColor
               );
            boolean hover5x = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)(this.getX() + this.getWidth() - 15.0F),
               (double)(macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset()),
               11.0,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               this.getX() + this.getWidth() - 15.0F,
               macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset(),
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
                  (double)(macroPlate.offset + this.getY() + 40.0F + this.getScrollOffset()),
                  -1
               );
            FontRenderers.sf_medium_mini
               .drawString(
                  context.getMatrices(),
                  hover2 + ".",
                  (double)(this.getX() + 3.0F),
                  (double)(macroPlate.offset + this.getY() + 41.0F + this.getScrollOffset()),
                  textColor
               );
         }
      }

      this.setMaxElementsHeight((float)(this.macroPlates.size() * 20));
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

      float nameX = this.getX() + 11.0F;
      float nameWidth = this.getWidth() / 4.5F;
      float bindX = nameX + nameWidth + 2.0F;
      float bindWidth = 27.0F;
      float textX = bindX + bindWidth + 2.0F;
      float textWidth = this.getWidth() - (textX - this.getX()) - 17.0F;
      boolean hoveringName = Render2DEngine.isHovered(mouseX, mouseY, (double)nameX, (double)(this.getY() + 19.0F), (double)nameWidth, 11.0);
      boolean hoveringBind = Render2DEngine.isHovered(mouseX, mouseY, (double)bindX, (double)(this.getY() + 19.0F), (double)bindWidth, 11.0);
      boolean hoveringText = Render2DEngine.isHovered(mouseX, mouseY, (double)textX, (double)(this.getY() + 19.0F), (double)textWidth, 11.0);
      boolean hoveringAdd = Render2DEngine.isHovered(mouseX, mouseY, (double)(this.getX() + this.getWidth() - 15.0F), (double)(this.getY() + 19.0F), 11.0, 11.0);
      if (hoveringName) {
         this.listeningType = MacroWindow.ListeningType.Name;
         this.addName = "";
      }

      if (hoveringBind) {
         this.listeningType = MacroWindow.ListeningType.Bind;
         this.addBind = "";
         this.addBindKeyCode = -1;
      }

      if (hoveringText) {
         this.listeningType = MacroWindow.ListeningType.Text;
         this.addText = "";
      }

      if (hoveringText || hoveringName || hoveringBind) {
         this.listeningId = -3;
      }

      if (hoveringAdd && this.addBindKeyCode != -1) {
         MacroManager.addMacro(new MacroManager.Macro(this.addName, this.addText, this.addBindKeyCode));
         this.refresh();
      }

      for (MacroWindow.MacroPlate macroPlate : Lists.newArrayList(this.macroPlates)) {
         if (!((float)((int)(macroPlate.offset + this.getY() + 50.0F)) + this.getScrollOffset() > this.getY() + this.getHeight())) {
            boolean hoveringName1 = Render2DEngine.isHovered(
               mouseX, mouseY, (double)nameX, (double)(macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset()), (double)nameWidth, 11.0
            );
            boolean hoveringBind1 = Render2DEngine.isHovered(
               mouseX, mouseY, (double)bindX, (double)(macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset()), (double)bindWidth, 11.0
            );
            boolean hoveringText1 = Render2DEngine.isHovered(
               mouseX, mouseY, (double)textX, (double)(macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset()), (double)textWidth, 11.0
            );
            boolean hoveringRemove = Render2DEngine.isHovered(
               mouseX,
               mouseY,
               (double)(this.getX() + this.getWidth() - 15.0F),
               (double)(macroPlate.offset + this.getY() + 36.0F + this.getScrollOffset()),
               11.0,
               11.0
            );
            if (hoveringName1) {
               this.listeningType = MacroWindow.ListeningType.Name;
            }

            if (hoveringBind1) {
               this.listeningType = MacroWindow.ListeningType.Bind;
            }

            if (hoveringText1) {
               this.listeningType = MacroWindow.ListeningType.Text;
            }

            if (hoveringName1 || hoveringBind1 || hoveringText1) {
               this.listeningId = macroPlate.id;
            }

            if (hoveringRemove) {
               Managers.MACRO.removeMacro(macroPlate.macro());
               this.refresh();
            }
         }
      }
   }

   @Override
   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode != 70
         || !InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 341) && !InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 345)) {
         if (keyCode != 86
            || !InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 341) && !InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 345)) {
            if (this.listeningId != -1) {
               switch (keyCode) {
                  case 32:
                     if (this.listeningId == -2) {
                        this.search = this.search + " ";
                        return;
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

                     for (MacroWindow.MacroPlate plate : this.macroPlates) {
                        if (this.listeningId == plate.id) {
                           switch (this.listeningType) {
                              case Name:
                                 plate.macro.setName(SliderElement.removeLastChar(plate.macro.getName()));
                                 return;
                              case Text:
                                 plate.macro.setText(SliderElement.removeLastChar(plate.macro.getText()));
                                 return;
                              case Bind:
                                 return;
                           }
                        }
                     }

                     if (this.listeningId == -3) {
                        switch (this.listeningType) {
                           case Name:
                              this.addName = SliderElement.removeLastChar(this.addName);
                              return;
                           case Text:
                              this.addText = SliderElement.removeLastChar(this.addText);
                              return;
                           case Bind:
                              return;
                        }
                     }
               }

               for (MacroWindow.MacroPlate platex : this.macroPlates) {
                  if (keyCode != -1 && this.listeningId == platex.id && this.listeningType == MacroWindow.ListeningType.Bind) {
                     platex.macro.setBind(keyCode);
                     this.listeningId = -1;
                  }
               }

               if (this.listeningId == -3 && keyCode != -1 && this.listeningType == MacroWindow.ListeningType.Bind) {
                  this.addBind = this.toString(keyCode);
                  this.addBindKeyCode = keyCode;
                  this.listeningId = -1;
               }
            }
         } else {
            String paste = GLFW.glfwGetClipboardString(Module.mc.getWindow().getHandle());
            if (paste != null) {
               for (MacroWindow.MacroPlate platexx : this.macroPlates) {
                  if (this.listeningId == platexx.id) {
                     switch (this.listeningType) {
                        case Name:
                           platexx.macro.setName(paste);
                           return;
                        case Text:
                           platexx.macro.setText(paste);
                           return;
                     }
                  }
               }

               if (this.listeningId == -3) {
                  switch (this.listeningType) {
                     case Name:
                        this.addName = paste;
                        break;
                     case Text:
                        this.addText = paste;
                  }
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

         for (MacroWindow.MacroPlate plate : this.macroPlates) {
            if (this.listeningId == plate.id) {
               switch (this.listeningType) {
                  case Name:
                     plate.macro.setName(plate.macro.getName() + key);
                     break;
                  case Text:
                     plate.macro.setText(plate.macro.getText() + key);
                  case Bind:
               }

               return;
            }
         }

         if (this.listeningId == -3) {
            switch (this.listeningType) {
               case Name:
                  this.addName = this.addName + key;
                  break;
               case Text:
                  this.addText = this.addText + key;
               case Bind:
            }
         }

         this.refresh();
      }
   }

   private void refresh() {
      this.resetScroll();
      this.macroPlates.clear();
      int id1 = 0;

      for (MacroManager.Macro m : Managers.MACRO.getMacros()) {
         if (this.search.equals("Search") || this.search.isEmpty() || m.getName().contains(this.search) || m.getText().contains(this.search)) {
            this.macroPlates.add(new MacroWindow.MacroPlate(id1, (float)(id1 * 20 + 18), m));
            id1++;
         }
      }
   }

   public String toString(int key) {
      String kn = key > 0 ? GLFW.glfwGetKeyName(key, GLFW.glfwGetKeyScancode(key)) : "None";
      if (kn == null) {
         try {
            for (Field declaredField : GLFW.class.getDeclaredFields()) {
               if (declaredField.getName().startsWith("GLFW_KEY_")) {
                  int a = (Integer)declaredField.get(null);
                  if (a == key) {
                     String nb = declaredField.getName().substring("GLFW_KEY_".length());
                     kn = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
                  }
               }
            }
         } catch (Exception var9) {
            kn = "unknown." + key;
         }
      }

      return key == -1 ? "None" : StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(kn).toUpperCase();
   }

   private String getSbind(String sbind) {
      if (sbind.equals("LEFT_CONTROL")) {
         sbind = "LCtrl";
      }

      if (sbind.equals("RIGHT_CONTROL")) {
         sbind = "RCtrl";
      }

      if (sbind.equals("LEFT_SHIFT")) {
         sbind = "LShift";
      }

      if (sbind.equals("RIGHT_SHIFT")) {
         sbind = "RShift";
      }

      if (sbind.equals("LEFT_ALT")) {
         sbind = "LAlt";
      }

      if (sbind.equals("RIGHT_ALT")) {
         sbind = "RAlt";
      }

      return sbind;
   }

   private static enum ListeningType {
      Name,
      Text,
      Bind;
   }

   private static record MacroPlate(int id, float offset, MacroManager.Macro macro) {
   }
}
