package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.world.WayPointManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.WayPointArgumentType;
import thunder.hack.features.modules.client.ClientSettings;

public class WayPointCommand extends Command {
   public WayPointCommand() {
      super("waypoint", "waypoints");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(
         literal("list")
            .executes(
               context -> {
                  sendMessage(ClientSettings.isRu() ? "\u041c\u0435\u0442\u043a\u0438:" : "Waypoints:");
                  sendMessage(" ");
                  Managers.WAYPOINT
                     .getWayPoints()
                     .forEach(
                        wp -> sendMessage(
                              wp.getName()
                                 + " X: "
                                 + wp.getX()
                                 + " Y: "
                                 + wp.getY()
                                 + " Z: "
                                 + wp.getZ()
                                 + " Server: "
                                 + wp.getServer()
                                 + " Dimension: "
                                 + wp.getDimension()
                           )
                     );
                  return 1;
               }
            )
      );
      builder.then(literal("remove").then(arg("name", WayPointArgumentType.create()).executes(context -> {
         WayPointManager.WayPoint wp = (WayPointManager.WayPoint)context.getArgument("name", WayPointManager.WayPoint.class);
         Managers.WAYPOINT.removeWayPoint(wp);
         sendMessage(ClientSettings.isRu() ? "\u0423\u0434\u0430\u043b\u0435\u043d\u0430 \u043c\u0435\u0442\u043a\u0430 " : "Deleted waypoint " + wp.getName());
         return 1;
      })));
      builder.then(
         literal("add")
            .then(
               ((RequiredArgumentBuilder)arg("name", StringArgumentType.word())
                     .executes(
                        context -> {
                           String name = (String)context.getArgument("name", String.class);
                           WayPointManager.WayPoint wp = new WayPointManager.WayPoint(
                              (int)mc.player.getX(),
                              (int)mc.player.getY(),
                              (int)mc.player.getZ(),
                              name,
                              mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address,
                              mc.world.getRegistryKey().getValue().getPath()
                           );
                           Managers.WAYPOINT.addWayPoint(wp);
                           sendMessage(
                              (
                                    ClientSettings.isRu()
                                       ? "\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430 \u043c\u0435\u0442\u043a\u0430 "
                                          + name
                                          + " \u0441 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u0430\u043c\u0438"
                                       : "Added waypoint " + name + " with coords"
                                 )
                                 + " X: "
                                 + (int)mc.player.getX()
                                 + " Y: "
                                 + (int)mc.player.getY()
                                 + " Z: "
                                 + (int)mc.player.getZ()
                           );
                           return 1;
                        }
                     ))
                  .then(
                     arg("x", IntegerArgumentType.integer())
                        .then(
                           arg("y", IntegerArgumentType.integer())
                              .then(
                                 arg("z", IntegerArgumentType.integer())
                                    .executes(
                                       context -> {
                                          String name = (String)context.getArgument("name", String.class);
                                          BlockPos pos = new BlockPos(
                                             (Integer)context.getArgument("x", Integer.class),
                                             (Integer)context.getArgument("y", Integer.class),
                                             (Integer)context.getArgument("z", Integer.class)
                                          );
                                          WayPointManager.WayPoint wp = new WayPointManager.WayPoint(
                                             pos.getX(),
                                             pos.getY(),
                                             pos.getZ(),
                                             name,
                                             mc.isInSingleplayer() ? "SinglePlayer" : mc.getNetworkHandler().getServerInfo().address,
                                             mc.world.getRegistryKey().getValue().getPath()
                                          );
                                          Managers.WAYPOINT.addWayPoint(wp);
                                          sendMessage(
                                             (
                                                   ClientSettings.isRu()
                                                      ? "\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430 \u043c\u0435\u0442\u043a\u0430 "
                                                         + name
                                                         + " \u0441 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u0430\u043c\u0438 X: "
                                                      : "Added waypoint " + name + " with coords"
                                                )
                                                + pos.getX()
                                                + " Y: "
                                                + pos.getY()
                                                + " Z: "
                                                + pos.getZ()
                                          );
                                          return 1;
                                       }
                                    )
                              )
                        )
                  )
            )
      );
      builder.executes(context -> {
         sendMessage(this.usage());
         return 1;
      });
   }

   String usage() {
      return "waypoint add/remove/list (waypoint add x y z name), (waypoint remove name)";
   }
}
