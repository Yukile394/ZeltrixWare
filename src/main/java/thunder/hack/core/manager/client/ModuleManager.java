package thunder.hack.core.manager.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.stream.Collectors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.hud.impl.ArmorHud;
import thunder.hack.features.hud.impl.CandleHud;
import thunder.hack.features.hud.impl.ChestCounter;
import thunder.hack.features.hud.impl.Companion;
import thunder.hack.features.hud.impl.Cooldowns;
import thunder.hack.features.hud.impl.Coords;
import thunder.hack.features.hud.impl.Crosshair;
import thunder.hack.features.hud.impl.CrosshairArrows;
import thunder.hack.features.hud.impl.FpsCounter;
import thunder.hack.features.hud.impl.GapplesHud;
import thunder.hack.features.hud.impl.Hotbar;
import thunder.hack.features.hud.impl.KeyBinds;
import thunder.hack.features.hud.impl.KillFeed;
import thunder.hack.features.hud.impl.KillStats;
import thunder.hack.features.hud.impl.LegacyHud;
import thunder.hack.features.hud.impl.MemoryHud;
import thunder.hack.features.hud.impl.ModuleList;
import thunder.hack.features.hud.impl.PVPResources;
import thunder.hack.features.hud.impl.PingHud;
import thunder.hack.features.hud.impl.PotionHud;
import thunder.hack.features.hud.impl.Radar;
import thunder.hack.features.hud.impl.RadarRewrite;
import thunder.hack.features.hud.impl.Speedometer;
import thunder.hack.features.hud.impl.SpotifyHud;
import thunder.hack.features.hud.impl.StaffBoard;
import thunder.hack.features.hud.impl.TPSCounter;
import thunder.hack.features.hud.impl.TargetHud;
import thunder.hack.features.hud.impl.TimerIndicator;
import thunder.hack.features.hud.impl.TotemCounter;
import thunder.hack.features.hud.impl.WaterMark;
import thunder.hack.features.hud.impl.WorldLoadAnim;
import thunder.hack.features.hud.impl.ZeltrixKeybinds;
import thunder.hack.features.hud.impl.ZeltrixTargetHud;
import thunder.hack.features.hud.impl.ZeltrixWatermark;
import thunder.hack.features.hud.impl.ZeltrixWatermarkAlternative;
import thunder.hack.features.hud.impl.ZeltrixWatermarkV2;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.AntiCrash;
import thunder.hack.features.modules.client.AntiPacketException;
import thunder.hack.features.modules.client.AntiServerAdd;
import thunder.hack.features.modules.client.AntiServerRP;
import thunder.hack.features.modules.client.BaritoneSettings;
import thunder.hack.features.modules.client.ChatTranslator;
import thunder.hack.features.modules.client.ClickGui;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.features.modules.client.ClientSpoof;
import thunder.hack.features.modules.client.FastLatency;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.client.Media;
import thunder.hack.features.modules.client.Notifications;
import thunder.hack.features.modules.client.OptifineCapes;
import thunder.hack.features.modules.client.RPC;
import thunder.hack.features.modules.client.Rotations;
import thunder.hack.features.modules.client.SoundFX;
import thunder.hack.features.modules.client.UnHook;
import thunder.hack.features.modules.client.WayPoints;
import thunder.hack.features.modules.client.Windows;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.features.modules.combat.AutoGApple;
import thunder.hack.features.modules.combat.AutoTotem;
import thunder.hack.features.modules.combat.AutoTrap;
import thunder.hack.features.modules.combat.AutoWeb;
import thunder.hack.features.modules.combat.Criticals;
import thunder.hack.features.modules.combat.ElytraTarget;
import thunder.hack.features.modules.combat.HitBox;
import thunder.hack.features.modules.misc.Ghost;
import thunder.hack.features.modules.misc.NameProtect;
import thunder.hack.features.modules.misc.Nuker;
import thunder.hack.features.modules.misc.Spammer;
import thunder.hack.features.modules.misc.TapeMouse;
import thunder.hack.features.modules.misc.TotemPopCounter;
import thunder.hack.features.modules.misc.VisualRange;
import thunder.hack.features.modules.movement.AutoPotion;
import thunder.hack.features.modules.movement.CustomModel;
import thunder.hack.features.modules.movement.ElytraBooster;
import thunder.hack.features.modules.movement.ElytraMotion;
import thunder.hack.features.modules.movement.ElytraPlus;
import thunder.hack.features.modules.movement.ElytraRecast;
import thunder.hack.features.modules.movement.TargetStrafe;
import thunder.hack.features.modules.player.ElytraHelper;
import thunder.hack.features.modules.player.ElytraSwap;
import thunder.hack.features.modules.player.FastUse;
import thunder.hack.features.modules.player.NoWeb;
import thunder.hack.features.modules.player.SpeedMine;
import thunder.hack.features.modules.render.Ambience;
import thunder.hack.features.modules.render.Animations;
import thunder.hack.features.modules.render.AspectRatio;
import thunder.hack.features.modules.render.BlockESP;
import thunder.hack.features.modules.render.BlockHighLight;
import thunder.hack.features.modules.render.BreadCrumbs;
import thunder.hack.features.modules.render.BreakHighLight;
import thunder.hack.features.modules.render.Chams;
import thunder.hack.features.modules.render.ChatFucker;
import thunder.hack.features.modules.render.ChunkAnimations;
import thunder.hack.features.modules.render.ClientSound;
import thunder.hack.features.modules.render.DamageTint;
import thunder.hack.features.modules.render.ESP;
import thunder.hack.features.modules.render.FOV;
import thunder.hack.features.modules.render.FreeCam;
import thunder.hack.features.modules.render.Fullbright;
import thunder.hack.features.modules.render.HitBubbles;
import thunder.hack.features.modules.render.HitParticles;
import thunder.hack.features.modules.render.Inventory;
import thunder.hack.features.modules.render.InventoryAnimation;
import thunder.hack.features.modules.render.ItemESP;
import thunder.hack.features.modules.render.JumpCircle;
import thunder.hack.features.modules.render.NameTags;
import thunder.hack.features.modules.render.NoRender;
import thunder.hack.features.modules.render.Particles;
import thunder.hack.features.modules.render.SantaHat;
import thunder.hack.features.modules.render.SkyShader;
import thunder.hack.features.modules.render.SmoothTab;
import thunder.hack.features.modules.render.TargetESP;
import thunder.hack.features.modules.render.TotemAnimation;
import thunder.hack.features.modules.render.TotemPop;
import thunder.hack.features.modules.render.ViewModel;
import thunder.hack.features.modules.render.WorldTweaks;
import thunder.hack.gui.clickui.ClickGUI;
import thunder.hack.gui.font.FontRenderers;

