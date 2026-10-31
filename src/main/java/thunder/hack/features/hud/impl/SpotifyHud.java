package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.jna.Library;
import com.sun.jna.Native;
import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

public class SpotifyHud extends HudElement {
   private final ExecutorService executorService = Executors.newSingleThreadExecutor();
   private MediaInfo mediaInfo = new MediaInfo("Waiting for media...", "Unknown Artist", new byte[0], 0L, 100L, false);
   private final Identifier artwork = Identifier.of("thunderhack", "textures/music_artwork.png");
   private boolean artworkRegistered = false;
   private IMediaSession session;
   public final Setting<SpotifyHud.ThemeStyle> themeStyle = new Setting<>("Style", SpotifyHud.ThemeStyle.Neon);
   public final Setting<Boolean> alwaysShow = new Setting<>("AlwaysShow", true);
   public final Setting<Boolean> glow = new Setting<>("Glow", true);
   public final Setting<SpotifyHud.LogoColorMode> logoColorMode = new Setting<>("LogoColor", SpotifyHud.LogoColorMode.SpotifyGreen);
   public final Setting<ColorSetting> logoCustomColor = new Setting<>(
      "CustomLogo", new ColorSetting(-14829228), v -> this.logoColorMode.getValue() == SpotifyHud.LogoColorMode.Custom
   );
   public final Setting<Boolean> whiteWaves = new Setting<>("WhiteWaves", false);
   public final Setting<Boolean> logoGlow = new Setting<>("LogoGlow", true);
   public final Setting<Boolean> customColor = new Setting<>("CustomColor", false);
   public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(-14829228), v -> this.customColor.getValue());
   public final Setting<Boolean> soundWaves = new Setting<>("SoundWaves", true);
   public final Setting<Boolean> controls = new Setting<>("Controls", true);
   private boolean isPlaying = false;
   private long trackDurationSeconds = 180L;
   private long currentPositionMs = 0L;
   private long lastFrameTime = System.currentTimeMillis();
   private float alpha = 0.0F;
   private float progressAnimated = 0.0F;
   private float animatedWidth = 168.0F;
   private final float[] waveHeights = new float[]{3.0F, 3.0F, 3.0F, 3.0F};
   private float prevHover = 0.0F;
   private float playHover = 0.0F;
   private float nextHover = 0.0F;
   private float titleScrollOffset = 0.0F;
   private float titleScrollDir = 1.0F;
   private long titleScrollTimer = 0L;
   private float prevBtnX;
   private float prevBtnY;
   private float playBtnX;
   private float playBtnY;
   private float nextBtnX;
   private float nextBtnY;
   private static final byte VK_MEDIA_NEXT_TRACK = -80;
   private static final byte VK_MEDIA_PREV_TRACK = -79;
   private static final byte VK_MEDIA_PLAY_PAUSE = -77;

   public SpotifyHud() {
      super("Spotify", 168, 48);
   }

   public Color getLogoColor() {
      switch ((SpotifyHud.LogoColorMode)this.logoColorMode.getValue()) {
         case SpotifyGreen:
            return new Color(1947988);
         case Theme:
            return HudEditor.getColor(0);
         case Custom:
            return this.logoCustomColor.getValue().getColorObject();
         case Rainbow:
            return Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 0.85F, 1.0F, 1.0F);
         default:
            return new Color(1947988);
      }
   }

   private int getThemeColor() {
      return this.customColor.getValue() ? this.color.getValue().getColor() : this.getLogoColor().getRGB();
   }

   public static String formatTime(long totalSeconds) {
      long minutes = totalSeconds / 60L;
      long seconds = totalSeconds % 60L;
      return String.format("%d:%02d", minutes, seconds);
   }

   private boolean isHover(float mouseX, float mouseY, float x, float y, float w, float h) {
      return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
   }

   private int getButtonColor(float hoverAnim, int baseAlpha) {
      int r = (int)(160.0F + 95.0F * hoverAnim);
      int g = (int)(160.0F + 95.0F * hoverAnim);
      int b = (int)(160.0F + 95.0F * hoverAnim);
      return baseAlpha << 24 | r << 16 | g << 8 | b;
   }

   private int getButtonColor(float hoverAnim, int baseAlpha, int themeColor) {
      int tr = themeColor >> 16 & 0xFF;
      int tg = themeColor >> 8 & 0xFF;
      int tb = themeColor & 0xFF;
      int r = (int)((float)tr + (float)(255 - tr) * hoverAnim * 0.6F);
      int g = (int)((float)tg + (float)(255 - tg) * hoverAnim * 0.6F);
      int b = (int)((float)tb + (float)(255 - tb) * hoverAnim * 0.6F);
      return baseAlpha << 24 | r << 16 | g << 8 | b;
   }

   public static void drawPlayIcon(MatrixStack matrices, float x, float y, float size, int color) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      Render2DEngine.setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix, x + 0.5F, y, 0.0F).color(color);
      buffer.vertex(matrix, x + size * 0.9F, y + size * 0.5F, 0.0F).color(color);
      buffer.vertex(matrix, x + 0.5F, y + size, 0.0F).color(color);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      Render2DEngine.endRender();
   }

   public static void drawPauseIcon(MatrixStack matrices, float x, float y, float w, float h, int color) {
      float barW = 2.2F;
      float gap = 2.4F;
      Render2DEngine.drawRound(matrices, x + 0.5F, y, barW, h, 0.6F, new Color(color, true));
      Render2DEngine.drawRound(matrices, x + 0.5F + barW + gap, y, barW, h, 0.6F, new Color(color, true));
   }

   public static void drawNextIcon(MatrixStack matrices, float x, float y, float size, int color) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      Render2DEngine.setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix, x, y, 0.0F).color(color);
      buffer.vertex(matrix, x + size - 2.0F, y + size * 0.5F, 0.0F).color(color);
      buffer.vertex(matrix, x + size, y + size, 0.0F).color(color);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      Render2DEngine.endRender();
      Render2DEngine.drawRound(matrices, x + size - 1.2F, y, 1.2F, size, 0.4F, new Color(color, true));
   }

   public static void drawPrevIcon(MatrixStack matrices, float x, float y, float size, int color) {
      Render2DEngine.drawRound(matrices, x, y, 1.2F, size, 0.4F, new Color(color, true));
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      Render2DEngine.setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
      buffer.vertex(matrix, x + size, y, 0.0F).color(color);
      buffer.vertex(matrix, x + 2.0F, y + size * 0.5F, 0.0F).color(color);
      buffer.vertex(matrix, x + size, y + size, 0.0F).color(color);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      Render2DEngine.endRender();
   }

   private static void sendMediaKey(byte vkCode) {
      try {
         if (System.getProperty("os.name").toLowerCase().contains("win")) {
            SpotifyHud.User32.INSTANCE.keybd_event(vkCode, (byte)0, 0, 0);
            SpotifyHud.User32.INSTANCE.keybd_event(vkCode, (byte)0, 2, 0);
         }
      } catch (Throwable var2) {
      }
   }

   private long getMs(long value) {
      if (value > 1000000000L) {
         return value / 10000L;
      } else {
         return value < 10000L && value > 0L ? value * 1000L : value;
      }
   }

   @EventHandler
   @Override
   public void onMouse(@NotNull EventMouse event) {
      if (event.getAction() == 1) {
         if (event.getButton() == 1 && this.isHovering() && (mc.currentScreen instanceof HudEditorGui || mc.currentScreen instanceof ChatScreen)) {
            SpotifyHud.ThemeStyle[] styles = SpotifyHud.ThemeStyle.values();
            this.themeStyle.setValue(styles[(this.themeStyle.getValue().ordinal() + 1) % styles.length]);
         }

         if (this.controls.getValue() && event.getButton() == 0 && (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui)) {
            float mouseX = (float)this.normaliseX();
            float mouseY = (float)this.normaliseY();
            if (this.handleClick(mouseX, mouseY)) {
               return;
            }
         }
      }

      super.onMouse(event);
   }

   private boolean handleClick(float mouseX, float mouseY) {
      if (!this.isPlaying) {
         return false;
      } else if (this.isHover(mouseX, mouseY, this.prevBtnX - 3.0F, this.prevBtnY - 3.0F, 16.0F, 16.0F)) {
         sendMediaKey((byte)-79);
         return true;
      } else if (this.isHover(mouseX, mouseY, this.playBtnX - 3.0F, this.playBtnY - 3.0F, 16.0F, 16.0F)) {
         sendMediaKey((byte)-77);
         return true;
      } else if (this.isHover(mouseX, mouseY, this.nextBtnX - 3.0F, this.nextBtnY - 3.0F, 16.0F, 16.0F)) {
         sendMediaKey((byte)-80);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      if (mc.player != null) {
         long now = System.currentTimeMillis();
         long delta = now - this.lastFrameTime;
         this.lastFrameTime = now;
         if (mc.player.age % 10 == 0) {
            this.executorService.execute(() -> {
               try {
                  Collection<IMediaSession> sessions = MediaPlayerInfo.Instance.getMediaSessions();
                  if (sessions == null || sessions.isEmpty()) {
                     return;
                  }

                  IMediaSession currentSession = sessions.stream().filter(s -> s != null && s.getMedia() != null).max(Comparator.comparing(s -> {
                     try {
                        return s.getMedia().getPlaying();
                     } catch (Exception var2x) {
                        return false;
                     }
                  })).orElse(null);
                  if (currentSession != null) {
                     MediaInfo info = currentSession.getMedia();
                     if (info == null) {
                        return;
                     }

                     if (!info.getTitle().isEmpty() || !info.getArtist().isEmpty() || info.getPlaying()) {
                        boolean needsUpdate = this.mediaInfo == null || !Arrays.equals(this.mediaInfo.getArtworkPng(), info.getArtworkPng());
                        if (needsUpdate && info.getArtworkPng() != null && info.getArtworkPng().length > 0) {
                           mc.execute(() -> {
                              try {
                                 NativeImage image = NativeImage.read(new ByteArrayInputStream(info.getArtworkPng()));
                                 NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                                 mc.getTextureManager().registerTexture(this.artwork, texture);
                                 this.artworkRegistered = true;
                              } catch (Exception var4x) {
                                 this.artworkRegistered = false;
                              }
                           });
                        }

                        long reportedMs = this.getMs(info.getPosition());
                        if (this.mediaInfo == null || !info.getTitle().equals(this.mediaInfo.getTitle())) {
                           this.titleScrollOffset = 0.0F;
                           this.titleScrollDir = 1.0F;
                           this.titleScrollTimer = System.currentTimeMillis();
                           this.currentPositionMs = reportedMs;
                        } else if (reportedMs > 0L && Math.abs(reportedMs - this.currentPositionMs) > 3000L) {
                           this.currentPositionMs = reportedMs;
                        }

                        this.mediaInfo = info;
                        this.session = currentSession;
                     }
                  }
               } catch (Exception var7x) {
               }
            });
         }

         this.isPlaying = this.mediaInfo != null && this.mediaInfo.getPlaying();
         if (this.isPlaying) {
            this.currentPositionMs += delta;
         }

         boolean active = this.alwaysShow.getValue() || this.isPlaying || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui;
         float targetAlpha = active ? 1.0F : 0.0F;
         this.alpha = MathHelper.clamp(Render2DEngine.interpolateFloat(this.alpha, targetAlpha, 0.2F), 0.0F, 1.0F);
         if (this.alpha <= 0.01F) {
            this.setBounds(this.getPosX(), this.getPosY(), 0.0F, 0.0F);
         } else {
            int drawAlpha = (int)(255.0F * this.alpha);
            Color logoColor = this.getLogoColor();
            int themeColor = this.getThemeColor();
            int accentColorWithAlpha = drawAlpha << 24 | themeColor & 16777215;
            this.trackDurationSeconds = this.mediaInfo != null ? this.mediaInfo.getDuration() / 10000000L : 180L;
            if (this.trackDurationSeconds == 0L) {
               this.trackDurationSeconds = 180L;
            }

            long elapsedSeconds = this.currentPositionMs / 1000L;
            if (elapsedSeconds > this.trackDurationSeconds) {
               elapsedSeconds = this.trackDurationSeconds;
            }

            String displayTitle = this.mediaInfo != null ? this.mediaInfo.getTitle() : "Waiting for media...";
            String displayArtist = this.mediaInfo != null ? this.mediaInfo.getArtist() : "Unknown Artist";
            float titleWidth = FontRenderers.sf_bold.getStringWidth(displayTitle);
            float artistWidth = FontRenderers.sf_bold_mini.getStringWidth(displayArtist);
            float contentW = Math.max(titleWidth, artistWidth);
            float targetWidth = Math.max(168.0F, Math.min(235.0F, 54.0F + contentW + (this.soundWaves.getValue() ? 32.0F : 14.0F)));
            this.animatedWidth = Render2DEngine.interpolateFloat(this.animatedWidth, targetWidth, 0.18F);
            float x = this.getPosX();
            float y = this.getPosY();
            float w = this.animatedWidth;
            float h = 48.0F;
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            SpotifyHud.ThemeStyle style = this.themeStyle.getValue();
            Color bgOuter = style == SpotifyHud.ThemeStyle.Glass
               ? new Color(14, 14, 18, (int)(100.0F * this.alpha))
               : new Color(14, 14, 18, (int)(235.0F * this.alpha));
            Color bgInner = style == SpotifyHud.ThemeStyle.Glass
               ? new Color(28, 28, 38, (int)(60.0F * this.alpha))
               : new Color(28, 28, 38, (int)(120.0F * this.alpha));
            if (style == SpotifyHud.ThemeStyle.Neon || this.glow.getValue()) {
               Render2DEngine.drawBlurredShadow(
                  matrices, x - 2.0F, y - 2.0F, w + 4.0F, h + 4.0F, 10, new Color(themeColor & 16777215 | (int)(45.0F * this.alpha) << 24, true)
               );
            }

            Render2DEngine.drawRound(matrices, x, y, w, h, 6.0F, bgOuter);
            Render2DEngine.drawRound(matrices, x + 0.5F, y + 0.5F, w - 1.0F, h - 1.0F, 5.5F, bgInner);
            float artX = x + 6.0F;
            float artY = y + 6.0F;
            float artSize = 36.0F;
            float radius = artSize / 2.0F;
            if (!this.whiteWaves.getValue()) {
               new Color(14, 14, 18);
            }

            if (this.logoGlow.getValue() || this.glow.getValue()) {
               Render2DEngine.drawBlurredShadow(
                  matrices, artX - 1.5F, artY - 1.5F, artSize + 3.0F, artSize + 3.0F, 12, Render2DEngine.applyOpacity(logoColor, 0.45F * this.alpha)
               );
            }

            Render2DEngine.drawRound(matrices, artX, artY, artSize, artSize, 5.0F, Render2DEngine.applyOpacity(new Color(30, 30, 30), this.alpha));
            if (this.artworkRegistered && this.mediaInfo != null && this.mediaInfo.getArtworkPng() != null && this.mediaInfo.getArtworkPng().length > 0) {
               RenderSystem.enableBlend();
               RenderSystem.setShaderTexture(0, this.artwork);
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
               Render2DEngine.renderTexture(
                  matrices,
                  (double)artX,
                  (double)artY,
                  (double)artSize,
                  (double)artSize,
                  0.0F,
                  0.0F,
                  (double)artSize,
                  (double)artSize,
                  (double)artSize,
                  (double)artSize
               );
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
               FontRenderers.sf_bold
                  .drawCenteredString(matrices, "?", (double)(artX + artSize / 2.0F), (double)(artY + artSize / 2.0F - 4.0F), drawAlpha << 24 | 16777215);
            }

            float textX = x + 48.0F;
            float maxTitleW = w - 48.0F - (this.soundWaves.getValue() ? 30.0F : 10.0F);
            long nowMs = System.currentTimeMillis();
            if (titleWidth > maxTitleW) {
               if (nowMs - this.titleScrollTimer > 1200L) {
                  this.titleScrollDir *= -1.0F;
                  this.titleScrollTimer = nowMs;
               }

               this.titleScrollOffset = Render2DEngine.interpolateFloat(
                  this.titleScrollOffset, this.titleScrollDir > 0.0F ? 0.0F : -(titleWidth - maxTitleW + 4.0F), 0.04F
               );
            } else {
               this.titleScrollOffset = 0.0F;
            }

            Render2DEngine.beginScissor((double)textX, (double)y, (double)(textX + maxTitleW), (double)(y + 17.0F));
            FontRenderers.sf_bold.drawString(matrices, displayTitle, (double)(textX + this.titleScrollOffset), (double)(y + 6.5F), drawAlpha << 24 | 16777215);
            Render2DEngine.endScissor();
            Render2DEngine.beginScissor((double)textX, (double)(y + 16.0F), (double)(textX + maxTitleW), (double)(y + 27.0F));
            FontRenderers.sf_bold_mini.drawString(matrices, displayArtist, (double)textX, (double)(y + 17.5F), drawAlpha << 24 | 10395294);
            Render2DEngine.endScissor();
            if (this.soundWaves.getValue()) {
               float waveBaseX = x + w - 24.0F;

               for (int i = 0; i < 4; i++) {
                  float targetSize = this.isPlaying ? 2.5F + (float)(Math.abs(Math.sin((double)now / 160.0 + (double)i * 0.85)) * 8.5) : 2.5F;
                  this.waveHeights[i] = Render2DEngine.interpolateFloat(this.waveHeights[i], targetSize, 0.25);
                  float barX = waveBaseX + (float)i * 4.2F;
                  float barY = y + 8.5F + (11.0F - this.waveHeights[i]) / 2.0F;
                  Render2DEngine.drawRound(matrices, barX, barY, 2.0F, this.waveHeights[i], 0.8F, new Color(accentColorWithAlpha, true));
               }
            }

            float barY = y + 28.5F;
            float barW = w - 54.0F;
            float barH = 2.2F;
            Render2DEngine.drawRound(matrices, textX, barY, barW, barH, 1.1F, new Color(255, 255, 255, (int)(35.0F * this.alpha)));
            float targetProgress = this.trackDurationSeconds > 0L
               ? MathHelper.clamp((float)elapsedSeconds / (float)this.trackDurationSeconds, 0.0F, 1.0F)
               : 0.0F;
            this.progressAnimated = Render2DEngine.interpolateFloat(this.progressAnimated, targetProgress, 0.2F);
            if (this.progressAnimated > 0.005F) {
               Render2DEngine.drawRound(matrices, textX, barY, Math.max(2.2F, barW * this.progressAnimated), barH, 1.1F, new Color(accentColorWithAlpha, true));
            }

            float bottomY = y + 34.5F;
            String timeStr = formatTime(elapsedSeconds) + " / " + formatTime(this.trackDurationSeconds);
            FontRenderers.sf_bold_micro.drawString(matrices, timeStr, (double)textX, (double)(bottomY + 2.0F), drawAlpha << 24 | 9079438);
            if (this.controls.getValue()) {
               float mouseX = (float)this.normaliseX();
               float mouseY = (float)this.normaliseY();
               this.prevBtnX = x + w - 44.0F;
               this.prevBtnY = bottomY;
               this.playBtnX = x + w - 30.0F;
               this.playBtnY = bottomY;
               this.nextBtnX = x + w - 16.0F;
               this.nextBtnY = bottomY;
               boolean canInteract = mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui;
               boolean prevHov = canInteract && this.isHover(mouseX, mouseY, this.prevBtnX - 2.0F, this.prevBtnY - 2.0F, 12.0F, 12.0F);
               boolean playHov = canInteract && this.isHover(mouseX, mouseY, this.playBtnX - 2.0F, this.playBtnY - 2.0F, 12.0F, 12.0F);
               boolean nextHov = canInteract && this.isHover(mouseX, mouseY, this.nextBtnX - 2.0F, this.nextBtnY - 2.0F, 12.0F, 12.0F);
               this.prevHover = Render2DEngine.interpolateFloat(this.prevHover, prevHov ? 1.0F : 0.0F, 0.25);
               this.playHover = Render2DEngine.interpolateFloat(this.playHover, playHov ? 1.0F : 0.0F, 0.25);
               this.nextHover = Render2DEngine.interpolateFloat(this.nextHover, nextHov ? 1.0F : 0.0F, 0.25);
               int pCol = this.getButtonColor(this.prevHover, drawAlpha);
               int plCol = this.getButtonColor(this.playHover, drawAlpha, themeColor);
               int nCol = this.getButtonColor(this.nextHover, drawAlpha);
               drawPrevIcon(matrices, this.prevBtnX, this.prevBtnY, 8.0F, pCol);
               if (this.isPlaying) {
                  drawPauseIcon(matrices, this.playBtnX, this.playBtnY, 8.0F, 8.0F, plCol);
               } else {
                  drawPlayIcon(matrices, this.playBtnX, this.playBtnY, 8.0F, plCol);
               }

               drawNextIcon(matrices, this.nextBtnX, this.nextBtnY, 8.0F, nCol);
            }

            matrices.pop();
            this.setBounds(x, y, w, h);
         }
      }
   }

   @Override
   public void onEnable() {
      this.lastFrameTime = System.currentTimeMillis();
      this.currentPositionMs = 0L;
   }

   @Override
   public void onDisable() {
   }

   public static enum LogoColorMode {
      SpotifyGreen,
      Theme,
      Custom,
      Rainbow;
   }

   public static enum ThemeStyle {
      Solid,
      Neon,
      Glass;
   }

   private interface User32 extends Library {
      SpotifyHud.User32 INSTANCE = (SpotifyHud.User32)Native.load("user32", SpotifyHud.User32.class);

      void keybd_event(byte var1, byte var2, int var3, int var4);
   }
}
