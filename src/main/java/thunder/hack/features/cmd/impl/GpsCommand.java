package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;
import thunder.hack.features.cmd.Command;

public class GpsCommand extends Command {
   public GpsCommand() {
      super("gps");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(literal("off").executes(context -> {
         ThunderHack.gps_position = null;
         return 1;
      }));
      builder.then(
         arg("x", IntegerArgumentType.integer())
            .then(
               arg("z", IntegerArgumentType.integer())
                  .executes(
                     context -> {
                        int x = (Integer)context.getArgument("x", Integer.class);
                        int z = (Integer)context.getArgument("z", Integer.class);
                        ThunderHack.gps_position = new BlockPos(x, 0, z);
                        sendMessage(
                           "GPS \u043d\u0430\u0441\u0442\u0440\u043e\u0435\u043d \u043d\u0430 X: "
                              + ThunderHack.gps_position.getX()
                              + " Z: "
                              + ThunderHack.gps_position.getZ()
                        );
                        return 1;
                     }
                  )
            )
      );
      builder.executes(context -> {
         sendMessage("\u041f\u043e\u043f\u0440\u043e\u0431\u0443\u0439 .gps off / .gps x z");
         return 1;
      });
   }
}
