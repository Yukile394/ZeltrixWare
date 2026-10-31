package thunder.hack.features.modules.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

public class Particles extends Module {
   private final Setting<BooleanSettingGroup> FireFlies = new Setting<>("FireFlies", new BooleanSettingGroup(true));
   private final Setting<Integer> ffcount = new Setting<>("FFCount", 30, 20, 200).addToGroup(this.FireFlies);
   private final Setting<Float> ffsize = new Setting<>("FFSize", 1.0F, 0.1F, 2.0F).addToGroup(this.FireFlies);
   private final Setting<Particles.Mode> mode = new Setting<>("Mode", Particles.Mode.SnowFlake);
   private final Setting<Integer> count = new Setting<>("Count", 100, 20, 800);
   private final Setting<Float> size = new Setting<>("Size", 1.0F, 0.1F, 6.0F);
   private final Setting<Particles.ColorMode> lmode = new Setting<>("ColorMode", Particles.ColorMode.Sync);
   private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(3649978), v -> this.lmode.getValue() == Particles.ColorMode.Custom);
   private final Setting<Particles.Physics> physics = new Setting<>("Physics", Particles.Physics.Fly, v -> this.mode.getValue() != Particles.Mode.Off);
   private final ArrayList<Particles.ParticleBase> fireFlies = new ArrayList<>();
   private final ArrayList<Particles.ParticleBase> particles = new ArrayList<>();

   public Particles() {
      super("Particles", Module.Category.RENDER);
   }

   @Override
   public void onUpdate() {
      this.fireFlies.removeIf(Particles.ParticleBase::tick);
      this.particles.removeIf(Particles.ParticleBase::tick);

      for (int i = this.fireFlies.size(); i < this.ffcount.getValue(); i++) {
         if (this.FireFlies.getValue().isEnabled()) {
            this.fireFlies
               .add(
                  new Particles.FireFly(
                     (float)(mc.player.getX() + (double)MathUtility.random(-25.0F, 25.0F)),
                     (float)(mc.player.getY() + (double)MathUtility.random(2.0F, 15.0F)),
                     (float)(mc.player.getZ() + (double)MathUtility.random(-25.0F, 25.0F)),
                     MathUtility.random(-0.2F, 0.2F),
                     MathUtility.random(-0.1F, 0.1F),
                     MathUtility.random(-0.2F, 0.2F)
                  )
               );
         }
      }

      for (int j = this.particles.size(); j < this.count.getValue(); j++) {
         boolean drop = this.physics.getValue() == Particles.Physics.Drop;
         if (this.mode.getValue() != Particles.Mode.Off) {
            this.particles
               .add(
                  new Particles.ParticleBase(
                     (float)(mc.player.getX() + (double)MathUtility.random(-48.0F, 48.0F)),
                     (float)(mc.player.getY() + (double)MathUtility.random(2.0F, 48.0F)),
                     (float)(mc.player.getZ() + (double)MathUtility.random(-48.0F, 48.0F)),
                     drop ? 0.0F : MathUtility.random(-0.4F, 0.4F),
                     drop ? MathUtility.random(-0.2F, -0.05F) : MathUtility.random(-0.1F, 0.1F),
                     drop ? 0.0F : MathUtility.random(-0.4F, 0.4F)
                  )
               );
         }
      }
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      if (this.FireFlies.getValue().isEnabled()) {
         stack.push();
         RenderSystem.setShaderTexture(0, TextureStorage.firefly);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
         BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         this.fireFlies.forEach(p -> p.render(bufferBuilder));
         Render2DEngine.endBuilding(bufferBuilder);
         RenderSystem.depthMask(true);
         RenderSystem.disableDepthTest();
         RenderSystem.disableBlend();
         stack.pop();
      }

      if (this.mode.getValue() != Particles.Mode.Off) {
         stack.push();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
         BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         this.particles.forEach(p -> p.render(bufferBuilder));
         Render2DEngine.endBuilding(bufferBuilder);
         RenderSystem.depthMask(true);
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
         RenderSystem.disableDepthTest();
         RenderSystem.disableBlend();
         stack.pop();
      }
   }

   public static enum ColorMode {
      Custom,
      Sync;
   }

   public class FireFly extends Particles.ParticleBase {
      public FireFly(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
         super(posX, posY, posZ, motionX, motionY, motionZ);
      }

      @Override
      public boolean tick() {
         if (Module.mc.player.squaredDistanceTo((double)this.posX, (double)this.posY, (double)this.posZ) > 100.0) {
            this.age -= 4;
         } else if (!Module.mc.world.getBlockState(new BlockPos((int)this.posX, (int)this.posY, (int)this.posZ)).isAir()) {
            this.age -= 8;
         } else {
            this.age--;
         }

         if (this.age < 0) {
            return true;
         } else {
            this.prevposX = this.posX;
            this.prevposY = this.posY;
            this.prevposZ = this.posZ;
            this.posX = this.posX + this.motionX;
            this.posY = this.posY + this.motionY;
            this.posZ = this.posZ + this.motionZ;
            this.motionX *= 0.99F;
            this.motionY *= 0.99F;
            this.motionZ *= 0.99F;
            return false;
         }
      }
   }

   public static enum Mode {
      Off,
      SnowFlake,
      Stars,
      Hearts,
      Dollars,
      Bloom;
   }

   public class ParticleBase {
      protected float prevposX;
      protected float prevposY;
      protected float prevposZ;
      protected float posX;
      protected float posY;
      protected float posZ;
      protected float motionX;
      protected float motionY;
      protected float motionZ;
      protected int age;
      protected int maxAge;

      public ParticleBase(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
         this.posX = posX;
         this.posY = posY;
         this.posZ = posZ;
         this.prevposX = posX;
         this.prevposY = posY;
         this.prevposZ = posZ;
         this.motionX = motionX;
         this.motionY = motionY;
         this.motionZ = motionZ;
         this.age = (int)MathUtility.random(100.0F, 300.0F);
         this.maxAge = this.age;
      }

      public boolean tick() {
         if (Module.mc.player.squaredDistanceTo((double)this.posX, (double)this.posY, (double)this.posZ) > 4096.0) {
            this.age -= 8;
         } else {
            this.age--;
         }

         if (this.age < 0) {
            return true;
         } else {
            this.prevposX = this.posX;
            this.prevposY = this.posY;
            this.prevposZ = this.posZ;
            this.posX = this.posX + this.motionX;
            this.posY = this.posY + this.motionY;
            this.posZ = this.posZ + this.motionZ;
            this.motionX *= 0.9F;
            if (Particles.this.physics.getValue() == Particles.Physics.Fly) {
               this.motionY *= 0.9F;
            }

            this.motionZ *= 0.9F;
            this.motionY -= 0.001F;
            return false;
         }
      }

      public void render(BufferBuilder bufferBuilder) {
         switch ((Particles.Mode)Particles.this.mode.getValue()) {
            case SnowFlake:
               RenderSystem.setShaderTexture(0, TextureStorage.snowflake);
               break;
            case Stars:
               RenderSystem.setShaderTexture(0, TextureStorage.star);
               break;
            case Hearts:
               RenderSystem.setShaderTexture(0, TextureStorage.heart);
               break;
            case Dollars:
               RenderSystem.setShaderTexture(0, TextureStorage.dollar);
               break;
            case Bloom:
               RenderSystem.setShaderTexture(0, TextureStorage.firefly);
         }

         Camera camera = Module.mc.gameRenderer.getCamera();
         Color color1 = Particles.this.lmode.getValue() == Particles.ColorMode.Sync
            ? HudEditor.getColor(this.age * 2)
            : Particles.this.color.getValue().getColorObject();
         Vec3d pos = Render3DEngine.interpolatePos(this.prevposX, this.prevposY, this.prevposZ, this.posX, this.posY, this.posZ);
         MatrixStack matrices = new MatrixStack();
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
         matrices.translate(pos.x, pos.y, pos.z);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
         Matrix4f matrix1 = matrices.peek().getPositionMatrix();
         bufferBuilder.vertex(matrix1, 0.0F, -Particles.this.size.getValue(), 0.0F)
            .texture(0.0F, 1.0F)
            .color(Render2DEngine.injectAlpha(color1, (int)(255.0F * ((float)this.age / (float)this.maxAge))).getRGB());
         bufferBuilder.vertex(matrix1, -Particles.this.size.getValue(), -Particles.this.size.getValue(), 0.0F)
            .texture(1.0F, 1.0F)
            .color(Render2DEngine.injectAlpha(color1, (int)(255.0F * ((float)this.age / (float)this.maxAge))).getRGB());
         bufferBuilder.vertex(matrix1, -Particles.this.size.getValue(), 0.0F, 0.0F)
            .texture(1.0F, 0.0F)
            .color(Render2DEngine.injectAlpha(color1, (int)(255.0F * ((float)this.age / (float)this.maxAge))).getRGB());
         bufferBuilder.vertex(matrix1, 0.0F, 0.0F, 0.0F)
            .texture(0.0F, 0.0F)
            .color(Render2DEngine.injectAlpha(color1, (int)(255.0F * ((float)this.age / (float)this.maxAge))).getRGB());
      }
   }

   public static enum Physics {
      Drop,
      Fly;
   }
}
