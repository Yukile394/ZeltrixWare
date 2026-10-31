package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.util.Formatting;
import net.minecraft.command.CommandSource;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.SearchArgumentType;
import thunder.hack.features.modules.client.ClientSettings;

public class NukerCommand extends Command {
   public NukerCommand() {
      super("nuker");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(
         literal("reset")
            .executes(
               context -> {
                  ModuleManager.nuker.selectedBlocks.getValue().clear();
                  sendMessage(
                     ClientSettings.isRu()
                        ? "\u0412\u0441\u0435 \u0431\u043b\u043e\u043a\u0438 \u0431\u044b\u043b\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u044b!"
                        : "Nuker got reset!"
                  );
                  return 1;
               }
            )
      );
      builder.then(
         literal("add")
            .then(
               arg("block", SearchArgumentType.create())
                  .executes(
                     context -> {
                        String blockName = (String)context.getArgument("block", String.class);
                        Block result = getRegisteredBlock(blockName);
                        if (result != null) {
                           ModuleManager.nuker.selectedBlocks.getValue().add(result);
                           sendMessage(
                              Formatting.GREEN
                                 + blockName
                                 + (ClientSettings.isRu() ? " \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 Nuker" : " added to Nuker")
                           );
                        } else {
                           sendMessage(
                              Formatting.RED
                                 + (
                                    ClientSettings.isRu()
                                       ? "\u0422\u0430\u043a\u043e\u0433\u043e \u0431\u043b\u043e\u043a\u0430 \u043d\u0435\u0442!"
                                       : "There is no such block!"
                                 )
                           );
                        }

                        return 1;
                     }
                  )
            )
      );
      builder.then(
         literal("del")
            .then(
               arg("block", SearchArgumentType.create())
                  .executes(
                     context -> {
                        String blockName = (String)context.getArgument("block", String.class);
                        Block result = getRegisteredBlock(blockName);
                        if (result != null) {
                           ModuleManager.nuker.selectedBlocks.getValue().remove(blockName);
                           sendMessage(
                              Formatting.GREEN
                                 + blockName
                                 + (ClientSettings.isRu() ? " \u0443\u0434\u0430\u043b\u0435\u043d \u0438\u0437 Nuker" : " removed from Nuker")
                           );
                        } else {
                           sendMessage(
                              Formatting.RED
                                 + (
                                    ClientSettings.isRu()
                                       ? "\u0422\u0430\u043a\u043e\u0433\u043e \u0431\u043b\u043e\u043a\u0430 \u043d\u0435\u0442!"
                                       : "There is no such block!"
                                 )
                           );
                        }

                        return 1;
                     }
                  )
            )
      );
      builder.executes(context -> {
         if (ModuleManager.nuker.selectedBlocks.getValue().getItemsById().isEmpty()) {
            sendMessage("Nuker list empty");
         } else {
            StringBuilder f = new StringBuilder("Nuker list: ");

            for (String name : ModuleManager.nuker.selectedBlocks.getValue().getItemsById()) {
               try {
                  f.append(name).append(", ");
               } catch (Exception var5) {
               }
            }

            sendMessage(f.toString());
         }

         return 1;
      });
   }

   public static Block getRegisteredBlock(String blockName) {
      for (Block block : Registries.BLOCK) {
         if (block.getTranslationKey().replace("block.minecraft.", "").equalsIgnoreCase(blockName.replace("block.minecraft.", ""))) {
            return block;
         }
      }

      return null;
   }
}
