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
import thunder.hack.events.impl.EventEatFood;
import thunder.hack.features.hud.HudElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class GapplesHud extends HudElement {
   private float angle;
   private float prevAngle;
   private final Setting<Boolean> crapple = new Setting<>("Crapple", true);

   public GapplesHud() {
      super("GapplesHud", 0, 0);
   }

   @Override
   public void onRender2D(DrawContext context) {
      Item targetItem = this.crapple.getValue() ? Items.GOLDEN_APPLE : Items.ENCHANTED_GOLDEN_APPLE;
      if (this.getItemCount(targetItem) != 0) {
         float xPos = ModuleManager.crosshair.getAnimatedPosX();
         float yPos = ModuleManager.crosshair.getAnimatedPosY();
         float factor = this.angle > 0.0F ? this.angle / 15.0F : 0.0F;
         float factor2 = 1.0F - (float)mc.player.getItemUseTime() / 40.0F;
         if (mc.player.getActiveItem().getItem() != targetItem) {
            factor2 = 1.0F;
         }

         factor2 = MathUtility.clamp(factor2, 0.01F, 1.0F);
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
         RenderSystem.setShaderColor(0.3F, 0.3F, 0.3F, 1.0F);
         context.getMatrices().translate(xPos + 20.0F, yPos - 9.0F, 0.0F);
         context.drawItem(targetItem.getDefaultStack(), 0, 0);
         context.getMatrices().translate(-(xPos + 20.0F), -(yPos - 9.0F), 0.0F);
         RenderSystem.setShaderColor(1.0F, 1.0F - factor, 1.0F - factor, 1.0F);
         context.getMatrices().translate(xPos + 28.0F, yPos - 1.0F, 0.0F);
         context.getMatrices().scale(factor2, factor2, 1.0F);
         context.drawItem(targetItem.getDefaultStack(), -8, -8);
         context.getMatrices().scale(factor2 != 0.0F ? 1.0F / factor2 : 1.0F, factor2 != 0.0F ? 1.0F / factor2 : 1.0F, 1.0F);
         context.getMatrices().translate(-(xPos + 28.0F), -(yPos - 1.0F), 0.0F);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         if (factor > 0.0F) {
            Render2DEngine.drawBlurredShadow(
               context.getMatrices(), xPos + 22.0F, yPos - 6.0F, 11.0F, 11.0F, 8, Render2DEngine.injectAlpha(new Color(16717056), (int)(255.0F * factor))
            );
         }

         FontRenderers.sf_bold_mini
            .drawCenteredString(context.getMatrices(), this.getItemCount(targetItem) + "", (double)(xPos + 28.5F), (double)(yPos + 8.0F), -1);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         context.getMatrices().pop();
      }
   }

   @EventHandler
   public void onEatFood(EventEatFood e) {
      if (e.getFood().getItem() == Items.GOLDEN_APPLE || e.getFood().getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
         this.angle = 15.0F;
      }
   }

   @Override
   public void onUpdate() {
      this.prevAngle = this.angle;
      if (this.angle > 0.0F) {
         this.angle--;
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
