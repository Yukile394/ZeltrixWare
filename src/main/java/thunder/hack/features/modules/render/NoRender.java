package thunder.hack.features.modules.render;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.entity.Entity.RemovalReason;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.gui.notification.Notification;
import thunder.hack.setting.Setting;

public class NoRender extends Module {
   public final Setting<Boolean> disableGuiBackGround = new Setting<>("noGuiBackGround", true);
   public final Setting<Boolean> noSwing = new Setting<>("NoHandSwing", false);
   public final Setting<Boolean> auto = new Setting<>("Auto", false);
   public final Setting<Boolean> hurtCam = new Setting<>("HurtCam", true);
   public final Setting<Boolean> potions = new Setting<>("Potions", false);
   public final Setting<Boolean> xp = new Setting<>("XP", false);
   public final Setting<Boolean> arrows = new Setting<>("Arrows", false);
   public final Setting<Boolean> eggs = new Setting<>("Eggs", false);
   public final Setting<Boolean> elderGuardian = new Setting<>("Guardian", false);
   public final Setting<Boolean> vignette = new Setting<>("Vignette", true);
   public final Setting<Boolean> portal = new Setting<>("Portal", true);
   public final Setting<Boolean> explosions = new Setting<>("Explosions", false);
   public final Setting<Boolean> campFire = new Setting<>("CampFire", false);
   public final Setting<Boolean> fireworks = new Setting<>("Fireworks", false);
   public final Setting<Boolean> armor = new Setting<>("Armor", false);
   public final Setting<Boolean> bossbar = new Setting<>("Bossbar", false);
   public final Setting<Boolean> fireOverlay = new Setting<>("FireOverlay", false);
   public final Setting<Boolean> waterOverlay = new Setting<>("WaterOverlay", false);
   public final Setting<Boolean> blockOverlay = new Setting<>("BlockOverlay", false);
   public final Setting<Boolean> nausea = new Setting<>("Nausea", false);
   public final Setting<Boolean> blindness = new Setting<>("Blindness", false);
   public final Setting<Boolean> fog = new Setting<>("Fog", false);
   public final Setting<Boolean> darkness = new Setting<>("Darkness", false);
   public final Setting<Boolean> items = new Setting<>("Items", false);
   public final Setting<Boolean> crystals = new Setting<>("Crystals", false);
   public final Setting<Boolean> fireEntity = new Setting<>("FireEntity", true);
   public final Setting<Boolean> breakParticles = new Setting<>("BreakParticles", true);
   public final Setting<Boolean> antiTitle = new Setting<>("AntiTitle", false);
   public final Setting<Boolean> antiPlayerCollision = new Setting<>("AntiPlayerCollision", true);
   public final Setting<Boolean> noScoreBoard = new Setting<>("NoScoreBoard", true);
   public final Setting<Boolean> signText = new Setting<>("SignText", false);
   public final Setting<Boolean> noWeather = new Setting<>("NoWeather", false);
   public final Setting<Boolean> noArmorStands = new Setting<>("NoArmorStands", false);
   public final Setting<Boolean> spawnerEntity = new Setting<>("SpawnerEntity", false);
   public final Setting<Boolean> hotbarItemName = new Setting<>("HotbarItemName", false);
   private int potionCouter;
   private int xpCounter;
   private int arrowCounter;
   private int itemsCounter;

   public NoRender() {
      super("NoRender", Module.Category.RENDER);
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.Receive e) {
      if (e.getPacket() instanceof TitleS2CPacket && this.antiTitle.getValue()) {
         e.cancel();
      }
   }