public class ModuleManager implements IManager {
   public ArrayList<Module> modules = new ArrayList<>();
   public List<Module> sortedModules = new ArrayList<>();
   public List<Integer> activeMouseKeys = new ArrayList<>();
   public static ChunkAnimations chunkAnimations = new ChunkAnimations();
   public static Inventory inventory = new Inventory();
   public static AntiPacketException antiPacketException = new AntiPacketException();
   public static BaritoneSettings baritoneSettings = new BaritoneSettings();
   public static TotemPopCounter totemPopCounter = new TotemPopCounter();
   public static CrosshairArrows crosshairArrows = new CrosshairArrows();
   public static ChatTranslator chatTranslator = new ChatTranslator();
   public static ClientSettings clientSettings = new ClientSettings();
   public static TimerIndicator timerIndicator = new TimerIndicator();
   public static BreakHighLight breakHighLight = new BreakHighLight();
   public static BlockHighLight blockHighLight = new BlockHighLight();
   public static TotemAnimation totemAnimation = new TotemAnimation();
   public static OptifineCapes optifineCapes = new OptifineCapes();
   public static Notifications notifications = new Notifications();
   public static AntiServerAdd antiServerAdd = new AntiServerAdd();
   public static RadarRewrite radarRewrite = new RadarRewrite();
   public static PVPResources pvpResources = new PVPResources();
   public static HitParticles hitParticles = new HitParticles();
   public static ElytraRecast elytraRecast = new ElytraRecast();
   public static AntiServerRP antiServerRP = new AntiServerRP();
   public static TotemCounter totemCounter = new TotemCounter();
   public static ChestCounter chestCounter = new ChestCounter();
   public static FastLatency fastLatency = new FastLatency();
   public static WorldTweaks worldTweaks = new WorldTweaks();
   public static VisualRange visualRange = new VisualRange();
   public static Speedometer speedometer = new Speedometer();
   public static NameProtect nameProtect = new NameProtect();
   public static BreadCrumbs breadCrumbs = new BreadCrumbs();
   public static AspectRatio aspectRatio = new AspectRatio();
   public static ClientSpoof clientSpoof = new ClientSpoof();
   public static TPSCounter tpsCounter = new TPSCounter();
   public static StaffBoard staffBoard = new StaffBoard();
   public static ModuleList moduleList = new ModuleList();
   public static JumpCircle jumpCircle = new JumpCircle();
   public static Fullbright fullbright = new Fullbright();
   public static FpsCounter fpsCounter = new FpsCounter();
   public static ElytraSwap elytraSwap = new ElytraSwap();
   public static ElytraPlus elytraPlus = new ElytraPlus();
   public static ElytraBooster elytraBooster = new ElytraBooster();
   public static ElytraHelper elytraHelper = new ElytraHelper();
   public static ElytraMotion elytraMotion = new ElytraMotion();
   public static CustomModel customModel = new CustomModel();
   public static NoWeb noWeb = new NoWeb();
   public static AutoGApple autoGApple = new AutoGApple();
   public static Animations animations = new Animations();
   public static DamageTint damageTint = new DamageTint();
   public static GapplesHud gapplesHud = new GapplesHud();
   public static HitBubbles hitBubbles = new HitBubbles();
   public static KillStats killStats = new KillStats();
   public static CandleHud candleHud = new CandleHud();
   public static Particles particles = new Particles();
   public static WayPoints wayPoints = new WayPoints();
   public static WaterMark waterMark = new WaterMark();
   public static WorldLoadAnim worldLoadAnim = new WorldLoadAnim();
   public static ZeltrixWatermark zeltrixWatermark = new ZeltrixWatermark();
   public static ZeltrixWatermarkAlternative zeltrixWatermarkAlternative = new ZeltrixWatermarkAlternative();
   public static ZeltrixWatermarkV2 zeltrixWatermarkV2 = new ZeltrixWatermarkV2();
   public static SpotifyHud spotifyHud = new SpotifyHud();
   public static ZeltrixTargetHud zeltrixTargetHud = new ZeltrixTargetHud();
   public static ZeltrixKeybinds zeltrixKeybinds = new ZeltrixKeybinds();
   public static ViewModel viewModel = new ViewModel();
   public static TargetHud targetHud = new TargetHud();
   public static SpeedMine speedMine = new SpeedMine();
   public static PotionHud potionHud = new PotionHud();
   public static LegacyHud legacyHud = new LegacyHud();
   public static HudEditor hudEditor = new HudEditor();
   public static Crosshair crosshair = new Crosshair();
   public static Criticals criticals = new Criticals();
   public static AutoTotem autoTotem = new AutoTotem();
   public static AutoPotion autoPotion = new AutoPotion();
   public static ElytraTarget elytraTarget = new ElytraTarget();
   public static Cooldowns cooldowns = new Cooldowns();
   public static TapeMouse tapeMouse = new TapeMouse();
   public static Rotations rotations = new Rotations();
   public static MemoryHud memoryHud = new MemoryHud();
   public static Companion companion = new Companion();
   public static AntiCrash antiCrash = new AntiCrash();
   public static NoRender noRender = new NoRender();
   public static NameTags nameTags = new NameTags();
   public static KeyBinds keyBinds = new KeyBinds();
   public static ClickGui clickGui = new ClickGui();
   public static AutoTrap autoTrap = new AutoTrap();
   public static ArmorHud armorHud = new ArmorHud();
   public static KillFeed killFeed = new KillFeed();
   public static BlockESP blockESP = new BlockESP();
   public static Windows windows = new Windows();
   public static SoundFX soundFX = new SoundFX();
   public static Spammer spammer = new Spammer();
   public static PingHud pingHud = new PingHud();
   public static ItemESP itemESP = new ItemESP();
   public static FreeCam freeCam = new FreeCam();
   public static FastUse fastUse = new FastUse();
   public static AutoWeb autoWeb = new AutoWeb();
   public static Hotbar hotbar = new Hotbar();
   public static HitBox hitBox = new HitBox();
   public static TargetStrafe targetStrafe = new TargetStrafe();
   public static Coords coords = new Coords();
   public static UnHook unHook = new UnHook();
   public static Timer timer = new Timer();
   public static Radar radar = new Radar();
   public static Nuker nuker = new Nuker();
   public static Media media = new Media();
   public static Ghost ghost = new Ghost();
   public static Chams chams = new Chams();
   public static SantaHat santaHat = new SantaHat();
   public static Aura aura = new Aura();
   public static FOV fov = new FOV();
   public static InventoryAnimation inventoryAnimation = new InventoryAnimation();
   public static SmoothTab smoothTab = new SmoothTab();
   public static ESP esp = new ESP();
   public static RPC rpc = new RPC();
   public static TargetESP targetESP = new TargetESP();
   public static TotemPop totemPop = new TotemPop();
   public static Ambience ambience = new Ambience();
   public static ClientSound clientSound = new ClientSound();
   public static SkyShader skyShader = new SkyShader();
   public static ChatFucker chatFucker = new ChatFucker();

