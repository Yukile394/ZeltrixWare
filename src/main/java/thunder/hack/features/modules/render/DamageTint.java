package thunder.hack.features.modules.render;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.features.modules.Module;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;

public class DamageTint extends Module {
   public DamageTint() {
      super("DamageTint", Module.Category.RENDER);
   }

   @Override
   public void onRender2D(DrawContext context) {
      float factor = 1.0F - MathUtility.clamp(mc.player.getHealth(), 0.0F, 12.0F) / 12.0F;
      Color red = new Color(16711680, true);
      if (factor < 1.0F) {
         Render2DEngine.draw2DGradientRect(
            context.getMatrices(),
            0.0F,
            0.0F,
            (float)mc.getWindow().getScaledWidth(),
            (float)mc.getWindow().getScaledHeight(),
            Render2DEngine.injectAlpha(red, (int)(factor * 170.0F)),
            red,
            Render2DEngine.injectAlpha(red, (int)(factor * 170.0F)),
            red
         );
      }
   }
}
