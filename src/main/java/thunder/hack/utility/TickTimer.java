package thunder.hack.utility;

import thunder.hack.core.manager.IManager;

public class TickTimer {
   private int time;

   public TickTimer() {
      this.reset();
   }

   public boolean passedTicks(long t) {
      if (this.getPassedTicks() < 0) {
         this.reset();
      }

      return (long)this.getPassedTicks() >= t;
   }

   public boolean every(long ms) {
      if (this.getPassedTicks() < 0) {
         this.reset();
      }

      boolean passed = (long)this.getPassedTicks() >= ms;
      if (passed) {
         this.reset();
      }

      return passed;
   }

   public void set(int t) {
      this.time = t;
   }

   public void reset() {
      this.time = IManager.mc.player == null ? 0 : IManager.mc.player.age;
   }

   private int getPassedTicks() {
      return IManager.mc.player == null ? 0 : IManager.mc.player.age - this.time;
   }
}
