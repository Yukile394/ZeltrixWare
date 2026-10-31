package thunder.hack.features.hud.impl;

import java.awt.Color;
import java.lang.invoke.StringConcatFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.util.Formatting;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Pair;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.registry.RegistryKeys;
import org.apache.commons.lang3.StringUtils;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.font.FontRenderer;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.math.MathUtility;

public class LegacyHud extends Module {
   private static final ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
   private final Setting<LegacyHud.Font> customFont = new Setting<>("Font", LegacyHud.Font.Minecraft);
   private final Setting<ColorSetting> colorSetting = new Setting<>("Color", new ColorSetting(new Color(30719)));
   private final Setting<Boolean> renderingUp = new Setting<>("RenderingUp", false);
   private final Setting<Boolean> waterMark = new Setting<>("Watermark", false);
   private final Setting<Boolean> arrayList = new Setting<>("ActiveModules", false);
   private final Setting<Boolean> coords = new Setting<>("Coords", false);
   private final Setting<Boolean> direction = new Setting<>("Direction", false);
   private final Setting<Boolean> armor = new Setting<>("Armor", false);
   private final Setting<Boolean> totems = new Setting<>("Totems", false);
   private final Setting<Boolean> greeter = new Setting<>("Welcomer", false);
   private final Setting<Boolean> speed = new Setting<>("Speed", false);
   private final Setting<Boolean> bps = new Setting<>("BPS", false, v -> this.speed.getValue());
   public final Setting<Boolean> potions = new Setting<>("Potions", false);
   private final Setting<Boolean> ping = new Setting<>("Ping", false);
   private final Setting<Boolean> tps = new Setting<>("TPS", false);
   private final Setting<Boolean> extraTps = new Setting<>("ExtraTPS", true, v -> this.tps.getValue());
   private final Setting<Boolean> offhandDurability = new Setting<>("OffhandDurability", false);
   private final Setting<Boolean> mainhandDurability = new Setting<>("MainhandDurability", false);
   private final Setting<Boolean> fps = new Setting<>("FPS", false);
   private final Setting<Boolean> chests = new Setting<>("Chests", false);
   private final Setting<Boolean> worldTime = new Setting<>("WorldTime", false);
   private final Setting<Boolean> biome = new Setting<>("Biome", false);
   public Setting<Boolean> time = new Setting<>("Time", false);
   public Setting<Integer> waterMarkY = new Setting<>("WatermarkPosY", 2, 0, 20, v -> this.waterMark.getValue());
   private int color;

   public LegacyHud() {
      super("LegacyHud", Module.Category.HUD);
   }

