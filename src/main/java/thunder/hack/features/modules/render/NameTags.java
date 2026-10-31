package thunder.hack.features.modules.render;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.Formatting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.GameMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.MutableText;
import net.minecraft.registry.RegistryKey;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.DataComponentTypes;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL11;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.player.FriendManager;
import thunder.hack.features.hud.impl.PotionHud;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.misc.NameProtect;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

public class NameTags extends Module {
   private final Map<RegistryKey<Enchantment>, String> encMap = new HashMap<>();
   private final Setting<Boolean> self = new Setting<>("Self", false);
   private final Setting<Float> scale = new Setting<>("Scale", 1.0F, 0.1F, 10.0F);
   private final Setting<Boolean> resize = new Setting<>("Resize", false);
   private final Setting<Float> height = new Setting<>("Height", 2.0F, 0.1F, 10.0F);
   private final Setting<Boolean> gamemode = new Setting<>("Gamemode", false);
   private final Setting<Boolean> spawners = new Setting<>("SpawnerNameTag", false);
   private final Setting<Boolean> entityOwner = new Setting<>("EntityOwner", false);
   private final Setting<Boolean> ping = new Setting<>("Ping", false);
   private final Setting<Boolean> hp = new Setting<>("HP", true);
   private final Setting<Boolean> distance = new Setting<>("Distance", true);
   private final Setting<Boolean> pops = new Setting<>("TotemPops", true);
   private final Setting<NameTags.OutlineColor> outline = new Setting<>("OutlineType", NameTags.OutlineColor.New);
   private final Setting<NameTags.OutlineColor> friendOutline = new Setting<>("FriendOutline", NameTags.OutlineColor.None);
   private final Setting<ColorSetting> outlineColor = new Setting<>("OutlineColor", new ColorSetting(Integer.MIN_VALUE));
   private final Setting<ColorSetting> friendOutlineColor = new Setting<>("FriendOutlineColor", new ColorSetting(Integer.MIN_VALUE));
   private final Setting<Boolean> enchantss = new Setting<>("Enchants", true);
   private final Setting<Boolean> onlyHands = new Setting<>("OnlyHands", false, v -> this.enchantss.getValue());
   private final Setting<Boolean> funtimeHp = new Setting<>("FunTimeHp", false);
   private final Setting<Boolean> ignoreBots = new Setting<>("IgnoreBots", false);
   private final Setting<Boolean> potions = new Setting<>("Potions", true);
   private final Setting<Boolean> shulkers = new Setting<>("Shulkers", true);
   private final Setting<ColorSetting> fillColorA = new Setting<>("Fill", new ColorSetting(Integer.MIN_VALUE));
   private final Setting<ColorSetting> fillColorF = new Setting<>("FriendFill", new ColorSetting(Integer.MIN_VALUE));
   private final Setting<NameTags.Font> font = new Setting<>("FontMode", NameTags.Font.Fancy);
   private final Setting<NameTags.Armor> armorMode = new Setting<>("ArmorMode", NameTags.Armor.Full);
   private final Setting<NameTags.Health> health = new Setting<>("Health", NameTags.Health.Number);

   public NameTags() {
      super("NameTags", Module.Category.RENDER);
      this.encMap.put(Enchantments.BLAST_PROTECTION, "B");
      this.encMap.put(Enchantments.PROTECTION, "P");
      this.encMap.put(Enchantments.SHARPNESS, "S");
      this.encMap.put(Enchantments.EFFICIENCY, "E");
      this.encMap.put(Enchantments.UNBREAKING, "U");
      this.encMap.put(Enchantments.POWER, "PO");
      this.encMap.put(Enchantments.THORNS, "T");
   }

