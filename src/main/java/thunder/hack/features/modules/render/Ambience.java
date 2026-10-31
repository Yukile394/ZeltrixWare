package thunder.hack.features.modules.render;

import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;

public class Ambience extends Module {
   public static final Setting<BooleanSettingGroup> fogModify = new Setting<>("FogModify", new BooleanSettingGroup(true));
   public static final Setting<Integer> fogStart = new Setting<>("FogStart", 0, 0, 256).addToGroup(fogModify);
   public static final Setting<Integer> fogEnd = new Setting<>("FogEnd", 64, 10, 256).addToGroup(fogModify);
   public static final Setting<ColorSetting> fogColor = new Setting<>("FogColor", new ColorSetting(new Color(11075839))).addToGroup(fogModify);
   public final Setting<Boolean> ctime = new Setting<>("ChangeTime", false);
   public final Setting<Integer> ctimeVal = new Setting<>("Time", 21, 0, 23);
   public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(255, 255, 255).getRGB()));
   long oldTime;

   public Ambience() {
      super("Ambience", Module.Category.RENDER);
   }

   @Override
   public void onEnable() {
      if (mc.world != null) {
         this.oldTime = mc.world.getTimeOfDay();
      }
   }

   @Override
   public void onDisable() {
      if (mc.world != null) {
         mc.world.setTimeOfDay(this.oldTime);
      }
   }

   @EventHandler
   private void onPacketReceive(PacketEvent.Receive event) {
      if (event.getPacket() instanceof WorldTimeUpdateS2CPacket && this.ctime.getValue()) {
         this.oldTime = ((WorldTimeUpdateS2CPacket)event.getPacket()).getTimeOfDay();
         event.cancel();
      }
   }

   @Override
   public void onUpdate() {
      if (mc.world != null && this.ctime.getValue()) {
         mc.world.setTimeOfDay((long)(this.ctimeVal.getValue() * 1000));
      }
   }
}
