package thunder.hack.features.modules.combat;

import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventTick;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.interfaces.IEntityLiving;
import thunder.hack.utility.render.Render3DEngine;

public class ElytraTarget extends Module {
   public final Setting<Boolean> drawBox = new Setting<>("Draw Box", true);
   public final Setting<ColorSetting> boxColor = new Setting<>("Box Color", new ColorSetting(new Color(0, 0, 255, 120).getRGB()));
   public final Setting<Float> overshoot = new Setting<>("Overshoot", 2.5F, 0.0F, 6.0F);
   public final Setting<Float> distance = new Setting<>("Distance", 100.0F, 10.0F, 400.0F);
   public final Setting<Boolean> predict = new Setting<>("Predict", true);
   public final Setting<Integer> predictTicks = new Setting<>("Predict Ticks", 10, 1, 50);
   public final Setting<Integer> predictLength = new Setting<>("Predict Length", 2, 1, 20);
   public final Setting<Float> driveBoost = new Setting<>("Drive Boost", 1.5F, 0.1F, 5.0F);
   public static PlayerEntity target = null;
   public static Vec3d predictedPos = null;
   private World lastWorld;
   private double oscillationAngle = 0.0;
   private boolean overshot = false;
   private int predictTimer = 0;
   private double currentOverrun = 0.0;

