package thunder.hack.injection;

import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.player.FriendManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.misc.NameProtect;

@Mixin({TextVisitFactory.class})
public class MixinTextVisitFactory {
   @ModifyArg(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
         ordinal = 0
      ),
      method = {"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"},
      index = 0
   )
   private static String adjustText(String text) {
      return protect(text);
   }

   private static String protect(String string) {
      if (ModuleManager.nameProtect.isEnabled() && Module.mc.player != null) {
         String me = Module.mc.getSession().getUsername();
         return !string.contains(me) && (!FriendManager.friends.stream().anyMatch(i -> i.contains(string)) || !NameProtect.hideFriends.getValue())
            ? string
            : string.replace(me, NameProtect.getCustomName());
      } else {
         return string;
      }
   }
}
