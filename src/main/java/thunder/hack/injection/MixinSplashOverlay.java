package thunder.hack.injection;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.resource.ResourceReload;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

@Mixin({SplashOverlay.class})
public abstract class MixinSplashOverlay {
   @Final
   @Shadow
   private boolean reloading;
   @Shadow
   private float progress;
   @Shadow
   private long reloadCompleteTime = -1L;
   @Shadow
   private long reloadStartTime = -1L;
   @Final
   @Shadow
   private ResourceReload reload;
   @Final
   @Shadow
   private Consumer<Optional<Throwable>> exceptionHandler;

   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (!ModuleManager.unHook.isEnabled() && ClientSettings.customLoadingScreen.getValue()) {
         ci.cancel();
         this.renderCustom(context, mouseX, mouseY, delta);
      }
   }

   public void renderCustom(DrawContext context, int mouseX, int mouseY, float delta) {
      int i = Module.mc.getWindow().getScaledWidth();
      int j = Module.mc.getWindow().getScaledHeight();
      long l = Util.getMeasuringTimeMs();
      if (this.reloading && this.reloadStartTime == -1L) {
         this.reloadStartTime = l;
      }

      float f = this.reloadCompleteTime > -1L ? (float)(l - this.reloadCompleteTime) / 1000.0F : -1.0F;
      float g = this.reloadStartTime > -1L ? (float)(l - this.reloadStartTime) / 500.0F : -1.0F;
      float h;
      if (f >= 1.0F) {
         if (Module.mc.currentScreen != null) {
            Module.mc.currentScreen.render(context, 0, 0, delta);
         }

         int k = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
         context.fill(0, 0, i, j, withAlpha(new Color(458773).getRGB(), k));
         h = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
      } else if (this.reloading) {
         if (Module.mc.currentScreen != null && g < 1.0F) {
            Module.mc.currentScreen.render(context, mouseX, mouseY, delta);
         }

         int k = MathHelper.ceil(MathHelper.clamp((double)g, 0.15, 1.0) * 255.0);
         context.fill(0, 0, i, j, withAlpha(new Color(458773).getRGB(), k));
         h = MathHelper.clamp(g, 0.0F, 1.0F);
      } else {
         int k = new Color(458773).getRGB();
         float m = (float)(k >> 16 & 0xFF) / 255.0F;
         float n = (float)(k >> 8 & 0xFF) / 255.0F;
         float o = (float)(k & 0xFF) / 255.0F;
         GlStateManager._clearColor(m, n, o, 1.0F);
         GlStateManager._clear(16384, MinecraftClient.IS_SYSTEM_MAC);
         h = 1.0F;
      }

      int var19 = (int)((double)context.getScaledWindowWidth() * 0.5);
      int p = (int)((double)context.getScaledWindowHeight() * 0.5);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.setShaderColor(0.1F, 0.1F, 0.1F, h);
      context.drawTexture(TextureStorage.thLogo, var19 - 150, p - 35, 0.0F, 0.0F, 300, 70, 300, 70);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, h);
      Render2DEngine.addWindow(
         context.getMatrices(), (float)(var19 - 150), (float)(p - 35), (float)(var19 - 150) + 300.0F * this.progress, (float)(p + 35), 1.0
      );
      context.drawTexture(TextureStorage.thLogo, var19 - 150, p - 35, 0.0F, 0.0F, 300, 70, 300, 70);
      Render2DEngine.popWindow();
      float t = this.reload.getProgress();
      this.progress = MathHelper.clamp(this.progress * 0.95F + t * 0.050000012F, 0.0F, 1.0F);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      if (f >= 2.0F) {
         Module.mc.setOverlay(null);
      }

      if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || g >= 2.0F)) {
         try {
            this.reload.throwException();
            this.exceptionHandler.accept(Optional.empty());
         } catch (Throwable var16) {
            this.exceptionHandler.accept(Optional.of(var16));
         }

         this.reloadCompleteTime = Util.getMeasuringTimeMs();
         if (Module.mc.currentScreen != null) {
            Module.mc.currentScreen.init(Module.mc, Module.mc.getWindow().getScaledWidth(), Module.mc.getWindow().getScaledHeight());
         }
      }
   }

   private static int withAlpha(int color, int alpha) {
      return color & 16777215 | alpha << 24;
   }
}
