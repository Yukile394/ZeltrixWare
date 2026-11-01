package thunder.hack.gui.clickui;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.hud.impl.TargetHud;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.clickui.impl.BindElement;
import thunder.hack.gui.clickui.impl.BooleanElement;
import thunder.hack.gui.clickui.impl.BooleanParentElement;
import thunder.hack.gui.clickui.impl.ColorPickerElement;
import thunder.hack.gui.clickui.impl.ItemSelectElement;
import thunder.hack.gui.clickui.impl.ModeElement;
import thunder.hack.gui.clickui.impl.ParentElement;
import thunder.hack.gui.clickui.impl.SliderElement;
import thunder.hack.gui.clickui.impl.StringElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.misc.DialogScreen;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.Bind;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.ItemSelectSetting;
import thunder.hack.setting.impl.PositionSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.AnimationUtility;
import thunder.hack.utility.render.animation.GearAnimation;

public class ModuleButton extends AbstractButton {
   private final List<AbstractElement> elements;
   public final Module module;
   private boolean open;
   private boolean hovered;
   private boolean prevHovered;
   private float animation;
   private float animation2;
   float category_animation = 0.0F;
   int ticksOpened;
   private final GearAnimation gearAnimation = new GearAnimation();
   private boolean binding = false;
   private boolean holdbind = false;

