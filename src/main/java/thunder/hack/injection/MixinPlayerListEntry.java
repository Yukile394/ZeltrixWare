package thunder.hack.injection;

import com.mojang.authlib.GameProfile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;
import net.minecraft.util.Util;
import net.minecraft.util.Identifier;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.utility.OptifineCapes;
import thunder.hack.utility.ThunderUtility;

@Mixin({PlayerListEntry.class})
public class MixinPlayerListEntry {
   @Unique
   private boolean loadedCapeTexture;
   @Unique
   private Identifier customCapeTexture;

   @Inject(
      method = {"<init>(Lcom/mojang/authlib/GameProfile;Z)V"},
      at = {@At("TAIL")}
   )
   private void initHook(GameProfile profile, boolean secureChatEnforced, CallbackInfo ci) {
      this.getTexture(profile);
   }

   @Inject(
      method = {"getSkinTextures"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private void getCapeTexture(CallbackInfoReturnable<SkinTextures> cir) {
      if (this.customCapeTexture != null) {
         SkinTextures prev = (SkinTextures)cir.getReturnValue();
         SkinTextures newTextures = new SkinTextures(
            prev.texture(), prev.textureUrl(), this.customCapeTexture, this.customCapeTexture, prev.model(), prev.secure()
         );
         cir.setReturnValue(newTextures);
      }
   }

   @Unique
   private void getTexture(GameProfile profile) {
      if (!this.loadedCapeTexture) {
         this.loadedCapeTexture = true;
         Util.getMainWorkerExecutor().execute(() -> {
            try {
               URL capesList = new URL("https://raw.githubusercontent.com/Pan4ur/THRecodeUtil/main/capes/capeBase.txt");
               BufferedReader in = new BufferedReader(new InputStreamReader(capesList.openStream()));

               String inputLine;
               while ((inputLine = in.readLine()) != null) {
                  String colune = inputLine.trim();
                  String name = colune.split(":")[0];
                  String cape = colune.split(":")[1];
                  if (Objects.equals(profile.getName(), name)) {
                     this.customCapeTexture = Identifier.of("thunderhack", "textures/capes/" + cape + ".png");
                     return;
                  }
               }
            } catch (Exception var8) {
            }

            for (String str : ThunderUtility.starGazer) {
               if (profile.getName().toLowerCase().equals(str.toLowerCase())) {
                  this.customCapeTexture = Identifier.of("thunderhack", "textures/capes/starcape.png");
               }
            }

            if (ModuleManager.optifineCapes.isEnabled()) {
               OptifineCapes.loadPlayerCape(profile, id -> this.customCapeTexture = id);
            }
         });
      }
   }
}
