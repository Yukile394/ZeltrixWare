package thunder.hack.injection;

import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.SkinTextures.Model;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.movement.CustomModel;
import thunder.hack.utility.render.TextureStorage;

@Mixin({AbstractClientPlayerEntity.class})
public class MixinAbstractClientPlayerEntity {
   @Inject(
      method = {"getSkinTextures"},
      at = {@At("RETURN")},
      cancellable = true
   )
   public void onGetSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
      if ((Object)this == MinecraftClient.getInstance().player && ModuleManager.customModel != null && ModuleManager.customModel.isEnabled()) {
         Identifier texture = switch ((CustomModel.Mode)ModuleManager.customModel.mode.getValue()) {
            case Tavsan -> Identifier.of("thunderhack", "textures/rabbit.png");
            case Boykisser -> TextureStorage.boykisser;
            case Paimon -> TextureStorage.paimon;
            case Cutie -> TextureStorage.cutie;
            default -> Identifier.of("thunderhack", "textures/yakuptv.png");
         };
         SkinTextures current = (SkinTextures)cir.getReturnValue();
         if (current == null) {
            cir.setReturnValue(new SkinTextures(texture, null, null, null, Model.WIDE, false));
         } else {
            cir.setReturnValue(new SkinTextures(texture, null, current.capeTexture(), current.elytraTexture(), Model.WIDE, current.secure()));
         }
      }
   }
}
