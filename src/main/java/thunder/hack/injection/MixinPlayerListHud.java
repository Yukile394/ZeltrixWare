package thunder.hack.injection;

import java.util.Comparator;
import java.util.List;
import net.minecraft.world.GameMode;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Nullables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.utility.render.animation.AnimationUtility;

@Mixin({PlayerListHud.class})
public class MixinPlayerListHud {
   private static final Comparator<Object> ENTRY_ORDERING = Comparator.<Object>comparingInt(
         entry -> ((PlayerListEntry)entry).getGameMode() == GameMode.SPECTATOR ? 1 : 0
      )
      .thenComparing(entry -> (String)Nullables.mapOrElse(((PlayerListEntry)entry).getScoreboardTeam(), Team::getName, ""))
      .thenComparing(entry -> ((PlayerListEntry)entry).getProfile().getName(), String::compareToIgnoreCase);
   private float animation = 0.0F;

   @Inject(
      method = {"collectPlayerEntries"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void collectPlayerEntriesHook(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
      if (!ClientSettings.futureCompatibility.getValue()) {
         if (!ThunderHack.isFuturePresent()) {
            cir.setReturnValue(Module.mc.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(80L).toList());
         }
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   public void renderHead(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
      if (ModuleManager.smoothTab.isEnabled()) {
         this.animation = AnimationUtility.fast(this.animation, 1.0F, ModuleManager.smoothTab.animationSpeed.getValue());
         context.getMatrices().push();
         context.getMatrices().translate(0.0F, -1000.0F * (1.0F - this.animation), 0.0F);
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   public void renderReturn(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
      if (ModuleManager.smoothTab.isEnabled()) {
         context.getMatrices().pop();
      } else {
         this.animation = 0.0F;
      }
   }
}
