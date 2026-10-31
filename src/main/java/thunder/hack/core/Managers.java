package thunder.hack.core;

import thunder.hack.ThunderHack;
import thunder.hack.core.manager.client.AddonManager;
import thunder.hack.core.manager.client.AsyncManager;
import thunder.hack.core.manager.client.CommandManager;
import thunder.hack.core.manager.client.ConfigManager;
import thunder.hack.core.manager.client.MacroManager;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.core.manager.client.NotificationManager;
import thunder.hack.core.manager.client.ProxyManager;
import thunder.hack.core.manager.client.ServerManager;
import thunder.hack.core.manager.client.ShaderManager;
import thunder.hack.core.manager.client.SoundManager;
import thunder.hack.core.manager.client.TelemetryManager;
import thunder.hack.core.manager.player.CombatManager;
import thunder.hack.core.manager.player.FriendManager;
import thunder.hack.core.manager.player.PlayerManager;
import thunder.hack.core.manager.world.HoleManager;
import thunder.hack.core.manager.world.WayPointManager;
import thunder.hack.utility.ThunderUtility;

public class Managers {
   public static final CombatManager COMBAT = new CombatManager();
   public static final FriendManager FRIEND = new FriendManager();
   public static final PlayerManager PLAYER = new PlayerManager();
   public static final HoleManager HOLE = new HoleManager();
   public static final WayPointManager WAYPOINT = new WayPointManager();
   public static final AddonManager ADDON = new AddonManager();
   public static final AsyncManager ASYNC = new AsyncManager();
   public static final ModuleManager MODULE = new ModuleManager();
   public static final ConfigManager CONFIG = new ConfigManager();
   public static final MacroManager MACRO = new MacroManager();
   public static final NotificationManager NOTIFICATION = new NotificationManager();
   public static final ProxyManager PROXY = new ProxyManager();
   public static final ServerManager SERVER = new ServerManager();
   public static final ShaderManager SHADER = new ShaderManager();
   public static final SoundManager SOUND = new SoundManager();
   public static final TelemetryManager TELEMETRY = new TelemetryManager();
   public static final CommandManager COMMAND = new CommandManager();

   public static void init() {
      ADDON.initAddons();
      CONFIG.load(CONFIG.getCurrentConfig());
      MODULE.onLoad("none");
      FRIEND.loadFriends();
      MACRO.onLoad();
      WAYPOINT.onLoad();
      PROXY.onLoad();
      SOUND.registerSounds();
      ASYNC.run(() -> {
         ThunderUtility.syncContributors();
         ThunderUtility.parseStarGazer();
         ThunderUtility.parseCommits();
         TELEMETRY.fetchData();
      });
   }

   public static void subscribe() {
      ThunderHack.EVENT_BUS.subscribe(NOTIFICATION);
      ThunderHack.EVENT_BUS.subscribe(SERVER);
      ThunderHack.EVENT_BUS.subscribe(PLAYER);
      ThunderHack.EVENT_BUS.subscribe(COMBAT);
      ThunderHack.EVENT_BUS.subscribe(ASYNC);
      ThunderHack.EVENT_BUS.subscribe(TELEMETRY);
   }
}