   @Override
   public void onRender2D(DrawContext context) {
      if (!mc.options.hudHidden) {
         for (PlayerEntity ent : mc.world.getPlayers()) {
            if ((ent != mc.player || !mc.options.getPerspective().isFirstPerson() && this.self.getValue())
               && (getEntityPing(ent) > 0 || !this.ignoreBots.getValue())) {
               double x = ent.prevX + (ent.getX() - ent.prevX) * (double)Render3DEngine.getTickDelta();
               double y = ent.prevY + (ent.getY() - ent.prevY) * (double)Render3DEngine.getTickDelta();
               double z = ent.prevZ + (ent.getZ() - ent.prevZ) * (double)Render3DEngine.getTickDelta();
               float scale = this.resize.getValue() ? this.scale.getValue() / mc.player.distanceTo(ent) : this.scale.getValue();
               Vec3d vector = new Vec3d(x, y + (double)this.height.getValue().floatValue(), z);
               Vector4d position = null;
               vector = Render3DEngine.worldSpaceToScreenSpace(vector);
               if (vector.z > 0.0 && vector.z < 1.0) {
                  position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
                  position.x = Math.min(vector.x, position.x);
                  position.y = Math.min(vector.y, position.y);
                  position.z = Math.max(vector.x, position.z);
               }

               String final_string = "";
               if (this.ping.getValue()) {
                  final_string = final_string + this.getPingColor(getEntityPing(ent)) + getEntityPing(ent) + "ms " + Formatting.WHITE;
               }

               if (this.gamemode.getValue()) {
                  final_string = final_string + this.translateGamemode(getEntityGamemode(ent)) + " ";
               }

               if (FriendManager.friends.stream().anyMatch(i -> i.contains(ent.getDisplayName().getString()))
                  && NameProtect.hideFriends.getValue()
                  && ModuleManager.nameProtect.isEnabled()) {
                  final_string = final_string + NameProtect.getCustomName() + " ";
               } else {
                  final_string = final_string + ent.getDisplayName().getString() + " ";
               }

               if (this.hp.getValue() && this.health.is(NameTags.Health.Number)) {
                  final_string = final_string + this.getHealthColor(this.getHealth(ent)) + round2(this.getHealth(ent)) + " ";
               }

               if (this.distance.getValue()) {
                  final_string = final_string + String.format("%.1f", mc.player.distanceTo(ent)) + "m ";
               }

               if (this.pops.getValue() && Managers.COMBAT.getPops(ent) != 0) {
                  final_string = final_string + Formatting.RESET + Managers.COMBAT.getPops(ent);
               }

               if (position != null) {
                  double posX = position.x;
                  double posY = position.y;
                  double endPosX = position.z;
                  double maxEnchantY = 0.0;
                  float diff = (float)(endPosX - posX) / 2.0F;
                  float textWidth;
                  if (this.font.getValue() == NameTags.Font.Fancy) {
                     textWidth = FontRenderers.sf_bold.getStringWidth(final_string) * 1.0F;
                  } else {
                     textWidth = (float)mc.textRenderer.getWidth(final_string);
                  }

                  float tagX = (float)((posX + (double)diff - (double)(textWidth / 2.0F)) * 1.0);
                  ArrayList<ItemStack> stacks = new ArrayList<>();
                  if (this.armorMode.getValue() != NameTags.Armor.Durability) {
                     stacks.add(ent.getOffHandStack());
                  }

                  stacks.add((ItemStack)ent.getInventory().armor.get(0));
                  stacks.add((ItemStack)ent.getInventory().armor.get(1));
                  stacks.add((ItemStack)ent.getInventory().armor.get(2));
                  stacks.add((ItemStack)ent.getInventory().armor.get(3));
                  if (this.armorMode.getValue() != NameTags.Armor.Durability) {
                     stacks.add(ent.getMainHandStack());
                  }

                  context.getMatrices().push();
                  context.getMatrices().translate(tagX - 2.0F + (textWidth + 4.0F) / 2.0F, (float)(posY - 13.0) + 6.5F, 0.0F);
                  context.getMatrices().scale(scale, scale, 1.0F);
                  context.getMatrices().translate(-(tagX - 2.0F + (textWidth + 4.0F) / 2.0F), -((float)(posY - 13.0 + 6.5)), 0.0F);
                  float item_offset = 0.0F;
                  if (this.armorMode.getValue() != NameTags.Armor.None) {
                     for (ItemStack armorComponent : stacks) {
                        if (!armorComponent.isEmpty()) {
                           if (this.armorMode.getValue() == NameTags.Armor.Full) {
                              context.getMatrices().push();
                              context.getMatrices().translate(posX - 55.0 + (double)item_offset, (double)((float)(posY - 33.0)), 0.0);
                              context.getMatrices().scale(1.1F, 1.1F, 1.1F);
                              DiffuseLighting.disableGuiDepthLighting();
                              context.drawItem(armorComponent, 0, 0);
                              context.drawItemInSlot(mc.textRenderer, armorComponent, 0, 0);
                              context.getMatrices().pop();
                           } else {
                              context.getMatrices().push();
                              context.getMatrices().translate(posX - 35.0 + (double)item_offset, (double)((float)(posY - 20.0)), 0.0);
                              context.getMatrices().scale(0.7F, 0.7F, 0.7F);
                              float durability = (float)(armorComponent.getMaxDamage() - armorComponent.getDamage());
                              int percent = (int)(durability / (float)armorComponent.getMaxDamage() * 100.0F);
                              Color color;
                              if (percent < 33) {
                                 color = Color.RED;
                              } else if (percent > 33 && percent < 66) {
                                 color = Color.YELLOW;
                              } else {
                                 color = Color.GREEN;
                              }

                              context.drawText(mc.textRenderer, percent + "%", 0, 0, color.getRGB(), false);
                              context.getMatrices().pop();
                           }

                           float enchantmentY = 0.0F;
                           ItemEnchantmentsComponent enchants = EnchantmentHelper.getEnchantments(armorComponent);
                           if (this.enchantss.getValue()
                              && (!this.onlyHands.getValue() || armorComponent == ent.getOffHandStack() || armorComponent == ent.getMainHandStack())) {
                              for (RegistryKey<Enchantment> enchantment : this.encMap.keySet()) {
                                 if (enchants.getEnchantments()
                                    .contains(mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(enchantment).get())) {
                                    String id = this.encMap.get(enchantment);
                                    int level = enchants.getLevel(
                                       (RegistryEntry)mc.world
                                          .getRegistryManager()
                                          .get(Enchantments.PROTECTION.getRegistryRef())
                                          .getEntry(enchantment)
                                          .get()
                                    );
                                    String encName = id + level;
                                    if (this.font.getValue() == NameTags.Font.Fancy) {
                                       FontRenderers.sf_bold
                                          .drawString(
                                             context.getMatrices(),
                                             encName,
                                             posX - 50.0 + (double)item_offset,
                                             (double)((float)posY - 45.0F + enchantmentY),
                                             -1
                                          );
                                    } else {
                                       context.getMatrices().push();
                                       context.getMatrices().translate(posX - 50.0 + (double)item_offset, posY - 45.0 + (double)enchantmentY, 0.0);
                                       context.drawText(mc.textRenderer, encName, 0, 0, -1, false);
                                       context.getMatrices().pop();
                                    }

                                    enchantmentY -= 8.0F;
                                    if (maxEnchantY > (double)enchantmentY) {
                                       maxEnchantY = (double)enchantmentY;
                                    }
                                 }
                              }
                           }
                        }

                        item_offset += 18.0F;
                     }
                  }

                  Color color = Managers.FRIEND.isFriend(ent) ? this.fillColorF.getValue().getColorObject() : this.fillColorA.getValue().getColorObject();
                  NameTags.OutlineColor cl = Managers.FRIEND.isFriend(ent) ? this.friendOutline.getValue() : this.outline.getValue();
                  if (cl == NameTags.OutlineColor.New) {
                     Render2DEngine.drawRectWithOutline(
                        context.getMatrices(),
                        tagX - 2.0F,
                        (float)(posY - 13.0),
                        textWidth + 4.0F,
                        11.0F,
                        color,
                        this.outlineColor.getValue().getColorObject()
                     );
                  } else {
                     Render2DEngine.drawRect(context.getMatrices(), tagX - 2.0F, (float)(posY - 13.0), textWidth + 4.0F, 11.0F, color);
                  }

                  if (Managers.TELEMETRY.getOnlinePlayers().contains(ent.getGameProfile().getName())) {
                     Render2DEngine.drawRect(context.getMatrices(), tagX - 14.0F, (float)(posY - 13.0), 12.0F, 11.0F, color.brighter().brighter());
                     RenderSystem.enableBlend();
                     RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
                     Color lColor = HudEditor.getColor(0);
                     RenderSystem.setShaderColor((float)lColor.getRed() / 255.0F, (float)lColor.getGreen() / 255.0F, (float)lColor.getBlue() / 255.0F, 1.0F);
                     RenderSystem.setShaderTexture(0, TextureStorage.miniLogo);
                     Render2DEngine.renderTexture(
                        context.getMatrices(), (double)(tagX - 13.0F), (double)((float)(posY - 12.5)), 10.0, 10.0, 0.0F, 0.0F, 256.0, 256.0, 256.0, 256.0
                     );
                     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                     RenderSystem.disableBlend();
                  }

                  switch (cl) {
                     case Sync:
                        Render2DEngine.drawRect(context.getMatrices(), tagX - 3.0F, (float)(posY - 14.0), textWidth + 6.0F, 1.0F, HudEditor.getColor(270));
                        Render2DEngine.drawRect(context.getMatrices(), tagX - 3.0F, (float)(posY - 3.0), textWidth + 6.0F, 1.0F, HudEditor.getColor(0));
                        Render2DEngine.drawRect(context.getMatrices(), tagX - 3.0F, (float)(posY - 14.0), 1.0F, 11.0F, HudEditor.getColor(180));
                        Render2DEngine.drawRect(context.getMatrices(), tagX + textWidth + 2.0F, (float)(posY - 14.0), 1.0F, 11.0F, HudEditor.getColor(90));
                        break;
                     case Custom:
                        Render2DEngine.drawRect(
                           context.getMatrices(),
                           tagX - 3.0F,
                           (float)(posY - 14.0),
                           textWidth + 6.0F,
                           1.0F,
                           Managers.FRIEND.isFriend(ent) ? this.friendOutlineColor.getValue().getColorObject() : this.outlineColor.getValue().getColorObject()
                        );
                        Render2DEngine.drawRect(
                           context.getMatrices(),
                           tagX - 3.0F,
                           (float)(posY - 3.0),
                           textWidth + 6.0F,
                           1.0F,
                           Managers.FRIEND.isFriend(ent) ? this.friendOutlineColor.getValue().getColorObject() : this.outlineColor.getValue().getColorObject()
                        );
                        Render2DEngine.drawRect(
                           context.getMatrices(),
                           tagX - 3.0F,
                           (float)(posY - 14.0),
                           1.0F,
                           11.0F,
                           Managers.FRIEND.isFriend(ent) ? this.friendOutlineColor.getValue().getColorObject() : this.outlineColor.getValue().getColorObject()
                        );
                        Render2DEngine.drawRect(
                           context.getMatrices(),
                           tagX + textWidth + 2.0F,
                           (float)(posY - 14.0),
                           1.0F,
                           11.0F,
                           Managers.FRIEND.isFriend(ent) ? this.friendOutlineColor.getValue().getColorObject() : this.outlineColor.getValue().getColorObject()
                        );
                     case None:
                     case New:
                  }

                  if (this.font.getValue() == NameTags.Font.Fancy) {
                     FontRenderers.sf_bold.drawString(context.getMatrices(), final_string, (double)tagX, (double)((float)posY - 10.0F), -1);
                  } else {
                     context.getMatrices().push();
                     context.getMatrices().translate(tagX, (float)posY - 11.0F, 0.0F);
                     context.drawText(mc.textRenderer, final_string, 0, 0, -1, false);
                     context.getMatrices().pop();
                  }

                  if (!this.health.is(NameTags.Health.Number)) {
                     int i = MathHelper.ceil(ent.getHealth());
                     float f = (float)ent.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
                     int p = MathHelper.ceil(ent.getAbsorptionAmount());
                     context.getMatrices().push();
                     context.getMatrices().translate(posX - 44.0, posY, 0.0);
                     context.getMatrices().scale(1.1F, 1.1F, 1.0F);
                     this.renderHealthBar(context, ent, f, i, p);
                     context.getMatrices().pop();
                  }

                  if (this.potions.getValue()) {
                     this.renderStatusEffectOverlay(context, (float)posX, (float)(posY + maxEnchantY - 60.0), ent);
                  }

                  Item handItem = ent.getMainHandStack().getItem();
                  if (this.shulkers.getValue()) {
                     int var10002;
                     int var10003;
                     ItemStack var10004;
                     label238: {
                        var10002 = (int)posX - 90;
                        var10003 = (int)posY - 120;
                        if (handItem instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
                           var10004 = ent.getMainHandStack();
                           break label238;
                        }

                        var10004 = ent.getOffHandStack();
                     }

                     this.renderShulkerToolTip(context, var10002, var10003, var10004);
                  }

                  context.getMatrices().pop();
               }
            }
         }

         if (this.spawners.getValue()) {
            this.drawSpawnerNameTag(context);
         }

         if (this.entityOwner.getValue()) {
            this.drawEntityOwner(context);
         }
      }
   }

