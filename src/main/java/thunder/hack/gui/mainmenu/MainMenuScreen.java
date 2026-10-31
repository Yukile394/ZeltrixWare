package thunder.hack.gui.mainmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class MainMenuScreen extends Screen {
   private final List<MainMenuButton> buttons = new ArrayList<>();
   private final List<MainMenuScreen.Particle> particles = new ArrayList<>();
   public boolean confirm = false;
   public static int ticksActive = 0;
   private static MainMenuScreen INSTANCE = new MainMenuScreen();

   protected MainMenuScreen() {
      super(Text.of("ZeltrixMainMenuScreen"));
      INSTANCE = this;
   }

   public static MainMenuScreen getInstance() {
      ticksActive = 0;
      if (INSTANCE == null) {
         INSTANCE = new MainMenuScreen();
      }

      return INSTANCE;
   }

   protected void init() {
      super.init();
      this.buttons.clear();
      if (this.particles.isEmpty()) {
         float sW = (float)Module.mc.getWindow().getScaledWidth();
         float sH = (float)Module.mc.getWindow().getScaledHeight();

         for (int i = 0; i < 55; i++) {
            this.particles.add(new MainMenuScreen.Particle(sW, sH, true));
         }
      }

      float screenW = (float)Module.mc.getWindow().getScaledWidth();
      float screenH = (float)Module.mc.getWindow().getScaledHeight();
      float totalW = 600.0F;
      float totalH = 340.0F;
      float startX = (screenW - totalW) / 2.0F;
      float startY = (screenH - totalH) / 2.0F;
      float rightX = startX + 265.0F;
      float btnW = 310.0F;
      float btnH = 37.0F;
      float btnGap = 7.0F;
      float btnStartY = startY + 54.0F;
      this.buttons
         .add(
            new MainMenuButton(
               rightX + 12.0F,
               btnStartY,
               btnW,
               btnH,
               ClientSettings.isRu() ? "\u041e\u0434\u0438\u043d\u043e\u0447\u043d\u0430\u044f \u0438\u0433\u0440\u0430" : "Singleplayer",
               ClientSettings.isRu() ? "\u041b\u043e\u043a\u0430\u043b\u044c\u043d\u044b\u0435 \u043c\u0438\u0440\u044b" : "Play on local worlds",
               "\u2756",
               () -> Module.mc.setScreen(new SelectWorldScreen(this))
            )
         );
      this.buttons
         .add(
            new MainMenuButton(
               rightX + 12.0F,
               btnStartY + btnH + btnGap,
               btnW,
               btnH,
               ClientSettings.isRu() ? "\u0421\u0435\u0442\u0435\u0432\u0430\u044f \u0438\u0433\u0440\u0430" : "Multiplayer",
               ClientSettings.isRu()
                  ? "\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u043a \u0441\u0435\u0440\u0432\u0435\u0440\u0430\u043c"
                  : "Join online servers",
               "\ud83c\udf10",
               () -> Module.mc.setScreen(new MultiplayerScreen(this))
            )
         );
      this.buttons
         .add(
            new MainMenuButton(
               rightX + 12.0F,
               btnStartY + (btnH + btnGap) * 2.0F,
               btnW,
               btnH,
               ClientSettings.isRu() ? "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438" : "Options",
               ClientSettings.isRu()
                  ? "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0438\u0433\u0440\u044b \u0438 \u0433\u0440\u0430\u0444\u0438\u043a\u0438"
                  : "Game & video settings",
               "\u2699",
               () -> Module.mc.setScreen(new OptionsScreen(this, Module.mc.options))
            )
         );
      this.buttons
         .add(
            new MainMenuButton(
               rightX + 12.0F,
               btnStartY + (btnH + btnGap) * 3.0F,
               btnW,
               btnH,
               ClientSettings.isRu() ? "\u041c\u043e\u0434\u0443\u043b\u0438 \u043a\u043b\u0438\u0435\u043d\u0442\u0430" : "Client GUI",
               ClientSettings.isRu() ? "\u041c\u0435\u043d\u044e \u0444\u0443\u043d\u043a\u0446\u0438\u0439 Zeltrix" : "Configure client features",
               "\u26a1",
               () -> ModuleManager.clickGui.setGui()
            )
         );
      this.buttons
         .add(
            new MainMenuButton(
               rightX + 12.0F,
               btnStartY + (btnH + btnGap) * 4.0F,
               btnW,
               btnH,
               ClientSettings.isRu() ? "\u041e\u0431\u044b\u0447\u043d\u043e\u0435 \u043c\u0435\u043d\u044e" : "Vanilla Menu",
               ClientSettings.isRu()
                  ? "\u0421\u0442\u0430\u043d\u0434\u0430\u0440\u0442\u043d\u043e\u0435 \u0433\u043b\u0430\u0432\u043d\u043e\u0435 \u043c\u0435\u043d\u044e"
                  : "Return to vanilla title",
               "\u25c0",
               () -> {
                  this.confirm = true;
                  Module.mc.setScreen(new TitleScreen());
                  this.confirm = false;
               }
            )
         );
      this.buttons
         .add(
            new MainMenuButton(
               rightX + 12.0F,
               btnStartY + (btnH + btnGap) * 5.0F,
               btnW,
               btnH,
               ClientSettings.isRu() ? "\u0412\u044b\u0439\u0442\u0438 \u0438\u0437 \u0438\u0433\u0440\u044b" : "Quit Game",
               ClientSettings.isRu() ? "\u0417\u0430\u043a\u0440\u044b\u0442\u044c Minecraft" : "Exit to desktop",
               "\u2715",
               Module.mc::scheduleStop
            )
         );
   }

   public void tick() {
      ticksActive++;
      float sW = (float)Module.mc.getWindow().getScaledWidth();
      float sH = (float)Module.mc.getWindow().getScaledHeight();

      for (MainMenuScreen.Particle p : this.particles) {
         p.update(sW, sH);
      }
   }

   public void render(@NotNull DrawContext context, int mouseX, int mouseY, float delta) {
      MatrixStack matrices = context.getMatrices();
      float screenW = (float)Module.mc.getWindow().getScaledWidth();
      float screenH = (float)Module.mc.getWindow().getScaledHeight();
      this.renderBackground(context, mouseX, mouseY, delta);
      Render2DEngine.drawRound(matrices, 0.0F, 0.0F, screenW, screenH, 0.0F, new Color(8, 6, 14, 220));

      for (MainMenuScreen.Particle p : this.particles) {
         p.render(matrices);
      }

      float totalW = 600.0F;
      float totalH = 340.0F;
      float startX = (screenW - totalW) / 2.0F;
      float startY = (screenH - totalH) / 2.0F;
      float leftW = 250.0F;
      Render2DEngine.drawBlurredShadow(matrices, startX, startY, leftW, totalH, 18, new Color(0, 0, 0, 190));
      Render2DEngine.drawRound(matrices, startX, startY, leftW, totalH, 14.0F, new Color(13, 10, 22, 235));
      Render2DEngine.drawGradientRound(
         matrices,
         startX + 10.0F,
         startY + 2.0F,
         leftW - 20.0F,
         2.0F,
         1.0F,
         new Color(0, 210, 255, 180),
         new Color(155, 89, 182, 220),
         new Color(155, 89, 182, 100),
         new Color(0, 210, 255, 80)
      );
      float emblemSize = 70.0F;
      float emblemX = startX + (leftW - emblemSize) / 2.0F;
      float emblemY = startY + 24.0F;
      Render2DEngine.drawBlurredShadow(matrices, emblemX, emblemY, emblemSize, emblemSize, 20, new Color(155, 89, 182, 130));
      Render2DEngine.drawBlurredShadow(matrices, emblemX + 10.0F, emblemY + 10.0F, emblemSize - 20.0F, emblemSize - 20.0F, 14, new Color(0, 210, 255, 100));
      Render2DEngine.drawGradientRound(
         matrices,
         emblemX,
         emblemY,
         emblemSize,
         emblemSize,
         18.0F,
         new Color(0, 210, 255),
         new Color(155, 89, 182),
         new Color(74, 0, 224),
         new Color(0, 210, 255)
      );
      Render2DEngine.drawRound(matrices, emblemX + 2.5F, emblemY + 2.5F, emblemSize - 5.0F, emblemSize - 5.0F, 16.0F, new Color(16, 12, 28, 250));
      FontRenderers.thglitchBig.drawCenteredString(matrices, "Z", (double)(emblemX + emblemSize / 2.0F), (double)(emblemY + 10.0F), Color.WHITE.getRGB());
      float brandY = startY + 108.0F;
      FontRenderers.sf_bold.drawCenteredString(matrices, "Zeltix Ware", (double)(startX + leftW / 2.0F), (double)brandY, Color.WHITE.getRGB());
      FontRenderers.sf_medium_mini
         .drawCenteredString(
            matrices, "PREMIUM UTILITY CLIENT", (double)(startX + leftW / 2.0F), (double)(brandY + 16.0F), new Color(0, 210, 255, 210).getRGB()
         );
      Render2DEngine.drawRound(matrices, startX + 25.0F, brandY + 32.0F, leftW - 50.0F, 1.5F, 0.75F, new Color(255, 255, 255, 25));
      float infoY = brandY + 44.0F;
      float pulse = (float)(0.6F + Math.sin((double)System.currentTimeMillis() * 0.005) * 0.4F);
      Render2DEngine.drawRound(matrices, startX + 28.0F, infoY + 2.0F, 6.0F, 6.0F, 3.0F, new Color(46, 204, 113, (int)(pulse * 255.0F)));
      FontRenderers.sf_bold_mini.drawString(matrices, "SYSTEM ONLINE", (double)(startX + 38.0F), (double)(infoY + 1.5F), new Color(46, 204, 113).getRGB());
      String username = Module.mc.getSession() != null ? Module.mc.getSession().getUsername() : "Player";
      FontRenderers.sf_medium_mini
         .drawString(matrices, "User: " + Formatting.WHITE + username, (double)(startX + 28.0F), (double)(infoY + 18.0F), new Color(175, 170, 195).getRGB());
      FontRenderers.sf_medium_mini
         .drawString(
            matrices,
            "Build: " + Formatting.WHITE + "v1.7 (Fabric 1.21)",
            (double)(startX + 28.0F),
            (double)(infoY + 31.0F),
            new Color(175, 170, 195).getRGB()
         );
      String onlineStr = "Online: " + Formatting.GREEN + Managers.TELEMETRY.getOnlinePlayers().size() + Formatting.GRAY + " users";
      FontRenderers.sf_medium_mini.drawString(matrices, onlineStr, (double)(startX + 28.0F), (double)(infoY + 44.0F), new Color(175, 170, 195).getRGB());
      FontRenderers.sf_medium_mini
         .drawCenteredString(
            matrices, "\"Dominance by Design\"", (double)(startX + leftW / 2.0F), (double)(startY + totalH - 22.0F), new Color(130, 125, 150, 190).getRGB()
         );
      float rightX = startX + 265.0F;
      float rightW = 335.0F;
      Render2DEngine.drawBlurredShadow(matrices, rightX, startY, rightW, totalH, 18, new Color(0, 0, 0, 190));
      Render2DEngine.drawRound(matrices, rightX, startY, rightW, totalH, 14.0F, new Color(13, 10, 22, 235));
      Render2DEngine.drawGradientRound(
         matrices,
         rightX + 10.0F,
         startY + 2.0F,
         rightW - 20.0F,
         2.0F,
         1.0F,
         new Color(155, 89, 182, 220),
         new Color(0, 210, 255, 180),
         new Color(0, 210, 255, 80),
         new Color(155, 89, 182, 100)
      );
      FontRenderers.sf_bold.drawString(matrices, "NAVIGATION", (double)(rightX + 18.0F), (double)(startY + 16.0F), Color.WHITE.getRGB());
      FontRenderers.sf_medium_mini
         .drawString(matrices, "Select a game mode or utility", (double)(rightX + 18.0F), (double)(startY + 30.0F), new Color(135, 130, 155).getRGB());
      Render2DEngine.drawRound(matrices, rightX + 18.0F, startY + 44.0F, rightW - 36.0F, 1.5F, 0.75F, new Color(255, 255, 255, 25));
      this.buttons.forEach(b -> b.onRender(context, (float)mouseX, (float)mouseY));
      int totalAddonsLoaded = Managers.ADDON.getTotalAddons();
      String addonsText = "Addons: " + Formatting.AQUA + totalAddonsLoaded;
      float textWidth = FontRenderers.sf_bold_mini.getStringWidth(addonsText);
      FontRenderers.sf_bold_mini.drawString(matrices, addonsText, (double)(screenW - textWidth - 12.0F), 10.0, Color.WHITE.getRGB());
      float btnIconY = screenH - 34.0F;
      boolean hoveredCredits = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)(screenW - 38.0F), (double)btnIconY, 26.0, 26.0);
      Render2DEngine.drawRound(matrices, screenW - 38.0F, btnIconY, 26.0F, 26.0F, 6.0F, new Color(20, 16, 32, hoveredCredits ? 240 : 160));
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hoveredCredits ? 1.0F : 0.7F);
      context.drawTexture(TextureStorage.thTeam, (int)(screenW - 35.0F), (int)(btnIconY + 3.0F), 20, 20, 0.0F, 0.0F, 30, 30, 30, 30);
      boolean hoveredDonate = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)(screenW - 70.0F), (double)btnIconY, 26.0, 26.0);
      Render2DEngine.drawRound(matrices, screenW - 70.0F, btnIconY, 26.0F, 26.0F, 6.0F, new Color(20, 16, 32, hoveredDonate ? 240 : 160));
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, hoveredDonate ? 1.0F : 0.7F);
      context.drawTexture(TextureStorage.donation, (int)(screenW - 67.0F), (int)(btnIconY + 3.0F), 20, 20, 0.0F, 0.0F, 30, 30, 30, 30);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      FontRenderers.sf_medium_mini
         .drawString(matrices, "ZeltrixWare \u2022 Powered by ThunderHack", 12.0, (double)(screenH - 16.0F), new Color(110, 105, 130, 180).getRGB());
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      for (MainMenuButton b : this.buttons) {
         if (b.onClick((int)mouseX, (int)mouseY)) {
            return true;
         }
      }

      float screenW = (float)Module.mc.getWindow().getScaledWidth();
      float screenH = (float)Module.mc.getWindow().getScaledHeight();
      float btnIconY = screenH - 34.0F;
      if (Render2DEngine.isHovered(mouseX, mouseY, (double)(screenW - 38.0F), (double)btnIconY, 26.0, 26.0)) {
         Module.mc.setScreen(CreditsScreen.getInstance());
         return true;
      } else if (Render2DEngine.isHovered(mouseX, mouseY, (double)(screenW - 70.0F), (double)btnIconY, 26.0, 26.0)) {
         Util.getOperatingSystem().open(URI.create("https://www.donationalerts.com/r/06ed/"));
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public static class Particle {
      private float x;
      private float y;
      private float size;
      private float speedY;
      private float wobbleSpeed;
      private float wobbleAmount;
      private float seed;
      private float alpha;
      private Color color;

      public Particle(float screenW, float screenH, boolean initial) {
         this.reset(screenW, screenH, initial);
      }

      public void reset(float screenW, float screenH, boolean initial) {
         this.x = (float)(Math.random() * (double)Math.max(screenW, 100.0F));
         this.y = initial ? (float)(Math.random() * (double)Math.max(screenH, 100.0F)) : screenH + 10.0F;
         this.size = (float)(2.0 + Math.random() * 3.5);
         this.speedY = (float)(0.35 + Math.random() * 0.75);
         this.wobbleSpeed = (float)(0.001 + Math.random() * 0.002);
         this.wobbleAmount = (float)(8.0 + Math.random() * 16.0);
         this.seed = (float)(Math.random() * 1000.0);
         this.alpha = (float)(0.25 + Math.random() * 0.65);
         Color[] palette = new Color[]{
            new Color(0, 210, 255), new Color(155, 89, 182), new Color(110, 140, 255), new Color(255, 255, 255), new Color(74, 0, 224)
         };
         this.color = palette[(int)(Math.random() * (double)palette.length)];
      }

      public void update(float screenW, float screenH) {
         this.y = this.y - this.speedY;
         if (this.y < -15.0F) {
            this.reset(screenW, screenH, false);
         }
      }

      public void render(MatrixStack matrices) {
         float curX = this.x + (float)Math.sin((double)((float)System.currentTimeMillis() * this.wobbleSpeed + this.seed)) * this.wobbleAmount;
         Color drawCol = Render2DEngine.applyOpacity(this.color, this.alpha);
         Render2DEngine.drawRound(matrices, curX, this.y, this.size, this.size, this.size / 2.0F, drawCol);
      }
   }
}
