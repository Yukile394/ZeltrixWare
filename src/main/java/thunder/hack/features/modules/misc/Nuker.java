package thunder.hack.features.modules.misc;

import java.awt.Color;
import java.util.ArrayList;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventAttackBlock;
import thunder.hack.events.impl.EventSetBlockState;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.ItemSelectSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.PlayerUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.world.ExplosionUtility;

public class Nuker extends Module {
   public final Setting<ItemSelectSetting> selectedBlocks = new Setting<>("SelectedBlocks", new ItemSelectSetting(new ArrayList<>()));
   private final Setting<Nuker.Mode> mode = new Setting<>("Mode", Nuker.Mode.Default);
   private final Setting<Integer> delay = new Setting<>("Delay", 25, 0, 1000);
   private final Setting<Nuker.BlockSelection> blocks = new Setting<>("Blocks", Nuker.BlockSelection.Select);
   private final Setting<Boolean> ignoreWalls = new Setting<>("IgnoreWalls", false);
   private final Setting<Boolean> flatten = new Setting<>("Flatten", false);
   private final Setting<Boolean> creative = new Setting<>("Creative", false);
   private final Setting<Boolean> avoidLava = new Setting<>("AvoidLava", false);
   private final Setting<Float> range = new Setting<>("Range", 4.2F, 1.5F, 25.0F);
   private final Setting<Nuker.ColorMode> colorMode = new Setting<>("ColorMode", Nuker.ColorMode.Sync);
   public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(575714484), v -> this.colorMode.getValue() == Nuker.ColorMode.Custom);
   private Block targetBlockType;
   private Nuker.BlockData blockData;
   private Timer breakTimer = new Timer();
   private Nuker.NukerThread nukerThread = new Nuker.NukerThread();
   private float rotationYaw;
   private float rotationPitch;

   public Nuker() {
      super("Nuker", Module.Category.MISC);
   }

   @Override
   public void onEnable() {
      this.nukerThread = new Nuker.NukerThread();
      this.nukerThread.setName("ThunderHack-NukerThread");
      this.nukerThread.setDaemon(true);
      this.nukerThread.start();
   }

   @Override
   public void onDisable() {
      this.nukerThread.interrupt();
   }

   @Override
   public void onUpdate() {
      if (!this.nukerThread.isAlive()) {
         this.nukerThread = new Nuker.NukerThread();
         this.nukerThread.setName("ThunderHack-NukerThread");
         this.nukerThread.setDaemon(true);
         this.nukerThread.start();
      }
   }

   @EventHandler
   public void onBlockInteract(EventAttackBlock e) {
      if (!mc.world.isAir(e.getBlockPos())) {
         if (this.blocks.getValue().equals(Nuker.BlockSelection.Select) && this.targetBlockType != mc.world.getBlockState(e.getBlockPos()).getBlock()) {
            this.targetBlockType = mc.world.getBlockState(e.getBlockPos()).getBlock();
            this.sendMessage(
               ClientSettings.isRu()
                  ? "\u0412\u044b\u0431\u0440\u0430\u043d \u0431\u043b\u043e\u043a: " + Formatting.AQUA + this.targetBlockType.getName().getString()
                  : "Selected block: " + Formatting.AQUA + this.targetBlockType.getName().getString()
            );
         }
      }
   }

   @EventHandler
   public void onBlockDestruct(EventSetBlockState e) {
      if (this.blockData != null && e.getPos() == this.blockData.bp && e.getState().isAir()) {
         this.blockData = null;
         new Thread(
               () -> {
                  if ((this.targetBlockType != null || this.blocks.getValue().equals(Nuker.BlockSelection.All))
                     && !mc.options.attackKey.isPressed()
                     && this.blockData == null) {
                     this.blockData = this.getNukerBlockPos();
                  }
               }
            )
            .start();
      }
   }

   @EventHandler
   public void onSync(EventSync e) {
      if (this.rotationYaw != -999.0F) {
         mc.player.setYaw(this.rotationYaw);
         mc.player.setPitch(this.rotationPitch);
         this.rotationYaw = -999.0F;
      }
   }

   @EventHandler
   public void onPlayerUpdate(PlayerUpdateEvent e) {
      if (this.blockData != null
         && (
            mc.world.getBlockState(this.blockData.bp).getBlock() != this.targetBlockType && this.blocks.getValue().equals(Nuker.BlockSelection.Select)
               || PlayerUtility.squaredDistanceFromEyes(this.blockData.bp.toCenterPos()) > this.range.getPow2Value()
               || mc.world.isAir(this.blockData.bp)
         )) {
         this.blockData = null;
      }

      if (this.blockData != null && !mc.options.attackKey.isPressed()) {
         float[] angle = InteractionUtility.calculateAngle(this.blockData.vec3d);
         this.rotationYaw = angle[0];
         this.rotationPitch = angle[1];
         ModuleManager.rotations.fixRotation = this.rotationYaw;
         if (this.mode.getValue() == Nuker.Mode.Default) {
            this.breakBlock();
         }

         if (this.mode.getValue() == Nuker.Mode.FastAF) {
            int intRange = (int)(Math.floor((double)this.range.getValue().floatValue()) + 1.0);

            for (BlockPos b : BlockPos.iterateOutwards(
               new BlockPos(BlockPos.ofFloored(mc.player.getPos()).up()), intRange, intRange, intRange
            )) {
               if ((!this.flatten.getValue() || !((double)b.getY() < mc.player.getY()))
                  && (!this.avoidLava.getValue() || !this.checkLava(b))) {
                  BlockState state = mc.world.getBlockState(b);
                  if (PlayerUtility.squaredDistanceFromEyes(b.toCenterPos()) <= this.range.getPow2Value() && this.isAllowed(state.getBlock())) {
                     try {
                        this.sendSequencedPacket(id -> new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, b, Direction.UP, id));
                        mc.interactionManager.breakBlock(b);
                        mc.player.swingHand(Hand.MAIN_HAND);
                     } catch (Exception var9) {
                     }
                  }
               }
            }
         }
      }
   }

   public synchronized void breakBlock() {
      if (this.blockData != null && !mc.options.attackKey.isPressed()) {
         if (!ModuleManager.speedMine.isEnabled() || ModuleManager.speedMine.mode.getValue() != SpeedMine.Mode.Packet) {
            BlockPos cache = this.blockData.bp;
            mc.interactionManager.updateBlockBreakingProgress(this.blockData.bp, this.blockData.dir);
            mc.player.swingHand(Hand.MAIN_HAND);
            if (this.creative.getValue()) {
               mc.interactionManager.breakBlock(cache);
            }
         } else if (!ModuleManager.speedMine.alreadyActing(this.blockData.bp)) {
            mc.interactionManager.attackBlock(this.blockData.bp, this.blockData.dir);
            mc.player.swingHand(Hand.MAIN_HAND);
         }
      }
   }

   @Override
   public void onRender3D(MatrixStack stack) {
      BlockPos renderBp = null;
      if (this.blockData != null && this.blockData.bp != null) {
         renderBp = this.blockData.bp;
      }

      if (renderBp != null) {
         Color color1 = this.colorMode.getValue() == Nuker.ColorMode.Sync ? HudEditor.getColor(1) : this.color.getValue().getColorObject();
         Render3DEngine.drawBoxOutline(new Box(this.blockData.bp), color1, 2.0F);
         Render3DEngine.drawFilledBox(stack, new Box(this.blockData.bp), Render2DEngine.injectAlpha(color1, 100));
      }

      if (this.mode.getValue() == Nuker.Mode.Fast && this.breakTimer.passedMs((long)this.delay.getValue().intValue())) {
         this.breakBlock();
         this.breakTimer.reset();
      }
   }

   public Nuker.BlockData getNukerBlockPos() {
      int intRange = (int)(Math.floor((double)this.range.getValue().floatValue()) + 1.0);

      for (BlockPos b : BlockPos.iterateOutwards(
         new BlockPos(BlockPos.ofFloored(mc.player.getPos()).up()), intRange, intRange, intRange
      )) {
         BlockState state = mc.world.getBlockState(b);
         if ((!this.flatten.getValue() || !((double)b.getY() < mc.player.getY()))
            && PlayerUtility.squaredDistanceFromEyes(b.toCenterPos()) <= this.range.getPow2Value()
            && (!this.avoidLava.getValue() || !this.checkLava(b))
            && this.isAllowed(state.getBlock())) {
            if (this.ignoreWalls.getValue()) {
               BlockHitResult result = ExplosionUtility.rayCastBlock(
                  new RaycastContext(InteractionUtility.getEyesPos(mc.player), b.toCenterPos(), ShapeType.COLLIDER, FluidHandling.NONE, mc.player),
                  b
               );
               if (result != null) {
                  return new Nuker.BlockData(b, result.getPos(), result.getSide());
               }
            } else {
               for (float x1 = 0.0F; x1 <= 1.0F; x1 += 0.2F) {
                  for (float y1 = 0.0F; y1 <= 1.0F; y1 += 0.2F) {
                     for (float z1 = 0.0F; z1 <= 1.0F; z1 += 0.2F) {
                        Vec3d p = new Vec3d(
                           (double)((float)b.getX() + x1), (double)((float)b.getY() + y1), (double)((float)b.getZ() + z1)
                        );
                        BlockHitResult bhr = mc.world
                           .raycast(
                              new RaycastContext(InteractionUtility.getEyesPos(mc.player), p, ShapeType.OUTLINE, FluidHandling.NONE, mc.player)
                           );
                        if (bhr != null && bhr.getType() == Type.BLOCK && bhr.getBlockPos().equals(b)) {
                           return new Nuker.BlockData(b, p, bhr.getSide());
                        }
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private boolean checkLava(BlockPos base) {
      for (Direction dir : Direction.values()) {
         if (mc.world.getBlockState(base.offset(dir)).getBlock() == Blocks.LAVA) {
            return true;
         }
      }

      return false;
   }

   private boolean isAllowed(Block block) {
      boolean allowed = this.selectedBlocks.getValue().getItemsById().contains(block.getTranslationKey().replace("block.minecraft.", ""));

      return switch ((Nuker.BlockSelection)this.blocks.getValue()) {
         case Select -> block == this.targetBlockType;
         case All -> block != Blocks.BEDROCK && block != Blocks.AIR && block != Blocks.CAVE_AIR && !(block instanceof FluidBlock);
         default -> !allowed
         && block != Blocks.BEDROCK
         && block != Blocks.AIR
         && block != Blocks.CAVE_AIR
         && !(block instanceof FluidBlock);
         case WhiteList -> allowed;
      };
   }

   public static record BlockData(BlockPos bp, Vec3d vec3d, Direction dir) {
   }

   private static enum BlockSelection {
      Select,
      All,
      BlackList,
      WhiteList;
   }

   private static enum ColorMode {
      Custom,
      Sync;
   }

   private static enum Mode {
      Default,
      Fast,
      FastAF;
   }

   public class NukerThread extends Thread {
      @Override
      public void run() {
         while (!Thread.currentThread().isInterrupted()) {
            try {
               if (Module.fullNullCheck()) {
                  Thread.yield();
               } else {
                  while (Managers.ASYNC.ticking.get()) {
                  }

                  if ((Nuker.this.targetBlockType != null || !Nuker.this.blocks.getValue().equals(Nuker.BlockSelection.Select))
                     && !Module.mc.options.attackKey.isPressed()
                     && Nuker.this.blockData == null) {
                     Nuker.this.blockData = Nuker.this.getNukerBlockPos();
                  }
               }
            } catch (Exception var2) {
            }
         }
      }
   }
}
