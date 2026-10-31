package thunder.hack.gui.clickui.impl;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.AbstractElement;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.AnimationUtility;

public class BooleanParentElement extends AbstractElement {
   private final Setting<BooleanSettingGroup> parentSetting;
   float animation;
   float arrowAnimation;

   public BooleanParentElement(Setting setting) {
      super(setting);
      this.parentSetting = setting;
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      MatrixStack matrixStack = context.getMatrices();
      float tx = this.x + this.width - 11.0F;
      float ty = this.y + 7.5F;
      this.arrowAnimation = AnimationUtility.fast(this.arrowAnimation, this.getParentSetting().getValue().isExtended() ? 0.0F : 1.0F, 15.0F);
      matrixStack.push();
      matrixStack.translate(tx, ty, 0.0F);
      matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-180.0F * this.arrowAnimation));
      matrixStack.translate(-tx, -ty, 0.0F);
      matrixStack.translate(this.x + this.width - 14.0F, this.y + 4.5F, 0.0F);
      context.drawTexture(TextureStorage.guiArrow, 0, 0, 0.0F, 0.0F, 6, 6, 6, 6);
      matrixStack.translate(-(this.x + this.width - 14.0F), -(this.y + 4.5F), 0.0F);
      matrixStack.pop();
      FontRenderers.sf_medium_mini
         .drawString(matrixStack, this.setting.getName(), (double)(this.x + 6.0F), (double)(this.y + this.height / 2.0F - 1.0F), new Color(-1).getRGB());
      this.animation = AnimationUtility.fast(this.animation, this.getParentSetting().getValue().isEnabled() ? 1.0F : 0.0F, 15.0F);
      float paddingX = 7.0F * this.animation;
      Color color = HudEditor.getColor(0);
      Render2DEngine.drawRound(
         context.getMatrices(),
         this.x + this.width - 36.0F,
         this.y + this.height / 2.0F - 4.0F,
         15.0F,
         8.0F,
         1.0F,
         paddingX > 4.0F ? color : new Color(687865855, true)
      );
      Render2DEngine.drawRound(
         context.getMatrices(), this.x + this.width - 35.0F + paddingX, this.y + this.height / 2.0F - 3.0F, 6.0F, 6.0F, 1.0F, new Color(-1)
      );
      if (7.0F * this.animation > 4.0F) {
         FontRenderers.sf_bold_mini
            .drawString(
               context.getMatrices(), "v", (double)(this.x + this.width - 34.0F), (double)(this.y + this.height / 2.0F - 2.0F), new Color(-1).getRGB()
            );
      } else {
         FontRenderers.sf_bold_mini
            .drawString(
               context.getMatrices(), "x", (double)(this.x + this.width - 27.0F), (double)(this.y + this.height / 2.0F - 2.0F), new Color(-1).getRGB()
            );
      }

      if (Render2DEngine.isHovered(
         (double)mouseX, (double)mouseY, (double)(this.x + this.width - 36.0F), (double)(this.y + this.height / 2.0F - 4.0F), 15.0, 8.0
      )) {
         if (GLFW.glfwGetPlatform() != 393219) {
            GLFW.glfwSetCursor(IManager.mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(221188));
         }

         ClickGUI.anyHovered = true;
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (this.hovered) {
         if (button == 0) {
            this.getParentSetting().getValue().setEnabled(!this.getParentSetting().getValue().isEnabled());
            Managers.SOUND.playBoolean();
         } else {
            this.getParentSetting().getValue().setExtended(!this.getParentSetting().getValue().isExtended());
            if (this.getParentSetting().getValue().isExtended()) {
               Managers.SOUND.playSwipeIn();
            } else {
               Managers.SOUND.playSwipeOut();
            }
         }
      }

      super.mouseClicked(mouseX, mouseY, button);
   }

   public Setting<BooleanSettingGroup> getParentSetting() {
      return this.parentSetting;
   }
}
