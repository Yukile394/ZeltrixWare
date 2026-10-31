package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.List;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.features.modules.misc.NameProtect;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

public class ZeltrixTargetHud extends HudElement {
   public final Setting<ZeltrixTargetHud.ThemeStyle> themeStyle = new Setting<>("Style", ZeltrixTargetHud.ThemeStyle.Neon);
   public final Setting<Float> scale = new Setting<>("Scale", 1.0F, 0.5F, 2.0F);
   private final Setting<Boolean> customColor = new Setting<>("CustomColor", false);
   private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(-16718337), v -> this.customColor.getValue());
   private final Setting<Boolean> absorp = new Setting<>("Absorption", true);
   private final Setting<Boolean> funTimeHP = new Setting<>("FunTimeHP", false);
   private static final int MAX_DIGITS = 3;
   private final float[] digitOffsetY = new float[3];
   private final float[] digitAlpha = new float[3];
   private final long[] digitChangeTime = new long[3];
   private final char[] lastDigits = new char[3];
   private int lastRoundedHp = -1;
   private float alpha = 0.0F;
   private float hpAnimated = 20.0F;
   private float hpProgressAnimated = 1.0F;
   private float absorptionAnimated = 0.0F;
   private LivingEntity target;
   private boolean direction = false;

   public ZeltrixTargetHud() {
      super("ZeltrixTargetHud", 140, 36);
   }

   private void updateTarget() {
      if (Aura.target instanceof LivingEntity living) {
         this.target = living;
         this.direction = true;
      } else if (!(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof HudEditorGui)) {
         this.direction = false;
         if (this.alpha < 0.02F) {
            this.target = null;
         }
      } else {
         this.target = mc.player;
         this.direction = true;
      }
   }

   public float getTargetHealth() {
      if (this.target == null) {
         return 20.0F;
      } else {
         if (this.target instanceof PlayerEntity ent
            && (
               mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null && mc.getNetworkHandler().getServerInfo().address.contains("funtime")
                  || this.funTimeHP.getValue()
            )) {
            String resolvedHp = "";
            if (ent.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
               ScoreboardObjective scoreBoard = ent.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
               if (scoreBoard != null) {
                  ReadableScoreboardScore readableScoreboardScore = ent.getScoreboard().getScore(ent, scoreBoard);
                  MutableText text2 = ReadableScoreboardScore.getFormattedScore(readableScoreboardScore, scoreBoard.getNumberFormatOr(StyledNumberFormat.EMPTY));
                  resolvedHp = text2.getString();
               }
            }

            float numValue = 0.0F;

            try {
               numValue = Float.parseFloat(resolvedHp);
            } catch (NumberFormatException var6) {
            }

            return this.absorp.getValue() ? numValue + this.target.getAbsorptionAmount() : numValue;
         }

         return this.absorp.getValue() ? this.target.getHealth() + this.target.getAbsorptionAmount() : this.target.getHealth();
      }
   }

   private int getThemeColor() {
      return this.customColor.getValue() ? this.color.getValue().getColor() : HudEditor.getColor(0).getRGB();
   }

   private float getDurabilityProgress(ItemStack stack) {
      if (!stack.isEmpty() && stack.isDamageable()) {
         int maxDamage = stack.getMaxDamage();
         return maxDamage <= 0 ? 1.0F : MathHelper.clamp(1.0F - (float)stack.getDamage() / (float)maxDamage, 0.0F, 1.0F);
      } else {
         return 1.0F;
      }
   }

   private int getDurabilityColor(float progress, int alphaVal) {
      int r;
      int g;
      int b;
      if (progress > 0.6F) {
         float t = (progress - 0.6F) / 0.4F;
         r = (int)(255.0F * (1.0F - t));
         g = 210;
         b = 60;
      } else if (progress > 0.3F) {
         float t = (progress - 0.3F) / 0.3F;
         r = 255;
         g = (int)(210.0F * t);
         b = 0;
      } else {
         r = 230;
         g = (int)(60.0F * (progress / 0.3F));
         b = 20;
      }

      return alphaVal << 24 | r << 16 | g << 8 | b;
   }

   public static void drawRingArc(MatrixStack matrices, float centerX, float centerY, float radius, float thickness, float startAngle, float endAngle, int color) {
      if (!(startAngle >= endAngle)) {
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
         float innerRadius = Math.max(0.1F, radius - thickness * 0.5F);
         float outerRadius = radius + thickness * 0.5F;
         float step = 3.0F;

         for (float angle = startAngle; angle <= endAngle + 0.1F; angle += step) {
            float curAngle = Math.min(angle, endAngle);
            double rad = Math.toRadians((double)curAngle);
            float cos = (float)Math.cos(rad);
            float sin = (float)Math.sin(rad);
            float ox = centerX + cos * outerRadius;
            float oy = centerY + sin * outerRadius;
            float ix = centerX + cos * innerRadius;
            float iy = centerY + sin * innerRadius;
            buffer.vertex(matrix, ox, oy, 0.0F).color(color);
            buffer.vertex(matrix, ix, iy, 0.0F).color(color);
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.disableBlend();
      }
   }

   @EventHandler
   @Override
   public void onMouse(@NotNull EventMouse event) {
      if (event.getAction() == 1
         && event.getButton() == 1
         && this.isHovering()
         && (mc.currentScreen instanceof HudEditorGui || mc.currentScreen instanceof ChatScreen)) {
         ZeltrixTargetHud.ThemeStyle[] styles = ZeltrixTargetHud.ThemeStyle.values();
         this.themeStyle.setValue(styles[(this.themeStyle.getValue().ordinal() + 1) % styles.length]);
      }

      super.onMouse(event);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      if (mc.player != null) {
         this.updateTarget();
         float targetAlpha = this.direction ? 1.0F : 0.0F;
         this.alpha = MathHelper.clamp(Render2DEngine.interpolateFloat(this.alpha, targetAlpha, 0.22F), 0.0F, 1.0F);
         if (this.target != null && !(this.alpha <= 0.01F)) {
            float maxHealth = Math.max(1.0F, this.target.getMaxHealth());
            float curHealth = this.getTargetHealth();
            this.hpAnimated = Render2DEngine.interpolateFloat(this.hpAnimated, curHealth, 0.2F);
            float targetHpProgress = MathHelper.clamp(curHealth / maxHealth, 0.0F, 1.0F);
            this.hpProgressAnimated = Render2DEngine.interpolateFloat(this.hpProgressAnimated, targetHpProgress, 0.2F);
            float absorpAmount = this.absorp.getValue() ? this.target.getAbsorptionAmount() : 0.0F;
            float targetAbsorpProgress = absorpAmount > 0.0F ? MathHelper.clamp(absorpAmount / maxHealth, 0.0F, 1.0F) : 0.0F;
            this.absorptionAnimated = Render2DEngine.interpolateFloat(this.absorptionAnimated, targetAbsorpProgress, 0.2F);
            String name = ModuleManager.media.isEnabled()
               ? "Protected"
               : (ModuleManager.nameProtect.isEnabled() && this.target == mc.player ? NameProtect.getCustomName() : this.target.getName().getString());
            int roundedHp = Math.round(this.hpAnimated);
            long now = System.currentTimeMillis();
            String hpText = String.valueOf(Math.max(0, roundedHp));
            char[] curDigits = new char[3];

            for (int i = 0; i < 3; i++) {
               curDigits[i] = ' ';
            }

            for (int i = 0; i < hpText.length(); i++) {
               curDigits[3 - hpText.length() + i] = hpText.charAt(i);
            }

            if (this.lastRoundedHp == -1) {
               System.arraycopy(curDigits, 0, this.lastDigits, 0, 3);

               for (int i = 0; i < 3; i++) {
                  this.digitOffsetY[i] = 0.0F;
                  this.digitAlpha[i] = 1.0F;
                  this.digitChangeTime[i] = now - 1000L;
               }
            } else if (roundedHp != this.lastRoundedHp) {
               boolean isDamage = roundedHp < this.lastRoundedHp;

               for (int i = 0; i < 3; i++) {
                  if (curDigits[i] != this.lastDigits[i]) {
                     this.digitOffsetY[i] = isDamage ? 5.0F : -5.0F;
                     this.digitAlpha[i] = 0.0F;
                     this.digitChangeTime[i] = now;
                  }
               }

               System.arraycopy(curDigits, 0, this.lastDigits, 0, 3);
            }

            this.lastRoundedHp = roundedHp;

            for (int ix = 0; ix < 3; ix++) {
               float el = (float)(now - this.digitChangeTime[ix]) / 1000.0F;
               float t = Math.min(1.0F, el / 0.28F);
               float ease = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
               float fadeT = Math.min(1.0F, el / 0.22F);
               this.digitOffsetY[ix] = this.digitOffsetY[ix] * (1.0F - ease);
               this.digitAlpha[ix] = (float)Math.sin((double)fadeT * Math.PI * 0.5);
            }

            float x = this.getPosX();
            float y = this.getPosY();
            float padding = 5.0F;
            float headSize = 26.0F;
            float healthRingRadius = 9.5F;
            float healthRingSize = healthRingRadius * 2.0F;
            float textX = x + padding + headSize + 6.0F;
            float nameWidth = FontRenderers.sf_bold_mini.getStringWidth(name);
            float itemsWidth = 66.0F;
            float contentW = Math.max(Math.max(nameWidth, itemsWidth), 66.0F);
            float width = padding + headSize + 6.0F + contentW + 10.0F + healthRingSize + padding;
            float height = padding * 2.0F + headSize;
            float headX = x + padding;
            float headY = y + padding;
            float nameY = y + padding + 1.0F;
            float itemsY = nameY + 12.0F;
            float healthCenterX = x + width - padding - healthRingRadius;
            float healthCenterY = y + height * 0.5F;
            int drawAlpha = (int)(255.0F * this.alpha);
            int themeColor = this.customColor.getValue() ? this.color.getValue().getColorObject().getRGB() : HudEditor.getColor(0).getRGB();
            int themeColorWithAlpha = drawAlpha << 24 | themeColor & 16777215;
            ZeltrixTargetHud.ThemeStyle style = this.themeStyle.getValue();
            Color bgOuter = style == ZeltrixTargetHud.ThemeStyle.Glass
               ? new Color(14, 14, 18, (int)(100.0F * this.alpha))
               : new Color(14, 14, 18, (int)(230.0F * this.alpha));
            Color bgInner = style == ZeltrixTargetHud.ThemeStyle.Glass
               ? new Color(25, 25, 32, (int)(60.0F * this.alpha))
               : new Color(25, 25, 32, (int)(120.0F * this.alpha));
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            float s = this.scale.getValue();
            matrices.translate(x, y, 0.0F);
            matrices.scale(s, s, 1.0F);
            matrices.translate(-x, -y, 0.0F);
            if (style == ZeltrixTargetHud.ThemeStyle.Neon) {
               Render2DEngine.drawBlurredShadow(
                  matrices, x - 1.0F, y - 1.0F, width + 2.0F, height + 2.0F, 8, new Color(themeColor & 16777215 | 1342177280, true)
               );
            }

            Render2DEngine.drawRound(matrices, x, y, width, height, 5.0F, bgOuter);
            Render2DEngine.drawRound(matrices, x + 0.5F, y + 0.5F, width - 1.0F, height - 1.0F, 4.5F, bgInner);
            float hurtPercent = 0.0F;
            if (this.target.hurtTime > 0) {
               hurtPercent = MathHelper.clamp((float)this.target.hurtTime / 10.0F * 0.55F, 0.0F, 0.55F);
            }

            if (this.target instanceof PlayerEntity player) {
               RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity)player).getSkinTextures().texture());
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.setShaderColor(1.0F, 1.0F - hurtPercent, 1.0F - hurtPercent, this.alpha);
               Render2DEngine.renderTexture(matrices, (double)headX, (double)headY, (double)headSize, (double)headSize, 8.0F, 8.0F, 8.0, 8.0, 64.0, 64.0);
               Render2DEngine.renderTexture(matrices, (double)headX, (double)headY, (double)headSize, (double)headSize, 40.0F, 8.0F, 8.0, 8.0, 64.0, 64.0);
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
               RenderSystem.setShaderTexture(0, mc.getEntityRenderDispatcher().getRenderer(this.target).getTexture(this.target));
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.setShaderColor(1.0F, 1.0F - hurtPercent, 1.0F - hurtPercent, this.alpha);
               Render2DEngine.renderTexture(matrices, (double)headX, (double)headY, (double)headSize, (double)headSize, 0.0F, 0.0F, 1.0, 1.0, 1.0, 1.0);
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }

            FontRenderers.sf_bold_mini.drawString(matrices, name, (double)textX, (double)nameY, drawAlpha << 24 | 16777215);
            ItemStack[] items = new ItemStack[6];
            if (this.target instanceof PlayerEntity player) {
               List<ItemStack> armor = player.getInventory().armor;
               items[0] = armor.get(3);
               items[1] = armor.get(2);
               items[2] = armor.get(1);
               items[3] = armor.get(0);
               items[4] = player.getOffHandStack();
               items[5] = player.getMainHandStack();
            } else {
               items[0] = this.target.getEquippedStack(EquipmentSlot.HEAD);
               items[1] = this.target.getEquippedStack(EquipmentSlot.CHEST);
               items[2] = this.target.getEquippedStack(EquipmentSlot.LEGS);
               items[3] = this.target.getEquippedStack(EquipmentSlot.FEET);
               items[4] = this.target.getOffHandStack();
               items[5] = this.target.getMainHandStack();
            }

            float itemScale = 0.55F;

            for (int ix = 0; ix < 6; ix++) {
               ItemStack stack = items[ix];
               float slotX = textX + (float)ix * 11.0F;
               if (stack != null && !stack.isEmpty()) {
                  matrices.push();
                  matrices.translate(slotX - 1.0F, itemsY - 1.0F, 0.0F);
                  matrices.scale(itemScale, itemScale, 1.0F);
                  context.drawItem(stack, 0, 0);
                  matrices.pop();
                  if (stack.isDamageable() && stack.getDamage() > 0) {
                     float durProgress = this.getDurabilityProgress(stack);
                     float durW = 8.5F;
                     float durBarY = itemsY + 9.5F;
                     int durBg = Math.min(drawAlpha, 180) << 24 | 2369072;
                     int durFill = this.getDurabilityColor(durProgress, drawAlpha);
                     Render2DEngine.drawRound(matrices, slotX, durBarY, durW, 1.4F, 0.5F, new Color(durBg, true));
                     if (durProgress > 0.01F) {
                        Render2DEngine.drawRound(matrices, slotX, durBarY, durW * durProgress, 1.4F, 0.5F, new Color(durFill, true));
                     }
                  }
               } else {
                  Render2DEngine.drawRound(matrices, slotX + 1.0F, itemsY + 1.0F, 7.5F, 7.5F, 2.0F, new Color(36, 38, 48, (int)(140.0F * this.alpha)));
               }
            }

            int tr = themeColor >> 16 & 0xFF;
            int tg = themeColor >> 8 & 0xFF;
            int tb = themeColor & 0xFF;
            int ringBgColor = drawAlpha << 24 | tr / 4 << 16 | tg / 4 << 8 | tb / 4;
            drawRingArc(matrices, healthCenterX, healthCenterY, healthRingRadius, 2.2F, 0.0F, 360.0F, ringBgColor);
            float sweep = MathHelper.clamp(this.hpProgressAnimated, 0.0F, 1.0F) * 360.0F;
            if (sweep > 0.5F) {
               drawRingArc(matrices, healthCenterX, healthCenterY, healthRingRadius, 2.2F, -90.0F, -90.0F + sweep, themeColorWithAlpha);
            }

            if (this.absorptionAnimated > 0.01F) {
               float absorbSweep = MathHelper.clamp(this.absorptionAnimated, 0.0F, 1.0F) * 360.0F;
               int goldColor = drawAlpha << 24 | 16762880;
               drawRingArc(matrices, healthCenterX, healthCenterY, healthRingRadius, 2.2F, -90.0F, -90.0F + absorbSweep, goldColor);
            }

            float totalW = FontRenderers.sf_bold_mini.getStringWidth(hpText);
            float startX = healthCenterX - totalW * 0.5F;
            float baseY = healthCenterY - 3.5F;
            float sx1 = x + (healthCenterX - healthRingRadius - 2.0F - x) * s;
            float sy1 = y + (healthCenterY - healthRingRadius - 2.0F - y) * s;
            float sx2 = x + (healthCenterX + healthRingRadius + 2.0F - x) * s;
            float sy2 = y + (healthCenterY + healthRingRadius + 2.0F - y) * s;
            Render2DEngine.beginScissor((double)sx1, (double)sy1, (double)sx2, (double)sy2);

            for (int ixx = 0; ixx < hpText.length(); ixx++) {
               int slot = 3 - hpText.length() + ixx;
               String ch = String.valueOf(hpText.charAt(ixx));
               float charW = FontRenderers.sf_bold_mini.getStringWidth(ch);
               int charAlpha = (int)((float)drawAlpha * this.digitAlpha[slot]);
               int charColor = MathHelper.clamp(charAlpha, 0, 255) << 24 | 16777215;
               FontRenderers.sf_bold_mini.drawString(matrices, ch, (double)startX, (double)(baseY + this.digitOffsetY[slot]), charColor);
               startX += charW;
            }

            Render2DEngine.endScissor();
            matrices.pop();
            this.setBounds(x, y, width * s, height * s);
         } else {
            this.setBounds(this.getPosX(), this.getPosY(), 0.0F, 0.0F);
         }
      }
   }

   public static enum ThemeStyle {
      Solid,
      Neon,
      Glass;
   }
}
