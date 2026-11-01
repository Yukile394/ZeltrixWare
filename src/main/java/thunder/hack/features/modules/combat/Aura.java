package thunder.hack.features.modules.combat;

import baritone.api.BaritoneAPI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.UseAction;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.Core;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.player.PlayerManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.player.ElytraHelper;
import thunder.hack.gui.notification.Notification;
import thunder.hack.injection.accesors.ILivingEntity;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.interfaces.IOtherClientPlayerEntity;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.animation.CaptureMark;

public class Aura extends Module {
   public final Setting<Float> attackRange = new Setting<>("Range", 3.1F, 1.0F, 6.0F);
   public final Setting<Float> targetDistance = new Setting<>("Distance", 10.0F, 0.0F, 100.0F);
   public final Setting<Float> wallRange = new Setting<>("ThroughWallsRange", 3.1F, 0.0F, 6.0F);
   public final Setting<Boolean> elytra = new Setting<>("ElytraOverride", false);
   public final Setting<Float> elytraAttackRange = new Setting<>("ElytraRange", 3.1F, 1.0F, 6.0F, v -> this.elytra.getValue());
   public final Setting<Float> elytraWallRange = new Setting<>("ElytraThroughWallsRange", 3.1F, 0.0F, 6.0F, v -> this.elytra.getValue());
   public final Setting<Aura.WallsBypass> wallsBypass = new Setting<>("WallsBypass", Aura.WallsBypass.Off, v -> this.getWallRange() > 0.0F);
   public final Setting<Integer> fov = new Setting<>("FOV", 180, 1, 180);
   public final Setting<Aura.Mode> rotationMode = new Setting<>("RotationMode", Aura.Mode.Track);
   public final Setting<Integer> interactTicks = new Setting<>("InteractTicks", 3, 1, 10, v -> this.rotationMode.getValue() == Aura.Mode.Interact);
   public final Setting<Aura.Switch> switchMode = new Setting<>("AutoWeapon", Aura.Switch.None);
   public final Setting<Boolean> onlyWeapon = new Setting<>("OnlyWeapon", false, v -> this.switchMode.getValue() != Aura.Switch.Silent);
   public final Setting<BooleanSettingGroup> smartCrit = new Setting<>("SmartCrit", new BooleanSettingGroup(true));
   public final Setting<Boolean> onlySpace = new Setting<>("OnlyCrit", false).addToGroup(this.smartCrit);
   public final Setting<Boolean> autoJump = new Setting<>("AutoJump", false).addToGroup(this.smartCrit);
   public final Setting<Boolean> shieldBreaker = new Setting<>("ShieldBreaker", true);
   public final Setting<Boolean> pauseWhileEating = new Setting<>("PauseWhileEating", false);
   public final Setting<Boolean> tpsSync = new Setting<>("TPSSync", false);
   public final Setting<Boolean> clientLook = new Setting<>("ClientLook", false);
   public final Setting<Boolean> fakeRotation = new Setting<>("FakeRotation", false);
   public final Setting<Boolean> pauseBaritone = new Setting<>("PauseBaritone", false);
   public final Setting<BooleanSettingGroup> oldDelay = new Setting<>("OldDelay", new BooleanSettingGroup(false));
   public final Setting<Integer> minCPS = new Setting<>("MinCPS", 7, 1, 20).addToGroup(this.oldDelay);
   public final Setting<Integer> maxCPS = new Setting<>("MaxCPS", 12, 1, 20).addToGroup(this.oldDelay);
   public final Setting<Aura.ESP> esp = new Setting<>("ESP", Aura.ESP.ThunderHack);
   public final Setting<SettingGroup> espGroup = new Setting<>("ESPSettings", new SettingGroup(false, 0), v -> this.esp.is(Aura.ESP.ThunderHackV2));
   public final Setting<Integer> espLength = new Setting<>("ESPLength", 14, 1, 40, v -> this.esp.is(Aura.ESP.ThunderHackV2)).addToGroup(this.espGroup);
   public final Setting<Integer> espFactor = new Setting<>("ESPFactor", 8, 1, 20, v -> this.esp.is(Aura.ESP.ThunderHackV2)).addToGroup(this.espGroup);
   public final Setting<Float> espShaking = new Setting<>("ESPShaking", 1.8F, 1.5F, 10.0F, v -> this.esp.is(Aura.ESP.ThunderHackV2)).addToGroup(this.espGroup);
   public final Setting<Float> espAmplitude = new Setting<>("ESPAmplitude", 3.0F, 0.1F, 8.0F, v -> this.esp.is(Aura.ESP.ThunderHackV2))
      .addToGroup(this.espGroup);
   public final Setting<Aura.Sort> sort = new Setting<>("Sort", Aura.Sort.LowestDistance);
   public final Setting<Boolean> lockTarget = new Setting<>("LockTarget", true);
   public final Setting<Boolean> elytraTarget = new Setting<>("ElytraTarget", true);
   public final Setting<SettingGroup> advanced = new Setting<>("Advanced", new SettingGroup(false, 0));
   public final Setting<Float> aimRange = new Setting<>("AimRange", 3.1F, 0.0F, 6.0F).addToGroup(this.advanced);
   public final Setting<Boolean> randomHitDelay = new Setting<>("RandomHitDelay", false).addToGroup(this.advanced);
   public final Setting<Boolean> pauseInInventory = new Setting<>("PauseInInventory", true).addToGroup(this.advanced);
   public final Setting<Boolean> dropSprint = new Setting<>("DropSprint", true).addToGroup(this.advanced);
   public final Setting<Boolean> returnSprint = new Setting<>("ReturnSprint", true, v -> this.dropSprint.getValue()).addToGroup(this.advanced);
   public final Setting<Aura.RayTrace> rayTrace = new Setting<>("RayTrace", Aura.RayTrace.OnlyTarget).addToGroup(this.advanced);
   public final Setting<Boolean> grimRayTrace = new Setting<>("GrimRayTrace", true).addToGroup(this.advanced);
   public final Setting<Boolean> unpressShield = new Setting<>("UnpressShield", true).addToGroup(this.advanced);
   public final Setting<Boolean> deathDisable = new Setting<>("DisableOnDeath", true).addToGroup(this.advanced);
   public final Setting<Boolean> tpDisable = new Setting<>("TPDisable", false).addToGroup(this.advanced);
   public final Setting<Boolean> pullDown = new Setting<>("FastFall", false).addToGroup(this.advanced);
   public final Setting<Boolean> onlyJumpBoost = new Setting<>("OnlyJumpBoost", false, v -> this.pullDown.getValue()).addToGroup(this.advanced);
   public final Setting<Float> pullValue = new Setting<>("PullValue", 3.0F, 0.0F, 20.0F, v -> this.pullDown.getValue()).addToGroup(this.advanced);
   public final Setting<Aura.AttackHand> attackHand = new Setting<>("AttackHand", Aura.AttackHand.MainHand).addToGroup(this.advanced);
   public final Setting<Aura.Resolver> resolver = new Setting<>("Resolver", Aura.Resolver.Advantage).addToGroup(this.advanced);
   public final Setting<Integer> backTicks = new Setting<>("BackTicks", 4, 1, 20, v -> this.resolver.is(Aura.Resolver.BackTrack)).addToGroup(this.advanced);
   public final Setting<Boolean> resolverVisualisation = new Setting<>("ResolverVisualisation", false, v -> !this.resolver.is(Aura.Resolver.Off))
      .addToGroup(this.advanced);
   public final Setting<Aura.AccelerateOnHit> accelerateOnHit = new Setting<>("AccelerateOnHit", Aura.AccelerateOnHit.Off).addToGroup(this.advanced);
   public final Setting<Integer> minYawStep = new Setting<>("MinYawStep", 65, 1, 180).addToGroup(this.advanced);
   public final Setting<Integer> maxYawStep = new Setting<>("MaxYawStep", 75, 1, 180).addToGroup(this.advanced);
   public final Setting<Float> aimedPitchStep = new Setting<>("AimedPitchStep", 1.0F, 0.0F, 90.0F).addToGroup(this.advanced);
   public final Setting<Float> maxPitchStep = new Setting<>("MaxPitchStep", 8.0F, 1.0F, 90.0F).addToGroup(this.advanced);
   public final Setting<Float> pitchAccelerate = new Setting<>("PitchAccelerate", 1.65F, 1.0F, 10.0F).addToGroup(this.advanced);
   public final Setting<Float> attackCooldown = new Setting<>("AttackCooldown", 0.9F, 0.5F, 1.0F).addToGroup(this.advanced);
   public final Setting<Float> attackBaseTime = new Setting<>("AttackBaseTime", 0.5F, 0.0F, 2.0F).addToGroup(this.advanced);
   public final Setting<Integer> attackTickLimit = new Setting<>("AttackTickLimit", 11, 0, 20).addToGroup(this.advanced);
   public final Setting<Float> critFallDistance = new Setting<>("CritFallDistance", 0.0F, 0.0F, 1.0F).addToGroup(this.advanced);
   public final Setting<SettingGroup> targets = new Setting<>("Targets", new SettingGroup(false, 0));
   public final Setting<Boolean> Players = new Setting<>("Players", true).addToGroup(this.targets);
   public final Setting<Boolean> Mobs = new Setting<>("Mobs", true).addToGroup(this.targets);
   public final Setting<Boolean> Animals = new Setting<>("Animals", true).addToGroup(this.targets);
   public final Setting<Boolean> Villagers = new Setting<>("Villagers", true).addToGroup(this.targets);
   public final Setting<Boolean> Slimes = new Setting<>("Slimes", true).addToGroup(this.targets);
   public final Setting<Boolean> hostiles = new Setting<>("Hostiles", true).addToGroup(this.targets);
   public final Setting<Boolean> onlyAngry = new Setting<>("OnlyAngryHostiles", true, v -> this.hostiles.getValue()).addToGroup(this.targets);
   public final Setting<Boolean> Projectiles = new Setting<>("Projectiles", true).addToGroup(this.targets);
   public final Setting<Boolean> ignoreInvisible = new Setting<>("IgnoreInvisibleEntities", false).addToGroup(this.targets);
   public final Setting<Boolean> ignoreNamed = new Setting<>("IgnoreNamed", false).addToGroup(this.targets);
   public final Setting<Boolean> ignoreTeam = new Setting<>("IgnoreTeam", false).addToGroup(this.targets);
   public final Setting<Boolean> ignoreCreative = new Setting<>("IgnoreCreative", true).addToGroup(this.targets);
   public final Setting<Boolean> ignoreNaked = new Setting<>("IgnoreNaked", false).addToGroup(this.targets);
   public final Setting<Boolean> ignoreShield = new Setting<>("AttackShieldingEntities", true).addToGroup(this.targets);
   public static Entity target;
   public float rotationYaw;
   public float rotationPitch;
   public float pitchAcceleration = 1.0F;
   private Vec3d rotationPoint = Vec3d.ZERO;
   private Vec3d rotationMotion = Vec3d.ZERO;
   private LivingEntity NetpotTrackedTarget;
   private float NetpotCurrentYaw;
   private float NetpotCurrentPitch;
   private float NetpotVelocityYaw;
   private float NetpotVelocityPitch;
   private double NetpotAimOffsetX;
   private double NetpotAimOffsetY;
   private double NetpotAimOffsetZ;
   private float NetpotNoiseAngle = 0.0F;
   private float NetpotSmoothYaw;
   private float NetpotSmoothPitch;
   private float NetpotLastSentYaw;
   private float NetpotLastSentPitch;
   private long NetpotTargetAcquiredTime = 0L;
   private boolean NetpotReactionDone = false;
   private int NetpotReactionDelayMs = 0;
   private int hitTicks;
   private int trackticks;
   private boolean lookingAtHitbox;
   private float[] fakeRotationAngles = new float[]{0.0F, 0.0F};
   private int fakeRotationTicks = 0;
   private final Timer delayTimer = new Timer();
   private final Timer pauseTimer = new Timer();
   public Box resolvedBox;
   static boolean wasTargeted = false;

