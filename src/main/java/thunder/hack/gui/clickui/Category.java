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
      this.scrollHover = Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)this.getX(), (double)(this.getY() + this.height), (double)this.width, (double)(this.catHeight + 20.0F)
      );
      context.getMatrices().push();
      boolean popStack = false;
      float height1;
      if (ModuleManager.clickGui.scrollMode.getValue() != ClickGui.scrollModeEn.Old
         && !(this.getButtonsHeight() < (double)ModuleManager.clickGui.catHeight.getValue().intValue())) {
         height1 = (float)ModuleManager.clickGui.catHeight.getValue().intValue();
      } else {
         height1 = (float)this.getButtonsHeight();
      }

      this.catHeight = AnimationUtility.fast(this.catHeight, height1, 30.0F);
      Color m1 = HudEditor.getColor(270);
      Color m2 = HudEditor.getColor(0);
      Color m3 = HudEditor.getColor(180);
      Color m4 = HudEditor.getColor(90);
      if (this.isOpen()) {
         if (ModuleManager.clickGui.scrollMode.getValue() != ClickGui.scrollModeEn.Old
            && !(this.getButtonsHeight() < (double)ModuleManager.clickGui.catHeight.getValue().intValue())) {
            Render2DEngine.addWindow(
               context.getMatrices(),
               this.getX() + 3.0F,
               this.getY() + this.height - 6.0F,
               this.getX() + 3.0F + this.width - 6.0F,
               this.getY() + this.height - 6.0F + (float)ModuleManager.clickGui.catHeight.getValue().intValue(),
               1.0
            );
            popStack = true;
         }

         if (popStack) {
            double maxScroll = this.getButtonsHeight() - (double)ModuleManager.clickGui.catHeight.getValue().intValue() + 20.0;
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
               button.setY(this.getY() + this.height + this.moduleOffset);
               button.setX(this.getX() + 2.0F);
               button.setWidth(this.width - 4.0F);
               button.setHeight((float)ModuleManager.clickGui.moduleHeight.getValue().intValue());
               button.render(context, mouseX, mouseY, delta);
            }
         }
      }

      if (popStack) {
         Render2DEngine.popWindow();
      }

      Render2DEngine.drawRound(context.getMatrices(), this.getX(), this.getY(), this.width, 20.0F, 4.0F, HudEditor.getColor(270));
      if (this.isOpen()) {
         Render2DEngine.drawRound(
            context.getMatrices(),
            this.getX(),
            this.getY() + 20.0F,
            this.width,
            this.catHeight,
            4.0F,
            ModuleManager.clickGui.categoryColor.getValue().getColorObject()
         );
      }

      FontRenderers.sf_bold
         .drawString(
            context.getMatrices(), this.getName(), (double)(this.getX() + 20.0F), (double)(this.getY() + 6.5F), HudEditor.textColor.getValue().getColor()
         );
      context.getMatrices().push();
      RenderSystem.setShaderTexture(0, this.ICON);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      Color c = HudEditor.textColor.getValue().getColorObject();
      RenderSystem.setShaderColor((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, (float)c.getAlpha() / 255.0F);
      context.drawTexture(this.ICON, (int)this.getX() + 3, (int)this.getY() + 3, 0.0F, 0.0F, 14, 14, 14, 14);
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
      double height = 8.0;

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
