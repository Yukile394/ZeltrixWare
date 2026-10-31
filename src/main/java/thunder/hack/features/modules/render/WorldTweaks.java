package thunder.hack.features.modules.render;

import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;

public class WorldTweaks extends Module {
   public static final Setting<BooleanSettingGroup> fogModify = new Setting<>("FogModify", new BooleanSettingGroup(true));
   public static final Setting<Integer> fogStart = new Setting<>("FogStart", 0, 0, 256).addToGroup(fogModify);
   public static final Setting<Integer> fogEnd = new Setting<>("FogEnd", 64, 10, 256).addToGroup(fogModify);
   public static final Setting<ColorSetting> fogColor = new Setting<>("FogColor", new ColorSetting(new Color(11075839))).addToGroup(fogModify);
   public final Setting<Boolean> ctime = new Setting<>("ChangeTime", false);
   public final Setting<Integer> ctimeVal = new Setting<>("Time", 21, 0, 23);
   long oldTime;

   public WorldTweaks() {
      super("WorldTweaks", Module.Category.RENDER);
   }

   @Override
   public void onEnable() {
      this.oldTime = mc.world.getTime();
   }

   @Override
   public void onDisable() {
      mc.world.setTimeOfDay(this.oldTime);
   }

   @EventHandler
   private void onPacketReceive(PacketEvent.Receive event) {
      if (event.getPacket() instanceof WorldTimeUpdateS2CPacket && this.ctime.getValue()) {
         this.oldTime = ((WorldTimeUpdateS2CPacket)event.getPacket()).getTime();
         event.cancel();
      }
   }

   @Override
   public void onUpdate() {
      if (this.ctime.getValue()) {
         mc.world.setTimeOfDay((long)(this.ctimeVal.getValue() * 1000));
      }
   }
}
