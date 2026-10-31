package thunder.hack.gui.windows.impl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.StringHelper;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.registry.Registries;
import net.minecraft.client.render.VertexFormat.DrawMode;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.clickui.impl.SliderElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.windows.WindowBase;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ItemSelectSetting;
import thunder.hack.utility.render.Render2DEngine;

public class ItemSelectWindow extends WindowBase {
   private Setting<ItemSelectSetting> itemSetting;
   private ArrayList<ItemSelectWindow.ItemPlate> itemPlates = new ArrayList<>();
   private ArrayList<ItemSelectWindow.ItemPlate> allItems = new ArrayList<>();
   private boolean allTab = true;
   private boolean listening = false;
   private String search = "Search";

   public ItemSelectWindow(Setting<ItemSelectSetting> itemSetting) {
      this(
         (float)Module.mc.getWindow().getScaledWidth() / 2.0F - 100.0F,
         (float)Module.mc.getWindow().getScaledHeight() / 2.0F - 150.0F,
         200.0F,
         300.0F,
         itemSetting
      );
   }

   public ItemSelectWindow(float x, float y, float width, float height, Setting<ItemSelectSetting> itemSetting) {
      super(x, y, width, height, "Items / " + Formatting.GRAY + itemSetting.getModule().getName(), null, null);
      this.itemSetting = itemSetting;
      this.refreshItemPlates();
      int id1 = 0;

      for (Block block : Registries.BLOCK) {
         this.allItems.add(new ItemSelectWindow.ItemPlate((float)id1, (float)(id1 * 20), block.asItem(), block.getTranslationKey()));
         id1++;
      }

      for (Item item : Registries.ITEM) {
         this.allItems.add(new ItemSelectWindow.ItemPlate((float)id1, (float)(id1 * 20), item, item.getTranslationKey()));
         id1++;
      }
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY) {
      super.render(context, mouseX, mouseY);
      boolean hover1 = Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)(this.getX() + this.getWidth() - 90.0F), (double)(this.getY() + 3.0F), 70.0, 10.0
      );
      Render2DEngine.drawRect(
         context.getMatrices(),
         this.getX() + this.getWidth() - 90.0F,
         this.getY() + 3.0F,
         70.0F,
         10.0F,
         hover1 ? new Color(-981236861, true) : new Color(-984131753, true)
      );
      FontRenderers.sf_medium_mini
         .drawString(
            context.getMatrices(), this.search, (double)(this.getX() + this.getWidth() - 86.0F), (double)(this.getY() + 7.0F), new Color(14013909).getRGB()
         );
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      int tabColor1 = this.allTab ? new Color(14013909).getRGB() : Color.GRAY.getRGB();
      int tabColor2 = this.allTab ? Color.GRAY.getRGB() : new Color(12434877).getRGB();
      BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
      bufferBuilder.vertex(this.getX() + 1.5F, this.getY() + 29.0F, 0.0F).color(Color.DARK_GRAY.getRGB());
      bufferBuilder.vertex(this.getX() + 8.0F, this.getY() + 29.0F, 0.0F).color(tabColor1);
      bufferBuilder.vertex(this.getX() + 8.0F, this.getY() + 19.0F, 0.0F).color(tabColor1);
      bufferBuilder.vertex(this.getX() + 48.0F, this.getY() + 19.0F, 0.0F).color(tabColor1);
      bufferBuilder.vertex(this.getX() + 54.0F, this.getY() + 29.0F, 0.0F).color(tabColor1);
      bufferBuilder.vertex(this.getX() + 52.0F, this.getY() + 25.0F, 0.0F).color(tabColor2);
      bufferBuilder.vertex(this.getX() + 52.0F, this.getY() + 19.0F, 0.0F).color(tabColor2);
      bufferBuilder.vertex(this.getX() + 92.0F, this.getY() + 19.0F, 0.0F).color(tabColor2);
      bufferBuilder.vertex(this.getX() + 100.0F, this.getY() + 29.0F, 0.0F).color(Color.GRAY.getRGB());
      bufferBuilder.vertex(this.getX() + this.getWidth() - 1.0F, this.getY() + 29.0F, 0.0F).color(Color.DARK_GRAY.getRGB());
      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      FontRenderers.sf_medium_mini.drawString(context.getMatrices(), "All", (double)(this.getX() + 25.0F), (double)(this.getY() + 25.0F), tabColor1);
      FontRenderers.sf_medium_mini.drawString(context.getMatrices(), "Selected", (double)(this.getX() + 60.0F), (double)(this.getY() + 25.0F), tabColor2);
      if (!this.allTab && this.itemPlates.isEmpty()) {
         FontRenderers.sf_medium
            .drawCenteredString(
               context.getMatrices(),
               ClientSettings.isRu() ? "\u0422\u0443\u0442 \u043f\u043e\u043a\u0430 \u043f\u0443\u0441\u0442\u043e" : "It's empty here yet",
               (double)(this.getX() + this.getWidth() / 2.0F),
               (double)(this.getY() + this.getHeight() / 2.0F),
               new Color(12434877).getRGB()
            );
      }

      Render2DEngine.addWindow(
         context.getMatrices(), this.getX(), this.getY() + 30.0F, this.getX() + this.getWidth(), this.getY() + this.getHeight() - 1.0F, 1.0
      );

      for (ItemSelectWindow.ItemPlate itemPlate : this.allTab ? this.allItems : this.itemPlates) {
         if (!(itemPlate.offset + this.getY() + 25.0F + this.getScrollOffset() > this.getY() + this.getHeight())
            && !(itemPlate.offset + this.getScrollOffset() + this.getY() + 10.0F < this.getY())) {
            context.getMatrices().push();
            context.getMatrices().translate(this.getX() + 6.0F, itemPlate.offset + this.getY() + 32.0F + this.getScrollOffset(), 0.0F);
            context.drawItem(itemPlate.item().getDefaultStack(), 0, 0);
            context.getMatrices().pop();
            FontRenderers.sf_medium
               .drawString(
                  context.getMatrices(),
                  I18n.translate(itemPlate.key(), new Object[0]),
                  (double)(this.getX() + 26.0F),
                  (double)(itemPlate.offset + this.getY() + 38.0F + this.getScrollOffset()),
                  new Color(12434877).getRGB()
               );
            boolean hover2 = Render2DEngine.isHovered(
               (double)mouseX,
               (double)mouseY,
               (double)(this.getX() + this.getWidth() - 20.0F),
               (double)(itemPlate.offset + this.getY() + 35.0F + this.getScrollOffset()),
               11.0,
               11.0
            );
            Render2DEngine.drawRect(
               context.getMatrices(),
               this.getX() + this.getWidth() - 20.0F,
               itemPlate.offset + this.getY() + 35.0F + this.getScrollOffset(),
               11.0F,
               11.0F,
               hover2 ? new Color(-981828998, true) : new Color(-984131753, true)
            );
            boolean selected = this.itemPlates.stream().anyMatch(sI -> Objects.equals(sI.key, itemPlate.key));
            if (this.allTab && !selected) {
               FontRenderers.categories
                  .drawString(
                     context.getMatrices(),
                     "+",
                     (double)(this.getX() + this.getWidth() - 17.0F),
                     (double)(itemPlate.offset + this.getY() + 39.0F + this.getScrollOffset()),
                     -1
                  );
            } else {
               FontRenderers.icons
                  .drawString(
                     context.getMatrices(),
                     "w",
                     (double)(this.getX() + this.getWidth() - 19.5F),
                     (double)(itemPlate.offset + this.getY() + 39.0F + this.getScrollOffset()),
                     -1
                  );
            }
         }
      }

      this.setMaxElementsHeight((float)((this.allTab ? this.allItems : this.itemPlates).size() * 20));
      Render2DEngine.popWindow();
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int button) {
      super.mouseClicked(mouseX, mouseY, button);
      if (Render2DEngine.isHovered(mouseX, mouseY, (double)(this.getX() + 8.0F), (double)(this.getY() + 19.0F), 52.0, 19.0)) {
         this.allTab = true;
         this.resetScroll();
         Managers.SOUND.playBoolean();
      }

      if (Render2DEngine.isHovered(mouseX, mouseY, (double)(this.getX() + 54.0F), (double)(this.getY() + 19.0F), 70.0, 19.0)) {
         this.allTab = false;
         this.resetScroll();
         Managers.SOUND.playBoolean();
      }

      if (Render2DEngine.isHovered(mouseX, mouseY, (double)(this.getX() + this.getWidth() - 90.0F), (double)(this.getY() + 3.0F), 70.0, 10.0)) {
         this.listening = true;
         this.search = "";
      }

      if (Render2DEngine.isHovered(mouseX, mouseY, (double)(this.getX() + this.getWidth() - 15.0F), (double)(this.getY() + 3.0F), 10.0, 10.0)) {
         Module.mc.setScreen(ClickGUI.getClickGui());
      }

      for (ItemSelectWindow.ItemPlate itemPlate : Lists.newArrayList(this.allTab ? this.allItems : this.itemPlates)) {
         if (!((float)((int)(itemPlate.offset + this.getY() + 50.0F)) + this.getScrollOffset() > this.getY() + this.getHeight())) {
            String name = itemPlate.key().replace("item.minecraft.", "").replace("block.minecraft.", "");
            if (Render2DEngine.isHovered(
               mouseX,
               mouseY,
               (double)(this.getX() + this.getWidth() - 20.0F),
               (double)(itemPlate.offset + this.getY() + 35.0F + this.getScrollOffset()),
               10.0,
               10.0
            )) {
               boolean selected = this.itemPlates.stream().anyMatch(sI -> Objects.equals(sI.key(), itemPlate.key));
               if (this.allTab && !selected) {
                  if (this.itemSetting.getValue().getItemsById().contains(name)) {
                     continue;
                  }

                  this.itemSetting.getValue().getItemsById().add(name);
                  this.refreshItemPlates();
               } else {
                  this.itemSetting.getValue().getItemsById().remove(name);
                  this.refreshItemPlates();
               }

               Managers.SOUND.playScroll();
            }
         }
      }
   }

   @Override
   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode != 70
         || !InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 341) && !InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 345)) {
         if (this.listening) {
            switch (keyCode) {
               case 32:
                  this.search = this.search + " ";
                  break;
               case 256:
                  this.listening = false;
                  this.search = "Search";
                  this.refreshAllItems();
                  break;
               case 259:
                  this.search = SliderElement.removeLastChar(this.search);
                  this.refreshAllItems();
                  if (Objects.equals(this.search, "")) {
                     this.listening = false;
                     this.search = "Search";
                  }
            }
         }
      } else {
         this.listening = !this.listening;
      }
   }

   @Override
   public void charTyped(char key, int keyCode) {
      if (StringHelper.isValidChar(key) && this.listening) {
         this.search = this.search + key;
         this.refreshAllItems();
      }
   }

   private void refreshItemPlates() {
      this.itemPlates.clear();
      int id = 0;

      for (Block block : Registries.BLOCK) {
         if (this.itemSetting.getValue().getItemsById().contains(block.getTranslationKey().replace("block.minecraft.", ""))) {
            this.itemPlates.add(new ItemSelectWindow.ItemPlate((float)id, (float)(id * 20), block.asItem(), block.getTranslationKey()));
            id++;
         }
      }

      for (Item item : Registries.ITEM) {
         if (this.itemSetting.getValue().getItemsById().contains(item.getTranslationKey().replace("item.minecraft.", ""))) {
            this.itemPlates.add(new ItemSelectWindow.ItemPlate((float)id, (float)(id * 20), item, item.getTranslationKey()));
            id++;
         }
      }
   }

   private void refreshAllItems() {
      this.allItems.clear();
      this.resetScroll();
      int id1 = 0;

      for (Block block : Registries.BLOCK) {
         if (this.search.equals("Search")
            || this.search.isEmpty()
            || block.getTranslationKey().contains(this.search)
            || I18n.translate(block.getTranslationKey(), new Object[0]).toLowerCase().contains(this.search.toLowerCase())) {
            this.allItems.add(new ItemSelectWindow.ItemPlate((float)id1, (float)(id1 * 20), block.asItem(), block.getTranslationKey()));
            id1++;
         }
      }

      for (Item item : Registries.ITEM) {
         if (this.search.equals("Search")
            || this.search.isEmpty()
            || item.getTranslationKey().contains(this.search)
            || item.getName().getString().toLowerCase().contains(this.search.toLowerCase())) {
            this.allItems.add(new ItemSelectWindow.ItemPlate((float)id1, (float)(id1 * 20), item, item.getTranslationKey()));
            id1++;
         }
      }
   }

   private static record ItemPlate(float id, float offset, Item item, String key) {
   }
}
