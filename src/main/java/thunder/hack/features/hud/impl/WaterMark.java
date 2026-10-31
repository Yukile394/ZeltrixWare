package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.util.Formatting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.client.Media;
import thunder.hack.features.modules.misc.NameProtect;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextUtil;
import thunder.hack.utility.render.TextureStorage;

public class WaterMark extends HudElement {
   public static final Setting<WaterMark.Mode> mode = new Setting<>("Mode", WaterMark.Mode.Big);
   private final Setting<Boolean> ru = new Setting<>("RU", false);
   private final TextUtil textUtil = new TextUtil(
      "\u0422\u0430\u043d\u0434\u0435\u0440\u0425\u0430\u043a",
      "\u0413\u0440\u043e\u043c\u0425\u0430\u043a",
      "\u0413\u0440\u043e\u0437\u0430\u041a\u043b\u0438\u0435\u043d\u0442",
      "\u0422\u0430\u043d\u0434\u0435\u0440\u0425\u0443\u0439",
      "\u0422\u0430\u043d\u0434\u0435\u0440\u0425\u0440\u044f\u043a",
      "\u0422\u0430\u043d\u0434\u0435\u0440\u0425\u0440\u044e\u043a",
      "\u0422\u0438\u043d\u0434\u0435\u0440\u0425\u0430\u043a",
      "\u0422\u0443\u043d\u0434\u0440\u0430\u0425\u0430\u043a",
      "\u0413\u0440\u043e\u043c\u0412\u0437\u043b\u043e\u043c"
   );

