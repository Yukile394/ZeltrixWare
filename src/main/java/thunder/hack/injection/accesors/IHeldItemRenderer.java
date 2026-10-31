package thunder.hack.injection.accesors;

import net.minecraft.item.ItemStack;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({HeldItemRenderer.class})
public interface IHeldItemRenderer {
   @Accessor("equipProgressMainHand")
   void setEquippedProgressMainHand(float var1);

   @Accessor("equipProgressOffHand")
   void setEquippedProgressOffHand(float var1);

   @Accessor("equipProgressMainHand")
   float getEquippedProgressMainHand();

   @Accessor("equipProgressOffHand")
   float getEquippedProgressOffHand();

   @Accessor("mainHand")
   void setItemStackMainHand(ItemStack var1);

   @Accessor("offHand")
   void setItemStackOffHand(ItemStack var1);
}
