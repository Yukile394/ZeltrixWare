package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.File;
import net.minecraft.util.Util;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;

public class OpenFolderCommand extends Command {
   public OpenFolderCommand() {
      super("openfolder", "folder");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes(context -> {
         Util.getOperatingSystem().open(new File("ThunderHackRecode/configs/"));
         return 1;
      });
   }
}