   @Override
   public void onRender2D(DrawContext context) {
      if (!fullNullCheck()) {
         int width = mc.getWindow().getScaledWidth();
         int height = mc.getWindow().getScaledHeight();

         int offset = switch ((LegacyHud.Font)this.customFont.getValue()) {
            case Minecraft -> 10;
            case Monsterrat -> 9;
            default -> 8;
         };
         this.color = this.colorSetting.getValue().getColor();
         if (this.waterMark.getValue()) {
            this.drawText(context, "thunderhack v1.7b2407", 2, this.waterMarkY.getValue());
         }

         int j = mc.currentScreen instanceof ChatScreen && !this.renderingUp.getValue() ? 14 : 0;
         if (this.arrayList.getValue()) {
            for (Module module : Managers.MODULE
               .getEnabledModules()
               .stream()
               .filter(Module::isDrawn)
               .sorted(Comparator.comparing(modulex -> this.getStringWidth(modulex.getFullArrayString()) * -1))
               .toList()) {
               if (module.isDrawn()) {
                  String str = module.getDisplayName()
                     + Formatting.GRAY
                     + (module.getDisplayInfo() != null ? " [" + Formatting.WHITE + module.getDisplayInfo() + Formatting.GRAY + "]" : "");
                  if (this.renderingUp.getValue()) {
                     this.drawText(context, str, width - 2 - this.getStringWidth(str), 2 + j * offset);
                     j++;
                  } else {
                     j += offset;
                     this.drawText(context, str, width - 2 - this.getStringWidth(str), height - j);
                  }
               }
            }
         }

         int i = mc.currentScreen instanceof ChatScreen && this.renderingUp.getValue() ? 13 : (this.renderingUp.getValue() ? -2 : 0);
         if (this.potions.getValue()) {
            for (StatusEffectInstance potionEffect : new ArrayList<>(mc.player.getStatusEffects())) {
               StatusEffect potion = (StatusEffect)potionEffect.getEffectType().value();
               String power = "";
               switch (potionEffect.getAmplifier()) {
                  case 0:
                     power = "I";
                     break;
                  case 1:
                     power = "II";
                     break;
                  case 2:
                     power = "III";
                     break;
                  case 3:
                     power = "IV";
                     break;
                  case 4:
                     power = "V";
               }

               String s = potion.getName().getString() + " " + power;
               String s2 = ("" + (PotionHud.getDuration(potionEffect)));
               Color c = new Color(((StatusEffect)potionEffect.getEffectType().value()).getColor());
               if (this.renderingUp.getValue()) {
                  i += offset;
                  this.drawText(context, s + " " + s2, width - this.getStringWidth(s + " " + s2) - 2, height - 2 - i, c.getRGB());
               } else {
                  this.drawText(context, s + " " + s2, width - this.getStringWidth(s + " " + s2) - 2, 2 + i++ * offset, c.getRGB());
               }
            }
         }

         if (this.worldTime.getValue()) {
            String str2 = "WorldTime: " + Formatting.WHITE + mc.world.getTimeOfDay() % 24000L;
            this.drawText(context, str2, width - this.getStringWidth(str2) - 2, this.renderingUp.getValue() ? height - 2 - (i += offset) : 2 + i++ * offset);
         }

         if (this.mainhandDurability.getValue()) {
            String str = "MainHand"
               + Formatting.WHITE
               + " ["
               + (mc.player.getMainHandStack().getMaxDamage() - mc.player.getMainHandStack().getDamage())
               + "]";
            this.drawText(context, str, width - this.getStringWidth(str) - 2, this.renderingUp.getValue() ? height - 2 - (i += offset) : 2 + i++ * offset);
         }

         if (this.tps.getValue()) {
            String str = "TPS " + Formatting.WHITE + Managers.SERVER.getTPS() + (this.extraTps.getValue() ? " [" + Managers.SERVER.getTPS2() + "]" : "");
            this.drawText(context, str, width - this.getStringWidth(str) - 2, this.renderingUp.getValue() ? height - 2 - (i += offset) : 2 + i++ * offset);
         }

         if (this.speed.getValue()) {
            String str = "Speed "
               + Formatting.WHITE
               + MathUtility.round(Managers.PLAYER.currentPlayerSpeed * (this.bps.getValue() ? 20.0F : 72.0F) * ThunderHack.TICK_TIMER)
               + (this.bps.getValue() ? " b/s" : " km/h");
            this.drawText(context, str, width - this.getStringWidth(str) - 2, this.renderingUp.getValue() ? height - 2 - (i += offset) : 2 + i++ * offset);
         }

         if (this.chests.getValue()) {
            Pair<Integer, Integer> chests = ModuleManager.chestCounter.getChestCount();
            String str = "Chests: " + Formatting.WHITE + "S:" + chests.getLeft() + " D:" + chests.getRight();
            this.drawText(context, str, width - this.getStringWidth(str) - 2, this.renderingUp.getValue() ? height - 2 - (i += offset) : 2 + i++ * offset);
         }

         if (this.biome.getValue()) {
            String str3 = "Biome: " + Formatting.WHITE + biome();
            this.drawText(context, str3, width - this.getStringWidth(str3) - 2, this.renderingUp.getValue() ? height - 2 - (i += offset) : 2 + i++ * offset);
         }

         if (this.time.getValue()) {
            String str = "Time " + Formatting.WHITE + new SimpleDateFormat("h:mm a").format(new Date());
            this.drawText(context, str, width - this.getStringWidth(str) - 2, this.renderingUp.getValue() ? height - 2 - (i += offset) : 2 + i++ * offset);
         }

         if (this.offhandDurability.getValue()) {
            String str = "OffHand"
               + Formatting.WHITE
               + " ["
               + (mc.player.getOffHandStack().getMaxDamage() - mc.player.getOffHandStack().getDamage())
               + "]";
            this.drawText(context, str, width - this.getStringWidth(str) - 2, this.renderingUp.getValue() ? height - 2 - (i += offset) : 2 + i++ * offset);
         }

         if (this.ping.getValue()) {
            String str1 = "Ping " + Formatting.WHITE + Managers.SERVER.getPing();
            this.drawText(context, str1, width - this.getStringWidth(str1) - 2, this.renderingUp.getValue() ? height - 2 - (i += offset) : 2 + i++ * offset);
         }

         if (this.fps.getValue()) {
            String fpsText = "FPS " + Formatting.WHITE + FrameRateCounter.INSTANCE.getFps();
            int var17;
            this.drawText(
               context, fpsText, width - this.getStringWidth(fpsText) - 2, this.renderingUp.getValue() ? height - 2 - (var17 = i + offset) : 2 + i++ * offset
            );
         }

         boolean inHell = Objects.equals(mc.world.getRegistryKey().getValue().getPath(), "the_nether");
         int posX = (int)mc.player.getX();
         int posY = (int)mc.player.getY();
         int posZ = (int)mc.player.getZ();
         float nether = !inHell ? 0.125F : 8.0F;
         int hposX = (int)(mc.player.getX() * (double)nether);
         int hposZ = (int)(mc.player.getZ() * (double)nether);
         i = mc.currentScreen instanceof ChatScreen ? 14 : 0;
         String coordinates = Formatting.WHITE
            + "XYZ "
            + Formatting.RESET
            + (
               inHell
                  ? posX
                     + ", "
                     + posY
                     + ", "
                     + posZ
                     + Formatting.WHITE
                     + " ["
                     + Formatting.RESET
                     + hposX
                     + ", "
                     + hposZ
                     + Formatting.WHITE
                     + "]"
                     + Formatting.RESET
                  : posX + ", " + posY + ", " + posZ + Formatting.WHITE + " [" + Formatting.RESET + hposX + ", " + hposZ + Formatting.WHITE + "]"
            );
         String direction1 = "";
         i += offset;
         if (this.direction.getValue()) {
            switch (mc.player.getHorizontalFacing()) {
               case EAST:
                  direction1 = "East" + Formatting.WHITE + " [+X]";
                  break;
               case WEST:
                  direction1 = "West" + Formatting.WHITE + " [-X]";
                  break;
               case NORTH:
                  direction1 = "North" + Formatting.WHITE + " [-Z]";
                  break;
               case SOUTH:
                  direction1 = "South" + Formatting.WHITE + " [+Z]";
            }

            this.drawText(context, direction1, 2, height - i - 11);
         }

         if (this.coords.getValue()) {
            this.drawText(context, coordinates, 2, height - i);
         }

         if (this.armor.getValue()) {
            this.renderArmorHUD(true, context);
         }

         if (this.totems.getValue()) {
            this.renderTotemHUD(context);
         }

         if (this.greeter.getValue()) {
            this.renderGreeter(context);
         }
      }
   }

