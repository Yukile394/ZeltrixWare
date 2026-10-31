package thunder.hack.gui.thundergui.components;

import java.io.IOException;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class SettingElement {
   protected Setting setting;
   protected float x;
   protected float y;
   protected float width;
   protected float height;
   protected float offsetY;
   protected float prev_offsetY;
   protected float scroll_offsetY;
   protected float scroll_animation;
   protected boolean hovered;

   public SettingElement(Setting setting) {
      this.setting = setting;
      this.scroll_animation = 1.0F;
      this.prev_offsetY = this.y;
      this.scroll_offsetY = this.y;
   }

   public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
      this.hovered = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y, (double)this.width, (double)this.height);
      if (this.scroll_offsetY != this.y) {
         this.scroll_animation = AnimationUtility.fast(this.scroll_animation, 1.0F, 5.0F);
         this.y = (float)((int)Render2DEngine.interpolate((double)this.prev_offsetY, (double)this.scroll_offsetY, (double)this.scroll_animation));
      }
   }

   public void init() {
   }

   public void onTick() {
   }

   public void mouseClicked(int mouseX, int mouseY, int button) {
   }

   public void tick() {
   }

   public boolean isHovered() {
      return this.hovered;
   }

   public void mouseReleased(int mouseX, int mouseY, int button) {
   }

   public void handleMouseInput() throws IOException {
   }

   public void keyTyped(String chr, int keyCode) {
   }

   public void onClose() {
   }

   public void resetAnimation() {
   }

   public Setting getSetting() {
      return this.setting;
   }

   public float getX() {
      return this.x;
   }

   public void setX(float x) {
      this.x = x;
   }

   public float getY() {
      return this.y;
   }

   public void setY(float y) {
      this.prev_offsetY = this.y;
      this.scroll_offsetY = y + this.offsetY;
   }

   public void setPrev_offsetY(float y) {
      this.prev_offsetY = y;
   }

   public float getWidth() {
      return this.width;
   }

   public void setWidth(float width) {
      this.width = width;
   }

   public float getHeight() {
      return this.height;
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

   public void checkMouseWheel(float dWheel) {
      if (dWheel != 0.0F) {
         this.scroll_animation = 0.0F;
      }
   }
}
