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
import thunder.hack.utility.render.Render3DEngine;

/**
 * Single owner of the "where should the elytra player fly" decision.
 * <p>
 * The aim point computed here is shared with Aura (see {@link #getSharedAimPoint(Entity)}), so Aura's rotation,
 * the Rotations travel override, the firework thrust and the velocity assist below all steer towards the same,
 * smoothed point instead of fighting each other.
 */
public class ElytraTarget extends Module {
   public final Setting<Boolean> drawBox = new Setting<>("Draw Box", true);
   public final Setting<ColorSetting> boxColor = new Setting<>("Box Color", new ColorSetting(new Color(0, 0, 255, 120).getRGB()));
   public final Setting<Float> overshoot = new Setting<>("Overshoot", 2.5F, 0.0F, 6.0F);
   public final Setting<Float> distance = new Setting<>("Distance", 100.0F, 10.0F, 400.0F);
   public final Setting<Boolean> predict = new Setting<>("Predict", true);
   public final Setting<Integer> predictTicks = new Setting<>("Predict Ticks", 10, 1, 50);
   public final Setting<Integer> predictLength = new Setting<>("Predict Length", 2, 1, 20);
   public final Setting<Float> driveBoost = new Setting<>("Drive Boost", 1.5F, 0.1F, 5.0F);
   public final Setting<Float> maxTurn = new Setting<>("Max Turn", 12.0F, 1.0F, 60.0F);
   public final Setting<Float> passThrough = new Setting<>("Pass Through", 4.0F, 0.0F, 12.0F);
   public static PlayerEntity target = null;
   public static Vec3d predictedPos = null;
   private World lastWorld;
   private int trackedId = -1;
   private Vec3d prevTargetPos = null;
   private Vec3d smoothVel = Vec3d.ZERO;
   private Vec3d aimPoint = null;
   private int aimTargetId = -1;
   private int aimTick = -100;
   private boolean passing = false;

   public ElytraTarget() {
      super("ElytraTarget", Module.Category.COMBAT);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.resetState();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.resetState();
   }

   private void resetState() {
      target = null;
      predictedPos = null;
      this.trackedId = -1;
      this.prevTargetPos = null;
      this.smoothVel = Vec3d.ZERO;
      this.aimPoint = null;
      this.aimTargetId = -1;
      this.aimTick = -100;
      this.passing = false;
   }

   /**
    * @return the smoothed flight aim point for the given Aura target, or null when this module is not steering
    * towards that entity right now (Aura then falls back to the target's eye position).
    */
   public Vec3d getSharedAimPoint(Entity entity) {
      if (!this.isEnabled() || entity == null || this.aimPoint == null || mc.player == null) {
         return null;
      }

      if (entity.getId() != this.aimTargetId || mc.player.age - this.aimTick > 2 || !mc.player.isFallFlying()) {
         return null;
      }

      return this.aimPoint;
   }

