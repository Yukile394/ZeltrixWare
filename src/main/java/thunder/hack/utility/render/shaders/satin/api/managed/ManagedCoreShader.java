package thunder.hack.utility.render.shaders.satin.api.managed;

import net.minecraft.client.gl.ShaderProgram;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.UniformFinder;

public interface ManagedCoreShader extends UniformFinder {
   ShaderProgram getProgram();

   void release();
}