   @EventHandler
   public void onSync(EventSync e) {
      for (Entity ent : Managers.ASYNC.getAsyncEntities()) {
         if (ent instanceof PotionEntity) {
            this.potionCouter++;
            if (this.potions.getValue()) {
               mc.world.removeEntity(ent.getId(), RemovalReason.KILLED);
            }
         }

         if (ent instanceof ExperienceBottleEntity) {
            this.xpCounter++;
            if (this.xp.getValue()) {
               mc.world.removeEntity(ent.getId(), RemovalReason.KILLED);
            }
         }

         if (ent instanceof EndCrystalEntity && this.crystals.getValue()) {
            mc.world.removeEntity(ent.getId(), RemovalReason.KILLED);
         }

         if (ent instanceof ArrowEntity) {
            this.arrowCounter++;
            if (this.arrows.getValue()) {
               mc.world.removeEntity(ent.getId(), RemovalReason.KILLED);
            }
         }

         if (ent instanceof EggEntity && this.eggs.getValue()) {
            mc.world.removeEntity(ent.getId(), RemovalReason.KILLED);
         }

         if (ent instanceof ItemEntity) {
            this.itemsCounter++;
            if (this.items.getValue()) {
               mc.world.removeEntity(ent.getId(), RemovalReason.KILLED);
            }
         }

         if (ent instanceof ArmorStandEntity && this.noArmorStands.getValue()) {
            mc.world.removeEntity(ent.getId(), RemovalReason.KILLED);
         }
      }

      if (this.auto.getValue()) {
         if (this.arrowCounter > 64) {
            Managers.NOTIFICATION
               .publicity(
                  "NoRender",
                  ClientSettings.isRu()
                     ? "\u041f\u0440\u0435\u0432\u044b\u0448\u0435\u043d \u043b\u0438\u043c\u0438\u0442 \u0441\u0442\u0440\u0435\u043b! \u0423\u0434\u0430\u043b\u044f\u044e..."
                     : "Arrows limit reached! Removing...",
                  3,
                  Notification.Type.SUCCESS
               );
         }

         if (this.itemsCounter > 16) {
            Managers.NOTIFICATION
               .publicity(
                  "NoRender",
                  ClientSettings.isRu()
                     ? "\u041f\u0440\u0435\u0432\u044b\u0448\u0435\u043d \u043b\u0438\u043c\u0438\u0442 \u0432\u0435\u0449\u0435\u0439! \u0423\u0434\u0430\u043b\u044f\u044e..."
                     : "Item limit reached! Removing...",
                  3,
                  Notification.Type.SUCCESS
               );
         }

         if (this.xpCounter > 16) {
            Managers.NOTIFICATION
               .publicity(
                  "NoRender",
                  ClientSettings.isRu()
                     ? "\u041f\u0440\u0435\u0432\u044b\u0448\u0435\u043d \u043b\u0438\u043c\u0438\u0442 \u043f\u0443\u0437\u044b\u0440\u044c\u043a\u043e\u0432 \u043e\u043f\u044b\u0442\u0430! \u0423\u0434\u0430\u043b\u044f\u044e..."
                     : "XP orbs limit reached! Removing...",
                  3,
                  Notification.Type.SUCCESS
               );
         }

         if (this.potionCouter > 8) {
            Managers.NOTIFICATION
               .publicity(
                  "NoRender",
                  ClientSettings.isRu()
                     ? "\u041f\u0440\u0435\u0432\u044b\u0448\u0435\u043d \u043b\u0438\u043c\u0438\u0442 \u0437\u0435\u043b\u0438\u0439! \u0423\u0434\u0430\u043b\u044f\u044e..."
                     : "Potions limit reached! Removing...",
                  3,
                  Notification.Type.SUCCESS
               );
         }

         List<Integer> toRemove = new ArrayList<>();

         for (Entity ent : Managers.ASYNC.getAsyncEntities()) {
            if (ent instanceof ArrowEntity && this.arrowCounter > 64) {
               toRemove.add(ent.getId());
            }

            if (ent instanceof ItemEntity && this.itemsCounter > 16) {
               toRemove.add(ent.getId());
            }

            if (ent instanceof ExperienceBottleEntity && this.xpCounter > 16) {
               toRemove.add(ent.getId());
            }

            if (ent instanceof PotionEntity && this.potionCouter > 8) {
               toRemove.add(ent.getId());
            }
         }

         try {
            toRemove.forEach(id -> mc.world.removeEntity(id, RemovalReason.KILLED));
         } catch (Exception var5) {
         }
      }

      this.arrowCounter = 0;
      this.itemsCounter = 0;
      this.potionCouter = 0;
      this.xpCounter = 0;
   }
}
