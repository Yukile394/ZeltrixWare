package thunder.hack.core;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.MacroManager;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventDeath;
import thunder.hack.events.impl.EventKeyPress;
import thunder.hack.events.impl.EventMouse;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.notification.Notification;
import thunder.hack.gui.thundergui.ThunderGui;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.CaptureMark;

public final class Core {
   public static boolean lockSprint;
   public static boolean serverSprint;
   public static boolean hold_mouse0;
   public static boolean showSkull;
   public static final Map<String, Identifier> HEADS = new ConcurrentHashMap<>();
   public ArrayList<Packet<?>> silentPackets = new ArrayList<>();
   private final Timer skullTimer = new Timer();
   private final Timer lastPacket = new Timer();
   private final Timer autoSave = new Timer();
   private final Timer setBackTimer = new Timer();

   @EventHandler
   public void onTick(PlayerUpdateEvent event) {
      if (!Module.fullNullCheck()) {
         Managers.NOTIFICATION.onUpdate();
         Managers.MODULE.onUpdate();
         ThunderGui.getInstance().onTick();
         if (ModuleManager.clickGui.getBind().getKey() == -1) {
            Command.sendMessage(
               Formatting.RED
                  + (
                     ClientSettings.isRu()
                        ? "\u041f\u0440\u0438\u0432\u044f\u0437\u043a\u0430 \u043a\u043b\u0430\u0432\u0438\u0448 Clickgui \u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e -> P"
                        : "Default clickgui keybind --> P"
                  )
            );
            Command.sendMessage(
               Formatting.RED
                  + (
                     ClientSettings.isRu()
                        ? "\u0412\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u043f\u043e\u043b\u0443\u0447\u0438\u0442\u044c \u0433\u043e\u0442\u043e\u0432\u0443\u044e \u043a\u043e\u043d\u0444\u0438\u0433\u0443\u0440\u0430\u0446\u0438\u044e, \u0432\u044b\u043f\u043e\u043b\u043d\u0438\u0432 \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0443\u044e \u043a\u043e\u043c\u0430\u043d\u0434\u0443 -> @cfg cloudlist."
                        : "You can obtain a pre-built configuration by executing the following command -> @cfg cloudlist."
                  )
            );
            ModuleManager.clickGui.setBind(InputUtil.fromTranslationKey("key.keyboard.p").getCode(), false, false);
         }

         for (PlayerEntity p : Module.mc.world.getPlayers()) {
            if (p.isDead() || p.getHealth() == 0.0F) {
               ThunderHack.EVENT_BUS.post(new EventDeath(p));
            }
         }

         if (!Objects.equals(Managers.COMMAND.getPrefix(), ClientSettings.prefix.getValue())) {
            Managers.COMMAND.setPrefix(ClientSettings.prefix.getValue());
         }

         new HashMap<>(InteractionUtility.awaiting).forEach((bp, time) -> {
            if ((float)(System.currentTimeMillis() - time) > (float)Managers.SERVER.getPing() * 2.0F) {
               InteractionUtility.awaiting.remove(bp);
            }
         });
         if (this.autoSave.every(600000L)) {
            Managers.FRIEND.saveFriends();
            Managers.CONFIG.save(Managers.CONFIG.getCurrentConfig());
            Managers.WAYPOINT.saveWayPoints();
            Managers.MACRO.saveMacro();
            Managers.NOTIFICATION
               .publicity(
                  "AutoSave",
                  ClientSettings.isRu() ? "\u0421\u043e\u0445\u0440\u0430\u043d\u044f\u044e \u043a\u043e\u043d\u0444\u0438\u0433.." : "Saving config..",
                  3,
                  Notification.Type.INFO
               );
         }
      }
   }

   @EventHandler
   public void onPacketSend(@NotNull PacketEvent.Send e) {
      if (e.getPacket() instanceof PlayerMoveC2SPacket && !(e.getPacket() instanceof OnGroundOnly)) {
         this.lastPacket.reset();
      }

      if (e.getPacket() instanceof ClientCommandC2SPacket c && (c.getMode() == Mode.START_SPRINTING || c.getMode() == Mode.STOP_SPRINTING)) {
         if (lockSprint) {
            e.cancel();
            return;
         }

         switch (c.getMode()) {
            case START_SPRINTING:
               serverSprint = true;
               break;
            case STOP_SPRINTING:
               serverSprint = false;
         }
      }
   }

   @EventHandler
   public void onSync(EventSync event) {
      if (!Module.fullNullCheck()) {
         CaptureMark.tick();
         Render3DEngine.updateTargetESP();
      }
   }

