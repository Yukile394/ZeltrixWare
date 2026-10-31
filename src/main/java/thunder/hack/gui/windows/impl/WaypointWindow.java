package thunder.hack.gui.windows.impl;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.StringHelper;
import net.minecraft.client.util.InputUtil;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.world.WayPointManager;
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

public class WaypointWindow extends WindowBase {
   private static WaypointWindow instance;
   private ArrayList<WaypointWindow.WaypointPlate> waypointPlates = new ArrayList<>();
   private int listeningId = -1;
   private WaypointWindow.ListeningType listeningType;
   private String search = "Search";
   private String addName = "Name";
   private String addX = "X";
   private String addY = "Y";
   private String addZ = "Z";
   private String addServer = "2b2t.org";
   private String addDimension = "overworld";

   public WaypointWindow(float x, float y, float width, float height, Setting<PositionSetting> position) {
      super(x, y, width, height, "Waypoints", position, TextureStorage.waypointIcon);
      this.refresh();
   }

   public static WaypointWindow get(float x, float y, Setting<PositionSetting> position) {
      if (instance == null) {
         instance = new WaypointWindow(x, y, 340.0F, 180.0F, position);
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
      if (this.waypointPlates.isEmpty()) {
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
      float nameWidth = this.getWidth() / 5.5F;
      float posXX = nameX + nameWidth + 2.0F;
      float posXWidth = this.getWidth() / 8.0F;
      float posYX = posXX + posXWidth + 2.0F;
      float posYWidth = this.getWidth() / 8.0F;
      float posZX = posYX + posYWidth + 2.0F;
      float posZWidth = this.getWidth() / 8.0F;
      float serverX = posZX + posZWidth + 2.0F;
      float serverWidth = this.getWidth() / 6.0F;
      float dimensionX = serverX + serverWidth + 2.0F;
      float dimensionWidth = this.getWidth() / 7.0F;
      boolean hover2 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)nameX, (double)(this.getY() + 19.0F), (double)nameWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), nameX, this.getY() + 19.0F, nameWidth, 11.0F, hover2 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addName + (this.listeningId == -3 && this.listeningType == WaypointWindow.ListeningType.Name ? blink : ""),
            (double)(nameX + 2.0F),
            (double)(this.getY() + 23.0F),
            new Color(12434877).getRGB()
         );
      boolean hover3 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)posXX, (double)(this.getY() + 19.0F), (double)posXWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), posXX, this.getY() + 19.0F, posXWidth, 11.0F, hover3 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addX + (this.listeningId == -3 && this.listeningType == WaypointWindow.ListeningType.X ? blink : ""),
            (double)(posXX + 2.0F),
            (double)(this.getY() + 23.0F),
            textColor
         );
      boolean hover4 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)posYX, (double)(this.getY() + 19.0F), (double)posYWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), posYX, this.getY() + 19.0F, posYWidth, 11.0F, hover4 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addY + (this.listeningId == -3 && this.listeningType == WaypointWindow.ListeningType.Y ? blink : ""),
            (double)(posYX + 2.0F),
            (double)(this.getY() + 23.0F),
            new Color(12434877).getRGB()
         );
      boolean hover5 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)posZX, (double)(this.getY() + 19.0F), (double)posZWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), posZX, this.getY() + 19.0F, posZWidth, 11.0F, hover5 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addZ + (this.listeningId == -3 && this.listeningType == WaypointWindow.ListeningType.Z ? blink : ""),
            (double)(posZX + 2.0F),
            (double)(this.getY() + 23.0F),
            new Color(12434877).getRGB()
         );
      boolean hover6 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)serverX, (double)(this.getY() + 19.0F), (double)serverWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), serverX, this.getY() + 19.0F, serverWidth, 11.0F, hover6 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addServer + (this.listeningId == -3 && this.listeningType == WaypointWindow.ListeningType.Server ? blink : ""),
            (double)(serverX + 2.0F),
            (double)(this.getY() + 23.0F),
            new Color(12434877).getRGB()
         );
      boolean hover7 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)dimensionX, (double)(this.getY() + 19.0F), (double)dimensionWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), dimensionX, this.getY() + 19.0F, dimensionWidth, 11.0F, hover7 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(context.getMatrices(), this.addDimension, (double)(dimensionX + 2.0F), (double)(this.getY() + 23.0F), new Color(12434877).getRGB());
      boolean hover8 = Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)(this.getX() + this.getWidth() - 15.0F), (double)(this.getY() + 19.0F), 11.0, 11.0
      );
      Render2DEngine.drawRectWithOutline(
         context.getMatrices(), this.getX() + this.getWidth() - 15.0F, this.getY() + 19.0F, 11.0F, 11.0F, hover8 ? hoveredColor : color, color2
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
      FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "X", (double)(posXX + posXWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Y", (double)(posYX + posYWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Z", (double)(posZX + posZWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      FontRenderers.sf_medium
         .drawCenteredString(context.getMatrices(), "Server", (double)(serverX + serverWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      FontRenderers.sf_medium
         .drawCenteredString(context.getMatrices(), "Dimension", (double)(dimensionX + dimensionWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      Render2DEngine.addWindow(
         context.getMatrices(), this.getX(), this.getY() + 50.0F, this.getX() + this.getWidth(), this.getY() + this.getHeight() - 1.0F, 1.0
      );
      hover2 = (boolean)0;

      for (WaypointWindow.WaypointPlate waypointPlate : this.waypointPlates) {
         hover2++;
         if (!((float)((int)(waypointPlate.offset + this.getY() + 25.0F)) + this.getScrollOffset() > this.getY() + this.getHeight())
            && !(waypointPlate.offset + this.getScrollOffset() + this.getY() + 10.0F < this.getY())) {
            hover5 = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)nameX,
               (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset),
               (double)nameWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               nameX,
               this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset,
               nameWidth,
               11.0F,
               hover5 ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  waypointPlate.waypoint.getName()
                     + (this.listeningId == waypointPlate.id && this.listeningType == WaypointWindow.ListeningType.Name ? blink : ""),
                  (double)(nameX + 2.0F),
                  (double)(this.getY() + 40.0F + this.getScrollOffset() + waypointPlate.offset),
                  new Color(12434877).getRGB()
               );
            hover6 = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)posXX,
               (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset),
               (double)posXWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               posXX,
               this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset,
               posXWidth,
               11.0F,
               hover6 ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  waypointPlate.waypoint.getX() + (this.listeningId == waypointPlate.id && this.listeningType == WaypointWindow.ListeningType.X ? blink : ""),
                  (double)(posXX + 2.0F),
                  (double)(this.getY() + 40.0F + this.getScrollOffset() + waypointPlate.offset),
                  textColor
               );
            hover7 = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)posYX,
               (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset),
               (double)posYWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               posYX,
               this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset,
               posYWidth,
               11.0F,
               hover7 ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  waypointPlate.waypoint.getY() + (this.listeningId == waypointPlate.id && this.listeningType == WaypointWindow.ListeningType.Y ? blink : ""),
                  (double)(posYX + 2.0F),
                  (double)(this.getY() + 40.0F + this.getScrollOffset() + waypointPlate.offset),
                  new Color(12434877).getRGB()
               );
            hover8 = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)posZX,
               (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset),
               (double)posZWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               posZX,
               this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset,
               posZWidth,
               11.0F,
               hover8 ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  waypointPlate.waypoint.getZ() + (this.listeningId == waypointPlate.id && this.listeningType == WaypointWindow.ListeningType.Z ? blink : ""),
                  (double)(posZX + 2.0F),
                  (double)(this.getY() + 40.0F + this.getScrollOffset() + waypointPlate.offset),
                  new Color(12434877).getRGB()
               );
            boolean hover6x = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)serverX,
               (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset),
               (double)serverWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               serverX,
               this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset,
               serverWidth,
               11.0F,
               hover6x ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  waypointPlate.waypoint.getServer()
                     + (this.listeningId == waypointPlate.id && this.listeningType == WaypointWindow.ListeningType.Server ? blink : ""),
                  (double)(serverX + 2.0F),
                  (double)(this.getY() + 40.0F + this.getScrollOffset() + waypointPlate.offset),
                  new Color(12434877).getRGB()
               );
            boolean hover7x = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)dimensionX,
               (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset),
               (double)dimensionWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               dimensionX,
               this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset,
               dimensionWidth,
               11.0F,
               hover7x ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  waypointPlate.waypoint.getDimension(),
                  (double)(dimensionX + 2.0F),
                  (double)(this.getY() + 40.0F + this.getScrollOffset() + waypointPlate.offset),
                  new Color(12434877).getRGB()
               );
            boolean hover8x = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)(this.getX() + this.getWidth() - 15.0F),
               (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset),
               11.0,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               this.getX() + this.getWidth() - 15.0F,
               this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset,
               11.0F,
               11.0F,
               hover8x ? hoveredColor : color,
               color2
            );
            FontRenderers.icons
               .drawString(
                  context.getMatrices(),
                  "w",
                  (double)(this.getX() + this.getWidth() - 15.0F),
                  (double)(waypointPlate.offset + this.getY() + 40.0F + this.getScrollOffset()),
                  -1
               );
            FontRenderers.sf_medium_mini
               .drawString(
                  context.getMatrices(),
                  hover2 + ".",
                  (double)(this.getX() + 3.0F),
                  (double)(this.getY() + 41.0F + this.getScrollOffset() + waypointPlate.offset),
                  textColor
               );
         }
      }

      this.setMaxElementsHeight((float)(this.waypointPlates.size() * 20));
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
      float nameWidth = this.getWidth() / 5.5F;
      float posXX = nameX + nameWidth + 2.0F;
      float posXWidth = this.getWidth() / 8.0F;
      float posYX = posXX + posXWidth + 2.0F;
      float posYWidth = this.getWidth() / 8.0F;
      float posZX = posYX + posYWidth + 2.0F;
      float posZWidth = this.getWidth() / 8.0F;
      float serverX = posZX + posZWidth + 2.0F;
      float serverWidth = this.getWidth() / 6.0F;
      float dimensionX = serverX + serverWidth + 2.0F;
      float dimensionWidth = this.getWidth() / 7.0F;
      boolean hoveringName = Render2DEngine.isHovered(mouseX, mouseY, (double)nameX, (double)(this.getY() + 19.0F), (double)nameWidth, 11.0);
      boolean hoveringX = Render2DEngine.isHovered(mouseX, mouseY, (double)posXX, (double)(this.getY() + 19.0F), (double)posXWidth, 11.0);
      boolean hoveringY = Render2DEngine.isHovered(mouseX, mouseY, (double)posYX, (double)(this.getY() + 19.0F), (double)posYWidth, 11.0);
      boolean hoveringZ = Render2DEngine.isHovered(mouseX, mouseY, (double)posZX, (double)(this.getY() + 19.0F), (double)posZWidth, 11.0);
      boolean hoveringServer = Render2DEngine.isHovered(mouseX, mouseY, (double)serverX, (double)(this.getY() + 19.0F), (double)serverWidth, 11.0);
      boolean hoveringDimension = Render2DEngine.isHovered(mouseX, mouseY, (double)dimensionX, (double)(this.getY() + 19.0F), (double)dimensionWidth, 11.0);
      boolean hoveringAdd = Render2DEngine.isHovered(mouseX, mouseY, (double)(this.getX() + this.getWidth() - 15.0F), (double)(this.getY() + 19.0F), 11.0, 11.0);
      if (hoveringName) {
         this.listeningType = WaypointWindow.ListeningType.Name;
         this.addName = "";
      }

      if (hoveringX) {
         this.listeningType = WaypointWindow.ListeningType.X;
         this.addX = "";
      }

      if (hoveringY) {
         this.listeningType = WaypointWindow.ListeningType.Y;
         this.addY = "";
      }

      if (hoveringZ) {
         this.listeningType = WaypointWindow.ListeningType.Z;
         this.addZ = "";
      }

      if (hoveringServer) {
         this.listeningType = WaypointWindow.ListeningType.Server;
         this.addServer = "";
      }

      if (hoveringDimension) {
         this.addDimension = this.switchType(this.addDimension);
      }

      if (hoveringName || hoveringX || hoveringY || hoveringZ || hoveringServer || hoveringDimension) {
         this.listeningId = -3;
      }

      if (hoveringAdd) {
         try {
            Managers.WAYPOINT
               .addWayPoint(
                  new WayPointManager.WayPoint(
                     Integer.parseInt(this.addX), Integer.parseInt(this.addY), Integer.parseInt(this.addZ), this.addName, this.addServer, this.addDimension
                  )
               );
         } catch (Exception var28) {
         }

         this.refresh();
      }

      for (WaypointWindow.WaypointPlate waypointPlate : Lists.newArrayList(this.waypointPlates)) {
         if (!((float)((int)(waypointPlate.offset + this.getY() + 50.0F)) + this.getScrollOffset() > this.getY() + this.getHeight())) {
            hoveringZ = Render2DEngine.isHovered(
               mouseX, mouseY, (double)nameX, (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset), (double)nameWidth, 11.0
            );
            hoveringServer = Render2DEngine.isHovered(
               mouseX, mouseY, (double)posXX, (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset), (double)posXWidth, 11.0
            );
            hoveringDimension = Render2DEngine.isHovered(
               mouseX, mouseY, (double)posYX, (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset), (double)posYWidth, 11.0
            );
            hoveringAdd = Render2DEngine.isHovered(
               mouseX, mouseY, (double)posZX, (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset), (double)posZWidth, 11.0
            );
            boolean hoveringServerx = Render2DEngine.isHovered(
               mouseX, mouseY, (double)serverX, (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset), (double)serverWidth, 11.0
            );
            boolean hoveringDimensionx = Render2DEngine.isHovered(
               mouseX, mouseY, (double)dimensionX, (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset), (double)dimensionWidth, 11.0
            );
            boolean hoveringRemove = Render2DEngine.isHovered(
               mouseX,
               mouseY,
               (double)(this.getX() + this.getWidth() - 15.0F),
               (double)(this.getY() + 36.0F + this.getScrollOffset() + waypointPlate.offset),
               11.0,
               11.0
            );
            if (hoveringZ) {
               this.listeningType = WaypointWindow.ListeningType.Name;
            }

            if (hoveringServer) {
               this.listeningType = WaypointWindow.ListeningType.X;
            }

            if (hoveringDimension) {
               this.listeningType = WaypointWindow.ListeningType.Y;
            }

            if (hoveringAdd) {
               this.listeningType = WaypointWindow.ListeningType.Z;
            }

            if (hoveringServerx) {
               this.listeningType = WaypointWindow.ListeningType.Server;
            }

            if (hoveringDimensionx) {
               waypointPlate.waypoint.setDimension(this.switchType(waypointPlate.waypoint.getDimension()));
            }

            if (hoveringZ || hoveringServer || hoveringDimension || hoveringAdd || hoveringServerx || hoveringDimensionx) {
               this.listeningId = waypointPlate.id;
            }

            if (hoveringRemove) {
               Managers.WAYPOINT.removeWayPoint(waypointPlate.waypoint);
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

                  for (WaypointWindow.WaypointPlate plate : this.waypointPlates) {
                     if (this.listeningId == plate.id) {
                        switch (this.listeningType) {
                           case Name:
                              plate.waypoint.setName(SliderElement.removeLastChar(plate.waypoint.getName()));
                              return;
                           case X:
                              String num = SliderElement.removeLastChar(String.valueOf(plate.waypoint.getX()));
                              if (num.isEmpty()) {
                                 num = "0";
                              }

                              plate.waypoint.setX(Integer.parseInt(num));
                              return;
                           case Y:
                              String num = SliderElement.removeLastChar(String.valueOf(plate.waypoint.getY()));
                              if (num.isEmpty()) {
                                 num = "0";
                              }

                              plate.waypoint.setY(Integer.parseInt(num));
                              return;
                           case Z:
                              String num = SliderElement.removeLastChar(String.valueOf(plate.waypoint.getZ()));
                              if (num.isEmpty()) {
                                 num = "0";
                              }

                              plate.waypoint.setZ(Integer.parseInt(num));
                              return;
                           case Server:
                              plate.waypoint.setServer(SliderElement.removeLastChar(plate.waypoint.getServer()));
                              return;
                        }
                     }
                  }

                  if (this.listeningId == -3) {
                     switch (this.listeningType) {
                        case Name:
                           this.addName = SliderElement.removeLastChar(this.addName);
                           break;
                        case X:
                           this.addX = SliderElement.removeLastChar(this.addX);
                           break;
                        case Y:
                           this.addY = SliderElement.removeLastChar(this.addY);
                           break;
                        case Z:
                           this.addZ = SliderElement.removeLastChar(this.addZ);
                           break;
                        case Server:
                           this.addServer = SliderElement.removeLastChar(this.addServer);
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

         for (WaypointWindow.WaypointPlate plate : this.waypointPlates) {
            if (this.listeningId == plate.id) {
               try {
                  switch (this.listeningType) {
                     case Name:
                        plate.waypoint.setName(plate.waypoint.getName() + key);
                        break;
                     case X:
                        plate.waypoint.setX(Integer.parseInt("" + plate.waypoint.getX() + key));
                        break;
                     case Y:
                        plate.waypoint.setY(Integer.parseInt("" + plate.waypoint.getY() + key));
                        break;
                     case Z:
                        plate.waypoint.setZ(Integer.parseInt("" + plate.waypoint.getZ() + key));
                        break;
                     case Server:
                        plate.waypoint.setServer(plate.waypoint.getServer() + key);
                  }
               } catch (Exception var6) {
                  var6.printStackTrace();
               }

               return;
            }
         }

         if (this.listeningId == -3) {
            try {
               switch (this.listeningType) {
                  case Name:
                     this.addName = this.addName + key;
                     break;
                  case X:
                     this.addX = this.addX + key;
                     break;
                  case Y:
                     this.addY = this.addY + key;
                     break;
                  case Z:
                     this.addZ = this.addZ + key;
                     break;
                  case Server:
                     this.addServer = this.addServer + key;
               }
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }

         this.refresh();
      }
   }

   private void refresh() {
      this.resetScroll();
      this.waypointPlates.clear();
      int id1 = 0;

      for (WayPointManager.WayPoint w : Managers.WAYPOINT.getWayPoints()) {
         if (this.search.equals("Search") || this.search.isEmpty() || w.getName().contains(this.search) || w.getServer().contains(this.search)) {
            this.waypointPlates.add(new WaypointWindow.WaypointPlate(id1, (float)(id1 * 20 + 18), w));
            id1++;
         }
      }

      this.addServer = !Module.mc.isInSingleplayer() && !Module.fullNullCheck() ? Module.mc.getNetworkHandler().getServerInfo().address : "SinglePlayer";
      this.addDimension = Module.fullNullCheck() ? "overworld" : Module.mc.world.getRegistryKey().getValue().getPath();
   }

   private String switchType(String t) {
      switch (t) {
         case "the_end":
            return "overworld";
         case "overworld":
            return "the_nether";
         case "the_nether":
            return "the_end";
         default:
            return "overworld";
      }
   }

   @Override
   public int getMinWidth() {
      return 340;
   }

   private static enum ListeningType {
      Name,
      X,
      Y,
      Z,
      Server;
   }

   private static record WaypointPlate(int id, float offset, WayPointManager.WayPoint waypoint) {
   }
}
