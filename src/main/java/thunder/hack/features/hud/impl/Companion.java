package thunder.hack.features.hud.impl;

import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class Companion extends HudElement {
   public Setting<Integer> scale = new Setting<>("Scale", 50, 0, 100);
   public Setting<Companion.Mode> mode = new Setting<>("Mode", Companion.Mode.Boykisser);
   public static int currentFrame;
   private String message = "";
   private Timer lastPop = new Timer();
   private Timer frameRate = new Timer();

   public Companion() {
      super("2DCompanion", 50, 10);
   }

   @Override
   public void onUpdate() {
      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player != mc.player && !(player.getHealth() > 0.0F) && Managers.COMBAT.popList.containsKey(player.getName().getString())) {
            if (ClientSettings.isRu()) {
               this.message = player.getName().getString()
                  + " \u043f\u043e\u043f\u043d\u0443\u043b "
                  + (
                     Managers.COMBAT.popList.get(player.getName().getString()) > 1
                        ? Managers.COMBAT.popList.get(player.getName().getString())
                           + " \u0442\u043e\u0442\u0435\u043c\u043e\u0432 \u0438 \u0441\u0434\u043e\u0445! \u0418\u0417\u0417\u0417\u0417\u0418\u0418"
                        : "\u0442\u043e\u0442\u0435\u043c \u0438 \u0441\u0434\u043e\u0445! \u0418\u0417\u0417\u0417\u0417\u0418\u0418"
                  );
            } else {
               this.message = player.getName().getString()
                  + " popped "
                  + (
                     Managers.COMBAT.popList.get(player.getName().getString()) > 1
                        ? Managers.COMBAT.popList.get(player.getName().getString()) + " totems and died EZ LMAO!"
                        : "totem and died EZ LMAO!"
                  );
            }

            this.lastPop.reset();
         }
      }
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      context.getMatrices().push();
      context.getMatrices().translate((float)((int)this.getPosX() + 100), (float)((int)this.getPosY() + 100), 0.0F);
      context.getMatrices().scale((float)this.scale.getValue().intValue() / 100.0F, (float)this.scale.getValue().intValue() / 100.0F, 1.0F);
      context.getMatrices().translate((float)(-((int)this.getPosX() + 100)), (float)(-((int)this.getPosY() + 100)), 0.0F);
      if (this.mode.getValue() == Companion.Mode.Boykisser) {
         context.drawTexture(TextureStorage.boykisser, (int)this.getPosX(), (int)this.getPosY(), 0.0F, (float)(currentFrame * 128), 130, 128, 130, 6784);
      } else if (this.mode.getValue() == Companion.Mode.Paimon) {
         context.drawTexture(TextureStorage.paimon, (int)this.getPosX(), (int)this.getPosY(), 0.0F, (float)(currentFrame * 200), 200, 200, 200, 10600);
      } else if (this.mode.getValue() == Companion.Mode.Baltika) {
         context.drawTexture(TextureStorage.baltika, (int)this.getPosX(), (int)this.getPosY(), 0.0F, 0.0F, 421, 800, 421, 800);
      } else if (this.mode.getValue() == Companion.Mode.Kowk) {
         context.drawTexture(TextureStorage.kowk, (int)this.getPosX(), (int)this.getPosY(), 0.0F, 0.0F, 287, 252, 287, 252);
      }

      context.getMatrices().pop();
      if (!this.lastPop.passedMs(2000L)) {
         float w = FontRenderers.sf_bold.getStringWidth(this.message) + 8.0F;
         float factor = MathUtility.clamp((float)this.lastPop.getPassedTimeMs(), 0.0F, 500.0F) / 500.0F;
         Render2DEngine.drawRound(
            context.getMatrices(),
            this.getPosX() + (float)this.scale.getValue().intValue() / 3.0F,
            this.getPosY() + 70.0F - (float)this.scale.getValue().intValue(),
            factor * w,
            10.0F,
            3.0F,
            new Color(16570333)
         );
         Render2DEngine.addWindow(
            context.getMatrices(),
            this.getPosX() + (float)this.scale.getValue().intValue() / 3.0F,
            this.getPosY() + 72.0F - (float)this.scale.getValue().intValue(),
            factor * w + this.getPosX() + (float)this.scale.getValue().intValue() / 3.0F,
            20.0F + this.getPosY() + 72.0F - (float)this.scale.getValue().intValue(),
            1.0
         );
         FontRenderers.sf_bold
            .drawString(
               context.getMatrices(),
               this.message,
               (double)(this.getPosX() + 2.0F + (float)this.scale.getValue().intValue() / 3.0F),
               (double)(this.getPosY() + 72.0F - (float)this.scale.getValue().intValue()),
               new Color(4737096).getRGB()
            );
         Render2DEngine.popWindow();
      }

      if (this.frameRate.passedMs(64L)) {
         this.frameRate.reset();
         currentFrame++;
         if (currentFrame > 52) {
            currentFrame = 0;
         }
      }

      if (this.mode.getValue() == Companion.Mode.Baltika) {
         this.setBounds(
            this.getPosX() + 100.0F, this.getPosY() + 100.0F, (float)this.scale.getValue().intValue() * 3.0F, (float)this.scale.getValue().intValue() * 3.0F
         );
      } else {
         this.setBounds(this.getPosX(), this.getPosY(), (float)this.scale.getValue().intValue() * 3.0F, (float)this.scale.getValue().intValue() * 3.0F);
      }
   }

   @EventHandler
   public void onTotemPop(@NotNull TotemPopEvent event) {
      if (event.getEntity() != mc.player) {
         if (ClientSettings.isRu()) {
            this.message = event.getEntity().getName().getString()
               + " \u043f\u043e\u043f\u043d\u0443\u043b "
               + (event.getPops() > 1 ? event.getPops() + " \u0442\u043e\u0442\u0435\u043c\u043e\u0432!" : "\u0442\u043e\u0442\u0435\u043c!");
         } else {
            this.message = event.getEntity().getName().getString() + " popped " + (event.getPops() > 1 ? event.getPops() + " totems!" : " a totem!");
         }

         this.lastPop.reset();
      }
   }

   private static enum Mode {
      Boykisser,
      Paimon,
      Baltika,
      Kowk;
   }
}
