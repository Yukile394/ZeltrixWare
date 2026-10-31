package thunder.hack.features.cmd.impl;

import baritone.api.BaritoneAPI;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import thunder.hack.ThunderHack;
import thunder.hack.core.manager.world.WayPointManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.WayPointArgumentType;
import thunder.hack.features.modules.client.ClientSettings;

public class GotoWaypointCommand extends Command {
   public GotoWaypointCommand() {
      super("goto");
   }

   @Override
   public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(
         arg("name", WayPointArgumentType.create())
            .executes(
               context -> {
                  if (!ThunderHack.baritone) {
                     sendMessage(
                        ClientSettings.isRu()
                           ? "\u0411\u0430\u0440\u0438\u0442\u043e\u043d \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d (\u043c\u043e\u0436\u0435\u0448\u044c \u0441\u043a\u0430\u0447\u0430\u0442\u044c \u043d\u0430 https://meteorclient.com)"
                           : "Baritone not found (you can download it at https://meteorclient.com)"
                     );
                     return 1;
                  } else {
                     WayPointManager.WayPoint wp = (WayPointManager.WayPoint)context.getArgument("name", WayPointManager.WayPoint.class);
                     if (!mc.world.getRegistryKey().getValue().getPath().equals(wp.getDimension())) {
                        sendMessage(
                           ClientSettings.isRu()
                              ? "\u041c\u0435\u0442\u043a\u0430 \u0432 \u0434\u0440\u0443\u0433\u043e\u043c \u0438\u0437\u043c\u0435\u0440\u0435\u043d\u0438\u0438"
                              : "Waypoint is in another dimension"
                        );
                        return 1;
                     } else {
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("goto " + wp.getX() + " " + wp.getY() + " " + wp.getZ());
                        return 1;
                     }
                  }
               }
            )
      );
   }
}
