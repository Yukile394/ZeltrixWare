package thunder.hack.events.impl;

import thunder.hack.events.Event;

public class EventMouse extends Event {
   int button;
   int action;

   public int getButton() {
      return this.button;
   }

   public int getAction() {
      return this.action;
   }

   public EventMouse(int b, int action) {
      this.button = b;
      this.action = action;
   }
}
