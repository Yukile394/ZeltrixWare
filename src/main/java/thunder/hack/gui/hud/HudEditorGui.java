package thunder.hack.gui.hud;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.Screen;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.gui.clickui.AbstractCategory;
import thunder.hack.gui.clickui.Category;
import thunder.hack.gui.clickui.ClickGUI;

public class HudEditorGui extends Screen {
   public static HudElement currentlyDragging;
   private final List<AbstractCategory> windows = Lists.newArrayList();
   private static HudEditorGui instance = new HudEditorGui();
   private boolean firstOpen = true;
   private double dWheel;

   public HudEditorGui() {
      super(Text.of("HudEditorGui"));
      this.setInstance();
   }

   protected void init() {
      if (this.firstOpen) {
         Category window = new Category(
            Module.Category.HUD,
            Managers.MODULE.getModulesByCategory(Module.Category.HUD),
            (float)Module.mc.getWindow().getScaledWidth() / 2.0F - 50.0F,
            20.0F,
            100.0F,
            18.0F
         );
         window.setOpen(true);
         this.windows.add(window);
         this.firstOpen = false;
      }

      this.windows.forEach(AbstractCategory::init);
   }

   public boolean shouldPause() {
      return false;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      ClickGUI.anyHovered = false;
      if (ModuleManager.clickGui.scrollMode.getValue() == ClickGui.scrollModeEn.Old) {
         for (AbstractCategory window : this.windows) {
            if (InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 264)) {
               window.setY(window.getY() + 2.0F);
            }

            if (InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 265)) {
               window.setY(window.getY() - 2.0F);
            }

            if (InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 262)) {
               window.setX(window.getX() + 2.0F);
            }

            if (InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 263)) {
               window.setX(window.getX() - 2.0F);
            }

            if (this.dWheel != 0.0) {
               window.setY((float)((double)window.getY() + this.dWheel));
            }
         }
      } else {
         for (AbstractCategory window : this.windows) {
            if (this.dWheel != 0.0) {
               window.setModuleOffset((float)this.dWheel, (float)mouseX, (float)mouseY);
            }
         }
      }

      this.dWheel = 0.0;

      for (AbstractCategory windowx : this.windows) {
         windowx.render(context, mouseX, mouseY, delta);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.dWheel = (double)((int)(verticalAmount * 5.0));
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.windows.forEach(w -> {
         w.mouseClicked((int)mouseX, (int)mouseY, button);
         this.windows.forEach(w1 -> {
            if (w.dragging && w != w1) {
               w1.dragging = false;
            }
         });
      });
      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.windows.forEach(w -> w.mouseReleased((int)mouseX, (int)mouseY, button));
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.windows.forEach(w -> w.keyTyped(keyCode));
      if (keyCode == 256) {
         super.keyPressed(keyCode, scanCode, modifiers);
         return true;
      } else {
         return false;
      }
   }

   public void removed() {
      ThunderHack.EVENT_BUS.unsubscribe(this);
   }

   public void hudClicked(Module module) {
      for (AbstractCategory window : this.windows) {
         window.hudClicked(module);
      }
   }

   public static HudEditorGui getInstance() {
      if (instance == null) {
         instance = new HudEditorGui();
      }

      return instance;
   }

   public static HudEditorGui getHudGui() {
      return getInstance();
   }

   private void setInstance() {
      instance = this;
   }
}
