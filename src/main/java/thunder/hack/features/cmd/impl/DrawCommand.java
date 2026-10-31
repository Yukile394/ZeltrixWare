package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.util.Formatting;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.ModuleArgumentType;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;

public class DrawCommand extends Command {
   public DrawCommand() {
      super("draw");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(
         arg("module", ModuleArgumentType.create())
            .executes(
               context -> {
                  Module module = (Module)context.getArgument("module", Module.class);
                  module.setDrawn(!module.isDrawn());
                  if (ClientSettings.isRu()) {
                     sendMessage(
                        "\u041c\u043e\u0434\u0443\u043b\u044c "
                           + Formatting.GREEN
                           + module.getName()
                           + Formatting.WHITE
                           + " \u0442\u0435\u043f\u0435\u0440\u044c "
                           + (
                              module.isDrawn()
                                 ? "\u0432\u0438\u0434\u0435\u043d \u0432 ArrayList"
                                 : "\u043d\u0435 \u0432\u0438\u0434\u0435\u043d \u0432 ArrayList"
                           )
                     );
                  } else {
                     sendMessage(
                        Formatting.GREEN
                           + module.getName()
                           + Formatting.WHITE
                           + " is now "
                           + (module.isDrawn() ? "visible in ArrayList" : "invisible in ArrayList")
                     );
                  }

                  return 1;
               }
            )
      );
   }
}
