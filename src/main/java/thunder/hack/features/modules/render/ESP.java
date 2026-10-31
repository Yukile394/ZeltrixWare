package thunder.hack.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.injection.accesors.IAreaEffectCloudEntity;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

public class ESP extends Module {
   private final Setting<Boolean> lingeringPotions = new Setting<>("LingeringPotions", false);
   private final Setting<Boolean> tntFuse = new Setting<>("TNTFuse", false);
   private final Setting<Float> tntrange = new Setting<>("TNTRange", 8.0F, 0.0F, 8.0F);
   private final Setting<ColorSetting> tntFuseText = new Setting<>("TNTFuseText", new ColorSetting(new Color(-1)), v -> this.tntFuse.getValue());
   private final Setting<Boolean> tntRadius = new Setting<>("TNTRadius", false);
   private final Setting<ColorSetting> tntRadiusColor = new Setting<>("TNTSphereColor", new ColorSetting(new Color(-1)), v -> this.tntRadius.getValue());
   private final Setting<Boolean> beaconRadius = new Setting<>("BeaconRadius", false);
   private final Setting<Boolean> keepY = new Setting<>("KeepY", false, v -> this.beaconRadius.getValue());
   private final Setting<ColorSetting> sphereColor = new Setting<>("SphereColor", new ColorSetting(new Color(-1)), v -> this.beaconRadius.getValue());
   private final Setting<ColorSetting> beakonColor = new Setting<>("BeakonColor", new ColorSetting(new Color(-1)), v -> this.beaconRadius.getValue());
   private final Setting<Boolean> burrow = new Setting<>("Burrow", false);
   private final Setting<ColorSetting> burrowTextColor = new Setting<>("BurrowTextColor", new ColorSetting(new Color(-1)), v -> this.burrow.getValue());
   private final Setting<ColorSetting> burrowColor = new Setting<>("BurrowColor", new ColorSetting(new Color(-1)), v -> this.burrow.getValue());
   private final Setting<Boolean> pearls = new Setting<>("Pearls", false);
   private final Setting<Boolean> dizorentRadius = new Setting<>("DizorentRadius", true);
   private final Setting<ColorSetting> dizorentColor = new Setting<>(
      "DizorentColor", new ColorSetting(new Color(-1291783732, true)), v -> this.dizorentRadius.getValue()
   );
   private final Setting<SettingGroup> boxEsp = new Setting<>("Box", new SettingGroup(false, 0));
   private final Setting<Boolean> players = new Setting<>("Players", true).addToGroup(this.boxEsp);
   private final Setting<Boolean> friends = new Setting<>("Friends", true).addToGroup(this.boxEsp);
   private final Setting<Boolean> crystals = new Setting<>("Crystals", true).addToGroup(this.boxEsp);
   private final Setting<Boolean> creatures = new Setting<>("Creatures", false).addToGroup(this.boxEsp);
   private final Setting<Boolean> monsters = new Setting<>("Monsters", false).addToGroup(this.boxEsp);
   private final Setting<Boolean> ambients = new Setting<>("Ambients", false).addToGroup(this.boxEsp);
   private final Setting<Boolean> others = new Setting<>("Others", false).addToGroup(this.boxEsp);
   private final Setting<Boolean> outline = new Setting<>("Outline", true).addToGroup(this.boxEsp);
   private final Setting<ESP.Colors> colorMode = new Setting<>("ColorMode", ESP.Colors.SyncColor).addToGroup(this.boxEsp);
   private final Setting<Boolean> renderHealth = new Setting<>("renderHealth", true).addToGroup(this.boxEsp);
   private final Setting<SettingGroup> boxColors = new Setting<>("BoxColors", new SettingGroup(false, 0));
   private final Setting<ColorSetting> playersC = new Setting<>("PlayersC", new ColorSetting(new Color(16749056))).addToGroup(this.boxColors);
   private final Setting<ColorSetting> friendsC = new Setting<>("FriendsC", new ColorSetting(new Color(3211008))).addToGroup(this.boxColors);
   private final Setting<ColorSetting> crystalsC = new Setting<>("CrystalsC", new ColorSetting(new Color(48127))).addToGroup(this.boxColors);
   private final Setting<ColorSetting> creaturesC = new Setting<>("CreaturesC", new ColorSetting(new Color(10527910))).addToGroup(this.boxColors);
   private final Setting<ColorSetting> monstersC = new Setting<>("MonstersC", new ColorSetting(new Color(16711680))).addToGroup(this.boxColors);
   private final Setting<ColorSetting> ambientsC = new Setting<>("AmbientsC", new ColorSetting(new Color(8061183))).addToGroup(this.boxColors);
   private final Setting<ColorSetting> othersC = new Setting<>("OthersC", new ColorSetting(new Color(16711778))).addToGroup(this.boxColors);
   public final Setting<ColorSetting> healthB = new Setting<>("healthB", new ColorSetting(new Color(16716032))).addToGroup(this.boxColors);
   public final Setting<ColorSetting> healthU = new Setting<>("healthU", new ColorSetting(new Color(3145472))).addToGroup(this.boxColors);
   private float dizorentAnimation = 0.0F;

