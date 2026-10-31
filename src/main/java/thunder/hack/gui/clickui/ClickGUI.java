package thunder.hack.gui.clickui;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ConfigManager;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;
import thunder.hack.utility.render.animation.EaseOutBack;

public class ClickGUI extends Screen {
   public static List<AbstractCategory> windows;
   public static boolean anyHovered;
   private boolean firstOpen;
   private float scrollY;
   private float closeAnimation;
   private float prevYaw;
   private float prevPitch;
   private float closeDirectionX;
   private float closeDirectionY;
   public static boolean close = false;
   public static boolean imageDirection;
   public static String currentDescription = "";
   public EaseOutBack imageAnimation = new EaseOutBack(6);
   public static Module.Category selectedCategory = Module.Category.COMBAT;
   private static ClickGUI INSTANCE = new ClickGUI();

   public ClickGUI() {
      super(Text.of("NewClickGUI"));
      windows = Lists.newArrayList();
      this.firstOpen = true;
      this.setInstance();
   }

   public static ClickGUI getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ClickGUI();
      }

      imageDirection = true;
      return INSTANCE;
   }

   public static ClickGUI getClickGui() {
      windows.forEach(AbstractCategory::init);
      return getInstance();
   }

   private void setInstance() {
      INSTANCE = this;
   }

   protected void init() {
      if (this.firstOpen) {
         float xOffset = 20.0F;

         for (Module.Category category : Managers.MODULE.getCategories()) {
            if (category != Module.Category.HUD) {
               Category window = new Category(category, Managers.MODULE.getModulesByCategory(category), xOffset, 20.0F, 110.0F, 20.0F);
               window.setOpen(true);
               windows.add(window);
               xOffset += 115.0F;
            }
         }

         ConfigCategory configWindow = new ConfigCategory(xOffset, 20.0F, 110.0F, 20.0F);
         configWindow.setOpen(true);
         windows.add(configWindow);
         this.firstOpen = false;
      }

      windows.forEach(AbstractCategory::init);
   }

   public boolean shouldPause() {
      return false;
   }

   public void tick() {
      windows.forEach(AbstractCategory::tick);
      this.imageAnimation.update(imageDirection);
      if (close) {
         if (Module.mc.player != null) {
            if (Module.mc.player.getPitch() > this.prevPitch) {
               this.closeDirectionY = (this.prevPitch - Module.mc.player.getPitch()) * 300.0F;
            }

            if (Module.mc.player.getPitch() < this.prevPitch) {
               this.closeDirectionY = (this.prevPitch - Module.mc.player.getPitch()) * 300.0F;
            }

            if (Module.mc.player.getYaw() > this.prevYaw) {
               this.closeDirectionX = (this.prevYaw - Module.mc.player.getYaw()) * 300.0F;
            }

            if (Module.mc.player.getYaw() < this.prevYaw) {
               this.closeDirectionX = (this.prevYaw - Module.mc.player.getYaw()) * 300.0F;
            }
         }

         if (this.closeDirectionX < 1.0F && this.closeDirectionY < 1.0F && this.closeAnimation > 2.0F) {
            this.closeDirectionY = -3000.0F;
         }

         this.closeAnimation++;
         if (this.closeAnimation > 6.0F) {
            close = false;
            windows.forEach(AbstractCategory::restorePos);
            this.close();
         }
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      if (ModuleManager.clickGui.blur.getValue()) {
         this.applyBlur(delta);
      }

      anyHovered = false;
      if (Module.fullNullCheck()) {
         this.renderBackground(context, mouseX, mouseY, delta);
      }

      context.getMatrices().push();
      if (this.closeAnimation <= 6.0F) {
         context.getMatrices().translate(this.closeDirectionX * AnimationUtility.deltaTime(), this.closeDirectionY * AnimationUtility.deltaTime(), 0.0F);
      }

      for (AbstractCategory window : windows) {
         if (this.scrollY != 0.0F) {
            window.setModuleOffset(this.scrollY, (float)mouseX, (float)mouseY);
         }

         window.render(context, mouseX, mouseY, delta);
      }

      this.scrollY = 0.0F;
      context.getMatrices().pop();
      FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), "Zeltrix", 40.0, 10.0, ModuleManager.clickGui.categoryColor.getValue().getColor());
      int sw = Module.mc.getWindow().getScaledWidth();
      int sh = Module.mc.getWindow().getScaledHeight();
      Color[] colors = new Color[]{
         new Color(25, 20, 30, 255), new Color(135, 80, 255, 255), new Color(255, 80, 80, 255), new Color(80, 255, 80, 255), new Color(80, 80, 255, 255)
      };
      float cx = (float)sw / 2.0F - (float)colors.length * 20.0F / 2.0F;
      float cy = (float)sh - 30.0F;
      FontRenderers.sf_medium.drawString(context.getMatrices(), "GUI Color", (double)(cx - 50.0F), (double)(cy + 4.0F), -1);

      for (int i = 0; i < colors.length; i++) {
         Render2DEngine.drawRound(context.getMatrices(), cx + (float)(i * 20), cy, 15.0F, 15.0F, 7.5F, colors[i]);
         if (Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)(cx + (float)(i * 20)), (double)cy, 15.0, 15.0)) {
            Render2DEngine.drawRound(context.getMatrices(), cx + (float)(i * 20), cy, 15.0F, 15.0F, 7.5F, new Color(255, 255, 255, 100));
         }
      }

      if (!Objects.equals(currentDescription, "") && ModuleManager.clickGui.descriptions.getValue()) {
         Render2DEngine.drawHudBase(
            context.getMatrices(),
            (float)(mouseX + 7),
            (float)(mouseY + 5),
            FontRenderers.sf_medium.getStringWidth(currentDescription) + 6.0F,
            11.0F,
            1.0F,
            false
         );
         FontRenderers.sf_medium
            .drawString(context.getMatrices(), currentDescription, (double)(mouseX + 10), (double)(mouseY + 8), HudEditor.getColor(0).getRGB());
         currentDescription = "";
      }

      if (!HudElement.anyHovered && !anyHovered && GLFW.glfwGetPlatform() != 393219) {
         GLFW.glfwSetCursor(Module.mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(221185));
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.scrollY += (float)((int)(verticalAmount * 15.0));
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      float cx = (float)Module.mc.getWindow().getScaledWidth() / 2.0F - 50.0F;
      float cy = (float)Module.mc.getWindow().getScaledHeight() - 30.0F;
      Color[] colors = new Color[]{
         new Color(25, 20, 30, 255), new Color(135, 80, 255, 255), new Color(255, 80, 80, 255), new Color(80, 255, 80, 255), new Color(80, 80, 255, 255)
      };

      for (int i = 0; i < colors.length; i++) {
         if (Render2DEngine.isHovered(mouseX, mouseY, (double)(cx + (float)(i * 20)), (double)cy, 15.0, 15.0)) {
            Color c = colors[i];
            ModuleManager.clickGui.categoryColor.setValue(new ColorSetting(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100).getRGB()));
            return true;
         }
      }

      for (AbstractCategory window : windows) {
         window.mouseClicked((int)mouseX, (int)mouseY, button);
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      for (AbstractCategory window : windows) {
         window.mouseReleased((int)mouseX, (int)mouseY, button);
      }

      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean charTyped(char key, int modifier) {
      for (AbstractCategory window : windows) {
         window.charTyped(key, modifier);
      }

      return true;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      for (AbstractCategory window : windows) {
         window.keyTyped(keyCode);
      }

      if (keyCode == 256) {
         if (Module.mc.player == null || !ModuleManager.clickGui.closeAnimation.getValue()) {
            imageDirection = false;
            this.imageAnimation.reset();
            super.keyPressed(keyCode, scanCode, modifiers);
            return true;
         } else if (close) {
            return true;
         } else {
            imageDirection = false;
            windows.forEach(AbstractCategory::savePos);
            this.closeDirectionX = 0.0F;
            this.closeDirectionY = 0.0F;
            close = true;
            Module.mc.mouse.lockCursor();
            this.closeAnimation = 0.0F;
            if (Module.mc.player != null) {
               this.prevYaw = Module.mc.player.getYaw();
               this.prevPitch = Module.mc.player.getPitch();
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public void filesDragged(List<Path> paths) {
      for (Path path : paths) {
         if (path.toString().endsWith(".th")) {
            try {
               Files.copy(path, new File(ConfigManager.CONFIGS_FOLDER, path.getFileName().toString()).toPath(), StandardCopyOption.REPLACE_EXISTING);
               Managers.CONFIG.load(path.getFileName().toString().replace(".th", ""));
               Command.sendMessage("Loaded dropped config: " + path.getFileName().toString());

               for (AbstractCategory window : windows) {
                  if (window instanceof ConfigCategory) {
                     window.init();
                  }
               }
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         }
      }
   }
}
