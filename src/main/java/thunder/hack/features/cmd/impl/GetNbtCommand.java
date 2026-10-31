package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.ClientSettings;

public class GetNbtCommand extends Command {
   public GetNbtCommand() {
      super("nbt", "getnbt");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes(
         context -> {
            sendMessage(
               mc.player.getMainHandStack().getComponents() != null
                  ? mc.player.getMainHandStack().getComponents().toString()
                  : (
                     ClientSettings.isRu()
                        ? "\u0423 \u044d\u0442\u043e\u0433\u043e \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 \u043d\u0435\u0442 nbt \u0442\u0435\u0433\u043e\u0432!"
                        : "This item don't contains nbt tags!"
                  )
            );
            return 1;
         }
      );
   }
}
