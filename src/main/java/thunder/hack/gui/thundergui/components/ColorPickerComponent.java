package thunder.hack.gui.thundergui.components;

import java.awt.Color;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.ThunderHack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

public class ColorPickerComponent extends SettingElement {
   private final Setting colorSetting;
   private boolean open;
   private float hue;
   private float saturation;
   private float brightness;
   private int alpha;
   private boolean afocused;
   private boolean hfocused;
   private boolean sbfocused;
   private boolean copy_focused;
   private boolean paste_focused;
   private boolean rainbow_focused;
   private float spos;
   private float bpos;
   private float hpos;
   private float apos;
   private Color prevColor;
   private boolean firstInit;

   public ColorPickerComponent(Setting setting) {
      super(setting);
      this.colorSetting = setting;
      this.firstInit = true;
   }

   public ColorSetting getColorSetting() {
      return (ColorSetting)this.colorSetting.getValue();
   }

   @Override
   public void render(MatrixStack stack, int mouseX, int mouseY, float delta) {
      super.render(stack, mouseX, mouseY, delta);
      if (!(this.getY() > (float)(ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height))
         && !(this.getY() < (float)ThunderGui.getInstance().main_posY)) {
         FontRenderers.modules
            .drawString(
               stack,
               this.getSetting().getName(),
               (double)this.getX(),
               (double)(this.getY() + 5.0F),
               this.isHovered() ? -1 : new Color(-1325400065, true).getRGB()
            );
         Render2DEngine.drawBlurredShadow(
            stack, (float)((int)(this.x + this.width - 20.0F)), (float)((int)(this.y + 5.0F)), 14.0F, 6.0F, 10, this.getColorSetting().getColorObject()
         );
         Render2DEngine.drawRound(stack, this.x + this.width - 20.0F, this.y + 5.0F, 14.0F, 6.0F, 1.0F, this.getColorSetting().getColorObject());
         if (this.open) {
            this.renderPicker(stack, mouseX, mouseY, this.getColorSetting().getColorObject());
         }
      }
   }

   @Override
   public void onTick() {
      super.onTick();
   }

