package thunder.hack.gui.windows.impl;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.util.Formatting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.StringHelper;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ProxyManager;
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

public class ProxyWindow extends WindowBase {
   private static ProxyWindow instance;
   private ArrayList<ProxyWindow.ProxyPlate> proxyPlates = new ArrayList<>();
   private int listeningId = -1;
   private ProxyWindow.ListeningType listeningType;
   private String search = "Search";
   private String addIp = "Ip";
   private String addPort = "Port";
   private String addPassword = "Password";
   private String addLogin = "Login";
   private String addName = "Name";
   private String[] pinging = new String[]{".", "..", "..."};

   public ProxyWindow(float x, float y, float width, float height, Setting<PositionSetting> position) {
      super(x, y, width, height, "Proxies", position, TextureStorage.proxyIcon);
      this.refresh();
   }

   public static ProxyWindow get(float x, float y, Setting<PositionSetting> position) {
      if (instance == null) {
         instance = new ProxyWindow(x, y, 340.0F, 180.0F, position);
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
      if (this.proxyPlates.isEmpty()) {
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
      float ipX = nameX + nameWidth + 2.0F;
      float posXWidth = this.getWidth() / 5.0F;
      float portX = ipX + posXWidth + 2.0F;
      float posYWidth = this.getWidth() / 12.0F;
      float posZX = portX + posYWidth + 2.0F;
      float posZWidth = this.getWidth() / 6.5F;
      float serverX = posZX + posZWidth + 2.0F;
      float serverWidth = this.getWidth() / 6.0F;
      boolean hover2 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)nameX, (double)(this.getY() + 19.0F), (double)nameWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), nameX, this.getY() + 19.0F, nameWidth, 11.0F, hover2 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addName + (this.listeningId == -3 && this.listeningType == ProxyWindow.ListeningType.Name ? blink : ""),
            (double)(nameX + 2.0F),
            (double)(this.getY() + 23.0F),
            new Color(12434877).getRGB()
         );
      boolean hover3 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)ipX, (double)(this.getY() + 19.0F), (double)posXWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), ipX, this.getY() + 19.0F, posXWidth, 11.0F, hover3 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addIp + (this.listeningId == -3 && this.listeningType == ProxyWindow.ListeningType.Ip ? blink : ""),
            (double)(ipX + 2.0F),
            (double)(this.getY() + 23.0F),
            textColor
         );
      boolean hover4 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)portX, (double)(this.getY() + 19.0F), (double)posYWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), portX, this.getY() + 19.0F, posYWidth, 11.0F, hover4 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addPort + (this.listeningId == -3 && this.listeningType == ProxyWindow.ListeningType.Port ? blink : ""),
            (double)(portX + 2.0F),
            (double)(this.getY() + 23.0F),
            new Color(12434877).getRGB()
         );
      boolean hover5 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)posZX, (double)(this.getY() + 19.0F), (double)posZWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), posZX, this.getY() + 19.0F, posZWidth, 11.0F, hover5 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addLogin + (this.listeningId == -3 && this.listeningType == ProxyWindow.ListeningType.Login ? blink : ""),
            (double)(posZX + 2.0F),
            (double)(this.getY() + 23.0F),
            new Color(12434877).getRGB()
         );
      boolean hover6 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)serverX, (double)(this.getY() + 19.0F), (double)serverWidth, 11.0);
      Render2DEngine.drawRectWithOutline(context.getMatrices(), serverX, this.getY() + 19.0F, serverWidth, 11.0F, hover6 ? hoveredColor : color, color2);
      FontRenderers.sf_medium
         .drawString(
            context.getMatrices(),
            this.addPassword + (this.listeningId == -3 && this.listeningType == ProxyWindow.ListeningType.Password ? blink : ""),
            (double)(serverX + 2.0F),
            (double)(this.getY() + 23.0F),
            new Color(12434877).getRGB()
         );
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
      FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "IP", (double)(ipX + posXWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Port", (double)(portX + posYWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      FontRenderers.sf_medium.drawCenteredString(context.getMatrices(), "Login", (double)(posZX + posZWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      FontRenderers.sf_medium
         .drawCenteredString(context.getMatrices(), "Password", (double)(serverX + serverWidth / 2.0F), (double)(this.getY() + 40.0F), textColor);
      Render2DEngine.addWindow(
         context.getMatrices(), this.getX(), this.getY() + 50.0F, this.getX() + this.getWidth(), this.getY() + this.getHeight() - 1.0F, 1.0
      );
      int plateIndex = 0;

      for (ProxyWindow.ProxyPlate proxyPlate : this.proxyPlates) {
         plateIndex++;
         if (!((float)((int)(proxyPlate.offset + this.getY() + 25.0F)) + this.getScrollOffset() > this.getY() + this.getHeight())
            && !(proxyPlate.offset + this.getScrollOffset() + this.getY() + 10.0F < this.getY())) {
            hover5 = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)nameX,
               (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset),
               (double)nameWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               nameX,
               this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset,
               nameWidth,
               11.0F,
               hover5 ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  proxyPlate.proxy.getName() + (this.listeningId == proxyPlate.id && this.listeningType == ProxyWindow.ListeningType.Name ? blink : ""),
                  (double)(nameX + 2.0F),
                  (double)(this.getY() + 40.0F + this.getScrollOffset() + proxyPlate.offset),
                  new Color(12434877).getRGB()
               );
            hover6 = Render2DEngine.isHovered(
               (double)mouseX, (double)mouseY, (double)ipX, (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset), (double)posXWidth, 11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               ipX,
               this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset,
               posXWidth,
               11.0F,
               hover6 ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  proxyPlate.proxy.getIp() + (this.listeningId == proxyPlate.id && this.listeningType == ProxyWindow.ListeningType.Ip ? blink : ""),
                  (double)(ipX + 2.0F),
                  (double)(this.getY() + 40.0F + this.getScrollOffset() + proxyPlate.offset),
                  textColor
               );
            hover8 = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)portX,
               (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset),
               (double)posYWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               portX,
               this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset,
               posYWidth,
               11.0F,
               hover8 ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  proxyPlate.proxy.getPort() + (this.listeningId == proxyPlate.id && this.listeningType == ProxyWindow.ListeningType.Port ? blink : ""),
                  (double)(portX + 2.0F),
                  (double)(this.getY() + 40.0F + this.getScrollOffset() + proxyPlate.offset),
                  new Color(12434877).getRGB()
               );
            boolean hover5x = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)posZX,
               (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset),
               (double)posZWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               posZX,
               this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset,
               posZWidth,
               11.0F,
               hover5x ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  hover5x
                     ? proxyPlate.proxy.getL() + (this.listeningId == proxyPlate.id && this.listeningType == ProxyWindow.ListeningType.Login ? blink : "")
                     : "*******",
                  (double)(posZX + 2.0F),
                  (double)(this.getY() + 40.0F + this.getScrollOffset() + proxyPlate.offset),
                  new Color(12434877).getRGB()
               );
            boolean hover6x = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)serverX,
               (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset),
               (double)serverWidth,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               serverX,
               this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset,
               serverWidth,
               11.0F,
               hover6x ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  hover6x
                     ? proxyPlate.proxy.getP() + (this.listeningId == proxyPlate.id && this.listeningType == ProxyWindow.ListeningType.Password ? blink : "")
                     : "*******",
                  (double)(serverX + 2.0F),
                  (double)(this.getY() + 40.0F + this.getScrollOffset() + proxyPlate.offset),
                  new Color(12434877).getRGB()
               );
            boolean hover8x = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)(this.getX() + this.getWidth() - 15.0F),
               (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset),
               11.0,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               this.getX() + this.getWidth() - 15.0F,
               this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset,
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
                  (double)(proxyPlate.offset + this.getY() + 40.0F + this.getScrollOffset()),
                  -1
               );
            FontRenderers.sf_medium_mini
               .drawString(
                  context.getMatrices(),
                  plateIndex + ".",
                  (double)(this.getX() + 3.0F),
                  (double)(this.getY() + 41.0F + this.getScrollOffset() + proxyPlate.offset),
                  textColor
               );
            boolean selected = Managers.PROXY.getActiveProxy() == proxyPlate.proxy;
            boolean hover9 = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)(this.getX() + this.getWidth() - 28.0F),
               (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset),
               11.0,
               11.0
            );
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               this.getX() + this.getWidth() - 28.0F,
               this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset,
               11.0F,
               11.0F,
               hover9 ? hoveredColor : color,
               color2
            );
            FontRenderers.icons
               .drawString(
                  context.getMatrices(),
                  selected ? "i" : "k",
                  (double)(this.getX() + this.getWidth() - 26.0F),
                  (double)(proxyPlate.offset + this.getY() + 41.0F + this.getScrollOffset()),
                  selected ? -1 : Color.GRAY.getRGB()
               );
            boolean hover10 = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)(this.getX() + this.getWidth() - 51.5F),
               (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset),
               21.5,
               11.0
            );
            String ping = proxyPlate.proxy.getPing() <= 0
               ? (
                  proxyPlate.proxy.getPing() == -1
                     ? Formatting.RED + "shit"
                     : (proxyPlate.proxy.getPing() == -2 ? this.pinging[(int)(System.currentTimeMillis() / 200L % 3L)] : "check")
               )
               : proxyPlate.proxy.getPing() + "ms";
            Render2DEngine.drawRectWithOutline(
               context.getMatrices(),
               this.getX() + this.getWidth() - 51.5F,
               this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset,
               21.5F,
               11.0F,
               hover10 ? hoveredColor : color,
               color2
            );
            FontRenderers.sf_medium_mini
               .drawCenteredString(
                  context.getMatrices(),
                  ping,
                  (double)(this.getX() + this.getWidth() - 41.0F),
                  (double)(proxyPlate.offset + this.getY() + 40.0F + this.getScrollOffset()),
                  hover10 ? -1 : Color.GRAY.getRGB()
               );
         }
      }

      this.setMaxElementsHeight((float)(this.proxyPlates.size() * 20));
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
      float posXWidth = this.getWidth() / 5.0F;
      float posYX = posXX + posXWidth + 2.0F;
      float posYWidth = this.getWidth() / 12.0F;
      float posZX = posYX + posYWidth + 2.0F;
      float posZWidth = this.getWidth() / 6.5F;
      float serverX = posZX + posZWidth + 2.0F;
      float serverWidth = this.getWidth() / 6.0F;
      boolean hoveringName = Render2DEngine.isHovered(mouseX, mouseY, (double)nameX, (double)(this.getY() + 19.0F), (double)nameWidth, 11.0);
      boolean hoveringX = Render2DEngine.isHovered(mouseX, mouseY, (double)posXX, (double)(this.getY() + 19.0F), (double)posXWidth, 11.0);
      boolean hoveringY = Render2DEngine.isHovered(mouseX, mouseY, (double)posYX, (double)(this.getY() + 19.0F), (double)posYWidth, 11.0);
      boolean hoveringZ = Render2DEngine.isHovered(mouseX, mouseY, (double)posZX, (double)(this.getY() + 19.0F), (double)posZWidth, 11.0);
      boolean hoveringServer = Render2DEngine.isHovered(mouseX, mouseY, (double)serverX, (double)(this.getY() + 19.0F), (double)serverWidth, 11.0);
      boolean hoveringAdd = Render2DEngine.isHovered(mouseX, mouseY, (double)(this.getX() + this.getWidth() - 15.0F), (double)(this.getY() + 19.0F), 11.0, 11.0);
      if (hoveringName) {
         this.listeningType = ProxyWindow.ListeningType.Name;
         this.addName = "";
      }

      if (hoveringX) {
         this.listeningType = ProxyWindow.ListeningType.Ip;
         this.addIp = "";
      }

      if (hoveringY) {
         this.listeningType = ProxyWindow.ListeningType.Port;
         this.addPort = "";
      }

      if (hoveringZ) {
         this.listeningType = ProxyWindow.ListeningType.Login;
         this.addLogin = "";
      }

      if (hoveringServer) {
         this.listeningType = ProxyWindow.ListeningType.Password;
         this.addPassword = "";
      }

      if (hoveringName || hoveringX || hoveringY || hoveringZ || hoveringServer) {
         this.listeningId = -3;
      }

      if (hoveringAdd) {
         try {
            Managers.PROXY.addProxy(new ProxyManager.ThProxy(this.addName, this.addIp, Integer.parseInt(this.addPort), this.addLogin, this.addPassword));
         } catch (Exception var27) {
         }

         this.refresh();
      }

      for (ProxyWindow.ProxyPlate proxyPlate : Lists.newArrayList(this.proxyPlates)) {
         if (!((float)((int)(proxyPlate.offset + this.getY() + 50.0F)) + this.getScrollOffset() > this.getY() + this.getHeight())) {
            hoveringZ = Render2DEngine.isHovered(
               mouseX, mouseY, (double)nameX, (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset), (double)nameWidth, 11.0
            );
            hoveringServer = Render2DEngine.isHovered(
               mouseX, mouseY, (double)posXX, (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset), (double)posXWidth, 11.0
            );
            hoveringAdd = Render2DEngine.isHovered(
               mouseX, mouseY, (double)posYX, (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset), (double)posYWidth, 11.0
            );
            boolean hoveringLogin = Render2DEngine.isHovered(
               mouseX, mouseY, (double)posZX, (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset), (double)posZWidth, 11.0
            );
            boolean hoveringPassword = Render2DEngine.isHovered(
               mouseX, mouseY, (double)serverX, (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset), (double)serverWidth, 11.0
            );
            boolean hoveringRemove = Render2DEngine.isHovered(
               mouseX,
               mouseY,
               (double)(this.getX() + this.getWidth() - 15.0F),
               (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset),
               11.0,
               11.0
            );
            boolean hoveringSelect = Render2DEngine.isHovered(
               mouseX,
               mouseY,
               (double)(this.getX() + this.getWidth() - 28.0F),
               (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset),
               11.0,
               11.0
            );
            boolean hoveringCheck = Render2DEngine.isHovered(
               mouseX,
               mouseY,
               (double)(this.getX() + this.getWidth() - 51.5F),
               (double)(this.getY() + 36.0F + this.getScrollOffset() + proxyPlate.offset),
               21.5,
               11.0
            );
            if (hoveringZ) {
               this.listeningType = ProxyWindow.ListeningType.Name;
            }

            if (hoveringServer) {
               this.listeningType = ProxyWindow.ListeningType.Ip;
            }

            if (hoveringAdd) {
               this.listeningType = ProxyWindow.ListeningType.Port;
            }

            if (hoveringLogin) {
               this.listeningType = ProxyWindow.ListeningType.Login;
            }

            if (hoveringPassword) {
               this.listeningType = ProxyWindow.ListeningType.Password;
            }

            if (hoveringCheck) {
               Managers.PROXY.checkPing(proxyPlate.proxy());
            }

            if (hoveringZ || hoveringServer || hoveringAdd || hoveringLogin || hoveringPassword) {
               this.listeningId = proxyPlate.id;
            }

            if (hoveringSelect) {
               if (Managers.PROXY.getActiveProxy() == proxyPlate.proxy) {
                  Managers.PROXY.setActiveProxy(null);
               } else {
                  Managers.PROXY.setActiveProxy(proxyPlate.proxy);
               }
            }

            if (hoveringRemove) {
               Managers.PROXY.removeProxy(proxyPlate.proxy);
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

                     for (ProxyWindow.ProxyPlate plate : this.proxyPlates) {
                        if (this.listeningId == plate.id) {
                           switch (this.listeningType) {
                              case Name:
                                 plate.proxy.setName(SliderElement.removeLastChar(plate.proxy.getName()));
                                 return;
                              case Ip:
                                 plate.proxy.setIp(SliderElement.removeLastChar(plate.proxy.getIp()));
                                 return;
                              case Port:
                                 String num = SliderElement.removeLastChar(String.valueOf(plate.proxy.getPort()));
                                 if (num.isEmpty()) {
                                    num = "0";
                                 }

                                 plate.proxy.setPort(Integer.parseInt(num));
                                 return;
                              case Login:
                                 plate.proxy.setL(SliderElement.removeLastChar(plate.proxy.getL()));
                                 return;
                              case Password:
                                 plate.proxy.setP(SliderElement.removeLastChar(plate.proxy.getP()));
                                 return;
                           }
                        }
                     }

                     if (this.listeningId == -3) {
                        switch (this.listeningType) {
                           case Name:
                              this.addName = SliderElement.removeLastChar(this.addName);
                              break;
                           case Ip:
                              this.addIp = SliderElement.removeLastChar(this.addIp);
                              break;
                           case Port:
                              this.addPort = SliderElement.removeLastChar(this.addPort);
                              break;
                           case Login:
                              this.addLogin = SliderElement.removeLastChar(this.addLogin);
                              break;
                           case Password:
                              this.addPassword = SliderElement.removeLastChar(this.addPassword);
                        }
                     }
               }
            }
         } else {
            String paste = GLFW.glfwGetClipboardString(Module.mc.getWindow().getHandle());
            if (paste != null) {
               for (ProxyWindow.ProxyPlate platex : this.proxyPlates) {
                  if (this.listeningId == platex.id) {
                     switch (this.listeningType) {
                        case Name:
                           platex.proxy.setName(paste);
                           return;
                        case Ip:
                           platex.proxy.setIp(paste);
                           return;
                        case Port:
                           try {
                              platex.proxy.setPort(Integer.parseInt(paste));
                           } catch (Exception var8) {
                              var8.printStackTrace();
                           }

                           return;
                        case Login:
                           platex.proxy.setL(paste);
                           return;
                        case Password:
                           platex.proxy.setP(paste);
                           return;
                     }
                  }
               }

               if (this.listeningId == -3) {
                  switch (this.listeningType) {
                     case Name:
                        this.addName = paste;
                        break;
                     case Ip:
                        this.addIp = paste;
                        break;
                     case Port:
                        this.addPort = paste;
                        break;
                     case Login:
                        this.addLogin = paste;
                        break;
                     case Password:
                        this.addPassword = paste;
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

         for (ProxyWindow.ProxyPlate plate : this.proxyPlates) {
            if (this.listeningId == plate.id) {
               try {
                  switch (this.listeningType) {
                     case Name:
                        plate.proxy.setName(plate.proxy.getName() + key);
                        break;
                     case Ip:
                        plate.proxy.setIp(plate.proxy.getIp() + key);
                        break;
                     case Port:
                        plate.proxy.setPort(Integer.parseInt("" + plate.proxy.getPort() + key));
                        break;
                     case Login:
                        plate.proxy.setL(plate.proxy.getL() + key);
                        break;
                     case Password:
                        plate.proxy.setP(plate.proxy.getP() + key);
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
                  case Ip:
                     this.addIp = this.addIp + key;
                     break;
                  case Port:
                     this.addPort = this.addPort + key;
                     break;
                  case Login:
                     this.addLogin = this.addLogin + key;
                     break;
                  case Password:
                     this.addPassword = this.addPassword + key;
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
      this.proxyPlates.clear();
      int id1 = 0;

      for (ProxyManager.ThProxy p : Managers.PROXY.getProxies()) {
         if (this.search.equals("Search") || this.search.isEmpty() || p.getName().contains(this.search)) {
            this.proxyPlates.add(new ProxyWindow.ProxyPlate(id1, (float)(id1 * 20 + 18), p));
            id1++;
         }
      }
   }

   @Override
   public int getMinWidth() {
      return 340;
   }

   private static enum ListeningType {
      Name,
      Ip,
      Port,
      Login,
      Password;
   }

   private static record ProxyPlate(int id, float offset, ProxyManager.ThProxy proxy) {
   }
}
