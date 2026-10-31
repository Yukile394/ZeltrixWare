package thunder.hack.features.modules.player;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Objects;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.player.PlayerManager;
import thunder.hack.events.impl.EventAttackBlock;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IInteractionManager;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public final class SpeedMine extends Module {
   public final Setting<SpeedMine.Mode> mode = new Setting<>("Mode", SpeedMine.Mode.Packet);
   public final Setting<Boolean> doubleMine = new Setting<>("DoubleMine", false);
   private final Setting<SpeedMine.StartMode> startMode = new Setting<>(
      "StartMode", SpeedMine.StartMode.StartAbort, v -> this.mode.is(SpeedMine.Mode.Packet) && !this.doubleMine.getValue()
   );
   private final Setting<SpeedMine.SwitchMode> switchMode = new Setting<>(
      "SwitchMode", SpeedMine.SwitchMode.Alternative, v -> this.mode.not(SpeedMine.Mode.Damage)
   );
   private final Setting<Integer> swapDelay = new Setting<>("SwapDelay", 50, 0, 1000, v -> this.mode.getValue() != SpeedMine.Mode.Damage);
   private final Setting<Float> factor = new Setting<>("Factor", 1.0F, 0.5F, 2.0F, v -> this.mode.getValue() != SpeedMine.Mode.Damage);
   private final Setting<Float> speed = new Setting<>("Speed", 0.5F, 0.0F, 1.0F, v -> this.mode.getValue() == SpeedMine.Mode.Damage);
   public final Setting<Float> range = new Setting<>("Range", 4.2F, 3.0F, 10.0F, v -> this.mode.getValue() != SpeedMine.Mode.Damage);
   private final Setting<Boolean> rotate = new Setting<>("Rotate", false, v -> this.mode.getValue() != SpeedMine.Mode.Damage);
   private final Setting<Boolean> resetOnSwitch = new Setting<>("ResetOnSwitch", true, v -> this.mode.getValue() != SpeedMine.Mode.Damage);
   private final Setting<Integer> breakAttempts = new Setting<>("BreakAttempts", 10, 1, 50, v -> this.mode.getValue() == SpeedMine.Mode.Packet);
   private final Setting<Boolean> pauseEat = new Setting<>("Pause On Eat", false);
   private final Setting<Boolean> clientRemove = new Setting<>("ClientRemove", true);
   private final Setting<SettingGroup> packets = new Setting<>(
      "Packets", new SettingGroup(false, 0), v -> this.mode.is(SpeedMine.Mode.Packet) && !this.doubleMine.getValue()
   );
   private final Setting<Boolean> stop = new Setting<>("Stop", true, v -> this.mode.is(SpeedMine.Mode.Packet) && !this.doubleMine.getValue())
      .addToGroup(this.packets);
   private final Setting<Boolean> abort = new Setting<>("Abort", true, v -> this.mode.is(SpeedMine.Mode.Packet) && !this.doubleMine.getValue())
      .addToGroup(this.packets);
   private final Setting<Boolean> start = new Setting<>("Start", true, v -> this.mode.is(SpeedMine.Mode.Packet) && !this.doubleMine.getValue())
      .addToGroup(this.packets);
   private final Setting<Boolean> stop2 = new Setting<>("Stop2", true, v -> this.mode.is(SpeedMine.Mode.Packet) && !this.doubleMine.getValue())
      .addToGroup(this.packets);
   private final Setting<BooleanSettingGroup> render = new Setting<>(
      "Render", new BooleanSettingGroup(false), v -> this.mode.getValue() != SpeedMine.Mode.Damage
   );
   private final Setting<Boolean> smooth = new Setting<>("Smooth", true, v -> this.mode.getValue() != SpeedMine.Mode.Damage).addToGroup(this.render);
   private final Setting<SpeedMine.RenderMode> renderMode = new Setting<>(
         "Render Mode", SpeedMine.RenderMode.Shrink, v -> this.mode.getValue() != SpeedMine.Mode.Damage
      )
      .addToGroup(this.render);
   private final Setting<ColorSetting> startLineColor = new Setting<>(
         "Start Line Color", new ColorSetting(new Color(255, 0, 0, 200)), v -> this.mode.getValue() != SpeedMine.Mode.Damage
      )
      .addToGroup(this.render);
   private final Setting<ColorSetting> endLineColor = new Setting<>(
         "End Line Color", new ColorSetting(new Color(47, 255, 0, 200)), v -> this.mode.getValue() != SpeedMine.Mode.Damage
      )
      .addToGroup(this.render);
   private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 10, v -> this.mode.getValue() != SpeedMine.Mode.Damage).addToGroup(this.render);
   private final Setting<ColorSetting> startFillColor = new Setting<>(
         "Start Fill Color", new ColorSetting(new Color(255, 0, 0, 120)), v -> this.mode.getValue() != SpeedMine.Mode.Damage
      )
      .addToGroup(this.render);
   private final Setting<ColorSetting> endFillColor = new Setting<>(
         "End Fill Color", new ColorSetting(new Color(47, 255, 0, 120)), v -> this.mode.getValue() != SpeedMine.Mode.Damage
      )
      .addToGroup(this.render);
   public ArrayList<SpeedMine.MineAction> actions = new ArrayList<>();

   public SpeedMine() {
      super("SpeedMine", Module.Category.PLAYER);
   }

   @Override
   public void onDisable() {
      this.actions.forEach(SpeedMine.MineAction::reset);
      this.actions.clear();
   }

   @Override
   public void onEnable() {
      this.actions.forEach(SpeedMine.MineAction::reset);
      this.actions.clear();
   }

   @Override
   public void onUpdate() {
      if (!fullNullCheck() && !mc.player.getAbilities().creativeMode) {
         if (!PlayerUtility.isEating() || !this.pauseEat.getValue()) {
            if (this.mode.getValue() == SpeedMine.Mode.Damage && ((IInteractionManager)mc.interactionManager).getCurBlockDamageMP() < this.speed.getValue()) {
               ((IInteractionManager)mc.interactionManager).setCurBlockDamageMP(this.speed.getValue());
            }

            this.actions.removeIf(SpeedMine.MineAction::update);
         }
      }
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      if (!this.mode.is(SpeedMine.Mode.Damage) && mc.world != null) {
         this.actions
            .forEach(
               a -> {
                  if (!mc.world.isAir(a.getPos())) {
                     float noom = (float)MathUtility.clamp(
                        Render2DEngine.interpolate((double)a.getPrevProgress(), (double)a.getProgress(), (double)Render3DEngine.getTickDelta()), 0.0, 1.0
                     );

                     Box renderBox = switch ((SpeedMine.RenderMode)this.renderMode.getValue()) {
                        case Block -> new Box(a.getPos());
                        case Shrink -> new Box(
                           (double)a.getPos().getX(),
                           (double)a.getPos().getY(),
                           (double)a.getPos().getZ(),
                           (double)a.getPos().getX(),
                           (double)a.getPos().getY(),
                           (double)a.getPos().getZ()
                        )
                        .shrink((double)noom, (double)noom, (double)noom)
                        .offset(0.5 + (double)noom * 0.5, 0.5 + (double)noom * 0.5, 0.5 + (double)noom * 0.5);
                        case Grow -> new Box(
                        (double)a.getPos().getX(),
                        (double)a.getPos().getY(),
                        (double)a.getPos().getZ(),
                        (double)(a.getPos().getX() + 1),
                        (double)((float)a.getPos().getY() + noom),
                        (double)(a.getPos().getZ() + 1)
                     );
                     };
                     Render3DEngine.FILLED_QUEUE
                        .add(
                           new Render3DEngine.FillAction(
                              renderBox,
                              Render2DEngine.getColor(
                                 this.startFillColor.getValue().getColorObject(),
                                 this.endFillColor.getValue().getColorObject(),
                                 a.getProgress(),
                                 this.smooth.getValue()
                              )
                           )
                        );
                     Render3DEngine.OUTLINE_QUEUE
                        .add(
                           new Render3DEngine.OutlineAction(
                              renderBox,
                              Render2DEngine.getColor(
                                 this.startLineColor.getValue().getColorObject(),
                                 this.endLineColor.getValue().getColorObject(),
                                 a.getProgress(),
                                 this.smooth.getValue()
                              ),
                              (float)this.lineWidth.getValue().intValue()
                           )
                        );
                  }
               }
            );
      }
   }

   @EventHandler
   public void onAttackBlock(@NotNull EventAttackBlock event) {
      if (!fullNullCheck() && this.canBreak(event.getBlockPos()) && !mc.player.getAbilities().creativeMode && !this.mode.is(SpeedMine.Mode.Damage)) {
         if (!this.alreadyActing(event.getBlockPos())) {
            if ((!this.doubleMine.getValue() || this.actions.size() >= 2) && !this.actions.isEmpty()) {
               this.actions.removeFirst().cancel();
            }

            this.actions.add(new SpeedMine.MineAction(event.getBlockPos(), event.getEnumFacing()));
         }

         event.cancel();
      }
   }

   public boolean alreadyActing(BlockPos blockPos) {
      return this.actions.stream().anyMatch(a -> a.pos.equals(blockPos));
   }

   @EventHandler(
      priority = -100
   )
   private void onSync(EventSync event) {
      this.actions.forEach(SpeedMine.MineAction::onSync);
   }

   @EventHandler
   private void onPacketSend(@NotNull PacketEvent.SendPost e) {
      if (e.getPacket() instanceof UpdateSelectedSlotC2SPacket
         && this.resetOnSwitch.getValue()
         && !this.switchMode.is(SpeedMine.SwitchMode.Silent)
         && !this.mode.is(SpeedMine.Mode.GrimInstant)) {
         this.actions.forEach(SpeedMine.MineAction::reset);
      }
   }

   private void closeScreen() {
      if (mc.player != null) {
         this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
      }
   }

   public float getBlockStrength(@NotNull BlockState state, BlockPos position) {
      if (state == Blocks.AIR.getDefaultState()) {
         return 0.02F;
      } else {
         float hardness = state.getHardness(mc.world, position);
         return hardness < 0.0F ? 0.0F : this.getDigSpeed(state, position) / hardness / (this.canBreak(position) ? 30.0F : 100.0F);
      }
   }

   private float getDestroySpeed(BlockPos position, BlockState state) {
      float destroySpeed = 1.0F;
      int slot = this.getTool(position);
      if (mc.player == null) {
         return 0.0F;
      } else {
         if (slot != -1 && mc.player.getInventory().getStack(slot) != null && !mc.player.getInventory().getStack(slot).isEmpty()) {
            destroySpeed *= mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(state);
         }

         return destroySpeed;
      }
   }

   public float getDigSpeed(BlockState state, BlockPos position) {
      if (mc.player == null) {
         return 0.0F;
      } else {
         float digSpeed = this.getDestroySpeed(position, state);
         if (digSpeed > 1.0F) {
            int slot = this.getTool(position);
            if (slot != -1) {
               ItemStack itemstack = mc.player.getInventory().getStack(slot);
               int efficiencyModifier = EnchantmentHelper.getLevel(
                  (RegistryEntry)mc.world.getRegistryManager().get(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY).get(),
                  itemstack
               );
               if (efficiencyModifier > 0 && !itemstack.isEmpty()) {
                  digSpeed += (float)(StrictMath.pow((double)efficiencyModifier, 2.0) + 1.0);
               }
            }
         }

         if (mc.player.hasStatusEffect(StatusEffects.HASTE)) {
            digSpeed *= 1.0F + (float)(Objects.requireNonNull(mc.player.getStatusEffect(StatusEffects.HASTE)).getAmplifier() + 1) * 0.2F;
         }

         if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            digSpeed *= (float)Math.pow(0.3F, (double)(Objects.requireNonNull(mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE)).getAmplifier() + 1));
         }

         if (mc.player.isSubmergedInWater()) {
            digSpeed *= (float)mc.player.getAttributeInstance(EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED).getValue();
         }

         if (!mc.player.isOnGround() && ModuleManager.freeCam.isDisabled()) {
            digSpeed /= 5.0F;
         }

         return digSpeed < 0.0F ? 0.0F : digSpeed * this.factor.getValue();
      }
   }

   public int getTool(BlockPos pos) {
      int index = -1;
      float currentFastest = 1.0F;
      if (mc.world != null && mc.player != null && !(mc.world.getBlockState(pos).getBlock() instanceof AirBlock)) {
         for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i >= 36 ? i - 36 : i);
            if (stack != ItemStack.EMPTY && stack.getMaxDamage() - stack.getDamage() > 10) {
               float digSpeed = (float)EnchantmentHelper.getLevel(
                  (RegistryEntry)mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.EFFICIENCY).get(), stack
               );
               float destroySpeed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(pos));
               if (digSpeed + destroySpeed > currentFastest) {
                  currentFastest = digSpeed + destroySpeed;
                  index = i;
               }
            }
         }

         return index >= 36 ? index - 36 : index;
      } else {
         return -1;
      }
   }

   private boolean canBreak(BlockPos pos) {
      if (mc.world != null && !(PlayerUtility.squaredDistanceFromEyes(pos.toCenterPos()) > this.range.getPow2Value())) {
         BlockState blockState = mc.world.getBlockState(pos);
         Block block = blockState.getBlock();
         return block.getHardness() != -1.0F;
      } else {
         return false;
      }
   }

   public boolean isBlockDrop(Entity ent) {
      if (ent instanceof ItemEntity && this.isOn() && ent.age < 3) {
         for (SpeedMine.MineAction a : this.actions) {
            if (a.getPos().toCenterPos().squaredDistanceTo(ent.getPos()) <= 1.0) {
               return true;
            }
         }
      }

      return false;
   }

   public class MineAction {
      @NotNull
      private final BlockPos pos;
      private float progress;
      private float prevProgress;
      private int mineBreaks;
      private final Timer attackTimer = new Timer();

      public MineAction(@NotNull BlockPos pos, Direction direction) {
         this.pos = pos;
         this.progress = 0.0F;
         this.mineBreaks = 0;
         this.start(direction);
      }

      public void start(Direction direction) {
         Direction startDirection = direction == null ? Module.mc.player.getHorizontalFacing() : direction;
         if (startDirection != null) {
            if (SpeedMine.this.doubleMine.getValue()) {
               SpeedMine.this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.pos, startDirection));
               SpeedMine.this.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.pos, startDirection));
               SpeedMine.this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.pos, startDirection));
            } else {
               SpeedMine.this.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.pos, startDirection));
               SpeedMine.this.sendPacket(
                  new PlayerActionC2SPacket(
                     SpeedMine.this.startMode.getValue() == SpeedMine.StartMode.StartAbort ? Action.ABORT_DESTROY_BLOCK : Action.STOP_DESTROY_BLOCK,
                     this.pos,
                     startDirection
                  )
               );
            }
         }
      }

      public boolean update() {
         Direction dir = InteractionUtility.getStrictDirections(this.pos).stream().findFirst().orElse(Module.mc.player.getHorizontalFacing());
         if (this.mineBreaks >= SpeedMine.this.breakAttempts.getValue() && SpeedMine.this.mode.not(SpeedMine.Mode.GrimInstant)) {
            return true;
         } else if (PlayerUtility.squaredDistanceFromEyes(this.pos.toCenterPos()) > SpeedMine.this.range.getPow2Value()) {
            this.cancel();
            return true;
         } else if (Module.mc.world.isAir(this.pos)) {
            this.progress = 0.0F;
            this.prevProgress = -1.0F;
            return false;
         } else {
            if (this.progress == 0.0F && this.prevProgress == -1.0F && SpeedMine.this.mode.is(SpeedMine.Mode.Packet) && this.attackTimer.every(800L)) {
               this.start(dir);
               Module.mc.player.swingHand(Hand.MAIN_HAND);
            }

            int pickSlot = SpeedMine.this.getTool(this.pos);
            int prevSlot = Module.mc.player.getInventory().selectedSlot;
            if (pickSlot == -1) {
               return false;
            } else {
               boolean instant = this.mineBreaks > 0 && SpeedMine.this.mode.is(SpeedMine.Mode.GrimInstant);
               if (!(this.progress >= 1.0F) && !instant) {
                  this.prevProgress = this.progress;
                  this.progress = this.progress + SpeedMine.this.getBlockStrength(Module.mc.world.getBlockState(this.pos), this.pos);
               } else {
                  this.switchTo(pickSlot, -1);
                  if (SpeedMine.this.mode.getValue() != SpeedMine.Mode.GrimInstant && !SpeedMine.this.doubleMine.getValue()) {
                     if (SpeedMine.this.stop.getValue()) {
                        SpeedMine.this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.pos, dir));
                     }

                     if (SpeedMine.this.abort.getValue()) {
                        SpeedMine.this.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, this.pos, dir));
                     }

                     if (SpeedMine.this.start.getValue()) {
                        SpeedMine.this.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.pos, dir));
                     }

                     if (SpeedMine.this.stop2.getValue()) {
                        SpeedMine.this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.pos, dir));
                     }
                  } else {
                     SpeedMine.this.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.pos, dir));
                  }

                  if (SpeedMine.this.clientRemove.getValue()) {
                     Module.mc.interactionManager.breakBlock(this.pos);
                  }

                  int delay = SpeedMine.this.doubleMine.getValue() ? 100 : SpeedMine.this.swapDelay.getValue();
                  if (delay != 0) {
                     Managers.ASYNC.run(() -> this.switchTo(prevSlot, pickSlot), (long)delay);
                  } else {
                     this.switchTo(prevSlot, pickSlot);
                  }

                  this.mineBreaks++;
                  this.progress = this.prevProgress = 0.0F;
                  if (SpeedMine.this.doubleMine.getValue() && SpeedMine.this.mode.is(SpeedMine.Mode.GrimInstant) && SpeedMine.this.actions.size() >= 2) {
                     return true;
                  }
               }

               this.fixMovement();
               return false;
            }
         }
      }

      private void switchTo(int slot, int from) {
         if (SpeedMine.this.switchMode.getValue() != SpeedMine.SwitchMode.Alternative && slot < 9) {
            if (SpeedMine.this.switchMode.is(SpeedMine.SwitchMode.Silent)) {
               InventoryUtility.switchToSilent(slot);
            } else {
               InventoryUtility.switchTo(slot);
            }
         } else {
            if (from == -1) {
               Module.clickSlot(slot < 9 ? slot + 36 : slot, Module.mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
            } else {
               Module.clickSlot(from < 9 ? from + 36 : from, Module.mc.player.getInventory().selectedSlot, SlotActionType.SWAP);
            }

            SpeedMine.this.closeScreen();
         }
      }

      public void fixMovement() {
         if (SpeedMine.this.rotate.getValue() && (double)this.progress > 0.95) {
            ModuleManager.rotations.fixRotation = PlayerManager.calcAngle(Module.mc.player.getEyePos(), this.pos.toCenterPos())[0];
         }
      }

      public BlockPos getPos() {
         return this.pos;
      }

      public float getPrevProgress() {
         return this.prevProgress;
      }

      public float getProgress() {
         return this.progress;
      }

      public void onSync() {
         if (SpeedMine.this.rotate.getValue() && (double)this.progress > 0.95) {
            float[] angle = PlayerManager.calcAngle(Module.mc.player.getEyePos(), this.pos.toCenterPos().add(0.0, -0.25, 0.0));
            Module.mc.player.setYaw(angle[0]);
            Module.mc.player.setPitch(angle[1]);
         }
      }

      public void reset() {
         if (this.progress != 0.0F) {
            this.prevProgress = this.progress = 0.0F;
            Direction dir = InteractionUtility.getStrictDirections(this.pos).stream().findFirst().orElse(Module.mc.player.getHorizontalFacing());
            SpeedMine.this.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, this.pos, dir));
            this.start(dir);
         }
      }

      public void cancel() {
         if (this.progress != 0.0F) {
            SpeedMine.this.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, this.pos, Direction.DOWN));
         }
      }

      public boolean instantBreaking() {
         return this.mineBreaks > 0 && SpeedMine.this.mode.is(SpeedMine.Mode.GrimInstant);
      }
   }

   public static enum Mode {
      Packet,
      GrimInstant,
      Damage;
   }

   public static enum RenderMode {
      Block,
      Shrink,
      Grow;
   }

   public static enum StartMode {
      StartAbort,
      StartStop;
   }

   public static enum SwitchMode {
      Silent,
      Normal,
      Alternative;
   }
}
