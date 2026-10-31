package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.modules.client.RPC;

public class RpcCommand extends Command {
   public RpcCommand() {
      super("rpc");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(
         ((RequiredArgumentBuilder)arg("bigImg", StringArgumentType.greedyString())
               .executes(
                  context -> {
                     String bigImg = (String)context.getArgument("bigImg", String.class);
                     RPC.WriteFile(bigImg, "none");
                     sendMessage(
                        "\u0411\u043e\u043b\u044c\u0448\u0430\u044f \u043a\u0430\u0440\u0442\u0438\u043d\u043a\u0430 RPC \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0430 \u043d\u0430 "
                           + bigImg
                     );
                     return 1;
                  }
               ))
            .then(
               arg("littleImg", StringArgumentType.greedyString())
                  .executes(
                     context -> {
                        String bigImg = (String)context.getArgument("bigImg", String.class);
                        String littleImg = (String)context.getArgument("littleImg", String.class);
                        RPC.WriteFile(bigImg, littleImg);
                        sendMessage(
                           "\u0411\u043e\u043b\u044c\u0448\u0430\u044f \u043a\u0430\u0440\u0442\u0438\u043d\u043a\u0430 RPC \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0430 \u043d\u0430 "
                              + bigImg
                        );
                        sendMessage(
                           "\u041c\u0430\u043b\u0435\u043d\u044c\u043a\u0430\u044f \u043a\u0430\u0440\u0442\u0438\u043d\u043a\u0430 RPC \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0430 \u043d\u0430 "
                              + littleImg
                        );
                        return 1;
                     }
                  )
            )
      );
      builder.executes(context -> {
         sendMessage(".rpc url or .rpc url url");
         return 1;
      });
   }
}
