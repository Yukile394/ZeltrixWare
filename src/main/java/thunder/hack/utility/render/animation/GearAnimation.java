package thunder.hack.utility.render.animation;

import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class GearAnimation {
   private float espValue = 1.0F;
   private float prevEspValue;
   private float espSpeed = 1.0F;
   private boolean flipSpeed;

   public float getValue() {
      return Render2DEngine.interpolateFloat(this.prevEspValue, this.espValue, (double)Render3DEngine.getTickDelta());
   }

   public void tick() {
      this.prevEspValue = this.espValue;
      this.espValue = this.espValue + this.espSpeed;
      if (this.espSpeed > (float)ClickGui.gearStop.getValue().intValue()) {
         this.flipSpeed = true;
      }

      if (this.espSpeed < (float)(-ClickGui.gearStop.getValue())) {
         this.flipSpeed = false;
      }

      this.espSpeed = this.flipSpeed ? this.espSpeed - ClickGui.gearDuration.getValue() : this.espSpeed + ClickGui.gearDuration.getValue();
   }
}