   public ElytraTarget() {
      super("ElytraTarget", Module.Category.COMBAT);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      target = null;
      predictedPos = null;
      this.overshot = false;
      this.oscillationAngle = 0.0;
      this.predictTimer = 0;
      this.currentOverrun = 0.0;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      target = null;
      predictedPos = null;
      this.overshot = false;
      this.oscillationAngle = 0.0;
      this.predictTimer = 0;
      this.currentOverrun = 0.0;
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (mc.world != this.lastWorld) {
            target = null;
            this.lastWorld = mc.world;
         }

         target = this.findTarget();
         if (target == null) {
            predictedPos = null;
            this.overshot = false;
            this.predictTimer = 0;
         } else if (this.predict.getValue()) {
            this.predictTimer++;
            if (this.predictTimer >= this.predictLength.getValue() || predictedPos == null) {
               this.predictTimer = 0;
               this.currentOverrun = this.calculateOverrun(this.overshoot.getValue(), this.driveBoost.getValue());
               Vec3d targetPos = target.getPos();
               Vec3d velocity = Vec3d.ZERO;
               if (target instanceof IEntityLiving el) {
                  Vec3d from = new Vec3d(el.getPrevServerX(), el.getPrevServerY(), el.getPrevServerZ());
                  velocity = targetPos.subtract(from);
               }

               Vec3d targetFacing = new Vec3d(
                     -Math.sin(Math.toRadians((double)target.getYaw())), 0.0, Math.cos(Math.toRadians((double)target.getYaw()))
                  )
                  .normalize();
               double totalDist = (double)this.predictTicks.getValue().intValue() + this.currentOverrun;
               Vec3d movePred = targetPos.add(velocity.multiply(totalDist));
               predictedPos = movePred.subtract(targetFacing.multiply(this.currentOverrun)).add(0.0, 0.2, 0.0);
            }

            boolean isFlying = mc.player.isFallFlying()
               || mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) && !mc.player.isOnGround();
            if (isFlying) {
               Vec3d dir = predictedPos.subtract(mc.player.getPos());
               Vec3d currentVel = mc.player.getVelocity();
               double speed = currentVel.length();
               if (speed < 0.1) {
                  speed = 1.0;
               }

               if (dir.lengthSquared() > 0.5) {
                  Vec3d dirNorm = dir.normalize();
                  double blend = Math.min(1.0, dir.length() / 3.0);
                  Vec3d blendedVel = currentVel.multiply(1.0 - blend).add(dirNorm.multiply(speed * blend));
                  mc.player.setVelocity(blendedVel);
               }
            }
         } else {
            predictedPos = this.getFrontPosition(target, this.overshoot.getValue().doubleValue());
            this.oscillationAngle = 0.0;
            this.overshot = false;
         }
      } else {
         target = null;
         predictedPos = null;
      }
   }

   private PlayerEntity findTarget() {
      PlayerEntity bestTarget = null;
      float bestVal = -1.0F;

      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player != mc.player && player.isAlive() && !player.isSpectator() && !(player.getHealth() <= 0.0F) && !Managers.FRIEND.isFriend(player)) {
            boolean isFlying = player.isFallFlying() || player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) && !player.isOnGround();
            if (isFlying) {
               float d = mc.player.distanceTo(player);
               if (!(d > this.distance.getValue())) {
                  float currentVal = 0.0F;
                  Aura.Sort sortMode = ModuleManager.aura.sort.getValue();
                  switch (sortMode) {
                     case LowestDistance:
                        currentVal = 1000.0F - d;
                        break;
                     case HighestDistance:
                        currentVal = d;
                        break;
                     case LowestHealth:
                        currentVal = 1000.0F - player.getHealth();
                        break;
                     case HighestHealth:
                        currentVal = player.getHealth();
                        break;
                     case LowestDurability:
                        currentVal = 1000.0F
                           - (float)(player.getEquippedStack(EquipmentSlot.CHEST).getMaxDamage() - player.getEquippedStack(EquipmentSlot.CHEST).getDamage());
                        break;
                     case HighestDurability:
                        currentVal = (float)(player.getEquippedStack(EquipmentSlot.CHEST).getMaxDamage() - player.getEquippedStack(EquipmentSlot.CHEST).getDamage());
                        break;
                     case FOV:
                        double deltaX = player.getX() - mc.player.getX();
                        double deltaZ = player.getZ() - mc.player.getZ();
                        float yawDelta = Math.abs(
                           MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F - mc.player.getYaw())
                        );
                        currentVal = 180.0F - yawDelta;
                  }

                  if (bestTarget == null || currentVal > bestVal) {
                     bestTarget = player;
                     bestVal = currentVal;
                  }
               }
            }
         }
      }

      return bestTarget;
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      if (target != null && this.drawBox.getValue()) {
         float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
         double x = target.lastRenderX + (target.getX() - target.lastRenderX) * (double)tickDelta;
         double y = target.lastRenderY + (target.getY() - target.lastRenderY) * (double)tickDelta;
         double z = target.lastRenderZ + (target.getZ() - target.lastRenderZ) * (double)tickDelta;
         Vec3d interpolatedTargetPos = new Vec3d(x, y, z);
         if (this.predict.getValue()) {
            if (predictedPos != null) {
               double s = 0.3;
               Box box = new Box(
                  predictedPos.x - s,
                  predictedPos.y - s,
                  predictedPos.z - s,
                  predictedPos.x + s,
                  predictedPos.y + s,
                  predictedPos.z + s
               );
               Color color = new Color(255, 255, 255, 120);
               Render3DEngine.drawFilledBox(stack, box, color);
               Render3DEngine.drawBoxOutline(box, new Color(255, 255, 255, 255), 2.0F);
            }
         } else {
            Vec3d renderPos;
            if (this.overshot) {
               Vec3d toPlayer = mc.player.getCameraPosVec(tickDelta).subtract(interpolatedTargetPos).normalize();
               double oscillationY = Math.sin(this.oscillationAngle) * 1.5;
               renderPos = new Vec3d(
                  interpolatedTargetPos.x + toPlayer.x * (double)this.overshoot.getValue().floatValue(),
                  interpolatedTargetPos.y + 0.3 + oscillationY,
                  interpolatedTargetPos.z + toPlayer.z * (double)this.overshoot.getValue().floatValue()
               );
            } else {
               renderPos = this.getFrontPosition(target, this.overshoot.getValue().doubleValue());
            }

            double s = 0.3;
            Box box = new Box(
               renderPos.x - s,
               renderPos.y - s,
               renderPos.z - s,
               renderPos.x + s,
               renderPos.y + s,
               renderPos.z + s
            );
            Color color = this.boxColor.getValue().getColorObject();
            Render3DEngine.drawFilledBox(stack, box, color);
            Render3DEngine.drawBoxOutline(box, color, 2.0F);
         }
      }
   }

   private Vec3d getFrontPosition(Entity target, double overshoot) {
      Vec3d targetPos = target.getPos();
      Vec3d targetFacing = new Vec3d(
            -Math.sin(Math.toRadians((double)target.getYaw())), 0.0, Math.cos(Math.toRadians((double)target.getYaw()))
         )
         .normalize();
      if (target instanceof IEntityLiving el) {
         Vec3d from = new Vec3d(el.getPrevServerX(), el.getPrevServerY(), el.getPrevServerZ());
         Vec3d to = new Vec3d(target.getX(), target.getY(), target.getZ());
         Vec3d velocity = to.subtract(from);
         if (velocity.lengthSquared() > 0.001) {
            Vec3d moveDir = velocity.normalize();
            Vec3d blended = moveDir.add(targetFacing.negate()).normalize();
            return to.add(blended.multiply(overshoot)).add(0.0, 0.2, 0.0);
         }
      }

      return targetPos.subtract(targetFacing.multiply(overshoot)).add(0.0, 0.2, 0.0);
   }

   private double calculateOverrun(float overshootVal, float driveBoostVal) {
      return (double)(overshootVal * driveBoostVal);
   }
}
