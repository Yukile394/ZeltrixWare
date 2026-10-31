package thunder.hack.gui.clickui;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class ConfigCategory extends AbstractCategory {
   public float catHeight;
   private boolean scrollHover;
   private List<String> configs;

   public ConfigCategory(float x, float y, float width, float height) {
      super("Configs", x, y, width, height);
   }

   @Override
   public void init() {
      this.configs = Managers.CONFIG.getConfigList();
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.setWidth((float)ModuleManager.clickGui.moduleWidth.getValue().intValue());
      this.scrollHover = Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)this.getX(), (double)(this.getY() + this.height), (double)this.width, (double)(this.catHeight + 20.0F)
      );
      context.getMatrices().push();
      float maxH = (float)ModuleManager.clickGui.catHeight.getValue().intValue();
      float contentHeight = this.configs == null ? 0.0F : (float)this.configs.size() * 14.0F + 8.0F;
      float height1 = ModuleManager.clickGui.scrollMode.getValue() != ClickGui.scrollModeEn.Old && !(contentHeight < maxH) ? maxH : contentHeight;
      this.catHeight = AnimationUtility.fast(this.catHeight, height1, 30.0F);
      boolean popStack = false;
      if (this.isOpen()) {
         if (ModuleManager.clickGui.scrollMode.getValue() != ClickGui.scrollModeEn.Old && !(contentHeight < maxH)) {
            Render2DEngine.addWindow(
               context.getMatrices(),
               this.getX() + 3.0F,
               this.getY() + this.height - 6.0F,
               this.getX() + 3.0F + this.width - 6.0F,
               this.getY() + this.height - 6.0F + maxH,
               1.0
            );
            popStack = true;
         }

         float offsetY = this.getY() + this.height;
         if (popStack && this.moduleOffset < 0.0F) {
            double maxScroll = (double)(contentHeight - maxH);
            if ((double)(-this.moduleOffset) > maxScroll && maxScroll > 0.0) {
               this.moduleOffset = -((float)maxScroll);
            }

            offsetY += this.moduleOffset;
         } else {
            this.moduleOffset = 0.0F;
         }

         if (this.configs != null) {
            for (String cfg : this.configs) {
               boolean hovered = Render2DEngine.isHovered(
                  (double)mouseX, (double)mouseY, (double)(this.getX() + 2.0F), (double)offsetY, (double)(this.width - 4.0F), 14.0
               );
               Render2DEngine.drawRound(
                  context.getMatrices(),
                  this.getX() + 2.0F,
                  offsetY,
                  this.width - 4.0F,
                  14.0F,
                  2.0F,
                  hovered ? new Color(0, 0, 0, 100) : new Color(0, 0, 0, 50)
               );
               FontRenderers.sf_medium.drawString(context.getMatrices(), cfg, (double)(this.getX() + 6.0F), (double)(offsetY + 4.0F), -1);
               offsetY += 14.0F;
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
      context.getMatrices().pop();
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (button == 1 && this.hovered) {
         this.setOpen(!this.isOpen());
      }

      super.mouseClicked(mouseX, mouseY, button);
      if (this.isOpen() && this.scrollHover && this.configs != null) {
         float offsetY = this.getY() + this.height + this.moduleOffset;

         for (String cfg : this.configs) {
            if (Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)(this.getX() + 2.0F), (double)offsetY, (double)(this.width - 4.0F), 14.0)) {
               if (button == 0) {
                  Managers.CONFIG.load(cfg);
               } else if (button == 1) {
                  Managers.CONFIG.save(cfg);
               }
            }

            offsetY += 14.0F;
         }
      }
   }
}