   public ESP() {
      super("ESP", Module.Category.RENDER);
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      if (!mc.options.hudHidden) {
         if (this.lingeringPotions.getValue()) {
            for (Entity ent : mc.world.getEntities()) {
               if (ent instanceof AreaEffectCloudEntity) {
                  AreaEffectCloudEntity aece = (AreaEffectCloudEntity)ent;
                  double x = aece.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
                  double y = aece.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
                  double z = aece.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
                  float middle = aece.getRadius();
                  stack.push();
                  stack.translate(x, y, z);
                  Render3DEngine.setupRender();
                  RenderSystem.disableDepthTest();
                  RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                  BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

                  for (int i = 0; i <= 360; i += 6) {
                     double v = Math.sin(Math.toRadians((double)i));
                     double u = Math.cos(Math.toRadians((double)i));
                     bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)u * middle, 0.0F, (float)v * middle)
                        .color(Render2DEngine.injectAlpha(new Color(this.getAreaCloudColor(aece)), 100).getRGB());
                     bufferBuilder.vertex(stack.peek().getPositionMatrix(), 0.0F, 0.0F, 0.0F)
                        .color(Render2DEngine.injectAlpha(new Color(this.getAreaCloudColor(aece)), 0).getRGB());
                  }

                  Render2DEngine.endBuilding(bufferBuilder);
                  RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                  bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

                  for (int i = 0; i <= 360; i += 6) {
                     double v = Math.sin(Math.toRadians((double)i));
                     double u = Math.cos(Math.toRadians((double)i));
                     bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)u * middle, 0.0F, (float)v * middle)
                        .color(Render2DEngine.injectAlpha(new Color(this.getAreaCloudColor(aece)), 255).getRGB());
                     bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)u * (middle - 0.04F), 0.0F, (float)v * (middle - 0.04F))
                        .color(Render2DEngine.injectAlpha(new Color(this.getAreaCloudColor(aece)), 255).getRGB());
                  }

                  Render2DEngine.endBuilding(bufferBuilder);
                  Render3DEngine.endRender();
                  RenderSystem.enableDepthTest();
                  stack.translate(-x, -y, -z);
                  stack.pop();
                  RenderSystem.disableDepthTest();
                  MatrixStack matrices = new MatrixStack();
                  Camera camera = mc.gameRenderer.getCamera();
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                  matrices.translate(x, y, z);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  matrices.translate(0.0F, 0.0F, 0.0F);
                  matrices.scale(-0.05F, -0.05F, 0.0F);
                  FontRenderers.modules.drawCenteredString(matrices, String.format("%.1f", aece.getRadius() * 10.0F - 5.0F), 0.0, -10.0, -1);
                  RenderSystem.disableBlend();
                  RenderSystem.enableDepthTest();
               }
            }
         }

         if (this.dizorentRadius.getValue()) {
            this.dizorentAnimation = AnimationUtility.fast(
               this.dizorentAnimation, mc.player.getMainHandStack().getItem() == Items.ENDER_EYE ? 10.0F : 0.0F, 15.0F
            );
            if (mc.player.getMainHandStack().getItem() == Items.ENDER_EYE) {
               double x = Render2DEngine.interpolate(mc.player.prevX, mc.player.getX(), (double)Render3DEngine.getTickDelta())
                  - mc.getEntityRenderDispatcher().camera.getPos().getX();
               double y = Render2DEngine.interpolate(mc.player.prevY, mc.player.getY(), (double)Render3DEngine.getTickDelta())
                  - mc.getEntityRenderDispatcher().camera.getPos().getY();
               double z = Render2DEngine.interpolate(mc.player.prevZ, mc.player.getZ(), (double)Render3DEngine.getTickDelta())
                  - mc.getEntityRenderDispatcher().camera.getPos().getZ();
               stack.push();
               stack.translate(x, y, z);
               Render3DEngine.setupRender();
               RenderSystem.disableDepthTest();
               RenderSystem.setShader(GameRenderer::getPositionColorProgram);
               BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

               for (int i = 0; i <= 360; i += 6) {
                  double v = Math.sin(Math.toRadians((double)i));
                  double u = Math.cos(Math.toRadians((double)i));
                  bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)u * this.dizorentAnimation, 0.0F, (float)v * this.dizorentAnimation)
                     .color(Render2DEngine.injectAlpha(new Color(this.dizorentColor.getValue().getColor()), 100).getRGB());
                  bufferBuilder.vertex(stack.peek().getPositionMatrix(), 0.0F, 0.0F, 0.0F)
                     .color(Render2DEngine.injectAlpha(new Color(this.dizorentColor.getValue().getColor()), 0).getRGB());
               }

               Render2DEngine.endBuilding(bufferBuilder);
               RenderSystem.setShader(GameRenderer::getPositionColorProgram);
               bufferBuilder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

               for (int i = 0; i <= 360; i += 6) {
                  double v = Math.sin(Math.toRadians((double)i));
                  double u = Math.cos(Math.toRadians((double)i));
                  bufferBuilder.vertex(stack.peek().getPositionMatrix(), (float)u * this.dizorentAnimation, 0.0F, (float)v * this.dizorentAnimation)
                     .color(Render2DEngine.injectAlpha(new Color(this.dizorentColor.getValue().getColor()), 255).getRGB());
                  bufferBuilder.vertex(
                        stack.peek().getPositionMatrix(), (float)u * (this.dizorentAnimation - 0.04F), 0.0F, (float)v * (this.dizorentAnimation - 0.04F)
                     )
                     .color(Render2DEngine.injectAlpha(new Color(this.dizorentColor.getValue().getColor()), 255).getRGB());
               }

               Render2DEngine.endBuilding(bufferBuilder);
               Render3DEngine.endRender();
               RenderSystem.enableDepthTest();
               stack.translate(-x, -y, -z);
               stack.pop();

               for (PlayerEntity pl : Managers.ASYNC.getAsyncPlayers()) {
                  if (!(mc.player.squaredDistanceTo(pl.getPos()) > 100.0) && pl != mc.player) {
                     Render3DEngine.drawTargetEsp(stack, pl);
                  }
               }
            }
         }

         if (this.beaconRadius.getValue()) {
         }

         if (this.burrow.getValue()) {
            for (PlayerEntity plx : mc.world.getPlayers()) {
               BlockPos blockPos = BlockPos.ofFloored(plx.getPos().add(0.0, 0.15F, 0.0));
               Block block = mc.world.getBlockState(blockPos).getBlock();
               double x = (double)blockPos.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
               double y = (double)blockPos.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
               double z = (double)blockPos.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
               if (block == Blocks.OBSIDIAN
                  || block == Blocks.CRYING_OBSIDIAN
                  || block == Blocks.ANVIL
                  || block == Blocks.PLAYER_HEAD
                  || block == Blocks.SKELETON_SKULL
                  || block == Blocks.WITHER_SKELETON_SKULL) {
                  Render3DEngine.drawBoxOutline(new Box(blockPos), this.burrowColor.getValue().getColorObject(), 2.0F);
                  RenderSystem.disableDepthTest();
                  MatrixStack matrices = new MatrixStack();
                  Camera camera = mc.gameRenderer.getCamera();
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                  matrices.translate(x + 0.5, y + 0.5, z + 0.5);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  matrices.translate(0.0F, 0.0F, 0.0F);
                  matrices.scale(-0.025F, -0.025F, 0.0F);
                  FontRenderers.modules.drawCenteredString(matrices, "BURROW", 0.0, -5.0, this.burrowTextColor.getValue().getColor());
                  RenderSystem.disableBlend();
                  RenderSystem.enableDepthTest();
               }
            }
         }

         if (this.tntFuse.getValue() || this.tntRadius.getValue()) {
            for (Entity entx : mc.world.getEntities()) {
               if (entx instanceof TntEntity) {
                  TntEntity tnt = (TntEntity)entx;
                  double x = tnt.prevX
                     + (tnt.getPos().getX() - tnt.prevX) * (double)Render3DEngine.getTickDelta()
                     - mc.getEntityRenderDispatcher().camera.getPos().getX();
                  double y = tnt.prevY
                     + (tnt.getPos().getY() - tnt.prevY) * (double)Render3DEngine.getTickDelta()
                     - mc.getEntityRenderDispatcher().camera.getPos().getY();
                  double z = tnt.prevZ
                     + (tnt.getPos().getZ() - tnt.prevZ) * (double)Render3DEngine.getTickDelta()
                     - mc.getEntityRenderDispatcher().camera.getPos().getZ();
                  if (this.tntFuse.getValue()) {
                     RenderSystem.disableDepthTest();
                     MatrixStack matrices = new MatrixStack();
                     Camera camera = mc.gameRenderer.getCamera();
                     matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                     matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                     matrices.translate(x, y + 0.5, z);
                     matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                     matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                     RenderSystem.enableBlend();
                     RenderSystem.defaultBlendFunc();
                     matrices.translate(0.0F, 0.0F, 0.0F);
                     matrices.scale(-0.025F, -0.025F, 0.0F);
                     FontRenderers.modules
                        .drawCenteredString(
                           matrices, String.format("%.1f", (float)tnt.getFuse() / 20.0F) + "s", 0.0, -5.0, this.tntFuseText.getValue().getColor()
                        );
                     RenderSystem.disableBlend();
                     RenderSystem.enableDepthTest();
                  }

                  if (this.tntRadius.getValue()) {
                     stack.push();
                     stack.translate(x, y, z);
                     Render3DEngine.drawSphere(stack, this.tntrange.getValue(), 20, 20, this.tntRadiusColor.getValue().getColor());
                     stack.translate(-x, -y, -z);
                     stack.pop();
                  }
               }
            }
         }
      }
   }

   @Override
   public void onRender2D(DrawContext context) {
      if (!mc.options.hudHidden) {
         if (this.pearls.getValue()) {
            for (Entity ent : mc.world.getEntities()) {
               if (ent instanceof EnderPearlEntity pearl) {
                  float xOffset = (float)mc.getWindow().getScaledWidth() / 2.0F;
                  float yOffset = (float)mc.getWindow().getScaledHeight() / 2.0F;
                  float xPos = (float)(pearl.prevX + (pearl.getPos().getX() - pearl.prevX) * (double)Render3DEngine.getTickDelta());
                  float zPos = (float)(pearl.prevZ + (pearl.getPos().getZ() - pearl.prevZ) * (double)Render3DEngine.getTickDelta());
                  float yaw = getRotations(new Vec2f(xPos, zPos)) - mc.player.getYaw();
                  context.getMatrices().translate(xOffset, yOffset, 0.0F);
                  context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
                  context.getMatrices().translate(-xOffset, -yOffset, 0.0F);
                  Render2DEngine.drawTracerPointer(
                     context.getMatrices(), xOffset, yOffset - 50.0F, 12.5F, 0.5F, 3.63F, true, true, HudEditor.getColor(1).getRGB()
                  );
                  context.getMatrices().translate(xOffset, yOffset, 0.0F);
                  context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
                  context.getMatrices().translate(-xOffset, -yOffset, 0.0F);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  FontRenderers.modules
                     .drawCenteredString(
                        context.getMatrices(),
                        String.format("%.1f", mc.player.distanceTo(pearl)) + "m",
                        (double)((float)(Math.sin(Math.toRadians((double)yaw)) * 50.0) + xOffset),
                        (double)((float)((double)yOffset - Math.cos(Math.toRadians((double)yaw)) * 50.0) - 20.0F),
                        -1
                     );
               }
            }
         }

         Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
         Render2DEngine.setupRender();
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

         for (Entity entx : mc.world.getEntities()) {
            if (this.shouldRender(entx)) {
               this.drawBox(bufferBuilder, entx, matrix);
            }
         }

         Render2DEngine.endBuilding(bufferBuilder);
         Render2DEngine.endRender();
      }
   }

   public boolean shouldRender(Entity entity) {
      if (entity == null) {
         return false;
      } else if (mc.player == null) {
         return false;
      } else if (entity instanceof PlayerEntity) {
         if (entity == mc.player) {
            return false;
         } else {
            return Managers.FRIEND.isFriend((PlayerEntity)entity) ? this.friends.getValue() : this.players.getValue();
         }
      } else if (entity instanceof EndCrystalEntity) {
         return this.crystals.getValue();
      } else {
         return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> this.creatures.getValue();
            case MONSTER -> this.monsters.getValue();
            case AMBIENT, WATER_AMBIENT -> this.ambients.getValue();
            default -> this.others.getValue();
         };
      }
   }

   public Color getEntityColor(Entity entity) {
      if (entity == null) {
         return new Color(-1);
      } else if (entity instanceof PlayerEntity) {
         return Managers.FRIEND.isFriend((PlayerEntity)entity) ? this.friendsC.getValue().getColorObject() : this.playersC.getValue().getColorObject();
      } else if (entity instanceof EndCrystalEntity) {
         return this.crystalsC.getValue().getColorObject();
      } else {
         return switch (entity.getType().getSpawnGroup()) {
            case CREATURE, WATER_CREATURE -> this.creaturesC.getValue().getColorObject();
            case MONSTER -> this.monstersC.getValue().getColorObject();
            case AMBIENT, WATER_AMBIENT -> this.ambientsC.getValue().getColorObject();
            default -> this.othersC.getValue().getColorObject();
         };
      }
   }

   public void drawBox(BufferBuilder bufferBuilder, @NotNull Entity ent, Matrix4f matrix) {
      Vec3d[] vectors = getVectors(ent);
      Color col = this.getEntityColor(ent);
      Vector4d position = null;

      for (Vec3d vector : vectors) {
         vector = Render3DEngine.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
         if (vector.z > 0.0 && vector.z < 1.0) {
            if (position == null) {
               position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
            }

            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
            position.w = Math.max(vector.y, position.w);
         }
      }

      if (position != null) {
         double posX = position.x;
         double posY = position.y;
         double endPosX = position.z;
         double endPosY = position.w;
         if (this.outline.getValue()) {
            Render2DEngine.setRectPoints(
               bufferBuilder,
               matrix,
               (float)(posX - 1.0),
               (float)posY,
               (float)(posX + 0.5),
               (float)(endPosY + 0.5),
               Color.BLACK,
               Color.BLACK,
               Color.BLACK,
               Color.BLACK
            );
            Render2DEngine.setRectPoints(
               bufferBuilder,
               matrix,
               (float)(posX - 1.0),
               (float)(posY - 0.5),
               (float)(endPosX + 0.5),
               (float)(posY + 0.5 + 0.5),
               Color.BLACK,
               Color.BLACK,
               Color.BLACK,
               Color.BLACK
            );
            Render2DEngine.setRectPoints(
               bufferBuilder,
               matrix,
               (float)(endPosX - 0.5 - 0.5),
               (float)posY,
               (float)(endPosX + 0.5),
               (float)(endPosY + 0.5),
               Color.BLACK,
               Color.BLACK,
               Color.BLACK,
               Color.BLACK
            );
            Render2DEngine.setRectPoints(
               bufferBuilder,
               matrix,
               (float)(posX - 1.0),
               (float)(endPosY - 0.5 - 0.5),
               (float)(endPosX + 0.5),
               (float)(endPosY + 0.5),
               Color.BLACK,
               Color.BLACK,
               Color.BLACK,
               Color.BLACK
            );
         }

         switch ((ESP.Colors)this.colorMode.getValue()) {
            case SyncColor:
               Render2DEngine.setRectPoints(
                  bufferBuilder,
                  matrix,
                  (float)(posX - 0.5),
                  (float)posY,
                  (float)(posX + 0.5 - 0.5),
                  (float)endPosY,
                  HudEditor.getColor(270),
                  HudEditor.getColor(0),
                  HudEditor.getColor(0),
                  HudEditor.getColor(270)
               );
               Render2DEngine.setRectPoints(
                  bufferBuilder,
                  matrix,
                  (float)posX,
                  (float)(endPosY - 0.5),
                  (float)endPosX,
                  (float)endPosY,
                  HudEditor.getColor(0),
                  HudEditor.getColor(180),
                  HudEditor.getColor(180),
                  HudEditor.getColor(0)
               );
               Render2DEngine.setRectPoints(
                  bufferBuilder,
                  matrix,
                  (float)(posX - 0.5),
                  (float)posY,
                  (float)endPosX,
                  (float)(posY + 0.5),
                  HudEditor.getColor(180),
                  HudEditor.getColor(90),
                  HudEditor.getColor(90),
                  HudEditor.getColor(180)
               );
               Render2DEngine.setRectPoints(
                  bufferBuilder,
                  matrix,
                  (float)(endPosX - 0.5),
                  (float)posY,
                  (float)endPosX,
                  (float)endPosY,
                  HudEditor.getColor(90),
                  HudEditor.getColor(270),
                  HudEditor.getColor(270),
                  HudEditor.getColor(90)
               );
               break;
            case Custom:
               Render2DEngine.setRectPoints(
                  bufferBuilder, matrix, (float)(posX - 0.5), (float)posY, (float)(posX + 0.5 - 0.5), (float)endPosY, col, col, col, col
               );
               Render2DEngine.setRectPoints(bufferBuilder, matrix, (float)posX, (float)(endPosY - 0.5), (float)endPosX, (float)endPosY, col, col, col, col);
               Render2DEngine.setRectPoints(bufferBuilder, matrix, (float)(posX - 0.5), (float)posY, (float)endPosX, (float)(posY + 0.5), col, col, col, col);
               Render2DEngine.setRectPoints(bufferBuilder, matrix, (float)(endPosX - 0.5), (float)posY, (float)endPosX, (float)endPosY, col, col, col, col);
         }

         if (ent instanceof LivingEntity lent && lent.getHealth() != 0.0F && this.renderHealth.getValue()) {
            Render2DEngine.setRectPoints(
               bufferBuilder, matrix, (float)(posX - 5.0), (float)posY, (float)posX - 3.0F, (float)endPosY, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK
            );
            switch ((ESP.Colors)this.colorMode.getValue()) {
               case SyncColor:
                  Render2DEngine.setRectPoints(
                     bufferBuilder,
                     matrix,
                     (float)(posX - 5.0),
                     (float)(endPosY + (posY - endPosY) * (double)lent.getHealth() / (double)lent.getMaxHealth()),
                     (float)posX - 3.0F,
                     (float)endPosY,
                     HudEditor.getColor(90),
                     HudEditor.getColor(90),
                     HudEditor.getColor(270),
                     HudEditor.getColor(270)
                  );
                  break;
               case Custom:
                  Render2DEngine.setRectPoints(
                     bufferBuilder,
                     matrix,
                     (float)(posX - 5.0),
                     (float)(endPosY + (posY - endPosY) * (double)lent.getHealth() / (double)lent.getMaxHealth()),
                     (float)posX - 3.0F,
                     (float)endPosY,
                     this.healthB.getValue().getColorObject(),
                     this.healthB.getValue().getColorObject(),
                     this.healthU.getValue().getColorObject(),
                     this.healthU.getValue().getColorObject()
                  );
            }
         }
      }
   }

   @NotNull
   private static Vec3d[] getVectors(@NotNull Entity ent) {
      double x = ent.prevX + (ent.getX() - ent.prevX) * (double)Render3DEngine.getTickDelta();
      double y = ent.prevY + (ent.getY() - ent.prevY) * (double)Render3DEngine.getTickDelta();
      double z = ent.prevZ + (ent.getZ() - ent.prevZ) * (double)Render3DEngine.getTickDelta();
      Box axisAlignedBB2 = ent.getBoundingBox();
      Box axisAlignedBB = new Box(
         axisAlignedBB2.minX - ent.getX() + x - 0.05,
         axisAlignedBB2.minY - ent.getY() + y,
         axisAlignedBB2.minZ - ent.getZ() + z - 0.05,
         axisAlignedBB2.maxX - ent.getX() + x + 0.05,
         axisAlignedBB2.maxY - ent.getY() + y + 0.15,
         axisAlignedBB2.maxZ - ent.getZ() + z + 0.05
      );
      return new Vec3d[]{
         new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ),
         new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ),
         new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ),
         new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ),
         new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ),
         new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ),
         new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ),
         new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)
      };
   }

   private int getAreaCloudColor(AreaEffectCloudEntity ent) {
      return ent.getParticleType() instanceof EntityEffectParticleEffect effect ? ((IAreaEffectCloudEntity)ent).getPotionContentsComponent().getColor() : -1;
   }

   public static float getRotations(Vec2f vec) {
      if (mc.player == null) {
         return 0.0F;
      } else {
         double x = (double)vec.x - mc.player.getPos().x;
         double z = (double)vec.y - mc.player.getPos().z;
         return (float)(-(Math.atan2(x, z) * (180.0 / Math.PI)));
      }
   }

   public static enum Colors {
      SyncColor,
      Custom;
   }
}
