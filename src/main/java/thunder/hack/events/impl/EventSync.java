package thunder.hack.events.impl;

import thunder.hack.events.Event;

public class EventSync extends Event {
   float yaw;
   float pitch;
   Runnable postAction;

   public EventSync(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public void addPostAction(Runnable r) {
      this.postAction = r;
   }

   public Runnable getPostAction() {
      return this.postAction;
   }
}