   private void drawSpawnerNameTag(DrawContext context) {
   }

   public void drawEntityOwner(DrawContext context) {
      for (Entity ent : mc.world.getEntities()) {
         String ownerName = "";
         if (ent instanceof ProjectileEntity pe) {
            if (pe.getOwner() != null) {
               ownerName = pe.getOwner().getDisplayName().getString();
            }
         } else if (ent instanceof HorseEntity he) {
            if (he.getOwnerUuid() != null) {
               ownerName = he.getOwnerUuid().toString();
            }
         } else {
            if (!(ent instanceof TameableEntity te) || !te.isTamed() || te.getOwner() == null) {
               continue;
            }

            ownerName = te.getOwner().getDisplayName().getString();
         }

         String final_string = "Owned by " + ownerName;
         double x = ent.prevX + (ent.getX() - ent.prevX) * (double)Render3DEngine.getTickDelta();
         double y = ent.prevY + (ent.getY() - ent.prevY) * (double)Render3DEngine.getTickDelta();
         double z = ent.prevZ + (ent.getZ() - ent.prevZ) * (double)Render3DEngine.getTickDelta();
         Vec3d vector = new Vec3d(x, y + 2.0, z);
         Vector4d position = null;
         vector = Render3DEngine.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
         if (vector.z > 0.0 && vector.z < 1.0) {
            position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
         }

         if (position != null) {
            double posX = position.x;
            double posY = position.y;
            double endPosX = position.z;
            float diff = (float)(endPosX - posX) / 2.0F;
            float textWidth = FontRenderers.sf_bold.getStringWidth(final_string) * 1.0F;
            float tagX = (float)((posX + (double)diff - (double)(textWidth / 2.0F)) * 1.0);
            Render2DEngine.drawRect(
               context.getMatrices(), tagX - 2.0F, (float)(posY - 13.0), textWidth + 4.0F, 11.0F, this.fillColorA.getValue().getColorObject()
            );
            switch ((NameTags.OutlineColor)this.outline.getValue()) {
               case Sync:
                  Render2DEngine.drawRect(context.getMatrices(), tagX - 3.0F, (float)(posY - 14.0), textWidth + 6.0F, 1.0F, HudEditor.getColor(270));
                  Render2DEngine.drawRect(context.getMatrices(), tagX - 3.0F, (float)(posY - 3.0), textWidth + 6.0F, 1.0F, HudEditor.getColor(0));
                  Render2DEngine.drawRect(context.getMatrices(), tagX - 3.0F, (float)(posY - 14.0), 1.0F, 11.0F, HudEditor.getColor(180));
                  Render2DEngine.drawRect(context.getMatrices(), tagX + textWidth + 2.0F, (float)(posY - 14.0), 1.0F, 11.0F, HudEditor.getColor(90));
                  break;
               case Custom:
                  Render2DEngine.drawRect(
                     context.getMatrices(), tagX - 3.0F, (float)(posY - 14.0), textWidth + 6.0F, 1.0F, this.outlineColor.getValue().getColorObject()
                  );
                  Render2DEngine.drawRect(
                     context.getMatrices(), tagX - 3.0F, (float)(posY - 3.0), textWidth + 6.0F, 1.0F, this.outlineColor.getValue().getColorObject()
                  );
                  Render2DEngine.drawRect(context.getMatrices(), tagX - 3.0F, (float)(posY - 14.0), 1.0F, 11.0F, this.outlineColor.getValue().getColorObject());
                  Render2DEngine.drawRect(
                     context.getMatrices(), tagX + textWidth + 2.0F, (float)(posY - 14.0), 1.0F, 11.0F, this.outlineColor.getValue().getColorObject()
                  );
               case None:
            }

            FontRenderers.sf_bold.drawString(context.getMatrices(), final_string, (double)tagX, (double)((float)posY - 10.0F), -1);
         }
      }
   }

