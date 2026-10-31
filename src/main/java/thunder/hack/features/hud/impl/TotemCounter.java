package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.features.hud.HudElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class TotemCounter extends HudElement {
   private float angle;
   private float prevAngle;

   public TotemCounter() {
      super("TotemCounter", 0, 0);
   }

   @Override
   public void onRender2D(DrawContext context) {
      if (this.getItemCount(Items.TOTEM_OF_UNDYING) != 0) {
         float xPos = ModuleManager.crosshair.getAnimatedPosX();
         float yPos = ModuleManager.crosshair.getAnimatedPosY();
         float factor = Math.abs(this.angle < 0.0F ? this.angle / 15.0F : 0.0F);
         context.getMatrices().push();
         context.getMatrices().translate(xPos, yPos, 0.0F);
         context.getMatrices()
            .multiply(
               RotationAxis.NEGATIVE_Z
                  .rotation(
                     (float)Math.toRadians((double)(-Render2DEngine.interpolateFloat(this.prevAngle, this.angle, (double)Render3DEngine.getTickDelta())))
                  )
            );
         context.getMatrices().translate(-xPos, -yPos, 0.0F);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         context.getMatrices().translate(xPos - 36.0F, yPos - 9.0F, 0.0F);
         context.drawItem(Items.TOTEM_OF_UNDYING.getDefaultStack(), 0, 0);
         context.getMatrices().translate(-(xPos - 36.0F), -(yPos - 9.0F), 0.0F);
         RenderSystem.setShaderColor(1.0F, 1.0F - factor, 1.0F - factor, 1.0F);
         if (factor > 0.0F) {
            Render2DEngine.drawBlurredShadow(
               context.getMatrices(), xPos - 34.0F, yPos - 6.0F, 11.0F, 11.0F, 8, Render2DEngine.injectAlpha(new Color(16711680), (int)(255.0F * factor))
            );
         }

         FontRenderers.sf_bold_mini
            .drawCenteredString(context.getMatrices(), this.getItemCount(Items.TOTEM_OF_UNDYING) + "", (double)(xPos - 28.0F), (double)(yPos + 8.0F), -1);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         context.getMatrices().pop();
      }
   }

   @EventHandler
   public void onTotemPop(TotemPopEvent e) {
      if (e.getEntity() == mc.player) {
         this.angle = -15.0F;
      }
   }

   @Override
   public void onUpdate() {
      this.prevAngle = this.angle;
      if (this.angle < 0.0F) {
         this.angle++;
      }
   }

   public int getItemCount(Item item) {
      if (mc.player == null) {
         return 0;
      } else {
         int n = 0;
         int n2 = 44;

         for (int i = 0; i <= n2; i++) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack.getItem() == item) {
               n += itemStack.getCount();
            }
         }

         return n;
      }
   }
}
