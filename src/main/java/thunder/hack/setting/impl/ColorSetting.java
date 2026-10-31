package thunder.hack.setting.impl;

import java.awt.Color;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.utility.render.Render2DEngine;

public final class ColorSetting {
   private int color;
   private final int defaultColor;
   private boolean rainbow;

   public ColorSetting(@NotNull Color color) {
      this(color.getRGB());
   }

   public ColorSetting(int color) {
      this.color = color;
      this.defaultColor = color;
   }

   @NotNull
   public ColorSetting withAlpha(int alpha) {
      int red = this.getColor() >> 16 & 0xFF;
      int green = this.getColor() >> 8 & 0xFF;
      int blue = this.getColor() & 0xFF;
      return new ColorSetting((alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF);
   }

   public int getColor() {
      return this.rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1.0F, 1.0F, 1.0F).getRGB() : this.color;
   }

   public void setColor(int color) {
      this.color = color;
   }

   public int getRed() {
      return this.rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1.0F, 1.0F, 1.0F).getRed() : this.color >> 16 & 0xFF;
   }

   public int getGreen() {
      return this.rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1.0F, 1.0F, 1.0F).getGreen() : this.color >> 8 & 0xFF;
   }

   public int getBlue() {
      return this.rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1.0F, 1.0F, 1.0F).getBlue() : this.color & 0xFF;
   }

   public float getGlRed() {
      return (float)this.getRed() / 255.0F;
   }

   public float getGlBlue() {
      return (float)this.getBlue() / 255.0F;
   }

   public float getGlGreen() {
      return (float)this.getGreen() / 255.0F;
   }

   public float getGlAlpha() {
      return (float)this.getAlpha() / 255.0F;
   }

   public int getAlpha() {
      return this.color >> 24 & 0xFF;
   }

   @NotNull
   public Color getColorObject() {
      return this.rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1.0F, 1.0F, 1.0F) : new Color(this.color, true);
   }

   public int getRawColor() {
      return this.rainbow ? Render2DEngine.rainbow(HudEditor.colorSpeed.getValue(), 1, 1.0F, 1.0F, 1.0F).getRGB() : this.color;
   }

   public boolean isRainbow() {
      return this.rainbow;
   }

   public void setRainbow(boolean rainbow) {
      this.rainbow = rainbow;
   }

   public void setDefault() {
      this.setColor(this.defaultColor);
   }
}