   public void onRender2D(DrawContext e) {
      this.drawGps(e);
      this.drawSkull(e);
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.Receive e) {
      if (!Module.fullNullCheck()) {
         if (e.getPacket() instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet = e.getPacket();
            if (packet.content().getString().contains("skull")) {
               showSkull = true;
               this.skullTimer.reset();
               Module.mc
                  .world
                  .playSound(Module.mc.player, Module.mc.player.getBlockPos(), SoundEvents.ENTITY_SKELETON_DEATH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
         }

         if (e.getPacket() instanceof GameJoinS2CPacket) {
            Managers.MODULE.onLogin();
         }

         if (e.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.setBackTimer.reset();
         }
      }
   }

   public void drawSkull(DrawContext e) {
      if (showSkull && !this.skullTimer.passedMs(3000L) && ClientSettings.skullEmoji.getValue()) {
         int xPos = (int)((float)Module.mc.getWindow().getScaledWidth() / 2.0F - 150.0F);
         int yPos = (int)((float)Module.mc.getWindow().getScaledHeight() / 2.0F - 150.0F);
         float alpha = 1.0F - (float)this.skullTimer.getPassedTimeMs() / 3000.0F;
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
         e.drawTexture(TextureStorage.skull, xPos, yPos, 0.0F, 0.0F, 300, 300, 300, 300);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         showSkull = false;
      }
   }

   public void drawGps(DrawContext e) {
      if (ThunderHack.gps_position != null) {
         float dst = (float)this.getDistance(ThunderHack.gps_position);
         float xOffset = (float)Module.mc.getWindow().getScaledWidth() / 2.0F;
         float yOffset = (float)Module.mc.getWindow().getScaledHeight() / 2.0F;
         float yaw = getRotations(new Vec2f((float)ThunderHack.gps_position.getX(), (float)ThunderHack.gps_position.getZ()))
            - Module.mc.player.getYaw();
         e.getMatrices().translate(xOffset, yOffset, 0.0F);
         e.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
         e.getMatrices().translate(-xOffset, -yOffset, 0.0F);
         Render2DEngine.drawTracerPointer(e.getMatrices(), xOffset, yOffset - 50.0F, 12.5F, 0.5F, 3.63F, true, true, HudEditor.getColor(1).getRGB());
         e.getMatrices().translate(xOffset, yOffset, 0.0F);
         e.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
         e.getMatrices().translate(-xOffset, -yOffset, 0.0F);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         FontRenderers.modules
            .drawCenteredString(
               e.getMatrices(),
               "gps (" + dst + "m)",
               (double)((float)(Math.sin(Math.toRadians((double)yaw)) * 50.0) + xOffset),
               (double)((float)((double)yOffset - Math.cos(Math.toRadians((double)yaw)) * 50.0) - 23.0F),
               -1
            );
         if (dst < 10.0F) {
            ThunderHack.gps_position = null;
         }
      }
   }

   @EventHandler
   public void onKeyPress(EventKeyPress event) {
      if (event.getKey() != -1) {
         for (MacroManager.Macro m : Managers.MACRO.getMacros()) {
            if (m.getBind() == event.getKey()) {
               m.runMacro();
            }
         }
      }
   }

   @EventHandler
   public void onMouse(EventMouse event) {
      if (event.getAction() == 0) {
         hold_mouse0 = false;
      }

      if (event.getAction() == 1) {
         hold_mouse0 = true;
      }
   }

   public int getDistance(BlockPos bp) {
      double d0 = Module.mc.player.getX() - (double)bp.getX();
      double d2 = Module.mc.player.getZ() - (double)bp.getZ();
      return (int)MathHelper.sqrt((float)(d0 * d0 + d2 * d2));
   }

   public long getSetBackTime() {
      return this.setBackTimer.getPassedTimeMs();
   }

   public static float getRotations(Vec2f vec) {
      if (Module.mc.player == null) {
         return 0.0F;
      } else {
         double x = (double)vec.x - Module.mc.player.getPos().x;
         double z = (double)vec.y - Module.mc.player.getPos().z;
         return (float)(-(Math.atan2(x, z) * (180.0 / Math.PI)));
      }
   }

   public void bobView(MatrixStack matrices, float tickDelta) {
      if (Module.mc.getCameraEntity() instanceof PlayerEntity playerEntity) {
         float var6 = -(playerEntity.horizontalSpeed + (playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed) * tickDelta);
         float h = MathHelper.lerp(tickDelta, playerEntity.prevStrideDistance, playerEntity.strideDistance);
         matrices.translate(
            (double)(MathHelper.sin(var6 * (float) Math.PI) * h * 0.1F),
            (double)(-Math.abs(MathHelper.cos(var6 * (float) Math.PI) * h)) * 0.3,
            0.0
         );
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(var6 * (float) Math.PI) * h * 3.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(var6 * (float) Math.PI - 0.2F) * h) * 0.3F));
      }
   }
}