   public static int getEntityPing(PlayerEntity entity) {
      if (mc.getNetworkHandler() == null) {
         return 0;
      } else {
         PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
         return playerListEntry == null ? 0 : playerListEntry.getLatency();
      }
   }

   public static GameMode getEntityGamemode(PlayerEntity entity) {
      if (entity == null) {
         return null;
      } else {
         PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
         return playerListEntry == null ? null : playerListEntry.getGameMode();
      }
   }

   private String translateGamemode(GameMode gamemode) {
      if (gamemode == null) {
         return "[BOT]";
      } else {
         return switch (gamemode) {
            case SURVIVAL -> "[S]";
            case CREATIVE -> "[C]";
            case SPECTATOR -> "[SP]";
            case ADVENTURE -> "[A]";
            default -> throw new MatchException(null, null);
         };
      }
   }

   public float getHealth(PlayerEntity ent) {
      if ((mc.getNetworkHandler() == null || mc.getNetworkHandler().getServerInfo() == null || !mc.getNetworkHandler().getServerInfo().address.contains("funtime"))
         && !this.funtimeHp.getValue()) {
         return ent.getHealth() + ent.getAbsorptionAmount();
      } else {
         ScoreboardObjective scoreBoard = null;
         String resolvedHp = "";
         if (ent.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
            scoreBoard = ent.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
            if (scoreBoard != null) {
               ReadableScoreboardScore readableScoreboardScore = ent.getScoreboard().getScore(ent, scoreBoard);
               MutableText text2 = ReadableScoreboardScore.getFormattedScore(readableScoreboardScore, scoreBoard.getNumberFormatOr(StyledNumberFormat.EMPTY));
               resolvedHp = text2.getString();
            }
         }

         float numValue = 0.0F;

         try {
            numValue = Float.parseFloat(resolvedHp);
         } catch (NumberFormatException var6) {
         }

         return numValue;
      }
   }