   public ModuleManager() {
      for (Field field : this.getClass().getDeclaredFields()) {
         if (Module.class.isAssignableFrom(field.getType())) {
            field.setAccessible(true);

            try {
               this.modules.add((Module)field.get(this));
            } catch (IllegalAccessException var6) {
               var6.printStackTrace();
            }
         }
      }
   }

   public Module get(String name) {
      for (Module module : this.modules) {
         if (module.getName().equalsIgnoreCase(name)) {
            return module;
         }
      }

      return null;
   }

   public ArrayList<Module> getEnabledModules() {
      ArrayList<Module> enabledModules = new ArrayList<>();

      for (Module module : this.modules) {
         if (module.isEnabled()) {
            enabledModules.add(module);
         }
      }

      return enabledModules;
   }

   public ArrayList<Module> getModulesByCategory(Module.Category category) {
      ArrayList<Module> modulesCategory = new ArrayList<>();
      this.modules.forEach(module -> {
         if (module.getCategory() == category) {
            modulesCategory.add(module);
         }
      });
      return modulesCategory;
   }

   public List<Module.Category> getCategories() {
      return new ArrayList<>(Module.Category.values());
   }

   public void onLoad(String category) {
      try {
         ThunderHack.EVENT_BUS.unsubscribe(unHook);
      } catch (Exception var3) {
      }

      unHook.setEnabled(false);
      this.modules.sort(Comparator.comparing(Module::getName));
      this.modules.forEach(m -> {
         if (m.isEnabled() && (m.getCategory().getName().equalsIgnoreCase(category) || category.equals("none"))) {
            ThunderHack.EVENT_BUS.subscribe(m);
         }
      });
      if (ConfigManager.firstLaunch) {
         notifications.enable();
         rpc.enable();
         soundFX.enable();
      }
   }

