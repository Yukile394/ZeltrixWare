package thunder.hack.features.hud.impl;

import java.awt.Color;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.windows.WindowsScreen;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

public class Hotbar extends HudElement {
   public static final Setting<Hotbar.Mode> lmode = new Setting<>("LeftHandMode", Hotbar.Mode.Merged);

   public Hotbar() {
      super("Hotbar", 0, 0);
   }

   @Override
   public void onRender2D(DrawContext context) {
      if (!(mc.currentScreen instanceof WindowsScreen)) {
         PlayerEntity playerEntity = mc.player;
         if (playerEntity != null) {
            MatrixStack matrices = context.getMatrices();
            int i = mc.getWindow().getScaledWidth() / 2;
            if (mc.player.getOffHandStack().isEmpty()) {
               Render2DEngine.drawHudBase(
                  matrices, (float)(i - 90), (float)(mc.getWindow().getScaledHeight() - 25), 180.0F, 20.0F, HudEditor.hudRound.getValue()
               );
            } else if (lmode.getValue() == Hotbar.Mode.Merged) {
               Render2DEngine.drawHudBase(
                  matrices, (float)(i - 111), (float)(mc.getWindow().getScaledHeight() - 25), 201.0F, 20.0F, HudEditor.hudRound.getValue()
               );
               if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
                  Render2DEngine.drawRect(
                     context.getMatrices(), (float)(i - 109 + 18), (float)(mc.getWindow().getScaledHeight() - 23), 0.5F, 15.0F, new Color(1157627903, true)
                  );
               } else {
                  Render2DEngine.verticalGradient(
                     matrices,
                     (float)(i - 109 + 18),
                     (float)(mc.getWindow().getScaledHeight() - 22 + 1 - 4),
                     (float)(i - 108 + 18) - 0.5F,
                     (float)(mc.getWindow().getScaledHeight() - 11 + 1 - 4),
                     Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0),
                     HudEditor.textColor.getValue().getColorObject()
                  );
                  Render2DEngine.verticalGradient(
                     matrices,
                     (float)(i - 109 + 18),
                     (float)(mc.getWindow().getScaledHeight() - 11 - 4),
                     (float)(i - 108 + 18) - 0.5F,
                     (float)(mc.getWindow().getScaledHeight() - 5),
                     HudEditor.textColor.getValue().getColorObject(),
                     Render2DEngine.injectAlpha(HudEditor.textColor.getValue().getColorObject(), 0)
                  );
               }
            } else {
               Render2DEngine.drawHudBase(
                  matrices, (float)(i - 90), (float)(mc.getWindow().getScaledHeight() - 25), 180.0F, 20.0F, HudEditor.hudRound.getValue()
               );
               Render2DEngine.drawHudBase(
                  matrices, (float)i - 112.5F, (float)(mc.getWindow().getScaledHeight() - 25), 20.0F, 20.0F, HudEditor.hudRound.getValue()
               );
            }

            Color c = HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry) ? new Color(2081756437, true) : new Color(2083467055, true);
            Render2DEngine.drawRect(
               matrices,
               (float)(i - 88) + (float)playerEntity.getInventory().selectedSlot * 19.8F,
               (float)(mc.getWindow().getScaledHeight() - 24),
               17.0F,
               17.0F,
               HudEditor.hudRound.getValue(),
               0.7F,
               c,
               c,
               c,
               c
            );
         }
      }
   }

   public static void renderHotBarItems(float tickDelta, DrawContext context) {
      if (!(mc.currentScreen instanceof WindowsScreen)) {
         PlayerEntity playerEntity = mc.player;
         if (playerEntity != null) {
            MatrixStack matrices = context.getMatrices();
            int i = mc.getWindow().getScaledWidth() / 2;
            int o = mc.getWindow().getScaledHeight() - 16 - 3;
            if (!mc.player.getOffHandStack().isEmpty()) {
               if (lmode.getValue() == Hotbar.Mode.Merged) {
                  renderHotbarItem(context, i - 109, o - 5, playerEntity.getOffHandStack());
               } else {
                  renderHotbarItem(context, i - 111, o - 5, playerEntity.getOffHandStack());
               }
            }

            for (int m = 0; m < 9; m++) {
               int n = i - 90 + m * 20 + 2;
               if (m == mc.player.getInventory().selectedSlot) {
                  renderHotbarItem(context, n, o - 7, (ItemStack)playerEntity.getInventory().main.get(m));
               } else {
                  renderHotbarItem(context, n, o - 5, (ItemStack)playerEntity.getInventory().main.get(m));
               }
            }
         }
      }
   }

   private static void renderHotbarItem(DrawContext context, int i, int j, ItemStack itemStack) {
      if (!itemStack.isEmpty()) {
         context.getMatrices().push();
         context.getMatrices().translate((float)(i + 8), (float)(j + 12), 0.0F);
         context.getMatrices().scale(0.9F, 0.9F, 1.0F);
         context.getMatrices().translate((float)(-(i + 8)), (float)(-(j + 12)), 0.0F);
         context.drawItem(itemStack, i, j);
         context.drawItemInSlot(mc.textRenderer, itemStack, i, j);
         context.getMatrices().pop();
      }
   }

   public static void renderXpBar(int x, MatrixStack matrices) {
      mc.getProfiler().push("expBar");
      mc.getProfiler().pop();
      if (mc.player.experienceLevel > 0) {
         mc.getProfiler().push("expLevel");
         String string = mc.player.experienceLevel + "";
         int k = (int)(((float)mc.getWindow().getScaledWidth() - FontRenderers.sf_bold_mini.getStringWidth(string)) / 2.0F);
         int l = mc.getWindow().getScaledHeight() - 31 - 4;
         FontRenderers.sf_bold_mini.drawString(matrices, string, (double)k, (double)l, 8453920);
         mc.getProfiler().pop();
      }
   }

   public static enum Mode {
      Merged,
      Separately;
   }
}