   private void renderHealthBar(DrawContext context, PlayerEntity player, float maxHealth, int lastHealth, int absorption) {
      int i = MathHelper.ceil((double)maxHealth / 2.0);
      int j = MathHelper.ceil((double)absorption / 2.0);
      int k = i * 2;
      int cont = 0;

      for (int l = i + j - 1; l >= 0; l--) {
         int n = l % 10;
         int o = n * 8;
         if (cont < 10) {
            this.drawHeart(context, NameTags.HeartType.CONTAINER, o, false, player);
            cont++;
         }

         int q = l * 2;
         if (q < lastHealth) {
            this.drawHeart(context, NameTags.HeartType.NORMAL, o, q + 1 == lastHealth, player);
         }

         if (l >= i) {
            int r = q - k;
            if (q - k < absorption) {
               context.getMatrices().push();
               context.getMatrices().translate(0.0F, 0.0F, 0.001F);
               this.drawHeart(context, NameTags.HeartType.ABSORBING, o, r + 1 == absorption, player);
               context.getMatrices().pop();
            }
         }
      }
   }

   private void drawHeart(DrawContext context, NameTags.HeartType type, int x, boolean half, PlayerEntity player) {
      if (this.health.is(NameTags.Health.Dots)) {
         Color color = Managers.FRIEND.isFriend(player) ? this.fillColorF.getValue().getColorObject() : this.fillColorA.getValue().getColorObject();
         if (type == NameTags.HeartType.CONTAINER) {
            Render2DEngine.drawRect(context.getMatrices(), (float)x, 0.0F, 7.0F, 3.0F, color);
         } else if (type == NameTags.HeartType.NORMAL) {
            if (half) {
               Render2DEngine.drawRect(context.getMatrices(), (float)x, 0.0F, 3.0F, 3.0F, this.getHealthColor2(player.getHealth() + player.getAbsorptionAmount()));
               Render2DEngine.drawRect(context.getMatrices(), (float)(x + 3), 0.0F, 4.0F, 3.0F, color);
            } else {
               Render2DEngine.drawRect(context.getMatrices(), (float)x, 0.0F, 7.0F, 3.0F, this.getHealthColor2(player.getHealth() + player.getAbsorptionAmount()));
            }
         }
      } else {
         context.drawGuiTexture(type.getTexture(half), x, 0, 9, 9);
      }
   }

