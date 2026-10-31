package thunder.hack.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class HitParticles extends Module {
   private final Setting<HitParticles.Mode> mode = new Setting<>("Mode", HitParticles.Mode.Stars);
   private final Setting<HitParticles.Physics> physics = new Setting<>("Physics", HitParticles.Physics.Fall);
   private final Setting<ColorSetting> colorrr = new Setting<>("Color", new ColorSetting(-2013200640));
   private final Setting<Boolean> onlySelf = new Setting<>("Self", false);
   private final Setting<Integer> amount = new Setting<>("Amount", 2, 1, 5);
   private final Setting<Integer> lifeTime = new Setting<>("LifeTime", 2, 1, 10);
   private final Setting<Integer> speed = new Setting<>("Speed", 2, 1, 20);
   private final Setting<Float> starsScale = new Setting<>("Scale", 3.0F, 1.0F, 10.0F, v -> this.mode.getValue() != HitParticles.Mode.Orbiz);
   private final Setting<HitParticles.ColorMode> colorMode = new Setting<>("ColorMode", HitParticles.ColorMode.Sync);
   private final Setting<ColorSetting> colorH = new Setting<>("HealColor", new ColorSetting(3142544), v -> this.mode.is(HitParticles.Mode.Text));
   private final Setting<ColorSetting> colorD = new Setting<>("DamageColor", new ColorSetting(15811379), v -> this.mode.is(HitParticles.Mode.Text));
   private final HashMap<Integer, Float> healthMap = new HashMap<>();
   private final CopyOnWriteArrayList<HitParticles.Particle> particles = new CopyOnWriteArrayList<>();

   public HitParticles() {
      super("HitParticles", Module.Category.RENDER);
   }

   @Override
   public void onUpdate() {
      this.particles.removeIf(HitParticles.Particle::update);
      if (this.mode.is(HitParticles.Mode.Text)) {
         for (Entity entity : mc.world.getEntities()) {
            if (entity != null && !(mc.player.squaredDistanceTo(entity) > 256.0) && entity.isAlive() && entity instanceof LivingEntity) {
               LivingEntity lent = (LivingEntity)entity;
               Color c = this.colorMode.getValue() == HitParticles.ColorMode.Sync
                  ? HudEditor.getColor((int)MathUtility.random(1.0F, 228.0F))
                  : this.colorrr.getValue().getColorObject();
               float health = lent.getHealth() + lent.getAbsorptionAmount();
               float lastHealth = this.healthMap.getOrDefault(entity.getId(), health);
               this.healthMap.put(entity.getId(), health);
               if (lastHealth != health) {
                  this.particles
                     .add(
                        new HitParticles.Particle(
                           (float)lent.getX(),
                           MathUtility.random((float)(lent.getY() + (double)lent.getHeight()), (float)lent.getY()),
                           (float)lent.getZ(),
                           c,
                           MathUtility.random(0.0F, 180.0F),
                           MathUtility.random(10.0F, 60.0F),
                           health - lastHealth
                        )
                     );
               }
            }
         }
      } else {
         for (PlayerEntity player : mc.world.getPlayers()) {
            if ((!this.onlySelf.getValue() || player == mc.player) && player.hurtTime > 0) {
               Color c = this.colorMode.getValue() == HitParticles.ColorMode.Sync
                  ? HudEditor.getColor((int)MathUtility.random(1.0F, 228.0F))
                  : this.colorrr.getValue().getColorObject();

               for (int i = 0; i < this.amount.getValue(); i++) {
                  this.particles
                     .add(
                        new HitParticles.Particle(
                           (float)player.getX(),
                           MathUtility.random((float)(player.getY() + (double)player.getHeight()), (float)player.getY()),
                           (float)player.getZ(),
                           c,
                           MathUtility.random(0.0F, 180.0F),
                           MathUtility.random(10.0F, 60.0F),
                           0.0F
                        )
                     );
               }
            }
         }
      }
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      RenderSystem.disableDepthTest();
      if (mc.player != null && mc.world != null) {
         for (HitParticles.Particle particle : this.particles) {
            particle.render(stack);
         }
      }

      RenderSystem.enableDepthTest();
   }

   public static enum ColorMode {
      Custom,
      Sync;
   }

   public static enum Mode {
      Orbiz,
      Stars,
      Hearts,
      Bloom,
      Text;
   }

   public class Particle {
      float x;
      float y;
      float z;
      float px;
      float py;
      float pz;
      float motionX;
      float motionY;
      float motionZ;
      float rotationAngle;
      float rotationSpeed;
      float health;
      long time;
      Color color;

      public Particle(float x, float y, float z, Color color, float rotationAngle, float rotationSpeed, float health) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.px = x;
         this.py = y;
         this.pz = z;
         this.motionX = MathUtility.random(
            -((float)HitParticles.this.speed.getValue().intValue()) / 50.0F, (float)HitParticles.this.speed.getValue().intValue() / 50.0F
         );
         this.motionY = MathUtility.random(
            -((float)HitParticles.this.speed.getValue().intValue()) / 50.0F, (float)HitParticles.this.speed.getValue().intValue() / 50.0F
         );
         this.motionZ = MathUtility.random(
            -((float)HitParticles.this.speed.getValue().intValue()) / 50.0F, (float)HitParticles.this.speed.getValue().intValue() / 50.0F
         );
         this.time = System.currentTimeMillis();
         this.color = color;
         this.rotationAngle = rotationAngle;
         this.rotationSpeed = rotationSpeed;
         this.health = health;
      }

      public long getTime() {
         return this.time;
      }

      public boolean update() {
         double sp = Math.sqrt((double)(this.motionX * this.motionX + this.motionZ * this.motionZ));
         this.px = this.x;
         this.py = this.y;
         this.pz = this.z;
         this.x = this.x + this.motionX;
         this.y = this.y + this.motionY;
         this.z = this.z + this.motionZ;
         if (this.posBlock((double)this.x, (double)(this.y - HitParticles.this.starsScale.getValue() / 10.0F), (double)this.z)) {
            this.motionY = -this.motionY / 1.1F;
            this.motionX /= 1.1F;
            this.motionZ /= 1.1F;
         } else if (this.posBlock((double)this.x - sp, (double)this.y, (double)this.z - sp)
            || this.posBlock((double)this.x + sp, (double)this.y, (double)this.z + sp)
            || this.posBlock((double)this.x + sp, (double)this.y, (double)this.z - sp)
            || this.posBlock((double)this.x - sp, (double)this.y, (double)this.z + sp)
            || this.posBlock((double)this.x + sp, (double)this.y, (double)this.z)
            || this.posBlock((double)this.x - sp, (double)this.y, (double)this.z)
            || this.posBlock((double)this.x, (double)this.y, (double)this.z + sp)
            || this.posBlock((double)this.x, (double)this.y, (double)this.z - sp)) {
            this.motionX = -this.motionX;
            this.motionZ = -this.motionZ;
         }

         if (HitParticles.this.physics.getValue() == HitParticles.Physics.Fall) {
            this.motionY -= 0.035F;
         }

         this.motionX /= 1.005F;
         this.motionZ /= 1.005F;
         this.motionY /= 1.005F;
         return System.currentTimeMillis() - this.getTime() > (long)(HitParticles.this.lifeTime.getValue() * 1000);
      }

      public void render(MatrixStack matrixStack) {
         float size = HitParticles.this.starsScale.getValue();
         float scale = HitParticles.this.mode.is(HitParticles.Mode.Text) ? 0.025F * size : 0.07F;
         double posX = Render2DEngine.interpolate((double)this.px, (double)this.x, (double)Render3DEngine.getTickDelta())
            - Module.mc.getEntityRenderDispatcher().camera.getPos().getX();
         double posY = Render2DEngine.interpolate((double)this.py, (double)this.y, (double)Render3DEngine.getTickDelta())
            + 0.1
            - Module.mc.getEntityRenderDispatcher().camera.getPos().getY();
         double posZ = Render2DEngine.interpolate((double)this.pz, (double)this.z, (double)Render3DEngine.getTickDelta())
            - Module.mc.getEntityRenderDispatcher().camera.getPos().getZ();
         matrixStack.push();
         matrixStack.translate(posX, posY, posZ);
         matrixStack.scale(scale, scale, scale);
         matrixStack.translate(size / 2.0F, size / 2.0F, size / 2.0F);
         matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-Module.mc.gameRenderer.getCamera().getYaw()));
         matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Module.mc.gameRenderer.getCamera().getPitch()));
         if (HitParticles.this.mode.is(HitParticles.Mode.Text)) {
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
         } else {
            matrixStack.multiply(
               RotationAxis.POSITIVE_Z.rotationDegrees(this.rotationAngle = this.rotationAngle + AnimationUtility.deltaTime() * this.rotationSpeed)
            );
         }

         matrixStack.translate(-size / 2.0F, -size / 2.0F, -size / 2.0F);
         switch ((HitParticles.Mode)HitParticles.this.mode.getValue()) {
            case Orbiz:
               Render2DEngine.drawOrbiz(matrixStack, 0.0F, 0.3, this.color);
               Render2DEngine.drawOrbiz(matrixStack, -0.1F, 0.5, this.color);
               Render2DEngine.drawOrbiz(matrixStack, -0.2F, 0.7, this.color);
               break;
            case Stars:
               Render2DEngine.drawStar(matrixStack, this.color, size);
               break;
            case Hearts:
               Render2DEngine.drawHeart(matrixStack, this.color, size);
               break;
            case Bloom:
               Render2DEngine.drawBloom(matrixStack, this.color, size);
               break;
            case Text:
               FontRenderers.sf_medium
                  .drawCenteredString(
                     matrixStack,
                     MathUtility.round2((double)this.health) + " ",
                     0.0,
                     0.0,
                     (this.health > 0.0F ? HitParticles.this.colorH.getValue() : HitParticles.this.colorD.getValue()).getColorObject()
                  );
         }

         matrixStack.scale(0.8F, 0.8F, 0.8F);
         matrixStack.pop();
      }

      private boolean posBlock(double x, double y, double z) {
         Block b = Module.mc.world.getBlockState(BlockPos.ofFloored(x, y, z)).getBlock();
         return !(b instanceof AirBlock) && b != Blocks.WATER && b != Blocks.LAVA;
      }
   }

   public static enum Physics {
      Fall,
      Fly;
   }
}
