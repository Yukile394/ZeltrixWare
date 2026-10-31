package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.util.Formatting;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class MemoryHud extends HudElement {
   public MemoryHud() {
      super("MemoryHud", 100, 10);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      long m = Runtime.getRuntime().maxMemory();
      long t = Runtime.getRuntime().totalMemory();
      long f = Runtime.getRuntime().freeMemory();
      long o = t - f;
      String str = "Mem: " + Formatting.WHITE + this.toMiB(o) + "/" + this.toMiB(m) + "MB [" + o * 100L / m + "%]";
      float pX = this.getPosX() > (float)mc.getWindow().getScaledWidth() / 2.0F
         ? this.getPosX() - FontRenderers.getModulesRenderer().getStringWidth(str)
         : this.getPosX();
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         Render2DEngine.drawRoundedBlur(
            context.getMatrices(),
            pX,
            this.getPosY(),
            FontRenderers.getModulesRenderer().getStringWidth(str) + 21.0F,
            13.0F,
            3.0F,
            HudEditor.blurColor.getValue().getColorObject()
         );
         Render2DEngine.drawRect(context.getMatrices(), pX + 14.0F, this.getPosY() + 2.0F, 0.5F, 8.0F, new Color(1157627903, true));
         Render2DEngine.setupRender();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.setShaderTexture(0, TextureStorage.memoryIcon);
         Render2DEngine.renderGradientTexture(
            context.getMatrices(),
            (double)(pX + 2.0F),
            (double)(this.getPosY() + 1.0F),
            10.0,
            10.0,
            0.0F,
            0.0F,
            512.0,
            512.0,
            512.0,
            512.0,
            HudEditor.getColor(270),
            HudEditor.getColor(0),
            HudEditor.getColor(180),
            HudEditor.getColor(90)
         );
         Render2DEngine.endRender();
      }

      FontRenderers.getModulesRenderer()
         .drawString(context.getMatrices(), str, (double)(pX + 18.0F), (double)(this.getPosY() + 5.0F), HudEditor.getColor(1).getRGB());
      this.setBounds(pX, this.getPosY(), FontRenderers.getModulesRenderer().getStringWidth(str) + 21.0F, 13.0F);
   }

   private long toMiB(long bytes) {
      return bytes / 1024L / 1024L;
   }
}