   private void drawText(DrawContext context, String str, int x, int y, int color) {
      if (!this.customFont.getValue().equals(LegacyHud.Font.Minecraft)) {
         FontRenderer adapter = switch ((LegacyHud.Font)this.customFont.getValue()) {
            case Monsterrat -> FontRenderers.monsterrat;
            case SF -> FontRenderers.sf_medium;
            default -> FontRenderers.modules;
         };
         adapter.drawString(context.getMatrices(), str.replace(Formatting.WHITE + "", ""), (double)x + 0.5, (double)y + 0.5, Color.BLACK.getRGB());
         adapter.drawString(context.getMatrices(), str, (double)x, (double)y, color);
      } else {
         context.drawText(mc.textRenderer, str, x, y, color, true);
      }
   }

   private void drawText(DrawContext context, String str, int x, int y) {
      if (!this.customFont.getValue().equals(LegacyHud.Font.Minecraft)) {
         FontRenderer adapter = switch ((LegacyHud.Font)this.customFont.getValue()) {
            case Monsterrat -> FontRenderers.monsterrat;
            case SF -> FontRenderers.sf_medium;
            default -> FontRenderers.modules;
         };
         adapter.drawString(context.getMatrices(), str.replace(Formatting.WHITE + "", ""), (double)x + 0.5, (double)y + 0.5, Color.BLACK.getRGB());
         adapter.drawString(context.getMatrices(), str, (double)x, (double)y, this.color);
      } else {
         context.drawText(mc.textRenderer, str, x, y, this.color, true);
      }
   }

