package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.features.modules.misc.NameProtect;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.EaseOutBack;
import thunder.hack.utility.render.animation.EaseOutCirc;

public class TargetHud extends HudElement {
   private final Setting<Integer> blurRadius = new Setting<>("BallonBlur", 10, 1, 10);
   private final Setting<Integer> animX = new Setting<>("AnimationX", 0, -2000, 2000);
   private final Setting<Integer> animY = new Setting<>("AnimationY", 0, -2000, 2000);
   private final Setting<TargetHud.HPmodeEn> hpMode = new Setting<>("HP Mode", TargetHud.HPmodeEn.HP);
   private final Setting<TargetHud.ImageModeEn> imageMode = new Setting<>("Image", TargetHud.ImageModeEn.Anime);
   private final Setting<TargetHud.ModeEn> Mode = new Setting<>("Mode", TargetHud.ModeEn.ThunderHack);
   private final Setting<ColorSetting> color = new Setting<>("Color1", new ColorSetting(-16492289), v -> this.Mode.getValue() == TargetHud.ModeEn.CelkaPasta);
   private final Setting<ColorSetting> color2 = new Setting<>("Color2", new ColorSetting(-16492289), v -> this.Mode.getValue() == TargetHud.ModeEn.CelkaPasta);
   private final Setting<Boolean> funTimeHP = new Setting<>("FunTimeHP", false);
   private final Setting<Boolean> mini = new Setting<>("Mini", false, v -> this.Mode.getValue() == TargetHud.ModeEn.NurikZapen);
   private final Setting<Boolean> absorp = new Setting<>("Absorption", true);
   private static Identifier custom;
   public EaseOutBack animation = new EaseOutBack();
   public static EaseOutCirc healthAnimation = new EaseOutCirc();
   public static EaseOutCirc headAnimation = new EaseOutCirc();
   private final ArrayList<Particles> particles = new ArrayList<>();
   private boolean sentParticles;
   private boolean direction = false;
   private LivingEntity target;
   float ticks;
   private final Timer timer = new Timer();

   public TargetHud() {
      super("TargetHud", 150, 50);
   }

   @Override
   public void onEnable() {
      try {
         custom = ThunderUtility.getCustomImg("thud");
      } catch (Exception var2) {
         this.sendMessage(".minecraft -> ThunderHackRecode -> misc -> images -> thud.png");
      }
   }

   public String getDurationString(StatusEffectInstance pe) {
      return pe.isInfinite() ? "*:*" : pe.getDuration() / 1200 + ":" + pe.getDuration() % 1200 / 20;
   }

   @Override
   public void onUpdate() {
      this.animation.update(this.direction);
      healthAnimation.update();
      headAnimation.update();
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      this.getTarget();
      if (this.target != null) {
         float health = Math.min(this.target.getMaxHealth(), this.getHealth());
         context.getMatrices().push();
         if (!HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            if (this.Mode.is(TargetHud.ModeEn.NurikZapen) && this.mini.getValue()) {
               sizeAnimation(
                  context.getMatrices(),
                  (double)(this.getPosX() + 45.0F + (float)this.animX.getValue().intValue()),
                  (double)(this.getPosY() + 15.0F + (float)this.animY.getValue().intValue()),
                  this.animation.getAnimationd()
               );
            } else {
               sizeAnimation(
                  context.getMatrices(),
                  (double)(this.getPosX() + 75.0F + (float)this.animX.getValue().intValue()),
                  (double)(this.getPosY() + 25.0F + (float)this.animY.getValue().intValue()),
                  this.animation.getAnimationd()
               );
            }
         }

         if (this.animation.getAnimationd() > 0.0) {
            float animationFactor = (float)MathUtility.clamp(this.animation.getAnimationd(), 0.0, 1.0);
            switch ((TargetHud.ModeEn)this.Mode.getValue()) {
               case ThunderHack:
                  this.renderThunderHack(context, health, animationFactor);
                  break;
               case NurikZapen:
                  if (this.mini.getValue()) {
                     this.renderMiniNurik(context, health, animationFactor);
                  } else {
                     this.renderNurik(context, health, animationFactor);
                  }
                  break;
               case CelkaPasta:
                  this.renderCelkaPasta(context, health);
            }
         }

         context.getMatrices().pop();
      }
   }

   private void getTarget() {
      if (Aura.target != null) {
         if (Aura.target instanceof LivingEntity) {
            this.target = (LivingEntity)Aura.target;
            this.direction = true;
         } else {
            this.target = null;
            this.direction = false;
         }
      } else if (!(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof HudEditorGui)) {
         this.direction = false;
         if (this.animation.getAnimationd() < 0.02) {
            this.target = null;
         }
      } else {
         this.target = mc.player;
         this.direction = true;
      }
   }