   public WaterMark() {
      super("WaterMark", 100, 35);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      String username = (!ModuleManager.media.isEnabled() || !Media.nickProtect.getValue()) && !ModuleManager.nameProtect.isEnabled()
         ? mc.getSession().getUsername()
         : (ModuleManager.nameProtect.isEnabled() ? NameProtect.getCustomName() : "Protected");
      if (mode.getValue() == WaterMark.Mode.Big) {
         Render2DEngine.drawHudBase(context.getMatrices(), this.getPosX(), this.getPosY(), 106.0F, 30.0F, HudEditor.hudRound.getValue());
         FontRenderers.thglitch.drawString(context.getMatrices(), "THUNDERHACK", (double)this.getPosX() + 5.5, (double)(this.getPosY() + 5.0F), -1);
         FontRenderers.monsterrat.drawGradientString(context.getMatrices(), "recode", this.getPosX() + 35.5F, this.getPosY() + 21.0F, 1);
         this.setBounds(this.getPosX(), this.getPosY(), 106.0F, 30.0F);
      } else if (mode.getValue() == WaterMark.Mode.Small) {
         if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            float offset1 = FontRenderers.sf_bold.getStringWidth(username) + 72.0F;
            float offset2 = FontRenderers.sf_bold.getStringWidth(mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address);
            float offset3 = Managers.PROXY.isActive() ? FontRenderers.sf_bold.getStringWidth(Managers.PROXY.getActiveProxy().getName()) + 11.0F : 0.0F;
            Render2DEngine.drawRoundedBlur(
               context.getMatrices(), this.getPosX(), this.getPosY(), 50.0F, 15.0F, 3.0F, HudEditor.blurColor.getValue().getColorObject()
            );
            Render2DEngine.drawRoundedBlur(
               context.getMatrices(),
               this.getPosX() + 55.0F,
               this.getPosY(),
               offset1 + offset2 - 36.0F + offset3,
               15.0F,
               3.0F,
               HudEditor.blurColor.getValue().getColorObject()
            );
            Render2DEngine.setupRender();
            Render2DEngine.drawRect(context.getMatrices(), this.getPosX() + 13.0F, this.getPosY() + 1.5F, 0.5F, 11.0F, new Color(1157627903, true));
            FontRenderers.sf_bold.drawGradientString(context.getMatrices(), "Recode", this.getPosX() + 18.0F, this.getPosY() + 5.0F, 20);
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
            RenderSystem.setShaderTexture(0, TextureStorage.miniLogo);
            Render2DEngine.renderGradientTexture(
               context.getMatrices(),
               (double)(this.getPosX() + 1.0F),
               (double)(this.getPosY() + 2.0F),
               11.0,
               11.0,
               0.0F,
               0.0F,
               128.0,
               128.0,
               128.0,
               128.0,
               HudEditor.getColor(270),
               HudEditor.getColor(0),
               HudEditor.getColor(180),
               HudEditor.getColor(90)
            );
            RenderSystem.setShaderTexture(0, TextureStorage.playerIcon);
            Render2DEngine.renderGradientTexture(
               context.getMatrices(),
               (double)(this.getPosX() + 58.0F),
               (double)(this.getPosY() + 3.0F),
               8.0,
               8.0,
               0.0F,
               0.0F,
               128.0,
               128.0,
               128.0,
               128.0,
               HudEditor.getColor(270),
               HudEditor.getColor(0),
               HudEditor.getColor(180),
               HudEditor.getColor(90)
            );
            RenderSystem.setShaderTexture(0, TextureStorage.serverIcon);
            Render2DEngine.renderGradientTexture(
               context.getMatrices(),
               (double)(this.getPosX() + offset1),
               (double)(this.getPosY() + 2.0F),
               10.0,
               10.0,
               0.0F,
               0.0F,
               128.0,
               128.0,
               128.0,
               128.0,
               HudEditor.getColor(270),
               HudEditor.getColor(0),
               HudEditor.getColor(180),
               HudEditor.getColor(90)
            );
            if (Managers.PROXY.isActive()) {
               RenderSystem.setShaderTexture(0, TextureStorage.proxyIcon);
               Render2DEngine.renderGradientTexture(
                  context.getMatrices(),
                  (double)(this.getPosX() + offset1 + offset2 + 16.0F),
                  (double)(this.getPosY() + 2.0F),
                  10.0,
                  10.0,
                  0.0F,
                  0.0F,
                  128.0,
                  128.0,
                  128.0,
                  128.0,
                  HudEditor.getColor(270),
                  HudEditor.getColor(0),
                  HudEditor.getColor(180),
                  HudEditor.getColor(90)
               );
               FontRenderers.sf_bold
                  .drawString(
                     context.getMatrices(),
                     Managers.PROXY.getActiveProxy().getName(),
                     (double)(this.getPosX() + offset1 + offset2 + 28.0F),
                     (double)(this.getPosY() + 5.0F),
                     -1
                  );
            }

            Render2DEngine.endRender();
            Render2DEngine.setupRender();
            RenderSystem.defaultBlendFunc();
            FontRenderers.sf_bold
               .drawString(
                  context.getMatrices(),
                  username,
                  (double)(this.getPosX() + 68.0F),
                  (double)(this.getPosY() + 4.5F),
                  HudEditor.textColor.getValue().getColor()
               );
            FontRenderers.sf_bold
               .drawString(
                  context.getMatrices(),
                  mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address,
                  (double)(this.getPosX() + offset1 + 13.0F),
                  (double)(this.getPosY() + 4.5F),
                  HudEditor.textColor.getValue().getColor()
               );
            Render2DEngine.endRender();
            this.setBounds(this.getPosX(), this.getPosY(), 100.0F, 15.0F);
         } else {
            String info = Formatting.DARK_GRAY
               + "| "
               + Formatting.RESET
               + username
               + Formatting.DARK_GRAY
               + " | "
               + Formatting.RESET
               + Managers.SERVER.getPing()
               + " ms"
               + Formatting.DARK_GRAY
               + " | "
               + Formatting.RESET
               + (mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address);
            float width = FontRenderers.sf_bold.getStringWidth("ThunderHack " + info) + 5.0F;
            Render2DEngine.drawHudBase(context.getMatrices(), this.getPosX(), this.getPosY(), width, 10.0F, 3.0F);
            FontRenderers.sf_bold
               .drawGradientString(
                  context.getMatrices(), this.ru.getValue() ? this.textUtil + " " : "ThunderHack ", this.getPosX() + 2.0F, this.getPosY() + 2.5F, 10
               );
            FontRenderers.sf_bold
               .drawString(
                  context.getMatrices(),
                  info,
                  (double)(this.getPosX() + 2.0F + FontRenderers.sf_bold.getStringWidth("ThunderHack ")),
                  (double)(this.getPosY() + 2.5F),
                  HudEditor.textColor.getValue().getColor()
               );
            this.setBounds(this.getPosX(), this.getPosY(), width, 10.0F);
         }
      } else if (mode.getValue() == WaterMark.Mode.BaltikaClient) {
         Render2DEngine.drawHudBase(context.getMatrices(), this.getPosX(), this.getPosY(), 100.0F, 64.0F, HudEditor.hudRound.getValue());
         Render2DEngine.addWindow(context.getMatrices(), this.getPosX(), this.getPosY(), this.getPosX() + 100.0F, this.getPosY() + 64.0F, 1.0);
         context.getMatrices().push();
         context.getMatrices().translate(this.getPosX() + 10.0F, this.getPosY() + 32.0F, 0.0F);
         context.getMatrices()
            .multiply(
               RotationAxis.POSITIVE_Z.rotation((float)Math.toRadians((double)((float)(mc.player.age * 3) + Render3DEngine.getTickDelta())))
            );
         context.getMatrices().translate(-(this.getPosX() + 10.0F), -(this.getPosY() + 32.0F), 0.0F);
         context.drawTexture(TextureStorage.baltika, (int)this.getPosX() - 10, (int)this.getPosY() + 2, 0.0F, 0.0F, 40, 64, 40, 64);
         context.getMatrices().pop();
         Render2DEngine.popWindow();
         FontRenderers.thglitch.drawString(context.getMatrices(), "BALTIKA", (double)(this.getPosX() + 43.0F), (double)this.getPosY() + 41.5, -1);
         this.setBounds(this.getPosX(), this.getPosY(), 100.0F, 64.0F);
      } else if (mode.is(WaterMark.Mode.Rifk)) {
         Date date = new Date(System.currentTimeMillis());
         SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
         String info = Formatting.GREEN
            + String.format("th7 | build: 16/06/2024 | rate: %d | %s", Math.round(Managers.SERVER.getTPS()), format.format(date));
         float width = FontRenderers.profont.getStringWidth(info) + 5.0F;
         Render2DEngine.drawRectWithOutline(
            context.getMatrices(), this.getPosX(), this.getPosY(), width, 8.0F, Color.decode("#192A1A"), Color.decode("#833B7B")
         );
         Render2DEngine.drawGradientBlurredShadow1(
            context.getMatrices(),
            this.getPosX(),
            this.getPosY(),
            width,
            8.0F,
            10,
            Color.decode("#161A1E"),
            Color.decode("#161A1E"),
            Color.decode("#382E37"),
            Color.decode("#382E37")
         );
         FontRenderers.profont
            .drawString(context.getMatrices(), info, (double)this.getPosX() + 2.7, (double)this.getPosY() + 2.953, HudEditor.textColor.getValue().getColor());
         this.setBounds(this.getPosX(), this.getPosY(), width, 8.0F);
      } else if (mode.is(WaterMark.Mode.Polyak)) {
         String ping = String.valueOf(Managers.SERVER.getPing());
         String fps = String.valueOf(mc.getCurrentFps());
         String text = "ZaltreWare | " + username + " | " + fps + " fps | " + ping + " ms";
         float width = FontRenderers.sf_bold.getStringWidth(text) + 12.0F;
         Render2DEngine.drawRoundedBlur(context.getMatrices(), this.getPosX(), this.getPosY(), width, 18.0F, 4.0F, new Color(0, 0, 0, 150));
         Render2DEngine.drawRound(context.getMatrices(), this.getPosX(), this.getPosY(), width, 18.0F, 4.0F, new Color(20, 20, 20, 150));
         Render2DEngine.drawRound(context.getMatrices(), this.getPosX(), this.getPosY(), width, 2.0F, 1.0F, HudEditor.textColor.getValue().getColorObject());
         FontRenderers.sf_bold.drawString(context.getMatrices(), text, (double)(this.getPosX() + 6.0F), (double)(this.getPosY() + 6.5F), -1);
         this.setBounds(this.getPosX(), this.getPosY(), width, 18.0F);
      } else {
         FontRenderers.monsterrat.drawGradientString(context.getMatrices(), "ThunderHack v1.7b2407", this.getPosX() + 5.5F, this.getPosY() + 5.0F, 10);
         this.setBounds(this.getPosX(), this.getPosY(), 100.0F, 3.0F);
      }
   }

   @Override
   public void onUpdate() {
      this.textUtil.tick();
   }

   private static enum Mode {
      Big,
      Small,
      Classic,
      BaltikaClient,
      Rifk,
      Polyak;
   }
}
