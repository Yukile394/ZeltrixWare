package thunder.hack.features.hud.impl;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.EaseOutCirc;

public class TimerIndicator extends HudElement {
   private final EaseOutCirc timerAnimation = new EaseOutCirc();

   public TimerIndicator() {
      super("TimerIndicator", 60, 10);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         Render2DEngine.drawRoundedBlur(
            context.getMatrices(), this.getPosX(), this.getPosY(), 65.0F, 15.0F, 3.0F, HudEditor.blurColor.getValue().getColorObject()
         );
         Render2DEngine.drawRect(context.getMatrices(), this.getPosX(), this.getPosY(), 65.0F, 15.0F, 3.0F, 0.4F);
         FontRenderers.sf_bold_mini
            .drawCenteredString(
               context.getMatrices(), "100%", (double)(this.getPosX() + 32.0F), (double)(this.getPosY() + 5.5F), new Color(200, 200, 200, 255).getRGB()
            );
         this.setBounds(this.getPosX(), this.getPosY(), 65.0F, 15.0F);
      } else {
         Render2DEngine.drawGradientBlurredShadow(
            context.getMatrices(),
            this.getPosX() - 1.0F,
            this.getPosY() - 1.0F,
            62.0F,
            12.0F,
            6,
            HudEditor.getColor(90),
            HudEditor.getColor(180),
            HudEditor.getColor(0),
            HudEditor.getColor(270)
         );
         Render2DEngine.drawRect(context.getMatrices(), this.getPosX(), this.getPosY(), 60.0F, 10.0F, new Color(-1644167168, true));
         Render2DEngine.draw2DGradientRect(
            context.getMatrices(),
            this.getPosX(),
            this.getPosY(),
            this.getPosX() + 60.0F,
            this.getPosY() + 10.0F,
            HudEditor.getColor(90),
            HudEditor.getColor(180),
            HudEditor.getColor(0),
            HudEditor.getColor(270)
         );
         Render2DEngine.drawBlurredShadow(context.getMatrices(), this.getPosX() + 20.0F, this.getPosY(), 22.0F, 10.0F, 6, new Color(1191182336, true));
         FontRenderers.sf_bold_mini
            .drawCenteredString(
               context.getMatrices(), "100%", (double)(this.getPosX() + 31.0F), (double)(this.getPosY() + 3.5F), new Color(200, 200, 200, 255).getRGB()
            );
         this.setBounds(this.getPosX(), this.getPosY(), 60.0F, 10.0F);
      }
   }

   @Override
   public void onUpdate() {
      this.timerAnimation.update();
   }
}
