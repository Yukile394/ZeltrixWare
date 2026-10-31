package thunder.hack.gui.notification;

import java.awt.Color;
import net.minecraft.util.Formatting;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.core.manager.client.NotificationManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.EaseOutBack;

public class Notification {
   private final String message;
   private final String title;
   private final String icon;
   private final int lifeTime;
   public final EaseOutBack animation;
   private float y;
   private float width;
   private float animationX;
   private float height = 25.0F;
   private boolean direction = false;
   private final Timer timer = new Timer();

   public Notification(String title, String message, Notification.Type type, int time) {
      this.lifeTime = time;
      this.title = title;
      this.message = message;
      switch (type) {
         case INFO:
            this.icon = "J";
            break;
         case WARNING:
            this.icon = "L";
            break;
         case ERROR:
         case DISABLED:
            this.icon = "I";
            break;
         case ENABLED:
            this.icon = "K";
            break;
         default:
            this.icon = "H";
      }

      this.width = NotificationManager.isDefault()
         ? FontRenderers.sf_bold_mini.getStringWidth(message) + 38.0F
         : FontRenderers.sf_bold_micro.getStringWidth(title + " " + message) + 20.0F;
      this.height = NotificationManager.isDefault() ? 25.0F : 13.0F;
      this.animation = new EaseOutBack(NotificationManager.isDefault() ? 10 : 20);
      this.animationX = this.width;
      if (NotificationManager.isDefault()) {
         this.y = (float)Module.mc.getWindow().getScaledHeight() - this.height;
      } else {
         this.y = (float)Module.mc.getWindow().getScaledHeight() / 2.0F + 10.0F;
      }
   }

   public void render(MatrixStack matrix, float getY) {
      int animatedAlpha = (int)MathUtility.clamp((1.0 - this.animation.getAnimationd()) * 255.0, 0.0, 255.0);
      Color color = new Color(170, 170, 170, animatedAlpha);
      if (NotificationManager.isDefault()) {
         this.direction = this.isFinished();
         this.animationX = (float)((double)this.width * this.animation.getAnimationd());
         this.y = this.animate(this.y, getY);
         float x = (float)(Module.mc.getWindow().getScaledWidth() - 6) - this.width + this.animationX;
         if (HudEditor.hudStyle.is(HudEditor.HudStyle.Glowing)) {
            Render2DEngine.verticalGradient(
               matrix,
               x + 25.0F,
               this.y + 1.0F,
               x + 25.5F,
               this.y + 12.0F,
               Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0),
               HudEditor.textColor.getValue().getColorObject()
            );
            Render2DEngine.verticalGradient(
               matrix,
               x + 25.0F,
               this.y + 11.0F,
               x + 25.5F,
               this.y + 22.0F,
               HudEditor.textColor.getValue().getColorObject(),
               Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0)
            );
         } else {
            Render2DEngine.drawRect(
               matrix, x + 25.0F, this.y + 2.0F, 0.5F, 20.0F, Render2DEngine.injectAlpha(new Color(1157627903, true), (int)((float)animatedAlpha * 0.1F))
            );
         }

         FontRenderers.sf_bold_mini.drawString(matrix, this.title, (double)(x + 30.0F), (double)(this.y + 6.0F), HudEditor.textColor.getValue().getColor());
         FontRenderers.sf_bold_mini.drawString(matrix, this.message, (double)(x + 30.0F), (double)(this.y + 15.0F), color.getRGB());
         FontRenderers.mid_icons.drawString(matrix, this.icon, (double)(x + 5.0F), (double)(this.y + 7.0F), color.getRGB());
      } else {
         this.direction = this.isFinished();
         this.animationX = (float)((double)this.width * this.animation.getAnimationd());
         this.y = this.animate(this.y, getY);
         float x = (float)Module.mc.getWindow().getScaledWidth() / 2.0F - this.width / 2.0F;
         if (HudEditor.hudStyle.is(HudEditor.HudStyle.Glowing)) {
            Render2DEngine.verticalGradient(
               matrix,
               x + 13.0F,
               this.y + 1.0F,
               x + 13.5F,
               this.y + 6.0F,
               Render2DEngine.injectAlpha(color, 0),
               Render2DEngine.injectAlpha(color, animatedAlpha)
            );
            Render2DEngine.verticalGradient(
               matrix,
               x + 13.0F,
               this.y + 6.0F,
               x + 13.5F,
               this.y + 11.0F,
               Render2DEngine.injectAlpha(color, animatedAlpha),
               Render2DEngine.injectAlpha(color, 0)
            );
         } else {
            Render2DEngine.drawRect(
               matrix, x + 13.0F, this.y + 1.0F, 0.5F, 10.0F, Render2DEngine.injectAlpha(new Color(1157627903, true), (int)((float)animatedAlpha * 0.1F))
            );
         }

         FontRenderers.sf_bold_micro.drawString(matrix, this.title + " " + this.message, (double)(x + 16.0F), (double)(this.y + 5.0F), color.getRGB());
         FontRenderers.icons.drawString(matrix, this.icon, (double)(x + 3.0F), (double)(this.y + 5.5F), color.getRGB());
      }
   }

   public void onUpdate() {
      this.animation.update(this.direction);
   }

   public void renderShaders(MatrixStack matrix, float getY) {
      this.direction = this.isFinished();
      this.animationX = (float)((double)this.width * this.animation.getAnimationd());
      this.y = this.animate(this.y, getY);
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         Render2DEngine.drawHudBase2(
            matrix,
            NotificationManager.isDefault()
               ? (float)(Module.mc.getWindow().getScaledWidth() - 6) - this.width + this.animationX
               : (float)Module.mc.getWindow().getScaledWidth() / 2.0F - this.width / 2.0F,
            this.y,
            this.width,
            this.height,
            NotificationManager.isDefault() ? 5.0F : 3.0F,
            HudEditor.blurStrength.getValue(),
            HudEditor.blurOpacity.getValue(),
            (float)MathUtility.clamp(1.0 - this.animation.getAnimationd(), 0.0, 1.0)
         );
      } else {
         Render2DEngine.drawHudBase(
            matrix,
            NotificationManager.isDefault()
               ? (float)(Module.mc.getWindow().getScaledWidth() - 6) - this.width + this.animationX
               : (float)Module.mc.getWindow().getScaledWidth() / 2.0F - this.width / 2.0F,
            this.y,
            this.width,
            this.height,
            NotificationManager.isDefault() ? 5.0F : 3.0F,
            (float)MathUtility.clamp(1.0 - this.animation.getAnimationd(), 0.0, 1.0)
         );
      }
   }

   private boolean isFinished() {
      return this.timer.passedMs((long)this.lifeTime);
   }

   public double getHeight() {
      return (double)this.height;
   }

   public boolean shouldDelete() {
      return this.isFinished() && this.animationX >= this.width - 5.0F;
   }

   public float animate(float value, float target) {
      return value + (target - value) / 8.0F;
   }

   public static enum Type {
      SUCCESS("Success", Formatting.GREEN),
      INFO("Information", Formatting.AQUA),
      WARNING("Warning", Formatting.GOLD),
      ERROR("Error", Formatting.RED),
      ENABLED("Module enabled", Formatting.DARK_GREEN),
      DISABLED("Module disabled", Formatting.DARK_RED);

      final String name;
      final Formatting color;

      private Type(String name, Formatting color) {
         this.name = name;
         this.color = color;
      }

      public String getName() {
         return this.name;
      }

      public Formatting getColor() {
         return this.color;
      }
   }
}
