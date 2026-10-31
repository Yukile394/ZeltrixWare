package thunder.hack.features.modules.player;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import thunder.hack.features.modules.Module;
import thunder.hack.injection.accesors.IMinecraftClient;
import thunder.hack.setting.Setting;

public class FastUse extends Module {
   private final Setting<Integer> delay = new Setting<>("Delay", 0, 0, 5);
   public Setting<Boolean> blocks = new Setting<>("Blocks", false);
   public Setting<Boolean> crystals = new Setting<>("Crystals", false);
   public Setting<Boolean> xp = new Setting<>("XP", false);
   public Setting<Boolean> all = new Setting<>("All", true);

   public FastUse() {
      super("FastUse", Module.Category.PLAYER);
   }

   @Override
   public void onUpdate() {
      if (this.check(mc.player.getMainHandStack().getItem()) && ((IMinecraftClient)mc).getUseCooldown() > this.delay.getValue()) {
         ((IMinecraftClient)mc).setUseCooldown(this.delay.getValue());
      }
   }

   public boolean check(Item item) {
      return item instanceof BlockItem && this.blocks.getValue()
         || item == Items.END_CRYSTAL && this.crystals.getValue()
         || item == Items.EXPERIENCE_BOTTLE && this.xp.getValue()
         || this.all.getValue();
   }
}
