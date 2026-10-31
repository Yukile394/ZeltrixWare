package thunder.hack.features.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import thunder.hack.events.impl.EventHeldItemRenderer;
import thunder.hack.events.impl.EventSetting;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class ViewModel extends Module {
   public final Setting<Boolean> syncHands = new Setting<>("SyncHands", true);
   public final Setting<SettingGroup> mainHand = new Setting<>("MainHand", new SettingGroup(false, 0));
   public final Setting<Float> positionMainX = new Setting<>("positionMainX", 0.0F, -3.0F, 3.0F).addToGroup(this.mainHand);
   public final Setting<Float> positionMainY = new Setting<>("positionMainY", 0.0F, -3.0F, 3.0F).addToGroup(this.mainHand);
   public final Setting<Float> positionMainZ = new Setting<>("positionMainZ", 0.0F, -3.0F, 3.0F).addToGroup(this.mainHand);
   public final Setting<Float> scaleMain = new Setting<>("ScaleMain", 1.0F, 0.1F, 1.5F).addToGroup(this.mainHand);
   public final Setting<SettingGroup> rotationMain = new Setting<>("Rotation", new SettingGroup(false, 1)).addToGroup(this.mainHand);
   public final Setting<Float> rotationMainX = new Setting<>("rotationMainX", 0.0F, -180.0F, 180.0F).addToGroup(this.rotationMain);
   public final Setting<Float> rotationMainY = new Setting<>("rotationMainY", 0.0F, -180.0F, 180.0F).addToGroup(this.rotationMain);
   public final Setting<Float> rotationMainZ = new Setting<>("rotationMainZ", 0.0F, -180.0F, 180.0F).addToGroup(this.rotationMain);
   public final Setting<SettingGroup> animateMain = new Setting<>("Animate", new SettingGroup(false, 1)).addToGroup(this.mainHand);
   public final Setting<Boolean> animateMainX = new Setting<>("animateMainX", false).addToGroup(this.animateMain);
   public final Setting<Boolean> animateMainY = new Setting<>("animateMainY", false).addToGroup(this.animateMain);
   public final Setting<Boolean> animateMainZ = new Setting<>("animateMainZ", false).addToGroup(this.animateMain);
   public final Setting<Float> speedAnimateMain = new Setting<>("speedAnimateMain", 1.0F, 1.0F, 5.0F).addToGroup(this.rotationMain);
   public final Setting<SettingGroup> offHand = new Setting<>("OffHand", new SettingGroup(false, 0));
   public final Setting<Float> positionOffX = new Setting<>("positionOffX", 0.0F, -3.0F, 3.0F).addToGroup(this.offHand);
   public final Setting<Float> positionOffY = new Setting<>("positionOffY", 0.0F, -3.0F, 3.0F).addToGroup(this.offHand);
   public final Setting<Float> positionOffZ = new Setting<>("positionOffZ", 0.0F, -3.0F, 3.0F).addToGroup(this.offHand);
   public final Setting<Float> scaleOff = new Setting<>("ScaleOff", 1.0F, 0.1F, 1.5F).addToGroup(this.offHand);
   public final Setting<SettingGroup> rotationOff = new Setting<>("RotationOff", new SettingGroup(false, 1)).addToGroup(this.offHand);
   public final Setting<Float> rotationOffX = new Setting<>("rotationOffX", 0.0F, -180.0F, 180.0F).addToGroup(this.rotationOff);
   public final Setting<Float> rotationOffY = new Setting<>("rotationOffY", 0.0F, -180.0F, 180.0F).addToGroup(this.rotationOff);
   public final Setting<Float> rotationOffZ = new Setting<>("rotationOffZ", 0.0F, -180.0F, 180.0F).addToGroup(this.rotationOff);
   public final Setting<SettingGroup> animateOff = new Setting<>("AnimateOff", new SettingGroup(false, 1)).addToGroup(this.offHand);
   public final Setting<Boolean> animateOffX = new Setting<>("animateOffX", false).addToGroup(this.animateOff);
   public final Setting<Boolean> animateOffY = new Setting<>("animateOffY", false).addToGroup(this.animateOff);
   public final Setting<Boolean> animateOffZ = new Setting<>("animateOffZ", false).addToGroup(this.animateOff);
   public final Setting<Float> speedAnimateOff = new Setting<>("speedAnimateOff", 1.0F, 1.0F, 5.0F).addToGroup(this.rotationOff);
   public final Setting<SettingGroup> eatMod = new Setting<>("Eat", new SettingGroup(false, 0));
   public final Setting<Float> eatX = new Setting<>("EatX", 1.0F, -1.0F, 2.0F).addToGroup(this.eatMod);
   public final Setting<Float> eatY = new Setting<>("EatY", 1.0F, -1.0F, 2.0F).addToGroup(this.eatMod);
   private float prevMainX;
   private float prevMainY;
   private float prevMainZ;
   private float prevOffX;
   private float prevOffY;
   private float prevOffZ;

   public ViewModel() {
      super("ViewModel", Module.Category.RENDER);
   }

   private float rotate(float value, float speed) {
      return value - speed <= 180.0F && value - speed > -180.0F ? value - speed : 180.0F;
   }

   @EventHandler
   public void onSettingChange(EventSetting e) {
      if (this.syncHands.getValue()) {
         if (e.getSetting() == this.positionMainX) {
            this.positionOffX.setValueSilent(this.positionMainX.getValue());
         }

         if (e.getSetting() == this.positionMainY) {
            this.positionOffY.setValueSilent(this.positionMainY.getValue());
         }

         if (e.getSetting() == this.positionMainZ) {
            this.positionOffZ.setValueSilent(this.positionMainZ.getValue());
         }

         if (e.getSetting() == this.positionOffX) {
            this.positionMainX.setValueSilent(this.positionOffX.getValue());
         }

         if (e.getSetting() == this.positionOffY) {
            this.positionMainY.setValueSilent(this.positionOffY.getValue());
         }

         if (e.getSetting() == this.positionOffZ) {
            this.positionMainZ.setValueSilent(this.positionOffZ.getValue());
         }

         if (e.getSetting() == this.scaleMain) {
            this.scaleOff.setValueSilent(this.scaleMain.getValue());
         }

         if (e.getSetting() == this.scaleOff) {
            this.scaleMain.setValueSilent(this.scaleOff.getValue());
         }
      }
   }

   @Override
   public void onUpdate() {
      this.prevMainX = this.rotationMainX.getValue();
      this.prevMainY = this.rotationMainY.getValue();
      this.prevMainZ = this.rotationMainZ.getValue();
      this.prevOffX = this.rotationOffX.getValue();
      this.prevOffY = this.rotationOffY.getValue();
      this.prevOffZ = this.rotationOffZ.getValue();
      if (this.animateMainX.getValue()) {
         this.rotationMainX.setValue(this.rotate(this.rotationMainX.getValue(), this.speedAnimateMain.getValue()));
      }

      if (this.animateMainY.getValue()) {
         this.rotationMainY.setValue(this.rotate(this.rotationMainY.getValue(), this.speedAnimateMain.getValue()));
      }

      if (this.animateMainZ.getValue()) {
         this.rotationMainZ.setValue(this.rotate(this.rotationMainZ.getValue(), this.speedAnimateMain.getValue()));
      }

      if (this.animateOffX.getValue()) {
         this.rotationOffX.setValue(this.rotate(this.rotationOffX.getValue(), this.speedAnimateOff.getValue()));
      }

      if (this.animateOffY.getValue()) {
         this.rotationOffY.setValue(this.rotate(this.rotationOffY.getValue(), this.speedAnimateOff.getValue()));
      }

      if (this.animateOffZ.getValue()) {
         this.rotationOffZ.setValue(this.rotate(this.rotationOffZ.getValue(), this.speedAnimateOff.getValue()));
      }
   }

   @EventHandler
   private void onHeldItemRender(EventHeldItemRenderer event) {
      if (event.getHand() == Hand.MAIN_HAND) {
         event.getStack().translate(this.positionMainX.getValue(), this.positionMainY.getValue(), this.positionMainZ.getValue());
         event.getStack().scale(this.scaleMain.getValue(), this.scaleMain.getValue(), this.scaleMain.getValue());
         event.getStack()
            .multiply(
               RotationAxis.POSITIVE_X
                  .rotationDegrees(Render2DEngine.interpolateFloat(this.prevMainX, this.rotationMainX.getValue(), (double)Render3DEngine.getTickDelta()))
            );
         event.getStack()
            .multiply(
               RotationAxis.POSITIVE_Y
                  .rotationDegrees(Render2DEngine.interpolateFloat(this.prevMainY, this.rotationMainY.getValue(), (double)Render3DEngine.getTickDelta()))
            );
         event.getStack()
            .multiply(
               RotationAxis.POSITIVE_Z
                  .rotationDegrees(Render2DEngine.interpolateFloat(this.prevMainZ, this.rotationMainZ.getValue(), (double)Render3DEngine.getTickDelta()))
            );
      } else {
         event.getStack().translate(-this.positionOffX.getValue(), this.positionOffY.getValue(), this.positionOffZ.getValue());
         event.getStack().scale(this.scaleOff.getValue(), this.scaleOff.getValue(), this.scaleOff.getValue());
         event.getStack()
            .multiply(
               RotationAxis.POSITIVE_X
                  .rotationDegrees(Render2DEngine.interpolateFloat(this.prevOffX, this.rotationOffX.getValue(), (double)Render3DEngine.getTickDelta()))
            );
         event.getStack()
            .multiply(
               RotationAxis.POSITIVE_Y
                  .rotationDegrees(Render2DEngine.interpolateFloat(this.prevOffY, this.rotationOffY.getValue(), (double)Render3DEngine.getTickDelta()))
            );
         event.getStack()
            .multiply(
               RotationAxis.POSITIVE_Z
                  .rotationDegrees(Render2DEngine.interpolateFloat(this.prevOffZ, this.rotationOffZ.getValue(), (double)Render3DEngine.getTickDelta()))
            );
      }
   }
}