   public void onUpdate() {
      if (!Module.fullNullCheck()) {
         this.modules.stream().filter(Module::isEnabled).forEach(Module::onUpdate);
      }
   }

   public void onRender2D(DrawContext context) {
      if (!mc.getDebugHud().shouldShowDebugHud() && !mc.options.hudHidden) {
         HudElement.anyHovered = false;
         this.modules.stream().filter(Module::isEnabled).forEach(module -> module.onRender2D(context));
         if (!HudElement.anyHovered && !ClickGUI.anyHovered && GLFW.glfwGetPlatform() != 393219) {
            GLFW.glfwSetCursor(mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(221185));
         }

         ThunderHack.core.onRender2D(context);
      }
   }

   public void onRender3D(MatrixStack stack) {
      this.modules.stream().filter(Module::isEnabled).forEach(module -> module.onRender3D(stack));
   }

   public void sortModules() {
      this.sortedModules = this.getEnabledModules()
         .stream()
         .filter(Module::isDrawn)
         .sorted(Comparator.comparing(module -> FontRenderers.getModulesRenderer().getStringWidth(module.getFullArrayString()) * -1.0F))
         .collect(Collectors.toList());
   }

   public void onLogout() {
      this.modules.forEach(Module::onLogout);
   }

   public void onLogin() {
      this.modules.forEach(Module::onLogin);
   }

   public void onUnload(String category) {
      this.modules.forEach(module -> {
         if (module.isEnabled() && (module.getCategory().getName().equalsIgnoreCase(category) || category.equals("none"))) {
            ThunderHack.EVENT_BUS.unsubscribe(module);
            module.setEnabled(false);
         }
      });
      this.modules.forEach(Module::onUnload);
   }

   public void onKeyPressed(int eventKey) {
      if (eventKey != -1 && eventKey != 0 && !(mc.currentScreen instanceof ClickGUI)) {
         this.modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey) {
               module.toggle();
            }
         });
      }
   }

   public void onKeyReleased(int eventKey) {
      if (eventKey != -1 && eventKey != 0 && !(mc.currentScreen instanceof ClickGUI)) {
         this.modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey && module.getBind().isHold()) {
               module.disable();
            }
         });
      }
   }

   public void onMoseKeyPressed(int eventKey) {
      if (eventKey != -1 && !(mc.currentScreen instanceof ClickGUI)) {
         this.modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + eventKey)) {
               module.toggle();
            }
         });
      }
   }

   public void onMoseKeyReleased(int eventKey) {
      if (eventKey != -1 && !(mc.currentScreen instanceof ClickGUI)) {
         this.activeMouseKeys.add(eventKey);
         this.modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + eventKey) && module.getBind().isHold()) {
               module.disable();
            }
         });
      }
   }

   public ArrayList<Module> getModulesSearch(String string) {
      ArrayList<Module> modulesCategory = new ArrayList<>();
      this.modules.forEach(module -> {
         if (module.getName().toLowerCase().contains(string.toLowerCase())) {
            modulesCategory.add(module);
         }
      });
      return modulesCategory;
   }

   public void registerModule(Module module) {
      if (module != null) {
         this.modules.add(module);
         if (module.isEnabled()) {
            ThunderHack.EVENT_BUS.subscribe(module);
         }
      }
   }

   public void registerHudElement(HudElement hudElement) {
      if (hudElement != null) {
         this.modules.add(hudElement);
      }
   }
}
