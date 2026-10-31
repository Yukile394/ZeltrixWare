package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.misc.NameProtect;
import thunder.hack.gui.font.FontRenderer;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class ZeltrixWatermarkV2 extends HudElement {
   private static final int[] PALETTE = new int[]{
      new Color(10181046).getRGB(),
      new Color(3447003).getRGB(),
      new Color(1752220).getRGB(),
      new Color(15158332).getRGB(),
      new Color(15965202).getRGB(),
      new Color(3066993).getRGB(),
      new Color(15277667).getRGB(),
      new Color(16733986).getRGB()
   };
   private int paletteIndex = 0;
   public final Setting<ZeltrixWatermarkV2.ThemeStyle> themeStyle = new Setting<>("Style", ZeltrixWatermarkV2.ThemeStyle.Neon);
   public final Setting<Boolean> showServer = new Setting<>("Show Server", true);
   public final Setting<Boolean> showTps = new Setting<>("Show TPS", true);
   public final Setting<Boolean> useTheme = new Setting<>("Use Theme Color", true);
   private static final float PADDING_X = 6.0F;
   private static final float GAP = 4.0F;
   private static final float LAYER_GAP = 2.0F;
   private static final float ROW_HEIGHT = 20.0F;
   private static final float CORNER_R = 4.0F;

   public ZeltrixWatermarkV2() {
      super("ZeltrixWatermarkV2", 160, 42);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      if (mc.player != null && mc.world != null) {
         MatrixStack matrices = context.getMatrices();
         float x = this.getPosX();
         float y = this.getPosY();
         Color accentColor = this.useTheme.getValue() ? HudEditor.getColor(0) : new Color(PALETTE[this.paletteIndex], true);
         int accent = accentColor.getRGB();
         Color bgColor = HudEditor.plateColor.getValue().getColorObject();
         int textNormal = HudEditor.textColor.getValue().getColorObject().getRGB();
         String username = NameProtect.getCustomName();
         String fpsValue = String.valueOf(mc.getCurrentFps());
         String fpsSuffix = " FPS";
         String pingValue = String.valueOf(this.getPing());
         String pingSuffix = " ms";
         String serverText = this.getServerAddress();
         int accentIdx = this.getServerAccentIndex(serverText);
         String serverPrefix = accentIdx >= 0 ? serverText.substring(0, accentIdx + 1) : serverText;
         String serverAccent = accentIdx >= 0 && accentIdx + 1 < serverText.length() ? serverText.substring(accentIdx + 1) : "";
         String tpsValue = this.formatOneDecimal(Managers.SERVER.getTPS());
         String tpsSuffix = " TPS";
         String slash = " / ";
         int slashColor = new Color(100, 100, 100).getRGB();
         FontRenderer font = FontRenderers.sf_bold;
         FontRenderer iconFont = FontRenderers.icons;
         float iconGap = 3.0F;
         float slashW = font.getStringWidth(" / ");
         float itemZW = font.getStringWidth("Z");
         float itemZeltrixW = font.getStringWidth("Zeltrix");
         float headW = iconFont.getStringWidth("p");
         float item3W = headW + iconGap + font.getStringWidth(username);
         float item4W = iconFont.getStringWidth("t")
            + iconGap
            + font.getStringWidth(serverPrefix)
            + (serverAccent.isEmpty() ? 0.0F : font.getStringWidth(serverAccent));
         float row1W = 12.0F + itemZW + slashW + itemZeltrixW + slashW + item3W + slashW + item4W;
         float item5W = iconFont.getStringWidth("w") + iconGap + font.getStringWidth(fpsValue) + font.getStringWidth(" FPS");
         float item6W = iconFont.getStringWidth("i") + iconGap + font.getStringWidth(pingValue) + font.getStringWidth(" ms");
         float item7W = this.showTps.getValue() ? iconFont.getStringWidth("u") + iconGap + font.getStringWidth(tpsValue) + font.getStringWidth(" TPS") : 0.0F;
         float row2W = 12.0F + item5W + slashW + item6W;
         if (this.showTps.getValue()) {
            row2W += slashW + item7W;
         }

         float row2Y = y + 20.0F + 2.0F;
         this.drawRect(matrices, x, y, row1W, 20.0F, bgColor, accentColor);
         this.drawRect(matrices, x, row2Y, row2W, 20.0F, bgColor, accentColor);
         float dx = x + 6.0F;
         font.drawString(matrices, "Z", (double)dx, (double)(y + 7.0F), textNormal);
         dx += itemZW;
         font.drawString(matrices, " / ", (double)dx, (double)(y + 7.0F), slashColor);
         dx += slashW;
         font.drawString(matrices, "Zeltrix", (double)dx, (double)(y + 7.0F), accent);
         dx += itemZeltrixW;
         font.drawString(matrices, " / ", (double)dx, (double)(y + 7.0F), slashColor);
         dx += slashW;
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
         RenderSystem.setShaderTexture(0, TextureStorage.playerIcon);
         RenderSystem.setShaderColor((float)(accent >> 16 & 0xFF) / 255.0F, (float)(accent >> 8 & 0xFF) / 255.0F, (float)(accent & 0xFF) / 255.0F, 1.0F);
         Render2DEngine.renderTexture(
            matrices,
            (double)dx,
            (double)(y + (20.0F - headW) / 2.0F),
            (double)headW,
            (double)headW,
            0.0F,
            0.0F,
            (double)headW,
            (double)headW,
            (double)headW,
            (double)headW
         );
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         dx += headW + iconGap;
         font.drawString(matrices, username, (double)dx, (double)(y + 7.0F), textNormal);
         dx += font.getStringWidth(username);
         font.drawString(matrices, " / ", (double)dx, (double)(y + 7.0F), slashColor);
         dx += slashW;
         iconFont.drawString(matrices, "t", (double)dx, (double)(y + 8.0F), accent);
         dx += iconFont.getStringWidth("t") + iconGap;
         font.drawString(matrices, serverPrefix, (double)dx, (double)(y + 7.0F), textNormal);
         dx += font.getStringWidth(serverPrefix);
         if (!serverAccent.isEmpty()) {
            font.drawString(matrices, serverAccent, (double)dx, (double)(y + 7.0F), accent);
            dx += font.getStringWidth(serverAccent);
         }

         dx = x + 6.0F;
         iconFont.drawString(matrices, "w", (double)dx, (double)(row2Y + 8.0F), accent);
         dx += iconFont.getStringWidth("w") + iconGap;
         font.drawString(matrices, fpsValue, (double)dx, (double)(row2Y + 7.0F), textNormal);
         dx += font.getStringWidth(fpsValue);
         font.drawString(matrices, " FPS", (double)dx, (double)(row2Y + 7.0F), accent);
         dx += font.getStringWidth(" FPS");
         font.drawString(matrices, " / ", (double)dx, (double)(row2Y + 7.0F), slashColor);
         dx += slashW;
         iconFont.drawString(matrices, "i", (double)dx, (double)(row2Y + 8.0F), accent);
         dx += iconFont.getStringWidth("i") + iconGap;
         font.drawString(matrices, pingValue, (double)dx, (double)(row2Y + 7.0F), textNormal);
         dx += font.getStringWidth(pingValue);
         font.drawString(matrices, " ms", (double)dx, (double)(row2Y + 7.0F), accent);
         dx += font.getStringWidth(" ms");
         if (this.showTps.getValue()) {
            font.drawString(matrices, " / ", (double)dx, (double)(row2Y + 7.0F), slashColor);
            dx += slashW;
            iconFont.drawString(matrices, "u", (double)dx, (double)(row2Y + 8.0F), accent);
            dx += iconFont.getStringWidth("u") + iconGap;
            font.drawString(matrices, tpsValue, (double)dx, (double)(row2Y + 7.0F), textNormal);
            dx += font.getStringWidth(tpsValue);
            font.drawString(matrices, " TPS", (double)dx, (double)(row2Y + 7.0F), accent);
         }

         float totalW = Math.max(row1W, row2W);
         this.setBounds(x, y, totalW, 42.0F);
      }
   }

   @EventHandler
   @Override
   public void onMouse(@NotNull EventMouse event) {
      if (event.getAction() == 1 && event.getButton() == 1 && this.isHovering()) {
         ZeltrixWatermarkV2.ThemeStyle[] styles = ZeltrixWatermarkV2.ThemeStyle.values();
         this.themeStyle.setValue(styles[(this.themeStyle.getValue().ordinal() + 1) % styles.length]);
      }

      super.onMouse(event);
   }

   private void drawRect(MatrixStack matrices, float x, float y, float w, float h, Color bg, Color outlineColor) {
      ZeltrixWatermarkV2.ThemeStyle style = this.themeStyle.getValue();
      if (style == ZeltrixWatermarkV2.ThemeStyle.Neon) {
         Render2DEngine.drawBlurredShadow(matrices, x - 2.0F, y - 2.0F, w + 4.0F, h + 4.0F, 10, new Color(outlineColor.getRGB() & 16777215 | 1342177280, true));
         Render2DEngine.drawRound(matrices, x, y, w, h, 4.0F, bg);
      } else if (style == ZeltrixWatermarkV2.ThemeStyle.Glass) {
         Render2DEngine.drawRound(matrices, x, y, w, h, 4.0F, new Color(0, 0, 0, 100));
      } else {
         Render2DEngine.drawRound(matrices, x, y, w, h, 4.0F, bg);
      }
   }

   private int getPing() {
      if (mc.getNetworkHandler() == null) {
         return 0;
      } else {
         if (mc.player != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null && entry.getLatency() > 0) {
               return entry.getLatency();
            }
         }

         String username = mc.getSession().getUsername();

         for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            if (entry.getProfile().getName().equalsIgnoreCase(username)) {
               return entry.getLatency();
            }
         }

         return 0;
      }
   }

   private String getServerAddress() {
      return mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address != null && !mc.getCurrentServerEntry().address.isEmpty()
         ? mc.getCurrentServerEntry().address
         : "localhost";
   }

   private int getServerAccentIndex(String serverText) {
      int firstDot = serverText.indexOf(46);
      if (firstDot < 0) {
         return -1;
      } else {
         int secondDot = serverText.indexOf(46, firstDot + 1);
         return secondDot >= 0 ? secondDot : firstDot;
      }
   }

   private String formatOneDecimal(float value) {
      int scaled = Math.round(value * 10.0F);
      return scaled / 10 + "." + Math.abs(scaled % 10);
   }

   public static enum ThemeStyle {
      Solid,
      Neon,
      Glass;
   }
}
