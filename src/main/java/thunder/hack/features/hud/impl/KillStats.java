package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

public class KillStats extends HudElement {
   int death = 0;
   int killstreak = 0;
   int kills = 0;

   public KillStats() {
      super("KillStats", 100, 35);
   }

   @Override
   public void onDisable() {
      this.death = 0;
      this.kills = 0;
      this.killstreak = 0;
   }

   @EventHandler
   private void death(PacketEvent.Receive event) {
      if (event.getPacket() instanceof EntityStatusS2CPacket pac && pac.getStatus() == 3) {
         if (!(pac.getEntity(mc.world) instanceof PlayerEntity)) {
            return;
         }

         if (pac.getEntity(mc.world) == mc.player) {
            this.death++;
            this.killstreak = 0;
         } else if (Aura.target == pac.getEntity(mc.world)) {
            this.killstreak++;
            this.kills++;
         }
      }
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      String streak = "KillStreak: " + Formatting.WHITE + this.killstreak;
      String kd = " KD: " + Formatting.WHITE + MathUtility.round((float)this.kills / (float)(this.death > 0 ? this.death : 1));
      float pX = this.getPosX() > (float)mc.getWindow().getScaledWidth() / 2.0F
         ? this.getPosX() - FontRenderers.getModulesRenderer().getStringWidth(streak) - FontRenderers.getModulesRenderer().getStringWidth(kd)
         : this.getPosX();
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         Render2DEngine.drawRoundedBlur(
            context.getMatrices(),
            pX,
            this.getPosY(),
            FontRenderers.getModulesRenderer().getStringWidth(streak) + FontRenderers.getModulesRenderer().getStringWidth(kd) + 21.0F,
            13.0F,
            3.0F,
            HudEditor.blurColor.getValue().getColorObject()
         );
         Render2DEngine.drawRect(context.getMatrices(), pX + 14.0F, this.getPosY() + 2.0F, 0.5F, 8.0F, new Color(1157627903, true));
         Render2DEngine.setupRender();
         RenderSystem.setShaderTexture(0, TextureStorage.swordIcon);
         Render2DEngine.renderGradientTexture(
            context.getMatrices(),
            (double)(pX + 2.0F),
            (double)(this.getPosY() + 1.0F),
            10.0,
            10.0,
            0.0F,
            0.0F,
            16.0,
            16.0,
            16.0,
            16.0,
            HudEditor.getColor(270),
            HudEditor.getColor(0),
            HudEditor.getColor(180),
            HudEditor.getColor(90)
         );
         Render2DEngine.endRender();
      }

      FontRenderers.getModulesRenderer()
         .drawString(context.getMatrices(), streak, (double)(pX + 18.0F), (double)(this.getPosY() + 5.0F), HudEditor.getColor(1).getRGB());
      FontRenderers.getModulesRenderer()
         .drawString(
            context.getMatrices(),
            kd,
            (double)(pX + 18.0F + FontRenderers.getModulesRenderer().getStringWidth(streak)),
            (double)(this.getPosY() + 5.0F),
            HudEditor.getColor(1).getRGB()
         );
      this.setBounds(
         pX, this.getPosY(), FontRenderers.getModulesRenderer().getStringWidth(streak) + FontRenderers.getModulesRenderer().getStringWidth(kd) + 21.0F, 13.0F
      );
   }
}
