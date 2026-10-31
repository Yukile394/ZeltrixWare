package thunder.hack.features.modules.client;

import java.awt.Color;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;

public final class HudEditor extends Module {
   public static final Setting<Boolean> sticky = new Setting<>("Sticky", true);
   public static final Setting<HudEditor.HudStyle> hudStyle = new Setting<>("HudStyle", HudEditor.HudStyle.Blurry);
   public static final Setting<HudEditor.ArrowsStyle> arrowsStyle = new Setting<>("ArrowsStyle", HudEditor.ArrowsStyle.Default);
   public static final Setting<ClickGui.colorModeEn> colorMode = new Setting<>("ColorMode", ClickGui.colorModeEn.Static);
   public static final Setting<Integer> colorSpeed = new Setting<>("ColorSpeed", 18, 2, 54);
   public static final Setting<Boolean> glow = new Setting<>("Light", true);
   public static final Setting<ColorSetting> hcolor1 = new Setting<>("Color", new ColorSetting(-6974059));
   public static final Setting<ColorSetting> acolor = new Setting<>("Color2", new ColorSetting(-8365735));
   public static final Setting<ColorSetting> plateColor = new Setting<>("PlateColor", new ColorSetting(new Color(-419430400, true).getRGB()));
   public static final Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(new Color(-1, true).getRGB()));
   public static final Setting<ColorSetting> textColor2 = new Setting<>("TextColor2", new ColorSetting(new Color(-1, true).getRGB()));
   public static final Setting<ColorSetting> blurColor = new Setting<>("BlurColor", new ColorSetting(new Color(-16773595, true).getRGB()));
   public static final Setting<Float> hudRound = new Setting<>("HudRound", 4.0F, 1.0F, 7.0F);
   public static final Setting<Float> alpha = new Setting<>("Alpha", 0.9F, 0.0F, 1.0F);
   public static final Setting<Float> blend = new Setting<>("Blend", 10.0F, 1.0F, 15.0F);
   public static final Setting<Float> outline = new Setting<>("Outline", 0.5F, 0.0F, 2.5F);
   public static final Setting<Float> glow1 = new Setting<>("Glow", 0.5F, 0.0F, 1.0F);
   public static final Setting<Float> blurOpacity = new Setting<>("BlurOpacity", 0.55F, 0.0F, 1.0F);
   public static final Setting<Float> blurStrength = new Setting<>("BlurStrength", 20.0F, 5.0F, 50.0F);

   public HudEditor() {
      super("HudEditor", Module.Category.CLIENT);
   }

   public static Color getColor(int count) {
      return switch ((ClickGui.colorModeEn)colorMode.getValue()) {
         case Sky -> Render2DEngine.skyRainbow(colorSpeed.getValue(), count);
         case LightRainbow -> Render2DEngine.rainbow(colorSpeed.getValue(), count, 0.6F, 1.0F, 1.0F);
         case Rainbow -> Render2DEngine.rainbow(colorSpeed.getValue(), count, 1.0F, 1.0F, 1.0F);
         case Fade -> Render2DEngine.fade(colorSpeed.getValue(), count, hcolor1.getValue().getColorObject(), 1.0F);
         case DoubleColor -> Render2DEngine.TwoColoreffect(
         hcolor1.getValue().getColorObject(), acolor.getValue().getColorObject(), (double)colorSpeed.getValue().intValue(), (double)count
      );
         case Analogous -> Render2DEngine.interpolateColorsBackAndForth(
         colorSpeed.getValue(), count, hcolor1.getValue().getColorObject(), Render2DEngine.getAnalogousColor(acolor.getValue().getColorObject()), true
      );
         default -> hcolor1.getValue().getColorObject();
      };
   }

   @Override
   public void onEnable() {
      mc.setScreen(HudEditorGui.getHudGui());
      this.disable();
   }

   public static enum ArrowsStyle {
      Default,
      New;
   }

   public static enum HudStyle {
      Blurry,
      Glowing;
   }
}