   @NotNull
   public String getHealthColor(float health) {
      if (health <= 15.0F && health > 7.0F) {
         return Formatting.YELLOW + "";
      } else {
         return health > 15.0F ? Formatting.GREEN + "" : Formatting.RED + "";
      }
   }

   @NotNull
   public String getPingColor(int ping) {
      if (ping <= 60) {
         return Formatting.GREEN + "";
      } else {
         return ping > 60 && ping < 120 ? Formatting.YELLOW + "" : Formatting.RED + "";
      }
   }

   @NotNull
   private Color getHealthColor2(float health) {
      if (health <= 15.0F && health > 7.0F) {
         return Color.YELLOW;
      } else {
         return health > 15.0F ? Color.GREEN : Color.RED;
      }
   }

   public static float round2(float value) {
      if (!Float.isNaN(value) && !Float.isInfinite(value)) {
         BigDecimal bd = new BigDecimal((double)value);
         bd = bd.setScale(1, RoundingMode.HALF_UP);
         return bd.floatValue();
      } else {
         return 1.0F;
      }
   }

   private void renderStatusEffectOverlay(DrawContext context, float x, float y, PlayerEntity player) {
      ArrayList<StatusEffectInstance> effects = new ArrayList<>(player.getStatusEffects());
      if (!effects.isEmpty()) {
         x += (float)effects.size() * 12.5F;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();

         for (StatusEffectInstance statusEffectInstance : Ordering.natural().reverse().sortedCopy(effects)) {
            x -= 25.0F;
            String power = "";
            switch (statusEffectInstance.getAmplifier()) {
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

            context.getMatrices().push();
            context.getMatrices().translate(x, y, 0.0F);
            context.drawSprite(0, 0, 0, 18, 18, mc.getStatusEffectSpriteManager().getSprite(statusEffectInstance.getEffectType()));
            FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), PotionHud.getDuration(statusEffectInstance), 9.0, -8.0, -1);
            FontRenderers.categories.drawCenteredString(context.getMatrices(), power, 9.0, -16.0, -1);
            context.getMatrices().pop();
         }

         RenderSystem.disableBlend();
      }
   }

   public boolean renderShulkerToolTip(DrawContext context, int offsetX, int offsetY, ItemStack stack) {
      try {
         ContainerComponent compoundTag = (ContainerComponent)stack.get(DataComponentTypes.CONTAINER);
         if (compoundTag == null) {
            return false;
         } else {
            float[] var10000 = new float[]{1.0F, 1.0F, 1.0F};
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
               float[] colors;
               try {
                  Color c = new Color(Objects.requireNonNull(ShulkerBoxBlock.getColor(stack.getItem())).getEntityColor());
                  colors = new float[]{(float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getRed() / 255.0F, (float)c.getAlpha() / 255.0F};
               } catch (NullPointerException var10) {
                  colors = new float[]{1.0F, 1.0F, 1.0F};
               }

               this.draw(context, compoundTag.stream().toList(), offsetX, offsetY, colors);
               return true;
            }

            return false;
         }
      } catch (Exception var11) {
         return false;
      }
   }

   private void draw(DrawContext context, List<ItemStack> itemStacks, int offsetX, int offsetY, float[] colors) {
      RenderSystem.disableDepthTest();
      GL11.glClear(256);
      offsetX += 8;
      offsetY -= 82;
      this.drawBackground(context, offsetX, offsetY, colors);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      DiffuseLighting.enableGuiDepthLighting();
      int row = 0;
      int i = 0;

      for (ItemStack itemStack : itemStacks) {
         context.drawItem(itemStack, offsetX + 8 + i * 18, offsetY + 7 + row * 18);
         context.drawItemInSlot(mc.textRenderer, itemStack, offsetX + 8 + i * 18, offsetY + 7 + row * 18);
         if (++i >= 9) {
            i = 0;
            row++;
         }
      }

      DiffuseLighting.disableGuiDepthLighting();
      RenderSystem.enableDepthTest();
   }

   private void drawBackground(DrawContext context, int x, int y, float[] colors) {
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(colors[0], colors[1], colors[2], 1.0F);
      RenderSystem.texParameter(3553, 10240, 9729);
      RenderSystem.texParameter(3553, 10241, 9987);
      context.drawTexture(TextureStorage.container, x, y, 0.0F, 0.0F, 176, 67, 176, 67);
      RenderSystem.enableBlend();
   }

   public static enum Armor {
      None,
      Full,
      Durability;
   }

   public static enum Font {
      Fancy,
      Fast;
   }

   public static enum Health {
      Number,
      Hearts,
      Dots;
   }

   private static enum HeartType {
      CONTAINER(Identifier.of("hud/heart/container"), Identifier.of("hud/heart/container")),
      NORMAL(Identifier.of("hud/heart/full"), Identifier.of("hud/heart/half")),
      ABSORBING(Identifier.of("hud/heart/absorbing_full"), Identifier.of("hud/heart/absorbing_half"));

      private final Identifier fullTexture;
      private final Identifier halfTexture;

      private HeartType(Identifier fullTexture, Identifier halfTexture) {
         this.fullTexture = fullTexture;
         this.halfTexture = halfTexture;
      }

      public Identifier getTexture(boolean half) {
         return half ? this.halfTexture : this.fullTexture;
      }
   }

   private static enum OutlineColor {
      Sync,
      Custom,
      None,
      New;
   }
}