   private void renderCelkaPasta(DrawContext context, float health) {
      float hurtPercent = (float)this.target.hurtTime / 6.0F;
      Render2DEngine.drawBlurredShadow(
         context.getMatrices(), this.getPosX() - 2.0F, this.getPosY() - 2.0F, 164.0F, 51.0F, 5, this.color.getValue().getColorObject()
      );
      Render2DEngine.drawRect(context.getMatrices(), this.getPosX(), this.getPosY(), 160.0F, 47.0F, new Color(1711276032, true));
      this.setBounds(this.getPosX(), this.getPosY(), 160.0F, 47.0F);
      Render2DEngine.drawRect(context.getMatrices(), this.getPosX() + 117.0F, this.getPosY() + 4.0F, 18.0F, 18.0F, new Color(1291845632, true));
      Render2DEngine.drawRect(context.getMatrices(), this.getPosX() + 137.0F, this.getPosY() + 4.0F, 18.0F, 18.0F, new Color(1291845632, true));
      Render2DEngine.drawRect(context.getMatrices(), this.getPosX() + 117.0F, this.getPosY() + 25.0F, 18.0F, 18.0F, new Color(1291845632, true));
      Render2DEngine.drawRect(context.getMatrices(), this.getPosX() + 137.0F, this.getPosY() + 25.0F, 18.0F, 18.0F, new Color(1291845632, true));
      Render2DEngine.drawBlurredShadow(
         context.getMatrices(),
         this.getPosX() + 49.0F,
         this.getPosY() + 29.0F,
         62.0F,
         12.0F,
         5,
         this.color.getValue().getColorObject().brighter().brighter().brighter()
      );
      Render2DEngine.drawRect(context.getMatrices(), this.getPosX() + 50.0F, this.getPosY() + 30.0F, 60.0F, 10.0F, new Color(-1644167168, true));
      Render2DEngine.drawRect(
         context.getMatrices(),
         this.getPosX() + 50.0F,
         this.getPosY() + 30.0F,
         (float)MathUtility.clamp((int)(60.0F * (health / this.target.getMaxHealth())), 0, 60),
         10.0F,
         this.color.getValue().getColorObject().brighter().brighter().brighter()
      );
      if (this.target instanceof PlayerEntity) {
         RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity)this.target).getSkinTextures().texture());
      } else {
         RenderSystem.setShaderTexture(0, mc.getEntityRenderDispatcher().getRenderer(this.target).getTexture(this.target));
      }

      RenderSystem.setShaderColor(1.0F, 1.0F - hurtPercent, 1.0F - hurtPercent, 1.0F);
      Render2DEngine.renderTexture(
         context.getMatrices(),
         (double)(this.getPosX() + 3.5F + hurtPercent),
         (double)(this.getPosY() + 3.5F + hurtPercent),
         (double)(40.0F - hurtPercent * 2.0F),
         (double)(40.0F - hurtPercent * 2.0F),
         8.0F,
         8.0F,
         8.0,
         8.0,
         64.0,
         64.0
      );
      Render2DEngine.renderTexture(
         context.getMatrices(),
         (double)(this.getPosX() + 3.5F + hurtPercent),
         (double)(this.getPosY() + 3.5F + hurtPercent),
         (double)(40.0F - hurtPercent * 2.0F),
         (double)(40.0F - hurtPercent * 2.0F),
         40.0F,
         8.0F,
         8.0,
         8.0,
         64.0,
         64.0
      );
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      FontRenderers.modules
         .drawString(
            context.getMatrices(),
            ModuleManager.media.isEnabled()
               ? "Protected "
               : (ModuleManager.nameProtect.isEnabled() && this.target == mc.player ? NameProtect.getCustomName() : this.target.getName().getString()),
            (double)(this.getPosX() + 50.0F),
            (double)(this.getPosY() + 7.0F),
            -1
         );
      FontRenderers.modules
         .drawCenteredString(
            context.getMatrices(),
            this.hpMode.getValue() == TargetHud.HPmodeEn.HP
               ? String.valueOf((double)Math.round(10.0 * (double)this.getHealth()) / 10.0)
               : (double)Math.round(10.0 * (double)this.getHealth()) / 10.0 / 20.0 * 100.0 + "%",
            (double)(this.getPosX() + 81.0F),
            (double)(this.getPosY() + 34.0F),
            -1
         );
      if (this.target instanceof PlayerEntity pe) {
         this.celestialArmor(context, pe, this.getPosX(), this.getPosY());
         this.celestialHands(context, pe, this.getPosX(), this.getPosY());
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderNurik(DrawContext context, float health, float animationFactor) {
      float hurtPercent = Render2DEngine.interpolateFloat(
            (float)MathUtility.clamp(this.target.hurtTime == 0 ? 0 : this.target.hurtTime + 1, 0, 10),
            (float)this.target.hurtTime,
            (double)Render3DEngine.getTickDelta()
         )
         / 8.0F;
      healthAnimation.setValue((double)health);
      health = (float)healthAnimation.getAnimationD();
      if (this.animation.getAnimationd() != 1.0 && !HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         Render2DEngine.drawGradientBlurredShadow1(
            context.getMatrices(),
            this.getPosX() + 4.0F,
            this.getPosY() + 4.0F,
            131.0F,
            40.0F,
            14,
            HudEditor.getColor(270),
            HudEditor.getColor(0),
            HudEditor.getColor(180),
            HudEditor.getColor(90)
         );
         Render2DEngine.renderRoundedGradientRect(
            context.getMatrices(),
            HudEditor.getColor(270),
            HudEditor.getColor(0),
            HudEditor.getColor(180),
            HudEditor.getColor(90),
            this.getPosX(),
            this.getPosY(),
            137.0F,
            47.5F,
            9.0F
         );
         Render2DEngine.drawRound(
            context.getMatrices(), this.getPosX() + 0.5F, this.getPosY() + 0.5F, 136.0F, 46.0F, 9.0F, Render2DEngine.injectAlpha(Color.BLACK, 220)
         );
      } else {
         Render2DEngine.drawHudBase2(
            context.getMatrices(),
            this.getPosX(),
            this.getPosY(),
            137.0F,
            47.5F,
            9.0F,
            HudEditor.blurStrength.getValue(),
            HudEditor.blurOpacity.getValue(),
            animationFactor
         );
      }

      this.setBounds(this.getPosX(), this.getPosY(), 137.0F, 47.5F);
      if (this.target instanceof PlayerEntity) {
         RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity)this.target).getSkinTextures().texture());
      } else {
         RenderSystem.setShaderTexture(0, mc.getEntityRenderDispatcher().getRenderer(this.target).getTexture(this.target));
      }

      context.getMatrices().push();
      context.getMatrices().translate(this.getPosX() + 3.5F + 20.0F, this.getPosY() + 3.5F + 20.0F, 0.0F);
      context.getMatrices().scale(1.0F - hurtPercent / 15.0F, 1.0F - hurtPercent / 15.0F, 1.0F);
      context.getMatrices().translate(-(this.getPosX() + 3.5F + 20.0F), -(this.getPosY() + 3.5F + 20.0F), 0.0F);
      RenderSystem.enableBlend();
      RenderSystem.colorMask(false, false, false, true);
      RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
      RenderSystem.clear(16384, false);
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      Render2DEngine.renderRoundedQuadInternal(
         context.getMatrices().peek().getPositionMatrix(),
         animationFactor,
         animationFactor,
         animationFactor,
         animationFactor,
         (double)(this.getPosX() + 3.5F),
         (double)(this.getPosY() + 3.5F),
         (double)(this.getPosX() + 3.5F + 40.0F),
         (double)(this.getPosY() + 3.5F + 40.0F),
         7.0,
         10.0
      );
      RenderSystem.blendFunc(772, 773);
      RenderSystem.setShaderColor(
         animationFactor,
         animationFactor - hurtPercent / 2.0F,
         animationFactor - hurtPercent / 2.0F,
         (float)MathUtility.clamp(this.animation.getAnimationd(), 0.0, 1.0)
      );
      Render2DEngine.renderTexture(
         context.getMatrices(), (double)(this.getPosX() + 3.5F), (double)(this.getPosY() + 3.5F), 40.0, 40.0, 8.0F, 8.0F, 8.0, 8.0, 64.0, 64.0
      );
      Render2DEngine.renderTexture(
         context.getMatrices(), (double)(this.getPosX() + 3.5F), (double)(this.getPosY() + 3.5F), 40.0, 40.0, 40.0F, 8.0F, 8.0, 8.0, 64.0, 64.0
      );
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      context.getMatrices().pop();
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         Render2DEngine.drawRect(
            context.getMatrices(), this.getPosX() + 48.0F, this.getPosY() + 32.0F, 85.0F, 11.0F, 4.0F, (float)(0.15F * this.animation.getAnimationd())
         );
         Render2DEngine.drawRect(
            context.getMatrices(),
            this.getPosX() + 48.0F,
            this.getPosY() + 32.0F,
            MathUtility.clamp(85.0F * (health / this.target.getMaxHealth()), 8.0F, 85.0F),
            11.0F,
            4.0F,
            (float)this.animation.getAnimationd()
         );
      } else {
         Render2DEngine.drawGradientRound(
            context.getMatrices(),
            this.getPosX() + 48.0F,
            this.getPosY() + 32.0F,
            85.0F,
            11.0F,
            4.0F,
            HudEditor.getColor(0).darker().darker(),
            HudEditor.getColor(0).darker().darker().darker().darker(),
            HudEditor.getColor(0).darker().darker().darker().darker(),
            HudEditor.getColor(0).darker().darker().darker().darker()
         );
         Render2DEngine.renderRoundedGradientRect(
            context.getMatrices(),
            HudEditor.getColor(270),
            HudEditor.getColor(0),
            HudEditor.getColor(0),
            HudEditor.getColor(270),
            this.getPosX() + 48.0F,
            this.getPosY() + 32.0F,
            (float)((int)MathUtility.clamp(85.0F * (health / this.target.getMaxHealth()), 8.0F, 85.0F)),
            11.0F,
            4.0F
         );
      }

      FontRenderers.sf_bold
         .drawCenteredString(
            context.getMatrices(),
            this.hpMode.getValue() == TargetHud.HPmodeEn.HP
               ? String.valueOf((double)Math.round(10.0 * (double)this.getHealth()) / 10.0)
               : (double)Math.round(10.0 * (double)this.getHealth()) / 10.0 / 20.0 * 100.0 + "%",
            (double)(this.getPosX() + 92.0F),
            (double)(this.getPosY() + 35.0F),
            Render2DEngine.applyOpacity(-1, animationFactor)
         );
      FontRenderers.sf_bold
         .drawString(
            context.getMatrices(),
            ModuleManager.media.isEnabled()
               ? "Protected "
               : (ModuleManager.nameProtect.isEnabled() && this.target == mc.player ? NameProtect.getCustomName() : this.target.getName().getString()),
            (double)(this.getPosX() + 48.0F),
            (double)(this.getPosY() + 7.0F),
            Render2DEngine.applyOpacity(-1, animationFactor)
         );
      if (this.target instanceof PlayerEntity) {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float)MathUtility.clamp(this.animation.getAnimationd(), 0.0, 1.0));
         List<ItemStack> armor = ((PlayerEntity)this.target).getInventory().armor;
         ItemStack[] items = new ItemStack[]{this.target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), this.target.getOffHandStack()};
         float xItemOffset = this.getPosX() + 48.0F;

         for (ItemStack itemStack : items) {
            context.getMatrices().push();
            context.getMatrices().translate(xItemOffset, this.getPosY() + 15.0F, 0.0F);
            context.getMatrices().scale(0.75F, 0.75F, 0.75F);
            context.drawItem(itemStack, 0, 0);
            context.drawItemInSlot(mc.textRenderer, itemStack, 0, 0);
            context.getMatrices().pop();
            xItemOffset += 12.0F;
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private void renderMiniNurik(DrawContext context, float health, float animationFactor) {
      float hurtPercent = Render2DEngine.interpolateFloat(
            (float)MathUtility.clamp(this.target.hurtTime == 0 ? 0 : this.target.hurtTime + 1, 0, 10),
            (float)this.target.hurtTime,
            (double)Render3DEngine.getTickDelta()
         )
         / 8.0F;
      healthAnimation.setValue((double)health);
      health = (float)healthAnimation.getAnimationD();
      if (this.animation.getAnimationd() != 1.0 && !HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         Render2DEngine.drawGradientBlurredShadow1(
            context.getMatrices(),
            this.getPosX() + 2.0F,
            this.getPosY() + 2.0F,
            91.0F,
            31.0F,
            12,
            HudEditor.getColor(270),
            HudEditor.getColor(0),
            HudEditor.getColor(180),
            HudEditor.getColor(90)
         );
         Render2DEngine.renderRoundedGradientRect(
            context.getMatrices(),
            HudEditor.getColor(270),
            HudEditor.getColor(0),
            HudEditor.getColor(180),
            HudEditor.getColor(90),
            this.getPosX(),
            this.getPosY(),
            95.0F,
            35.0F,
            7.0F
         );
         Render2DEngine.drawRound(
            context.getMatrices(), this.getPosX() + 0.5F, this.getPosY() + 0.5F, 94.0F, 34.0F, 7.0F, Render2DEngine.injectAlpha(Color.BLACK, 220)
         );
      } else {
         Render2DEngine.drawHudBase2(
            context.getMatrices(),
            this.getPosX(),
            this.getPosY(),
            95.0F,
            35.5F,
            8.0F,
            HudEditor.blurStrength.getValue(),
            HudEditor.blurOpacity.getValue(),
            animationFactor
         );
      }

      this.setBounds(this.getPosX(), this.getPosY(), 95.0F, 35.5F);
      if (this.target instanceof PlayerEntity) {
         RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity)this.target).getSkinTextures().texture());
      } else {
         RenderSystem.setShaderTexture(0, mc.getEntityRenderDispatcher().getRenderer(this.target).getTexture(this.target));
      }

      context.getMatrices().push();
      context.getMatrices().translate((double)this.getPosX() + 2.5 + 15.0, (double)this.getPosY() + 2.5 + 15.0, 0.0);
      context.getMatrices().scale(1.0F - hurtPercent / 20.0F, 1.0F - hurtPercent / 20.0F, 1.0F);
      context.getMatrices().translate(-((double)this.getPosX() + 2.5 + 15.0), -((double)this.getPosY() + 2.5 + 15.0), 0.0);
      RenderSystem.enableBlend();
      RenderSystem.colorMask(false, false, false, true);
      RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
      RenderSystem.clear(16384, false);
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      Render2DEngine.renderRoundedQuadInternal(
         context.getMatrices().peek().getPositionMatrix(),
         animationFactor,
         animationFactor,
         animationFactor,
         animationFactor,
         (double)this.getPosX() + 2.5,
         (double)this.getPosY() + 2.5,
         (double)this.getPosX() + 2.5 + 30.0,
         (double)this.getPosY() + 2.5 + 30.0,
         5.0,
         10.0
      );
      RenderSystem.blendFunc(772, 773);
      RenderSystem.setShaderColor(animationFactor, animationFactor - hurtPercent / 2.0F, animationFactor - hurtPercent / 2.0F, animationFactor);
      Render2DEngine.renderTexture(
         context.getMatrices(), (double)this.getPosX() + 2.5, (double)this.getPosY() + 2.5, 30.0, 30.0, 8.0F, 8.0F, 8.0, 8.0, 64.0, 64.0
      );
      Render2DEngine.renderTexture(
         context.getMatrices(), (double)this.getPosX() + 2.5, (double)this.getPosY() + 2.5, 30.0, 30.0, 40.0F, 8.0F, 8.0, 8.0, 64.0, 64.0
      );
      RenderSystem.defaultBlendFunc();
      context.getMatrices().pop();
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         Render2DEngine.drawRect(
            context.getMatrices(), this.getPosX() + 38.0F, this.getPosY() + 25.0F, 52.0F, 7.0F, 2.0F, (float)(0.15F * this.animation.getAnimationd())
         );
         Render2DEngine.drawRect(
            context.getMatrices(),
            this.getPosX() + 38.0F,
            this.getPosY() + 25.0F,
            MathUtility.clamp(52.0F * (health / this.target.getMaxHealth()), 8.0F, 52.0F),
            7.0F,
            2.0F,
            (float)this.animation.getAnimationd()
         );
      } else {
         Render2DEngine.drawGradientRound(
            context.getMatrices(),
            this.getPosX() + 38.0F,
            this.getPosY() + 25.0F,
            52.0F,
            7.0F,
            2.0F,
            HudEditor.getColor(0).darker().darker(),
            HudEditor.getColor(0).darker().darker().darker().darker(),
            HudEditor.getColor(0).darker().darker().darker().darker(),
            HudEditor.getColor(0).darker().darker().darker().darker()
         );
         Render2DEngine.renderRoundedGradientRect(
            context.getMatrices(),
            HudEditor.getColor(270),
            HudEditor.getColor(0),
            HudEditor.getColor(0),
            HudEditor.getColor(270),
            this.getPosX() + 38.0F,
            this.getPosY() + 25.0F,
            (float)((int)MathUtility.clamp(52.0F * (health / this.target.getMaxHealth()), 8.0F, 52.0F)),
            7.0F,
            2.0F
         );
      }

      FontRenderers.sf_bold_mini
         .drawCenteredString(
            context.getMatrices(),
            this.hpMode.getValue() == TargetHud.HPmodeEn.HP
               ? String.valueOf((double)Math.round(10.0 * (double)this.getHealth()) / 10.0)
               : (double)Math.round(10.0 * (double)this.getHealth()) / 10.0 / 20.0 * 100.0 + "%",
            (double)(this.getPosX() + 65.0F),
            (double)(this.getPosY() + 27.0F),
            Render2DEngine.applyOpacity(-1, animationFactor)
         );
      FontRenderers.sf_bold_mini
         .drawString(
            context.getMatrices(),
            ModuleManager.media.isEnabled()
               ? "Protected "
               : (ModuleManager.nameProtect.isEnabled() && this.target == mc.player ? NameProtect.getCustomName() : this.target.getName().getString()),
            (double)(this.getPosX() + 38.0F),
            (double)(this.getPosY() + 5.0F),
            Render2DEngine.applyOpacity(-1, animationFactor)
         );
      if (this.target instanceof PlayerEntity) {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float)MathUtility.clamp(this.animation.getAnimationd(), 0.0, 1.0));
         List<ItemStack> armor = ((PlayerEntity)this.target).getInventory().armor;
         ItemStack[] items = new ItemStack[]{this.target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), this.target.getOffHandStack()};
         float xItemOffset = this.getPosX() + 38.0F;

         for (ItemStack itemStack : items) {
            context.getMatrices().push();
            context.getMatrices().translate(xItemOffset, this.getPosY() + 13.0F, 0.0F);
            context.getMatrices().scale(0.5F, 0.5F, 0.5F);
            context.drawItem(itemStack, 0, 0);
            context.drawItemInSlot(mc.textRenderer, itemStack, 0, 0);
            context.getMatrices().pop();
            xItemOffset += 9.0F;
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private void renderThunderHack(DrawContext context, float health, float animationFactor) {
      float hurtPercent;
      hurtPercent = (float)this.target.hurtTime / 6.0F;
      Render2DEngine.drawRound(context.getMatrices(), this.getPosX(), this.getPosY(), 70.0F, 50.0F, 6.0F, new Color(0, 0, 0, 139));
      Render2DEngine.drawRound(context.getMatrices(), this.getPosX() + 50.0F, this.getPosY(), 100.0F, 50.0F, 6.0F, new Color(0, 0, 0, 255));
      this.setBounds(this.getPosX(), this.getPosY(), 150.0F, 50.0F);
      label125:
      if (!this.imageMode.is(TargetHud.ImageModeEn.None)) {
         if (this.imageMode.is(TargetHud.ImageModeEn.Anime)) {
            RenderSystem.setShaderTexture(0, TextureStorage.thudPic);
         } else {
            if (custom == null) {
               break label125;
            }

            RenderSystem.setShaderTexture(0, custom);
         }

         context.getMatrices().push();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         Render2DEngine.drawRound(context.getMatrices(), this.getPosX() + 50.0F, this.getPosY(), 100.0F, 50.0F, 12.0F, new Color(0, 0, 0, 255));
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(0.3F, 0.3F, 0.3F, 1.0F);
         Render2DEngine.renderTexture(
            context.getMatrices(), (double)(this.getPosX() + 50.0F), (double)this.getPosY(), 95.0, 50.0, 0.0F, 0.0F, 100.0, 50.0, 100.0, 50.0
         );
         context.getMatrices().pop();
      }

      for (Particles p : this.particles) {
         if (p.opacity > 4.0) {
            p.render2D(context.getMatrices());
         }
      }

      if (this.timer.passedMs(16L)) {
         this.ticks += 0.1F;

         for (Particles px : this.particles) {
            px.updatePosition();
            if (px.opacity < 1.0) {
               this.particles.remove(px);
            }
         }

         this.timer.reset();
      }

      ArrayList<Particles> removeList = new ArrayList<>();

      for (Particles pxx : this.particles) {
         if (pxx.opacity <= 1.0) {
            removeList.add(pxx);
         }
      }

      for (Particles pxxx : removeList) {
         this.particles.remove(pxxx);
      }

      if (this.target.hurtTime == 9 && !this.sentParticles) {
         for (int i = 0; i <= 6; i++) {
            Particles pxxx = new Particles();
            Color c = Particles.mixColors(
               this.color.getValue().getColorObject(),
               this.color2.getValue().getColorObject(),
               (Math.sin((double)(this.ticks + this.getPosX() * 0.4F + (float)i)) + 1.0) * 0.5
            );
            pxxx.init((double)this.getPosX(), (double)this.getPosY(), (double)MathUtility.random(-3.0F, 3.0F), (double)MathUtility.random(-3.0F, 3.0F), 20.0, c);
            this.particles.add(pxxx);
         }

         this.sentParticles = true;
      }

      if (this.target.hurtTime == 8) {
         this.sentParticles = false;
      }

      headAnimation.setValue((double)hurtPercent);
      if (this.target instanceof PlayerEntity) {
         RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity)this.target).getSkinTextures().texture());
      } else {
         RenderSystem.setShaderTexture(0, mc.getEntityRenderDispatcher().getRenderer(this.target).getTexture(this.target));
      }

      context.getMatrices().push();
      context.getMatrices().translate((double)this.getPosX() + 2.5 + 15.0, (double)this.getPosY() + 2.5 + 15.0, 0.0);
      context.getMatrices().scale(1.0F - hurtPercent / 20.0F, 1.0F - hurtPercent / 20.0F, 1.0F);
      context.getMatrices().translate(-((double)this.getPosX() + 2.5 + 15.0), -((double)this.getPosY() + 2.5 + 15.0), 0.0);
      RenderSystem.enableBlend();
      RenderSystem.colorMask(false, false, false, true);
      RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
      RenderSystem.clear(16384, false);
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      Render2DEngine.renderRoundedQuadInternal(
         context.getMatrices().peek().getPositionMatrix(),
         animationFactor,
         animationFactor,
         animationFactor,
         animationFactor,
         (double)this.getPosX() + 2.5,
         (double)this.getPosY() + 2.5,
         (double)this.getPosX() + 2.5 + 45.0,
         (double)this.getPosY() + 2.5 + 45.0,
         5.0,
         10.0
      );
      RenderSystem.blendFunc(772, 773);
      RenderSystem.setShaderColor(animationFactor, animationFactor - hurtPercent / 2.0F, animationFactor - hurtPercent / 2.0F, animationFactor);
      Render2DEngine.renderTexture(
         context.getMatrices(), (double)this.getPosX() + 2.5, (double)this.getPosY() + 2.5, 45.0, 45.0, 8.0F, 8.0F, 8.0, 8.0, 64.0, 64.0
      );
      Render2DEngine.renderTexture(
         context.getMatrices(), (double)this.getPosX() + 2.5, (double)this.getPosY() + 2.5, 45.0, 45.0, 40.0F, 8.0F, 8.0, 8.0, 64.0, 64.0
      );
      RenderSystem.defaultBlendFunc();
      context.getMatrices().pop();
      healthAnimation.setValue((double)health);
      health = (float)healthAnimation.getAnimationD();
      Render2DEngine.drawBlurredShadow(
         context.getMatrices(), this.getPosX() + 55.0F, this.getPosY() + 22.0F, 90.0F, 8.0F, this.blurRadius.getValue(), HudEditor.getColor(0)
      );
      Render2DEngine.drawGradientRound(
         context.getMatrices(),
         this.getPosX() + 55.0F,
         this.getPosY() + 35.0F - 14.0F,
         90.0F,
         10.0F,
         2.0F,
         HudEditor.getColor(0).darker().darker(),
         HudEditor.getColor(0).darker().darker().darker().darker(),
         HudEditor.getColor(0).darker().darker().darker().darker(),
         HudEditor.getColor(0).darker().darker().darker().darker()
      );
      Render2DEngine.renderRoundedGradientRect(
         context.getMatrices(),
         HudEditor.getColor(270),
         HudEditor.getColor(0),
         HudEditor.getColor(0),
         HudEditor.getColor(270),
         this.getPosX() + 55.0F,
         this.getPosY() + 35.0F - 14.0F,
         (float)((int)MathUtility.clamp(90.0F * (health / this.target.getMaxHealth()), 3.0F, 90.0F)),
         10.0F,
         2.0F
      );
      FontRenderers.sf_bold
         .drawCenteredString(
            context.getMatrices(),
            this.hpMode.getValue() == TargetHud.HPmodeEn.HP
               ? String.valueOf((double)Math.round(10.0 * (double)this.getHealth()) / 10.0)
               : (int)((double)Math.round(10.0 * (double)health) / 10.0 / 20.0 * 100.0) + "%",
            (double)(this.getPosX() + 102.0F),
            (double)(this.getPosY() + 24.0F),
            Render2DEngine.applyOpacity(-1, animationFactor)
         );
      FontRenderers.sf_bold
         .drawString(
            context.getMatrices(),
            ModuleManager.media.isEnabled()
               ? "Protected "
               : (ModuleManager.nameProtect.isEnabled() && this.target == mc.player ? NameProtect.getCustomName() : this.target.getName().getString()),
            (double)(this.getPosX() + 55.0F),
            (double)(this.getPosY() + 5.0F),
            Render2DEngine.applyOpacity(-1, animationFactor)
         );
      if (this.target instanceof PlayerEntity) {
         List<ItemStack> armor = ((PlayerEntity)this.target).getInventory().armor;
         ItemStack[] items = new ItemStack[]{this.target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), this.target.getOffHandStack()};
         float xItemOffset = this.getPosX() + 60.0F;

         for (ItemStack itemStack : items) {
            if (!itemStack.isEmpty()) {
               context.getMatrices().push();
               context.getMatrices().translate(xItemOffset, this.getPosY() + 35.0F, 0.0F);
               context.getMatrices().scale(0.75F, 0.75F, 0.75F);
               context.drawItem(itemStack, 0, 0);
               context.drawItemInSlot(mc.textRenderer, itemStack, 0, 0);
               context.getMatrices().pop();
               xItemOffset += 14.0F;
            }
         }

         this.drawPotionEffect(context.getMatrices(), (PlayerEntity)this.target);
      }
   }

   private void celestialArmor(DrawContext context, PlayerEntity target, float posX, float posY) {
      for (int i = 0; i < 4; i++) {
         if (!((ItemStack)target.getInventory().armor.get(3 - i)).isEmpty()) {
            context.getMatrices().push();
            context.getMatrices().translate(posX + (float)(i > 1 ? 138 : 118), posY + (float)(i % 2 == 0 ? 5 : 26), 0.0F);
            context.drawItem((ItemStack)target.getInventory().armor.get(3 - i), 0, 0);
            context.drawItemInSlot(mc.textRenderer, (ItemStack)target.getInventory().armor.get(3 - i), 0, 0);
            context.getMatrices().pop();
         }
      }
   }

   private void celestialHands(DrawContext context, PlayerEntity target, float posX, float posY) {
      for (int i = 0; i < 2; i++) {
         if (!(i == 0 ? target.getMainHandStack() : target.getOffHandStack()).isEmpty()) {
            context.getMatrices().push();
            context.getMatrices().translate(posX + (float)(i == 0 ? 50 : 77), posY + 14.0F, 0.0F);
            context.getMatrices().scale(0.75F, 0.75F, 1.0F);
            context.drawItem(i == 0 ? target.getMainHandStack() : target.getOffHandStack(), 0, 0);
            context.getMatrices().pop();
            FontRenderers.settings
               .drawString(
                  context.getMatrices(),
                  "x" + (i == 0 ? target.getMainHandStack() : target.getOffHandStack()).getCount(),
                  (double)(posX + (float)(i == 0 ? 50 : 77) + 12.0F),
                  (double)(posY + 21.0F),
                  -1
               );
         }
      }
   }

   private void drawPotionEffect(MatrixStack ms, PlayerEntity entity) {
      StringBuilder finalString = new StringBuilder();

      for (StatusEffectInstance potionEffect : entity.getStatusEffects()) {
         StatusEffect potion = (StatusEffect)potionEffect.getEffectType().value();
         if (potion == StatusEffects.REGENERATION.value()
            || potion == StatusEffects.SPEED.value()
            || potion == StatusEffects.STRENGTH.value()
            || potion == StatusEffects.WEAKNESS.value()) {
            boolean potRanOut = (double)potionEffect.getDuration() != 0.0;
            if (entity.hasStatusEffect(potionEffect.getEffectType()) && potRanOut) {
               finalString.append(getPotionName(potion))
                  .append(potionEffect.getAmplifier() < 1 ? "" : potionEffect.getAmplifier() + 1)
                  .append(" ")
                  .append(this.getDurationString(potionEffect))
                  .append(" ");
            }
         }
      }

      FontRenderers.settings
         .drawString(ms, finalString.toString(), (double)(this.getPosX() + 55.0F), (double)(this.getPosY() + 15.0F), new Color(9276813).getRGB());
   }

   public float getHealth() {
      if (this.target instanceof PlayerEntity ent
         && (
            mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null && mc.getNetworkHandler().getServerInfo().address.contains("funtime")
               || this.funTimeHP.getValue()
         )) {
         ScoreboardObjective scoreBoard = null;
         String resolvedHp = "";
         if (ent.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
            scoreBoard = ent.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
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

   public static void sizeAnimation(MatrixStack matrixStack, double width, double height, double animation) {
      matrixStack.translate(width, height, 0.0);
      matrixStack.scale((float)animation, (float)animation, 1.0F);
      matrixStack.translate(-width, -height, 0.0);
   }

   public static String getPotionName(StatusEffect p) {
      if (p == StatusEffects.REGENERATION.value()) {
         return "Reg";
      } else if (p == StatusEffects.STRENGTH.value()) {
         return "Str";
      } else if (p == StatusEffects.SPEED.value()) {
         return "Spd";
      } else if (p == StatusEffects.HASTE.value()) {
         return "H";
      } else if (p == StatusEffects.WEAKNESS.value()) {
         return "W";
      } else {
         return p == StatusEffects.RESISTANCE.value() ? "Res" : "pon";
      }
   }

   public static enum HPmodeEn {
      HP,
      Percentage;
   }

   public static enum ImageModeEn {
      None,
      Anime,
      Custom;
   }

   public static enum ModeEn {
      ThunderHack,
      NurikZapen,
      CelkaPasta;
   }
}