   private int getStringWidth(String str) {
      switch ((LegacyHud.Font)this.customFont.getValue()) {
         case Minecraft:
            return mc.textRenderer.getWidth(str);
         case Comfortaa:
         default:
            return (int)FontRenderers.modules.getStringWidth(str);
         case Monsterrat:
            return (int)FontRenderers.monsterrat.getStringWidth(str);
         case SF:
            return (int)FontRenderers.sf_medium.getStringWidth(str);
      }
   }

   public void renderGreeter(DrawContext context) {
      String text = "Good " + getTimeOfDay() + mc.player.getName().getString();
      this.drawText(context, text, (int)((float)mc.getWindow().getScaledWidth() / 2.0F - (float)this.getStringWidth(text) / 2.0F + 2.0F), 2);
   }

   public static String getTimeOfDay() {
      int timeOfDay = Calendar.getInstance().get(11);
      if (timeOfDay < 12) {
         return "Morning ";
      } else if (timeOfDay < 16) {
         return "Afternoon ";
      } else {
         return timeOfDay < 21 ? "Evening " : "Night ";
      }
   }

   public void renderTotemHUD(DrawContext context) {
      int width = mc.getWindow().getScaledWidth();
      int height = mc.getWindow().getScaledHeight();
      int totems = mc.player
         .getInventory()
         .main
         .stream()
         .filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING)
         .mapToInt(ItemStack::getCount)
         .sum();
      int u = mc.player.getMaxAir();
      int v = Math.min(mc.player.getAir(), u);
      if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
         totems += mc.player.getOffHandStack().getCount();
      }

      if (totems > 0) {
         int i = width / 2;
         int y = height - 55 - (!mc.player.isSubmergedInWater() && v >= u ? 0 : 10);
         int x = i - 189 + 180 + 2;
         context.drawItem(totem, x, y);
         context.drawItemInSlot(mc.textRenderer, totem, x, y);
         this.drawText(context, totems + "", 8 + (int)((float)x - (float)this.getStringWidth(totems + "") / 2.0F), y - 7, 16777215);
      }
   }

   private static String biome() {
      if (mc.player != null && mc.world != null) {
         Identifier id = mc.world
            .getRegistryManager()
            .get(RegistryKeys.BIOME)
            .getId((Biome)mc.world.getBiome(mc.player.getBlockPos()).value());
         return id == null
            ? "Unknown"
            : Arrays.stream(id.getPath().split("_")).<CharSequence>map(StringUtils::capitalize).collect(Collectors.joining(" "));
      } else {
         return null;
      }
   }

   public void renderArmorHUD(boolean percent, DrawContext context) {
      int i = 0;
      int u = mc.player.getMaxAir();
      int v = Math.min(mc.player.getAir(), u);
      int y = mc.getWindow().getScaledHeight() - 55 - (!mc.player.isSubmergedInWater() && v >= u ? 0 : 10);

      for (ItemStack is : mc.player.getInventory().armor) {
         i++;
         if (!is.isEmpty()) {
            int x = mc.getWindow().getScaledWidth() / 2 - 90 + (9 - i) * 20 + 2;
            context.drawItem(is, x, y);
            context.drawItemInSlot(mc.textRenderer, is, x, y);
            String s = is.getCount() > 1 ? is.getCount() + "" : "";
            this.drawText(context, s, x + 19 - 2 - this.getStringWidth(s), y + 9, 16777215);
            if (percent) {
               float green = (float)(is.getMaxDamage() - is.getDamage()) / (float)is.getMaxDamage();
               float red = 1.0F - green;
               int dmg = 100 - (int)(red * 100.0F);
               this.drawText(
                  context,
                  dmg + "",
                  x + 8 - this.getStringWidth(dmg + "") / 2,
                  y - 11,
                  new Color((int)MathUtility.clamp(red * 255.0F, 0.0F, 255.0F), (int)MathUtility.clamp(green * 255.0F, 0.0F, 255.0F), 0).getRGB()
               );
            }
         }
      }
   }

   private static enum Font {
      Minecraft,
      Comfortaa,
      Monsterrat,
      SF;
   }
}
