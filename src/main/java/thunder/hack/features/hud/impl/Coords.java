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
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class Coords extends HudElement {
   private final Setting<Coords.NetherCoords> netherCoords = new Setting<>("NetherCoords", Coords.NetherCoords.On);

   public Coords() {
      super("Coords", 100, 10);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      int posX = (int)mc.player.getX();
      int posY = (int)mc.player.getY();
      int posZ = (int)mc.player.getZ();
      float nether = !PlayerUtility.isInHell() ? 0.125F : 8.0F;
      int hposX = (int)(mc.player.getX() * (double)nether);
      int hposZ = (int)(mc.player.getZ() * (double)nether);
      String coordinates = "XYZ "
         + Formatting.WHITE
         + posX
         + " "
         + posY
         + " "
         + posZ
         + Formatting.WHITE
         + (
            !this.netherCoords.is(Coords.NetherCoords.On) && (!this.netherCoords.is(Coords.NetherCoords.OnlyNether) || PlayerUtility.isInHell())
               ? ""
               : " [" + Formatting.RESET + hposX + " " + hposZ + Formatting.WHITE + "]"
         );
      float pX = this.getPosX() > (float)mc.getWindow().getScaledWidth() / 2.0F
         ? this.getPosX() - FontRenderers.getModulesRenderer().getStringWidth(coordinates)
         : this.getPosX();
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         Render2DEngine.drawRoundedBlur(
            context.getMatrices(),
            pX,
            this.getPosY(),
            FontRenderers.getModulesRenderer().getStringWidth(coordinates) + 21.0F,
            13.0F,
            3.0F,
            HudEditor.blurColor.getValue().getColorObject()
         );
         Render2DEngine.drawRect(context.getMatrices(), pX + 14.0F, this.getPosY() + 2.0F, 0.5F, 8.0F, new Color(1157627903, true));
         Render2DEngine.setupRender();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.setShaderTexture(0, TextureStorage.coordsIcon);
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
         .drawString(context.getMatrices(), coordinates, (double)(pX + 18.0F), (double)(this.getPosY() + 5.0F), HudEditor.getColor(1).getRGB());
      this.setBounds(pX, this.getPosY(), FontRenderers.getModulesRenderer().getStringWidth(coordinates) + 21.0F, 13.0F);
   }

   private static enum NetherCoords {
      Off,
      On,
      OnlyNether;
   }
}
