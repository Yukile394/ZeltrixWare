package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.core.Managers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class ZeltrixWatermark extends HudElement {
   public final Setting<Boolean> showIp = new Setting<>("Show IP", true);
   public final Setting<Boolean> showTime = new Setting<>("Show Time", true);

   public ZeltrixWatermark() {
      super("ZeltrixWatermark", 120, 24);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      String title = "Zeltrix";
      String fps = mc.getCurrentFps() + " FPS";
      String ping = Managers.SERVER.getPing() + " ms";
      String ip = mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer";
      String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
      float logoSize = 13.0F;
      float logoSpacing = 5.0F;
      float titleWidth = FontRenderers.sf_bold.getStringWidth(title);
      float fpsWidth = FontRenderers.sf_bold.getStringWidth(fps);
      float pingWidth = FontRenderers.sf_bold.getStringWidth(ping);
      float ipWidth = this.showIp.getValue() ? FontRenderers.sf_bold.getStringWidth(ip) : 0.0F;
      float timeWidth = this.showTime.getValue() ? FontRenderers.sf_bold.getStringWidth(time) : 0.0F;
      float totalWidth = 7.0F + logoSize + logoSpacing + titleWidth + 10.0F + fpsWidth + 10.0F + pingWidth + 10.0F;
      if (this.showIp.getValue()) {
         totalWidth += ipWidth + 10.0F;
      }

      if (this.showTime.getValue()) {
         totalWidth += timeWidth + 10.0F;
      }

      Render2DEngine.drawRound(context.getMatrices(), this.getPosX(), this.getPosY(), totalWidth, 24.0F, 4.0F, new Color(5, 5, 5, 240));
      int normalColor = HudEditor.textColor.getValue().getColorObject().getRGB();
      int secondaryColor = new Color(170, 170, 170).getRGB();
      float currentX = this.getPosX() + 7.0F;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderTexture(0, TextureStorage.miniLogo);
      Render2DEngine.renderTexture(
         context.getMatrices(), (double)currentX, (double)(this.getPosY() + 5.5F), (double)logoSize, (double)logoSize, 0.0F, 0.0F, 128.0, 128.0, 128.0, 128.0
      );
      currentX += logoSize + logoSpacing;
      FontRenderers.sf_bold.drawString(context.getMatrices(), title, (double)currentX, (double)(this.getPosY() + 8.5F), normalColor);
      currentX += titleWidth + 10.0F;
      FontRenderers.sf_bold.drawString(context.getMatrices(), fps, (double)currentX, (double)(this.getPosY() + 8.5F), secondaryColor);
      currentX += fpsWidth + 10.0F;
      FontRenderers.sf_bold.drawString(context.getMatrices(), ping, (double)currentX, (double)(this.getPosY() + 8.5F), secondaryColor);
      currentX += pingWidth + 10.0F;
      if (this.showIp.getValue()) {
         FontRenderers.sf_bold.drawString(context.getMatrices(), ip, (double)currentX, (double)(this.getPosY() + 8.5F), secondaryColor);
         currentX += ipWidth + 10.0F;
      }

      if (this.showTime.getValue()) {
         FontRenderers.sf_bold.drawString(context.getMatrices(), time, (double)currentX, (double)(this.getPosY() + 8.5F), secondaryColor);
      }

      this.setBounds(this.getPosX(), this.getPosY(), totalWidth, 24.0F);
   }
}