   private void renderPicker(MatrixStack stack, int mouseX, int mouseY, Color color) {
      double cx = (double)(this.x + 6.0F);
      float cy = this.y + 20.0F;
      double cw = (double)(this.width - 38.0F);
      double ch = (double)(this.height - 20.0F);
      if (this.prevColor != this.getColorSetting().getColorObject()) {
         this.updatePos();
         this.prevColor = this.getColorSetting().getColorObject();
      }

      if (this.firstInit) {
         this.spos = (float)(cx + cw - (cw - cw * (double)this.saturation));
         this.bpos = (float)((double)cy + (ch - ch * (double)this.brightness));
         this.hpos = (float)((double)cy + ch - 3.0 + (ch - 3.0) * (double)this.hue);
         this.apos = (float)((double)cy + (ch - 3.0 - (ch - 3.0) * (double)((float)this.alpha / 255.0F)));
         this.firstInit = false;
      }

      this.spos = Render2DEngine.scrollAnimate(this.spos, (float)(cx + 40.0 + (cw - 40.0) - (cw - 40.0 - (cw - 40.0) * (double)this.saturation)), 0.6F);
      this.bpos = Render2DEngine.scrollAnimate(this.bpos, (float)((double)cy + (ch - ch * (double)this.brightness)), 0.6F);
      this.hpos = Render2DEngine.scrollAnimate(this.hpos, (float)((double)cy + ch - 3.0 + (ch - 3.0) * (double)this.hue), 0.6F);
      this.apos = Render2DEngine.scrollAnimate(this.apos, (float)((double)cy + (ch - 3.0 - (ch - 3.0) * (double)((float)this.alpha / 255.0F))), 0.6F);
      Color colorA = Color.getHSBColor(this.hue, 0.0F, 1.0F);
      Color colorB = Color.getHSBColor(this.hue, 1.0F, 1.0F);
      Color colorC = new Color(0, 0, 0, 0);
      Color colorD = new Color(0, 0, 0);
      Render2DEngine.horizontalGradient(stack, (float)(cx + 40.0), cy, (float)(cx + cw), (float)((double)cy + ch), colorA, colorB);
      Render2DEngine.verticalGradient(stack, (float)(cx + 40.0), cy, (float)(cx + cw), (float)((double)cy + ch), colorC, colorD);

      for (float i = 1.0F; (double)i < ch - 2.0; i++) {
         float curHue = (float)(1.0 / (ch / (double)i));
         Render2DEngine.drawRect(stack, (float)(cx + cw + 4.0), cy + i, 8.0F, 1.0F, Color.getHSBColor(curHue, 1.0F, 1.0F));
      }

      Render2DEngine.drawRect(stack, (float)(cx + cw + 17.0), cy + 1.0F, 8.0F, (float)(ch - 3.0), new Color(-1));
      Render2DEngine.verticalGradient(
         stack,
         (float)(cx + cw + 17.0),
         (float)((double)cy + 0.8),
         (float)(cx + cw + 25.0),
         (float)((double)cy + ch - 2.0),
         new Color(color.getRed(), color.getGreen(), color.getBlue(), 255),
         new Color(0, 0, 0, 0)
      );
      Render2DEngine.drawRect(stack, (float)(cx + cw + 3.0), this.hpos + 0.5F, 10.0F, 1.0F, Color.WHITE);
      Render2DEngine.drawRect(stack, (float)(cx + cw + 16.0), this.apos + 0.5F, 10.0F, 1.0F, Color.WHITE);
      Render2DEngine.drawRound(stack, this.spos, this.bpos, 3.0F, 3.0F, 1.5F, new Color(-1));
      Color value = Color.getHSBColor(this.hue, this.saturation, this.brightness);
      if (this.sbfocused) {
         this.saturation = (float)(MathUtility.clamp((double)mouseX - (cx + 40.0), 0.0, cw - 40.0) / (cw - 40.0));
         this.brightness = (float)((ch - MathUtility.clamp((double)((float)mouseY - cy), 0.0, ch)) / ch);
         value = Color.getHSBColor(this.hue, this.saturation, this.brightness);
         this.setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), this.alpha));
      }

      if (this.hfocused) {
         this.hue = (float)(-((ch - (double)MathUtility.clamp((float)mouseY - cy, 0.0F, (float)ch)) / ch));
         value = Color.getHSBColor(this.hue, this.saturation, this.brightness);
         this.setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), this.alpha));
      }

      if (this.afocused) {
         this.alpha = (int)((ch - (double)MathUtility.clamp((float)mouseY - cy, 0.0F, (float)ch)) / ch * 255.0);
         this.setColor(new Color(value.getRed(), value.getGreen(), value.getBlue(), this.alpha));
      }

      this.rainbow_focused = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.getX(), (double)cy, 40.0, 10.0);
      this.copy_focused = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.getX(), (double)(cy + 13.0F), 40.0, 10.0);
      this.paste_focused = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.getX(), (double)(cy + 26.0F), 40.0, 10.0);
      Render2DEngine.drawRound(
         stack,
         this.getX(),
         cy,
         40.0F,
         10.0F,
         2.0F,
         this.getColorSetting().isRainbow() ? new Color(86, 63, 105, 250) : (this.rainbow_focused ? new Color(66, 48, 80, 250) : new Color(50, 35, 60, 250))
      );
      Render2DEngine.drawRound(stack, this.getX(), cy + 13.0F, 40.0F, 10.0F, 2.0F, this.copy_focused ? new Color(66, 48, 80, 250) : new Color(50, 35, 60, 250));
      Render2DEngine.drawRound(stack, this.getX(), cy + 26.0F, 40.0F, 9.5F, 2.0F, this.paste_focused ? new Color(66, 48, 80, 250) : new Color(50, 35, 60, 250));
      FontRenderers.modules
         .drawCenteredString(
            stack,
            "rainbow",
            (double)(this.getX() + 20.0F),
            (double)(cy + 3.0F),
            this.rainbow_focused ? -1 : (this.getColorSetting().isRainbow() ? this.getColorSetting().getColor() : new Color(-1241513985, true).getRGB())
         );
      FontRenderers.modules
         .drawCenteredString(stack, "copy", (double)(this.getX() + 20.0F), (double)(cy + 15.5F), this.copy_focused ? -1 : new Color(-1241513985, true).getRGB());
      FontRenderers.modules
         .drawCenteredString(
            stack, "paste", (double)(this.getX() + 20.0F), (double)(cy + 28.5F), this.paste_focused ? -1 : new Color(-1241513985, true).getRGB()
         );
   }

   private void updatePos() {
      float[] hsb = Color.RGBtoHSB(
         this.getColorSetting().getColorObject().getRed(),
         this.getColorSetting().getColorObject().getGreen(),
         this.getColorSetting().getColorObject().getBlue(),
         null
      );
      this.hue = -1.0F + hsb[0];
      this.saturation = hsb[1];
      this.brightness = hsb[2];
      this.alpha = this.getColorSetting().getAlpha();
   }

   private void setColor(Color color) {
      this.getColorSetting().setColor(color.getRGB());
      this.prevColor = color;
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (!(this.getY() > (float)(ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height))
         && !(this.getY() < (float)ThunderGui.getInstance().main_posY)) {
         double cx = (double)(this.x + 4.0F);
         double cy = (double)(this.y + 21.0F);
         double cw = (double)(this.width - 34.0F);
         double ch = (double)(this.height - 20.0F);
         if (Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)(this.x + this.width - 20.0F), (double)(this.y + 5.0F), 14.0, 6.0)) {
            boolean var12 = this.open = !this.open;
         }

         if (this.open) {
            if (Render2DEngine.isHovered((double)mouseX, (double)mouseY, cx + cw + 17.0, cy, 8.0, ch) && button == 0) {
               this.afocused = true;
            } else if (Render2DEngine.isHovered((double)mouseX, (double)mouseY, cx + cw + 4.0, cy, 8.0, ch) && button == 0) {
               this.hfocused = true;
            } else if (Render2DEngine.isHovered((double)mouseX, (double)mouseY, cx + 40.0, cy, cw - 40.0, ch) && button == 0) {
               this.sbfocused = true;
            }

            if (this.rainbow_focused) {
               this.getColorSetting().setRainbow(!this.getColorSetting().isRainbow());
            }

            if (this.copy_focused) {
               ThunderHack.copy_color = this.getColorSetting().getColorObject();
            }

            if (this.paste_focused) {
               this.setColor(ThunderHack.copy_color == null ? this.getColorSetting().getColorObject() : ThunderHack.copy_color);
            }
         }
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int button) {
      this.hfocused = false;
      this.afocused = false;
      this.sbfocused = false;
   }

   @Override
   public void onClose() {
      this.hfocused = false;
      this.afocused = false;
      this.sbfocused = false;
   }

   public boolean isOpen() {
      return this.open;
   }
}
