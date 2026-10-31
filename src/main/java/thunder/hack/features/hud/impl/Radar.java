package thunder.hack.features.hud.impl;

import java.awt.Color;
import net.minecraft.util.Formatting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.render.NameTags;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class Radar extends HudElement {
   private final Setting<Radar.Mode> mode = new Setting<>("Mode", Radar.Mode.Rect);
   private final Setting<Radar.ColorMode> colorMode = new Setting<>("ColorMode", Radar.ColorMode.Sync);
   private final Setting<Integer> size = new Setting<>("Size", 80, 20, 300);
   private final Setting<ColorSetting> color2 = new Setting<>("Color", new ColorSetting(-15724528));
   private final Setting<ColorSetting> color3 = new Setting<>("PlayerColor", new ColorSetting(-979657829));
   private final Setting<Radar.Component> c1 = new Setting<>("Component1", Radar.Component.Name, v -> this.mode.is(Radar.Mode.Text));
   private final Setting<Radar.Component> c2 = new Setting<>("Component2", Radar.Component.Hp, v -> this.mode.is(Radar.Mode.Text));
   private final Setting<Radar.Component> c3 = new Setting<>("Component3", Radar.Component.Ping, v -> this.mode.is(Radar.Mode.Text));
   private final Setting<Radar.Component> c4 = new Setting<>("Component4", Radar.Component.None, v -> this.mode.is(Radar.Mode.Text));
   private final Setting<Radar.Component> c5 = new Setting<>("Component5", Radar.Component.None, v -> this.mode.is(Radar.Mode.Text));
   private final Setting<Formatting> c12 = new Setting<>("Color1", Formatting.WHITE, v -> this.mode.is(Radar.Mode.Text));
   private final Setting<Formatting> c22 = new Setting<>("Color2", Formatting.WHITE, v -> this.mode.is(Radar.Mode.Text));
   private final Setting<Formatting> c32 = new Setting<>("Color3", Formatting.WHITE, v -> this.mode.is(Radar.Mode.Text));
   private final Setting<Formatting> c42 = new Setting<>("Color4", Formatting.WHITE, v -> this.mode.is(Radar.Mode.Text));

   public Radar() {
      super("Radar", 100, 100);
   }

   @Override
   public void onRender2D(DrawContext context) {
      super.onRender2D(context);
      if (this.mode.getValue() == Radar.Mode.Rect) {
         Render2DEngine.drawHudBase(
            context.getMatrices(),
            this.getPosX(),
            this.getPosY(),
            (float)this.size.getValue().intValue(),
            (float)this.size.getValue().intValue(),
            HudEditor.hudRound.getValue()
         );
         if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
            Render2DEngine.drawRectDumbWay(
               context.getMatrices(),
               this.getPosX(),
               this.getPosY() + (float)this.size.getValue().intValue() / 2.0F + 0.25F,
               this.getPosX() + (float)this.size.getValue().intValue(),
               this.getPosY() + (float)this.size.getValue().intValue() / 2.0F - 0.25F,
               new Color(1426063359, true)
            );
            Render2DEngine.drawRectDumbWay(
               context.getMatrices(),
               this.getPosX() + ((float)this.size.getValue().intValue() / 2.0F - 0.25F),
               this.getPosY() - 0.5F,
               this.getPosX() + (float)this.size.getValue().intValue() / 2.0F + 0.25F,
               this.getPosY() + (float)this.size.getValue().intValue() - 1.0F,
               new Color(1426063359, true)
            );
         } else {
            Render2DEngine.draw2DGradientRect(
               context.getMatrices(),
               (float)((double)this.getPosX() + ((double)((float)this.size.getValue().intValue() / 2.0F) - 0.5)),
               (float)((double)this.getPosY() + 3.5),
               (float)((double)this.getPosX() + (double)((float)this.size.getValue().intValue() / 2.0F) + 0.2),
               (float)((double)(this.getPosY() + (float)this.size.getValue().intValue()) - 3.5),
               this.color2.getValue().getColorObject(),
               this.color2.getValue().getColorObject(),
               this.color2.getValue().getColorObject(),
               this.color2.getValue().getColorObject()
            );
            Render2DEngine.draw2DGradientRect(
               context.getMatrices(),
               this.getPosX() + 3.5F,
               this.getPosY() + ((float)this.size.getValue().intValue() / 2.0F - 0.2F),
               this.getPosX() + (float)this.size.getValue().intValue() - 3.5F,
               this.getPosY() + (float)this.size.getValue().intValue() / 2.0F + 0.5F,
               this.color2.getValue().getColorObject(),
               this.color2.getValue().getColorObject(),
               this.color2.getValue().getColorObject(),
               this.color2.getValue().getColorObject()
            );
         }

         for (PlayerEntity entityPlayer : Managers.ASYNC.getAsyncPlayers()) {
            if (entityPlayer != mc.player) {
               float posX = (float)(
                     entityPlayer.prevX
                        + (entityPlayer.prevX - entityPlayer.getX()) * (double)Render3DEngine.getTickDelta()
                        - mc.player.getX()
                  )
                  * 2.0F;
               float posZ = (float)(
                     entityPlayer.prevZ
                        + (entityPlayer.prevZ - entityPlayer.getZ()) * (double)Render3DEngine.getTickDelta()
                        - mc.player.getZ()
                  )
                  * 2.0F;
               float cos = (float)Math.cos((double)mc.player.getYaw(Render3DEngine.getTickDelta()) * 0.017453292);
               float sin = (float)Math.sin((double)mc.player.getYaw(Render3DEngine.getTickDelta()) * 0.017453292);
               float rotY = -(posZ * cos - posX * sin);
               float rotX = -(posX * cos + posZ * sin);
               if (rotY > (float)this.size.getValue().intValue() / 2.0F - 6.0F) {
                  rotY = (float)this.size.getValue().intValue() / 2.0F - 6.0F;
               } else if (rotY < -((float)this.size.getValue().intValue() / 2.0F - 8.0F)) {
                  rotY = -((float)this.size.getValue().intValue() / 2.0F - 8.0F);
               }

               if (rotX > (float)this.size.getValue().intValue() / 2.0F - 5.0F) {
                  rotX = (float)this.size.getValue().intValue() / 2.0F - 5.0F;
               } else if (rotX < -((float)this.size.getValue().intValue() / 2.0F - 5.0F)) {
                  rotX = -((float)this.size.getValue().intValue() / 2.0F - 5.0F);
               }

               Render2DEngine.drawRound(
                  context.getMatrices(),
                  this.getPosX() + (float)this.size.getValue().intValue() / 2.0F + rotX - 2.0F,
                  this.getPosY() + (float)this.size.getValue().intValue() / 2.0F + rotY - 2.0F,
                  4.0F,
                  4.0F,
                  2.0F,
                  this.color3.getValue().getColorObject()
               );
            }
         }
      }

      if (this.mode.getValue() == Radar.Mode.Text) {
         float offset_y = 0.0F;

         for (PlayerEntity entityPlayerx : Managers.ASYNC.getAsyncPlayers()) {
            if (entityPlayerx != mc.player) {
               String str = String.format(
                  "%s %s %s %s %s",
                  this.getText(this.c1, entityPlayerx),
                  this.getText(this.c2, entityPlayerx),
                  this.getText(this.c3, entityPlayerx),
                  this.getText(this.c4, entityPlayerx),
                  this.getText(this.c5, entityPlayerx)
               );
               if (this.colorMode.getValue() == Radar.ColorMode.Sync) {
                  FontRenderers.sf_bold
                     .drawString(
                        context.getMatrices(),
                        str,
                        (double)this.getPosX(),
                        (double)(this.getPosY() + offset_y),
                        HudEditor.getColor((int)(offset_y * 2.0F)).getRGB()
                     );
               } else {
                  FontRenderers.sf_bold
                     .drawString(context.getMatrices(), str, (double)this.getPosX(), (double)(this.getPosY() + offset_y), this.color2.getValue().getColor());
               }

               offset_y += FontRenderers.sf_bold.getFontHeight(str);
            }
         }
      }

      this.setBounds(this.getPosX(), this.getPosY(), (float)this.size.getValue().intValue(), (float)this.size.getValue().intValue());
   }

   public String getText(Setting<Radar.Component> c, PlayerEntity player) {
      switch ((Radar.Component)c.getValue()) {
         case Name:
            return this.c12.getValue() + player.getName().getString() + Formatting.RESET;
         case Hp:
            int health = (int)Math.ceil((double)(player.getHealth() + player.getAbsorptionAmount()));
            return ModuleManager.nameTags.getHealthColor((float)health) + health + Formatting.RESET;
         case Distance:
            return "" + this.c32.getValue() + (int)Math.ceil((double)mc.player.distanceTo(player)) + "m" + Formatting.RESET;
         case Ping:
            return "" + this.c22.getValue() + NameTags.getEntityPing(player) + "ms" + Formatting.RESET;
         case TotemPops:
            return "" + this.c42.getValue() + Managers.COMBAT.getPops(player) + Formatting.RESET;
         default:
            return "";
      }
   }

   public static enum ColorMode {
      Sync,
      Custom;
   }

   public static enum Component {
      Name,
      Hp,
      Distance,
      Ping,
      TotemPops,
      None;
   }

   private static enum Mode {
      Rect,
      Text;
   }
}
