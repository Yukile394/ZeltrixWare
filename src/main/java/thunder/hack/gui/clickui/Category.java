package thunder.hack.gui.clickui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.BaritoneSettings;
import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.impl.SearchBar;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class Category extends AbstractCategory {
   private final Identifier ICON;
   private boolean scrollHover;
   private final List<AbstractButton> buttons = new ArrayList<>();
   public float catHeight;

   public Category(Module.Category category, ArrayList<Module> features, float x, float y, float width, float height) {
      super(category.getName(), x, y, width, height);
      this.ICON = Identifier.of(
         "thunderhack", "textures/gui/headers/" + (Module.Category.isCustomCategory(category) ? "stock" : category.getName().toLowerCase()) + ".png"
      );
      if (category.getName().equals("Client")) {
         this.buttons.add(new SearchBar());
      }

      features.forEach(feature -> {
         if (!(feature instanceof BaritoneSettings) || ThunderHack.baritone) {
            this.buttons.add(new ModuleButton(feature));
         }
      });
   }

   @Override
   public void init() {
      this.buttons.forEach(AbstractButton::init);
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.setWidth((float)ModuleManager.clickGui.moduleWidth.getValue().intValue());
      float headerH = this.height;
      this.scrollHover = Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)this.getX(), (double)(this.getY() + headerH), (double)this.width, (double)(this.catHeight + 20.0F)
      );
      context.getMatrices().push();
      boolean scrolling = ModuleManager.clickGui.scrollMode.getValue() != ClickGui.scrollModeEn.Old
         && !(this.getButtonsHeight() < (double)ModuleManager.clickGui.catHeight.getValue().intValue());
      float targetH = scrolling ? (float)ModuleManager.clickGui.catHeight.getValue().intValue() : (float)this.getButtonsHeight();
      this.catHeight = AnimationUtility.fast(this.catHeight, targetH, 30.0F);
      Color accent1 = HudEditor.getColor(270);
      Color accent2 = HudEditor.getColor(0);

      if (this.isOpen()) {
         Render2DEngine.drawRound(
            context.getMatrices(),
            this.getX(),
            this.getY(),
            this.width,
            headerH + this.catHeight,
            3.0F,
            ModuleManager.clickGui.categoryColor.getValue().getColorObject()
         );
      }

      Render2DEngine.drawRound(context.getMatrices(), this.getX(), this.getY(), this.width, headerH, 3.0F, new Color(22, 22, 28, 255));
      Render2DEngine.draw2DGradientRect(
         context.getMatrices(),
         this.getX(),
         this.getY() + headerH - 1.5F,
         this.getX() + this.width,
         this.getY() + headerH,
         accent1,
         accent2,
         accent1,
         accent2
      );

      boolean popStack = false;
      if (this.isOpen()) {
         if (scrolling) {
            Render2DEngine.addWindow(
               context.getMatrices(),
               this.getX(),
               this.getY() + headerH,
               this.getX() + this.width,
               this.getY() + headerH + (float)ModuleManager.clickGui.catHeight.getValue().intValue(),
               1.0
            );
            popStack = true;
            double maxScroll = this.getButtonsHeight() - (double)ModuleManager.clickGui.catHeight.getValue().intValue();
            if (maxScroll > 0.0) {
               if ((double)(-this.moduleOffset) > maxScroll) {
                  this.moduleOffset = -((float)maxScroll);
               } else if (this.moduleOffset > 0.0F) {
                  this.moduleOffset = 0.0F;
               }
            } else {
               this.moduleOffset = 0.0F;
            }
         } else {
            this.moduleOffset = 0.0F;
         }

         for (AbstractButton button : this.buttons) {
            if (!(button instanceof ModuleButton mb) || !SearchBar.listening || mb.module.getName().toLowerCase().contains(SearchBar.moduleName.toLowerCase())) {
               button.setY(this.getY() + headerH + this.moduleOffset);
               button.setX(this.getX());
               button.setWidth(this.width);
               button.setHeight((float)ModuleManager.clickGui.moduleHeight.getValue().intValue());
               button.render(context, mouseX, mouseY, delta);
            }
         }

         if (popStack) {
            Render2DEngine.popWindow();
         }
      }

      FontRenderers.sf_bold
         .drawString(
            context.getMatrices(), this.getName(), (double)(this.getX() + 20.0F), (double)(this.getY() + headerH / 2.0F - 3.0F), HudEditor.textColor.getValue().getColor()
         );
      String count = String.valueOf(this.buttons.size());
      FontRenderers.sf_medium_mini
         .drawString(
            context.getMatrices(),
            count,
            (double)(this.getX() + this.width - 6.0F - FontRenderers.sf_medium_mini.getStringWidth(count)),
            (double)(this.getY() + headerH / 2.0F - 2.5F),
            new Color(255, 255, 255, 110).getRGB()
         );
      context.getMatrices().push();
      RenderSystem.setShaderTexture(0, this.ICON);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      Color c = HudEditor.textColor.getValue().getColorObject();
      RenderSystem.setShaderColor((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, (float)c.getAlpha() / 255.0F);
      context.drawTexture(this.ICON, (int)this.getX() + 4, (int)this.getY() + (int)(headerH / 2.0F) - 6, 0.0F, 0.0F, 12, 12, 12, 12);
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      context.getMatrices().pop();
      context.getMatrices().pop();
      this.updatePosition();
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (button == 1 && this.hovered) {
         this.setOpen(!this.isOpen());
      }

      super.mouseClicked(mouseX, mouseY, button);
      if (this.isOpen() && this.scrollHover) {
         this.buttons.forEach(b -> b.mouseClicked(mouseX, mouseY, button));
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int button) {
      super.mouseReleased(mouseX, mouseY, button);
      if (this.isOpen()) {
         this.buttons.forEach(b -> b.mouseReleased(mouseX, mouseY, button));
      }
   }

   @Override
   public boolean keyTyped(int keyCode) {
      if (this.isOpen()) {
         for (AbstractButton button : this.buttons) {
            button.keyTyped(keyCode);
         }
      }

      return false;
   }

   @Override
   public void charTyped(char key, int keyCode) {
      if (this.isOpen()) {
         for (AbstractButton button : this.buttons) {
            button.charTyped(key, keyCode);
         }
      }
   }

   @Override
   public void onClose() {
      super.onClose();
      this.buttons.forEach(AbstractButton::onGuiClosed);
   }

   @Override
   public void tick() {
      this.buttons.forEach(AbstractButton::tick);
   }

   private void updatePosition() {
      float offsetY = 0.0F;
      float openY = 0.0F;

      for (AbstractButton button : this.buttons) {
         if (!(button instanceof ModuleButton mb) || !SearchBar.listening || mb.module.getName().toLowerCase().contains(SearchBar.moduleName.toLowerCase())) {
            button.setTargetOffset(offsetY);
            if (button instanceof ModuleButton mbutton && mbutton.isOpen()) {
               for (AbstractElement element : mbutton.getElements()) {
                  if (element.isVisible()) {
                     offsetY += element.getHeight();
                  }
               }

               offsetY += 2.0F;
            }

            offsetY += button.getHeight() + openY;
         }
      }
   }

   @Override
   public void hudClicked(Module module) {
      for (AbstractButton button : this.buttons) {
         if (button instanceof ModuleButton) {
            ModuleButton mbutton = (ModuleButton)button;
            if (mbutton.module == module) {
               mbutton.setOpen(true);
            }
         }
      }
   }

   public double getButtonsHeight() {
      double height = 0.0;

      for (AbstractButton button : this.buttons) {
         if (button instanceof ModuleButton mb && SearchBar.listening && !mb.module.getName().toLowerCase().contains(SearchBar.moduleName.toLowerCase())) {
            continue;
         }

         if (button instanceof ModuleButton mbutton) {
            if (mbutton.isOpen()) {
               height += 2.0;
            }

            height += mbutton.getElementsHeight();
         }

         height += (double)button.getHeight();
      }

      return height;
   }
}
