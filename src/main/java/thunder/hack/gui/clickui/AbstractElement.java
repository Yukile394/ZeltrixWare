package thunder.hack.gui.clickui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

public abstract class AbstractElement {
   protected Setting setting;
   protected float x;
   protected float y;
   protected float width;
   protected float height;
   protected float offsetY;
   protected boolean hovered;

   public AbstractElement(Setting setting) {
      this.setting = setting;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.hovered = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y, (double)this.width, (double)this.height);
   }

   public void init() {
   }

   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 261) && button == 2 && this.hovered) {
         if (this.setting.getValue() instanceof ColorSetting cs) {
            cs.setDefault();
         } else {
            this.setting.setValue(this.setting.getDefaultValue());
         }
      }
   }

   public void mouseReleased(int mouseX, int mouseY, int button) {
   }

   public void keyTyped(int keyCode) {
   }

   public void onClose() {
   }

   public Setting getSetting() {
      return this.setting;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getWidth() {
      return this.width;
   }

   public float getHeight() {
      return this.height;
   }

   public void setX(float x) {
      this.x = x;
   }

   public void setY(float y) {
      this.y = y + this.offsetY;
   }

   public void setWidth(float width) {
      this.width = width;
   }

   public void setHeight(float height) {
      this.height = height;
   }

   public void setOffsetY(float offsetY) {
      this.offsetY = offsetY;
   }

   public boolean isVisible() {
      return this.setting.isVisible();
   }

   public void charTyped(char key, int keyCode) {
   }
}
