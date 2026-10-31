package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.world.GameMode;
import net.minecraft.command.CommandSource;
import thunder.hack.features.cmd.Command;

public class GamemodeCommand extends Command {
   public GamemodeCommand() {
      super("gamemode", "gm");
   }

   @Override
   public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(arg("mode", StringArgumentType.greedyString()).executes(context -> {
         String mode = (String)context.getArgument("mode", String.class);
         switch (mode) {
            case "survival":
            case "0":
               mc.interactionManager.setGameMode(GameMode.SURVIVAL);
            case "creative":
            case "1":
               mc.interactionManager.setGameMode(GameMode.CREATIVE);
            case "spectator":
            case "2":
               mc.interactionManager.setGameMode(GameMode.SPECTATOR);
            case "adventure":
            case "3":
               mc.interactionManager.setGameMode(GameMode.ADVENTURE);
            default:
               return 1;
         }
      }));
   }
}
