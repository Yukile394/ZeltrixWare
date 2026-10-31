package thunder.hack.features.hud.impl;

import java.awt.Color;
import net.minecraft.util.Formatting;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class PotionHud extends HudElement {
   private float vAnimation;
   private float hAnimation;
   private final Setting<Boolean> colored = new Setting<>("Colored", false);

   public PotionHud() {
      super("Potions", 100, 100);
   }

   public static String getDuration(StatusEffectInstance pe) {
      if (pe.isInfinite()) {
         return "*:*";
      } else {
         int var1 = pe.getDuration();
         int mins = var1 / 1200;
         String sec = String.format("%02d", var1 % 1200 / 20);
         return mins + ":" + sec;
      }
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      int y_offset1 = 0;
      float max_width = 50.0F;
      float pointerX = 0.0F;

      for (StatusEffectInstance potionEffect : mc.player.getStatusEffects()) {
         StatusEffect potion = (StatusEffect)potionEffect.getEffectType().value();
         if (y_offset1 == 0) {
            y_offset1 += 4;
         }

         y_offset1 += 9;
         float nameWidth = FontRenderers.sf_bold_mini.getStringWidth(potion.getName().getString() + " " + (potionEffect.getAmplifier() + 1));
         float timeWidth = FontRenderers.sf_bold_mini.getStringWidth(getDuration(potionEffect));
         float width = (nameWidth + timeWidth) * 1.4F;
         if (width > max_width) {
            max_width = width;
         }

         if (timeWidth > pointerX) {
            pointerX = timeWidth;
         }
      }

      this.vAnimation = AnimationUtility.fast(this.vAnimation, (float)(14 + y_offset1), 15.0F);
      this.hAnimation = AnimationUtility.fast(this.hAnimation, max_width, 15.0F);
      Render2DEngine.drawHudBase(context.getMatrices(), this.getPosX(), this.getPosY(), this.hAnimation, this.vAnimation, HudEditor.hudRound.getValue());
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Glowing)) {
         FontRenderers.sf_bold
            .drawCenteredString(
               context.getMatrices(),
               "Potions",
               (double)(this.getPosX() + this.hAnimation / 2.0F),
               (double)(this.getPosY() + 4.0F),
               HudEditor.textColor.getValue().getColorObject()
            );
      } else {
         FontRenderers.sf_bold
            .drawGradientCenteredString(context.getMatrices(), "Potions", this.getPosX() + this.hAnimation / 2.0F, this.getPosY() + 4.0F, 10);
      }

      if (y_offset1 > 0) {
         if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawRectDumbWay(
               context.getMatrices(),
               this.getPosX() + 4.0F,
               this.getPosY() + 13.0F,
               this.getPosX() + this.getWidth() - 4.0F,
               this.getPosY() + 13.5F,
               new Color(1426063359, true)
            );
         } else {
            Render2DEngine.horizontalGradient(
               context.getMatrices(),
               this.getPosX() + 2.0F,
               this.getPosY() + 13.7F,
               this.getPosX() + 2.0F + this.hAnimation / 2.0F - 2.0F,
               this.getPosY() + 14.0F,
               Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0),
               HudEditor.textColor.getValue().getColorObject()
            );
            Render2DEngine.horizontalGradient(
               context.getMatrices(),
               this.getPosX() + 2.0F + this.hAnimation / 2.0F - 2.0F,
               this.getPosY() + 13.7F,
               this.getPosX() + 2.0F + this.hAnimation - 4.0F,
               this.getPosY() + 14.0F,
               HudEditor.textColor.getValue().getColorObject(),
               Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0)
            );
         }
      }

      Render2DEngine.addWindow(context.getMatrices(), this.getPosX(), this.getPosY(), this.getPosX() + this.hAnimation, this.getPosY() + this.vAnimation, 1.0);
      int y_offset = 0;

      for (StatusEffectInstance potionEffect : mc.player.getStatusEffects()) {
         StatusEffect potionx = (StatusEffect)potionEffect.getEffectType().value();
         float px = this.getPosX() + (max_width - pointerX - 10.0F);
         context.getMatrices().push();
         context.getMatrices().translate(this.getPosX() + 2.0F, this.getPosY() + 16.0F + (float)y_offset, 0.0F);
         context.drawSprite(0, 0, 0, 8, 8, mc.getStatusEffectSpriteManager().getSprite(potionEffect.getEffectType()));
         context.getMatrices().pop();
         FontRenderers.sf_bold_mini
            .drawString(
               context.getMatrices(),
               potionx.getName().getString() + " " + Formatting.RED + (potionEffect.getAmplifier() + 1),
               (double)(this.getPosX() + 12.0F),
               (double)(this.getPosY() + 19.0F + (float)y_offset),
               HudEditor.textColor.getValue().getColor()
            );
         FontRenderers.sf_bold_mini
            .drawCenteredString(
               context.getMatrices(),
               getDuration(potionEffect),
               (double)(px + (this.getPosX() + max_width - px) / 2.0F),
               (double)(this.getPosY() + 19.0F + (float)y_offset),
               HudEditor.textColor.getValue().getColor()
            );
         Render2DEngine.drawRect(context.getMatrices(), px, this.getPosY() + 17.0F + (float)y_offset, 0.5F, 8.0F, new Color(1157627903, true));
         y_offset += 9;
      }

      Render2DEngine.popWindow();
      this.setBounds(this.getPosX(), this.getPosY(), this.hAnimation, this.vAnimation);
   }
}