   @EventHandler
   public void onTick(EventTick event) {
      if (mc.player == null || mc.world == null) {
         this.resetState();
         return;
      }

      if (mc.world != this.lastWorld) {
         this.resetState();
         this.lastWorld = mc.world;
      }

      Aura aura = ModuleManager.aura;
      // Aura off or not gliding: hand control back to the player completely.
      if (aura == null || !aura.isEnabled() || !mc.player.isFallFlying()) {
         this.resetState();
         return;
      }

      PlayerEntity chosen = this.pickTarget();
      if (chosen == null) {
         this.resetState();
         return;
      }

      if (chosen.getId() != this.trackedId) {
         // target switched: drop everything that belonged to the previous target
         this.resetState();
         this.trackedId = chosen.getId();
      }

      target = chosen;
      this.updateTargetVelocity(chosen);

      Vec3d eye = mc.player.getEyePos();
      Vec3d body = chosen.getEyePos();
      double dist = eye.distanceTo(body);
      Vec3d playerVel = mc.player.getVelocity();
      double playerSpeed = playerVel.length();

      Vec3d raw = body;
      double heading = this.smoothVel.length();
      Vec3d moveDir = heading > 0.05 ? this.smoothVel.normalize() : null;
      if (this.predict.getValue() && moveDir != null) {
         double tti = MathHelper.clamp(dist / Math.max(playerSpeed, 0.5), 0.0, (double)this.predictTicks.getValue().intValue());
         double lead = tti * (double)(this.driveBoost.getValue() / 1.5F);
         raw = body.add(this.smoothVel.multiply(lead));
      }

      if (moveDir != null) {
         double behind = Math.min((double)this.overshoot.getValue().floatValue(), dist * 0.25);
         raw = raw.subtract(moveDir.multiply(behind));
      }

      // far away: lead the target; close: aim at the real target so that hits actually land
      double near = (double)(aura.attackRange.getValue() + aura.aimRange.getValue() + 1.0F);
      double w = MathHelper.clamp((dist - near) / 6.0, 0.0, 1.0);
      w = w * w * (3.0 - 2.0 * w);
      raw = body.lerp(raw, w);

      // do not U-turn instantly after flying past the target: keep a straight line until there is room to turn
      double pass = (double)this.passThrough.getValue().floatValue();
      if (pass > 0.0 && playerSpeed > 0.3) {
         Vec3d toTarget = body.subtract(eye);
         double d = toTarget.length();
         if (d > 1.0E-3) {
            double cos = playerVel.dotProduct(toTarget) / (playerSpeed * d);
            if (this.passing) {
               if (d > pass * 1.8 || cos > 0.3) {
                  this.passing = false;
               }
            } else if (d < pass && cos < -0.2) {
               this.passing = true;
            }
         }
      } else {
         this.passing = false;
      }

      if (this.passing) {
         raw = eye.add(playerVel.normalize().multiply(8.0));
      }

      this.aimPoint = this.aimPoint == null ? raw : this.aimPoint.lerp(raw, 0.5);
      this.aimTargetId = chosen.getId();
      this.aimTick = mc.player.age;
      predictedPos = this.aimPoint;

      this.assistVelocity(eye, playerVel, playerSpeed);
   }

   /** Rotates (never rescales) the current velocity towards the shared aim point, limited per tick. */
   private void assistVelocity(Vec3d eye, Vec3d vel, double speed) {
      if (speed < 0.15 || this.aimPoint == null) {
         return;
      }

      Vec3d desired = this.aimPoint.subtract(eye);
      if (desired.lengthSquared() < 0.25) {
         return;
      }

      Vec3d d = desired.normalize();
      Vec3d c = vel.normalize();
      double angle = Math.acos(MathHelper.clamp(c.dotProduct(d), -1.0, 1.0));
      if (angle < Math.toRadians(1.5)) {
         return;
      }

      double t = Math.min(1.0, Math.toRadians((double)this.maxTurn.getValue().floatValue()) / angle);
      Vec3d dir = c.multiply(1.0 - t).add(d.multiply(t));
      if (dir.lengthSquared() < 1.0E-6) {
         Vec3d side = c.crossProduct(new Vec3d(0.0, 1.0, 0.0));
         dir = side.lengthSquared() < 1.0E-6 ? new Vec3d(1.0, 0.0, 0.0) : side;
      }

      mc.player.setVelocity(dir.normalize().multiply(speed));
   }

   private void updateTargetVelocity(PlayerEntity chosen) {
      Vec3d pos = chosen.getPos();
      if (this.prevTargetPos != null) {
         Vec3d step = pos.subtract(this.prevTargetPos);
         if (step.lengthSquared() > 64.0) {
            this.smoothVel = Vec3d.ZERO; // teleport / chunk reload
         } else {
            double alpha = 2.0 / ((double)this.predictLength.getValue().intValue() + 1.0);
            this.smoothVel = this.smoothVel.multiply(1.0 - alpha).add(step.multiply(alpha));
         }
      }

      this.prevTargetPos = pos;
   }

   /** Aura's own target wins, so Aura, rotations and steering always agree on who is being chased. */
   private PlayerEntity pickTarget() {
      Entity auraTarget = Aura.target;
      if (auraTarget instanceof PlayerEntity p) {
         return p != mc.player && p.isAlive() && !p.isSpectator() && p.getHealth() > 0.0F ? p : null;
      }

      return auraTarget == null ? this.findTarget() : null;
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
      if (target != null && this.drawBox.getValue() && this.aimPoint != null) {
         double s = 0.3;
         Vec3d p = this.aimPoint;
         Box box = new Box(p.x - s, p.y - s, p.z - s, p.x + s, p.y + s, p.z + s);
         Color color = this.predict.getValue() ? new Color(255, 255, 255, 120) : this.boxColor.getValue().getColorObject();
         Render3DEngine.drawFilledBox(stack, box, color);
         Render3DEngine.drawBoxOutline(box, this.predict.getValue() ? new Color(255, 255, 255, 255) : color, 2.0F);
      }
   }
}
