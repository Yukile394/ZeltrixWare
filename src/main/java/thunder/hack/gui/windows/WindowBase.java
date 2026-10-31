package thunder.hack.gui.windows;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.PositionSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class WindowBase {
   public final Setting<PositionSetting> position;
   private float x;
   private float y;
   private float width;
   private float height;
   private float dragX;
   private float dragY;
   private float scrollOffset;
   private float prevScrollOffset;
   private float maxElementsHeight;
   private final String name;
   private boolean dragging;
   private boolean hoveringWindow;
   private boolean scaling;
   private boolean scrolling;
   private boolean visible = true;
   private final Identifier icon;

   protected WindowBase(float x, float y, float width, float height, String name, Setting<PositionSetting> pos, Identifier icon) {
      this.setX(x);
      this.setY(y);
      this.setWidth(width);
      this.setHeight(height);
      this.name = name;
      this.position = pos;
      this.icon = icon;
   }

   protected void render(DrawContext context, int mouseX, int mouseY) {
      this.prevScrollOffset = AnimationUtility.fast(this.prevScrollOffset, this.scrollOffset, 12.0F);
      Color color2 = new Color(-983868581, true);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      Render2DEngine.drawHudBase(context.getMatrices(), this.x, this.y, this.width + 10.0F, this.height, 1.0F, false);
      Render2DEngine.drawRect(context.getMatrices(), this.x + 0.5F, this.y, this.width + 9.0F, 16.0F, new Color(1593835520, true));
      Render2DEngine.horizontalGradient(
         context.getMatrices(),
         this.x + 2.0F,
         this.y + 16.0F,
         this.x + 2.0F + this.width / 2.0F - 2.0F,
         this.y + 16.5F,
         Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0),
         HudEditor.textColor.getValue().getColorObject()
      );
      Render2DEngine.horizontalGradient(
         context.getMatrices(),
         this.x + 2.0F + this.width / 2.0F - 2.0F,
         this.y + 16.0F,
         this.x + 2.0F + this.width - 4.0F,
         this.y + 16.5F,
         HudEditor.textColor.getValue().getColorObject(),
         Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0)
      );
      FontRenderers.sf_medium.drawString(context.getMatrices(), this.name, (double)(this.x + 4.0F), (double)(this.y + 5.5F), -1);
      boolean hover1 = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)(this.x + this.width - 4.0F), (double)(this.y + 3.0F), 10.0, 10.0);
      Render2DEngine.drawRectWithOutline(
         context.getMatrices(),
         this.x + this.width - 4.0F,
         this.y + 3.0F,
         10.0F,
         10.0F,
         hover1 ? new Color(-982026377, true) : new Color(-984131753, true),
         color2
      );
      float ratio = (this.getHeight() - 35.0F) / this.maxElementsHeight;
      boolean hover2 = Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)(this.x + this.width), (double)(this.y + 19.0F), 6.0, (double)(this.getHeight() - 34.0F)
      );
      Render2DEngine.drawRectWithOutline(
         context.getMatrices(),
         this.x + this.width,
         this.y + 19.0F,
         6.0F,
         this.getHeight() - 34.0F,
         hover2 ? new Color(1595085587, true) : new Color(1593835520, true),
         color2
      );
      Render2DEngine.drawRect(
         context.getMatrices(),
         this.x + this.width,
         Math.max(this.y + 19.0F - this.scrollOffset * ratio, this.y + 19.0F),
         6.0F,
         Math.min((this.getHeight() - 34.0F) * ratio, this.getHeight() - 34.0F),
         new Color(-1590611663, true)
      );
      Render2DEngine.drawLine(this.x + this.width - 2.0F, this.y + 5.0F, this.x + this.width + 4.0F, this.y + 11.0F, -1);
      Render2DEngine.drawLine(this.x + this.width - 2.0F, this.y + 11.0F, this.x + this.width + 4.0F, this.y + 5.0F, -1);
      RenderSystem.disableBlend();
      if (this.dragging) {
         this.setX(Render2DEngine.scrollAnimate((float)this.normaliseX() - this.dragX, this.getX(), 0.15F));
         this.setY(Render2DEngine.scrollAnimate((float)this.normaliseY() - this.dragY, this.getY(), 0.15F));
         if (this.position != null) {
            this.position.getValue().setX(this.getX() / (float)IManager.mc.getWindow().getScaledWidth());
            this.position.getValue().setY(this.getY() / (float)IManager.mc.getWindow().getScaledHeight());
         }
      }

      if (this.scaling) {
         this.setWidth(Math.max(Render2DEngine.scrollAnimate((float)this.normaliseX() - this.dragX, this.getWidth(), 0.15F), (float)this.getMinWidth()));
         this.setHeight(Math.max(Render2DEngine.scrollAnimate((float)this.normaliseY() - this.dragY, this.getHeight(), 0.15F), (float)this.getMinHeight()));
      }

      if (this.scrolling) {
         float diff = ((float)mouseY - this.y - 19.0F) / (this.getHeight() - 34.0F);
         this.scrollOffset = -(diff * this.maxElementsHeight);
         this.scrollOffset = MathUtility.clamp(this.scrollOffset, -this.maxElementsHeight + (this.getHeight() - 40.0F), 0.0F);
      }

      this.hoveringWindow = Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)this.getX(), (double)this.getY(), (double)this.getWidth(), (double)this.getHeight()
      );
      Render2DEngine.drawLine(
         this.getX() + this.getWidth(),
         this.getY() + this.getHeight() - 3.0F,
         this.getX() + this.getWidth() + 7.0F,
         this.getY() + this.getHeight() - 10.0F,
         color2.getRGB()
      );
      Render2DEngine.drawLine(
         this.getX() + this.getWidth() + 5.0F,
         this.getY() + this.getHeight() - 3.0F,
         this.getX() + this.getWidth() + 7.0F,
         this.getY() + this.getHeight() - 5.0F,
         color2.getRGB()
      );
   }

   protected void mouseClicked(double mouseX, double mouseY, int button) {
      if (Render2DEngine.isHovered(mouseX, mouseY, (double)(this.x + this.width - 4.0F), (double)(this.y + 3.0F), 10.0, 10.0)) {
         this.setVisible(false);
      } else if (Render2DEngine.isHovered(mouseX, mouseY, (double)this.x, (double)this.y, (double)this.width, 10.0)) {
         if (WindowsScreen.draggingWindow == null) {
            this.dragging = true;
         }

         if (WindowsScreen.draggingWindow == null) {
            WindowsScreen.draggingWindow = this;
         }

         WindowsScreen.lastClickedWindow = this;
         this.dragX = (float)((int)(mouseX - (double)this.getX()));
         this.dragY = (float)((int)(mouseY - (double)this.getY()));
      } else if (Render2DEngine.isHovered(mouseX, mouseY, (double)(this.x + this.width), (double)(this.y + this.height - 10.0F), 10.0, 10.0)) {
         WindowsScreen.lastClickedWindow = this;
         this.dragX = (float)((int)(mouseX - (double)this.getWidth()));
         this.dragY = (float)((int)(mouseY - (double)this.getHeight()));
         this.scaling = true;
      } else {
         if (Render2DEngine.isHovered(mouseX, mouseY, (double)(this.x + this.width), (double)(this.y + 19.0F), 6.0, (double)(this.getHeight() - 34.0F))) {
            WindowsScreen.lastClickedWindow = this;
            this.dragX = (float)((int)(mouseX - (double)this.getWidth()));
            this.dragY = (float)((int)(mouseY - (double)this.getHeight()));
            this.scrolling = true;
         }
      }
   }

   protected void keyPressed(int keyCode, int scanCode, int modifiers) {
   }

   protected void charTyped(char key, int keyCode) {
   }

   protected void mouseScrolled(int i) {
      if (this.hoveringWindow) {
         this.scrollOffset += (float)(i * 2);
         this.scrollOffset = MathUtility.clamp(this.scrollOffset, -this.maxElementsHeight + (this.getHeight() - 40.0F), 0.0F);
      }
   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
      this.dragging = false;
      this.scaling = false;
      this.scrolling = false;
      WindowsScreen.draggingWindow = null;
   }

   protected float getX() {
      return this.x;
   }

   protected void setX(float x) {
      this.x = x;
   }

   protected float getY() {
      return this.y;
   }

   protected void setY(float y) {
      this.y = y;
   }

   protected float getWidth() {
      return this.width;
   }

   protected void setWidth(float width) {
      this.width = width;
   }

   protected float getHeight() {
      return this.height;
   }

   protected void setHeight(float height) {
      this.height = height;
   }

   protected int normaliseX() {
      return (int)(IManager.mc.mouse.getX() / Render3DEngine.getScaleFactor());
   }

   protected int normaliseY() {
      return (int)(IManager.mc.mouse.getY() / Render3DEngine.getScaleFactor());
   }

   protected float getScrollOffset() {
      return this.prevScrollOffset;
   }

   protected void resetScroll() {
      this.prevScrollOffset = 0.0F;
      this.scrollOffset = 0.0F;
   }

   protected int getMinWidth() {
      return 150;
   }

   protected int getMinHeight() {
      return 150;
   }

   protected void setMaxElementsHeight(float maxElementsHeight) {
      this.maxElementsHeight = maxElementsHeight;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public Identifier getIcon() {
      return this.icon;
   }
}
