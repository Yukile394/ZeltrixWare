package thunder.hack.utility.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import thunder.hack.features.modules.Module;
import thunder.hack.utility.render.animation.AnimationUtility;
import thunder.hack.utility.render.shaders.satin.api.managed.ManagedCoreShader;
import thunder.hack.utility.render.shaders.satin.api.managed.ShaderEffectManager;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform1f;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform2f;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.Uniform4f;

public class SkyShaderProgram {
   private Uniform1f Time;
   private Uniform2f uSize;
   private Uniform4f color1;
   private Uniform4f color2;
   private Uniform2f uRotation;
   public static float time_ = 10000.0F;
   public static final ManagedCoreShader SKY_SHADER = ShaderEffectManager.getInstance()
      .manageCoreShader(Identifier.of("thunderhack", "skyshader"), VertexFormats.POSITION);

   public SkyShaderProgram() {
      this.setup();
   }

   public void setParameters(float width, float height, float yaw, float pitch, Color c1, Color c2) {
      float i = (float)Module.mc.getWindow().getScaleFactor();
      this.uSize.set(width * i, height * i);
      time_ = time_ + (float)(0.55 * (double)AnimationUtility.deltaTime());
      this.Time.set(time_);
      float rotX = yaw / 360.0F;
      float rotY = pitch / 360.0F;
      this.uRotation.set(rotX, rotY);
      this.color1.set((float)c1.getRed() / 255.0F, (float)c1.getGreen() / 255.0F, (float)c1.getBlue() / 255.0F, (float)c1.getAlpha() / 255.0F);
      this.color2.set((float)c2.getRed() / 255.0F, (float)c2.getGreen() / 255.0F, (float)c2.getBlue() / 255.0F, (float)c2.getAlpha() / 255.0F);
   }

   public void use() {
      RenderSystem.setShader(SKY_SHADER::getProgram);
   }

   protected void setup() {
      this.uSize = SKY_SHADER.findUniform2f("uSize");
      this.Time = SKY_SHADER.findUniform1f("Time");
      this.color1 = SKY_SHADER.findUniform4f("color1");
      this.color2 = SKY_SHADER.findUniform4f("color2");
      this.uRotation = SKY_SHADER.findUniform2f("uRotation");
   }
}
