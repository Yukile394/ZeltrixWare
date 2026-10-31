package thunder.hack.core.manager.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.manager.IManager;
import thunder.hack.utility.interfaces.IShaderEffect;
import thunder.hack.utility.render.shaders.satin.api.managed.ManagedShaderEffect;
import thunder.hack.utility.render.shaders.satin.api.managed.ShaderEffectManager;

public class ShaderManager implements IManager {
   private static final List<ShaderManager.RenderTask> tasks = new ArrayList<>();
   private ShaderManager.ThunderHackFramebuffer shaderBuffer;
   public float time = 0.0F;
   public static ManagedShaderEffect DEFAULT_OUTLINE;
   public static ManagedShaderEffect SMOKE_OUTLINE;
   public static ManagedShaderEffect GRADIENT_OUTLINE;
   public static ManagedShaderEffect SNOW_OUTLINE;
   public static ManagedShaderEffect FADE_OUTLINE;
   public static ManagedShaderEffect DEFAULT;
   public static ManagedShaderEffect SMOKE;
   public static ManagedShaderEffect GRADIENT;
   public static ManagedShaderEffect SNOW;
   public static ManagedShaderEffect FADE;

   public void renderShader(Runnable runnable, ShaderManager.Shader mode) {
      tasks.add(new ShaderManager.RenderTask(runnable, mode));
   }

   public void renderShaders() {
      if (DEFAULT == null) {
         this.shaderBuffer = new ShaderManager.ThunderHackFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
         this.reloadShaders();
      }

      if (this.shaderBuffer != null) {
         tasks.forEach(t -> this.applyShader(t.task(), t.shader()));
         tasks.clear();
      }
   }

   public void applyShader(Runnable runnable, ShaderManager.Shader mode) {
      Framebuffer MCBuffer = MinecraftClient.getInstance().getFramebuffer();
      RenderSystem.assertOnRenderThreadOrInit();
      if (this.shaderBuffer.textureWidth != MCBuffer.textureWidth || this.shaderBuffer.textureHeight != MCBuffer.textureHeight) {
         this.shaderBuffer.resize(MCBuffer.textureWidth, MCBuffer.textureHeight, false);
      }

      GlStateManager._glBindFramebuffer(36009, this.shaderBuffer.fbo);
      this.shaderBuffer.beginWrite(true);
      runnable.run();
      this.shaderBuffer.endWrite();
      GlStateManager._glBindFramebuffer(36009, MCBuffer.fbo);
      MCBuffer.beginWrite(false);
      ManagedShaderEffect shader = this.getShader(mode);
      Framebuffer mainBuffer = MinecraftClient.getInstance().getFramebuffer();
      PostEffectProcessor effect = shader.getShaderEffect();
      if (effect != null) {
         ((IShaderEffect)effect).addFakeTargetHook("bufIn", this.shaderBuffer);
      }

      Framebuffer outBuffer = shader.getShaderEffect().getSecondaryTarget("bufOut");
      this.setupShader(mode, shader);
      this.shaderBuffer.clear(false);
      mainBuffer.beginWrite(false);
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
      RenderSystem.backupProjectionMatrix();
      outBuffer.draw(outBuffer.textureWidth, outBuffer.textureHeight, false);
      RenderSystem.restoreProjectionMatrix();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
   }

   public ManagedShaderEffect getShader(@NotNull ShaderManager.Shader mode) {
      return switch (mode) {
         case Smoke -> SMOKE;
         case Gradient -> GRADIENT;
         case Snow -> SNOW;
         case Fade -> FADE;
         default -> DEFAULT;
      };
   }

   public ManagedShaderEffect getShaderOutline(@NotNull ShaderManager.Shader mode) {
      return switch (mode) {
         case Smoke -> SMOKE_OUTLINE;
         case Gradient -> GRADIENT_OUTLINE;
         case Snow -> SNOW_OUTLINE;
         case Fade -> FADE_OUTLINE;
         default -> DEFAULT_OUTLINE;
      };
   }

   public void setupShader(ShaderManager.Shader shader, ManagedShaderEffect effect) {
   }

   public void reloadShaders() {
      DEFAULT = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/outline.json"));
      SMOKE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/smoke.json"));
      GRADIENT = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/gradient.json"));
      SNOW = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/snow.json"));
      FADE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/fade.json"));
      FADE_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/fade.json"), managedShaderEffect -> {
         PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
         if (effect != null) {
            ((IShaderEffect)effect).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect)effect).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
         }
      });
      DEFAULT_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/outline.json"), managedShaderEffect -> {
         PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
         if (effect != null) {
            ((IShaderEffect)effect).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect)effect).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
         }
      });
      SMOKE_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/smoke.json"), managedShaderEffect -> {
         PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
         if (effect != null) {
            ((IShaderEffect)effect).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect)effect).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
         }
      });
      GRADIENT_OUTLINE = ShaderEffectManager.getInstance()
         .manage(Identifier.of("thunderhack", "shaders/post/gradient.json"), managedShaderEffect -> {
            PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
            if (effect != null) {
               ((IShaderEffect)effect).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
               ((IShaderEffect)effect).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
            }
         });
      SNOW_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/snow.json"), managedShaderEffect -> {
         PostEffectProcessor effect = managedShaderEffect.getShaderEffect();
         if (effect != null) {
            ((IShaderEffect)effect).addFakeTargetHook("bufIn", mc.worldRenderer.getEntityOutlinesFramebuffer());
            ((IShaderEffect)effect).addFakeTargetHook("bufOut", mc.worldRenderer.getEntityOutlinesFramebuffer());
         }
      });
   }

   public boolean fullNullCheck() {
      if (GRADIENT != null && SMOKE != null && DEFAULT != null) {
         return false;
      } else {
         this.shaderBuffer = new ShaderManager.ThunderHackFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
         this.reloadShaders();
         return true;
      }
   }

   public static record RenderTask(Runnable task, ShaderManager.Shader shader) {
   }

   public static enum Shader {
      Default,
      Smoke,
      Gradient,
      Snow,
      Fade;
   }

   public static class ThunderHackFramebuffer extends Framebuffer {
      public ThunderHackFramebuffer(int width, int height) {
         super(false);
         RenderSystem.assertOnRenderThreadOrInit();
         this.resize(width, height, true);
         this.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      }
   }
}
