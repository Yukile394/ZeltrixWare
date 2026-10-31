package thunder.hack.gui.thundergui.components;

import java.awt.Color;
import java.util.Objects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

public class SliderComponent extends SettingElement {
   private final float min;
   private final float max;
   public boolean listening;
   public String Stringnumber = "";
   private float animation;
   private double stranimation;
   private boolean dragging;

   public SliderComponent(Setting setting) {
      super(setting);
      this.min = ((Number)setting.getMin()).floatValue();
      this.max = ((Number)setting.getMax()).floatValue();
   }

   public static String removeLastChar(String str) {
      String output = "";
      if (str != null && !str.isEmpty()) {
         output = str.substring(0, str.length() - 1);
      }

      return output;
   }

   @Override
   public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
      super.render(stack, mouseX, mouseY, partialTicks);
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
         double currentPos = (double)((((Number)this.setting.getValue()).floatValue() - this.min) / (this.max - this.min));
         this.stranimation = this.stranimation + ((double)(((Number)this.setting.getValue()).floatValue() * 100.0F / 100.0F) - this.stranimation) / 2.0;
         this.animation = Render2DEngine.scrollAnimate(this.animation, (float)currentPos, 0.5F);
         Color color = new Color(-1973791);
         Render2DEngine.drawRound(stack, this.x + 54.0F, this.y + this.height - 8.0F, 90.0F, 1.0F, 0.5F, new Color(-15856114));
         Render2DEngine.drawRound(stack, this.x + 54.0F, this.y + this.height - 8.0F, 90.0F * this.animation, 1.0F, 0.5F, color);
         Render2DEngine.drawRound(stack, this.x + 52.0F + 90.0F * this.animation, this.y + this.height - 9.5F, 4.0F, 4.0F, 1.5F, color);
         if ((float)mouseX > this.x + 154.0F
            && (float)mouseX < this.x + 176.0F
            && (float)mouseY > this.y + this.height - 11.0F
            && (float)mouseY < this.y + this.height - 4.0F) {
            Render2DEngine.drawRound(stack, this.x + 154.0F, this.y + this.height - 11.0F, 22.0F, 7.0F, 0.5F, new Color(82, 57, 100, 178));
         } else {
            Render2DEngine.drawRound(stack, this.x + 154.0F, this.y + this.height - 11.0F, 22.0F, 7.0F, 0.5F, new Color(50, 35, 60, 178));
         }

         if (!this.listening) {
            if (this.setting.getValue() instanceof Float) {
               FontRenderers.modules
                  .drawString(
                     stack,
                     String.valueOf(MathUtility.round((Float)this.setting.getValue(), 2)),
                     (double)(this.x + 156.0F),
                     (double)(this.y + this.height - 9.0F),
                     new Color(-1157627905, true).getRGB()
                  );
            }

            if (this.setting.getValue() instanceof Integer) {
               FontRenderers.modules
                  .drawString(
                     stack,
                     String.valueOf(this.setting.getValue()),
                     (double)(this.x + 156.0F),
                     (double)(this.y + this.height - 9.0F),
                     new Color(-1157627905, true).getRGB()
                  );
            }
         } else if (Objects.equals(this.Stringnumber, "")) {
            FontRenderers.modules
               .drawString(stack, "...", (double)(this.x + 156.0F), (double)(this.y + this.height - 9.0F), new Color(-1157627905, true).getRGB());
         } else {
            FontRenderers.modules
               .drawString(stack, this.Stringnumber, (double)(this.x + 156.0F), (double)(this.y + this.height - 9.0F), new Color(-1157627905, true).getRGB());
         }

         this.animation = MathUtility.clamp(this.animation, 0.0F, 1.0F);
         if (this.dragging) {
            this.setValue(mouseX, (double)(this.x + 54.0F), 90.0);
         }
      }
   }

   private void setValue(int mouseX, double x, double width) {
      double diff = (double)(((Number)this.setting.getMax()).floatValue() - ((Number)this.setting.getMin()).floatValue());
      double percentBar = MathHelper.clamp(((double)mouseX - x) / width, 0.0, 1.0);
      double value = (double)((Number)this.setting.getMin()).floatValue() + percentBar * diff;
      if (this.setting.getValue() instanceof Float) {
         this.setting.setValue((float)value);
      } else if (this.setting.getValue() instanceof Integer) {
         this.setting.setValue((int)value);
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (!(this.getY() > (float)(ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height))
         && !(this.getY() < (float)ThunderGui.getInstance().main_posY)) {
         if ((float)mouseX > this.x + 154.0F
            && (float)mouseX < this.x + 176.0F
            && (float)mouseY > this.y + this.height - 11.0F
            && (float)mouseY < this.y + this.height - 4.0F) {
            this.Stringnumber = "";
            this.listening = true;
         } else if (button == 0 && this.hovered) {
            this.dragging = true;
         }

         if (this.listening) {
            ThunderHack.currentKeyListener = ThunderHack.KeyListening.Sliders;
         }
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int button) {
      this.dragging = false;
   }

   @Override
   public void resetAnimation() {
      this.dragging = false;
      this.animation = 0.0F;
      this.stranimation = 0.0;
   }

   @Override
   public void keyTyped(String typedChar, int keyCode) {
      if (ThunderHack.currentKeyListener == ThunderHack.KeyListening.Sliders) {
         if (this.listening) {
            switch (keyCode) {
               case 256:
                  this.listening = false;
                  this.Stringnumber = "";
                  return;
               case 257:
                  try {
                     this.searchNumber();
                  } catch (Exception var4) {
                     this.Stringnumber = "";
                     this.listening = false;
                  }

                  return;
               case 258:
               default:
                  this.Stringnumber = this.Stringnumber + GLFW.glfwGetKeyName(keyCode, 0);
                  break;
               case 259:
                  this.Stringnumber = removeLastChar(this.Stringnumber);
                  return;
            }
         }
      }
   }

   private void searchNumber() {
      if (this.setting.getValue() instanceof Float) {
         this.setting.setValue(Float.valueOf(this.Stringnumber));
         this.Stringnumber = "";
         this.listening = false;
      } else if (this.setting.getValue() instanceof Integer) {
         this.setting.setValue(Integer.valueOf(this.Stringnumber));
         this.Stringnumber = "";
         this.listening = false;
      }
   }

   @Override
   public void checkMouseWheel(float value) {
      super.checkMouseWheel(value);
      if (this.isHovered()) {
         ThunderGui.scroll_lock = true;
         if (value < 0.0F) {
            if (this.setting.getValue() instanceof Float) {
               this.setting.setValue((Float)this.setting.getValue() + 0.01F);
            } else if (this.setting.getValue() instanceof Integer) {
               this.setting.setValue((Integer)this.setting.getValue() + 1);
            }
         } else if (value > 0.0F) {
            if (this.setting.getValue() instanceof Float) {
               this.setting.setValue((Float)this.setting.getValue() - 0.01F);
            } else if (this.setting.getValue() instanceof Integer) {
               this.setting.setValue((Integer)this.setting.getValue() - 1);
            }
         }
      }
   }
}
