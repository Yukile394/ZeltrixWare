package thunder.hack.gui.thundergui.components;

import java.awt.Color;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class BooleanParentComponent extends SettingElement {
   float animation = 0.0F;
   private final Setting<BooleanSettingGroup> parentSetting;

   public BooleanParentComponent(Setting setting) {
      super(setting);
      this.parentSetting = setting;
   }

   @Override
   public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
      super.render(stack, mouseX, mouseY, partialTicks);
      if (!(this.getY() > (float)(ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height))
         && !(this.getY() < (float)ThunderGui.getInstance().main_posY)) {
         FontRenderers.modules
            .drawString(
               stack,
               this.getSetting().getName(),
               (double)this.getX(),
               (double)(this.getY() + 5.0F),
               this.isHovered() ? -1 : new Color(-1325400065, true).getRGB()
            );
         this.animation = AnimationUtility.fast(this.animation, this.getParentSetting().getValue().isEnabled() ? 1.0F : 0.0F, 15.0F);
         double paddingX = (double)(7.0F * this.animation);
         Color color = HudEditor.getColor(1);
         Render2DEngine.drawRound(
            stack, this.x + this.width - 18.0F, this.y + this.height / 2.0F - 4.0F, 15.0F, 8.0F, 4.0F, paddingX > 4.0 ? color : new Color(-5066319)
         );
         Render2DEngine.drawRound(
            stack, (float)((double)(this.x + this.width - 17.0F) + paddingX), this.y + this.height / 2.0F - 3.0F, 6.0F, 6.0F, 3.0F, new Color(-1)
         );
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (!(this.getY() > (float)(ThunderGui.getInstance().main_posY + ThunderGui.getInstance().height))
         && !(this.getY() < (float)ThunderGui.getInstance().main_posY)) {
         if (this.isHovered()) {
            this.getParentSetting().getValue().setEnabled(!this.getParentSetting().getValue().isEnabled());
         }
      }
   }

   public Setting<BooleanSettingGroup> getParentSetting() {
      return this.parentSetting;
   }
}