   public Aura() {
      super("Aura", Module.Category.COMBAT);
   }

   private float getRange() {
      float baseRange = this.elytra.getValue() && mc.player.isFallFlying() ? this.elytraAttackRange.getValue() : this.attackRange.getValue();
      return this.rotationMode.is(Aura.Mode.Netpot) && !mc.player.isFallFlying() ? Math.min(baseRange, 2.98F) : baseRange;
   }

   private float getWallRange() {
      return this.elytra.getValue() && mc.player != null && mc.player.isFallFlying() ? this.elytraWallRange.getValue() : this.wallRange.getValue();
   }

   public void auraLogic() {
      if (!this.haveWeapon()) {
         target = null;
      } else {
         this.handleKill();
         this.updateTarget();
         if (target != null) {
            if (!mc.options.jumpKey.isPressed() && mc.player.isOnGround() && this.autoJump.getValue()) {
               mc.player.jump();
            }

            boolean withinAttackRange = mc.player.distanceTo(target) <= this.getRange();
            boolean zeltrixWallHit = this.wallsBypass.is(Aura.WallsBypass.Zeltrix)
               && !mc.player.canSee(target)
               && mc.player.distanceTo(target) <= this.getWallRange()
               && this.autoCrit();
            boolean readyForAttack;
            if (this.grimRayTrace.getValue()) {
               readyForAttack = withinAttackRange && this.autoCrit() && (this.lookingAtHitbox || this.skipRayTraceCheck() || zeltrixWallHit);
               this.calcRotations(withinAttackRange && this.autoCrit() || zeltrixWallHit);
            } else {
               this.calcRotations(withinAttackRange && this.autoCrit() || zeltrixWallHit);
               readyForAttack = withinAttackRange && this.autoCrit() && (this.lookingAtHitbox || this.skipRayTraceCheck() || zeltrixWallHit);
            }

            if (readyForAttack) {
               if (this.shieldBreaker(false)) {
                  return;
               }

               boolean[] playerState = this.preAttack();
               if (!(target instanceof PlayerEntity pl)
                  || !pl.isUsingItem()
                  || pl.getOffHandStack().getItem() != Items.SHIELD
                  || this.ignoreShield.getValue()) {
                  this.attack();
               }

               this.postAttack(playerState[0], playerState[1]);
            }
         }
      }
   }

   private boolean haveWeapon() {
      Item handItem = mc.player.getMainHandStack().getItem();
      if (!this.onlyWeapon.getValue()) {
         return true;
      } else {
         return this.switchMode.getValue() == Aura.Switch.None
            ? handItem instanceof SwordItem || handItem instanceof AxeItem || handItem instanceof TridentItem
            : InventoryUtility.getSwordHotBar().found() || InventoryUtility.getAxeHotBar().found();
      }
   }

