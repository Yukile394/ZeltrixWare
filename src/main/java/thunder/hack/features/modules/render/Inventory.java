package thunder.hack.features.modules.render;

import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.features.hud.HudElement;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

public class Inventory extends HudElement {
   public final Setting<Inventory.ThemeStyle> themeStyle = new Setting<>("Style", Inventory.ThemeStyle.Transparent);

   public Inventory() {
      super("Inventory", 162, 54);
   }

   @EventHandler
   @Override
   public void onMouse(EventMouse event) {
      if (event.getAction() == 1 && event.getButton() == 1 && this.isHovering()) {
         Inventory.ThemeStyle[] styles = Inventory.ThemeStyle.values();
         this.themeStyle.setValue(styles[(this.themeStyle.getValue().ordinal() + 1) % styles.length]);
      }

      super.onMouse(event);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      if (mc.player != null) {
         float x = this.getPosX();
         float y = this.getPosY();
         if (this.themeStyle.getValue() == Inventory.ThemeStyle.Solid) {
            Render2DEngine.drawRound(context.getMatrices(), x, y, 162.0F, 54.0F, 4.0F, new Color(15, 15, 15, 230));
         } else {
            Render2DEngine.drawRound(context.getMatrices(), x, y, 162.0F, 54.0F, 4.0F, new Color(0, 0, 0, 100));
         }

         DefaultedList<ItemStack> inventory = mc.player.getInventory().main;

         for (int i = 9; i < 36; i++) {
            ItemStack stack = (ItemStack)inventory.get(i);
            if (!stack.isEmpty()) {
               int slotX = (int)x + (i - 9) % 9 * 18;
               int slotY = (int)y + (i - 9) / 9 * 18;
               context.drawItem(stack, slotX, slotY);
               context.drawItemInSlot(mc.textRenderer, stack, slotX, slotY);
            }
         }

         this.setBounds(x, y, 162.0F, 54.0F);
      }
   }

   public static enum ThemeStyle {
      Transparent,
      Solid;
   }
}
