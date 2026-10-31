package thunder.hack.features.hud.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;

public class PVPResources extends HudElement {
   public PVPResources() {
      super("PVPResources", 50, 50);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      Render2DEngine.drawHudBase(context.getMatrices(), this.getPosX(), this.getPosY(), 50.0F, 50.0F, HudEditor.hudRound.getValue());
      this.setBounds(this.getPosX(), this.getPosY(), 50.0F, 50.0F);
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         Render2DEngine.drawRectDumbWay(
            context.getMatrices(), this.getPosX(), this.getPosY() + 24.5F, this.getPosX() + 50.0F, this.getPosY() + 25.0F, new Color(1426063359, true)
         );
         Render2DEngine.drawRectDumbWay(
            context.getMatrices(), this.getPosX() + 24.5F, this.getPosY() - 1.0F, this.getPosX() + 25.0F, this.getPosY() + 49.0F, new Color(1426063359, true)
         );
      } else {
         Render2DEngine.horizontalGradient(
            context.getMatrices(),
            this.getPosX() + 2.0F,
            this.getPosY() + 24.5F,
            this.getPosX() + 26.0F,
            this.getPosY() + 25.0F,
            Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0),
            HudEditor.textColor.getValue().getColorObject()
         );
         Render2DEngine.horizontalGradient(
            context.getMatrices(),
            this.getPosX() + 26.0F,
            this.getPosY() + 24.5F,
            this.getPosX() + 48.0F,
            this.getPosY() + 25.0F,
            HudEditor.textColor.getValue().getColorObject(),
            Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0)
         );
         Render2DEngine.verticalGradient(
            context.getMatrices(),
            this.getPosX() + 25.5F,
            this.getPosY() + 2.0F,
            this.getPosX() + 26.0F,
            this.getPosY() + 23.0F,
            Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0),
            HudEditor.textColor.getValue().getColorObject()
         );
         Render2DEngine.verticalGradient(
            context.getMatrices(),
            this.getPosX() + 25.5F,
            this.getPosY() + 23.0F,
            this.getPosX() + 26.0F,
            this.getPosY() + 48.0F,
            HudEditor.textColor.getValue().getColorObject(),
            Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0)
         );
      }

      int totemCount = this.getItemCount(Items.TOTEM_OF_UNDYING);
      int xpCount = this.getItemCount(Items.EXPERIENCE_BOTTLE);
      int crystalCount = this.getItemCount(Items.END_CRYSTAL);
      int gappleCount = this.getItemCount(Items.ENCHANTED_GOLDEN_APPLE);
      List<ItemStack> list = new ArrayList<>();
      if (totemCount > 0) {
         list.add(new ItemStack(Items.TOTEM_OF_UNDYING, totemCount));
      }

      if (xpCount > 0) {
         list.add(new ItemStack(Items.EXPERIENCE_BOTTLE, xpCount));
      }

      if (crystalCount > 0) {
         list.add(new ItemStack(Items.END_CRYSTAL, crystalCount));
      }

      if (gappleCount > 0) {
         list.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, gappleCount));
      }

      for (int i = 0; i < list.size(); i++) {
         int offsetX = i % 2 * 25;
         int offsetY = i / 2 * 25;
         context.drawItem(list.get(i), (int)(this.getPosX() + (float)offsetX + 4.0F), (int)(this.getPosY() + (float)offsetY + 4.0F));
         context.getMatrices().push();
         context.getMatrices().translate(0.0F, 0.0F, 151.0F);
         Render2DEngine.drawBlurredShadow(
            context.getMatrices(), this.getPosX() + (float)offsetX + 8.0F, this.getPosY() + (float)offsetY + 8.0F, 9.0F, 9.0F, 12, Color.BLACK
         );
         FontRenderers.sf_medium
            .drawCenteredString(
               context.getMatrices(),
               String.valueOf(list.get(i).getCount()),
               (double)((int)(this.getPosX() + (float)offsetX + 12.0F)),
               (double)((int)(this.getPosY() + (float)offsetY + 11.0F)),
               HudEditor.textColor.getValue().getColor()
            );
         context.getMatrices().pop();
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
