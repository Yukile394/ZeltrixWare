package thunder.hack.utility.render.shaders.satin.api.managed.uniform;

import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.gl.Framebuffer;

public interface SamplerUniform {
   void set(AbstractTexture var1);

   void set(Framebuffer var1);

   void set(int var1);
}