   private boolean skipRayTraceCheck() {
      return this.rotationMode.getValue() == Aura.Mode.None
         || this.rayTrace.getValue() == Aura.RayTrace.OFF
         || this.rotationMode.is(Aura.Mode.Grim)
         || this.rotationMode.is(Aura.Mode.HvH)
         || this.rotationMode.is(Aura.Mode.Interact)
            && (
               this.interactTicks.getValue() <= 1
                  || mc.world
                     .getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.25, 0.0, -0.25).offset(0.0, 1.0, 0.0))
                     .iterator()
                     .hasNext()
            );
   }

   public void attack() {
      if (!mc.player.isFallFlying() || !ElytraHelper.getInstance().isEnabled() || !ElytraHelper.swapping) {
         if (mc.player.isFallFlying() && ElytraHelper.getInstance().isEnabled()) {
            ElytraHelper.lastElytraAttackTime = System.currentTimeMillis();
         }

         Criticals.cancelCrit = true;
         ModuleManager.criticals.doCrit();
         int prevSlot = this.switchMethod();
         mc.interactionManager.attackEntity(mc.player, target);
         Criticals.cancelCrit = false;
         this.swingHand();
         this.hitTicks = this.getHitTicks();
         if (prevSlot != -1) {
            InventoryUtility.switchTo(prevSlot);
         }

         if (target != null && this.fakeRotation.getValue()) {
            double dx = target.getX() - mc.player.getX();
            double dy = target.getY() + (double)target.getStandingEyeHeight() * 0.5 - (mc.player.getY() + (double)mc.player.getStandingEyeHeight());
            double dz = target.getZ() - mc.player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            this.fakeRotationAngles[0] = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
            this.fakeRotationAngles[1] = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
            this.fakeRotationTicks = 2;
         }
      }
   }

   @NotNull
   public boolean[] preAttack() {
      boolean blocking = mc.player.isUsingItem()
         && mc.player.getActiveItem().getItem().getUseAction(mc.player.getActiveItem()) == UseAction.BLOCK;
      if (blocking && this.unpressShield.getValue()) {
         this.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
      }

      boolean sprint = Core.serverSprint;
      if (sprint && this.dropSprint.getValue()) {
         this.disableSprint();
      }

      if (this.rotationMode.is(Aura.Mode.Grim)) {
         this.sendPacket(
            new Full(
               mc.player.getX(),
               mc.player.getY(),
               mc.player.getZ(),
               this.rotationYaw,
               this.rotationPitch,
               mc.player.isOnGround()
            )
         );
      }

      return new boolean[]{blocking, sprint};
   }

   public void postAttack(boolean block, boolean sprint) {
      if (sprint && this.returnSprint.getValue() && this.dropSprint.getValue()) {
         this.enableSprint();
      }

      if (block && this.unpressShield.getValue()) {
         this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, this.rotationYaw, this.rotationPitch));
      }

      if (this.rotationMode.is(Aura.Mode.Grim)) {
         this.sendPacket(
            new Full(
               mc.player.getX(),
               mc.player.getY(),
               mc.player.getZ(),
               mc.player.getYaw(),
               mc.player.getPitch(),
               mc.player.isOnGround()
            )
         );
      }
   }

   private void disableSprint() {
      mc.player.setSprinting(false);
      mc.options.sprintKey.setPressed(false);
      this.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
   }

   private void enableSprint() {
      mc.player.setSprinting(true);
      mc.options.sprintKey.setPressed(true);
      this.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
   }

   public void resolvePlayers() {
      if (this.resolver.not(Aura.Resolver.Off)) {
         for (PlayerEntity player : mc.world.getPlayers()) {
            if (player instanceof OtherClientPlayerEntity) {
               ((IOtherClientPlayerEntity)player).resolve(this.resolver.getValue());
            }
         }
      }
   }

   public void restorePlayers() {
      if (this.resolver.not(Aura.Resolver.Off)) {
         for (PlayerEntity player : mc.world.getPlayers()) {
            if (player instanceof OtherClientPlayerEntity) {
               ((IOtherClientPlayerEntity)player).releaseResolver();
            }
         }
      }
   }

   public void handleKill() {
      if (target instanceof LivingEntity && (((LivingEntity)target).getHealth() <= 0.0F || ((LivingEntity)target).isDead())) {
         Managers.NOTIFICATION
            .publicity(
               "Aura",
               ClientSettings.isRu()
                  ? "\u011e\u00a6\u011e\u00b5\u011e\u00bb\u00d1\u0152 \u00d1\u0192\u00d1\u0081\u011e\u00bf\u011e\u00b5\u00d1\u02c6\u011e\u00bd\u011e\u00be \u011e\u00bd\u011e\u00b5\u011e\u00b9\u00d1\u201a\u00d1\u20ac\u011e\u00b0\u011e\u00bb\u011e\u00b8\u011e\u00b7\u011e\u00be\u011e\u00b2\u011e\u00b0\u011e\u00bd\u011e\u00b0!"
                  : "Target successfully neutralized!",
               3,
               Notification.Type.SUCCESS
            );
      }
   }

   private int switchMethod() {
      int prevSlot = -1;
      SearchInvResult swordResult = InventoryUtility.getSharpestHotBar();
      if (!swordResult.found()) {
         swordResult = InventoryUtility.getSwordHotBar();
      }

      if (swordResult.found() && this.switchMode.getValue() != Aura.Switch.None) {
         if (this.switchMode.getValue() == Aura.Switch.Silent) {
            prevSlot = mc.player.getInventory().selectedSlot;
         }

         swordResult.switchTo();
      }

      return prevSlot;
   }

   private int getHitTicks() {
      return this.oldDelay.getValue().isEnabled()
         ? 1 + (int)(20.0F / MathUtility.random((float)this.minCPS.getValue().intValue(), (float)this.maxCPS.getValue().intValue()))
         : (this.shouldRandomizeDelay() ? (int)MathUtility.random(11.0F, 13.0F) : this.attackTickLimit.getValue());
   }

   @EventHandler
   public void onUpdate(PlayerUpdateEvent e) {
      if (this.fakeRotationTicks > 0) {
         this.fakeRotationTicks--;
      }

      if (this.pauseTimer.passedMs(1000L)) {
         if (!mc.player.isUsingItem() || !this.pauseWhileEating.getValue()) {
            if (this.pauseBaritone.getValue() && ThunderHack.baritone) {
               boolean isTargeted = target != null;
               if (isTargeted && !wasTargeted) {
                  BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
                  wasTargeted = true;
               } else if (!isTargeted && wasTargeted) {
                  BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
                  wasTargeted = false;
               }
            }

            this.resolvePlayers();
            this.auraLogic();
            this.restorePlayers();
            this.hitTicks--;
         }
      }
   }

   @EventHandler
   public void onSync(EventSync e) {
      if (this.pauseTimer.passedMs(1000L)) {
         if (!mc.player.isUsingItem() || !this.pauseWhileEating.getValue()) {
            if (this.haveWeapon()) {
               if (target != null && this.rotationMode.getValue() != Aura.Mode.None && this.rotationMode.getValue() != Aura.Mode.Grim) {
                  mc.player.setYaw(this.rotationYaw);
                  mc.player.setPitch(this.rotationPitch);
               } else {
                  this.rotationYaw = mc.player.getYaw();
                  this.rotationPitch = mc.player.getPitch();
               }

               if (this.oldDelay.getValue().isEnabled() && this.minCPS.getValue() > this.maxCPS.getValue()) {
                  this.minCPS.setValue(this.maxCPS.getValue());
               }

               if (target != null && this.pullDown.getValue() && (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) || !this.onlyJumpBoost.getValue())) {
                  mc.player.addVelocity(0.0, (double)(-this.pullValue.getValue() / 1000.0F), 0.0);
               }
            }
         }
      }
   }

   @EventHandler
   public void onPacketSend(@NotNull PacketEvent.Send e) {
      if (e.getPacket() instanceof PlayerInteractEntityC2SPacket pie && Criticals.getInteractType(pie) != Criticals.InteractType.ATTACK && target != null) {
         e.cancel();
      }
   }

   @EventHandler
   public void onPacketReceive(@NotNull PacketEvent.Receive e) {
      if (e.getPacket() instanceof EntityStatusS2CPacket status
         && status.getStatus() == 30
         && status.getEntity(mc.world) != null
         && target != null
         && status.getEntity(mc.world) == target) {
         Managers.NOTIFICATION
            .publicity(
               "Aura",
               ClientSettings.isRu()
                  ? "\u011e\u00a3\u00d1\u0081\u011e\u00bf\u011e\u00b5\u00d1\u02c6\u011e\u00bd\u011e\u00be \u00d1\u0081\u011e\u00bb\u011e\u00be\u011e\u00bc\u011e\u00b0\u011e\u00bb\u011e\u00b8 \u00d1\u2030\u011e\u00b8\u00d1\u201a \u011e\u00b8\u011e\u00b3\u00d1\u20ac\u011e\u00be\u011e\u00ba\u00d1\u0192 "
                     + target.getName().getString()
                  : "Succesfully destroyed " + target.getName().getString() + "'s shield",
               2,
               Notification.Type.SUCCESS
            );
      }

      if (e.getPacket() instanceof PlayerPositionLookS2CPacket && this.tpDisable.getValue()) {
         this.disable(
            ClientSettings.isRu()
               ? "\u011e\u009e\u00d1\u201a\u011e\u00ba\u011e\u00bb\u00d1\u008e\u00d1\u2021\u011e\u00b0\u00d1\u008e \u011e\u00b8\u011e\u00b7-\u011e\u00b7\u011e\u00b0 \u00d1\u201a\u011e\u00b5\u011e\u00bb\u011e\u00b5\u011e\u00bf\u011e\u00be\u00d1\u20ac\u00d1\u201a\u011e\u00b0\u00d1\u2020\u011e\u00b8\u011e\u00b8!"
               : "Disabling due to tp!"
         );
      }

      if (e.getPacket() instanceof EntityStatusS2CPacket pac
         && pac.getStatus() == 3
         && pac.getEntity(mc.world) == mc.player
         && this.deathDisable.getValue()) {
         this.disable(
            ClientSettings.isRu()
               ? "\u011e\u009e\u00d1\u201a\u011e\u00ba\u011e\u00bb\u00d1\u008e\u00d1\u2021\u011e\u00b0\u00d1\u008e \u011e\u00b8\u011e\u00b7-\u011e\u00b7\u011e\u00b0 \u00d1\u0081\u011e\u00bc\u011e\u00b5\u00d1\u20ac\u00d1\u201a\u011e\u00b8!"
               : "Disabling due to death!"
         );
      }
   }

   @Override
   public void onEnable() {
      target = null;
      this.lookingAtHitbox = false;
      this.rotationPoint = Vec3d.ZERO;
      this.rotationMotion = Vec3d.ZERO;
      this.rotationYaw = mc.player.getYaw();
      this.rotationPitch = mc.player.getPitch();
      this.NetpotTrackedTarget = null;
      this.NetpotCurrentYaw = 0.0F;
      this.NetpotCurrentPitch = 0.0F;
      this.NetpotVelocityYaw = 0.0F;
      this.NetpotVelocityPitch = 0.0F;
      this.NetpotAimOffsetX = 0.0;
      this.NetpotAimOffsetY = 0.0;
      this.NetpotAimOffsetZ = 0.0;
      this.NetpotNoiseAngle = 0.0F;
      this.NetpotSmoothYaw = 0.0F;
      this.NetpotSmoothPitch = 0.0F;
      this.NetpotLastSentYaw = 0.0F;
      this.NetpotLastSentPitch = 0.0F;
      this.NetpotTargetAcquiredTime = 0L;
      this.NetpotReactionDone = false;
      this.NetpotReactionDelayMs = 0;
      this.fakeRotationTicks = 0;
      this.fakeRotationAngles = new float[]{0.0F, 0.0F};
      this.delayTimer.reset();
   }

   private boolean autoCrit() {
      boolean reasonForSkipCrit = !this.smartCrit.getValue().isEnabled()
         || mc.player.getAbilities().flying
         || mc.player.isFallFlying()
         || ModuleManager.elytraPlus.isEnabled()
         || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
         || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)
         || Managers.PLAYER.isInWeb();
      if (this.hitTicks > 0) {
         return false;
      } else if (this.pauseInInventory.getValue() && Managers.PLAYER.inInventory) {
         return false;
      } else if (this.getAttackCooldown() < this.attackCooldown.getValue() && !this.oldDelay.getValue().isEnabled()) {
         return false;
      } else if (ModuleManager.criticals.isEnabled() && ModuleManager.criticals.mode.is(Criticals.Mode.Grim)) {
         return true;
      } else {
         boolean mergeWithTargetStrafe = true;
         boolean mergeWithSpeed = mc.player.isOnGround();
         if (!mc.options.jumpKey.isPressed() && mergeWithTargetStrafe && mergeWithSpeed && !this.onlySpace.getValue() && !this.autoJump.getValue()) {
            return true;
         } else if (mc.player.isInLava() || mc.player.isSubmergedInWater()) {
            return true;
         } else if (!mc.options.jumpKey.isPressed() && this.isAboveWater()) {
            return true;
         } else if (mc.player.fallDistance > 1.0F && (double)mc.player.fallDistance < 1.14) {
            return false;
         } else {
            return reasonForSkipCrit
               ? true
               : !mc.player.isOnGround()
                  && mc.player.fallDistance > (this.shouldRandomizeFallDistance() ? MathUtility.random(0.15F, 0.7F) : this.critFallDistance.getValue());
         }
      }
   }

   private boolean shieldBreaker(boolean instant) {
      int axeSlot = InventoryUtility.getAxe().slot();
      if (axeSlot == -1) {
         return false;
      } else if (!this.shieldBreaker.getValue()) {
         return false;
      } else if (!(target instanceof PlayerEntity)) {
         return false;
      } else if (!((PlayerEntity)target).isUsingItem() && !instant) {
         return false;
      } else if (((PlayerEntity)target).getOffHandStack().getItem() != Items.SHIELD
         && ((PlayerEntity)target).getMainHandStack().getItem() != Items.SHIELD) {
         return false;
      } else {
         if (axeSlot >= 9) {
            mc.interactionManager
               .clickSlot(mc.player.currentScreenHandler.syncId, axeSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            mc.interactionManager.attackEntity(mc.player, target);
            this.swingHand();
            mc.interactionManager
               .clickSlot(mc.player.currentScreenHandler.syncId, axeSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
         } else {
            this.sendPacket(new UpdateSelectedSlotC2SPacket(axeSlot));
            mc.interactionManager.attackEntity(mc.player, target);
            this.swingHand();
            this.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
         }

         this.hitTicks = 10;
         return true;
      }
   }

   private void swingHand() {
      switch ((Aura.AttackHand)this.attackHand.getValue()) {
         case MainHand:
            mc.player.swingHand(Hand.MAIN_HAND);
            break;
         case OffHand:
            mc.player.swingHand(Hand.OFF_HAND);
      }
   }

   public boolean isAboveWater() {
      return mc.player.isSubmergedInWater()
         || mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos().add(0.0, -0.4, 0.0))).getBlock()
            == Blocks.WATER;
   }

   public float getAttackCooldownProgressPerTick() {
      return (float)(
         1.0
            / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED)
            * 20.0
            * (double)ThunderHack.TICK_TIMER
            * (double)(this.tpsSync.getValue() ? Managers.SERVER.getTPSFactor() : 1.0F)
      );
   }

   public float getAttackCooldown() {
      return MathHelper.clamp(
         ((float)((ILivingEntity)mc.player).getLastAttackedTicks() + this.attackBaseTime.getValue()) / this.getAttackCooldownProgressPerTick(), 0.0F, 1.0F
      );
   }

   private void updateTarget() {
      Entity candidat = this.findTarget();
      if (target == null) {
         target = candidat;
      } else {
         if (this.sort.getValue() == Aura.Sort.FOV || !this.lockTarget.getValue()) {
            target = candidat;
         }

         if (candidat instanceof ProjectileEntity) {
            target = candidat;
         }

         if (this.skipEntity(target)) {
            target = null;
         }
      }
   }

   private void calcRotations(boolean ready) {
      if (this.rotationMode.is(Aura.Mode.Netpot)) {
         this.calcNetpotRotations(target, ready);
      } else if (this.rotationMode.is(Aura.Mode.HvH)) {
         this.calcHvHRotations(target, ready);
      } else {
         if (ready) {
            this.trackticks = mc.world
                  .getBlockCollisions(mc.player, mc.player.getBoundingBox().expand(-0.25, 0.0, -0.25).offset(0.0, 1.0, 0.0))
                  .iterator()
                  .hasNext()
               ? 1
               : this.interactTicks.getValue();
         } else if (this.trackticks > 0) {
            this.trackticks--;
         }

         if (target != null) {
            Vec3d targetVec;
            if (!mc.player.isFallFlying() && !ModuleManager.elytraPlus.isEnabled()) {
               targetVec = this.getLegitLook(target);
            } else {
               targetVec = target.getEyePos();
            }

            if (targetVec != null) {
               this.pitchAcceleration = Managers.PLAYER
                     .checkRtx(
                        this.rotationYaw,
                        this.rotationPitch,
                        this.getRange() + this.aimRange.getValue(),
                        this.getRange() + this.aimRange.getValue(),
                        this.rayTrace.getValue()
                     )
                  ? this.aimedPitchStep.getValue()
                  : (
                     this.pitchAcceleration < this.maxPitchStep.getValue()
                        ? this.pitchAcceleration * this.pitchAccelerate.getValue()
                        : this.maxPitchStep.getValue()
                  );
               float delta_yaw = MathHelper.wrapDegrees(
                     (float)MathHelper.wrapDegrees(
                           Math.toDegrees(Math.atan2(targetVec.z - mc.player.getZ(), targetVec.x - mc.player.getX()))
                              - 90.0
                        )
                        - this.rotationYaw
                  )
                  + (float)(this.wallsBypass.is(Aura.WallsBypass.V2) && !ready && !mc.player.canSee(target) ? 20 : 0);
               float delta_pitch = (float)(
                     -Math.toDegrees(
                        Math.atan2(
                           targetVec.y - (mc.player.getPos().y + (double)mc.player.getEyeHeight(mc.player.getPose())),
                           Math.sqrt(
                              Math.pow(targetVec.x - mc.player.getX(), 2.0)
                                 + Math.pow(targetVec.z - mc.player.getZ(), 2.0)
                           )
                        )
                     )
                  )
                  - this.rotationPitch;
               float yawStep = this.rotationMode.getValue() != Aura.Mode.Track
                  ? 360.0F
                  : MathUtility.random((float)this.minYawStep.getValue().intValue(), (float)this.maxYawStep.getValue().intValue());
               float pitchStep = this.rotationMode.getValue() != Aura.Mode.Track
                  ? 180.0F
                  : (Managers.PLAYER.ticksElytraFlying > 5 ? 180.0F : this.pitchAcceleration + MathUtility.random(-1.0F, 1.0F));
               if (mc.player.isFallFlying() && ElytraHelper.getInstance().isEnabled() && ElytraHelper.getInstance().isSwingEnabled()) {
                  yawStep = (float)(55.0 + Math.random() * 25.0);
                  pitchStep = (float)(20.0 + Math.random() * 15.0);
               }

               boolean elytraSwingActive = mc.player.isFallFlying() && ElytraHelper.getInstance().isEnabled() && ElytraHelper.getInstance().isSwingEnabled();
               if (ready && !elytraSwingActive) {
                  switch ((Aura.AccelerateOnHit)this.accelerateOnHit.getValue()) {
                     case Yaw:
                        yawStep = 180.0F;
                        break;
                     case Pitch:
                        pitchStep = 90.0F;
                        break;
                     case Both:
                        yawStep = 180.0F;
                        pitchStep = 90.0F;
                  }
               }

               float deltaYaw = MathHelper.clamp(MathHelper.abs(delta_yaw), 0.0F, yawStep);
               float deltaPitch = MathHelper.clamp(delta_pitch, -pitchStep, pitchStep);
               float newYaw = this.rotationYaw + (delta_yaw > 0.0F ? deltaYaw : (delta_yaw < 0.0F ? -deltaYaw : 0.0F));
               float newPitch = MathHelper.clamp(this.rotationPitch + deltaPitch, -90.0F, 90.0F);
               float gcd = (float)(Math.pow((Double)mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0) * 1.2);
               if (this.trackticks <= 0 && this.rotationMode.getValue() != Aura.Mode.Track) {
                  if (mc.player != null) {
                     this.rotationYaw = mc.player.getYaw();
                     this.rotationPitch = mc.player.getPitch();
                  }
               } else if (gcd > 1.0E-4F) {
                  float rawDeltaYaw = newYaw - this.rotationYaw;
                  float rawDeltaPitch = newPitch - this.rotationPitch;
                  float deltaYawGCD = (float)(Math.floor((double)(Math.abs(rawDeltaYaw) / gcd)) * (double)gcd * (double)Math.signum(rawDeltaYaw));
                  float deltaPitchGCD = (float)(Math.floor((double)(Math.abs(rawDeltaPitch) / gcd)) * (double)gcd * (double)Math.signum(rawDeltaPitch));
                  if (deltaYawGCD == 0.0F && Math.abs(rawDeltaYaw) > 1.0E-4F) {
                     deltaYawGCD = gcd * Math.signum(rawDeltaYaw);
                  }

                  if (deltaPitchGCD == 0.0F && Math.abs(rawDeltaPitch) > 1.0E-4F) {
                     deltaPitchGCD = gcd * Math.signum(rawDeltaPitch);
                  }

                  this.rotationYaw += deltaYawGCD;
                  this.rotationPitch = MathHelper.clamp(this.rotationPitch + deltaPitchGCD, -90.0F, 90.0F);
               } else {
                  this.rotationYaw = newYaw;
                  this.rotationPitch = newPitch;
               }

               if (!this.rotationMode.is(Aura.Mode.Grim)) {
                  ModuleManager.rotations.fixRotation = this.rotationYaw;
               }

               this.lookingAtHitbox = Managers.PLAYER
                  .checkRtx(this.rotationYaw, this.rotationPitch, this.getRange(), this.getWallRange(), this.rayTrace.getValue());
            }
         }
      }
   }

   private void calcNetpotRotations(Entity target, boolean ready) {
      if (target != null && mc.player != null) {
         if (this.NetpotTrackedTarget != target) {
            this.NetpotTrackedTarget = target instanceof LivingEntity le ? le : null;
            this.NetpotTargetAcquiredTime = System.currentTimeMillis();
            this.NetpotReactionDone = false;
            this.NetpotReactionDelayMs = 80 + (int)(Math.random() * 120.0);
            this.NetpotNoiseAngle = (float)(Math.random() * Math.PI * 2.0);
            this.NetpotCurrentYaw = this.rotationYaw;
            this.NetpotCurrentPitch = this.rotationPitch;
            this.NetpotVelocityYaw = 0.0F;
            this.NetpotVelocityPitch = 0.0F;
         }

         long elapsed = System.currentTimeMillis() - this.NetpotTargetAcquiredTime;
         if (!this.NetpotReactionDone) {
            if (elapsed < (long)this.NetpotReactionDelayMs) {
               this.NetpotNoiseAngle += 0.06F;
               this.rotationYaw = this.rotationYaw + (float)(Math.sin((double)this.NetpotNoiseAngle) * 0.008);
               this.rotationPitch = this.rotationPitch + (float)(Math.cos((double)this.NetpotNoiseAngle * 1.3) * 0.005);
               ModuleManager.rotations.fixRotation = this.rotationYaw;
               this.lookingAtHitbox = false;
               return;
            }

            this.NetpotReactionDone = true;
         }

         Vec3d eyePos;
         boolean var10000;
         label87: {
            eyePos = mc.player.getEyePos();
            if (target instanceof LivingEntity le2 && le2.isFallFlying()) {
               var10000 = true;
               break label87;
            }

            var10000 = false;
         }

         boolean targetFlying = var10000;
         Vec3d aimPoint;
         if (!mc.player.isFallFlying() && !targetFlying) {
            double bodyFraction = 0.6;
            double bY = target.getY() + (double)target.getEyeHeight(target.getPose()) * bodyFraction;
            Vec3d bodyCenter = new Vec3d(target.getX(), bY, target.getZ());
            Vec3d vel = new Vec3d(
               target.getX() - target.prevX, target.getY() - target.prevY, target.getZ() - target.prevZ
            );
            aimPoint = bodyCenter.add(vel.multiply(2.5));
         } else {
            aimPoint = target.getEyePos();
         }

         this.NetpotNoiseAngle += 0.09F;
         double noiseX = Math.sin((double)this.NetpotNoiseAngle) * 0.022;
         double noiseY = Math.cos((double)this.NetpotNoiseAngle * 0.91) * 0.015;
         double noiseZ = Math.sin((double)this.NetpotNoiseAngle * 1.1) * 0.022;
         aimPoint = aimPoint.add(noiseX, noiseY, noiseZ);
         double dx = aimPoint.x - eyePos.x;
         double dy = aimPoint.y - eyePos.y;
         double dz = aimPoint.z - eyePos.z;
         double dxz = Math.sqrt(dx * dx + dz * dz);
         float targetYaw = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
         float targetPitch = (float)MathHelper.clamp(-Math.toDegrees(Math.atan2(dy, dxz)), -90.0, 90.0);
         float dYaw = MathHelper.wrapDegrees(targetYaw - this.NetpotCurrentYaw);
         float dPitch = targetPitch - this.NetpotCurrentPitch;
         float angularDist = (float)Math.hypot((double)dYaw, (double)dPitch);
         float springStrength = ready ? 0.32F : 0.22F;
         float dampening = 0.78F;
         float distFactor = MathHelper.clamp(angularDist / 30.0F, 0.0F, 1.0F);
         float accel = springStrength * (0.5F + distFactor * 1.5F);
         this.NetpotVelocityYaw = this.NetpotVelocityYaw * dampening + dYaw * accel;
         this.NetpotVelocityPitch = this.NetpotVelocityPitch * dampening + dPitch * accel;
         float maxTickYaw = 20.0F;
         float maxTickPitch = 15.0F;
         this.NetpotVelocityYaw = MathHelper.clamp(this.NetpotVelocityYaw, -maxTickYaw, maxTickYaw);
         this.NetpotVelocityPitch = MathHelper.clamp(this.NetpotVelocityPitch, -maxTickPitch, maxTickPitch);
         this.NetpotCurrentYaw = this.NetpotCurrentYaw + this.NetpotVelocityYaw;
         this.NetpotCurrentPitch = MathHelper.clamp(this.NetpotCurrentPitch + this.NetpotVelocityPitch, -90.0F, 90.0F);
         double sensD = (Double)mc.options.getMouseSensitivity().getValue();
         float gcd = (float)(Math.pow(sensD * 0.6 + 0.2, 3.0) * 8.0);
         if (gcd > 0.001F) {
            float rawDY = this.NetpotCurrentYaw - this.NetpotLastSentYaw;
            float rawDP = this.NetpotCurrentPitch - this.NetpotLastSentPitch;
            float stepY = (float)Math.round(rawDY / gcd) * gcd;
            float stepP = (float)Math.round(rawDP / gcd) * gcd;
            if (stepY == 0.0F && Math.abs(rawDY) > 0.001F) {
               stepY = gcd * Math.signum(rawDY);
            }

            if (stepP == 0.0F && Math.abs(rawDP) > 0.001F) {
               stepP = gcd * Math.signum(rawDP);
            }

            this.NetpotLastSentYaw += stepY;
            this.NetpotLastSentPitch = MathHelper.clamp(this.NetpotLastSentPitch + stepP, -90.0F, 90.0F);
         } else {
            this.NetpotLastSentYaw = this.NetpotCurrentYaw;
            this.NetpotLastSentPitch = MathHelper.clamp(this.NetpotCurrentPitch, -90.0F, 90.0F);
         }

         this.rotationYaw = this.NetpotLastSentYaw;
         this.rotationPitch = this.NetpotLastSentPitch;
         ModuleManager.rotations.fixRotation = this.rotationYaw;
         this.lookingAtHitbox = Managers.PLAYER.checkRtx(this.rotationYaw, this.rotationPitch, this.getRange(), this.getWallRange(), this.rayTrace.getValue());
         if (!this.lookingAtHitbox) {
            float yawDiff = Math.abs(MathHelper.wrapDegrees(this.rotationYaw - targetYaw));
            float pitchDiff = Math.abs(this.rotationPitch - targetPitch);
            boolean anyGliding = mc.player.isFallFlying() || targetFlying;
            float maxYaw = anyGliding ? 45.0F : 25.0F;
            float maxPitch = anyGliding ? 40.0F : 30.0F;
            if (yawDiff <= maxYaw && pitchDiff <= maxPitch) {
               this.lookingAtHitbox = true;
            }
         }
      }
   }

   private void calcHvHRotations(Entity target, boolean ready) {
      if (target != null && mc.player != null) {
         Vec3d eyePos = mc.player.getEyePos();
         Vec3d aimPoint;
         if (!mc.player.isFallFlying() && !ModuleManager.elytraPlus.isEnabled()) {
            Box bb = target.getBoundingBox();
            double clampedX = MathHelper.clamp(eyePos.x, bb.minX + 0.05, bb.maxX - 0.05);
            double clampedY = MathHelper.clamp(eyePos.y, bb.minY + 0.1, bb.maxY - 0.1);
            double clampedZ = MathHelper.clamp(eyePos.z, bb.minZ + 0.05, bb.maxZ - 0.05);
            aimPoint = new Vec3d(clampedX, clampedY, clampedZ);
            Vec3d vel = new Vec3d(
               target.getX() - target.prevX, target.getY() - target.prevY, target.getZ() - target.prevZ
            );
            if (vel.lengthSquared() > 1.0E-4) {
               aimPoint = aimPoint.add(vel.multiply(1.5));
            }
         } else {
            aimPoint = target.getEyePos();
         }

         double dx = aimPoint.x - eyePos.x;
         double dy = aimPoint.y - eyePos.y;
         double dz = aimPoint.z - eyePos.z;
         double distXZ = Math.sqrt(dx * dx + dz * dz);
         float targetYaw = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
         float targetPitch = (float)MathHelper.clamp(-Math.toDegrees(Math.atan2(dy, distXZ)), -90.0, 90.0);
         float yawStep = ready ? 360.0F : 180.0F;
         float pitchStep = ready ? 180.0F : 90.0F;
         float deltaYaw = MathHelper.wrapDegrees(targetYaw - this.rotationYaw);
         float deltaPitch = targetPitch - this.rotationPitch;
         float clampedDeltaYaw = MathHelper.clamp(deltaYaw, -yawStep, yawStep);
         float clampedDeltaPitch = MathHelper.clamp(deltaPitch, -pitchStep, pitchStep);
         float newYaw = this.rotationYaw + clampedDeltaYaw;
         float newPitch = MathHelper.clamp(this.rotationPitch + clampedDeltaPitch, -90.0F, 90.0F);
         float gcd = (float)(Math.pow((Double)mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0) * 1.2);
         if (gcd > 1.0E-4F) {
            float rawDY = newYaw - this.rotationYaw;
            float rawDP = newPitch - this.rotationPitch;
            float stepY = (float)Math.round(rawDY / gcd) * gcd;
            float stepP = (float)Math.round(rawDP / gcd) * gcd;
            if (stepY == 0.0F && Math.abs(rawDY) > 1.0E-4F) {
               stepY = gcd * Math.signum(rawDY);
            }

            if (stepP == 0.0F && Math.abs(rawDP) > 1.0E-4F) {
               stepP = gcd * Math.signum(rawDP);
            }

            this.rotationYaw += stepY;
            this.rotationPitch = MathHelper.clamp(this.rotationPitch + stepP, -90.0F, 90.0F);
         } else {
            this.rotationYaw = newYaw;
            this.rotationPitch = newPitch;
         }

         ModuleManager.rotations.fixRotation = this.rotationYaw;
         this.lookingAtHitbox = Managers.PLAYER.checkRtx(this.rotationYaw, this.rotationPitch, this.getRange(), this.getWallRange(), this.rayTrace.getValue());
         if (ready) {
            this.lookingAtHitbox = true;
         }
      }
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      if (this.haveWeapon() && target != null) {
         if ((this.resolver.is(Aura.Resolver.BackTrack) || this.resolverVisualisation.getValue()) && this.resolvedBox != null) {
            Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(this.resolvedBox, HudEditor.getColor(0), 1.0F));
         }

         switch ((Aura.ESP)this.esp.getValue()) {
            case ThunderHack:
               Render3DEngine.drawTargetEsp(stack, target);
               break;
            case NurikZapen:
               CaptureMark.render(target);
               break;
            case CelkaPasta:
               Render3DEngine.drawOldTargetEsp(stack, target);
               break;
            case ThunderHackV2:
               Render3DEngine.renderGhosts(
                  this.espLength.getValue(), this.espFactor.getValue(), this.espShaking.getValue(), this.espAmplitude.getValue(), target
               );
         }

         if (this.clientLook.getValue() && this.rotationMode.getValue() != Aura.Mode.None) {
            mc.player
               .setYaw(
                  (float)Render2DEngine.interpolate((double)mc.player.prevYaw, (double)this.rotationYaw, (double)Render3DEngine.getTickDelta())
               );
            mc.player
               .setPitch(
                  (float)Render2DEngine.interpolate((double)mc.player.prevPitch, (double)this.rotationPitch, (double)Render3DEngine.getTickDelta())
               );
         }
      }
   }

   @Override
   public void onDisable() {
      target = null;
      this.NetpotTrackedTarget = null;
      if (ModuleManager.rotations != null) {
         ModuleManager.rotations.fixRotation = Float.NaN;
      }

      this.resolvedBox = null;
      if (ThunderHack.baritone && wasTargeted) {
         try {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
         } catch (Exception var2) {
         }

         wasTargeted = false;
      }
   }

   public float getSquaredRotateDistance() {
      float dst = this.targetDistance.getValue();
      dst += this.aimRange.getValue();
      if ((mc.player.isFallFlying() || ModuleManager.elytraPlus.isEnabled()) && target != null) {
         dst += 4.0F;
      }

      if (this.rotationMode.getValue() != Aura.Mode.Track && this.rotationMode.getValue() != Aura.Mode.HvH || this.rayTrace.getValue() == Aura.RayTrace.OFF) {
         dst = this.targetDistance.getValue();
      }

      return dst * dst;
   }

   public Vec3d getLegitLook(Entity target) {
      float minMotionXZ = 0.003F;
      float maxMotionXZ = 0.03F;
      float minMotionY = 0.001F;
      float maxMotionY = 0.03F;
      double lenghtX = target.getBoundingBox().getLengthX();
      double lenghtY = target.getBoundingBox().getLengthY();
      double lenghtZ = target.getBoundingBox().getLengthZ();
      if (this.rotationMotion.equals(Vec3d.ZERO)) {
         this.rotationMotion = new Vec3d(
            (double)MathUtility.random(-0.05F, 0.05F), (double)MathUtility.random(-0.05F, 0.05F), (double)MathUtility.random(-0.05F, 0.05F)
         );
      }

      this.rotationPoint = this.rotationPoint.add(this.rotationMotion);
      if (this.rotationPoint.x >= (lenghtX - 0.05) / 2.0) {
         this.rotationMotion = new Vec3d(
            (double)(-MathUtility.random(minMotionXZ, maxMotionXZ)), this.rotationMotion.getY(), this.rotationMotion.getZ()
         );
      }

      if (this.rotationPoint.y >= lenghtY) {
         this.rotationMotion = new Vec3d(
            this.rotationMotion.getX(), (double)(-MathUtility.random(minMotionY, maxMotionY)), this.rotationMotion.getZ()
         );
      }

      if (this.rotationPoint.z >= (lenghtZ - 0.05) / 2.0) {
         this.rotationMotion = new Vec3d(
            this.rotationMotion.getX(), this.rotationMotion.getY(), (double)(-MathUtility.random(minMotionXZ, maxMotionXZ))
         );
      }

      if (this.rotationPoint.x <= -(lenghtX - 0.05) / 2.0) {
         this.rotationMotion = new Vec3d(
            (double)MathUtility.random(minMotionXZ, 0.03F), this.rotationMotion.getY(), this.rotationMotion.getZ()
         );
      }

      if (this.rotationPoint.y <= 0.05) {
         this.rotationMotion = new Vec3d(
            this.rotationMotion.getX(), (double)MathUtility.random(minMotionY, maxMotionY), this.rotationMotion.getZ()
         );
      }

      if (this.rotationPoint.z <= -(lenghtZ - 0.05) / 2.0) {
         this.rotationMotion = new Vec3d(
            this.rotationMotion.getX(), this.rotationMotion.getY(), (double)MathUtility.random(minMotionXZ, maxMotionXZ)
         );
      }

      this.rotationPoint.add((double)MathUtility.random(-0.03F, 0.03F), 0.0, (double)MathUtility.random(-0.03F, 0.03F));
      if (!mc.player.canSee(target)) {
         if (Objects.requireNonNull(this.wallsBypass.getValue()) == Aura.WallsBypass.V1) {
            return target.getPos().add(MathUtility.random(-0.15, 0.15), lenghtY, MathUtility.random(-0.15, 0.15));
         }

         if (this.wallsBypass.getValue() == Aura.WallsBypass.Zeltrix) {
            double jitterX = MathUtility.random(-0.08, 0.08);
            double jitterZ = MathUtility.random(-0.08, 0.08);
            double zeltrixY = lenghtY * 0.92 + MathUtility.random(-0.04, 0.04);
            return target.getPos().add(jitterX, zeltrixY, jitterZ);
         }
      }

      if (!Managers.PLAYER.checkRtx(this.rotationYaw, this.rotationPitch, this.getRange(), this.getWallRange(), this.rayTrace.getValue())) {
         float eyeH = target.getEyeHeight(target.getPose());
         Vec3d center = target.getPos().add(0.0, (double)(eyeH / 2.0F), 0.0);
         float[] rotation1 = PlayerManager.calcAngle(center);
         if (PlayerUtility.squaredDistanceFromEyes(center) <= this.attackRange.getPow2Value()
            && Managers.PLAYER.checkRtx(rotation1[0], rotation1[1], this.getRange(), 0.0F, this.rayTrace.getValue())) {
            this.rotationPoint = new Vec3d(
               (double)MathUtility.random(-0.1F, 0.1F), (double)(eyeH / MathUtility.random(1.8F, 2.5F)), (double)MathUtility.random(-0.1F, 0.1F)
            );
         } else {
            Vec3d eye = target.getEyePos();
            float[] eyeRot = PlayerManager.calcAngle(eye);
            if (PlayerUtility.squaredDistanceFromEyes(eye) <= this.attackRange.getPow2Value()
               && Managers.PLAYER.checkRtx(eyeRot[0], eyeRot[1], this.getRange(), 0.0F, this.rayTrace.getValue())) {
               this.rotationPoint = new Vec3d(0.0, (double)(eyeH * 0.9F), 0.0);
            } else {
               float halfBox = (float)(lenghtX / 2.0);

               for (float x1 : new float[]{-halfBox * 0.8F, 0.0F, halfBox * 0.8F}) {
                  for (float z1 : new float[]{-halfBox * 0.8F, 0.0F, halfBox * 0.8F}) {
                     for (float y1 : new float[]{0.3F, eyeH * 0.5F, eyeH * 0.85F}) {
                        Vec3d v1 = new Vec3d(target.getX() + (double)x1, target.getY() + (double)y1, target.getZ() + (double)z1);
                        if (!(PlayerUtility.squaredDistanceFromEyes(v1) > this.attackRange.getPow2Value())) {
                           float[] rotation = PlayerManager.calcAngle(v1);
                           if (Managers.PLAYER.checkRtx(rotation[0], rotation[1], this.getRange(), 0.0F, this.rayTrace.getValue())) {
                              this.rotationPoint = new Vec3d((double)x1, (double)y1, (double)z1);
                              return target.getPos().add(this.rotationPoint);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return target.getPos().add(this.rotationPoint);
   }

   public boolean isInRange(Entity target) {
      if (target == null) {
         return false;
      } else {
         double maxDistSq = (double)this.getSquaredRotateDistance();
         if ((double)PlayerUtility.squaredDistanceFromEyes(target.getEyePos()) > maxDistSq + 16.0
            && (double)PlayerUtility.squaredDistanceFromEyes(target.getPos()) > maxDistSq + 16.0) {
            return false;
         } else if (this.rayTrace.getValue() != Aura.RayTrace.OFF && !(this.getWallRange() >= this.getRange())) {
            float maxDist = (float)Math.sqrt(maxDistSq);
            Vec3d eyePos = target.getEyePos();
            if ((double)PlayerUtility.squaredDistanceFromEyes(eyePos) <= maxDistSq) {
               float[] rot = PlayerManager.calcAngle(eyePos);
               if (Managers.PLAYER.checkRtx(rot[0], rot[1], maxDist, this.getWallRange(), this.rayTrace.getValue())) {
                  return true;
               }
            }

            Vec3d centerPos = target.getBoundingBox().getCenter();
            if ((double)PlayerUtility.squaredDistanceFromEyes(centerPos) <= maxDistSq) {
               float[] rot = PlayerManager.calcAngle(centerPos);
               if (Managers.PLAYER.checkRtx(rot[0], rot[1], maxDist, this.getWallRange(), this.rayTrace.getValue())) {
                  return true;
               }
            }

            Vec3d chestPos = target.getPos().add(0.0, (double)target.getEyeHeight(target.getPose()) * 0.5, 0.0);
            if ((double)PlayerUtility.squaredDistanceFromEyes(chestPos) <= maxDistSq) {
               float[] rot = PlayerManager.calcAngle(chestPos);
               if (Managers.PLAYER.checkRtx(rot[0], rot[1], maxDist, this.getWallRange(), this.rayTrace.getValue())) {
                  return true;
               }
            }

            Box bb = target.getBoundingBox();
            Vec3d[] corners = new Vec3d[]{
               new Vec3d(bb.minX, bb.maxY, bb.minZ),
               new Vec3d(bb.maxX, bb.maxY, bb.maxZ),
               new Vec3d(bb.minX, bb.minY + 0.3, bb.maxZ),
               new Vec3d(bb.maxX, bb.minY + 0.3, bb.minZ)
            };

            for (Vec3d corner : corners) {
               if ((double)PlayerUtility.squaredDistanceFromEyes(corner) <= maxDistSq) {
                  float[] rot = PlayerManager.calcAngle(corner);
                  if (Managers.PLAYER.checkRtx(rot[0], rot[1], maxDist, this.getWallRange(), this.rayTrace.getValue())) {
                     return true;
                  }
               }
            }

            return false;
         } else {
            return (double)PlayerUtility.squaredDistanceFromEyes(target.getEyePos()) <= maxDistSq
               || (double)PlayerUtility.squaredDistanceFromEyes(target.getPos()) <= maxDistSq;
         }
      }
   }

   public Entity findTarget() {
      List<LivingEntity> first_stage = new CopyOnWriteArrayList<>();

      for (Entity ent : mc.world.getEntities()) {
         if ((ent instanceof ShulkerBulletEntity || ent instanceof FireballEntity) && ent.isAlive() && this.isInRange(ent) && this.Projectiles.getValue()) {
            return ent;
         }

         if (!this.skipEntity(ent) && ent instanceof LivingEntity) {
            first_stage.add((LivingEntity)ent);
         }
      }
      return switch ((Aura.Sort)this.sort.getValue()) {
         case LowestDistance -> (LivingEntity)first_stage.stream().min(Comparator.comparing(e -> mc.player.squaredDistanceTo(e.getPos()))).orElse(null);
         case HighestDistance -> (LivingEntity)first_stage.stream().max(Comparator.comparing(e -> mc.player.squaredDistanceTo(e.getPos()))).orElse(null);
         case LowestHealth -> (LivingEntity)first_stage.stream().min(Comparator.comparing(e -> e.getHealth() + e.getAbsorptionAmount())).orElse(null);
         case HighestHealth -> (LivingEntity)first_stage.stream().max(Comparator.comparing(e -> e.getHealth() + e.getAbsorptionAmount())).orElse(null);
         case LowestDurability -> (LivingEntity)first_stage.stream().min(Comparator.comparing(e -> {
         float v = 0.0F;

         for (ItemStack armor : e.getArmorItems()) {
            if (armor != null && !armor.getItem().equals(Items.AIR)) {
               v += (float)(armor.getMaxDamage() - armor.getDamage()) / (float)armor.getMaxDamage();
            }
         }

         return v;
      })).orElse(null);
         case HighestDurability -> (LivingEntity)first_stage.stream().max(Comparator.comparing(e -> {
         float v = 0.0F;

         for (ItemStack armor : e.getArmorItems()) {
            if (armor != null && !armor.getItem().equals(Items.AIR)) {
               v += (float)(armor.getMaxDamage() - armor.getDamage()) / (float)armor.getMaxDamage();
            }
         }

         return v;
      })).orElse(null);
         case FOV -> (LivingEntity)first_stage.stream().min(Comparator.comparing(this::getFOVAngle)).orElse(null);
      };
   }

   private boolean skipEntity(Entity entity) {
      if (this.isBullet(entity)) {
         return false;
      } else if (!(entity instanceof LivingEntity ent)) {
         return true;
      } else if (ent.isDead() || !entity.isAlive()) {
         return true;
      } else if (entity instanceof ArmorStandEntity) {
         return true;
      } else if (entity instanceof CatEntity) {
         return true;
      } else if (this.skipNotSelected(entity)) {
         return true;
      } else if (!InteractionUtility.isVecInFOV(ent.getPos(), this.fov.getValue())) {
         return true;
      } else {
         if (entity instanceof PlayerEntity player) {
            if (player == mc.player || Managers.FRIEND.isFriend(player)) {
               return true;
            }

            if (player.isCreative() && this.ignoreCreative.getValue()) {
               return true;
            }

            if (player.getArmor() == 0 && this.ignoreNaked.getValue()) {
               return true;
            }

            if (player.isInvisible() && this.ignoreInvisible.getValue()) {
               return true;
            }

            if (player.getTeamColorValue() == mc.player.getTeamColorValue() && this.ignoreTeam.getValue() && mc.player.getTeamColorValue() != 16777215) {
               return true;
            }
         }

         return !this.isInRange(entity) || entity.hasCustomName() && this.ignoreNamed.getValue();
      }
   }

   private boolean isBullet(Entity entity) {
      return (entity instanceof ShulkerBulletEntity || entity instanceof FireballEntity)
         && entity.isAlive()
         && PlayerUtility.squaredDistanceFromEyes(entity.getPos()) < this.getSquaredRotateDistance()
         && this.Projectiles.getValue();
   }

   private boolean skipNotSelected(Entity entity) {
      if (entity instanceof SlimeEntity && !this.Slimes.getValue()) {
         return true;
      } else {
         if (entity instanceof HostileEntity he) {
            if (!this.hostiles.getValue()) {
               return true;
            }

            if (this.onlyAngry.getValue()) {
               return !he.isAngryAt(mc.player);
            }
         }

         if (entity instanceof PlayerEntity && !this.Players.getValue()) {
            return true;
         } else if (entity instanceof VillagerEntity && !this.Villagers.getValue()) {
            return true;
         } else {
            return entity instanceof MobEntity && !this.Mobs.getValue() ? true : entity instanceof AnimalEntity && !this.Animals.getValue();
         }
      }
   }

   private float getFOVAngle(@NotNull LivingEntity e) {
      double difX = e.getX() - mc.player.getX();
      double difZ = e.getZ() - mc.player.getZ();
      float yaw = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
      return Math.abs(yaw - MathHelper.wrapDegrees(mc.player.getYaw()));
   }

   public void pause() {
      this.pauseTimer.reset();
   }

   private boolean shouldRandomizeDelay() {
      return this.randomHitDelay.getValue()
         && (mc.player.isOnGround() || mc.player.fallDistance < 0.12F || mc.player.isSwimming() || mc.player.isFallFlying());
   }

   private boolean shouldRandomizeFallDistance() {
      return this.randomHitDelay.getValue() && !this.shouldRandomizeDelay();
   }

   public static enum AccelerateOnHit {
      Off,
      Yaw,
      Pitch,
      Both;
   }

   public static enum AttackHand {
      MainHand,
      OffHand,
      None;
   }

   public static enum ESP {
      Off,
      ThunderHack,
      NurikZapen,
      CelkaPasta,
      ThunderHackV2;
   }

   public static enum Mode {
      Interact,
      Track,
      Grim,
      Netpot,
      HvH,
      None;
   }

   public static class Position {
      private double x;
      private double y;
      private double z;
      private int ticks;

      public Position(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public boolean shouldRemove() {
         return this.ticks++ > ModuleManager.aura.backTicks.getValue();
      }

      public double getX() {
         return this.x;
      }

      public double getY() {
         return this.y;
      }

      public double getZ() {
         return this.z;
      }
   }

   public static enum RayTrace {
      OFF,
      OnlyTarget,
      AllEntities;
   }

   public static enum Resolver {
      Off,
      Advantage,
      Predictive,
      BackTrack;
   }

   public static enum Sort {
      LowestDistance,
      HighestDistance,
      LowestHealth,
      HighestHealth,
      LowestDurability,
      HighestDurability,
      FOV;
   }

   public static enum Switch {
      Normal,
      None,
      Silent;
   }

   public static enum WallsBypass {
      Off,
      V1,
      V2,
      Zeltrix;
   }
}