   public ModuleButton(Module module) {
      this.module = module;
      this.elements = new ArrayList<>();

      for (Setting setting : module.getSettings()) {
         if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled") && !setting.getName().equals("Drawn")) {
            this.elements.add(new BooleanElement(setting));
         } else if (setting.getValue() instanceof ColorSetting) {
            this.elements.add(new ColorPickerElement(setting));
         } else if (setting.getValue() instanceof BooleanSettingGroup) {
            this.elements.add(new BooleanParentElement(setting));
         } else if (setting.isNumberSetting() && setting.hasRestriction()) {
            this.elements.add(new SliderElement(setting));
         } else if (setting.getValue() instanceof ItemSelectSetting) {
            this.elements.add(new ItemSelectElement(setting));
         } else if (setting.getValue() instanceof SettingGroup) {
            this.elements.add(new ParentElement(setting));
         } else if (setting.isEnumSetting() && !(setting.getValue() instanceof PositionSetting)) {
            this.elements.add(new ModeElement(setting));
         } else if (setting.getValue() instanceof Bind && !setting.getName().equals("Keybind")) {
            this.elements.add(new BindElement(setting));
         } else if ((setting.getValue() instanceof String || setting.getValue() instanceof Character) && !setting.getName().equalsIgnoreCase("displayName")) {
            this.elements.add(new StringElement(setting));
         }
      }
   }

   @Override
   public void init() {
      this.elements.forEach(AbstractElement::init);
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.hovered = Render2DEngine.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y, (double)this.width, (double)this.height);
      this.animation = AnimationUtility.fast(this.animation, this.module.isEnabled() ? 1.0F : 0.0F, 8.0F);
      this.animation2 = AnimationUtility.fast(this.animation2, 1.0F, 10.0F);
      if (this.hovered) {
         if (!this.prevHovered) {
            Managers.SOUND.playScroll();
         }

         ClickGUI.currentDescription = I18n.translate(this.module.getDescription(), new Object[0]);
      }

      this.prevHovered = this.hovered;
      float ix = this.x + 4.0F;
      float iy = this.y + this.height / 2.0F - 3.0F;
      this.offsetY = AnimationUtility.fast(this.offsetY, this.target_offset, 20.0F);
      float offsetY = 0.0F;
      if (this.isOpen()) {
         Render2DEngine.drawRect(context.getMatrices(), this.x, this.y + this.height, this.width, (float)this.getElementsHeight() + 2.0F, new Color(13, 13, 17, 255));
         Render2DEngine.addWindow(
            context.getMatrices(),
            new Render2DEngine.Rectangle(
               this.x + 1.0F,
               this.y + this.height - 2.0F,
               this.width + this.x - 2.0F,
               (float)((double)(this.height + this.y + 1.0F) + this.getElementsHeight())
            )
         );
         if (Module.mc.player != null && ClickGui.gear.getValue().isEnabled()) {
            Render2DEngine.addWindow(
               context.getMatrices(),
               new Render2DEngine.Rectangle(
                  this.x, this.y + this.height + 1.0F, this.width + this.x + 6.0F, (float)((double)(this.height + this.y + 1.0F) + this.getElementsHeight())
               )
            );
            float px = this.x + 4.0F + (this.width - 8.0F) / 2.0F;
            float py = this.y + 12.0F + (this.height + (float)this.getElementsHeight()) / 2.0F;
            int gScale = ModuleManager.clickGui.gearScale.getValue();
            context.getMatrices().push();
            context.getMatrices().translate(px, py, 0.0F);
            context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.gearAnimation.getValue()));
            context.getMatrices().translate(-px, -py, 0.0F);
            RenderSystem.setShaderTexture(0, TextureStorage.Gear);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
            Render2DEngine.renderGradientTexture(
               context.getMatrices(),
               (double)(px - (float)gScale / 2.0F),
               (double)(py - (float)gScale / 2.0F),
               (double)gScale,
               (double)gScale,
               0.0F,
               0.0F,
               (double)gScale,
               (double)gScale,
               (double)gScale,
               (double)gScale,
               Render2DEngine.injectAlpha(HudEditor.getColor(270).darker(), 110),
               Render2DEngine.injectAlpha(HudEditor.getColor(0).darker(), 110),
               Render2DEngine.injectAlpha(HudEditor.getColor(180).darker(), 110),
               Render2DEngine.injectAlpha(HudEditor.getColor(90).darker(), 110)
            );
            RenderSystem.disableBlend();
            context.getMatrices().translate(px, py, 0.0F);
            context.getMatrices()
               .multiply(
                  RotationAxis.POSITIVE_Z
                     .rotationDegrees(
                        (float)Render2DEngine.interpolate(
                              (double)(Module.mc.player.age - 1), (double)Module.mc.player.age, (double)Render3DEngine.getTickDelta()
                           )
                           * -4.0F
                     )
               );
            context.getMatrices().translate(-px, -py, 0.0F);
            context.getMatrices().pop();
            Render2DEngine.popWindow();
         }

         for (AbstractElement element : this.elements) {
            if (element.isVisible()) {
               element.setOffsetY(offsetY);
               element.setX(this.x);
               element.setY(this.y + this.height + 2.0F);
               element.setWidth(this.width);
               element.setHeight(13.0F);
               if (element instanceof ColorPickerElement picker) {
                  element.setHeight(picker.getHeight());
               } else if (element instanceof SliderElement) {
                  element.setHeight(18.0F);
               }

               if (element instanceof ModeElement) {
                  ModeElement combobox = (ModeElement)element;
                  combobox.setWHeight(13.0);
                  if (combobox.isOpen()) {
                     element.setHeight((float)(13 + combobox.getSetting().getModes().length * 12));
                  } else {
                     element.setHeight(13.0F);
                  }
               }

               offsetY += element.getHeight();
            }
         }

         context.getMatrices().push();
         TargetHud.sizeAnimation(
            context.getMatrices(),
            (double)(this.x + this.width / 2.0F + 6.0F),
            (double)(this.y + this.height / 2.0F - 12.0F),
            this.ticksOpened < 5 ? (double)Math.clamp(this.category_animation / offsetY, 0.0F, 1.0F) : 1.0
         );
         this.elements.forEach(e -> {
            if (e.isVisible()) {
               e.render(context, mouseX, mouseY, delta);
            }
         });
         context.getMatrices().pop();
         Render2DEngine.popWindow();
      }

      this.category_animation = AnimationUtility.fast(this.category_animation, offsetY, 20.0F);
      Render2DEngine.drawRect(context.getMatrices(), this.x, this.y, this.width, this.height, new Color(20, 20, 26, 255));
      if (this.hovered) {
         Render2DEngine.drawRect(context.getMatrices(), this.x, this.y, this.width, this.height, new Color(255, 255, 255, 14));
      }

      if ((double)this.animation > 0.02) {
         Render2DEngine.draw2DGradientRect(
            context.getMatrices(),
            this.x,
            this.y,
            this.x + this.width,
            this.y + this.height,
            Render2DEngine.applyOpacity(HudEditor.getColor(270), this.animation * 0.85F),
            Render2DEngine.applyOpacity(HudEditor.getColor(270), this.animation * 0.85F),
            Render2DEngine.applyOpacity(HudEditor.getColor(0), this.animation * 0.55F),
            Render2DEngine.applyOpacity(HudEditor.getColor(0), this.animation * 0.55F)
         );
         Render2DEngine.drawRect(context.getMatrices(), this.x, this.y, 2.0F, this.height, Render2DEngine.applyOpacity(Color.WHITE, this.animation));
      }

      if (this.isOpen()) {
         Render2DEngine.drawRect(context.getMatrices(), this.x, this.y + this.height - 0.5F, this.width, 0.5F, new Color(255, 255, 255, 40));
      }

      if (!this.module.getBind().getBind().equalsIgnoreCase("none") && !this.binding) {
         FontRenderers.sf_medium_modules
            .drawString(
               context.getMatrices(),
               this.getSbind(),
               (double)(this.x + this.width - 11.0F - FontRenderers.sf_medium_modules.getStringWidth(this.getSbind())),
               (double)(this.y + 6.0F),
               this.module.isEnabled() ? HudEditor.textColor2.getValue().getColor() : HudEditor.textColor.getValue().getColor()
            );
      }

      if (this.binding) {
         FontRenderers.sf_medium_modules
            .drawString(
               context.getMatrices(),
               this.holdbind
                  ? Formatting.GRAY + "Toggle / " + Formatting.RESET + "Hold"
                  : Formatting.RESET + "Toggle " + Formatting.GRAY + "/ Hold",
               (double)(this.x + this.width - 11.0F - FontRenderers.sf_medium_modules.getStringWidth("Toggle/Hold")),
               (double)(iy + 2.0F),
               Render2DEngine.applyOpacity(Color.WHITE.getRGB(), this.animation2)
            );
      }

      if (this.hovered && InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 340)) {
         FontRenderers.sf_medium_modules
            .drawString(
               context.getMatrices(),
               "Drawn " + (this.module.isDrawn() ? Formatting.GREEN + "TRUE" : Formatting.RED + "FALSE"),
               (double)(ix + 1.0F),
               (double)(iy + 2.0F),
               this.module.isEnabled() ? HudEditor.textColor2.getValue().getColor() : HudEditor.textColor.getValue().getColor()
            );
      } else if (this.binding) {
         FontRenderers.sf_medium_modules
            .drawString(
               context.getMatrices(),
               "PressKey",
               (double)ix,
               (double)(iy + 2.0F),
               this.module.isEnabled()
                  ? Render2DEngine.applyOpacity(HudEditor.textColor2.getValue().getColor(), this.animation2)
                  : Render2DEngine.applyOpacity(HudEditor.textColor.getValue().getColor(), this.animation2)
            );
      } else if (ModuleManager.clickGui.textSide.getValue() == ClickGui.TextSide.Left) {
         FontRenderers.sf_medium_modules
            .drawString(
               context.getMatrices(),
               this.module.getName(),
               (double)(ix + 2.0F),
               (double)(iy + 2.0F),
               this.module.isEnabled() ? HudEditor.textColor2.getValue().getColor() : HudEditor.textColor.getValue().getColor()
            );
      } else {
         FontRenderers.sf_medium_modules
            .drawCenteredString(
               context.getMatrices(),
               this.module.getName(),
               (double)(ix + this.getWidth() / 2.0F - 4.0F),
               (double)(iy + 2.0F),
               this.module.isEnabled() ? HudEditor.textColor2.getValue().getColor() : HudEditor.textColor.getValue().getColor()
            );
      }
   }

   @NotNull
   private String getSbind() {
      String sbind = this.module.getBind().getBind();
      if (sbind.equals("LEFT_CONTROL")) {
         sbind = "LCtrl";
      }

      if (sbind.equals("RIGHT_CONTROL")) {
         sbind = "RCtrl";
      }

      if (sbind.equals("LEFT_SHIFT")) {
         sbind = "LShift";
      }

      if (sbind.equals("RIGHT_SHIFT")) {
         sbind = "RShift";
      }

      if (sbind.equals("LEFT_ALT")) {
         sbind = "LAlt";
      }

      if (sbind.equals("RIGHT_ALT")) {
         sbind = "RAlt";
      }

      return sbind;
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (this.binding) {
         if ((float)mouseX > this.x + 56.0F && (float)mouseX < this.x + 67.0F && (float)mouseY > this.y && (float)mouseY < this.y + this.height) {
            this.holdbind = false;
            this.module.getBind().setHold(false);
            return;
         }

         if ((float)mouseX > this.x + 78.0F && (float)mouseX < this.x + 88.0F && (float)mouseY > this.y && (float)mouseY < this.y + this.height) {
            this.holdbind = true;
            this.module.getBind().setHold(true);
            return;
         }

         this.module.setBind(button, true, this.holdbind);
         this.binding = false;
      }

      if (this.hovered) {
         if (InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 340) && button == 0) {
            this.module.setDrawn(!this.module.isDrawn());
            return;
         }

         if (InputUtil.isKeyPressed(Module.mc.getWindow().getHandle(), 261) && button == 0) {
            DialogScreen dialogScreen = new DialogScreen(
               TextureStorage.questionPic,
               ClientSettings.isRu() ? "\u0421\u0431\u0440\u043e\u0441 \u043c\u043e\u0434\u0443\u043b\u044f" : "Reset module",
               ClientSettings.isRu()
                  ? "\u0422\u044b \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0442\u0435\u043b\u044c\u043d\u043e \u0445\u043e\u0447\u0435\u0448\u044c \u0441\u0431\u0440\u043e\u0441\u0438\u0442\u044c "
                     + this.module.getName()
                     + "?"
                  : "Are you sure you want to reset " + this.module.getName() + "?",
               ClientSettings.isRu() ? "\u0414\u0430" : "Yes",
               ClientSettings.isRu() ? "\u041d\u0435\u0442" : "No",
               () -> {
                  if (this.module.isEnabled()) {
                     this.module.disable("reseting");
                  }

                  for (Setting s : this.module.getSettings()) {
                     if (s.getValue() instanceof ColorSetting cs) {
                        cs.setDefault();
                     } else {
                        s.setValue(s.getDefaultValue());
                     }
                  }

                  Module.mc.setScreen(null);
               },
               () -> Module.mc.setScreen(null)
            );
            Module.mc.setScreen(dialogScreen);
         }

         if (button == 0) {
            if (this.module.isToggleable()) {
               this.module.toggle();
            }
         } else if (button == 1) {
            this.setOpen(!this.isOpen());
            if (this.open) {
               Managers.SOUND.playSwipeIn();
            } else {
               Managers.SOUND.playSwipeOut();
            }

            this.animation = 0.5F;
         } else if (button == 2) {
            this.animation2 = 0.0F;
            this.binding = !this.binding;
         }
      }

      if (this.open) {
         this.elements.forEach(element -> {
            if (element.isVisible()) {
               element.mouseClicked(mouseX, mouseY, button);
            }
         });
      }
   }

   @Override
   public void mouseReleased(int mouseX, int mouseY, int button) {
      if (this.isOpen()) {
         this.elements.forEach(element -> element.mouseReleased(mouseX, mouseY, button));
      }
   }

   @Override
   public void charTyped(char key, int keyCode) {
      if (this.isOpen()) {
         for (AbstractElement element : this.elements) {
            element.charTyped(key, keyCode);
         }
      }
   }

   @Override
   public void keyTyped(int keyCode) {
      if (this.isOpen()) {
         for (AbstractElement element : this.elements) {
            element.keyTyped(keyCode);
         }
      }

      if (this.binding) {
         if (keyCode != 256 && keyCode != 261) {
            this.module.setBind(keyCode, false, this.holdbind);
            Command.sendMessage(
               this.module.getName()
                  + (ClientSettings.isRu() ? " \u0431\u0438\u043d\u0434 \u0438\u0437\u043c\u0435\u043d\u0435\u043d \u043d\u0430 " : " bind changed to ")
                  + this.module.getBind().getBind()
            );
         } else {
            this.module.setBind(-1, false, this.holdbind);
            Command.sendMessage(
               (
                     ClientSettings.isRu()
                        ? "\u0423\u0434\u0430\u043b\u0435\u043d \u0431\u0438\u043d\u0434 \u0441 \u043c\u043e\u0434\u0443\u043b\u044f "
                        : "Removed bind from "
                  )
                  + this.module.getName()
            );
         }

         this.binding = false;
      }
   }

   @Override
   public void onGuiClosed() {
      this.elements.forEach(AbstractElement::onClose);
   }

   public List<AbstractElement> getElements() {
      return this.elements;
   }

   public double getElementsHeight() {
      return (double)this.category_animation;
   }

   public double interp(double d, double d2, float d3) {
      return d2 + (d - d2) * (double)d3;
   }

   public boolean isOpen() {
      return this.open;
   }

   public void setOpen(boolean open) {
      this.open = open;
   }

   @Override
   public void tick() {
      if (this.isOpen()) {
         this.gearAnimation.tick();
         this.ticksOpened++;
      } else {
         this.ticksOpened = 0;
      }
   }
}
